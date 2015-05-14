package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import java.io.Serializable;

import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;

public class GreetingCardPageLayer implements Cloneable,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7107103894254707457L;

	private final String TAG = GreetingCardPageLayer.class.getSimpleName();

	public static final String TYPE = "Type";
	public static final String LOCATION = "Location";
	public static final String ANGLE = "Angle";
	public static final String PINNED = "Pinned";
	public static final String CONTENT_BASE_URI = "ContentBaseURI";
	public static final String CONTENT_Id = "ContentId";
	public static final String DATA = "Data";
	
	public static final String TYPE_IMAGE = "Image";
	public static final String TYPE_TEXT_BLOCK = "TextBlock";
	
	
	/*
	 * for location ROI
	 */
	public static final String LOCATION_X = "X";
	public static final String LOCATION_Y = "Y";
	public static final String LOCATION_W = "W";
	public static final String LOCATION_H = "H";
	public static final String LOCATION_CONTAINER_W = "ContainerW";
	public static final String LOCATION_CONTAINER_H = "ContainerH";
	
	
	public String type;
	public ROI location;
	public int angle;
	public boolean pinned;
	public String contentBaseURI;
	public String contentId = "";
	public GreetingCardPageLayerData[] data;	
	
	public String textValue;
	
	public int holeIndex = -1;
	//public String imageURI = "";
	private PhotoInfo photoInfo = new PhotoInfo();
	public String imageThumbPath = "";
	public int degree = 0;
	public int rotatedDegree = 0;
	public float offsetX = 0;
	public float offsetY = 0;
	public float scale = 1.0f;
	
	private String textInputVlaue = "";
	private String textInputDefaultValue = "";
	private boolean isEditedBefore = false; //if the edittext is edited before;
	
	@Override
	public String toString() {
		String toString = TAG + "[type:" + type + ", location[" + location.toString() + "], angle:" + angle + ", pinned:"+ pinned + ", contentId:" + contentId;
		if(data!=null){
			toString += ", data[";
			for(GreetingCardPageLayerData _data : data){
				toString += _data.toString() + "\n";
			}
			toString += "]";
		} else {
			toString += ", data:null";
		}
		toString += "]";
		return toString;
	}	
	
	public String toGetText(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_Text);
		String text = "";
		if(value!=null){
			text = value.toString();
		}
		return text;
	}
	
	public String toGetDefaultText(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_DefaultText);
		String defaultText = "";
		if(value!=null){
			defaultText = value.toString();
		}
		return defaultText;
	}
	
	public String toGetDisplayableText(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_DisplayableText);
		String displayableText = "";
		if(value!=null){
			displayableText = value.toString();
		}
		return displayableText;
	}
	
	public boolean toGetDisplayableTextWasTruncated(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_DisplayableTextWasTruncated);
		Boolean displayableTextWasTruncated = false;
		if(value!=null){
			displayableTextWasTruncated = Boolean.valueOf(value.toString());
		}
		return displayableTextWasTruncated;
	}
	
	public boolean toGetDisplayableTextWordWasSplit(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_DisplayableTextWordWasSplit);
		Boolean displayableTextWordWasSplit = false;
		if(value!=null){
			displayableTextWordWasSplit = Boolean.valueOf(value.toString());
		}
		return displayableTextWordWasSplit;
	}
	
	public String toGetSampleText(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_SampleText);
		String sampleText = "";
		if(value!=null){
			sampleText = value.toString();
		}
		return sampleText;
	}
	
	public String toGetTextLanguage(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_Language);
		String language = "";
		if(value!=null){
			language = value.toString();
		}
		return language;
	}
	
	public String toGetTextFont(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_Font);
		String font = "";
		if(value!=null){
			font = value.toString();
		}
		return font;
	}
	
	public int toGetTextSize(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_FontPointSize);
		int size = 0;
		if(value!=null){
			size = (Integer) value;
		}
		return size;
	}
	
	public int toGetFontPointSizeMin(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_FontPointSizeMin);
		int size = 0;
		if(value!=null){
			size = (Integer) value;
		}
		return size;
	}
	
	public int toGetFontPointSizeMax(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_FontPointSizeMax);
		int size = 0;
		if(value!=null){
			size = (Integer) value;
		}
		return size;
	}
	
	public int toGetFontPointSizeMinMaxUsed(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_FontPointSizeMinMaxUsed);
		int size = 0;
		if(value!=null){
			size = (Integer) value;
		}
		return size;
	}
	
	public int[] toGetTextColor(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_Color);
		int[] color = new int[]{0, 0, 0, 0};
		if(value!=null){
			String strColor = value.toString();
			int start = strColor.indexOf("[");
			int end = strColor.lastIndexOf("]");
			strColor = strColor.substring(start+1, end);
			String[] strColors = strColor.split(",");
			if (color.length == strColors.length) {
				for (int i = 0; i < strColors.length; i++) {
					String tempColor = strColors[i].substring(strColors[i].indexOf("=")+1, strColors[i].length());
					color[i] = Integer.parseInt(tempColor);
				}
			}
		}
		return color;
	}
	
	public String toGetTextJustification(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_Justification);
		String justification = "";
		if(value!=null){
			justification = value.toString();
		}
		return justification;
	}
	
	public String toGetTextAlignment(){
		Object value = getDataValue(GreetingCardPageLayerData.NAME_Alignment);
		String alignment = "";
		if(value!=null){
			alignment = value.toString();
		}
		return alignment;
	}
	
	private Object getDataValue(String name){
		Object value = null;
		for(GreetingCardPageLayerData _data : data){
			if(_data.name.equals(name)){
				value = _data.value;
				break;
			}
		}
		return value;
	}
	
	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	public String getImageThumbPath() {
		return imageThumbPath;
	}

	public void setImageThumbPath(String imageThumbPath) {
		this.imageThumbPath = imageThumbPath;
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}

	public int getRotatedDegree() {
		return rotatedDegree;
	}

	public void setRotatedDegree(int rotatedDegree) {
		this.rotatedDegree = rotatedDegree;
	}
	
	public void clearEditInfo(){
		rotatedDegree = 0;
		degree = 0;
		offsetX = 0;
		offsetY = 0;
		scale = 1.0f;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		GreetingCardPageLayer clone = (GreetingCardPageLayer) super.clone();
		if(data!=null){
			clone.data = data.clone(); 
		}
		if(location!=null){
			clone.location = (ROI) location.clone();
		}
		return clone;
	}

	public String getTextInputVlaue() {
		return textInputVlaue;
	}

	public void setTextInputVlaue(String textInputVlaue) {
		this.textInputVlaue = textInputVlaue;
	}


	public boolean isEditedBefore() {
		return isEditedBefore;
	}

	public void setEditedBefore(boolean isEditedBefore) {
		this.isEditedBefore = isEditedBefore;
	}

	public PhotoInfo getPhotoInfo() {
		return photoInfo;
	}

	public void setPhotoInfo(PhotoInfo photoInfo) {
		this.photoInfo = photoInfo;
	}
	

	public String getTextInputDefaultValue() {
		return textInputDefaultValue;
	}

	public void setTextInputDefaultValue(String textInputDefaultValue) {
		if (this.textInputDefaultValue ==""){
			this.textInputDefaultValue = textInputDefaultValue;
		}
	}

	public void copyLayerInfo(GreetingCardPageLayer olderLayer){
		  // Image Part
		  this.photoInfo = olderLayer.photoInfo;
		  this.holeIndex = olderLayer.holeIndex;
		  this.imageThumbPath = olderLayer.imageThumbPath;
		  this.degree = olderLayer.degree;
		  this.rotatedDegree = olderLayer.rotatedDegree;
		  this.offsetX = olderLayer.offsetX;
		  this.offsetY = olderLayer.offsetY;
		  this.scale = olderLayer.scale;
		  // Text Block Part
		  this.textInputVlaue = olderLayer.textInputVlaue;
		  this.isEditedBefore = olderLayer.isEditedBefore;
		 }
}
