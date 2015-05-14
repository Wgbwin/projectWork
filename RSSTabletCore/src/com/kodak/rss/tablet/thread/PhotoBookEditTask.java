package com.kodak.rss.tablet.thread;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Message;

import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Data;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.text.TextBlock;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfos;
import com.kodak.rss.tablet.handler.PhotoBookEditTaskHandler;
import com.kodak.rss.tablet.handler.PhotoBookEditTaskHandler.MsgData;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;

/**
 * @author Robin.Qian
 *
 */
public class PhotoBookEditTask extends Thread{
	private static final String TAG = "PhotoBookEditTask";
	public static final int ROTATE_CONTENT = 1;//drag for rotate
	public static final int CROP_IMAGE = 2;
	public static final int CROP_AND_ROTATE = 3;
	public static final int ENHANCE = 4;
	public static final int FLIP_VERTICAL_OR_HORIZONTAL = 6;
	public static final int SET_AS_PAGE_BACKGROUND = 7;
	public static final int RED_EYE = 8;
	public static final int COLOR_EFFECT = 9;
	public static final int REMOVE_IMAGE = 10;
	public static final int ADD_PAGE = 11;
	public static final int DELETE_PAGE = 12;
	public static final int PAGE_BACKGROND_COPY = 13;
	public static final int PAGE_BACKGROND_EXTEND = 14;
	public static final int PAGE_BACKGROND_REMOVE = 15;
	public static final int MOVE_CONTENT = 16;
	public static final int ADD_PAGE_TEXT = 17;
	public static final int DELETE_CAPTION = 18;
	public static final int DELETE_PAGE_TEXT = 19;
	public static final int ROTATE_IMAGE = 20;//rotate for pop item
	public static final int SWAP_CONTENT = 21;//swapContent
	
	private int taskId;
	private WeakReference<Context> contextRef;
	private PhotobookWebService pbWs;
	private WebService ws;
	private PhotoBookEditTaskHandler handler;
	private PhotobookPage page;
	private Layer layer;
	private Object[] params;

	public PhotoBookEditTask(Context context,int taskId, PhotoBookEditTaskHandler editTaskHandler,PhotobookPage page, Layer layer, Object... params){
		this.taskId = taskId;
		this.page = page;
		this.layer = layer;
		this.params = params;
		this.contextRef = new WeakReference<Context>(context);
		this.pbWs = new PhotobookWebService(context);
		this.ws = new WebService(context);
		this.handler = editTaskHandler;
	}
	
	public PhotoBookEditTask(Context context,int taskId, PhotoBookEditTaskHandler handler,PhotobookPage page, Layer layer){
		this(context, taskId, handler, page, layer, new Object[]{});
	}
	
	@Override
	public void run() {
		sendStartMsg(taskId);
		Message msg = handler.obtainMessage();
		Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();	
		ProductLayerLocalInfo layerLocalInfo = null;
		//if this layer is not in local layer infos map, put it into the map
		if(layer != null && layer.type.equals(Layer.TYPE_IMAGE) && contextRef.get()!=null && contextRef.get() instanceof PhotoBooksProductActivity){
			PhotoBooksProductActivity activity = (PhotoBooksProductActivity) contextRef.get();
			ProductLayerLocalInfos infos = RssTabletApp.getInstance().getProductLayerLocalInfos();
			if(!infos.contains(layer.contentId)){
				activity.addLayerLocalInfo(layer);
			}else{
				layerLocalInfo = infos.get(layer.contentId);
			}
			activity = null;//avoid memory leak
		}
		
		switch(taskId){
		case ROTATE_CONTENT:
			try {
				PhotobookPage newPage = pbWs.rotateContentTask(page.id, layer.contentId, (Float) params[0]);
				if(newPage != null){
					page = newPage;
					PhotoBookProductUtil.updatePageInPhotobook(newPage, true);				
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, newPage!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}			
			
			msg.sendToTarget();
			break;
		case CROP_IMAGE:
			ROI roi = (ROI) params[0];
			PhotobookPage pageCrop = page;
			try {
				ws.setCropTask(layer.contentId, roi);
				try {
					pageCrop = pbWs.layoutPageTask(page.id);
					if(pageCrop != null){
						PhotoBookProductUtil.updatePageInPhotobook(pageCrop, true);						
					}else{					
						for(int i=0;i<layer.data.length;i++){
							if(Data.FLAG_ROI_VAL.equals(layer.data[i].valueType)){
								layer.data[i].value = roi;
							}
						}
					}
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, true, pageCrop, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, pageCrop==null? page : pageCrop, layer,e);
			}
			
			msg.sendToTarget();
			break;
		case CROP_AND_ROTATE:
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
						
			PhotobookPage newPageRotate = null;
			try {
				newPageRotate = pbWs.rotateContentTask(page.id, layer.contentId, (Float) params[1]);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				exCAR = e;
			}
			if(newPageRotate!=null){
				page = newPageRotate;
				PhotoBookProductUtil.updatePageInPhotobook(newPageRotate, true);					
			}
			
			if(newPageRotate!=null || succeedCrop){
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, true, page, layer);
			}else{
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer,exCAR);
			}
			
			msg.sendToTarget();
			break;
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
					
					PhotobookPage p = null;
					try {
						p = pbWs.layoutPageTask(page.id);
					} catch (RssWebServiceException e) {
						e.printStackTrace();
					}
					if(p != null){
						page = p;
						PhotoBookProductUtil.updatePageInPhotobook(p,true);
					}else{
						for(int i=0;i<layer.data.length;i++){
							if(Data.FLAG_ROI_VAL.equals(layer.data[i].valueType)){
								layer.data[i].value = roiRi;
							}
						}
					}
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, roiRi != null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer,e);
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
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer,e);
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
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer,e);
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
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, roiFlip!= null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;
		case SET_AS_PAGE_BACKGROUND:
			try {
				String cloneImgIdSetB = ws.cloneImageTask(layer.contentId);
				PhotobookPage pageSetB = null;
				if(cloneImgIdSetB != null && !cloneImgIdSetB.isEmpty()){
					pageSetB = pbWs.setPageBackgroundTask(currentPhotoBook.id, page.id, cloneImgIdSetB);
				}
				
				if(pageSetB != null){
					page = pageSetB;
					PhotoBookProductUtil.updatePageInPhotobook(pageSetB, true);				
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, pageSetB!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer, e);
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
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;
		case REMOVE_IMAGE:
			try {
				PhotobookPage pageRi = pbWs.removeContentFromPageTask(currentPhotoBook.id, page.id, layer.contentId);
				if(pageRi != null){
					page = pageRi;
					PhotoBookProductUtil.updatePageInPhotobook(pageRi, true);						
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, pageRi!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		case DELETE_PAGE_TEXT:
			try {
				PhotobookPage pageDelPageText = pbWs.removeContentFromPageTask(currentPhotoBook.id, page.id, layer.contentId);
				if(pageDelPageText != null){
					page = pageDelPageText;
					PhotoBookProductUtil.updatePageInPhotobook(pageDelPageText, true);	
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, pageDelPageText!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;
		case ADD_PAGE:
			int index = page.sequenceNumber;
			if(page.sequenceNumber==1 && PhotoBookProductUtil.isTitlePage(page)){
				index++;
			}
			try {
				Photobook pbAp = pbWs.addPageToPhotobookTask(currentPhotoBook.id, index, null);
				if(pbAp!=null){
					PhotoBookProductUtil.setCurrentPhotoBook(pbAp);
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, pbAp!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;
		case DELETE_PAGE:
			try {
				Photobook pbDp = pbWs.deletePhotobookPageTask(currentPhotoBook.id, page.id);
				if(pbDp!=null){
					PhotoBookProductUtil.setCurrentPhotoBook(pbDp);
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, pbDp!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		case PAGE_BACKGROND_COPY:
			PhotobookPage pageBCopy = null;
			try {
				String cloneImgId = ws.cloneImageTask(page.backgroundImageId);
				if(cloneImgId != null && !cloneImgId.isEmpty()){
					pageBCopy = pbWs.setPageBackgroundTask(currentPhotoBook.id, ((PhotobookPage)params[0]).id, cloneImgId);
				}
				if(pageBCopy !=null){
					PhotoBookProductUtil.updatePageInPhotobook(pageBCopy, true);	
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, pageBCopy!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer,e);
			}
			
			msg.sendToTarget();
			break;
		case PAGE_BACKGROND_EXTEND:
			PhotobookPage pageBExt = (PhotobookPage) params[0];
			String cloneImgIdBExt = null;
			RssWebServiceException exExt = null;
			try {
				cloneImgIdBExt = ws.cloneImageTask(page.backgroundImageId);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				exExt = e;
			}
			
			boolean cropBExt = false;
			PhotobookPage pageBExtResult = null;
			
			if(cloneImgIdBExt != null && !cloneImgIdBExt.isEmpty()){
				//crop page background
				ROI roiBExt = new ROI();
				roiBExt.x = page.sequenceNumber < pageBExt.sequenceNumber ? 0 : 0.5;
				roiBExt.y = 0;
				roiBExt.w = 0.5;
				roiBExt.h = 1.0;
				
				try {
					ws.setCropTask(page.backgroundImageId, roiBExt);
				} catch (RssWebServiceException e) {
					e.printStackTrace();
					exExt = e;
				}
				cropBExt = true;
				
				//set page background for page extend
				if(cropBExt){
					try {
						pageBExtResult = pbWs.setPageBackgroundTask(currentPhotoBook.id, pageBExt.id, cloneImgIdBExt);
					} catch (RssWebServiceException e) {
						e.printStackTrace();
						exExt = e;
					}
				}
				
				//crop page background for page extend
				if(pageBExtResult != null){
					roiBExt.x = page.sequenceNumber < pageBExt.sequenceNumber ? 0.5 : 0;
					try {
						ws.setCropTask(cloneImgIdBExt, roiBExt);
					} catch (RssWebServiceException e) {
						e.printStackTrace();
						exExt = e;
					}
				}
				
			}
			
			if(cropBExt){
				page.setPageRefresh();
			}
			
			if(pageBExtResult != null){
				PhotoBookProductUtil.updatePageInPhotobook(pageBExtResult, true);						
			}
			
			if( cropBExt||pageBExtResult!=null ){
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, true, page, layer);
			}else{
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer,exExt);
			}
			
			msg.sendToTarget();
			
			break;
		case PAGE_BACKGROND_REMOVE:
			try {
				PhotobookPage pageBkRe = pbWs.setPageBackgroundTask(currentPhotoBook.id, page.id, "");
				if(pageBkRe != null){
					page = pageBkRe;
					PhotoBookProductUtil.updatePageInPhotobook(pageBkRe, true);					
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, pageBkRe!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			msg.sendToTarget();
			break;
		case MOVE_CONTENT:
			try {
				List<PhotobookPage> pagesMc = pbWs.moveContentTask(currentPhotoBook.id, page.id, (String)params[0], layer.contentId);
				if(pagesMc != null && !pagesMc.isEmpty()){
					for (PhotobookPage photobookPage : pagesMc) {
						if(photobookPage.id.equals(page.id)){
							page = photobookPage;
						}
						PhotoBookProductUtil.updatePageInPhotobook(photobookPage, true);						
					}
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, pagesMc != null && !pagesMc.isEmpty(), page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer , e);
			}
			
			msg.sendToTarget();
			break;
		case ADD_PAGE_TEXT:
			try {
				TextBlock textBlock = pbWs.getTextBlockForPageTask(page.id);
				if(textBlock != null){
					pbWs.setTextTask(textBlock.id, contextRef.get().getResources().getString(R.string.Common_SampleText));
						List<String> contentIds = new ArrayList<String>();
						contentIds.add(textBlock.id);
						PhotobookPage tempPage = pbWs.insertContentTask(currentPhotoBook.id, page.id, contentIds);
						Layer layerApt = null;
						if(tempPage != null){
							PhotoBookProductUtil.updatePageInPhotobook(tempPage, true);
							page = tempPage;
							//find layer in the page
							layerApt = PhotoBookProductUtil.findLayerById(tempPage, textBlock.id);
						}
						
						msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, tempPage != null, page, layer, params[0],layerApt);
				}
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer , e);
			}
			msg.sendToTarget();
			break;
		case DELETE_CAPTION:
			PhotobookPage pageDelCaption = null;
			try {
				ws.setCaptionTask(layer.contentId, "");
				try {
					pageDelCaption = pbWs.layoutPageTask(page.id);
					if(pageDelCaption != null){
						page = pageDelCaption;
						PhotoBookProductUtil.updatePageInPhotobook(pageDelCaption, true);						
					}
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;
		case SWAP_CONTENT:
			String slayerId = (String) params[0];			
			try {
				PhotobookPage pageSwapContent = pbWs.swapContentTask(page.id,layer.contentId, slayerId);
				if(pageSwapContent != null){
					page = pageSwapContent;
					PhotoBookProductUtil.updatePageInPhotobook(pageSwapContent, true);					
				}
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, pageSwapContent!=null, page, layer);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_FINISH, false, page, layer, e);
			}
			
			msg.sendToTarget();
			break;	
	
		}			
	}
	
	private void sendStartMsg(int taskId){
		if(handler != null){
			Message msg = handler.obtainMessage();
			msg.obj = new MsgData(taskId, PhotoBookEditTaskHandler.STATUS_START,true,page,layer);
			msg.sendToTarget();
		}
	}
	
}
