package com.kodak.rss.tablet.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.thread.AddCopyPhotoToPageTask;
import com.kodak.rss.tablet.thread.BackageSetPageThemeTask;
import com.kodak.rss.tablet.thread.MovePageTask;
import com.kodak.rss.tablet.thread.MovePhotoTask;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;

public class PhotoBookDragTaskHandler extends Handler{	
	
	private PhotoBooksProductActivity activity;
	
	public PhotoBookDragTaskHandler(PhotoBooksProductActivity activity){
		this.activity = activity;
	}
	
	@Override
	public void handleMessage(Message msg) {
		if (activity == null) return;
		if (activity.isFinishing()) return;		
		final int action = msg.what;			
		switch (action) {	
		case AppConstants.ActionMovePageFlag:				
			String[] strArray = (String[]) msg.obj;
			String fromPageId = strArray[0];
			String toPageId = strArray[1];
			MovePageTask task = new MovePageTask(activity, fromPageId, toPageId);
			task.execute();				
			break;			
		case AppConstants.ActionSetBackgroudFlag:	
			Bundle bundle = msg.getData();
			if (bundle != null ) {
				String backgroudId = bundle.getString("mBackgroudId");
				String mThemeId = bundle.getString("mThemeId");					
				boolean isLeft = bundle.getBoolean("isLeft");
				int pos = isLeft ? 0:1;
				int position = pos == 0 ? 1:0;					
				PhotobookPage[] currentPages = PhotoBookProductUtil.getCurrentPages(activity.pbLayout.getCurrentPosition());
				currentPages[position] = null;
				if (currentPages != null && PhotoBookProductUtil.getPhotobookPageEditable(currentPages[pos])) {						
					BackageSetPageThemeTask setPageThemeTask = new BackageSetPageThemeTask(activity, currentPages, backgroudId,mThemeId);
					setPageThemeTask.execute();								
				}	
			}
			break;	
		case AppConstants.ActionAddCopyFlag:	
			Bundle AddBundle = msg.getData();
			if (AddBundle != null ) {
				String mImageInfoId = AddBundle.getString("mImageInfoId");
				String mLayerId = AddBundle.getString("mLayerId");					
				boolean isLeft = AddBundle.getBoolean("isLeft");
				int pos = isLeft ? 0:1;								
				Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
				int currentPos = activity.pbLayout.getCurrentPosition();
				PhotobookPage[] pageItems = PhotoBookProductUtil.getPageItems(currentPhotoBook).get(currentPos);
				PhotobookPage page = pageItems[pos];
				if (page != null) {
					ImageInfo dealInfo = null;
					for (int i = 0; i < currentPhotoBook.chosenpics.size(); i++) {
						ImageInfo mInfo = currentPhotoBook.chosenpics.get(i);
						if (mImageInfoId.equals(mInfo.id)) {
							dealInfo = mInfo;
							break;
						}
					}	
					Layer dealLayer = null;
					if (currentPhotoBook.chosenLayers != null) {
						for (int i = 0; i < currentPhotoBook.chosenLayers.size(); i++) {
							Layer layer = currentPhotoBook.chosenLayers.get(i);
							if (mLayerId.equals(layer.contentId)) {
								dealLayer = layer;
								break;
							}
						}
					}
					if (dealInfo != null || dealLayer != null) {
						boolean isDisplayMove = PhotoBookProductUtil.isDiaplayMove(pageItems);
						if (isDisplayMove) {
							MovePhotoTask movePhotoTask =  new MovePhotoTask(activity, page, dealInfo, dealLayer);		
							movePhotoTask.execute();					
						}else if (PhotoBookProductUtil.getPhotobookPageEditable(page)) {													
							AddCopyPhotoToPageTask  addCopyTask = new AddCopyPhotoToPageTask(activity, page, dealInfo,dealLayer);
							addCopyTask.execute();																
						}			
					}										
				}
			}
			break;				
		}
	}
	
	
}
