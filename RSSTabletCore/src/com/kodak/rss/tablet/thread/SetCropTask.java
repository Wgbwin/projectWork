package com.kodak.rss.tablet.thread;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Data;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.prints.Page;
import com.kodak.rss.core.n2r.webservice.CollageWebService;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class SetCropTask extends AsyncTask<String, Void, Object>{
		
	private Context mContext;		
	private InfoDialog waitingDialog;		
	private String layerId;
	private ROI cropROI;
	private Page page;
	private String pageId;	
	
	public SetCropTask(Context context,String layerId,Page page, ROI cropROI) {
		this.mContext = context;						
		this.layerId = layerId;				
		this.page = page;
		if (page != null) {
			this.pageId = page.id;	
		}		
		this.cropROI = cropROI;			
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (!(cropROI == null || page == null || layerId == null )){
			waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait).setProgressBar(true).create();
			waitingDialog.show();
		} 		
	}
		
	@Override
	protected Object doInBackground(String... params) {
		if (cropROI == null || pageId == null || layerId == null) return null;
		try {
			if (page instanceof CollagePage) {	
				Layer selectedLayer = null;
				if (page.layers != null) {
			    	for(Layer layer : page.layers){
			    		if (layer == null ) continue;
			    		if (layer.contentId == null ) continue;
			    		if (layer.contentId.equals(layerId)) {
			    			selectedLayer = layer;
			    			break;
			    		}
					}
				}
				if (selectedLayer == null) return null;
				CollageWebService cService = new CollageWebService(mContext);											
				ROI oldRoi = CollageUtil.getImageCropROI(selectedLayer);				
			     
			    ROI newRoi= new ROI();		     
			    newRoi.ContainerW = oldRoi.ContainerW;
			    newRoi.ContainerH = oldRoi.ContainerH;
			    newRoi.w = cropROI.w * oldRoi.ContainerW;
			    newRoi.h = cropROI.h * oldRoi.ContainerH;
			    newRoi.x = cropROI.x * oldRoi.ContainerW;
			    newRoi.y = cropROI.y * oldRoi.ContainerH;
			    
			    if (newRoi.x + newRoi.w > newRoi.ContainerW) {
			    	newRoi.w = newRoi.ContainerW - newRoi.x;
				}
			    if (newRoi.y + newRoi.h > newRoi.ContainerH) {
			    	newRoi.h = newRoi.ContainerH - newRoi.y;
				}	
			    cService.setCropTask(layerId, newRoi);
				for(int i=0;i<selectedLayer.data.length;i++){
					if(Data.FLAG_ROI_VAL.equals(selectedLayer.data[i].valueType)){
						selectedLayer.data[i].value = newRoi;
					}
				}
				boolean isUseNewLayout= false;					
				if (oldRoi.w != newRoi.w || oldRoi.h != newRoi.h) {					
					CollagePage collagePage = cService.layoutCollagePageTask(page.id,true);
					if (collagePage != null) {
						page = collagePage;	
						isUseNewLayout = true;
					}
				}
				CollageUtil.updatePageInCollage((CollagePage) page, true, isUseNewLayout);
				return page;
			}else if (page instanceof PhotobookPage){
				PhotobookPage page = null;
				PhotobookWebService pbService = new PhotobookWebService(mContext);								
				pbService.setCropTask(layerId,cropROI);
				page = pbService.layoutPageTask(pageId);
				if (page != null) {
					PhotoBookProductUtil.updatePageInPhotobook(page, true);
				}				
				return page;
			}
			return null;
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}	
	}

	@Override
	protected void onPostExecute(Object  result) {
		super.onPostExecute(result);
		if(mContext != null && !((Activity)mContext).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}			
		
			if(result instanceof RssWebServiceException){
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			}else if(result != null){													
				Intent intent = new Intent();			
				intent.putExtra("fromCrop", true);
				((Activity)mContext).setResult(Activity.RESULT_OK,intent);	
				((Activity)mContext).finish();
			}else {								
				new InfoDialog.Builder(mContext).setMessage(R.string.try_again_crop)			
				.setNegativeButton(R.string.d_yes, null)
				.create()
				.show();		
			}			
		}
	}	

}
