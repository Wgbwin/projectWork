package com.kodak.rss.tablet.thread;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.content.SearchStarter;
import com.kodak.rss.core.n2r.bean.greetingcard.GCCategory;
import com.kodak.rss.core.n2r.webservice.GreetingCardWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.GCSSCategorySelectActivity;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class GCLoadCategoryTask extends AsyncTask<String, Void, Object>{
	private static final String TAG = "GCLoadInfoTask:";	
	private Context mContext;	
	private SearchStarter ss;
	private InfoDialog waitingDialog;
	
	public GCLoadCategoryTask(Context context,SearchStarter ss){
		this.mContext = context;	
		this.ss = ss;	
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.i(TAG, "Start load categorys...");
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true)				
		.create();
		waitingDialog.show();
	}
	
	@Override
	protected Object doInBackground(String... params) {
		String param = ss.filters;			
		GreetingCardWebService mService = new GreetingCardWebService(mContext);							
		String app_id = mContext.getResources().getString(R.string.cumulus_appid);
		List<GCCategory> categorys = null;
		try {
			categorys = mService.getGreetingCardsForCategoryTask(app_id, param);
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
		return categorys;
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
				Log.e(TAG, "load categorys failed.");				
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			} else {
				Log.i(TAG, "load categorys succeed.");							
				if (mContext instanceof GCSSCategorySelectActivity) {
					((GCSSCategorySelectActivity)mContext).dealSkip(ss,(List<GCCategory>) result);					
				}				
			}
		}
	}

}	