package com.kodak.kodak_kioskconnect_n2r;

import java.util.Map;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class WiFiConnectionActivity extends Activity implements ServiceConnection
{
//	Button wifi;
//	TextView status;
//	WifiConnectionSurfaceView surface;
	String ssid;
	String networkType;
	String password;
	Button back;
	Thread thrd;
	Intent myIntent;
//	TextView title;
	// private KioskTransferService mKioskTransferService = null;
	// private IBinder mIBinder = null;
	private static final String TAG = "WiFiConnectionActivity";
	private int mDisplayHeight = 0;
	Button backButton;
	TextView headerTV;
	Button nextButton;
//	TextView numSelectedTV;
	TextView statusTV;
	TextView lookAtKioskTV;
	AnimationDrawable animWifiConn;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.wifi_connection2);
		backButton = (Button) findViewById(R.id.backButton);
		nextButton = (Button) findViewById(R.id.nextButton);
		headerTV = (TextView) findViewById(R.id.headerBarText);
		statusTV = (TextView) findViewById(R.id.statusTV);
		lookAtKioskTV = (TextView) findViewById(R.id.lookAtKioskTV);
		animWifiConn = (AnimationDrawable) ((ImageView)findViewById(R.id.wifi_conn_anim)).getDrawable();
//		numSelectedTV = (TextView) findViewById(R.id.totalSelectedTextView);
		backButton.setVisibility(View.INVISIBLE);
		nextButton.setVisibility(View.INVISIBLE);
//		numSelectedTV.setVisibility(View.GONE);
		headerTV.setText(getString(R.string.connectingtokiosktitle));
//		wifi = (Button) findViewById(R.id.wifisettings);
		// back = (Button)findViewById(R.id.backButton);
		// back.setVisibility(android.view.View.GONE);
//		title = (TextView) findViewById(R.id.TextView01);
//		title.setVisibility(View.INVISIBLE);
		headerTV.setTypeface(PrintHelper.tf);
		lookAtKioskTV.setTypeface(PrintHelper.tf);
//		status = (TextView) findViewById(R.id.statusTV);
//		wifi.setVisibility(android.view.View.GONE);
		statusTV.setTypeface(PrintHelper.tf);
		WindowManager mWinMgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		// int displayWidth = mWinMgr.getDefaultDisplay().getWidth();
		// final int displayHeight = mWinMgr.getDefaultDisplay().getHeight();
		mDisplayHeight = mWinMgr.getDefaultDisplay().getHeight();

		Class<com.kodak.kodak_kioskconnect_n2r.KioskTransferService> kioskTransferServiceClass = com.kodak.kodak_kioskconnect_n2r.KioskTransferService.class;
		myIntent = new Intent(this, kioskTransferServiceClass);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		if(sp.getBoolean("isSentAll", false)){
			statusTV.setText(R.string.kioskconnectinstructions);
		}
		
		try
		{
			ComponentName serviceComponentName = startService(myIntent);
			if (serviceComponentName != null)
			{
				Log.i(TAG, "onCreate() startService called CompnentName=" + serviceComponentName.toString());
			}
		}
		catch (SecurityException se)
		{
			se.printStackTrace();
		}
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(animWifiConn != null){
					animWifiConn.start();
				}
			}
		}, 500);
	}

	@Override
	protected void onDestroy()
	{
		if (this.isServiceRunning())
		{
			Log.i(TAG, "onDestroy(), Service was still running...");
		}
		super.onDestroy();
	}

	@Override
	public void onPause()
	{
		Log.i(TAG, "Entered onPause()");
		super.onPause();
		// Stop it here or within the service?
		// stopService(myIntent);
		// WifiManager wifiManager = (WifiManager)
		// WiFiConnectionActivity.this.getSystemService(Context.WIFI_SERVICE);
		// wifiManager.removeNetwork(wifiManager.getConnectionInfo().getNetworkId());
		/*
		 * if (this.isServiceRunning()) { Log.i(TAG,
		 * "onPause(), Unbinding running service"); unbindService(this); }
		 */
		PrintHelper.toast.clear();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		return false;
	}

	/*
	 * public void onActivityResult(int requestCode, int resultCode, Intent
	 * intent) { IntentResult scanResult =
	 * IntentIntegrator.parseActivityResult(requestCode, resultCode, intent); if
	 * (scanResult != null) { String[] result =
	 * scanResult.getContents().split(";"); ssid =
	 * result[0].split(":")[2].toString(); networkType =
	 * result[1].split(":")[1].toString(); password =
	 * result[2].split(":")[1].toString();
	 * status.setText(getString(R.string.wifi) + " : " + ssid); WifiManager
	 * wifiManager = (WifiManager)
	 * WiFiConnectionActivity.this.getSystemService(Context.WIFI_SERVICE);
	 * WifiConfigManager.configure(wifiManager, ssid, password, networkType);
	 * //surface = (WifiConnectionSurfaceView) findViewById(R.id.surfaceView1);
	 * // handle scan result // else continue with any other code you need in
	 * the method } } public Handler selectionHandler = new Handler() {
	 * @Override public void handleMessage(Message msg) {
	 * //status.setText(getString(R.string.wifi) + PrintHelper.status); } };
	 */
	@Override
	public void onResume()
	{
		if (this.isServiceRunning())
		{
			Log.d(TAG, "onResume(), Service is running, binding");
			try
			{
				boolean bound = bindService(myIntent, this, 0);
				if (bound)
				{
					Log.d(TAG, "onResume(), bound to Service Successfully");
				}
				else
				{
					Log.d(TAG, "onResume(), did not bind to the Service Successfully");
				}
			}
			catch (SecurityException se)
			{
				se.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}/*
		 * else { Log.i(TAG,
		 * "onResume(), Service is not running, go to Workflow Selection Screen"
		 * ); Intent intent = new Intent(WiFiConnectionActivity.this,
		 * WifiDisconnectedActivity.class);
		 * intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		 * startActivity(intent); }
		 */
		thrd = new Thread()
		{
			@Override
			public void run()
			{
				if (PrintHelper.toast.size() > 0)
					PrintHelper.toast.clear();
				for (int i = 0; i < 25; i++)
				{
					Toaster toasterObj = new Toaster(WiFiConnectionActivity.this, i);
					toasterObj.setDx((i % 2) + 1);
					if (toasterObj.getImg() != null)
					{
						toasterObj.setY((mDisplayHeight / 2) - (toasterObj.getImg().getHeight() / 2) - 25);
					}
					else
					{
						toasterObj.setY((mDisplayHeight / 2) - (96 / 2) - 25);
					}
					PrintHelper.toast.add(toasterObj);
				}
			}
		};
		thrd.start();
		super.onResume();
	}

	public boolean isServiceRunning()
	{
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if ("com.kodak.kodak_kioskconnect_n2r.KioskTransferService".equals(service.service.getClassName()))
			{
				return true;
			}
		}
		return false;
	}

	/***** Remove Network and Disable WiFi logic *****/
	// connectButton.setOnLongClickListener(new OnLongClickListener()
	// {
	// @Override
	// public boolean onLongClick(View v)
	// {
	// WifiManager wifiManager = (WifiManager)
	// WifiManualInputActivity.this.getSystemService(Context.WIFI_SERVICE);
	// WifiInfo info = wifiManager.getConnectionInfo();
	// boolean removed = wifiManager.removeNetwork(info.getNetworkId());
	//
	// return true;
	// }
	// });
	// }
	//

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		Log.i(TAG, "Entered onServiceConnected() " + name.toString());
		// mIBinder = service;
		// mKioskTransferService = ((KioskTransferService.LocalBinder)
		// service).getService();
	}

	@Override
	public void onServiceDisconnected(ComponentName name)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WiFiConnectionActivity.this);
		if(!prefs.getBoolean("isSentAll", false)){
			ImageSelectionDatabase mImageSelectionDatabase = new ImageSelectionDatabase(this);
			mImageSelectionDatabase.handleDeleteAllUrisWiFi();
			for (Map.Entry<String, String> entry : PrintHelper.selectedHash.entrySet())
			{
				if (entry.getValue().equals("1"))
				{
					PrintHelper.selectedHash.put(entry.getKey(), "0");
				}
			}
			
			//The number of selected picture on album should be cleared after clear all tag on Tagged set.
			for (Album mAlbum : PrintHelper.mAlbumButton) {
				mAlbum.selected = "0";
			}
		}
		
		Log.i(TAG, "Entered onServiceDisconnected() " + name.toString());
		unbindService(this);
		// mIBinder = null;
		// mKioskTransferService = null;
		Intent intent = new Intent(WiFiConnectionActivity.this, WifiDisconnectedActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}
