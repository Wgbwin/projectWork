package com.kodak.kodak_kioskconnect_n2r;


public class ImageInfo {
	private final String TAG = ImageInfo.class.getSimpleName();
	
	public static final String IMAGE_INFO = "ImageInfo";
	
	public static final String BASE_URI = "BaseURI";
	public static final String ID = "Id";
	public static final String WIDTH = "Width";
	public static final String HEIGHT = "Height";
	public static final String ANGLE = "Angle";
	public static final String CROP = "Crop";
	public static final String KPT_LEVEL = "KPTLevel";
	public static final String COLOR_EFFECT = "ColorEffect";
	public static final String AUTO_RED_EYE = "AutoRedEye";
	public static final String MANUAL_RED_EYE = "ManualRedEye";
	public static final String PET_EYE = "PetEye";
	public static final String CAPTION_LANGUAGE = "CaptionLanguage";
	public static final String CROP_X = "X";
	public static final String CROP_Y = "Y";
	public static final String CROP_W = "W";
	public static final String CROP_H = "H";
	public static final String CROP_CONTAINER_W = "ContainerW";
	public static final String CROP_CONTAINER_H = "ContainerH";
	
	public String baseURI;
	public String id;
	public int width;
	public int height;
	public int angle;
	public ROI crop;
	
	public int kptLevel;
	public int colorEffect;
	public boolean autoRedEye;
	public boolean manualRedEye;
	public boolean petEye;
	public String captionLanguage;
	
	public ImageInfo(){
		crop = new ROI();
	}
	
}
