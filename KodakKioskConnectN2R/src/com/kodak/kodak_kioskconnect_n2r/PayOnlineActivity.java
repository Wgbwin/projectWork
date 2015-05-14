package com.kodak.kodak_kioskconnect_n2r;

import java.util.HashMap;

import com.AppContext;
import com.kodak.utils.RSSLocalytics;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PayOnlineActivity extends Activity {
	private final String TAG = PayOnlineActivity.class.getSimpleName();
	WebView webView;
	private String URL;
	private InfoDialog inDialog = null;
	private int successTimes =0 ;
	private boolean isFinish = false;
	
	private final String UNCLASSFIELD_ERROR= "Payment Error Occurred";
	private final String PAYMENT_ERROR_OCCURRED = "Payment Error Occurred";
	private final String PAYMENT_USER_CANCELLED = "Payment User Cancelled ";
	private final String ORDER_FAIL_REASON = "Order Not Completed Reason";
	private final  String EVENT = "Order Not Completed";
	private HashMap<String, String> attrOrderFailed;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.pay_online);
		RSSLocalytics.onActivityCreate(this);
		getViews();
		initWebView();
	}

	private void getViews() {
		webView = (WebView) findViewById(R.id.payOnlineWebView);

	}

	private void initWebView() {
		URL = this.getIntent().getStringExtra("payURI");
		webView.getSettings().setJavaScriptEnabled(true);;
		webView.setWebViewClient(client);
		webView.setWebChromeClient(new WebChromeClient());
		webView.loadUrl(URL);
	}
	
	WebViewClient client = new WebViewClient() {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			String status = "";
			if (url.contains("PaymentDone.aspx") && !isFinish) {
				status = getPaymentStatus(url);
				if (PAYMENT_SUCCESS.equals(status) && successTimes ==0) {
					if (null !=inDialog && inDialog.isShowing()) {
						inDialog.dismiss();
					}
					successTimes ++;
					Intent intent = new Intent(PayOnlineActivity.this, OrderSummaryActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
					isFinish = true;
				} else if (PAYMENT_ABORTED.contains(status)) {
					attrOrderFailed = new HashMap<String, String>();
					attrOrderFailed.put(ORDER_FAIL_REASON, PAYMENT_ERROR_OCCURRED);
					RSSLocalytics.recordLocalyticsEvents(PayOnlineActivity.this, EVENT, attrOrderFailed);
					showPaymentErrorDialog(getString(R.string.N2RShoppingCart_PaymentAborted));
				} else if (PAYMENT_CANCELED.contains(status)) {
					attrOrderFailed = new HashMap<String, String>();
					attrOrderFailed.put(ORDER_FAIL_REASON, PAYMENT_USER_CANCELLED);
					RSSLocalytics.recordLocalyticsEvents(PayOnlineActivity.this, EVENT, attrOrderFailed);
					PayOnlineActivity.this.setVisible(false);
					showPaymentErrorDialog(getString(R.string.N2RShoppingCart_PaymentCancelled));
					isFinish = true;
				} else if (PAYMENT_FAILED.contains(status)) {
					attrOrderFailed = new HashMap<String, String>();
					attrOrderFailed.put(ORDER_FAIL_REASON, PAYMENT_ERROR_OCCURRED);
					RSSLocalytics.recordLocalyticsEvents(PayOnlineActivity.this, EVENT, attrOrderFailed);
					showPaymentErrorDialog(getString(R.string.N2RShoppingCart_PaymentFailed));
				} else if (PAYMENT_ERROR.contains(status)) {
					attrOrderFailed = new HashMap<String, String>();
					attrOrderFailed.put(ORDER_FAIL_REASON, PAYMENT_ERROR_OCCURRED);
					RSSLocalytics.recordLocalyticsEvents(PayOnlineActivity.this, EVENT, attrOrderFailed);
					showPaymentErrorDialog(getString(R.string.N2RShoppingCart_PaymentCumulusError));
				} else if (PAYMENT_UNEXPECTED.contains(status)) {
					attrOrderFailed = new HashMap<String, String>();
					attrOrderFailed.put(ORDER_FAIL_REASON, PAYMENT_ERROR_OCCURRED);
					RSSLocalytics.recordLocalyticsEvents(PayOnlineActivity.this, EVENT, attrOrderFailed);
				}
			}
		}

	};

	// check the pay online webview state.
	public static String PAYMENT_SUCCESS = "0";
	public static String PAYMENT_ABORTED = "100";
	public static String PAYMENT_CANCELED = "150";
	public static String PAYMENT_FAILED = "200";
	public static String PAYMENT_ERROR = "300";
	public static String PAYMENT_UNEXPECTED = "400";

	public static String getPaymentStatus(String url) {
		final String STATUS = "status";
		String status = "";
		if (url.contains(STATUS)) {
			status = url.substring(url.lastIndexOf("=") + 1, url.length());
			return status;
		}
		return PAYMENT_UNEXPECTED;
	}

	private void showPaymentErrorDialog(String message) {
		if (null !=inDialog && inDialog.isShowing()) {
			inDialog.dismiss();
		}
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(
				PayOnlineActivity.this);
		builder.setTitle("");
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.yes),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread(removeProducts).start();
						finish();
						dialog.dismiss();
					}
				});
		inDialog = builder.create();
		inDialog.show();

	}

	@Override
	public void onBackPressed() {
		return;
	}
	
	Runnable removeProducts = new Runnable(){

		@Override
		public void run() {
			String response = "";
			int count = 0;
			PrintMakerWebService service = new PrintMakerWebService(PayOnlineActivity.this, "");
			
			while(count<5 && response.equals("")){
				try{
					//response = service.RemoveProducts(PayOnlineActivity.this);
//					response = service.removeProducts(PayOnlineActivity.this, AppContext.getApplication().getProductInfos());
					response = service.removeAllProducts();
					count ++;
				} catch (Exception e) {
					count = 5;
					response = "";
				}
			}
			Log.d(TAG, "remove product: " + response);			
		}
		
	};

	@Override
	protected void onResume() {
		RSSLocalytics.onActivityResume(this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		RSSLocalytics.onActivityPause(this);
		super.onPause();
	}
}
