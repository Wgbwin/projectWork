package com.kodak.rss.core.n2r.bean.photobook;

import com.kodak.rss.core.n2r.bean.prints.Page;

public class PhotobookPage extends Page{
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
	
    //mainRefreshCount big mainRefreshSucCount then to load main photo   	
	private int mainRefreshCount;
	private int mainRefreshSucCount;
	
	//thumbRefreshCount big thumbRefreshSucCount then to load thumbnail photo 
	private int thumbRefreshCount;
	private int thumbRefreshSucCount;	
	
	public int getMainRefreshCount() {
		return mainRefreshCount;
	}

	public int getMainRefreshSucCount() {
		return mainRefreshSucCount;
	}

	public int getThumbRefreshCount() {
		return thumbRefreshCount;
	}

	public int getThumbRefreshSucCount() {
		return thumbRefreshSucCount;
	}

	public void setPageRefresh(){
		mainRefreshCount += 1;
		thumbRefreshCount += 1;
	}
	
	public boolean isWantMainRefresh(){			
		return mainRefreshCount > mainRefreshSucCount;
	}
	
	public boolean isWantThumbRefresh(){			
		return thumbRefreshCount > thumbRefreshSucCount;
	}
	
	//refreshNum is the refresh success series number
	public void mainRefreshSuc(int refreshNum){			
		mainRefreshSucCount = refreshNum;		
	}		
	
	//refreshNum is the refresh success series number
	public void thumbRefreshSuc(int refreshNum){	
		thumbRefreshSucCount = refreshNum;		
	}

	//get the new photobookPage structure,must call this function to set is want to refresh the page photo
	public void setPageRefresh(int mainCount,int mainSucCount,int thumbCount,int thumbSucCount){
		mainRefreshCount = mainCount;
		mainRefreshSucCount = mainSucCount;
		thumbRefreshCount = thumbCount;
		thumbRefreshSucCount = thumbSucCount;
	}
	
}
