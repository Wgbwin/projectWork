package com.kodakalaris.kodakmomentslib.service;

import java.util.ArrayList;

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

import com.kodak.kioskconnect.KioskConnectService;
import com.kodak.kioskconnect.KioskTransferListener;
import com.kodakalaris.kodakmomentslib.util.Log;

public class KioskTransferService extends Service implements KioskTransferListener
{
	/**
	 * when you app start this service and is send tagged images,
	 * put a Arraylist<String> with this intent key
	 */
	public final static String INTENT_KEY_TAGGED_SET_URIS = "TAGGED_SET_URIS";
	
	final String mobile_kiosk_interface_version = "1.0.0.0";
	private final IBinder mBinder = new LocalBinder();
	private static final String TAG = "KioskTransferService";
	WifiManager.WifiLock mWifiLock = null;
	
	Handler mTerminationHandler;
	// Command Loop Termination Messages
	static final int NORMAL_KIOSK_TERMINATION = 1000;
	static final int NO_COMMAND_RECEIVED_TIMEOUT = 2000;
	static final int ABNORMAL_TERMINATION = 3000;

	// Null because this isn't going to be attached to any Activity
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	// Called only once when the Service is created
	@Override
	public void onCreate() {
		// code to execute when the service is first created
		// Need to register for the wifi receiver
		mTerminationHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String terminationReason = "";
				switch (msg.what) {
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
				if (info != null) {
					String ssid = info.getSSID();
					if (ssid != null) {
						Log.i(TAG, "SSID == " + info.getSSID());
						if (ssid.contains("kodak")) {
							int networkId = info.getNetworkId();
							Boolean disabled = wifiManager.disableNetwork(networkId);
							if (disabled) {
								Log.d(TAG, ssid + " has been successfully disabled");
							}
							Boolean removed = wifiManager.removeNetwork(networkId);
							if (removed) {
								Log.d(TAG, "Network ID " + networkId + " has been successfully removed");
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
				
				KioskTransferService.this.stopSelf();
			}
		};
		registerReceiver(KioskTransferService.this.receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		Log.d(TAG, "onCreate() complete");
	}

	// Called when we stop the service
	@Override
	public void onDestroy() {
		// Get rid of the WiFi receiver
		unregisterReceiver(receiver);
		super.onDestroy();
		Log.d(TAG, "onDestroy() complete");
	}

	// Called whenever Context.startservice is called
	@Override
	public void onStart(final Intent intent, int startid) {
		if (intent == null) {
			Log.i(TAG, "Intent is null. Maybe Service is auto-restarted because of crash");
			stopSelf();
			return;
		}
		
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
				
				ArrayList<String> taggedSetURIs = null;
				boolean isSendAll = ! intent.hasExtra(INTENT_KEY_TAGGED_SET_URIS);
				if (! isSendAll) {
					taggedSetURIs = (ArrayList<String>) intent.getSerializableExtra(INTENT_KEY_TAGGED_SET_URIS);
				}

				KioskConnectService kcs = new KioskConnectService(KioskTransferService.this);
				if (taggedSetURIs == null || taggedSetURIs.size() == 0) {
					kcs.startWiFiServer(KioskTransferService.this, null);
				}
				else {
					kcs.startWiFiServer(KioskTransferService.this, taggedSetURIs);
				}
			}
		}).start();
	}

	public class LocalBinder extends Binder {
		KioskTransferService getService() {
			return KioskTransferService.this;
		}
	}

	public BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED)) {
				// KioskTransferService.this.stopSelf();
				Log.i(TAG, "onReceive(disconnected");
			}
		}
	};
	
	private void wifiServerTerminated(int code) {
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
	public void kioskWifiServerStarted() {
		
	}

	@Override
	public void kioskWifiServerTerminated() {
		wifiServerTerminated(NORMAL_KIOSK_TERMINATION);
	}

	@Override
	public void kioskWifiServerTerminatedUnauthorized() {
		wifiServerTerminated(ABNORMAL_TERMINATION);
	}

	@Override
	public void kioskWifiServerTerminatedWithError() {
		wifiServerTerminated(ABNORMAL_TERMINATION);
	}
}
