package com.kodak.rss.core.n2r.bean.shoppingcart;

import java.util.List;

import com.kodak.rss.core.n2r.bean.retailer.RssEntry.UnitPrice;

public class Pricing {
	public static final String FLAG_PRICING = "Pricing";
	public static final String FLAG_CURRENCY = "Currency";
	public static final String FLAG_CURRENCY_SYMBOL = "CurrencySymbol";
	public static final String FLAG_LINE_ITEMS = "LineItems";
	public static final String FLAG_LI_NAME = "Name";
	public static final String FLAG_LI_PRODUCT_DESCRIPTION_ID = "ProductDescriptionId";
	public static final String FLAG_LI_QUANTITY = "Quantity";
	public static final String FLAG_LI_UNIT_PRICE = "UnitPrice";
	public static final String FLAG_LI_TOTAL = "Total";
	public static final String FLAG_LI_CII = "CartItemIndices";
	public static final String FLAG_LI_INCLUDED = "Included";
	public static final String FLAG_SUB_TOTAL = "SubTotal";
	public static final String FLAG_GRAND_TOTAL = "GrandTotal";
	public static final String FLAG_TAX_BE_CAL_BY_RETAILER = "TaxWillBeCalculatedByRetailer";
	public static final String FLAG_TAXES_ARE_ESTIMATED = "TaxesAreEstimated";
	public static final String FLAG_LIRU = "LineItemRollUps";
	public static final String FLAG_SHIP_AND_HAN = "ShippingAndHandling";
	
	public static final String FLAG_TOTAL_SAVINGS = "TotalSavings";
	
	public String currency = "";
	public String currencySymbol = "";
	public List<LineItem> lineItems;
	public UnitPrice shipAndHandling;
	public UnitPrice subTotal;
	public UnitPrice grandTotal;
	public boolean taxWillBeCalculatedByRetailer = false;
	public boolean taxesAreEstimated = false;
	public List<LineItem> lineItemRollUps;	
	public UnitPrice totalSavings;
	
	public static class LineItem{
		public String name = "";
		public String productDescriptionId = "";
		public int quantity = 0;
		public UnitPrice unitPrice;
		public UnitPrice subtotalPrice;
		public UnitPrice totalPrice;
		public List<Integer> cartItemIndices;
		public List<LineItem> included;
	}
	
	public String subUnitPrice(String productName){
		if(lineItems != null){
			for(LineItem item : lineItems){
				if(item.name.equals(productName)){
					return item.unitPrice.priceStr;
				}
			}
		}
		return "";
	}
	
	public String groupItemsPrice(String desId){
		if(lineItemRollUps != null){
			for(LineItem lineItem : lineItemRollUps){
				if(lineItem.productDescriptionId.equalsIgnoreCase(desId)){
					return lineItem.totalPrice.priceStr;
				}
			}
		}
		return "";
	}
	
	public String totalUnitPrice(String productName){
		return totalUnitPrice(productName, 0);
	}
	
	public String totalUnitPrice(String productName, int index){
		if(lineItems != null && lineItems.size()>0){
			int count = 0;
			for(int i=0; i<lineItems.size(); i++)
			{
				LineItem item = lineItems.get(i);
				if(item.name.equals(productName)){
					if(count==index) {
						return item.totalPrice.priceStr;
					}
					count ++;
				}
			}
		}
		return "";
	}
	
	public LineItem getCurrentLineItem(String productName, int index){
		if(lineItems != null && lineItems.size()>0){
			int count = 0;
			for(int i=0; i<lineItems.size(); i++)
			{
				LineItem item = lineItems.get(i);
				if(item.name.equals(productName)){
					if(count==index) {
						return item;
					}
					count ++;
				}
			}
		}
		return null;
	}
	
	public String subTotalPrice(){
		if(subTotal != null){
			return subTotal.priceStr;
		}
		return "";
	}
	
	public String getProductSubTotalPrice(String productName, int index){
		String price = "";
		int tempNumber = 0;
		for(LineItem item : lineItems){
			if(item.name.equals(productName)){
				if(tempNumber == index){
					price = item.subtotalPrice.priceStr;
					break;
				} else {
					tempNumber ++;
				}
			}
		}
		return price;
	}
	
	public String totalPrice(){
		if(grandTotal != null){
			return grandTotal.priceStr;
		}
		return "";
	}
}
