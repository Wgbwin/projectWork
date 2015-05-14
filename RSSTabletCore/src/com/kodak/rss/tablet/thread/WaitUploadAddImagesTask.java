package com.kodak.rss.tablet.thread;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseActivity;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.UploadProgressUtil;
import com.kodak.rss.tablet.view.dialog.DialogUploadImageError;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class WaitUploadAddImagesTask extends AsyncTask<String, Void, Object>{
	private static final String TAG = "WaitUploadAddImagesTask:";	
	private Context mContext;	
	private InfoDialog waitingDialog;	
	private int size;
	private String[] addMoreImages;
	private Photobook currentPhotoBook;
	
	public static final int Add_End_Flg = 1;
	public static final int Relayout_Flg = 2;	
	private int mAction;
	private ImageInfo failInfo;
	private int addPageNum;
	private int haveDuplexFillerNum;
	public WaitUploadAddImagesTask(Context context,int action,String[] moreImages){
		this.mContext = context;
		this.mAction = action;		
		this.addMoreImages = moreImages;		
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		//fixed RSSMOBILEPDC-1896 by bing
		haveDuplexFillerNum = PhotoBookProductUtil.getFillerPageNum(currentPhotoBook);
	}
		
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.i(TAG, "Start upload add images ...");	
		size = currentPhotoBook.chosenpics.size();		
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true)				
		.create();
		waitingDialog.show();				
	}
	
	@Override
	protected Object doInBackground(String... params) {		
		boolean isUploadAll = true;				
		int sucNum = UploadProgressUtil.getUploadPicSuccessNum(currentPhotoBook.chosenpics,true);								
		while (size > sucNum) {	
			failInfo = UploadProgressUtil.getUploadPicFailImageInfo(currentPhotoBook.chosenpics,true);
			if (failInfo != null ) {						
				isUploadAll = false;
				break;
			}																
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {}
			size = currentPhotoBook.chosenpics.size();
			sucNum = UploadProgressUtil.getUploadPicSuccessNum(currentPhotoBook.chosenpics,true);
		}									
		if (!isUploadAll) return false;	
		
		if (addMoreImages == null ) return false;
		int newAddNum = addMoreImages.length;
		if (newAddNum == 0 ) return false;
		ArrayList<String> newAddImageResources = new ArrayList<String>(newAddNum);
		for (int i = 0; i < newAddNum; i++) {
			String key = addMoreImages[i];
			for (ImageInfo info : currentPhotoBook.chosenpics) {
				if (info.imageThumbnailResource != null && key.equals(info.id)) {
					newAddImageResources.add(info.imageThumbnailResource.id);
					break;
				}		
			}
		}
		
		if (newAddImageResources.size() == 0) return false;
		PhotobookWebService pbService = new PhotobookWebService(mContext);
		try {
			// mResourceIds is the new add list  addPhotosToBookTask
			pbService.addPhotosToBookTask(currentPhotoBook.id, newAddImageResources);
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
			
		switch (mAction) {
		case Add_End_Flg:
			//call insertPageWithContent2Task.
			Photobook phtotbook = null;					
			String previousPageId = null;			
			int num = currentPhotoBook.pages.size();			
			PhotobookPage previousPage = null;
			for (int i = num - 1; i >= 0; i--) {
				PhotobookPage page = currentPhotoBook.pages.get(i);
				if (page != null && page.pageType != null && PhotobookPage.TYPE_STANDARD.equals(page.pageType)) {
					previousPage = page;
					if (i+1 < num - 1) {
						previousPageId = currentPhotoBook.pages.get(i+1).id;
					}else {
						previousPageId = page.id;
					}
					break;
				}
			}
										
			if (previousPageId != null) {
				int maxNum = Integer.valueOf(previousPage.maxNumberOfImages);
				int totelNum = newAddImageResources.size();												
				if (totelNum > maxNum) {
					int tNum = 0;
					ArrayList<ArrayList<String>> addImageResourcesList = new ArrayList<ArrayList<String>>(2);							
					while (tNum*maxNum < totelNum) {
						ArrayList<String> addImageResources = new ArrayList<String>(maxNum);
						for (int i = 0; i < maxNum ; i++) {
							if (tNum*maxNum+i < totelNum) {
								addImageResources.add(newAddImageResources.get(tNum*maxNum+i));
							}									
						}
						tNum = tNum +1;
						addImageResourcesList.add(addImageResources);
					}
					addPageNum = addImageResourcesList.size();
										
					if (num + addPageNum - haveDuplexFillerNum > currentPhotoBook.maxNumberOfPages) return false;						
					
					for (int i = 0; i < addImageResourcesList.size(); i++) {
						ArrayList<String> addImageResources = addImageResourcesList.get(i);
						try {
							phtotbook = (Photobook) pbService.insertPageWithContent2Task(currentPhotoBook.id, previousPageId, addImageResources);
						} catch (RssWebServiceException e) {
							e.printStackTrace();
							return e;
						}
						if (phtotbook != null) {									
							PhotoBookProductUtil.setCurrentPhotoBook(phtotbook);
							currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
						}else {
							break;
						}							
					}	
				}else {
					addPageNum = 1;
					if (num + addPageNum - haveDuplexFillerNum > currentPhotoBook.maxNumberOfPages) return false;	
					
					try {
						phtotbook = (Photobook) pbService.insertPageWithContent2Task(currentPhotoBook.id, previousPageId, newAddImageResources);
					} catch (RssWebServiceException e) {
						e.printStackTrace();
						return e;
					}
					if (phtotbook != null) {									
						PhotoBookProductUtil.setCurrentPhotoBook(phtotbook);
						currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
					}
				}
			}							
		break;
		case Relayout_Flg:
			Photobook mPhotobook;
			try {
				mPhotobook = pbService.layoutPhotobookTask(currentPhotoBook.id);
				if (mPhotobook != null) {					
					PhotoBookProductUtil.setCurrentPhotoBook(mPhotobook);
					currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
				}
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				return e;
			}		
			break;			
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
				Log.e(TAG, "add images to book fail.");				
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
				if (mAction == Add_End_Flg) {
					((PhotoBooksProductActivity)mContext).notifyPhotoBookPagesChanged();
				}
			}else if(result instanceof Boolean){
				if((Boolean)result){			
					Log.i(TAG, "succeed.");
					if (mContext instanceof PhotoBooksProductActivity) {
						if (mAction == Add_End_Flg) {
							((PhotoBooksProductActivity)mContext).notifyPhotoBookPagesChanged();
						}else {
							((PhotoBooksProductActivity)mContext).notifyPhotoBookChanged();
							((PhotoBooksProductActivity)mContext).removeGiveUpItems();
						}	
					}
				}else  {
					if (currentPhotoBook.pages.size() + addPageNum - haveDuplexFillerNum > currentPhotoBook.maxNumberOfPages) {
						new InfoDialog.Builder(mContext).setMessage(R.string.Book_enough_prompt)			
						.setNegativeButton(R.string.d_ok, null)
						.create()
						.show();						
					}else if (failInfo != null && failInfo.imageThumbnailResource == null && !((BaseActivity)mContext).isHaveUploadErrorDialog) {									
						new DialogUploadImageError().initDialogUploadImageError(mContext,failInfo);
					}
				} 				
			}
		}
	}	
	
}	