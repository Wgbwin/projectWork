package com.kodak.rss.tablet.thread;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Message;

import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.bean.prints.Data;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.webservice.GreetingCardWebService;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.GCEditActivity;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfos;
import com.kodak.rss.tablet.handler.GCEditTaskHandler;
import com.kodak.rss.tablet.handler.GCEditTaskHandler.MsgData;
import com.kodak.rss.tablet.util.GreetingCardUtil;

public class GCEditTask extends Thread {
	private static final String TAG = "GreetingCardEditTask";
	public static final int ROTATE_CONTENT = 1;//drag for rotate
	public static final int CROP_IMAGE = 2;
	public static final int CROP_AND_ROTATE = 3;
	public static final int ENHANCE = 4;
	public static final int FLIP_VERTICAL_OR_HORIZONTAL = 6;
	public static final int RED_EYE = 8;
	public static final int COLOR_EFFECT = 9;
	public static final int REMOVE_IMAGE = 10;
	public static final int ROTATE_IMAGE = 20;//rotate for pop item
	
	private int taskId;
	private WeakReference<Context> contextRef;
	private GreetingCardWebService gcWs;
	private WebService ws;
	private GCEditTaskHandler handler;
	private GCPage page;
	private Layer layer;
	private Object[] params;
	
	public GCEditTask(Context context,int taskId, GCEditTaskHandler editTaskHandler, GCPage page, Layer layer, Object... params){
		this.taskId = taskId;
		this.page = page;
		this.layer = layer;
		this.params = params;
		this.contextRef = new WeakReference<Context>(context);
		this.gcWs = new GreetingCardWebService(context);
		this.ws = new WebService(context);
		this.handler = editTaskHandler;
	}
	
	public GCEditTask(Context context,int taskId, GCEditTaskHandler handler,GCPage page, Layer layer){
		this(context, taskId, handler, page, layer, new Object[]{});
	}
	
	@Override
	public void run() {
		sendStartMsg(taskId);
		Message msg = handler.obtainMessage();
		ProductLayerLocalInfo layerLocalInfo = null;
		//if this layer is not in local layer infos map, put it into the map
		if(layer != null && layer.type.equals(Layer.TYPE_IMAGE) && contextRef.get()!=null && contextRef.get() instanceof GCEditActivity){
			GCEditActivity activity = (GCEditActivity) contextRef.get();
			ProductLayerLocalInfos infos = RssTabletApp.getInstance().getProductLayerLocalInfos();
			if(!infos.contains(layer.contentId)){
				activity.addLayerLocalInfo(layer);
			}else{
				layerLocalInfo = infos.get(layer.contentId);
			}
			activity = null;//avoid memory leak
		}
		
		switch(taskId){
		case ROTATE_IMAGE:
			ROI roiRi = null;
			try {
				roiRi = ws.rotateImageTask(layer.contentId, -90);
				
				if(roiRi != null){
					if(layerLocalInfo != null){
						layerLocalInfo.isUseServerImage = true;
						layerLocalInfo.needRefresh();
						layerLocalInfo.rotate();
					}
					
					GCPage p = null;
					try {
						p = gcWs.layoutCardPageTask(page.id);
					} catch (RssWebServiceException e) {
						e.printStackTrace();
					}
					if(p != null){
						page = p;
						GreetingCardUtil.updatePageInCard(p, true);
					}else{
						for(int i=0;i<layer.data.length;i++){
							if(Data.FLAG_ROI_VAL.equals(layer.data[i].valueType)){
								layer.data[i].value = roiRi;
							}
						}
					}
				}
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, roiRi != null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;
		case CROP_IMAGE:
			ROI roi = (ROI) params[0];
			GCPage pageCrop = page;
			try {
				ws.setCropTask(layer.contentId, roi);
				for(int i=0;i<layer.data.length;i++){
					if(Data.FLAG_ROI_VAL.equals(layer.data[i].valueType)){
						layer.data[i].value = roi;
					}
				}
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, true, pageCrop, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, false, pageCrop==null? page : pageCrop, layer,e);
			}
			
			msg.sendToTarget();
			break;	
		case ENHANCE:
			int enLevel = (Integer)params[0];
			try {
				ws.setKPTLevelTask(layer.contentId, (Integer)params[0]);
				page.setPageRefresh();
				if(layerLocalInfo != null){
					layerLocalInfo.isUseServerImage = true;
					layerLocalInfo.needRefresh();
					layerLocalInfo.isEnhanced = enLevel == 1;
				}
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;
		case RED_EYE:
			boolean redRye = (Boolean) params[0];
			try {
				ws.setAutoRedEyeTask(layer.contentId, redRye);
				page.setPageRefresh();
				if(layerLocalInfo != null){
					layerLocalInfo.isUseServerImage = true;
					layerLocalInfo.needRefresh();
					layerLocalInfo.isRedEyed = redRye;
				}
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;
			
		case FLIP_VERTICAL_OR_HORIZONTAL:
			ROI roiFlip = null;
			try {
				roiFlip = ws.flipImageTask(layer.contentId, (Boolean)params[0]);
				if(roiFlip != null){
					//update roi in layer
					for(int i=0;i<layer.data.length;i++){
						if(Data.FLAG_ROI_VAL.equals(layer.data[i].valueType)){
							layer.data[i].value = roiFlip;
							break;
						}
					}
					
					if(layerLocalInfo!= null){
						layerLocalInfo.isUseServerImage = true;
						layerLocalInfo.needRefresh();
					}
					page.setPageRefresh();
				}
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, roiFlip!= null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;
		case COLOR_EFFECT:
			try {
				ws.setColorEffectTask(layer.contentId, (Integer) params[0]);
				if(layerLocalInfo != null){
					layerLocalInfo.isUseServerImage = true;
					layerLocalInfo.needRefresh();
					layerLocalInfo.colorEffectId = (Integer) params[0];
				}
				page.setPageRefresh();
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;
		case REMOVE_IMAGE:
			try {
				GCPage pageRi = gcWs.deleteImageFromCardTask(page.id, layer.contentId);
				if(pageRi != null){
					page = pageRi;									
					GreetingCardUtil.updatePageInCard(pageRi, true);						
				}
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, pageRi!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		}
			
	}
	
	private void sendStartMsg(int taskId){
		if(handler != null){
			Message msg = handler.obtainMessage();
			msg.obj = new MsgData(taskId, GCEditTaskHandler.STATUS_START,true,page,layer);
			msg.sendToTarget();
		}
	}
}
