package com.kodak.rss.core.n2r.bean.collage;

import java.util.List;

import com.kodak.rss.core.n2r.bean.prints.Page;

public class CollagePage extends Page {
	private static final long serialVersionUID = 1L;

	public static final String FLAG_PAGE_TYPE = "PageType";
	public static final String FLAG_LAYOUT_TYPE = "LayoutType";
	public static final String FLAG_ALTERNATE_LAYOUTS = "AlternateLayouts";
	
	public String pageType = "";
	public String layoutType = "";
	public int minNumberOfImages;
	public int maxNumberOfImages;	
	public List<AlternateLayout> alternateLayouts;
		
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

	//get the new CollagePage structure,must call this function to set is want to refresh the page photo
	public void setPageRefresh(int mainCount,int mainSucCount){
		mainRefreshCount = mainCount;
		mainRefreshSucCount = mainSucCount;
	}
	
	
}
