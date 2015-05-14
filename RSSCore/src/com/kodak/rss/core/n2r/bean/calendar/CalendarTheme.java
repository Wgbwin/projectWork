package com.kodak.rss.core.n2r.bean.calendar;

import java.io.Serializable;

public class CalendarTheme implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_CalendarResults = "CalendarResults";
	public static final String FLAG_Calendars = "Calendars";
	
	public static final String FLAG_Id = "Id";
	public static final String FLAG_Name = "Name";
	public static final String FLAG_GlyphURL = "GlyphURL";	
	public static final String FLAG_Backgrounds = "Backgrounds";
	
	public String id = "";
	public String name = "";
	public String productDescriptionId = "";
	public String glyphUrl = "";	
	public BackGround[] backGrounds;
	
	public static class BackGround implements Serializable{
		private static final long serialVersionUID = 1L;
		public static final String FLAG_ImageURL = "ImageURL";
		public static final String FLAG_GlyphURL = "GlyphURL";
		
		public String id = "";
		public String name = "";
		public String imageURL = "";
		public String glyphURL = "";
	}
}