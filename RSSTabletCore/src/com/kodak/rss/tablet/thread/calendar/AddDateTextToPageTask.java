package com.kodak.rss.tablet.thread.calendar;

import java.util.List;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.CalendarGridItemPO;
import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.webservice.CalendarWebService;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

public class AddDateTextToPageTask extends AsyncTask<Void, Void, Object> {
	
	private static final String TAG = "AddDateToPageTask";
	
	private Context mContext;
	private InfoDialog waitingDialog;
	private String mCalendarId;
	private CalendarPage mPage;
	
	public AddDateTextToPageTask(Context context, String calendarId, CalendarPage page){
		this.mContext = context;
		this.mCalendarId = calendarId;
		this.mPage = page;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait).setProgressBar(true).create();
		waitingDialog.show();
	}

	@Override
	protected Object doInBackground(Void... params) {
		CalendarWebService service = new CalendarWebService(mContext);
		if(RssTabletApp.getInstance().fonts == null){
			try {
				RssTabletApp.getInstance().fonts = service.getAvailableFontsTask(RssTabletApp.getInstance().getCurrentLanguage());
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				return e;
			}
		}
		
		List<CalendarGridItemPO> items = CalendarUtil.getDatesInLayer(CalendarUtil.getDaysGridLayer(mPage));
		try {
			CalendarPage[] newPages = service.addTextToCalendarGridsTask(mCalendarId, items);
			if(newPages!=null && newPages.length>0){
				for(CalendarPage newPage : newPages){
					CalendarUtil.updatePageInCalendar(newPage, false);
					if(CalendarUtil.isDaysGridPage(newPage)){
						mPage = newPage;
					}
				}
				((CalendarEditActivity)mContext).calendarMainView.refresh(CalendarUtil.getCurrentCalendar());
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
					if (mContext instanceof CalendarEditActivity) {
						((CalendarEditActivity)mContext).showEditDateView(mPage, CalendarUtil.getDaysGridLayer(mPage));
					}
				}else {
					// TODO: show add page text failed dialog		
				}
			}
		}
	}
	
	

}
