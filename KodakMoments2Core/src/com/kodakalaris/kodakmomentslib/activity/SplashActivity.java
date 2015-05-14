package com.kodakalaris.kodakmomentslib.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.activity.appintro.MAppIntroActivity;
import com.kodakalaris.kodakmomentslib.util.Log;

public class SplashActivity extends BaseActivity {
	protected static final String TAG = "SplashActivity";
	public static final String INTENT_KEY_IS_CRASH_RESTART = "IS_CRASH_RESTART";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//check if is restart because of crash
		if(getIntent().getBooleanExtra(INTENT_KEY_IS_CRASH_RESTART, false)){
			Log.i(TAG,"App is restarted because of uncaught exception.");
		}else{
			Log.i(TAG,"App is started normally");
		}
		
		initData();
		
		if (isTablet(SplashActivity.this)) {
			/*Intent myIntent = new Intent(SplashActivity.this, TAppIntroActivity.class);
			startActivity(myIntent);*/
			Toast.makeText(this, "TBD", Toast.LENGTH_SHORT).show();
			KM2Application.getInstance().setIsTablet(true);
		} else {
			Intent myIntent = new Intent(SplashActivity.this, MAppIntroActivity.class);
//			KM2Application.getInstance().devRestoreGlobalVariables();
//			Intent myIntent = new Intent(SplashActivity.this, MHomeActivity.class);
			startActivity(myIntent);
			finish();
			KM2Application.getInstance().setIsTablet(false);
		}
	}
	
	
	private void initData() {
		getDeviceSize();
		KM2Application.getInstance().deleteGlobalVaribalesCacheFile();
		KM2Application.getInstance().setHaveNotBeenRecyled(true);
	}
	
	private void getDeviceSize() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		int screenHeigh = dm.heightPixels;
		KM2Application.getInstance().setScreenW(screenWidth);
		KM2Application.getInstance().setScreenH(screenHeigh);
	}
	
}
