package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class GreetingCardProduct {
	private final String TAG = GreetingCardProduct.class.getSimpleName();
	
	public static final String GREETING_CARD = "GreetingCard";

	public static final String ID = "Id";
	public static final String PRODUCT_DESC_BASE_URI = "ProductDescriptionBaseURI";
	public static final String PRODUCT_DESC_ID = "ProductDescriptionId";
	public static final String THEME = "Theme";
	public static final String PAGES = "Pages";
	public static final String PAGE = "Page";
	public static final String IS_DUPLEX = "IsDuplex";
	public static final String MIN_NUM_OF_PAGES = "MinNumberOfPages";
	public static final String MAX_NUM_OF_PAGES = "MaxNumberOfPages";
	public static final String NUM_OF_PAGES_PER_BASE_CARD = "NumberOfPagesPerBaseCard";
	public static final String MIN_NUM_OF_IMAGES = "MinNumberOfImages";
	public static final String MAX_NUM_OF_IMAGES = "MaxNumberOfImages";
	public static final String MAX_NUM_OF_IMAGES_PER_ADDED_PAGE = "MaxNumberOfImagesPerAddedPage";
	public static final String MAX_NUM_OF_IMAGES_PER_BASE_CARD = "MaxNumberOfImagesPerBaseCard";
	public static final String IDEAL_NUM_OF_IMAGES_PER_BASE_CARD = "IdealNumberOfImagesPerBaseCard";
	public static final String NUM_OF_IMAGES_IN_CARD = "NumberOfImagesInCard";
	public static final String NUM_OF_UNASSIGNED_IMAGES = "NumberOfUnassignedImages";
	public static final String SUGGESTED_CAPTION_VISIBILITY = "SuggestedCaptionVisibility";
	public static final String CAN_SET_TITLE = "CanSetTitle";
	public static final String CAN_SET_SUBTITLE = "CanSetSubtitle";
	public static final String CAN_SET_AUTHOR = "CanSetAuthor";
	
	public String id;
	public String productDescriptionBaseURI;
	public String productDescriptionId;
	public String theme;
	public GreetingCardPage[] pages;
	public boolean isDuplex;
	public int minNumberOfPages;
	public int maxNumberOfPages;
	public int numberOfPagesPerBaseCard;
	public int minNumberOfImages;
	public int maxNumberOfImages;
	public Object maxNumberOfImagesPerAddedPage;
	public int maxNumberOfImagesPerBaseCard;
	public int idealNumberOfImagesPerBaseCard;
	public int numberOfImagesInCard;
	public Object numberOfUnassignedImages;
	public boolean suggestedCaptionVisibility;
	public boolean canSetTitle;
	public boolean canSetSubtitle;
	public boolean canSetAuthor;
	/*private  LinkedHashMap<String, Bitmap> previewsText;
	private LinkedHashMap<String, Bitmap> previews;*/
	private  LinkedHashMap<String, String> previewsTextByPath;
	private LinkedHashMap<String, String> previewsByPath;
	private final int EDITFLAG = 16;
	private final int PREVIEWFLAG = 17;
	public int count = 1;
	
	public void replacePage(String id, GreetingCardPage newPage){
		if(pages!=null && pages.length>0){
			for(int i=0; i<pages.length; i++){
				if(pages[i].id.equals(id)){
					pages[i] = newPage;
					break;
				}
			}
		}
	}
	
	public GreetingCardPage getPageById(String contentId){
		if(pages != null){
			for(GreetingCardPage page : pages){
				if(page.id.equals(contentId)){
					return page;
				}
			}
			Log.e(TAG, "can not find the page[" + contentId + "] in detail[" + id + "].");
			return null;
		} else {
			Log.e(TAG, "card detail[" + id + "]" + " has no pages.");
			return null;
		}
		
	}
	
	
	
	protected void putPagePreviewByPath(String pageId, String filePath, int flag){
		Log.i(TAG, "!!!!!!!!!!! putPagePreview flag = " + flag);
		if (flag == EDITFLAG) {
			if(previewsTextByPath == null){
				previewsTextByPath = new LinkedHashMap<String, String>();
			}
			previewsTextByPath.put(pageId, filePath);
		} else if (flag == PREVIEWFLAG) {
			if(previewsByPath == null){
				previewsByPath = new LinkedHashMap<String, String>();
			}
			previewsByPath.put(pageId, filePath);
		}
	}

	
	public Bitmap getPagePreviewByPath(String pageId, int flag){
		Bitmap result = null;
		String filePath = null;
		if (flag == EDITFLAG) {
			filePath = previewsTextByPath.get(pageId);
		} else if (flag == PREVIEWFLAG) {
			filePath = previewsByPath.get(pageId);
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		result = BitmapFactory.decodeFile(filePath, options);
		return result;
	}
	
	public GreetingCardPage getPageBySequenceNumber(int sequenceNumber){
		if(pages != null){
			for(GreetingCardPage page : pages){
				if(page.sequenceNumber == sequenceNumber){
					return page;
				}
			}
			Log.e(TAG, "can not find the page[" + sequenceNumber + "] in detail[" + id + "].");
			return null;
		} else {
			Log.e(TAG, "card detail[" + id + "]" + " has no pages.");
			return null;
		}
	}
	
	
	
	@Override
	public String toString() {
		String toString = "GreetingCardProduct[\n" 
			+ ID + ": " +id + "\n,"
			+ PRODUCT_DESC_BASE_URI + ": " + productDescriptionBaseURI + "\n,"
			+ PRODUCT_DESC_ID + ": " + productDescriptionId + "\n,"
			+ THEME + ": " + theme + "\n,";
		if(pages!=null){
			for(GreetingCardPage page : pages){
				toString += page.toString();
			}
		}	
		toString += IS_DUPLEX + ": " + isDuplex + "\n,"
			+ MIN_NUM_OF_PAGES + ": " + minNumberOfPages + "\n,"
			+ MAX_NUM_OF_PAGES + ": " + maxNumberOfPages + "\n,"
			+ NUM_OF_PAGES_PER_BASE_CARD + ": " + numberOfPagesPerBaseCard + "\n,"
			+ MIN_NUM_OF_IMAGES + ": " + minNumberOfImages + "\n,"
			+ MAX_NUM_OF_IMAGES + ": " + maxNumberOfImages + "\n,"
			+ MAX_NUM_OF_IMAGES_PER_BASE_CARD + ": " + maxNumberOfImagesPerBaseCard + "\n,"
			+ IDEAL_NUM_OF_IMAGES_PER_BASE_CARD + ": " + idealNumberOfImagesPerBaseCard + "\n,"
			+ NUM_OF_IMAGES_IN_CARD + ": " + numberOfImagesInCard + "\n,"
			//+ NUM_OF_UNASSIGNED_IMAGES + ": " + numberOfUnassignedImages==null?"null":numberOfUnassignedImages.toString() + "\n,"
			+ SUGGESTED_CAPTION_VISIBILITY + ": " + suggestedCaptionVisibility + "\n,"
			+ CAN_SET_TITLE + ": " + canSetTitle + "\n,"
			+ CAN_SET_SUBTITLE + ": " + canSetSubtitle + "\n,"
			+ CAN_SET_AUTHOR + ": " + canSetAuthor + "\n,";
		toString += "]";
		return toString;
	}
	
}
