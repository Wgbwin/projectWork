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

public class RemoveImageInPageTask extends AsyncTask<Void, Void, Object> {
	
	private static final String TAG = "RemoveImageInPageTask";
	
	private Context mContext;
	private InfoDialog waitingDialog;
	private String pageId;
	private String contentId;
	
	public RemoveImageInPageTask(Context context, String pageId,String contentId){
		this.mContext = context;
		this.pageId = pageId;
		this.contentId = contentId;
	}

	@Override
	protected void onPreExecute() {
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true).create();
		waitingDialog.show();
	}
	
	@Override
	protected Object doInBackground(Void... params) {
		CalendarWebService service = new CalendarWebService(mContext);		
		try {			
			CalendarPage newPage = service.removeContentInCalendarPageTask(pageId, contentId);
			if (newPage != null) {
				CalendarUtil.updatePageInCalendar(newPage, true);	
				return true;
			} else {
				return false;
			}
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
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
