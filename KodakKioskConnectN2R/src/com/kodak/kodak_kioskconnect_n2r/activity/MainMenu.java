package com.kodak.kodak_kioskconnect_n2r.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants.FlowType;
import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.CountrySelectionActivity;
import com.kodak.kodak_kioskconnect_n2r.GPSGetter;
import com.kodak.kodak_kioskconnect_n2r.ImageSelectionDatabase;
import com.kodak.kodak_kioskconnect_n2r.InfoDialog;
import com.kodak.kodak_kioskconnect_n2r.PictureUploadService2;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintMakerWebService;
import com.kodak.kodak_kioskconnect_n2r.PrintProduct;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.WiFiSelectWorkflowActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.retailer.CountryInfo;
import com.kodak.utils.RSSLocalytics;
import com.kodakalaris.video.activities.MyStoriesActivity;

public class MainMenu extends BaseActivity implements ServiceConnection {
	// This specific class for logging
	private final String TAG = this.getClass().getSimpleName();
	TextView mPhotoProductTV = null;
	ImageButton mPhotobookImageButton = null;
	TextView mCreateNewTextView = null;
	TextView mLoadExistingTextView = null;
	TextView mKioskWIFITextView = null;
	TextView mPrintsWIFITextView = null;
	TextView mTellMyStoryTextView = null;
	TextView mPrintsTV = null;
	ImageView mTellMyStoryImageView = null;
	ImageView mKioskWIFIImageView = null;
	ImageView mPhotoProductIV = null;
	ImageView logoView = null;
	ImageView mPrintsIV = null;
	RelativeLayout mPhotoProductRL = null;
	RelativeLayout mConnectKioskRL = null;
	RelativeLayout mTellMyStoryRL = null;
	RelativeLayout mPrintRL = null;
	String mLoadBookIdString = "";
	boolean mAllowToNavigate = false;
	ProgressDialog mProgressDialog = null;
	Button mNextButton = null;
	boolean atKioskPrintsEnabled = false;
	public static final String CurrentlyCountryCode = "Currently_Country_Code";
	public static final String SelectedCountryCode = "Selected_Country_Code";
	String selector;
	long adjTime;
	final String[] columns = { MediaColumns.DATA, BaseColumns._ID };
	final String orderBy = BaseColumns._ID;
	Cursor monthCursor = null;
	ImageSelectionDatabase mImageSelectionDatabase = null;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;

	SharedPreferences prefs;
	Geocoder mGeocoder;
	LocationManager mLocationManager;
	ProgressDialog dialog;
	private boolean locked = false;

	public static final String DM_COMBINED_PACKAGE_NAME = "com.kodak.dm.rsscombinedapp";
	public static final String KODAK_COMBINED_PACKAGE_NAME = "com.kodak.kodak.rsscombinedapp";

	private GPSGetter gps;

	private boolean useCountryCodeOfSelectedStore = false;
	private final String SCREEN_NAME = "Workflow Choice";
	private final String WORKFLOW_SELECTED = "Workflow Selected";
	private final String WORKFLOW_TYPE = "Workflow Type";
	private final String WORKFLOW_TYPE_KIOSK_CONN = "Kiosk Connect";
	public static final String START_OVER = "Start Over";
	private final String WORKFLOW_TYPE_TELL_MY_STORY="Tell My Story";
	private HashMap<String, String> attr = new HashMap<String, String>();
	private int errorCode;
	Intent uploadService;
	/* land==false;port==true; */
	private boolean ScreenOrientationFlag = false;
	private int errorTypeForChangeCountry = -2;
	private View noConnectDialog_View = null;
	private View showNoWifiConnectionDialog_view = null;
	private RelativeLayout bottomBar = null;
	private InfoDialog exitInfoDialog = null;
	private InfoDialog SDCardAmoutDialog = null;
	private InfoDialog norespondDialog = null;
	private InfoDialog internetErrorDialog = null;
	private InfoDialog changeCountryDialog = null;
	private InfoDialog noWifiConnectionDialog = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RSSLocalytics.onActivityCreate(this);
		// setContentView(R.layout.mainmenu);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			ScreenOrientationFlag = true;
			setContentLayout(R.layout.mainmenufield);
			getViews();
			logoView.setVisibility(View.VISIBLE);
			if (AppContext.getApplication().isContinueShopping()) {
				mNextButton.setVisibility(View.VISIBLE);
				mNextButton.setText(R.string.cart);
			}
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			ScreenOrientationFlag = false;
			setContentLayout(R.layout.mainmenufield_port);
			getViews();
			if (AppContext.getApplication().isContinueShopping()) {
				mNextButton.setVisibility(View.VISIBLE);
				mNextButton.setText(R.string.cart);
				logoView.setVisibility(View.GONE);
			}else {
				logoView.setVisibility(View.VISIBLE);
			}
		}
		/* hide the bottomBar */
		bottomBar.setVisibility(View.INVISIBLE);
		initData();
		setEvents();

	}

	@Override
	protected void onDestroy() {
		AppContext.getApplication().setScreenOrientationFlag(true);
		super.onDestroy();
		if (PrintHelper.mActivities.contains(this)) {
			PrintHelper.mActivities.remove(this);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		if (dialog != null && dialog.isShowing()) {
			IsScreenChange = true;
		}
	
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setContentLayout(R.layout.mainmenufield);
			getViews();
			ScreenOrientationFlag = true;
			logoView.setVisibility(View.VISIBLE);
			if (AppContext.getApplication().isContinueShopping()) {
				mNextButton.setVisibility(View.VISIBLE);
				mNextButton.setText(R.string.cart);
			}
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setContentLayout(R.layout.mainmenufield_port);
			getViews();
			ScreenOrientationFlag = false;
			if (AppContext.getApplication().isContinueShopping()) {
				mNextButton.setVisibility(View.VISIBLE);
				mNextButton.setText(R.string.cart);
				logoView.setVisibility(View.GONE);
			}else {
				logoView.setVisibility(View.VISIBLE);
			}
		}
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = new GettingProductsDialog(this);
			dialog.setCancelable(false);
			GettingProductDialogDismissListener();
			dialog.show();
			Log.i("ScreenChange", IsScreenChange + "");
		}
		initData();
		setEvents();
		/**
		 * if the configuration changed the dialog need recreate;
		 * add by song
		 */
		reCreateDialog();
		super.onConfigurationChanged(newConfig);
	}

	private boolean IsScreenChange = false;

	private void gotoNextStep(final View v, boolean wififlow) {
		Intent myIntent = null;
		PrintHelper.wififlow = wififlow;
		if (wififlow) {
			AppContext.getApplication().setFlowType(FlowType.WIFI);
			if (prefs.getBoolean("analytics", false)) {
				try {
					PrintHelper.mTracker.trackEvent("Workflow", "Wifi_At_Kiosk", "", 0);
					PrintHelper.mTracker.dispatch();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			attr.put(WORKFLOW_TYPE, WORKFLOW_TYPE_KIOSK_CONN);
			RSSLocalytics.recordLocalyticsEvents(this, WORKFLOW_SELECTED, attr);
			myIntent = new Intent(MainMenu.this, WiFiSelectWorkflowActivity.class);
			startActivity(myIntent);
		} else {
			Class<com.kodak.kodak_kioskconnect_n2r.PictureUploadService2> pictureUploadService2 = com.kodak.kodak_kioskconnect_n2r.PictureUploadService2.class;
			Intent serviceIntent = new Intent(MainMenu.this, pictureUploadService2);
			try {
				ComponentName serviceComponentName = startService(serviceIntent);
				if (serviceComponentName != null) {
					Log.i(TAG, "onCreate() startService called CompnentName=" + serviceComponentName.toString());
				}
			} catch (SecurityException se) {
				se.printStackTrace();
			}
			if (PrintHelper.mLoggingEnabled)
				Log.d(TAG, "prints touch gotoNextStep");
			if (dialog != null && dialog.isShowing()) {
				return;
			}
			dialog = new GettingProductsDialog(this);
			dialog.setCancelable(false);

			GettingProductDialogDismissListener();
			if (appForbidden(MainMenu.this)){
				return;
			}
			dialog.show();
			new Thread(new CheckInternet()).start();
			return;
		}
	}

	private void GettingProductDialogDismissListener() {
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (!IsScreenChange) {
					Log.i("ScreenChangeINstep", IsScreenChange + "");
					if (locked) {
						return;
					}

					boolean ifGetRequiredInfo = prefs.getBoolean("ifGetRequiredInfo", false);
					if (errorCode == ERROR_CANNOT_CONNECT_TO_INTERNET) {
						showInternetErrorDialog();
					} else if (errorCode == ERROR_NO_COUNTRY_LIST) {
						showNorespondDialog();
					} else if (errorCode == ERROR_COUNTRY_DIFFER_FROM_SAVAED_STORE) {
						showChangeCountryDialog(ERROR_COUNTRY_DIFFER_FROM_SAVAED_STORE);
					} else if (errorCode == ERROR_COUNTRY_NULL) {
						showChangeCountryDialog(ERROR_COUNTRY_NULL);
					} else if (errorCode == ERROR_COUNTRY_VALID) {
						showChangeCountryDialog(ERROR_COUNTRY_VALID);
					} else if (PrintHelper.products != null && PrintHelper.products.size() > 0 && ifGetRequiredInfo
							&& PrintHelper.selectedCountryInfo != null) {
						if (mPrintClicked) {
							Intent myIntent = new Intent(MainMenu.this, PhotoSelectMainFragmentActivity.class);
							AppContext.getApplication().setFlowType(FlowType.PRINT);
							attr.put(WORKFLOW_TYPE, ProductSelectActivity.TYPE_PRINT);
							RSSLocalytics.recordLocalyticsEvents(MainMenu.this, WORKFLOW_SELECTED, attr);
							startActivity(myIntent);
						} else {
							Intent myIntent = new Intent(MainMenu.this, ProductSelectActivity.class);
							myIntent.putExtra("isFromMainMenu", true);
							startActivity(myIntent);
						}
					} else {
						showNorespondDialog();
					}
				}
				IsScreenChange = false;
			}
		});
	}

	class CheckInternet implements Runnable {

		@Override
		public void run() {
			if (!Connection.isConnectedInternet(MainMenu.this)) {
				errorCode = ERROR_CANNOT_CONNECT_TO_INTERNET;
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
			} else {
				checkCountryCode();
			}
		}

	}

	private void checkCountryCode() {
		if (PrintHelper.countries == null || PrintHelper.countries.size() == 0) {
			Log.e(TAG, "Country list has not been fetched, please wait...");
			if (!gettingCountries) {
				new Thread(new GetCountries()).start();
			}
		} else if (doesCurrentCountryDifferFromSavedStore() && !useCountryCodeOfSelectedStore) {
			errorCode = ERROR_COUNTRY_DIFFER_FROM_SAVAED_STORE;
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
		} else if (isCurrentCountryIsNull()) {
			errorCode = ERROR_COUNTRY_NULL;
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
		} else if (!isCurrentCountryValid()) {
			errorCode = ERROR_COUNTRY_VALID;
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
		} else {
			errorCode = ERROR_NOTHING;
			new Thread(new GetProducts()).start();
		}
	}

	private boolean isCurrentCountryValid() {
		boolean isCurrentCountryValid = false;
		String currentCountry = prefs.getString(MainMenu.CurrentlyCountryCode, "");
		if ((!currentCountry.equals("") && currentCountry.toLowerCase().equals(prefs.getString(MainMenu.CurrentlyCountryCode, "").toLowerCase()))) {
			Iterator<Entry<String, String>> iter = PrintHelper.countries.entrySet().iterator();
			while (iter.hasNext()) {
				String needToCompareCode = iter.next().getKey().toLowerCase();
				// Log.w(TAG, "isCurrentCountryValid[currentCountry:" +
				// currentCountry.toLowerCase() + ", code:" + needToCompareCode
				// + "]");
				if (currentCountry.toLowerCase().equals(needToCompareCode)) {
					prefs.edit().putString(MainMenu.SelectedCountryCode, currentCountry).commit();
					isCurrentCountryValid = true;
					break;
				}
			}
		}
		Log.i(TAG, "isCurrentCountryValid.... " + isCurrentCountryValid);
		return isCurrentCountryValid;
	}

	/**
	 * to check the counties which are fetched from server whether is null
	 * 
	 * @return true if the countries is null, false otherwise
	 */
	private boolean isCurrentCountryIsNull() {
		boolean isCurrentCountryIsNull = prefs.getString(MainMenu.CurrentlyCountryCode, "").equals("");
		if (isCurrentCountryIsNull) {
			String lastSelectedCountryCode = prefs.getString(MainMenu.SelectedCountryCode, "");
			isCurrentCountryIsNull = lastSelectedCountryCode.equals("");
			if (!isCurrentCountryIsNull) {
				prefs.edit().putString(MainMenu.CurrentlyCountryCode, lastSelectedCountryCode).commit();
			}
		}
		Log.i(TAG, "isCurrentCountryIsNull.... " + isCurrentCountryIsNull);
		return isCurrentCountryIsNull;
	}

	private boolean doesCurrentCountryDifferFromSavedStore() {
		boolean doesCurrentCountryDifferFromSavedStore = false;
		String selectedStoreCounry = prefs.getString("selectedStoreCountry", "");
		if (selectedStoreCounry.equals("")) {
			doesCurrentCountryDifferFromSavedStore = false;
		} else {
			String currentCountryCode = getCurrentCountryCode();
			if ("".equals(currentCountryCode)) {
				doesCurrentCountryDifferFromSavedStore = false;
			} else {
				doesCurrentCountryDifferFromSavedStore = !currentCountryCode.toLowerCase().equals(selectedStoreCounry.toLowerCase());
			}
		}
		Log.i(TAG, "doesCurrentCountryDifferFromSavedStore.... " + doesCurrentCountryDifferFromSavedStore);
		return doesCurrentCountryDifferFromSavedStore;
	}

	private String getCurrentCountryCode() {
		String currentCountryCode = prefs.getString(MainMenu.CurrentlyCountryCode, "");
		if (("").equals(currentCountryCode)) {
			Geocoder geocoder = new Geocoder(this, new Locale("en"));
			List<Address> addresses = null;
			if (gps.getLatitude() != 0 && gps.getLongitude() != 0) {
				try {
					addresses = geocoder.getFromLocation(gps.getLatitude(), gps.getLongitude(), 5);
					if (addresses != null && addresses.size() > 0) {
						Address address = addresses.get(0);
						currentCountryCode = address.getCountryCode();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return currentCountryCode == null ? "" : currentCountryCode;
	}

	class GetProducts implements Runnable {

		@Override
		public void run() {
			if (PrintHelper.products == null) {
				PrintHelper.products = new ArrayList<PrintProduct>();
			}
			try {
				int count = 0;
				String result = "";
				PrintMakerWebService service = new PrintMakerWebService(MainMenu.this, "");
				while (count < 5 && result.equals("")) {
					result = service.getPrintProducts(false, "");
					count++;
				}
				result = "";
				count = 0;
				while (count < 5 && result.equals("")) {
					result = service.GetRequiredContactInformation(MainMenu.this);
					count++;
				}
				// PrintHelper.countryInfos = null;
				PrintHelper.allCountryInfoes = null;
//				StringBuilder countryCodesSb = null;
				String countryCodesString = "";
				/*
				 * fixed for RSSMOBILEPDC-2126 by song
				 */
//				if (PrintHelper.countries != null && !PrintHelper.countries.isEmpty()) {
//					Iterator<Entry<String, String>> iterator = PrintHelper.countries.entrySet().iterator();
//					countryCodesSb = new StringBuilder("");
//					if (iterator != null) {
//						while (iterator.hasNext()) {
//							Entry<String, String> entry = iterator.next();
//							String countryCode = entry.getKey();
//							countryCodesSb.append(countryCode).append(",");
//
//						}
//						countryCodesString = countryCodesSb.substring(0, countryCodesSb.length() - 1);
//
//					}
//
//				} else {
					countryCodesString = prefs.getString(MainMenu.SelectedCountryCode, "");
//				}

				PrintHelper.allCountryInfoes = service.getCountryInfoTask(countryCodesString);
				if (PrintHelper.allCountryInfoes != null) {
					Iterator<CountryInfo> iter = PrintHelper.allCountryInfoes.iterator();
					if (iter != null) {
						if (PrintHelper.countryInfoMap != null) {
							PrintHelper.countryInfoMap.clear();
						} else {
							PrintHelper.countryInfoMap = new HashMap<String, CountryInfo>();
						}
						while (iter.hasNext()) {
							CountryInfo countryInfo = (CountryInfo) iter.next();
							PrintHelper.countryInfoMap.put(countryInfo.countryCode, countryInfo);

						}
					}

					PrintHelper.selectedCountryInfo = PrintHelper.countryInfoMap.get(prefs.getString(MainMenu.SelectedCountryCode, ""));

				}

				// PrintHelper.countryInfos =
				// service.getCountryInfoTask(countryCodesString);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			resetDefaultSize();
		}

		/**
		 * add by song fixed for RSSMOBILEPDC-1857
		 */
		private void resetDefaultSize() {
			List<String> pringSizeList = new ArrayList<String>();
			if (PrintHelper.products != null && !PrintHelper.products.isEmpty()) {
				for (int i = 0; i < PrintHelper.products.size(); i++) {
					if (PrintHelper.products.get(i) != null
							&& PrintHelper.products.get(i).getType()
									.equals(PrintProduct.TYPE_PRINTS)) {
						pringSizeList.add(PrintHelper.products.get(i).getShortName());
					}
				}
			}
			String defaultSize = prefs.getString("defaultSize", "");
			for (int i = 0 ; i < pringSizeList.size() ; i ++) {
				if (pringSizeList.get(i).equals(defaultSize)) {
					prefs.edit().putString("defaultSize", pringSizeList.get(i)).commit();
					PrintHelper.defaultPrintSizeIndex = i;
					break;
				} else {
					PrintHelper.defaultPrintSizeIndex = 0;
					prefs.edit().putString("defaultSize", pringSizeList.get(0)).commit();
				}
			}
		
		}
	}

	private boolean gettingCountries = false;

	class GetCountries implements Runnable {

		@Override
		public void run() {
			try {
				gettingCountries = true;
				int count = 0;
				String result = "";
				PrintMakerWebService service = new PrintMakerWebService(MainMenu.this, "");
				while (count < 5 && result.equals("")) {
					result = service.getCountries();
					count++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				gettingCountries = false;
				if (PrintHelper.countries != null && PrintHelper.countries.size() == 0) {
					errorCode = ERROR_NO_COUNTRY_LIST;
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
				} else {
					checkCountryCode();
				}
			}
		}

	}

	/**
	 * This popup will be displayed any time the user starts the N2R work flow
	 * when Wi-Fi is not running. it will be shown every time unless "don't ask"
	 * is selected
	 * 
	 * **/
	private void showNoWifiConnectionDialog(final View v, final SharedPreferences.Editor editor) {
		showNoWifiConnectionDialog_view = v;
		PrintHelper.isRecommendWiFiDialog = true;
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.nowificonnection));
		builder.setPositiveButton(getString(R.string.donotaskagain), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PrintHelper.ifRecommendWiFiSet = false;
				editor.putBoolean("ifRecommendWiFiSet", false); // if
																// recommend
																// user
																// changing
																// phone
																// settings
																// to
																// wifi
																// , add
																// by
																// song
				editor.commit();
				dialog.dismiss();
				gotoNextStep(v, false);
			}
		});
		builder.setNegativeButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				gotoNextStep(v, false);
			}
		});
		noWifiConnectionDialog = builder.create();
		noWifiConnectionDialog.show();
	}

	private void showNorespondDialog() {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		String message = getString(R.string.share_upload_error_no_responding);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (mPrintClicked) {
					mPrintRL.performClick();
				} else {
					mPhotoProductRL.performClick();
				}
			}
		});
		builder.setCancelable(false);
		norespondDialog = builder.create();
		norespondDialog.show();
	}

	private void showInternetErrorDialog() {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		String message = getString(R.string.error_cannot_connect_to_internet);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (mPrintClicked) {
					mPrintRL.performClick();
				} else {
					mPhotoProductRL.performClick();
				}
			}
		});
		builder.setCancelable(false);
		internetErrorDialog = builder.create();
		internetErrorDialog.show();
	}

	private final int ERROR_NOTHING = -1;
	private final int ERROR_COUNTRY_VALID = 0;
	private final int ERROR_COUNTRY_NULL = 1;
	private final int ERROR_COUNTRY_DIFFER_FROM_SAVAED_STORE = 2;
	private final int ERROR_NO_COUNTRY_LIST = 3;
	private final int ERROR_CANNOT_CONNECT_TO_INTERNET = 4;

	private void showChangeCountryDialog(final int type) {
		errorTypeForChangeCountry = type;
		Log.w(TAG, "showChangeCountryDialog[type:" + type + "]");
		int positiveButtonStringId = 0;
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		switch (type) {
		case ERROR_COUNTRY_VALID:
			builder.setMessage(getString(R.string.titlepage_error_no_products_in_country));
			positiveButtonStringId = R.string.Back;
			break;
		case ERROR_COUNTRY_NULL:
			builder.setMessage(getString(R.string.titlepage_error_location_not_determined));
			positiveButtonStringId = R.string.Back;
			break;
		case ERROR_COUNTRY_DIFFER_FROM_SAVAED_STORE:
			builder.setMessage(getString(R.string.titlepage_error_saved_store_not_in_country));
			positiveButtonStringId = R.string.OK;
			break;
		}
		builder.setPositiveButton(positiveButtonStringId, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (type == ERROR_COUNTRY_VALID || type == ERROR_COUNTRY_NULL) {
					dialog.dismiss();
				} else if (type == ERROR_COUNTRY_DIFFER_FROM_SAVAED_STORE) {
					String selectedStoreCountry = prefs.getString("selectedStoreCountry", "");
					prefs.edit().putString(MainMenu.SelectedCountryCode, selectedStoreCountry).commit();
					prefs.edit().putString(MainMenu.CurrentlyCountryCode, selectedStoreCountry).commit();
					useCountryCodeOfSelectedStore = true;
					dialog.dismiss();
				}

			}
		});
		builder.setNegativeButton(R.string.shoppting_cart_change, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(MainMenu.this, CountrySelectionActivity.class);
				startActivity(intent);
			}
		});
		changeCountryDialog = builder.create();
		changeCountryDialog.show();
	}

	private void showSDCardAmoutDialog() {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(MainMenu.this);
		builder.setMessage(getString(R.string.sdcardnotmountedmessage));
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		SDCardAmoutDialog = builder.create();
		SDCardAmoutDialog.show();
	}

	private LocationListener mLocationListener = new LocationListener() {

		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.e(TAG, "onStatusChanged, provider = " + provider);
		}

		public void onProviderEnabled(String provider) {
			Log.e(TAG, "onProviderEnabled, provider = " + provider);
		}

		public void onProviderDisabled(String provider) {
			Log.e(TAG, "onProviderDisabled, provider = " + provider);
		}

		public void onLocationChanged(Location location) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			Log.d(TAG, "onLocationChanged, latitude: " + latitude + ", longitude: " + longitude);

			boolean flag = true;
			int iCount = 0;
			while (flag && iCount < 3) {
				try {
					List<Address> lstAddress = mGeocoder.getFromLocation(latitude, longitude, 1);
					for (Address a : lstAddress) {
						Locale l = new Locale(Locale.getDefault().getLanguage(), a.getCountryCode());
						Editor editor = prefs.edit();
						editor.putString(CurrentlyCountryCode, a.getCountryCode());
						editor.commit();
						Log.e(TAG, "onLocationChanged, CountryCode: " + a.getCountryCode() + ", Currently Locale:" + l);
					}
					flag = false;
					iCount++;
				} catch (Exception e) {
					flag = true;
					iCount++;
					prefs.edit().putString(CurrentlyCountryCode, "").commit();
					e.printStackTrace();
				}
			}
		}
	};

	private Handler findAlbumsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				PrintHelper.albumsLoaded = true;
			} catch (Exception ex) {

			}
		}
	};
	boolean quit = false;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			showCloseInforDialog();
		}
		return false;
	}

	@Override
	public void onResume() {
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			ScreenOrientationFlag = true;

		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			ScreenOrientationFlag = false;

		}
		AppContext.getApplication().setScreenOrientationFlag(ScreenOrientationFlag);
		AppContext.getApplication().setFlowType(null);
		RSSLocalytics.onActivityResume(this);
		locked = false;
		PrintHelper.inQuickbook = false;
		PrintHelper.hasQuickbook = false;

		uploadService = new Intent(getApplicationContext(), com.kodak.kodak_kioskconnect_n2r.PictureUploadService2.class);
		if (PrintHelper.isNull()) {
			new PrintHelper(MainMenu.this);
		} else {
			PrintHelper.StartOver();
		}
		try {
			stopService(uploadService);
		} catch (SecurityException sex) {
			sex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		super.onResume();
		//RSSMOBILEPDC-1760: Auto connect kisok
		if (Connection.isConnectedKioskWifi(this)) {
			startActivity(new Intent(this,	WiFiSelectWorkflowActivity.class));
		}
		// wl.acquire();
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		if (!mExternalStorageAvailable || !mExternalStorageWriteable) {
			showSDCardAmoutDialog();
		}
		if (mImageSelectionDatabase == null)
			mImageSelectionDatabase = new ImageSelectionDatabase(this);
		mImageSelectionDatabase.open();

		try {
			if (prefs.getBoolean("analytics", false)) {
				if (PrintHelper.wififlow) {
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "None", 3);
				} else {
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "None", 3);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			if (prefs.getBoolean("analytics", false)) {
				PrintHelper.mTracker.trackPageView("Page-Workflow");
				PrintHelper.mTracker.dispatch();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		AppContext.getApplication().setScreenOrientationFlag(true);
		RSSLocalytics.onActivityPause(this);
		super.onPause();
		mImageSelectionDatabase.close();
		try {
			if (mLocationManager != null && mLocationListener != null) {
				mLocationManager.removeUpdates(mLocationListener);
			}
			mLocationManager = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (dialog != null && dialog.isShowing()) {
			locked = true;
			dialog.dismiss();
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
	}

	class GettingProductsDialog extends ProgressDialog {

		public GettingProductsDialog(Context context) {
			super(context);
		}

		public GettingProductsDialog(Context context, int theme) {
			super(context, theme);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.getting_products_dialog);
			RelativeLayout dialog_LinearLayout = (RelativeLayout) findViewById(R.id.dialog_LinearLayout);
			LayoutParams dialogLp = dialog_LinearLayout.getLayoutParams();
			Display display = getWindowManager().getDefaultDisplay();
			if (ScreenOrientationFlag) {
				dialogLp.height = display.getHeight() * 2 / 3;
				dialogLp.width = display.getWidth() * 3 / 4;
				dialog_LinearLayout.setLayoutParams(dialogLp);
			} else {
				dialogLp.height = display.getHeight() * 2 / 5;
				dialogLp.width = display.getWidth() * 4 / 5;
				dialog_LinearLayout.setLayoutParams(dialogLp);
			}
		}

		@Override
		public void show() {
			super.show();
		}

	}

	public void getViews() {
		mNextButton = (Button) findViewById(R.id.next_btn);
		mPhotoProductTV = (TextView) findViewById(R.id.photoproductsTV);
		mKioskWIFITextView = (TextView) findViewById(R.id.connectKioskTV);
		mPhotoProductIV = (ImageView) findViewById(R.id.photoproductsIV);
		mKioskWIFIImageView = (ImageView) findViewById(R.id.connectKioskIV);
		logoView = (ImageView) findViewById(R.id.imageView1);
		mTellMyStoryImageView = (ImageView) findViewById(R.id.tellMyStoryIV);
		mTellMyStoryTextView = (TextView) findViewById(R.id.tellMyStoryTV);
		mPhotoProductRL = (RelativeLayout) findViewById(R.id.photoproductRL);
		mConnectKioskRL = (RelativeLayout) findViewById(R.id.connectKioskRL);
		mTellMyStoryRL = (RelativeLayout) findViewById(R.id.tellMyStoryItem);
		mPrintRL = (RelativeLayout) findViewById(R.id.printsRL);
		mPrintsIV = (ImageView) findViewById(R.id.printsIV);
		mPrintsTV = (TextView) findViewById(R.id.printsTV);
		bottomBar = (RelativeLayout) findViewById(R.id.main_bottombar);
	}

	public void initData() {
		AppContext.getApplication().setScreenOrientationFlag(ScreenOrientationFlag);
//		if (AppContext.getApplication().isContinueShopping()) {
//			mNextButton.setVisibility(View.VISIBLE);
//			mNextButton.setText(R.string.cart);
//			logoView.setVisibility(View.VISIBLE);
//		}
		prefs = PreferenceManager.getDefaultSharedPreferences(MainMenu.this);
		PrintHelper.handleUncaughtException(MainMenu.this, this);
		mPhotoProductTV.setVisibility(android.view.View.VISIBLE);
		mPhotoProductTV.setTypeface(PrintHelper.tf);
		mKioskWIFITextView.setTypeface(PrintHelper.tf);
		mPhotoProductTV.setText(getString(R.string.TMS_mainmenu_photoproducts_text));
		mKioskWIFITextView.setText(getString(R.string.TMS_mainmenu_kioskconnect_text));
		mPrintsTV.setText(getString(R.string.product_prints));
		mTellMyStoryTextView.setText(getString(R.string.TMS_mainmenu_tellmystory_text));
		// if(getApplicationContext().getPackageName().contains(DM_COMBINED_PACKAGE_NAME)){
		mPhotoProductIV.setImageResource(R.drawable.button_photoproducts);
		mPrintsIV.setImageResource(R.drawable.button_prints);
		mTellMyStoryImageView.setImageResource(R.drawable.button_tellmystory);
		mKioskWIFIImageView.setImageResource(R.drawable.button_kioskconnect);
		// }
		View tellMyStoryItem = findViewById(R.id.tellMyStoryItem);
		if (!getResources().getBoolean(R.bool.enable_tms)) {
			tellMyStoryItem.setVisibility(View.INVISIBLE);
		} else {
			tellMyStoryItem.setVisibility(View.VISIBLE);
		}

		gps = GPSGetter.getGpsInstance(MainMenu.this);
		gps.openGps();
		// mImageSelectionDatabase.handleDeselecteAllUris();
		Thread findAlbums = new Thread() {
			@Override
			public void run() {
				try {
					// setupAlbums();
					// createThumbnails();
				} catch (Exception ex) {
				}
				findAlbumsHandler.sendEmptyMessage(0);
			}
		};
		if (!PrintHelper.albumsLoaded)
			findAlbums.start();

		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
	}

	private boolean mPrintClicked = false;

	public void setEvents() {

		mPrintRL.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mPrintClicked = true;
				SharedPreferences.Editor editor = prefs.edit();
				if (!Connection.isConnected(MainMenu.this)) {
					noConnectDialog_View = v;
					showNoConnectionDialog(v);
				} else if (!Connection.isConnectedAnyWifi(MainMenu.this) && PrintHelper.ifRecommendWiFiSet) {
					showNoWifiConnectionDialog(v, editor);
				} else {
					gotoNextStep(v, false);
				}
			}
		});
		mPhotoProductRL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPrintClicked = false;
				SharedPreferences.Editor editor = prefs.edit();
				if (!Connection.isConnected(MainMenu.this)) {
					noConnectDialog_View = v;
					showNoConnectionDialog(v);
				} else if (!Connection.isConnectedAnyWifi(MainMenu.this) && PrintHelper.ifRecommendWiFiSet) {
					showNoWifiConnectionDialog(v, editor);
				} else {
					gotoNextStep(v, false);
				}
			}
		});
		mConnectKioskRL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mExternalStorageAvailable || !mExternalStorageWriteable) {
					showSDCardAmoutDialog();
				} else {
					gotoNextStep(v, true);
				}
			}
		});

		mNextButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(MainMenu.this, ShoppingCartActivity.class);
				startActivity(myIntent);
				MainMenu.this.finish();

			}
		});

		mTellMyStoryRL.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				attr.put(WORKFLOW_TYPE, WORKFLOW_TYPE_TELL_MY_STORY);
				RSSLocalytics.recordLocalyticsEvents(MainMenu.this, WORKFLOW_SELECTED, attr);
				Intent myIntent = new Intent(MainMenu.this, MyStoriesActivity.class);
				startActivity(myIntent);
			}
		});

	}
	
	private void showCloseInforDialog() {

		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(MainMenu.this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.exitapplication));
		builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				mImageSelectionDatabase.handleDeleteAllUrisN2R();
				PrintHelper.StartOver();
				PictureUploadService2.mTerminated = true;
				if (uploadService != null) {
					try {
						stopService(uploadService);
					} catch (SecurityException sex) {
						sex.printStackTrace();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			
				finish();
				System.exit(0);
				
			}
		});
		builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		exitInfoDialog = builder.create();
		exitInfoDialog.show();
	}
	
	private void reCreateDialog() {
		if (exitInfoDialog != null && exitInfoDialog.isShowing()) {
			exitInfoDialog.dismiss();
			showCloseInforDialog();
		}
		if (SDCardAmoutDialog != null && SDCardAmoutDialog.isShowing()) {
			SDCardAmoutDialog.dismiss();
			showSDCardAmoutDialog();
		}
		if (norespondDialog != null && norespondDialog.isShowing()) {
			norespondDialog.dismiss();
			showNorespondDialog();
		}
		if (internetErrorDialog != null && internetErrorDialog.isShowing()) {
			internetErrorDialog.dismiss();
			showInternetErrorDialog();
		}
		if (changeCountryDialog != null && changeCountryDialog.isShowing()) {
			changeCountryDialog.dismiss();
			showChangeCountryDialog(errorTypeForChangeCountry);
		}
		if (noConnectionDialog != null && noConnectionDialog.isShowing()) {
			noConnectionDialog.dismiss();
			showNoConnectionDialog(noConnectDialog_View);
		}
		if (noWifiConnectionDialog != null && noWifiConnectionDialog.isShowing()) {
			noWifiConnectionDialog.dismiss();
			showNoWifiConnectionDialog(showNoWifiConnectionDialog_view, prefs.edit());
		}
		if (gotoHomeDialog != null && gotoHomeDialog.isShowing()) {
			gotoHomeDialog.dismiss();
		} 
	}
}
