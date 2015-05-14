package com.kodak.kodak_kioskconnect_n2r;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.AppConstants;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.utils.PhotobookUtil;
import com.kodak.utils.RSSLocalytics;

public class KodakPrintMakerActivity extends Activity {
	private final String TAG = this.getClass().getSimpleName();
	SharedPreferences prefs;
	long startTime;
	long endTime;
	long sleepTime;
	String selector;
	long adjTime;
	private Cursor imagecursor;
	private int image_column_index;
	ImageSelectionDatabase mImageSelectionDatabase = null;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	public static String PACKAGE_NAME;
	Geocoder mGeocoder;
	// Matches the form ID:yyyy_mm_dd
	final String mEULADateRegularExpression = "ID:\\d{4}_\\d{2}_\\d{2}";
	final Pattern mEULADatePattern = Pattern
			.compile(mEULADateRegularExpression);
	LocationManager mLocationManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		RSSLocalytics.onActivityCreateForPush(KodakPrintMakerActivity.this);
		
		
		//for auto connect to kiosk
		//we can only get the latest wifi scan result, so we start scan when start, it will improve the chance to get the correct current list
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiManager.startScan();
		
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.main);
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setContentView(R.layout.main_port);
		}
		Log.i("screenChangeOnCreate", this.getResources().getConfiguration().orientation+"");
		// PrintHelper.mActivities.add(this);
		PrintHelper.handleUncaughtException(KodakPrintMakerActivity.this, this);
		prefs = PreferenceManager
				.getDefaultSharedPreferences(KodakPrintMakerActivity.this);
		mGeocoder = new Geocoder(this);
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		prefs.edit().putBoolean(NewSettingActivity.ENABLE_ALLOW_COOKIES, true)
				.commit();
		if (mLocationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			mLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 60 * 1000, 100,
					mLocationListener);
		}

		mImageSelectionDatabase = new ImageSelectionDatabase(this);
		mImageSelectionDatabase.open();
		new PrintHelper(getApplicationContext());
		PACKAGE_NAME = getApplicationContext().getPackageName();
		checkForfirstTimeRun(getVersionName(), getVersionCode());
		String state = Environment.getExternalStorageState();
		prefs.edit().putString("defaultSize", "").commit();
		prefs.edit().putString(MainMenu.CurrentlyCountryCode, "").commit();
		// prefs.edit().putString(MainMenu.SelectedCountryCode, "").commit();
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
		
		Thread splashScreen = new Thread() {
			@Override
			public void run() {
				Log.d(TAG, "Start Loading Database");
				try {
					startTime = System.currentTimeMillis();
					if (!Connection.isConnectedKioskWifi(KodakPrintMakerActivity.this)) {
						// if connected to kiosk, there is no internet
						newEULACheck();
					}
					if (mExternalStorageAvailable) {
						try {
							setupDatabase();
						} catch (Exception ex) {
							Log.e(TAG,
									"Problem in KodakPrintMakerActivity - setupDatabase()");
							ex.printStackTrace();
						}
					}
					if (PrintHelper.mLoggingEnabled)
						Log.d(TAG, "Entered splashScreen Thread");
					endTime = System.currentTimeMillis();
					sleepTime = 2000 - (endTime - startTime);
					Log.d(TAG, "Time to setup Albums: " + (endTime - startTime));
					if (sleepTime > 0) {
						sleep(sleepTime);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Log.d(TAG, "Done Loading Database");
				doneLoading.sendEmptyMessage(0);
			}
		};
		/*
		 * Thread printPrices = new Thread() {
		 * 
		 * @Override public void run() { try { PrintMakerWebService service =
		 * new PrintMakerWebService(KodakPrintMakerActivity.this, ""); int count
		 * = 0; String result = ""; if (PrintHelper.products == null) {
		 * PrintHelper.products = new ArrayList<PrintProduct>(); } while (count
		 * < 5 && result.equals("")) { result =
		 * service.GetRequiredContactInformation(KodakPrintMakerActivity.this);
		 * count++; } Log.d(TAG, "Done Finding Prices"); } catch (Exception e) {
		 * e.printStackTrace(); } } };
		 */
		Thread createPhotobookDatabase = new Thread() {

			@Override
			public void run() {
				PhotobookUtil.clearPhotobooksData(KodakPrintMakerActivity.this);
			}

		};
		// printPrices.start();
		appForbidden();
		splashScreen.start();
		createPhotobookDatabase.start();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.main);
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setContentView(R.layout.main_port);
		}
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mImageSelectionDatabase == null)
			mImageSelectionDatabase = new ImageSelectionDatabase(this);
		mImageSelectionDatabase.open();
		Log.i("screenChangeOnResume", this.getResources().getConfiguration().orientation+"");
//		if(!packageName.contains("wmc")){
//			this.localyticsSession.open();
//			this.localyticsSession.handlePushReceived(getIntent());
//		}
		
		RSSLocalytics.onActivityResumeForPush(this);
		
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageSelectionDatabase.close();
		try {
			mLocationManager.removeUpdates(mLocationListener);
			mLocationManager = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RSSLocalytics.onActivityPause(this);
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
			final double latitude = location.getLatitude();
			final double longitude = location.getLongitude();
			Log.d(TAG, "onLocationChanged, latitude: " + latitude
					+ ", longitude: " + longitude);

		
		
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					int iCount = 0;
					boolean flag = true;
					while (flag && iCount < 3) {
						try {
							
							List<Address> lstAddress = mGeocoder.getFromLocation(
									latitude, longitude, 1);
							for (Address a : lstAddress) {
								Locale l = new Locale(
										Locale.getDefault().getLanguage(),
										a.getCountryCode());
								Editor editor = prefs.edit();
								editor.putString(MainMenu.CurrentlyCountryCode,
										a.getCountryCode());
								editor.commit();
								Log.e(TAG,
										"onLocationChanged, CountryCode: "
												+ a.getCountryCode()
												+ ", Currently Locale:" + l);
							}
							flag = false;
							iCount++;
						} catch (Exception e) {
							flag = true;
							iCount++;
							prefs.edit().putString(MainMenu.CurrentlyCountryCode, "")
									.commit();
							e.printStackTrace();
						}
					}
				}
			}).start();
		
		}
	};

	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public void setupDatabase() {
		final String[] columns = { MediaColumns.DATA, BaseColumns._ID };
		final String orderBy = BaseColumns._ID;
		imagecursor = managedQuery(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
				null, orderBy);
		if (imagecursor != null) {
			image_column_index = imagecursor.getColumnIndex(BaseColumns._ID);
			if (PrintHelper.mLoggingEnabled)
				Log.d(TAG, "Count=" + imagecursor.getCount());
			PrintHelper.count = imagecursor.getCount();
			PrintHelper.imageFilePaths = new ArrayList<String>();
			PrintHelper.uriEncodedPaths = new ArrayList<String>();
			PrintHelper.allUriEncodedPaths = new ArrayList<String>();
			int i = 0;
			imagecursor.moveToFirst();
			while (i < imagecursor.getCount()) {
				int id = imagecursor.getInt(image_column_index);
				Uri uri = Uri.withAppendedPath(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						Integer.toString(id));
				if (PrintHelper.selectedHash == null) {
					PrintHelper.selectedHash = new HashMap<String, String>();
				}
				if (PrintHelper.selectedFileNames == null) {
					PrintHelper.selectedFileNames = new HashMap<String, String>();
				}
				PrintHelper.selectedHash.put(uri.toString(), "0");
				int column_index = imagecursor
						.getColumnIndexOrThrow(MediaColumns.DATA);
				PrintHelper.selectedFileNames.put(uri.toString(),
						imagecursor.getString(column_index));
				PrintHelper.uriEncodedPaths.add(uri.toString());
				PrintHelper.allUriEncodedPaths.add(uri.toString());
				i++;
				if (PrintHelper.mLoggingEnabled)
					Log.d(TAG, "" + i);
				if (!imagecursor.isClosed())
					imagecursor.moveToNext();
			}
		} else {
			Log.e(TAG, "imagecursor was null!");
		}
	}

	private void checkForfirstTimeRun(String versionName, int versionCode) {
		final String firstTimeRunKey = "FIRST_TIME_RUN_KEY";
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Boolean firstTimeRun = prefs.getBoolean(firstTimeRunKey, true);
		String storedVersionName = prefs.getString("VERSION_NAME_KEY", "");
		int storedVersionCode = prefs.getInt("VERSION_CODE_KEY", -1);
		WifiManager wifiManager = (WifiManager) KodakPrintMakerActivity.this
				.getSystemService(Context.WIFI_SERVICE);
		PrintHelper.ifRecommendWiFiSet = prefs.getBoolean("ifRecommendWiFiSet",
				true);
		if (PrintHelper.mLoggingEnabled)
			Log.i(TAG, "AppName:" + getResources().getString(R.string.app_name)
					+ " | Version Name:" + versionName + " | Version Code:"
					+ versionCode + " | Stored Version Name:"
					+ storedVersionName + " | Stored Version Code:"
					+ storedVersionCode);
		if (firstTimeRun) {
			// Set up default preference just in case Preferences isn't hit
			// first
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(firstTimeRunKey, false);
			editor.putBoolean("privacyAccepted", false);
			// editor.putBoolean(AppConstants.KEY_LOCALYTICS,
			// getResources().getBoolean(R.bool.localytics));
			// TBD before release - change default logging back to false
			editor.putBoolean("LOGGING_ENABLED_KEY", true);
			editor.putBoolean("WiFiEnabled", wifiManager.isWifiEnabled());
			editor.putBoolean("eulaAccepted", false);
			editor.putString("VERSION_NAME_KEY", versionName);
			editor.putInt("VERSION_CODE_KEY", versionCode);
			editor.putString("firstName", "");
			editor.putString("lastName", "");
			editor.putString("streetAddress", "");
			editor.putString("phone", "");
			editor.putString("email", "");
			editor.putString("storeAddress", "");
			editor.putString("city", "");
			editor.putString("state", "");
			editor.putString("zipcode", "");
			editor.putString("defaultStore", "");
			editor.putString("defaultSizeQuantity", "1");
			editor.putString("selectedStoreName", "");
			editor.putString("selectedStoreAddress", "");
			editor.putString("selectedStoreHours", "");
			editor.putString("selectedCity", "");
			editor.putString("selectedPostalCode", "");
			editor.putString("selectedStoreEmail", "");
			editor.putString("selectedStorePhone", "");
			editor.putBoolean("CellUsage", false);
			editor.putBoolean("sendOnlyTaggedImages", true);
			editor.putBoolean("ifRecommendWiFiSet", true); // if recommend user
															// changing phone
															// settings to wifi
															// , add by song
			editor.commit();
			if (PrintHelper.mLoggingEnabled)
				Log.i(TAG, "This is the first time "
						+ getResources().getString(R.string.app_name)
						+ " has been run");
			PrintHelper.setLocalyticsValue();
		} else {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("WiFiEnabled", wifiManager.isWifiEnabled());
			String packageName = KodakPrintMakerActivity.this.getApplicationContext().getPackageName();
			if(packageName.contains("wmc")){
				editor.putBoolean(AppConstants.KEY_LOCALYTICS, false);
				editor.putBoolean(NewSettingActivity.ENABLE_ALLOW_COOKIES, false);
				RSSLocalytics.closeLocalytics(KodakPrintMakerActivity.this);
			}
			editor.commit();
			if (PrintHelper.mLoggingEnabled)
				Log.i(TAG, "This is not the first time "
						+ getResources().getString(R.string.app_name)
						+ " has been run");
		}
	}

	String getVersionName() {
		String versionName = "";
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(),
					0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	int getVersionCode() {
		int versionCode = -1;
		try {
			versionCode = getPackageManager().getPackageInfo(getPackageName(),
					0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	private Handler doneLoading = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (PrintHelper.mLoggingEnabled)
					Log.d(TAG, "Loading took" + (2000 - sleepTime));
				// PrintHelper.albumsLoaded = true;
				mImageSelectionDatabase.close();
				/*
				 * if(Connection.isConnectedWifi(KodakPrintMakerActivity.this))
				 * { Intent intent = new Intent(KodakPrintMakerActivity.this,
				 * WiFiConnectionActivity.class); startActivity(intent);
				 * finish(); } else {
				 */
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(KodakPrintMakerActivity.this);
				Boolean eulaAccepted = prefs.getBoolean("eulaAccepted", false);
				Boolean privacySeen = prefs
						.getBoolean("privacyAccepted", false);
				Intent myIntent;
				boolean isWMC = PACKAGE_NAME.contains("wmc");
				boolean isDMC = PACKAGE_NAME
						.contains(MainMenu.DM_COMBINED_PACKAGE_NAME);
				boolean isPrintMaker = PACKAGE_NAME.contains("kodakprintmaker");
				if (isPrintMaker) {
					PrintHelper.wififlow = true;
				}
				// boolean isDMC = PACKAGE_NAME.contains("dm");
				if (isWMC) {
					PrintHelper.wififlow = true;
				}
				if (!eulaAccepted) {
					myIntent = new Intent(KodakPrintMakerActivity.this,
							EULAActivity.class);
					startActivity(myIntent);
					finish();
				} else {
					/*
					 * boolean isWMC = PACKAGE_NAME.contains("wmc"); if
					 * (!privacySeen && isWMC) { myIntent = new
					 * Intent(KodakPrintMakerActivity.this,
					 * PrivacyActivity.class); startActivity(myIntent);
					 * finish(); } else { myIntent = new
					 * Intent(KodakPrintMakerActivity.this, MainMenu.class);
					 * startActivity(myIntent); finish(); }
					 */
					if (isWMC) {
						if (privacySeen) {
							myIntent = new Intent(KodakPrintMakerActivity.this,
									WiFiSelectWorkflowActivity.class);
							// myIntent = new
							// Intent(KodakPrintMakerActivity.this,
							// MainMenu.class);
							startActivity(myIntent);
							finish();
						} else {
							myIntent = new Intent(KodakPrintMakerActivity.this,
									PrivacyActivity.class);
							startActivity(myIntent);
							finish();
						}
					} else if (isPrintMaker) {
						/* if(privacySeen){ */
						myIntent = new Intent(KodakPrintMakerActivity.this,
								WiFiSelectWorkflowActivity.class);
						startActivity(myIntent);
						finish();
						/*
						 * } else { myIntent = new
						 * Intent(KodakPrintMakerActivity.this,
						 * PrivacyActivity.class); startActivity(myIntent);
						 * finish(); }
						 */
					} else if (isDMC) {
						if (Connection.isNearKioskWifi(KodakPrintMakerActivity.this)) {
							myIntent = new Intent(KodakPrintMakerActivity.this,
									WiFiSelectWorkflowActivity.class);
						} else {
							myIntent = new Intent(KodakPrintMakerActivity.this,
									MainMenu.class);
						}
						
						startActivity(myIntent);
						finish();
					} else {
						if (Connection.isNearKioskWifi(KodakPrintMakerActivity.this)) {
							myIntent = new Intent(KodakPrintMakerActivity.this,
									WiFiSelectWorkflowActivity.class);
						} else {
							myIntent = new Intent(KodakPrintMakerActivity.this,
									MainMenu.class);
						}
						startActivity(myIntent);
						finish();
					}
				}
				// }
			} catch (Exception ex) {
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void newEULACheck() {
		String eulaURL = "";
		String storedEulaData = "";
		String currentEulaDate = "";
		String language = Locale.getDefault().toString();
		String brandStr = PrintHelper.getBrandForURL();
		String currentServer = PrintHelper.getServerURL(); //for RSSMOBILEPDC-1952

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(KodakPrintMakerActivity.this);
		storedEulaData = prefs.getString("STORED_EULA_DATE_KEY", "");
		
		try {
			eulaURL = "https://" + currentServer + "Mob/eula.aspx?" + brandStr+ "language="+language;
			Log.i(TAG, "eulaURL=" + eulaURL);
			HttpURLConnection conn = (HttpURLConnection) new URL(eulaURL)
					.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.setReadTimeout(10 * 1000);
			currentEulaDate = getCurrentEULADate(conn.getInputStream());
			if (!currentEulaDate.equals("unknown")) {
				if (!storedEulaData.equals(currentEulaDate)) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("STORED_EULA_DATE_KEY", currentEulaDate);
					editor.putBoolean("eulaAccepted", false);
					editor.commit();
				}
			}

			if (conn != null) {
				conn.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getCurrentEULADate(InputStream in) {

		String result = "unknown";
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		String s = sb.toString();
		Matcher m = mEULADatePattern.matcher(s);
		if (m.find()) {
			try {
				result = m.group();
				Log.i(TAG, result);
			} catch (IllegalStateException ise) {
				ise.printStackTrace();
			}
		}
		return result;
	}
	
	private void appForbidden() {
		Thread tokenThred = new Thread(new Runnable() {
			
			@Override
			public void run() {
				PrintMakerWebService service = new PrintMakerWebService(KodakPrintMakerActivity.this, "");
				PrintMakerWebService.mAuthorizationToken = service.getAuthorizationToken();			
			}
		});
		tokenThred.start();
	}

}