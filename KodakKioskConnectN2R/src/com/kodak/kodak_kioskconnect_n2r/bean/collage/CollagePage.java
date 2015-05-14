package com.kodak.kodak_kioskconnect_n2r.bean.collage;

import java.util.ArrayList;
import java.util.List;

import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Page;

public class CollagePage extends Page{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_PAGE_TYPE = "PageType";
	public static final String FLAG_LAYOUT_TYPE = "LayoutType";
	
	public static final String FLAG_BackgroundImageId = "BackgroundImageId";
	public static final String FLAG_BackgroundImageBaseURI = "BackgroundImageBaseURI";
	public static final String FLAG_AlternateLayouts = "AlternateLayouts";
	
	public String pageType = "";
	public String layoutType = "";
	public int minNumberOfImages;
	public int maxNumberOfImages;

	public String backgroundImageId = "";
	public String backgroundImageBaseURI = "";
	
	public List<AlternateLayout> alternateLayouts;
	
	
	private List<PhotoInfo> photosInCollagePage ;
	
	private int textLayerNumber ;
	
	
	
	/*
	 * 
	 */
	public boolean isPhotoInCollagePage(PhotoInfo photo){
		boolean isPhotoInCollagePage = false ;
		if(photosInCollagePage==null){
			photosInCollagePage = new ArrayList<PhotoInfo>() ;
		}
		
		for (PhotoInfo photoInList : photosInCollagePage) {
			if(photo.equals(photoInList) && photoInList.isInsertedForCollagePage() ){
				isPhotoInCollagePage = true ;
				break ;
			}
		}
		
		return isPhotoInCollagePage ;
		
	}



	public List<PhotoInfo> getPhotosInCollagePage() {
	
		return photosInCollagePage;
	}



	public void setPhotosInCollagePage(List<PhotoInfo> photosInCollagePage) {
		this.photosInCollagePage = photosInCollagePage;
	}
	
   public List<PhotoInfo> getUnInsertedPhotos(){
	   if(photosInCollagePage==null){
			photosInCollagePage = new ArrayList<PhotoInfo>() ;
		}
	   List<PhotoInfo> photosUnInserted = new ArrayList<PhotoInfo>() ;
	   for (PhotoInfo photo : photosInCollagePage) {
		   if(!photo.isInsertedForCollagePage()){
			   photosUnInserted.add(photo) ;
		   }
	   }
	   
	   return photosUnInserted ;
   }
	
    public void addPhotoToList(PhotoInfo photo){
    	if(photosInCollagePage==null){
			photosInCollagePage = new ArrayList<PhotoInfo>() ;
		}
    	if(!photosInCollagePage.contains(photo)){
    		photosInCollagePage.add(photo) ;
    	}
    	
    	
    }
    
    public void removePhotoFromList(PhotoInfo photo){
    	if(photosInCollagePage==null){
    		photosInCollagePage = new ArrayList<PhotoInfo>() ;
    	}
    	
    	if(photosInCollagePage.contains(photo) && !photo.isInsertedForCollagePage()){
    		photosInCollagePage.remove(photo) ;
    	}
    	
    	
    }
    
    
    public void updatePhotoInsertStateAfterInsertSuccess(List<PhotoInfo> photos){
    	
    	if(photosInCollagePage==null || photosInCollagePage.size()==0){
    		return  ;
    	}
    	
    	for (PhotoInfo photoInfo : photosInCollagePage) {
			
    		if(photos.contains(photoInfo)){
    			photoInfo.setInsertedForCollagePage(true) ;
    		}
    		
		}
    }
    
    
    public boolean isPortrait(){
		
    	return width < height ;
    	
    	
    }



	public int getTextLayerNumber() {
		return textLayerNumber;
	}



	public void setTextLayerNumber(int textLayerNumber) {
		this.textLayerNumber = textLayerNumber;
	}
	

}
