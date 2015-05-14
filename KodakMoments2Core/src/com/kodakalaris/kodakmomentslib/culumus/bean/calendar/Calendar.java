package com.kodakalaris.kodakmomentslib.culumus.bean.calendar;

import java.util.List;

import com.kodakalaris.kodakmomentslib.culumus.bean.product.Product;

public class Calendar extends Product {
	private static final long serialVersionUID = 1L;
	public static final String FLAG_CALENDAR = "Calendar";

	public static final String FLAG_PRODUCT_DESC_BASE_URI = "ProductDescriptionBaseURI";
	public static final String FLAG_CALENDARTYPE = "CalendarType";
	public static final String FLAG_CONTENT_BASE_URI = "ContentBaseURI";
	public static final String FLAG_CONTENT_ID = "ContentId";
	public static final String FLAG_LANGUAGE = "Language";
	
	public static final String FLAG_STARTING_MONTH = "StartingMonth";
	public static final String FLAG_STARTING_YEAR = "StartingYear";
	public static final String FLAG_STARTING_DAY_OF_WEEK = "StartingDayOfWeek";
	
	public static final String FLAG_PAGES = "Pages";
	
	public static final String FLAG_IS_DUPLEX = "IsDuplex";
	public static final String FLAG_MIN_NUM_OF_PAGES = "MinNumberOfPages";
	public static final String FLAG_MAX_NUM_OF_PAGES = "MaxNumberOfPages";
	public static final String FLAG_NUM_OF_PAGES_PER_BASE_Product = "NumberOfPagesPerBaseProduct";
	public static final String FLAG_MIN_NUM_OF_IMAGES = "MinNumberOfImages";
	public static final String FLAG_MAX_NUM_OF_IMAGES = "MaxNumberOfImages";
	
	public static final String FLAG_IDEAL_NUM_OF_IMAGES = "IdealNumberOfImages";
	public static final String FLAG_NUM_OF_IMAGES = "NumberOfImages";
	
	public static final String FLAG_SUGGESTED_CAPTION_VISIBILITY = "SuggestedCaptionVisibility";
	public static final String FLAG_CAN_SET_TITLE = "CanSetTitle";
	public static final String FLAG_CAN_SET_SUBTITLE = "CanSetSubtitle";
	public static final String FLAG_CAN_SET_AUTHOR = "CanSetAuthor";
	
	public static final int Annual_Calendars = 0;
	public static final int Monthly_Simplex = 1;
	public static final int Monthly_Duplex = 2;
	
	public String productDescriptionBaseURI;
	public String calendarType;
	public String contentBaseURI;
	public String contentId;
	public String language;
	
	public int startingMonth;
	public int startingYear;
	public int startingDayOfWeek;
	
	public List<CalendarPage> pages;
	
	public boolean isDuplex;
	public int minNumberOfPages;
	public int maxNumberOfPages;
	public int numberOfPagesPerBaseProduct;
	public int minNumberOfImages;
	public int maxNumberOfImages;
	public int idealNumberOfImages;
	public int numberOfImages;
	
	public boolean suggestedCaptionVisibility;
	public boolean canSetTitle;
	public boolean canSetSubtitle;
	public boolean canSetAuthor;
		
	/**
	 * isDuplex Monthly Duplex
	 * isSimplex Monthly Simplex
	 * isSingle Annual Calendars
	 */
	public int getCalendarType() {		
		if (pages == null) return -1;
		if (isDuplex) return Calendar.Monthly_Duplex;			
		if (pages.size() > 1) return Calendar.Monthly_Simplex;		
		return Calendar.Annual_Calendars;
	}
}
