package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import java.io.Serializable;

public class GreetingCard implements Cloneable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6768554627324428683L;
	/*
	 * these are the parts of result
	 */
	public static final String CONTENT_RESULTS = "ContentResults";
	public static final String CONTENT_COUNT = "ContentCount";
	public static final String STARTING_OFFSET = "StartingOffset";
	public static final String CONTENTS = "Contents";
	
	/*
	 * these are the parts of card
	 */
	public static final String ID = "Id";
	public static final String LOCALIZED_NAME = "LocalizedName";
	public static final String USAGE = "Usage";
	public static final String CLASS = "Class";
	public static final String VENDOR = "Vendor";
	public static final String GLYPH_URL = "GlyphURL";
	public static final String NUM_ASSET_GROUPS = "NumAssetGroups";
	public static final String BEARS_ROYALTY = "BearsRoyalty";
	public static final String WIDTH = "Width";
	public static final String HEIGHT = "Height";
	public static final String PRODUCT_IDENTIFIERS = "ProductIdentifiers";
	
	public String id;
	public String localizedName;
	public String usage;
	public String card_class;
	public String vendor;
	public String glyphURL;
	public int numAssetGroups;
	public boolean bearsRoyalty;
	public float width;
	public float height;
	public String[] productIdentifiers;
	
	// this is the theme's language
	public String language;
	
	public GreetingCard(String language){
		this.language = language;
	}
	
	@Override
	public String toString() {
		String toString = "GreetingCard[\n" 
			+ ID + ": " +id + "\n,"
			+ LOCALIZED_NAME + ": " + localizedName + "\n,"
			+ USAGE + ": " + usage + "\n,"
			+ CLASS + ": " + card_class + "\n,"
			+ VENDOR + ": " + vendor + "\n,"
			+ GLYPH_URL + ": " + glyphURL + "\n,"
			+ NUM_ASSET_GROUPS + ": " + numAssetGroups + "\n,"
			+ BEARS_ROYALTY + ": " + bearsRoyalty + "\n,"
			+ WIDTH + ": " + width + "\n,"
			+ HEIGHT + ": " + height + "\n,"
			+ PRODUCT_IDENTIFIERS + "[";
		if(productIdentifiers!=null){
			for(String indentifier : productIdentifiers){
				toString += " " + indentifier + " ";
			}
		}
		toString += "]\n" + "]";
		return toString;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	
	

}
