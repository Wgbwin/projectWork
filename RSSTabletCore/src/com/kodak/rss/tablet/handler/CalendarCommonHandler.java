package com.kodak.rss.tablet.handler;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.tablet.activities.CalendarEditActivity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class CalendarCommonHandler extends Handler {
	
	private CalendarEditActivity activity;
	
	public CalendarCommonHandler(Context context){
		activity = (CalendarEditActivity) context;
	}
	
	@Override
	public void handleMessage(Message msg) {
		Object result = msg.obj;
		if(result instanceof RssWebServiceException){			
			activity.showErrorWarning((RssWebServiceException) result);
		}else if(result instanceof Boolean){
			activity.notifyCalendarPagesChanged();
		}
	}
}
