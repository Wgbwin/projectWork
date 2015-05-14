package com.kodak.rss.core.n2r.bean.greetingcard;

import java.util.ArrayList;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.shoppingcart.Product;

public class GreetingCard extends Product {
	private static final long serialVersionUID = 1L;
	public static final String GREETING_CARD = "GreetingCard";

	public static final String FLAG_PRODUCT_DESC_BASE_URI = "ProductDescriptionBaseURI";
	public static final String FLAG_THEME = "Theme";
	public static final String FLAG_PAGES = "Pages";
	public static final String FLAG_PAGE = "Page";
	public static final String FLAG_IS_DUPLEX = "IsDuplex";
	public static final String FLAG_MIN_NUM_OF_PAGES = "MinNumberOfPages";
	public static final String FLAG_MAX_NUM_OF_PAGES = "MaxNumberOfPages";
	public static final String FLAG_NUM_OF_PAGES_PER_BASE_CARD = "NumberOfPagesPerBaseCard";
	public static final String FLAG_MIN_NUM_OF_IMAGES = "MinNumberOfImages";
	public static final String FLAG_MAX_NUM_OF_IMAGES = "MaxNumberOfImages";
	public static final String FLAG_MAX_NUM_OF_IMAGES_PER_ADDED_PAGE = "MaxNumberOfImagesPerAddedPage";
	public static final String FLAG_MAX_NUM_OF_IMAGES_PER_BASE_CARD = "MaxNumberOfImagesPerBaseCard";
	public static final String FLAG_IDEAL_NUM_OF_IMAGES_PER_BASE_CARD = "IdealNumberOfImagesPerBaseCard";
	public static final String FLAG_NUM_OF_IMAGES_IN_CARD = "NumberOfImagesInCard";
	public static final String FLAG_NUM_OF_UNASSIGNED_IMAGES = "NumberOfUnassignedImages";
	public static final String FLAG_SUGGESTED_CAPTION_VISIBILITY = "SuggestedCaptionVisibility";
	public static final String FLAG_CAN_SET_TITLE = "CanSetTitle";
	public static final String FLAG_CAN_SET_SUBTITLE = "CanSetSubtitle";
	public static final String FLAG_CAN_SET_AUTHOR = "CanSetAuthor";
	
	public String productDescriptionBaseURI;
	public String theme;
	public GCPage[] pages;
	public boolean isDuplex;
	public int minNumberOfPages;
	public int maxNumberOfPages;
	public int numberOfPagesPerBaseCard;
	public int minNumberOfImages;
	public int maxNumberOfImages;
	public Integer maxNumberOfImagesPerAddedPage;
	public int maxNumberOfImagesPerBaseCard;
	public int idealNumberOfImagesPerBaseCard;
	public int numberOfImagesInCard;
	public Integer numberOfUnassignedImages;
	public boolean suggestedCaptionVisibility;
	public boolean canSetTitle;
	public boolean canSetSubtitle;
	public boolean canSetAuthor;
	
	/**add by bing wang  this book isCurrentChosen */
	public boolean isCurrentChosen = false;
	/**add by bing wang
	this list for this book tell the book use how many native images */
	public ArrayList<ImageInfo> chosenpics = null;
}
