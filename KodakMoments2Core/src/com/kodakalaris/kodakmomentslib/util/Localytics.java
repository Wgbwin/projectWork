package com.kodakalaris.kodakmomentslib.util;

import java.util.HashMap;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.R;
import com.localytics.android.LocalyticsAmpSession;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

public class Localytics {
	public static final String  PAGE_VIEW_SELECT_WORKFLOW = "Workflow Choice";
	public static final String  EVENT_WORKFLOW_SELECTED = "Workflow Selected";
	public static final String  KEY_SELECT_WORKFLOW_TYPE = "Workflow Type";
	public static final String  VALUE_WORKFLOW_KIOSK = "Kiosk Connect";
	public static final String  VALUE_WORKFLOW_PHOTOBOOKS = "Photobooks";
	public static final String  VALUE_WORKFLOW_PRINTS = "Prints";
	public static final String  VALUE_WORKFLOW_COLLAGES = "Collages";
	public static final String  VALUE_WORKFLOW_GREETING_CARDS = "Greeting Cards";
	
	public static final String  EVENT_START_OVER = "Start Over";
	
	public static final String  EVENT_HELP_ACCESS = "Help Access"; 
	public static final String  KEY_HELP_TYPE = "Help Type";
	public static final String  KEY_HELP_LOCATION = "Help Location";
	public static final String  VALUE_HELP_TYPE_HELP = "Help";
	public static final String  VALUE_HELP_TYPE_TIPS = "Tips";
	
	public static final String  PAGE_VIEW_WIFI_CHOICE = "Wifi Choice";
	public static final String  PAGE_VIEW_WIFI_DISPLAY_ALL = "Wifi Display All";
	public static final String  EVENT_WIFI_DISPLAY_ALL = "Wifi Display All Selected";
	public static final String  PAGE_VIEW_WIFI_SELECT_AND_SEND = "Wifi Select and Send";
	public static final String  EVENT_WIFI_SELECT_AND_SEND = "Wifi Select and Send";
	public static final String  KEY_WIFI_SELECT_IMAGES_SENT = "Images Sent to Kiosk";
	
	public static final String  PAGE_VIEW_PHOTOBOOK_TYPE = "Photobook Type";
	public static final String  LOCALYTICS_EVENT_PHOTOBOOK_TYPE_SELECTED = "Photobook Type Selected";
	public static final String  LOCALYTICS_KEY_PHOTOBOOK_TYPE = "Photobook Type";
	
	public static final String  PAGE_VIEW_STORE_LOCATOR = "Store Locator";
	public static final String  PAGE_VIEW_SAVED_PROJECTS = "Saved Projects";
	public static final String  PAGE_VIEW_PB_THEME = "PB Theme";
	public static final String  PAGE_VIEW_PB_PREVIEW = "PB Preview";
	public static final String  PAGE_VIEW_PRINTT_IMAGE_SOURCE = "Prt Image Source";
	public static final String  PAGE_VIEW_SHOPPING_CART = "Shopping Cart";
	public static final String  PAGE_VIEW_ORDER_SUCCESS = "Order Success";
	public static final String  PAGE_VIEW_PB_IMAGE_SOURCE = "PB Image Source";
	
	
	public static final String  KEY_SELECT_PROJECT_TYPE = "Project Type";	
	public static final String  EVENT_PROJECT_LOAD = "Project Load";
	
	public static final String  VALUE_PROJECT_SAVE_IN_PHOTOBOOK = "Photobook";	
	public static final String  EVENT_PROJECT_SAVE = "Project Save";
	
	public static final String  KEY_SELECT_PB_EDIT_PAGES = "PB Pages Tab Used";	
	public static final String  KEY_SELECT_PB_EDIT_REARRANGE = "PB Rearrange Tab Used";	
	public static final String  KEY_SELECT_PB_EDIT_BACKGROUND = "PB Background Tab Used";	
	public static final String  KEY_SELECT_PB_EDIT_PICTURES = "PB Pictures Tab Used";	
	public static final String  EVENT_PB_EDIT = "PB Edit Summary";

	public static final String  EVENT_SELECT_SOURCE_TYPE ="Source Selection";
	public static final String  KEY_SELECT_FACEBOOK_SOURCE = "Facebook Selected";	
	public static final String  KEY_SELECT_NATIVE_SOURCE = "Photos Selected";
	
	public static final String  PAGE_VIEW_IMAGE_SELECTION_SCREEN = "Image Selection";
	
	public static final String  VALUE_YES = "yes";
	public static final String  VALUE_NO = "no";
	public static final String  VALUE_UNKNOWN = "Unknown";
	
	// Settings 
	public static final String  EVENT_SETTINGS_SUMMARY = "Settings Summary";
	public static final String  KEY_CUST_INOF_CHANGED = "Customer Info Changed";
	public static final String  KEY_SHIP_ADD_CHANGED = "Shipping Address Changed";
	public static final String  KEY_LICENSE_VIEWED = "License viewed";
	public static final String  KEY_PRI_POLICY_VIEWED = "Privacy Policy viewed";
	public static final String  KEY_ORDER_HIS_VIEWED = "Order History viewed";
	public static final String  KEY_AGREE_TO_TRACKING = "Agree to Tracking";
	public static final String  KEY_AGREE_TO_EMAIL = "Agree to Email";
	public static final String  KEY_ABOUT_SCREEN_VIEWED = "About Screen viewed";
	public static final String  KEY_APP_RATED = "App Rated";
	public static final String  KEY_SETTINGS_LOCATION = "Settings Location";
	
	// Store Locator
	public static final String  EVENT_STORE_CHANGED = "Store Changed";
	
	// Order Summary
	public static final String  EVENT_ORDER_SUCCESS = "Order Success";
	public static final String  PAGE_VIEW_ORDER_NOT_COMPLETE = "Order Not Completed";
	public static final String  EVENT_ORDER_NOT_COMPLETE = "Order Not Completed";
	public static final String  KEY_ORD_NOT_COMP_REASON = "Order Not Completed Reason";
	public static final String  VALUE_PAYMENT_CANCELLED = "Payment User Cancelled";
	public static final String  VALUE_PAYMENT_ERROR = "Payment Error Occurred";
	public static final String  NET_NOT_AVAILABLE = "Internet Not Available";
	public static final String  UNCLASSIFIED_ERROR = "Unclassified Error";
	
	// Shopping Cart
	public static final String  EVENT_SHOPPING_CART = "Shopping Cart";
	public static final String  EVENT_DO_MORE = "Do More";
	public static final String  PAGE_VIEW_ORDER_START = "Order Start";
	public static final String  EVENT_ORDER_START = "Order Start";
	public static final String  EVENT_CREATION_SUMMARY = "Creation Summary";
	public static final String  KEY_FACEBOOK_USED = "Facebook Used";
	public static final String  KEY_LOCAL_PHOTOS_USED = "Local Photos Used";
	public static final String  KEY_UNKNOWN_USED = "Unknown Used";
	public static final String  EVENT_ORDER_LINE_ITEM = "Order Line Item";
	public static final String  KEY_PRODUCT_ID = "Product ID";
	public static final String  KEY_PRODUCT_QUANTITY = "Product Quantity";
	public static final String  KEY_PRODUCT_IMAGES = "Product Images";
	public static final String  EVENT_ORDER_ACTIVITY_SUMMARY = "Order Activity Summary";
	public static final String  KEY_QUANTITY_CHANGES = "Quantity Changes";
	public static final String  KEY_CART_REMOVALS = "Cart Removals";
	public static final String  KEY_DELIVERY = "Delivery";
	public static final String  KEY_USER_INFO_CHANGED = "User Info Changed";
	public static final String  KEY_COUPON_APPLIED = "Coupon Applied";
	public static final String  VALUE_HOME = "Home";
	public static final String  VALUE_STORE = "Store";
	
	// Greeting Card
	public static final String  PAGE_VIEW_GC_CATEGROY = "GC Category";
	public static final String  EVENT_GC_CATEGORY = "GC Category Type";
	public static final String  VAULE_GC_CATEGORY_TYPE_FEATURED = "Featured";
	public static final String  VAULE_GC_CATEGORY_TYPE_STANDARD = "Standard";
	public static final String  PAGE_VIEW_GC_DESIGN = "GC Design";
	public static final String  PAGE_VIEW_GC_TYPE = "GC Type";
	public static final String  PAGE_VIEW_GC_PREVIEW = "GC Preview";
	public static final String  EVENT_GC_EDIT_SUMMARY = "GC Edit Summary";
	public static final String  KEY_GC_PIC_ADDED = "GC Pictures Added";
	public static final String  KEY_GC_TEXT_ADDED = "GC Text Added";
	public static final String  KEY_GC_PREVIEW_USED = "GC Preview Used";
	public static final String  KEY_GC_ROTATE_USED = "GC Rotate Used";
	public static final String  KEY_GC_EFFECT_USED = "GC Effects Used";
	public static final String  KEY_GC_PIC_DELETED = "GC Pictures Deleted";
	public static final String  KEY_GC_PIC_REPLACED = "GC Pictures Replaced";

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
