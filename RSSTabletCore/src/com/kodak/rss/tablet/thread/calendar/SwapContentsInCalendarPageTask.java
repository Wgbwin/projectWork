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

public class SwapContentsInCalendarPageTask extends AsyncTask<Void, Void, Object> {
	
	private static final String TAG = "SwapContentsInCalendarPageTask";
	
	private Context mContext;
	private InfoDialog waitingDialog;
	private String pageId;	
	private String contentId1;
	private String contentId2;
	
	public SwapContentsInCalendarPageTask(Context context, String pageId,String contentId1, String contentId2){
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
		CalendarWebService mService = new CalendarWebService(mContext);		
		CalendarPage newPage = null;		
		try {			
			newPage = mService.swapContentInCalendarTask(pageId, contentId1,contentId2);
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
