package com.kodak.kodak_kioskconnect_n2r;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.Settings;
import android.util.Log;

import com.AppConstants;
import com.AppContext;
import com.AppManager;
import com.example.android.bitmapfun.util.Utils;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.kodak.flip.SelectedImage;
import com.kodak.kodak_kioskconnect_n2r.Pricing.LineItem;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.kodak_kioskconnect_n2r.activity.ShoppingCartActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.retailer.CountryInfo;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardProduct;
import com.kodak.shareapi.AccessTokenResponse;
import com.kodak.utils.PhotobookUtil;
import com.kodak.utils.RSSLocalytics;

/****************
 * 
 * Eastman Kodak Company Copyright 2012
 * 
 ****************/
public class PrintHelper extends Activity
{
	//public static ArrayList<PhotoBookPage> photoBookPages;
	public static ArrayList<String> selectedImageUrls;
	public static ArrayList<String> selectedImageIds;
	public static ArrayList<String> cartGroups;
	public static ArrayList<ArrayList<CartItem>> cartChildren;
	public static ArrayList<Activity> mActivities = new ArrayList<Activity>();
	// public static ArrayList<CartItem> items;
	public static ArrayList<Bitmap> imgs;
	public static List<Store> stores;
	public static ArrayList<PrintProduct> products;
	public static ArrayList<Album> mAlbumButton = null;
	public static ArrayList<String> imageFilePaths;
	public static ArrayList<String> uriEncodedPaths;
	public static ArrayList<String> allUriEncodedPaths;
	public static ArrayList<Toaster> toast = new ArrayList<Toaster>();
	public static ArrayList<String> wifiURIs;	
	public static ArrayList<Order> sentOrders;
	public static ArrayList<String> uploadQueue = null;
	public static ArrayList<String> uploadShare2WmcQueue = null;
	public static ArrayList<String> uploadedShare2WmcQueue = null;
	public static ArrayList<Double> uploadTimes = null;
	public static ArrayList<Double> uploadFileSize = null;
	public static ArrayList<String> contentIdOfEditedImages; 
	/*public static ArrayList<String> groupsToShow;
	public static ArrayList<ArrayList<CartItem>> childrenToShow;*/
	public static List<LineItem> lineItems;
	public static List <String> greetingCardPageIds = null; //add by song . get the greeting card page ids.
	public static List<CountryInfo> allCountryInfoes;	
	private List<ProductInfo> productInfoList;
	
	//public static HashMap<String, SelectedImage> selectedImages;
	public static HashMap<String, String> selectedHash;
	public static HashMap<String, String> selectedFileNames;
	public static HashMap<String, String> imageFileUriAlbumName;
	//public static HashMap<String, String> uploadedImageIDs;
	public static HashMap<String, String> albumSelected;
	public static HashMap<String, String> uploadShare2WmcImageIDs;
	public static HashMap<String, String> countries;
	public static HashMap<String, ArrayList<Integer>> requiredContactInfos;
	public static Map<String, String> productWithId;
	public static Map<String,CountryInfo> countryInfoMap ;
	
	public static boolean[] thumbnailsselection = null;
	public static double defaultRoiX;
	public static double defaultRoiY;
	public static double defaultRoiWidth;
	public static double defaultRoiHeight;
	public static double customRoiX;
	public static double customRoiY;
	public static double customRoiWidth;
	public static double customRoiHeight;
	
	public static boolean isDirty = true;
	public static boolean albumsLoaded = false;
	public static boolean isOrderSending = false;
	public static boolean mLoggingEnabled = true;
	public static boolean wififlow = false;
	public static boolean infoEnabled = true;
	public static boolean uploadError = false;
	public static boolean qbUploadThumbError = false;
	public static boolean canValidate = true;
	public static boolean uploadShare2WmcError = false;
	//public static boolean ifQuickBookLowWarningShow = false; //check if low warning should show in quick book , add by song
	public static boolean ifRecommendWiFiSet = true;//if recommend user changing phone settings to wifi , add by song
	public static boolean isRecommendWiFiDialog = false;//check the dialog is recommend user changing  phone seetings to wifi. add by song 
	//public static boolean isDrawPath = false; //add by song . check if the path will be drawed.
	public static boolean isFlippingAnimation = false;
	/*
	 * Quick book part
	 */
	public static boolean inQuickbook = false;
	public static boolean hasQuickbook = false;	
	public static boolean inPrint = false;
	public static boolean forward2Quick = false;
	public static boolean isHandleUncaught = true;
	
	public static boolean isClearDataForDoMore = false;
	public static boolean PayOnline = false;
	public static boolean appForbidden = false; 
	
	public final static String[] columns = { MediaColumns.DATA, BaseColumns._ID };
	public static final boolean SHOW_PNG = true;//whether the app should show png images
	public final static String TAG = "PrintHelper";
	public final static String orderBy = BaseColumns._ID;
	//public static final String HAS_ACCEPT_BLANK_PAGE = "hasAcceptBlankPage";
	public static final String INCLUDE_TEST_STORES = "includeteststores";
	public static final String LAST_IMAGE_CONTENT_ID = "lastImageContentId";
	
	public static String cartID;
	public static String albumName = "";
	public static String currentUploadingFile;
	public static String status = "Not Connected";
	public static String selector;
	public static String totalCost = "See Store";
	public static String orderDetails = "";
	public static String orderEmail = "";
	public static String orderSubtotal = "";
	public static String orderStore = "";
	public static String orderTime = "";
	public static String currencySymbol = "$";
	public static String kioskVersion;
	public static String IS_AUTO_UPLOAD2ALBUMS = "isAutomaticUpload2Albums";
	public static String SHARE_EMAIL_FLAG = "share_email_flag";
	public static String SHARE_PASSWORD_FLAG = "share_password_flag";
	//public static AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
	//public static ClientTokenResponse clientTokenResponse = new ClientTokenResponse();
	public static String galleryUUID = "";
	public static String networkStatus = "";
	public static String editTextForGreetingCard= "";
	public static String TEMP_FOLDER = "/temp/.kodak";
	public static String orderID = "0";
	public static String defaultPrintSize = "4x6";
	public static String subTotal = "";
	public static String GreetingCardProductID = "";
	public static String couponCode = "";
	public static String appInfoUrl = "";
	
	
	public static Cursor imagecursor;
	public static int albumid;
	public static int selectedStorePos = -1;
	public static int selectedCursorPosition = 0;
	public static int numPrintsCreated = 0;
	public static int mNumberOfAlbums = 0;
	public static int screenCount = 0;
	public static int image_column_index;
	public static int offset = 0;
	public static int thumbnailSize = 74;
	public static int defaultPrintSizeIndex = 0;
	public static int selectedImageGroup = 0;
	public static int selectedImageChild = 0;
	public static int lastGroupPosition = 0;
	public static int lastChildPosition = 0;
	public static int orderSummaryOffset = 0;
	private static int outWidth = 0;
	private static int outHeight = 0;
	public static int lastFailedDegree = -1;
	public static int model = 1; // add by song. the model of the greeting card
	public static int flipIndex = -1;
	public static int lastStepTo = 1;
	public static int editedPageIndex = -1;
	public static int carSelectedPosition = 0;
	public static int EDITFLAG = 16;
	public static int PREVIEWFLAG = 17;
	/**
	 * quickBookSize - index 0 is width, index 1 is height
	 */
	//public static int[] quickBookSize = new int[2];
	public static int orderType = 0; ////1: pick up in store,2:Home Delivery 
	public static int count = 0;
	public static int quantityChanges = 0; //items with changed quantity in photo book;
	public static int cartRemovals = 0; //quantity for items removed from cart
	
	public static Bitmap uploadingImage;
	public static Bitmap cachedImage;
	
	public static Store selectedStore;	
	public static Context mContext = null;
	//public static CartItem selectedImage;	
	public static ProductInfo selectedImage;
	public static double defaultPrintCost = 0.19;	
	public static Typeface tf = null;
	public static Typeface tfb = null;
	//public static Typeface tfOrangelet = null;	
	public static GoogleAnalyticsTracker tracker;
	
	public static long adjTime;
	public static Cursor monthCursor = null;
	public static GoogleAnalyticsTracker mTracker;
	//public static CartItem lastSelectedImage;	
	public static ProductInfo lastSelectedImage;	
	public static ROI lastFailedROI = null;	
	public static Pricing price;
//	public static List<CountryInfo> countryInfos;
	public static CountryInfo selectedCountryInfo ;
	public static Set<Integer> wifiSetTryConnected = new HashSet<Integer>();//add by Robin. A Set remember the wifi for kiosk connected. We should remove it from android network list after try connect to it	
	
	/**
	 * Simple static adapter to use for images.
	 */
	public final static com.example.android.bitmapfun.util.ImageWorkerTaggedSetTN.ImageWorkerAdapter imageSelectionTaggedSetWorkerUrlsAdapter = new com.example.android.bitmapfun.util.ImageWorkerTaggedSetTN.ImageWorkerAdapter()
	{
		@Override
		public Object getItem(int num)
		{
			return wifiURIs.get(num);
		}

		@Override
		public int getSize()
		{
			return wifiURIs.size();
		}
	};
	/**
	 * Simple static adapter to use for image thumbnails.
	 */
	public final static com.example.android.bitmapfun.util.ImageWorkerTaggedSetTN.ImageWorkerAdapter imageSelectionTaggedSetThumbWorkerUrlsAdapter = new com.example.android.bitmapfun.util.ImageWorkerTaggedSetTN.ImageWorkerAdapter()
	{
		@Override
		public Object getItem(int num)
		{
			return wifiURIs.get(num);
		}

		@Override
		public int getSize()
		{
			return wifiURIs.size();
		}
	};
	/**
	 * Simple static adapter to use for images.
	 */
	public final static com.example.android.bitmapfun.util.ImageWorkerTN.ImageWorkerAdapter imageSelectionWorkerUrlsAdapter = new com.example.android.bitmapfun.util.ImageWorkerTN.ImageWorkerAdapter()
	{
		@Override
		public Object getItem(int num)
		{
			return uriEncodedPaths.get(num);
		}

		@Override
		public int getSize()
		{
			return uriEncodedPaths.size();
		}
	};
	/**
	 * Simple static adapter to use for image thumbnails.
	 */
	public final static com.example.android.bitmapfun.util.ImageWorkerTN.ImageWorkerAdapter imageSelectionThumbWorkerUrlsAdapter = new com.example.android.bitmapfun.util.ImageWorkerTN.ImageWorkerAdapter()
	{
		@Override
		public Object getItem(int num)
		{
			return uriEncodedPaths.get(num);
		}

		@Override
		public int getSize()
		{
			return uriEncodedPaths.size();
		}
	};
	
	public final static void handleUncaughtException(final Context context,final Activity mActivity) {
		if (isHandleUncaught) {
			Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable ex) {
						Log.e(TAG, ex.getMessage(),ex);
						Log.i(TAG, "application is crash!!");
						AppManager.getAppManager().finishAllActivity();
						String packageName = context.getApplicationContext().getPackageName();
						
						// Walmart
						boolean isWMC = packageName.contains("wmc");;
						// Print Maker 2
						boolean isKC = packageName.contains("kodakprintmaker");
						Intent mIntent = new Intent(context.getApplicationContext(), KodakPrintMakerActivity.class);
						if(isWMC){
							mIntent = new Intent(context.getApplicationContext(), WiFiSelectWorkflowActivity.class);
						} else if(isKC){
							mIntent = new Intent(context.getApplicationContext(), WiFiSelectWorkflowActivity.class);
						} else {
							mIntent = new Intent(context.getApplicationContext(), MainMenu.class);
						}
						mIntent.putExtra("fromError", true);
						mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(mIntent);
						System.exit(0);
					}
			});
		}
	}
	/** PhotoBook variables */
	//public static String selectedPhotoBookID= "selectedPhotoBookID";
	public static String AdditionalPage= "AdditionalPage";
	public static String PhotoBook= "PhotoBook";
	public static String AdditionalPageName = "defaultValue";
	//public static String selectedPhotoBookAddPageID= "selectedPhotoBookAddPageID";
	
	private static String PackageName = null;
	
	public PrintHelper(Context context)
	{
		Log.d(TAG, "new PrintHelper");
		mContext = context;
		AppContext appContex = AppContext.getApplication();
		tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Regular.ttf");
		tfb = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Bold.ttf");
		//tfOrangelet = Typeface.createFromAsset(mContext.getAssets(), "fonts/orangelet.ttf");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mLoggingEnabled = prefs.getBoolean("LOGGING_ENABLED_KEY", false);
		selectedImageIds = new ArrayList<String>();
		selectedImageUrls = new ArrayList<String>();
		lineItems = new ArrayList<Pricing.LineItem>();
		wifiURIs = new ArrayList<String>();
		stores = new ArrayList<Store>();
		products = new ArrayList<PrintProduct>();
		mAlbumButton = new ArrayList<Album>();
		imgs = new ArrayList<Bitmap>();
		selectedHash = new HashMap<String, String>();
		selectedFileNames = new HashMap<String, String>();
		imageFileUriAlbumName = new HashMap<String, String>();
		//uploadedImageIDs = new HashMap<String, String>();
		appContex.setProductInfos(new ArrayList<ProductInfo>());
		mTracker = GoogleAnalyticsTracker.getInstance();
		mTracker.startNewSession(mContext.getString(R.string.analytics), mContext);
		mTracker.setCustomVar(1, "Retailer", mContext.getString(R.string.analyticsRetailer), 2);
		mTracker.setAnonymizeIp(true);
		infoEnabled = true;
		// items = new ArrayList<CartItem>();
		cartGroups = new ArrayList<String>();
		cartChildren = new ArrayList<ArrayList<CartItem>>();
		uploadQueue = new ArrayList<String>();
		if(context.getPackageName().contains("wmc")){
			PackageName = context.getPackageName();
			uploadShare2WmcQueue = new ArrayList<String>();
			uploadedShare2WmcQueue = new ArrayList<String>();
			uploadShare2WmcImageIDs = new HashMap<String, String>();
		}else{
			uploadShare2WmcQueue = null;
			uploadedShare2WmcQueue = null;
			uploadShare2WmcImageIDs = null;
		}
		if(countries!=null){
			countries.clear();
		} else {
			countries = new HashMap<String, String>();
		}
		requiredContactInfos = new HashMap<String, ArrayList<Integer>>();
		albumSelected = new HashMap<String, String>();
		uploadTimes = new ArrayList<Double>();
		uploadFileSize = new ArrayList<Double>();
		uploadError = false;
		uploadShare2WmcError = false;
		if(sentOrders==null){
			sentOrders = new ArrayList<Order>();
		}
		lastFailedDegree = -1;
		lastFailedROI = null;
		SharedPreferences.Editor editor = prefs.edit();
		//editor.putBoolean(PrintHelper.HAS_ACCEPT_BLANK_PAGE, false);
		editor.putString(LAST_IMAGE_CONTENT_ID, "");
		editor.commit();
		if(contentIdOfEditedImages == null){
			contentIdOfEditedImages = new ArrayList<String>();
		} else {
			contentIdOfEditedImages.clear();
		}
		deleteTempFolder();
		price = null;
		selectedCountryInfo= null ;
		allCountryInfoes  =null ;
//		countryInfos = null;
		orderType = 0;
		if(productWithId == null){
			productWithId = new HashMap<String, String>();
		} /*else {
			productWithId.clear();
		}*/
		couponCode = "";
	}
	
	private static void deleteTempFolder(){
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER;
		File file = new File(path);
		if(file.exists()){
			if(file.isDirectory()){
				for(File f : file.listFiles()){
					f.delete();
				}
			}
			
			//in MI2S, it will casue filenotfoundException(Device or resource Busy) when resend order with png images.
			//I'm not sure if other devices have this problem. 
			//So, the dir will not been deleted.
			//By Robin.Qian
//			file.delete();
		}
	}

	public static boolean isNull()
	{
		if (selectedImageIds == null || selectedImageUrls == null || wifiURIs == null ||  stores == null || products == null || imgs == null || selectedHash == null || selectedFileNames == null || /*uploadedImageIDs == null ||*/ mTracker == null || cartGroups == null || cartChildren == null || uploadQueue == null || requiredContactInfos == null || albumSelected == null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean StartOver()
	{
		Log.d(TAG, "new StartOver");
		boolean success = false;
		try
		{
			try
			{
				NotificationManager mManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);		
				mManager.cancel(0);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			
			selectedHash = new HashMap<String, String>();
			/*for (Map.Entry<String, String> entry : selectedHash.entrySet())
			{
				entry.setValue("0");
			}*/
			tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Regular.ttf");
			tfb = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Bold.ttf");
			//tfOrangelet = Typeface.createFromAsset(mContext.getAssets(), "fonts/orangelet.ttf");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			mLoggingEnabled = prefs.getBoolean("LOGGING_ENABLED_KEY", false);
			selectedImageIds = new ArrayList<String>();
			selectedImageUrls = new ArrayList<String>();
			wifiSetTryConnected = new HashSet<Integer>();
			wifiURIs = new ArrayList<String>();	
			stores = new ArrayList<Store>();
			if (products == null)
				products = new ArrayList<PrintProduct>();
			imgs = new ArrayList<Bitmap>();
			cartGroups = new ArrayList<String>();
			cartChildren = new ArrayList<ArrayList<CartItem>>();
			for (int i = 0; i < products.size(); i++)
			{
				cartGroups.add(products.get(i).getName());
				cartChildren.add(new ArrayList<CartItem>());
			}
			mAlbumButton = new ArrayList<Album>();
			albumSelected = new HashMap<String, String>();
			albumName = "";
			uploadTimes = new ArrayList<Double>();
			if(PackageName != null && PackageName.contains("wmc")){
				uploadShare2WmcQueue = new ArrayList<String>();
				uploadedShare2WmcQueue = new ArrayList<String>();
				uploadShare2WmcImageIDs = new HashMap<String, String>();
			}
			else{
				uploadShare2WmcQueue = null;
				uploadedShare2WmcQueue = null;
				uploadShare2WmcImageIDs = null;
			}
			uploadFileSize = new ArrayList<Double>();
			success = true;
			uploadError = false;
			uploadShare2WmcError = false;
			orderDetails = "";
			orderEmail = "";
			orderSubtotal = "";
			orderStore = "";
			orderTime = "";
			
			lastFailedDegree = -1;
			lastFailedROI = null;
			
			lastStepTo = 1;
			SharedPreferences.Editor editor = prefs.edit();
			//editor.putBoolean(PrintHelper.HAS_ACCEPT_BLANK_PAGE, false);
			editor.putString(LAST_IMAGE_CONTENT_ID, "");
			editor.commit();
			PrintHelper.mActivities.clear();
			if(contentIdOfEditedImages != null){
				contentIdOfEditedImages.clear();
			}
			price = null;
			if(productWithId == null){
				productWithId = new HashMap<String, String>();
			} /*else {
				productWithId.clear();
			}*/
			AppContext.getApplication().getmTempSelectedPhotos().clear();
			List<GreetingCardManager> mGreetingCardManagers = AppContext.getApplication().getmGreetingCardManagers();
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return success;
	}

	public static int retWidth = -1;
	public static int retHeight = -1;

	public static void GetAspectRatioForCrop(int origHeight, int origWidth, int longRatio, int shortRatio)
	{
		retWidth = origWidth;
		retHeight = origHeight;
		if (origWidth > origHeight)
		{
			retWidth = origHeight * longRatio / shortRatio;
			if (retWidth > origWidth)
			{
				retWidth = retHeight * shortRatio / longRatio;
			}
		}
		else
		{
			retHeight = retWidth * longRatio / shortRatio;
			if (retHeight > origHeight)
			{
				retHeight = retWidth * shortRatio / longRatio;
			}
		}
	}

	public static void CalculateCustomRoi(double itemWidth, double itemHeight, double roiAspectRatio, double verticalOffsetFactor, double horizontalOffsetFactor, double scaleFactor)
	{
		boolean canRotateRoi = true;
		// initialize the "out" arguments to an empty crop
		customRoiX = 0.0;
		customRoiY = 0.0;
		customRoiWidth = 0.0;
		customRoiHeight = 0.0;
		// Proceed with the calculation only if the item width and height are >
		// 0,
		// and the roi aspect ratio must be not zero and be a valid number.
		if ((itemWidth > 0.0) && (itemHeight > 0.0) && (roiAspectRatio != 0.0))
		{
			// use the absolute value of the passed apsect ratio
			double roiAR = roiAspectRatio;
			// compute item aspect ratios
			double itemAR = itemWidth / itemHeight;
			// if the caller indicated that the roi could be rotated, adjust
			// the roi to be on the same side of 1 as the imageAR.
			if ((canRotateRoi) && (((itemAR > 1.0) && (roiAR < 1.0)) || ((itemAR < 1.0) && (roiAR > 1.0))))
			{
				// invert the roi aspect ratio
				roiAR = 1.0 / roiAR;
			}
			// begin by presuming that the entire width of the item will be
			// within the crop.
			customRoiWidth = itemWidth;
			customRoiHeight = customRoiWidth / roiAR;
			// if the crop height is taller than the item, then we need
			// to recalculate presuming that the entire height of the item
			// will be within the crop.
			if (customRoiHeight > itemHeight)
			{
				customRoiHeight = itemHeight;
				customRoiWidth = customRoiHeight * roiAR;
			}
			// calculate values for crop location taking the offset factors into
			// account. Ensure that the offsets are in the range [0,1.0]
			horizontalOffsetFactor = Math.min(1.0, Math.max(0.0, horizontalOffsetFactor));
			verticalOffsetFactor = Math.min(1.0, Math.max(0.0, verticalOffsetFactor));
			customRoiX = (itemWidth - customRoiWidth) * horizontalOffsetFactor;
			customRoiY = (itemHeight - customRoiHeight) * verticalOffsetFactor;
		}
	}

	public static ROI CalculateDefaultRoi(double itemWidth, double itemHeight, double defaultRoiAspectRatio)
	{
		boolean canRotateRoi = true;
		double verticalOffsetFactor = 0.2;
		double horizontalOffsetFactor = 0.5;
		ROI roi = new ROI();
		// initialize the "out" arguments to an empty crop
		defaultRoiX = 0.0;
		defaultRoiY = 0.0;
		defaultRoiWidth = 0.0;
		defaultRoiHeight = 0.0;
		// Proceed with the calculation only if the item width and height are >
		// 0,
		// and the roi aspect ratio must be not zero and be a valid number.
		if ((itemWidth > 0.0) && (itemHeight > 0.0) && (defaultRoiAspectRatio != 0.0))
		{
			// use the absolute value of the passed apsect ratio
			double roiAR = defaultRoiAspectRatio;
			// compute item aspect ratios
			double itemAR = itemWidth / itemHeight;
			// if the caller indicated that the roi could be rotated, adjust
			// the roi to be on the same side of 1 as the imageAR.
			if ((canRotateRoi) && (((itemAR > 1.0) && (roiAR < 1.0)) || ((itemAR < 1.0) && (roiAR > 1.0))))
			{
				// invert the roi aspect ratio
				roiAR = 1.0 / roiAR;
			}
			// begin by presuming that the entire width of the item will be
			// within the crop.
			defaultRoiWidth = itemWidth;
			defaultRoiHeight = defaultRoiWidth / roiAR;
			// if the crop height is taller than the item, then we need
			// to recalculate presuming that the entire height of the item
			// will be within the crop.
			if (defaultRoiHeight > itemHeight)
			{
				defaultRoiHeight = itemHeight;
				defaultRoiWidth = defaultRoiHeight * roiAR;
			}
			// calculate values for crop location taking the offset factors into
			// account. Ensure that the offsets are in the range [0,1.0]
			horizontalOffsetFactor = Math.min(1.0, Math.max(0.0, horizontalOffsetFactor));
			verticalOffsetFactor = Math.min(1.0, Math.max(0.0, verticalOffsetFactor));
			defaultRoiX = (itemWidth - defaultRoiWidth) * horizontalOffsetFactor;
			defaultRoiY = (itemHeight - defaultRoiHeight) * verticalOffsetFactor;
		}
		roi.x = defaultRoiX;
		roi.y = defaultRoiY;
		roi.w = defaultRoiWidth;
		roi.h = defaultRoiHeight;
		return roi;
	}

	public static Bitmap loadThumbnailImage(String uriEncodedString, int kind, Options options, Context context)
	{
		BitmapFactory.Options opt = new BitmapFactory.Options();  
		opt.inPreferredConfig = Bitmap.Config.ALPHA_8;      
		opt.inPurgeable = true;     
		opt.inInputShareable = true;   
		//opt.inSampleSize = 4;  
		// Get original image ID
		int originalImageId = Integer.parseInt(uriEncodedString.substring(uriEncodedString.lastIndexOf("/") + 1, uriEncodedString.length()));
		Log.e(TAG, "loadThumbnailImage, originalImageId: " + originalImageId);
		// Get (or create upon demand) the micro thumbnail for the original image.
		Bitmap mBitmap = null;
		String fileName = Utils.getFilePath(uriEncodedString, context);
		/*if (fileName.toUpperCase().endsWith(".PNG")){
			mBitmap = getMiniOfPNG(fileName);
		}else{*/
			mBitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), originalImageId, kind, opt);
//		}
		
		//Cause in this project, Thumbnails.MINI_KIND is necessary, so we consider images which do not have Thumbnails.MINI_KIND as damaged images.
//		if(mBitmap == null && kind == MediaStore.Images.Thumbnails.MINI_KIND)
		if(mBitmap == null)
			mBitmap = ImageSelectionActivity.overlay(readBitMap(context, R.drawable.imagewait96x96),readBitMap(context, R.drawable.alertred16x16));
		return mBitmap;
	}

	/****** GreyScale **********/
	public static Bitmap doGreyscale(Bitmap src)
	{
		// constant factors
		final double GS_RED = 0.299;
		final double GS_GREEN = 0.587;
		final double GS_BLUE = 0.114;
		// create output bitmap
		Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
		// pixel information
		int A, R, G, B;
		int pixel;
		// get image size
		int width = src.getWidth();
		int height = src.getHeight();
		// scan through every single pixel
		for (int x = 0; x < width; ++x)
		{
			for (int y = 0; y < height; ++y)
			{
				// get one pixel color
				pixel = src.getPixel(x, y);
				// retrieve color of all channels
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);
				// take conversion up to one single value
				R = G = B = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);
				// set new pixel color to output bitmap
				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}
		// return final image
		return bmOut;
	}

	/**************/
	/****** SEPIA ******/
	public static Bitmap createSepiaToningEffect(Bitmap src, int depth, double red, double green, double blue)
	{
		// image size
		int width = src.getWidth();
		int height = src.getHeight();
		// create output bitmap
		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
		// constant grayscale
		final double GS_RED = 0.3;
		final double GS_GREEN = 0.59;
		final double GS_BLUE = 0.11;
		// color information
		int A, R, G, B;
		int pixel;
		// scan through all pixels
		for (int x = 0; x < width; ++x)
		{
			for (int y = 0; y < height; ++y)
			{
				// get pixel color
				pixel = src.getPixel(x, y);
				// get color on each channel
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);
				// apply grayscale sample
				B = G = R = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);
				// apply intensity level for sepid-toning on each channel
				R += (depth * red);
				if (R > 255)
				{
					R = 255;
				}
				G += (depth * green);
				if (G > 255)
				{
					G = 255;
				}
				B += (depth * blue);
				if (B > 255)
				{
					B = 255;
				}
				// set new pixel color to output image
				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}
		// return final image
		return bmOut;
	}

	/***************/
	public static boolean isLowResWarning(ProductInfo item, double newWidth, double newHeight)
	{
		int dpi = 300;
		int acceptableScaleFactor = 2;
		int neededWidthPixels = 0;
		int neededHeightPixels = 0;
		double productWidth = Double.parseDouble(item.width);
		double productHeight = Double.parseDouble(item.height);
		
		decodeFile(item.photoInfo);
		if (item.roi == null)
		{
			double ratio = 1.0;
			if (productWidth > productHeight)
			{
				ratio = productWidth / productHeight;
			}
			else
			{
				ratio = productHeight / productWidth;
			}
			ROI roi = PrintHelper.CalculateDefaultRoi(1.0 * outWidth, 1.0 * outHeight, ratio);
			ROI tempRoi2 = new ROI();
			tempRoi2.h = roi.h / outHeight;
			tempRoi2.w = roi.w / outWidth;
			tempRoi2.y = roi.y / outHeight;
			tempRoi2.x = roi.x / outWidth;
			item.roi = tempRoi2;
		}
		
		neededWidthPixels = (int) (dpi * productWidth);
		neededHeightPixels = (int) (dpi * productHeight);
		int cropW = (int) (newWidth * outWidth);
		int cropH = (int) (newHeight * outHeight);
		
		int smallNeedPixels = neededWidthPixels<neededHeightPixels?neededWidthPixels:neededHeightPixels;
		int smallCrop = cropW<cropH?cropW:cropH;
		if(smallCrop*acceptableScaleFactor < smallNeedPixels){
			return true;
		}else {
			return false;
		}
		
	}
	
	// only for Photobook right now, added by Kane, 2014-05-24
	public static boolean isLowResWarning(ProductInfo item, PhotoInfo photoInfo, double newWidth, double newHeight){
		int dpi = 300;
		int acceptableScaleFactor = 2;
		int neededWidthPixels = 0;
		int neededHeightPixels = 0;
		double productWidth = Double.parseDouble(item.width);
		double productHeight = Double.parseDouble(item.height);
		decodeFile(photoInfo);
		if (item.roi == null)
		{
			double ratio = 1.0;
			if (productWidth > productHeight)
			{
				ratio = productWidth / productHeight;
			}
			else
			{
				ratio = productHeight / productWidth;
			}
			ROI roi = PrintHelper.CalculateDefaultRoi(1.0 * outWidth, 1.0 * outHeight, ratio);
			ROI tempRoi2 = new ROI();
			tempRoi2.h = roi.h / outHeight;
			tempRoi2.w = roi.w / outWidth;
			tempRoi2.y = roi.y / outHeight;
			tempRoi2.x = roi.x / outWidth;
			item.roi = tempRoi2;
		}
		
		neededWidthPixels = (int) (dpi * productWidth);
		neededHeightPixels = (int) (dpi * productHeight);
		int cropW, cropH;
		// when edit image in Photobook, ContainerW and ContainerH are not 0
		if(!photoInfo.getPhotoSource().isFromPhone() && item.roi!=null && item.roi.ContainerW!=0 && item.roi.ContainerH!=0){
			cropW = (int) (newWidth * item.roi.ContainerW);
			cropH = (int) (newHeight * item.roi.ContainerH);
		} else {
			cropW = (int) (newWidth * outWidth);
			cropH = (int) (newHeight * outHeight);
		}
		
		int smallNeedPixels = neededWidthPixels<neededHeightPixels?neededWidthPixels:neededHeightPixels;
		int smallCrop = cropW<cropH?cropW:cropH;
		if(smallCrop*acceptableScaleFactor<smallNeedPixels){
			return true;
		}else {
			return false;
		}
	}

	public static boolean isLowResWarning(CartItem item)
	{
		int acceptableScaleFactor = 2;
		int dpi = 300;
		int neededWidthPixels = 0;
		int neededHeightPixels = 0;
		double productWidth = Double.parseDouble(item.width);
		double productHeight = Double.parseDouble(item.height);
		decodeFile(item.photoInfo);
		if (item.roi == null)
		{
			double ratio = 1.0;
			if (productWidth > productHeight)
			{
				ratio = productWidth / productHeight;
			}
			else
			{
				ratio = productHeight / productWidth;
			}
			ROI roi = PrintHelper.CalculateDefaultRoi(1.0 * outWidth, 1.0 * outHeight, ratio);
			ROI tempRoi2 = new ROI();
			tempRoi2.h = roi.h / outHeight;
			tempRoi2.w = roi.w / outWidth;
			tempRoi2.y = roi.y / outHeight;
			tempRoi2.x = roi.x / outWidth;
			item.roi = tempRoi2;
		}
		int cropW = (int) (item.roi.w * outWidth);
		int cropH = (int) (item.roi.h * outHeight);
		neededWidthPixels = (int) (dpi * productWidth);
		neededHeightPixels = (int) (dpi * productHeight);
		if(cropW>cropH){
			if (((cropW * acceptableScaleFactor) < neededWidthPixels) || ((cropH * acceptableScaleFactor) < neededHeightPixels))
			{
				return true;
			}
			else
			{
				return false;
			}
		}else {
			
			if (((cropH * acceptableScaleFactor) < neededWidthPixels) || ((cropW * acceptableScaleFactor) < neededHeightPixels))
			{
				return true;
			}
			else
			{
				return false;
			}
			
		}
	}
	//add by song
	public static boolean isLowResWarning(ProductInfo item)
	{
		int acceptableScaleFactor = 2;
		int dpi = 300;
		int neededWidthPixels = 0;
		int neededHeightPixels = 0;
		double productWidth = Double.parseDouble(item.width);
		double productHeight = Double.parseDouble(item.height);
		decodeFile(item.photoInfo);
		if (item.roi == null)
		{
			double ratio = 1.0;
			if (productWidth > productHeight)
			{
				ratio = productWidth / productHeight;
			}
			else
			{
				ratio = productHeight / productWidth;
			}
			ROI roi = PrintHelper.CalculateDefaultRoi(1.0 * outWidth, 1.0 * outHeight, ratio);
			ROI tempRoi2 = new ROI();
			tempRoi2.h = roi.h / outHeight;
			tempRoi2.w = roi.w / outWidth;
			tempRoi2.y = roi.y / outHeight;
			tempRoi2.x = roi.x / outWidth;
			item.roi = tempRoi2;
		}
		int cropW = (int) (item.roi.w * outWidth);
		int cropH = (int) (item.roi.h * outHeight);
		neededWidthPixels = (int) (dpi * productWidth);
		neededHeightPixels = (int) (dpi * productHeight);
		
		if(cropW>cropH){
			if (((cropW * acceptableScaleFactor) < neededWidthPixels) || ((cropH * acceptableScaleFactor) < neededHeightPixels))
			{
				return true;
			}
			else
			{
				return false;
			}
		}else {
			
			if (((cropH * acceptableScaleFactor) < neededWidthPixels) || ((cropW * acceptableScaleFactor) < neededHeightPixels))
			{
				return true;
			}
			else
			{
				return false;
			}
			
		}
	}

	public static void decodeFile(PhotoInfo photo)
	{
		if(photo.getPhotoSource().isFromPhone()){
			try
			{
				// decode image size
				File f = new File(photo.getPhotoPath());
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(new FileInputStream(f), null, o);
				outWidth = o.outWidth;
				outHeight = o.outHeight;
				try {
					ExifInterface exif = new ExifInterface(f.getPath());
					int attOri = exif.getAttributeInt("Orientation", 0);
					if(attOri == ExifInterface.ORIENTATION_ROTATE_90 || attOri == ExifInterface.ORIENTATION_ROTATE_270){
						int temp = outWidth;
						outWidth = outHeight;
						outHeight = temp;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			catch (FileNotFoundException e){}
		} else {
			outWidth = photo.getWidth();
			outHeight = photo.getHeight();
		}
	}
	
	public static AccessTokenResponse getAccessTokenResponse(Context context){
		Log.d(TAG, "getAccessTokenResponse....");
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		accessTokenResponse.access_token = prefs.getString("access_token", "");
		accessTokenResponse.client_token = prefs.getString("client_token", "");
		accessTokenResponse.expire_in = prefs.getString("expire_int", "0");
		accessTokenResponse.refresh_token = prefs.getString("refresh_token", "");
		accessTokenResponse.getAccessTokenTime = prefs.getLong("token_create_time", 0);
		accessTokenResponse.status = prefs.getString("status", "");
		return accessTokenResponse;
	}
	
	public static void setAccessTokenResponse(AccessTokenResponse accessTokenResponse, Context context){
		Log.d(TAG, "setAccessTokenResponse....");
		Log.d(TAG, "access_token: " + accessTokenResponse.access_token 
				+ ",expire_int: " + accessTokenResponse.expire_in
				+ ",refresh_token: " + accessTokenResponse.refresh_token
				+ ",token_create_time: " + accessTokenResponse.getAccessTokenTime
				+ ",status: " + accessTokenResponse.status);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putString("access_token", accessTokenResponse.access_token);
		editor.putString("client_token", accessTokenResponse.client_token);
		editor.putString("expire_int", accessTokenResponse.expire_in);
		editor.putString("refresh_token", accessTokenResponse.refresh_token);
		editor.putLong("token_create_time", accessTokenResponse.getAccessTokenTime);
		editor.putString("status", accessTokenResponse.status);
		editor.commit();
	}
	
	public static Bitmap readBitMap(Context context, int resId){     
		   BitmapFactory.Options opt = new BitmapFactory.Options();     
		   opt.inPreferredConfig = Bitmap.Config.ALPHA_8;      
		   opt.inPurgeable = true;     
		   opt.inInputShareable = true;   
		   //opt.inSampleSize = 2;  
		   InputStream is = context.getResources().openRawResource(resId);     
		   return BitmapFactory.decodeStream(is,null,opt);     
		}   
	
	public static String escapeURL(String url){
		return url.replaceAll(" ", "%20");
	}

	public static Bitmap getThumbOfPNG (String strUri){
		Bitmap bit = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inSampleSize = 1;
		options.inPreferredConfig = Bitmap.Config.ALPHA_8;      
		//add by song for png file
		 String	fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER + strUri.substring(strUri.lastIndexOf("/"), strUri.length()) + "newL" + ".jpg";
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(fileName, options);
			if(options.outHeight>options.outWidth){
				options.inSampleSize = options.outHeight/400;
			} else {
				options.inSampleSize = options.outWidth/400;
			}
			options.inJustDecodeBounds = false;
			bit = BitmapFactory.decodeFile(fileName, options);
		return bit;
	}
	
	/**
	 * Checks whether the "Avoid poor networks" setting (named "Auto network switch" on 
	 * some Samsung devices) is enabled, which can in some instances interfere with Wi-Fi.
	 *
	 * @return true if the "Avoid poor networks" or "Auto network switch" setting is enabled
	 */
	@SuppressLint("NewApi")
	public static boolean isPoorNetworkAvoidanceEnabled (Context ctx) {
	    final int SETTING_UNKNOWN = -1;
	    final int SETTING_ENABLED = 1;
	    final String AVOID_POOR = "wifi_watchdog_poor_network_test_enabled";
	    final String WATCHDOG_CLASS = "android.net.wifi.WifiWatchdogStateMachine";
	    final String DEFAULT_ENABLED = "DEFAULT_POOR_NETWORK_AVOIDANCE_ENABLED";
	    final String GLOBAL_CLASS = "android.provider.Settings$Global";
	    final ContentResolver cr = ctx.getContentResolver();

	    int result;

	    if (VERSION.SDK_INT >= 17) {
	        //Setting was moved from Secure to Global as of JB MR1
	    	//because the compile is api 9 ,so we can only use reflect
//	        result = Settings.Global.getInt(cr, AVOID_POOR, SETTING_UNKNOWN);
	    	try {
				Class global = Class.forName(GLOBAL_CLASS);
				Method method = global.getDeclaredMethod("getInt", ContentResolver.class,String.class,int.class);
				result = (Integer) method.invoke(null, cr,AVOID_POOR,SETTING_UNKNOWN);
			} catch (Exception e) {
				e.printStackTrace();
				result = SETTING_UNKNOWN;
			}
	    } else if (VERSION.SDK_INT >= 15) {
	        result = Settings.Secure.getInt(cr, AVOID_POOR, SETTING_UNKNOWN);
	    } else {
	        //Poor network avoidance not introduced until ICS MR1
	        //See android.provider.Settings.java
	        return false;
	    }

	    //Exit here if the setting value is known
	    if (result != SETTING_UNKNOWN) {
	        return (result == SETTING_ENABLED);
	    }

	    //Setting does not exist in database, so it has never been changed.
	    //It will be initialized to the default value.
	    if (VERSION.SDK_INT >= 17) {
	        //As of JB MR1, a constant was added to WifiWatchdogStateMachine to determine 
	        //the default behavior of the Avoid Poor Networks setting.
	        try {
	            //In the case of any failures here, take the safe route and assume the 
	            //setting is disabled to avoid disrupting the user with false information
	            Class wifiWatchdog = Class.forName(WATCHDOG_CLASS);
	            Field defValue = wifiWatchdog.getField(DEFAULT_ENABLED);
	            if (!defValue.isAccessible()) defValue.setAccessible(true);
	            return defValue.getBoolean(null);
	        } catch (IllegalAccessException ex) {
	            return false;
	        } catch (NoSuchFieldException ex) {
	            return false;
	        } catch (ClassNotFoundException ex) {
	            return false;
	        } catch (IllegalArgumentException ex) {
	            return false;
	        }
	    } else {
	        //Prior to JB MR1, the default for the Avoid Poor Networks setting was
	        //to enable it unless explicitly disabled
	        return true;
	    }
	}  
	
	public static void removeKioskWifiListTryConnected(Context context){
		if(wifiSetTryConnected != null && wifiSetTryConnected.size()>0){
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			synchronized (wifiSetTryConnected) {
				for(int id : wifiSetTryConnected){
					wifiManager.removeNetwork(id);
				}
				wifiSetTryConnected.clear();
			}
		}
	}
	
	/**
	 * Remove/forget all kiosk wifi which android sysytem remembered
	 * @param context
	 */
	public static void removeKioskWifi(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		if (list != null && !list.isEmpty()) {
			for (WifiConfiguration wifi : list) {
				if (Connection.isKioskWifi(wifi.SSID)) {
					wifiManager.removeNetwork(wifi.networkId);
				}
			}
		}
	}
	
	/*public static Bitmap getMiniOfPNG (String fileName){
		Bitmap bit = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inSampleSize = 1;
		options.inPreferredConfig = Bitmap.Config.ALPHA_8;      
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(fileName, options);
			if(options.outHeight>options.outWidth){
				options.inSampleSize = options.outHeight/400;
			} else {
				options.inSampleSize = options.outWidth/400;
			}
			options.inJustDecodeBounds = false;
			bit = BitmapFactory.decodeFile(fileName, options);
		return bit;
	}*/
	
	public static void clearDataForDoMore(){
		PrintHelper.orderType = 0;
		AppContext appContex = AppContext.getApplication();
		List<GreetingCardManager> mGreetingCardManagers = appContex.getmGreetingCardManagers();
		PrintHelper.productWithId.clear();
		//PrintHelper.photoBookPages.clear();
		PrintHelper.lineItems.clear();
		appContex.setProductInfos(null);
		appContex.setChildItemList(null);
		appContex.setGroupItemList(null);
		appContex.getmGreetingCardManagers().clear();
		appContex.setContinueShopping(false);
		GreetingCardProductID = "";
		couponCode = "";
		AppContext.getApplication().getmTempSelectedPhotos().clear();
		PhotobookUtil.clearPhotobooksData(mContext);
		AppContext.getApplication().getmPrints().clear() ;
		AppContext.getApplication().getmUploadPhotoList().clear() ;
		AppContext.getApplication().getUploadSucceedImages().clear();
		AppContext.getApplication().getCollages().clear() ;
		deleteTempFolder();
	}
	
	public static void clearDataForWifi(){
		AppContext.getApplication().getmTempSelectedPhotos().clear();
		ImageSelectionDatabase db = new ImageSelectionDatabase(AppContext.getApplication());
		db.open();
		db.handleDeleteAllUrisWiFi();
		db.close();
	}
	
	/*
	 * fixed for RSSMOBILEPDC-1663
	 * DM - the message/check box should not display at all. 
	 * MKM, MKM HD,  KC -  The message displays and the check box is "ON/Checked" for all countries.
	 * @author song
	 */
	public static void setLocalyticsValue (){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		String packageName = mContext.getApplicationContext().getPackageName();
		//for dm
		if(packageName.contains("dm") || packageName.contains("wmc")){
			prefs.edit().putBoolean(AppConstants.KEY_LOCALYTICS, false).commit();
			prefs.edit().putBoolean(NewSettingActivity.ENABLE_ALLOW_COOKIES, false).commit();
			RSSLocalytics.closeLocalytics(mContext);
			//for kioskconnect and mkm
		}else {
			prefs.edit().putBoolean(AppConstants.KEY_LOCALYTICS, true).commit();
			prefs.edit().putBoolean(NewSettingActivity.ENABLE_ALLOW_COOKIES, true).commit();
			RSSLocalytics.openLocalytics(mContext);
		}
	}
	
	//for RSSMOBILEPDC-1952
	public static String getServerURL() {
		String currentServer = "";
		String firstName = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext()).getString("firstName", "");
		currentServer = mContext.getResources().getString(R.string.cumulus_check_internet)+"/";
		if ("RSS_Staging".equalsIgnoreCase(firstName)) {
			currentServer = "mykodakmomentsstage.kodak.com/";
		} else if ("RSS_Production".equalsIgnoreCase(firstName)) {
			currentServer = "mykodakmoments.kodak.com/";
		} else if ("RSS_Development".equalsIgnoreCase(firstName)) {
			currentServer = "rssdev.kodak.com/";
		} else if ("RSS_ENV1".equalsIgnoreCase(firstName)) {
			currentServer = "RSSDEV1.KODAK.COM/";
		} else if ("RSS_ENV2".equalsIgnoreCase(firstName)) {
			currentServer = "RSSDEV2.KODAK.COM/";
		}
		
		return currentServer;
	}
	
	public static String getBrandForURL(){
		String brandStr = "";
		if (mContext.getPackageName().contains(MainMenu.DM_COMBINED_PACKAGE_NAME)) {
			brandStr = "brand=dm&";
		}else if (mContext.getPackageName().contains("wmc")) {
			brandStr = "brand=wmc&";
		}
		return brandStr;
	}
}
