package com.kodak.rss.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

public class ConnectionUtil {
	/*
	 * HACKISH: These constants aren't yet available in my API level (7), but I
	 * need to handle these cases if they come up, on newer versions
	 */
	public static final int NETWORK_TYPE_EHRPD = 14; // Level 11
	public static final int NETWORK_TYPE_EVDO_B = 12; // Level 9
	public static final int NETWORK_TYPE_HSPAP = 15; // Level 13
	public static final int NETWORK_TYPE_IDEN = 11; // Level 8
	public static final int NETWORK_TYPE_LTE = 13; // Level 11

	/**
	 * Check if there is any connectivity
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnected(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected());
	}
	
	/**
	 * check if connect kiosk wifi
	 * @param context
	 * @return
	 */
	public static boolean isConnectedKioskWifi(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
		boolean returnValue = false;
		if (myWifiInfo != null && info != null)
		{
			String ssid = myWifiInfo.getSSID();
			if (ssid == null)
			{
				Log.i("Connection", "isConnectedWifi() ssid == null");
				ssid = "";
			}
			else
			{
				Log.i("Connection", "isConnectedWifi() ssid == " + ssid);
			}
			returnValue = info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI && isKioskWifi(ssid);
		}
		return returnValue;
	}
	
	public static boolean isNearKioskWifi(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		if (!wifiManager.isWifiEnabled()) {
			return false;
		}
		
		List<ScanResult> list = wifiManager.getScanResults();
		if (list != null) {
			for (ScanResult wifi : list) {
				if (isKioskWifi(wifi.SSID)) {
					return true;
				}
			}
		}
		
		//maybe it is not in scan list but is current connected kiosk (is this case exist?)
		WifiInfo info = wifiManager.getConnectionInfo();
		if (info != null && isKioskWifi(info.getSSID())) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * check if the ssid is kiosk wifi
	 * @param ssid
	 * @return
	 */
	public static boolean isKioskWifi(String ssid) {
		if (ssid == null) {
			return false;
		}
		//WifiInfo.getSSID will rerun ssid surrounded with double quotation mark
		String regex = "\"?\\d{3}\\.kodak\\.[a-zA-Z0-9]{5}\"?";
		return Pattern.matches(regex, ssid.trim());
	}
	/**
	 * check if connect wifi. any ssid is ok.
	 * 
	 * **/
	public static boolean isConnectedAnyWifi(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
		boolean returnValue = false;
		if (myWifiInfo != null && info != null)
		{
			String ssid = myWifiInfo.getSSID();
			if (ssid == null)
			{
				Log.i("Connection", "isConnectedWifi() ssid == null");
				ssid = "";
			}
			else
			{
				Log.i("Connection", "isConnectedWifi() ssid == " + ssid);
			}
			returnValue = (info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI );
		}
		return returnValue;
	}
	
	public static String getCurrentWifiSSID(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
		String returnValue = null;
		if (myWifiInfo != null && info != null)
		{
			String ssid = myWifiInfo.getSSID();
			if (ssid == null)
			{
				Log.i("Connection", "isConnectedWifi() ssid == null");
				ssid = "";
			}
			else
			{
				Log.i("Connection", "isConnectedWifi() ssid == " + ssid);
			}
			
			if(info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI ){
				returnValue = ssid;
			}
		}
		return returnValue;
	}
	
	/**
	 * Check if there is fast connectivity
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnectedCellular(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected() && ConnectionUtil.isConnectionCellular(info.getType(), info.getSubtype()));
	}

	/**
	 * Check if the connection is fast
	 * 
	 * @param type
	 * @param subType
	 * @return
	 */
	public static boolean isConnectionCellular(int type, int subType)
	{
		if (type == ConnectivityManager.TYPE_WIFI)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public static boolean isConnectedInternet(Context contexts) {

		String website = "www.kodakalaris.com";
		/** for samsung devices which VESRION_RELEASE >= 4.3 (API CODE > =18) can
		 *  not use "ping" command
		 *  so,just create a http connection to www.kodakalaris.com
		 */
		Log.i("isconnectiong", "----Build.VERSION.SDK_INT: "
				+ Build.VERSION.SDK_INT);
		if (Build.VERSION.SDK_INT >= 18) {
			Log.i("isconnectiong", "----Build.VERSION.SDK_INT>=18 true : "
					+ Build.VERSION.SDK_INT);
			HttpGet httpGet = null;
			try {
				HttpClient client = new DefaultHttpClient();
				httpGet = new HttpGet();
				HttpParams params = new BasicHttpParams();
				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
				HttpConnectionParams.setConnectionTimeout(params, 15000);
				HttpConnectionParams.setSoTimeout(params, 30000);
				httpGet.setParams(params);
				String url ="http://"+website ;
				Log.v("isconnection", "isconnection url :" + website);
				httpGet.setURI(new URI(url));
				long begin = 0;
				long end = 0;
				begin = SystemClock.currentThreadTimeMillis();
				Log.v("isconnection ", "isconnection begin: " + begin);
				HttpResponse response = client.execute(httpGet);
				int code = response.getStatusLine().getStatusCode();
				Log.v("isconnection ", "isconnection  code : " + code);
				end = SystemClock.currentThreadTimeMillis();
				Log.v("isconnection ", "isconnection done: " + end);
				long cost = end - begin;
				Log.v("isconnection", "isconnection cost " + cost);
				return true;

			} catch (ClientProtocolException e) {

				e.printStackTrace();
				return false;
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} finally {
				if (httpGet != null) {
					httpGet.abort();
				}
			}

		} else {
			Process process  = null;
			try {
				boolean result = false;
				process= Runtime.getRuntime().exec("ping -c 1 -W 5 " + website);
				int status = process.waitFor();
				Log.v("isconnection", "----status= " + status);
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				String buff = "";
				StringBuffer stringBuffer = new StringBuffer();
				while (null != (buff = reader.readLine())) {
					stringBuffer.append(buff);
				}
				Log.i("isconnenction", "----status info : " + stringBuffer);
				if (status == 0) {
					result = true;
				} else if (is3gConnected(contexts)) {
					result = true;
				} else {
					result = false;
				}

				return result;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false ;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false ;
			}finally{
				process.destroy() ;
			}

		}

	}

	public static boolean is3gConnected(Context mContext) {  
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
        if (cm != null) {  
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();  
            if (networkInfo != null 
                    && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {  
                return true;  
            }  
        }  
        return false;  
    }
	
	/**
	 * Remove/forget all kiosk wifi which android sysytem remembered
	 * @param context
	 */
	public static void removeKioskWifi(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		if (list != null && !list.isEmpty()) {
			for (WifiConfiguration wifi : list) {
				if (ConnectionUtil.isKioskWifi(wifi.SSID)) {
					wifiManager.removeNetwork(wifi.networkId);
				}
			}
		}
	}

	public static String activeWiFiName(Context context)
	{
		WifiManager man = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		// Get WiFi status
		try
		{
			return man.getConnectionInfo().getSSID().toString();
		}
		catch (Exception ex)
		{
			return "Not Connected";
		}
	}

	public static String activeWiFiSpeed(Context context)
	{
		WifiManager man = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		// Get WiFi status
		return "" + man.getConnectionInfo().getLinkSpeed();
	}

	public static String activeWiFiStrength(Context context)
	{
		WifiManager man = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return "" + man.getConnectionInfo().getRssi();
	}
	
	/**
	 * Checks whether the "Avoid poor networks" setting (named "Auto network switch" on 
	 * some Samsung devices) is enabled, which can in some instances interfere with Wi-Fi.
	 *
	 * @return true if the "Avoid poor networks" or "Auto network switch" setting is enabled
	 */
	public static boolean isPoorNetworkAvoidanceEnabled (Context ctx) {
	    final int SETTING_UNKNOWN = -1;
	    final int SETTING_ENABLED = 1;
	    final String AVOID_POOR = "wifi_watchdog_poor_network_test_enabled";
	    final String WATCHDOG_CLASS = "android.net.wifi.WifiWatchdogStateMachine";
	    final String DEFAULT_ENABLED = "DEFAULT_POOR_NETWORK_AVOIDANCE_ENABLED";
	    final String GLOBAL_CLASS = "android.provider.Settings$Global";
	    final ContentResolver cr = ctx.getContentResolver();

	    int result;

	    if (VERSION.SDK_INT >= 17) {
	        //Setting was moved from Secure to Global as of JB MR1
	    	//because the compile is api 9 ,so we can only use reflect
//	        result = Settings.Global.getInt(cr, AVOID_POOR, SETTING_UNKNOWN);
	    	try {
				Class global = Class.forName(GLOBAL_CLASS);
				Method method = global.getDeclaredMethod("getInt", ContentResolver.class,String.class,int.class);
				result = (Integer) method.invoke(null, cr,AVOID_POOR,SETTING_UNKNOWN);
			} catch (Exception e) {
				e.printStackTrace();
				result = SETTING_UNKNOWN;
			}
	    } else if (VERSION.SDK_INT >= 15) {
	        result = Settings.Secure.getInt(cr, AVOID_POOR, SETTING_UNKNOWN);
	    } else {
	        //Poor network avoidance not introduced until ICS MR1
	        //See android.provider.Settings.java
	        return false;
	    }

	    //Exit here if the setting value is known
	    if (result != SETTING_UNKNOWN) {
	        return (result == SETTING_ENABLED);
	    }

	    //Setting does not exist in database, so it has never been changed.
	    //It will be initialized to the default value.
	    if (VERSION.SDK_INT >= 17) {
	        //As of JB MR1, a constant was added to WifiWatchdogStateMachine to determine 
	        //the default behavior of the Avoid Poor Networks setting.
	        try {
	            //In the case of any failures here, take the safe route and assume the 
	            //setting is disabled to avoid disrupting the user with false information
	            Class wifiWatchdog = Class.forName(WATCHDOG_CLASS);
	            Field defValue = wifiWatchdog.getField(DEFAULT_ENABLED);
	            if (!defValue.isAccessible()) defValue.setAccessible(true);
	            return defValue.getBoolean(null);
	        } catch (IllegalAccessException ex) {
	            return false;
	        } catch (NoSuchFieldException ex) {
	            return false;
	        } catch (ClassNotFoundException ex) {
	            return false;
	        } catch (IllegalArgumentException ex) {
	            return false;
	        }
	    } else {
	        //Prior to JB MR1, the default for the Avoid Poor Networks setting was
	        //to enable it unless explicitly disabled
	        return true;
	    }
	} 
	
}
