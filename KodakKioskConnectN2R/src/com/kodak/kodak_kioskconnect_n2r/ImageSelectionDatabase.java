package com.kodak.kodak_kioskconnect_n2r;

import java.util.ArrayList;
import java.util.HashMap;

import com.AppConstants.FlowType;
import com.AppConstants.PhotoSource;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

public class ImageSelectionDatabase
{
	private String TAG = this.getClass().getSimpleName();
	private SQLiteDatabase imageSelectionDatabase = null;
	private ImageSelectionDatabaseHelper dbHelper = null;
	long startTime = 0;
	double endTime = 0;
	private String[] allN2RColumns = { ImageSelectionDatabaseHelper.COLUMN_ID, ImageSelectionDatabaseHelper.COLUMN_URI, ImageSelectionDatabaseHelper.COLUMN_FILENAME, ImageSelectionDatabaseHelper.COLUMN_QUANTITY, ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, ImageSelectionDatabaseHelper.COLUMN_PRINTID, ImageSelectionDatabaseHelper.COLUMN_IMAGEID, ImageSelectionDatabaseHelper.COLUMN_ROIX, ImageSelectionDatabaseHelper.COLUMN_ROIY, ImageSelectionDatabaseHelper.COLUMN_ROIW, ImageSelectionDatabaseHelper.COLUMN_ROIH, ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, ImageSelectionDatabaseHelper.COLUMN_SELECTED };
	private String[] allWIFIColumns = { ImageSelectionDatabaseHelper.COLUMN_ID, ImageSelectionDatabaseHelper.COLUMN_URI, ImageSelectionDatabaseHelper.COLUMN_FILENAME, ImageSelectionDatabaseHelper.COLUMN_SELECTED };
	//modify by song
		private String[] N2ROrderColumns = {
				ImageSelectionDatabaseHelper.COLUMN_ID,
				ImageSelectionDatabaseHelper.COLUMN_ORDEREMAIL,
				ImageSelectionDatabaseHelper.COLUMN_ORDERID,
				ImageSelectionDatabaseHelper.COLUMN_ORDERTIME,
				ImageSelectionDatabaseHelper.COLUMN_ORDERSUBTOTAL,
				ImageSelectionDatabaseHelper.COLUMN_ORDERSTORE,
				ImageSelectionDatabaseHelper.COLUMN_ORDERDETAILS,
				ImageSelectionDatabaseHelper.COLUMN_ORDERSTOREADDRESS,
				ImageSelectionDatabaseHelper.COLUMN_ORDERSTORECITYZIP,
				ImageSelectionDatabaseHelper.COLUMN_ORDERSTOREPHONE,
				ImageSelectionDatabaseHelper.COLUMN_ORDERSTORELATITUDE,
				ImageSelectionDatabaseHelper.COLUMN_ORDERSTORELONGITUDE,
				ImageSelectionDatabaseHelper.COLUMN_ORDERFIRSTNAMESHIP,
				ImageSelectionDatabaseHelper.COLUMN_ORDERLASTNAMESHIP,
				ImageSelectionDatabaseHelper.COLUMN_ORDERADDONESHIPSHIP,
				ImageSelectionDatabaseHelper.COLUMN_ORDERADDTWOSHIPSHIP,
				ImageSelectionDatabaseHelper.COLUMN_ORDERCITYSHIP,
				ImageSelectionDatabaseHelper.COLUMN_ORDERZIPSHIP,
				ImageSelectionDatabaseHelper.COLUMN_ORDERSTATESHIP,
				ImageSelectionDatabaseHelper.COLUMN_ORDERISCALCULATEDSHOW};
	SharedPreferences prefs;
	private Context mContext = null;

	public ImageSelectionDatabase(Context context)
	{
		mContext = context;
		dbHelper = new ImageSelectionDatabaseHelper(context);
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public void open() throws SQLException
	{
		imageSelectionDatabase = dbHelper.getWritableDatabase();
	}

	public void close()
	{
		dbHelper.close();
		imageSelectionDatabase = null;
	}

	// WIFI
	public Boolean handleAddUriWIFI(String uriEncodedPath, String fileName)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered handleAddUriWIFI() uriEncodedPath=" + uriEncodedPath);
		}
		Boolean retVal = false;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			try {
				String selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + fileName.replace("'", "''") + "'";
				Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.WIFI_IMAGES, allWIFIColumns, selection, null, null, null, null);
				if (cursor.getCount() <= 0)
				{
					ContentValues values = new ContentValues();
					values.put(ImageSelectionDatabaseHelper.COLUMN_URI, uriEncodedPath);
					values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, fileName);
					values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, 1);
					long insertId = imageSelectionDatabase.insert(ImageSelectionDatabaseHelper.WIFI_IMAGES, null, values);
					if (insertId != -1)
					{
						if (PrintHelper.mLoggingEnabled)
							Log.d(TAG, "Inserted " + uriEncodedPath + " into database, which returned insertId " + insertId);
						retVal = true;
					}
					if (PrintHelper.mLoggingEnabled)
					{
						printAllSelected();
					}
				}
				cursor.close();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return retVal;
	}

	public Boolean handleAddUrisWIFI(String[] uriEncodedPaths, String[] fileNames)
	{
		Boolean retVal = false;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			imageSelectionDatabase.beginTransaction();
			String selection = "";
			if (PrintHelper.mLoggingEnabled)
			{
				Log.d(TAG, "Entered handleAddUri() uriEncodedPath[]");
			}
			for (int i = 0; i < uriEncodedPaths.length; i++)
			{
				selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + fileNames[i].replace("'", "''") + "'";
				Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.WIFI_IMAGES, allWIFIColumns, selection, null, null, null, null);
				if (cursor.getCount() <= 0)
				{
					ContentValues values = new ContentValues();
					values.put(ImageSelectionDatabaseHelper.COLUMN_URI, uriEncodedPaths[i]);
					values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, fileNames[i]);
					values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, 1);
					long insertId = imageSelectionDatabase.insertWithOnConflict(ImageSelectionDatabaseHelper.WIFI_IMAGES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
					if (insertId != -1)
					{
						if (PrintHelper.mLoggingEnabled)
							Log.d(TAG, "Inserted " + uriEncodedPaths[i] + " into database, which returned insertId " + insertId);
					}
				}
				cursor.close();
			}
			imageSelectionDatabase.setTransactionSuccessful();
			retVal = true;
		}
		imageSelectionDatabase.endTransaction();
		if (PrintHelper.mLoggingEnabled)
		{
			printAllSelected();
		}
		return retVal;
	}

	
	// N2R
	//modify by song add the "orderLatitude, orderLongitude, orderStoreAddress, orderStorePhone"
	//in database
	/*
	 * modify by song
	 * date : 2014-03-21
	 * add the isTaxWillBeCalculatedByRetailer into the database
	 * */
	public void saveOrderDetails(String email, String orderID, String time, String subtotal, String storeName, String details,String orderLatitude,
			String orderLongitude, String orderStoreAddress, String orderStoreCityZip, String orderStorePhone,String shipFirstName,String shipLastName,
			String shipAddress1,String shipAddress2,String shipCity,String shipZip,String stateShip,String isTaxWillBeCalculatedByRetailer)
	{
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			imageSelectionDatabase.beginTransaction();
			if (PrintHelper.mLoggingEnabled)
			{
				Log.d(TAG, "Entered handleAddUri() uriEncodedPath[]");
			}

				Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_ORDERS,N2ROrderColumns, null, null, null, null, null);

				ContentValues values = new ContentValues();
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERID, orderID);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDEREMAIL, email);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERTIME, time);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERSUBTOTAL, subtotal);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERSTORE, storeName);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERSTOREADDRESS, orderStoreAddress);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERSTORECITYZIP, orderStoreCityZip);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERSTOREPHONE, orderStorePhone);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERSTORELATITUDE, orderLatitude);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERSTORELONGITUDE, orderLongitude);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERDETAILS, details);
				
				//add the shipping address information
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERFIRSTNAMESHIP, shipFirstName);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERLASTNAMESHIP, shipLastName);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERADDONESHIPSHIP, shipAddress1);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERADDTWOSHIPSHIP, shipAddress2);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERCITYSHIP, shipCity);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERZIPSHIP, shipZip);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERSTATESHIP, stateShip);
				
				//add the isTaxWillBeCalculatedByRetailer
				values.put(ImageSelectionDatabaseHelper.COLUMN_ORDERISCALCULATEDSHOW, isTaxWillBeCalculatedByRetailer);
				

				long insertId = imageSelectionDatabase.insertWithOnConflict(ImageSelectionDatabaseHelper.N2R_ORDERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
				if (insertId != -1)
				{
					if (PrintHelper.mLoggingEnabled)
						Log.d(TAG, "Inserted " + orderID + " into database, which returned insertId " + insertId);
				}
				
				cursor.close();
			}
			imageSelectionDatabase.setTransactionSuccessful();
		
		imageSelectionDatabase.endTransaction();
	}
	
	public int getNumberSavedOrders()
	{
		int retVal = 0;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_ORDERS, N2ROrderColumns, null, null, null, null, null);
			retVal = cursor.getCount();
			cursor.close();
		}
		return retVal;
	}
	
	
	
	public int getUploadedFiles()
	{
		int retVal = 0;
		String selection = ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED + " = " + "'1'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			retVal = cursor.getCount();
			cursor.close();
		}
		return retVal;
	}

	public Boolean setFileUploaded(String fileName)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered setProductID() filename=" + fileName);
		}
		boolean retVal = false;
		String selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + fileName.replace("'", "''") + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				ContentValues values = new ContentValues();
				values.put(ImageSelectionDatabaseHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRINTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_IMAGEID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIX)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIY)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIH)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIW)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, 1);
				values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, "1");
				imageSelectionDatabase.update(ImageSelectionDatabaseHelper.N2R_IMAGES, values, selection, null);
				retVal = true;
			}
			cursor.close();
		}
		return retVal;
	}

	public Boolean handleAddUris(String[] uriEncodedPaths, String[] fileNames)
	{
		Boolean retVal = false;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			imageSelectionDatabase.beginTransaction();
			String selection = "";
			if (PrintHelper.mLoggingEnabled)
			{
				Log.d(TAG, "Entered handleAddUri() uriEncodedPath[]");
			}
			for (int i = 0; i < uriEncodedPaths.length; i++)
			{
				selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + fileNames[i].replace("'", "''") + "'";
				Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
				if (cursor.getCount() <= 0)
				{
					ContentValues values = new ContentValues();
					values.put(ImageSelectionDatabaseHelper.COLUMN_URI, uriEncodedPaths[i]);
					values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, fileNames[i]);
					values.put(ImageSelectionDatabaseHelper.COLUMN_QUANTITY, prefs.getString("defaultSizeQuantity", "1"));
					values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, prefs.getString("defaultSize", "4x6"));
					values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, -1);
					values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, -1);
					values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, -1);
					values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, -1);
					values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, "-1");
					values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, "-1");
					values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, 0);
					values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, 0);
					long insertId = imageSelectionDatabase.insertWithOnConflict(ImageSelectionDatabaseHelper.N2R_IMAGES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
					if (insertId != -1)
					{
						if (PrintHelper.mLoggingEnabled)
							Log.d(TAG, "Inserted " + uriEncodedPaths[i] + " into database, which returned insertId " + insertId);
					}
				}
				cursor.close();
			}
			imageSelectionDatabase.setTransactionSuccessful();
			retVal = true;
		}
		imageSelectionDatabase.endTransaction();
		if (PrintHelper.mLoggingEnabled)
		{
			printAllSelected();
		}
		return retVal;
	}

	public Boolean handleAddUri(String uriEncodedPath, String fileName)
	{// ),
		// float
		// roiX,
		// float
		// roiY,
		// float
		// roiW,
		// float
		// roiH)
		// {
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered handleAddUri() uriEncodedPath=" + uriEncodedPath);
		}
		Boolean retVal = false;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			String selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + fileName.replace("'", "''") + "'";
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() <= 0)
			{
				ContentValues values = new ContentValues();
				values.put(ImageSelectionDatabaseHelper.COLUMN_URI, uriEncodedPath);
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, fileName);
				values.put(ImageSelectionDatabaseHelper.COLUMN_QUANTITY, prefs.getString("defaultSizeQuantity", "1"));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, prefs.getString("defaultSize", "4x6"));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, -1);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, -1);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, -1);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, -1);
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, "-1");
				values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, "-1");
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, 0);
				values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, 1);
				long insertId = imageSelectionDatabase.insert(ImageSelectionDatabaseHelper.N2R_IMAGES, null, values);
				if (insertId != -1)
				{
					if (PrintHelper.mLoggingEnabled)
						Log.d(TAG, "Inserted " + uriEncodedPath + " into database, which returned insertId " + insertId);
					retVal = true;
				}
				if (PrintHelper.mLoggingEnabled)
				{
					printAllSelected();
				}
			}
			cursor.close();
		}
		return retVal;
	}

	public void checkClosed()
	{
		if (imageSelectionDatabase == null)
		{
			if (PrintHelper.mLoggingEnabled)
			{
				Log.d(TAG, "imageSelectionDatabase == null");
			}
		}
		else
		{
			if (imageSelectionDatabase.isOpen())
			{
				imageSelectionDatabase.close();
			}
		}
	}

	public String getImageID(String filename)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getImageID() filename=" + filename);
		}
		String retVal = "";
		String selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + filename.replace("'", "''") + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				retVal = cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_IMAGEID));
			}
			cursor.close();
		}
		return retVal;
	}

	public boolean setImageID(String filename, String id)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered setProductID() filename=" + filename + " id : " + id);
		}
		boolean retVal = false;
		String selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + filename.replace("'", "''") + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				ContentValues values = new ContentValues();
				values.put(ImageSelectionDatabaseHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRINTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, id);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIX)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIY)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIH)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIW)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_SELECTED)));
				imageSelectionDatabase.update(ImageSelectionDatabaseHelper.N2R_IMAGES, values, selection, null);
				retVal = true;
			}
			cursor.close();
		}
		return retVal;
	}

	public boolean setPrintID(String imageID, String id)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered setProductID() imageID=" + imageID);
		}
		boolean retVal = false;
		String selection = ImageSelectionDatabaseHelper.COLUMN_IMAGEID + " = " + "'" + imageID + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				ContentValues values = new ContentValues();
				values.put(ImageSelectionDatabaseHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, id);
				values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_IMAGEID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIX)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIY)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIH)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIW)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_SELECTED)));
				imageSelectionDatabase.update(ImageSelectionDatabaseHelper.N2R_IMAGES, values, selection, null);
				retVal = true;
			}
			cursor.close();
		}
		return retVal;
	}

	public String getProductType(String filename)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getProductType() filename=" + filename);
		}
		String retVal = "";
		String selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + filename.replace("'", "''") + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				int test = cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID);
				retVal = cursor.getString(test);
			}
			cursor.close();
		}
		return retVal;
	}

	public String getProductId(String filename)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getProductId() filename=" + filename);
		}
		String retVal = "";
		String selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + filename.replace("'", "''") + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				int test = cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRINTID);
				retVal = cursor.getString(test);
			}
			cursor.close();
		}
		return retVal;
	}

	public int getProductQuantity(String uriEncodedPath)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getProductQuantity() uriEncodedPath=" + uriEncodedPath);
		}
		int retVal = 0;
		String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPath + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				int test = cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_QUANTITY);
				retVal = cursor.getInt(test);
			}
			cursor.close();
		}
		return retVal;
	}

	public boolean addPrintId(String filename, String id)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered addPrintId() filename=" + filename + " id= " + id);
		}
		boolean retVal = false;
		String selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + filename.replace("'", "''") + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				ContentValues values = new ContentValues();
				values.put(ImageSelectionDatabaseHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_QUANTITY, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_QUANTITY)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIX)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIY)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIH)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIW)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, id);
				values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_IMAGEID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_SELECTED)));
				imageSelectionDatabase.update(ImageSelectionDatabaseHelper.N2R_IMAGES, values, selection, null);
				retVal = true;
			}
			cursor.close();
		}
		return retVal;
	}

	public String getProductURI(String filename)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getProductURI() filename=" + filename);
		}
		String selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + filename.replace("'", "''") + "'";
		String retVal = "";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				retVal = cursor.getString((cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
			}
			cursor.close();
		}
		return retVal;
	}

	public ROI getProductCrop(String uriEncodedPath)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getProductCrop() uriEncodedPath=" + uriEncodedPath);
		}
		String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPath + "'";
		ROI retVal = new ROI();
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				double x = cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIX));
				double y = cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIY));
				double h = cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIH));
				double w = cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIW));
				retVal.x = x;
				retVal.y = y;
				retVal.w = w;
				retVal.h = h;
			}
			cursor.close();
		}
		return retVal;
	}

	public boolean setProductCrop(String uriEncodedPath, double x, double y, double w, double h)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered setProductCrop() uriEncodedPath=" + uriEncodedPath);
		}
		boolean retVal = false;
		String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPath + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				ContentValues values = new ContentValues();
				values.put(ImageSelectionDatabaseHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRINTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_IMAGEID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, x);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, y);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, h);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, w);
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_SELECTED)));
				imageSelectionDatabase.update(ImageSelectionDatabaseHelper.N2R_IMAGES, values, selection, null);
				retVal = true;
			}
			cursor.close();
		}
		return retVal;
	}

	public boolean setProductQuantity(String uriEncodedPath, String quantity)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered setProductQuantity() uriEncodedPath=" + uriEncodedPath);
		}
		boolean retVal = false;
		String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPath + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				ContentValues values = new ContentValues();
				values.put(ImageSelectionDatabaseHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_QUANTITY, quantity);
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIX)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIY)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIH)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIW)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRINTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_IMAGEID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_SELECTED)));
				imageSelectionDatabase.update(ImageSelectionDatabaseHelper.N2R_IMAGES, values, selection, null);
				retVal = true;
			}
			cursor.close();
		}
		return retVal;
	}

	public Boolean handleSelectUri(String uriEncodedPath)
	{
		Boolean retVal = false;
		String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPath + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				ContentValues values = new ContentValues();
				values.put(ImageSelectionDatabaseHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRINTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_IMAGEID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIX)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIY)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIH)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIW)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, 1);
				imageSelectionDatabase.update(ImageSelectionDatabaseHelper.N2R_IMAGES, values, selection, null);
				retVal = true;
				cursor.close();
			}
		}
		return retVal;
	}

	public Boolean handleSelectAllUris(String[] uriEncodedPaths, String[] fileNames)
	{
		Boolean retVal = false;
		startTime = System.currentTimeMillis();
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			try
			{
				imageSelectionDatabase.beginTransaction();
				ContentValues values = new ContentValues();
				if (PrintHelper.mLoggingEnabled)
				{
					Log.d(TAG, "Entered selectAllUris() uriEncodedPath[]");
				}
				for (int i = 0; i < uriEncodedPaths.length; i++)
				{
					String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPaths[i] + "' AND " + ImageSelectionDatabaseHelper.COLUMN_SELECTED + " = '" + 0 + "'";
					Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
					if (cursor.getCount() > 0)
					{
						cursor.moveToFirst();
						values.put(ImageSelectionDatabaseHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ID)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRINTID)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_IMAGEID)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIX)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIY)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIH)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIW)));
						int uploaded = cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED));
						if (uploaded == 1)
						{
						}
						else
						{
						//	PrintHelper.numToUpload++;
						}
						values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, 1);
					}
					else
					{
						values.put(ImageSelectionDatabaseHelper.COLUMN_URI, uriEncodedPaths[i]);
						values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, fileNames[i]);
						values.put(ImageSelectionDatabaseHelper.COLUMN_QUANTITY, prefs.getString("defaultSizeQuantity", "1"));
						values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, prefs.getString("defaultSize", "4x6"));
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, -1);
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, -1);
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, -1);
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, -1);
						values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, "-1");
						values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, "-1");
						values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, 0);
						values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, 1);
					}
					long insertId = imageSelectionDatabase.insertWithOnConflict(ImageSelectionDatabaseHelper.N2R_IMAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
					if (insertId != -1)
					{
						Log.d(TAG, "Inserted " + uriEncodedPaths[i] + " into database, which returned insertId " + insertId);
					}
					retVal = true;
					cursor.close();
				}
				imageSelectionDatabase.setTransactionSuccessful();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			finally
			{
				imageSelectionDatabase.endTransaction();
				endTime = new Double(System.currentTimeMillis() - startTime) / 1000.0f;
				if (PrintHelper.mLoggingEnabled)
				{
					Log.d(TAG, "Insertion of " + uriEncodedPaths.length + " images took " + endTime + " seconds");
				}
			}
			if (PrintHelper.mLoggingEnabled)
			{
				printAllSelected();
			}
		}
		return retVal;
	}

	public void populatePrintHelper()
	{
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			if (PrintHelper.wifiURIs == null)
			{
				Log.e(TAG, "wifiURIs was null, repopulating it");
				PrintHelper.wifiURIs = new ArrayList<String>();
			}
			if (PrintHelper.selectedHash == null)
			{
				Log.e(TAG, "selected hash was null, do we need to repopulate it all?");
				PrintHelper.selectedHash = new HashMap<String, String>();
			}
			PrintHelper.wifiURIs.clear();
			try
			{
				Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.WIFI_IMAGES, allWIFIColumns, null, null, null, null, null);
				cursor.moveToFirst();
				while (!cursor.isAfterLast())
				{
					String uri = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_URI));
					//The images might be deleted already, so here should have check.
					Uri mUri = Uri.parse(uri);
					Cursor mCursor = mContext.getContentResolver().query(mUri, null, null, null, null);
					if(mCursor.getCount() > 0) {
						PrintHelper.wifiURIs.add(uri);
						PrintHelper.selectedHash.put(uri, "1");
					}
					cursor.moveToNext();
					mCursor.close();
				}
				cursor.close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public Boolean handleDeselectUri(String uriEncodedPath)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered handleDeselectUri() uriEncodedPath=" + uriEncodedPath);
		}
		boolean retVal = false;
		long startTime = System.currentTimeMillis();
		String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPath + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				ContentValues values = new ContentValues();
				values.put(ImageSelectionDatabaseHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRINTID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_IMAGEID)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIX)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIY)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIH)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIW)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED)));
				values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, 0);
				imageSelectionDatabase.update(ImageSelectionDatabaseHelper.N2R_IMAGES, values, selection, null);
				retVal = true;
				endTime = new Double(System.currentTimeMillis() - startTime) / 1000.0f;
				if (PrintHelper.mLoggingEnabled)
				{
					Log.d(TAG, "Deselect All took " + endTime + " seconds");
				}
			}
			cursor.close();
		}
		return retVal;
	}

	public Boolean handleDeselectAllUris(String[] uriEncodedPaths)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered handleDeselectAllUris");
		}
		Boolean retVal = false;
		startTime = System.currentTimeMillis();
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			try
			{
				imageSelectionDatabase.beginTransaction();
				for (int i = 0; i < uriEncodedPaths.length; i++)
				{
					String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPaths[i] + "' AND " + ImageSelectionDatabaseHelper.COLUMN_SELECTED + " = '" + 1 + "'";
					Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
					ContentValues values = new ContentValues();
					if (cursor.getCount() > 0)
					{
						cursor.moveToFirst();
						values.put(ImageSelectionDatabaseHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ID)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRINTID)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_IMAGEID)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIX)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIY)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIH)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIW)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED)));
						values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, 0);
						imageSelectionDatabase.update(ImageSelectionDatabaseHelper.N2R_IMAGES, values, selection, null);
						retVal = true;
						cursor.close();
					}
				}
				imageSelectionDatabase.setTransactionSuccessful();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			finally
			{
				imageSelectionDatabase.endTransaction();
				endTime = new Double(System.currentTimeMillis() - startTime) / 1000.0f;
				if (PrintHelper.mLoggingEnabled)
				{
					Log.d(TAG, "Deselect All took " + endTime + " seconds");
				}
			}
		}
		return retVal;
	}

	public Boolean handleDeselecteAllUris()
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered handleDeselecteAllUris");
		}
		String selection = ImageSelectionDatabaseHelper.COLUMN_SELECTED + " = " + "'" + 1 + "'";
		Boolean retVal = false;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			try
			{
				imageSelectionDatabase.beginTransaction();
				Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
				cursor.moveToFirst();
				while (!cursor.isAfterLast())
				{
					ContentValues values = new ContentValues();
					values.put(ImageSelectionDatabaseHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ID)));
					values.put(ImageSelectionDatabaseHelper.COLUMN_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_URI)));
					values.put(ImageSelectionDatabaseHelper.COLUMN_FILENAME, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME)));
					values.put(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRODUCTID)));
					values.put(ImageSelectionDatabaseHelper.COLUMN_PRINTID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_PRINTID)));
					values.put(ImageSelectionDatabaseHelper.COLUMN_IMAGEID, cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_IMAGEID)));
					values.put(ImageSelectionDatabaseHelper.COLUMN_ROIX, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIX)));
					values.put(ImageSelectionDatabaseHelper.COLUMN_ROIY, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIY)));
					values.put(ImageSelectionDatabaseHelper.COLUMN_ROIH, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIH)));
					values.put(ImageSelectionDatabaseHelper.COLUMN_ROIW, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_ROIW)));
					values.put(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED, cursor.getDouble(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILEUPLOADED)));
					values.put(ImageSelectionDatabaseHelper.COLUMN_SELECTED, 0);
					imageSelectionDatabase.update(ImageSelectionDatabaseHelper.N2R_IMAGES, values, selection, null);
				}
				imageSelectionDatabase.setTransactionSuccessful();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			finally
			{
				imageSelectionDatabase.endTransaction();
				endTime = new Double(System.currentTimeMillis() - startTime) / 1000.0f;
				if (PrintHelper.mLoggingEnabled)
				{
					Log.d(TAG, "Deselect All took " + endTime + " seconds");
				}
			}
		}
		return retVal;
	}

	public Boolean handleDeleteN2RUri(String uriEncodedPath)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered handleDeleteUri() uriEncodedPath=" + uriEncodedPath);
		}
		Boolean retVal = false;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPath + "'";
			int numDeleted = imageSelectionDatabase.delete(ImageSelectionDatabaseHelper.N2R_IMAGES, selection, null);
			if (PrintHelper.mLoggingEnabled)
			{
				Log.d(TAG, "numDeleted=" + numDeleted);
			}
			if (PrintHelper.mLoggingEnabled)
			{
				printAllSelected();
			}
		}
		return retVal;
	}

	public Boolean handleDeleteWifiUri(String uriEncodedPath)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered handleDeleteUri() uriEncodedPath=" + uriEncodedPath);
		}
		Boolean retVal = false;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPath + "'";
			int numDeleted = imageSelectionDatabase.delete(ImageSelectionDatabaseHelper.WIFI_IMAGES, selection, null);
			if (PrintHelper.mLoggingEnabled)
			{
				Log.d(TAG, "numDeleted=" + numDeleted);
			}
			if (PrintHelper.mLoggingEnabled)
			{
				printAllSelected();
			}
		}
		return retVal;
	}

	public Boolean handleDeleteAllUris(String[] uriEncodedPaths)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered handleDeleteAllUris");
		}
		Boolean retVal = false;
		long startTime = System.currentTimeMillis();
		int numDeleted = 0;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			try
			{
				imageSelectionDatabase.beginTransaction();
				for (int i = 0; i < uriEncodedPaths.length; i++)
				{
					String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPaths[i] + "'";
					imageSelectionDatabase.delete(ImageSelectionDatabaseHelper.N2R_IMAGES, selection, null);
				}
				imageSelectionDatabase.setTransactionSuccessful();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			finally
			{
				imageSelectionDatabase.endTransaction();
				endTime = new Double(System.currentTimeMillis() - startTime) / 1000.0f;
				if (PrintHelper.mLoggingEnabled)
				{
					Log.d(TAG, "Deletion all in array " + uriEncodedPaths.length + " images took " + endTime + " seconds");
				}
			}
			if (PrintHelper.mLoggingEnabled)
			{
				Log.d(TAG, "numDeleted=" + numDeleted);
			}
			if (PrintHelper.mLoggingEnabled)
			{
				printAllSelected();
			}
		}
		return retVal;
	}

	public Boolean handleDeleteAllUrisN2R()
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered handleDeleteAllUris");
		}
		Boolean retVal = false;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			int numDeleted = imageSelectionDatabase.delete(ImageSelectionDatabaseHelper.N2R_IMAGES, ImageSelectionDatabaseHelper.COLUMN_URI + " > " + "'-1'", null);
			if (PrintHelper.mLoggingEnabled)
			{
				Log.d(TAG, "numDeleted=" + numDeleted);
			}
			if (PrintHelper.mLoggingEnabled)
			{
				printAllSelected();
			}
		}
		return retVal;
	}

	public Boolean handleDeleteAllUrisWiFi()
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered handleDeleteAllUrisWiFi");
		}
		Boolean retVal = false;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			int numDeleted = imageSelectionDatabase.delete(ImageSelectionDatabaseHelper.WIFI_IMAGES, ImageSelectionDatabaseHelper.COLUMN_URI + " > " + "'-1'", null);
			if (PrintHelper.mLoggingEnabled)
			{
				Log.d(TAG, "numDeleted=" + numDeleted);
			}
			if (PrintHelper.mLoggingEnabled)
			{
				printAllSelected();
			}
		}
		return retVal;
	}

	public String getFileName(String uriEncodedPath)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getFileName() uriEncodedPath=" + uriEncodedPath);
		}
		String retVal = "";
		String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPath + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			cursor.moveToFirst();
			retVal = cursor.getString(cursor.getColumnIndexOrThrow(ImageSelectionDatabaseHelper.COLUMN_FILENAME));
			cursor.close();
		}
		return retVal;
	}

	public Boolean isUriSelected(String uriEncodedPath)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered isUriSelected() uriEncodedPath=" + uriEncodedPath);
		}
		Boolean retVal = false;
		String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPath + "' AND " + ImageSelectionDatabaseHelper.COLUMN_SELECTED + " = '" + 1 + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				retVal = true;
			}
			cursor.close();
		}
		return retVal;
	}

	public Boolean isUriSelectedFilename(String filename)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered isUriSelectedFilename() uriEncodedPath=" + filename);
		}
		Boolean retVal = false;
		String selection = ImageSelectionDatabaseHelper.COLUMN_FILENAME + " = " + "'" + filename + "' AND " + ImageSelectionDatabaseHelper.COLUMN_SELECTED + " = " + "'" + 1 + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			if (cursor.getCount() > 0)
			{
				retVal = true;
			}
			cursor.close();
		}
		return retVal;
	}

	public void printAllSelected()
	{
		/*
		 * if (isLogging()) { Log.d(TAG, "Entered printAllSelected()"); } String
		 * selection = ImageSelectionDatabaseHelper.COLUMN_SELECTED + " = " +
		 * "'" + 1 + "'"; Boolean dbOpen = checkOpen(); if (dbOpen) { Cursor
		 * cursor = imageSelectionDatabase.query(
		 * ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection,
		 * null, null, null, null); cursor.moveToFirst(); while
		 * (!cursor.isAfterLast()) { String id = cursor .getString(cursor
		 * .getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ID)); String uri
		 * = cursor .getString(cursor
		 * .getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_URI)); if
		 * (isLogging()) { Log.d(TAG, "id:" + id + " uri:" + uri); }
		 * cursor.moveToNext(); } cursor.close(); } if (isLogging()) {
		 * Log.d(TAG, "Exiting printAllSelected()"); }
		 */
	}

	public int getNumInstances(String uriEncodedPath)
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getNumInstances() filename=" + uriEncodedPath);
		}
		int retVal = 0;
		String selection = ImageSelectionDatabaseHelper.COLUMN_URI + " = " + "'" + uriEncodedPath + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			retVal = cursor.getCount();
			cursor.close();
		}
		return retVal;
	}

	public ArrayList<String> getAllSelected()
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getAllSelected()");
		}
		ArrayList<String> uriList = new ArrayList<String>();
		String selection = ImageSelectionDatabaseHelper.COLUMN_SELECTED + " = " + "'" + 1 + "'";
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				String id = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ID));
				String uri = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_URI));
				uriList.add(uri);
				if (PrintHelper.mLoggingEnabled)
				{
					Log.d(TAG, "id:" + id + " uri:" + uri);
				}
				cursor.moveToNext();
			}
			cursor.close();
		}
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Exiting getAllSelected()");
		}
		return uriList;
	}

	public int getSelectedCount()
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getSelectedCount()");
		}
		String selection = ImageSelectionDatabaseHelper.COLUMN_SELECTED + " = " + "'" + 1 + "'";
		int retCount = 0;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_IMAGES, allN2RColumns, selection, null, null, null, null);
			retCount = cursor.getCount();
			cursor.close();
		}
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Exiting getSelectedCount() retCount=" + retCount);
		}
		return retCount;
	}

	public boolean isSelectedWiFi()
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getSelectedCount()");
		}
		String selection = ImageSelectionDatabaseHelper.COLUMN_SELECTED + " = " + "'" + 1 + "'";
		int retCount = 0;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.WIFI_IMAGES, allWIFIColumns, selection, null, null, null, null);
			
			//The images might be deleted already, so here should have check.
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				Uri mUri = Uri.parse(cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_URI)));
				Cursor mCursor = mContext.getContentResolver().query(mUri, null, null, null, null);
				if(mCursor.getCount() > 0) {
					retCount++;
				}
				cursor.moveToNext();
				mCursor.close();
				//It will take a long time if we do all the iteration when a lot of pictures were selected. 
				//In our project, what we need is "is there any pictures were selected", so, if we just make sure the number is grater than zero, then we break this iteration.
				if(retCount > 0)
					break;
			}
			cursor.close();
		}
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Exiting getSelectedCount() retCount=" + retCount);
		}
		return retCount>0;
	}
	
	/**
	 * Note: If people delete some image, it may cause the count is not correct
	 * @return
	 */
	public int getSelectedCountWiFi()
	{
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Entered getSelectedCount()");
		}
		String selection = ImageSelectionDatabaseHelper.COLUMN_SELECTED + " = " + "'" + 1 + "'";
		int retCount = 0;
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.WIFI_IMAGES, allWIFIColumns, selection, null, null, null, null);
			retCount = cursor.getCount();
			cursor.close();
		}
		if (PrintHelper.mLoggingEnabled)
		{
			Log.d(TAG, "Exiting getSelectedCount() retCount=" + retCount);
		}
		return retCount;
	}

	private Boolean checkOpen()
	{
		Boolean retVal = false;
		if (imageSelectionDatabase == null)
		{
			if (PrintHelper.mLoggingEnabled)
			{
				Log.d(TAG, "imageSelectionDatabase == null");
			}
			try
			{
				open();
				retVal = true;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			if (imageSelectionDatabase.isOpen())
			{
				retVal = true;
			}
			else
			{
				if (PrintHelper.mLoggingEnabled)
				{
					Log.d(TAG, "imageSelectionDatabase.isOpen() == false");
				}
				try
				{
					open();
					retVal = true;
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		return retVal;
	}

	public void export()
	{
		DatabaseAssistant ass = new DatabaseAssistant(mContext, imageSelectionDatabase);
		ass.exportData();
	}

	public ArrayList<String> getTaggedSetURIs()
	{
		ArrayList<String> taggedSetURIs = null;
		Boolean dbOpen = checkOpen();
		taggedSetURIs = new ArrayList<String>();
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.WIFI_IMAGES, allWIFIColumns, null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				String uri = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_URI));
				//The images might be deleted already, so here should have check.
				Uri mUri = Uri.parse(uri);
				Cursor mCursor = mContext.getContentResolver().query(mUri, null, null, null, null);
				if(mCursor.getCount() > 0) {
					taggedSetURIs.add(uri);
				}
				cursor.moveToNext();
				mCursor.close();
			}
			cursor.close();
		}
		return taggedSetURIs;
	}
	
	public ArrayList<PhotoInfo> getTaggedSetPhotos(){
		ArrayList<PhotoInfo> list = null;
		Boolean dbOpen = checkOpen();
		list = new ArrayList<PhotoInfo>();
		if (dbOpen){
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.WIFI_IMAGES, allWIFIColumns, null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				String uri = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_URI));
				//The images might be deleted already, so here should have check.
				Uri mUri = Uri.parse(uri);
				Cursor mCursor = mContext.getContentResolver().query(mUri, null, null, null, null);
				if(mCursor.moveToNext()) {
					PhotoInfo p = new PhotoInfo();
					p.setLocalUri(uri);
					p.setPhotoSource(PhotoSource.PHONE);
					p.setPhotoId(mCursor.getString(mCursor.getColumnIndex(Images.Media._ID)));
					p.setPhotoPath(mCursor.getString(mCursor.getColumnIndex(Images.Media.DATA)));
					p.setBucketId(mCursor.getString(mCursor.getColumnIndex(Images.Media.BUCKET_ID)));
					p.setBucketName(mCursor.getString(mCursor.getColumnIndex(Images.Media.BUCKET_DISPLAY_NAME)));
					p.setSelected(true) ;
					p.setFlowType(FlowType.WIFI);
					list.add(p);
				}
				cursor.moveToNext();
				mCursor.close();
			}
			cursor.close();
		}
		return list;
	}

	public void populateOrderInfo(String orderID)
	{
		Boolean dbOpen = checkOpen();
		String selection = ImageSelectionDatabaseHelper.COLUMN_ORDERID + " = " + "'" + orderID + "'";
		if (dbOpen)
		{
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_ORDERS, N2ROrderColumns, selection, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{

				PrintHelper.orderDetails = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERDETAILS));
				PrintHelper.orderEmail = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDEREMAIL));
				PrintHelper.totalCost = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERSUBTOTAL));
				PrintHelper.orderStore = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERSTORE));
				cursor.moveToNext();
			}
			cursor.close();
		}
	}

	//modify by song
	public ArrayList<Order> getPreviousOrders(Context context)
	{
		ArrayList<Order> shoppingCart = new ArrayList<Order>();
		Boolean dbOpen = checkOpen();
		if (dbOpen)
		{	
			Cursor cursor = imageSelectionDatabase.query(ImageSelectionDatabaseHelper.N2R_ORDERS, N2ROrderColumns, null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				Order order = new Order();
				order.orderID = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERID));
				order.orderPersonEmail = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDEREMAIL));
				order.orderTime = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERTIME));
				order.orderSubtotal = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERSUBTOTAL));
				order.orderStoreName = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERSTORE));
				order.orderSelectedStoreAddress = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERSTOREADDRESS));
				order.orderSelectedCityAndZip = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERSTORECITYZIP));
				order.orderSelectedStorePhone = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERSTOREPHONE));
				order.orderLatitude = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERSTORELATITUDE));
				order.orderLongitude = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERSTORELONGITUDE));
				order.orderDetails = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERDETAILS));
				
				//get the shipping address information
				order.orderFirstnameShip = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERFIRSTNAMESHIP));
				order.orderLastnameShip = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERLASTNAMESHIP));
				order.orderAddressoneShip = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERADDONESHIPSHIP));
				order.orderAddresstwoShip = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERADDTWOSHIPSHIP));
				order.orderCityShip = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERCITYSHIP));
				order.orderStateShip = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERSTATESHIP));
				order.orderZipShip = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERZIPSHIP));
				
				//get the Calculated string info
				order.isTaxWillBeCalculatedByRetailer = cursor.getString(cursor.getColumnIndex(ImageSelectionDatabaseHelper.COLUMN_ORDERISCALCULATEDSHOW));
				
				shoppingCart.add(order);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return shoppingCart;
	}
	
}
