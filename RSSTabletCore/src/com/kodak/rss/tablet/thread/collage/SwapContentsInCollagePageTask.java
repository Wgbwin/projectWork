package com.kodak.rss.tablet.thread.collage;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.webservice.CollageWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CollageEditActivity;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class SwapContentsInCollagePageTask extends AsyncTask<Void, Void, Object> {
	
	private static final String TAG = "SwapContentsInCalendarPageTask";
	
	private Context mContext;
	private InfoDialog waitingDialog;
	private String pageId;	
	private String contentId1;
	private String contentId2;
	
	public SwapContentsInCollagePageTask(Context context, String pageId,String contentId1, String contentId2){
		this.mContext = context;
		this.pageId = pageId;
		this.contentId1 = contentId1;
		this.contentId2 = contentId2;
	}

	@Override
	protected void onPreExecute() {
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true).create();
		waitingDialog.show();
	}
	
	@Override
	protected Object doInBackground(Void... params) {
		if (mContext instanceof CollageEditActivity) {
			boolean isWait = ((CollageEditActivity)mContext).isWaitAddImageDone(pageId);			
			while(isWait) {
				try {
					Thread.sleep(900);
				} catch (InterruptedException e) {}
				isWait = ((CollageEditActivity)mContext).isWaitAddImageDone(pageId);				
			}
		}		
		
		CollageWebService mService = new CollageWebService(mContext);		
		CollagePage newPage = null;		
		try {			
			newPage = mService.swapCollageContentTask(pageId, contentId1, contentId2,true);
			if (newPage != null) {
				CollageUtil.updatePageInCollage(newPage, true, true);					
			} else {
				return false;
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
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			}else if(result instanceof Boolean){
				if((Boolean)result){			
					Log.i(TAG, "succeed.");
					if (mContext instanceof CollageEditActivity) {
						((CollageEditActivity)mContext).notifyCollagePageChanged();
					}
				}else {
					Log.i(TAG, "failed.");						
				}
			}
		}
	}

}
