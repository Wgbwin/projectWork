package com.kodak.rss.tablet.thread;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseActivity;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.UploadProgressUtil;
import com.kodak.rss.tablet.view.dialog.DialogUploadImageError;
import com.kodak.rss.tablet.view.dialog.UploadImageProgress;

public class UploadImagesTask extends AsyncTask<String, Void, Boolean>{	
	private Context mContext;	
	private Photobook  photobook;	
	private Theme mSelectedTheme;	
	private UploadImageProgress uploadProgress = null;	
	private int size;
	private boolean isFromProduct;	
	private ImageInfo failInfo;

	public UploadImagesTask(Context context,Theme selectedTheme, boolean fromProduct){
		this.mContext = context;
		this.photobook = PhotoBookProductUtil.getCurrentPhotoBook();;			
		this.mSelectedTheme = selectedTheme;
		this.isFromProduct = fromProduct;				
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();		
		size = photobook.chosenpics.size();
		if (UploadProgressUtil.getUploadPicSuccessNum(photobook.chosenpics,true) < size ) {				
			PictureUploadService.flowType = AppConstants.bookType;
			((BaseActivity)mContext).startUploadService();
			String promptStr = mContext.getResources().getString(R.string.Task_CreatingPhotobook);
			String message = mContext.getResources().getString(R.string.Task_Add_Photos_Prompt);			
			uploadProgress = new UploadImageProgress(mContext, promptStr, message);				
		}
	}
	
	@Override
	protected Boolean doInBackground(String... params) {		
		boolean isUploadAll = true;		
		//upload all images
		if (uploadProgress != null) {			
			int sucNum = UploadProgressUtil.getUploadPicSuccessNum(photobook.chosenpics,true);
			ImageInfo info = UploadProgressUtil.getRunningUploadInfo(photobook.chosenpics,true);
			uploadProgress.refresh(size, info, 0);	
						
			while (size > sucNum) {	
				failInfo = UploadProgressUtil.getUploadPicFailImageInfo(photobook.chosenpics,true);
				if (failInfo != null ) {						
					isUploadAll = false;
					break;
				}								
				info = UploadProgressUtil.getRunningUploadInfo(photobook.chosenpics,true);				
				uploadProgress.refresh(sucNum, info, 1);
				size = photobook.chosenpics.size();
				sucNum = UploadProgressUtil.getUploadPicSuccessNum(photobook.chosenpics,true);
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {					
					isUploadAll = false;
				}
			}							
		}
		return isUploadAll;
	}

	@Override
	protected void onPostExecute(Boolean  result) {
		super.onPostExecute(result);
		if (mContext != null && !((BaseActivity)mContext).isFinishing()) {
			if(uploadProgress != null ){
				uploadProgress.refresh(size, null, 2);	
	        }											
			if (result) {
				String backCoverResourceId = null;
				String facebookId = SharedPreferrenceUtil.getFacebookUserId(mContext);					
				ArrayList<String> imageResources = new ArrayList<String>(size);
				for (ImageInfo info : photobook.chosenpics) {
					if (facebookId != null && !"".equals(facebookId) && facebookId.equals(info.id)) {
						if (info.imageThumbnailResource != null) {
							backCoverResourceId = info.imageThumbnailResource.id;
						}					
					}				
					if (info.imageThumbnailResource != null) {
						imageResources.add(info.imageThumbnailResource.id);
					}		
				}
				SetPhotoBookParamsTask setParamsTask = new SetPhotoBookParamsTask(mContext,imageResources,mSelectedTheme,isFromProduct,backCoverResourceId);
				setParamsTask.execute();					
			}else {
				if (failInfo != null && !((BaseActivity)mContext).isHaveUploadErrorDialog) {
					new DialogUploadImageError().initDialogUploadImageError(mContext,failInfo);
				}	
			}
		}
	}

}
