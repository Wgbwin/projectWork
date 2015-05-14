package com.kodak.rss.core.n2r.bean.shoppingcart;

import com.kodak.rss.core.n2r.bean.retailer.RssEntry.UnitPrice;

public class NewOrder extends Pricing{
	
	public static final String FLAG_NEW_ORDER = "NewOrder";
	
	public static final String FLAG_ORDER_ID = "OrderId";
	public static final String FLAG_TAX = "Tax";
	public static final String FLAG_INITIATED = "Initiated";
	
	public String orderId = "";
	public UnitPrice tax;
	public boolean initiated = false;
}
