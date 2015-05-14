package com.kodak.rss.tablet.util;

import java.util.ArrayList;
import java.util.List;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.tablet.RssTabletApp;

public class UploadProgressUtil {
	
	public static int getUploadPicSuccessNum(List<ImageInfo> list,boolean isThumbnail){
		int mSuccessNum = 0;
		if (list == null) return 0;			
		int size = list.size();		
		for (int i = 0; i < size; i++) {					
			ImageInfo pbInfo = list.get(i);
			if (isThumbnail) {
				if (pbInfo.imageThumbnailResource != null) {
					mSuccessNum++;							
				}
			}else {
				if (pbInfo.imageOriginalResource != null) {
					mSuccessNum++;							
				}
			}			
		}					
		return mSuccessNum;
	}
	
	public static long[] getUploadPicSuccessNum(List<ImageInfo> list){
		long[] info = new long[2];		
		if (list == null) return info;
		int mSuccessNum = 0;
		long uploadSucTime = 0;
		synchronized (list) {			
			int size = list.size();		
			for (int i = 0; i < size; i++) {					
				ImageInfo pbInfo = list.get(i);			
				if (pbInfo.imageOriginalResource != null) {
					mSuccessNum++;						
					if (uploadSucTime < pbInfo.uploadOriginalSucTime) {
						uploadSucTime = pbInfo.uploadOriginalSucTime;
					}
				}						
			}					
		}
		info[0] = mSuccessNum;
		info[1] = uploadSucTime;
		return info;
	}	

	public static boolean getUploadPicFail(List<ImageInfo> list,boolean isThumbnail){
		boolean isHaveFail = false;
		if (list == null) return true;		
		int size = list.size();		
		for (int i = 0; i < size; i++) {					
			ImageInfo pbInfo = list.get(i);
			if (isThumbnail) {
				if ( pbInfo.imageThumbnailResource == null && pbInfo.isHavedThumbnailUpload) {
					isHaveFail = true;	
					break;					
				}
			}else {
				if (pbInfo.imageOriginalResource == null && pbInfo.isHavedOriginalUpload) {
					isHaveFail = true;	
					break;					
				}
			}					
		}							
		return isHaveFail;
	}
	
	public static ImageInfo getUploadPicFailImageInfo(List<ImageInfo> list,boolean isThumbnail){
		ImageInfo failInfo = null;
		if (list == null) return failInfo;		
		int size = list.size();		
		for (int i = 0; i < size; i++) {					
			ImageInfo pbInfo = list.get(i);
			if (isThumbnail) {
				if ( pbInfo.imageThumbnailResource == null && pbInfo.isHavedThumbnailUpload) {
					failInfo = pbInfo;	
					break;					
				}
			}else {
				if (pbInfo.imageOriginalResource == null && pbInfo.isHavedOriginalUpload) {
					failInfo = pbInfo;	
					break;					
				}
			}					
		}							
		return failInfo;
	}	
	
	public static ImageInfo getRunningUploadInfo(List<ImageInfo> list,boolean isThumbnail){
		ImageInfo info = null;
		if (list == null) return null;	
		int size = list.size();		
		for (int i = 0; i < size; i++) {					
			ImageInfo pbInfo = list.get(i);
			if (isThumbnail) {
				if (pbInfo.imageThumbnailResource == null && !pbInfo.isHavedThumbnailUpload) {
					info = pbInfo;
					break;
				}		
			}else {
				if (pbInfo.imageOriginalResource == null && !pbInfo.isHavedOriginalUpload) {
					info = pbInfo;
					break;
				}		
			}				
		}					
		return info;
	}	
		
	public static boolean isImageUploading(List<ImageInfo> images,boolean isThumbnail){
		boolean haveImageForWaitting = true;
		if(images.size() == 0){
			return false;
		}
		for(ImageInfo image : images){
			if (isThumbnail) {
				if(image.imageThumbnailResource == null){
					haveImageForWaitting = true;
					break;
				}
			}else {
				if(image.imageOriginalResource == null){
					haveImageForWaitting = true;
					break;
				}
			}			
			haveImageForWaitting = false;
		}
		return haveImageForWaitting;
	}
	
	public static List<ImageInfo> allImages(){
		List<ImageInfo> images = new ArrayList<ImageInfo>();
		List<ImageInfo> printsList = RssTabletApp.getInstance().chosenList;
		if(printsList!=null && printsList.size()>0){
			images.addAll(printsList);
		}
				
		List<Photobook> chosenBookList = RssTabletApp.getInstance().chosenBookList;
		if (chosenBookList != null) {
			for (int i = 0; i < chosenBookList.size(); i++) {
				Photobook book = chosenBookList.get(i);
				if (book != null && book.chosenpics != null && book.chosenpics.size()>0) {					
					images.addAll(book.chosenpics);					
				}
			}
		}
		
		List<GreetingCard> chosenCardList = RssTabletApp.getInstance().gCardList;
		if (chosenCardList != null) {
			for (int i = 0; i < chosenCardList.size(); i++) {
				GreetingCard card = chosenCardList.get(i);
				if (card != null && card.chosenpics != null && card.chosenpics.size()>0) {					
					images.addAll(card.chosenpics);					
				}
			}
		}
		
		List<Calendar> chosenCalendarList = RssTabletApp.getInstance().calendarList;
		if (chosenCalendarList != null) {
			for (int i = 0; i < chosenCalendarList.size(); i++) {
				Calendar calendar = chosenCalendarList.get(i);
				if (calendar != null && calendar.chosenpics != null && calendar.chosenpics.size()>0) {					
					images.addAll(calendar.chosenpics);					
				}
			}
		}
		
		List<Collage> chosenCollageList = RssTabletApp.getInstance().collageList;
		if (chosenCollageList != null) {
			for (int i = 0; i < chosenCollageList.size(); i++) {
				Collage collage = chosenCollageList.get(i);
				if (collage != null && collage.chosenpics != null && collage.chosenpics.size()>0) {					
					images.addAll(collage.chosenpics);					
				}
			}
		}
		return images;
	}
	
	public static boolean isContainsPrints(List<ProductInfo> products){
		boolean containPrints = false;
		for(ProductInfo proInfo : products){
			if(proInfo.productType.equalsIgnoreCase(ProductInfo.PRO_TYPE_PRINT)){
				containPrints = true;
				break;
			}
		}
		return containPrints;
	}
}
