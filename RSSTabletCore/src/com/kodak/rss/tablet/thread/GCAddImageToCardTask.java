package com.kodak.rss.tablet.thread;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.webservice.GreetingCardWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseActivity;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.GCEditActivity;
import com.kodak.rss.tablet.util.GreetingCardUtil;
import com.kodak.rss.tablet.view.dialog.DialogUploadImageError;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class GCAddImageToCardTask extends AsyncTask<Void, Void, Object>{
	private static final String TAG = "GCLoadInfoTask:";	
	private Context mContext;		
	private InfoDialog waitingDialog;	
	private String pageId;
	private int holeIndex;
	private ImageInfo mInfo;
	
	public GCAddImageToCardTask(Context context, String pageId,int holeIndex, ImageInfo mInfo){
		this.mContext = context;		
		this.pageId = pageId;
		this.holeIndex = holeIndex;
		this.mInfo = mInfo;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.i(TAG, "Start add image...");
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.CardAddImage_Prompt)
		.setProgressBar(true)				
		.create();
		waitingDialog.show();
	}
	
	@Override
	protected Object doInBackground(Void... params) {			
		boolean isSuccess = false;
		while (mInfo.imageOriginalResource == null) {	
			if (mInfo.imageOriginalResource == null && mInfo.isHavedOriginalUpload) break;								
			try {
				Thread.sleep(900);
			} catch (InterruptedException e) {}				
		}							
		if (mInfo.imageOriginalResource != null) isSuccess = true;
		
		if (!isSuccess) return false;
		GreetingCardWebService mService = new GreetingCardWebService(mContext);	
		String contentId = mInfo.imageOriginalResource.id;
					
		GCPage newPage = null;
		try {
			newPage = mService.addImageToCardTask(pageId, holeIndex, contentId);
			if (newPage != null) {
				GreetingCardUtil.updatePageInCard(newPage, true);	
			}
			
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
		return true;
	}

	@Override
	protected void onPostExecute(Object result) {
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
					if (mContext instanceof GCEditActivity) {
						((GCEditActivity)mContext).notifyGCPagesChanged(pageId);
					}
				}else {
					 if (mInfo != null && mInfo.imageOriginalResource == null && !((BaseActivity)mContext).isHaveUploadErrorDialog) {									
						new DialogUploadImageError().initDialogUploadImageError(mContext,mInfo);
					}			
				}
			}
		}
	}

}	