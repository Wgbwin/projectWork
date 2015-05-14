package com.kodakalaris.kodakmomentslib.culumus.bean.collage;

import com.kodakalaris.kodakmomentslib.culumus.bean.product.Page;

public class CollagePage extends Page<CollageLayer> {
	private static final long serialVersionUID = 1L;

	public static final String FLAG_PAGE_TYPE = "PageType";
	public static final String FLAG_LAYOUT_TYPE = "LayoutType";
	
	public String pageType = "";
	public String layoutType = "";
	public int minNumberOfImages;
	public int maxNumberOfImages;
		
}
