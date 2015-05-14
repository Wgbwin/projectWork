package com.kodakalaris.kodakmomentslib.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.kodakalaris.kodakmomentslib.R;

/**
 * @Purpose: Used for 
 * @Author: Kane
 * @CreateTime: Sep 16th, 2013 10:42:35 AM
 */
public class SharedPreferrenceUtil {
	
	/**
	 * used in main page
	 */
	public static final String SBSAVE_HELPFUL_HELP = "SBSAVE_HELPFUL_HELP"; /* user guide. */
	public static final String SBELUA_ACCEPTED = "SBELUA_ACCEPTED"; /* elua dialog */
	public static final String SBPRIVACY_ACCEPTED_TORUN = "SBPRIVACY_ACCEPTED_TORUN"; /* privacy dilog. */
	public static final String NEED_SHOW_CELLULAR_WARNING = "needShowCellularDataWarning";
	
	public static final String CURRENT_COUNTRY_CODE = "currentCountryCode";
	public static final String SELECTED_COUNTRY_CODE = "selectedCountryCode";
	public static final String AUTHORIZATION_TOKEN = "Authorization_token";
	
	public static final String BACK_DOOR_NAME = "backDoorName";
	
	public static final String ACCESS_TOKEN = "access_token";
	public static final String ACCESS_EXPIRES = "access_expires";
	public static final String FACEBOOK_USER_ID = "facebookUserId";
	public static final String FACEBOOK_FIRST_NAME = "facebookFirstName";
	public static final String FACEBOOK_LAST_NAME = "facebookLastName";
	
	// Retailer
	public static final String SELECTED_RETAILER_ID = "selected_retailer_id";
	public static final String ACCEPT_CLOLITE = "accept_clolite";
	
	// Application
	public static final String TRACKING_ENABLED = "tracking_enabled";
	public static final String CDG360_DESIRED = "cdg360_desired";
	
	public static final String IS_FIRST_TIME_START = "isFirstTimeStart"; 
	
	
	public static void saveCurrentCountryCode(Context context, String currentCountryCode){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putString(CURRENT_COUNTRY_CODE, currentCountryCode).commit();
	}
	
	public static String currentCountryCode(Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(CURRENT_COUNTRY_CODE, "");
	}
	
	public static void saveSelectedCountryCode(Context context, String selectedCountryCode){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putString(SELECTED_COUNTRY_CODE, selectedCountryCode).commit();
	}
	
	public static String selectedCountryCode(Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(SELECTED_COUNTRY_CODE, "");
	}
	
	public static void saveAuthorizationToken(Context context, String token){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putString(AUTHORIZATION_TOKEN, token).commit();
	}
	
	public static String authorizationToken(Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(AUTHORIZATION_TOKEN, "");
	}
	
	/* add setter and getter for the boolean object. */
	public static boolean getBoolean(Context context, String key){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		if(key.equals(TRACKING_ENABLED)){
			String[] disabledCountries = context.getResources().getStringArray(R.array.googleAnalyticCountryDisableList);
			String currentCountry = currentCountryCode(context);
			if(!currentCountry.equals("") && disabledCountries.length>0){
				for(String country : disabledCountries){
					if(country.equalsIgnoreCase(currentCountry)){
						return sp.getBoolean(key, false);
					}
				}
			}
			return sp.getBoolean(key, true);
		}
		if(key.equals(CDG360_DESIRED)){
			return sp.getBoolean(CDG360_DESIRED, context.getResources().getBoolean(R.bool.show_CDG360_Option));
		}
		return sp.getBoolean(key, false);
	}
	public static void setBoolean(Context context, String key, boolean value){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putBoolean(key, value).commit();
	}
	
	public static String getString(Context context, String key){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(key, "");
	}
	
	public static void setString(Context context, String key, String value){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putString(key, value).commit();
	}
	
	public static void saveFacebookToken(Context context, String accessToken){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putString(ACCESS_TOKEN, accessToken).commit();
	}
	
	public static String getFacebookToken(Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(ACCESS_TOKEN, null);		
	}
	
	public static void saveFacebookAccessExpires(Context context, long accessExpires){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putLong(ACCESS_EXPIRES, accessExpires).commit();
	}
	
	public static long getFacebookAccessExpires(Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getLong(ACCESS_EXPIRES, 0);		
	}
	
	public static void saveFacebookUserId(Context context, String facebookUserId){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putString(FACEBOOK_USER_ID, facebookUserId).commit();
	}
	
	public static String getFacebookUserId(Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(FACEBOOK_USER_ID, "");
		
	}
	
	public static void saveFacebookFristName(Context context, String facebookFirstName){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putString(FACEBOOK_FIRST_NAME, facebookFirstName).commit();
	}
	
	public static String getFacebookFristName(Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(FACEBOOK_FIRST_NAME, "");
		
	}
	
	public static void saveFacebookLastName(Context context, String facebookLastName){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putString(FACEBOOK_LAST_NAME, facebookLastName).commit();
	}
	
	public static String getFacebookLastName(Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(FACEBOOK_LAST_NAME, "");		
	}
	
	public static boolean isNeedShowCellularDataWarning(Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getBoolean(NEED_SHOW_CELLULAR_WARNING, true);
	}
	
	public static void saveNeedShowCellularDataWarning(Context context, boolean needShowCellularDataWarning){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putBoolean(NEED_SHOW_CELLULAR_WARNING, needShowCellularDataWarning).commit();
	}
}
