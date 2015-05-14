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

public class ShuffleContentInCollagePageTask extends AsyncTask<Void, Void, Object> {
	
	private static final String TAG = "ShuffleContentInPageTask";
	
	private Context mContext;
	private InfoDialog waitingDialog;
	private String pageId;
	
	public ShuffleContentInCollagePageTask(Context context, String pageId){
		this.mContext = context;
		this.pageId = pageId;		
	}

	@Override
	protected void onPreExecute() {		
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true).create();
		waitingDialog.show();
	}
	
	private boolean isEnd(){
		boolean isEnd = false;
		if (mContext instanceof CollageEditActivity) {
			isEnd  = ((CollageEditActivity)mContext).addImagesView.endTasks();	
		}						
		return isEnd;
	}
	
	@Override
	protected Object doInBackground(Void... params) {
		while(!isEnd()) {
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {}						
		}
		CollageWebService mService = new CollageWebService(mContext);		
		CollagePage newPage = null;		
		try {			
			newPage = mService.shuffleCollageTask(pageId,true);
			if (newPage != null) {
				CollageUtil.updatePageInCollage(newPage, true, true);	
			}else {
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
