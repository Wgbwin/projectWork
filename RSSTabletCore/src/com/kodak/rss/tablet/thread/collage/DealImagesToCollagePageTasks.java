package com.kodak.rss.tablet.thread.collage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.webservice.CollageWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.bean.DealImagesToCollagePageTaskPo;
import com.kodak.rss.tablet.view.collage.DealCollageImagesLayout;

public class DealImagesToCollagePageTasks extends Thread{
	private static final String TAG = "AddImagesToCalendarPageTasks:";	
	private Context mContext;					
	public List<DealImagesToCollagePageTaskPo> dealImageToPageTaskList;
	
	private CollageWebService mService;
	private Handler handler;
	private boolean isRunning;
	private boolean isSkipFlag;
	
	public DealImagesToCollagePageTasks(Context context, Handler handler){
		this.mContext = context;		
		this.handler = handler;				
		this.mService = new CollageWebService(mContext);
		this.dealImageToPageTaskList = new ArrayList<DealImagesToCollagePageTaskPo>(4);
		this.isRunning = false;		
	}
	
	private void isStart(){
		if (!DealImagesToCollagePageTasks.this.isAlive() && dealImageToPageTaskList.size() == 0 && !isRunning) {			
			DealImagesToCollagePageTasks.this.start();
		}		
	}
	
	public DealImagesToCollagePageTaskPo getTaskPo(String pageId,boolean isNotSelectUsing){	
		DealImagesToCollagePageTaskPo taskPo = null;	
		if (pageId == null) return taskPo;
		if ("".equals(pageId)) return taskPo;				
		int size = dealImageToPageTaskList.size();			
		if (size == 0) return taskPo;
		DealImagesToCollagePageTaskPo addTaskPo = dealImageToPageTaskList.get(size-1);
		if (addTaskPo != null && addTaskPo.pageId != null && pageId.equals(addTaskPo.pageId)) {				
			if (isNotSelectUsing) {
				if (!addTaskPo.isInUse) {
					taskPo = addTaskPo;					
				}					
			}else {
				taskPo = addTaskPo;				
			}				
		}				
		return taskPo;		
	}
	
	public void dealTask(String pageId,ImageInfo imageInfo,boolean isAdd){			
		if (pageId == null) return;
		fristSleepTime = 0;
		isStart();
		synchronized (dealImageToPageTaskList) {	
			DealImagesToCollagePageTaskPo taskIsHavePo = getTaskPo(pageId,true);			
			if (taskIsHavePo != null && taskIsHavePo.isAdd == isAdd) {
				taskIsHavePo.dealImageInfo(imageInfo);								
			}else {
				DealImagesToCollagePageTaskPo taskPo = new DealImagesToCollagePageTaskPo(pageId, imageInfo,isAdd);								
				dealImageToPageTaskList.add(taskPo);									
			}			
		}
	}
	
	private DealImagesToCollagePageTaskPo getTaskRunnable(){			
		synchronized (dealImageToPageTaskList) {			
			DealImagesToCollagePageTaskPo taskPo = null;	
			int size = dealImageToPageTaskList.size();	
			Log.d(TAG, "getTaskRunnable size:"+size);			
			if (size == 0) return taskPo;							
			int taskIndex = 0;
			taskPo = dealImageToPageTaskList.get(taskIndex);									
			return taskPo;
		}
	}
	
	private List<ImageInfo> getAddImageInfos(DealImagesToCollagePageTaskPo taskPo){
		List<ImageInfo> imageInfoList = null;
		if (taskPo == null) return imageInfoList;
		if (taskPo.infoList != null) {
			imageInfoList = taskPo.infoList;	
		} else {
			imageInfoList = new ArrayList<ImageInfo>();			
		}	
		return imageInfoList;
	}
	
	private long fristSleepTime = 0;
	@Override	
	public void run() {			
		while (isRunning) {					
			DealImagesToCollagePageTaskPo taskPo = getTaskRunnable();
			if (taskPo != null) {
				sendStartMsg();
				fristSleepTime = 0;
				Object result = taskPo.run(mService);	
				synchronized (dealImageToPageTaskList){
					if (result != null && result instanceof ImageInfo) {
						sendAddErrorMsg(result);
						try {
							Thread.sleep(1200);
						} catch (InterruptedException e) {					
							e.printStackTrace();
						}
					}else {
						List<ImageInfo> imageInfoList = getAddImageInfos(taskPo);
						dealImageToPageTaskList.remove(taskPo);	
						sendAddMsg(imageInfoList);
						if(result != null && result instanceof RssWebServiceException){
							sendAddErrorMsg(result);	
						}			
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
						synchronized (dealImageToPageTaskList){
							if (dealImageToPageTaskList == null || dealImageToPageTaskList.size() == 0) {
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
		if (DealImagesToCollagePageTasks.this.isAlive() && dealImageToPageTaskList.size() > 0) {
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
		if(handler != null ){
			Message msg = handler.obtainMessage();
			msg.what = DealCollageImagesLayout.HANDLER_ADD_START;			
			msg.sendToTarget();			
		}
	}
	
	private void sendEndMsg(){
		if(handler != null && dealImageToPageTaskList.size() == 0){
			isRunning = false;	
			isSkipFlag = false;	
			Message msg = handler.obtainMessage();
			msg.what = DealCollageImagesLayout.HANDLER_ADD_END;			
			msg.sendToTarget();					
		}
	}
	
	private void sendAddMsg(List<ImageInfo> imageInfos){
		if(handler != null){			
			Message msg = handler.obtainMessage();
			msg.what = DealCollageImagesLayout.HANDLER_ADD;
			msg.obj = imageInfos;
			msg.sendToTarget();			
		}
	}
	
	private void sendAddErrorMsg(Object result){
		if(handler != null){			
			Message msg = handler.obtainMessage();
			msg.what = DealCollageImagesLayout.HANDLER_ADD_ERROR;
			msg.obj = result;
			msg.sendToTarget();			
		}
	}
	

}	