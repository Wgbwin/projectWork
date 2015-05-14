package com.kodak.rss.core.n2r.bean.collage;

import java.util.ArrayList;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.shoppingcart.Product;

public class Collage extends Product{

	private static final long serialVersionUID = 1L;
	public static final String COLLAGE = "Collage";

	public static final String FLAG_PRODUCT_DESC_BASE_URI = "ProductDescriptionBaseURI";
	public static final String FLAG_THEME = "Theme";
	public static final String FLAG_PAGE = "Page";

	public static final String FLAG_SUGGESTED_CAPTION_VISIBILITY = "SuggestedCaptionVisibility";
	public static final String FLAG_CAN_SET_TITLE = "CanSetTitle";
	public static final String FLAG_CAN_SET_SUBTITLE = "CanSetSubtitle";
	public static final String FLAG_CAN_SET_AUTHOR = "CanSetAuthor";
	public static final String FLAG_CAN_SET_ORIENTATION = "canSetOrientation";
	
	public String productDescriptionBaseURI;
	
	public CollagePage page;
	
	public boolean suggestedCaptionVisibility;
	public boolean canSetTitle;
	public boolean canSetSubtitle;
	public boolean canSetAuthor;
	public boolean canSetOrientation;
	
	/**add by bing wang */
	public boolean isCurrentChosen = false;
	/**add by bing wang
	this list tell the collage use how many native images */
	public ArrayList<ImageInfo> chosenpics = null;
	
	public String themeId;
	
	
}
