package com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart;

import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry.UnitPrice;

public class NewOrder extends Pricing{
	
	public static final String FLAG_NEW_ORDER = "NewOrder";
	
	public static final String FLAG_ORDER_ID = "OrderId";
	public static final String FLAG_TAX = "Tax";
	public static final String FLAG_INITIATED = "Initiated";
	
	public String orderId = "";
	public UnitPrice tax;
	public boolean initiated = false;
}
