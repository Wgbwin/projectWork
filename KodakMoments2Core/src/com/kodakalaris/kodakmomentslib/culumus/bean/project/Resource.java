package com.kodakalaris.kodakmomentslib.culumus.bean.project;

import java.io.Serializable;

public class Resource implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public static final String FLAG_RESOURCE = "Resource";
	public static final String FLAG_BASE_URI = "BaseURI";
	public static final String FLAG_ID = "Id";
	public static final String FLAG_ORIGINALURL = "originalURL";

	public String baseURI;
	public String id;
	public String originalURL;
}
