package com.kodak.kodak_kioskconnect_n2r;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;

public class WiFiSelectInputActivity extends Activity
{
	// private final String TAG = this.getClass().getSimpleName();
	TextView scanFlow;
	TextView manualFlow;
	Button settings;
	Button info;
	TextView version;
	Button backButton;
	TextView title;
	TextView totalSelectedTV;
	TextView instructionsTV;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.wifiinput);
		scanFlow = (TextView) findViewById(R.id.textView1);
		//manualFlow = (TextView) findViewById(R.id.textView2);
		settings = (Button) findViewById(R.id.settingsButton);
		info = (Button) findViewById(R.id.infoButton);
		scanFlow.setText(getString(R.string.scanbarcode));
		manualFlow.setText(getString(R.string.manualinput));
		version = (TextView) findViewById(R.id.versionCopyrightTextView);
		title = (TextView) findViewById(R.id.headerBarText);
		backButton = (Button) findViewById(R.id.backButton);
		totalSelectedTV = (TextView) findViewById(R.id.totalSelectedTextView);
		instructionsTV = (TextView) findViewById(R.id.instructions);
		totalSelectedTV.setVisibility(android.view.View.GONE);
		backButton.setText(getString(R.string.Back));
		title.setText(getString(R.string.scanconnect));
		scanFlow.setTypeface(PrintHelper.tfb);
		manualFlow.setTypeface(PrintHelper.tfb);
		instructionsTV.setTypeface(PrintHelper.tfb);
		// settings.setVisibility(android.view.View.INVISIBLE);
		backButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(WiFiSelectInputActivity.this, WiFiSelectWorkflowActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		scanFlow.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent myIntent = new Intent(WiFiSelectInputActivity.this, CaptureActivity.class);
				startActivity(myIntent);
			}
		});
		manualFlow.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Intent myIntent = new Intent(WiFiSelectInputActivity.this, WifiManualInputActivity.class);
				startActivity(myIntent);
			}
		});
	}

	@Override
	public void onResume()
	{
		if (Connection.isConnectedKioskWifi(WiFiSelectInputActivity.this))
		{
			Intent intent = new Intent(WiFiSelectInputActivity.this, WiFiConnectionActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		else
		{
			info.setVisibility(android.view.View.VISIBLE);
			version.setText(getString(R.string.wifi) + " " + getString(R.string.notconnected));
			version.setGravity(android.view.Gravity.LEFT);
			info.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent myIntent;
					myIntent = new Intent(WiFiSelectInputActivity.this, HelpActivity.class);
					startActivity(myIntent);
				}
			});
		}
		super.onResume();
	}
}
