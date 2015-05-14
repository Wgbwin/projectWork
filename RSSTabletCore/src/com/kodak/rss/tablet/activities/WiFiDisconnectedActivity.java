package com.kodak.rss.tablet.activities;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;

import com.kodak.rss.core.util.ConnectionUtil;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.tablet.AppManager;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;

public class WiFiDisconnectedActivity extends BaseActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_disconnected);
		
//		RssTabletApp.getInstance().removeWifiKioskAdded();
		ConnectionUtil.removeKioskWifi(this);
		//if wifi enabled before connect kiosk wifi,we need to enable wifi
		if(RssTabletApp.getInstance().isWifiEnabledBeforeConnectKioskWifi){
			WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			wm.setWifiEnabled(true);
		}
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				
				//add by joy  @time 2015.2.4
				RSSLocalytics.onActivityCreate(WiFiDisconnectedActivity.this);
				RSSLocalytics.recordLocalyticsEvents(WiFiDisconnectedActivity.this, "WiFi Transfer Complete");
				
				AppManager.getInstance().finishAllActivity();
				Intent intent = new Intent(WiFiDisconnectedActivity.this,WiFiSelectWorkflowActivity.class);
				
				//start add by bing for add function(rate this app) on 2015-3-5
				Bundle bundle = new Bundle();				
				bundle.putBoolean("fromWifiDisconnect", true);
				intent.putExtras(bundle);
				//end add by bing for add function(rate this app) on 2015-3-5
				
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		}, 2000);
	}
	
}
