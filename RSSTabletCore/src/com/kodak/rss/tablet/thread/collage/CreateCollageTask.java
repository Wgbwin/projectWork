package com.kodak.rss.tablet.thread.collage;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.webservice.CollageWebService;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CollageEditActivity;
import com.kodak.rss.tablet.activities.CollageSelectionActivity;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class CreateCollageTask extends AsyncTask<String, Void, Object> {
	
	private final String TAG = CreateCollageTask.class.getSimpleName();
	private Context mContext;	
	
	private InfoDialog waitingDialog;
	private CollageWebService mService;	
	private String  proDescriptionId;
	
	public CreateCollageTask(Context context,String id){
		this.mContext = context;		
		this.proDescriptionId = id;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.i(TAG, "Start get calendarThemes...");
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
			//1.create collage
			collage = mService.createCollageTask(proDescriptionId,"","",false);
			if (collage == null) return null;
			
			//2.get background
			String language = RssTabletApp.getInstance().getCurrentLanguage();
			List<Theme> themes = mService.getThemesTask(proDescriptionId, language);			
			if (themes == null) return null;
			RssTabletApp.getInstance().setThemes(themes);	
			
		} catch (com.kodak.rss.core.exception.RssWebServiceException e) {
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
				Log.e(TAG, "Creating collage failed.");				
			}else if(result instanceof RssWebServiceException){
				Log.e(TAG, "Creating collage failed.");
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			} else {					
				Log.i(TAG, "Creating collage succeed.");
				
				HashMap<String,String> map = new HashMap<String, String>();					
				map.put(RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_COLLAGE_TYPE, proDescriptionId);
				RSSLocalytics.recordLocalyticsEvents(mContext, RSSTabletLocalytics.LOCALYTICS_EVENT_COLLAGE_TYPE_SELECTED, map);						
				
				CollageUtil.addCurrentCollage((Collage) result);	
				Intent mIntent = new Intent(mContext, CollageEditActivity.class);										
				mContext.startActivity(mIntent);
				if (mContext instanceof CollageSelectionActivity)  {
					((CollageSelectionActivity)mContext).adapter.cancelRequest();
					((CollageSelectionActivity)mContext).finish();
				}				
				System.gc();
			}
		}
	}
}
