package com.kodak.kodak_kioskconnect_n2r;

import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;

import android.content.Context;

public class CartItem
{

	//public String uri;
	//public String filename;
	public PhotoInfo photoInfo = new PhotoInfo();
	public int quantity = 0;
	public int quantityIncrement = 1;
	public double price = 0.00;
	public String height = "4";
	public String width = "6";
	public ROI roi = null;	
	public String serverID = "0";
	public String name;
	public String cartItemID = "";
	public String shortName;
	public int rotate;
	public String imageId = "";
	public String productType = "";
	public String productDescriptionId;
	public String ProductId;
	
	public int newWidth = 0;
	public int newHeight = 0;
	public double scaleFactor = 1.0;
	public double lastScaleFactor = 1.0;
	public double defaultScaleFactor = 1.0;
	public int imgWidth = 0;
	public int imgHeight = 0;
	public CartItem(Context con)
	{
		
	}
}
