package com.kodak.kodak_kioskconnect_n2r;

import com.kodak.utils.RSSLocalytics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class WifiDisconnectedActivity extends Activity
{
	TextView headerTV;
	TextView totalSelectedTV;
	Button backButton;
	Button nextButton;
	boolean localHelp = true;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.wifidisconnect);
		headerTV = (TextView) findViewById(R.id.headerBarText);
		totalSelectedTV = (TextView) findViewById(R.id.totalSelectedTextView);
		backButton = (Button) findViewById(R.id.backButton);
		nextButton = (Button) findViewById(R.id.nextButton);
		backButton.setVisibility(View.INVISIBLE);
		nextButton.setVisibility(View.INVISIBLE);
		headerTV.setTypeface(PrintHelper.tf);
		totalSelectedTV.setVisibility(View.GONE);
		headerTV.setText(getString(R.string.disconnected));
		
//		PrintHelper.removeKioskWifiListTryConnected(this);
		PrintHelper.removeKioskWifi(this);
		
		Thread thrd = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					sleep(2000);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				enteringForm.sendEmptyMessage(0);
			}
		};
		thrd.start();
	}

	private Handler enteringForm = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			try
			{
				//add by joy  @time 2015.2.4
				RSSLocalytics.onActivityCreate(WifiDisconnectedActivity.this);
				RSSLocalytics.recordLocalyticsEvents(WifiDisconnectedActivity.this, "WiFi Transfer Complete");
				
				Intent myIntent = new Intent(WifiDisconnectedActivity.this, WiFiSelectWorkflowActivity.class);
			
				//start add by bing for add function(rate this app) on 2015-3-5
				Bundle bundle = new Bundle();				
				bundle.putBoolean("fromWifiDisconnect", true);
				myIntent.putExtras(bundle);
				//end add by bing for add function(rate this app) on 2015-3-5
				myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(myIntent);
			}
			catch (Exception ex)
			{
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			return false;
		}
		return false;
	}
}
