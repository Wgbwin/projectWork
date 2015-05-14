package com.kodakalaris.kodakmomentslib.culumus.bean.retailer;

import java.io.Serializable;

import com.kodakalaris.kodakmomentslib.culumus.bean.project.ProductDescription;


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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((proDescription == null) ? 0 : proDescription.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RssEntry other = (RssEntry) obj;
		if (proDescription == null) {
			if (other.proDescription != null)
				return false;
		} else if (!proDescription.equals(other.proDescription))
			return false;
		return true;
	}
	
	
}
