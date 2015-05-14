package com.kodakalaris.kodakmomentslib.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kodakalaris.kodakmomentslib.db.KioskImageSelectionDatabase.WifiImagesFields;

public class KioskImageSelectionDatabaseHelper extends SQLiteOpenHelper{

	private String TAG = "ImageSelectionDatabaseHelper :";
	
	private static final String DATABASE_NAME = "image_info.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_KIOSKCONNENT = "create table " + WifiImagesFields.WIFI_IMAGES + "( " + WifiImagesFields.COLUMN_ID + " integer primary key autoincrement, " 
	+ WifiImagesFields.COLUMN_URI + " text not null, " + WifiImagesFields.COLUMN_PATH + " text not null, " +  " UNIQUE(" + WifiImagesFields.COLUMN_URI + ") ON CONFLICT IGNORE)";
	
	@Override
	public void onCreate(SQLiteDatabase db) {		
		db.execSQL(DATABASE_KIOSKCONNENT);
	}

	public KioskImageSelectionDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);		
	}

	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		
	}
	
	

	
	

}
