package com.kodak.rss.tablet;

public class AppConstants {
	
	public static final boolean IS_DEBUG = false;
		
	public static String uploadPicAction ="com.kodak.rss.tablet.action.upload";	
		
	public static String printType ="print";
	public static String bookType = "Photobook";
	public static String cardType_GC = "Greeting Cards";
	public static String cardType_DMG = "DuplexMyGreeting";
	public static String projectType = "project";
	public static String kioskConnectType = "kiosk_connect";
	public static String cardType = "card";	
	public static String calendarType = "calendar";
	public static String collageType = "collage";
			
	public static String imageId = "imageId";
	public static String bookId = "bookId";
	public static String cardId = "cardId";
	public static String calendarId = "calendarId";
	public static String collageId = "collageId";
	
	public static final String OrderHistory = "OrderHistory";
	//public static final String SelectedStoreInfo = "selected_store_info";
	//public static final String LocalCustomerInfo = "local_customer_info";
	
	public static String isFromPhotoBookProduct = "isFromPhotoBookProduct";
	public static String isFromMyProject = "isFromMyProject";
	public static String projectName = "projectName";
	public static String selectMoreImges = "selectMoreImges";	
	
	public static final String PROJECT_DEFAULT_NAME = "Moments HD";
	
	public static final String FACEBOOK_VERSION = "v2.2/";
	public static final String APPID = "273320029352960";//273320029352960 1399455837008973   384884358194368
	public static final String[] PERMISSIONS = new String[] {"friends_photos", "user_photos" ,"user_groups","user_status","friends_status"};
	public static final String SCOPE= "https://graph.facebook.com/"+FACEBOOK_VERSION;
	public static final String SPTAG = "FBTAG";
			
	public static final String getFriendUsers = "getFriendUsers";
	public static final String getMainUser = "getMainUser";
	public static final String getGroups = "getGroups";
	
	public static final String fbkMainUser = "fbkMainUser";
	public static final String fbkUser = "fbkUser";
	public static final String fbkGroup = "fbkGroup";
	
	public static final String isfbkMain = "isfbkMain";
	public static final String isfbkFriend = "isfbkFriend";
	public static final String isfbkGroud = "isfbkGroud";
		
	public static final int ActionMovePageFlag = 0;
	public static final int ActionSetBackgroudFlag = 1;
	public static final int ActionAddCopyFlag = 2;
	
	public static final String PicUploadFailFlag = "PicUploadFailFlag";
	public static final String PicUploadSuceessId = "PicUploadSuceessId";
	public static final String isThumbnail = "isThumbnail";
	public static final String productId = "productId";
	public static final String flowType = "flowType";
	
	public static final int PRODUCT_DPI = 200;
	
	public static final double EXTERNAL_MAX_SIZE = 5*1024*1024;
	
	public static final String FB_SOURCE = "Facebook";
	public static final String NATIVE_SOURCE = "Native";
	public static final String QUANTITY_INCREMENT = "QuantityIncrement";
	public final static String KEY_LOCALYTICS = "localyticsEnabled";
	
	public static final int ActionAddImageToCardFlag = 0;
	
}
