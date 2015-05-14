package com.kodak.rss.tablet.thread.collage;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Message;

import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.collage.CollageLayer;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.prints.Data;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.webservice.CollageWebService;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.CollageEditActivity;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfos;
import com.kodak.rss.tablet.handler.CollageEditTaskHandler;
import com.kodak.rss.tablet.handler.CollageEditTaskHandler.MsgData;
import com.kodak.rss.tablet.util.CollageUtil;

public class CollageEditTask extends Thread{
	private static final String TAG = "CalendarEditTask";
	
	public static final int COLOR_EFFECT = 1;	
	public static final int ENHANCE = 2;
	public static final int RED_EYE = 3;
	public static final int ROTATE_IMAGE = 4;
	public static final int REMOVE_IMAGE = 5;
	public static final int SET_AS_BACKGROUND = 6;
	public static final int FLIP_VERTICAL_OR_HORIZONTAL = 7;
	public static final int DELETE_PAGE_TEXT = 8;
	
	public static final int MOVE_IMAGE = 9;	
	public static final int ROTATE_CONTENT = 10;
	public static final int MOVE_AND_ROTATE = 11;
	
	private int taskId;
	private WeakReference<Context> contextRef;
	private CollageWebService cWs;
	private WebService ws;
	private CollageEditTaskHandler handler;
	private CollagePage page;
	private CollageLayer layer;
	private Object[] params;
	
	public CollageEditTask(Context context,int taskId, CollageEditTaskHandler editTaskHandler,CollagePage page, CollageLayer layer, Object... params){
		this.taskId = taskId;
		this.page = page;
		this.layer = layer;
		this.params = params;
		this.contextRef = new WeakReference<Context>(context);
		this.cWs = new CollageWebService(context);
		this.ws = new WebService(context);
		this.handler = editTaskHandler;
	}
	
	@Override
	public void run() {
		sendStartMsg(taskId);
		Message msg = handler.obtainMessage();
		
		ProductLayerLocalInfo layerLocalInfo = null;		
		if(layer != null && layer.type.equals(Layer.TYPE_IMAGE) && contextRef.get()!=null && contextRef.get() instanceof CollageEditActivity){
			CollageEditActivity activity = (CollageEditActivity) contextRef.get();
			ProductLayerLocalInfos infos = RssTabletApp.getInstance().getProductLayerLocalInfos();
			if(!infos.contains(layer.contentId)){
				activity.addLayerLocalInfo(layer);
			}else{
				layerLocalInfo = infos.get(layer.contentId);
			}
			activity = null;//avoid memory leak
		}
		
		switch (taskId) {
		case COLOR_EFFECT:
			try {
				ws.setColorEffectTask(layer.contentId, (Integer) params[0]);
				if(layerLocalInfo != null){
					layerLocalInfo.isUseServerImage = true;
					layerLocalInfo.needRefresh();
					layerLocalInfo.colorEffectId = (Integer) params[0];					
				}			
				CollageUtil.updatePageInCollage(page, true, false);	
				
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}			
			msg.sendToTarget();
			break;		
		
		case ENHANCE :
			int level = (Integer) params[0];
			try {
				ws.setKPTLevelTask(layer.contentId, (Integer)params[0]);
				if(layerLocalInfo != null){
					layerLocalInfo.isUseServerImage = true;
					layerLocalInfo.needRefresh();
					layerLocalInfo.isEnhanced = level == 1;
				}
				CollageUtil.updatePageInCollage(page, true, false);
				
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			msg.sendToTarget();
			break;
			
		case RED_EYE:
			boolean redEye = (Boolean) params[0];
			try {
				ws.setAutoRedEyeTask(layer.contentId, redEye);
				if(layerLocalInfo != null){
					layerLocalInfo.isUseServerImage = true;
					layerLocalInfo.needRefresh();
					layerLocalInfo.isRedEyed = redEye;
				}
				CollageUtil.updatePageInCollage(page, true, false);
				
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}			
			msg.sendToTarget();
			break;
		
		case ROTATE_IMAGE:			
			ROI roiRi = null;
			try {
				roiRi = cWs.rotateImageTask(layer.contentId, -90);
				if(roiRi != null){
					if(layerLocalInfo != null){
						layerLocalInfo.isUseServerImage = true;
						layerLocalInfo.needRefresh();
						layerLocalInfo.rotate();
					}
					
					CollagePage collagePage = null;
					try {
						collagePage = cWs.layoutCollagePageTask(page.id,true);
					} catch (RssWebServiceException e) {
						e.printStackTrace();
					}
					if(collagePage != null){
						page = collagePage;
						CollageUtil.updatePageInCollage((CollagePage) page, true, true);
					}else{
						for(int i=0;i<layer.data.length;i++){
							if(Data.FLAG_ROI_VAL.equals(layer.data[i].valueType)){
								layer.data[i].value = roiRi;
							}
						}
						CollageUtil.updatePageInCollage((CollagePage) page, true, false);
					}
				}
		    
			    msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, roiRi != null, page, layer);			     
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;
			
		case SET_AS_BACKGROUND:
			try {
				String cloneImgIdSetB = ws.cloneImageTask(layer.contentId);
				CollagePage pageSetB = null;
				if(cloneImgIdSetB != null && !cloneImgIdSetB.isEmpty()){
					pageSetB = cWs.setCollageBackgroundImageTask(page.id, cloneImgIdSetB,1);
				}
				
				if(pageSetB != null){
					page = pageSetB;
					CollageUtil.updatePageInCollage(pageSetB, true, false);				
				}
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, pageSetB!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, false, page, layer, e);
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
					CollageUtil.updatePageInCollage(page, true, false);
				}
				
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
			
		case REMOVE_IMAGE:
		case DELETE_PAGE_TEXT:
			CollagePage pageDelPageText;
			try {
				pageDelPageText = cWs.removeCollageContentTask(page.id, layer.contentId,true);
				
				if (pageDelPageText != null) {
					CollageUtil.deleteImageInfo(layer.type,layer.contentId);										
					page = pageDelPageText;
				}				
				CollageUtil.updatePageInCollage(page, true, true);
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				Log.e(TAG, e);
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
			
		case MOVE_IMAGE:
			ROI roi = (ROI) params[0];		
			try {
				ws.setCropTask(layer.contentId, roi);
				
				for(int i=0;i<layer.data.length;i++){
					if(Data.FLAG_ROI_VAL.equals(layer.data[i].valueType)){
						layer.data[i].value = roi;
					}
				}				
				
				page.setPageRefresh();

//				Collage currentCollage = CollageUtil.getCurrentCollage();
//				Collage collage = cWs.getCollageTask(currentCollage.id, true);
//				CollageUtil.updateCollage(collage, true);	
				
				if(layerLocalInfo!= null){
					layerLocalInfo.isUseServerImage = true;
					layerLocalInfo.needRefresh();
				}
				
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;	
			
		case ROTATE_CONTENT:
			try {
				CollagePage newPage = cWs.rotateCollageContentTask(page.id, layer.contentId, (Float) params[0],false);
				if(newPage != null){
					page = newPage;
					CollageUtil.updatePageInCollage(page, true, false);				
				}
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, newPage!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}			
			
			msg.sendToTarget();
			break;	
			
		case MOVE_AND_ROTATE:
			ROI roi2 = (ROI)params[0];
			boolean succeedCrop = false;
			RssWebServiceException exCAR = null;
			try {
				ws.setCropTask(layer.contentId, roi2);
				succeedCrop = true;
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				succeedCrop = false;
				exCAR = e;
			}
			if(succeedCrop){
				page.setPageRefresh();
				for(int i=0;i<layer.data.length;i++){
					if(Data.FLAG_ROI_VAL.equals(layer.data[i].valueType)){
						layer.data[i].value = roi2;
					}
				}
			}
						
			CollagePage newPageRotate = null;
			try {
				newPageRotate = cWs.rotateCollageContentTask(page.id, layer.contentId, (Float) params[1],false);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				exCAR = e;
			}
			if(newPageRotate!=null){
				page = newPageRotate;
				CollageUtil.updatePageInCollage(page, true, false);						
			}
			
			if(newPageRotate!=null || succeedCrop){
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
			}else{
				msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_FINISH, false, page, layer,exCAR);
			}
			
			msg.sendToTarget();
			break;	
		}
	}
	
	private void sendStartMsg(int taskId){
		if(handler != null){
			Message msg = handler.obtainMessage();
			msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_START,true,page,layer);
			msg.sendToTarget();
		}
	}
	
}
