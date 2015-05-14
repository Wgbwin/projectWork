package com.kodakalaris.kodakmomentslib.culumus.bean.retailer;

import java.io.Serializable;

public class Retailer implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_RETAILERS = "Retailers";
	public static final String FLAG_ID = "Id";
	public static final String FLAG_NAME = "Name";
	public static final String FLAG_SHIP_TO_HOME = "ShipToHome";
	public static final String FLAG_LG_GLYPH_URL = "LgGlyphURL";
	public static final String FLAG_SM_GLYPH_URL = "SmGlyphURL";
	public static final String FLAG_COUNTRY = "Country";
	public static final String FLAG_REQUIRED_CUSTOMER_INFO = "RequiredCustomerInfo";
	public static final String FLAG_PAY_ONLINE = "PayOnline";
	public static final String FLAG_CLOLITE = "CLOLite";
	public static final String FLAG_CART_LIMIT = "CartLimit";
	public static final String FLAG_CART_MINIMUM_LIMIT = "CartMinimum";
	public static final String FLAG_CL_CURRENCY = "Currency";
	public static final String FLAG_CL_CURRENCY_SYMBOL = "CurrencySymbol";
	public static final String FLAG_CL_PRICE = "Price";
	public static final String FLAG_CL_PRICE_STR = "PriceStr";
	
	public String id;
	public String name;
	public boolean shipToHome = false;
	public String lgGlyphURL;
	public String smGlyphURL;
	public String country;
	public int[] requiredCustomerInfo;
	public boolean payOnline = false;
	public CartLimit cartLimit;
	public CartLimit cartMinimumLimit;
	public boolean cloLite = false;
	
	public static class CartLimit implements Serializable{
		private static final long serialVersionUID = 1L;
		public String currency;
		public String currencySymbol;
		public int price;
		public String PriceStr;
	}
}
