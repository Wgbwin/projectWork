package com.kodak.kodak_kioskconnect_n2r.bean.print;

import java.io.Serializable;

public class Data implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_NAME = "Name";
	public static final String FLAG_TYPE = "Type";
	public static final String FLAG_STRING_VAL = "StringVal";
	public static final String FLAG_BOOL_VAL = "BoolVal";
	public static final String FLAG_DOUBLE_VAL = "DoubleVal";
	public static final String FLAG_ROI_VAL = "ROIVal";
	public static final String FLAG_VALUE = "Value";
	
	public static final String VALUE_TYPE_CROP_REGION = "CropRegion";
	public static final String VALUE_TYPE_LOW_RES_WARNING = "LoResWarning";
	
	public static final String TYPE_TEXT = "Text";
	public static final String TYPE_LANGUAGE = "Language";
	public static final String TYPE_ALIGNMENT = "Alignment";
	public static final String TYPE_JUSTIFICATION = "Justification";
	public static final String TYPE_FONT = "Font";
	public static final String TYPE_SIZE = "FontPointSize";
	public static final String TYPE_COLOR = "Color";
	
	public static final String TYPE_VISIBLE = "Visible";
	public static final String TYPE_DEFAULTTEXT = "DefaultText";
	public static final String TYPE_SAMPLETEXT = "SampleText";
	public static final String TYPE_FONTPOINTSIZEMIN = "FontPointSizeMin";
	public static final String TYPE_FONTPOINTSIZEMAX = "FontPointSizeMax";
	public static final String TYPE_FONTPOINTSIZEMINMAXUSED = "FontPointSizeMinMaxUsed";
	public static final String TYPE_OPACITY = "Opacity";
	public static final String TYPE_BOLD = "Bold";
	public static final String TYPE_ITALIC = "Italic";
	public static final String TYPE_STRIKETHROUGH = "StrikeThrough";
	public static final String TYPE_UNDERSCORE = "Underscore";
	public static final String TYPE_ISAPPEND = "IsAppendable";
	
	public static final String TYPE_CAPTIONTEXT = "CaptionText";
	
	public String name = "";
	public int type;
	public String valueType = "";
	public Serializable value;
	
	public String getValue(){
		return value + "";
	}
	
	public Serializable getSerializableValue(){
		return value ;
	}
	
	
}
