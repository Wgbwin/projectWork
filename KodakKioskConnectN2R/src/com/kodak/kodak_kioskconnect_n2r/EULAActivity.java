package com.kodak.kodak_kioskconnect_n2r;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;

public class EULAActivity extends Activity
{
	WebView mWebView;
	Button accept;
	Button reject;
	Button exitButton;
	Button readLicenseButton;
	TextView eulaInstructionsTV;
	TextView headerTV;
	TextView agreetotermsTV;
	String brand = "";
	String lang = "";
	String sub = "";
	String path = "";
	// CheckBox trackingCB;
	boolean localHelp = true;
	public static String PACKAGE_NAME;
	RelativeLayout bottomBarLayout;
	RelativeLayout bottomBarPortLayout;
	Button acceptPort;
	Button rejectPort;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// mImageSelectionDatabase.open();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.help);
		getView();
		setType();
		// trackingCB.setTypeface(PrintHelper.tf);
		
		exitButton.setVisibility(View.INVISIBLE);
		agreetotermsTV.setVisibility(View.VISIBLE);
		
		readLicenseButton.setVisibility(View.VISIBLE);
		eulaInstructionsTV.setVisibility(View.VISIBLE);
	
		setWebView();
		if (this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
			accept.setVisibility(View.VISIBLE);
			reject.setVisibility(View.VISIBLE);
			bottomBarPortLayout.setVisibility(View.GONE);
			bottomBarLayout.setVisibility(View.VISIBLE);
			onSetEvent(accept,reject);
		}else {
			acceptPort.setVisibility(View.VISIBLE);
			rejectPort.setVisibility(View.VISIBLE);
			bottomBarPortLayout.setVisibility(View.VISIBLE);
			bottomBarLayout.setVisibility(View.GONE);
			onSetEvent(acceptPort,rejectPort);
		}
	}
	public void setWebView(){
		String language = Locale.getDefault().toString();
		String brandStr = PrintHelper.getBrandForURL();
		String currentServer = PrintHelper.getServerURL(); //for RSSMOBILEPDC-1952
		String eulaURL = "https://" + currentServer + "Mob/eula.aspx?" + brandStr+ "language="+language;
		mWebView.setVisibility(View.INVISIBLE);
		PACKAGE_NAME = getApplicationContext().getPackageName();
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
		mWebView.setBackgroundColor(Color.BLACK);
		mWebView.loadUrl(eulaURL);
		headerTV.setText(getString(R.string.eulatitle));
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.getString("path") != null)
			path = extras.getString("path");
	}
	public void getView(){
		mWebView = (WebView) findViewById(R.id.webview);
		accept = (Button) findViewById(R.id.acceptbutton);
		acceptPort=(Button) findViewById(R.id.acceptbutton_port);
		reject = (Button) findViewById(R.id.rejectbutton);
		rejectPort=(Button) findViewById(R.id.rejectbutton_port);
		exitButton = (Button) findViewById(R.id.backButton);
		headerTV = (TextView) findViewById(R.id.headerBarText);
		agreetotermsTV = (TextView) findViewById(R.id.agreetotermsTV);
		eulaInstructionsTV = (TextView) findViewById(R.id.eulainstructionsTV);
		readLicenseButton = (Button) findViewById(R.id.eulaReadButton);
		bottomBarLayout=(RelativeLayout) findViewById(R.id.helpbottombar);
		bottomBarPortLayout=(RelativeLayout) findViewById(R.id.helpbottombar_port);
		
	}
	public void setType(){
		eulaInstructionsTV.setTypeface(PrintHelper.tf);
		readLicenseButton.setTypeface(PrintHelper.tf);
		agreetotermsTV.setTypeface(PrintHelper.tf);
		headerTV.setTypeface(PrintHelper.tf);
		accept.setTypeface(PrintHelper.tf);
		reject.setTypeface(PrintHelper.tf);
		
	}
	public void onSetEvent(View accept,View reject){
	
		readLicenseButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				readLicenseButton.setVisibility(View.INVISIBLE);
				eulaInstructionsTV.setVisibility(View.INVISIBLE);
				mWebView.setVisibility(View.VISIBLE);
				// trackingCB.setVisibility(View.INVISIBLE);
			}
		});
		
		accept.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(EULAActivity.this);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("eulaAccepted", true);
				editor.commit();
				Intent myIntent = null;
				boolean isWMC = PACKAGE_NAME.contains("wmc");
				boolean isDMC = PACKAGE_NAME.contains(MainMenu.DM_COMBINED_PACKAGE_NAME);
				boolean isPrintMaker = PACKAGE_NAME.contains("kodakprintmaker");
				boolean privacyAccepted = prefs.getBoolean("privacyAccepted", false);
				if(!privacyAccepted && isWMC)
				{
					 myIntent = new Intent(EULAActivity.this, PrivacyActivity.class);
					startActivity(myIntent);
					finish();
				}
				else
				{
					if(isPrintMaker){
						myIntent = new Intent(EULAActivity.this, WiFiSelectWorkflowActivity.class);
					}else if (Connection.isNearKioskWifi(EULAActivity.this)) {
						myIntent = new Intent(EULAActivity.this, WiFiSelectWorkflowActivity.class);
					}else{
						myIntent = new Intent(EULAActivity.this, MainMenu.class);
					}
					startActivity(myIntent);
					finish();
				}
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
	
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		if (this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
			bottomBarPortLayout.setVisibility(View.GONE);
			bottomBarLayout.setVisibility(View.VISIBLE);
			onSetEvent(accept, reject);
		}else {
			bottomBarPortLayout.setVisibility(View.VISIBLE);
			bottomBarLayout.setVisibility(View.GONE);
			onSetEvent(acceptPort,rejectPort);
		}
		
		super.onConfigurationChanged(newConfig);
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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(EULAActivity.this);
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
