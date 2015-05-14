package com.kodak.rss.tablet.thread.calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.CalendarGridItemPO;
import com.kodak.rss.core.n2r.webservice.CalendarWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.bean.AddImagesToPageTaskPo;
import com.kodak.rss.tablet.view.AddImagesLayout;

public class AddImagesToCalendarPageTasks extends Thread{
	private static final String TAG = "AddImagesToCalendarPageTasks:";	
	private Context mContext;					
	public List<AddImagesToPageTaskPo> addImageToPageTaskList;
	
	private CalendarWebService mService;
	private Handler handler;
	private boolean isRunning;
	private boolean isSkipFlag;
	
	public AddImagesToCalendarPageTasks(Context context, Handler handler){
		this.mContext = context;		
		this.handler = handler;				
		this.mService = new CalendarWebService(mContext);
		this.addImageToPageTaskList = new ArrayList<AddImagesToPageTaskPo>(4);
		this.isRunning = false;		
	}
	
	private void isStart(){
		if (!AddImagesToCalendarPageTasks.this.isAlive() && addImageToPageTaskList.size() == 0 && !isRunning) {			
			AddImagesToCalendarPageTasks.this.start();
		}		
	}
	
	public AddImagesToPageTaskPo getTaskPo(String pageId,boolean isNotSelectUsing){	
		AddImagesToPageTaskPo taskPo = null;	
		if (pageId == null) return taskPo;
		if ("".equals(pageId)) return taskPo;				
		int size = addImageToPageTaskList.size();			
		if (size == 0) return taskPo;
		for (AddImagesToPageTaskPo addTaskPo : addImageToPageTaskList) {
			if (addTaskPo != null && addTaskPo.mInfo == null &&addTaskPo.pageId != null && pageId.equals(addTaskPo.pageId)) {				
				if (isNotSelectUsing) {
					if (!addTaskPo.isInUse) {
						taskPo = addTaskPo;
						break;
					}					
				}else {
					taskPo = addTaskPo;
					break;
				}				
			}				
		}				
		return taskPo;		
	}
	
	public void addTask(String pageId,ImageInfo imageInfo,CalendarGridItemPO gridItemPo,int holeIndex){			
		if (pageId == null) return;
		fristSleepTime = 0;
		isStart();
		synchronized (addImageToPageTaskList) {	
			AddImagesToPageTaskPo taskIsHavePo = getTaskPo(pageId,true);
			if (taskIsHavePo != null) {
				taskIsHavePo.addImageInfo(imageInfo,gridItemPo);								
			}else {
				AddImagesToPageTaskPo taskPo = null;
				if (holeIndex >= 0) {
					taskPo = new AddImagesToPageTaskPo(pageId, imageInfo, holeIndex);	
				}else {
					taskPo = new AddImagesToPageTaskPo(pageId, imageInfo, gridItemPo);						
				}					
				addImageToPageTaskList.add(taskPo);									
			}
		}
	}
	
	private AddImagesToPageTaskPo getTaskRunnable(){			
		synchronized (addImageToPageTaskList) {			
			AddImagesToPageTaskPo taskPo = null;	
			int size = addImageToPageTaskList.size();	
			Log.d(TAG, "getTaskRunnable size:"+size);			
			if (size == 0) return taskPo;							
			int taskIndex = 0;
			taskPo = addImageToPageTaskList.get(taskIndex);									
			return taskPo;
		}
	}
	
	private List<ImageInfo> getAddImageInfos(AddImagesToPageTaskPo taskPo){
		List<ImageInfo> imageInfoList = null;
		if (taskPo == null) return imageInfoList;
		if (taskPo.addImageInfoList != null) {
			imageInfoList = taskPo.addImageInfoList;	
		}else if (taskPo.gridItemList != null){
			imageInfoList = taskPo.getWantAddImageInfos(taskPo.gridItemList);
		}else {
			imageInfoList = new ArrayList<ImageInfo>();
			imageInfoList.add(taskPo.mInfo);
		}	
		return imageInfoList;
	}
	
	private long fristSleepTime = 0;
	@Override	
	public void run() {			
		while (isRunning) {					
			AddImagesToPageTaskPo taskPo = getTaskRunnable();
			if (taskPo != null) {
				fristSleepTime = 0;
				Object result = taskPo.run(mService);	
				synchronized (addImageToPageTaskList){
					if (result == null) {
						
					}else if(result instanceof Boolean){
						List<ImageInfo> imageInfoList = getAddImageInfos(taskPo);						
						addImageToPageTaskList.remove(taskPo);	
						sendAddMsg(imageInfoList);
					} else {															
						if(result instanceof RssWebServiceException){
							List<ImageInfo> imageInfoList = getAddImageInfos(taskPo);	
							sendAddMsg(imageInfoList);
							addImageToPageTaskList.remove(taskPo);	
						}												
						sendAddErrorMsg(result);						
					}					
				}			
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {					
					e.printStackTrace();
				}
			}else {									
				if (isSkipFlag) {				
					sendEndMsg();
				}else {					
					try {						
						Thread.sleep(1500);
						synchronized (addImageToPageTaskList){
							if (addImageToPageTaskList== null || addImageToPageTaskList.size() == 0) {
								long sleepTime = new Date().getTime();	
								if (fristSleepTime == 0) {
									fristSleepTime = sleepTime;
								}						
								if (sleepTime - fristSleepTime > 1000*60) {
									sendEndMsg();
								}	
							}							
						}					
					} catch (InterruptedException e) {					
						e.printStackTrace();
					}
				}
			}			
		}
		Log.d(TAG, "run end");
	}
	
	@Override
	public void interrupt() {
		isRunning = false;
		super.interrupt();
	}

	public void setSkipFlag(){
		isSkipFlag = true;			
		if (AddImagesToCalendarPageTasks.this.isAlive() && addImageToPageTaskList.size() > 0) {
			sendStartMsg();	
		}
	}
	
	@Override
	public synchronized void start() {
		isRunning = true;
		super.start();
		Log.d(TAG, "run start");
	}
	
	private void sendStartMsg(){
		if(handler != null && isSkipFlag){
			Message msg = handler.obtainMessage();
			msg.what = AddImagesLayout.HANDLER_ADD_START;			
			msg.sendToTarget();			
		}
	}
	
	private void sendEndMsg(){
		if(handler != null && addImageToPageTaskList.size() == 0){
			isRunning = false;	
			isSkipFlag = false;	
			Message msg = handler.obtainMessage();
			msg.what = AddImagesLayout.HANDLER_ADD_END;			
			msg.sendToTarget();					
		}
	}
	
	private void sendAddMsg(List<ImageInfo> imageInfos){
		if(handler != null){			
			Message msg = handler.obtainMessage();
			msg.what = AddImagesLayout.HANDLER_ADD;
			msg.obj = imageInfos;
			msg.sendToTarget();			
		}
	}
	
	private void sendAddErrorMsg(Object result){
		if(handler != null){			
			Message msg = handler.obtainMessage();
			msg.what = AddImagesLayout.HANDLER_ADD_ERROR;
			msg.obj = result;
			msg.sendToTarget();			
		}
	}
	

}	