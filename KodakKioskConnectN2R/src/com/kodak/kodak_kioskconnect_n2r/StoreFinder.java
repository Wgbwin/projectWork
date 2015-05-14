package com.kodak.kodak_kioskconnect_n2r;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.AppConstants;
import com.AppContext;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.kodak.kodak_kioskconnect_n2r.view.BubbleViewOverlay;
import com.kodak.utils.EfficientAdapter;
import com.kodak.utils.RSSLocalytics;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.kodak_kioskconnect_n2r.activity.ShoppingCartActivity;

public class StoreFinder extends MapActivity
{
	private final String TAG = this.getClass().getSimpleName();
	
	private ImageButton find;
	private ImageButton locate;
	private Button next;
	private Button back;
	private TextView title;
	private MyMapView mapView;
	private MapController mapcontroller;
	private EditText zip;
	private ProgressBar progress;
	private TextView mTxtSearching;
	private ListView storeList;
	private StoreListAdapter mStoreListAdapter ;
	
	private boolean storeSelected = false;
	private MyLocationOverlay whereAmI = null;
	protected SharedPreferences prefs;
	protected Editor editor;
	private Geocoder coder;
	protected TextView totalNumSelectedTV;
	private GeoPoint animateToPoint = null;
	private String productString = "";
	private InfoDialog.InfoDialogBuilder connectBuilder;
	/**
	 * the main field layout
	 */
	private FrameLayout mMapLayout;
	
	private StoreItemizedOverlay storeItemizedOverlay ;
	
	private Button vSearchScopeButton ;
	private LinearLayout vLinearLayoutSearchScopeContainer ;
	private ListView vListViewSearchScope ;	
	private SearchScopeAdapter mSearchScopeAdapter ;
	private Double[] mSearchScope = new Double[]{0.1,0.2,0.5,1.0,2.0,5.0,10.0,20.0,25.0,50.0,100.0} ;
	
	private boolean isUseMiles = true ;
	private double mSearchRadiusInMile = 10.0f;  //the radius of search scope in mile
	
	private final double MILE_EACH_LATITA  = 69.0f ;
	private final double MILE_TO_KILOMETER_FACTOR = 1.609344f ;
	
	private int deviceWidth;
	
	private HashMap<Integer, String> alphaNumMap = new HashMap<Integer, String>();
	private Timer mTimer;
	private TimerTask mTask;
	
	private final int STARTLOCATE = 8;
	private final int UNABLELOCATE = 9;
	private final int TEST_STORES_STATUS_CHANGED = 10;
	
	private String packName = "";
	private int currentPosition = -1;
	private String addressName = "";
	private boolean isNeedNotFindStores ;
	
	private final String SCREEN_NAME = "Store Locator";
	private final String EVENT = "Store Changed";
	private boolean isStoreChanged = false;
	private static final int MSG_FIND_STORE_FOR_MY_LOCATION = 1 ;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == MSG_FIND_STORE_FOR_MY_LOCATION ){
				findStoreByMyLocation() ;
			}
			
		};
	} ;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		RSSLocalytics.onActivityCreate(this);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.storefinder);
		initAlphaNumMap();
		if(getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString("productStringCheckStore") != null)
			productString = getIntent().getExtras().getString("productStringCheckStore");
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		deviceWidth = dm.widthPixels;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		editor = prefs.edit();
		totalNumSelectedTV = (TextView) findViewById(R.id.totalSelectedTextView);
		totalNumSelectedTV.setVisibility(View.GONE);
		String countryCode = prefs.getString(MainMenu.CurrentlyCountryCode, "");
		Locale locale = null;
		if("".equals(countryCode)){
			locale = Locale.getDefault();
		} else {
			for(Locale mLocale : Locale.getAvailableLocales()){
				if(mLocale.getCountry().toLowerCase().equals(countryCode.toLowerCase())){
					locale = mLocale;
					break;
				}
			}
			if(locale == null){
				locale = Locale.getDefault();
			}
		}
		if(locale == null){
			coder = new Geocoder(this);
		} else {
			coder = new Geocoder(this, locale);
		}
		
		if("US".equalsIgnoreCase(countryCode)||"GB".equalsIgnoreCase(countryCode)||"".equals(countryCode)){
			isUseMiles = true ;
			
		}else {
			isUseMiles = false ;
		}
		
//		fieldLayout = (RelativeLayout) findViewById(R.id.RelativeLayout01);
		mMapLayout = (FrameLayout) findViewById(R.id.mapFL);
		back = (Button) findViewById(R.id.backButton);
		title = (TextView) findViewById(R.id.headerBarText);
		if(PrintHelper.wififlow){
			title.setText(getString(R.string.storefinder_wifititle));
		} else {
			title.setText(getString(R.string.storefinder));
		}
		back.setVisibility(android.view.View.INVISIBLE);
		find = (ImageButton) findViewById(R.id.imageButtonSearch);
		locate = (ImageButton) findViewById(R.id.imageButtonLocate);
		mapView = (MyMapView) findViewById(R.id.mapview);
		
		vSearchScopeButton = (Button) findViewById(R.id.search_scope_btn) ;
		vSearchScopeButton.setTypeface(PrintHelper.tf);
		vLinearLayoutSearchScopeContainer = (LinearLayout) findViewById(R.id.search_scope_container) ;
		vListViewSearchScope = (ListView) findViewById(R.id.search_scope_list) ;
		mSearchScopeAdapter = new SearchScopeAdapter(getApplicationContext(), mSearchScope) ;
		vListViewSearchScope.setAdapter(mSearchScopeAdapter) ;
		
		String unit = "" ;
		if(isUseMiles){
			unit = getString(R.string.mile) ;
		}else {
			unit = getString(R.string.kilometer) ;
		}

		vSearchScopeButton.setText(mSearchRadiusInMile+" "+unit) ;
		
//		zoomTheMap(mSearchRadiusInMile);
	
		mapcontroller = mapView.getController();
		zip = (EditText) findViewById(R.id.editText1);
		AppContext.getApplication().setEmojiFilter(zip);
		storeList = (ListView) findViewById(R.id.listView1);
		mStoreListAdapter = new StoreListAdapter(getApplicationContext(), null) ;
		storeList.setAdapter(mStoreListAdapter) ;
		
		
		
		next = (Button) findViewById(R.id.nextButton);
		progress = (ProgressBar) findViewById(R.id.progressBar1);
		mTxtSearching = (TextView) findViewById(R.id.txt_searchingLocation);
		mapView.setBuiltInZoomControls(false);
		whereAmI = new MyLocationOverlay(this, mapView);
		whereAmI.enableMyLocation();
		mapView.getOverlays().add(whereAmI);
		zoomTheMap(mSearchRadiusInMile) ;
//		mapcontroller.setZoom(12);
		GeoPoint myLocation = whereAmI.getMyLocation() ;
		if(myLocation!=null){
			mapcontroller.animateTo(whereAmI.getMyLocation());
		}
		
		mapView.postInvalidate(); 
		
		next.setText(getString(R.string.done));
		title.setTypeface(PrintHelper.tf);
		next.setTypeface(PrintHelper.tf);
		Drawable kiosk = getResources().getDrawable(R.drawable.pinpoint2);
		storeItemizedOverlay = new StoreItemizedOverlay(kiosk, mapView) ;

		setListeners() ;
		
	}
	
	
	private void setListeners(){
		find.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String key = zip.getText().toString().trim();
				if(null != key && !"".equals(key)){
					findStores(key);
				}else{
					findStores(mapView.getMapCenter());
				}
			}
		});
		locate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(whereAmI.isMyLocationEnabled() && whereAmI.getMyLocation()!=null){
					
					mapcontroller.animateTo(whereAmI.getMyLocation());
					zoomTheMap(mSearchRadiusInMile);
//					mapcontroller.setZoom(15);
					findStores(whereAmI.getMyLocation());
				}else{
					//TODO find by default location
				}
				
			}
		});
		next.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				Store temp = null;
				if(isStoreChanged){
					RSSLocalytics.recordLocalyticsEvents(StoreFinder.this, EVENT);
				}
				if (storeList.getAdapter() == null || PrintHelper.stores == null || PrintHelper.stores.size() == 0)
				{
					finish();
				}
				else if (PrintHelper.selectedStorePos < 0)
				{
					finish();
				}
				else
				{
					if(PrintHelper.wififlow){
						finish();
						return;
					}
					if (PrintHelper.stores.size() > PrintHelper.selectedStorePos)
					{
						temp = PrintHelper.stores.get(PrintHelper.selectedStorePos);
					}
					if (temp != null)
					{
						//add by song
						if (temp.name != null && !temp.name.equals("")){
							editor.putString("selectedStoreName", temp.name);
						}else{
							editor.putString("selectedStoreName", "");
						}
						if (temp.address1 != null && !temp.address1.equals("")){
							editor.putString("selectedStoreAddress", temp.address1);
						}else{
							editor.putString("selectedStoreAddress", "");
						}
						if (temp.city != null && !temp.city.equals("")) {
							editor.putString("selectedCity", temp.city + (temp.stateProvince == null ? "" : ", " + 
									temp.stateProvince));
						} else {
							editor.putString("selectedCity", "");
						}
						if (temp.postalCode != null && !temp.postalCode.equals("")) {
							editor.putString("selectedPostalCode", temp.postalCode);
						} else {
							editor.putString("selectedPostalCode", "");
						}
						if (temp.email != null && !temp.email.equals("")){
							editor.putString("selectedStoreEmail", temp.email);
						}else{
							editor.putString("selectedStoreEmail", "");
						}
						if(temp.country != null && !temp.country.equals("")){
							editor.putString("selectedStoreCountry", temp.country);
						}else {
							editor.putString("selectedStoreCountry", "");
						}
						if (temp.phone != null && !temp.phone.equals("")){
							editor.putString("selectedStorePhone", temp.phone);
						}else{
							editor.putString("selectedStorePhone", "");
						}
						if (temp.latitude != null && !temp.latitude.equals("")){
							editor.putString("selectedStoreLatitude", temp.latitude);
						}else{
							editor.putString("selectedStoreLatitude", "");
						}
						if (temp.longitude != null && !temp.longitude.equals("")){
							editor.putString("selectedStoreLongitude", temp.longitude);
						}else{
							editor.putString("selectedStoreLongitude", "");
						}
						if (temp.id != null && !temp.id.equals("")){
							editor.putString("selectedStoreId", temp.id);
						}else{
							editor.putString("selectedStoreId", "");
						}
						if (temp.retailerID != null && !temp.retailerID.equals("")){
							editor.putString("selectedRetailerId", temp.retailerID);
							editor.putString("selectedRetailerInfo", "");
						}else{
							editor.putString("selectedRetailerId", "");
						}
						if (temp.getHoursMap() != null && temp.getHoursMap().size() > 0) {
							editor.putString("selectedStoreHours", getStoreHours(temp));
						} else {
							editor.putString("selectedStoreHours", "");
						}
						editor.commit();
					}
					try
					{
						if (prefs.getBoolean("analytics", false) && storeSelected)
						{
							PrintHelper.mTracker.trackEvent("Settings", "*Store_Changed", "", 0);
							PrintHelper.mTracker.dispatch();
						}
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						progress.setVisibility(View.INVISIBLE);
						mTxtSearching.setVisibility(View.INVISIBLE);
					}
					finish();
				}
			}
		});
		
		storeList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(final AdapterView<?> parentView, View selectedItemView, int position, long id)
			{
				isStoreChanged = true;
				// if the scopeContainer is visible ,set it invisible
				if(vLinearLayoutSearchScopeContainer.getVisibility()== View.VISIBLE){
					vLinearLayoutSearchScopeContainer.setVisibility(View.INVISIBLE) ;
				}
				
				if(storeItemizedOverlay.size()!=0){
					List<Overlay> overlays = mapView.getOverlays() ;
					Iterator<Overlay> iter = overlays.iterator() ;
					while (iter.hasNext()) {
						Overlay overlay = iter.next() ;
						if(overlay!=null && !(overlay instanceof MyLocationOverlay) ){
							iter.remove() ;
						}
					}
					mapView.getOverlays().add(storeItemizedOverlay);
					
				}
				
				currentPosition = position;
				PrintHelper.selectedStorePos = position;
				PrintHelper.selectedStore = PrintHelper.stores.get(position);
				storeSelected =true ;
				mStoreListAdapter.notifyDataSetChanged();
				
				if (!PrintHelper.stores.isEmpty()) {
					
					storeItemizedOverlay.doOnTapEvent(position) ;
					
				} else {
					Toast.makeText(StoreFinder.this, "sorry , something wrong with searching", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		zip.setOnEditorActionListener(new OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == EditorInfo.IME_ACTION_DONE)
				{
					String keyInfo = zip.getText().toString().trim();
					if (null == keyInfo || keyInfo.equals("")){
						findStores(mapView.getMapCenter());
					}else {
						findStores(zip.getText().toString().trim());
					}
				}
				return false;
			}
		});
		zip.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (!hasFocus)
				{
				} 
			}
		});
		
		vSearchScopeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(vLinearLayoutSearchScopeContainer.getVisibility()==View.INVISIBLE||vLinearLayoutSearchScopeContainer.getVisibility()==View.GONE) {
					vLinearLayoutSearchScopeContainer.setVisibility(View.VISIBLE) ;
				}else if(vLinearLayoutSearchScopeContainer.getVisibility()== View.VISIBLE){
					vLinearLayoutSearchScopeContainer.setVisibility(View.INVISIBLE) ;
				}
				
				
			}
		}) ;
		
		vListViewSearchScope.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
	     		double searchRadius = mSearchScope[position] ;
				String unit="" ;
				if(isUseMiles){
					unit = getString(R.string.mile) ;
				}else {
					unit = getString(R.string.kilometer) ;
				}
				vSearchScopeButton.setText(searchRadius+" "+unit) ;
				
				vLinearLayoutSearchScopeContainer.setVisibility(View.INVISIBLE) ;
				//change the map
				if(isUseMiles){
					
					mSearchRadiusInMile = searchRadius ;
					
				}else {
					mSearchRadiusInMile = searchRadius/MILE_TO_KILOMETER_FACTOR ;
				}
				zoomTheMap(mSearchRadiusInMile) ;
				GeoPoint centerPoint = mapView.getMapCenter() ;
				findStores(centerPoint) ;
				
				
			}
			
			
			
		}) ;
		
		
		mapView.setOnMapDragEndListener(new MyMapView.OnMapDragEndListener() {

			@Override
			public void onMapDragEnd(GeoPoint oldMapCenter,
					GeoPoint newMapCenter) {
				Log.i(TAG,"on map drag end");
				
				if(storeItemizedOverlay!=null && storeItemizedOverlay.isBubbleVisible()){
					try {
						mapZoomChangeTheScope() ;
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else {
					if(isNeedNotFindStores){
						   isNeedNotFindStores = !isNeedNotFindStores ;
					   }else {
						   try {
								mapZoomChangeTheScope() ;
							} catch (ParseException e) {
								e.printStackTrace();
							}
							findStores(newMapCenter); 
					   }
				}
				
			}
			
		});
	}
	
  
	/**
	 * 
	 */
	private void zoomTheMap(double scopeRadiusInMile){
		int latitudeSpan =  (int) (scopeRadiusInMile/MILE_EACH_LATITA*2*1e6) ;
//		Log.v("map", "sunnymap: "+latitudeSpan) ;
		mapcontroller.zoomToSpan(latitudeSpan,latitudeSpan) ;
	}
	
	/**
	 * after the mapview zoomed, the scope value changed
	 * @throws ParseException 
	 */
	private void mapZoomChangeTheScope() throws ParseException{
		int latitudeSpan = mapView.getLatitudeSpan() ;
		double mapviewHeightInMile = latitudeSpan/1e6*69 ;
		mSearchRadiusInMile=mapviewHeightInMile/2 ;
		java.text.DecimalFormat df = new java.text.DecimalFormat("0.0");
		mSearchRadiusInMile = df.parse(df.format(mSearchRadiusInMile)).doubleValue() ;
		String unit="" ;
		double showNum = 0.0f ;
		if(isUseMiles){
			unit = getString(R.string.mile) ;
			showNum = mSearchRadiusInMile;
		}else {
			unit = getString(R.string.kilometer) ;
		    showNum = df.parse(df.format(mSearchRadiusInMile*MILE_TO_KILOMETER_FACTOR)).doubleValue()  ;
		}
	
		vSearchScopeButton.setText(showNum+" "+unit) ;
		
	}
	
	
	
	private void initAlphaNumMap() {
		alphaNumMap.put(1, "A");alphaNumMap.put(2, "B");alphaNumMap.put(3, "C");alphaNumMap.put(4, "D");alphaNumMap.put(5, "E");
		alphaNumMap.put(6, "F");alphaNumMap.put(7, "G");alphaNumMap.put(8, "H");alphaNumMap.put(9, "I");alphaNumMap.put(10, "J");
		alphaNumMap.put(11, "K");alphaNumMap.put(12, "L");alphaNumMap.put(13, "M");alphaNumMap.put(14, "N");alphaNumMap.put(15, "O");
		alphaNumMap.put(16, "P");alphaNumMap.put(17, "Q");alphaNumMap.put(18, "R");alphaNumMap.put(19, "S");alphaNumMap.put(20, "T");
		alphaNumMap.put(21, "U");alphaNumMap.put(22, "V");alphaNumMap.put(23, "W");alphaNumMap.put(24, "X");alphaNumMap.put(25, "Y");
		alphaNumMap.put(26, "Z");
	}
	
	
	private Handler findStore = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == STARTLOCATE) {
//				Toast.makeText(StoreFinder.this, "start to locate!", Toast.LENGTH_SHORT).show();
				if (mTimer != null) {
					mTimer.cancel();
					mTimer = null;
				}
				if (mTask != null) {
					mTask = null;
				}
			} else if (msg.what == UNABLELOCATE) {
				if (progress != null)
				{
					progress.setVisibility(View.INVISIBLE);
					mTxtSearching.setVisibility(View.INVISIBLE);
				}
				if (mTimer != null) {
					mTimer.cancel();
					mTimer = null;
				}
				if (mTask != null) {
					mTask = null;
				}	
				
				//add an error handle by sunny
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(StoreFinder.this);
				builder.setMessage(getString(R.string.locate_failed));
				builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
				if(!StoreFinder.this.isFinishing()){
					builder.create().show();
				}
				
				
			} else if(msg.what == TEST_STORES_STATUS_CHANGED){
				if(progress!=null){
					progress.setVisibility(View.INVISIBLE);
					mTxtSearching.setVisibility(View.INVISIBLE);
					zip.setText("");
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(StoreFinder.this);
					if(prefs.getBoolean(PrintHelper.INCLUDE_TEST_STORES, false)){
						mMapLayout.setBackgroundColor(Color.RED);
					} else {
						mMapLayout.setBackgroundDrawable(null);
					}
				}
			} else {
				try
				{
					if (progress != null)
					{
						progress.setVisibility(View.INVISIBLE);
						mTxtSearching.setVisibility(View.INVISIBLE);
					}
				}
				catch (Exception ex) {
					progress.setVisibility(View.INVISIBLE);
					mTxtSearching.setVisibility(View.INVISIBLE);
				}
				try
				{
					if (PrintHelper.stores.size() > 0)
					{
						List<Overlay> overlays = mapView.getOverlays() ;
						Iterator<Overlay> iter = overlays.iterator() ;
						while (iter.hasNext()) {
							Overlay overlay = iter.next() ;
							if(overlay!=null && !(overlay instanceof MyLocationOverlay) ){
								iter.remove() ;
							}
						}
						storeItemizedOverlay.clearAllStoreMark() ;
						
						for (Store str : PrintHelper.stores)
						{
							String phone = "";
							if (str.phone != null) {
								phone = str.phone;
							} 
							int lat = (int) (Float.parseFloat(str.latitude) * 1000000);
							int longi = (int) (Float.parseFloat(str.longitude) * 1000000);
							GeoPoint geopoint = new GeoPoint(lat, longi);
							
							String snipple = "" ;
							if(packName.contains("dm")){
								snipple = "" + str.address1 + "\n" + str.postalCode + " " + str.city + "\n" + phone ;
							} else {
								snipple = "" + str.address1 + "\n" + str.city + (str.stateProvince==null?"":", "+str.stateProvince) + " " + str.postalCode + "\n" + phone;
							}
							
							
							OverlayItem overlayItem = new OverlayItem(geopoint, str.name, snipple);
							storeItemizedOverlay.addOverlay(overlayItem) ;
							
						}
						mapView.getOverlays().add(storeItemizedOverlay);
						mStoreListAdapter.setDataSource(PrintHelper.stores) ;
						
						storeSelected = false;
					}else{
						mStoreListAdapter.setDataSource(PrintHelper.stores) ;
						Boolean ifCanFollowCLOLite = prefs.getBoolean("ifCanFollowCLOLite", false);
						Boolean ifFollowCLO = prefs.getBoolean("ifFollowCLO", false);
						Boolean isFromShoppingcart = getIntent().getBooleanExtra(AppConstants.IS_FORM_SHOPPINGCART, false);
						String appName = StoreFinder.this.getApplicationContext().getPackageName();
						Boolean ifMyKokdaMoments = appName.contains(MainMenu.KODAK_COMBINED_PACKAGE_NAME);
						if (ifMyKokdaMoments && isFromShoppingcart && ifCanFollowCLOLite && !ifFollowCLO){//CLO flow							
							noFindStroeDialogCLOShow ();							
						}else {
							noFindStroeDialogShow();
						}
							
						
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					progress.setVisibility(View.INVISIBLE);
					mTxtSearching.setVisibility(View.INVISIBLE);
				}
				
				try
				{
					storeList.setVisibility(View.VISIBLE);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					progress.setVisibility(View.INVISIBLE);
					mTxtSearching.setVisibility(View.INVISIBLE);
				}

				if(animateToPoint != null)
				{
					mapcontroller.animateTo(animateToPoint);
					animateToPoint = null ;
				}
				else
				{
					try
					{
						if( PrintHelper.stores.size()>0){
							Store firstStroe = PrintHelper.stores.get(0) ;
							int lat = (int) (Float.parseFloat(firstStroe.latitude) * 1E6);
							int longi = (int) (Float.parseFloat(firstStroe.longitude) * 1E6);
							animateToPoint= new GeoPoint(lat, longi);
//							LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//							animateToPoint = new GeoPoint((int)(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude() * 1E6),(int)(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude() * 1E6));
							mapcontroller.animateTo(animateToPoint);
							animateToPoint = null ;
						}
						
					}
					catch(Exception ex)
					{
						Log.e(TAG, "Error Found while handling animation point", ex);

						progress.setVisibility(View.INVISIBLE);
						mTxtSearching.setVisibility(View.INVISIBLE);
					}
				}
			}
			
		}
	};

	private String getStoreHours(Store store) {
		String result = "";
		HashMap<Integer, String> hoursMap = store.getHoursMap();
		if (hoursMap.size() != 0) {
			Iterator<Entry<Integer, String>> iter = hoursMap.entrySet().iterator();
			int count = 0;
			while (iter.hasNext()) {
				Entry<Integer, String> entry = (Entry<Integer, String>) iter.next();
				String value = entry.getValue();
				if (value != null) {
					if (count < hoursMap.size() - 1) {
						result += value + "\n";
					} else {
						result += value;
					}
				} 
				count ++;
			}
		}
		return result;
	}
	
	class StoreHolder {
		TextView storeName;
		TextView storeDetails;
		TextView storeDistance;
		TextView storeIndex;
		TextView storeHours;
		RelativeLayout layout2;
	}
	
	
	/**
	 * store list adpater
	 * @author sunny
	 *
	 */
	class StoreListAdapter extends EfficientAdapter<Store>{

		public StoreListAdapter(Context context, List<Store> dataList) {
			super(context, dataList);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected int getItemLayout() {
			// TODO Auto-generated method stub
			return R.layout.storeitem;
		}

		@Override
		protected void initView(View v) {
			ViewHolder holder = new ViewHolder() ;
			holder.storeName = (TextView) v.findViewById(R.id.storeNameTextField);
			holder.storeDetails = (TextView) v.findViewById(R.id.storeDetailsTextField);
			holder.storeDistance = (TextView) v.findViewById(R.id.distanceTextField);
			holder.storeIndex = (TextView) v.findViewById(R.id.storeLogoImageView);
			holder.storeHours = (TextView) v.findViewById(R.id.txt_storehours);
			holder.layout2 = (RelativeLayout) v.findViewById(R.id.linearlayout2);
			
			holder.storeName.setTypeface(PrintHelper.tfb);
			holder.storeDetails.setTypeface(PrintHelper.tf);
			holder.storeDistance.setTypeface(PrintHelper.tf);
			
			v.setTag(holder);
			
		}

		@Override
		protected void bindView(View v, Store data, int position) {
			if(data==null){
				return  ;
			}
			
			final ViewHolder holder = (ViewHolder) v.getTag();
			
			if (position < 25) {
				holder.storeIndex.setText(alphaNumMap.get(position + 1));
			}
		
			holder.storeName.setText("" +data.name);
			if(data.isATestStore){
				holder.storeName.setTextColor(Color.RED);
			} else {
				holder.storeName.setTextColor(0xFFFBBA06);
			}
			String details = "" ;
			String phone="";
			if(data.phone!=null &&!"".equals(data.phone)){
				phone = data.phone ;
			}
			if(packName.contains("dm")){
				details = "" + data.address1 + "\n" + data.postalCode + " " + data.city + "\n" + phone ;
			} else {
				details = "" + data.address1 + "\n" + data.city + (data.stateProvince==null?"":", "+data.stateProvince) + " " + data.postalCode + "\n" + phone ;
			}
			holder.storeDetails.setText(details);
			
			String unit = "" ;
			java.text.DecimalFormat df = new java.text.DecimalFormat("###.##");
			double distance = 0.0f ;
			if(!isUseMiles){//KM
				distance =Double.valueOf(data.miles) * MILE_TO_KILOMETER_FACTOR;
				unit = getString(R.string.kilometer) ;
			}else {//Mile
				distance =Double.valueOf(data.miles) ;
				unit = getString(R.string.mile) ;
			}
			
			try {
				holder.storeDistance.setText("(" + (df.parse(df.format(distance)).doubleValue()) + " "+unit+")");
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				holder.storeDistance.setText("(" + distance+" "+unit+")");
			}
			if (position != currentPosition) {
				holder.layout2.setVisibility(View.GONE);
				holder.storeHours.setText("");
			} else {
				String businessHours = getStoreHours(data)  ;
				if (!"".equals(businessHours)) {
					if (holder.layout2.getVisibility() == View.GONE) {
						holder.layout2.setVisibility(View.VISIBLE);
						holder.storeHours.setText(businessHours);
					} else {
						holder.layout2.setVisibility(View.GONE);
					}
				} else {
					holder.layout2.setVisibility(View.GONE);
				}
			}
			
			
			
			if (position == PrintHelper.selectedStorePos)
			{
				v.setBackgroundResource(R.drawable.highlight_change);
			}
			else
			{
				v.setBackgroundColor(Color.TRANSPARENT);
			}
			
			
		}
		
		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			return super.getDropDownView(position, convertView, parent);
		}
		
		class ViewHolder {
			TextView storeName;
			TextView storeDetails;
			TextView storeDistance;
			TextView storeIndex;
			TextView storeHours;
			RelativeLayout layout2;
		}
		
	}
	
	/**
	 * the simple adapte for seachscope listview 
	 * @author sunny
	 *
	 */
	class SearchScopeAdapter extends EfficientAdapter<Double>{

		public SearchScopeAdapter(Context context, Double[] mSearchScope) {
			super(context, mSearchScope);
			
		}

		@Override
		protected int getItemLayout() {
			// TODO Auto-generated method stub
			return R.layout.simple_list_item_for_text;
		}

		@Override
		protected void initView(View v) {
			ViewHolder holder = new ViewHolder() ;
			holder.vText = (TextView) v.findViewById(R.id.textview01) ;
			holder.vText.setTypeface(PrintHelper.tf);
			holder.vText.setTextColor(getResources().getColor(android.R.color.white));
		    v.setTag(holder) ; 
			
		}

		@Override
		protected void bindView(View v, Double data, int position) {
			if(data == null){
				return ;
			}
			final ViewHolder holder = (ViewHolder) v.getTag();
			String unit = "" ;
			if(isUseMiles){
				unit = getString(R.string.mile) ;
			}else {
				unit = getString(R.string.kilometer) ;
			}
			holder.vText.setText(data+" "+unit) ;
			
		}
		class ViewHolder{
			TextView vText ;
		}
	}
	
	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Auto-generated method stub
		return false;
	}

	// validate zip
	public static boolean validateZip(String zip)
	{
		return zip.matches("\\d{5}");
	} // end method validateZip

	/**
	 * StoreItemizedOverlay class
	 * 
	 * @author sunny
	 *
	 */
	class StoreItemizedOverlay extends BubbleViewOverlay<OverlayItem>{
		private List<OverlayItem>  m_overlays = new ArrayList<OverlayItem>();
		private Drawable marker;
		
		public StoreItemizedOverlay(Drawable defaultMarker, MapView mapView) {
			super(defaultMarker, mapView);
			this.marker = defaultMarker ;
			
			setBubbleBottomOffset(30);
		}
		
		public void addOverlay(OverlayItem overlay) {
		    m_overlays.add(overlay);
		    populate();
		}

		@Override
		protected OverlayItem createItem(int arg0) {
			// TODO Auto-generated method stub
			return m_overlays.get(arg0);
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return m_overlays.size();
		}
		
		public void clearAllStoreMark(){
			if(m_overlays!=null){
				m_overlays.clear() ;
				super.hideAllBubbles() ;
			}
		}
		
		
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			// TODO Auto-generated method stub

			super.draw(canvas, mapView, shadow);
			
			boundCenterBottom(marker);
			
			// Paint
			Paint paint = new Paint();
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setStyle(Style.FILL);
			
			for (int index = 0; index < size(); index++)
			{
				OverlayItem item = createItem(index);
				// Converts lat/lng-Point to coordinates on the screen
				GeoPoint point = item.getPoint();
				// Converts lat/lng-Point to OUR coordinates on the screen.
				Point myScreenCoords = new Point();
				mapView.getProjection().toPixels(point, myScreenCoords);
				paint.setStrokeWidth(1);
				paint.setARGB(255, 255, 255, 255);
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				paint.setTextSize(20);
				paint.setTypeface(PrintHelper.tfb);
				paint.setColor(Color.WHITE);
				paint.setStrokeWidth(1);
				if (index < 25) {
					if (deviceWidth < 960) {
						canvas.drawText(alphaNumMap.get(index + 1), myScreenCoords.x,
								 myScreenCoords.y - 17, paint);
					} else {
						canvas.drawText(alphaNumMap.get(index + 1), myScreenCoords.x,
								 myScreenCoords.y - 22, paint);
					}
				}
			}
		
		}
		
		@Override
		public boolean onTap(int index) {
			// TODO Auto-generated method stub
			
			isNeedNotFindStores = true ;
			currentPosition = index;
			PrintHelper.selectedStorePos = index;
			PrintHelper.selectedStore = PrintHelper.stores.get(index);
			storeSelected =true ;
			mStoreListAdapter.notifyDataSetChanged();
			return super.onTap(index);
		}
		
		
		@Override
		protected void onBubbleOpen(int index) {
			// TODO Auto-generated method stub
			super.onBubbleOpen(index);
			
			//we should zoom out map 
			
			if(mSearchRadiusInMile> 1.0){
				mapcontroller.setZoom(15);
			
				try {
					mapZoomChangeTheScope() ;
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		
		@Override
		protected void animateTo(int index, GeoPoint center) {
			Point screenPoint = null ;
			screenPoint =  mapView.getProjection().toPixels(center, screenPoint) ;
			screenPoint.y = screenPoint.y-30 ;
			
			GeoPoint center1 =   mapView.getProjection().fromPixels(screenPoint.x, screenPoint.y) ;
			if(center1!=null){
				mapcontroller.animateTo(center1) ;
			}
			
		}
		
		
		@Override
		protected boolean onBubbleTap(int index, OverlayItem item) {
    
			hideBubble() ;
			
			return super.onBubbleTap(index, item);
		}
		
	}

	private void executeLocate() {
		if (progress != null) {
			progress.setVisibility(View.VISIBLE);
			mTxtSearching.setVisibility(View.VISIBLE);
		}
			
		boolean locationAvailable = whereAmI.enableMyLocation();
		if (locationAvailable)
		{
			if (whereAmI.getMyLocation() == null)
			{
				if (mTimer != null) {
					mTimer.cancel();
					mTimer = null;
				}
				if (mTask != null) {
					mTimer = null;
				}
				mTimer = new Timer();
				mTask = new TimerTask() {
					
					@Override
					public void run() {
						Message message = new Message();
						message.what = UNABLELOCATE;
						findStore.sendMessage(message);
					}
				};
				mTimer.schedule(mTask, 20000);
				whereAmI.runOnFirstFix(new Runnable()
				{
					@Override
					public void run()
					{ 
						if(listThreadFindStores.size()==0){
							Message msg = new Message() ;
							msg.what = MSG_FIND_STORE_FOR_MY_LOCATION ;
							handler.sendMessage(msg) ;
//							findStoreByMyLocation();
						}
					}
				});
			}
			else
			{
				if(listThreadFindStores.size()==0){
					findStoreByMyLocation();
				}
			}
		}
		else
		{
			if (progress != null) {
				progress.setVisibility(View.INVISIBLE);
				mTxtSearching.setVisibility(View.INVISIBLE);
			}
				
			InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(StoreFinder.this);
			builder.setMessage(getString(R.string.locate_failed));
			builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			
			builder.create().show();
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		RSSLocalytics.onActivityResume(this);
		packName = getApplication().getPackageName();
		zip.setImeOptions(EditorInfo.IME_ACTION_DONE);
		if(prefs == null){
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
		}
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
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				progress.setVisibility(View.INVISIBLE);
				mTxtSearching.setVisibility(View.INVISIBLE);
			}
		}
		
		if (!Connection.isConnected(StoreFinder.this))
		{
			connectBuilder = new InfoDialog.InfoDialogBuilder(StoreFinder.this);
			connectBuilder.setTitle("");
			connectBuilder.setMessage(getString(R.string.nointernetconnection));
			connectBuilder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					connectBuilder = null;
				}
			});
			connectBuilder.setNegativeButton("", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			connectBuilder.setCancelable(false);
			connectBuilder.create().show();
		} 
		else if (PrintHelper.stores == null || PrintHelper.stores.size() <= 0)
		{
			executeLocate();
		}
		else
		{
			if (progress != null) {
				progress.setVisibility(View.VISIBLE);
				mTxtSearching.setVisibility(View.VISIBLE);
			}
				
			List<Overlay> overlays = mapView.getOverlays() ;
			Iterator<Overlay> iter = overlays.iterator() ;
			while (iter.hasNext()) {
				Overlay overlay = iter.next() ;
				if(overlay!=null && !(overlay instanceof MyLocationOverlay) ){
					iter.remove() ;
				}
			}
			storeItemizedOverlay.clearAllStoreMark() ;
			for (Store str : PrintHelper.stores)
			{
				String phone = "";
				if (str.phone != null) {
					phone = str.phone;
				} 
				int lat = (int) (Float.parseFloat(str.latitude) * 1000000);
				int longi = (int) (Float.parseFloat(str.longitude) * 1000000);
				GeoPoint geopoint = new GeoPoint(lat, longi);
				
				//TODO
				String snipple = "" ;
				if(packName.contains("dm")){
					snipple = "" + str.address1 + "\n" + str.postalCode + " " + str.city + "\n" + phone ;
				} else {
					snipple = "" + str.address1 + "\n" + str.city + (str.stateProvince==null?"":", "+str.stateProvince) + " " + str.postalCode + "\n" + phone;
				}
				
				OverlayItem overlayItem = new OverlayItem(geopoint, str.name, snipple);
				storeItemizedOverlay.addOverlay(overlayItem) ;
				
			}
			mapView.getOverlays().add(storeItemizedOverlay);
			mStoreListAdapter.setDataSource(PrintHelper.stores) ;
			storeSelected = false;
			
			if (progress != null) {
				progress.setVisibility(View.INVISIBLE);
				mTxtSearching.setVisibility(View.INVISIBLE);
			}
				
			/*
			 * for(int i=0;i<PrintHelper.stores.size();i++) { Store str =
			 * (S)storeList.getItemAtPosition(i); }
			 */
		}
		if (prefs.getBoolean("analytics", false))
		{
			try
			{
				PrintHelper.mTracker.trackPageView("Page-Store_Locator");
				PrintHelper.mTracker.dispatch();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				progress.setVisibility(View.INVISIBLE);
				mTxtSearching.setVisibility(View.INVISIBLE);
			}
		}
		if(prefs.getBoolean(PrintHelper.INCLUDE_TEST_STORES, false)){
			mMapLayout.setBackgroundColor(Color.RED);
		} else {
			mMapLayout.setBackgroundDrawable(null);
		}
	}

	@Override
	public void onPause()
	{
		RSSLocalytics.onActivityPause(this);
		super.onPause();
		if (progress != null) {
			progress.setVisibility(View.INVISIBLE);
			mTxtSearching.setVisibility(View.INVISIBLE);
		}
			
		if (whereAmI != null)
			whereAmI.disableMyLocation();
	}
	/**
	 * 1,if can not find store will pup up a dialog to ask user
	 * 2,is come from shopping cart screen
	 * 3,is for united kingdom
	 * to change order type to shipping home.
	 * add : song 
	 * date : 2013-12-06
	 */
	private void noFindStroeDialogCLOShow (){
		String countryCode = "";
		String addressInfo = "";
		if (null != addressName && !addressName.equals("")){
			addressInfo = "in " + addressName;
			countryCode = getTheCoutryCodeByaddress(addressName);
		}
		if (null != countryCode && !countryCode.equals("")){
			addressInfo = addressInfo + " " + countryCode;
		}	
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(StoreFinder.this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.StoreFinder_CLOLite).replace("%%", addressInfo));
		builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				
				dialog.dismiss();
				Intent intent = new Intent();
				intent.setClass(StoreFinder.this, ShoppingCartActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("isFromStroeFinder", true);
				editor.putBoolean("ifFollowCLO", true);
				editor.commit();
				startActivity(intent);
				StoreFinder.this.finish();
			}
		});
		builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	/**
	 * 1,if can not find store will pup up a dialog to ask user
	 * 2,is not come from shopping cart screen.
	 *  3,is for united kingdom
	 * to change order type to shipping home.
	 * add : song 
	 * date : 2013-12-06
	 */
	private void errorFindStroeDialogShow(){
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(StoreFinder.this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.StoreFinder_ProblemFindingStore));
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	/**
	 * 1,if can not find store will pup up a dialog to ask user
	 * 2,is not for united kingdom
	 * to change order type to shipping home.
	 * add : song 
	 * date : 2013-12-06
	 */
	private void noFindStroeDialogShow(){
		String countryCode = "";
		String addressInfo = "";
		if (null != addressName && !addressName.equals("")){
			addressInfo = "in " + addressName;
			countryCode = getTheCoutryCodeByaddress(addressName);
		}
		if (null != countryCode && !countryCode.equals("")){
			addressInfo = addressInfo + " " + countryCode;
		}	
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(StoreFinder.this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.nostores).replace("%%", addressInfo));
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	
	/**
	 * get the country code by address.
	 * add : song 
	 * date : 2013-12-11
	 */
	private String getTheCoutryCodeByaddress(String address){
		String countryCode = "";
		
				try {
					List<Address> addresses = coder.getFromLocationName(address, 5);
					if (addresses.size()>0){
						countryCode = addresses.get(0).getCountryCode();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		return countryCode;
	}
	
	/**
	 * check if the device support the google's framework.
	 * add : song 
	 * date : 2013-12-11
	 */
	private boolean ifSupportGoogleFramework(){
		boolean suffprtGoogleFarme = true;
		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
				try {
					List<Address> addresses = geoCoder.getFromLocationName("Rochester", 5);
					if (addresses.size()>0){
						suffprtGoogleFarme =  true;
					}else {
						suffprtGoogleFarme = false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		return suffprtGoogleFarme;
	}
	
	/**
	 * check if the city code in united kingdom.
	 * add : song 
	 * date : 2013-12-11
	 */
	private boolean ifCityInCountry(String cityCode){
		boolean ifCityInCountry = false;
		Geocoder geoCoder = new Geocoder(this, Locale.UK);
				try {
					List<Address> addresses = geoCoder.getFromLocationName(cityCode, 5);
					if (addresses.size()>0){
						ifCityInCountry =  true;
					}else {
						ifCityInCountry = false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		return ifCityInCountry;
	}
	
	/**
	 * check if the coordinate code in united kingdom.
	 * add : song 
	 * date : 2013-12-11
	 */
	private boolean ifCoordinateInCountry(GeoPoint geo){
		boolean ifCoordinateInCountry = false;
		Geocoder geoCoder = new Geocoder(this, Locale.UK);
		Double lat =Double.valueOf(new BigDecimal(geo.getLatitudeE6()/1e6).setScale(6, RoundingMode.HALF_UP).toString());
		Double lon = Double.valueOf(new BigDecimal(geo.getLongitudeE6()/1e6).setScale(6, RoundingMode.HALF_UP).toString());
				try {
					List<Address> addresses = geoCoder.getFromLocation(lat, lon, 5);
					if (addresses.size()>0){
						ifCoordinateInCountry =  true;
					}else {
						ifCoordinateInCountry = false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		return ifCoordinateInCountry;
	}
	
	//added by Robin.Qian
	//all find store should use method below
	LinkedList<ThreadFindStores> listThreadFindStores = new LinkedList<StoreFinder.ThreadFindStores>();
	private void findStores(String keyword){
		findStores(keyword, null,false);
	}
	private void findStores(GeoPoint geo){
		findStores(null, geo,false);
	}
	private void findStoreByMyLocation(){
		findStores(null, null, true);
	}
	/**
	 * only need keyword or geo, if all of them is set, use keyword
	 * @param keyword
	 * @param geo
	 */
	private void findStores(String keyword,GeoPoint geo,boolean byMylocation){
		
		// if the scopeContainer is visible ,set it invisible
		if(vLinearLayoutSearchScopeContainer.getVisibility()== View.VISIBLE){
			vLinearLayoutSearchScopeContainer.setVisibility(View.INVISIBLE) ;
		}
		
		if (progress != null && !byMylocation)
		{
			progress.setVisibility(View.VISIBLE);
			mTxtSearching.setVisibility(View.VISIBLE);
			storeList.setVisibility(View.INVISIBLE);
			List<Overlay> overlays = mapView.getOverlays() ;
			Iterator<Overlay> iter = overlays.iterator() ;
			while (iter.hasNext()) {
				Overlay overlay = iter.next() ;
				if(overlay!=null && !(overlay instanceof MyLocationOverlay) ){
					iter.remove() ;
				}
			}
			
			mapcontroller.animateTo(mapView.getMapCenter());//refresh the mapview
			storeItemizedOverlay.clearAllStoreMark();
//			mapKiosks.clearAllLocations();
		}
		ThreadFindStores findStores;
		
		if(byMylocation)
			findStores = new ThreadFindStores(true);
		else if(null != keyword && !"".equals(keyword))
			findStores = new ThreadFindStores(keyword);
		else
			findStores = new ThreadFindStores(geo);
		
		synchronized (listThreadFindStores) {
			boolean run = false;
			
			if(listThreadFindStores.size()==0){
				listThreadFindStores.add(findStores);
				run = true;
			}else if(listThreadFindStores.size()==1){
				listThreadFindStores.add(findStores);
				listThreadFindStores.get(0).interrupt();
			}else{
				//remove all thread except the first one
				int i = 0;
				for(Iterator<ThreadFindStores> it=listThreadFindStores.iterator();it.hasNext();i++){
					ThreadFindStores t = it.next();
					if(i!=0)
						it.remove();
					
					t.interrupt();
					
				}
				
				listThreadFindStores.add(findStores);
			}
			
			if(run)
				findStores.start();
		}
			
	}
	
	private class ThreadFindStores extends Thread{
		private String keyword = null;
		private GeoPoint geo = null;
		private boolean byMyLocation = false;
		public ThreadFindStores(String keyword) {
			this.keyword = keyword;
		}
		public ThreadFindStores(GeoPoint geo) {
			this.geo = geo;
		}
		public ThreadFindStores(boolean byMyLocation){
			this.byMyLocation = byMyLocation;
		}
		@Override
		public void run() {
			try{
				addressName = keyword;
				if(byMyLocation){//search by my location
					Log.i(TAG,"find store by my location");
					Message message = new Message();
					message.what = STARTLOCATE;
					findStore.sendMessage(message);
//					PrintHelper.stores.clear();
					PrintHelper.selectedStorePos = -1;
					animateToPoint = whereAmI.getMyLocation();
					String current = prefs.getString(MainMenu.CurrentlyCountryCode, "");
			        if(current.equalsIgnoreCase("de") || getApplicationContext().getPackageName().contains(MainMenu.DM_COMBINED_PACKAGE_NAME)){
			        	animateToPoint = new GeoPoint( 51 * 1000000,(int)9 * 1000000);
			        }
					PrintMakerWebService webService = new PrintMakerWebService(StoreFinder.this, "");
					BigDecimal lat = new BigDecimal(animateToPoint.getLatitudeE6()/1e6).setScale(6, RoundingMode.HALF_UP);
					BigDecimal lon = new BigDecimal(animateToPoint.getLongitudeE6()/1e6).setScale(6, RoundingMode.HALF_UP);
					if(isInterrupted()){
						return;
					}
					webService.FindStores("", lat.toString(), lon.toString(), productString,mSearchRadiusInMile ,false);
					if(isInterrupted()){
						return;
					}
					findStore.sendEmptyMessage(0);
				}else if(null != keyword){//searched by keyword
					Log.i(TAG,"find stores by keyword:"+keyword);
					addressName = keyword;
					List<Address> addresses = null;
					if(prefs == null){
						prefs = PreferenceManager.getDefaultSharedPreferences(StoreFinder.this);
					}
					if(keyword.toUpperCase().equals(Store.TEST_STORE_ON)){
						prefs.edit().putBoolean(PrintHelper.INCLUDE_TEST_STORES, true).commit();
						findStore.obtainMessage(TEST_STORES_STATUS_CHANGED).sendToTarget();
						Log.i(TAG, "Include test stores -> On");
						return;
					} else if(keyword.toUpperCase().equals(Store.TEST_STORE_OFF)){
						prefs.edit().putBoolean(PrintHelper.INCLUDE_TEST_STORES, false).commit();
						findStore.obtainMessage(TEST_STORES_STATUS_CHANGED).sendToTarget();
						Log.i(TAG, "Include test stores -> Off");
						return;
					}
					boolean flag = true;int iCount = 0;
					while (flag && iCount < 3 && !isInterrupted()) {
						try {
//							PrintHelper.stores.clear();
							PrintHelper.selectedStorePos = -1;
							PrintMakerWebService webService = new PrintMakerWebService(StoreFinder.this, "");
							if(isInterrupted()){
								return;
							}
							Log.e(TAG,"getFromLocationName, addresses: " + keyword);
							addresses = coder.getFromLocationName(keyword,5);
							
							if (addresses != null && addresses.size() > 0) {
								Log.v("findStore ", "findStore: address :"+addresses.get(0).toString()) ;
								animateToPoint = new GeoPoint((int) (addresses.get(0).getLatitude() * 1E6),(int) (addresses.get(0).getLongitude() * 1E6));								
//								mapcontroller.setZoom(15);
								zoomTheMap(mSearchRadiusInMile);
								webService.FindStores("",""+ (addresses.get(0).getLatitude()),""+ (addresses.get(0).getLongitude()),productString,mSearchRadiusInMile, true);
								if(isInterrupted()){
									return;
								}
								flag = false;iCount++;
							}else{
								Log.e(TAG,"getFromLocationName FAILED!!!");
								if(iCount == 2){
									if(isInterrupted()){
										return;
									}
								}
								flag = true;iCount++;
							}
							
						} catch (Exception e) {
							flag = true;iCount++;
							Log.e(TAG, "Error Found while finding store", e);
						}
					}
					
					if(isInterrupted()){
						System.out.println(toString()+":interrupted");
						return;
					}
					
					if (addresses != null && addresses.size() > 0) {
					}else if(keyword.length()>0){
						PrintMakerWebService webService = new PrintMakerWebService(StoreFinder.this, "");
						webService.FindStores(keyword,"","",productString, mSearchRadiusInMile,true);
						if(isInterrupted()){
							return;
						}
						
					}
					findStore.sendEmptyMessage(0);
				}else if(null != geo){//searched by location
					Log.i(TAG,"find stores by location:"+geo);
					boolean flag = true;int iCount = 0;
					while (flag && iCount < 3 && !isInterrupted()) {
						try {
//							PrintHelper.stores.clear();
							PrintHelper.selectedStorePos = -1;
							PrintMakerWebService webService = new PrintMakerWebService(StoreFinder.this, "");
							if(isInterrupted()){
								return;
							}
							Log.e(TAG,"getFromLocation, lat,lng: " + geo);
							webService.FindStores("",""+ (new BigDecimal(geo.getLatitudeE6()/1e6).setScale(6, RoundingMode.HALF_UP)),""+ (new BigDecimal(geo.getLongitudeE6()/1e6).setScale(6, RoundingMode.HALF_UP)),productString,mSearchRadiusInMile, true);
							if(isInterrupted()){
								return;
							}
							animateToPoint = geo;
							findStore.sendEmptyMessage(0);
							flag = false;iCount++;
							
						} catch (Exception e) {
							flag = true;iCount++;
							Log.e(TAG, "Error Found while finding store", e);
						}
					}
					
				}
				
			}finally{
				synchronized (listThreadFindStores) {
					listThreadFindStores.remove(this);
					if(listThreadFindStores.size()>0){
						listThreadFindStores.get(0).start();
					}
				}
			}
			
		}
		
	}
}
