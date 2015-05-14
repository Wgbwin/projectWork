package com.kodak.kodak_kioskconnect_n2r;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.AppConstants;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.shareapi.AccessTokenResponse;
import com.kodak.shareapi.ClientTokenResponse;
import com.kodak.shareapi.TokenGetter;
@Deprecated
public class SettingsActivity extends MapActivity
{
	protected String TAG = this.getClass().getSimpleName();
	protected String mFirstName = "";
	protected String mLastName = "";
	protected String mAddress = "";
	protected String mPhone = "";
	protected EditText mFirstNameEditText = null;
	protected EditText mLastNameEditText = null;
	protected EditText mPhoneEditText = null;
	protected EditText mEmailEditText = null;
	
	protected ImageView firstNameIV = null;
	protected ImageView lastNameIV = null;
	protected ImageView phoneIV = null;
	protected ImageView emailIV = null;
	
	protected Button printSize;
	protected Button nextButton = null;
	protected Button back;
	protected TextView title;
	protected Button changeStore;
	protected TextView storeName;
	protected TextView storeAddress;
	protected TextView mTxtCityAndZip;
	protected TextView storeNumber;
	protected TextView storeHours;
	protected TextView allowCookiesTV;
	protected CheckBox allowCookiesCB;
	protected MapView mapView;
	protected TextView defaultPrintSizeTV;
	protected TextView contactInformationTV;
	protected TextView nameTV;
	protected TextView phoneTV;
	protected TextView requiredTV;
	protected TextView emailTV;
	protected LinearLayout mStoreLayout;
	private TextView selectedTextView;
	protected ScrollView scrollVW;
	protected PictureKiosks mapKiosks;
	protected Button info;
	protected TextView totalNumSelectedTV;
	protected Button findoutmoreButton;
	protected Button readPolicyBT;
	protected CheckBox allowCookiesCB1;
	protected CheckBox sendOnlyTaggedCB;
	protected TextView sendOnlyTaggedTV;
	Drawable requiredDrawable;
	MyCustomAdapter adapter;
	PreviousOrderAdapter orderAdapter;
	FrameLayout mapViewFrame;
	TextView previousOrdersTV;
	TextView orderDateTV;
	TextView orderIDTV;
	Button details;
	ArrayList<Order> previousOrders;
	ImageSelectionDatabase mImageSelectionDatabase;
	LinearLayout scrollView;
	LinearLayout previousOrderLL;
	boolean printSizeChanged = false;
	SharedPreferences prefs;
	
	private TextView tvCountry;
	private Button btCountry;
	private String countryName = "";

	private EditText editTextUploadEmail, editTextUploadPassword;
	private CheckBox checkBoxAutoUpload;
	private View uploadShareLayout;
	private InfoDialog.InfoDialogBuilder connectBuilder;
	private TextView copyVersion;
	
	public static String SHOW_CHANGE_COUNTRY = "showChangeCountry";
	public static final String ENABLE_ALLOW_COOKIES = "enable_allow_cookies";
	private boolean isFromSetting = true;
	private  String FORM_SETTING = "isFromSetting";
	private boolean isCountryChanged = false;
	
	public class MyCustomAdapter extends ArrayAdapter<String>
	{
		
		public MyCustomAdapter(Context context, int textViewResourceId, String[] productSizes)
		{
			super(context, textViewResourceId, productSizes);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
			return getCustomView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			return getCustomView(position, convertView, parent);
		}

		public View getCustomView(int position, View convertView, ViewGroup parent)
		{
			// return super.getView(position, convertView, parent);
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.row, parent, false);
			TextView label = (TextView) row.findViewById(R.id.productSizeTextView);
			if (label != null)
			{
				Log.i(TAG, "PrintHelper.products.size() = " + PrintHelper.products.size());
				if (PrintHelper.products != null && PrintHelper.products.size() > position)
				{
					label.setText(PrintHelper.products.get(position).getShortName());
				}
				else
				{
					label.setText("");
				}
			}
			return row;
		}
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		selectedTextView = (TextView) findViewById(R.id.totalSelectedTextView);
		back = (Button) findViewById(R.id.backButton);
		title = (TextView) findViewById(R.id.headerBarText);
		totalNumSelectedTV = (TextView) findViewById(R.id.totalSelectedTextView);
		findoutmoreButton = (Button) findViewById(R.id.findoutmorebutton);
		printSize = (Button) findViewById(R.id.printSizeSpinner);
		nextButton = (Button) findViewById(R.id.nextButton);
		changeStore = (Button) findViewById(R.id.changeStoreButton);
		storeName = (TextView) findViewById(R.id.storeNameTV);
		storeAddress = (TextView) findViewById(R.id.storeAddressTV);
		mTxtCityAndZip = (TextView) findViewById(R.id.cityZipTV);
		storeNumber = (TextView) findViewById(R.id.storePhoneTV);
		storeHours = (TextView) findViewById(R.id.storeHoursTV);
		mStoreLayout = (LinearLayout) findViewById(R.id.linearLayout1);
		allowCookiesCB = (CheckBox) findViewById(R.id.analyticsCB);
		scrollVW = (ScrollView) findViewById(R.id.scrollView1);
		defaultPrintSizeTV = (TextView) findViewById(R.id.defaultPrintSizeTV);
		contactInformationTV = (TextView) findViewById(R.id.contactInfoTV);
		nameTV = (TextView) findViewById(R.id.nameTV);
		phoneTV = (TextView) findViewById(R.id.phoneTV);
		requiredTV = (TextView) findViewById(R.id.requiredTV);
		emailTV = (TextView) findViewById(R.id.emailTV);
		allowCookiesTV = (TextView) findViewById(R.id.analyticPermissionTV);
		readPolicyBT = (Button) findViewById(R.id.licenseButton);
		allowCookiesCB1 = (CheckBox) findViewById(R.id.allowCookiesCB);
		copyVersion = (TextView) findViewById(R.id.versionCopyrightTextView);
		tvCountry = (TextView) findViewById(R.id.changeCountryTV);
		btCountry = (Button) findViewById(R.id.changeCountryBT);
		String versionName = "";
		try
		{
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = getString(R.string.mainMenuVersion) + " " + packageInfo.versionName + " " + getString(R.string.mainMenuCopyright);
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		copyVersion.setTypeface(PrintHelper.tf);
		copyVersion.setText(versionName);
		copyVersion.setVisibility(View.VISIBLE);
		
		mFirstNameEditText = (EditText) findViewById(R.id.firstNameEditText);
		mLastNameEditText = (EditText) findViewById(R.id.lastNameEditText);
		mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
		mEmailEditText = (EditText) findViewById(R.id.emailEditText);
		
		
		firstNameIV = (ImageView)findViewById(R.id.firstNameBullet);
		lastNameIV = (ImageView)findViewById(R.id.lastNameBullet);
		phoneIV = (ImageView)findViewById(R.id.phoneBullet);
		emailIV = (ImageView)findViewById(R.id.emailBullet);
		
		firstNameIV.setVisibility(View.INVISIBLE);
		lastNameIV.setVisibility(View.INVISIBLE);
		phoneIV.setVisibility(View.INVISIBLE);
		emailIV.setVisibility(View.INVISIBLE);
		
		mapViewFrame = (FrameLayout) findViewById(R.id.frameLayout1);
		previousOrdersTV = (TextView) findViewById(R.id.previousOrdersLabel);
		orderDateTV = (TextView) findViewById(R.id.orderDateTV);
		orderIDTV = (TextView) findViewById(R.id.orderIDTV);
		
		scrollView = (LinearLayout) findViewById(R.id.scrollViewLinearLayout);
		previousOrderLL = (LinearLayout) scrollView.findViewById(R.id.previousOrderLL);
		sendOnlyTaggedCB = (CheckBox) findViewById(R.id.sendOnlyTaggedCB);
		sendOnlyTaggedTV = (TextView) findViewById(R.id.sendOnlyTaggedTV);
		sendOnlyTaggedTV.setTypeface(PrintHelper.tf);
		requiredDrawable = getResources().getDrawable(R.drawable.bullet);
		requiredDrawable.setBounds(new Rect(0, 0, 20, 20));
		requiredTV.setCompoundDrawables(requiredDrawable, null, null, null);
		nextButton.setText(R.string.done);
		back.setVisibility(android.view.View.INVISIBLE);
		title.setText(R.string.setup);
		defaultPrintSizeTV.setTypeface(PrintHelper.tf);
		contactInformationTV.setTypeface(PrintHelper.tf);
		nameTV.setTypeface(PrintHelper.tf);
		phoneTV.setTypeface(PrintHelper.tf);
		requiredTV.setTypeface(PrintHelper.tf);
		emailTV.setTypeface(PrintHelper.tf);
		changeStore.setTypeface(PrintHelper.tf);
		storeName.setTypeface(PrintHelper.tf);
		storeAddress.setTypeface(PrintHelper.tf);
		mTxtCityAndZip.setTypeface(PrintHelper.tf);
		storeNumber.setTypeface(PrintHelper.tf);
		storeHours.setTypeface(PrintHelper.tf);
//		storeHours.setVisibility(View.GONE);
		allowCookiesCB.setTypeface(PrintHelper.tf);
		allowCookiesTV.setTypeface(PrintHelper.tf);
		title.setTypeface(PrintHelper.tf);
		nextButton.setTypeface(PrintHelper.tf);
		allowCookiesCB.setTypeface(PrintHelper.tf);
		allowCookiesCB1.setTypeface(PrintHelper.tf);
		//previousOrdersTV.setTypeface(PrintHelper.tfb);
		totalNumSelectedTV.setVisibility(View.GONE);
		tvCountry.setTypeface(PrintHelper.tf);
		btCountry.setTypeface(PrintHelper.tf);
		//allowCookiesTV.setText(getString(R.string.privacyTitle));
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!PrintHelper.wififlow && getApplicationContext().getPackageName().contains("wmc")){
			uploadShareLayout = findViewById(R.id.automaticUpload2AlbumsSettingsLayout);
			uploadShareLayout.setVisibility(View.VISIBLE);
			emailTV.setText(getString(R.string.email_hint));
			
			editTextUploadEmail = (EditText) findViewById(R.id.autoUploadEmailEditText);
			editTextUploadPassword = (EditText) findViewById(R.id.autoUploadPasswordEditText);
			checkBoxAutoUpload = (CheckBox) findViewById(R.id.automaticUploadSettingsCB);
			checkBoxAutoUpload.setChecked(prefs.getBoolean(PrintHelper.IS_AUTO_UPLOAD2ALBUMS, false));
			checkBoxAutoUpload.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Editor editor = prefs.edit();
					editor.putBoolean(PrintHelper.IS_AUTO_UPLOAD2ALBUMS, isChecked);
					editor.commit();
				}
			});
		}
		
		if(PrintHelper.countries!=null && PrintHelper.countries.size()!=0){
			tvCountry.setVisibility(View.VISIBLE);
			btCountry.setVisibility(View.VISIBLE);
		}else{
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try{
						int count = 0;
						String result = "";
						PrintMakerWebService service = new PrintMakerWebService(SettingsActivity.this, "");
						while(count<5 && result.equals("")){
							result = service.getCountries();
							count ++;
						}
					} catch(Exception e){
						e.printStackTrace();
					} 
				}
			}).start();
		}
		
		if (PrintHelper.wififlow)
		{
			defaultPrintSizeTV.setVisibility(View.GONE);
			contactInformationTV.setVisibility(View.GONE);
			nameTV.setVisibility(View.GONE);
			phoneTV.setVisibility(View.GONE);
			requiredTV.setVisibility(View.GONE);
			emailTV.setVisibility(View.GONE);
			changeStore.setVisibility(View.GONE);
			storeName.setVisibility(View.GONE);
			storeAddress.setVisibility(View.GONE);
			mTxtCityAndZip.setVisibility(View.GONE);
			storeNumber.setVisibility(View.GONE);
			storeHours.setVisibility(View.GONE);
			mFirstNameEditText.setVisibility(View.GONE);
			mLastNameEditText.setVisibility(View.GONE);
			mEmailEditText.setVisibility(View.GONE);
			mPhoneEditText.setVisibility(View.GONE);
			printSize.setVisibility(View.GONE);
			selectedTextView.setVisibility(View.GONE);
		}
		else
		{
			defaultPrintSizeTV.setVisibility(View.VISIBLE);
			contactInformationTV.setVisibility(View.VISIBLE);
			nameTV.setVisibility(View.VISIBLE);
			phoneTV.setVisibility(View.VISIBLE);
			requiredTV.setVisibility(View.VISIBLE);
			emailTV.setVisibility(View.VISIBLE);
			changeStore.setVisibility(View.VISIBLE);
			storeName.setVisibility(View.VISIBLE);
			storeAddress.setVisibility(View.VISIBLE);
			mTxtCityAndZip.setVisibility(View.VISIBLE);
			storeNumber.setVisibility(View.VISIBLE);
			storeHours.setVisibility(View.VISIBLE);
			mFirstNameEditText.setVisibility(View.VISIBLE);
			mLastNameEditText.setVisibility(View.VISIBLE);
			mEmailEditText.setVisibility(View.VISIBLE);
			mPhoneEditText.setVisibility(View.VISIBLE);
			printSize.setVisibility(View.VISIBLE);
			selectedTextView.setVisibility(View.VISIBLE);
			mapView = (MapView) findViewById(R.id.mapview);
			if (!Connection.isConnected(SettingsActivity.this)) {
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
						// Disallow ScrollView to intercept touch events.
						vp3.requestDisallowInterceptTouchEvent(true);
						break;
					case MotionEvent.ACTION_UP:
						// Allow ScrollView to intercept touch events.
						// Navigation stuff
						/*
						 * SharedPreferences prefs =
						 * PreferenceManager.getDefaultSharedPreferences
						 * (SettingsActivity.this); Intent i = new
						 * Intent(Intent.ACTION_VIEW,
						 * Uri.parse("google.navigation:q="
						 * +prefs.getString("selectedStoreLatitude",
						 * "")+"+"+prefs.getString("selectedStoreLongitude",
						 * ""))); SettingsActivity.this.startActivity(i);
						 */
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
//			printSize.setOnClickListener(new OnClickListener()
//			{
//				@Override
//				public void onClick(View v)
//				{
//					final Dialog dialog = new Dialog(SettingsActivity.this, R.style.DropDownDialog);
//					dialog.setContentView(R.layout.custom_dialog);
//					dialog.setCancelable(true);
//					TextView titleTV = (TextView) dialog.findViewById(R.id.titleTV);
//					titleTV.setText(getString(R.string.printSize));
//					ListView ssidLV = (ListView) dialog.findViewById(R.id.ssidLV);
//					ssidLV.setAdapter(adapter);
//					ssidLV.setOnItemClickListener(new OnItemClickListener()
//					{
//						@Override
//						public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3)
//						{
//							printSizeChanged = true;
//							String item = ((TextView) view.findViewById(R.id.productSizeTextView)).getText().toString();
//							PrintHelper.defaultPrintSize = item;
//							PrintHelper.defaultPrintSizeIndex = position;
//							printSize.setText(item);
//							dialog.dismiss();
//						}
//					});
//					dialog.show();
//				}
//			});
//			if (PrintHelper.products == null)
//			{
//				PrintHelper.products = new ArrayList<PrintProduct>();
//			}
//			if (PrintHelper.products.size() <= 0)
//			{
//				try
//				{
//					Thread thrd = new Thread()
//					{
//						@Override
//						public void run()
//						{
//							PrintMakerWebService service = new PrintMakerWebService(SettingsActivity.this, "");
//							service.getPrintProducts(false,"");
//							service.GetRequiredContactInformation(SettingsActivity.this);
//						}
//					};
//					thrd.start();
//				}
//				catch (Exception ex)
//				{
//					Log.e(TAG, "Error getting print prices");
//				}
//			}
//			if (prefs.getString("defaultSize", "").equals("") && PrintHelper.products != null && PrintHelper.products.size() > 0)
//			{
//				SharedPreferences.Editor editor = prefs.edit();
//				editor.putString("defaultSize", PrintHelper.products.get(0).getShortName().toString());
//				editor.commit();
//			}
//			if (!Connection.isConnected(SettingsActivity.this)) {
//				printSize.setText("");
//				printSize.setEnabled(false);
//			} else {
//				printSize.setText(prefs.getString("defaultSize", ""));
//			}
//			int iSizeCount = 0;
//			for(PrintProduct product : PrintHelper.products){
//				if(!product.getId().contains(PrintHelper.PhotoBook)){
//					iSizeCount++;
//				}
//			}
//			String[] sizes = new String[iSizeCount];
//			
//				for (int i = 0; i < iSizeCount; i++)
//				{
//					sizes[i] = PrintHelper.products.get(i).getName();
//				}
//				if (Connection.isConnected(SettingsActivity.this)) {
//					myPrintAdapter = new MyPrintAdapter(PrintHelper.products);
//				} else {
//					ArrayList<PrintProduct> mArrayList = new ArrayList<PrintProduct>();
//					myPrintAdapter = new MyPrintAdapter(mArrayList);
//				}
//			
//			if (!Connection.isConnected(SettingsActivity.this)) {
//				printSize.setText("");
//			} else {
//				printSize.setText(prefs.getString("defaultSize", ""));
//			}
//			printSize.setOnTouchListener(new OnTouchListener()
//			{
//				@Override
//				public boolean onTouch(View v, MotionEvent event)
//				{
//					return false;
//				}
//			});
			changeStore.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (!Connection.isConnected(SettingsActivity.this))
					{
						connectBuilder = new InfoDialog.InfoDialogBuilder(SettingsActivity.this);
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
						/*if (mFirstNameEditText.isFocusable()) {
							mFirstNameEditText.setFocusable(false);
						}
						if (mLastNameEditText.isFocusable()) {
							mLastNameEditText.setFocusable(false);
						}
						if (mPhoneEditText.isFocusable()) {
							mPhoneEditText.setFocusable(false);
						}
						if (mEmailEditText.isFocusable()) {
							mEmailEditText.setFocusable(false);
						}*/
						Intent intent = new Intent(SettingsActivity.this, StoreFinder.class);
						startActivity(intent);
					}
				}
			});
			mEmailEditText.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				@Override
				public void onFocusChange(View v, boolean hasFocus)
				{
					if (!hasFocus)
					{
						if ((!mEmailEditText.getText().toString().contains("@") || !mEmailEditText.getText().toString().contains(".")) && !mEmailEditText.getText().toString().equals(""))
						{
							mEmailEditText.setError(getString(R.string.incorrectEmail));
							/*
							 * InfoDialog.InfoDialogBuilder builder = new
							 * InfoDialog.InfoDialogBuilder(
							 * SettingsActivity.this);
							 * builder.setTitle("Email not in correct format");
							 * builder.setMessage("");
							 * builder.setPositiveButton("Ok", new
							 * DialogInterface.OnClickListener() {
							 * @Override public void onClick(DialogInterface
							 * dialog, int which) { dialog.dismiss();
							 * mEmailEditText.requestFocus(); } });
							 * builder.setNegativeButton("", new
							 * DialogInterface.OnClickListener() {
							 * @Override public void onClick(DialogInterface
							 * dialog, int which) { dialog.dismiss(); finish();
							 * } }); builder.create().show();
							 */
						}
					}
				}
			});
			
			mFirstNameEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
						Editor editor = prefs.edit();
						if ( mFirstNameEditText.getText().toString().trim().length() != 0) {
							editor.putString("firstName", mFirstNameEditText.getText().toString().trim());
						} else {
							editor.putString("firstName","");
						}
						editor.commit();
					}
					
				}
			});
//			String firstName = prefs.getString("firstName", ""); //TODO
//			String lastName = prefs.getString("lastName", "");
//			String phone = prefs.getString("phone", "");
//			String email = prefs.getString("email", "");
			
			mLastNameEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
						Editor editor = prefs.edit();
						if (mLastNameEditText.getText().toString().trim().length() != 0) {
							editor.putString("lastName", mLastNameEditText.getText().toString().trim());
						} else {
							editor.putString("lastName", "");
						}
						editor.commit();
					}
				}
			});
			
			mPhoneEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
						Editor editor = prefs.edit();
						if (mPhoneEditText.getText().toString().trim().length() != 0) {
							editor.putString("phone", mPhoneEditText.getText().toString().trim());
						} else {
							editor.putString("phone", "");
						}
						editor.commit();
					}
				}
			});
			
			mEmailEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
						Editor editor = prefs.edit();
						if (mEmailEditText.getText().toString().trim().length() != 0) {
							editor.putString("email", mEmailEditText.getText().toString().trim());
						} else {
							editor.putString("email", "");
						}
						editor.commit();
					}
				}
			});
			
			if(editTextUploadEmail != null)
				editTextUploadEmail.setOnFocusChangeListener(new OnFocusChangeListener()
				{
					@Override
					public void onFocusChange(View v, boolean hasFocus)
					{
						if (!hasFocus)
						{
							if ((!editTextUploadEmail.getText().toString().contains("@") || !editTextUploadEmail.getText().toString().contains(".")) && !editTextUploadEmail.getText().toString().equals(""))
							{
								editTextUploadEmail.setError(getString(R.string.incorrectEmail));
							}
						}
					}
				});
			
			mStoreLayout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String hours = prefs.getString("selectedStoreHours", "");
					if (!hours.equals("")) {
						if (storeHours.getVisibility() == View.GONE) {
							storeHours.setVisibility(View.VISIBLE);
						} else {
							storeHours.setVisibility(View.GONE);
						}
					}
				}
			});
		}
		if (nextButton == null)
		{
			Log.e(TAG, "mConfirmButton is null");
		}
		Bundle b = getIntent().getExtras();
		if(b!=null){
			isCountryChanged = b.getBoolean(CountrySelectionActivity.COUNTRY_CHANGED);
		}
		nextButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				try
				{
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean("sendOnlyTaggedImages", sendOnlyTaggedCB.isChecked());
					editor.commit();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				if (prefs.getBoolean("analytics", false))
				{
					try
					{
						PrintHelper.mTracker.trackEvent("Settings", "*Default_Print_Size", printSize.getText().toString(), 0);
						PrintHelper.mTracker.dispatch();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				// Make sure we set the error if the phone ignored our
				// configuration change
				if ((!mEmailEditText.getText().toString().contains("@") || !mEmailEditText.getText().toString().contains(".")) && !mEmailEditText.getText().toString().equals(""))
				{
					mEmailEditText.setError(getString(R.string.incorrectEmail));
				}
				
				if (editTextUploadEmail != null && (!editTextUploadEmail.getText().toString().contains("@") || !editTextUploadEmail.getText().toString().contains(".")) && !editTextUploadEmail.getText().toString().equals(""))
				{
					editTextUploadEmail.setError(getString(R.string.incorrectEmail));
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
				
				if (mEmailEditText != null && mEmailEditText.getError() != null && mEmailEditText.getError().toString().equals(getString(R.string.incorrectEmail)))
				{
					mEmailEditText.requestFocus();
				}
				else
				{
					if(!PrintHelper.wififlow && getApplicationContext().getPackageName().contains("wmc")){
						
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
						Editor editor = prefs.edit();
						if (!PrintHelper.wififlow)
						{
							if (editTextUploadEmail != null && editTextUploadEmail.getText().toString() != null)
								editor.putString(PrintHelper.SHARE_EMAIL_FLAG, editTextUploadEmail.getText().toString());
							if (editTextUploadPassword != null && editTextUploadPassword.getText().toString() != null)
								editor.putString(PrintHelper.SHARE_PASSWORD_FLAG, editTextUploadPassword.getText().toString());
						}
						editor.commit();
						long now = new Date().getTime()/1000;
						long expire = Long.parseLong(PrintHelper.getAccessTokenResponse(getApplicationContext()).expire_in);
						long pass = now - PrintHelper.getAccessTokenResponse(getApplicationContext()).getAccessTokenTime;
						Log.e(TAG, "AccessToken expire in: " + expire + "; Time have pass: " + pass);
						if(!prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "").equals("") && !prefs.getString(PrintHelper.SHARE_PASSWORD_FLAG, "").equals("") && ((pass + 60) > expire))
							new Thread(signInRunnable).start();
						else
							finish();
					}else{
						String oldFirstName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("firstName", "");
						String newFirstName = mFirstNameEditText.getText().toString();
						if(!oldFirstName.equals(newFirstName) && (newFirstName.equals("RSS_Staging") || newFirstName.equals("RSS_Production") || newFirstName.equals("RSS_Development"))){
							Log.e(TAG, oldFirstName + " --> " + newFirstName);
							Intent intent = new Intent(SettingsActivity.this, MainMenu.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							PrintHelper.products = new ArrayList<PrintProduct>();
						}else{
							if (isCountryChanged){
								Intent intent = new Intent(SettingsActivity.this, MainMenu.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								PrintHelper.products = new ArrayList<PrintProduct>();
							}else{
								finish();
							}
						}							
					}
					Editor editor = prefs.edit();
					if (allowCookiesCB != null)
						editor.putBoolean(AppConstants.KEY_LOCALYTICS, allowCookiesCB.isChecked());
					if (allowCookiesCB1 != null)
						editor.putBoolean(AppConstants.KEY_LOCALYTICS, allowCookiesCB1.isChecked());
					if (!PrintHelper.wififlow)
					{
						if (mFirstNameEditText.getText().toString() != null)// &&
																			// !mFirstNameEditText.getText().toString().equals(""))
							editor.putString("firstName", mFirstNameEditText.getText().toString());
						if (mLastNameEditText.getText().toString() != null)// &&
																			// !mLastNameEditText.getText().toString().equals(""))
							editor.putString("lastName", mLastNameEditText.getText().toString());
						if (mPhoneEditText.getText().toString() != null)// &&
																		// !mPhoneEditText.getText().toString().equals(""))
							editor.putString("phone", mPhoneEditText.getText().toString());
						if (mEmailEditText.getText().toString() != null && mEmailEditText.getText().toString().trim().length() != 0)// &&
																		// !mEmailEditText.getText().toString().equals(""))
							editor.putString("email", mEmailEditText.getText().toString());
						if (printSize != null && printSize.length() > 0)
							editor.putString("defaultSize", printSize.getText().toString());
						
						if (editTextUploadEmail != null && editTextUploadEmail.getText().toString() != null)
							editor.putString(PrintHelper.SHARE_EMAIL_FLAG, editTextUploadEmail.getText().toString());
						if (editTextUploadPassword != null && editTextUploadPassword.getText().toString() != null)
							editor.putString(PrintHelper.SHARE_PASSWORD_FLAG, editTextUploadPassword.getText().toString());
					}
					editor.commit();
				}
				if(!countryName.equals("")){
					if(PrintHelper.countries!=null){
						Iterator<Entry<String, String>> iter = PrintHelper.countries.entrySet().iterator();
						while(iter.hasNext()){
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
						if(prefs.getString(MainMenu.CurrentlyCountryCode, "").equalsIgnoreCase("de")){
							prefs.edit().putBoolean(AppConstants.KEY_LOCALYTICS, false);
							prefs.edit().putBoolean(SettingsActivity.ENABLE_ALLOW_COOKIES, false).commit();
						} else {
							prefs.edit().putBoolean(SettingsActivity.ENABLE_ALLOW_COOKIES, true).commit();
						}
					}
				}
			}
		});
		
		if (findoutmoreButton == null)
		{
			Log.e(TAG, "findoutmoreButton == null");
		}
		else
		{
			findoutmoreButton.setClickable(true);
			findoutmoreButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (!Connection.isConnected(SettingsActivity.this))
					{
						connectBuilder = new InfoDialog.InfoDialogBuilder(SettingsActivity.this);
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
						Intent myIntent = new Intent(SettingsActivity.this, HelpActivity.class);
						myIntent.putExtra("privacy", true);
						startActivity(myIntent);
					}
				}
			});
		}
		btCountry.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this, CountrySelectionActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(FORM_SETTING, isFromSetting);
				intent.putExtras(b);
				startActivity(intent);
			}
		});
		
		storeAddress.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + storeAddress.getText().toString()));
				startActivity(intent);
			}
		});
		
		readPolicyBT.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!Connection.isConnected(SettingsActivity.this)) {
					connectBuilder = new InfoDialog.InfoDialogBuilder(SettingsActivity.this);
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
				}  else {
					Intent myIntent = new Intent(SettingsActivity.this, HelpActivity.class);
					myIntent.putExtra("eula", true);
					startActivity(myIntent);
				}
			}
		});
	}
	
	public Handler previousOrderHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (previousOrders.size() == 0) {
				// previousOrdersLV.setVisibility(View.GONE);
				previousOrdersTV.setVisibility(View.VISIBLE);
				orderDateTV.setVisibility(View.VISIBLE);
				orderIDTV.setVisibility(View.VISIBLE);
			} else {
				previousOrdersTV.setVisibility(View.VISIBLE);
				orderDateTV.setVisibility(View.VISIBLE);
				orderIDTV.setVisibility(View.VISIBLE);
				PrintHelper.sentOrders = previousOrders; //add by song
				View row = null;
				if (Connection.isConnected(SettingsActivity.this)) {
					for (int i = 0; i < previousOrders.size(); i++)
					{
						final TextView orderID;
						final TextView orderTime;
						LayoutInflater inflater = getLayoutInflater();
						row = inflater.inflate(R.layout.orderitem, null);
						orderID = (TextView) row.findViewById(R.id.orderIDTV);
						orderTime = (TextView) row.findViewById(R.id.orderTimeTV);
						details = (Button) row.findViewById(R.id.detailsButton);
						orderID.setText(previousOrders.get(i).orderID);
						orderTime.setText(previousOrders.get(i).orderTime);
						orderID.setOnClickListener(new OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								Intent myIntent = new Intent(SettingsActivity.this, OrderSummaryActivity.class);
								myIntent.putExtra(FORM_SETTING, isFromSetting);
								myIntent.putExtra("details", true);
								myIntent.putExtra("orderid", orderID.getText());
								PrintHelper.orderID = orderID.getText().toString();
								PrintHelper.orderTime = orderTime.getText().toString();
								//mImageSelectionDatabase.populateOrderInfo(orderID.getText().toString());
								startActivity(myIntent);
								// ViewGroup vg = (ViewGroup) v.getParent();
								// vg.removeAllViews();
							}
						});
						details.setOnClickListener(new OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								Intent myIntent = new Intent(SettingsActivity.this, OrderSummaryActivity.class);
								myIntent.putExtra(FORM_SETTING, isFromSetting);
								myIntent.putExtra("details", true);
								myIntent.putExtra("orderid", orderID.getText());
								PrintHelper.orderID = orderID.getText().toString();
								PrintHelper.orderTime = orderTime.getText().toString();
								//mImageSelectionDatabase.populateOrderInfo(orderID.getText().toString());
								startActivity(myIntent);
								// ViewGroup vg = (ViewGroup) v.getParent();
								// vg.removeAllViews();
							}
						});
						previousOrderLL.addView(row);
					}
				}
				// orderAdapter = new
				// PreviousOrderAdapter(SettingsActivity.this,R.layout.orderitem,previousOrders);
				// previousOrdersLV.setVisibility(View.VISIBLE);
				// previousOrdersTV.setVisibility(View.VISIBLE);
				// previousOrdersLV.setAdapter(orderAdapter);
			}
		}
	};

	// from the link above
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		// Checks whether a hardware keyboard is available
		if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO)
		{
		}
		else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
		{
			if ((!mEmailEditText.getText().toString().contains("@") || !mEmailEditText.getText().toString().contains(".")) && !mEmailEditText.getText().toString().equals(""))
			{
				mEmailEditText.setError(getString(R.string.incorrectEmail));
			}
			
			if (editTextUploadEmail != null && (!editTextUploadEmail.getText().toString().contains("@") || !editTextUploadEmail.getText().toString().contains(".")) && !editTextUploadEmail.getText().toString().equals(""))
			{
				editTextUploadEmail.setError(getString(R.string.incorrectEmail));
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
					Intent myIntent = new Intent(SettingsActivity.this, StoreFinder.class);
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if(keyCode == KeyEvent.KEYCODE_BACK){
			 if (mFirstNameEditText.isFocusable()) {
					mFirstNameEditText.setFocusable(false);
				}
				if (mLastNameEditText.isFocusable()) {
					mLastNameEditText.setFocusable(false);
				}
				if (mPhoneEditText.isFocusable()) {
					mPhoneEditText.setFocusable(false);
				}
				if (mEmailEditText.isFocusable()) {
					mEmailEditText.setFocusable(false);
				}
		 }
		return super.onKeyDown(keyCode, event);
	}
	
	public class PreviousOrderAdapter extends BaseAdapter
	{
		Context mContext;

		public PreviousOrderAdapter(Context context, int textViewResourceId, ArrayList<Order> previousOrders)
		{
			mContext = context;
		}

		TextView orderID;
		TextView orderTime;

		@Override
		public int getCount()
		{
			return previousOrders.size();
		}

		@Override
		public Object getItem(int position)
		{
			return position;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View row = null;
			if (convertView == null)
			{
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.orderitem, parent, false);
			}
			else
			{
				row = convertView;
			}
			orderID = (TextView) row.findViewById(R.id.orderIDTV);
			orderTime = (TextView) row.findViewById(R.id.orderTimeTV);
			details = (Button) row.findViewById(R.id.detailsButton);
			orderID.setText(previousOrders.get(position).orderID);
			orderTime.setText(previousOrders.get(position).orderTime);
			orderID.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent myIntent = new Intent(SettingsActivity.this, OrderSummaryActivity.class);
					myIntent.putExtra(FORM_SETTING, isFromSetting);
					myIntent.putExtra("details", true);
					myIntent.putExtra("orderid", orderID.getText());
					PrintHelper.orderID = orderID.getText().toString();
					PrintHelper.orderTime = orderTime.getText().toString();
					//mImageSelectionDatabase.populateOrderInfo(orderID.getText().toString());
					startActivity(myIntent);
				}
			});
			details.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent myIntent = new Intent(SettingsActivity.this, OrderSummaryActivity.class);
					myIntent.putExtra(FORM_SETTING, isFromSetting);
					myIntent.putExtra("details", true);
					myIntent.putExtra("orderid", orderID.getText());
					PrintHelper.orderID = orderID.getText().toString();
					PrintHelper.orderTime = orderTime.getText().toString();
					//mImageSelectionDatabase.populateOrderInfo(orderID.getText().toString());
					startActivity(myIntent);
				}
			});
			convertView = row;
			return row;
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		mImageSelectionDatabase.close();
		previousOrderLL.removeAllViews();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		firstNameIV.setVisibility(View.INVISIBLE);
		lastNameIV.setVisibility(View.INVISIBLE);
		phoneIV.setVisibility(View.INVISIBLE);
		emailIV.setVisibility(View.INVISIBLE);
		mFirstNameEditText.setFocusable(true);
		mLastNameEditText.setFocusable(true);
		mPhoneEditText.setFocusable(true);
		mEmailEditText.setFocusable(true);
		String selectedRetailerInfo = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getString("selectedRetailerInfo", "");
		if (!Connection.isConnected(SettingsActivity.this)) {
			printSize.setText("");
			printSize.setEnabled(false);
		} else {
			printSize.setEnabled(true);
			int iSizeCount = 0;
			if (PrintHelper.products == null) {
				Log.i(TAG, "PrintHelper.products is null*************************");
				PrintHelper.products = new ArrayList<PrintProduct>();
			}
			if (selectedRetailerInfo.length() <=2 || selectedRetailerInfo ==null)
			{
				Log.i(TAG, "PrintHelper.products.size() <= 0*************************");
				try
				{
					Thread thrd = new Thread()
					{
						@Override
						public void run()
						{
							PrintMakerWebService service = new PrintMakerWebService(SettingsActivity.this, "");
							//service.getPrintProducts(false,"");
							service.GetRequiredContactInformation(SettingsActivity.this);
						}
					};
					thrd.start();
				}
				catch (Exception ex)
				{
					Log.e(TAG, "Error getting print prices");
				}
			}
			if (prefs.getString("defaultSize", "").equals("") && PrintHelper.products != null && PrintHelper.products.size() > 0)
			{
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("defaultSize", PrintHelper.products.get(0).getShortName().toString());
				editor.commit();
			}
			Log.i(TAG, "the size of PrintHelper.products is " + PrintHelper.products.size() + "*******************************");
			for(PrintProduct product : PrintHelper.products){
				if(product.getType().equals(PrintProduct.TYPE_PRINTS)){
					iSizeCount++;
				}
			}
			String[] sizes = new String[iSizeCount];
			for (int i = 0; i < iSizeCount; i++)
			{
				sizes[i] = PrintHelper.products.get(i).getName();
			}
			adapter = new MyCustomAdapter(SettingsActivity.this, R.layout.row, sizes);
			printSize.setText(prefs.getString("defaultSize", ""));
			printSize.setOnTouchListener(new OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					return false;
				}
			});
			
			printSize.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					final Dialog dialog = new Dialog(SettingsActivity.this, R.style.DropDownDialog);
					dialog.setContentView(R.layout.custom_dialog);
					dialog.setCancelable(true);
					TextView titleTV = (TextView) dialog.findViewById(R.id.titleTV);
					titleTV.setText(getString(R.string.printSize));
					ListView ssidLV = (ListView) dialog.findViewById(R.id.ssidLV);
					ssidLV.setAdapter(adapter);
					ssidLV.setOnItemClickListener(new OnItemClickListener()
					{
						@Override
						public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3)
						{
							printSizeChanged = true;
							String item = ((TextView) view.findViewById(R.id.productSizeTextView)).getText().toString();
							PrintHelper.defaultPrintSize = item;
							PrintHelper.defaultPrintSizeIndex = position;
							printSize.setText(item);
							dialog.dismiss();
						}
					});
					dialog.show();
				}
			});
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
			}
		}
		ArrayList<Integer> requiredContactInfo = new ArrayList<Integer>();
		String selectedRetailerId = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getString("selectedRetailerId", "");
		
		
		if (selectedRetailerInfo.length()>2){
			String c= selectedRetailerInfo.substring(1,selectedRetailerInfo.length()-1);
			String []e = c.split(",");
			for(String i :e){
				requiredContactInfo.add(Integer.valueOf(i.replace(" ", "")));
			}
		} else {
			if(PrintHelper.requiredContactInfos == null)
				PrintHelper.requiredContactInfos = new HashMap<String, ArrayList<Integer>>();
			if(PrintHelper.requiredContactInfos.size()>1){
				ArrayList<Integer>  a = PrintHelper.requiredContactInfos.get(selectedRetailerId);
				if(a != null){
					requiredContactInfo = a;
					Log.e(TAG, "1.requiredContactInfo: " + a.size());
				}else{
					Iterator iterator = PrintHelper.requiredContactInfos.keySet().iterator();
					while(iterator.hasNext() && requiredContactInfo.size() ==0) {
						requiredContactInfo = PrintHelper.requiredContactInfos.get(iterator.next());
					}
				}
			}
			else if(PrintHelper.requiredContactInfos.size()==1){
				for(ArrayList<Integer> a : PrintHelper.requiredContactInfos.values()){
					requiredContactInfo = a;
					Log.e(TAG, "2.requiredContactInfo: " + a.size());
				}
			}
		}
		
		Editor editor;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		editor = prefs.edit();
		editor.putString("selectedRetailerInfo", requiredContactInfo.toString());
		editor.commit();
		for (int i = 0; i < requiredContactInfo.size(); i++)
		{
			Log.e(TAG, "requiredContactInfo: " + requiredContactInfo.get(i));
			switch (requiredContactInfo.get(i))
			{
			case 0:
				firstNameIV.setVisibility(View.VISIBLE);
			case 1:
				lastNameIV.setVisibility(View.VISIBLE);
				break;
			case 2:
				phoneIV.setVisibility(View.VISIBLE);
				break;
			case 6:
				emailIV.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		}
		try
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			sendOnlyTaggedCB.setChecked(prefs.getBoolean("sendOnlyTaggedImages", true));
		}
		catch (Exception ex)
		{
		}
		if (mImageSelectionDatabase == null)
		{
			mImageSelectionDatabase = new ImageSelectionDatabase(SettingsActivity.this);
			mImageSelectionDatabase.open();
		}
		if (previousOrders == null)
		{
			previousOrders = new ArrayList<Order>();
		}
		else
		{
			previousOrders.clear();
		}
		Thread findPreviousOrders = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					previousOrders = mImageSelectionDatabase.getPreviousOrders(SettingsActivity.this);
					Log.d(TAG, "previousorderscount = " + previousOrders.size());
				}
				catch (Exception ex)
				{
				}
				previousOrderHandler.sendEmptyMessage(0);
			}
		};
		findPreviousOrders.start();
		totalNumSelectedTV.setVisibility(View.GONE);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (prefs.getBoolean("analytics", getResources().getBoolean(R.bool.analyticsEnabled)))
		{
			try
			{
				PrintHelper.mTracker.trackPageView("SetupScreen");
				PrintHelper.mTracker.dispatch();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		if(getResources().getBoolean(R.bool.localytics))
		{
			allowCookiesCB.setVisibility(View.GONE);
		}
		else
		{
			allowCookiesCB.setVisibility(View.GONE);
		}
		
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
					if(entry.getKey().equalsIgnoreCase("de")){
						prefs.edit().putBoolean(AppConstants.KEY_LOCALYTICS, false);
						prefs.edit().putBoolean(SettingsActivity.ENABLE_ALLOW_COOKIES, false).commit();
					} else {
						prefs.edit().putBoolean(SettingsActivity.ENABLE_ALLOW_COOKIES, true).commit();
					}
					break;
				}
			}
			tvCountry.setText(getString(R.string.country_label)+" " + countryName);
		}
		
		allowCookiesTV.setText(getResources().getString(R.string.privacyTitle));
		allowCookiesCB.setChecked(prefs.getBoolean(AppConstants.KEY_LOCALYTICS, getResources().getBoolean(R.bool.localytics)));
		allowCookiesCB1.setChecked(prefs.getBoolean(AppConstants.KEY_LOCALYTICS, getResources().getBoolean(R.bool.localytics)));
		if(prefs.getBoolean(SettingsActivity.ENABLE_ALLOW_COOKIES, false)){
			allowCookiesCB1.setClickable(true);
		}else{
			allowCookiesCB1.setClickable(false);
			allowCookiesCB1.setButtonDrawable(R.drawable.checkbox_disable);
		}
		allowCookiesCB1.setText(getResources().getString(R.string.analyticPermissionsDescription));
		allowCookiesCB.setText(getResources().getString(R.string.analyticPermissionsDescription));
		if (!PrintHelper.wififlow)
		{
			String firstName = prefs.getString("firstName", ""); //TODO
			String lastName = prefs.getString("lastName", "");
			String phone = prefs.getString("phone", "");
			String email = prefs.getString("email", "");
			String emailUpload = prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "");
			String emailUploadPassword = prefs.getString(PrintHelper.SHARE_PASSWORD_FLAG, "");
			mFirstNameEditText.setText(firstName);
			mLastNameEditText.setText(lastName);
			mPhoneEditText.setText(phone);
			if (!email.equals(""))
			{
				if (!Connection.isConnected(SettingsActivity.this)) { 
					mEmailEditText.setText("");
				} else {
					mEmailEditText.setText(email);
				}
				
				if ((!mEmailEditText.getText().toString().contains("@") || !mEmailEditText.getText().toString().contains(".")) && !mEmailEditText.getText().toString().equals(""))
				{
					mEmailEditText.setError(getString(R.string.incorrectEmail));
				}
				
			}
			if (editTextUploadEmail != null && !emailUpload.equals(""))
				editTextUploadEmail.setText(emailUpload);
			if (editTextUploadEmail != null && (!editTextUploadEmail.getText().toString().contains("@") || !editTextUploadEmail.getText().toString().contains(".")) && !editTextUploadEmail.getText().toString().equals(""))
			{
				editTextUploadEmail.setError(getString(R.string.incorrectEmail));
			}
			
			if(!emailUploadPassword.equals("")){
				if (editTextUploadPassword != null)
					editTextUploadPassword.setText(emailUploadPassword);
			}
			
			String storename = prefs.getString("selectedStoreName", "");
			String address = prefs.getString("selectedStoreAddress", "");
			String phonenumber = prefs.getString("selectedStorePhone", "");
			String hours = prefs.getString("selectedStoreHours", "");
			String cityAndZip = prefs.getString("selectedCity", "") + " " + prefs.getString("selectedPostalCode", "");
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
				mTxtCityAndZip.setVisibility(View.INVISIBLE);
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
			
			if (!hours.equals("") && Connection.isConnected(SettingsActivity.this)) {
				storeHours.setText(hours);
				storeHours.setVisibility(View.VISIBLE);
			} else {
				storeHours.setVisibility(View.GONE);
			}
			Drawable kiosk = getResources().getDrawable(R.drawable.pinpoint2);
			kiosk.setBounds(0, 0, kiosk.getIntrinsicWidth(), kiosk.getIntrinsicHeight());
			try
			{
				if (!Connection.isConnected(SettingsActivity.this)) {
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
					mapKiosks = new PictureKiosks(kiosk, SettingsActivity.this, prefs.getString("selectedStoreLatitude", ""), prefs.getString("selectedStoreLongitude", ""), prefs.getString("selectedStoreName", ""), prefs.getString(
							"selectedStoreAddress", ""), prefs.getString("selectedStorePhone", ""));
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
			if (mEmailEditText != null && mEmailEditText.getError() != null && mEmailEditText.getError().toString().equals(getString(R.string.incorrectEmail)))
			{
				mEmailEditText.requestFocus();
			}
		}
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}
	private static final int SIGNIN_START = 0;
	private static final int SIGNIN_FINISH= 1;
	private static final int SIGNIN_FAILED = 2;
	Runnable signInRunnable = new Runnable(){

		@Override
		public void run() {
			waitingHandler.sendEmptyMessage(SIGNIN_START);
			TokenGetter tokenGetter = new TokenGetter();
			ClientTokenResponse clientTokenResponse = tokenGetter.httpClientTokenUrlPost(ShareLoginActivity.CLIENT_TOKEN);
			if(clientTokenResponse != null){
				String username = prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "");
				String userPwd = prefs.getString(PrintHelper.SHARE_PASSWORD_FLAG, "");
				AccessTokenResponse accessTokenResponse = null;
				try {
					int count = 0;
					while (count < 2 && accessTokenResponse == null)
					{
						Log.e(TAG, "Account: " + username + " Password: " + userPwd);
						accessTokenResponse = tokenGetter.httpAccessTokenUrlPost(ShareLoginActivity.ACCESS_TOKEN_HOST, clientTokenResponse.client_token, username, userPwd, clientTokenResponse.client_secret);
						count++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(accessTokenResponse != null){
					PrintHelper.setAccessTokenResponse(accessTokenResponse, getApplicationContext());
					waitingHandler.sendEmptyMessage(SIGNIN_FINISH);
				} else {
					Log.e(TAG, "Can not get access token response.");
					waitingHandler.sendEmptyMessage(SIGNIN_FAILED);
				}
			} else {
				Log.e(TAG, "Can not get client token response.");
				waitingHandler.sendEmptyMessage(SIGNIN_FAILED);
			}
		}};
		
		Handler waitingHandler = new Handler(){

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
		InfoDialog waitingDialog;
		void showDialog(){
			InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(SettingsActivity.this);
			builder.setTitle("");
			builder.setMessage(getString(R.string.share_signin) + " ... ");
			waitingDialog = builder.create();
			waitingDialog.show();
		}
}
