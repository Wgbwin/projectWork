package com.kodak.rss.tablet.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kodak.rss.core.bean.LocalCustomerInfo;
import com.kodak.rss.core.n2r.bean.retailer.CountryInfo;
import com.kodak.rss.core.n2r.bean.retailer.Retailer;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.OrderHistoryAdapter;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.view.MapFragment;
import com.kodak.rss.tablet.view.RoundedWebView;
import com.kodak.rss.tablet.view.dialog.DialogCountrySelector;
import com.kodak.rss.tablet.view.dialog.DialogCountrySelector.onDialogErrorListener;
import com.kodak.rss.tablet.view.dialog.DialogSetupLegal;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class SettingsActivity extends BaseActivity implements OnClickListener, OnFocusChangeListener{
	private final String TAG = SettingsActivity.class.getSimpleName();
	
	private RssTabletApp app;
	
	private int[][] items;
	public static final int Customer_Info_ID = 0x000001;
	public static final int Shipping_Info_ID = 0x000002;
	private final int Store_Info_ID = 0x000003;
	private final int Legal_ID = 0x000004;
	private final int Order_History_ID = 0x000005;
	private final int About_ID = 0x000006;
	
	private RelativeLayout detailContainer;
	private View mapDetailItem;
	
	private TextView tvTitle;
	private Button btDone;
	private LocalCustomerInfo customerInfo;
	
	// CustomerInfo
	private EditText etCusFirstName;
	private EditText etCusLastName;
	private EditText etCusPhone;
	private EditText etCusEmail;
	private View vCusFirstName;
	private View vCusLastName;
	private View vCusPhone;
	private View vCusEmail;
	private View vRequired;
	private View vChangeCountryContainer;
	
	// Shipping Address
	private EditText etShippingFirstName;
	private EditText etShippingLastName;
	private EditText etShippingAdd1;
	private EditText etShippingAdd2;
	private EditText etShippingCity;
	private EditText etShippingState;
	private EditText etShippingZip;
	
	private View vShippingFirstName;
	private View vShippingLastName;
	private View vShippingAdd1;
	private View vShippingCity;
	private View vShippingState;
	private View vShippingZip;
	
	private PopupWindow stateWindow;
	private ArrayList<String> states;
	
	// Store Info
	private GoogleMap googleMap;
	
	// Order Histroy
	private ListView lvOrderHistroy;
	private OrderHistoryAdapter orderHistoryAdapter;
	
	// Legal
	private CheckBox cbAllowCookies;
	private CheckBox cbAllowCD360;
	
	// Settings
	private TextView tvVersion;
	
	// 
	private boolean showCountryChange = false;
	
	/**
	 * if value is 0, it is means Settings screen is started by Setup button.
	 */
	private int detailPart = 0;
	
	private String settingsLocation = "";
	private HashMap<String, String> attr = new HashMap<String, String>();
	private SettingTextWatcher customerWathcer;
	private SettingTextWatcher shipAddWatcher;
	private boolean previousAgreeTracking = false;
	private boolean currentAgreeTracking = false;
	
	private InputMethodManager imm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		app = RssTabletApp.getInstance();
		setContentView(R.layout.activity_settings_hd);
		if(getIntent()!=null && getIntent().getExtras()!=null){
			detailPart = getIntent().getExtras().getInt("item");
			showCountryChange = getIntent().getExtras().getBoolean("fromMain") && (app.getCountries()!=null && !app.getCountries().isEmpty());
			settingsLocation = getIntent().getExtras().getString(INTENT_KEY_LOCALYTICS_PAGE_VIEW_NAME);
		}
		initData();
		initViews();
		if(detailPart != 0){
			findViewById(R.id.act_setup_info_container).setVisibility(View.GONE);
			findViewById(R.id.line).setVisibility(View.GONE);
			findViewById(R.id.tvTitle).setVisibility(View.INVISIBLE);
			refreshDetailView(detailPart, true);
		} 
		currentAgreeTracking = previousAgreeTracking;
		
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	@Override
	protected boolean hasSideMenu() {
		return false;
	}
	
	private void initViews(){
		initDoneButton();
		LinearLayout infoContainer = (LinearLayout) findViewById(R.id.act_setup_info_container);
		detailContainer = (RelativeLayout) findViewById(R.id.info_container);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		
		tvTitle.setText(getString(R.string.SettingsScreen_ScreenTitle));
		
		items = new int[][]{new int[]{R.string.SettingsScreen_Tab_CustomerInfo, Customer_Info_ID}, 
							new int[]{R.string.SettingsScreen_Tab_ShippingInfo, Shipping_Info_ID},
							new int[]{R.string.SettingsScreen_Tab_StoreInfo, Store_Info_ID},
							new int[]{R.string.SettingsScreen_Legal, Legal_ID},
							new int[]{R.string.SettingsScreen_Tab_OrderHistory, Order_History_ID},
							new int[]{R.string.SettingsScreen_Tab_About, About_ID}};
		
		LayoutInflater mInflater = LayoutInflater.from(this);
		for(int i=0; i<items.length; i++){
			int itemId = items[i][1];
			if(itemId == Shipping_Info_ID && !needShowShipToHome()){
				continue;
			}
			View item = mInflater.inflate(R.layout.setup_info_item_hd, null);
			item.setId(itemId);
			item.setTag(true);
			item.setOnClickListener(this);
			TextView itemLabel = (TextView) item.findViewById(R.id.tv_setup_info_item);
			itemLabel.setText(getString(items[i][0]));
			infoContainer.addView(item);
		}
		
		// set Customer Info part as default
		refreshDetailView(Customer_Info_ID, false);
		lastSelectedItem = Customer_Info_ID;
	}
	
	private boolean needShowShipToHome(){
		List<Retailer> retailers = RssTabletApp.getInstance().getRetailers();
		if(retailers != null){
			for(Retailer retailer : retailers){
				if(retailer.cloLite || retailer.shipToHome){
					return true;
				}
			}
		}
		return false;
	}
	
	private void initDoneButton(){
		btDone = (Button) findViewById(R.id.btDone);
		btDone.setVisibility(View.VISIBLE);
		btDone.setOnClickListener(this);
	}
	
	private void initData(){
		customerInfo = new LocalCustomerInfo(this);		
		initCountryCode();
		initLocalyticsTrackData();
	}
	
	private void initLocalyticsTrackData(){
		previousAgreeTracking = SharedPreferrenceUtil.getBoolean(this, SharedPreferrenceUtil.TRACKING_ENABLED);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_CUST_INOF_CHANGED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_SHIP_ADD_CHANGED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_LICENSE_VIEWED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_PRI_POLICY_VIEWED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_ORDER_HIS_VIEWED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_AGREE_TO_TRACKING, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_ABOUT_SCREEN_VIEWED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		if(!"".equals(settingsLocation) && detailPart==0){
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_SETTINGS_LOCATION, settingsLocation);
		}
	}
	
	private void refreshDetailView(int id, boolean needResize){		
		detailContainer.removeAllViews();
		LayoutInflater mInflater = LayoutInflater.from(this);
		View detailItem = null;
		switch (id) {
		case Customer_Info_ID:
			detailItem = mInflater.inflate(R.layout.setup_customer_info_hd, null);
			initCusInfoView(detailItem);
			break;
		case Shipping_Info_ID:
			detailItem = mInflater.inflate(R.layout.setup_shipping_address_hd, null);
			initShipAddView(detailItem);
			break;
		case Store_Info_ID:
			if(mapDetailItem==null){
				mapDetailItem = mInflater.inflate(R.layout.setup_store_info_hd, null);
			}
			initStoreInfoView(mapDetailItem);
			break;
		case Legal_ID:
			detailItem = mInflater.inflate(R.layout.setup_legal_hd, null);
			initLegalView(detailItem);
			break;
		case Order_History_ID:
			detailItem = mInflater.inflate(R.layout.setup_order_history_hd, null);
			initOrderHistoryView(detailItem);
			break;
		case About_ID:
			detailItem = mInflater.inflate(R.layout.setup_about_hd, null);
			initAboutView(detailItem);
			break;
		}
		if(id == Store_Info_ID){
			detailContainer.addView(mapDetailItem, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		} else {
			int width = LayoutParams.MATCH_PARENT;
			int height = LayoutParams.MATCH_PARENT;
			if(needResize){
				width = getWindowManager().getDefaultDisplay().getWidth() * 7 / 10;
			}
			detailContainer.addView(detailItem, new LayoutParams(width, height));
		}
		tabChanged = false;
	}
	
	private void initCusInfoView(View detailItem){
		etCusFirstName = (EditText) detailItem.findViewById(R.id.et_customer_first_name);
		TextUtil.addEmojiFilter(etCusFirstName);
		etCusFirstName.setOnFocusChangeListener(this);
		etCusFirstName.setText(customerInfo.getCusFirstName());
		vCusFirstName = detailItem.findViewById(R.id.iv_customer_first_name);
		
		etCusLastName = (EditText) detailItem.findViewById(R.id.et_customer_last_name);
		TextUtil.addEmojiFilter(etCusLastName);
		etCusLastName.setOnFocusChangeListener(this);
		etCusLastName.setText(customerInfo.getCusLastName());
		vCusLastName = detailItem.findViewById(R.id.iv_customer_last_name);
		
		etCusPhone = (EditText) detailItem.findViewById(R.id.et_customer_phone);
		TextUtil.addEmojiFilter(etCusPhone);
		etCusPhone.setOnFocusChangeListener(this);
		etCusPhone.setText(customerInfo.getCusPhone());
		vCusPhone = detailItem.findViewById(R.id.iv_customer_phone);
		
		etCusEmail = (EditText) detailItem.findViewById(R.id.et_customer_email);
		TextUtil.addEmojiFilter(etCusEmail);
		etCusEmail.setOnFocusChangeListener(this);
		etCusEmail.setText(customerInfo.getCusEmail());
		vCusEmail = detailItem.findViewById(R.id.iv_customer_email);
		
		vRequired = detailItem.findViewById(R.id.tv_required);
		vChangeCountryContainer = detailItem.findViewById(R.id.container_changeCountry);
		
		detailItem.findViewById(R.id.bt_change_country).setOnClickListener(this);
		((TextView)(detailItem.findViewById(R.id.tv_current_country))).setText(RssTabletApp.getInstance().getCountryNameCurrentUsed());
		
		if(showCountryChange){
			vChangeCountryContainer.setVisibility(View.VISIBLE);
		} else {
			vChangeCountryContainer.setVisibility(View.INVISIBLE);
		}
		
		Retailer currentRetailer = RssTabletApp.getInstance().getCurrentRetailer(this);
		if(currentRetailer != null){
			vRequired.setVisibility(View.VISIBLE);
			int[] requiredInfo = currentRetailer.requiredCustomerInfo;
			for(int i=0; i<requiredInfo.length; i++){
				switch (requiredInfo[i]) {
				case 0:
					vCusFirstName.setVisibility(View.VISIBLE);
					break;
				case 1:
					vCusLastName.setVisibility(View.VISIBLE);
					break;
				case 2:
					vCusPhone.setVisibility(View.VISIBLE);
					break;
				case 6:
					vCusEmail.setVisibility(View.VISIBLE);
					break;
				}
			}
		}
		// Track Localytics events
		initCustomerInfoLocalyticsTrack();
	}
	
	private void initCustomerInfoLocalyticsTrack(){
		if(customerWathcer == null){
			customerWathcer = new SettingTextWatcher(RSSTabletLocalytics.LOCALYTICS_KEY_CUST_INOF_CHANGED);
		}
		etCusFirstName.addTextChangedListener(customerWathcer);
		etCusLastName.addTextChangedListener(customerWathcer);
		etCusPhone.addTextChangedListener(customerWathcer);
		etCusEmail.addTextChangedListener(customerWathcer);
	}
	
	private void initShipAddView(View detailItem){
		CountryInfo countryInfo = RssTabletApp.getInstance().getCountryInfo(app.getCountrycodeCurrentUsed());
		
		etShippingFirstName = (EditText) detailItem.findViewById(R.id.et_shipping_first_name);
		TextUtil.addEmojiFilter(etShippingFirstName);
		etShippingFirstName.setText(customerInfo.getShipFirstName());
		etShippingFirstName.setOnFocusChangeListener(this);
		vShippingFirstName = detailItem.findViewById(R.id.iv_shipping_first_name);
		
		etShippingLastName = (EditText) detailItem.findViewById(R.id.et_shipping_last_name);
		TextUtil.addEmojiFilter(etShippingLastName);
		etShippingLastName.setText(customerInfo.getShipLastName());
		etShippingLastName.setOnFocusChangeListener(this);
		vShippingLastName = detailItem.findViewById(R.id.iv_shipping_last_name);
		
		etShippingAdd1 = (EditText) detailItem.findViewById(R.id.et_shipping_add1);
		TextUtil.addEmojiFilter(etShippingAdd1);
		etShippingAdd1.setText(customerInfo.getShipAddress1());
		etShippingAdd1.setOnFocusChangeListener(this);
		vShippingAdd1 = detailItem.findViewById(R.id.iv_shipping_add1);
		
		etShippingAdd2 = (EditText) detailItem.findViewById(R.id.et_shipping_add2);
		TextUtil.addEmojiFilter(etShippingAdd2);
		etShippingAdd2.setText(customerInfo.getShipAddress2());
		etShippingAdd2.setOnFocusChangeListener(this);
		
		etShippingCity = (EditText) detailItem.findViewById(R.id.et_shipping_city);
		TextUtil.addEmojiFilter(etShippingCity);
		etShippingCity.setText(customerInfo.getShipCity());
		etShippingCity.setOnFocusChangeListener(this);
		vShippingCity = detailItem.findViewById(R.id.iv_shipping_city);
		etShippingCity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_NEXT){
					etShippingZip.requestFocus();
					return true;
				}
				return false;
			}
		});
		
		etShippingState = (EditText) detailItem.findViewById(R.id.et_shipping_state);
		TextUtil.addEmojiFilter(etShippingState);
		etShippingState.setText(customerInfo.getShipState());
		etShippingState.setOnFocusChangeListener(this);
		etShippingState.setOnClickListener(this);
		vShippingState = detailItem.findViewById(R.id.iv_shipping_state);
		
		etShippingZip = (EditText) detailItem.findViewById(R.id.et_shipping_zip);
		TextUtil.addEmojiFilter(etShippingZip);
		etShippingZip.setText(customerInfo.getShipZip());
		etShippingZip.setOnFocusChangeListener(this);
		vShippingZip = detailItem.findViewById(R.id.iv_shipping_zip);
		
		if(countryInfo != null){
			vShippingAdd1.setVisibility(View.VISIBLE);
			if(countryInfo.addressStyle.contains(CountryInfo.CITY)){
				vShippingCity.setVisibility(View.VISIBLE);
			}
			if(countryInfo.addressStyle.contains(CountryInfo.STATE)){
				vShippingState.setVisibility(View.VISIBLE);
				if(customerInfo.getShipState().equals("")){
					etShippingState.setHint(countryInfo.localizedSubregionName);
				}
			}
			if(countryInfo.addressStyle.contains(CountryInfo.ZIP)){
				vShippingZip.setVisibility(View.VISIBLE);
				if(customerInfo.getShipZip().equals("")){
					etShippingZip.setHint(countryInfo.localizedPostalCodeName);
				}
			}
		}
		((TextView)(detailItem.findViewById(R.id.tv_current_country))).setText(RssTabletApp.getInstance().getCountryNameCurrentUsed());
		
		// if state list is empty, then hide this edit text
		if(countryInfo.countrySubregions==null || countryInfo.countrySubregions.isEmpty()){
			etShippingState.setVisibility(View.GONE);
			vShippingState.setVisibility(View.GONE);
		}
		
		// Track Localytics events
		initShippingAddressLocalyticsTrack();
	}
	
	private void initShippingAddressLocalyticsTrack(){
		if(shipAddWatcher == null){
			shipAddWatcher = new SettingTextWatcher(RSSTabletLocalytics.LOCALYTICS_KEY_SHIP_ADD_CHANGED);
		}
		etShippingFirstName.addTextChangedListener(shipAddWatcher);
		etShippingLastName.addTextChangedListener(shipAddWatcher);
		etShippingAdd1.addTextChangedListener(shipAddWatcher);
		etShippingAdd2.addTextChangedListener(shipAddWatcher);
		etShippingCity.addTextChangedListener(shipAddWatcher);
		etShippingState.addTextChangedListener(shipAddWatcher);
		etShippingZip.addTextChangedListener(shipAddWatcher);
		
	}
	
	private void initLegalView(View detailItem){
		detailItem.findViewById(R.id.bt_view_license).setOnClickListener(this);
		detailItem.findViewById(R.id.bt_view_privacy).setOnClickListener(this);
		cbAllowCookies = (CheckBox) detailItem.findViewById(R.id.cb_legal_allowCookies);
		cbAllowCD360 = (CheckBox) detailItem.findViewById(R.id.cb_legal_allowCD360);
		
		cbAllowCookies.setChecked(currentAgreeTracking);
		cbAllowCD360.setChecked(SharedPreferrenceUtil.getBoolean(this, SharedPreferrenceUtil.CDG360_DESIRED));
		
		cbAllowCookies.setOnCheckedChangeListener(checkChangedListener);
		cbAllowCD360.setOnCheckedChangeListener(checkChangedListener);
	}
	
	private void initAboutView(View detailItem){
		tvVersion = (TextView) detailItem.findViewById(R.id.tvVersion);
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String versionDetail = getString(R.string.Version_String) + " " + packageInfo.versionName + " ";
			if(RssTabletApp.getInstance().isBrandApp()){
				versionDetail += getString(R.string.Copyright_String);
			} else {
				versionDetail += getString(R.string.Cobranded_Copyright_String);
			}
			tvVersion.setText(versionDetail);
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		detailItem.findViewById(R.id.bt_rate).setOnClickListener(this);
		WebView helpWebView = new WebView(this);
		helpWebView.setBackgroundColor(0);
		WebSettings webSettings = helpWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		helpWebView.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			
		});
		helpWebView.loadUrl(getString(R.string.helpURL));
		((RoundedWebView)detailItem.findViewById(R.id.wv_help)).refreshWebView(helpWebView);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_ABOUT_SCREEN_VIEWED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
	}
	
	private void initOrderHistoryView(View detailItem){
		lvOrderHistroy = (ListView) detailItem.findViewById(R.id.lv_order_histroy);
		lvOrderHistroy.setDivider(null);
		if(orderHistoryAdapter == null){
			orderHistoryAdapter = new OrderHistoryAdapter(this);
		}
		lvOrderHistroy.setAdapter(orderHistoryAdapter);
		orderHistoryAdapter.notifyDataSetChanged();
	}
	
	private void initStoreInfoView(View detailItem){
		StoreInfo store = StoreInfo.loadSelectedStore(this);
		View mapContainer = detailItem.findViewById(R.id.mapview_container);
		if(store == null){
			mapContainer.setVisibility(View.GONE);
		} else {
			if(googleMap == null){
				googleMap = ((MapFragment)getSupportFragmentManager().findFragmentById(R.id.mv_store_mapview)).getMap();
				UiSettings mapSetting = googleMap.getUiSettings();
				mapSetting.setAllGesturesEnabled(false);
				mapSetting.setMyLocationButtonEnabled(false);
				mapSetting.setRotateGesturesEnabled(false);
				mapSetting.setScrollGesturesEnabled(false);
				mapSetting.setZoomControlsEnabled(false);
				mapSetting.setZoomGesturesEnabled(false);
				mapSetting.setTiltGesturesEnabled(false);
			}
			googleMap.clear();
			MarkerOptions marker=null;
			try {
				marker = StoreSelectActivity.createMapMaker(store);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mapContainer.setVisibility(View.VISIBLE);
			LatLng latlng = new LatLng(store.latitude, store.longitude);
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
			if(marker!=null){
				googleMap.addMarker(marker);
			}
			
		}
		detailItem.findViewById(R.id.bt_store_change).setOnClickListener(this);
		((TextView)detailItem.findViewById(R.id.tv_store_name)).setText(store==null?"":store.name);
		((TextView)detailItem.findViewById(R.id.tv_store_add)).setText(store==null?"":store.address.address1);
		((TextView)detailItem.findViewById(R.id.tv_store_city)).setText(store==null?"":store.address.city);
		((TextView)detailItem.findViewById(R.id.tv_store_phone)).setText(store==null?"":store.phone);
		String timeFormat = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.TIME_12_24);
		((TextView)detailItem.findViewById(R.id.tv_store_hours)).setText(store==null?"":store.convertHoursToString(timeFormat));
	}
	
	private void initCountryCode(){
		if(RssTabletApp.getInstance().getCountrycodeCurrentUsed() == null){
			String countryCode = RssTabletApp.getInstance().getDefaultCountryCode();
			if(countryCode != null && !"".equals(countryCode)){
				RssTabletApp.getInstance().setCountryCodeCurrentUsed(countryCode);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(lastSelectedItem == Store_Info_ID){
			refreshDetailView(Store_Info_ID, false);
		}
		if(orderHistoryAdapter!=null && orderHistoryAdapter.isOrderHistoryViewed()){
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_ORDER_HIS_VIEWED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void saveSetupInfo(){
		if(customerInfo!=null){
			customerInfo.save(this);
		}
		SharedPreferrenceUtil.setString(this, SharedPreferrenceUtil.BACK_DOOR_NAME, customerInfo.getCusFirstName());
	}

	private int lastSelectedItem = -1;
	private boolean doneClicked = false;
	private boolean tabChanged = false;
	private boolean shownWarningOnDone = false;
	private boolean shownWarningOnChangeTab = false;
	@Override
	public void onClick(View v) {
		if(v.getTag()!=null && (Boolean)v.getTag()){
			if(v.getId() != lastSelectedItem){
				if(!shownWarningOnChangeTab){
					shownWarningOnChangeTab = setCustomerFocusEvents(getCurrentFocus(), false);
					if(!shownWarningOnChangeTab){
						shownWarningOnChangeTab = setShippingAddressFocusEvents(getCurrentFocus(), false);
					}
					
					if(shownWarningOnChangeTab){
						return;
					}
				}
				
				tabChanged = true;
				lastSelectedItem = v.getId();
				refreshDetailView(v.getId(), false);
				shownWarningOnChangeTab = false;
			}
		}
		if(v.getId() == R.id.btDone){
			//need to check the last focus when click done
			if(!shownWarningOnDone){
				shownWarningOnDone = setCustomerFocusEvents(getCurrentFocus(), false);
				if(!shownWarningOnDone){
					shownWarningOnDone = setShippingAddressFocusEvents(getCurrentFocus(), false);
				}
				if(shownWarningOnDone){
					return;
				}
			}
			
			if(detailPart == 0){
				if(previousAgreeTracking || currentAgreeTracking){
					if(!previousAgreeTracking){
						RSSLocalytics.openLocalyticsSession(this);
					}
					if(currentAgreeTracking){
						attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_AGREE_TO_TRACKING, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					} else {
						attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_AGREE_TO_TRACKING, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
					}
					RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_SETTINGS_SUMMARY, attr);
				}
				SharedPreferrenceUtil.setBoolean(this, SharedPreferrenceUtil.TRACKING_ENABLED, currentAgreeTracking);
			}
			doneClicked = true;
			if(getCurrentFocus() != null){
				hideSoftInputWindow(getCurrentFocus());
				getCurrentFocus().clearFocus();
			}
			saveSetupInfo();
			finish();
		}
		
		setCusInfoClickEvents(v);
		setShippingAddressClickEvents(v);
		setStoreClickEvents(v);
		setLegalClickEvents(v);
		setAboutClickEvents(v);
	}
	
	private OnCheckedChangeListener checkChangedListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			int viewId = buttonView.getId();
			if(viewId == R.id.cb_legal_allowCookies){
				//SharedPreferrenceUtil.setBoolean(SettingsActivity.this, SharedPreferrenceUtil.TRACKING_ENABLED, isChecked);
				currentAgreeTracking = isChecked;
				//add by song for RSSMOBILEPDC-1663
				if (isChecked){
					SharedPreferrenceUtil.setBoolean(SettingsActivity.this,AppConstants.KEY_LOCALYTICS, true);
					RSSLocalytics.openLocalytics(SettingsActivity.this);
				} else {
					SharedPreferrenceUtil.setBoolean(SettingsActivity.this,AppConstants.KEY_LOCALYTICS, false);
					RSSLocalytics.closeLocalytics(SettingsActivity.this);
				}
				/*if(isChecked){
					currentAgreeTracking = 
					attr.put(AppConstants.LOCALYTICS_KEY_AGREE_TO_TRACKING, AppConstants.LOCALYTICS_VALUE_YES);
				} else {
					attr.put(AppConstants.LOCALYTICS_KEY_AGREE_TO_TRACKING, AppConstants.LOCALYTICS_VALUE_NO);
				}*/
			} else if(viewId == R.id.cb_legal_allowCD360){
				SharedPreferrenceUtil.setBoolean(SettingsActivity.this, SharedPreferrenceUtil.CDG360_DESIRED, isChecked);
			}
		}
	};
	
	private void setShippingAddressClickEvents(View v){
		if(v.getId() == R.id.et_shipping_state){			
			if(stateWindow!= null && !stateWindow.isShowing()){
				showSelectStateList(v, ((View)v.getParent()).getBottom(), detailContainer.getLeft(), detailContainer.getWidth());
			}
		}
	}
	
	private boolean isChangeCountry = false;
	private void setCusInfoClickEvents(View v){
		onDialogErrorListener listener = new DialogCountrySelector.onDialogErrorListener() {
			public void onYes(String countryName, String countryCode, String oriCountryName, String oriCountryCode) {
				if(!oriCountryCode.equals(countryCode)){
					if(customerInfo != null){
						customerInfo.setShipState("");
						app.clearLastCountryData();
						isChangeCountry = true;
					}
					StoreInfo.clearSelectedStore(SettingsActivity.this);
				}
				SharedPreferrenceUtil.saveSelectedCountryCode(SettingsActivity.this, countryCode);
				RssTabletApp.getInstance().setCountryCodeCurrentUsed(countryCode);
			}
			public void onDismiss(){
				refreshDetailView(Customer_Info_ID, false);
			}
		};
		if(v.getId() == R.id.bt_change_country){
			if(RssTabletApp.getInstance().getCountries()!=null){
				String currentCountryCode = app.getDefaultCountryCode();
				DialogCountrySelector dialog = new DialogCountrySelector();
				dialog.initCountrySelectorMessage(this,getResources().getString(R.string.SelectCountry), getResources().getString(R.string.d_ok), RssTabletApp.getInstance().getCountries(), currentCountryCode,listener);
			}
		}
	}
	
	private void setStoreClickEvents(View v){
		if(v.getId() == R.id.bt_store_change){
			Intent mIntent = new Intent(this, StoreSelectActivity.class);
			mIntent.putExtra(StoreInfo.IS_WIFI_LOCATOR, false);
			startActivity(mIntent);
		}
	}
	
	private void setLegalClickEvents(View v){
		if(v.getId() == R.id.bt_view_license){
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_LICENSE_VIEWED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
			new DialogSetupLegal().createDialog(this, R.string.EULAScreen_Title, app.getEulaURL(), detailContainer.getHeight());
		} else if(v.getId() == R.id.bt_view_privacy) {
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_PRI_POLICY_VIEWED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
			new DialogSetupLegal().createDialog(this, R.string.PrivacyScreen_Title, app.getPrivacyURL(), detailContainer.getHeight());
		}
	}

	private void setAboutClickEvents(View v){
		if(v.getId() == R.id.bt_rate){
//			DialogDismissListener listener = new DialogDismissListener() {
//				
//				@Override
//				public void onDismiss() {
//					
//				}
//			};
//			new DialogFeedback().createDialog(this, detailContainer.getHeight(), listener);
			
			//start change by bing wang for fixed RSSMOBILEPDC-2089 on 2015-2-26
//			String url = "https://itunes.apple.com/us/app/my-kodak-moments/id553536717"; on ios
			String packageName = SettingsActivity.this.getPackageName();
			Uri uri = null;
			Intent goToMarket = null;						
			try {
				uri = Uri.parse("market://details?id=" + packageName);
				goToMarket =new Intent(Intent.ACTION_VIEW, uri);
				goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				startActivity(goToMarket);
			} catch (ActivityNotFoundException e) {
				uri = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
				goToMarket =new Intent(Intent.ACTION_VIEW, uri);
				goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				startActivity(goToMarket);
			}
			//end change by bing wang for fixed RSSMOBILEPDC-2089 on 2015-2-26
		}   
	}
	
	private void hideSoftInputWindow(View v){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive()){
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}
	
	private boolean validateInputFormat(String str,String rule){
		boolean flag = false;
		if (str.trim().length() == 0) {
			return false;
		}
		try{
			if (rule == null) {
				return false;
			}
			Pattern regex = Pattern.compile(rule);
			Matcher matcher = regex.matcher(str);
			flag = matcher.matches();
		} catch(Exception e) {
			flag = false;
		}
		return flag;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		setCustomerFocusEvents(v, hasFocus);
		setShippingAddressFocusEvents(v, hasFocus);
	}
	
	/**
	 * @param v
	 * @param hasFocus
	 * @return whether this method shown wanrning
	 */
	private boolean setCustomerFocusEvents(View v, boolean hasFocus){
		boolean shownWarning = false;
		if(v == null){
			return shownWarning;
		}
		
		if(!hasFocus){
			int id = v.getId();
			if(id == R.id.et_customer_first_name){
				String tempCusFirstName = ((EditText)v).getText().toString().trim();
				if(tempCusFirstName.length()==0 && vCusFirstName.getVisibility()==View.VISIBLE && !doneClicked && !tabChanged && !isChangeCountry){
					showMissingUserInfoDialog(v, getString(R.string.SettingsScreen_MissingInfoPrompt));
					shownWarning = true;
					isChangeCountry = false;
				}
				customerInfo.setCusFirstName(tempCusFirstName);
			} 
			else if(id == R.id.et_customer_last_name){
				String tempCusLastName = ((EditText)v).getText().toString().trim();
				if(tempCusLastName.length()==0 && vCusLastName.getVisibility()==View.VISIBLE && !doneClicked && !tabChanged && !isChangeCountry){
					showMissingUserInfoDialog(v, getString(R.string.SettingsScreen_MissingInfoPrompt));
					shownWarning = true;
					isChangeCountry = false;
				}
				customerInfo.setCusLastName(tempCusLastName);
			} 
			else if(id == R.id.et_customer_phone){
				String tempCusPhone= ((EditText)v).getText().toString().trim();
				if(tempCusPhone.length()==0 && vCusPhone.getVisibility()==View.VISIBLE && !doneClicked && !tabChanged && !isChangeCountry){
					showMissingUserInfoDialog(v, getString(R.string.SettingsScreen_MissingInfoPrompt));
					shownWarning = true;
					isChangeCountry = false;
				}
				customerInfo.setCusPhone(tempCusPhone);
			} 
			else if(id == R.id.et_customer_email){
				String tempCusEmail = ((EditText)v).getText().toString().trim();
				boolean isEmailValid = tempCusEmail.length()!=0 && tempCusEmail.contains("@") && tempCusEmail.contains(".");
				if(!isEmailValid && vCusEmail.getVisibility()==View.VISIBLE && !doneClicked && !tabChanged && !isChangeCountry){
					showMissingUserInfoDialog(v, getString(R.string.SettingsScreen_EmailFormatPrompt));
					shownWarning = true;
					isChangeCountry = false;
				}
				customerInfo.setCusEmail(tempCusEmail);
			}
		}
		return shownWarning;
	}
	
	/**
	 * @param v
	 * @param hasFocus
	 * @return whether this method shown wanrning
	 */
	private boolean setShippingAddressFocusEvents(View v, boolean hasFocus){
		boolean shownWarning = false;
		if(v == null){
			return shownWarning;
		}
		
		if(!hasFocus){
			int id = v.getId();
			if(id == R.id.et_shipping_first_name){
				String tempShipFirstName = ((EditText)v).getText().toString().trim();
				if(tempShipFirstName.length()==0 && vShippingFirstName.getVisibility()==View.VISIBLE && !doneClicked && !tabChanged){
					showMissingUserInfoDialog(v, getString(R.string.SettingsScreen_MissingInfoPrompt));
					shownWarning = true;
				}
				customerInfo.setShipFirstName(tempShipFirstName);
			} 
			else if(id == R.id.et_shipping_last_name){
				String tempShipLastName = ((EditText)v).getText().toString().trim();
				if(tempShipLastName.length()==0 && vShippingLastName.getVisibility()==View.VISIBLE && !doneClicked && !tabChanged){
					showMissingUserInfoDialog(v, getString(R.string.SettingsScreen_MissingInfoPrompt));
					shownWarning = true;
				}
				customerInfo.setShipLastName(tempShipLastName);
			} 
			else if(id == R.id.et_shipping_add1){
				String tempShipAdd1 = ((EditText)v).getText().toString().trim();
				if(tempShipAdd1.length()==0 && vShippingAdd1.getVisibility()==View.VISIBLE && !doneClicked && !tabChanged){
					showMissingUserInfoDialog(v, getString(R.string.SettingsScreen_MissingInfoPrompt));
					shownWarning = true;
				}
				customerInfo.setShipAddress1(tempShipAdd1);
			} 
			else if(id == R.id.et_shipping_add2){
				String tempShipAdd2 = ((EditText)v).getText().toString().trim();
				customerInfo.setShipAddress2(tempShipAdd2);
			} 
			else if(id == R.id.et_shipping_city){
				String tempShipCity = ((EditText)v).getText().toString().trim();
				if(tempShipCity.length()==0 && vShippingCity.getVisibility()==View.VISIBLE && !doneClicked && !tabChanged){
					showMissingUserInfoDialog(v, getString(R.string.SettingsScreen_MissingInfoPrompt));
					shownWarning = true;
				}
				customerInfo.setShipCity(tempShipCity);
			} 
			else if(id == R.id.et_shipping_state){
				String tempShipState = ((EditText)v).getText().toString().trim();
				boolean isStateValid = true;
				CountryInfo countryInfo = RssTabletApp.getInstance().getCountryInfo(app.getCountrycodeCurrentUsed());
				if(countryInfo!=null && countryInfo.countrySubregions!=null){
					if(countryInfo.countrySubregions.containsKey(tempShipState)){
						isStateValid = true;
					} else {
						isStateValid = false;
					}
				}
				if(!isStateValid && vShippingState.getVisibility()==View.VISIBLE && !doneClicked && !tabChanged){
					//showMissingUserInfoDialog(getString(R.string.SettingsScreen_MissingInfoPrompt));
				}
			} 
			else if(id == R.id.et_shipping_zip){
				String tempShipZip = ((EditText)v).getText().toString().trim();
				boolean isZipValid = true;
				CountryInfo countryInfo = RssTabletApp.getInstance().getCountryInfo(app.getCountrycodeCurrentUsed());
				if(countryInfo!=null){
					isZipValid = validateInputFormat(tempShipZip, countryInfo.postalCodeAuditExpression);
				}
				if(!isZipValid && vShippingZip.getVisibility()==View.VISIBLE && !doneClicked && !tabChanged){
					showMissingUserInfoDialog(v, countryInfo.localizedPostalCodeAuditErrorMessage);
					shownWarning = true;
				}
				customerInfo.setShipZip(tempShipZip);
			} 
		} else {
			if(v.getId() == R.id.et_shipping_state){
				View parent = (View) v.getParent();
				showSelectStateList(v, parent.getBottom(), detailContainer.getLeft(), detailContainer.getWidth());
			}
		}
		return shownWarning;
	}
	
	private class SettingTextWatcher implements TextWatcher {
		
		private String key = "";
		
		public SettingTextWatcher(String key){
			this.key = key;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			attr.put(key, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		}

		@Override
		public void afterTextChanged(Editable s) {
			
		}
		
	}
	
	private void showSelectStateList(final View v, final int top, int left, final int width){		
		long time = 100;
		if (imm != null && imm.isActive()) {
			hideSoftKeyboard();
			time = 500;
		}
		new Handler().postDelayed(new Runnable() {			
			@Override
			public void run() {
				CountryInfo countryInfo = RssTabletApp.getInstance().getCountryInfo(app.getCountrycodeCurrentUsed());
				if(countryInfo!=null && countryInfo.countrySubregions!=null && countryInfo.countrySubregions.size()>0){
					states = new ArrayList<String>();
					Iterator<String> iter = countryInfo.countrySubregions.keySet().iterator();
					while(iter.hasNext()){
						states.add(iter.next());
					}
					states = (ArrayList<String>) sortByFirstChar(states);
					final View view = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.setup_state_select, null);
					ListView lvStates = (ListView) view.findViewById(R.id.lv_states);
					lvStates.setAdapter(new StateAdapter(SettingsActivity.this));
					lvStates.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							etShippingState.setText(states.get(position));
							customerInfo.setShipState(states.get(position));
							etShippingZip.requestFocus();
							stateWindow.dismiss();
						}
					});
					int swWidth = (int)(width/2);
					int swHeight = detailContainer.getHeight()- top;
					stateWindow = new PopupWindow(view, swWidth, swHeight, true);
					
					//The two lines below can make popwindow can be dismissed when click system back button or click outside
					stateWindow.setBackgroundDrawable(new PaintDrawable(Color.TRANSPARENT));
					stateWindow.setOutsideTouchable(true);
					
//					int wleft = (v.getWidth() - swWidth)/2;
//					stateWindow.showAsDropDown(v, wleft, 0);
							
					int[] location = new int[2];
					v.getLocationOnScreen(location);	
					int x = location[0]+(v.getWidth() - swWidth)/2;
					int y = location[1]+v.getHeight();			
					
					stateWindow.showAtLocation(v, Gravity.NO_GRAVITY, x, y);
				}
			}
		}, time);
	}
	
	private void hideSoftKeyboard(){
		if (imm != null) {
			imm.hideSoftInputFromWindow(SettingsActivity.this.getCurrentFocus().getWindowToken(), 0);	
		}			
	}
	
	private List<String> sortByFirstChar(List<String> source){
		ArrayList<String> result = new ArrayList<String>();
		String[] s = new String[source.size()];
		for (int i = 0 ; i < source.size() ; i ++) {
			s[i] = source.get(i);
		}
		int i,j,n=s.length;
		for(i=1;i<n;i++){
			for(j=0;j<n-i;j++){
				if(s[j].compareTo(s[j+1])>0){
					String temp = s[j];
					s[j] = s[j+1];
					s[j+1] = temp;     
				}
			}
		}
		for (int h = 0 ; h < s.length ; h ++) {
			result.add(s[h]);
		}
		return result;
	}
	
	class StateHolder{
		TextView tvStateItem;
	}
	
	private class StateAdapter extends BaseAdapter {
		LayoutInflater mInflater;
		
		public StateAdapter(Context context){
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			StateHolder holder = null;
			if(convertView == null){
				holder = new StateHolder();
				convertView = mInflater.inflate(R.layout.setup_state_item, null);
				holder.tvStateItem = (TextView) convertView.findViewById(R.id.tv_state_item);
				convertView.setTag(holder);
			} else {
				holder = (StateHolder) convertView.getTag();
			}
			holder.tvStateItem.setText(states.get(position));
			return convertView;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public Object getItem(int position) {
			return position;
		}
		
		@Override
		public int getCount() {
			if(states != null){
				return states.size();
			}
			return 0;
		}
		
	}

	@Override
	public void onBackPressed() {
		// do nothing
	}

	private void showMissingUserInfoDialog(View v, String errorMsg){
		hideSoftInputWindow(v);
		new InfoDialog.Builder(this).setMessage(errorMsg)
		.setPositiveButton(R.string.d_yes, null)	
		.create().show();			
	}
	
}
