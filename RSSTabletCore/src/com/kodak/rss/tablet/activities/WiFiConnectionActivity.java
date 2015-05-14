package com.kodak.rss.tablet.activities;

import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.kodak.rss.core.services.KioskTransferService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.db.ImageSelectionDatabase;
import com.kodak.rss.tablet.view.RainPicView;

public class WiFiConnectionActivity extends BaseActivity implements ServiceConnection{
	private static final String TAG = WiFiConnectionActivity.class.getSimpleName();
	
	private RainPicView rainPicView;
	private TextView tvInstruction;
	private Intent serviceIntent;
	private boolean isDisplayAll;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_connection);
		
		rainPicView = (RainPicView) findViewById(R.id.rainView);
		tvInstruction = (TextView) findViewById(R.id.tv_instruction);
		
		serviceIntent = new Intent(WiFiConnectionActivity.this,KioskTransferService.class);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		isDisplayAll = sp.getBoolean(WiFiSelectWorkflowActivity.PREFERENCE_KEY_IS_DISPLAY_ALL, false);
		if(isDisplayAll){
			tvInstruction.setText(R.string.kioskconnectinstructions);
		}else{
			//put tagged set uris in intent
			ImageSelectionDatabase imageSelectionDatabase = new ImageSelectionDatabase(this);
			ArrayList<String> list = imageSelectionDatabase.getTaggedSetURIs();
			
			serviceIntent.putExtra(KioskTransferService.INTENT_KEY_TAGGED_SET_URIS, list);
		}
		
		try {
			ComponentName componentName = startService(serviceIntent);
			if(null != componentName){
				Log.i(TAG, "onCreate() startService called CompnentName=" + componentName.toString());
			}
		} catch (Exception e) {
			Log.e(TAG,"start kiosk transfer service exception",e);
		}
		
	}
	
	@Override
	protected boolean hasSideMenu() {
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		rainPicView.start();
		
		if (this.isServiceRunning())
		{
			Log.d(TAG, "onResume(), Service is running, binding");
			try
			{
				boolean bound = bindService(serviceIntent, this, 0);
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
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		rainPicView.stop();
	}
	
	@Override
	public void onBackPressed() {
		return;
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.i(TAG,"service connected");
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.i(TAG,"service disconnected");
		
		if(!isDisplayAll){
			ImageSelectionDatabase db = new ImageSelectionDatabase(this);
			db.handleDeleteAllUrisWiFi();
		}
		
		unbindService(this);
		
		Intent intent = new Intent(this,WiFiDisconnectedActivity.class);
		startActivity(intent);
		finish();
	}
	
	public boolean isServiceRunning()
	{
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if ("com.kodak.rss.core.services.KioskTransferService".equals(service.service.getClassName()))
			{
				return true;
			}
		}
		return false;
	}
}
