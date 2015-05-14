package com.kodak.rss.tablet.thread.collage;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.webservice.CollageWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CollageEditActivity;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class RotateCollageTask extends AsyncTask<String, Void, Object> {
	
	private final String TAG = RotateCollageTask.class.getSimpleName();
	private Context mContext;	
	
	private InfoDialog waitingDialog;
	private CollageWebService mService;	
	private String collageId;
	private String pageId;
	private boolean isPortrait;
	
	public RotateCollageTask(Context context, String collageId, String pageId, boolean isPortrait){
		this.mContext = context;
		this.collageId = collageId;
		this.pageId = pageId;
		this.isPortrait = isPortrait;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.i(TAG, "Start set theme...");
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true)				
		.create();
		waitingDialog.show();
	}
	
	@Override
	protected Object doInBackground(String... params) {		
		if(mService==null){
			mService = new CollageWebService(mContext);
		}		
		Collage collage = null;
		try {		
			collage = mService.rotateCollageTask(collageId,isPortrait,true);
			if (collage == null) return null;

		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}			
		return collage;
	}
	
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if(mContext!= null && !((Activity)mContext).isFinishing()){
			if(waitingDialog!=null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}
			if(result == null){
				Log.e(TAG, "Setting theme failed.");				
			}else if(result instanceof RssWebServiceException){
				Log.e(TAG, "Setting theme failed.");
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			} else {					
				Log.i(TAG, "Setting theme succeed.");			
				if (mContext instanceof CollageEditActivity)  {
					((CollageEditActivity)mContext).notifyCollageChanged((Collage) result,true);
				}		
				System.gc();
			}
		}
	}
}

