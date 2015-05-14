package com.kodak.rss.tablet.thread.calendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.bean.text.TextBlock;
import com.kodak.rss.core.n2r.webservice.CalendarWebService;
import com.kodak.rss.tablet.handler.CalendarCommonHandler;
import com.kodak.rss.tablet.handler.CalendarEditTaskHandler.MsgData;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.view.CalendarDateTextView.DateFont;

public class CalendarSetDateTaskGroup extends Thread{
	
	private List<String> taskKeys;
	private Map<String, DateFont> tasks;
	private Context mContext;
	public boolean destroy = false;
	private CalendarCommonHandler handler;
	private CalendarWebService service;
	
	public CalendarSetDateTaskGroup(Context context, CalendarCommonHandler handler){
		taskKeys = new ArrayList<String>();
		tasks = new HashMap<String, DateFont>();
		this.mContext = context;
		this.handler = handler;
		service = new CalendarWebService(mContext);
	}
	
	public void addTask(DateFont dateFont) {
		String key = dateFont.getMonthAndDayString();
		if(taskKeys.contains(key)){
			taskKeys.remove(key);
		}
		taskKeys.add(key);
		tasks.put(key, dateFont);
	}
	
	private Object startTask(){
		String key = taskKeys.get(0);
		DateFont dateFont = tasks.get(key);
		taskKeys.remove(key);
		tasks.remove(key);
		try {
			TextBlock textBlock = service.updateTextBlockTask(dateFont.textBlock);
			if(textBlock != null){
				CalendarPage page = service.layoutPageInCalendarTask(dateFont.pageId);
				if(page != null){
					CalendarUtil.updatePageInCalendar(page, true);
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
	}
	
	@Override
	public void run() {
		while(!destroy){
			if(taskKeys.size()>0){
				Object obj = startTask();
				handler.obtainMessage(0, obj).sendToTarget();
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	public boolean isAllTasksFinished(){
		return taskKeys.size()==0;
	}
	
	public void destroyThread(){
		destroy = true;
		interrupt();
	}

}
