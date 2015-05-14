package com.kodak.rss.core.n2r.bean.calendar;

import com.kodak.rss.core.n2r.bean.prints.Page;

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

	private int haveImageNum;

	//mainRefreshCount big mainRefreshSucCount then to load main photo   	
	private int mainRefreshCount;
	private int mainRefreshSucCount;

	public int getMainRefreshCount() {
		return mainRefreshCount;
	}

	public int getMainRefreshSucCount() {
		return mainRefreshSucCount;
	}

	public void setPageRefresh(){
		mainRefreshCount += 1;		
	}
	
	public boolean isWantMainRefresh(){			
		return mainRefreshCount > mainRefreshSucCount;
	}
		
	//refreshNum is the refresh success series number
	public void mainRefreshSuc(int refreshNum){			
		mainRefreshSucCount = refreshNum;		
	}		

	//get the new photobookPage structure,must call this function to set is want to refresh the page photo
	public void setPageRefresh(int mainCount,int mainSucCount){
		mainRefreshCount = mainCount;
		mainRefreshSucCount = mainSucCount;	
	}	
	
	public void addImageNum(){
		haveImageNum += 1;
	}
	
	public void removeImageNum(int num){
		haveImageNum -= num;
		if (haveImageNum < 0) {
			haveImageNum = 0;
		}
	}
	
	public void setImageNum(int num){		
		haveImageNum = num;
	}
	
	public int getImageNum(){		
		return haveImageNum;
	}
}
