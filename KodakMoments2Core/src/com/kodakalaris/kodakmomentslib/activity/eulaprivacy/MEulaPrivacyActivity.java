package com.kodakalaris.kodakmomentslib.activity.eulaprivacy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.kodakalaris.kodakmomentslib.DataKey;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.home.MHomeActivity;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;

public class MEulaPrivacyActivity extends BaseEulaPrivacyActivity{
	private static final String TAG = "MEulaPrivacyActivity";
	private WebView vWebViewEula;
	private WebView vWebViewPrivacy;
	private Button vBtnAgree;
	private MActionBar vActionBar;
	private View vFooter;
	private boolean mWebViewLoaded = false;
	private boolean mWebViewError = false;
	
	/**
	 * when start this activity from Setting screen, set bundle type as {@link TYPE_SETTING}
	 */
	public static final int TYPE_SETTING = 1;
	/**
	 * when start this activity from Welcome screen, set bundle type as {@link TYPE_WELCOME}
	 */
	public static final int TYPE_WELCOME = 2;
	public static final String TYPE = "type";
	
	private int type;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_eula_privacy);
		if(getIntent() != null){
			type = getIntent().getIntExtra(TYPE, 0);
		}
		initViews(savedInstanceState);
		initEvents(savedInstanceState);
		initData(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		updateAgreeBtn();
	}
	
	private void initViews(Bundle savedInstanceState) {
		vWebViewEula = (WebView) findViewById(R.id.webView_eula);
		vWebViewPrivacy = (WebView) findViewById(R.id.webView_privacy);
		vBtnAgree = (Button) findViewById(R.id.btn_agree);
		vActionBar = (MActionBar) findViewById(R.id.actionbar);
		vFooter = findViewById(R.id.vg_footer);
		if(type == TYPE_SETTING){
			vFooter.setVisibility(View.GONE);
		} else if (type == TYPE_WELCOME){
			((View)vWebViewPrivacy.getParent()).setVisibility(View.GONE);
		}
	}
	
	private void initData(Bundle savedInstanceState) {
		loadUrlForWebView(vWebViewEula, getEulaUrl());
		if(type == TYPE_SETTING){
			loadUrlForWebView(vWebViewPrivacy, getPrivacyUrl());
		}
	}
	
	private void initEvents(Bundle savedInstanceState) {
		vBtnAgree.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferrenceUtil.setBoolean(MEulaPrivacyActivity.this, DataKey.EULA_ACCEPTED, true);
				Intent intent = new Intent(MEulaPrivacyActivity.this, MHomeActivity.class);
				// need to modify
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
		
		vActionBar.setOnLeftButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO your event
				finish();
			}
		});
	}
	
	private void loadUrlForWebView(WebView content, String url) {
		Log.i(TAG, "web view load url:" + url);
		content.setBackgroundColor(0);
		WebSettings webSettings = content.getSettings();
		webSettings.setJavaScriptEnabled(true);
		content.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				mWebViewLoaded = true;
				updateAgreeBtn();
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				mWebViewError = true;
			}
			
		});
		content.loadUrl(url);
	}
	
	public void updateAgreeBtn() {
		vFooter.setVisibility(!mWebViewError && mWebViewLoaded ? View.VISIBLE : View.GONE );
	}
	
}
