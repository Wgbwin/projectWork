package com.kodak.rss.core.n2r.bean.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Content implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_ContentResults = "ContentResults";
	public static final String FLAG_Contents = "Contents";
	
	public static final String FLAG_BaseURI = "BaseURI";
	public static final String FLAG_Id = "Id";
	public static final String FLAG_LocalizedName = "LocalizedName";
	public static final String FLAG_Usage = "Usage";
	public static final String FLAG_Class = "Class";
	public static final String FLAG_Vendor = "Vendor";
	public static final String FLAG_GlyphURL = "GlyphURL";
	public static final String FLAG_NumAssetGroups = "NumAssetGroups";
	public static final String FLAG_BearsRoyalty = "BearsRoyalty";
	public static final String FLAG_Width = "Width";
	public static final String FLAG_Height = "Height";
	public static final String FLAG_ContentCount = "ContentCount";
	public static final String FLAG_ProductIdentifiers = "ProductIdentifiers";
	
	public String baseURI = "";
	public String id = "";
	public String localizedName = "";
	public String usage = "";
	public String strClass = "";
	public String vendor = "";
	public String glyphURL = "";
	public int numAssetGroups;
	public boolean bearsRoyalty = false;
	public int width;
	public int height;
	public int contentCount;
	public List<String> productIdentifiers = new ArrayList<String>();
}
