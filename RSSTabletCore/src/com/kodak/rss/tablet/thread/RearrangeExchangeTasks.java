package com.kodak.rss.tablet.thread;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.tablet.bean.ExchangeTaskPo;
import com.kodak.rss.tablet.handler.PhotoBookRearTaskHandler;
import com.kodak.rss.tablet.handler.PhotoBookRearTaskHandler.MsgRearData;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;

public class RearrangeExchangeTasks extends Thread{
	
	private static final String TAG = "RearrangeExchangeTasks:";
		
	private Handler handler;
	private boolean isRunning;
	private boolean isSkipFlag;
	
	private ArrayList<ExchangeTaskPo> exchangeTaskPoList;
	private ArrayList<PhotobookPage> exchangePageList;
	private PhotobookWebService pbService;
	private String photoBookId;
	
	public RearrangeExchangeTasks(Context context,Handler handler) {
		super();	
		this.handler = handler;
		pbService = new PhotobookWebService(context);
		photoBookId = PhotoBookProductUtil.getCurrentPhotoBook().id;
		exchangeTaskPoList = new ArrayList<ExchangeTaskPo>();
		exchangePageList = new ArrayList<PhotobookPage>();
		isRunning = false;
		isSkipFlag = false;	
	}
	
	private void isStart(){
		if (!RearrangeExchangeTasks.this.isAlive()&& exchangeTaskPoList.size() == 0) {			
			RearrangeExchangeTasks.this.start();
		}		
	}

	public void addTask(int taskAction, PhotobookPage selectPage,Layer selectLayer, int selectLayerPosition, PhotobookPage toPage,int toLayerPosition, Layer deleteLayer){			
		isStart();
		synchronized (exchangeTaskPoList) {		
			ExchangeTaskPo taskPo = new ExchangeTaskPo(taskAction, selectPage, selectLayer, selectLayerPosition, toPage, toLayerPosition, deleteLayer);	
			exchangeTaskPoList.add(taskPo);			
		}	
	}		
	
	private ExchangeTaskPo getTaskRunnable(){			
		synchronized (exchangeTaskPoList) {
			ExchangeTaskPo taskPo = null;	
			int size = exchangeTaskPoList.size();			
			if (size == 0) return taskPo;							
			int taskIndex = 0;//size-1
			taskPo = exchangeTaskPoList.get(taskIndex);			
			if (taskPo == null) return taskPo;							
			return taskPo;
		}
	}
		
	public void setSkipFlag(boolean fromRear){
		isSkipFlag = fromRear;			
		if (RearrangeExchangeTasks.this.isAlive() && exchangeTaskPoList.size() > 0) {
			sendStartMsg();	
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void run() {			
		while (isRunning) {					
			ExchangeTaskPo taskPo = getTaskRunnable();
			if (taskPo != null) {				
				Object result = taskPo.run(pbService,photoBookId);	
				synchronized (exchangeTaskPoList){
					if(result instanceof RssWebServiceException){
					
					} else {					
						ArrayList<PhotobookPage> pageList = (ArrayList<PhotobookPage>) result;
						for (int i = 0; i < pageList.size(); i++) {
							PhotobookPage page = pageList.get(i);					
							PhotoBookProductUtil.updatePageData(exchangePageList, page,false);	
						}
						exchangeTaskPoList.remove(taskPo);
						sendExchangeMsg();
					}					
				}			
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {					
					e.printStackTrace();
				}
			}else {									
				if (isSkipFlag) {
					for (PhotobookPage page : exchangePageList) {
						if (page == null) continue;
						PhotoBookProductUtil.updatePageInPhotobook(page, false);						
					}
					sendEndMsg();
				}else {					
					try {
						Thread.sleep(1000);
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

	@Override
	public synchronized void start() {
		isRunning = true;
		super.start();
		Log.d(TAG, "run start");
	}

	private void sendStartMsg(){
		if(handler != null && isSkipFlag){
			Message msg = handler.obtainMessage();
			msg.obj = new MsgRearData(PhotoBookRearTaskHandler.STATUS_START,true);
			msg.sendToTarget();
		}
	}
	
	private void sendEndMsg(){
		if(handler != null && exchangeTaskPoList.size() == 0){
			isRunning = false;	
			isSkipFlag = false;		
			Message msg = handler.obtainMessage();
			msg.obj = new MsgRearData(PhotoBookRearTaskHandler.STATUS_FINISH,true);
			msg.sendToTarget();			
		}
	}
	
	private void sendExchangeMsg(){
		if(handler != null){			
			Message msg = handler.obtainMessage();
			msg.obj = new MsgRearData(PhotoBookRearTaskHandler.STATUS_EXCHANGE,true);
			msg.sendToTarget();			
		}
	}
	
}
