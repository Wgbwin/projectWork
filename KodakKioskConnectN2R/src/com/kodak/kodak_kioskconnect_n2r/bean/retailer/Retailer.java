package com.kodak.kodak_kioskconnect_n2r.bean.retailer;

public class Retailer {
	
	public static final String FLAG_RETAILERS = "Retailers";
	public static final String FLAG_ID = "Id";
	public static final String FLAG_NAME = "Name";
	public static final String FLAG_SHIP_TO_HOME = "ShipToHome";
	public static final String FLAG_CLOLite = "CLOLite";
	public static final String FLAG_LG_GLYPH_URL = "LgGlyphURL";
	public static final String FLAG_SM_GLYPH_URL = "SmGlyphURL";
	public static final String FLAG_COUNTRY = "Country";
	public static final String FLAG_REQUIRED_CUSTOMER_INFO = "RequiredCustomerInfo";
	public static final String FLAG_PAY_ONLINE = "PayOnline";
	public static final String FLAG_CART_LIMIT = "CartLimit";
	public static final String FLAG_CART_MINIMUM_LIMIT = "CartMinimum";
	public static final String FLAG_CL_CURRENCY = "Currency";
	public static final String FLAG_CL_CURRENCY_SYMBOL = "CurrencySymbol";
	public static final String FLAG_CL_PRICE = "Price";
	public static final String FLAG_CL_PRICE_STR = "PriceStr";
	public static final String FLAG_IN_STORE = "InStore";
	
	private String id;
	private String name;
	private boolean shipToHome = false;
	private boolean isCLOLite = false;
	private String lgGlyphURL;
	private String smGlyphURL;
	private String country;
	private int[] requiredCustomerInfo;
	private boolean payOnline = false;
	private CartLimit cartLimit;
	private CartLimit cartMinimumLimit ;
	private boolean inStore = false;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isShipToHome() {
		return shipToHome;
	}

	public void setShipToHome(boolean shipToHome) {
		this.shipToHome = shipToHome;
	}

	public boolean isCLOLite() {
		return isCLOLite;
	}

	public void setCLOLite(boolean isCLOLite) {
		this.isCLOLite = isCLOLite;
	}
	public String getLgGlyphURL() {
		return lgGlyphURL;
	}

	public void setLgGlyphURL(String lgGlyphURL) {
		this.lgGlyphURL = lgGlyphURL;
	}

	public String getSmGlyphURL() {
		return smGlyphURL;
	}

	public void setSmGlyphURL(String smGlyphURL) {
		this.smGlyphURL = smGlyphURL;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int[] getRequiredCustomerInfo() {
		return requiredCustomerInfo;
	}

	public void setRequiredCustomerInfo(int[] requiredCustomerInfo) {
		this.requiredCustomerInfo = requiredCustomerInfo;
	}

	public boolean isPayOnline() {
		return payOnline;
	}

	public void setPayOnline(boolean payOnline) {
		this.payOnline = payOnline;
	}

	public CartLimit getCartLimit() {
		return cartLimit;
	}

	public void setCartLimit(CartLimit cartLimit) {
		this.cartLimit = cartLimit;
	}

	public boolean isInStore() {
		return inStore;
	}

	public void setInStore(boolean inStore) {
		this.inStore = inStore;
	}

	public CartLimit getCartMinimumLimit() {
		return cartMinimumLimit;
	}

	public void setCartMinimumLimit(CartLimit cartMinimumLimit) {
		this.cartMinimumLimit = cartMinimumLimit;
	}

	public static class CartLimit{
		public String currency;
		public String currencySymbol;
		public double price;
		public String PriceStr;
	}
}
