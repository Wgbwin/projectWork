package com.kodakalaris.kodakmomentslib.activity.kioskconnection;

import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.manager.KioskManager;
import com.kodakalaris.kodakmomentslib.service.KioskTransferService;
import com.kodakalaris.kodakmomentslib.util.Log;

public class MKioskConnectionActivity extends BaseKioskConnectionActivity implements ServiceConnection{
	private static final String TAG = "MKioskConnectionActivity";
	
	private Intent mServiceIntent;
	private TextView vTxtSendingTagged;
	private ImageView vImgPhone;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_kiosk_connection);
		
		vTxtSendingTagged = (TextView) findViewById(R.id.txt_sending_tagged_set_only);
		vImgPhone = (ImageView) findViewById(R.id.img_phone);
		mServiceIntent = new Intent(MKioskConnectionActivity.this,KioskTransferService.class);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		if(KioskManager.getInstance().isDisplayAll){
			vTxtSendingTagged.setVisibility(View.INVISIBLE);
		}else{
			vTxtSendingTagged.setVisibility(View.VISIBLE);
			//put tagged set uris in intent
			ArrayList<String> list = KioskManager.getInstance().getSelectedImgUris(this);
			
			mServiceIntent.putExtra(KioskTransferService.INTENT_KEY_TAGGED_SET_URIS, list);
		}
		
		try {
			ComponentName componentName = startService(mServiceIntent);
			if(null != componentName){
				Log.i(TAG, "onCreate() startService called CompnentName=" + componentName.toString());
			}
		} catch (Exception e) {
			Log.e(TAG,"start kiosk transfer service exception",e);
		}
		
		
		
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (this.isServiceRunning())
		{
			Log.d(TAG, "onResume(), Service is running, binding");
			try
			{
				boolean bound = bindService(mServiceIntent, this, 0);
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
		
		vImgPhone.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (!isFinishing()) {
					AnimationDrawable anim = (AnimationDrawable) vImgPhone.getDrawable();
					anim.start();
				}
			}
		}, 500);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
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
		
		unbindService(this);
		
//		Intent intent = new Intent(this,WiFiDisconnectedActivity.class);
//		startActivity(intent);
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
