package com.kodakalaris.kodakmomentslib.culumus.bean.retailer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Catalog implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_CATALOGS = "Catalogs";
	public static final String FLAG_CURRENCY = "Currency";
	public static final String FLAG_CURRENCY_SYMBOL = "CurrencySymbol";
	public static final String FLAG_PRICES_INCLUDE_TAX = "PricesIncludeTax";
	public static final String FLAG_ENTRIES = "Entries";
	
	public String currency;
	public String currencySymbol;
	public boolean pricesIncludeTax;
	public List<RssEntry> rssEntries;
	private HashMap<String, List<RssEntry>> mGroupedProducts;
	
	public Catalog(){
		mGroupedProducts = new HashMap<String, List<RssEntry>>();
	}
	
	public List<RssEntry> getProducts(String type){
		/*List<RssEntry> products = new ArrayList<RssEntry>();
		for(RssEntry entry : rssEntries){
			if(type.equalsIgnoreCase(entry.proDescription.type)){
				products.add(entry);
			}
		}
		return products;*/
		return mGroupedProducts.get(type);
	}
	
	public RssEntry getProductEntry(String descriptionId){
		if(rssEntries != null){
			for(RssEntry entry : rssEntries){
				if(entry.proDescription.id.equals(descriptionId)){
					return entry;
				}
			}
		}
		return null;
	}
	
	public String getDestination(String identifier){
		String destination = null;
		RssEntry entry = getProductEntry(identifier);
		if(entry != null){
			destination = entry.getDestination();
		}
		return destination==null ? "" : destination;
	}
	
	public void groupProducts(String productTypes){
		String[] types = productTypes.split(",");
		if(types != null && types.length > 0){
			for(String type : types){
				List<RssEntry> products = new ArrayList<RssEntry>();
				for(RssEntry entry : rssEntries){
					if(type.equalsIgnoreCase(entry.proDescription.type)){
						products.add(entry);
					}
				}
				mGroupedProducts.put(type, products);
			}
		}
	}
	
}
