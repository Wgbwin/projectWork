package com.kodak.rss.tablet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.view.View;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.kodak.rss.RssApp;
import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.content.SearchStarterCategory;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.project.Project;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.CountryInfo;
import com.kodak.rss.core.n2r.bean.retailer.Retailer;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.core.n2r.bean.text.Font;
import com.kodak.rss.core.n2r.parse.Parse;
import com.kodak.rss.core.n2r.webservice.DataKey;
import com.kodak.rss.core.util.ConnectionUtil;
import com.kodak.rss.core.util.DeviceInfoUtil;
import com.kodak.rss.core.util.FileUtil;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.activities.BaseActivity;
import com.kodak.rss.tablet.activities.MainActivity;
import com.kodak.rss.tablet.activities.PrintsActivity;
import com.kodak.rss.tablet.activities.StartupActivity;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfos;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.OnProcessResponseEndListener;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.mobileapptracker.MobileAppTracker;


/**
 * Purpose:  for store the application level of the data
 * Author: Bing Wang 
 * Created Time: Aug 15, 2013 3:13:42 PM 
 */
public class RssTabletApp extends RssApp{
	private final static String TAG = "RssTabletApp";
	private final static String TEMP_FOLDER = "/.temp";
	
	/** this list for print one imageInfo maybe have some products */
	public volatile List<ImageInfo> chosenList = null;	
	
	/** this list for photobook one book is one productInfo */
	public volatile List<Photobook> chosenBookList = null;

	public List<ProductInfo> products = null;
	
	private ProductLayerLocalInfos productLayerLocalInfos = new ProductLayerLocalInfos() ;
	
	private HashMap<String, String> countries = null;
	private List<CountryInfo> countryInfoList=null;
	private List<Catalog> catalogList = null;

	public boolean isUseDoMore = false;
	public boolean skipMainFromShoppingCart = false;
	public boolean needGetPriceFromServer = true;
	public int orderType = 0;
	
	private boolean needShowCellularDataWarning = false;
	
	/**
	 * the country code current used
	 */
	private String countryCodeCurrentUsed =null;
	
	private CountryInfo countryInfo;
	private List<Retailer> retailers =null;
	private List<ColorEffect> colorEffectList =null;				
	
	private final String GLOBAL_VARIABLES_FILE_NAME = "global_variables";
	/** a flag for check if app is killed by system */
	public boolean isInited = false;
	/** before device connect to kiosk wifi, is wifi enabled, 
	 * if enabled, app need to enable wifi after kisok connect workflow finished */
	public boolean isWifiEnabledBeforeConnectKioskWifi = false;
		
	private List<Theme> themes = null;
	public List<Font> fonts = null;
	
	private Parse mParse;
	private String tempFolderPath;
	public int statusBarHeight;
	public List <Project> projects;
	
	public List<SearchStarterCategory> sSCategorys;	
	public List<GreetingCard> gCardList;		
	public List<Calendar> calendarList;	
	
	public List<Collage> collageList = null;
	
	/** If last order did not completed, then should remove all products from server with this id */
	private String lastFailedCartID = "";		
	public RSSTabletLocalytics localytics; 
	private String couponCode = "";
	
	public ImageUseURIDownloader imageDownloader;
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();	
	private OnProcessResponseEndListener onProcessResponseEndListener;	
	private onProcessImageResponseListener onResponseListener = new onProcessImageResponseListener() {		
		@Override
		public void onProcess(Response response, String profileId, View view,int position,String flowTpye,String productId) {										
			if (response == null || imageDownloader ==null) return;								
			ImageInfo imageInfo = null;	
			if (flowTpye.equals(FilePathConstant.bookType)) {
				Photobook chosePhotoBook = PhotoBookProductUtil.getPhotoBook(productId);
				if (chosePhotoBook == null) return;
				for (ImageInfo info : chosePhotoBook.chosenpics) {
					if (info != null && !info.isfromNative && profileId.equals(info.id)) {
						imageInfo =	info;
						break;																													
					}
				}
				if (imageInfo == null) return;
				if (imageInfo.id == null) return;	
				
				String facebookId = SharedPreferrenceUtil.getFacebookUserId(getApplicationContext());
				if (facebookId != null && !"".equals(facebookId) && imageInfo.id.equals(facebookId)) {
					if (response.getBitmap() == null && (imageInfo.imageThumbnailResource == null)) {
						synchronized (chosePhotoBook){
							chosePhotoBook.chosenpics.remove(imageInfo);
						}						
					}				
				}
			}else if (flowTpye.equals(FilePathConstant.printType)&& chosenList != null){				
				for (ImageInfo info : chosenList) {
					if (info != null && !info.isfromNative && profileId.equals(info.id)) {
						imageInfo =	info;
						break;																													
					}
				}
				if (imageInfo == null) return;
				if (imageInfo.id == null) return;		
			}						
			String originalPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, profileId, false);
			imageInfo.originalUrl= originalPath;
			imageInfo.editUrl = originalPath;			
			if (!(imageInfo.origHeight > 0 && imageInfo.origWidth > 0)) {
				imageInfo.uploadOriginalUrl = originalPath;			
			}					
			String thumbnailPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, profileId, true);
			imageInfo.thumbnailUrl = thumbnailPath;	
			
			if (flowTpye.equals(FilePathConstant.bookType)) {
				if (!(imageInfo.origHeight > 0 && imageInfo.origWidth > 0)) {
					imageInfo.uploadThumbnailUrl = thumbnailPath;		
				}					
			}
			
			if (flowTpye.equals(FilePathConstant.printType)){
				if (products != null) {
					for (ProductInfo pInfo : products) {
						if (pInfo != null && pInfo.chosenImageList!= null && AppConstants.printType.equals(pInfo.productType)) {
							if (pInfo.chosenImageList.get(0) != null && pInfo.chosenImageList.get(0).id.equals(imageInfo.id) ) {										
								pInfo.displayImageUrl = imageInfo.editUrl;
								if (!(imageInfo.origHeight > 0 && imageInfo.origWidth > 0)) {
									ImageUtil.calculateDefaultRoi(imageInfo, pInfo);	
								}							
							}	
						}												
					}	
				}
				if (onProcessResponseEndListener != null) {
					boolean isEdit = false;
					Activity activity = (BaseActivity) AppManager.getInstance().currentActivity();												
					if(activity instanceof PrintsActivity){
						isEdit = true;
					}
					onProcessResponseEndListener.onProcessEnd(imageInfo,isEdit);
				}				
			}
		}														
	};
	
	public static RssTabletApp getInstance() {
		return (RssTabletApp) app;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "app onCreate");
		
		tempFolderPath = getDataFolderPath() + TEMP_FOLDER;
		createDataFoldersIfNotExist();
		
		statusBarHeight = DeviceInfoUtil.getStatusHeight(getApplicationContext());
		mParse = new Parse();
		clearAllNetData();
		imageDownloader = new ImageUseURIDownloader(this,pendingRequests);	
		imageDownloader.setSaveType(FilePathConstant.externalType);		
		imageDownloader.setOnProcessImageResponseListener(onResponseListener);
		Thread.setDefaultUncaughtExceptionHandler(new AppCrashHandler());
		localytics = new RSSTabletLocalytics();
		if(isBrandApp()){
			initMobileAppTracker();
		}
		needShowCellularDataWarning = SharedPreferrenceUtil.isNeedShowCellularDataWarning(this);
	}
	
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
				String packName = RssTabletApp.getInstance().getPackageName();
				tracker.setPackageName(packName);
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
	
	private void createDataFoldersIfNotExist(){
		FileUtil.createDirIfNotExist(getDataFolderPath());
		FileUtil.createDirIfNotExist(getTempFolderPath());
		FileUtil.createDirIfNotExist(getTempImageFolderPath());
	}
	
	/**
	 * Get the temp folder path for app.
	 * This temp folder is  been cleared when app is exit 
	 */
	@Override
	public String getTempFolderPath() {
		return tempFolderPath;
	}
	
	@Override
	public String getTempImageFolderPath(){
		return tempFolderPath + "/.image";
	}
	
	public void setCountryCodeCurrentUsed(String countryCode) {
		this.countryCodeCurrentUsed = countryCode;
	}
	
	@Override
	public String getCountrycodeCurrentUsed() {
		return countryCodeCurrentUsed;
	}
	
	public String getCurrentLanguage(){
		return getResources().getConfiguration().locale.getLanguage();
	}
	
	public boolean isNeedShowCellularDataWarning(){
		return needShowCellularDataWarning;
	}
	
	public void setNeedShowCellularDataWarning(boolean needShowCellularDataWarning){
		this.needShowCellularDataWarning = needShowCellularDataWarning;
	}
	
	@Override
	public int[] getSizeForResize(int width, int height, String productType, String proDescriptionId) {
		if(getCatalogList() == null || getCatalogList().isEmpty()){
			return null;
		}
				
		final int DPI = AppConstants.PRODUCT_DPI;
		
		//change by bing wang for add MinImageSizeLongDim on 2014-12-19
		int max = 0;
		int minImageSizeLongDim = -1;
		for(Catalog catalog : getCatalogList()){
			if(catalog.rssEntries!= null){
				for(RssEntry entry : catalog.rssEntries){
					if(entry.proDescription!= null){
						if (productType != null && productType.equals(AppConstants.printType) ) {
							if (AppConstants.printType.equalsIgnoreCase(entry.proDescription.type.trim())) {
								int tmpMinImageSizeLongDim = entry.proDescription.getMinImageSizeLongDim();
								if(minImageSizeLongDim < tmpMinImageSizeLongDim){
									minImageSizeLongDim = tmpMinImageSizeLongDim;
								}	
							}
						}else if (proDescriptionId != null && proDescriptionId.equalsIgnoreCase(entry.proDescription.id)){
							minImageSizeLongDim = entry.proDescription.getMinImageSizeLongDim();
							if (minImageSizeLongDim != -1) break;
						}
						
						if (minImageSizeLongDim == -1) {
							if(entry.proDescription.pageWidth > max) max = entry.proDescription.pageWidth;
							if(entry.proDescription.pageHeight > max) max = entry.proDescription.pageHeight;
						}
					}
				}
			}
		}
		int maxSize = -1;
		if (minImageSizeLongDim != -1) {
			maxSize = minImageSizeLongDim;
		}else {
			maxSize = max * DPI;
		}
						
//		int max = 0;
//		for(Catalog catalog : getCatalogList()){
//			if(catalog.rssEntries!= null){
//				for(RssEntry entry : catalog.rssEntries){
//					if(entry.proDescription!= null){
//						if(entry.proDescription.pageWidth > max)
//							max = entry.proDescription.pageWidth;
//						if(entry.proDescription.pageHeight > max)
//							max = entry.proDescription.pageHeight;
//					}
//				}
//			}
//		}
//		int maxSize = max * DPI;
		
		if(width > height){
			if(height <= maxSize){
				return null;
			}
			
			return new int[]{ (int) ((double) width * maxSize / height), maxSize };
		} else {
			if(width <= maxSize){
				return null;
			}			
			return new int[] { maxSize, (int) ((double) maxSize * height / width) };
		}
		
	}
	
	public String getCountryNameCurrentUsed() {
		String countryName = "";
		if(countryCodeCurrentUsed !=null && getCountries()!=null && getCountries().containsKey(countryCodeCurrentUsed)){
			countryName = getCountries().get(countryCodeCurrentUsed);
		}
		return countryName;
	}
	
	public boolean isCountryCodeValid(String countryCode){
		boolean isValid = false;
		if(getCountries()==null || getCountries().size()==0){
			return isValid;
		}
		
		Iterator<Entry<String, String>> iter = getCountries().entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> entry = iter.next();
			if(entry.getKey().equalsIgnoreCase(countryCode)){
				isValid = true;
				break;
			}
		}
		return isValid;
	}
	
	public String getDefaultCountryCode(){
		String countryCode = "";
		if(getCountries()!=null && !getCountries().isEmpty()){
			countryCode = SharedPreferrenceUtil.currentCountryCode(this);
			if("".equals(countryCode) || !isCountryCodeValid(countryCode)){
				countryCode = SharedPreferrenceUtil.selectedCountryCode(this);
			}
		}
		
		if(isCountryCodeValid(countryCode)){
			return countryCode;
		}else{
			return "";
		}
	}
	
	public void clearTempImageFolder(){
		FileUtil.deleteAllFilesInFolder(getTempImageFolderPath());
	}
	
	public void clearDatasInCrash(){		
		clearLastCountryData();		
		deleteGlobalVaribalesCacheFile();
		projects = null;
		chosenList = null;		
		chosenBookList = null;
		products = null;
		gCardList = null;		
		calendarList = null;
		collageList = null;
		isInited = true;
		isUseDoMore = false;		
		RssTabletApp.getInstance().clearTempImageFolder();
		localytics = new RSSTabletLocalytics();		
	}
	
	public boolean isGloablVariablesRecyled(){
		return !isInited;
	}
	
	public void setOnProcessResponseEndListener(OnProcessResponseEndListener listener){
		this.onProcessResponseEndListener = listener;
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
		map.put("isInited", isInited);
		map.put("countries", countries);
		map.put("countryInfo", countryInfo);		
		map.put("themes", (Serializable) themes);
		map.put("countryInfoList", (Serializable) countryInfoList);
		map.put("chosenList", (Serializable) chosenList);
		map.put("catalogList", (Serializable) catalogList);
		map.put("retailers", (Serializable) retailers);
		map.put("colorEffectList", (Serializable) colorEffectList);
		map.put("countryCodeCurrentUsed", countryCodeCurrentUsed);
		map.put("fonts", (Serializable)fonts);
		map.put("chosenBookList", (Serializable) chosenBookList);
		map.put("products", (Serializable) products);
		map.put("needShowCellularDataWarning", needShowCellularDataWarning);
		map.put("productLayerLocalInfos", productLayerLocalInfos);
		map.put("sSCategorys",(Serializable)sSCategorys);	
		map.put("gCards",(Serializable)gCardList);
		
		map.put("calendars",(Serializable)calendarList);
		map.put("collages",(Serializable)collageList);
		
		saveObject(map, filename);
	}
	
	private boolean restoreGlobalVariables(String filename){
		Log.i(TAG, "restoreGlobalVariables");
		try {
			Object object = readObject(filename);
			
			if(object == null){
				return false;
			}
			
			HashMap<String, Serializable> map = (HashMap<String, Serializable>) object;
			try {
				isInited = (Boolean) map.get("isInited");
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				countries = (HashMap<String, String>) map.get("countries");
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				countryInfo = (CountryInfo) map.get("countryInfo");
			} catch (Exception e) {Log.e(TAG, e);	}			
			try {
				themes = (List<Theme>) map.get("themes");
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				countryInfoList = (List<CountryInfo>) map.get("countryInfoList");
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				chosenList = (List<ImageInfo>) map.get("chosenList");
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				catalogList = (List<Catalog>) map.get("catalogList");
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				retailers = (List<Retailer>) map.get("retailers");
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				colorEffectList = (List<ColorEffect>) map.get("colorEffectList");
			} catch (Exception e) {Log.e(TAG, e);	}						
			try {
				countryCodeCurrentUsed =  (String) map.get("countryCodeCurrentUsed");
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				fonts =  (List<Font>) map.get("fonts");
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				chosenBookList = (List<Photobook>) map.get("chosenBookList") ;
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				products = (List<ProductInfo>) map.get("products") ;
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				needShowCellularDataWarning = (Boolean) map.get("needShowCellularDataWarning") ;
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				productLayerLocalInfos = (ProductLayerLocalInfos) map.get("productLayerLocalInfos") ;
			} catch (Exception e) {Log.e(TAG, e);	}
			try {
				sSCategorys = (List<SearchStarterCategory>) map.get("sSCategorys") ;
			} catch (Exception e) {Log.e(TAG, e);	}			
			try {
				gCardList =  (List<GreetingCard>) map.get("gCards") ;
			} catch (Exception e) {Log.e(TAG, e);	}
			
			try {
				calendarList =  (List<Calendar>) map.get("calendars") ;
			} catch (Exception e) {Log.e(TAG, e);	}
			
			try {
				collageList =  (List<Collage>) map.get("collages") ;
			} catch (Exception e) {Log.e(TAG, e);	}
			
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
	
	public boolean saveObject(Serializable ser, String filename){
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = openFileOutput(filename, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "save object("+ ser.toString()+") fail", e);
			return false;
		} finally {
			if(oos != null){
				try{
					oos.close();
				}catch(Exception e){
					Log.e(TAG,e);
				}
			}
			if(fos != null){
				try{
					fos.close();
				}catch(Exception e){
					Log.e(TAG,e);
				}
			}
		}
	}
	
	public Serializable readObject(String filename){
		if(!getFileStreamPath(filename).exists()){
			return null;
		}
		
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = openFileInput(filename);
			ois = new ObjectInputStream(fis);
			return (Serializable) ois.readObject();
		} catch (Exception e) {
			Log.e(TAG, "read object fail" ,e);
			//delete cache file if fail
			if(e instanceof InvalidClassException){
				File data = getFileStreamPath(filename);
				data.delete();
			}
			return null;
		} finally {
			if(ois != null){
				try {
					ois.close();
				} catch (Exception e2) {
					Log.e(TAG, e2);
				}
			}
			if(fis != null){
				try {
					fis.close();
				} catch (Exception e2) {
					Log.e(TAG, e2);
				}
			}
		}
	}
	
	public Retailer getCurrentRetailer(Context context){
		Retailer currentRetailer = null;
		String currentRetailerID = SharedPreferrenceUtil.getString(context,SharedPreferrenceUtil.SELECTED_RETAILER_ID);
		List<Retailer> retailers = getRetailers();
		if(retailers != null && !currentRetailerID.equals("")){
			for(Retailer retailer : retailers){
				if(retailer.id.equals(currentRetailerID)){
					return retailer;
				}
			}
		}
		return currentRetailer;
	}
		
	/**
	 * when country changed or the app started, this function should be called.
	 */
	public void clearAllNetData(){
		SharedPreferrenceUtil.setString(getApplicationContext(), DataKey.CATALOGS, "");
		SharedPreferrenceUtil.setString(getApplicationContext(), DataKey.RETAILERS, "");
		SharedPreferrenceUtil.setString(getApplicationContext(), DataKey.COLOR_EFFECTS, "");
		SharedPreferrenceUtil.setString(getApplicationContext(), DataKey.THEMES, "");
		SharedPreferrenceUtil.setString(getApplicationContext(), DataKey.COUNTRIES, "");
		SharedPreferrenceUtil.setString(getApplicationContext(), DataKey.COUNTRY_INFOS, "");
	}
	
	public List<Catalog> getCatalogList() {
		String data = SharedPreferrenceUtil.getString(getApplicationContext(), DataKey.CATALOGS);
		if(!"".equals(data)){
			try {
				catalogList = mParse.parseCatalogs(data);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
			}
		}
		return catalogList;
	}
	
	public void clearCatalogList(){
		SharedPreferrenceUtil.setString(getApplicationContext(), DataKey.CATALOGS, "");
		if(catalogList!=null){
			catalogList.clear();
		}
	}

	public void setCatalogList(List<Catalog> catalogList) {
		this.catalogList = catalogList;
	}

	public List<Retailer> getRetailers() {
		if(retailers == null){
			String data = SharedPreferrenceUtil.getString(getApplicationContext(), DataKey.RETAILERS);
			if(!"".equals(data)){
				try {
					retailers = mParse.parseRetailers(data);
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
			}
		}
		return retailers;
	}

	public void setRetailers(List<Retailer> retailers) {
		this.retailers = retailers;
	}

	public List<ColorEffect> getColorEffectList() {
		if(colorEffectList == null){
			String data = SharedPreferrenceUtil.getString(getApplicationContext(), DataKey.COLOR_EFFECTS);
			if(!"".equals(data)){
				try {
					colorEffectList = mParse.parseColorEffects(data);
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
			}
		}
		return colorEffectList;
	}

	public void setColorEffectList(List<ColorEffect> colorEffectList) {
		this.colorEffectList = colorEffectList;
	}

	public List<Theme> getThemes() {
		if(themes == null){
			String data = SharedPreferrenceUtil.getString(getApplicationContext(), DataKey.THEMES);
			if(!"".equals(data)){
				try {
					themes = mParse.parseThemes(data);
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
			}
		}
		return themes;
	}

	public void setThemes(List<Theme> themes) {
		this.themes = themes;
	}

	public HashMap<String, String> getCountries() {
		if(countries == null){
			String data = SharedPreferrenceUtil.getString(getApplicationContext(), DataKey.COUNTRIES);
			if(!"".equals(data)){
				try {
					countries = mParse.parseCountries(data);
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
			}
		}
		return countries;
	}

	public void setCountries(HashMap<String, String> countries) {
		this.countries = countries;
	}

	public List<CountryInfo> getCountryInfoList() {
		if(countryInfoList == null){
			String data = SharedPreferrenceUtil.getString(getApplicationContext(), DataKey.COUNTRY_INFOS);
			if(!"".equals(data)){
				try {
					countryInfoList = mParse.parseCountryInfo(data);
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
			}
		}
		return countryInfoList;
	}

	public void setCountryInfoList(List<CountryInfo> countryInfoList) {
		this.countryInfoList = countryInfoList;
	}

	public CountryInfo getCountryInfo(String countryCode) {
		List<CountryInfo> countryInfos = getCountryInfoList();
		if(countryInfos != null){
			for(CountryInfo ci : countryInfos){
				if(ci.countryCode.equalsIgnoreCase(countryCode)){
					countryInfo = ci;
					break;
				}
			}
		}
		return countryInfo;
	}

	public void setCountryInfo(CountryInfo countryInfo) {
		this.countryInfo = countryInfo;
	}
	
	/**
	 * Some samsung device can not connect to kiosk wifi if  DisablePoorNetworkAvoidance is enabled.
	 * @return
	 */
	public boolean isNeedDisablePoorNetworkAvoidance(){
		return VERSION.SDK_INT >= 18 
				&& ConnectionUtil.isPoorNetworkAvoidanceEnabled(this)
				&& "samsung".equalsIgnoreCase(android.os.Build.MANUFACTURER);
	}
	
	public void startOver(){
		projects = null;
		chosenList = null;		
		chosenBookList = null;
		products = null;		
		localytics = new RSSTabletLocalytics();
		isUseDoMore = false;
		productLayerLocalInfos = new ProductLayerLocalInfos();		
		gCardList = null;		
		calendarList = null;
		collageList = null;
		couponCode = "";
		clearCatalogData();
		AppManager.getInstance().goToHomeActivity();
	}
	
	private void clearCatalogData(){
		if(catalogList!=null){
			catalogList.clear();
			catalogList = null;
			SharedPreferrenceUtil.setString(getApplicationContext(), DataKey.CATALOGS, "");
		}
	}
	
	/**
	 * each time when selected country changed, then call this method to clear all data which based on previous country
	 */
	public void clearLastCountryData(){	
		// add by bing wang for clear old CLO
		SharedPreferrenceUtil.setBoolean(getApplicationContext(), SharedPreferrenceUtil.ACCEPT_CLOLITE, false);				
		clearCatalogData();
		if(retailers != null){
			retailers.clear();
			retailers = null;
			SharedPreferrenceUtil.setString(getApplicationContext(), DataKey.RETAILERS, "");
		}
		if(colorEffectList != null){
			colorEffectList.clear();
			colorEffectList = null;
			SharedPreferrenceUtil.setString(getApplicationContext(), DataKey.COLOR_EFFECTS, "");
		}
		
		if(themes != null){
			themes.clear();
			themes = null;
			SharedPreferrenceUtil.setString(getApplicationContext(), DataKey.THEMES, "");
		}
		
		if(fonts != null){
			fonts.clear();
			fonts = null;
		}
	}
	
	public ProductLayerLocalInfos getProductLayerLocalInfos(){
		return productLayerLocalInfos;
	}
	
	public String getHomeDeliveryRetailerId(List<Retailer> retailers){
		String retailerId = "";
		if(retailers != null){
			for(Retailer retailer : retailers){
				if(retailer.shipToHome){
					retailerId = retailer.id;
					break;
				}
			}
		}
		return retailerId;
	}
	
	public boolean isFacebookLogin(){
		String access_token = SharedPreferrenceUtil.getFacebookToken(this);
        long expires = SharedPreferrenceUtil.getFacebookAccessExpires(this);      
        if (access_token != null &&((expires == 0) ||(System.currentTimeMillis() < expires))) {
        	return true;
        } 	
		return false;
	}
	
	public boolean isFacebookCanLogout(){
		Activity activity = AppManager.getInstance().currentActivity();
		if((activity instanceof MainActivity || activity instanceof StartupActivity ) && !isUseDoMore){
			return true;
		}
		return false;
	}

	public String getLastFailedCartID() {
		return lastFailedCartID==null?"":lastFailedCartID;
	}

	public void setLastFailedCartID(String lastFailedCartID) {
		this.lastFailedCartID = lastFailedCartID;
	}
	
	public String getCouponCode() {
		if(couponCode == null){
			couponCode = "";
		}
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public int getQuantityIncrement(String desId){
		int quantityIncrement = 1;
		String quantityIncrementStr = "";
		List<Catalog> catalogs =  RssTabletApp.getInstance().getCatalogList();
		for(Catalog catalog : catalogs){
			RssEntry entry = catalog.getProductEntry(desId);
			if (entry !=null && entry.proDescription.attributes !=null){
				quantityIncrementStr = entry.proDescription.attributes.get(AppConstants.QUANTITY_INCREMENT);
				if(quantityIncrementStr!=null && !quantityIncrementStr.equals("")){
					quantityIncrement =Integer.valueOf(quantityIncrementStr);
					break;
				}
			}
		}
		return quantityIncrement;
	}
	
	public boolean isProductAvaible(String productType1, String productType2){
		List<Catalog> catalogs = getCatalogList();
		if(catalogs != null){
			for(Catalog catalog : catalogs){
				if(catalog.getProducts(productType1).size()>0 || catalog.getProducts(productType2).size()>0){
					return true;
				}
			}
		}
		return false;
	}
	
	public String getEulaURL(){
		String url = "";
		String packageName = getPackageName();
		String language = Locale.getDefault().getLanguage();
		if(packageName.equals("com.kodak.rss.mkmhd")){
			url = "https://" + getCulumusServer() + "/mob/eula.aspx?language=" + language;
		} else if(packageName.equals("com.kodak.rss.dmhd")){
			url =  "https://" + getCulumusServer() + "mob/eula.aspx?brand=dm&language=" + language;
		}
		return url;
	}
	
	public String getPrivacyURL(){
		String url = "";
		String packageName = getPackageName();
		String language = Locale.getDefault().getLanguage();
		if(packageName.equals("com.kodak.rss.mkmhd")){
			url = "https://" + getCulumusServer() + "/mob/privacy.aspx?language=" + language;
		} else if(packageName.equals("com.kodak.rss.dmhd")){
			url =  "https://" + getCulumusServer() + "mob/privacy.aspx?brand=dm&language=" + language;
		}
		return url;
	}
}
