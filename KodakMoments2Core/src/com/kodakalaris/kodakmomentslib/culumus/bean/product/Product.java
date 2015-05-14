package com.kodakalaris.kodakmomentslib.culumus.bean.product;

import java.io.Serializable;

public class Product implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public static final String TYPE = "ProductType";
	public static final String TYPE_PRINT = "Prints";
	public static final String TYPE_PHOTOBOOK = "Photobook";
	public static final String FLAG_ID = "Id";
	public static final String FLAG_PRO_DESC_ID = "ProductDescriptionId";
	
	public String id = "";
	public String proDescId = "";
	public int quantity = 1;
}
