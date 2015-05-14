package com.kodakalaris.kodakmomentslib;

public class AppConstants {
	
	// Product types
	public static final String PRO_TYPE_PRINT = "print";
	public static final String PRO_TYPE_PRINT_HUB = "printHub";
	
	public final static String KEY_LOCALYTICS = "localyticsEnabled";
	
	public final static String PACK_NAME_KODAKMOMENT2 = "com.kodakalaris.kodakmomentsapp"; 
	
	public static String MMENU_SETTING_FRAGMENT ="MMenuSettingFragment";
	public static int SETTING_SIZE = 1011; 
	public static int SETTING_COUNTRY = 1012; 
	public static int SETTING_ABOUT = 1013; 
	public static int SETTING_LICENSE = 1014;
	public final static int THUMNAIL_STANDARD_HEIGHT = 400 ;
	public final static int THUMNAIL_STANDARD_WIDTH = 400 ;
	public static final int PRODUCT_DPI = 200;
	public static final int RETRY_TIMES_PRINTHUB = 3;
	public static final int CONNECT_PRINTHUB_FAIL = 1003;
	public static final int CREATE_PRINTJOB_FAIL = 1002;
	public static final int UPLOAD_PRINTHUB_PHOTO_SUCCESS = 1001;
	public static final int UPLOAD_PRINTHUB_PHOTO_FAIL = 1000;
	
	public static final String UPLOAD_PHOTO_FLAG="uploadPhotoFlag";
	public static final String UPLOAD_PHOTO_ACTION ="com.kodakalaris.kodakmomentslib.action.upload";	
	public static final String UPLOAD_PRINTHUB_PHOTO_ACTION ="com.kodakalaris.kodakmomentslib.action.upload.printhub";
	public static final String UPLOAD_PRINTHUB_PHOTO_INDEX ="currAssetIndex";
	public static final String UPLOAD_PRINTHUB_PHOTO_RESULT ="result";
	public static final String UPLOAD_PRINTHUB_JOB_ID = "job_id";
	
	public static final String KEY_PHOTO_SOURCE = "photoSource" ;
	public static final String KEY_PRODUCT_ID = "productId";
	public static final String KEY_ALBUMS_HOLDER="albumsHolder";
	public static final String KEY_ALBUM="album";
	public static final String KEY_ALBUM_SHOW_HEAD="showHeadView";
	
	public static final String KEY_ORDER="order";

	public enum FlowType {
		
		PRINT(PRO_TYPE_PRINT), BOOK("book"), CARD("card"), KIOSK("kiosk"), COLLAGE("collage"), PRINT_HUB(PRO_TYPE_PRINT_HUB);
        private String productType;
		FlowType(String type){
			this.setProductType(type);
		}
		
		public boolean isPrintWorkFlow() {
			return this == FlowType.PRINT;
		}

		public boolean isPhotoBookWorkFlow() {
			return this == FlowType.BOOK;
		}

		public boolean isGreetingCardWorkFlow() {
			return this == FlowType.CARD;
		}

		public boolean isKioskWorkFlow() {
			return this == FlowType.KIOSK;
		}

		public boolean isCollageWorkFlow() {
			return this == FlowType.COLLAGE;
		}
		
		public boolean isPrintHubWorkFlow() {
			return this == FlowType.PRINT_HUB;
		}

		public String getProductType() {
			return productType;
		}

		public void setProductType(String productType) {
			this.productType = productType;
		}
	}

	public enum PhotoSource {
		PHONE, FACEBOOK ,INSTAGRAM , DROPBOX, FLICKR ;

		public boolean isFromPhone() {
			return this == PHONE;
		}

		public boolean isFromFaceBook() {
			return this == FACEBOOK;
		}
		
		public boolean isFromInstagram(){
			return this == INSTAGRAM;
		}
		
		public boolean isFromDropBox(){
			return this == DROPBOX;
		}
		
		public boolean isFromFlickr(){
			return this == FLICKR;
		}
		

	}
	
	public enum PhotoSizeType{
		PANAROMA(4), //defined as width > height * 2
		LANDSCAPE(1.3), //defined as width > height
		PORTRAIT (0.85); //defined as width < height
		
		private double sizeFactor ;
		
		PhotoSizeType(double sizeFactor){
			this.sizeFactor = sizeFactor;
		}
		
		public boolean isPanaroma(){
			return this == PANAROMA;
		}
		
		
		public boolean isLandscape(){
			return this == LANDSCAPE;
		}
		
		public boolean isPortrait(){
			return this == PORTRAIT;
		}

		public double getSizeFactor() {
			return sizeFactor;
		}

		public void setSizeFactor(double sizeFactor) {
			this.sizeFactor = sizeFactor;
		}
	}
	
	
	public enum PhotoUploadingState{
		INITIAL,
		UPLOADING , 
		UPLOADED_SUCCESS,
		UPLOADED_FAILED;
		
		public boolean isInital(){
			return this==INITIAL;
		}
		
		public boolean isUploading(){
			return this== UPLOADING;
		}
		
		public boolean isUploadedSuccess(){
			return this==UPLOADED_SUCCESS;
		}
		
		public boolean isUploadedFailed(){
			return this==UPLOADED_FAILED;
		}
		
		public boolean hasEverUploaded(){
			return (this==UPLOADED_SUCCESS|| this==UPLOADED_FAILED);
		}
		
		
	}
	
	
	public enum ActivityState{
		CREATED,STARTED,RESUMED,PAUSED,STOPPED,DESTROYED;
	}
	
	public enum ActivityTheme {
		DARK, LIGHT;
	}
}
