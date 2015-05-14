package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import java.io.ByteArrayOutputStream;
import java.util.Currency;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardDatabase.CardPreview;
import com.kodak.quickbook.database.QuickBook.Thumbnail;

public class GreetingCardProvider {
	private final String TAG = GreetingCardProvider.class.getSimpleName();

	private final String DATABASE_NAME = "GreetingCardPreviwe.db";
	private final int DATABASE_VERSION = 1;
	private final String PREVIEW_TABLE_NAME = "card_previews";

	private final String[] PREVIEW_COLUMN = new String[] { CardPreview.PREVIEW_DATA };
	private final String GET_PREVIEW_SELECTION = CardPreview.PATH + "=" + "?"
			+ ";";
	private final String[] GET_PREVIEW_SELECTION_ARGS = new String[1];

	private static GreetingCardProvider provider;
	private DBHelper dbHelper;

	public static GreetingCardProvider getGreetingCardProvoider(Context context) {
		if (provider == null) {
			provider = new GreetingCardProvider(context);
		}
		return provider;
	}

	private GreetingCardProvider(Context context) {
		dbHelper = new DBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void initPreviewDatabase() {
		if (isTableExist(PREVIEW_TABLE_NAME)) {
			clearTable(PREVIEW_TABLE_NAME);
		} else {
			createTable(PREVIEW_TABLE_NAME);
		}
	}

	private void createTable(String tableName) {
		Log.i(TAG, "createTable[" + tableName + "].");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String sql = "";
		if(tableName.equals(PREVIEW_TABLE_NAME)){
			sql = "CREATE TABLE IF NOT EXISTS " + PREVIEW_TABLE_NAME + " ( " + CardPreview.PATH + " TEXT PRIMARY KEY," + CardPreview.PREVIEW_DATA + " BLOB," + CardPreview.CREATE_TIME + " INTEGER" + ");";
		}
		try {
			db.execSQL(sql);
		} catch (SQLiteException e){
			e.printStackTrace();
		}
	}

	private void clearTable(String tableName) {
		Log.i(TAG, "clearTable[" + tableName + "].");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// clear all data from table
		String sql = "delete from " + tableName + ";";
		try {
			db.execSQL(sql);
		} catch (SQLiteException e){
			e.printStackTrace();
		}
		
		// these code below throw exception
		/*// clear the sequence
		sql = "DELETE FROM sqlite_sequence;";
		try {
			db.execSQL(sql);
		} catch (SQLiteException e){
			e.printStackTrace();
		}*/
	}

	private boolean isTableExist(String tableName) {
		boolean result = false;
		if (tableName == null) {
			return false;
		}
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT COUNT(*) AS c FROM SQLITE_MASTER WHERE TYPE='table' AND NAME='" + tableName.trim() + "' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(cursor!=null && !cursor.isClosed()){
				cursor.close();
			}
		} finally {
			if(cursor!=null && !cursor.isClosed()){
				cursor.close();
			}
		}
		return result;
	}

	public void cachPreview(String id, byte[] data) {
		if (data == null) {
			Log.w(TAG, "Compressed byte array is null when caching preview.");
			return;
		}
		ContentValues values = new ContentValues();
		values.put(CardPreview.PATH, id);
		values.put(CardPreview.PREVIEW_DATA, data);
		values.put(CardPreview.CREATE_TIME, System.currentTimeMillis());
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.insert(PREVIEW_TABLE_NAME, null, values);
		} catch(SQLiteException e){
			e.printStackTrace();
		}
		
	}

	public Bitmap getPreview(String id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		GET_PREVIEW_SELECTION_ARGS[0] = id;
		Cursor c = null;
		try{
			c = db.query(PREVIEW_TABLE_NAME, PREVIEW_COLUMN, GET_PREVIEW_SELECTION, GET_PREVIEW_SELECTION_ARGS, null, null, null);
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
				return BitmapFactory.decodeByteArray(data, 0, data.length);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				return null;
			}
		} else {
			c.close();
			//Log.w(TAG, "get preview from database " + id + " null");
			return null;
		}
	}

	public void dropPreviewDatabase() {

	}

	private class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}

	}

	public static final byte[] compressToByteArray(Bitmap bitmap, int quality) {
		if (bitmap == null)
			return null;

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
		if (bitmap.compress(CompressFormat.JPEG, quality, outputStream)) {
			byte[] data = outputStream.toByteArray();
			return data;
		}
		return null;
	}
}
