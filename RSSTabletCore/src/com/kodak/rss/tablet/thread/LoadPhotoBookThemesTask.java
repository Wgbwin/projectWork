package com.kodak.rss.tablet.thread;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.PhotoBooksPicSelectActivity;
import com.kodak.rss.tablet.activities.PhotoBooksThemeSelectActivity;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class LoadPhotoBookThemesTask extends AsyncTask<String, Void, Object>{
	private static final String TAG = "LoadPhotoBookThemesTask:";	
	private Context mContext;	
	private InfoDialog waitingDialog;
	public LoadPhotoBookThemesTask(Context context){
		this.mContext = context;		
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.i(TAG, "Start load Themes ...");
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true)				
		.create();
		waitingDialog.show();				
	}
	
	@Override
	protected Object doInBackground(String... params) {
		String theme = params[0];			
		PhotobookWebService mService = new PhotobookWebService(mContext);							
		String language = RssTabletApp.getInstance().getCurrentLanguage();
		List<Theme> themes = null;
		try {
			themes = mService.getThemesTask(theme, language);
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
		return themes;
	}

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if(mContext != null && !((Activity)mContext).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}
		
			if(result == null){
				Log.e(TAG, "load Themes failed.");				
			}else if(result instanceof RssWebServiceException){
				Log.e(TAG, "load Themes failed.");				
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			} else {
				Log.i(TAG, "load Themes succeed.");
								
				RssTabletApp.getInstance().setThemes((List<Theme>) result);
				Intent mIntent = new Intent(mContext, PhotoBooksThemeSelectActivity.class);
				mContext.startActivity(mIntent);
				if (mContext instanceof PhotoBooksPicSelectActivity) {
					((PhotoBooksPicSelectActivity)mContext).clearDownDataRequest();
					((PhotoBooksPicSelectActivity)mContext).photoGridView.setAdapter(null);					
				}				
				((Activity)mContext).finish();	
			}
		}
	}

}	