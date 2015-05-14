package com.kodak.rss.tablet.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.bean.content.Theme.BackGround;
import com.kodak.rss.core.n2r.bean.prints.Data;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.prints.Page;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.ProductDescription;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.core.n2r.bean.upload.ImageResource;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.RssTabletApp;

public class ProductUtil{
	public final static String TAG = "ProductUtil:";	

	/**proDescId : the product Description id*/
	public static RssEntry getRssEntry(String proDescId){
		RssEntry rssEntry = null;
		if (proDescId == null) return rssEntry;	
		RssTabletApp app = RssTabletApp.getInstance();
		boolean isJump = false;
		List<Catalog> catalogs = app.getCatalogList();
		if (catalogs != null) {
			int catalogsSize = catalogs.size();
			for (int i = 0; i < catalogsSize; i++) {
				if (isJump) break;							
				Catalog catalog = catalogs.get(i);
				if (catalog.rssEntries != null) {		
					for (int j = 0; j < catalog.rssEntries.size(); j++) {
						RssEntry tempRssEntry = catalog.rssEntries.get(j);
						if (tempRssEntry != null && tempRssEntry.proDescription != null) {
							if (proDescId.equals(tempRssEntry.proDescription.id)) {
								rssEntry = tempRssEntry;
								isJump = true;
								break;
							}
						}
					}	
				}
			}					
		}				
		return rssEntry;
	}
	
	/**proDescId : the product Description id*/
	public static String getProductName(String proDescId){
		String productName = null;
		RssEntry rssEntry = getRssEntry(proDescId);
		if (rssEntry != null) {
			productName = rssEntry.proDescription.name;		
		}										
		return productName;
	}
	
	/**proDescId : the product Description id*/
	public static float getWHRation(String proDescId) {
		float wHRatio = 1f;
		if (proDescId == null) return wHRatio;		
		RssEntry rssEntry = getRssEntry(proDescId);
		if (rssEntry != null && rssEntry.proDescription != null) {
			ProductDescription proDescription = rssEntry.proDescription;
			float pageWidth = proDescription.pageWidth;
			float pageHeight = proDescription.pageHeight;
			pageWidth = pageWidth > 0 ? pageWidth : 1f ;
			pageHeight = pageHeight > 0 ? pageHeight : 1f;								
			wHRatio = pageHeight/pageWidth;
		}
		return wHRatio;
	}
	
	public static URI getURI(RssEntry rssEntry){
		URI pictureURI = null;
		try {
			if (rssEntry != null && rssEntry.proDescription != null) {			
				String url = rssEntry.proDescription.lgGlyphURL;
				url= url.replaceAll(" ", "%20");
				pictureURI = new URI(url);			
			}
		} catch (URISyntaxException e) {
			pictureURI = null;
		}
		return pictureURI;
	}
	
	public static String getUrl(Page page, int width, int height) {		
		String url = null;
		if (page != null && !"".equals(page.id.trim())) {
			url = page.baseURI+page.id+"/preview?maxWidth="+width+"&maxHeight="+height;
			url= url.replaceAll(" ", "%20");
		}
		return url;
	}
	
	public static URI getURI(Page page, int width, int height) {
		URI pictureURI = null;
		String url = getUrl(page, width, height);
		if (url != null) {
			try {
				pictureURI = new URI(url);
			} catch (URISyntaxException e) {
				Log.e(TAG, "parse URI error:"+e);				
			}
		}
		return pictureURI;
	}
	
	public static URI getURI(Layer layer,int width,int height){
		URI pictureURI = null;
		try {
			if (layer != null && !"".equals(layer.contentId.trim())) {			
				String url = layer.contentBaseURI+layer.contentId+"/preview?maxWidth="+width+"&maxHeight="+height;
				url= url.replaceAll(" ", "%20");
				pictureURI = new URI(url);			
			}
		} catch (URISyntaxException e) {
			pictureURI = null;
		}
		return pictureURI;
	}	
	
	public static final String LOW_RES_URL_PARAMS = "maxWidth=358&maxHeight=258";
	public static final String HI_RES_URL_PARAMS = "maxWidth=716&maxHeight=516";
	public static final String SUPER_HIGH_RES_URL_PARAMS = "maxWidth=3000&maxHeight=2000";
	
	public static String getLayerLowResUrl(Layer layer){
		return layer.contentBaseURI + layer.contentId+"/preview?"+LOW_RES_URL_PARAMS;
	}
	
	public static String getLayerHighResUrl(Layer layer){
		return layer.contentBaseURI + layer.contentId+"/preview?"+HI_RES_URL_PARAMS;
	}
	
	public static String getLayerSuperHighResUrl(Layer layer){
		return layer.contentBaseURI + layer.contentId+"/preview?"+SUPER_HIGH_RES_URL_PARAMS;
	}
	
	public static boolean isSymbiosis(String Id, String closenId) {
		boolean symbiosisFlag = false;
		if (Id == null || closenId == null) return symbiosisFlag;
		if (Id.equals(closenId)) {
			symbiosisFlag = true;
		}
		return symbiosisFlag;
	}
	
	public static boolean isSymbiosis(String Id, ImageResource imageResource) {
		boolean isSymbiosis = false;
		if (imageResource == null) return isSymbiosis;
		isSymbiosis = isSymbiosis(Id,imageResource.id);
		if (!isSymbiosis && imageResource.copyIds != null && imageResource.copyIds.size() > 0) {
			for (String id :  imageResource.copyIds) {
				isSymbiosis = isSymbiosis(Id,id);
				if (isSymbiosis) {
					break;
				}					
			}
		}			
		return isSymbiosis;
	}
	
	public static boolean isSymbiosis(String Id, Layer layer) {
		boolean isSymbiosis = false;
		if (layer == null) return isSymbiosis;
		isSymbiosis = isSymbiosis(Id,layer.contentId);
		if (!isSymbiosis && layer.copyIds != null && layer.copyIds.size() > 0) {
			for (String id :  layer.copyIds) {
				isSymbiosis = isSymbiosis(Id,id);
				if (isSymbiosis) {
					break;
				}					
			}
		}			
		return isSymbiosis;
	}
	
	public static ImageInfo getLayerImageInfo(Layer layer,ArrayList<ImageInfo> nationPhotos){						
		ImageInfo info = null;
		if (layer.contentId == null) return info;		
		if (nationPhotos == null) return info;
		for (int i = 0; i < nationPhotos.size(); i++) {
			ImageInfo dInfo = nationPhotos.get(i);	
			if (dInfo != null && isSymbiosis(layer.contentId,dInfo.imageThumbnailResource)) {
				info = dInfo;
				break;
			}
		}			
		return info;
	}	
	
	public static Layer getLayerInfo(Layer layer,ArrayList<Layer> chosenLayers){						
		Layer info = null;
		if (layer.contentId == null) return info;		
		if (chosenLayers == null) return info;
		for (int i = 0; i < chosenLayers.size(); i++) {
			Layer lInfo = chosenLayers.get(i);	
			if (lInfo != null && isSymbiosis(layer.contentId,lInfo)) {
				info = lInfo;
				break;
			}
		}			
		return info;
	}	
	
	public static int getViewWidth(int defaultWidth,int num ,float totelWidth, int rightMargin){
		if (num < 1) return defaultWidth;
		int singleWidth = 0;		
		singleWidth = (int) ((totelWidth - num *rightMargin)/num*1f);	
		singleWidth = singleWidth > defaultWidth ? defaultWidth : singleWidth;		
		return singleWidth;
	}
	
	public static ROI getImageCropROI(Layer layer){
		ROI roi = null;		
		if(layer != null && layer.data != null){
			for(int i=0;i<layer.data.length;i++){
				if(Data.FLAG_ROI_VAL.equals(layer.data[i].valueType)){
					try{
						roi = (ROI) layer.data[i].value;
						break;
					}catch(Exception e){
						Log.e(TAG, "", e);
					}
				}
			}
		}		
		return roi;
	}
	
	
	
	public static ProductInfo getPInfo(String pInfoCorrespondId,String productType){
		if (productType == null) return null;	
		if (pInfoCorrespondId == null) return null;		
		if (RssTabletApp.getInstance().products == null) {
			RssTabletApp.getInstance().products = new ArrayList<ProductInfo>();
		}			
		ProductInfo dealInfo = null;														
		for(ProductInfo pInfo : RssTabletApp.getInstance().products) {
			if (pInfo != null && productType.equals(pInfo.productType)) {				
				if (pInfo.correspondId.equals(pInfoCorrespondId) ) {												
					dealInfo = pInfo;
					dealInfo.quantityIncrement = RssTabletApp.getInstance().getQuantityIncrement(dealInfo.descriptionId);
					break;												
				}
			}
		}
		return dealInfo;
	}
	
	public static boolean isLayerCaptionAdded(Layer layer){
		if(layer.data!=null){
			for(int i=0;i<layer.data.length;i++){
				if(Data.TYPE_CAPTIONTEXT.equals(layer.data[i].name)){
					return true;
				}
			}
		}
		return false;
	}
	
	public static String getLayerImageLowResCacheFilePath(Layer layer){
		return RssTabletApp.getInstance().getTempImageFolderPath() +  "/.layer_cache_l_"+layer.contentId+".jpg";
	}
	
	public static String getLayerImageSuperHighResCacheFilePath(Layer layer){
		return RssTabletApp.getInstance().getTempImageFolderPath() +  "/.layer_cache_sh_"+layer.contentId+".jpg";
	}
	
	
	public static ImageInfo getLayerImageInfo(Layer layer,ArrayList<ImageInfo> chosenpics,boolean isUseThum){						
		ImageInfo info = null;
		if (layer == null) return info;	
		if (layer.contentId == null) return info;	
		if (chosenpics == null) return info;	
		for (ImageInfo imageInfo : chosenpics) {
			if (imageInfo == null) continue;
			if (isUseThum) {
				if (imageInfo.imageThumbnailResource != null && layer.contentId.equals(imageInfo.imageThumbnailResource.id)) {					
					info = imageInfo;
					break;										
				}
			}else {
				if (imageInfo.imageOriginalResource != null && layer.contentId.equals(imageInfo.imageOriginalResource.id)) {					
					info = imageInfo;
					break;										
				}
			}						
		}			
		return info;
	}	
		
	public static List<BackGround> fillBackGrouds(List<Theme> themes){
		List<BackGround> backGroundList = null;
		if (themes == null ) return backGroundList;
		int size = 0;
		for (int i = 0; i < themes.size(); i++) {
			Theme theme =  themes.get(i);
			if (theme != null && theme.backGrounds !=null) {
				size +=	theme.backGrounds.length;
			}		
		}		
		backGroundList = new ArrayList<BackGround>(size);
		List<String> idList = new ArrayList<String>(size);;
		for (int i = 0; i < themes.size(); i++) {
			Theme theme =  themes.get(i);
			if (theme != null && theme.backGrounds !=null) {
				for (int j = 0; j < theme.backGrounds.length; j++) {
					if (!idList.contains(theme.backGrounds[j].id)) {
						idList.add(theme.backGrounds[j].id);
						backGroundList.add(theme.backGrounds[j]);
					}	
				}
			}	
		}	
		return backGroundList;
	}	
	
	public static List<Theme> fillThemes(List<Theme> themes){
		List<Theme> themeList = null;
		if (themes == null ) return themeList;
		int size = themes.size();		
		themeList = new ArrayList<Theme>(size);
		List<String> idList = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			Theme theme = themes.get(i);
			if (theme != null && theme.id != null) {	
				if (theme.backGrounds != null && theme.backGrounds.length == 1) {				
					if (!idList.contains(theme.id)) {
						idList.add(theme.id);
						themeList.add(theme);
					}						
				}	
			}			
		}	
		return themeList;
	}	
	
}
