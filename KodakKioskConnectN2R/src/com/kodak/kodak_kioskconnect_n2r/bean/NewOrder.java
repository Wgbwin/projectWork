package com.kodak.kodak_kioskconnect_n2r.bean;

import com.kodak.kodak_kioskconnect_n2r.Pricing.UnitPrice;

public class NewOrder {
	
	public static final String FLAG_NEW_ORDER = "NewOrder";
	
	public static final String FLAG_ORDER_ID = "OrderId";
	public static final String FLAG_TAX = "Tax";
	public static final String FLAG_INITIATED = "Initiated";
	public static final String FLAG_PRICING = "Pricing";
	public static final String FLAG_CURRENCY = "Currency";
	public static final String FLAG_CURRENCY_SYMBOL = "CurrencySymbol";
	public static final String FLAG_LINE_ITEMS = "LineItems";
	public static final String FLAG_LI_NAME = "Name";
	public static final String FLAG_LI_PRODUCT_DESCRIPTION_ID = "ProductDescriptionId";
	public static final String FLAG_LI_QUANTITY = "Quantity";
	public static final String FLAG_LI_UNIT_PRICE = "UnitPrice";
	public static final String FLAG_LI_TOTAL = "Total";
	public static final String FLAG_SUB_TOTAL = "SubTotal";
	public static final String FLAG_GRAND_TOTAL = "GrandTotal";
	public static final String FLAG_TAX_BE_CAL_BY_RETAILER = "TaxWillBeCalculatedByRetailer";
	public static final String FLAG_TAXES_ARE_ESTIMATED = "TaxesAreEstimated";
	
	public String orderId = "";
	public boolean initiated = false;
	public String currency = "";
	public String currencySymbol = "";
	public UnitPrice subTotal;
	public UnitPrice grandTotal;
	public UnitPrice tax;
	public boolean taxWillBeCalculatedByRetailer = false;
	public boolean taxesAreEstimated = false;
}
