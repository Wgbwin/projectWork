package com.kodakalaris.kodakmomentslib.bean;

import java.io.Serializable;
import java.util.List;

import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;

/**
 * Purpose: Describe product
 * Author: Bing Wang
 * Created Time: Sep 3, 2013 10:40:35 AM 
 */
public class ProductInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String PRO_TYPE_PRINT = "print";
	public static final String PRO_TYPE_PHOTOBOOK = "PhotoBook";
	public static final String PRO_TYPE_COLLAGES = "Collages";
		
	public static final String PRO_TYPE_DUPLEXCALENDAR = "DuplexCalendar";
	public static final String PRO_TYPE_SIMPLEXCALENDAR = "SimplexCalendar";
	public static final String PRO_TYPE_ANNUALCALENDAR = "AnnualCalendar";
	
	/**4x6 5x7 6x8 */
	public String category;	
	public int pageHeight;	
	public int pageWidth;	
	public String descriptionId;
	public int num;
	public String price;
	public ROI roi;
	public boolean isCurrentChecked;
	public String cloneImageId;
	public int quantityIncrement = 1;
	
	/**Print Greeting Cards  SocialPrint DuplexMyGreeting  PhotoBook*/  
	public String productType;	
	/**correspond Id
	 *Print the Id is imageOriginalResourceId maybe is null
	 *PhotoBook the Id is the bookId*/  
	public String correspondId;
	/**
	 * Kane added
	 * to Prints: this is equal to Standard Print id, sending Prints order need Standard Print id, but not image content id <P>
	 * to Photobook: this is equal to Photobook id
	 */
	public String cartItemId;
	public String displayImageUrl; 
	public String downloadDisplayImageUrl; 
	
	public ROI getRoi() {
		return roi;
	}
	public void setRoi(ROI roi) {
		this.roi = roi;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
		
	
}
