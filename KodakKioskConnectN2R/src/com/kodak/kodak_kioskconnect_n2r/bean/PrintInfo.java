package com.kodak.kodak_kioskconnect_n2r.bean;

import java.io.Serializable;

import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.ROI;

public class PrintInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2747872853690315971L;
	private PhotoInfo photo ;
	
	private int quantity ; 
	private double price ;
	private String width = "4" ;
	private String height = "6" ;
	private ROI roi = null;	 
	private String name ;
	private String shortName ;
	private String productDescriptionId ;
	
	
	public PrintInfo(PhotoInfo photoInfo){
		this.photo = photoInfo ;
		
		String height = ""+PrintHelper.products.get(
				PrintHelper.defaultPrintSizeIndex)
				.getHeight() ;
		
		String width = ""+PrintHelper.products.get(
				PrintHelper.defaultPrintSizeIndex)
				.getWidth();
		String name = PrintHelper.products.get(
				PrintHelper.defaultPrintSizeIndex).getName() ;
		
		String shortName = PrintHelper.products.get(
				PrintHelper.defaultPrintSizeIndex)
				.getShortName();
		
		String productDescriptionId = PrintHelper.products.get(
				PrintHelper.defaultPrintSizeIndex).getId();
		double price = Double.parseDouble(PrintHelper.products
				.get(PrintHelper.defaultPrintSizeIndex)
				.getMinPrice());
		
		this.setHeight(height) ;
		this.setWidth(width) ;
		this.setName(name) ;
		this.setShortName(shortName) ;
		this.setPrice(price) ;
		this.setProductDescriptionId(productDescriptionId) ;
		this.setQuantity(1) ;
		this.setRoi(null) ;
	}
	
	
	

	public PhotoInfo getPhoto() {
		return photo;
	}

	public void setPhoto(PhotoInfo photo) {
		this.photo = photo;
	}
	
	

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public ROI getRoi() {
		return roi;
	}

	public void setRoi(ROI roi) {
		this.roi = roi;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getProductDescriptionId() {
		return productDescriptionId;
	}

	public void setProductDescriptionId(String productDescriptionId) {
		this.productDescriptionId = productDescriptionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((photo == null) ? 0 : photo.hashCode());
		result = prime * result + ((productDescriptionId == null) ? 0 : productDescriptionId.hashCode());
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
		PrintInfo other = (PrintInfo) obj;
		if (photo == null) {
			if (other.photo != null)
				return false;
		} else if (!photo.equals(other.photo))
			return false;
		if (productDescriptionId == null) {
			if (other.productDescriptionId != null)
				return false;
		} else if (!productDescriptionId.equals(other.productDescriptionId))
			return false;
		return true;
	}
	

}
