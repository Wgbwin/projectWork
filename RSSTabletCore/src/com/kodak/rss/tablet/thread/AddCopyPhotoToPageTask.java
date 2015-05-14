package com.kodak.rss.tablet.thread;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.upload.ImageResource;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class AddCopyPhotoToPageTask extends AsyncTask<String, Void, Object>{
	
	private static final String TAG = "AddCopyPhotoToPageTask:";
	private Context mContext;		
	private InfoDialog waitingDialog;
	private Photobook mPhotobook;		
	private ImageInfo imageInfo;
	private ImageResource imageThumbnailResource;	
	private PhotobookPage mPage;
	private Layer addNeweLayer;
	
	public AddCopyPhotoToPageTask(Context context,PhotobookPage page, ImageInfo imageInfo,Layer layer) {
		this.mContext = context;				
		this.addNeweLayer = layer;
		this.mPage = page;	
		this.imageInfo = imageInfo;	
		mPhotobook = PhotoBookProductUtil.getCurrentPhotoBook();
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (PhotoBookProductUtil.getIsNotNullNum(mPage.layers) < Integer.valueOf(mPage.maxNumberOfImages)) {
			waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
			.setProgressBar(true)				
			.create();
			waitingDialog.show();
		}		
	}
		
	@Override
	protected Object doInBackground(String... params) {		
		if (imageInfo != null) {
			imageThumbnailResource = imageInfo.imageThumbnailResource;
			while (imageThumbnailResource == null) {	
				if (imageInfo.isHavedThumbnailUpload) {
					imageThumbnailResource = imageInfo.imageThumbnailResource;
					break;
				}								
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {										
				}
			}							
		}
		if (imageThumbnailResource == null && addNeweLayer == null) return null;				
		if (PhotoBookProductUtil.getIsNotNullNum(mPage.layers) >= Integer.valueOf(mPage.maxNumberOfImages)) return null;
		PhotobookWebService pbService = new PhotobookWebService(mContext);
		PhotobookPage page = null;
		String imageResourceId = "";
		if (imageThumbnailResource != null) {
			imageResourceId = imageThumbnailResource.id;
		}else {
			imageResourceId = addNeweLayer.contentId;
		}
		String imageId = null;
		try {
			imageId = pbService.cloneImageTask(imageResourceId);
			if (imageId != null) {
				if (imageThumbnailResource != null) {
					if (imageInfo.imageThumbnailResource.copyIds == null) {
						imageInfo.imageThumbnailResource.copyIds = new ArrayList<String>();
					}
					imageInfo.imageThumbnailResource.copyIds.add(imageId);				
				}else {
					if (addNeweLayer.copyIds == null) {
						addNeweLayer.copyIds = new ArrayList<String>();
					}
					addNeweLayer.copyIds.add(imageId);	
				}
				ArrayList<String> imageIds = new ArrayList<String>(2);
				imageIds.add(imageId);
				page =  pbService.insertContentTask(mPhotobook.id, mPage.id, imageIds);
			}
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}	
		return page;
	}

	@Override
	protected void onPostExecute(Object  result) {
		super.onPostExecute(result);
		if(mContext != null && !((Activity)mContext).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}		
			if(result != null && (result instanceof PhotobookPage)){	
				Log.i(TAG, "succeed.");				
				PhotoBookProductUtil.updatePageInPhotobook((PhotobookPage) result, true);
				if (mContext instanceof PhotoBooksProductActivity) {
					((PhotoBooksProductActivity)mContext).notifyPhotoBookPagesChanged();
				}

			}else if (PhotoBookProductUtil.getIsNotNullNum(mPage.layers) >= Integer.valueOf(mPage.maxNumberOfImages)){								
				new InfoDialog.Builder(mContext).setMessage(R.string.page_enough_prompt)			
				.setNegativeButton(R.string.d_ok, null)
				.create()
				.show();		
			}else if(result instanceof RssWebServiceException){
				((PhotoBooksProductActivity)mContext).showErrorWarning((RssWebServiceException) result);
			}
		}
	}	

}
