package com.kodak.quickbook.database;

import java.io.ByteArrayOutputStream;

import com.kodak.quickbook.database.QuickBook.Mini;
import com.kodak.quickbook.database.QuickBook.Thumbnail;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ThumbnailProvider {
	private static final String TAG = ThumbnailProvider.class.getSimpleName();

	private DBHelper mOpenHelper;
	private static final String DATABASE_NAME = "QuickBook.db";

	private final String [] THUMB_COLUMN = new String [] {Thumbnail.THUMBNAIL_DATA};
	private final String [] MINI_COLUMN = new String[]{Mini.MINI_DATA};

	private final String GET_THUMBNAIL_SELECTION = Thumbnail.PATH + "=" + "?" + ";";
	private final String GET_MINI_SELECTION = Mini.PATH + "=" + "?" + ";";

	private final String [] GET_THUMBNAIL_SELECTION_ARGS = new String [1];
	private final String [] GET_MINI_SELECTION_ARGS = new String [1];
	
	private static ThumbnailProvider sInstance;
	private Bitmap wait_image;

	public void setWait_image(Bitmap wait_image) {
		this.wait_image = wait_image;
	}

	public static ThumbnailProvider obtainInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ThumbnailProvider(context);
		}
		return sInstance;
	}
	
	private ThumbnailProvider(Context context) {
		mOpenHelper = new DBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
    private static final int DATABASE_VERSION = 1;
	private final static String THUMBNAIL_TABLE_NAME = "thumbnails";
	private final static String MINI_TABLE_NAME = "minis";


	public Bitmap getThumbnail(String imageUrl) {
//		Log.e("thumb", "get from database");
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		GET_THUMBNAIL_SELECTION_ARGS[0] = imageUrl;
		Cursor c = db.query(THUMBNAIL_TABLE_NAME, THUMB_COLUMN,
				GET_THUMBNAIL_SELECTION, GET_THUMBNAIL_SELECTION_ARGS, null,
				null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			byte[] data = c.getBlob(0);
			c.close();
			try {
				return BitmapFactory.decodeByteArray(data, 0, data.length);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				return null;
			}
		} else {
			c.close();
			return null;
		}

	}

	public Bitmap getMini(String imageUrl) {	
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		GET_MINI_SELECTION_ARGS[0] = imageUrl;
		Cursor c = null;
		try{
		c = db.query(MINI_TABLE_NAME, MINI_COLUMN,
				GET_MINI_SELECTION, GET_MINI_SELECTION_ARGS, null,
				null, null);
		} catch(Exception iae){
			iae.printStackTrace();
			if(c != null)
				c.close();
			return null;
		}
		if (c.getCount() > 0) {
			c.moveToFirst();
			byte[] data = c.getBlob(0);
			c.close();
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeByteArray(data, 0, data.length, options);
				if(options.outHeight>options.outWidth){
					options.inSampleSize = options.outHeight/400<=0?1:options.outHeight/400;
				} else {
					options.inSampleSize = options.outWidth/400<=0?1:options.outWidth/400;
				}
				options.inJustDecodeBounds = false;
				Log.e("thumb", "get mini from database " + imageUrl + " not null.");
				options.inPreferredConfig = Bitmap.Config.ALPHA_8;      
				options.inPurgeable = true;     
				return BitmapFactory.decodeByteArray(data, 0, data.length, options);
			} catch (OutOfMemoryError e) {
				Log.e("thumb", "get mini from database " + imageUrl + " null");
				e.printStackTrace();
				return wait_image;
			}
		} else {
			c.close();
			Log.e("thumb", "get mini from database " + imageUrl + " null");
			return null;
		}
		
	}

	public void cacheThumbnail(String imageUrl, byte[] data) {
		if (data == null) {
			Log.w(TAG, "Compressed byte array is null when caching thumbnail.");
			return;
		}
		ContentValues values = new ContentValues();
		values.put(Thumbnail.PATH, imageUrl);
		values.put(Thumbnail.THUMBNAIL_DATA, data);
		values.put(Thumbnail.CREATE_TIME, System.currentTimeMillis());
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.insert(THUMBNAIL_TABLE_NAME, null, values);
	}
	
	public void updateMini(String miniId, byte[] data){
		Log.e(TAG, "updateMini.... " + miniId);
		if(data != null){
			ContentValues values = new ContentValues();
			values.put(Mini.PATH, miniId);
			values.put(Mini.MINI_DATA, data);
			values.put(Mini.CREATE_TIME, System.currentTimeMillis());
			
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			db.update(MINI_TABLE_NAME, values, Mini.PATH+"=?",new String[]{miniId});
		}
		
	}
	
	public void clearMiniCach() {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		try{
			db.delete(MINI_TABLE_NAME, null, null);
		} catch(Exception e){
			Log.e(TAG, "Fail to delete mini table database:" + e.getMessage());
		}
	}
	
	public void cacheMini(String imageUrl, byte[] data) throws Exception {
		Log.e(TAG, "cach Mini url: " + imageUrl);
		if (data == null) {
			Log.w(TAG, "Compressed byte array is null when caching mini.");
			return;
		}
		Log.e(TAG, "cacheMini bitmap size: " + data.length);
		ContentValues values = new ContentValues();
		values.put(Mini.PATH, imageUrl);
		values.put(Mini.MINI_DATA, data);
		values.put(Mini.CREATE_TIME, System.currentTimeMillis());
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		try{
			String sql = "DELETE FROM " + MINI_TABLE_NAME + " WHERE " + Mini.PATH + "='" + imageUrl + "'";
			db.execSQL(sql);
		} catch (Exception e) {
			Log.e(TAG, "delete mini result:" + e.getMessage());
		}
		try{
			db.insert(MINI_TABLE_NAME, null, values);
		} catch (Exception e){
			e.printStackTrace();
		}		
		// TODO need to remove just for test
		/*File sdFloder = new File("/sdcard/DMCombinedApp_Minis/");
		if(!sdFloder.exists()){
			sdFloder.mkdirs();
		}
		File f = new File("/sdcard/DMCombinedApp_Minis/" + imageUrl + ".jpg");
		if(f.exists()){
			f.delete();
		}
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(data);
		fos.close();*/
		
	}
	
	private final static String CREATE_THUMB_TABLE = 
			"CREATE TABLE IF NOT EXISTS " + THUMBNAIL_TABLE_NAME + " ( "
			+ Thumbnail.PATH + " TEXT PRIMARY KEY,"
			+ Thumbnail.THUMBNAIL_DATA + " BLOB,"
			+ Thumbnail.CREATE_TIME + " INTEGER"
			+ ");";
	
	private final static String CREATE_MINI_TABLE = 
		"CREATE TABLE IF NOT EXISTS " + MINI_TABLE_NAME + " ( "
		+ Mini.PATH + " TEXT PRIMARY KEY,"
		+ Mini.MINI_DATA + " BLOB,"
		+ Mini.CREATE_TIME + " INTEGER"
		+ ");";
	
	private final static String DROP_MINI_TALBE = "DROP TABLE IF EXISTS " + MINI_TABLE_NAME;

	public void deleteMini(String miniId) {
		Log.e(TAG, "delete Mini url: " + miniId);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		try{
			//deleteInt = db.delete(MINI_TABLE_NAME, Mini.PATH+"=?",new String[]{miniId});
			String sql = "DELETE FROM " + MINI_TABLE_NAME + " WHERE " + Mini.PATH + "='" + miniId + "'";
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteThumbnail(String imageURL) {
//		Log.i("database", "delete image url:" + imageURL);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.delete(THUMBNAIL_TABLE_NAME, Thumbnail.PATH + "= '" + imageURL + "'", null);
	}
	
	public void dropMiniTable() {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.execSQL(DROP_MINI_TALBE);
	}

	public void createMiniTable() {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.execSQL(CREATE_MINI_TABLE);
	}

	
	private class DBHelper extends SQLiteOpenHelper{

		public DBHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_THUMB_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
		
	}

	public static final byte[] compressToByteArray(Bitmap bitmap, int quality) {
//		long t1 = System.currentTimeMillis();
		if (bitmap == null) return null;

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
		if (bitmap.compress(CompressFormat.JPEG, quality, outputStream)) {
			byte [] data = outputStream.toByteArray();
//			long t2 = System.currentTimeMillis();
//			Log.d("compress time", "" + (t2 - t1) + " thumb data length: " + data.length);
			return data;
		}
		return null;
	}
	
}
