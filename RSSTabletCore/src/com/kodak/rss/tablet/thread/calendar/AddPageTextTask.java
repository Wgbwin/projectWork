package com.kodak.rss.tablet.thread.calendar;

import java.util.ArrayList;
import java.util.List;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.bean.text.Font;
import com.kodak.rss.core.n2r.bean.text.TextBlock;
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

public class AddPageTextTask extends AsyncTask<Void, Void, Object> {
	
	private static final String TAG = "AddPageTextTask";
	
	private Context mContext;
	private InfoDialog waitingDialog;
	private String pageId;
	private TextBlock textBlock;
	
	public AddPageTextTask(Context context, String pageId){
		this.mContext = context;
		this.pageId = pageId;
	}

	@Override
	protected void onPreExecute() {
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
		Font font = RssTabletApp.getInstance().fonts.get(0);
		try {
			textBlock = service.createTextBlockTask(mContext.getString(R.string.Common_SampleText), font.name);
			if(textBlock == null){
				return null;
			}
			List<String> contents = new ArrayList<String>();
			contents.add(textBlock.id);
			CalendarPage newPage = service.addContentToCalendarPageTask(pageId, contents);
			if (newPage != null) {
				CalendarUtil.updatePageInCalendar(newPage, true);	
				return newPage;
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
			if(result == null || result instanceof RssWebServiceException){
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			} else if(result instanceof CalendarPage){
				if (mContext instanceof CalendarEditActivity) {
					CalendarEditActivity activity = (CalendarEditActivity)mContext;
					activity.notifyCalendarPagesChanged();
					CalendarPage page = (CalendarPage) result;
					CalendarLayer layer = null;
					for(CalendarLayer tempLayer : page.layers){
						if(tempLayer.contentId.equals(textBlock.id)){
							layer = tempLayer;
							break;
						}
					}
					if(layer != null){
						activity.showFontEditView(false, page, layer, false);
					}
				}
			} else if(result instanceof Boolean){
				if(!(Boolean)result){
					// TODO: show add page text failed dialog
				}
			}
		}
	}

}
