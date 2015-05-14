package com.kodak.rss.tablet.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.kodak.rss.tablet.R;

public class PaymentView extends FrameLayout {
	
	private Context mContext;
	private DisplayMetrics dm;
	private InputMethodManager imm;
	private RoundBorderView rbView;

	public PaymentView(Context context) {
		super(context);
		init(context);
	}
	
	public PaymentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public PaymentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	protected void init(Context context) {
		mContext = context;
		dm = getResources().getDisplayMetrics();
		imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		inflate(mContext, R.layout.dialog_payment, this);				
		rbView = (RoundBorderView) findViewById(R.id.rbv_container);		
	}
	
	public PaymentView initWebViewData(String url, WebViewClient client){
		initWebView(mContext, url, client, rbView);
		return this;
	}
	
	private void initWebView(Context context, String url, WebViewClient client, RoundBorderView container){
		container.removeAllViews();
		WebView webView = new AutoPopWebView(context);
		webView.clearFormData();
		webView.clearHistory();
		webView.clearSslPreferences();
		webView.setBackgroundColor(Color.WHITE);
		WebSettings webSettings = webView.getSettings();
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webSettings.setJavaScriptEnabled(true);
		webView.setWebViewClient(client);
		webView.setWebChromeClient(new WebChromeClient());
		webView.loadUrl(url);
		container.setTag(webView);
		((RoundBorderView)container).refreshContentView(webView);				
	}
	
	public PaymentView setViewSize(){
		int height = dm.heightPixels;
		int width = (int) (height*1.1f);		
		RelativeLayout.LayoutParams params  = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
		params.height = height;
		params.width = width;
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		setLayoutParams(params);
		return this;
	}	
	
	private void hideSoftKeyboard(){
		if(mContext instanceof Activity){
			imm.hideSoftInputFromWindow( ((Activity)mContext).getCurrentFocus().getWindowToken(), 0);
		}
	}
	
	public void dismiss(){		
		((View)getParent()).setVisibility(View.GONE);		
		hideSoftKeyboard();
	}
	
	private int curSoftKeyboardHeight = 0;
	private int preSoftKeyboardHeight = 0;
	
	public void setViewVisible(){
		((View)getParent()).setVisibility(View.VISIBLE);
		PaymentView.this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
	        @Override
	        public void onGlobalLayout() {	           
	            Rect r = new Rect();
	            PaymentView.this.getWindowVisibleDisplayFrame(r);
	            AutoPopWebView webView = (AutoPopWebView)rbView.getTag();
	            preSoftKeyboardHeight = curSoftKeyboardHeight;
	            curSoftKeyboardHeight = dm.heightPixels - (r.bottom - r.top); 	
	            if (curSoftKeyboardHeight - preSoftKeyboardHeight != 0 ) {	            	
	            	if (curSoftKeyboardHeight > 0 ) {	            			            		
	            		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) PaymentView.this.getLayoutParams();																	
	            		params.height = dm.heightPixels - curSoftKeyboardHeight;
						PaymentView.this.setLayoutParams(params);
						PaymentView.this.requestLayout();	
						if (webView != null) {							
							webView.setScroll(true,curSoftKeyboardHeight);
						}
						
					}else {						
						RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) PaymentView.this.getLayoutParams();	
						params.height = dm.heightPixels;	
		        		PaymentView.this.setLayoutParams(params);
		        		PaymentView.this.requestLayout();	
		        		if (webView != null) {						
							webView.setScroll(false,0);
						}
					}	            
				}	            
	        }
	    });	
	}
	
	public boolean isShowing(){
		return ((View)getParent()).getVisibility()==View.VISIBLE;
	}
	
}
