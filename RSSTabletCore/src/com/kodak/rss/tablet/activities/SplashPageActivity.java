package com.kodak.rss.tablet.activities;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.kodak.rss.core.util.ConnectionUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.LoaderUtil;

public class SplashPageActivity extends BaseActivity{
	public static final String INTENT_KEY_IS_CRASH_RESTART = "is_crash_restart";
	
	private static final String TAG = "SplashPageActivity";
	private Animation alphaAnimation = null;  
	private View view;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//for auto coonect to kiosk
		//we can only get the latest wifi scan result, so we start scan when start, it will improve the chance to get the correct current list
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiManager.startScan();
		
		//check if is restart because of crash
		if(getIntent().getBooleanExtra(INTENT_KEY_IS_CRASH_RESTART, false)){
			Log.i(TAG,"App is restarted because of uncaught exception.");
		}else{
			Log.i(TAG,"App is started normally");
			// clear token when app launched.
			SharedPreferrenceUtil.saveAuthorizationToken(this, "");
		}
		getApp().deleteGlobalVaribalesCacheFile();
		RssTabletApp.getInstance().isInited = true;
		LoaderUtil.clearCaches(getApplicationContext(),"."+FilePathConstant.bookType);
		LoaderUtil.clearCaches(getApplicationContext(),"."+FilePathConstant.printType);
		LoaderUtil.clearCaches(getApplicationContext(),"."+FilePathConstant.projectType);
		LoaderUtil.clearCaches(getApplicationContext(),"."+FilePathConstant.cardType);
		LoaderUtil.clearCaches(getApplicationContext(),"."+FilePathConstant.calendarType);
		LoaderUtil.clearCaches(getApplicationContext(),"."+FilePathConstant.collageType);
		LayoutInflater mInflater = LayoutInflater.from(this);	
		view = mInflater.inflate(R.layout.activity_splash_page, null);
		setContentView(view);	
		initData();
		alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.alpha_anim);
		alphaAnimation.setAnimationListener(new AnimationListener() {		
			@Override
			public void onAnimationStart(Animation animation) {							
			}	
			@Override
			public void onAnimationEnd(Animation animation) {
				alphaAnimation.setFillAfter(true);
				//RSSMIBOLEPDC-1840 auto connect to kiosk
				Intent intent = null;
				if (ConnectionUtil.isNearKioskWifi(SplashPageActivity.this)) {
					intent = new Intent(SplashPageActivity.this, WiFiSelectWorkflowActivity.class);
				} else {
					intent = new Intent(SplashPageActivity.this, StartupActivity.class);
				}				
				startActivity(intent);
				SplashPageActivity.this.finish();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {					
			}
		});
		view.startAnimation(alphaAnimation);	
	}
	
	private void initData(){
		setLocalyticsValue ();		
	}
	
	/*
	 * fixed for RSSMOBILEPDC-1663
	 * DM - the message/check box should not display at all. 
	 * MKM, MKM HD,  KC -  The message displays and the check box is "ON/Checked" for all countries.
	 * @author song
	 */
	public  void setLocalyticsValue (){
		if(SharedPreferrenceUtil.getBoolean(SplashPageActivity.this, SharedPreferrenceUtil.IS_FIRST_TIME_START)){
			SharedPreferrenceUtil.setBoolean(SplashPageActivity.this,AppConstants.KEY_LOCALYTICS, true);
			SharedPreferrenceUtil.setBoolean(SplashPageActivity.this,SharedPreferrenceUtil.IS_FIRST_TIME_START, false);
		}
	}
	
	
}
