package com.kodak.kodak_kioskconnect_n2r.bean;

import java.io.Serializable;

import android.content.Context;

import com.AppConstants;
import com.kodak.kodak_kioskconnect_n2r.ROI;

/**
 * Product information
 * 
 * @author song
 * @version 1.0
 * @created 2014-4-4
 */
public class ProductInfo implements Serializable {
	/**
	 * 
	 */
	public ProductInfo(Context con) {

	}

	public ProductInfo() {

	}

	private static final long serialVersionUID = 1L;
	public static final String PRO_TYPE_PRINT = "print";
	public static final String PRO_TYPE_PHOTOBOOK = "PhotoBook";

	/** 4x6 5x7 6x8 */
	public String category;
	public String descriptionId;
	public int num;
	public double price=0.0;
	public ROI roi;
	public boolean isCurrentChecked;
	public String cloneImageId;
	public int minQuantity;
	public int quantity=0;
	public String height = "4";
	public String width = "6";
	//public String filename;
	//public String uri;
	public PhotoInfo photoInfo = new PhotoInfo();
	public String shortName;
	public String name="";
	public String ProductId = "";
	public String imageId = "";
	public String serverID = "0";
	public String cartItemID = "";
	public String priceStr= "";
	public int imgWidth = 0;
	public int imgHeight = 0;
	public int newWidth;
	public int newHeight;
	public int additionalPageCount;
	public double scaleFactor = 1.0;
	public double lastScaleFactor = 1.0;
	public double defaultScaleFactor = 1.0;

	/** Print Greeting Cards SocialPrint DuplexMyGreeting PhotoBook */
	public String productType;
	public String displayImageUrl;

	public ROI getRoi() {
		return roi;
	}

	public void setRoi(ROI roi) {
		this.roi = roi;
	}

	@Override
	public boolean equals(Object o) {
		ProductInfo product = (ProductInfo) o;
		if (this.productType.equalsIgnoreCase(product.productType)) {
			if (product.productType.equalsIgnoreCase(AppConstants.PRINT_TYPE)) {
				if (this.photoInfo.getPhotoPath().equals(product.photoInfo.getPhotoPath()) && this.descriptionId.equals(product.descriptionId)) {
					return true;
				} else {
					return false;
				}
			} else {
				if (!"".equals(this.ProductId) && this.ProductId.equals(product.ProductId)) {
					return true;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}
}
