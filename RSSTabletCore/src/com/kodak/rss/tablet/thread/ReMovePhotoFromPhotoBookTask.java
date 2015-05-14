package com.kodak.rss.tablet.thread;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.upload.ImageResource;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class ReMovePhotoFromPhotoBookTask extends AsyncTask<String, Void, Object>{	
	private Context mContext;		
	private InfoDialog waitingDialog;
	private Photobook mPhotobook;		
	private ImageResource mImageResource;
	private Layer addNeweLayer;
	private boolean isHave = false;
	
	public ReMovePhotoFromPhotoBookTask(Context context,ImageResource imageResource,Layer layer) {
		this.mContext = context;				
		this.mImageResource = imageResource;
		this.addNeweLayer = layer;
		mPhotobook = PhotoBookProductUtil.getCurrentPhotoBook();	
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mImageResource != null) {
			isHave = PhotoBookProductUtil.isInlayer(mImageResource);			
		}else {
			isHave = PhotoBookProductUtil.isInPagelayer(addNeweLayer);				
		}
		if (isHave) {
			waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
			.setProgressBar(true)				
			.create();
			waitingDialog.show();			
		}			
	}
	
	@Override
	protected Object doInBackground(String... params) {	
		if (!isHave) return null;
		PhotobookWebService pbService = new PhotobookWebService(mContext);
		List<PhotobookPage> pages = new ArrayList<PhotobookPage>(2);
		String imageResourceId = "";
		if (mImageResource != null) {
			imageResourceId = mImageResource.id;
		}else {
			imageResourceId = addNeweLayer.contentId;
		}
		
		try {
			if (PhotoBookProductUtil.isInlayer(imageResourceId)) {
				PhotobookPage page;
				page = pbService.removeContentFromBookTask(mPhotobook.id, imageResourceId);
				if (page != null) {
					pages.add(page);
				}	
			}
			
			if (mImageResource != null && mImageResource.copyIds != null && mImageResource.copyIds.size() > 0 ) {
				List<String> delList = new ArrayList<String>();
				for (String id : mImageResource.copyIds) {
					if (PhotoBookProductUtil.isInlayer(id)) {
						PhotobookPage newPage = null;
						newPage = pbService.removeContentFromBookTask(mPhotobook.id, id);
						if (newPage != null) {
							delList.add(id);
							int item = -1;
							for (int i = 0; i < pages.size(); i++) {
								PhotobookPage  dealPage = pages.get(i);
								if (dealPage != null && dealPage.id.equals(newPage.id)) {
									item = i;					
									break;
								}
							}
							if (item >= 0) {
								pages.set(item, newPage);		
							}else {
								pages.add(newPage);
							}	
						}				
					}				
				}
				mImageResource.copyIds.removeAll(delList);
			}
			
			if (addNeweLayer != null && addNeweLayer.copyIds != null && addNeweLayer.copyIds.size() > 0 ) {
				List<String> delList = new ArrayList<String>();
				for (String id : addNeweLayer.copyIds) {
					if (PhotoBookProductUtil.isInlayer(id)) {
						PhotobookPage newPage = null;
						newPage = pbService.removeContentFromBookTask(mPhotobook.id, id);
						if (newPage != null) {
							delList.add(id);
							int item = -1;
							for (int i = 0; i < pages.size(); i++) {
								PhotobookPage  dealPage = pages.get(i);
								if (dealPage != null && dealPage.id.equals(newPage.id)) {
									item = i;					
									break;
								}
							}
							if (item >= 0) {
								pages.set(item, newPage);		
							}else {
								pages.add(newPage);
							}	
						}
					}				
				}
				addNeweLayer.copyIds.removeAll(delList);
			}
			return pages;
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
					((BaseNetActivity) mContext).showErrorWarning((RssWebServiceException) result);
				}
			}else if(result != null){
				List<PhotobookPage> list = (List<PhotobookPage>) result;
				for (int i = 0; i < list.size(); i++) {
					PhotobookPage newPage = list.get(i);
					if (newPage != null) {	
						PhotoBookProductUtil.updatePageInPhotobook(newPage, true);								
					}
				}
			}
			if (mContext instanceof PhotoBooksProductActivity) {
				((PhotoBooksProductActivity)mContext).notifyPhotoBookPagesChanged();
			}
		}
	}	
	
}
