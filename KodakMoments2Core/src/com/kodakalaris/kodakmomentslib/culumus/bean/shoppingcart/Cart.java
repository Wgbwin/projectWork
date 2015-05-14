package com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart;

import java.io.Serializable;

public class Cart implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_CART = "Cart";
	public static final String FLAG_ID = "Id";
	public static final String FLAG_RETAILER_ID = "RetailerId";
	public static final String FLAG_STORE_ID = "StoreId";
	public static final String FLAG_PRICING = "Pricing";
	public static final String FLAG_CART_ITEMS = "CartItems";
	public static final String FLAG_DISCOUNTS = "Discounts";
	
	public String cartId = "";
	public String retailerId = "";
	public String storeId = "";
	public CartItem[] cartItems;
	public Pricing pricing;	
	public Discount[] discounts;
}
