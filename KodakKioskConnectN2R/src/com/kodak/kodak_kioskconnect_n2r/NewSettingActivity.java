package com.kodak.kodak_kioskconnect_n2r;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.kodak.kodak_kioskconnect_n2r.QuickBookFlipperActivity.ViewHolder;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.kodak_kioskconnect_n2r.bean.retailer.CountryInfo;
import com.kodak.kodak_kioskconnect_n2r.newsetting.ShippingEditText;
import com.kodak.shareapi.AccessTokenResponse;
import com.kodak.shareapi.ClientTokenResponse;
import com.kodak.shareapi.TokenGetter;
import com.kodak.utils.RSSLocalytics;

public class NewSettingActivity extends MapActivity {

	protected String TAG = this.getClass().getSimpleName();
	private ScrollView mScrollContainer;
	private RadioGroup mGroup;
	private RadioButton mRadioCusInfo;
	private RadioButton mRadioPrintSize;
	private RadioButton mRadioAddress;
	private ImageView mImageViewAddressLine;
	private RadioButton mRadioStoreInfo;
	private RadioButton mRadioOrderHistory;
	private RadioButton mRadioLegal;
	private RadioButton mRadioAbout;
	private RelativeLayout mRelaLeft;
	private RelativeLayout mRelaRight;
	private int currentItem = R.id.radio_cusInfo;
	private ArrayList<Integer> mLayoutIds = new ArrayList<Integer>();

	private Button mBtnBack;
	private Button mBtnNext;
	private TextView mTxtTitle;

	/**
	 * customer information
	 */
	private EditText mFirstNameEditText;
	private EditText mLastNameEditText;
	private EditText mPhoneEditText;
	private EditText mEmailEditText;
	private TextView mSelectedCountry;
	private Button btnChangeCountry;
	private String countryName = "";
	private ImageView mImgBulFirstNameCus;
	private ImageView mImgBulLastNameCus;
	private ImageView mImgBulEmailCus;
	private ImageView mImgBulPhoneCus;

	/**
	 * default print size
	 */
	private RadioGroup mGroupSize;
	private TextView mTxtWait;

	/**
	 * shipping address
	 */
	private ShippingEditText mEditFirstName;
	private ShippingEditText mEditLastName;
	private ShippingEditText mEditAddressOne;
	private ShippingEditText mEditAddressTwo;
	private ShippingEditText mEditEmail;
	private ShippingEditText mEditPhone;
	private ShippingEditText mEditCity;
	private EditText mEditState;
	private ShippingEditText mEditZipcode;
	private TextView mTextCountryName;
	private ImageView mImgBulFirstNameShip;
	private ImageView mImgBulLastNameShip;
	private LinearLayout mLayoutMore;
	private ImageView mImgBulAddOneShip;
	private ImageView mImgBulAddTwoShip;
	private ImageView mImgBulCityShip;
	private ImageView mImgBulStateShip;
	private ImageView mImgBulZipShip;
	private ImageView mImgBulEmailShip;
	private ImageView mImgBulPhoneShip;
	private ImageView mImgBulCountryNameShip;
	private ArrayList<String> stateList = new ArrayList<String>();
	private int popWidth = -1;
	private int popHeight = -1;
	private LinearLayout shippingLayout;
	public static PopupWindow mPopupWindow;
	private boolean alreadyPop = false;
	private long downTime = 0;
	private ScrollView mScrollView;
	private RelativeLayout mShipRelativeLayout;
	private float offsetY = -1;
	private float offsetX = -1;
	private LinearLayout mStateLayoutAll;
	private LinearLayout mStateLayout;

	private int[] locationState = new int[2];

	/**
	 * store information
	 */
	private Button changeStore;
	private InfoDialog.InfoDialogBuilder connectBuilder;
	private TextView storeName;
	private TextView storeAddress;
	private TextView mTxtCityAndZip;
	private TextView storeNumber;
	private TextView storeHours;
	private MapView mapView;
	private PictureKiosks mapKiosks;
	private FrameLayout mapViewFrame;
	private LinearLayout mLayout;

	/**
	 * order history
	 */
	private LinearLayout previousOrderLL;
	private ArrayList<Order> previousOrders;
	private ImageSelectionDatabase mImageSelectionDatabase;
	private String FORM_SETTING = "isFromSetting";
	private boolean isFromSetting = true;

	/**
	 * legal
	 */
	private Button readLicenseBtn;
	private Button readPolicyBtn;
	private CheckBox allowCookiesCB1;
	private CheckBox checkTwo;

	/**
	 * about
	 */
	private TextView mTxtVersion;
	private Button mBtnRate;
	private WebView mWebView;
	private String versionName = "";
	private boolean isCountryChanged = false;
	private String selectedPrintStr = "";

	private static final int SIGNIN_START = 0;
	private static final int SIGNIN_FINISH = 1;
	private static final int SIGNIN_FAILED = 2;
	public static final String ENABLE_ALLOW_COOKIES = "enable_allow_cookies";
	public static String SHOW_CHANGE_COUNTRY = "showChangeCountry";
	private boolean showChangeCountry = false;

	private HashMap<Integer, View> cacheMap4View = new HashMap<Integer, View>();
	private boolean isHideLeft = false;

	private SharedPreferences pref;
	private String retailerId = "";
	private ArrayList<Integer> requireList = new ArrayList<Integer>();

	public static final String SETTINGS_LOCATION = "Settings Location";
	private final String SETTINGS_SUMMARY = "Settings Summary";
	private final String EVENT_CUSINFO = "Customer Info Changed";
	private final String EVENT_SHIPINFO = "Shipping Address Changed";
	private final String EVENT_PRINT_SIZE_CHANGED = "User Changed Default Print Size";
	private final String EVENT_LICENSE = "License viewed";
	private final String EVENT_POLICY = "Privacy Policy viewed";
	private final String EVENT_ORDHISTORY = "Order History viewed";
	private final String EVENT_COOKIE = "Agree to Tracking";
	private final String EVENT_ABOUT = "About Screen viewed";
	private final String EVENT_RATE = "App Rated";
	private final String YES = "yes";
	private final String NO = "no";
	private HashMap<String, String> attr = new HashMap<String, String>();
	private String settingsLocation = "";
	private boolean previousAllowcookie = false;
	
	
	private int mEachItemHeight ;

	Runnable signInRunnable = new Runnable() {

		@Override
		public void run() {
			waitingHandler.sendEmptyMessage(SIGNIN_START);
			TokenGetter tokenGetter = new TokenGetter();
			ClientTokenResponse clientTokenResponse = tokenGetter
					.httpClientTokenUrlPost(ShareLoginActivity.CLIENT_TOKEN);
			if (clientTokenResponse != null) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(NewSettingActivity.this);
				String username = prefs.getString(PrintHelper.SHARE_EMAIL_FLAG,
						"");
				String userPwd = prefs.getString(
						PrintHelper.SHARE_PASSWORD_FLAG, "");
				AccessTokenResponse accessTokenResponse = null;
				try {
					int count = 0;
					while (count < 2 && accessTokenResponse == null) {
						Log.e(TAG, "Account: " + username + " Password: "
								+ userPwd);
						accessTokenResponse = tokenGetter
								.httpAccessTokenUrlPost(
										ShareLoginActivity.ACCESS_TOKEN_HOST,
										clientTokenResponse.client_token,
										username, userPwd,
										clientTokenResponse.client_secret);
						count++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (accessTokenResponse != null) {
					PrintHelper.setAccessTokenResponse(accessTokenResponse,
							getApplicationContext());
					waitingHandler.sendEmptyMessage(SIGNIN_FINISH);
				} else {
					Log.e(TAG, "Can not get access token response.");
					waitingHandler.sendEmptyMessage(SIGNIN_FAILED);
				}
			} else {
				Log.e(TAG, "Can not get client token response.");
				waitingHandler.sendEmptyMessage(SIGNIN_FAILED);
			}
		}
	};

	Handler waitingHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case SIGNIN_START:
				Log.d(TAG, "SIGNIN_START");
				showDialog();
				break;
			case SIGNIN_FINISH:
				Log.d(TAG, "SIGNIN_FINISH");
				waitingDialog.dismiss();
				finish();
				break;
			case SIGNIN_FAILED:
				Log.d(TAG, "SIGNIN_FAILED");
				waitingDialog.dismiss();
				finish();
				break;
			}
		}
	};

	private void initLocalyticsAttr() {
		attr.put(EVENT_CUSINFO, NO);
		attr.put(EVENT_SHIPINFO, NO);
		attr.put(EVENT_PRINT_SIZE_CHANGED, NO);
		attr.put(EVENT_LICENSE, NO);
		attr.put(EVENT_POLICY, NO);
		attr.put(EVENT_ORDHISTORY, NO);
		attr.put(EVENT_COOKIE, YES);
		attr.put(EVENT_ABOUT, NO);
		attr.put(EVENT_RATE, NO);
	}

	private InfoDialog waitingDialog;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.newsetting);
		popWidth = getWindowManager().getDefaultDisplay().getWidth() * 2 / 11;
		popHeight = getWindowManager().getDefaultDisplay().getHeight() / 2;
		PrintHelper.handleUncaughtException(NewSettingActivity.this, this);

		findViews();
		
		pref = PreferenceManager
				.getDefaultSharedPreferences(NewSettingActivity.this);
		if (getIntent().getBooleanExtra("requireInfoEntry", false)) {
			isHideLeft = true;
		}
		currentItem = getIntent().getIntExtra("currentItem", R.id.radio_cusInfo);
		mBtnBack.setVisibility(View.INVISIBLE);
		mTxtTitle.setText(getResources().getString(R.string.setup));
		mBtnNext.setText(getResources().getString(R.string.done));
		mBtnNext.setTypeface(PrintHelper.tf);

		initLocalyticsAttr();
		RSSLocalytics.onActivityCreate(this);
		Bundle b = getIntent().getExtras();
		if (b != null) {
			isCountryChanged = b.getBoolean(CountrySelectionActivity.COUNTRY_CHANGED);
			settingsLocation = b.getString(SETTINGS_LOCATION);
			if(settingsLocation != null){
				attr.put(SETTINGS_LOCATION, settingsLocation);
			}
			showChangeCountry = b.getBoolean(SHOW_CHANGE_COUNTRY, false) && !AppContext.getApplication().isContinueShopping(); 
		}
		if (mBtnNext != null) {
			mBtnNext.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(NewSettingActivity.this);
					
					if (prefs.getBoolean("analytics", false)) {
						try {
							PrintHelper.mTracker.trackEvent("Settings", "*Default_Print_Size", selectedPrintStr, 0);
							PrintHelper.mTracker.dispatch();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					if (mEmailEditText != null && !mEmailEditText.getText().toString().equals("")) {
						if (!isValidEmail(trimSpace(mEmailEditText.getText().toString()))) {
						} else {
							mEmailEditText.setError(null);
						}
					}
					
						
					Editor editor = prefs.edit();
						
					if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
						if (mFirstNameEditText != null && mFirstNameEditText.getText().toString() != null 
								&& !"".equals(mFirstNameEditText.getText().toString())) 
							editor.putString("firstName", trimSpace(mFirstNameEditText.getText().toString()));
						
						if (mLastNameEditText != null && mLastNameEditText.getText().toString() != null 
								&& !"".equals(mLastNameEditText.getText().toString())) 
							editor.putString("lastName", trimSpace(mLastNameEditText.getText().toString()));
						
						if (mPhoneEditText != null && mPhoneEditText.getText().toString() != null 
								&& !"".equals(mPhoneEditText.getText().toString()))
							editor.putString("phone", trimSpace(mPhoneEditText.getText().toString()));
						
						if (mEmailEditText != null && mEmailEditText.getText().toString() != null 
								&& !"".equals(mEmailEditText.getText().toString()) && isValidEmail(trimSpace(mEmailEditText.getText().toString().trim())))
							editor.putString("email", trimSpace(mEmailEditText.getText().toString()));
						
						if (mEditFirstName != null &&  mEditFirstName.getText().toString() != null 
								&& !"".equals(mEditFirstName.getText().toString()))
							editor.putString("firstNameShip", trimSpace(mEditFirstName.getText().toString()));
						
						if (mEditLastName != null &&  mEditLastName.getText().toString() != null 
								&& !"".equals(mEditLastName.getText().toString()))
							editor.putString("lastNameShip", trimSpace(mEditLastName.getText().toString()));
						
						if (mEditAddressOne != null &&  mEditAddressOne.getText().toString() != null 
								&& !"".equals(mEditAddressOne.getText().toString()))
							editor.putString("addressOneShip", trimSpace(mEditAddressOne.getText().toString()));
							
						if (mEditAddressTwo != null &&  mEditAddressTwo.getText().toString() != null 
								&& !"".equals(mEditAddressTwo.getText().toString()))
							editor.putString("addressTwoShip", trimSpace(mEditAddressTwo.getText().toString()));
						
						if (mEditCity != null &&  mEditCity.getText().toString() != null 
								&& !"".equals(mEditCity.getText().toString()))
							editor.putString("cityShip", trimSpace(mEditCity.getText().toString()));
						
						if (mEditState != null &&  mEditState.getText().toString() != null 
								&& !"".equals(mEditState.getText().toString()))
							editor.putString("stateShip", trimSpace(mEditState.getText().toString()));
						
						if (mEditZipcode != null) {
							if(PrintHelper.selectedCountryInfo!=null){
//								CountryInfo countryInfo = PrintHelper.selectedCountryInfo;
								if ( mEditZipcode.getText().toString() != null && !"".equals(mEditZipcode.getText().toString())
										&& validateInput(mEditZipcode.getText().toString(), PrintHelper.selectedCountryInfo.postalCodeAuditExpression)) {
									editor.putString("zipcodeShip", trimSpace(mEditZipcode.getText().toString()));
								} 
							}
						}
							
						if (mTextCountryName != null &&  mTextCountryName.getText().toString() != null 
								&& !"".equals(mTextCountryName.getText().toString()))
							editor.putString("countryNameShip", trimSpace(mTextCountryName.getText().toString()));
						
						if (mEditPhone != null &&  mEditPhone.getText().toString() != null 
								&& !"".equals(mEditPhone.getText().toString()))
							editor.putString("phoneShip", trimSpace(mEditPhone.getText().toString()));
						
						if (selectedPrintStr != null &&  selectedPrintStr.trim().length() != 0)
							editor.putString("defaultSize", selectedPrintStr);
					
						editor.commit();
					}
					
					if (!countryName.equals("") && PrintHelper.countries != null) {
						Iterator<Entry<String, String>> iter = PrintHelper.countries.entrySet().iterator();
						while(iter.hasNext()) {
							Entry<String, String> entry = iter.next();
							String tempName = entry.getValue();
							if(tempName.equals(countryName)){
								String countryCode = entry.getKey();
								prefs.edit().putString(MainMenu.SelectedCountryCode, countryCode).commit();
								prefs.edit().putString(MainMenu.CurrentlyCountryCode, countryCode).commit();
								Log.e(TAG, "countryCode:" + countryCode);
								break;
							}
						}
					}
					if (!AppContext.getApplication().getFlowType().isWifiWorkFlow() && getApplicationContext().getPackageName().contains("wmc")) {
						long now = new Date().getTime()/1000;
						long expire = Long.parseLong(PrintHelper.getAccessTokenResponse(getApplicationContext()).expire_in);
						long pass = now - PrintHelper.getAccessTokenResponse(getApplicationContext()).getAccessTokenTime;
						Log.e(TAG, "AccessToken expire in: " + expire + "; Time have pass: " + pass);
						if(!prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "").equals("") 
								&& !prefs.getString(PrintHelper.SHARE_PASSWORD_FLAG, "").equals("") && ((pass + 60) > expire)){
							new Thread(signInRunnable).start();
						} else {
							NewSettingActivity.this.finish();
						}
					} else {
						if (isCountryChanged){
							Intent intent = new Intent(NewSettingActivity.this, MainMenu.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							PrintHelper.products = new ArrayList<PrintProduct>();
						} else	if (currentItem == R.id.radio_cusInfo) {
							String oldFirstName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("firstName", "");
							String newFirstName = mFirstNameEditText.getText().toString();
							if(!oldFirstName.equals(newFirstName) && (newFirstName.equals("RSS_Staging") 
									|| newFirstName.equals("RSS_Production") || newFirstName.equals("RSS_Development")
									|| newFirstName.equals("RSS_ENV1") || newFirstName.equals("RSS_ENV2"))){
								Log.e(TAG, oldFirstName + " --> " + newFirstName);
								Intent intent = new Intent(NewSettingActivity.this, MainMenu.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								PrintHelper.products = new ArrayList<PrintProduct>();
							} else {
								NewSettingActivity.this.finish();
							}
						}else {
							NewSettingActivity.this.finish();
						}
					}
					if(prefs.getBoolean(AppConstants.KEY_LOCALYTICS, false)){
						//attr.put(EVENT_COOKIE, YES);
						RSSLocalytics.onActivityCreate(NewSettingActivity.this);
					}
					editor.commit();
					if(previousAllowcookie){
						RSSLocalytics.recordLocalyticsEventsIgnoreTheSwitch(NewSettingActivity.this, SETTINGS_SUMMARY, attr);
					}else {
						RSSLocalytics.recordLocalyticsEvents(NewSettingActivity.this, SETTINGS_SUMMARY, attr);	
					}
										
				}
			});
		}
		
		mGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				currentItem = checkedId;
				saveData() ;
				if (checkedId == R.id.radio_cusInfo) {
					layoutCusInfo();
					refreshPopStatus();
				} else if (checkedId == R.id.radio_printSize) {
					layoutDefaultPrintSize();
					refreshPopStatus();
				} else if (checkedId == R.id.radio_address) {
					layoutShiAddress();
					refreshPopStatus();
				} else if (checkedId == R.id.radio_storeInfo) {
					layoutStoreInfo();
					refreshPopStatus();
				} else if (checkedId == R.id.radio_orderHistory) {
					layoutOrderHistory();
					refreshPopStatus();
				} else if (checkedId == R.id.radio_legal) {
					layoutLegal();
					refreshPopStatus();
				} else if (checkedId == R.id.radio_about) {
					layoutAbout();
					refreshPopStatus();
				} 
				
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		RSSLocalytics.onActivityResume(this);
		if (PrintHelper.orderType == 0) {
			retailerId = "";
		} else if (PrintHelper.orderType == 1) {
		    retailerId = pref.getString("selectedRetailerId", "");
		} else if (PrintHelper.orderType == 2) {
			retailerId = pref.getString("retailerIdPayOnline", "");
		}
		if (retailerId.trim().length() != 0 ) {
			Iterator iter = PrintHelper.requiredContactInfos.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, ArrayList<Integer>> entry = (Entry<String, ArrayList<Integer>>) iter.next();
				String key = entry.getKey();
				if (retailerId.equals(key)) {
					requireList = entry.getValue();
					break;
				}
			}
		}
		for (int i = 0 ; i < mLayoutIds.size() ; i ++) {
			if (mLayoutIds.get(i) != R.id.rela_storeinfo) {
				if (cacheMap4View.containsKey(mLayoutIds.get(i)) && cacheMap4View.get(mLayoutIds.get(i)) != null) {
					mRelaRight.removeView(cacheMap4View.get(mLayoutIds.get(i)));
					cacheMap4View.remove(mLayoutIds.get(i));
				}
				mLayoutIds.remove(i);
			}
		}
		boolean isShipToHome = pref.getBoolean("ifCanShipToHome", false) ;
		boolean flag = pref.getBoolean("ifCanFollowCLOLite", false) ;
		if(flag||isShipToHome){
			mRadioAddress.setVisibility(View.VISIBLE) ;
			mImageViewAddressLine.setVisibility(View.VISIBLE) ;
		}else {
			mRadioAddress.setVisibility(View.GONE) ;
			mImageViewAddressLine.setVisibility(View.GONE) ;
		}
		
		if (currentItem == R.id.radio_cusInfo) {
			layoutCusInfo();
			mRadioCusInfo.setChecked(true);
		} else if (currentItem == R.id.radio_printSize) {
			layoutDefaultPrintSize();
			mRadioPrintSize.setChecked(true);
		} else if (currentItem == R.id.radio_address && (flag||isShipToHome)) {
			layoutShiAddress();
			mRadioAddress.setChecked(true);
		} else if (currentItem == R.id.radio_storeInfo) {
			layoutStoreInfo();
			mRadioStoreInfo.setChecked(true);
		} else if (currentItem == R.id.radio_legal) {
			layoutLegal();
			mRadioLegal.setChecked(true);
		} else if (currentItem == R.id.radio_orderHistory) {
			layoutOrderHistory();
			mRadioOrderHistory.setChecked(true);
		} else if (currentItem == R.id.radio_about) {
			layoutAbout();
			mRadioAbout.setChecked(true);
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (PrintHelper.mActivities.contains(this)) {
			PrintHelper.mActivities.remove(this);
		}
	}
	
	private void showDialog(){
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(NewSettingActivity.this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.share_signin) + " ... ");
		waitingDialog = builder.create();
		waitingDialog.show();
	}
	
	private void layoutCusInfo() {
		launchLayout(R.layout.customerinformation,R.id.linear_customerinfo,mRelaRight);
		if (!mLayoutIds.contains(R.id.linear_customerinfo)) {
			mFirstNameEditText = (EditText) findViewById(R.id.firstNameEditText);
			AppContext.getApplication().setEmojiFilter(mFirstNameEditText);
			
			mLastNameEditText = (EditText) findViewById(R.id.lastNameEditText);
			AppContext.getApplication().setEmojiFilter(mLastNameEditText);
			
			mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
			AppContext.getApplication().setEmojiFilter(mPhoneEditText);
			
			mEmailEditText = (EditText) findViewById(R.id.emailEditText);
			AppContext.getApplication().setEmojiFilter(mEmailEditText);
			
			mImgBulFirstNameCus = (ImageView) findViewById(R.id.bul_firstName);
			mImgBulLastNameCus = (ImageView) findViewById(R.id.bul_lastName);
			mImgBulEmailCus = (ImageView) findViewById(R.id.bul_email);
			mImgBulPhoneCus = (ImageView) findViewById(R.id.bul_phone);
			mSelectedCountry = (TextView) findViewById(R.id.txt_selectedCountry);
			btnChangeCountry = (Button) findViewById(R.id.btn_changeCountry);
			mLayoutIds.add(R.id.linear_customerinfo);
			
			TextChangedListener textChangedListener = new TextChangedListener(EVENT_CUSINFO);
			mFirstNameEditText.addTextChangedListener(textChangedListener);
			mLastNameEditText.addTextChangedListener(textChangedListener);
			mPhoneEditText.addTextChangedListener(textChangedListener);
			mEmailEditText.addTextChangedListener(textChangedListener);
		}
		
		    String changeCountry = (getResources().getString(R.string.change_country)+" "+getResources().getString(R.string.country_label));
		    if(changeCountry.contains(":")){
		    	int index = changeCountry.lastIndexOf(":");
		    	changeCountry = changeCountry.substring(0,index);
		    }
			btnChangeCountry.setText(changeCountry);
			
			if (AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
				mFirstNameEditText.setVisibility(View.GONE);
				mLastNameEditText.setVisibility(View.GONE);
				mEmailEditText.setVisibility(View.GONE);
				mPhoneEditText.setVisibility(View.GONE);
				mSelectedCountry.setVisibility(View.GONE);
				btnChangeCountry.setVisibility(View.GONE);
			} else {
				mFirstNameEditText.setVisibility(View.VISIBLE);
				mLastNameEditText.setVisibility(View.VISIBLE);
				mEmailEditText.setVisibility(View.VISIBLE);
				mPhoneEditText.setVisibility(View.VISIBLE);
//				if (getApplicationContext().getPackageName().contains("dm")) {
				mSelectedCountry.setVisibility(isHideLeft ? View.INVISIBLE: View.VISIBLE);
				btnChangeCountry.setVisibility(isHideLeft ? View.INVISIBLE: View.VISIBLE);
//				} else {
//					mSelectedCountry.setVisibility(View.GONE);
//					btnChangeCountry.setVisibility(View.GONE);
//				}
			}
			
			if(showChangeCountry){
				btnChangeCountry.setVisibility(View.VISIBLE);
			} else {
				btnChangeCountry.setVisibility(View.INVISIBLE);
			}
			
			btnChangeCountry.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					attr.put(EVENT_CUSINFO, YES);
					Intent intent = new Intent(NewSettingActivity.this, CountrySelectionActivity.class);
					Bundle b = new Bundle();
					b.putBoolean(FORM_SETTING, isFromSetting);
					intent.putExtras(b);
					startActivity(intent);
				}
			});
			
			mEmailEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(NewSettingActivity.this);
					Editor editor = prefs.edit();
					String emailStr = mEmailEditText.getText().toString()
							.trim();
					if (emailStr.length() != 0) {
						if (isValidEmail(trimSpace(emailStr))) {
							editor.putString("email", trimSpace(emailStr));
							editor.commit();
						} else {
							if (currentItem == R.id.radio_cusInfo) {
								showInvalidateDialog(getResources().getString(
										R.string.invalidateEmail));
							}
						}
					} else {
						editor.putString("email", "");
						editor.commit();
					}
				}
			}
			});
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			String code = prefs.getString(MainMenu.SelectedCountryCode, "");
			countryName = "";
			if(PrintHelper.countries!=null){
				if(!code.equals("")){
					Iterator<Entry<String, String>> iter = PrintHelper.countries.entrySet().iterator();
					while(iter.hasNext()){
						Entry<String, String> entry = iter.next();
						if(entry.getKey().equals(code)){
							countryName = entry.getValue();
							break;
						}
					}
				} else {
					Iterator<Entry<String, String>> iter = PrintHelper.countries.entrySet().iterator();
					while(iter.hasNext()){
						Entry<String, String> entry = iter.next();
						countryName = entry.getValue();						
						break;
					}
				}
					mSelectedCountry.setText(countryName);
				}
		
			if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
			String firstName = prefs.getString("firstName", "").trim(); //TODO
			String lastName = prefs.getString("lastName", "").trim();
			String phone = prefs.getString("phone", "").trim();
			String email = prefs.getString("email", "").trim();
			
				mFirstNameEditText.setText(firstName);
				mLastNameEditText.setText(lastName);
				mPhoneEditText.setText(phone);
				if (!email.equals(""))
				{
					if (!Connection.isConnected(NewSettingActivity.this)) { 
						mEmailEditText.setText("");
					} else {
						mEmailEditText.setText(email);
					}
					
					if ((!mEmailEditText.getText().toString().contains("@") || !mEmailEditText.getText().toString().contains(".")) 
							&& !mEmailEditText.getText().toString().equals(""))
					{
						mEmailEditText.setError(getString(R.string.incorrectEmail));
					}
					
				}
				
				if (mFirstNameEditText != null && mFirstNameEditText.getText().toString().equals(""))
				{
					mFirstNameEditText.requestFocus();
				}
				else if (mLastNameEditText != null && mLastNameEditText.getText().toString().equals(""))
				{
					mLastNameEditText.requestFocus();
				}
				else if (mPhoneEditText != null && mPhoneEditText.getText().toString().equals(""))
				{
					mPhoneEditText.requestFocus();
				}
				else if (mEmailEditText != null && mEmailEditText.getText().toString().equals(""))
				{
					mEmailEditText.requestFocus();
				} 
			}
		
		for (int i = 0 ; i < requireList.size() ; i ++) {
			int value = requireList.get(i);
			if (value == 0) {
				mImgBulFirstNameCus.setVisibility(View.VISIBLE);
			} else if (value == 1) {
				mImgBulLastNameCus.setVisibility(View.VISIBLE);
			} else if (value == 2) {
				mImgBulPhoneCus.setVisibility(View.VISIBLE);
				
			} else if (value == 6) {
				mImgBulEmailCus.setVisibility(View.VISIBLE);
			}
		}
	}
	private boolean isValidEmail(String email) {
		boolean result = false;
		if (email.trim().length() == 0) {
			return true;
		}
		if (email.contains("@") && email.contains(".")) {
			int indexAt = email.indexOf("@");
			int indexDot = email.indexOf(".");
			if (indexAt != 0 && indexDot != (email.length() - 1) && indexDot != (indexAt + 1)) {
				result = true;
			}
		}
		return result;
	}
	private String trimSpace(String str) {
		String result = "";
		if(str!=null){
			result = str.trim();
		}
		
		return result;
	}
	
	private void showInvalidateDialog(String message) {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(NewSettingActivity.this);
		builder.setTitle("");
		builder.setMessage(message);
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
	
	/**
	  * validate format of email
	  * @param email
	  * @return
	  */
	 public static boolean validateInput(String str,String rule){
	  boolean flag = false;
	  if (str.trim().length() == 0) {
		  return true;
	  }
	  try{
//		   String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		  if (rule == null) {
//		   Matcher matcher = regex.matcher(email);
//		   flag = matcher.matches();
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
	
	private void layoutShiAddress() {
		launchLayout(R.layout.shippingaddress, R.id.linear_shippingaddress,mRelaRight);
		if (!mLayoutIds.contains(R.id.linear_shippingaddress)) {
			TextChangedListener textChangedListener = new  TextChangedListener(EVENT_SHIPINFO);
			shippingLayout = (LinearLayout) findViewById(R.id.linear_shippingaddress);
			mEditFirstName = (ShippingEditText) findViewById(R.id.edt_firstName);
			mEditFirstName.addTextChangedListener(textChangedListener);
			mEditLastName = (ShippingEditText) findViewById(R.id.edt_lastName);
			mEditLastName.addTextChangedListener(textChangedListener);
			mLayoutMore = (LinearLayout) findViewById(R.id.layout_more);
			mEditAddressOne = (ShippingEditText) findViewById(R.id.edt_addressOne);
			mEditAddressOne.addTextChangedListener(textChangedListener);
			mEditAddressTwo = (ShippingEditText) findViewById(R.id.edt_addressTwo);
			mEditAddressTwo.addTextChangedListener(textChangedListener);
			mEditEmail = (ShippingEditText) findViewById(R.id.edt_email);
			mEditEmail.addTextChangedListener(textChangedListener);
			mEditPhone = (ShippingEditText) findViewById(R.id.edt_phone);
			mEditPhone.addTextChangedListener(textChangedListener);
			mEditCity = (ShippingEditText) findViewById(R.id.edt_city);
			mEditCity.addTextChangedListener(textChangedListener);
			mEditState = (EditText) findViewById(R.id.edt_state);
			mEditState.addTextChangedListener(textChangedListener);
			mEditZipcode = (ShippingEditText) findViewById(R.id.edt_zipCode);
			mEditZipcode.addTextChangedListener(textChangedListener);
			mStateLayoutAll = (LinearLayout) findViewById(R.id.layout_state_all) ;
			mStateLayout = (LinearLayout) findViewById(R.id.state_layout);
			mScrollView = (ScrollView) findViewById(R.id.scrollview_ship);
			mLayoutIds.add(R.id.linear_shippingaddress);
		}
		mScrollView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				final int action = event.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					offsetY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					offsetY = event.getY();
					refreshPopStatus();
					break;
				case MotionEvent.ACTION_UP:
					offsetY = event.getY();
					break;
				}
				return false;
			}
		});
		mTextCountryName = (TextView) findViewById(R.id.edt_countryName);
		mImgBulFirstNameShip = (ImageView) findViewById(R.id.bul_firstName_ship);
		mImgBulLastNameShip = (ImageView) findViewById(R.id.bul_lastName_ship);
		mImgBulAddOneShip = (ImageView) findViewById(R.id.bul_addressOne_ship);
		mImgBulAddTwoShip = (ImageView) findViewById(R.id.bul_addressTwo_ship);
		mImgBulEmailShip = (ImageView) findViewById(R.id.bul_email_ship);
		mImgBulPhoneShip = (ImageView) findViewById(R.id.bul_phone_ship);
		mImgBulCityShip = (ImageView) findViewById(R.id.bul_city_ship);
		mImgBulStateShip = (ImageView) findViewById(R.id.bul_state_ship);
		mImgBulZipShip = (ImageView) findViewById(R.id.bul_zipcode_ship);
		mImgBulCountryNameShip = (ImageView) findViewById(R.id.bul_countryName_ship);
		mShipRelativeLayout = (RelativeLayout) findViewById(R.id.reta_top);
			
		mEditState.setFocusable(false);
			
		mEditZipcode.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(NewSettingActivity.this);
					Editor editor = prefs.edit();
					String zipcode = mEditZipcode.getText().toString().trim();
						if (zipcode.length() != 0) {
							if (PrintHelper.selectedCountryInfo!= null && validateInput(trimSpace(mEditZipcode.getText().toString().trim()),PrintHelper.selectedCountryInfo== null ? 
									null : PrintHelper.selectedCountryInfo.postalCodeAuditExpression) ) {
								editor.putString("zipcodeShip", trimSpace(zipcode));
								editor.commit();
							} else {
								if (PrintHelper.selectedCountryInfo != null && currentItem == R.id.radio_address) {
									showInvalidateDialog(PrintHelper.selectedCountryInfo.localizedPostalCodeAuditErrorMessage);
								}
							}
						} else {
							editor.putString("zipcodeShip", "");
							editor.commit();
						}
					}
			}
		});
			
		mEditEmail.setOnFocusChangeListener(new OnFocusChangeListener() {
				
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(NewSettingActivity.this);
					Editor editor = prefs.edit();
					String emailStr = mEditEmail.getText().toString().trim();
					if (emailStr.length() != 0) {
						if (isValidEmail(trimSpace(emailStr))) {
							editor.putString("emailShip", trimSpace(emailStr));
							editor.commit();
						} else {
							if (currentItem == R.id.radio_address) {
								showInvalidateDialog(getResources().getString(R.string.invalidateEmail));
							}
						}
					}else {
						editor.putString("emailShip", "");
						editor.commit();
					}
				}

			}
		}) ;
			
			
		if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
			String firstNameShip = pref.getString("firstNameShip", ""); //TODO
			String lastNameShip = pref.getString("lastNameShip", "");
			String addressOne = pref.getString("addressOneShip", "");
			String addressTwo = pref.getString("addressTwoShip", "");
			String city = pref.getString("cityShip", "");
			String state = pref.getString("stateShip", "");
			String zipcode = pref.getString("zipcodeShip", "");
			String phoneShip = pref.getString("phoneShip", "");
			String emailShip = pref.getString("emailShip", "" ) ;
			String countryName = pref.getString("countryNameShip", "");
			
			mEditFirstName.setText(firstNameShip);
			mEditLastName.setText(lastNameShip);
			mEditEmail.setText(emailShip) ;
			mEditPhone.setText(phoneShip);
			mEditAddressOne.setText(addressOne);
			mEditAddressTwo.setText(addressTwo);
			mEditCity.setText(city);
			mEditState.setText(state);
			mEditZipcode.setText(zipcode);
			mTextCountryName.setText(countryName);
		}
			
		if (AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
			mEditFirstName.setVisibility(View.GONE);
			mEditLastName.setVisibility(View.GONE);
			mEditAddressOne.setVisibility(View.GONE);
			mEditAddressTwo.setVisibility(View.GONE);
			mEditCity.setVisibility(View.GONE);
			mEditState.setVisibility(View.GONE);
			mEditPhone.setVisibility(View.GONE);
			mEditEmail.setVisibility(View.GONE);
		} else {
			mEditFirstName.setVisibility(View.VISIBLE);
			mEditLastName.setVisibility(View.VISIBLE);
			mEditAddressOne.setVisibility(View.VISIBLE);
			mEditAddressTwo.setVisibility(View.VISIBLE);
			mEditCity.setVisibility(View.VISIBLE);
			mEditState.setVisibility(View.VISIBLE);
			mEditPhone.setVisibility(View.VISIBLE);
			mEditEmail.setVisibility(View.VISIBLE);
		}		
		
		for (int i = 0 ; i < requireList.size() ; i ++) {
			int value = requireList.get(i);
			if (value == 0) {
				mImgBulFirstNameShip.setVisibility(View.VISIBLE);
			} else if (value == 1) {
				mImgBulLastNameShip.setVisibility(View.VISIBLE);
			} else if (value == 2) {
				mImgBulPhoneShip.setVisibility(View.VISIBLE);
			} else if (value == 6) {
				mImgBulEmailShip.setVisibility(View.VISIBLE);
			}
		}
		
		
		if (PrintHelper.selectedCountryInfo != null ) {
			mLayoutMore.setVisibility(View.VISIBLE);
			stateList.clear();
			HashMap<String, String> stateHashMap = PrintHelper.selectedCountryInfo.countrySubregions;
			if(stateHashMap!=null){
				Set<Entry<String, String>> set = stateHashMap.entrySet() ;
				Iterator<Entry<String, String>> iterator = set.iterator();
				while (iterator.hasNext()) {
					Entry<String, String> entry = iterator.next();
					stateList.add(entry.getKey());
				}
			}
			
			CountryInfo countryInfo = PrintHelper.selectedCountryInfo;
			if (countryInfo != null) {
				boolean isHomeDelivery =  PrintHelper.orderType ==2 ? true :false ;
				if(isHomeDelivery){
					mImgBulAddOneShip.setVisibility(View.VISIBLE);
				}
				mTextCountryName.setText(countryInfo.countryName);
				String addressStyle = countryInfo.addressStyle;
			
				if (isHomeDelivery && addressStyle.contains(CountryInfo.CITY)) {
					mImgBulCityShip.setVisibility(View.VISIBLE);
				}
				if(stateList!=null && stateList.size()>0){
					mStateLayoutAll.setVisibility(View.VISIBLE) ;
					if (isHomeDelivery && addressStyle.contains(CountryInfo.STATE)) {
						mImgBulStateShip.setVisibility(View.VISIBLE);
					}
				}else {
					mImgBulStateShip.setVisibility(View.INVISIBLE) ;
					mStateLayoutAll.setVisibility(View.GONE) ;
					
				}
				
				if (isHomeDelivery && addressStyle.contains(CountryInfo.ZIP)) {
					mImgBulZipShip.setVisibility(View.VISIBLE);
				}
			}
			mEditZipcode.setHint(PrintHelper.selectedCountryInfo.localizedPostalCodeName.toString());
			mEditState.setHint(PrintHelper.selectedCountryInfo.localizedSubregionName.toString());
		}else{
			//TODO
			mImgBulFirstNameShip.setVisibility(View.INVISIBLE);
			mImgBulLastNameShip.setVisibility(View.INVISIBLE);
			mLayoutMore.setVisibility(View.GONE);
		}
		mStateLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float downPostionX;
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					long time = new Date().getTime() - downTime;
					System.out.println("timetime time = " + time);
					if(time<200){
						if (mPopupWindow == null || !mPopupWindow.isShowing()) {
								popEditWindow(sortState());
								alreadyPop = true;
						} else {
							mPopupWindow.dismiss();
						}
					}
					break;
				case MotionEvent.ACTION_DOWN:
					downTime = new Date().getTime();
					downPostionX = event.getX();
					break;
				}
				return true;
			}
		});
	}
	
	private String[] sortState() {
		ArrayList<String> result = new ArrayList<String>();
		String[] s = new String[stateList.size()];
		for (int i = 0 ; i < stateList.size() ; i ++) {
			s[i] = stateList.get(i);
		}
		
		  for(int i=0;i<s.length;i++){
		   System.out.println(s[i]);
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
		
		  for(int ii=0;ii<s.length;ii++){
		   System.out.println(s[ii]);
		  } 
		for (int h = 0 ; h < s.length ; h ++) {
			result.add(s[h]);
		}
		return s;
	}
	
	private void saveDataToSharedPreference( EditText mEditText,  String prefStr ,Editor editor) {
		if(mEditText!=null){
		
		String str = mEditText.getText().toString().trim();
		if (!"".equals(str)) {
			editor.putString(prefStr, trimSpace(str));
		} else {
			editor.putString(prefStr, "");
		}
	  }
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}
	
	private void layoutDefaultPrintSize() {
		launchLayout(R.layout.defaultprintsize, R.id.linear_defaultsize,mRelaRight);
		if (!mLayoutIds.contains(R.id.linear_defaultsize)) {
			mGroupSize = (RadioGroup) findViewById(R.id.radiogroup_size);
			mTxtWait = (TextView) findViewById(R.id.txt_wait);
			
			if (AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
				mGroupSize.setVisibility(View.GONE);
			} else {
				mGroupSize.setVisibility(View.VISIBLE);
			}
			
			if(!Connection.isConnected(NewSettingActivity.this)) {
				mGroupSize.setEnabled(false);
			} else {
				mGroupSize.setEnabled(true);
			}
			
			mGroupSize.removeAllViews();
			if (PrintHelper.products != null && !PrintHelper.products.isEmpty()) {
				mTxtWait.setVisibility(View.GONE);
				DisplayMetrics myMetrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(myMetrics);
				float myDensity = myMetrics.density;

				for (int i = 0; i < PrintHelper.products.size(); i++) {
					if (PrintHelper.products.get(i) != null
							&& PrintHelper.products.get(i).getType()
									.equals(PrintProduct.TYPE_PRINTS)) {
						RadioButton mButton = new RadioButton(
								NewSettingActivity.this);
						mButton.setButtonDrawable(new ColorDrawable(
								Color.TRANSPARENT));
						mButton.setButtonDrawable(R.drawable.radio_circle_background);
						mButton.setPadding((int)(70*myDensity*0.7), (int) (20 * myDensity), 0,
								(int) (20 * myDensity));
						mButton.setText(PrintHelper.products.get(i)
								.getShortName());

						LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);

						mLayoutParams.gravity = Gravity.LEFT;

						mButton.setLayoutParams(mLayoutParams);
						mGroupSize.addView(mButton);
					}
				}
			} else {
				mTxtWait.setVisibility(View.VISIBLE);
			}
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(NewSettingActivity.this);
			String defaultSize = prefs.getString("defaultSize", "");
			boolean containedDefaultSize = false;
			if ((defaultSize == null || defaultSize.trim().length() == 0) && mGroupSize.getChildCount() > 0) {
				((RadioButton) mGroupSize.getChildAt(0)).setChecked(true);
			} else {
				for (int i = 0 ; i < mGroupSize.getChildCount() ; i ++) {
					RadioButton mButton = (RadioButton) mGroupSize.getChildAt(i);
					if (mButton.getText().toString().equals(prefs.getString("defaultSize", ""))) {
						mButton.setChecked(true);
						containedDefaultSize = true;
					} else {
						mButton.setChecked(false);
					}
				}
				if (!containedDefaultSize){
					if ((RadioButton) mGroupSize.getChildAt(0)!=null){
						((RadioButton) mGroupSize.getChildAt(0)).setChecked(true);
					}
				}
			}
			
			
			mGroupSize.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					RadioButton mButton = (RadioButton) findViewById(checkedId);
					if (mButton != null) {
						selectedPrintStr = mButton.getText().toString();
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(NewSettingActivity.this);
						prefs.edit().putString("defaultSize", selectedPrintStr).commit();
						PrintHelper.defaultPrintSizeIndex = mGroupSize.indexOfChild(mButton);
						attr.put(EVENT_PRINT_SIZE_CHANGED, selectedPrintStr);
					}
				}
			});
			mLayoutIds.add(R.id.linear_defaultsize);
		}
		
	}
	
	private void layoutAbout() {
		attr.put(EVENT_ABOUT, YES);
		launchLayout(R.layout.about, R.id.rela_about, mRelaRight);
		if (!mLayoutIds.contains(R.id.rela_about)) {
			mTxtVersion = (TextView) findViewById(R.id.txt_version);
			mBtnRate = (Button) findViewById(R.id.btn_rate);
			mBtnRate.setVisibility(View.VISIBLE);
			mWebView = (WebView) findViewById(R.id.web);
			try
			{
				PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				if(AppContext.getApplication().isBrandedApp()){
					versionName = getString(R.string.mainMenuVersion) + " " + packageInfo.versionName + " " + getString(R.string.Copyright_String);
				} else {
					versionName = getString(R.string.mainMenuVersion) + " " + packageInfo.versionName + " " + getString(R.string.Cobranded_Copyright_String);
				}
			}
			catch (NameNotFoundException e)
			{
				e.printStackTrace();
			}
			mTxtVersion.setText(versionName);
//			
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.loadUrl(getResources().getString(R.string.helpURL));
			mWebView.setWebViewClient(new WebViewClient(){
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);
					return true;
				}
			});
			mWebView.requestFocus() ;
			
			mLayoutIds.add(R.id.rela_about);
			mBtnRate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//start change by bing for fixed RSSMOBILEPDC-2089 on 2015-2-26
					String packageName = NewSettingActivity.this.getPackageName();
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
					//end change by bing for fixed RSSMOBILEPDC-2089 on 2015-2-26	
				}
			});
		}
	}
	
	private void layoutLegal() {
		previousAllowcookie = pref.getBoolean(AppConstants.KEY_LOCALYTICS, false);
		launchLayout(R.layout.legal, R.id.rela_legal, mRelaRight);
		if (!mLayoutIds.contains(R.id.rela_legal)) {
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			readLicenseBtn = (Button) findViewById(R.id.btn_readLicense);
			readPolicyBtn = (Button) findViewById(R.id.btn_readPolicy);
			allowCookiesCB1 = (CheckBox) findViewById(R.id.checkOne);
			checkTwo = (CheckBox) findViewById(R.id.checkTwo);
			if (getApplicationContext().getPackageName().contains("dm") || getApplicationContext().getPackageName().contains("wmc")) {
				allowCookiesCB1.setVisibility(View.GONE);
			} else {
				allowCookiesCB1.setVisibility(View.VISIBLE);
			}
			readLicenseBtn.setOnClickListener(new HelpClickListener("eula"));
			readPolicyBtn.setOnClickListener(new HelpClickListener("privacy"));
		
			allowCookiesCB1.setTypeface(PrintHelper.tf);
			if(prefs.getBoolean(NewSettingActivity.ENABLE_ALLOW_COOKIES, false)){
				allowCookiesCB1.setClickable(true);
			} else {
				allowCookiesCB1.setClickable(false);
				allowCookiesCB1.setButtonDrawable(R.drawable.checkbox_disable);
			}		
			allowCookiesCB1.setChecked(previousAllowcookie);
			if(previousAllowcookie){
				attr.put(EVENT_COOKIE, YES);
			}else {
				attr.put(EVENT_COOKIE, NO);
			}
			
			mLayoutIds.add(R.id.rela_legal);
			/*allowCookiesCB1.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (allowCookiesCB1.isChecked()){
						prefs.edit().putBoolean(AppConstants.KEY_LOCALYTICS, true).commit();
						attr.put(EVENT_COOKIE, YES);
					}else {
						prefs.edit().putBoolean(AppConstants.KEY_LOCALYTICS, false).commit();
						attr.put(EVENT_COOKIE, NO);
					}
					
				}
			});*/
			
			
			allowCookiesCB1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//					prefs.edit().putBoolean(AppConstants.KEY_LOCALYTICS, isChecked).commit();
					if(isChecked){
						attr.put(EVENT_COOKIE, YES);
						RSSLocalytics.openLocalytics(NewSettingActivity.this);
					}else {
						attr.put(EVENT_COOKIE, NO);
						RSSLocalytics.closeLocalytics(NewSettingActivity.this);
					}
					
					
				}
			}) ;
				
			
		}
	}
	
	class HelpClickListener implements OnClickListener {

		private String strExtra;
		
		public HelpClickListener(String strExtra) {
			this.strExtra = strExtra;
		}
		
		@Override
		public void onClick(View v) {
			if (!Connection.isConnected(NewSettingActivity.this))
			{
				connectBuilder = new InfoDialog.InfoDialogBuilder(NewSettingActivity.this);
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
			} else {
				if(strExtra.equals("eula")){
					attr.put(EVENT_LICENSE, YES);
				} else if (strExtra.equals("privacy")){
					attr.put(EVENT_POLICY, YES);
				}
				Intent myIntent = new Intent(NewSettingActivity.this, HelpActivity.class);
				myIntent.putExtra(strExtra, true);
				startActivity(myIntent);
			}
		}
		
	}
	
	@Override
	public void onPause()
	{
		RSSLocalytics.onActivityPause(this);
		super.onPause();
		saveData() ;
		if (mImageSelectionDatabase != null) {
			mImageSelectionDatabase.close();
		}
		
		if (previousOrderLL != null) {
			previousOrderLL.removeAllViews();
		}
	}
	
	
	private void saveData(){
		
		Editor editor = pref.edit();
		saveDataToSharedPreference(mFirstNameEditText,"firstName",editor);
		saveDataToSharedPreference(mLastNameEditText,"lastName",editor);
		saveDataToSharedPreference(mPhoneEditText,"phone",editor);
		
		saveDataToSharedPreference(mEditFirstName, "firstNameShip",editor);
		saveDataToSharedPreference(mEditLastName, "lastNameShip",editor);
		saveDataToSharedPreference(mEditAddressOne, "addressOneShip",editor);
		saveDataToSharedPreference(mEditAddressTwo, "addressTwoShip",editor);
		saveDataToSharedPreference(mEditCity, "cityShip",editor);
		saveDataToSharedPreference(mEditState, "stateShip",editor);
		saveDataToSharedPreference(mEditPhone, "phoneShip",editor);
		
        saveDataToSharedPreference(mEditZipcode, "zipcodeShip", editor);
        saveDataToSharedPreference(mEditEmail, "emailShip", editor);
		
		editor.commit() ;
		
		
	}
	
	private void layoutOrderHistory() {
		launchLayout(R.layout.orderhistory, R.id.rela_orderhistory, mRelaRight);
		if (!mLayoutIds.contains(R.id.rela_orderhistory)) {
			previousOrderLL = (LinearLayout) findViewById(R.id.previousOrderLL);
			if (mImageSelectionDatabase == null) {
				mImageSelectionDatabase = new ImageSelectionDatabase(NewSettingActivity.this);
				mImageSelectionDatabase.open();
			}
			mLayoutIds.add(R.id.rela_orderhistory);
		}
		if (previousOrders == null) {
			previousOrders = new ArrayList<Order>();
		} else {
			previousOrders.clear();
		}
		Thread findPreviousOrders = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					previousOrders = mImageSelectionDatabase.getPreviousOrders(NewSettingActivity.this);
					Log.d(TAG, "previousorderscount = " + previousOrders.size());
				}
				catch (Exception ex)
				{
				}
				previousOrderHandler.sendEmptyMessage(0);
			}
		};
		findPreviousOrders.start();
	}
	
	private Handler previousOrderHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (previousOrders.size() != 0) {
				PrintHelper.sentOrders = previousOrders; //add by song
				View row = null;
				previousOrderLL.removeAllViews();
				if (Connection.isConnected(NewSettingActivity.this)) {
					for (int i = 0; i < previousOrders.size(); i++)
					{
						final TextView orderID;
						final TextView orderTime;
						final Button details;
						LayoutInflater inflater = getLayoutInflater();
						row = inflater.inflate(R.layout.orderitem, null);
						orderID = (TextView) row.findViewById(R.id.orderIDTV);
						orderTime = (TextView) row.findViewById(R.id.orderTimeTV);
						details = (Button) row.findViewById(R.id.detailsButton);
						orderID.setText(previousOrders.get(i).orderID);
						orderTime.setText(previousOrders.get(i).orderTime);
						orderID.setOnClickListener(new OrderClickListener(orderID, orderTime));
						details.setOnClickListener(new OrderClickListener(orderID, orderTime));
						previousOrderLL.addView(row);
					}
				}
			}
		}
	};
	
	class OrderClickListener implements OnClickListener {

		private TextView orderID;
		private TextView orderTime;
		
		public OrderClickListener(TextView orderID, TextView orderTime) {
			this.orderID = orderID;
			this.orderTime = orderTime;
		}
		
		@Override
		public void onClick(View v) {
			attr.put(EVENT_ORDHISTORY, YES);
			Intent myIntent = new Intent(NewSettingActivity.this, OrderSummaryActivity.class);
			myIntent.putExtra(FORM_SETTING, isFromSetting);
			myIntent.putExtra("details", true);
			myIntent.putExtra("orderid", orderID.getText());
			PrintHelper.orderID = orderID.getText().toString();
			PrintHelper.orderTime = orderTime.getText().toString();
			startActivity(myIntent);
		}
		
	}
	
	private void layoutStoreInfo() {
		launchLayout(R.layout.storeinformation, R.id.rela_storeinfo, mRelaRight);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(NewSettingActivity.this);
		if (!mLayoutIds.contains(R.id.rela_storeinfo)) {
			changeStore = (Button) findViewById(R.id.btn_changeStore);
			changeStore.setTypeface(PrintHelper.tf);
			changeStore.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (!Connection.isConnected(NewSettingActivity.this))
					{
						connectBuilder = new InfoDialog.InfoDialogBuilder(NewSettingActivity.this);
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
					} else {
						Boolean isFromShoppingcart = getIntent().getBooleanExtra(AppConstants.IS_FORM_SHOPPINGCART, false);
						Intent intent = new Intent(NewSettingActivity.this, StoreFinder.class);
						intent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, isFromShoppingcart);
						startActivity(intent);
					}
				}
			});
			storeName = (TextView) findViewById(R.id.storeNameTV);
			storeAddress = (TextView) findViewById(R.id.storeAddressTV);
			mTxtCityAndZip = (TextView) findViewById(R.id.cityZipTV);
			storeNumber = (TextView) findViewById(R.id.storePhoneTV);
			storeHours = (TextView) findViewById(R.id.storeHoursTV);
			mLayout = (LinearLayout) findViewById(R.id.linearLayout1);
			mapViewFrame = (FrameLayout) findViewById(R.id.frameLayout1);
			storeName.setTypeface(PrintHelper.tf);
			storeAddress.setTypeface(PrintHelper.tf);
			mTxtCityAndZip.setTypeface(PrintHelper.tf);
			storeNumber.setTypeface(PrintHelper.tf);
			storeHours.setTypeface(PrintHelper.tf);
			
			if (AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
				storeName.setVisibility(View.GONE);
				storeAddress.setVisibility(View.GONE);
				mTxtCityAndZip.setVisibility(View.GONE);
				storeNumber.setVisibility(View.GONE);
				storeHours.setVisibility(View.GONE);
			} else {
				storeName.setVisibility(View.VISIBLE);
				storeAddress.setVisibility(View.VISIBLE);
				mTxtCityAndZip.setVisibility(View.VISIBLE);
				storeNumber.setVisibility(View.VISIBLE);
				storeHours.setVisibility(View.VISIBLE);
				
					mapView = (MapView) findViewById(R.id.mapview);
					if (!Connection.isConnected(NewSettingActivity.this)) {
						mapView.setVisibility(View.GONE);
					}
					mapView.setBuiltInZoomControls(false);
					mapView.setOnTouchListener(new OnTouchListener()
					{
						@Override
						public boolean onTouch(View v, MotionEvent event)
						{
							int action = event.getAction();
							ViewParent vp = mapView.getParent();
							ViewParent vp1 = vp.getParent();
							ViewParent vp2 = vp1.getParent();
							ViewParent vp3 = vp2.getParent();
							switch (action)
							{
							case MotionEvent.ACTION_DOWN:
								vp3.requestDisallowInterceptTouchEvent(true);
								break;
							case MotionEvent.ACTION_UP:
								vp3.getParent().getParent().requestDisallowInterceptTouchEvent(true);
								break;
							case MotionEvent.ACTION_MOVE:
								// Allow ScrollView to intercept touch events.
								vp3.getParent().getParent().requestDisallowInterceptTouchEvent(true);
							}
							mapView.onTouchEvent(event);
							return true;
						}
					});
			}
			
			
			
			if (AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
				changeStore.setVisibility(View.GONE);
			} else {
				changeStore.setVisibility(View.VISIBLE);
			}
			mLayoutIds.add(R.id.rela_storeinfo);
		} 
		
		if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
			String storename = prefs.getString("selectedStoreName", "");
			String address = prefs.getString("selectedStoreAddress", "");
			String phonenumber = prefs.getString("selectedStorePhone", "");
			String hours = prefs.getString("selectedStoreHours", "");
			String cityAndZip = prefs.getString("selectedCity", "") + " " + prefs.getString("selectedPostalCode", "");
			Log.i(TAG, "gggg storename = " + storename + "\n address = " + address + "\n phonenumber = " + 
					phonenumber + "\n hours = " + hours + "\n cityAndZip = " + cityAndZip);
			if (storename.equals(""))
				storeName.setVisibility(View.GONE);
			else
			{
				storeName.setText(storename);
				storeName.setVisibility(View.VISIBLE);
			}
			if (address.equals(""))
				storeAddress.setVisibility(View.GONE);
			else
			{
				storeAddress.setText(address);
				storeAddress.setVisibility(View.VISIBLE);
			}
			if (cityAndZip.equals("") || cityAndZip.trim().length() == 0) {
				mTxtCityAndZip.setVisibility(View.GONE);
			} else {
				mTxtCityAndZip.setText(cityAndZip);
				mTxtCityAndZip.setVisibility(View.VISIBLE);
			} 
			if (phonenumber.equals(""))
				storeNumber.setVisibility(View.GONE);
			else
			{
				storeNumber.setText(phonenumber);
				storeNumber.setVisibility(View.VISIBLE);
			}
			
			if (!hours.equals("") && Connection.isConnected(NewSettingActivity.this)) {
				storeHours.setText(hours);
				storeHours.setVisibility(View.VISIBLE);
			} else {
				storeHours.setVisibility(View.GONE);
			}
			
			Drawable kiosk = getResources().getDrawable(R.drawable.pinpoint2);
			kiosk.setBounds(0, 0, kiosk.getIntrinsicWidth(), kiosk.getIntrinsicHeight());
			try
			{
				if (!Connection.isConnected(NewSettingActivity.this)) {
					mapViewFrame.setVisibility(View.GONE);
					mapView.setVisibility(View.GONE);
					storeName.setVisibility(View.GONE);
					storeAddress.setVisibility(View.GONE);
					mTxtCityAndZip.setVisibility(View.GONE);
					storeNumber.setVisibility(View.GONE);
				}
				else if (storeName.getVisibility() == View.GONE && storeAddress.getVisibility() == View.GONE && storeNumber.getVisibility() == View.GONE)
				{
					mapViewFrame.setVisibility(View.GONE);
					mapView.setVisibility(View.GONE);
				}
				else
				{
					mapViewFrame.setVisibility(View.VISIBLE);
					mapView.setVisibility(View.VISIBLE);
					mapKiosks = new PictureKiosks(kiosk, NewSettingActivity.this, prefs.getString("selectedStoreLatitude", ""), 
							prefs.getString("selectedStoreLongitude", ""), prefs.getString("selectedStoreName", ""),
							prefs.getString("selectedStoreAddress", ""), prefs.getString("selectedStorePhone", ""));
					mapView.getOverlays().clear();
					mapView.getOverlays().add(mapKiosks);
					mapView.setClickable(false);
					mapView.getController().setZoom(15);
					mapView.invalidate();
				}
			}
			catch (Exception ex)
			{
				Log.e(TAG, "Error adding the overlay");
			}
		}
	}
	
	class PictureKiosks extends ItemizedOverlay<OverlayItem>
	{
		private List<OverlayItem> locations = new ArrayList<OverlayItem>();
		private Drawable marker;
		private Context mContext;

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
			populate();
		}

		@Override
		public boolean onTap(int index)
		{
			OverlayItem item = locations.get(index);
			AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
			dlg.setTitle(item.getTitle());
			dlg.setPositiveButton("", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					finish();
					Intent myIntent = new Intent(NewSettingActivity.this, StoreFinder.class);
					Boolean isFromShoppingcart = getIntent().getBooleanExtra(AppConstants.IS_FORM_SHOPPINGCART, false);
					myIntent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, isFromShoppingcart);
					startActivity(myIntent);
				}
			}).setNegativeButton("", new DialogInterface.OnClickListener()
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
	
	// from the link above
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		// Checks whether a hardware keyboard is available
		if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO)
		{
		}
		else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES && currentItem == R.id.radio_cusInfo)
		{
			if ((!mEmailEditText.getText().toString().contains("@") 
					|| !mEmailEditText.getText().toString().contains("."))
					&& !mEmailEditText.getText().toString().equals(""))
			{
				mEmailEditText.setError(getString(R.string.incorrectEmail));
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		} else {
		 if(keyCode == KeyEvent.KEYCODE_BACK){
//				 if (mFirstNameEditText != null && mFirstNameEditText.isFocusable()) {
//					mFirstNameEditText.setFocusable(false);
//				}
//					if (mLastNameEditText != null && mLastNameEditText.isFocusable()) {
//					mLastNameEditText.setFocusable(false);
//				}
//					if (mPhoneEditText != null && mPhoneEditText.isFocusable()) {
//					mPhoneEditText.setFocusable(false);
//				}
//					if (mEmailEditText != null && mEmailEditText.isFocusable()) {
//					mEmailEditText.setFocusable(false);
//				}
//				if (mEditFirstName != null && mEditFirstName.isFocusable()) {
//					mEditFirstName.setFocusable(false);
//				}
//				if (mEditLastName != null && mEditLastName.isFocusable()) {
//					mEditLastName.setFocusable(false);
//				}
//				if (mEditAddressOne != null && mEditAddressOne.isFocusable()) {
//					mEditAddressOne.setFocusable(false);
//				}
//				if (mEditAddressTwo != null && mEditAddressTwo.isFocusable()) {
//					mEditAddressTwo.setFocusable(false);
//				}
//				if (mEditCity != null && mEditCity.isFocusable()) {
//					mEditCity.setFocusable(false);
//				}
//				if (mEditState != null && mEditState.isFocusable()) {
//					mEditState.setFocusable(false);
//				}
//				if (mEditZipcode != null && mEditZipcode.isFocusable()) {
//					mEditZipcode.setFocusable(false);
//				}
//				if (mEditPhone != null && mEditPhone.isFocusable()) {
//					mEditPhone.setFocusable(false);
//				}
//				if (mEditCountryName != null && mEditCountryName.isFocusable()) {
//					mEditCountryName.setFocusable(false);
//				}
		 }
		return super.onKeyDown(keyCode, event);
		}
		return false;
	}
	
	private void launchLayout(int res,int layoutId,View parentView) {
		
		// hide the soft-keyboard if it is shown
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE) ;
		inputMethodManager.hideSoftInputFromWindow(parentView.getWindowToken(), 0);
		
		
		
		if (isHideLeft) {
			mRelaLeft.setVisibility(View.GONE);
		} else {
			mRelaLeft.setVisibility(View.VISIBLE);
		}
		if (!mLayoutIds.contains(layoutId)) {
			for (int i = 0 ; i < ((ViewGroup) parentView).getChildCount() ; i ++) {
				((ViewGroup) parentView).getChildAt(i).setVisibility(View.GONE);
			}
			View view = View.inflate(NewSettingActivity.this, res, null);
			RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			view.setLayoutParams(mLayoutParams);
			((ViewGroup) parentView).addView(view);
			if (cacheMap4View != null && !cacheMap4View.containsKey(layoutId)) {
				cacheMap4View.put(layoutId, view);
			}
		} else {
			for (int i = 0 ; i < ((ViewGroup) parentView).getChildCount() ; i ++) {
				if (((ViewGroup) parentView).getChildAt(i).getId() != layoutId) {
					((ViewGroup) parentView).getChildAt(i).setVisibility(View.GONE);
				} else {
					((ViewGroup) parentView).getChildAt(i).setVisibility(View.VISIBLE);
				}
			}
		}
	}
	
	class TextChangedListener implements TextWatcher {
		
		private String key = "";
		
		public TextChangedListener(String key){
			this.key = key;
		}

		@Override
		public void afterTextChanged(Editable s) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			attr.put(key, YES);
		}
		
	}
	
	private void findViews() {
		mGroup = (RadioGroup) findViewById(R.id.radiogroup);
		mScrollContainer = (ScrollView) findViewById(R.id.scroll_container);
		mScrollContainer.setVisibility(View.GONE);
		mRelaLeft = (RelativeLayout) findViewById(R.id.layout_left);
		mRelaRight = (RelativeLayout) findViewById(R.id.layout_right);
		mRadioCusInfo = (RadioButton) findViewById(R.id.radio_cusInfo);
		mRadioPrintSize = (RadioButton) findViewById(R.id.radio_printSize);
		String textdps = getResources().getString(R.string.defaultprintsize);
		int length = textdps.length();
		mRadioPrintSize.setText(textdps.substring(0, length - 1));
		mRadioAddress = (RadioButton) findViewById(R.id.radio_address);
		mImageViewAddressLine = (ImageView) findViewById(R.id.image_address_line);
		mRadioStoreInfo = (RadioButton) findViewById(R.id.radio_storeInfo);
		String textsti = getResources().getString(R.string.storeInformation);
		int length2 = textsti.length();
		mRadioStoreInfo.setText(textsti.substring(0, length2 - 1));
		mRadioOrderHistory = (RadioButton) findViewById(R.id.radio_orderHistory);
		mRadioLegal = (RadioButton) findViewById(R.id.radio_legal);
		mRadioAbout = (RadioButton) findViewById(R.id.radio_about);
		mBtnBack = (Button) findViewById(R.id.backButton);
		mBtnNext = (Button) findViewById(R.id.nextButton);
		mTxtTitle = (TextView) findViewById(R.id.headerBarText);
		

		
		
		mGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){

			@TargetApi(16)
			@Override
			public void onGlobalLayout() {
				int groupHeight = mGroup.getHeight() ;
				mEachItemHeight = groupHeight/7 ;
				LinearLayout.LayoutParams mLayoutParamsItem = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						mEachItemHeight);
				mRadioCusInfo.setLayoutParams(mLayoutParamsItem) ;
				mRadioPrintSize.setLayoutParams(mLayoutParamsItem) ;
				mRadioAddress.setLayoutParams(mLayoutParamsItem) ;
				mRadioStoreInfo.setLayoutParams(mLayoutParamsItem) ;
				mRadioOrderHistory.setLayoutParams(mLayoutParamsItem) ;
				mRadioLegal.setLayoutParams(mLayoutParamsItem) ;
				mRadioAbout.setLayoutParams(mLayoutParamsItem) ;
				Class<?> c =mGroup.getViewTreeObserver().getClass() ;
				try {
					Method method = c.getDeclaredMethod("removeOnGlobalLayoutListener", OnGlobalLayoutListener.class) ;
					method.invoke(mGroup.getViewTreeObserver(), this);
					
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}) ;
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private void refreshPopStatus() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			Log.i(TAG, "exception situation is executed!");
			mPopupWindow.dismiss();
			alreadyPop = false;
		}
	}
	
	private void popEditWindow(String[] editItems) {
		if (mPopupWindow != null) {
			mPopupWindow = null;
		}
		if (editItems != null && editItems.length > 0) {
			PopEditAdapter popEditAdapter = new PopEditAdapter(editItems);
			ListView mPopListView = new ListView(NewSettingActivity.this);
			mPopListView.setBackgroundColor(Color.TRANSPARENT);
			mPopListView.setDivider(null);
			mPopListView.setFadingEdgeLength(0);
			mPopListView.setAdapter(popEditAdapter);
			mPopupWindow = new PopupWindow(mPopListView,popWidth,popHeight);
			mPopupWindow.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.windowdropdown3));
			mEditState.getLocationOnScreen(locationState);
			System.out.println("pppppppp size t l b r" + mEditState.getTop() + " , " + mEditState.getLeft() + " , "
					+ mEditState.getBottom() + " , " + mEditState.getRight() + " , location " + locationState[0] + " , " +
					locationState[1]);
			mPopupWindow.showAtLocation(mScrollView, Gravity.LEFT|Gravity.TOP, popWidth*3 , locationState[1] - popHeight*2/5);
		 }
		}
	class EditViewHolder {
		RelativeLayout mRelaEdit;
		TextView mTextView;
	}
	class PopEditAdapter extends BaseAdapter {

//		ArrayList<String> editItems;
		String[] editArray;
		ViewHolder holder;
		public PopEditAdapter(String[] editArray ) {
			this.editArray = editArray;
}
		@Override
		public int getCount() {
			return editArray.length;
		}
		@Override
		public Object getItem(int position) {
			return position;
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final EditViewHolder mEditViewHolder;
			if (convertView == null) {
				mEditViewHolder = new EditViewHolder();
				convertView = View.inflate(NewSettingActivity.this, R.layout.ship_state_list, null);
				mEditViewHolder.mTextView = (TextView) convertView.findViewById(R.id.txt_deplay_specification);
				mEditViewHolder.mRelaEdit = (RelativeLayout) convertView.findViewById(R.id.rela_edit);
				RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
						RelativeLayout.LayoutParams.FILL_PARENT);
				mLayoutParams.setMargins(0, 0, 10, 0);
				mEditViewHolder.mRelaEdit.setLayoutParams(mLayoutParams);
				convertView.setTag(mEditViewHolder);
			} else {
				mEditViewHolder = (EditViewHolder) convertView.getTag();
			}
			
			if (editArray != null && editArray.length > 0) {
				final String displayItem = editArray[position];
				mEditViewHolder.mTextView.setText(displayItem);
				mEditViewHolder.mRelaEdit.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditState.setText(displayItem);
						refreshPopStatus();
					}
				});
			}
			return convertView;
		}
	}
}
