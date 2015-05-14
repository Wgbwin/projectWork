package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ImageView;

public class ImageSelectionDatabaseHelper extends SQLiteOpenHelper
{
	public static final String N2R_IMAGES = "n2r";
	public static final String WIFI_IMAGES = "wifi";
	public static final String UPLOAD_IMAGES = "upload";
	public static final String N2R_ORDERS = "orders";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_URI = "_uri";
	public static final String COLUMN_FILENAME = "_filename";
	public static final String COLUMN_QUANTITY = "_quantity";
	public static final String COLUMN_PRODUCTID = "_productid";
	public static final String COLUMN_ROIX = "_roix";
	public static final String COLUMN_ROIY = "_roiy";
	public static final String COLUMN_ROIW = "_roiw";
	public static final String COLUMN_ROIH = "_roih";
	public static final String COLUMN_PRINTID = "_printid";
	public static final String COLUMN_IMAGEID = "_imageid";
	public static final String COLUMN_FILEUPLOADED = "_fileuploaded";
	public static final String COLUMN_SELECTED = "_selected";
	private static final String DATABASE_NAME = "image_info.db";
	public static final String COLUMN_ORDERID = "_orderid";
	public static final String COLUMN_ORDEREMAIL = "_orderemail";
	public static final String COLUMN_ORDERTIME = "_ordertime";
	public static final String COLUMN_ORDERSUBTOTAL = "_ordersubtotal";
	public static final String COLUMN_ORDERDETAILS = "_orderdetails";
	public static final String COLUMN_ORDERSTORE = "_orderstore";
	public static final String COLUMN_ORDERSTOREADDRESS = "_orderstoreaddress";//add by song
	public static final String COLUMN_ORDERSTORECITYZIP = "_orderstorecityzip";//added by fancy
	public static final String COLUMN_ORDERSTOREPHONE = "_orderstorephone";//add by song
	public static final String COLUMN_ORDERSTORELATITUDE = "_orderstorelatitude";  //add by song
	public static final String COLUMN_ORDERSTORELONGITUDE = "_orderstorelongitude";//add by song
	
	/*
	 *2013.11.18
	 * by song
	 * alter the table  N2R_ORDERS. add the shipAddress information into the table.
	 * update the database version from 2 to 3.
	 * */
	
	/*
	 * 2014.3.24
	 * by song
	 * add the _orderiscalculatedshow information into the N2R_ORDERS table.
	 * update the database version from 3 to 4.
	 * */
	public static final String COLUMN_ORDERFIRSTNAMESHIP = "_orderfirstnameship";
	public static final String COLUMN_ORDERLASTNAMESHIP = "_orderlastnameship";
	public static final String COLUMN_ORDERADDONESHIPSHIP = "_orderaddressoneship";
	public static final String COLUMN_ORDERADDTWOSHIPSHIP = "_orderaddresstwoship";
	public static final String COLUMN_ORDERCITYSHIP = "_ordercityship";
	public static final String COLUMN_ORDERZIPSHIP = "_orderzipship";
	public static final String COLUMN_ORDERSTATESHIP = "_orderstateship";
	public static final String COLUMN_ORDERISCALCULATEDSHOW = "_orderiscalculatedshow";
	private static final int DATABASE_VERSION = 4;
	private final String TAG = this.getClass().getSimpleName();
	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table " + N2R_IMAGES + "( " + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_URI + " text not null, " + COLUMN_FILENAME + " text not null, " + COLUMN_QUANTITY + " integer, " + COLUMN_PRODUCTID + " text not null, " + COLUMN_ROIX + " decimal(2,5), " + COLUMN_ROIY + " decimal(2,5), " + COLUMN_ROIW + " decimal(2,5), " + COLUMN_ROIH + " decimal(2,5), " + COLUMN_PRINTID + " text not null, " + COLUMN_IMAGEID + " text not null," + COLUMN_FILEUPLOADED + " boolean," + COLUMN_SELECTED + " boolean," + " UNIQUE(" + COLUMN_URI + ") ON CONFLICT IGNORE)";
	// Database creation sql statement
	private static final String DATABASE_CREATE_WIFI = "create table " + WIFI_IMAGES + "( " + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_URI + " text not null, " + COLUMN_FILENAME + " text not null, " + COLUMN_SELECTED + " boolean," + " UNIQUE(" + COLUMN_URI + ") ON CONFLICT IGNORE)";

	private static final String DATABASE_CREATE_UPLOAD = "create table " + UPLOAD_IMAGES + "( " + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_URI + " text not null, " + COLUMN_FILENAME + " text not null," + COLUMN_FILEUPLOADED + " boolean," + " UNIQUE(" + COLUMN_URI + ") ON CONFLICT IGNORE)";
	
	private static final String DATABASE_CREATE_ORDERS = "create table "
			+ N2R_ORDERS + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_ORDEREMAIL
			+ " text not null," + COLUMN_ORDERID + " text not null,"
			+ COLUMN_ORDERTIME + " text not null," + COLUMN_ORDERSUBTOTAL
			+ " text not null," + COLUMN_ORDERSTORE + " text not null,"
			+ COLUMN_ORDERSTORELATITUDE + " text not null,"
			+ COLUMN_ORDERSTOREADDRESS + " text not null,"
			+ COLUMN_ORDERSTORECITYZIP + " text not null,"
			+ COLUMN_ORDERSTOREPHONE + " text not null,"
			+ COLUMN_ORDERSTORELONGITUDE + " text not null,"
			+ COLUMN_ORDERDETAILS + " text not null,"
			
			+ COLUMN_ORDERFIRSTNAMESHIP + " text not null,"
			+ COLUMN_ORDERLASTNAMESHIP + " text not null,"
			+ COLUMN_ORDERADDONESHIPSHIP + " text not null,"
			+ COLUMN_ORDERADDTWOSHIPSHIP + " text not null,"
			+ COLUMN_ORDERCITYSHIP + " text not null,"
			+ COLUMN_ORDERZIPSHIP + " text not null,"
			+ COLUMN_ORDERSTATESHIP + " text not null,"
			+ COLUMN_ORDERISCALCULATEDSHOW + " text not null)";	
	public ImageSelectionDatabaseHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		if (PrintHelper.mLoggingEnabled)
			Log.d(TAG, "onCreate()");
		database.execSQL(DATABASE_CREATE);
		database.execSQL(DATABASE_CREATE_WIFI);
		database.execSQL(DATABASE_CREATE_UPLOAD);
		database.execSQL(DATABASE_CREATE_ORDERS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		if(oldVersion == 1 && newVersion == 2){
			checkTable(db);
			addColumFist(db);
		}else if (oldVersion == 1 && newVersion == 3){
			checkTable(db);
			addColumFist(db);
			addColumSecond(db);
		}else if (oldVersion ==1 && newVersion == 4){
			checkTable(db);
			addColumFist(db);
			addColumSecond(db);
		}else if (oldVersion == 2 && newVersion == 3){//add shipping address information 
			checkTable(db);
			addColumSecond(db);
		}else if (oldVersion == 2 && newVersion == 4){//add shipping address information 
			checkTable(db);
			addColumSecond(db);
			addColumThird(db);
		}else if (oldVersion == 3 && newVersion == 4){//add shipping address information 
			checkTable(db);
			addColumThird(db);
		}
	}
	
	private void checkTable (SQLiteDatabase db){
		String sql = "";
		sql = "create table IF NOT EXISTS " + N2R_IMAGES + "( " + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_URI + " text not null, " + COLUMN_FILENAME + " text not null, " + COLUMN_QUANTITY + " integer, " + COLUMN_PRODUCTID + " text not null, " + COLUMN_ROIX + " decimal(2,5), " + COLUMN_ROIY + " decimal(2,5), " + COLUMN_ROIW + " decimal(2,5), " + COLUMN_ROIH + " decimal(2,5), " + COLUMN_PRINTID + " text not null, " + COLUMN_IMAGEID + " text not null," + COLUMN_FILEUPLOADED + " boolean," + COLUMN_SELECTED + " boolean," + " UNIQUE(" + COLUMN_URI + ") ON CONFLICT IGNORE)";
		db.execSQL(sql);
		sql = "create table IF NOT EXISTS " + WIFI_IMAGES + "( " + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_URI + " text not null, " + COLUMN_FILENAME + " text not null, " + COLUMN_SELECTED + " boolean," + " UNIQUE(" + COLUMN_URI + ") ON CONFLICT IGNORE)";
		db.execSQL(sql);
		sql = "create table IF NOT EXISTS " + UPLOAD_IMAGES + "( " + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_URI + " text not null, " + COLUMN_FILENAME + " text not null," + COLUMN_FILEUPLOADED + " boolean," + " UNIQUE(" + COLUMN_URI + ") ON CONFLICT IGNORE)";
		db.execSQL(sql);
		sql = "create table  IF NOT EXISTS " + N2R_ORDERS +"( " + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_ORDEREMAIL + " text not null," + COLUMN_ORDERID + " text not null," + COLUMN_ORDERTIME + " text not null," + COLUMN_ORDERSUBTOTAL + " text not null," + COLUMN_ORDERSTORE + " text not null," + COLUMN_ORDERDETAILS + " text not null)";
		db.execSQL(sql);
	}
	
	private void addColumFist(SQLiteDatabase db){
		String sql = "";
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERSTORELATITUDE +"] nvarchar(300)";
		db.execSQL(sql);
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERSTOREADDRESS +"] nvarchar(300)";
		db.execSQL(sql);
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERSTORECITYZIP +"] nvarchar(300)";
		db.execSQL(sql);
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERSTOREPHONE +"] nvarchar(300)";
		db.execSQL(sql);
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERSTORELONGITUDE +"] nvarchar(300)";
		db.execSQL(sql);
	}
	
	//add shipping address information 
	private void addColumSecond(SQLiteDatabase db){
		String sql = "";
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERFIRSTNAMESHIP +"] nvarchar(300)";
		db.execSQL(sql);
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERLASTNAMESHIP +"] nvarchar(300)";
		db.execSQL(sql);
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERADDONESHIPSHIP +"] nvarchar(300)";
		db.execSQL(sql);
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERADDTWOSHIPSHIP +"] nvarchar(300)";
		db.execSQL(sql);
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERCITYSHIP +"] nvarchar(300)";
		db.execSQL(sql);
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERZIPSHIP +"] nvarchar(300)";
		db.execSQL(sql);
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERSTATESHIP +"] nvarchar(300)";
		db.execSQL(sql);
	}
	
	//add is calculated show information 
	private void addColumThird(SQLiteDatabase db){
		String sql = "";
		sql = "alter table ["+N2R_ORDERS+ "] add ["+COLUMN_ORDERISCALCULATEDSHOW +"] nvarchar(300)";
		db.execSQL(sql);
	}
	
}
