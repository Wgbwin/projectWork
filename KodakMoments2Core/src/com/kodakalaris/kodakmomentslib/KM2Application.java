package com.kodakalaris.kodakmomentslib;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.kodakalaris.kodakmomentslib.AppConstants.FlowType;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig;
import com.kodakalaris.kodakmomentslib.culumus.bean.imageedit.ColorEffect;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.Catalog;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.CountryInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo;
import com.kodakalaris.kodakmomentslib.culumus.parse.Parse;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.KioskManager;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;
import com.kodakalaris.kodakmomentslib.util.DataPersistenceUtil;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;
import com.mobileapptracker.MobileAppTracker;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class KM2Application extends Application {
	private static final String TAG = "KM2Application";
	private static final String TEMP_FOLDER = "/temp/.kodak";
	private static final String GLOBAL_VARIABLES_FILE_NAME = "KM2_GLOBAL_VARIABLES";

	/** a flag for check if app is killed by system */
	private boolean mHaveNotBeenRecyled = false;
	
	private static KM2Application app;
	private int mScreenH;
	private int mScreenW;
	private boolean scanSDCard = false;
	private FlowType mFlowType;
	
	private LinkedHashMap<String, String> mCountries;
	private List<CountryInfo> mCountryInfos;
	private String mCountryCodeUsed;
	private List<Catalog> mCatalogs;
	private List<ColorEffect> mColorEffects;
	
	private boolean mIsTablet;
	public boolean isAppObsolete = false;
	
	public static KM2Application getInstance() {
		return app;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		
		Thread.setDefaultUncaughtExceptionHandler(new AppCrashHandler());
		
		// TODO: if only some Applications need to add this function, there need a judgment. Added by Kane
		initMobileAppTracker();
		initImageLoader(getApplicationContext());
		clearAllCumulusData();
		
		String lastSelectedCountry = SharedPreferrenceUtil.getString(this, DataKey.LAST_SELECTED_COUNTRY);
		// if country is changed, the data which have to be removed should added here
		if(!"".equals(lastSelectedCountry)){
			setCountryCodeUsed(lastSelectedCountry);
			SharedPreferrenceUtil.setString(this, DataKey.LAST_SELECTED_COUNTRY, "");
			KMConfig.clearProperties(this);
			StoreInfo.clearSelectedStore(this);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.CUPCAKE)
	private void initMobileAppTracker(){
		MobileAppTracker.init(this, getString(R.string.mobileAppTracker_advertiser_id), getString(R.string.mobileAppTracker_conversion_key));
		final MobileAppTracker tracker = MobileAppTracker.getInstance();
		new Thread(){
			public void run() {
				Info adInfo = null;
				String id = "";
				try {
					adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
					tracker.setGoogleAdvertisingId(adInfo.getId(), adInfo.isLimitAdTrackingEnabled());
				} catch (IOException e) {
					id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
				} catch (GooglePlayServicesNotAvailableException e) {
					id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
				} catch (IllegalStateException e) {
					id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
				} catch (GooglePlayServicesRepairableException e) {
					id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
				}
				tracker.setPackageName(getPackageName());
				if(id != null && !"".equals(id)){
					tracker.setAndroidId(id);
				}
				String deviceId = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
				tracker.setDeviceId(deviceId);
				try {
				    WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				    tracker.setMacAddress(wm.getConnectionInfo().getMacAddress());
				} catch (NullPointerException e) {
				}
				tracker.setAppAdTrackingEnabled(true);
			};
		}.start();
	}
	
	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		  .showImageOnLoading(R.drawable.imagewait96x96)
		  .showImageForEmptyUri(R.drawable.imageerror)
		  .showImageOnFail(R.drawable.imageerror)
		  .cacheInMemory(true)
		  .cacheOnDisk(true)
		  .considerExifParams(true)
		  .bitmapConfig(Bitmap.Config.RGB_565)
		  .displayer(new FadeInBitmapDisplayer(500))
		  .imageScaleType(ImageScaleType.EXACTLY)
		  .build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024) // 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.memoryCache(new LRULimitedMemoryCache(5 * 1024 * 1024))
				.memoryCacheExtraOptions(400, 400)
			    .defaultDisplayImageOptions(options)
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
	
	public int getScreenH() {
		return mScreenH;
	}

	public void setScreenH(int screenH) {
		this.mScreenH = screenH;
	}

	public int getScreenW() {
		return mScreenW;
	}

	public void setScreenW(int screenW) {
		this.mScreenW = screenW;
	}

	/*public String getCountrycodeCurrentUsed() {
		// TODO hard code
		return "us";
	}*/
	
	public boolean isIsTablet() {
		return mIsTablet;
	}

	public void setIsTablet(boolean mIsTablet) {
		this.mIsTablet = mIsTablet;
	}

	public String getDataFolderPath(){
		return getExternalFilesDir(null).getAbsolutePath();
	}
	
	public String getTempFolderPath(){
		return getDataFolderPath() + TEMP_FOLDER;
	}
	
	public String getTempImageFolderPath(){
		return getTempFolderPath() + "/.image";
	}
	
	//##################### Save/Restore global values #####################//
	public boolean isGloablVariablesRecyled(){
		return !mHaveNotBeenRecyled;
	}

	public void setHaveNotBeenRecyled(boolean mHaveNotBeenRecyled) {
		this.mHaveNotBeenRecyled = mHaveNotBeenRecyled;
	}
	
	public synchronized void saveGlobalVariables(){
		saveGlobalVariables(GLOBAL_VARIABLES_FILE_NAME);
	}
	
	public boolean restoreGlobalVariables(){
		return restoreGlobalVariables(GLOBAL_VARIABLES_FILE_NAME);
	}
	
	private void saveGlobalVariables(String filename){
		Log.i(TAG, "Save globalVariables");
		HashMap<String,Serializable> map = new HashMap<String, Serializable>();
		//put the global varibales in the map
		//please avoid duplicate key, it is recommend to use the variable name as the map key.
		//if you save it here, you should restore it in restoreGlobalVariables()
		//Note: you can save it in another way, but you need to restore it in restoreGlobalVariables().
		map.put("mHaveNotBeenRecyled", mHaveNotBeenRecyled);
		map.put("mScreenH", mScreenH);
		map.put("mScreenW", mScreenW);
		map.put("scanSDCard", scanSDCard);
		map.put("mFlowType", mFlowType);
		map.put("mCountries", mCountries);
		map.put("mCountryInfos", (Serializable) mCountryInfos);
		map.put("mCountryCodeUsed", mCountryCodeUsed);
		map.put("mCatalogs", (Serializable) mCatalogs);
		map.put("mColorEffects", (Serializable) mColorEffects);
		map.put("mIsTablet", mIsTablet);
		map.put("isAppObsolete", isAppObsolete);
		
		KioskManager.getInstance().saveGlobalVariables(map);
		PrintManager.getInstance(this).saveGlobalVariables(map);
		ShoppingCartManager.getInstance().saveGlobalVariables(map);
		PrintHubManager.getInstance().saveGlobalVariables(map);
		
		DataPersistenceUtil.saveObject(this, map, filename);
	}
	
	@SuppressWarnings("unchecked")
	private boolean restoreGlobalVariables(String filename){
		Log.i(TAG, "restoreGlobalVariables");
		try {
			Object object = DataPersistenceUtil.readObject(this, filename);
			
			if(object == null){
				return false;
			}
			
			HashMap<String, Serializable> map = (HashMap<String, Serializable>) object;
			try {
				mHaveNotBeenRecyled = (Boolean) map.get("mHaveNotBeenRecyled");
			} catch (Exception e) { Log.e(TAG, e); }	
			try {
				mScreenH = (Integer) map.get("mScreenH");
			} catch (Exception e) { Log.e(TAG, e); }	
			try {
				mScreenW = (Integer) map.get("mScreenW");
			} catch (Exception e) { Log.e(TAG, e); }	
			try {
				scanSDCard = (Boolean) map.get("scanSDCard");
			} catch (Exception e) { Log.e(TAG, e); }	
			try {
				mFlowType = (FlowType) map.get("mFlowType");
			} catch (Exception e) { Log.e(TAG, e); }	
			try {
				mCountries = (LinkedHashMap<String, String>) map.get("mCountries");
			} catch (Exception e) { Log.e(TAG, e); }	
			try {
				mCountryInfos = (List<CountryInfo>) map.get("mCountryInfos");
			} catch (Exception e) { Log.e(TAG, e); }	
			try {
				mCountryCodeUsed = (String) map.get("mCountryCodeUsed");
			} catch (Exception e) { Log.e(TAG, e); }	
			try {
				mCatalogs = (List<Catalog>) map.get("mCatalogs");
			} catch (Exception e) { Log.e(TAG, e); }	
			try {
				mColorEffects = (List<ColorEffect>) map.get("mColorEffects");
			} catch (Exception e) { Log.e(TAG, e); }	
			try {
				mIsTablet = (Boolean) map.get("mIsTablet");
			} catch (Exception e) { Log.e(TAG, e); }	
			try {
				isAppObsolete = (Boolean) map.get("isAppObsolete");
			} catch (Exception e) { Log.e(TAG, e); }	
			
			KioskManager.getInstance().restoreGlobalVariables(map);
			PrintManager.getInstance(this).restoreGlobalVariables(map);
			ShoppingCartManager.getInstance().restoreGlobalVariables(map);
			PrintHubManager.getInstance().restoreGlobalVariables(map);
			
			return true;
		} catch (Exception e) {
			Log.e(TAG,"restoreGlobalVariables fail",e);
			return false;
		}
	}
	
	/**
	 * only used for develop
	 * Don't use this method in version released
	 * @return
	 */
	public synchronized void devSaveGlobalVariables(){
		saveGlobalVariables(GLOBAL_VARIABLES_FILE_NAME+"_dev");
	}
	
	/**
	 * only used for develop
	 * Don't use this method in version released
	 * @return
	 */
	public boolean devRestoreGlobalVariables(){
		return restoreGlobalVariables(GLOBAL_VARIABLES_FILE_NAME+"_dev");
	}
	
	
	public void deleteGlobalVaribalesCacheFile(){
		File file = getFileStreamPath(GLOBAL_VARIABLES_FILE_NAME);
		if(file.exists() && file.delete()){
			Log.i(TAG, "cache file for global values is deleted");
		}
	}
	
	/**
	 * After global variables is changed, please invoke this method.
	 */
	public void notifyGlobalDateSetChanged(){
		if(AppManager.getInstance().isAppInBackground()){
			saveGlobalVariables();
		}
	}
	//##################### End for Save/Restore global values #####################//

	public boolean isScanSDCard() {
		return scanSDCard;
	}

	public void setScanSDCard(boolean scanSDCard) {
		this.scanSDCard = scanSDCard;
	}
	
	public FlowType getFlowType() {
	 if (mFlowType ==null){
	   mFlowType = FlowType.PRINT;
	   }
	   return mFlowType;
	}
	public void setFlowType(FlowType mFlowType) {
		this.mFlowType = mFlowType;
	}
	
	public LinkedHashMap<String, String> getCountries(){
		if(mCountries == null){
			String data = SharedPreferrenceUtil.getString(getApplicationContext(), DataKey.COUNTRIES);
			if(!"".equals(data)){
				try {
					Parse parse = new Parse();
					mCountries = parse.parseCountries(data);
				} catch (WebAPIException e) {
					e.printStackTrace();
				}
			}
		}
		return mCountries;
	}
	
	public List<CountryInfo> getCountryInfoList() {
		if(mCountryInfos == null){
			String data = SharedPreferrenceUtil.getString(getApplicationContext(), DataKey.COUNTRY_INFOS);
			if(!"".equals(data)){
				try {
					Parse parse = new Parse();
					mCountryInfos = parse.parseCountryInfo(data);
				} catch (WebAPIException e) {
					e.printStackTrace();
				}
			}
		}
		return mCountryInfos;
	}
	
	public CountryInfo getCountryInfo(){
		final List<CountryInfo> countryInfos = getCountryInfoList();
		if(countryInfos != null && countryInfos.size()>0){
			return countryInfos.get(0);
		}
		return null;
	}

	public String getCountryCodeUsed() {
		if(mCountryCodeUsed == null || mCountryCodeUsed.equals("")){
			String countryCode = SharedPreferrenceUtil.selectedCountryCode(this);
			if(!countryCode.equals("")){
				mCountryCodeUsed = countryCode;
			} else {
				countryCode = SharedPreferrenceUtil.currentCountryCode(this);
				if(!countryCode.equals("")) {
					mCountryCodeUsed = countryCode;
				}
			}
		}
		return mCountryCodeUsed;
	}

	public void setCountryCodeUsed(String mCountryCodeUsed) {
		SharedPreferrenceUtil.saveSelectedCountryCode(this, mCountryCodeUsed);
		this.mCountryCodeUsed = mCountryCodeUsed;
	}
	
	public List<Catalog> getCatalogs() {
		String data = SharedPreferrenceUtil.getString(getApplicationContext(), DataKey.CATALOGS);
		if(mCatalogs == null && !"".equals(data)){
			try {
				Parse parse = new Parse();
				String types = getString(R.string.cumulus_support_products);
				mCatalogs = parse.parseCatalogs(types, data);
			} catch (WebAPIException e) {
				e.printStackTrace();
			}
		}
		return mCatalogs;
	}
	
	public void setCatalogs(List<Catalog> catalogs) {
		mCatalogs = catalogs;
	}
	
	public List<ColorEffect> getColorEffects() {
		if(mColorEffects == null){
			String data = SharedPreferrenceUtil.getString(getApplicationContext(), DataKey.COLOR_EFFECTS);
			if(!"".equals(data)){
				try {
					Parse parse = new Parse();
					mColorEffects = parse.parseColorEffects(data);
				} catch (WebAPIException e) {
					e.printStackTrace();
				}
			}
		}
		return mColorEffects;
	}
	
	/**
	 * used for clear the cumulus data which are stored in SharePreferences.
	 */
	public void clearAllCumulusData(){
		SharedPreferrenceUtil.setString(this, DataKey.CATALOGS, "");
		SharedPreferrenceUtil.setString(this, DataKey.COLOR_EFFECTS, "");
		SharedPreferrenceUtil.setString(this, DataKey.COUNTRY_INFOS, "");
		SharedPreferrenceUtil.setString(this, DataKey.COUNTRIES, "");
	}
	
	/**
	 * get the product description by product type
	 * @param productType 
	 * @return
	 */
	public List<RssEntry> getProductsByType(String productType){
		List<RssEntry> products = null;
		
		if(getCatalogs() != null && getCatalogs().size() > 0) {
			products =getCatalogs().get(0).getProducts(productType);
		}
		return products;
	}
	
	public void startOver() {
		mFlowType = null;
	}
}
