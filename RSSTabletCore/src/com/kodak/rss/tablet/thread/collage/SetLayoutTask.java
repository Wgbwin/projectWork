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

public class SetLayoutTask extends AsyncTask<String, Void, Object> {
	
	private final String TAG = CreateCollageTask.class.getSimpleName();
	private Context mContext;	
	
	private InfoDialog waitingDialog;
	private CollageWebService mService;	
	private String collagePageId;
	private String layoutId;
	private boolean isFrist;
	
	public SetLayoutTask(Context context, String collagePageId, String layoutId,boolean isFrist){
		this.mContext = context;
		this.collagePageId = collagePageId;
		this.layoutId = layoutId;
		this.isFrist = isFrist;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.i(TAG, "Start set layout...");
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
		CollagePage collagePage = null;
		try {		
			collagePage = mService.setCollagePageLayoutTask(collagePageId,layoutId,isFrist);
			if (collagePage != null) {				
				CollageUtil.updatePageInCollage(collagePage, true, isFrist);	
			} else {
				if (mContext instanceof CollageEditActivity)  {
					String checkLayoutId = ((CollageEditActivity)mContext).layoutsAdapter.oldCheckLayoutId;
					CollageUtil.setLayoutsData(checkLayoutId);
				}		
				return null;
			}
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			if (mContext instanceof CollageEditActivity)  {
				String checkLayoutId = ((CollageEditActivity)mContext).layoutsAdapter.oldCheckLayoutId;
				CollageUtil.setLayoutsData(checkLayoutId);
			}		
			return e;
		}			
		return collagePage;
	}
	
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if(mContext!= null && !((Activity)mContext).isFinishing()){
			if(waitingDialog!=null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}
			if(result == null){
				Log.e(TAG, "Setting layout failed.");				
			}else if(result instanceof RssWebServiceException){
				Log.e(TAG, "Setting layout failed.");
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			} else {					
				Log.i(TAG, "Setting layout succeed.");																					
				System.gc();
			}
			if (mContext instanceof CollageEditActivity)  {
				((CollageEditActivity)mContext).notifyCollagePageChanged();
			}		
		}
	}
}

