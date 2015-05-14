package com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart;

import java.io.Serializable;
import java.util.List;

public class Placeholders implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_PLACEHOLDERS = "Placeholders";
	public static final String FLAG_BASE_URI = "BaseURI";
	public static final String FLAG_IDS = "Ids";
	
	public String baseURI = "";
	public List<String> ids;
}
