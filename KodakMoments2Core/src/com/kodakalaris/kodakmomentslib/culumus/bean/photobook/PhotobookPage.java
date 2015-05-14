package com.kodakalaris.kodakmomentslib.culumus.bean.photobook;

import com.kodakalaris.kodakmomentslib.culumus.bean.product.Page;

public class PhotobookPage extends Page<PhotobookLayer>{
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_Page = "Page";
	public static final String FLAG_PageType = "PageType";
	public static final String FLAG_LayoutType = "LayoutType";
	public static final String FLAG_Margin = "Margin";
	public static final String FLAG_Top = "Top";
	public static final String FLAG_Left = "Left";
	public static final String FLAG_Bottom = "Bottom";
	public static final String FLAG_Right = "Right";
	public static final String FLAG_ThemeBackground = "ThemeBackground";
	public static final String FLAG_BackgroundImageId = "BackgroundImageId";
	public static final String FLAG_BackgroundImageBaseURI = "BackgroundImageBaseURI";
	
	public static final String TYPE_TITLE = "Title";
	public static final String TYPE_COVER = "Cover";
	public static final String TYPE_STANDARD = "Standard";
	public static final String TYPE_INSIDE_COVER = "InsideCover";
	public static final String TYPE_BACK_COVER = "BackCover";
	public static final String TYPE_INSIDE_BACK_COVER = "InsideBackCover";
	public static final String TYPE_DUPLEX_FILLER = "DuplexFiller";
	
	public static final String TYPE_LAYOUT_FIXED = "Fixed";
	public static final String TYPE_LAYOUT_NONE = "None";
	public static final String TYPE_LAYOUT_AUTO = "Auto";
	
	public String pageType = "";
	public String layoutType = "";
	public String themeBackGround = "";
	public String backgroundImageId = "";
	public String backgroundImageBaseURI = "";
	
}
