package com.kodak.rss.core.util;

import java.util.HashMap;

import com.kodak.rss.mobile.R;
import com.localytics.android.LocalyticsAmpSession;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

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
			localyticsSession.registerPush("632750380104");
			localyticsSession.setSessionExpiration(SESSION_TIME_OUT);			
		}
	}
	
	public static void openLocalyticsSession(Context context){
		init(context);
		localyticsSession.open();
	}
	
	/**
	 * this function must be call when the Activity onCreate;
	 * @see {@link LocalyticsAmpSession#open()} and {@link LocalyticsAmpSession#upload()}
	 * @param context
	 */
	public static void onActivityCreate(Activity context){
		if(isAppUsesLocalytics(context)){
			localyticsSession.open();
			localyticsSession.handlePushReceived(context.getIntent());
			localyticsSession.upload();
		}
	}

	/**
	 * this function must be call when the Activity onResume;
	 * @see {@link LocalyticsAmpSession#open()}
	 * @param context
	 */
	public static void onActivityResume(Activity context){
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
		
		if(localyticsSession!=null){
			localyticsSession.setPushDisabled(true);
		}
	}
	
	public static void openLocalytics(Context context){
		
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
		if(pres.getBoolean("localytics", context.getResources().getBoolean(R.bool.localyticsEnabled))){
			init(context);
			return true;
		}
		return false;
	}
}
