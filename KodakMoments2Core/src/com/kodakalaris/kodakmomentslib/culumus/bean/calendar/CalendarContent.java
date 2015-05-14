package com.kodakalaris.kodakmomentslib.culumus.bean.calendar;

import java.io.Serializable;

public class CalendarContent implements Serializable{	
	private static final long serialVersionUID = 1L;
	public static final String FLAG_CONTENTS = "Contents";

	public static final String FLAG_CONTENT_BASE_URI = "ContentBaseURI";		
	public static final String FLAG_CONTENT_ID = "ContentId";
	public static final String FLAG_CONTENT_TYPE = "ContentType";
		
	public String contentBaseURI;
	public String contentId;
	public String contentType;
	
	
}
