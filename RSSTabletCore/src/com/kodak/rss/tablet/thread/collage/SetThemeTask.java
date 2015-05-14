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
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class SetThemeTask extends AsyncTask<String, Void, Object> {
	
	private final String TAG = CreateCollageTask.class.getSimpleName();
	private Context mContext;	
	
	private InfoDialog waitingDialog;
	private CollageWebService mService;	
	private String  collageId;
	private String  themeId;
	
	public SetThemeTask(Context context, String collageId, String themeId){
		this.mContext = context;
		this.collageId = collageId;
		this.themeId = themeId;
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
			collage = mService.setCollageThemeTask(collageId,themeId,false);
			if (collage == null){
				if (mContext instanceof CollageEditActivity)  {
					String checkThemeId = ((CollageEditActivity)mContext).themesAdapter.oldCheckThemeId;
					CollageUtil.setCheckTheme(checkThemeId);
				}		
				return null;
			}			
		} catch (RssWebServiceException e) {			
			e.printStackTrace();
			if (mContext instanceof CollageEditActivity)  {
				String checkThemeId = ((CollageEditActivity)mContext).themesAdapter.oldCheckThemeId;
				CollageUtil.setCheckTheme(checkThemeId);
			}		
			
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
				System.gc();
			}
			if (mContext instanceof CollageEditActivity)  {
				if (result != null && result instanceof Collage) {
					((CollageEditActivity)mContext).notifyCollageChanged((Collage) result,false);
				}else {
					((CollageEditActivity)mContext).notifyCollageChanged(null,false);
				}	
			}		
		}
	}
}

