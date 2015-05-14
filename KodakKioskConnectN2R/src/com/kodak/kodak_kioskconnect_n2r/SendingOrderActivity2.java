package com.kodak.kodak_kioskconnect_n2r;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.kodak.kodak_kioskconnect_n2r.Pricing.UnitPrice;
import com.kodak.kodak_kioskconnect_n2r.bean.NewOrder;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.kodak_kioskconnect_n2r.bean.shoppingcart.Cart;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.utils.EncryptUtil;
import com.kodak.utils.FileUtils;
import com.kodak.utils.RSSLocalytics;

public class SendingOrderActivity2 extends Activity
{
	protected static final String TAG = null;
	// private final String TAG = this.getClass().getSimpleName();
	ProgressBar dialog;
	int increment;
	boolean nav = true;
	Button next;
	Button back;
	TextView headerBarText;
	TextView progressDescriptionTextView;
	ImageView pictureUploading;
	Thread setupCart;
	Thread createPrints;
	ImageSelectionDatabase mImageSelectionDatabase = null;
	TextView changeStore;
	Button information;
	Button settings;
	SharedPreferences prefs;
	ProgressBar error_dialog;
	boolean success = true;
	int cumulusCall = 0;
	boolean retry = false;
	boolean ifUploadImageAgain = false;
	private final  String SCREEN_NAME = "Order Not Completed";
	private final  String EVENT = "Order Not Completed";
	private final String UNCLASSFIELD_ERROR= "Unclassified Error";
	private final String ORDER_FAIL_REASON = "Order Not Completed Reason";
	private HashMap<String, String> attrOrderFailed;
	private List<ProductInfo> productInfoes ;

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.sendingorder);
		RSSLocalytics.onActivityCreate(this);
		back = (Button) findViewById(R.id.backButton);
		next = (Button) findViewById(R.id.nextButton);
		settings = (Button) findViewById(R.id.setupButton);
		changeStore = (TextView) findViewById(R.id.storeTV);
		information = (Button) findViewById(R.id.infoButton);
		dialog = (ProgressBar) findViewById(R.id.progressBar);
		headerBarText = (TextView) findViewById(R.id.headerBarText);
		headerBarText.setText(getString(R.string.sendingOrder));
		progressDescriptionTextView = (TextView) findViewById(R.id.progressDescription);
		progressDescriptionTextView.setText(R.string.sendingOrder);
		pictureUploading = (ImageView) findViewById(R.id.pictureUploading);
		error_dialog = (ProgressBar) findViewById(R.id.error_prograssBar);
		changeStore.setVisibility(View.VISIBLE);
		pictureUploading.setVisibility(View.GONE);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		changeStore.setText(prefs.getString("selectedStoreName", ""));
		information.setVisibility(View.GONE);
		back.setVisibility(View.INVISIBLE);
		next.setVisibility(View.INVISIBLE);
		settings.setVisibility(View.GONE);
		headerBarText.setText(R.string.sendingOrder);
		headerBarText.setTypeface(PrintHelper.tf);
		back.setTypeface(PrintHelper.tf);
		next.setTypeface(PrintHelper.tf);
		changeStore.setTypeface(PrintHelper.tf);
		progressDescriptionTextView.setTypeface(PrintHelper.tf);
		dialog.setMax(7);
		dialog.setProgress(0);
		error_dialog.setVisibility(View.INVISIBLE);
		AppContext appContext = AppContext.getApplication();
		productInfoes = appContext.getProductInfos() ;
				
		Log.i(TAG, "######### PrintHelper.isOrderSending = " + PrintHelper.isOrderSending);
		if (!PrintHelper.isOrderSending) {
		    new SetupCart().execute();
		}
	}
	
	@Override
	public void onResume()
	{
		RSSLocalytics.onActivityResume(this);
		super.onResume();
		if (prefs.getBoolean("analytics", false))
		{
			try
			{
				if (PrintHelper.wififlow)
				{
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "Wifi_At_Kiosk", 3);
				}
				else
				{
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "Prints_To_Store", 3);
				}
				PrintHelper.mTracker.trackPageView("Page-Sending_Prepare");
				PrintHelper.mTracker.dispatch();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	private final static int START_WAITING = 1;
	private final static int STOP_WAITING = 2;
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case START_WAITING:
				error_dialog.setVisibility(View.VISIBLE);
				error_dialog.bringToFront();
				new Thread(removeProducts).start();
				break;
			case STOP_WAITING:
				error_dialog.setVisibility(View.INVISIBLE);
				if(retry){
					new SetupCart().execute();
				} else {
					finish();
				}
				break;
			}
		}
		
	};
	
	Runnable removeProducts = new Runnable(){

		@Override
		public void run() {
			Log.d(TAG, "remove projects start....");
			String response = "";
			int count = 0;
			PrintMakerWebService service = new PrintMakerWebService(SendingOrderActivity2.this, "");
			
			while(count<5 && response.equals("")){
				try{
//					response = service.RemoveProducts(SendingOrderActivity2.this);
					if(!"".equals(PrintHelper.cartID)){
						response = service.removeAllProducts();
					}
					count ++;
				} catch (Exception e) {
					count = 5;
					response = "";
				}
			}
			Log.d(TAG, "remove product: " + response);
			if(!PrintHelper.wififlow && (PrintHelper.requiredContactInfos == null || PrintHelper.requiredContactInfos.size() == 0)){
				count = 0;
				response = "";
				while(count<5 && response.equals("")){
					try{
						response = service.GetRequiredContactInformation(SendingOrderActivity2.this);
						count ++;
					}catch(Exception e){
						count = 5;
						response = "";
					}
				}
			}
			Log.d(TAG, "get required contact information: " + response);
			handler.sendEmptyMessage(STOP_WAITING);
			
		}
		
	};

	private class SetupCart extends AsyncTask<String, Integer, String>
	{
		private String jsonValue = "" ;
		PrintMakerWebService service = new PrintMakerWebService(SendingOrderActivity2.this, "");
		@Override
		protected String doInBackground(String... params)
		{		
			String result = "";
			int count = 0;
			if(!"".equals(PrintHelper.cartID)){
				while(count<5 && "".equals(result)){
					result = service.removeAllProducts();
					
				}
				if("".equals(result)){
					return "fail";
				}
			}
			
			
			PrintHelper.cartID = "";
			result = "";
			// 1st: Clone Image IDs
			addErrorUploadImageToQueueList ();
			
			while (count < 5 && result.equals(""))
			{
				try
				{
					result = service.CreateCart(SendingOrderActivity2.this);
					Log.d(TAG, "CreateCart: " + result);
					count++;
				}
				catch (Exception ex)
				{
					count = 5;
					result = "";
				}
			}
			if (count != 5 && !result.equals(""))
			{
				count = 0;
				result = "";
				publishProgress(1);
			}
			
			// //PrintHelper.orderType 1: pick up in store,2:Home Delivery 
			if (PrintHelper.orderType == 1){
				while (count < 5 && result.equals(""))
				{
					try
					{
						result = service.SetStoreID(SendingOrderActivity2.this, prefs.getString("selectedStoreId", ""));
						Log.d(TAG, "SetStoreID: " + result);
						count++;
					}
					catch (Exception ex)
					{
						count = 5;
						result = "";
					}
				}
			}else if (PrintHelper.orderType == 2){
				while (count < 5 && result.equals(""))
				{
					try
					{
						result = service.setRetailerId();
						Log.d(TAG, "SetRetailerID: " + result);
						count++;
					}
					catch (Exception ex)
					{
						count = 5;
						result = "";
					}
				}
			}
	
			if (count != 5 && !result.equals(""))
			{
				count = 0;
				result = "";
				publishProgress(3);
			}
			while (count < 5 && result.equals(""))
			{
				try
				{
					result = service.SetCustomerInformation(SendingOrderActivity2.this);
					Log.d(TAG, "SetCustomerInformation: " + result);
					count++;
				}
				catch (Exception ex)
				{
					count = 5;
					result = "";
				}
			}
			
			if(PrintHelper.couponCode!=null && !PrintHelper.couponCode.equals("")){
				count = 0;
				Cart cart = null;
				while(count<5 && cart==null){
					try{
						cart = service.checkCouponsTask(PrintHelper.cartID, PrintHelper.couponCode);
					} catch(Exception e){
						count = 5;
						cart = null;
					}
				}
				if(cart == null){
					return "fail";
				}
			}
			
			
			boolean hasPrintProduct = false ;
			List<ProductInfo> printProductInfos = null ;
			if(productInfoes!=null && productInfoes.size()>0){
				for (ProductInfo productInfo : productInfoes) {
					if(productInfo.productType.equalsIgnoreCase(AppConstants.PRINT_TYPE)){
						if(printProductInfos==null){
							printProductInfos = new ArrayList<ProductInfo>() ;
						}
						hasPrintProduct = true ;
						printProductInfos.add(productInfo) ;
					}
				}
			}
			
			if(hasPrintProduct){
				if (count != 5 && !"".equals(result))
				{
					count = 0;
					result = "";
					publishProgress(4);
				}
				while (count < 5 && "".equals(result))
				{
					try
					{
						result = service.createPrints(SendingOrderActivity2.this,printProductInfos);
						Log.d(TAG, "CreatePrints: " + result);
						count++;
					}
					catch (Exception ex)
					{
						count = 5;
						result = "";
					}
				}
			}
			
			if (count != 5 && !"".equals(result))
			{
				count = 0;
				result = "";
				publishProgress(5);
			}
			while (count < 5 && "".equals(result))
			{
				try
				{
					result = service.addItemsToCart(SendingOrderActivity2.this,productInfoes);
					Log.d(TAG, "AddItemsToCart: " + result);
					count++;
				}
				catch (Exception ex)
				{
					count = 5;
					result = "";
				}
			}
			if (count != 5 && !result.equals(""))
			{
				count = 0;
				result = "";
				publishProgress(6);
			}
			while (count < 5 && result.equals(""))
			{
				try
				{
					result = service.ConvertToOrder(SendingOrderActivity2.this);
					Log.d(TAG, "ConvertToOrder: " + result);
					count++;
				}
				catch (Exception ex)
				{
					count = 5;
					result = "";
				}
			}
			if (count != 5 && !result.equals(""))
			{
				publishProgress(7);
				jsonValue = result;
				return "success";
			}
			else
			{
				return "fail";
			}
		}

		@Override
		protected void onPostExecute(String result)
		{
			PrintHelper.isOrderSending = false;
			if ( "fail".equals(result) )
			{
				RSSLocalytics.recordLocalyticsPageView(SendingOrderActivity2.this, SCREEN_NAME);
				attrOrderFailed = new HashMap<String, String>();
				attrOrderFailed.put(ORDER_FAIL_REASON, UNCLASSFIELD_ERROR);
				RSSLocalytics.recordLocalyticsEvents(SendingOrderActivity2.this, EVENT, attrOrderFailed);
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(SendingOrderActivity2.this);
				builder.setTitle(getString(R.string.n2r_upload_order_fail));
				builder.setMessage("");
				builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						handler.sendEmptyMessage(START_WAITING);
						dialog.dismiss();
						retry = false;
					}
				});
				builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (ifUploadImageAgain){
							Intent intent = new Intent(SendingOrderActivity2.this, SendingOrderActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else {
							handler.sendEmptyMessage(START_WAITING);
							dialog.dismiss();
							retry = true;
						}
					}
				});
				if (!SendingOrderActivity2.this.isFinishing()) {
				builder.create().show();
				} else {
					Log.i(TAG, "the activity is finished!!!!!!!!");
				}
			}
			else
			{
				//clean up facebook cache
				cleanUpFacebookImageCache() ;
				
				
				
				String url = "";
				if (prefs.getBoolean("analytics", false))
				{
					try
					{
						PrintHelper.mTracker.trackPageView("Page-Sending_Complete");
						PrintHelper.mTracker.dispatch();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}

				// //PrintHelper.orderType 1: pick up in store,2:Home Delivery 
				if (PrintHelper.orderType == 2 || PrintHelper.PayOnline){
					NewOrder newOrder = parseNewOrder(jsonValue);
					EncryptUtil encrypt = new EncryptUtil();
					try {
						//String needToEncrypt = "orderId=" + newOrder.orderId + "&locale=en_US&brand=Kodak&appname=MOBPRINT_iOS_Kodak"; 
						String appId = getString(R.string.cumulus_appid);
						String language = Locale.getDefault().toString();
						String needToEncrypt = "orderId=" + newOrder.orderId + "&locale=" + language + "&brand=Kodak&appname="+appId; 
						String key = getString(R.string.encrypt_key).replace("-", "");
						String iv = getString(R.string.encrypt_iv).replace("-", "");
						byte[] bytes = encrypt.encrypt(needToEncrypt.getBytes(), encrypt.generateKey(key, 32), encrypt.generateKey(iv, 16));
						String resultEncode = encrypt.encodeResult(bytes);
						url = getString(R.string.cumulus_paymenturl);
						
						// below is change server part. these may be update later.
						String firstName = PreferenceManager.getDefaultSharedPreferences(SendingOrderActivity2.this).getString("firstName", "");
						String mAuthorizationServiceURL = getString(R.string.cumulus_authorizationserviceurl) + getString(R.string.cumulus_appid) + "&scope=all";
						String currentServer = mAuthorizationServiceURL.substring(mAuthorizationServiceURL.indexOf("https://") + 8, mAuthorizationServiceURL.indexOf("KodakAuthorizationService/Service.svc"));
						if("RSS_Staging".equalsIgnoreCase(firstName)){
							url = url.replace(currentServer, "mykodakmomentsstage.kodak.com/");
						}else if("RSS_Production".equalsIgnoreCase(firstName)){
							url = url.replace(currentServer, "mykodakmoments.kodak.com/");
						}else if("RSS_Development".equalsIgnoreCase(firstName)){
							url = url.replace(currentServer, "rssdev.kodak.com/");
						}else if("RSS_ENV1".equalsIgnoreCase(firstName)){
							url = url.replace(currentServer, "RSSDEV1.KODAK.COM/");
						}else if("RSS_ENV2".equalsIgnoreCase(firstName)){
							url = url.replace(currentServer, "RSSDEV2.KODAK.COM/");
						}
						
						url += "PaymentBegin.aspx?info=" + resultEncode;
					} catch (Exception e) {
						e.printStackTrace();
						//listener.onTaskFailed(ERROR_CODE_CONVERT_ORDER);
					}
					if(newOrder!=null && newOrder.grandTotal!=null){
						PrintHelper.totalCost = newOrder.grandTotal.priceStr;
					}
					Intent intent = new Intent(SendingOrderActivity2.this, PayOnlineActivity.class);
					intent.putExtra("payURI", url);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}else {
					Intent intent = new Intent(SendingOrderActivity2.this, OrderSummaryActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}
			}
		}

		@Override
		protected void onPreExecute()
		{
		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			dialog.setProgress(values[0]);
		}
	}

	@Override
	public void onPause()
	{
		RSSLocalytics.onActivityPause(this);
		super.onPause();
		try
		{
			if (setupCart.isAlive()) {
				setupCart.interrupt();
				Log.i(TAG, "setupCart is interrupt !!!!!!!!");
			}
		}
		catch (Exception ex)
		{
		}
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			nav = false;
		}
		return nav;
	}
	
	public NewOrder parseNewOrder(String result){
		NewOrder newOrder = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(NewOrder.FLAG_NEW_ORDER)){
				newOrder = new NewOrder();
				JSONObject jsOrder = jsObj.getJSONObject(NewOrder.FLAG_NEW_ORDER);
				if(jsOrder.has(NewOrder.FLAG_ORDER_ID)){
					newOrder.orderId = jsOrder.getString(NewOrder.FLAG_ORDER_ID);
				}
				if(jsOrder.has(NewOrder.FLAG_CURRENCY)){
					newOrder.currency = jsOrder.getString(NewOrder.FLAG_CURRENCY);
				}
				if(jsOrder.has(NewOrder.FLAG_CURRENCY_SYMBOL)){
					newOrder.currencySymbol = jsOrder.getString(NewOrder.FLAG_CURRENCY_SYMBOL);
				}
				if(jsOrder.has(NewOrder.FLAG_SUB_TOTAL)){
					JSONObject jsSt = jsOrder.getJSONObject(NewOrder.FLAG_SUB_TOTAL);
					newOrder.subTotal = new UnitPrice();
					if(jsSt.has(UnitPrice.FLAG_PRICE)){
						newOrder.subTotal.price = jsSt.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsSt.has(UnitPrice.FLAG_PRICE_STR)){
						newOrder.subTotal.priceStr = jsSt.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if(jsOrder.has(NewOrder.FLAG_TAX)){
					JSONObject jsTax = jsOrder.getJSONObject(NewOrder.FLAG_TAX);
					newOrder.tax = new UnitPrice();
					if(jsTax.has(UnitPrice.FLAG_PRICE)){
						newOrder.tax.price = jsTax.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsTax.has(UnitPrice.FLAG_PRICE_STR)){
						newOrder.tax.priceStr = jsTax.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if(jsOrder.has(NewOrder.FLAG_GRAND_TOTAL)){
					JSONObject jsGt = jsOrder.getJSONObject(NewOrder.FLAG_GRAND_TOTAL);
					newOrder.grandTotal = new UnitPrice();
					if(jsGt.has(UnitPrice.FLAG_PRICE)){
						newOrder.grandTotal.price = jsGt.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsGt.has(UnitPrice.FLAG_PRICE_STR)){
						newOrder.grandTotal.priceStr = jsGt.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if(jsOrder.has(NewOrder.FLAG_TAX_BE_CAL_BY_RETAILER)){
					newOrder.taxWillBeCalculatedByRetailer = jsOrder.getBoolean(NewOrder.FLAG_TAX_BE_CAL_BY_RETAILER);
				}
				if(jsOrder.has(NewOrder.FLAG_TAXES_ARE_ESTIMATED)){
					newOrder.taxesAreEstimated = jsOrder.getBoolean(NewOrder.FLAG_TAXES_ARE_ESTIMATED);
				}
				if(jsOrder.has(NewOrder.FLAG_INITIATED)){
					newOrder.initiated = jsOrder.getBoolean(NewOrder.FLAG_INITIATED);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return newOrder;
	}
	
	/**
	 * check the Image upload.
	 * if upload has been fail. will add the upload Queue list to upload again. 
	 * @author song
	 * @created 2013-12-27
	 */
	private void addErrorUploadImageToQueueList (){
		List<Photobook> photobooks = AppContext.getApplication().getPhotobooks();
		if(photobooks != null){
			for(Photobook photobook : photobooks){
				for(PhotoInfo photo : photobook.selectedImages){
					if((photo.getContentId()==null || "".equals(photo.getContentId())) && !AppContext.getApplication().getmUploadPhotoList().contains(photo)){
						AppContext.getApplication().getmUploadPhotoList().add(photo);
					}
				}
			}
		}
	}
	
	
	/**
	 * clean up facebook image cache
	 * 
	 */
	private void cleanUpFacebookImageCache(){
		CleanUpFacebookImageRunnable cleanUpFacebookImageRunnable = new CleanUpFacebookImageRunnable() ;
		Thread cleanUpFacebookImageCacheThread = new Thread(cleanUpFacebookImageRunnable) ;
		cleanUpFacebookImageCacheThread.start() ;
	}
	
	/**
	 * 
	 * @author sunny
	 *
	 */
	class CleanUpFacebookImageRunnable implements Runnable{

		@Override
		public void run() {
			File facebookImageDiskCache = ImageCache.getDiskCacheDir(getApplicationContext(), AppConstants.KODAK_TEMP_PICTURE_WEB) ;
			if(facebookImageDiskCache!=null
					&& facebookImageDiskCache.exists()
					&& facebookImageDiskCache.isDirectory()){
				
				long totalSize = FileUtils.calculateDirectDirectorySize(facebookImageDiskCache) ;
				Log.v("sunny", "sunny total  "+ totalSize) ;
				// if cache exceed 5MB ,clean cache
				if(totalSize > AppConstants.FACEBOOK_IMAGE_CACHESIZE){
					FileUtils.deleteAllFilesInDirectory(ImageCache.getDiskCacheDir(getApplicationContext(),"" ) ) ;
				}
			}
		}
		
	}
}
