package com.kodak.kodak_kioskconnect_n2r;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kodak.kioskconnect.KioskConnectService;
import com.kodak.kioskconnect.KioskTransferListener;

public class KioskTransferService extends Service implements KioskTransferListener
{
	final String mobile_kiosk_interface_version = "1.0.0.0";
	private final IBinder mBinder = new LocalBinder();
	private static final String TAG = "KioskTransferService";
	WifiManager.WifiLock mWifiLock = null;

	Handler mTerminationHandler;
	// Command Loop Termination Messages
	static final int NORMAL_KIOSK_TERMINATION = 1000;
	static final int NO_COMMAND_RECEIVED_TIMEOUT = 2000;
	static final int ABNORMAL_TERMINATION = 3000;

	enum Command
	{
		getAlbums, getThumbnail,
	}

	// Null because this isn't going to be attached to any Activity
	@Override
	public IBinder onBind(Intent arg0)
	{
		return mBinder;
	}

	// Called only once when the Service is created
	@Override
	public void onCreate()
	{
		// code to execute when the service is first created
		// Need to register for the wifi receiver
		mTerminationHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				String terminationReason = "";
				switch (msg.what)
				{
				case NORMAL_KIOSK_TERMINATION:
					terminationReason = "Normal Kiosk Termination";
					break;
				case NO_COMMAND_RECEIVED_TIMEOUT:
					terminationReason = "No Command Received Timeout";
					break;
				case ABNORMAL_TERMINATION:
					terminationReason = "Abnormal Termination";
					break;
				default:
					terminationReason = "Unexpected Termination msg = " + msg.what;
					break;
				}
				Log.i(TAG, "handleMessage invoked: " + terminationReason);
				WifiManager wifiManager = (WifiManager) KioskTransferService.this.getSystemService(Context.WIFI_SERVICE);
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(KioskTransferService.this);
				WifiInfo info = wifiManager.getConnectionInfo();
				if (info != null)
				{
					String ssid = info.getSSID();
					if (ssid != null)
					{
						Log.i(TAG, "SSID == " + info.getSSID());
						if (ssid.contains("kodak"))
						{
							int networkId = info.getNetworkId();
							Boolean disabled = wifiManager.disableNetwork(networkId);
							if (disabled)
							{
								Log.d(TAG, ssid + " has been successfully disabled");
							}
							Boolean removed = wifiManager.removeNetwork(networkId);
							if (removed)
							{
								Log.d(TAG, "Network ID " + networkId + " has been successfully removed");
								synchronized (PrintHelper.wifiSetTryConnected){
									PrintHelper.wifiSetTryConnected.remove(networkId);
								}
							}
						}
					}
				}
				
				java.util.List<WifiConfiguration> allCfgs = wifiManager.getConfiguredNetworks();
				boolean wifiResult;
				for (int i = 0; i < allCfgs.size(); i++) {
					WifiConfiguration cfgNow = allCfgs.get(i);
					if (cfgNow.status == WifiConfiguration.Status.DISABLED) {
						wifiResult = wifiManager.enableNetwork(cfgNow.networkId, false);
						Log.i(TAG, "Network:" + cfgNow.SSID + "[networkId:" + cfgNow.networkId + "] re-enabled:" + wifiResult);

					}
				}
				wifiResult = wifiManager.saveConfiguration();
				Log.i(TAG, "Save Wifi Configuration Result:" + wifiResult);
				
				boolean wifiwason = prefs.getBoolean("WiFiEnabled", false);
				boolean actionCompleted = wifiManager.setWifiEnabled(wifiwason);
				if (actionCompleted) {
					if (wifiwason) {
						Log.i(TAG,
								"WiFi was successfully re-enabled after Kiosk termination");
					} else {
						Log.i(TAG,
								"WiFi was successfully disabled after Kiosk termination");
					}
				} else {
					if (wifiwason) {
						Log.i(TAG,
								"WiFi was not successfully re-enabled after Kiosk termination");
					} else {
						Log.i(TAG,
								"WiFi was not successfully disabled after Kiosk termination");
					}

				}

				KioskTransferService.this.stopSelf();
			}
		};
		registerReceiver(KioskTransferService.this.receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		Log.d(TAG, "onCreate() complete");
	}

	// Called when we stop the service
	@Override
	public void onDestroy()
	{
		// Get rid of the WiFi receiver
		unregisterReceiver(receiver);
		super.onDestroy();
		Log.d(TAG, "onDestroy() complete");
	}

	// Called whenever Context.startservice is called
	@Override
	public void onStart(Intent intent, int startid)
 {
		new Thread(new Runnable() {
			@Override
			public void run() {
				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				if (wifiManager != null) {
					mWifiLock = wifiManager.createWifiLock(TAG);
					mWifiLock.acquire();
					WifiInfo info = wifiManager.getConnectionInfo();
					if (info != null) {
						String ssid = info.getSSID();
						if (ssid != null) {
							Log.i(TAG, "ssid==" + ssid);
						}

						SupplicantState state = info.getSupplicantState();
						if (SupplicantState.isValidState(state)) {
							Log.i(TAG, "SupplicantState==" + state.toString());
						}
					}
				}
				
				java.util.ArrayList<String> taggedSetURIs = null;
				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(KioskTransferService.this);
				boolean sendOnlyTaggedImages = sp.getBoolean("sendOnlyTaggedImages",
						true);
				boolean isSendAll = sp.getBoolean("isSentAll", false);
				if (sendOnlyTaggedImages && (! isSendAll)) {
					try {
						ImageSelectionDatabase db = new ImageSelectionDatabase(KioskTransferService.this);
						db.open();
						taggedSetURIs = db.getTaggedSetURIs();
						db.close();
					} catch (Exception e) {
						Log.e(TAG, "Fail to get tagged set uri", e);
					}
				}

				KioskConnectService kcs = new KioskConnectService(KioskTransferService.this);
				if (taggedSetURIs == null || taggedSetURIs.size() == 0) {
//					kcs.rssDisplayAll(KioskTransferService.this, getString(R.string.earlieralbum)
//							, getString(R.string.otheralbum));
					
					//if the parameter taggedSetURIs is setted as null, it will display all the albums which are sorted by album name
					kcs.startWiFiServer(KioskTransferService.this, null);
				}
				else {
					kcs.startWiFiServer(KioskTransferService.this, taggedSetURIs);
				}
			}
		}).start();
	}

	public class LocalBinder extends Binder
	{
		KioskTransferService getService()
		{
			return KioskTransferService.this;
		}
	}

	public BroadcastReceiver receiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED))
			{
				// KioskTransferService.this.stopSelf();
				Log.i(TAG, "onReceive(disconnected");
			}
		}
	};
	
	private void wifiServerTerminated(int code)
	{
		mTerminationHandler.sendEmptyMessage(code);
		if (mWifiLock != null) {
			try {
				mWifiLock.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void kioskWifiServerStarted() 
	{
		
	}

	@Override
	public void kioskWifiServerTerminated() 
	{
		wifiServerTerminated(NORMAL_KIOSK_TERMINATION);
	}

	@Override
	public void kioskWifiServerTerminatedUnauthorized() 
	{
		wifiServerTerminated(ABNORMAL_TERMINATION);
	}

	@Override
	public void kioskWifiServerTerminatedWithError() 
	{
		wifiServerTerminated(ABNORMAL_TERMINATION);
	}
}
