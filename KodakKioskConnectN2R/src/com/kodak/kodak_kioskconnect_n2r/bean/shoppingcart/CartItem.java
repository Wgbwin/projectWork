package com.kodak.kodak_kioskconnect_n2r.bean.shoppingcart;

public class CartItem {
	
	public static final String FLAG_CART_ITEMS = "CartItems";
	
	public static final String FLAG_PRO_ID = "ProductId";
	public static final String FLAG_RES_TYPE = "ResourceType";
	public static final String FLAG_NAME = "Name";
	public static final String FLAG_QUANTITY = "Quantity";
	
	public String productId = "";
	public String resourceType = "";
	public String name = "";
	public int quantity;
}
