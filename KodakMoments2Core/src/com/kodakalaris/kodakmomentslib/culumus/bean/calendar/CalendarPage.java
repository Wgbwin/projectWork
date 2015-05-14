package com.kodakalaris.kodakmomentslib.culumus.bean.calendar;

import com.kodakalaris.kodakmomentslib.culumus.bean.product.Page;

public class CalendarPage extends Page<CalendarLayer> {
	private static final long serialVersionUID = 1L;

	public static final String FLAG_PAGE = "Page";
	
	public static final String FLAG_PAGE_TYPE = "PageType";
	public static final String FLAG_LAYOUT_TYPE = "LayoutType";
	public static final String FLAG_THEME_BACKGROUND = "ThemeBackground";
		
	public static final String TYPE_COVER = "Cover";
	public static final String TYPE_STANDARD = "Standard";	
	public static final String TYPE_BACK_COVER = "BackCover";
	
	public static final String TYPE_LAYOUT_FIXED = "Fixed";
	public static final String TYPE_LAYOUT_NONE = "None";
	public static final String TYPE_LAYOUT_AUTO = "Auto";	
	
	public String pageType = "";
	public String layoutType = "";
	public String themeBackground;
	public int minNumberOfImages;
	public int maxNumberOfImages;

}
