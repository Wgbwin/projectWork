package com.kodak.kodak_kioskconnect_n2r;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

public class Connection
{
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
				ssid = context.getResources().getString(R.string.notconnected);
			}
			else
			{
				Log.i("Connection", "isConnectedWifi() ssid == " + ssid);
			}
			if (info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI)
			{
				PrintHelper.status = ssid;
			}
			else
			{
				PrintHelper.status = context.getResources().getString(R.string.notconnected);
			}
			returnValue = info.isConnected() && (info.getType() == ConnectivityManager.TYPE_WIFI) && isKioskWifi(ssid);
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
				ssid = context.getResources().getString(R.string.notconnected);
			}
			else
			{
				Log.i("Connection", "isConnectedWifi() ssid == " + ssid);
			}
			if (info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI)
			{
				PrintHelper.status = ssid;
			}
			else
			{
				PrintHelper.status = context.getResources().getString(R.string.notconnected);
			}
			returnValue = (info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI );
		}
		return returnValue;
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
	 * Check if there is fast connectivity
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnectedCellular(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected() && Connection.isConnectionCellular(info.getType(), info.getSubtype()));
	}
	
	/**
	 * check  if there is network connetion
	 */
	public static boolean isNetWorkAvailable(Context context) {
		ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE) ;
		if(cManager!=null){
			NetworkInfo netWorkInfo = cManager.getActiveNetworkInfo() ;
			if(netWorkInfo!=null && netWorkInfo.isConnected()){
				if(netWorkInfo.getState()==NetworkInfo.State.CONNECTED){
					return true ;
				}
			}
			
		}
		
		return false ;
		
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
}