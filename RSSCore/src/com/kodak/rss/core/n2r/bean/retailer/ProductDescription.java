package com.kodak.rss.core.n2r.bean.retailer;

import java.io.Serializable;
import java.util.Map;

public class ProductDescription implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_ProductDescription = "ProductDescription";
	public static final String FLAG_ID = "Id";
	public static final String FLAG_NAME = "Name";
	public static final String FLAG_SHORT_NAME = "ShortName";
	public static final String FLAG_TYPE = "Type";
	public static final String FLAG_PAGE_WIDTH = "PageWidth";
	public static final String FLAG_PAGE_HEIGHT = "PageHeight";
	public static final String FLAG_LG_GLYPH_URL = "LgGlyphURL";
	public static final String FLAG_Sm_GLYPH_URL = "SmGlyphURL";
	public static final String FLAG_ATTIBUTES = "Attributes";
	public static final String FLAG_ATT_NAME = "Name";
	public static final String FLAG_ATT_VALUE = "Value";
	
	public static final String GREETINGCARDS = "Greeting Cards";
	public static final String DUPLEXMYGREETING = "DuplexMyGreeting";
	
	private final String MARKETING = "Marketing";
	private final String MIN_IMAGE_SIZE_LONG_DIM = "MinImageSizeLongDim";
	
	private final String ProductDeliveryPrompt = "ProductDeliveryPrompt";
	
	public String id = "";
	public String name = "";
	public String shortName = "";
	public String type = "";
	public int pageWidth;
	public int pageHeight;
	public String lgGlyphURL = "";
	public String smGlyphURL = "";
	public Map<String, String> attributes;
	
	protected String getMarketing(){
		String result = null;
		if(attributes!=null){
			result = attributes.get(MARKETING);
		}
		return result == null ? "" : result;
	}
	
	public int getMinImageSizeLongDim(){
		int dim = -1;
		String result = null;
		if(attributes!=null){
			result = attributes.get(MIN_IMAGE_SIZE_LONG_DIM);
		}
		if (result == null) return dim;
		try {			
			dim = Integer.valueOf(result);			
		} catch (Exception e) {}				
		return dim;
	}
	
	protected String getDelivery(){
		String delivery = null;
		if(attributes != null){
			delivery = attributes.get(ProductDeliveryPrompt);
		}
		return delivery==null ? "" : delivery;
	}
	
}
