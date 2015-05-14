package com;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.TextView;

import com.AppConstants.FlowType;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.ColorEffect;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PrintInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductLayerLocalInfos;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.kodak_kioskconnect_n2r.bean.text.Font;
import com.kodak.kodak_kioskconnect_n2r.bean.text.TextBlock;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.mobileapptracker.MobileAppTracker;

/**
 * save the global variable, do not add the final static variable.
 * 
 * @author song
 * @version 1.0
 * @created 2014-3-25
 */
public class AppContext extends Application {
	private List<ProductInfo> productInfos;
	private List<List<ProductInfo>> childItemList;
	private List<String> groupItemList;
	// This List is used for store all photobooks
	private List<Photobook> photobooks;
	private List<GreetingCardManager> mGreetingCardManagers;
	private List<PrintInfo> mPrints ;
	private List<PhotoInfo> mTempSelectedPhotos ;
	private List<PhotoInfo> uploadSucceedImages;
	private List<Font> fonts = null;
	private Vector<PhotoInfo> mUploadPhotoList ;
	// This photobook is the current edited photobook
	private Photobook photobook;	
	private boolean isContinueShopping = false;	
	private boolean isEditGreetingCart = false;	
	private FlowType flowType;
	private static AppContext appContex;
	private File kodakTempPictureWeb ;
	private boolean isFacebookGroupHinted;
	private boolean brandedApp = true;
	private boolean screenOrientationFlag = true;
	
	private List<String> mErrorBitmapIds ;
	
	private String facebook_photos_of_you_cover ;
	private String facebook_your_photos_cover ;
	private String facebook_your_friends_cover ;
	
	private List<Collage> collages;
	
	private ProductLayerLocalInfos productLayerLocalInfos = new ProductLayerLocalInfos() ;
	private List<ColorEffect> colorEffects;
	private String tempFolderPath;
	private String TEMP_FOLDER = "/.temp";
	private TextBlock textBlock;
	
	@Override
	public void onCreate() {
		super.onCreate();
		appContex = this;
		mTempSelectedPhotos = new ArrayList<PhotoInfo>() ;
		mUploadPhotoList = new Vector<PhotoInfo>() ;
		uploadSucceedImages = new ArrayList<PhotoInfo>();
		textBlock = new TextBlock();
		getExternalKodakTempPictureWeb() ;
//		Settings.setPlatformCompatibilityEnabled(true);
		String packName = getPackageName();
		if(packName.contains("com.kodak.kodakprintmaker") || 
		   packName.contains("com.kodak.kodak.rsscombinedapp")){
			brandedApp = true;
		} else {
			brandedApp = false;
		}
		tempFolderPath = getDataFolderPath() + TEMP_FOLDER;
		if(packName.equals(AppConstants.KODAK_MY_KODAK_MOMENTS)){
			initMobileAppTracker();
		}
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
				String packName = getPackageName();
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
	
	public File getExternalKodakTempPictureWeb(){
		kodakTempPictureWeb = ImageCache.getDiskCacheDir(getApplicationContext(), AppConstants.KODAK_TEMP_PICTURE_WEB) ;
//		kodakTempPictureWeb = new File(getExternalCacheDir() , KODAK_TEMP_PICTURE_WEB) ;
		if(!kodakTempPictureWeb.exists()){
			kodakTempPictureWeb.mkdirs() ;
		}
		return kodakTempPictureWeb ;
		
	}
	
	public String getTempImageFolderPath(){
		return tempFolderPath + "/.image";
	}
	public String getDataFolderPath(){
		return getExternalFilesDir(null).getAbsolutePath();
	}

	
	public ProductLayerLocalInfos getProductLayerLocalInfos(){
		return productLayerLocalInfos;
	}
	
	
	public List<ColorEffect> getColorEffects() {
		return colorEffects;
	}

	public void setColorEffects(List<ColorEffect> colorEffects) {
		this.colorEffects = colorEffects;
	}

	public static AppContext getApplication() {
		return appContex;
	}

	public List<ProductInfo> getProductInfos() {
		return productInfos;
	}

	public void setProductInfos(List<ProductInfo> productInfos) {
		this.productInfos = productInfos;
	}

	public List<List<ProductInfo>> getChildItemList() {
		return childItemList;
	}

	public void setChildItemList(List<List<ProductInfo>> childItemList) {
		this.childItemList = childItemList;
	}

	public List<String> getGroupItemList() {
		return groupItemList;
	}

	public void setGroupItemList(List<String> groupItemList) {
		this.groupItemList = groupItemList;
	}

	public FlowType getFlowType() {
		if (flowType ==null){
			flowType = FlowType.PRINT;
		}
		return flowType;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}
	
	public List<PhotoInfo> getmUploadPhotoList() {
		if(mUploadPhotoList==null){
			mUploadPhotoList = new Vector<PhotoInfo>() ;
		}
		return mUploadPhotoList;
	}

	public void setmUploadPhotoList(Vector<PhotoInfo> mUploadPhotoList) {
		this.mUploadPhotoList = mUploadPhotoList;
	}

	
	public List<PhotoInfo> getmTempSelectedPhotos() {
		if(mTempSelectedPhotos == null){
			mTempSelectedPhotos = new ArrayList<PhotoInfo>();
		}
		return mTempSelectedPhotos;
	}

	public void setmTempSelectedPhotos(List<PhotoInfo> mTempSelectedPhotos) {
		this.mTempSelectedPhotos = mTempSelectedPhotos;
	}
	
		public Photobook getPhotobook() {
		if(null == photobook){
			photobook = new Photobook();
	}
		return photobook;
	}

	public void setPhotobook(Photobook photobook) {
		this.photobook = photobook;
	}

	public List<Photobook> getPhotobooks() {
		if(photobooks == null){
			photobooks = new ArrayList<Photobook>();
		}
		return photobooks;
	}

	public void setPhotobooks(List<Photobook> photobooks) {
		this.photobooks = photobooks;
	}
	
	public void deletePhotobook(Photobook photobook){
		if(photobook.selectedImages != null && photobook.selectedImages.size()>0){
			for(PhotoInfo photoInfo : photobook.selectedImages){
				removePhotoFromUploadQueue(photoInfo);
			}
		}
		if(photobooks != null){
			photobooks.remove(photobook);
		}
	}
	
	public List<GreetingCardManager> getmGreetingCardManagers() {
		if(mGreetingCardManagers == null || mGreetingCardManagers.size()==0){
			mGreetingCardManagers = new ArrayList<GreetingCardManager>();
			mGreetingCardManagers.add(new GreetingCardManager(appContex));
		}
		return mGreetingCardManagers;
	}

	public void setmGreetingCardManagers(List<GreetingCardManager> mGreetingCardManagers) {
		this.mGreetingCardManagers = mGreetingCardManagers;
	}

	public boolean isContinueShopping() {
		return isContinueShopping;
	}

	public void setContinueShopping(boolean isContinueShopping) {
		this.isContinueShopping = isContinueShopping;
	}
	
	public List<Collage> getCollages() {
		if (collages==null) {
			collages=new ArrayList<Collage>();
		}
		return collages;
	}

	
	public boolean addPhotosToUploadQueue(PhotoInfo photo){
		boolean success = false ;
		if(mUploadPhotoList==null){
			mUploadPhotoList = new Vector<PhotoInfo>() ;
		}
		
		if(!mUploadPhotoList.contains(photo)){
			synchronized (mUploadPhotoList) {
				success = mUploadPhotoList.add(photo) ;
			}
		}
		
		return success ;
    }
	
	public boolean  removePhotoFromUploadQueue(PhotoInfo photo){
		boolean success = false ;
		synchronized (mUploadPhotoList) {
			if(mUploadPhotoList!=null && mUploadPhotoList.size()>0){
				Iterator<PhotoInfo> itor = mUploadPhotoList.iterator() ;
				while(itor.hasNext()){
					PhotoInfo photoInList = itor.next() ;
					if(photo.equals(photoInList)){
						itor.remove() ;
						success = true ;
					}
					
				}
				
			}
		}
		return success ;
	}
	
	public boolean addPhotoToTempSelectedList(PhotoInfo photo){
		boolean success = false ;
		if(mTempSelectedPhotos==null){
			mTempSelectedPhotos = new ArrayList<PhotoInfo>() ;
		}
		
		if(!mTempSelectedPhotos.contains(photo)){
			success = this.mTempSelectedPhotos.add(photo) ;
			
		}
		return success ;
	}
	
	public boolean removePhotoFromTempSelectedList(PhotoInfo photo){
		boolean success = false ;
		if(mTempSelectedPhotos!=null && mTempSelectedPhotos.size()>0
				&&mTempSelectedPhotos.contains(photo)){
			success = this.mTempSelectedPhotos.remove(photo) ;
			
		}
		return success ;
	}

	
	public boolean addPrintToPrintList(PrintInfo printInfo){
		boolean success = false ;
		if(mPrints==null){
			mPrints = new ArrayList<PrintInfo>() ;
		}
		
		if(!mPrints.contains(printInfo)){
			
			success=mPrints.add(printInfo) ;
		}
		
		return success ;
	}
	
	public boolean removePrintFromPrintList(PrintInfo printInfo){
		boolean success = false ;
		if(mPrints!=null && mPrints.size()>0){
			success = 	mPrints.remove(printInfo) ;
		}
		return success ;
	}
	
	/**
	 * add by song. fixed for RSSMOBILEPDC-2075
	 * @param photo
	 * @return
	 */
	public boolean removePrintFromPrintListByPhoto(PhotoInfo photo){
		boolean success = false ;
		int size = mPrints.size();
		if(mPrints!=null && size >0){
			for (int i = 0; i < size; i++){
				if (mPrints.get(i).getPhoto().equals(photo)){
					mPrints.remove(i);
					return true;
				}
			}
		}
		return success ;
	}
	
	public List<PrintInfo> getmPrints() {
		if(mPrints==null){
			mPrints = new ArrayList<PrintInfo>() ;
		}
		return mPrints;
	}

	public void setmPrints(List<PrintInfo> mPrints) {
		this.mPrints = mPrints;
	}

	public List<PhotoInfo> getUploadSucceedImages() {
		if(uploadSucceedImages == null){
			uploadSucceedImages = new ArrayList<PhotoInfo>();
		}
		return uploadSucceedImages;
	}

	public void setUploadSucceedImages(List<PhotoInfo> uploadSucceedImages) {
		this.uploadSucceedImages = uploadSucceedImages;
	}



	public boolean isEditGreetingCart() {
		return isEditGreetingCart;
	}



	public void setEditGreetingCart(boolean isEditGreetingCart) {
		this.isEditGreetingCart = isEditGreetingCart;
	}



	public boolean isFacebookGroupHinted() {
		return isFacebookGroupHinted;
	}



	public void setFacebookGroupHinted(boolean isFacebookGroupHinted) {
		this.isFacebookGroupHinted = isFacebookGroupHinted;
	}



	public boolean isBrandedApp() {
		return brandedApp;
	}
	
	
	
	public void addErrorBitmapId(String errorId){
		if(mErrorBitmapIds==null){
			synchronized (AppContext.class) {
				mErrorBitmapIds = new ArrayList<String>() ;
			}
		}
		
		
		synchronized (mErrorBitmapIds) {
			mErrorBitmapIds.add(errorId) ;
		}
		
		
	}


	public boolean isBitmapInErrorList(String errorId){
		if(mErrorBitmapIds!=null){
			synchronized (mErrorBitmapIds) {
				return mErrorBitmapIds.contains(errorId) ;
			}
		}
		
		return false ;
	}
	
	
	public void clearBitmapErrorList(){
		if(mErrorBitmapIds!=null){
			mErrorBitmapIds.clear() ;
			mErrorBitmapIds=null;
		}
		
		
	}
	
	public List<String> getmErrorBitmapIds() {
		return mErrorBitmapIds;
	}



	public boolean isScreenOrientationFlag() {
		return screenOrientationFlag;
	}



	public void setScreenOrientationFlag(boolean screenOrientationFlag) {
		this.screenOrientationFlag = screenOrientationFlag;
	}



	public String getFacebook_photos_of_you_cover() {
		return facebook_photos_of_you_cover;
	}



	public void setFacebook_photos_of_you_cover(String facebook_photos_of_you_cover) {
		this.facebook_photos_of_you_cover = facebook_photos_of_you_cover;
	}



	public String getFacebook_your_photos_cover() {
		return facebook_your_photos_cover;
	}



	public void setFacebook_your_photos_cover(String facebook_your_photos_cover) {
		this.facebook_your_photos_cover = facebook_your_photos_cover;
	}



	public String getFacebook_your_friends_cover() {
		return facebook_your_friends_cover;
	}



	public void setFacebook_your_friends_cover(String facebook_your_friends_cover) {
		this.facebook_your_friends_cover = facebook_your_friends_cover;
	}

	public void setIsInStoreCloud(boolean inStore){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putBoolean(AppConstants.IS_IN_STORE_CLOUD, inStore).commit();
	}

	public boolean isInStoreCloud(){
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(AppConstants.IS_IN_STORE_CLOUD, false);
	}
	
	public void setInStoreCloundRetailerID(String retailerId){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putString(AppConstants.IN_STORE_RETAILER_ID, retailerId).commit();
	}
	
	public String getInStoreCloudRetailerID(){
		return PreferenceManager.getDefaultSharedPreferences(this).getString(AppConstants.IN_STORE_RETAILER_ID, "");
	}
	
	public String getCurrentRetailerID(){
		String retailerID = "";
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(isInStoreCloud()){
			retailerID = getInStoreCloudRetailerID();
		} else if (PrintHelper.orderType == 1) {
			retailerID = prefs.getString("selectedRetailerId", "");
		} else if (PrintHelper.orderType == 2) {
			retailerID = prefs.getString("retailerIdPayOnline", "");
		}
		return retailerID;
	}

	public List<Font> getFonts() {
		return fonts;
	}

	public void setFonts(List<Font> fonts) {
		this.fonts = fonts;
	}

	public TextBlock getTextBlock() {
		return textBlock;
	}

	public void setTextBlock(TextBlock textBlock) {
		this.textBlock = textBlock;
	}
	
	
	
	public InputFilter getEmojiFilter() {
		return emojiFilter;
	}


	private InputFilter emojiFilter = new InputFilter() {
		Pattern emoji = Pattern.compile(
			      "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
			       Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
			
			
		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			Matcher emojiMatcher = emoji.matcher(source);

			if (emojiMatcher.find()) {
				return "";
			}
			
			return null ;
		}
		
	} ;
	
	public void setEmojiFilter(TextView textView) {
		
		InputFilter[] inputFilter= textView.getFilters() ;
		InputFilter[] newInputFilter = null ;
		if(inputFilter!=null){
			newInputFilter =  Arrays.copyOf(inputFilter, inputFilter.length+1) ;
			
			newInputFilter[newInputFilter.length-1] = emojiFilter ;
			
		}else {
			newInputFilter = new InputFilter[]{emojiFilter} ;
		}
		textView.setFilters(newInputFilter) ;
		
	}
	
}
