package com.kodak.kodak_kioskconnect_n2r;

import java.util.Locale;

import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class PrivacyActivity extends Activity
{
	WebView mWebView;
	Button accept;
	Button reject;
	Button exitButton;
	Button readLicenseButton;
	TextView eulaInstructionsTV;
	TextView headerTV;
	TextView agreetotermsTV;
	TextView copyrightTV;
	String brand = "";
	String lang = "";
	String sub = "";
	String path = "";
	// CheckBox trackingCB;
	boolean localHelp = true;
	public static String PACKAGE_NAME;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// mImageSelectionDatabase.open();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.help);
		// trackingCB = (CheckBox)findViewById(R.id.trackingCB);
		mWebView = (WebView) findViewById(R.id.webview);
		accept = (Button) findViewById(R.id.acceptbutton);
		reject = (Button) findViewById(R.id.rejectbutton);
		exitButton = (Button) findViewById(R.id.backButton);
		headerTV = (TextView) findViewById(R.id.headerBarText);
		agreetotermsTV = (TextView) findViewById(R.id.agreetotermsTV);
		eulaInstructionsTV = (TextView) findViewById(R.id.eulainstructionsTV);
		copyrightTV = (TextView) findViewById(R.id.copyrightsTV);
		readLicenseButton = (Button) findViewById(R.id.eulaReadButton);
		// trackingCB.setTypeface(PrintHelper.tf);
		eulaInstructionsTV.setTypeface(PrintHelper.tf);
		readLicenseButton.setTypeface(PrintHelper.tf);
		agreetotermsTV.setTypeface(PrintHelper.tf);
		headerTV.setTypeface(PrintHelper.tf);
		accept.setTypeface(PrintHelper.tf);
		reject.setTypeface(PrintHelper.tf);
		headerTV.setText(R.string.privacy);
		exitButton.setVisibility(View.INVISIBLE);
		accept.setVisibility(View.VISIBLE);
		reject.setVisibility(View.VISIBLE);
		readLicenseButton.setVisibility(View.VISIBLE);
		readLicenseButton.setText(R.string.readPrivacyButton);
		eulaInstructionsTV.setVisibility(View.VISIBLE);
		eulaInstructionsTV.setText(R.string.privacyinstructions);
		agreetotermsTV.setText(R.string.agreeprivacy);
		mWebView.setVisibility(View.INVISIBLE);
		agreetotermsTV.setVisibility(View.VISIBLE);
		PACKAGE_NAME = getApplicationContext().getPackageName();
		boolean isDMC = PACKAGE_NAME.contains(MainMenu.DM_COMBINED_PACKAGE_NAME);
		if(isDMC){
			copyrightTV.setVisibility(View.VISIBLE);
		}
		readLicenseButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				readLicenseButton.setVisibility(View.INVISIBLE);
				eulaInstructionsTV.setVisibility(View.INVISIBLE);
				copyrightTV.setVisibility(View.INVISIBLE);
				mWebView.setVisibility(View.VISIBLE);
				// trackingCB.setVisibility(View.INVISIBLE);
			}
		});
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.getString("path") != null)
			path = extras.getString("path");
		accept.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PrivacyActivity.this);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("privacyAccepted", true);
				editor.commit();
				boolean isWMC = PACKAGE_NAME.contains("wmc");
				boolean isDMC = PACKAGE_NAME.contains(MainMenu.DM_COMBINED_PACKAGE_NAME);
				boolean isPrintMaker = PACKAGE_NAME.contains("kodakprintmaker");
				Intent myIntent;
				if(isWMC){
					myIntent = new Intent(PrivacyActivity.this, WiFiSelectWorkflowActivity.class);
					//myIntent = new Intent(PrivacyActivity.this, MainMenu.class);
				} else if(isPrintMaker){
					myIntent = new Intent(PrivacyActivity.this, WiFiSelectWorkflowActivity.class);
					//myIntent = new Intent(PrivacyActivity.this, MainMenu.class);
				}/* else if (isDMC){
					myIntent = new Intent(PrivacyActivity.this, AllowCookiesActivity.class);
				}*/ else {
					myIntent = new Intent(PrivacyActivity.this, MainMenu.class);
				}
				
				startActivity(myIntent);
				finish();
			}
		});
		reject.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		mWebView.setWebViewClient(new WebViewClient()
		{
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				if (url != null && url.startsWith("http://"))
				{
					mWebView.loadUrl(url);
					return true;
				}
				else
				{
					return false;
				}
			}
		});
		String language = Locale.getDefault().toString();
		String brandStr = PrintHelper.getBrandForURL();
		String currentServer = PrintHelper.getServerURL(); //for RSSMOBILEPDC-1952
		mWebView.setBackgroundColor(Color.BLACK);
		String privacyUrl = "https://" + currentServer + "mob/privacy.aspx?" + brandStr+ "language="+language;
		mWebView.loadUrl(privacyUrl);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			return false;
		}
		return false;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PrivacyActivity.this);
		if (prefs.getBoolean("acceptCookies", false))
		{
			try {
				PrintHelper.mTracker.trackPageView("/EULAScreen");
				PrintHelper.mTracker.dispatch();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}
}
