package com.kodak.kodak_kioskconnect_n2r;

import java.util.ArrayList;
import java.util.List;

import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;

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
	public static final String FLAG_TOTAL_SAVINGS = "TotalSavings";
	public static final String FLAG_TAX_BE_CAL_BY_RETAILER = "TaxWillBeCalculatedByRetailer";
	public static final String FLAG_TAXES_ARE_ESTIMATED = "TaxesAreEstimated";
	public static final String FLAG_LIRU = "LineItemRollUps";
	public static final String FLAG_SAH_TOTAL = "ShippingAndHandling";
	public static final String FLAG_NEWORDER =  "NewOrder";
	
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
					return item.totalPrice.priceStr;
				}
			}
		}
		return "";
	}
	
	public LineItem getCurrentLineItem(String productDescriptionId, int index){
		if(lineItems != null && lineItems.size()>0){
			int count = 0;
			for(int i=0; i<lineItems.size(); i++)
			{
				LineItem item = lineItems.get(i);
				if(item.productDescriptionId.equals(productDescriptionId)){
					if(count==index) {
						return item;
					}
					count ++;
				}
			}
		}
		return null;
	}
	
	public ProductInfo getAdditionalPro (String productDescriptionId,int index){
		ProductInfo AdditionalPro = null;		
		LineItem item = getCurrentLineItem(productDescriptionId, index);
		if(item!=null && item.included!=null && item.included.size()>0){
			LineItem additionalLineItem = item.included.get(0);
			AdditionalPro = new ProductInfo();
			AdditionalPro.name = additionalLineItem.name;
			AdditionalPro.quantity = additionalLineItem.quantity;
			AdditionalPro.priceStr = additionalLineItem.totalPrice.priceStr;
			AdditionalPro.price = additionalLineItem.totalPrice.price;
		}
		
		return AdditionalPro;
		
	}
	
	public List<ProductInfo> getIncludePros (String productDescriptionId,int index){
		List<ProductInfo> includePros = null;	
		ProductInfo tempPro = null;
		LineItem item = getCurrentLineItem(productDescriptionId, index);
		if(item!=null && item.included!=null && item.included.size()>0){
			includePros = new ArrayList<ProductInfo>();
			for (int i =0;i<item.included.size();i++){
				LineItem additionalLineItem = item.included.get(i);
				tempPro = new ProductInfo();
				tempPro.name = additionalLineItem.name;
				tempPro.quantity = additionalLineItem.quantity;
				tempPro.priceStr = additionalLineItem.totalPrice.priceStr;
				tempPro.price = additionalLineItem.totalPrice.price;	
				includePros.add(tempPro);
			}
			
		}
		
		return includePros;
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
	
	public ProductInfo getNormalPro (String productDescriptionId,int index){
		ProductInfo normalBookPro = null;
		LineItem photoBookLineItem = null;
		photoBookLineItem = getCurrentLineItem(productDescriptionId, index);
		if (photoBookLineItem !=null){
			normalBookPro = new ProductInfo();
			normalBookPro.name = photoBookLineItem.name;
			normalBookPro.quantity = photoBookLineItem.quantity;
			normalBookPro.priceStr = photoBookLineItem.subtotalPrice.priceStr;
			normalBookPro.price = photoBookLineItem.subtotalPrice.price;
		}	
		return normalBookPro;
		
	}
	
	
	public String totalUnitPrice(String descId){
		if(lineItems != null){
			for(LineItem item : lineItems){
				if(item.productDescriptionId.equals(descId)){
					return item.totalPrice.priceStr;
				}
			}
		}
		return "";
	}
	
	public String subTotalPrice(){
		if(subTotal != null){
			return subTotal.priceStr;
		}
		return "";
	}
	
	public String totalPrice(){
		if(grandTotal != null){
			return grandTotal.priceStr;
		}
		return "";
	}
	
	public String shippingAndHandlingPrice(){
		if(shipAndHandling != null){
			return shipAndHandling.priceStr;
		}
		return "";
	}
	
	public static class UnitPrice{
		public static final String FLAG_PRICE = "Price";
		public static final String FLAG_PRICE_STR = "PriceStr";
		
		public double price;
		public String priceStr;
	}
}
