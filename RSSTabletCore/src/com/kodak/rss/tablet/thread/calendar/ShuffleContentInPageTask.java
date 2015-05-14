package com.kodak.rss.tablet.thread.calendar;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.webservice.CalendarWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class ShuffleContentInPageTask extends AsyncTask<Void, Void, Object> {
	
	private static final String TAG = "ShuffleContentInPageTask";
	
	private Context mContext;
	private InfoDialog waitingDialog;
	private String pageId;
	
	public ShuffleContentInPageTask(Context context, String pageId){
		this.mContext = context;
		this.pageId = pageId;		
	}

	@Override
	protected void onPreExecute() {		
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true).create();
		waitingDialog.show();
	}
	
	@Override
	protected Object doInBackground(Void... params) {
		if (mContext instanceof CalendarEditActivity) {
			boolean isWait = ((CalendarEditActivity)mContext).isWaitAddImageDone(pageId);			
			while(isWait) {
				try {
					Thread.sleep(900);
				} catch (InterruptedException e) {}
				isWait = ((CalendarEditActivity)mContext).isWaitAddImageDone(pageId);				
			}
		}
		CalendarWebService mService = new CalendarWebService(mContext);		
		CalendarPage newPage = null;		
		try {			
			newPage = mService.shuffleContentCalendarPageTask(pageId);
			if (newPage != null) {
				CalendarUtil.updatePageInCalendar(newPage, true);					
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
					if (mContext instanceof CalendarEditActivity) {
						((CalendarEditActivity)mContext).notifyCalendarPagesChanged();
					}
				}else {
					Log.i(TAG, "failed.");						
				}
			}
		}
	}

}
