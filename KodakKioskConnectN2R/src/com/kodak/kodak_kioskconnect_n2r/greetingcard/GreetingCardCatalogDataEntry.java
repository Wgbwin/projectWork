package com.kodak.kodak_kioskconnect_n2r.greetingcard;

public class GreetingCardCatalogDataEntry {
	
	public static final String PRODUCT_DESCRIPTION = "ProductDescription";
	public static final String MAX_UNIT_PRICE = "MaxUnitPrice";
	public static final String MIN_UNIT_PRICE = "MinUnitPrice";
	public static final String PRICE = "Price";
	public static final String PRICE_STR = "PriceStr";
	
	public static final String PRO_BASE_URI = "BaseURI";
	public static final String PRO_ID = "Id";
	public static final String PRO_NAME = "Name";
	public static final String PRO_SHORT_NAME = "ShortName";
	public static final String PRO_TYPE = "Type";
	public static final String PRO_PAGE_WIDTH = "PageWidth";
	public static final String PRO_PAGE_HEIGHT = "PageHeight";
	public static final String PRO_LG_GLYPH_URL = "LgGlyphURL";
	public static final String PRO_SM_GLYPH_URL = "SmGlyphURL";
	public static final String PRO_ATTRIBUTES = "Attributes";
	public static final String PRO_ATTR_NAME = "Name";
	public static final String PRO_ATTR_VALUE = "Value";	
	
	public static final String TYPE_MARKETING = "Marketing";
	public static final String TYPE_PRODUCTDELIVERYPROMPT  = "ProductDeliveryPrompt";
	
	public GreetingCardProductDescription description = new GreetingCardProductDescription();
	public Price maxUnitPrice = new Price();
	public Price minUnitPrice = new Price();
	
	public class GreetingCardProductDescription{
		
		public String baseUri;
		public String id;
		public String name;
		public String shortName;
		public String type;
		public double pageWidth;
		public double pageHeight;
		public String lgGlyphURL;
		public String smGlyphURL;
		public Attributes[] attributes;
		
		public class Attributes{
			public String name;
			public String value;
		}
	}

	protected class Price{
		public double price;
		public String priceStr;
	}
	
	public String getMarketing(String type){
		if(description.attributes!=null){
			for(GreetingCardProductDescription.Attributes attr : description.attributes){
				if(attr.name.equals(type)){
					return attr.value;
				}
			}
		}
		return "";
	}
	
	
	
}
