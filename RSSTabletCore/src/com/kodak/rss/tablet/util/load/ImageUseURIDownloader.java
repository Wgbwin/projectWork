package com.kodak.rss.tablet.util.load;

import java.net.URI;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.View;

public class ImageUseURIDownloader {
	
	private Map<String, Request> pendingRequests;
	private Context mContext;	
	private onProcessImageResponseListener onProcessImageResponseListener;		
	private boolean isThumbnail;	
	private String saveType;
	public int[] viewParameters;
	
	public ImageUseURIDownloader(Context context,Map<String, Request> pendingRequests,onProcessImageResponseListener Listener) {		
		this.mContext = context;
		this.pendingRequests = pendingRequests;
		this.onProcessImageResponseListener = Listener;		
	}
	
	public ImageUseURIDownloader(Context context,Map<String, Request> pendingRequests) {		
		this.mContext = context;
		this.pendingRequests = pendingRequests;		
	}
	
	public void setOnProcessImageResponseListener(onProcessImageResponseListener Listener){
		this.onProcessImageResponseListener = Listener;		
	}

	/**save file_Path for Use URI to download network file */
	public void setSaveType(String flowType){
		this.saveType =  FilePathConstant.printType;
		if (flowType != null) {
			this.saveType = flowType;
		}
	}
	
	/**isThumbnail The photo loading is thumbnail_image*/
	public void setIsThumbnail(boolean isThumbnail){
		this.isThumbnail = isThumbnail;
	}
	
	/**return Bitmap mapping view*/
	public void setViewParameters(int width,int height){
		if (this.viewParameters == null) {
			this.viewParameters = new int[2];  
		}
		viewParameters[0]  = width;
		viewParameters[1]  = height;
	}
	
		
	public void downloadProfilePicture(String profileId, String filePath, View view, int position){
		downloadPicture(profileId, null, filePath, view, position, true, null, isThumbnail, null,null,0);	
	}
	
	/**
	 * pictureURI the network file path
	 * view diaplayView
	 * position InAdapterView position
	 * setAllowCached if false must use the pictureURI to load the network file;if true then first judge the native file is or not exist,exist get the native file directly,not exist load the network file		
	 */
	public void downloadProfilePicture(String profileId, URI pictureURI, View view, int position,boolean setAllowCached) {		
		downloadPicture(profileId, pictureURI,null, view, position, setAllowCached, null, isThumbnail, null,null,0);		
	}	
		
	/**
	 * pictureURI the network file path
	 * view diaplayView
	 * position view InAdapterView position	
	 * setAllowCached if false must use the pictureURI to load the network file;if true then first judge the native file is or not exist,exist get the native file directly,not exist load the network file	 
	 * isThumbnail this Request will save the thumbnail-image	
	 * */
	public void downloadProfilePicture(String profileId, URI pictureURI, View view, int position,boolean setAllowCached,boolean isThumbnail) {		
		downloadPicture(profileId, pictureURI,null, view, position, setAllowCached, null, isThumbnail, null,null,0);
	}	
	
	/**
	 * pictureURI the network file path
	 * view diaplayView
	 * position view InAdapterView position	
	 * setAllowCached if false must use the pictureURI to load the network file;if true then first judge the native file is or not exist,exist get the native file directly,not exist load the network file	 
	 * isThumbnail this Request will save the thumbnail-image	
	 * flowType load Data in the business type
	 * */
	public void downloadProfilePicture(String profileId, URI pictureURI, View view, int position,boolean setAllowCached,boolean isThumbnail, String flowType) {			
		downloadPicture(profileId, pictureURI,null, view, position, setAllowCached, flowType, isThumbnail, null,null,0);
	}	
	
	/**
	 * pictureURI the network file path
	 * view diaplayView
	 * position view InAdapterView position	
	 * setAllowCached if false must use the pictureURI to load the network file;if true then first judge the native file is or not exist,exist get the native file directly,not exist load the network file	 
	 * isThumbnail this Request will save the thumbnail-image	
	 * flowType load Data in the business type
	 * productId business Id
	 * */
	public void downloadProfilePicture(String profileId, URI pictureURI, View view, int position,boolean setAllowCached,boolean isThumbnail, String flowType,String productId) {			
		downloadPicture(profileId, pictureURI,null, view, position, setAllowCached, flowType, isThumbnail, productId,null,0);
	}	

	/**
	 * pictureURI the network file path
	 * view diaplayView
	 * position view InAdapterView position	
	 * setAllowCached if false must use the pictureURI to load the network file;if true then first judge the native file is or not exist,exist get the native file directly,not exist load the network file	 
	 * isThumbnail this Request will save the thumbnail-image	
	 * flowType load Data in the business type
	 * productId business Id
	 * saveType save file path
	 * */
	public void downloadProfilePicture(String profileId, URI pictureURI, View view, int position,boolean setAllowCached,boolean isThumbnail, String flowType,String productId,String saveType) {			
		downloadPicture(profileId, pictureURI,null, view, position, setAllowCached, flowType, isThumbnail, productId,saveType,0);
	}	
	
	/**
	 * pictureURI the network file path
	 * view diaplayView
	 * position view InAdapterView position	
	 * setAllowCached if false must use the pictureURI to load the network file;if true then first judge the native file is or not exist,exist get the native file directly,not exist load the network file	 		
	 * flowType load Data in the business type
	 * productId business Id
	 * saveType save file path
	 * */
	public void downloadProfilePicture(String profileId, URI pictureURI, View view, int position,boolean setAllowCached, String productId, int refreshCount) {		
		downloadPicture(profileId, pictureURI,null, view, position, setAllowCached, null, isThumbnail, productId,null,refreshCount);		
	}
	//for photobook
	public void downloadProfilePicture(String profileId, URI pictureURI, View view, int position,boolean setAllowCached, String productId) {		
		downloadPicture(profileId, pictureURI,null, view, position, setAllowCached, null, isThumbnail, productId,null,0);		
	}	
	
	/**
	 * pictureURI the network filePath
	 * filePath the native filePath
	 * */
	private void downloadPicture(final String profileId, URI pictureURI, final String filePath, final View view,final int position,boolean setAllowCached,final String flowType,
			boolean isThumbnail,final String productId,final String saveType,final int refreshCount){
		if (profileId == null) return;			
		if (pictureURI == null && filePath == null) {			
			try {
				Integer.valueOf(profileId);
			} catch (Exception e) {
				return;
			}
		}				
		String pendKey = isThumbnail ? FilePathConstant.thumbnail+profileId:profileId;
		if (productId != null) {
			pendKey = productId + pendKey;
		}	
		if (refreshCount > 0) {
			for (int i = 0; i < refreshCount; i++) {
				String tmpPendKey = null;
				if (i == 0) {
					tmpPendKey = pendKey;
				}else {
					tmpPendKey = i + pendKey;
				}
				Request request = pendingRequests.get(tmpPendKey);
				if (request != null) {					
					Log.d("cancelRequest", "cancelRequest refreshCount:" + refreshCount);					
					ImageDownloader.cancelRequest(request);
					pendingRequests.remove(tmpPendKey);
				}	
			}			
			pendKey = refreshCount + pendKey;
		}
		Request request = pendingRequests.get(pendKey);
		if (request != null) {
			ImageDownloader.prioritizeRequest(request);			
		}else {	
			Request.Builder builder = null;
			if (pictureURI != null) {
				builder = new Request.Builder(mContext.getApplicationContext(), profileId, pictureURI);
			}else {
				builder = new Request.Builder(mContext.getApplicationContext(), profileId, filePath);
			}
			builder.setAllowCached(setAllowCached).setThumbnail(isThumbnail).setRefreshCount(refreshCount).setCallerTag(this).setCallback(new Request.Callback() {
				@Override
				public void onCompleted(Response response) {
					if (response != null && response.getRequest() != null) {
						removeRequest(response, productId);
						if (onProcessImageResponseListener != null) {
							onProcessImageResponseListener.onProcess(response, profileId, view, position,flowType,productId);					
						}	
					}				
				}
			});
			Request newRequest = builder.build();			
			pendingRequests.put(pendKey, newRequest);
			String saveFolderName = saveType;
			if (saveFolderName == null ) {
				saveFolderName = this.saveType;
				if (saveFolderName == null) {
					saveFolderName =  FilePathConstant.printType;
				}
			}
			if (viewParameters == null) {				
				this.viewParameters = new int[2];  				
			}		
			ImageDownloader.downloadAsync(newRequest,saveFolderName,viewParameters);				
		}										
	}
	
	private void removeRequest(Response response ,String productId){
		if (pendingRequests == null) return;
		String profileId = response.getRequest().getImageId();
		boolean isThumbnail = response.getRequest().isThumbnail();
		int refreshCount = response.getRequest().getRefreshCount();		
		String pendKey = getRequestKey(profileId, productId, refreshCount,isThumbnail);
		pendingRequests.remove(pendKey);
		return;
	}
	
	public void cancelRequest(String imageId,String productId,int refreshCount){
		if (pendingRequests == null) return;	
		if (imageId == null) return;		
		if("".equals(imageId)) return;	
		String pendKey = getRequestKey(imageId, productId, refreshCount,isThumbnail);
		Request request = pendingRequests.get(pendKey);
		if (request != null) {	        	
	       ImageDownloader.cancelRequest(request);
	       pendingRequests.remove(pendKey);				     	
	    }  				        
	}

	public void prioritizeRequest(String imageId,String productId,int refreshCount){
		if (pendingRequests == null) return;
		if (imageId == null) return;		
		if("".equals(imageId)) return;
		String pendKey = getRequestKey(imageId, productId, refreshCount,isThumbnail);
		Request request = pendingRequests.get(pendKey);
		if (request != null) {	        	
	       ImageDownloader.prioritizeRequest(request);	       	     	
	    }  				        
	}
	
	private String getRequestKey(String imageId,String productId,int refreshCount,boolean isThumbnail){
		String pendKey = isThumbnail ? FilePathConstant.thumbnail + imageId : imageId;
		if (productId != null && !"".equals(productId)) {
			pendKey = productId + pendKey;
		}		
		if (refreshCount > 0) {
			pendKey = refreshCount + pendKey;
		}				
		return pendKey;
	}
}
