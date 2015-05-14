package com.kodak.rss.tablet.handler;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.handler.PhotoBookEditTaskHandler.MsgData;
import com.kodak.rss.tablet.thread.PhotoBookEditTask;

public class CalendarEditTaskHandler extends Handler{
	private static final String TAG = "CalendarEditTaskHandler";
	public static final int STATUS_START = 1;
	public static final int STATUS_FINISH = 2;
	
	private WeakReference<CalendarEditActivity> activityRef;
	
	public CalendarEditTaskHandler(CalendarEditActivity activity){
		this.activityRef = new WeakReference<CalendarEditActivity>(activity);
	}
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		CalendarEditActivity activity = activityRef.get();
		
		if(activity == null || activity.isFinishing()){
			return;
		}
		
		if(msg.obj != null){
			MsgData data = (MsgData) msg.obj;
			
			if (data.status == STATUS_START) {
				if (data.layer != null) {
					activity.calendarEditLayer.showLayerEditProgress(data.page, data.layer);
				} else {
					activity.calendarEditLayer.showPageEditProgress(data.page);
				}
			} else if (data.status == STATUS_FINISH) {
				activity.calendarEditLayer.dismissEditProgress();
				activity.calendarEditLayer.dismiss();
				activity.calendarMainView.exitEditMode();
				
				if (!data.succeed) {
					showErrorWarning(activity, data);
				} else {
					activity.notifyCalendarPagesChanged();
				}
			}
			
		}
		
	}
	
	public static class MsgData{
		int taskId;
		int status;
		boolean succeed;
		CalendarPage page;
		CalendarLayer layer;
		Object[] params;
		
		public MsgData(int taskId,int status,boolean succeed,CalendarPage page, CalendarLayer layer,Object... params){
			this.taskId = taskId;
			this.status = status;
			this.succeed = succeed;
			this.page = page;
			this.layer = layer;
			this.params = params;
		}
		
		public MsgData(int taskId,int status,boolean succeed,CalendarPage page,CalendarLayer layer){
			this(taskId, status, succeed, page,layer,new Object[]{});
		}
		
	}
	
	private void showErrorWarning(CalendarEditActivity activity, MsgData data){
		//show error warning
		boolean shown = false;
		if(data.params != null){
			for(int i=0;i<data.params.length;i++){
				if(data.params[i] instanceof RssWebServiceException){
					activity.showErrorWarning((RssWebServiceException) data.params[0]);
					shown = true;
				}
			}
		}
		
		if(!shown){
			Log.e(TAG, "Havn't find the exception, there must be something wrong");
		}
	}
}
