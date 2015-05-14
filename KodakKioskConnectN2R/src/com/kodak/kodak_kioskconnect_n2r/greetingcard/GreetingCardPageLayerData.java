package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import com.kodak.kodak_kioskconnect_n2r.ROI;

public class GreetingCardPageLayerData implements Cloneable{
	private final String TAG = GreetingCardPageLayerData.class.getSimpleName();
	
	public static final String NAME = "Name";
	public static final String TYPE = "Type";
	public static final String STRING_VAL = "StringVal";
	public static final String BOOL_VAL = "BoolVal";
	public static final String DOUBLE_VAL = "DoubleVal";
	public static final String ROI_VAL = "ROIVal";
	public static final String VALUE = "Value";
	
	public static final String NAME_Text = "Text";
	public static final String NAME_DefaultText = "DefaultText";
	public static final String NAME_DisplayableText = "DisplayableText";
	public static final String NAME_DisplayableTextWasTruncated = "DisplayableTextWasTruncated";
	public static final String NAME_DisplayableTextWordWasSplit = "DisplayableTextWordWasSplit";
	public static final String NAME_Language = "Language";
	public static final String NAME_IsAppendable = "IsAppendable";
	public static final String NAME_Font = "Font";
	public static final String NAME_FontPointSize = "FontPointSize";
	public static final String NAME_FontPointSizeMin = "FontPointSizeMin";
	public static final String NAME_FontPointSizeMax = "FontPointSizeMax";
	public static final String NAME_FontPointSizeMinMaxUsed = "FontPointSizeMinMaxUsed";
	public static final String NAME_Justification = "Justification";
	public static final String NAME_Color = "Color";
	public static final String NAME_SampleText = "SampleText";
	public static final String NAME_Alignment = "Alignment";
	
	public static final String VALUE_TYPE_CROP_REGION = "CropRegion";
	
	public String name = "";
	public int type;
	public Object value;
	public String valueType;
	
	@Override
	public String toString() {
		String toString = TAG + "[name:" + name + ", type:" + type + ", valueType" + valueType + ", value:" + value + "]";
		return toString;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		GreetingCardPageLayerData clone = (GreetingCardPageLayerData) super.clone();
		if(valueType.equals(ROI_VAL) && value!=null){
			clone.value = ((ROI)value).clone();
		}
		return clone;
	}
}
