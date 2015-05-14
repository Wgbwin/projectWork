package com.kodakalaris.kodakmomentslib.culumus.bean.prints;

import com.kodakalaris.kodakmomentslib.culumus.bean.product.Product;

public class StandardPrint extends Product{
	
	private static final long serialVersionUID = 1L;

	public static final String FLAG_STANDAR_PRINTS = "StandardPrints";
	
	public static final String FLAG_DATE = "Date";
	public static final String FLAG_PAGE = "Page";
	
	public String date = "";
	public PrintPage page;
	
}
