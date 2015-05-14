package com.kodak.kodak_kioskconnect_n2r;

import com.AppConstants;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class AllowCookiesActivity extends Activity {
	static final String TAG = AllowCookiesActivity.class.getSimpleName();
	
	TextView headerTV;
	Button backBtn;
	Button nextBtn;
	Button settingBtn;
	Button infoBtn;
	Button findmoreBtn;
	CheckBox allowCookiesCB;
	SharedPreferences prefs;
	WebView findmoreWV;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.allowcookies);
		headerTV = (TextView) findViewById(R.id.headerBarText);
		backBtn = (Button) findViewById(R.id.backButton);
		nextBtn = (Button) findViewById(R.id.nextButton);
		settingBtn = (Button) findViewById(R.id.settingsButton);
		infoBtn = (Button) findViewById(R.id.infoButton);
		findmoreBtn = (Button) findViewById(R.id.findoutmorebutton);
		allowCookiesCB = (CheckBox) findViewById(R.id.analyticsCB);
		findmoreWV = (WebView) findViewById(R.id.webviewFindmore);
		
		headerTV.setTypeface(PrintHelper.tf);
		allowCookiesCB.setTypeface(PrintHelper.tf);
		
		String title = getResources().getText(R.string.analyticsPermission).toString();
		title = title.split(":")[0];
		headerTV.setText(title);
		backBtn.setVisibility(View.INVISIBLE);
		settingBtn.setVisibility(View.INVISIBLE);
		infoBtn.setVisibility(View.INVISIBLE);
		
		findmoreWV.getSettings().setJavaScriptEnabled(true);
		findmoreWV.getSettings().setSupportZoom(true);
		findmoreWV.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		findmoreWV.setWebViewClient(new WebViewClient()
		{
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				if (url != null && url.startsWith("http://"))
				{
					findmoreWV.loadUrl(url);
					return true;
				}
				else
				{
					return false;
				}
			}
		});
		findmoreWV.setBackgroundColor(Color.BLACK);
		findmoreWV.loadUrl(getResources().getString(R.string.helpURL));
		
		setEvents();
	}
	
	private void setEvents() {
		nextBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Editor editor = prefs.edit();
				editor.putBoolean(AppConstants.KEY_LOCALYTICS, allowCookiesCB.isChecked());
				editor.commit();
				Intent mIntent = new Intent(AllowCookiesActivity.this, MainMenu.class);
				startActivity(mIntent);
				finish();
			}
		});
		
		findmoreBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showWebView();			
			}
		});
		
	}
	
	private void showWebView() {
		nextBtn.setVisibility(View.INVISIBLE);
		findmoreBtn.setVisibility(View.INVISIBLE);
		findmoreWV.setVisibility(View.VISIBLE);
	}
	
	private void hideWebView() {
		nextBtn.setVisibility(View.VISIBLE);
		findmoreBtn.setVisibility(View.VISIBLE);
		findmoreWV.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK && nextBtn.getVisibility() == View.INVISIBLE)) {
			hideWebView();
			return false;
		} else
			return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("analytics", false)){
			allowCookiesCB.setChecked(true);
		} else {
			allowCookiesCB.setChecked(false);
		}
		
	}
	

}
