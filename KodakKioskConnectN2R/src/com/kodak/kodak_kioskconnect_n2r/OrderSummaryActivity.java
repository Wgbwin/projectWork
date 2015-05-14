package com.kodak.kodak_kioskconnect_n2r;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.AppManager;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.kodak.kodak_kioskconnect_n2r.Pricing.LineItem;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.shoppingcart.Cart;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardPage;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardPageLayer;
import com.kodak.kodak_kioskconnect_n2r.view.InfoDialogWindow;
import com.kodak.utils.PhotobookUtil;
import com.kodak.utils.RSSLocalytics;

public class OrderSummaryActivity extends MapActivity
{
	private final String TAG = this.getClass().getSimpleName();
	public int outHeight = 0;
	public int outWidth = 0;
	TextView orderIdTV;
	TextView orderIdLabelTV;
	TextView confirmationEmailLabelTV;
	TextView confirmationEmailTV;
	TextView pricingEstimatedTV;
	ImageView barcodeIV;
	TextView orderSentLabelTV;
	TextView orderSentTV;
	TextView orderDetailLabelTV;
	TextView orderDetailTV;
	MapView mapViewIV;
	TextView storeNameTV;
	TextView storeNumberTV;
	TextView storeAddressTV;
	TextView mTxtCityAndZip;
	TextView estimatedSubtotalTV;
	TextView estimatedSubtotalLabelTV;
	TextView headerTextTV;
	TextView shipping_firstName,shipping_lastName,shipping_addressOne,shipping_addressTwo,shipping_city,shipping_zip,shipping_state;
	private TextView sideMenuHome_tex;
	private TextView sideMenuSetting_tex;
	private TextView sideMenuInfo_tex;
	RelativeLayout LayoutPayOnStroe,LayoutPayOnLine;
	private String shipFirstName = "";
	private String shipLastName = "";
	private String shipAddress1 = "";
	private String shipAddress2 = "";
	private String shipCity = "";
	private String shipZip = "";
	private String stateShip = "";
	private final String SCREEN_NAME = "Order Success";
	private final String EVENT = "Order Success";
	private final String PRODUCT_ID = "Product ID";
	private final String PRODUCT_QUANTITY = "Product Quantity";
	private final String PRODUCT_IMAGES = "Product Images";
	private final String EVENT_ORDER_ITEM = "Order Line Item";
	
	private HashMap<String, String> attr;
	
	Button nextButton;
	Button backButton;
	private Button slideMenuOpen;
	private Button slideMenuClose_btn;
	SharedPreferences prefs;
	ImageSelectionDatabase mImageSelectionDatabase = null;
	protected PictureKiosks mapKiosks;
	boolean settingsDetails = false;
	boolean savedOrder = false;
	String isTaxWillBeCalculatedByRetailer = "0";
	String intentId;
	private String ISFROMSEETING = "isFromSetting";
	private AppContext appContex;
	private Cart cart;
	private ProgressDialog processDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.ordersummary);
		RSSLocalytics.onActivityCreate(this);
		appContex = AppContext.getApplication();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		getViews ();
		initData ();
		if (PrintHelper.orderType == 1){
			LayoutPayOnStroe.setVisibility(View.VISIBLE);
			LayoutPayOnLine.setVisibility(View.GONE);
			if(!prefs.getBoolean("TaxWillBeCalculatedByRetailer", false)){
				pricingEstimatedTV.setVisibility(View.INVISIBLE);
				changeSubtotalTest();
				isTaxWillBeCalculatedByRetailer = "0";
			}				
			else{
				pricingEstimatedTV.setVisibility(View.VISIBLE);
				isTaxWillBeCalculatedByRetailer = "1";
			}				
		}else {
			//for shipping home
			changeSubtotalTest();
			shipFirstName = prefs.getString( "firstNameShip", "none");
			shipLastName = prefs.getString("lastNameShip", "none");
			shipAddress1 = prefs.getString("addressOneShip", "none");
			shipAddress2 = prefs.getString("addressTwoShip", "none");
			shipCity = prefs.getString("cityShip", "none");
			shipZip = prefs.getString("zipcodeShip", "none");
			stateShip = prefs.getString("stateShip", "none");
			LayoutPayOnStroe.setVisibility(View.GONE);
			LayoutPayOnLine.setVisibility(View.VISIBLE);
			pricingEstimatedTV.setVisibility(View.INVISIBLE);
		}
		
		
		if(getApplicationContext().getPackageName().contains("wmc") && prefs.getBoolean(PrintHelper.IS_AUTO_UPLOAD2ALBUMS, false)){
			View view  = findViewById(R.id.automaticUpload2AlbumsConfirmLayout);
			TextView tvEmail = (TextView) findViewById(R.id.confirmAutoUploadEmailAccountTV);
			TextView tvAlbum = (TextView) findViewById(R.id.confirmAutoUploadAlbumTV);
			String email = prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "");
			String album = prefs.getString("share_album_name", "");
			if(!email.contains("@") || !email.contains(".") || email.equals("") || PrintHelper.getAccessTokenResponse(getApplicationContext()).access_token.equals("")){
				//Invisible the autoConfirmLayout
				view.setVisibility(View.GONE);
			}else{
				view.setVisibility(View.VISIBLE);
				tvEmail.setText(email);
				tvAlbum.setText(album);
			}
		}
		if(getIntent()!=null){
			if(getIntent().getStringExtra("orderid")!=null){
				intentId = getIntent().getStringExtra("orderid");
			} 
			if(!getIntent().getBooleanExtra(ISFROMSEETING, false)){
				RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
				RSSLocalytics.recordLocalyticsEvents(this, EVENT);
				sendProductInfoForLocalytics();
				Log.i(TAG, "localytics track");
			}
		}
		setOnclickers();
	}
	
	
	private void getViews (){
		LayoutPayOnStroe = (RelativeLayout) findViewById(R.id.LayoutPayOnStroe);
		LayoutPayOnLine = (RelativeLayout) findViewById(R.id.LayoutPayOnLine);
		shipping_firstName = (TextView) findViewById(R.id.shipping_firstName);
		shipping_lastName = (TextView) findViewById(R.id.shipping_lastName);
		shipping_addressOne = (TextView) findViewById(R.id.shipping_addressOne);
		shipping_addressTwo = (TextView) findViewById(R.id.shipping_addressTwo);
		shipping_city = (TextView) findViewById(R.id.shipping_city);
		shipping_zip = (TextView) findViewById(R.id.shipping_zip);
		shipping_state = (TextView) findViewById(R.id.shipping_state);
		headerTextTV = (TextView) findViewById(R.id.headerBarText);
		headerTextTV.setText(getString(R.string.orderConfirmationTitle));
		orderIdLabelTV = (TextView) findViewById(R.id.orderIDLabelTV);
		orderSentTV = (TextView) findViewById(R.id.orderSentTV);
		orderIdTV = (TextView) findViewById(R.id.orderIDTV);
		confirmationEmailLabelTV = (TextView) findViewById(R.id.confirmationEmailLabelTV);
		confirmationEmailTV = (TextView) findViewById(R.id.confirmationEmailTV);
		pricingEstimatedTV = (TextView) findViewById(R.id.pricingEstimatedTV);
		barcodeIV = (ImageView) findViewById(R.id.barcodeIV);
		orderSentLabelTV = (TextView) findViewById(R.id.orderSentLabelTV);
		orderDetailLabelTV = (TextView) findViewById(R.id.orderDetailsLabelTV);
		orderDetailTV = (TextView) findViewById(R.id.orderDetailsTV);
		mapViewIV = (MapView) findViewById(R.id.mapview);
		storeNameTV = (TextView) findViewById(R.id.storeNameTV);
		storeNumberTV = (TextView) findViewById(R.id.storePhoneNumberTV);
		storeAddressTV = (TextView) findViewById(R.id.storeAddressTV);
		mTxtCityAndZip = (TextView) findViewById(R.id.cityZipTV);
		estimatedSubtotalTV = (TextView) findViewById(R.id.estimatedSubtotalTV);
		estimatedSubtotalLabelTV = (TextView) findViewById(R.id.estimatedSubtotalLabelTV);
		nextButton = (Button) findViewById(R.id.nextButton);
		backButton = (Button) findViewById(R.id.backButton);
		slideMenuOpen = (Button) findViewById(R.id.slideMenuOpen_btn);
		slideMenuClose_btn = (Button) findViewById(R.id.slideMenuClose_btn);
		sideMenuHome_tex = (TextView) findViewById(R.id.sideMenuHome_tex);
		sideMenuSetting_tex = (TextView) findViewById(R.id.sideMenuSetting_tex);
		sideMenuInfo_tex = (TextView) findViewById(R.id.sideMenuInfo_tex);
		slideMenuOpen = (Button) findViewById(R.id.slideMenuOpen_btn);
		slideMenuClose_btn = (Button) findViewById(R.id.slideMenuClose_btn);
		backButton.setVisibility(View.INVISIBLE);
		slideMenuOpen.setVisibility(View.VISIBLE);
		nextButton.setText(getString(R.string.newOrder));
		orderIdLabelTV.setVisibility(android.view.View.VISIBLE);
		orderIdTV.setTypeface(PrintHelper.tf);
		orderIdLabelTV.setTypeface(PrintHelper.tf);
		confirmationEmailLabelTV.setTypeface(PrintHelper.tf);
		confirmationEmailTV.setTypeface(PrintHelper.tf);
		pricingEstimatedTV.setTypeface(PrintHelper.tf);
		orderSentLabelTV.setTypeface(PrintHelper.tf);
		orderSentTV.setTypeface(PrintHelper.tf);
		orderDetailLabelTV.setTypeface(PrintHelper.tf);
		orderDetailTV.setTypeface(PrintHelper.tf);
		storeNameTV.setTypeface(PrintHelper.tf);
		storeNumberTV.setTypeface(PrintHelper.tf);
		storeAddressTV.setTypeface(PrintHelper.tf);
		mTxtCityAndZip.setTypeface(PrintHelper.tf);
		estimatedSubtotalTV.setTypeface(PrintHelper.tf);
		estimatedSubtotalLabelTV.setTypeface(PrintHelper.tf);
		headerTextTV.setTypeface(PrintHelper.tf);
		shipping_firstName.setTypeface(PrintHelper.tf);
		shipping_lastName.setTypeface(PrintHelper.tf);
		shipping_addressOne.setTypeface(PrintHelper.tf);
		shipping_addressTwo.setTypeface(PrintHelper.tf);
		shipping_city.setTypeface(PrintHelper.tf);
		shipping_zip.setTypeface(PrintHelper.tf);
		shipping_state.setTypeface(PrintHelper.tf);
	}
	
	private void setOnclickers(){
		slideMenuOpen.setOnClickListener(openMenu());

		slideMenuClose_btn.setOnClickListener(closeMenu());

		sideMenuHome_tex.setOnClickListener(gotoHome());

		sideMenuSetting_tex.setOnClickListener(gotoSettings());

		sideMenuInfo_tex.setOnClickListener(gotoInfo());
		backButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
			}
		});
		nextButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (!settingsDetails)
				{
					if (prefs.getBoolean("analytics", false))
					{
						try
						{
							PrintHelper.mTracker.trackEvent("Order", "New_Order", "", 0);
							PrintHelper.mTracker.dispatch();
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}

					//start change by bing wang for fixed RSSMOBILEPDC-2089 on 2015-2-26
//					AppContext.getApplication().setContinueShopping(false);
//					Intent intent = new Intent(OrderSummaryActivity.this, MainMenu.class);
//					//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//					AppManager.getAppManager().finishAllActivity();
//					startActivity(intent);
//					finish();																
					android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {							
							AppContext.getApplication().setContinueShopping(false);
							Intent intent = new Intent(OrderSummaryActivity.this, MainMenu.class);
							startActivity(intent);	
							
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									String packageName = OrderSummaryActivity.this.getPackageName();
									Uri uri = null;
									Intent goToMarket = null;						
									try {
										uri = Uri.parse("market://details?id=" + packageName);
										goToMarket = new Intent(Intent.ACTION_VIEW, uri);										
										startActivity(goToMarket);
									} catch (ActivityNotFoundException e) {
										uri = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
										goToMarket = new Intent(Intent.ACTION_VIEW, uri);
										startActivity(goToMarket);
									}
									OrderSummaryActivity.this.finish();
								}
							}, 200);
						}						
					};
					android.content.DialogInterface.OnClickListener cancelOnClickListener  = new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {							
							gotoMainMenu();			
						}		
					};

					String promptStr = getResources().getString(R.string.OrderScreen_RateApp);
					new InfoDialogWindow.Builder(OrderSummaryActivity.this).setMessage(promptStr)
					.setPositiveButton(getText(R.string.OK), yesOnClickListener)
					.setNeturalButton(R.string.Cancel, cancelOnClickListener)
					.create().show();
					//end change by bing wang for fixed RSSMOBILEPDC-2089 on 2015-2-26					
				}else{
					finish();
				}
			}
		});
	}
	
	private void gotoMainMenu(){
		AppContext.getApplication().setContinueShopping(false);
		Intent intent = new Intent(OrderSummaryActivity.this, MainMenu.class);
		AppManager.getAppManager().finishAllActivity();
		startActivity(intent);
		finish();				
	}	
	
	private void setSummaryInfo(String orderId){
		Log.i(TAG, "setSummaryInfo(String orderId)[" + orderId + "]");
		Order order = null;
		for(Order tempOrder : PrintHelper.sentOrders){
			if(tempOrder.orderID.equals(orderId)){
				order = tempOrder;
				break;
			}
		}
		if(order != null){
			if (!order.orderFirstnameShip.equals("")){
				LayoutPayOnStroe.setVisibility(View.GONE);
				LayoutPayOnLine.setVisibility(View.VISIBLE);
				mapViewIV.setVisibility(View.GONE);
				//pricingEstimatedTV.setVisibility(View.GONE);
				shipFirstName = order.orderFirstnameShip;
				shipLastName = order.orderLastnameShip;
				shipAddress1 = order.orderAddressoneShip;
				shipAddress2 = order.orderAddresstwoShip;
				shipCity = order.orderCityShip;
				shipZip = order.orderZipShip;
				stateShip = order.orderStateShip;
				shipping_firstName.setText(shipFirstName);
				shipping_lastName.setText(shipLastName);
				shipping_addressOne.setText(shipAddress1);
				shipping_addressTwo.setText(shipAddress2);
				shipping_city .setText(shipCity);
				shipping_zip.setText(shipZip);
				shipping_state .setText(stateShip);
				changeSubtotalTest();
			}else {
				mapViewIV.setVisibility(View.VISIBLE);
				if (null != order.isTaxWillBeCalculatedByRetailer && order.isTaxWillBeCalculatedByRetailer.equalsIgnoreCase("0")){
					pricingEstimatedTV.setVisibility(View.INVISIBLE);
					changeSubtotalTest();
				}else {
					pricingEstimatedTV.setVisibility(View.VISIBLE);
				}
				LayoutPayOnStroe.setVisibility(View.VISIBLE);
				LayoutPayOnLine.setVisibility(View.GONE);
			}
			orderSentTV.setText(order.orderTime);
			orderDetailTV.setText(order.orderDetails);
			estimatedSubtotalTV.setText(order.orderSubtotal);
			orderIdTV.setText(order.orderID);
			try {
				storeNameTV.setVisibility(View.GONE);
				storeAddressTV.setVisibility(View.GONE);
				mTxtCityAndZip.setVisibility(View.GONE);
				storeNumberTV.setVisibility(View.GONE);
				
				String storename = order.orderStoreName;
				String address = order.orderSelectedStoreAddress;
				String phonenumber = order.orderSelectedStorePhone;
				String personEmail = order.orderPersonEmail;
				String cityZip = order.orderSelectedCityAndZip;
				
				if (storename.equals("")){
					storeNameTV.setVisibility(View.GONE);
				} else{
					storeNameTV.setText(storename);
					storeNameTV.setVisibility(View.VISIBLE);
				}
				if (address.equals("")){
					storeAddressTV.setVisibility(View.GONE);
				} else{
					storeAddressTV.setText(address);
					storeAddressTV.setVisibility(View.VISIBLE);
				}
				if (cityZip.equals("") || cityZip.trim().length() == 0) {
					mTxtCityAndZip.setVisibility(View.GONE);
				} else {
					mTxtCityAndZip.setText(cityZip);
					mTxtCityAndZip.setVisibility(View.VISIBLE);
				}
				if (phonenumber.equals("")){
					storeNumberTV.setVisibility(View.GONE);
				} else{
					storeNumberTV.setText(phonenumber);
					storeNumberTV.setVisibility(View.VISIBLE);
				}
				if (personEmail != null && !personEmail.equals("")){
					confirmationEmailTV.setText(personEmail);
				} else{
					confirmationEmailLabelTV.setVisibility(View.GONE);
					confirmationEmailTV.setVisibility(View.GONE);
				}
				if (storeNameTV.getVisibility() == View.GONE && storeAddressTV.getVisibility() == View.GONE && storeNumberTV.getVisibility() == View.GONE){
					mapViewIV.setVisibility(View.GONE);
					//pricingEstimatedTV.setVisibility(View.GONE);
				} else{
					Drawable kiosk = getResources().getDrawable(R.drawable.pinpoint2);
					kiosk.setBounds(0, 0, kiosk.getIntrinsicWidth(), kiosk.getIntrinsicHeight());							
					Log.w(TAG, "Latitude[" + order.orderLatitude + "], Longitude["+ order.orderLongitude + "]");
					mapKiosks = new PictureKiosks(kiosk, OrderSummaryActivity.this, order.orderLatitude, order.orderLongitude, storename, address, phonenumber);						
					mapViewIV.getOverlays().add(mapKiosks);
					mapViewIV.setClickable(false);
					mapViewIV.getController().setZoom(15);
					mapViewIV.invalidate();
				}
			} catch(Exception e){
				Log.e(TAG, "order summary error");
			}
			
		} else {
			new SetupOrderSummary().execute();
		}
	}
	
	

	class PictureKiosks extends ItemizedOverlay<OverlayItem>
	{
		private List<OverlayItem> locations = new ArrayList<OverlayItem>();
		private Drawable marker;
		private Context mContext;
		private boolean moveToPoint;

		public PictureKiosks(Drawable marker, Context mContext, String latitude, String longitude, String name, String address, String phone)
		{
			super(marker);
			this.marker = marker;
			this.mContext = mContext;
			// (43.169829460917256, -77.70490944385529)
			int lat = (int) (Float.parseFloat(latitude) * 1000000);
			int longi = (int) (Float.parseFloat(longitude) * 1000000);
			GeoPoint geopoint = new GeoPoint(lat, longi);
			locations.add(new OverlayItem(geopoint, null, name + "\n" + address + "\n" + phone));
			moveToPoint = false;
			populate();
		}

		@Override
		public boolean onTap(int index)
		{
			OverlayItem item = locations.get(index);
			AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
			dlg.setTitle(item.getTitle());
			dlg.setPositiveButton("Place Order", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					finish();
					Intent myIntent = new Intent(OrderSummaryActivity.this, StoreFinder.class);
					startActivity(myIntent);
				}
			}).setNegativeButton("Back", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.cancel();
				}
			});
			dlg.setMessage(item.getSnippet());
			dlg.show();
			return true;
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow)
		{
			super.draw(canvas, mapView, shadow);
			boundCenterBottom(marker);
			// Paint
			Paint paint = new Paint();
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTextSize(5);
			paint.setARGB(150, 0, 0, 0); // alpha, r, g, b (Black, semi
											// see-through)
			OverlayItem item = locations.get(0);
			// Converts lat/lng-Point to coordinates on the screen
			GeoPoint point = item.getPoint();
			if(!moveToPoint){
				MapController mapCon = mapView.getController();
				mapCon.animateTo(point);
				moveToPoint = true;
			}
			// Converts lat/lng-Point to OUR coordinates on the screen.
			Point myScreenCoords = new Point();
			mapView.getProjection().toPixels(point, myScreenCoords);
			paint.setStrokeWidth(1);
			paint.setARGB(255, 255, 255, 255);
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			paint.setTextSize(25);
			paint.setTypeface(PrintHelper.tfb);
			paint.setColor(Color.WHITE);
			paint.setStrokeWidth(2);
			// canvas.drawText("Here I am...",
			// myScreenCoords.x-10,myScreenCoords.y-48, paint);
			// show text to the right of the icon
			// canvas.drawText(""+PrintHelper.selectedStore, myScreenCoords.x,
			// myScreenCoords.y-20, paint);
		}

		@Override
		protected OverlayItem createItem(int i)
		{
			return locations.get(i);
		}

		@Override
		public int size()
		{
			return locations.size();
		}
	}

	@Override
	public void onResume()
	{
		RSSLocalytics.onActivityResume(this);
		super.onResume();
		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		// close
		mDrawerLayout.closeDrawer(Gravity.LEFT);
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
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (mImageSelectionDatabase == null)
			mImageSelectionDatabase = new ImageSelectionDatabase(this);
			mImageSelectionDatabase.open();
		try
		{
			Intent myIntent = getIntent();
			settingsDetails = myIntent.getExtras().getBoolean("details");
		}
		catch (Exception ex)
		{
		}
		if (!settingsDetails)
		{
			if (savedOrder)
			{
				Intent myIntent = new Intent(OrderSummaryActivity.this, MainMenu.class);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(myIntent);
			}
			else
			{
				nextButton.setText(getString(R.string.newOrder));
			}
		}
		else
		{
			nextButton.setText(getString(R.string.done));
		}
		if(intentId == null){
			new SetupOrderSummary().execute();
		} else {
			setSummaryInfo(intentId);
		}
		if (prefs.getBoolean("analytics", false))
		{
			try
			{
				PrintHelper.mTracker.trackPageView("Page-Order_Confirmation");
				PrintHelper.mTracker.dispatch();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void onPause()
	{
		RSSLocalytics.onActivityPause(this);
		super.onPause();
		if (mImageSelectionDatabase == null)
		{
			mImageSelectionDatabase = new ImageSelectionDatabase(getApplicationContext());
			mImageSelectionDatabase.open();
		}
		try
		{
			String email = "";
			if (confirmationEmailTV.getVisibility() != View.GONE)
			{
				email = confirmationEmailTV.getText().toString();
			}
			else
			{
				email = "";
			}
			if (!settingsDetails && !savedOrder)
			{	
				String orderLatitude = prefs.getString("selectedStoreLatitude", "");
				String orderLongitude = prefs.getString("selectedStoreLongitude", "");
				String storePhone = prefs.getString("selectedStorePhone", "");
				Log.e(TAG, "detail: " + orderDetailTV.getText().toString());
				/*mImageSelectionDatabase.saveOrderDetails(email, orderIdTV.getText().toString(), orderSentTV.getText().toString(), PrintHelper.totalCost, 
						storeNameTV.getText().toString() + "\n" + storeAddressTV.getText().toString(), orderDetailTV
						.getText().toString());*/
				mImageSelectionDatabase.saveOrderDetails(email, orderIdTV.getText().toString(), orderSentTV.getText().toString(), PrintHelper.totalCost, 
						storeNameTV.getText().toString(), orderDetailTV.getText().toString(), orderLatitude, orderLongitude, storeAddressTV.getText().toString(), 
						mTxtCityAndZip.getText().toString(),storePhone ,shipFirstName,shipLastName,
						shipAddress1,shipAddress2,shipCity,shipZip,stateShip,isTaxWillBeCalculatedByRetailer);
				savedOrder = true;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		try
		{
			mImageSelectionDatabase.handleDeleteAllUrisN2R();
		}
		catch (Exception ex)
		{
		}
		boolean isFromSeeting = false;
		isFromSeeting = getIntent().getBooleanExtra(ISFROMSEETING, false);
		if(isFromSeeting){
			
		}else{
			boolean success = PrintHelper.StartOver();
			PrintHelper.clearDataForDoMore();
			if (!success)
			{
				new PrintHelper(getApplicationContext());
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			mImageSelectionDatabase = new ImageSelectionDatabase(getApplicationContext());
			mImageSelectionDatabase.open();
			mImageSelectionDatabase.handleDeleteAllUrisN2R();
			mImageSelectionDatabase.close();
			if (!settingsDetails){
				boolean success = PrintHelper.StartOver();
				PrintHelper.clearDataForDoMore();
				if (!success)
				{
					new PrintHelper(getApplicationContext());
				}
				nextButton.performClick();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * private void decodeFile(File f){ try { //decode image size
	 * BitmapFactory.Options o = new BitmapFactory.Options();
	 * o.inJustDecodeBounds = true; BitmapFactory.decodeStream(new
	 * FileInputStream(f),null,o); outWidth = o.outWidth; outHeight
	 * =o.outHeight; } catch (FileNotFoundException e) {} }
	 */
	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * send the product information to Localytics
	 * @author song
	 * @created 2014-3-25
	 */
	private void sendProductInfoForLocalytics(){
		attr = new HashMap<String, String>();
		int productQuantity = 0;
		int productImages = 0;
		String productName = "0";
		String productQuantityStr = "0";
		String productImagesStr = "0";
		for (int i = 0; i < appContex.getGroupItemList().size(); i++)
		{
			if (appContex.getGroupItemList().get(i).equals(getResources().getString(R.string.orderConfirmationEstimated))) {
				continue;
			}
			productQuantity = 0;
			productImages = 0;
			productName = "0";
			productQuantityStr = "00";
			productImagesStr = "000";
			ProductInfo proTemp = null;
			if (appContex.getChildItemList().get(i).size() > 0)
			{
				for (int j = 0; j < appContex.getChildItemList().get(i).size(); j++){
					proTemp = appContex.getChildItemList().get(i).get(j);
					productQuantity += proTemp.quantity; 
					if (proTemp.productType.equals(AppConstants.BOOK_TYPE)){
						productImages += proTemp.quantity * PhotobookUtil.getPhotobookFromList(proTemp.ProductId).selectedImages.size();
					}else if (proTemp.productType.equals(AppConstants.CARD_TYPE)){
						productImages += proTemp.quantity * getCardImagesNumberByProductId (proTemp.ProductId);
					}else if (proTemp.productType.equals(AppConstants.PRINT_TYPE)){
						productImages = 1;
					}
				}
				if (appContex.getChildItemList().get(i).get(0).productType.equals(AppConstants.PRINT_TYPE)){
					productImages = 1;
				}else{
					productImages = productImages/productQuantity;
				}
				
				productName = appContex.getGroupItemList().get(i);
				//get the string value of productQuantityStr
				if (productQuantity <= 5 ){
					productQuantityStr = "0" + productQuantity;
				}else if(productQuantity >=6 && productQuantity <= 10){
					productQuantityStr = "06 - 10";
				}else if(productQuantity >=11 && productQuantity <= 15){
					productQuantityStr = "11 - 15";
				}else if(productQuantity >=16 && productQuantity <= 20){
					productQuantityStr = "16 - 20";
				}else if(productQuantity >=21 && productQuantity <= 50){
					productQuantityStr = "21 - 50";
				}else if(productQuantity >=51){
					productQuantityStr = "51+";
				}
				//get the string value of productImagesStr
				if (productImages == 0){
					productImagesStr = "000";
				}else if (productImages >=1 && productImages <= 10){
					productImagesStr = "001 - 010";
				}else if(productImages >=11 && productImages <= 20){
					productImagesStr = "011 - 020";
				}else if(productImages >=11 && productImages <= 30){
					productImagesStr = "021 - 030";
				}else if(productImages >=31 && productImages <= 40){
					productImagesStr = "031 - 040";
				}else if(productImages >=41 && productImages <= 50){
					productImagesStr = "041 - 050";
				}else if(productImages >=51 && productImages <= 60){
					productImagesStr = "051 - 060";
				}else if(productImages >=61 && productImages <= 70){
					productImagesStr = "061 - 070";
				}else if(productImages >=71 && productImages <= 80){
					productImagesStr = "071 - 080";
				}else if(productImages >=81 && productImages <= 90){
					productImagesStr = "081 - 090";
				}else if(productImages >=91 && productImages <= 100){
					productImagesStr = "091 - 100";
				}else if(productImages >=101 && productImages <= 150){
					productImagesStr = "101 - 150";
				}else if(productImages >=151 && productImages <= 200){
					productImagesStr = "151 - 200";
				}else if(productImages >=201 && productImages <= 250){
					productImagesStr = "201 - 250";
				}else if(productImages >=251 && productImages <= 300){
					productImagesStr = "251 - 300";
				}else if(productImages >=301 && productImages <= 350){
					productImagesStr = "301 - 350";
				}else if(productImages >=401){
					productImagesStr = "400+";
				}
				attr.put(PRODUCT_ID, productName);
				attr.put(PRODUCT_QUANTITY, productQuantityStr);
				attr.put(PRODUCT_IMAGES, productImagesStr);
				Log.i(TAG, productName+"----------"+productQuantityStr+"----------"+productImagesStr);
			}
			
		}
		RSSLocalytics.recordLocalyticsEvents(OrderSummaryActivity.this, EVENT_ORDER_ITEM, attr);
	}
	
	private OnClickListener gotoHome() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setClass(OrderSummaryActivity.this, MainMenu.class);
				AppManager.getAppManager().finishAllActivity();
				startActivity(intent);

			}
		};
		return listener;

	}

	private OnClickListener gotoSettings() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(OrderSummaryActivity.this, NewSettingActivity.class);	
				startActivity(myIntent);
			}
		};
		return listener;

	}

	private OnClickListener gotoInfo() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(OrderSummaryActivity.this, HelpActivity.class);
				startActivity(myIntent);

			}
		};
		return listener;

	}

	private OnClickListener openMenu() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
				// open
				mDrawerLayout.openDrawer(Gravity.LEFT);

			}
		};
		return listener;

	}

	private OnClickListener closeMenu() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
				// close
				mDrawerLayout.closeDrawer(Gravity.LEFT);

			}
		};
		return listener;

	}
	
	private int getCardImagesNumberByProductId (String productId){
		int num = 0;
		GreetingCardManager manager = null;
		for (GreetingCardManager managersTemp : AppContext.getApplication().getmGreetingCardManagers()){
			if (null == managersTemp.getGreetingCardProductCardProduct() || null == managersTemp.getGreetingCardProductCardProduct().id){
				continue;
			}
			if (managersTemp.getGreetingCardProductCardProduct().id.equals(productId)){
				manager = managersTemp;
				break;
			}	
		}
		GreetingCardPage[] pages = manager.getGreetingCardProduct().pages;
		GreetingCardPageLayer[] layers = null;
		for (int i =0;i<pages.length;i++){
			layers = pages[i].layers;
			for (int j=0;j<layers.length;j++){
				if (layers[j].getPhotoInfo()!=null){
					if(layers[j].getPhotoInfo().getPhotoSource()!=null){
						if (!layers[j].getPhotoInfo().getPhotoPath().equals("")){
							num ++;
						}
					}
				}
			}
		}
	return num;
	}
	
	private void initData (){
		
		processDialog = new ProgressDialog(OrderSummaryActivity.this);
		
	}
	
	private class SetupOrderSummary extends AsyncTask<String , Integer, Cart >{

		@Override
		protected void onPreExecute() {
			processDialog.show();
		};
		
		@Override
		protected Cart doInBackground(String... params) {
			PrintMakerWebService service = new PrintMakerWebService(OrderSummaryActivity.this, "");
			cart = service.getCartTask(PrintHelper.cartID);
			return cart;
		}
		
		@Override
		protected void onPostExecute(Cart result) {
			processDialog.dismiss();
			if (cart !=null ){

				Log.i(TAG, "setSummaryInfo()");
				Order completeOrder = new Order();
				try
				{
					if (PrintHelper.orderTime.equals(""))
					{
						String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
						orderDetailTV.setText("");
						orderSentTV.setText(currentDateTimeString);
						completeOrder.orderTime = currentDateTimeString;
					}
					else
					{
						orderSentTV.setText(PrintHelper.orderTime);
						completeOrder.orderTime = PrintHelper.orderTime;
					}
					int numGroupsWithChildren = 0;
					int totalItems = 0;
					int numLowResWarnings = 0;
					String printSize = "";
					for (int i = 0; i < appContex.getGroupItemList().size(); i++)
					{
						int count = 0;
						for (int j = 0; j < appContex.getChildItemList().get(i).size(); j++)
						{
							ProductInfo pro = appContex.getChildItemList().get(i).get(j);
							count += pro.quantity;
							if(pro.productType!=null && pro.productType.equalsIgnoreCase(AppConstants.PRINT_TYPE)){
								if (PrintHelper.isLowResWarning(pro)) {
									numLowResWarnings++;
									printSize = pro.name;
								}
							}
						}
						if (count > 0)
						{
							if (numLowResWarnings > 0)
							{
								if (prefs.getBoolean("analytics", false))
								{
									try
									{
										PrintHelper.mTracker.trackEvent("Order", "Low_Res_Items", printSize, numLowResWarnings);
										PrintHelper.mTracker.dispatch();
									}
									catch (Exception ex)
									{
										ex.printStackTrace();
									}
								}
								numLowResWarnings = 0;
							}
							numGroupsWithChildren++;
							totalItems += count;
						}
					}
					if (prefs.getBoolean("analytics", false))
					{
						try
						{
							PrintHelper.mTracker.trackEvent("Order", "Items", "Total_Items", totalItems);
							PrintHelper.mTracker.trackEvent("Order", "Items", "Line_Items", numGroupsWithChildren);
							PrintHelper.mTracker.trackEvent("Order", "Items", "Images_Selected", AppContext.getApplication().getUploadSucceedImages().size());
							PrintHelper.mTracker.dispatch();
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
					List<LineItem> lineItems = cart.pricing.lineItems;
					StringBuffer orderDetails = new StringBuffer("");
					for (LineItem lineItem : lineItems){
						orderDetails.append( lineItem.quantity + " - " + lineItem.name + "\n") ;
						if (lineItem.included !=null){
							orderDetails.append(lineItem.included.get(0).quantity + " - " + lineItem.included.get(0).name + "\n");
						}
					}
					completeOrder.orderDetails = orderDetails.toString();
					orderDetailTV.setText(orderDetails.toString());
					estimatedSubtotalTV.setText(PrintHelper.totalCost);
					orderIdTV.setText(PrintHelper.orderID); //song
					completeOrder.orderSubtotal = PrintHelper.totalCost;
					completeOrder.orderID = PrintHelper.orderID;
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				try
				{
					mapViewIV.setVisibility(View.VISIBLE);
					if (PrintHelper.cachedImage != null)
					{
						mapViewIV.setVisibility(View.GONE);
						storeNameTV.setVisibility(View.GONE);
						mTxtCityAndZip.setVisibility(View.GONE);
						storeAddressTV.setVisibility(View.GONE);
						storeNumberTV.setVisibility(View.GONE);
					}
					else
					{
						mapViewIV.setVisibility(View.GONE);
						storeNameTV.setVisibility(View.GONE);
						mTxtCityAndZip.setVisibility(View.GONE);
						storeAddressTV.setVisibility(View.GONE);
						storeNumberTV.setVisibility(View.GONE);
					}
					try
					{
						String storename="", address="", phonenumber="", personEmail="", cityAndZip="";
						if(!AppContext.getApplication().isInStoreCloud()){
							storename = prefs.getString("selectedStoreName", "");
							address = prefs.getString("selectedStoreAddress", "");
							phonenumber = prefs.getString("selectedStorePhone", "");
							personEmail = prefs.getString("email", "");
							cityAndZip = prefs.getString("selectedCity", "") + " " + prefs.getString("selectedPostalCode", "");
						} else {
							storename = "Print Place";
							Editor editor = prefs.edit();
							editor.putString("selectedStoreName", storename);
							editor.putString("selectedStoreLatitude", "50.9572449");
							editor.putString("selectedStoreLongitude", "6.9673223");
							editor.putString("selectedStoreAddress", "");
							editor.putString("selectedStorePhone", "");
							editor.putString("selectedCity", "");
							editor.putString("selectedPostalCode", "");
							editor.commit();
						}
						
						completeOrder.orderStoreName = storename;
						completeOrder.orderAddress = address;
						completeOrder.orderPhonenumber = phonenumber;
						completeOrder.orderPersonEmail = personEmail;
						
						if (storename.equals(""))
						{
							storeNameTV.setVisibility(View.GONE);
						}
						else
						{
							storeNameTV.setText(storename);
							storeNameTV.setVisibility(View.VISIBLE);
						}
						if (address.equals(""))
						{
							storeAddressTV.setVisibility(View.GONE);
						}
						else
						{
							storeAddressTV.setText(address);
							storeAddressTV.setVisibility(View.VISIBLE);
						}
						if (cityAndZip.equals("") || cityAndZip.trim().length() == 0) {
							mTxtCityAndZip.setVisibility(View.INVISIBLE);
						} else {
							mTxtCityAndZip.setText(cityAndZip);
							mTxtCityAndZip.setVisibility(View.VISIBLE);
						}
						if (phonenumber.equals(""))
						{
							storeNumberTV.setVisibility(View.GONE);
						}
						else
						{
							storeNumberTV.setText(phonenumber);
							storeNumberTV.setVisibility(View.VISIBLE);
						}
						if (personEmail != null && !personEmail.equals(""))
						{
							confirmationEmailTV.setText(personEmail);
						}
						else
						{
							confirmationEmailLabelTV.setVisibility(View.GONE);
							confirmationEmailTV.setVisibility(View.GONE);
						}
						if (storeNameTV.getVisibility() == View.GONE && storeAddressTV.getVisibility() == View.GONE && mTxtCityAndZip.getVisibility() == View.GONE && storeNumberTV.getVisibility() == View.GONE)
						{
							mapViewIV.setVisibility(View.GONE);
							//pricingEstimatedTV.setVisibility(View.GONE);
						}
						else
						{
							if (PrintHelper.orderType == 1){
								Drawable kiosk = getResources().getDrawable(R.drawable.pinpoint2);
								kiosk.setBounds(0, 0, kiosk.getIntrinsicWidth(), kiosk.getIntrinsicHeight());
								try
								{
									mapViewIV.setVisibility(View.VISIBLE);
									//pricingEstimatedTV.setVisibility(View.VISIBLE);
									mapKiosks = new PictureKiosks(kiosk, OrderSummaryActivity.this, prefs.getString("selectedStoreLatitude", ""), prefs.getString("selectedStoreLongitude", ""), prefs.getString("selectedStoreName", ""), prefs.getString(
											"selectedStoreAddress", ""), prefs.getString("selectedStorePhone", ""));
									
									completeOrder.orderLatitude = prefs.getString("selectedStoreLatitude", "");
									completeOrder.orderLongitude = prefs.getString("selectedStoreLongitude", "");
									completeOrder.oderSelectedStoreName = prefs.getString("selectedStoreName", "");
									completeOrder.orderSelectedStoreAddress = prefs.getString("selectedStoreAddress", "");
									completeOrder.orderSelectedStorePhone = prefs.getString("selectedStorePhone", "");
									completeOrder.orderSelectedCityAndZip = prefs.getString("selectedCity", "") + " " + prefs.getString("selectedPostalCode", "");
									mapViewIV.getOverlays().add(mapKiosks);
									mapViewIV.setClickable(false);
									mapViewIV.getController().setZoom(15);
									mapViewIV.invalidate();
								}
								catch (Exception ex)
								{
									Log.e(TAG, "Error adding the overlay");
								}
							}
							
						}
					}
					catch (Exception ex)
					{
						Log.e(TAG, "Exception setting store information");
					}
				}
				catch (Exception ex)
				{
					Log.e(TAG, "Exception setting store information");
				}
				if(PrintHelper.sentOrders == null){
					PrintHelper.sentOrders = new ArrayList<Order>();
				}
				if (PrintHelper.orderType == 2){
					shipping_firstName.setText(shipFirstName);
					shipping_lastName.setText(shipLastName);
					shipping_addressOne.setText(shipAddress1);
					shipping_addressTwo.setText(shipAddress2);
					shipping_city .setText(shipCity);
					shipping_zip.setText(shipZip);
					shipping_state .setText(stateShip);
				}
				PrintHelper.sentOrders.add(completeOrder);
			
			}
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * remove the "*" from the text.
	 * add by song
	 */
	private void changeSubtotalTest(){
		String subTotalText = getString(R.string.orderConfirmationSubtotal);
		subTotalText = subTotalText.replace("*", "");
		estimatedSubtotalLabelTV.setText(subTotalText);
	}
}
