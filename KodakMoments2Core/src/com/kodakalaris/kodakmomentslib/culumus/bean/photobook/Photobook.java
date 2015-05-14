package com.kodakalaris.kodakmomentslib.culumus.bean.photobook;

import java.util.ArrayList;
import java.util.List;

import com.kodakalaris.kodakmomentslib.culumus.bean.product.Layer;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.Product;

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
	public static final String FLAG_Title = "Title";
	public static final String FLAG_Subtitle = "Subtitle";
	public static final String FLAG_Author = "Author";
	public static final String FLAG_CanSetTitle = "CanSetTitle";
	public static final String FLAG_CanSetSubtitle = "CanSetSubtitle";
	public static final String FLAG_CanSetAuthor = "CanSetAuthor";
	
	
	public String theme = "";
	public List<PhotobookPage> pages;
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
	public String projectName = "";
	
	/**add by bing wang
	this book isCurrentChosen */
	public boolean isCurrentChosen = false;
	public boolean isTempStopUpload = false;
}
