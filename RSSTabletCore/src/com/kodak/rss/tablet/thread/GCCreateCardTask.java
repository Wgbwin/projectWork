package com.kodak.rss.tablet.thread;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.core.n2r.bean.text.Font;
import com.kodak.rss.core.n2r.webservice.GreetingCardWebService;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.GCCategorySelectActivity;
import com.kodak.rss.tablet.util.GreetingCardUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class GCCreateCardTask extends AsyncTask<Void, Void, Object>{
	private static final String TAG = "GCLoadInfoTask:";	
	private Context mContext;	
	private String contentId;
	private String productIdentifier;
	private InfoDialog waitingDialog;
	
	public GCCreateCardTask(Context context, String contentId, String productIdentifier){
		this.mContext = context;
		this.contentId = contentId;
		this.productIdentifier = productIdentifier;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.i(TAG, "Start create Card...");
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true)				
		.create();
		waitingDialog.show();
	}
	
	@Override
	protected Object doInBackground(Void... params) {			
		GreetingCardWebService mService = new GreetingCardWebService(mContext);								
		GreetingCard gCard = null;
		try {
			gCard = mService.createGreetingCardTask(contentId, productIdentifier);
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
		
		try {
	    	if(RssTabletApp.getInstance().fonts == null){
	    		WebService webService = new WebService(mContext);
	    		List<Font> fonts;
	    		fonts = webService.getAvailableFontsTask(RssTabletApp.getInstance().getCurrentLanguage());
	    		RssTabletApp.getInstance().fonts = fonts;
	    	}
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
		return gCard;
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
				GreetingCardUtil.addCurrentGreetingCard((GreetingCard) result);					
				if (mContext instanceof GCCategorySelectActivity) {
					((GCCategorySelectActivity)mContext).dealCreateCard();					
				}	
			}
		}
	}

}	