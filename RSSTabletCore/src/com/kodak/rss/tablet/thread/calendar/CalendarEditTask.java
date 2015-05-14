package com.kodak.rss.tablet.thread.calendar;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Message;

import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.bean.prints.Data;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.webservice.CalendarWebService;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfos;
import com.kodak.rss.tablet.handler.CalendarEditTaskHandler;
import com.kodak.rss.tablet.handler.CalendarEditTaskHandler.MsgData;
import com.kodak.rss.tablet.util.CalendarUtil;

public class CalendarEditTask extends Thread{
	private static final String TAG = "CalendarEditTask";
	public static final int ENHANCE = 1;
	public static final int RED_EYE = 2;
	public static final int COLOR_EFFECT = 3;
	public static final int REMOVE_IMAGE = 4;
	public static final int FLIP_VERTICAL_OR_HORIZONTAL = 5;
	public static final int DELETE_CAPTION = 6;
	public static final int DELETE_PAGE_TEXT = 7;
	public static final int CROP_IMAGE = 8;
	
	private int taskId;
	private WeakReference<Context> contextRef;
	private CalendarWebService cWs;
	private WebService ws;
	private CalendarEditTaskHandler handler;
	private CalendarPage page;
	private CalendarLayer layer;
	private Object[] params;
	
	public CalendarEditTask(Context context,int taskId, CalendarEditTaskHandler editTaskHandler,CalendarPage page, CalendarLayer layer, Object... params){
		this.taskId = taskId;
		this.page = page;
		this.layer = layer;
		this.params = params;
		this.contextRef = new WeakReference<Context>(context);
		this.cWs = new CalendarWebService(context);
		this.ws = new WebService(context);
		this.handler = editTaskHandler;
	}
	
	@Override
	public void run() {
		sendStartMsg(taskId);
		Message msg = handler.obtainMessage();
		Calendar calendar = CalendarUtil.getCurrentCalendar();
		ProductLayerLocalInfo layerLocalInfo = null;
		
		//if this layer is not in local layer infos map, put it into the map
		if(layer != null && layer.type.equals(Layer.TYPE_IMAGE) && contextRef.get()!=null && contextRef.get() instanceof CalendarEditActivity){
			CalendarEditActivity activity = (CalendarEditActivity) contextRef.get();
			ProductLayerLocalInfos infos = RssTabletApp.getInstance().getProductLayerLocalInfos();
			if(!infos.contains(layer.contentId)){
				activity.addLayerLocalInfo(layer);
			}else{
				layerLocalInfo = infos.get(layer.contentId);
			}
			activity = null;//avoid memory leak
		}
		
		switch (taskId) {
		case CROP_IMAGE:
			ROI roiCrop = (ROI) params[0];
			CalendarPage pageCrop = null;
			
			try {
				ws.setCropTask(layer.contentId, roiCrop);
				
				try {
					pageCrop = cWs.layoutPageInCalendarTask(page.id);
				} catch (RssWebServiceException e) {
					Log.e(TAG, e);
				}
				
				if (pageCrop == null) {
					for(int i=0;i<layer.data.length;i++){
						if(Data.FLAG_ROI_VAL.equals(layer.data[i].valueType)){
							layer.data[i].value = roiCrop;
						}
					}
				} else {
					page = pageCrop;
				}
				
				CalendarUtil.updatePageInCalendar(page, true);
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		case ENHANCE :
			int level = (Integer) params[0];
			try {
				ws.setKPTLevelTask(layer.contentId, (Integer)params[0]);
				
				layerLocalInfo.isUseServerImage = true;
				layerLocalInfo.needRefresh();
				layerLocalInfo.isEnhanced = level == 1;
				
				CalendarUtil.updatePageInCalendar(page, true);
				
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		case RED_EYE:
			boolean redEye = (Boolean) params[0];
			try {
				ws.setAutoRedEyeTask(layer.contentId, redEye);
				
				layerLocalInfo.isUseServerImage = true;
				layerLocalInfo.needRefresh();
				layerLocalInfo.isRedEyed = redEye;
				
				CalendarUtil.updatePageInCalendar(page, true);
				
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		case COLOR_EFFECT:
			try {
				ws.setColorEffectTask(layer.contentId, (Integer) params[0]);
				
				layerLocalInfo.isUseServerImage = true;
				layerLocalInfo.needRefresh();
				layerLocalInfo.colorEffectId = (Integer) params[0];
				
				CalendarUtil.updatePageInCalendar(page, true);
				
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		case REMOVE_IMAGE:
			CalendarPage removePage = null;
			try {
				removePage = cWs.removeContentInCalendarPageTask(page.id, layer.contentId);
				if (removePage != null) {
					page = removePage;
					CalendarUtil.updatePageInCalendar(removePage, true);
				}
				
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		case FLIP_VERTICAL_OR_HORIZONTAL:
			ROI roiFlip = null;
			try {
				roiFlip = ws.flipImageTask(layer.contentId, (Boolean)params[0]);
				if (roiFlip != null) {
					if(layerLocalInfo!= null){
						layerLocalInfo.isUseServerImage = true;
						layerLocalInfo.needRefresh();
					}
					
					try {
						page = cWs.layoutPageInCalendarTask(page.id);
					} catch (RssWebServiceException e) {
						Log.e(TAG, e);
						//update roi in layer
						for(int i=0;i<layer.data.length;i++){
							if(Data.FLAG_ROI_VAL.equals(layer.data[i].valueType)){
								layer.data[i].value = roiFlip;
								break;
							}
						}
						
					}
					
					CalendarUtil.updatePageInCalendar(page, true);
				}
				
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		case DELETE_CAPTION:
			CalendarPage pageDelCaption = null;
			try {
				ws.setCaptionTask(layer.contentId, "");
				
				pageDelCaption = cWs.layoutPageInCalendarTask(page.id);
				
				if (pageDelCaption != null) {
					page = pageDelCaption;
				}
				
				CalendarUtil.updatePageInCalendar(page, true);
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		case DELETE_PAGE_TEXT:
			CalendarPage pageDelPageText;
			try {
				pageDelPageText = cWs.removeContentInCalendarPageTask(page.id, layer.contentId);
				
				if (pageDelPageText != null) {
					page = pageDelPageText;
				}
				
				CalendarUtil.updatePageInCalendar(page, true);
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		}
	}
	
	private void sendStartMsg(int taskId){
		if(handler != null){
			Message msg = handler.obtainMessage();
			msg.obj = new MsgData(taskId, CalendarEditTaskHandler.STATUS_START,true,page,layer);
			msg.sendToTarget();
		}
	}
	
}
