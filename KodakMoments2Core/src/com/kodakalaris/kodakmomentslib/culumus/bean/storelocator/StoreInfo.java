package com.kodakalaris.kodakmomentslib.culumus.bean.storelocator;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.kodakalaris.kodakmomentslib.culumus.parse.Parse;

public class StoreInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private static final String SELECTED_STORE = "selectedStore";
	public static final String FLAG_STORES = "Stores";
	public static final String FLAG_ID = "Id";
	public static final String FLAG_RETAILER_ID = "RetailerId";
	public static final String FLAG_NAME = "Name";
	public static final String FLAG_ADDRESS = "Address";
	public static final String FLAG_ADD_ADDRESS1 = "Address1";
	public static final String FLAG_ADD_ADDRESS2 = "Address2";
	public static final String FLAG_ADD_ADDRESS3 = "Address3";
	public static final String FLAG_ADD_CITY = "City";
	public static final String FLAG_ADD_STATE_PROVINCE = "StateProvince";
	public static final String FLAG_ADD_POSTAL_CODE = "PostalCode";
	public static final String FLAG_ADD_COUNTRY = "Country";
	public static final String FLAG_PHONE = "Phone";
	public static final String FLAG_EMAIL = "Email";
	public static final String FLAG_LON = "Longitude";
	public static final String FLAG_LAT = "Latitude";
	public static final String FLAG_MILES = "Miles";
	public static final String FLAG_HOURS = "Hours";
	public static final String FLAG_H_DAY = "Day";
	public static final String FLAG_H_OPEN = "Open";
	public static final String FLAG_H_CLOSE = "Close";
	public static final String FLAG_IS_A_TEST_STORE = "IsATestStore";
	
	public static final String TEST_STORE_ON = "TESTSTORES:ON";
    public static final String TEST_STORE_OFF = "TESTSTORES:OFF";
    public static final String IS_TEST_STORE = "is_test_store";
    public static final String IS_WIFI_LOCATOR = "is_wifi_locator";
	private final double MILE_TO_KILOMETER_FACTOR = 1.609344f ;
	
	public String id = "";
	public String retailerID = "";
	public String name = "";
	public StoreAddress address = new StoreAddress();
	public String phone = "";
	public String email = "";
	public double longitude;
	public double latitude;
	public double miles;
	public List<StoreHour> hours = new ArrayList<StoreInfo.StoreHour>();
	public boolean isATestStore = false;
	
	public void saveAsSelectedAtLocal(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString(SELECTED_STORE, convertToStringWithJson().toString()).commit();
		
	}
	
	public static StoreInfo loadSelectedStore(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String strData = prefs.getString(SELECTED_STORE, "");
		try {
			return new Parse().parseStoreInfo(new JSONObject(strData));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void clearSelectedStore(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString(SELECTED_STORE, "").commit();
	}
	
//	public static void clearSelectedStore(Context context){
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//		prefs.edit().putString(SELECTED_STORE, "").commit();
//		prefs.edit().putString(SharedPreferrenceUtil.SELECTED_RETAILER_ID, "").commit();
//	}
	
	public JSONObject convertToStringWithJson(){
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put(FLAG_ID, id);
			jsObj.put(FLAG_RETAILER_ID, retailerID);
			jsObj.put(FLAG_NAME, name);
			jsObj.put(FLAG_ADDRESS, address.convertToStringWithJson());
			jsObj.put(FLAG_PHONE, phone);
			jsObj.put(FLAG_EMAIL, email);
			jsObj.put(FLAG_LON, longitude);
			jsObj.put(FLAG_LAT, latitude);
			jsObj.put(FLAG_MILES, miles);
			JSONArray jsHours = new JSONArray();
			for(StoreHour hour : hours){
				jsHours.put(hour.convertToStringWithJson());
			}
			jsObj.put(FLAG_HOURS, jsHours);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsObj;
	}
	
	public String convertHoursToString(String timeFormat){
		String strHours = "";
		if(hours != null){
			for(StoreHour hour : hours){
				String day = DateUtils.getDayOfWeekString(hour.day+1, DateUtils.LENGTH_MEDIUM) + ": ";
				strHours += day + hour.open + " - " + hour.close + "\n";
			}
		}
		return strHours;
	}
	
	public String convertMilesToString(){
		java.text.DecimalFormat df = new java.text.DecimalFormat("###.##");
		String strMiles = "";
		try {
			strMiles = (df.parse(df.format(Float.valueOf(miles + ""))).floatValue()) + "";
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return strMiles;
	}
	
	public String convertKiloMilesToString(){
		java.text.DecimalFormat df = new java.text.DecimalFormat("###.##");
		String strMiles = "";
		try {
			strMiles = (df.parse(df.format(Float.valueOf(miles + "")*MILE_TO_KILOMETER_FACTOR)).floatValue()) + "";
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return strMiles;
	}
    
    public static class StoreAddress implements Serializable{
    	
    	public String address1 = "";
    	public String address2 = "";
    	public String address3 = "";
    	public String city = "";
    	public String stateProvince = "";
    	public String postalCode = "";
    	public String country = "";
    	
    	protected JSONObject convertToStringWithJson(){
    		JSONObject jsObj = new JSONObject();
    		try {
				jsObj.put(FLAG_ADD_ADDRESS1, address1);
				jsObj.put(FLAG_ADD_ADDRESS2, address2);
				jsObj.put(FLAG_ADD_ADDRESS3, address3);
				jsObj.put(FLAG_ADD_CITY, city);
				jsObj.put(FLAG_ADD_STATE_PROVINCE, stateProvince);
				jsObj.put(FLAG_ADD_POSTAL_CODE, postalCode);
				jsObj.put(FLAG_ADD_COUNTRY, country);
			} catch (JSONException e) {
				e.printStackTrace();
			}
    		return jsObj;
    	}
    }
    
    public static class StoreHour implements Serializable{
    	public int day;
    	public String open = "";
    	public String close = "";
    	
    	protected JSONObject convertToStringWithJson(){
    		JSONObject jsObj = new JSONObject();
    		try {
				jsObj.put(FLAG_H_DAY, day);
				jsObj.put(FLAG_H_OPEN, open);
				jsObj.put(FLAG_H_CLOSE, close);
			} catch (JSONException e) {
				e.printStackTrace();
			}
    		return jsObj;
    	}
    }
}
