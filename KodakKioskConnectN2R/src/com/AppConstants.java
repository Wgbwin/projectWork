package com;

public class AppConstants {

	public static final boolean IS_DEBUG = false;
	public static final String PRINT_TYPE = "print";
	public static final String BOOK_TYPE = "book";
	public static final String CARD_TYPE = "card";
	public static final String COLLAGE_TYPE = "collage";
	public static final String KEY_PRODUCT_ID = "productId";
	public static final String KEY_PRODUCT_DECID = "productDesId";
	public static final String KEY_FOR_ADD_PICTURE = "forAdd";
	public static final String KEY_FROM_IMAGESELECTION = "FROMIMAGESELECTION";
	public static final String KEY_PHOTO_SOURCE = "photoSource" ;
	public static final String IS_FORM_SHOPPINGCART = "isFromShoppingCart"; //if the activity from the shoppingcartActivity.
	public final static String  KODAK_TEMP_PICTURE_WEB = "kodak/.webPicture" ;//do no add the final static variable into AppContext.java.
    public final static int  LIMIT_COUNT = 500 ; // for facebook request the "limit" key's value
    public final static String KEY_LOCALYTICS = "localyticsEnabled";
	
	public final static int THUMNAIL_STANDARD_HEIGHT = 400 ;
	public final static int THUMNAIL_STANDARD_WIDTH = 400 ;
	public static long FACEBOOK_IMAGE_CACHESIZE = 5*1024*1024 ;//5MB //do no add the final static variable into AppContext.java.
	public static final String KODAK_PRINT_MAKER_PACKAGE_NAME = "com.kodak.kodakprintmaker";
	public static final String KODAK_MY_KODAK_MOMENTS = "com.kodak.kodak.rsscombinedapp";
	
	public static final String IS_IN_STORE_CLOUD = "in-store_cloud";
	public static final String IN_STORE_RETAILER_ID	 = "in-store_cloud_retailer";
	public static final String IN_STORE_ID = "PrintPlace";
	public final static String FROM_SHOPPINGCART = "fromShoppingcart";
	
	/**
	 * The max dimension for tms image sent to server
	 */
	public static final int TMS_IMAGE_MAX_DIMENSION = 1080;

	public enum FlowType {
		PRINT, BOOK, CARD, WIFI,COLLAGE;

		public boolean isPrintWorkFlow() {
			return this == FlowType.PRINT;
		}

		public boolean isPhotoBookWorkFlow() {
			return this == FlowType.BOOK;
		}

		public boolean isGreetingCardWorkFlow() {
			return this == FlowType.CARD;
		}

		public boolean isWifiWorkFlow() {
			return this == FlowType.WIFI;
		}
		
		public boolean isCollageWorkFlow(){
			return this == FlowType.COLLAGE ;
		}
	}
	
	public enum PhotoSource {
		PHONE ,FACEBOOK ;
		
		public boolean isFromPhone(){
			return this == PHONE ;
		}
		
		public boolean isFromFaceBook(){
			return this == FACEBOOK ; 
		}
		
		
	}
	
	public enum LoadImageType {
		WEB_IMAGE, WEB_FONT_IMAGE, MEDIA_IMAGE ,FILE_PATH ;
		
		public boolean isWebImage(){
			return this == WEB_IMAGE ;
		}
		
		public boolean isMediaImage(){
			return this == MEDIA_IMAGE ; 
		}
		
		public boolean isFilePathlImage(){
			return this == FILE_PATH ; 
		}
		
		public boolean isWebFontImage(){
			return this == WEB_FONT_IMAGE ;
		}
		
	}


	
	
	
	
	
	
	
}
