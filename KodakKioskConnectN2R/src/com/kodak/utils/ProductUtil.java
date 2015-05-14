package com.kodak.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.util.Log;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Data;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Page;

public class ProductUtil{
	public final static String TAG = "ProductUtil:";	


	
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
//	
//	public static boolean isSymbiosis(String Id, ImageResource imageResource) {
//		boolean isSymbiosis = false;
//		if (imageResource == null) return isSymbiosis;
//		isSymbiosis = isSymbiosis(Id,imageResource.id);
//		if (!isSymbiosis && imageResource.copyIds != null && imageResource.copyIds.size() > 0) {
//			for (String id :  imageResource.copyIds) {
//				isSymbiosis = isSymbiosis(Id,id);
//				if (isSymbiosis) {
//					break;
//				}					
//			}
//		}			
//		return isSymbiosis;
//	}
	
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
	
//	public static ImageInfo getLayerImageInfo(Layer layer,ArrayList<ImageInfo> nationPhotos){						
//		ImageInfo info = null;
//		if (layer.contentId == null) return info;		
//		if (nationPhotos == null) return info;
//		for (int i = 0; i < nationPhotos.size(); i++) {
//			ImageInfo dInfo = nationPhotos.get(i);	
//			if (dInfo != null && isSymbiosis(layer.contentId,dInfo.imageThumbnailResource)) {
//				info = dInfo;
//				break;
//			}
//		}			
//		return info;
//	}	
	
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
		return AppContext.getApplication().getTempImageFolderPath() +  "/.layer_cache_l_"+layer.contentId+".jpg";
	}
	
	public static String getLayerImageSuperHighResCacheFilePath(Layer layer){
		return AppContext.getApplication().getTempImageFolderPath() +  "/.layer_cache_sh_"+layer.contentId+".jpg";
	}
	
	
	
	
	
	
	
	
	
	
}
