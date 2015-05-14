package com.kodak.kodak_kioskconnect_n2r.bean.photobook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.kodak.flip.PhotoBookPage;
import com.kodak.flip.PhotoDefinition;
import com.kodak.flip.SelectedImage;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.shoppingcart.Product;

public class Photobook extends Product{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_PHOTOBOOK = "PhotoBook";
	public static final String FLAG_Theme = "Theme";
	public static final String FLAG_Pages = "Pages";
	public static final String FLAG_IsDuplex = "IsDuplex";
	public static final String FLAG_MinNumberOfPages = "MinNumberOfPages";
	public static final String FLAG_MaxNumberOfPages = "MaxNumberOfPages";
	public static final String FLAG_NumberOfPagesPerBaseBook = "NumberOfPagesPerBaseBook";
	public static final String FLAG_MinNumberOfImages = "MinNumberOfImages";
	public static final String FLAG_MaxNumberOfImages = "MaxNumberOfImages";
	public static final String FLAG_MaxNumberOfImagesPerAddedPage = "MaxNumberOfImagesPerAddedPage";
	public static final String FLAG_MaxNumberOfImagesPerBaseBook = "MaxNumberOfImagesPerBaseBook";
	public static final String FLAG_IdealNumberOfImagesPerBaseBook = "IdealNumberOfImagesPerBaseBook";
	public static final String FLAG_NumberOfImagesInBook = "NumberOfImagesInBook";
	public static final String FLAG_NumberOfUnassignedImages = "NumberOfUnassignedImages";
	public static final String FLAG_SuggestedCaptionVisibility = "SuggestedCaptionVisibility";
	public static final String FLAG_SequenceNumberOfIconicPage = "SequenceNumberOfIconicPage";
	public static final String FLAG_CanSetTitle = "CanSetTitle";
	public static final String FLAG_CanSetSubtitle = "CanSetSubtitle";
	public static final String FLAG_CanSetAuthor = "CanSetAuthor";
	
	
	public String theme = "";
	// Added by Kane(2014-4-24): This PhotobookPage is not the same as server
	public List<PhotoBookPage> photoBookPages = new ArrayList<PhotoBookPage>();
	public boolean isDuplex = false;
	public int minNumberOfPages;
	public int maxNumberOfPages;
	public int numberOfPagesPerBaseBook;
	public int minNumberOfImages;
	public int maxNumberOfImages;
	public int maxNumberOfImagesPerAddedPage;
	public int maxNumberOfImagesPerBaseBook;
	public int idealNumberOfImagesPerBaseBook;
	public int numberOfImagesInBook;
	public int numberOfUnassignedImages;
	public boolean suggestedCaptionVisibility;
	public int sequenceNumberOfIconicPage;
	public boolean canSetTitle = false;
	public boolean canSetSubtitle = false;
	public boolean canSetAuthor = false;
	
	public String author = "";
	public String title = "";
	public String subTitle = "";
	
	public List<PhotoInfo> selectedImages = new ArrayList<PhotoInfo>();
	public HashMap<PhotoInfo, SelectedImage> imageEditParams = new HashMap<PhotoInfo, SelectedImage>();
	public boolean isFirstToCreatePhotoBook = true;
	public boolean hasAcceptBlankPage = false;
	
	
	// below need to remove when Photobook structure refactor successfuly
	public String titlePageId = "";
	public float width;
	public float height;
	public String titleImageLocalUri = "";
	public String titleImagePath = "";
	public String titleImageId = "";
	public String additionalPageId = "";
	public boolean isLowResWarningShow = false;
	
	public boolean isImageAlreadyInPhotobook(PhotoInfo photo){
    	boolean hasAdded = false;
		if(photoBookPages!=null && photoBookPages.size()>0){
			for(PhotoBookPage page : photoBookPages){
				if(page.PhotoBookPageImages!=null && page.PhotoBookPageImages.size()>0){
					for(PhotoDefinition pd : page.PhotoBookPageImages){
						if(pd!=null && photo.getPhotoPath().equals(pd.photoPath)){
							hasAdded = true;
						}
					}
				} else if(photo.getPhotoPath().equals(titleImagePath)){
					hasAdded = true;
				}
			}
		} 
		return hasAdded;
    }
}
