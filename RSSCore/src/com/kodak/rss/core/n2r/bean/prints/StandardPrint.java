package com.kodak.rss.core.n2r.bean.prints;

import com.kodak.rss.core.n2r.bean.shoppingcart.Product;

public class StandardPrint extends Product{
	
	public static final String FLAG_STANDAR_PRINTS = "StandardPrints";
	
	public static final String FLAG_DATE = "Date";
	public static final String FLAG_PAGE = "Page";
	
	public String date = "";
	public Page page;
	
}
