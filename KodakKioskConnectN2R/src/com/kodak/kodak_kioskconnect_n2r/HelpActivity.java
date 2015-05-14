package com.kodak.kodak_kioskconnect_n2r;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.kodak.utils.RSSLocalytics;

public class HelpActivity extends Activity
{
	Button acceptButton;
	Button rejectButton;
	TextView acceptTermsTV;
	Button readEULAButton;
	TextView eulaInstructionsTV;
	RelativeLayout bottomBarRL;
	WebView mWebView;
	Button backButton;
	TextView headerTV;
	String brand = "kodak";
	String lang = "en-us";
	String sub = "";
	String path = "";
	boolean localHelp = false;
	boolean privacy = false;
	boolean eula = false;
	boolean couponTerms = false;
	String couponTermsURL = "";
	SharedPreferences prefs;
	
	public static final String HELP_LOCATION = "Help Location";
	private static final String EVENT_KEY = "Help Access";
	private static final String EVENT_HELP = "Help";
	private static final String EVENT_TIPS = "Tips";
	private static final String EVENT_HELPTYPE = "Help Type";
	private HashMap<String, String> attr = new HashMap<String, String>();
	private String helpLocation = "";
	private String currentServer = "";
	private String language = "";
	private String brandStr = "";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		RSSLocalytics.onActivityCreate(this);
		// mImageSelectionDatabase.open();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		prefs = PreferenceManager.getDefaultSharedPreferences(HelpActivity.this);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.help);
		acceptButton = (Button) findViewById(R.id.acceptbutton);
		rejectButton = (Button) findViewById(R.id.rejectbutton);
		acceptTermsTV = (TextView) findViewById(R.id.agreetotermsTV);
		readEULAButton = (Button) findViewById(R.id.eulaReadButton);
		eulaInstructionsTV = (TextView) findViewById(R.id.eulainstructionsTV);
		bottomBarRL = (RelativeLayout) findViewById(R.id.helpbottombar);
		mWebView = (WebView) findViewById(R.id.webview);
		backButton = (Button) findViewById(R.id.backButton);
		headerTV = (TextView) findViewById(R.id.headerBarText);
		headerTV.setText(getString(R.string.help));
		Bundle extras = getIntent().getExtras();
		if (extras != null){
			if(extras.getString("path") != null){
				path = extras.getString("path");
			}
			helpLocation = extras.getString(HELP_LOCATION);
			if(helpLocation!=null){
				attr.put(HELP_LOCATION, helpLocation);
			}
			
		}
		backButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		language = Locale.getDefault().toString();
		brandStr = PrintHelper.getBrandForURL();
		currentServer = PrintHelper.getServerURL(); //for RSSMOBILEPDC-1952
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
		// Animation fadeInAnimation =
		// AnimationUtils.loadAnimation(HelpActivity.this, R.style.Activity);
		// mWebView.setAnimation(fadeInAnimation);
		mWebView.setWebViewClient(new WebViewClient()
		{
			public void onPageFinished(WebView view, String url)
			{
				view.setVisibility(View.VISIBLE);
			}
		});
		if (extras != null)
		{
			if (extras.containsKey("brand"))
				brand = extras.getString("brand");
			if (extras.containsKey("localHelp"))
				localHelp = extras.getBoolean("localHelp");
			if (extras.containsKey("privacy"))
				privacy = extras.getBoolean("privacy");
			if (extras.containsKey("eula"))
			{
				eula = extras.getBoolean("eula");
			}
			if(extras.containsKey("coupon")){
				couponTerms = extras.getBoolean("coupon");
				couponTermsURL = extras.getString("couponTerms");
			}
		}
		else
		{
			brand = "kodak";
			localHelp = false;
			privacy = false;
			couponTerms = false;
			couponTermsURL = "";
		}
		acceptButton.setVisibility(View.INVISIBLE);
		rejectButton.setVisibility(View.INVISIBLE);
		acceptTermsTV.setVisibility(View.INVISIBLE);
		readEULAButton.setVisibility(View.INVISIBLE);
		eulaInstructionsTV.setVisibility(View.INVISIBLE);
		bottomBarRL.setVisibility(View.GONE);
		if (privacy)
		{
			
			String privacyUrl = "https://" + currentServer + "mob/privacy.aspx?" + brandStr+ "language="+language;
			mWebView.loadUrl(privacyUrl);
			headerTV.setVisibility(View.INVISIBLE);
			backButton.setText(R.string.OK) ;
			backButton.setBackgroundResource(R.drawable.next_button) ;
			
		}
		else if (eula)
		{
			String eulaURL = "https://" + currentServer + "Mob/eula.aspx?" + brandStr + "language="+language;
			mWebView.loadUrl(eulaURL);
			headerTV.setVisibility(View.INVISIBLE);
			backButton.setText(R.string.OK) ;
			backButton.setBackgroundResource(R.drawable.next_button) ;
		} 
		else if(couponTerms){
			mWebView.loadUrl(couponTermsURL);
			headerTV.setVisibility(View.INVISIBLE);
			backButton.setText(R.string.OK) ;
			backButton.setBackgroundResource(R.drawable.next_button) ;
		}
		else
		{
			try
			{
				PrintHelper.mTracker.trackPageView("Page-Info");
				PrintHelper.mTracker.dispatch();
			}
			catch (Exception ex)
			{
			}
			mWebView.loadUrl(getResources().getString(R.string.helpURL));
			attr.put(EVENT_HELPTYPE, EVENT_HELP);
			RSSLocalytics.recordLocalyticsEvents(this, EVENT_KEY, attr);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			finish();
			return true;
		}
		return false;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		RSSLocalytics.onActivityResume(this);
		try
		{
			if (prefs.getBoolean("analytics", false))
			{
				if (PrintHelper.wififlow)
				{
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "Wifi_At_Kiosk", 3);
				}
				else
				{
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "Prints_To_Store", 3);
				}
				PrintHelper.mTracker.trackPageView("/HelpScreen");
				PrintHelper.mTracker.dispatch();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void onPause()
	{
		RSSLocalytics.onActivityPause(this);
		super.onPause();
	}
}
