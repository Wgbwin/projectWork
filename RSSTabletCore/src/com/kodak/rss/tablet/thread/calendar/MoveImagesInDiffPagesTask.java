package com.kodak.rss.tablet.thread.calendar;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.calendar.CalendarGridItemPO;
import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.webservice.CalendarWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class MoveImagesInDiffPagesTask extends AsyncTask<Void, Void, Object> {
	
	private static final String TAG = "MoveImagesInDiffPagesTask";
	
	private Context mContext;
	private InfoDialog waitingDialog;
	private String fromPageId;
	private CalendarPage toPage;
	private String contentId;
	private CalendarLayer toLayer;
	private CalendarGridItemPO gridItemPo;
	
	public MoveImagesInDiffPagesTask(Context context, CalendarPage fromPage,CalendarPage toPage, String contentId,CalendarGridItemPO gridItemPo,CalendarLayer toLayer){
		this.mContext = context;
		this.fromPageId = fromPage.id;
		this.toPage = toPage;
		this.contentId = contentId;
		this.gridItemPo = gridItemPo;
		this.toLayer = toLayer;
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
			boolean isWait = ((CalendarEditActivity)mContext).isWaitAddImageDone(toPage.id);
			while(isWait) {
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {}
				isWait = ((CalendarEditActivity)mContext).isWaitAddImageDone(toPage.id);
			}
		}		
		
		CalendarWebService mService = new CalendarWebService(mContext);		
		CalendarPage newFromPage = null;		
		try {			
			newFromPage = mService.removeContentInCalendarPageTask(fromPageId, contentId);
			if (newFromPage != null) {				
				CalendarUtil.updatePageInCalendar(newFromPage, true);					
			} else {
				return false;
			}
			
			if (gridItemPo != null) {
				if (gridItemPo.contentIds == null) {
					gridItemPo.contentIds = new ArrayList<String>();
				}else {
					gridItemPo.contentIds.clear();
				}
				gridItemPo.contentIds.add(contentId);
				CalendarPage[] newPages = null;
				Calendar calendar = CalendarUtil.getCurrentCalendar();
				List<CalendarGridItemPO> gridItemList = new ArrayList<CalendarGridItemPO>(2);
				gridItemList.add(gridItemPo);
				newPages = mService.addContentToCalendarGridsTask(calendar.id, gridItemList);
				if (newPages != null) {
					for (CalendarPage newPage : newPages) {
						if (newPage == null) continue;
						CalendarUtil.updatePageInCalendar(newPage, true);	
					}
					return true;
				}else {
					return false;
				}								
			}else {
				if ((CalendarUtil.isSimplex() || !CalendarUtil.isDisplayPages()) || CalendarUtil.isEditableCoverPage(toPage)) {
					CalendarPage newToPage = null;		
					int holeIndex = -1;
					if (toLayer == null) {
						holeIndex = CalendarUtil.getFristImageLayerInPage(toPage);
					}else {
						holeIndex = CalendarUtil.getHoleIndexInPage(toLayer, toPage.layers);
					}									
					if (holeIndex < 0) return true;	
					newToPage = mService.insertContentOnPageTask(toPage.id, holeIndex, contentId);	
					if (newToPage != null) {
						CalendarUtil.updatePageInCalendar(newToPage, true);	
						return true;						
					}else {
						return false;
					}		
				}
				
				if (CalendarUtil.isDuplex()) {										
					List<String> contentIds = new ArrayList<String>(2);	
					contentIds.add(contentId);
					CalendarPage newToPage = null;	
					newToPage = mService.addContentToCalendarPageTask(toPage.id, contentIds);
					if (newToPage != null) {
						CalendarUtil.updatePageInCalendar(newToPage, true,1);	
						return true;						
					}else {
						return false;
					}				
				}else {
					return true;
				}	
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
