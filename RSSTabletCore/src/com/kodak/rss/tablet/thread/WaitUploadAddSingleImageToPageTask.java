package com.kodak.rss.tablet.thread;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseActivity;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.DialogUploadImageError;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class WaitUploadAddSingleImageToPageTask extends AsyncTask<String, Void, Object>{
	private static final String TAG = "WaitUploadAddImagesTask:";	
	private Context mContext;	
	private InfoDialog waitingDialog;
	private PhotobookPage currentPage;
	private ImageInfo mInfo;
	private Layer addNeweLayer;	
	private String layerId;
	
	public WaitUploadAddSingleImageToPageTask(Context context,PhotobookPage page,ImageInfo info ,Layer layer){
		this.mContext = context;				
		this.currentPage = page;
		this.mInfo = info;
		this.addNeweLayer = layer;	
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (PhotoBookProductUtil.getIsNotNullNum(currentPage.layers) < Integer.valueOf(currentPage.maxNumberOfImages)) {
			waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
			.setProgressBar(true)				
			.create();
			waitingDialog.show();
		}		
	}
	
	@Override
	protected Object doInBackground(String... params) {				
		if (currentPage == null ) return false;
		if (currentPage.layers == null ) return false;
		if (PhotoBookProductUtil.getIsNotNullNum(currentPage.layers) >= Integer.valueOf(currentPage.maxNumberOfImages)) return false;		
		boolean isSuccess = false;		
		if (addNeweLayer != null) {
			isSuccess = true;
		}else {
			while (mInfo.imageThumbnailResource == null) {	
				if (mInfo.imageThumbnailResource == null && mInfo.isHavedThumbnailUpload) break;								
				try {
					Thread.sleep(900);
				} catch (InterruptedException e) {}				
			}							
			if (mInfo.imageThumbnailResource != null) isSuccess = true;			
		}
		if (!isSuccess) return false;
		PhotobookWebService pbService = new PhotobookWebService(mContext);
//		String layerId = "";
		if (addNeweLayer != null) {
			layerId = addNeweLayer.contentId;
		}else {
			layerId = mInfo.imageThumbnailResource.id;
		}
		Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		PhotobookPage newPage = null;
		try {
			newPage = pbService.addImageToPageTask(currentPhotoBook.id, currentPage, layerId);
			if (newPage != null) {
				PhotoBookProductUtil.updatePageInPhotobook(newPage, true);					
			}else {
				layerId = "";
			}				
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}		
		return true;
	}

	@Override
	protected void onPostExecute(Object  result) {
		super.onPostExecute(result);
		if(mContext!= null && !((Activity)mContext).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}				
		
			if(result instanceof RssWebServiceException){
				Log.e(TAG, "add image to page fail.");				
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			}else if(result instanceof Boolean){
				if((Boolean)result){			
					Log.i(TAG, "succeed.");
					if (mContext instanceof PhotoBooksProductActivity) {
						((PhotoBooksProductActivity)mContext).notifyPhotoBookPagesChanged();
						((PhotoBooksProductActivity) mContext).removeGiveUpItem(layerId);
					}
				}else {
					if (PhotoBookProductUtil.getIsNotNullNum(currentPage.layers) >= Integer.valueOf(currentPage.maxNumberOfImages)){								
						new InfoDialog.Builder(mContext).setMessage(R.string.page_enough_prompt)			
						.setNegativeButton(R.string.d_yes, null)
						.create()
						.show();		
					}else if (mInfo != null && mInfo.imageThumbnailResource == null && !((BaseActivity)mContext).isHaveUploadErrorDialog) {									
						new DialogUploadImageError().initDialogUploadImageError(mContext,mInfo);
					}			
				}
			}

		}
	}	
	
}	