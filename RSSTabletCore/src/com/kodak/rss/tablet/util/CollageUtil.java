package com.kodak.rss.tablet.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.DisplayMetrics;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.collage.AlternateLayout;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.collage.Element;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.RssTabletApp;

public class CollageUtil extends ProductUtil{

	/**proDescId : the product Description id*/
	public static float getWHRation(Collage collage) {
		float wHRatio = 1f;
		if (collage == null) return wHRatio;
		CollagePage collagePage = collage.page;
		if (collage.page != null) {
			float pw = collagePage.width;
			float ph = collagePage.height;
			if (pw == 0 || ph == 0) return wHRatio;
			wHRatio = ph/pw;
		}else {
			wHRatio = getWHRation(collage.proDescId);
		}
		return wHRatio;
	}
	
	public static void refreshSucPageInCollage(String pageId,int refreshNum){
		Collage collage = getCurrentCollage();
		if(collage!=null){
			synchronized (collage) {			
				CollagePage page = collage.page;										
				page.mainRefreshSuc(refreshNum);									
				return;				
			}
		}
	}
	
	public static void addImageToCollage(ImageInfo addInfo){
		if (addInfo == null) return;
		Collage collage = getCurrentCollage();
		synchronized (collage) {
			collage.chosenpics.add(addInfo);
		}			
	}
	
	private static List<AlternateLayout> getPageLayoutList(List<AlternateLayout> alternateLayouts){
		List<AlternateLayout> alternateLayoutList = null;
		if (alternateLayouts == null) return alternateLayoutList;		
		boolean isAddBlank = false;
		alternateLayoutList = new ArrayList<AlternateLayout>();			
		for (AlternateLayout alternateLayout : alternateLayouts) {
			if (alternateLayout == null) continue;
			if (alternateLayout.elements == null) continue;	
			boolean isBlank = true;
			for (Element element : alternateLayout.elements) {
				ROI mRoi = element.location;
				if (mRoi.x != 0 || mRoi.y != 0 || mRoi.w != 0 || mRoi.h != 0) {
					isBlank = false;
				}		
			}			
			if (isBlank) {
				if (!isAddBlank) {
					alternateLayoutList.add(alternateLayout);	
				}
				isAddBlank = true;
			}else {
				alternateLayoutList.add(alternateLayout);	
			}					
		}						
		return alternateLayoutList;
	}
	
	//if change the page ,must call the function to replace old data
	public static boolean updatePageInCollage(CollagePage newPage,boolean isChange,boolean isUseNewLayout){
		Collage collage = getCurrentCollage();
		if(collage != null){
			synchronized (collage) {				
				CollagePage oldPage = collage.page;
				newPage.baseURI = oldPage.baseURI;					
				newPage.setPageRefresh(oldPage.getMainRefreshCount(), oldPage.getMainRefreshSucCount());				
				if (isUseNewLayout && newPage.alternateLayouts != null) {	
					newPage.alternateLayouts = getPageLayoutList(newPage.alternateLayouts);
					if (newPage.alternateLayouts.size() > 0) {
						newPage.alternateLayouts.get(0).isCheck = true;
					}											
				}else {
					newPage.alternateLayouts = oldPage.alternateLayouts;		
				}		
				if (isChange) {
					newPage.setPageRefresh();
				}
				collage.page = newPage;
				return true;
			}			
		}
		return false;
	}
	
	public  static void updateCollage(Collage collage,boolean isUseNewLayout){
		if (collage == null) return;		
		CollagePage newPage = collage.page;	
		if (newPage == null) return;
		Collage currentCollage = getCurrentCollage();
		if(currentCollage != null){
			synchronized (currentCollage) {				
				CollagePage oldPage = currentCollage.page;									
				newPage.setPageRefresh(oldPage.getMainRefreshCount(), oldPage.getMainRefreshSucCount());				
				if (isUseNewLayout && newPage.alternateLayouts != null) {	
					newPage.alternateLayouts = getPageLayoutList(newPage.alternateLayouts);
					if (newPage.alternateLayouts.size() > 0) {
						newPage.alternateLayouts.get(0).isCheck = true;
					}											
				}else {
					newPage.alternateLayouts = oldPage.alternateLayouts;		
				}						
				newPage.setPageRefresh();								
			}			
		}
		setCurrentCollage(collage);						
	}		
	
	public static String getContentId(ImageInfo info){
		String contentId = null;		
		if (info == null) return contentId;
		if (info.imageOriginalResource == null) return contentId;
		String tempContentId = info.imageOriginalResource.id;
		Collage collage = getCurrentCollage();
		if(collage != null){
			synchronized (collage) {
				CollagePage page = collage.page;
				if (page != null && page.layers != null) {
					for (Layer layer : page.layers) {
						if (layer == null) continue;
						if (layer.contentId == null) continue;
						if (layer.contentId.equals(tempContentId)) {
							contentId = tempContentId;
							break;
						}						
					}
				}
			}
		}		
		return contentId;
	}
	
	public static List<ImageInfo> getAddList(List<ImageInfo> infoList){
		List<ImageInfo> addList = new ArrayList<ImageInfo>();
		if (infoList == null) return addList;
		Collage collage = getCurrentCollage();
		if(collage != null){
			synchronized (collage) {
				ArrayList<ImageInfo> imageInfoList = collage.chosenpics;
				for (ImageInfo info : infoList) {
					if (info == null) continue;
					for (ImageInfo gInfo : imageInfoList) {
						if (gInfo == null) continue;
						if (info.id.equals(gInfo.id)) {
							addList.add(gInfo);
						}							
					}
				}
			}
		}		
		return addList;		
	}
	
	public static void setLayoutsData(String layoutId){	
		Collage collage = getCurrentCollage();
		if(collage != null){
			synchronized (collage) {	
				List<AlternateLayout> alternateLayouts = collage.page.alternateLayouts;
				if (alternateLayouts == null) return;
				int size = alternateLayouts.size();
				for (int i = 0; i < size; i++) {
					AlternateLayout layout = alternateLayouts.get(i);
					if (layout == null) continue;
					layout.isCheck = false;	
					if (layoutId != null && layout.layoutId.equals(layoutId)) {
						layout.isCheck = true;	
					}			
				}	
			}
		}		
	}
	
	public static String getCheckLayoutId(){	
		String currentCheckLayoutId = null;
		Collage collage = getCurrentCollage();
		if(collage != null){
			synchronized (collage) {	
				List<AlternateLayout> alternateLayouts = collage.page.alternateLayouts;
				if (alternateLayouts != null) {
					int size = alternateLayouts.size();				
					for (int i = 0; i < size; i++) {
						AlternateLayout layout = alternateLayouts.get(i);
						if (layout == null) continue;						
						if (layout.isCheck) {
							currentCheckLayoutId = layout.layoutId;	
						}			
					}					
				}				
			}
		}
		return currentCheckLayoutId;
	}

	public static void setCheckTheme(String themeId){		
		Collage collage = getCurrentCollage();
		if(collage != null){
			synchronized (collage) {	
				collage.themeId = themeId;								
			}
		}		
	}
	
	public static String getCheckThemeId(){	
		String currentCheckThemeId = null;
		Collage collage = getCurrentCollage();
		if(collage != null){
			synchronized (collage) {	
				currentCheckThemeId = collage.themeId;								
			}
		}	
		return currentCheckThemeId;
	}
	
	public static boolean getThemeCheckState(String themeId){	
		boolean isCheck = false;
		if (themeId == null || "".equals(themeId)) return isCheck;;
		String checkThemeId = getCheckThemeId();
		if (checkThemeId != null && checkThemeId.equals(themeId)) {
			isCheck = true;				
		}		
		return isCheck;
	}
	
	public static boolean isArriveMax(Layer[] layers, List<ImageInfo> infoList, int maxNumber){	
		boolean isCheck = false;
		int haveNum = 0;		
		if (layers != null) {
			for (Layer layer : layers) {
				if (layer == null) continue;
				if (!isUseImage(layer,infoList)) {
					haveNum += 1;
				}					
			}			
		}
		if (infoList != null) {
			haveNum += infoList.size();
		}
		isCheck = haveNum >= maxNumber;		
		return isCheck;
	}
	
	public static void resetCollage(){		
		Collage collage = getCurrentCollage();
		if(collage != null && collage.page != null && collage.chosenpics != null){
			synchronized (collage) {	
				Layer[] layers = collage.page.layers;
				List<ImageInfo> deleteInfoList = new ArrayList<ImageInfo>(3);				
				for (ImageInfo info : collage.chosenpics) {
					if (!isUseImage(info, layers)) {
						deleteInfoList.add(info);
					}		
				}
				collage.chosenpics.removeAll(deleteInfoList);					
			}
		}	
	}
	
	public static boolean isUseImage(ImageInfo imageInfo, Layer[] layers){	
		boolean isUseImage = false;	
		if (imageInfo == null) return isUseImage;		
		if (imageInfo.imageOriginalResource == null) return isUseImage;
		if (layers == null) return isUseImage;		
		for (Layer layer : layers) {
			if (layer == null) continue;			
			if (layer.contentId == null) continue;			
			if (imageInfo.imageOriginalResource.id.equals(layer.contentId) ) {
				isUseImage = true;
				break;
			}				
		}				
		return isUseImage;
	}	
	
	public static boolean isUseImage(Layer layer, List<ImageInfo> infoList){	
		boolean isUseImage = false;	
		if (layer == null) return isUseImage;		
		if (layer.contentId == null) return isUseImage;
		if (infoList == null) return isUseImage;		
		for (ImageInfo info : infoList) {
			if (info == null) continue;			
			if (info.imageOriginalResource == null) continue;	
			if (info.imageOriginalResource.id == null) continue;	
			if (info.imageOriginalResource.id.equals(layer.contentId) ) {
				isUseImage = true;
				break;
			}				
		}				
		return isUseImage;
	}	
	
	public static void addCurrentCollage(Collage collage){		 
		if (RssTabletApp.getInstance().collageList == null) {
			RssTabletApp.getInstance().collageList = new ArrayList<Collage>();
		}
		if (collage == null) return;
		collage.isCurrentChosen = true;
		if (collage.chosenpics == null) {
			collage.chosenpics = new ArrayList<ImageInfo>();
		}
		for (Collage colla : RssTabletApp.getInstance().collageList) {
			if (colla != null && colla.isCurrentChosen) {
				colla.isCurrentChosen = false;				
			}
		}				
		RssTabletApp.getInstance().collageList.add(collage);				
	}
	
	public static void setCurrentCollage(Collage collage){
		if (collage == null) return;
		if (RssTabletApp.getInstance().collageList == null) {
			RssTabletApp.getInstance().collageList = new ArrayList<Collage>();
		}
		collage.isCurrentChosen = true;
		Collage currentCollage = getCurrentCollage();			
		if (currentCollage != null ) {
			collage.themeId = currentCollage.themeId;
			collage.chosenpics = currentCollage.chosenpics;
			RssTabletApp.getInstance().collageList.remove(currentCollage);			
		}
		RssTabletApp.getInstance().collageList.add(collage);				
	}
	
	public static Collage getCurrentCollage(){
		Collage currentCollage = null;
		if (RssTabletApp.getInstance().collageList == null) {
			RssTabletApp.getInstance().collageList = new ArrayList<Collage>();
		}
		for (Collage colla : RssTabletApp.getInstance().collageList) {
			if (colla != null && colla.isCurrentChosen) {
				currentCollage = colla;
				if (currentCollage.chosenpics == null) {
					currentCollage.chosenpics = new ArrayList<ImageInfo>();
				}
				break;
			}
		}
		return currentCollage;
	}
	
	public static void dealWithItem(Context context,Collage collage, int num){
		if (collage == null) return;		
		if (RssTabletApp.getInstance().products == null) {
			RssTabletApp.getInstance().products = new ArrayList<ProductInfo>();
		}			
		ProductInfo dealInfo = null;														
		for(ProductInfo pInfo : RssTabletApp.getInstance().products) {
			if (pInfo != null && AppConstants.collageType.equals(pInfo.productType)) {				
				if (pInfo.correspondId.equals(collage.id) ) {												
					dealInfo = pInfo;
					break;												
				}
			}
		}											
		if (dealInfo != null) {
			if (num < 1) {
				RssTabletApp.getInstance().products.remove(dealInfo);
			}else {
				dealInfo.chosenImageList = collage.chosenpics;
				setDisplayPath(dealInfo, collage, context);
				dealInfo.num = num;
			}			
		}else if (num > 0 ){
			dealInfo = new ProductInfo();
			dealInfo.descriptionId = collage.proDescId;
			dealInfo.num = num;
			dealInfo.productType = AppConstants.collageType;
			dealInfo.correspondId = dealInfo.cartItemId = collage.id;
			dealInfo.chosenImageList = collage.chosenpics;	
			setDisplayPath(dealInfo, collage, context);
			RssTabletApp.getInstance().products.add(dealInfo);
		}
	}
	
	private static void setDisplayPath(ProductInfo dealInfo,Collage collage,Context context){		
		if (dealInfo.downloadDisplayImageUrl != null) return;		
		if (collage.page == null) return;					
		float ratio = 1f;
		if (collage.page.width > 0 && collage.page.height > 0  ) {
			 ratio = collage.page.height/collage.page.width;
		}
		DisplayMetrics dm = context.getResources().getDisplayMetrics();	
		int pageWidth = (int) ((dm.widthPixels - dm.density*50)/8f);							
		int pageHeight = (int) (pageWidth*ratio);	
		String displayId = "";
		
		if (collage.page != null ) {
			displayId = collage.page.id;
			dealInfo.displayImageUrl = displayId;
			dealInfo.downloadDisplayImageUrl = getUrl(collage.page, pageWidth, pageHeight).toString();
		}		
	}
	
	public static boolean findNotFilledFixedPage(Collage collage) {
		if (collage == null || collage.page == null) return false;					
		CollagePage page = collage.page;			
		if (page.minNumberOfImages <= 0) return false;	
		
		if (page.layers == null || page.layers.length < page.minNumberOfImages) return true;								
		return false;
	}
	
	public static boolean isBlank(Collage collage) {
		if (collage == null || collage.page == null) return false;	
		CollagePage page = collage.page;
		if (page.layers == null || page.layers.length > 0) return false;				
		return true;
	}
	
	public static void deleteImageInfo(String layerType,String contentId){
		if (layerType == null || contentId  == null) return;		
		Collage currentCollage = getCurrentCollage();
		synchronized (currentCollage) {	
			ImageInfo deleteInfo = null;
			if (layerType.equals(Layer.TYPE_IMAGE)&& currentCollage.chosenpics != null&& currentCollage.chosenpics.size() > 0) {
				for (ImageInfo info : currentCollage.chosenpics) {
					if (info == null ) continue;
					if (info.imageOriginalResource == null ) continue;	
					if (info.imageOriginalResource.id == null ) continue;	
					if (info.imageOriginalResource.id.equals(contentId)){
						deleteInfo = info;
						break;
					}
				}
				if (deleteInfo != null) {
					currentCollage.chosenpics.remove(deleteInfo);
				}
			}			
		}	
	}				

	public static String getLayerImageInfoPath(Layer layer){
		if (layer == null) return null;
		if (layer.type == null) return null;
		if (!Layer.TYPE_IMAGE.equals(layer.type)) return null;			
		Collage curentCollage = getCurrentCollage();
		ArrayList<ImageInfo> chosenpics = curentCollage.chosenpics;				
		ImageInfo imageInfo = getLayerImageInfo(layer, chosenpics, false);		
		if (imageInfo != null) return imageInfo.editUrl;
		return null;
	}
	
}
