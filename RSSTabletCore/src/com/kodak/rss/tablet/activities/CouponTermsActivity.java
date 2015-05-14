package com.kodak.rss.tablet.activities;

import com.kodak.rss.tablet.R;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class CouponTermsActivity extends Activity {

	private Button btOK;
	private WebView wvTerms;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coupon_terms);
		findViewById(R.id.tvTitle).setVisibility(View.INVISIBLE);
		findViewById(R.id.iv_line).setVisibility(View.GONE);
		btOK = (Button) findViewById(R.id.btDone);
		wvTerms = (WebView) findViewById(R.id.wv_terms);
		String url = getIntent().getExtras().getString("coupon_terms");
		btOK.setVisibility(View.VISIBLE);
		btOK.setText(R.string.d_ok);
		btOK.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		wvTerms.setBackgroundColor(Color.BLACK);
		wvTerms.setVerticalScrollBarEnabled(false);
		wvTerms.setHorizontalScrollBarEnabled(false);
		wvTerms.getSettings().setJavaScriptEnabled(true);
		wvTerms.setWebViewClient(new WebViewClient());
		wvTerms.loadUrl(url);
	}

}
