package com.kodak.kodak_kioskconnect_n2r;

import java.util.HashMap;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.AppConstants;
import com.AppConstants.FlowType;
import com.AppContext;
import com.AppManager;
import com.google.zxing.client.android.CaptureActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.BaseActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.kodak_kioskconnect_n2r.activity.PhotoSelectMainFragmentActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.WifiTaggedImagesActivity;
import com.kodak.kodak_kioskconnect_n2r.view.InfoDialogWindow;
import com.kodak.utils.RSSLocalytics;

public class WiFiSelectWorkflowActivity extends BaseActivity {
	// private final String TAG = this.getClass().getSimpleName();
	private final String TAG = this.getClass().getSimpleName();
	View viewDisplayAll;
	View viewSelect;
	View viewFindStore;
	View viewDivider;
	TextView tagFlow;
	TextView allFlow;
	TextView findStoreTV;
	ImageView tagFlowIV;
	ImageView allFlowIV;
	Button backButton;
	ImageSelectionDatabase mImageSelectionDatabase;
	boolean quit = false;
	boolean isWMC, isPrintMaker = false;
	boolean isImageSelected = false;
	ImageView brand;

	private final String SCREEN_NAME = "Wifi Choice";
	private final String VIEW_WIFI_DISPLAY_ALL = "Wifi Display All";
	private final String VIEW_WIFI_SELECT_AND_SEND = "Wifi Select and Send";
	private final String EVENT_WIFI_DISPLAY_ALL_SELECTED = "Wifi Display All Selected";
	private HashMap<String, String> attr = new HashMap<String, String>();
	
	private boolean isFromWifiD = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.wifiselect);
		setContentLayout(R.layout.activity_wifi_select);
		PrintHelper.handleUncaughtException(WiFiSelectWorkflowActivity.this, this);
		AppContext.getApplication().setFlowType(FlowType.WIFI);
		AppContext.getApplication().setmTempSelectedPhotos(null);
		
		if (getIntent() != null) {
			isFromWifiD = getIntent().getBooleanExtra("fromWifiDisconnect", false);
		}
		
		getViews();
		initData();
		setEvents();
		RSSLocalytics.onActivityCreate(this);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
	}

	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;

	private boolean canReadStorage() {
		String state = Environment.getExternalStorageState();
		Log.i("WifiSelectWorkflowA", "state: " + state);
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		if (!mExternalStorageAvailable || !mExternalStorageWriteable) {
			InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
			builder.setTitle("");
			builder.setMessage(getString(R.string.sdcardnotmountedmessage));
			builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					// PrintHelper.StartOver();
					// System.gc();
				}
			});
			builder.setNegativeButton("", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					// mImageSelectionDatabase.handleDeleteAllUrisN2R();
					// PrintHelper.StartOver();
					// System.gc();
					// PrintHelper.hasQuickbook = false;
				}
			});

			builder.setCancelable(false);
			builder.create().show();
			return false;
		}
		return true;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentLayout(R.layout.activity_wifi_select);
		getViews();
		initData();
		setEvents();
		
		tagFlow.setText(isImageSelected ? R.string.viewtaggedset : R.string.tagpictures);
	}

	@Override
	public void onPause() {
		RSSLocalytics.onActivityPause(this);
		super.onPause();
		try {
			mImageSelectionDatabase.close();
		} catch (Exception ex) {
		}
	}

	@Override
	public void onResume() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WiFiSelectWorkflowActivity.this);
		Editor editor = prefs.edit();
		editor.putBoolean("isSentAll", false);
		editor.commit();
		if (mImageSelectionDatabase == null)
			mImageSelectionDatabase = new ImageSelectionDatabase(WiFiSelectWorkflowActivity.this);
		mImageSelectionDatabase.open();

		if (mImageSelectionDatabase.isSelectedWiFi()) {
			tagFlow.setText(getString(R.string.viewtaggedset));
			isImageSelected = true;
		} else {
			tagFlow.setText(getString(R.string.tagpictures));
			isImageSelected = false;
		}
		if (prefs.getBoolean("analytics", false)) {
			try {
				PrintHelper.mTracker.trackPageView("/ChoiceScreen");
				PrintHelper.mTracker.dispatch();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.onResume();
		RSSLocalytics.onActivityResume(this);
		
		if (isFromWifiD) {
			android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {							
						String packageName = WiFiSelectWorkflowActivity.this.getPackageName();
						Uri uri = null;
						Intent goToMarket = null;						
						try {
							uri = Uri.parse("market://details?id=" + packageName);
							goToMarket = new Intent(Intent.ACTION_VIEW, uri);	
							goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
							startActivity(goToMarket);
						} catch (ActivityNotFoundException e) {
							uri = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
							goToMarket = new Intent(Intent.ACTION_VIEW, uri);
							goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
							startActivity(goToMarket);
						}	
				}						
			};			

			String promptStr = getResources().getString(R.string.OrderScreen_RateApp);
			new InfoDialogWindow.Builder(WiFiSelectWorkflowActivity.this).setMessage(promptStr)
			.setPositiveButton(getText(R.string.OK), yesOnClickListener)
			.setNeturalButton(R.string.Cancel, null)
			.create().show();
			isFromWifiD = false;
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (PrintHelper.mActivities.contains(this)) {
			PrintHelper.mActivities.remove(this);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			// MOBILEPDC-45: changed
			if (isPrintMaker || isWMC) {
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
				builder.setTitle("");
				builder.setMessage(getString(R.string.exitapplication));
				builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						mImageSelectionDatabase.handleDeleteAllUrisN2R();
						PrintHelper.StartOver();
						finish();
					}
				});
				builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			} else {
				if (AppManager.getAppManager().isActivityExist(MainMenu.class)) {
					finish();
				} else {
					startActivity(new Intent(this, MainMenu.class));
					finish();
				}
				return true;
			}
			// finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/** Check if this device has back camera */
	public static boolean FindBackCamera() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
			return false;
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();

		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				Log.e("WiFiSelectWorkflowActivity", "Camera.CameraInfo.CAMERA_FACING_BACK");
				return true;
			} else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				Log.e("WiFiSelectWorkflowActivity", "Camera.CameraInfo.CAMERA_FACING_FRONT");
			}
		}
		return false;
	}

	@Override
	public void getViews() {
		viewDisplayAll = findViewById(R.id.kiosk_display_all);
		viewSelect = findViewById(R.id.kiosk_select);
		viewFindStore = findViewById(R.id.find_store);
		tagFlow = (TextView) findViewById(R.id.selectTV);
		allFlow = (TextView) findViewById(R.id.displayAllTV);
		tagFlowIV = (ImageView) findViewById(R.id.selectIV);
		allFlowIV = (ImageView) findViewById(R.id.displayAllIV);
		backButton = (Button) findViewById(R.id.back_btn);
		brand = (ImageView) findViewById(R.id.imageView1);
		findStoreTV = (TextView) findViewById(R.id.findStoreTV);
		viewDivider = findViewById(R.id.divider);
	}

	@Override
	public void initData() {
		brand.setVisibility(View.VISIBLE);
		backButton.setVisibility(View.VISIBLE);
		tagFlow.setTypeface(PrintHelper.tf);
		allFlow.setTypeface(PrintHelper.tf);
		allFlow.setText(getString(R.string.connectkiosk));
		isPrintMaker = getApplicationContext().getPackageName().contains("kodakprintmaker");
		isWMC = getApplicationContext().getPackageName().contains("wmc");
		if (isPrintMaker) {
//			tagFlowIV.setImageResource(R.drawable.select_tag_wifi);
			backButton.setVisibility(android.view.View.INVISIBLE);

			// MOBILEPDC-163
			String currentCountryCode = PreferenceManager.getDefaultSharedPreferences(this).getString(MainMenu.CurrentlyCountryCode, "");
			if (currentCountryCode.equalsIgnoreCase("DE") && Locale.getDefault().getLanguage().equalsIgnoreCase("DE")) {
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
				builder.setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				builder.setPositiveButton(R.string.getItHereButton, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("market://details?id=com.kodak.kodak.rsscombinedapp"));
						startActivity(intent);
					}
				});
				builder.setMessage(R.string.GermanDiscontinuanceMessage);
				builder.create().show();
			}
		} else if (isWMC) {
			viewFindStore.setVisibility(View.INVISIBLE);
			backButton.setVisibility(View.INVISIBLE);
			viewDivider.setVisibility(View.INVISIBLE);
		} else {
			viewFindStore.setVisibility(View.VISIBLE);
			viewDivider.setVisibility(View.VISIBLE);
			backButton.setVisibility(View.VISIBLE);
		}
		findStoreTV.setTypeface(PrintHelper.tf);
		
		brand.setVisibility(View.INVISIBLE);
	}

	@Override
	public void setEvents() {
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i < PrintHelper.uriEncodedPaths.size(); i++) {
					PrintHelper.selectedHash.put(PrintHelper.uriEncodedPaths.get(i), "0");
				}
				if (AppManager.getAppManager().isActivityExist(MainMenu.class)) {
					finish();
				} else {
					startActivity(new Intent(WiFiSelectWorkflowActivity.this, MainMenu.class));
					finish();
				}
			}
		});

		viewFindStore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				prefs.edit().putBoolean("connect_wifi", true).commit();
				Intent intent = new Intent(WiFiSelectWorkflowActivity.this, StoreFinder.class);
				startActivity(intent);
			}
		});
		/*
		 * if (Connection.isConnectedWifi(WiFiSelectWorkflowActivity.this)) {
		 * version.setText(getString(R.string.wifi) + " " + PrintHelper.status);
		 * } else { version.setText(getString(R.string.wifi) + " " +
		 * getString(R.string.notconnected)); }
		 */
		viewSelect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!canReadStorage()) {
					return;
				}
				if (mImageSelectionDatabase == null)
					mImageSelectionDatabase = new ImageSelectionDatabase(WiFiSelectWorkflowActivity.this);
				mImageSelectionDatabase.open();
				if (mImageSelectionDatabase.isSelectedWiFi()) {
					mImageSelectionDatabase.close();
					Intent myIntent = new Intent(WiFiSelectWorkflowActivity.this, WifiTaggedImagesActivity.class);
					startActivity(myIntent);
				} else {
					mImageSelectionDatabase.close();
//					Intent myIntent = new Intent(WiFiSelectWorkflowActivity.this, AlbumSelectActivity.class);
					Intent myIntent = new Intent(WiFiSelectWorkflowActivity.this, PhotoSelectMainFragmentActivity.class);
//					Bundle bundle = new Bundle();
//					bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.PHONE);
					myIntent.putExtra(AppConstants.KEY_PRODUCT_ID, "") ;
//					myIntent.putExtra("bundle", bundle);
					startActivity(myIntent);
				}
				RSSLocalytics.recordLocalyticsPageView(WiFiSelectWorkflowActivity.this, VIEW_WIFI_SELECT_AND_SEND);
			}
		});
		viewDisplayAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WiFiSelectWorkflowActivity.this);
				Editor editor = prefs.edit();
				editor.putBoolean("isSentAll", true);
				editor.commit();
				Intent myIntent;
				if (Connection.isConnectedKioskWifi(WiFiSelectWorkflowActivity.this)) {
					myIntent = new Intent(WiFiSelectWorkflowActivity.this, WiFiConnectionActivity.class);
					startActivity(myIntent);
				} else {
					if (FindBackCamera()) {
						myIntent = new Intent(WiFiSelectWorkflowActivity.this, CaptureActivity.class);
						startActivity(myIntent);
					} else {
						myIntent = new Intent(WiFiSelectWorkflowActivity.this, WifiManualInputActivity.class);
						startActivity(myIntent);
					}
				}
				RSSLocalytics.recordLocalyticsPageView(WiFiSelectWorkflowActivity.this, VIEW_WIFI_DISPLAY_ALL);
				RSSLocalytics.recordLocalyticsEvents(WiFiSelectWorkflowActivity.this, EVENT_WIFI_DISPLAY_ALL_SELECTED);
			}
		});
	}

}
