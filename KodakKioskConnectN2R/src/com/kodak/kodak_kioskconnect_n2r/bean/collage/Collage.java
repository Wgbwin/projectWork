package com.kodak.kodak_kioskconnect_n2r.bean.collage;

import java.util.List;

import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.shoppingcart.Product;

public class Collage extends Product {

	private static final long serialVersionUID = 1L;

	public static final String FLAG_Collage = "Collage";
	public static final String FLAG_ProductDescriptionBaseURI = "ProductDescriptionBaseURI";
	public static final String FLAG_Page = "Page";
	public static final String FLAG_SuggestedCaptionVisibility = "SuggestedCaptionVisibility";
	public static final String FLAG_CanSetTitle = "CanSetTitle";
	public static final String FLAG_CanSetSubtitle = "CanSetSubtitle";
	public static final String FLAG_CanSetAuthor = "CanSetAuthor";
	public static final String FLAG_CanSetOrientation = "CanSetOrientation";
    public static final String FLAG_THEME = "Theme";
	public CollagePage page;
	private String theme="" ;
	
	public boolean suggestedCaptionVisibility = false;
	public boolean canSetTitle = false;
	public boolean canSetSubtitle = false;
	public boolean canSetAuthor = false;
	public boolean canSetOrientation = false;
	
	private boolean isCurrentCollage ;

	public boolean isCurrentCollage() {
		return isCurrentCollage;
	}

	public void setCurrentCollage(boolean isCurrentCollage) {
		this.isCurrentCollage = isCurrentCollage;
	}
	
	public void updateCollagePageSelectedPhotos(List<PhotoInfo> photos){
		page.setPhotosInCollagePage(photos) ;
	}
	
	public boolean isPhotoInCollage(PhotoInfo photo){
		
		return page.isPhotoInCollagePage(photo) ;
	}
	
    public void removePhotoFromCollage(PhotoInfo photo)	{
    	 page.removePhotoFromList(photo) ;
    }

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

}
