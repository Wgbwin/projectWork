package com.kodak.rss.tablet.db;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.core.util.StringUtils;
import com.kodak.rss.tablet.db.SchemaFieldConstants.WifiImagesFields;

/*
 * Purpose:Store data to database 
 * Author:Bing Wang
 * Created Time:20131105;
 */
public class ImageSelectionDatabase {

	private static String TAG = "ImageSelectionDatabase :";
	private Context mContext;
	private static ImageSelectionDatabaseHelper dbHelper;
	private SharedPreferences prefs;
	
	private String[] allWIFIColumns = {WifiImagesFields.COLUMN_ID, WifiImagesFields.COLUMN_URI, WifiImagesFields.COLUMN_PATH};
	
	private final static ReadWriteLock rwl = new ReentrantReadWriteLock();
	
	public ImageSelectionDatabase(Context context){
		this.mContext = context;
		dbHelper = new ImageSelectionDatabaseHelper(context);
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public static SQLiteDatabase getReadableDatabase() {
		SQLiteDatabase db = null;
		try {
			rwl.readLock().lock();
			db = dbHelper.getReadableDatabase();
		} catch (Exception ex) {
			Log.e(TAG, "error", ex);
		} finally {
			rwl.readLock().unlock();
		}
		return db;
	}
	
	public static SQLiteDatabase getWritableDatabase() {
		SQLiteDatabase db = null;
		try {
			rwl.writeLock().lock();
			db = dbHelper.getWritableDatabase();
		} catch (Exception ex) {
			Log.e(TAG, "error", ex);
		} finally {
			rwl.writeLock().unlock();
		}
		return db;
	}

	public void close(){
		dbHelper.close();		
	}
	
	public static void closeDb(SQLiteDatabase db, Cursor cursor) {
		if (cursor != null) {
			cursor.close();
		}
		if (db != null) {
			db.close();
		}
	}
	
	private static void filterWhereArgs(String... whereArgs) {
		if (whereArgs != null && whereArgs.length > 0) {
			for (int i = 0, j = whereArgs.length; i < j; i++) {
				if (whereArgs[i] == null) {
					whereArgs[i] = "";
				}
			}
		}
	}
	
	public long insert(String table, ContentValues values) {
		long result = 0L;
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			if (db != null) {
				result = db.insert(table, null, values);
			}
		} catch (Exception e) {
			Log.e(TAG, "error",e);
		} finally {
			closeDb(db, null);
		}
		return result;
	}
	
	public boolean update(String table, ContentValues values, String whereClause, String... whereArgs) {
		filterWhereArgs(whereArgs);
		SQLiteDatabase db = getWritableDatabase();
		int affectedRows = 0;
		if (db != null) {
			affectedRows = db.update(table, values, whereClause, whereArgs);
		}
		closeDb(db, null);
		return affectedRows > 0;
	}
		
	// Single insert/update
	public boolean insertOrUpdate(String table, ContentValues values, String whereClause, String... whereArgs) {		
		filterWhereArgs(whereArgs);
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getReadableDatabase();
			if (db != null) {
				cursor = db.query(table, null, whereClause, whereArgs, null, null, null);
				if (cursor != null) {
					int count = cursor.getCount();
					if (count > 0) {
						return update(table, values, whereClause, whereArgs);
					} else {
						return insert(table, values) > 0L;
					}
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "error",ex);
		} finally {			
			closeDb(db, cursor);
		}
		return false;
	}
	
	// batch insert
	public boolean batchInsert(String table, ContentValues[] values) {
		SQLiteDatabase db = null;
		boolean retVal = true;
		try {
			db = getWritableDatabase();
			if (db != null) {
				db.beginTransaction();
				db.delete(table, null, null);				
				for (int i = 0; i < values.length; i++) {
					db.insert(table, null,  values[i]);					
				}
				db.setTransactionSuccessful();				
			}			
		} catch (Exception ex) {			
			retVal = false;
			Log.e(TAG, "error",ex);
		} finally {
			db.endTransaction();
			closeDb(db, null);
		}
		return retVal;
	}
		
	//delete
	public boolean delete(String table, String whereClause, String... whereArgs) {
		boolean result = true;
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			if (db != null) {
				filterWhereArgs(whereArgs);
				result = db.delete(table, whereClause, whereArgs) > 0;
			}
		} catch (SQLException e) {
			result = false;
			Log.e(TAG, "database error", e);
		} catch (Exception e) {
			result = false;
			Log.e(TAG, "database error", e);
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return result;
	}
	
	//getCurrentDataBaseVersion
	public static int getCurrentDatabaseVersion() {
		int version = 1;
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			String sql = "select * from data_base_version";
			db = getReadableDatabase();
			if (db != null) {
				cursor = db.rawQuery(sql, null);
				int culIndex = cursor.getColumnIndex("version_num");
				if (cursor != null && cursor.moveToFirst()) {
					version = cursor.getInt(culIndex);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "database error", e);
		} finally {
			closeDb(db, cursor);
		}
		return version;
	}

    //For update the database
	public static boolean updateDatabaseVersion(DatabaseVersion dv) {
		SQLiteDatabase db = null;		
		if (StringUtils.isEmpty(dv.getSql())) {
			return false;
		}

		boolean result = true;
		try {
			db = getWritableDatabase();
			if (db != null) {
				db.beginTransaction();
				String[] sqls = dv.getSql().split(";");
				for (String execSql : sqls) {
					execSql = execSql.replace("\n", " ").trim();
					if (execSql.equals("")) {
						continue;
					}
					db.execSQL(execSql);
				}				
				db.execSQL("update data_base_version set version_num = ?", new Object[] { dv.getVersionNum() });
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			Log.e(TAG, "error", e);
		} finally {
			if (db != null) {
				db.endTransaction();
				db.close();
			}
		}
		return result;
	}
	
	
	//WIFI Images query uri
	public ArrayList<String> getTaggedSetURIs(){
		ArrayList<String> taggedSetURIs = null;
		SQLiteDatabase db = null; 
		Cursor cursor = null;		
		try {
			db = getReadableDatabase();			
		    if (db != null ){
		    	taggedSetURIs = new ArrayList<String>();
				cursor = db.query(WifiImagesFields.WIFI_IMAGES, allWIFIColumns, null, null, null, null, null);
				cursor.moveToFirst();
				while (!cursor.isAfterLast()){
					String uri = cursor.getString(cursor.getColumnIndex(WifiImagesFields.COLUMN_URI)).trim();
					Uri mUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, uri);
															
					taggedSetURIs.add(mUri.toString());					
					cursor.moveToNext();					
				}				
		    }
		} catch (Exception ex) {
			Log.e(TAG, "error", ex);
		} finally {
			closeDb(db, cursor);
		}    
		return taggedSetURIs;
	}
	
	
	public SortableHashMap<Integer, String> getTaggedSetMap(){
		SortableHashMap<Integer, String> kioskchosenpics = null;
		SQLiteDatabase db = null; 
		Cursor cursor = null;		
		try {
			db = getReadableDatabase();			
		    if (db != null ){
		    	kioskchosenpics = new SortableHashMap<Integer, String>();
				cursor = db.query(WifiImagesFields.WIFI_IMAGES, allWIFIColumns, null, null, null, null, null);
				cursor.moveToFirst();
				while (!cursor.isAfterLast()){					 				
					String UriId = cursor.getString(cursor.getColumnIndex(WifiImagesFields.COLUMN_URI)).trim();         								
					String picPath = cursor.getString(cursor.getColumnIndex(WifiImagesFields.COLUMN_PATH)).trim();						
					kioskchosenpics.put(Integer.valueOf(UriId), picPath);					
					cursor.moveToNext();					
				}				
		    }
		} catch (Exception ex) {
			Log.e(TAG, "error", ex);
		} finally {
			closeDb(db, cursor);
		}    
		return kioskchosenpics;
	}
	
	
	public boolean insertOrUpdateUriWIFI(String uri, String path){		
		boolean retVal = false;		
		ContentValues values = new ContentValues();
		values.put(WifiImagesFields.COLUMN_URI, uri);
		values.put(WifiImagesFields.COLUMN_PATH, path);			
		String whereClause = WifiImagesFields.COLUMN_URI + " =?" ;	
		retVal = insertOrUpdate(WifiImagesFields.WIFI_IMAGES, values, whereClause, uri);	
		return retVal;
	}
	
	public boolean batchInsertOrUpdateUriWIFI(SortableHashMap<Integer, String> kioskchosenpics){
		boolean retVal = false;		
		int size = kioskchosenpics.size();
		ContentValues[] values = new ContentValues[size];	
		for (int i = 0; i < size ; i++) {
			ContentValues value = new ContentValues();
			value.put(WifiImagesFields.COLUMN_URI, String.valueOf(kioskchosenpics.keyAt(i)));
			value.put(WifiImagesFields.COLUMN_PATH, kioskchosenpics.valueAt(i));
            values[i]= value;												
		}		
		retVal = batchInsert(WifiImagesFields.WIFI_IMAGES, values);	
		return retVal;
	}
		
	public boolean deleteSingleUriWIFI(String uri){		
		boolean retVal = false;				
		String whereClause = WifiImagesFields.COLUMN_URI + " = ?";		
		retVal = delete(WifiImagesFields.WIFI_IMAGES, whereClause, uri);	
		return retVal;
	}
	
	// delete all
	public boolean handleDeleteAllUrisWiFi(){		
		boolean retVal = false;
		retVal = delete(WifiImagesFields.WIFI_IMAGES, WifiImagesFields.COLUMN_URI + " > ?" , "'-1'");					
		return retVal;
	}
	
	
}
