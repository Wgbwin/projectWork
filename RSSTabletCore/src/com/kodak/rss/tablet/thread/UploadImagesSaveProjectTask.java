package com.kodak.rss.tablet.thread;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.Window;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseActivity;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.UploadProgressUtil;
import com.kodak.rss.tablet.view.dialog.DialogSaveProject;
import com.kodak.rss.tablet.view.dialog.DialogUploadImageError;
import com.kodak.rss.tablet.view.dialog.UploadImageProgressForSaveProject;

public class UploadImagesSaveProjectTask extends AsyncTask<String, Void, Boolean>{	
	private Context mContext;	
	private Photobook  currentPhotoBook;	
	private UploadImageProgressForSaveProject uploadSaveProject;
	private int totelSize;
	private String projectName;
	public boolean isCancel = false;
	private ImageInfo failInfo;
	
	public UploadImagesSaveProjectTask(Context context,String projectName){
		this.mContext = context;
		this.projectName = projectName;		
		this.currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		isCancel = false;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();		
		totelSize = currentPhotoBook.chosenpics.size();
		int uploadNum = UploadProgressUtil.getUploadPicSuccessNum(currentPhotoBook.chosenpics,false);
		if (uploadNum < totelSize) {				
			PictureUploadService.flowType = AppConstants.bookType;
			((BaseActivity)mContext).startUploadService();
			String promptStr = mContext.getResources().getString(R.string.save_project);
			ImageInfo info = UploadProgressUtil.getRunningUploadInfo(currentPhotoBook.chosenpics,false);
			uploadSaveProject = new UploadImageProgressForSaveProject(mContext, promptStr, totelSize,UploadImagesSaveProjectTask.this);	
			uploadSaveProject.refresh(uploadNum, info, 0);	
		}
	}
	
	@Override
	protected Boolean doInBackground(String... params) {		
		boolean isUploadAll = true;
		//upload all images
		if (uploadSaveProject != null) {			
			int sucNum = UploadProgressUtil.getUploadPicSuccessNum(currentPhotoBook.chosenpics,false);
			ImageInfo info = UploadProgressUtil.getRunningUploadInfo(currentPhotoBook.chosenpics,false);
			uploadSaveProject.refresh(sucNum, info, 0);	
						
			while (totelSize > sucNum) {
				failInfo = UploadProgressUtil.getUploadPicFailImageInfo(currentPhotoBook.chosenpics,false);
				if (failInfo != null) {					
					isUploadAll = false;
					break;
				}								
				info = UploadProgressUtil.getRunningUploadInfo(currentPhotoBook.chosenpics,false);				
				uploadSaveProject.refresh(sucNum, info, 0);
				totelSize = currentPhotoBook.chosenpics.size();
				sucNum = UploadProgressUtil.getUploadPicSuccessNum(currentPhotoBook.chosenpics,false);
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
		if(uploadSaveProject != null ){
			 if (mContext != null && !((Activity)mContext).isFinishing()) {
				 uploadSaveProject.refresh(totelSize, null, 1);					
            }				
		}
		if (mContext == null) return;
		if (((Activity)mContext).isFinishing()) return;			
		
		if (result) {			
			if (!isCancel) {
				DialogSaveProject dialog = new DialogSaveProject(mContext,projectName,currentPhotoBook.id);
				Window window = dialog.getWindow();
				window.setBackgroundDrawable(new ColorDrawable(0));
				dialog.show();	
			}					        				
		}else {
			if (failInfo != null &&!((BaseActivity)mContext).isHaveUploadErrorDialog) {
				new DialogUploadImageError().initDialogUploadImageError(mContext,failInfo);
			}	
		}		 
	}

}
