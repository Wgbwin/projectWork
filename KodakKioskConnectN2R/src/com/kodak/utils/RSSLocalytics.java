package com.kodak.utils;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.AppConstants;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.localytics.android.LocalyticsAmpSession;

/**
 * @author Kane Jin
 *
 */
public class RSSLocalytics {
	
	private static LocalyticsAmpSession localyticsSession;
	// session time out: 6 minutes
	private static final int SESSION_TIME_OUT = 6 * 60 * 1000;
		
	/**
	* initial the {@link LocalyticsAmpSession}.
	* @param context
	*/
	private static void init(Context context){
		if(localyticsSession == null){
			localyticsSession = new LocalyticsAmpSession(context.getApplicationContext());
			String packName = context.getPackageName();
			String projectNumber = "";
			if(packName.contains("com.kodak.kodak.rsscombinedapp")){
				projectNumber = "1095939300560";
			}else if(packName.contains("com.kodak.kodakprintmaker")){
				projectNumber = "378375119160";
			}
			localyticsSession.registerPush(projectNumber);//1095939300560
			localyticsSession.setSessionExpiration(SESSION_TIME_OUT);	
			
		}		
	}

	/**
	 * this function must be call when the Activity onCreate;
	 * @see {@link LocalyticsAmpSession#open()} and {@link LocalyticsAmpSession#upload()}
	 * @param context
	 */
	public static void onActivityCreate(Context context){
		if(isAppUsesLocalytics(context)){
			localyticsSession.open();
			localyticsSession.upload();
		}
	}
	
	public static void onActivityCreateForPush(Activity context){
		if(isAppUsesLocalytics(context)){
			localyticsSession.open();
			localyticsSession.handlePushReceived(context.getIntent());
			localyticsSession.upload();
		}
	}
	
	public static void onActivityResumeForPush(Activity context){
		if(isAppUsesLocalytics(context)){
			localyticsSession.open();
			localyticsSession.handlePushReceived(context.getIntent());
			try {
				localyticsSession.attach((FragmentActivity) context);
			} catch(Exception e){
				
			}
		}
	}
	
	/**
	 * this function must be call when the Activity onResume;
	 * @see {@link LocalyticsAmpSession#open()}
	 * @param context
	 */
	public static void onActivityResume(Context context){
		if(isAppUsesLocalytics(context)){
			localyticsSession.open();
			try {
				localyticsSession.attach((FragmentActivity) context);
			} catch(Exception e){
				
			}
		}
	}
		
	/**
	 * this function must be call when the Activity onPause;
	 * @see {@link LocalyticsAmpSession#close()}
	 * @param context
	 */
	public static void onActivityPause(Context context){
		if(isAppUsesLocalytics(context)){
			try {
				localyticsSession.detach();		
			} catch(Exception e){
				
			}
			localyticsSession.close();	
		}
	}
	
	/**
	 * Track events.
	 * @see {@link #recordLocalyticsEvents(Context, String, HashMap)}}
	 * @param context
	 * @param event
	 */
	public static void recordLocalyticsEvents(Context context, String event){
		recordLocalyticsEvents(context, event, null);
	}
	
	/**
	 * Track events and actions.
	 * @see {@link LocalyticsAmpSession#tagEvent(String)} and {@link LocalyticsAmpSession#tagEvent(String, Map)}
	 * @param context
	 * @param event
	 * @param attributes
	 */
	public static void recordLocalyticsEvents(Context context, String event, HashMap<String, String> attributes){
		if(isAppUsesLocalytics(context)){			
			if(attributes==null || attributes.size()==0){
				localyticsSession.tagEvent(event);
			} else {
				localyticsSession.tagEvent(event, attributes);
			}
		}
	}
	
	
	/**
	 * Track events and actions.And Ignore the switch "KEY_LOCALYTICS" in sharedPreference
	 * @see {@link LocalyticsAmpSession#tagEvent(String)} and {@link LocalyticsAmpSession#tagEvent(String, Map)}
	 * @param context
	 * @param event
	 * @param attributes
	 */
	public static void recordLocalyticsEventsIgnoreTheSwitch(Context context, String event, HashMap<String, String> attributes){
		init(context);
		localyticsSession.open();
		localyticsSession.upload();
		if(attributes==null || attributes.size()==0){
			localyticsSession.tagEvent(event);
		} else {
			localyticsSession.tagEvent(event, attributes);
		}
	}
	
	/**
	 * Track screens that user have switched to.
	 * @see {@link LocalyticsAmpSession#tagScreen(String)}
	 * @param context
	 * @param pageViewName
	 */
	public static void recordLocalyticsPageView(Context context, String pageViewName){
		if(isAppUsesLocalytics(context)){			
			localyticsSession.tagScreen(pageViewName);
			localyticsSession.upload();
		}
	}
	
	public static void closeLocalytics(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putBoolean(AppConstants.KEY_LOCALYTICS, false).commit();
		if(localyticsSession!=null){
			localyticsSession.setPushDisabled(true);
		}
	}
	
	public static void openLocalytics(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putBoolean(AppConstants.KEY_LOCALYTICS, true).commit();
		if(localyticsSession!=null){
			localyticsSession.setPushDisabled(false);
		}
		
	}
	/**
	 * Check if user want us to track his/her actions.
	 * @param context
	 * @return
	 * 		true is accept, otherwise no.
	 */
	private static boolean isAppUsesLocalytics(Context context){
		SharedPreferences pres = PreferenceManager.getDefaultSharedPreferences(context);
		if(pres.getBoolean(AppConstants.KEY_LOCALYTICS, context.getResources().getBoolean(R.bool.localytics))){
			init(context);
			return true;
		}
		return false;
	}

}
