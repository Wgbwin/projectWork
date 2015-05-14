package com.kodak.rss.tablet.bean;

import java.io.Serializable;

public class StorePriceInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	public String descriptionId;
	public String unitPrice;
	
	public StorePriceInfo(String descriptionId, String price) {
		this.descriptionId = descriptionId;
		this.unitPrice = price;	
	}

}
