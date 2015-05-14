package com.kodak.rss.core.n2r.bean.retailer;

import java.io.Serializable;


public class RssEntry implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_PRODUCT_DESCRIPTION = "ProductDescription";
	public static final String FLAG_MAX_UNIT_PRICE = "MaxUnitPrice";
	public static final String FLAG_MIN_UNIT_PRICE = "MinUnitPrice";
	
	public ProductDescription proDescription;
	public UnitPrice maxUnitPrice;
	public UnitPrice minUnitPrice;
	
	
	public static class UnitPrice implements Serializable{
		private static final long serialVersionUID = 1L;
		public static final String FLAG_PRICE = "Price";
		public static final String FLAG_PRICE_STR = "PriceStr";
		
		public double price;
		public String priceStr;
	}
	
	public String getMarketing(){
		String marketing = "";
		if(proDescription != null){
			marketing = proDescription.getMarketing();
			if (marketing.contains("%@")) {
				marketing = marketing.replace("%@",	maxUnitPrice.priceStr);
			}
		}
		return marketing;
	}
	
	public String getGCMarketing(){
		String marketing = "";
		if(proDescription != null){
			marketing = proDescription.getMarketing();
			for(int i=0; marketing.contains("%@"); i++){
				if(i==0){
					marketing = marketing.replaceFirst("%@", "%%");
				} else {
					marketing = marketing.replace("%@",	maxUnitPrice.priceStr);
				}
			}
		}
		return marketing;
	}
	
	public String getDestination(){
		String destination = "";
		if(proDescription != null){
			destination = proDescription.getDelivery();
		}
		return destination;
	}
}
