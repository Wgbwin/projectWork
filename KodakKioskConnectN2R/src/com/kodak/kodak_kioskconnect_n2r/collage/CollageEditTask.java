package com.kodak.kodak_kioskconnect_n2r.collage;

import java.lang.ref.WeakReference;
import java.util.Iterator;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.activity.CollageEditActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductLayerLocalInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductLayerLocalInfos;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageEditTaskHandler.MsgData;
import com.kodak.kodak_kioskconnect_n2r.webservices.CollageWebServices;
import com.kodak.utils.ProductUtil;

public class CollageEditTask extends Thread {
	private static final String TAG = "CalendarEditTask";
	public static final int ENHANCE = 1;
	public static final int RED_EYE = 2;
	public static final int COLOR_EFFECT = 3;
	public static final int REMOVE_IMAGE = 4;
	public static final int FLIP_VERTICAL_OR_HORIZONTAL = 5;
	public static final int DELETE_CAPTION = 6;
	public static final int DELETE_PAGE_TEXT = 7;
	public static final int CROP_IMAGE = 8;
	public static final int ROTATE_PAGE = 9;
	public static final int CROP_AND_ROTATE = 10;
	public static final int ROTATE_CONTENT = 11;
	public static final int PAGE_DELETE_PAGE_TEXT = 12;

	private int taskId;
	private WeakReference<Context> contextRef;
	private CollageWebServices cWs;
	private CollageEditTaskHandler handler;
	private CollagePage page;
	private Layer layer;
	private Object[] params;
	private Context context;

	public CollageEditTask(Context context, int taskId,CollageEditTaskHandler editTaskHandler, CollagePage page,Layer layer, Object... params) {
		this.taskId = taskId;
		this.page = page;
		this.layer = layer;
		this.params = params;
		this.contextRef = new WeakReference<Context>(context);
		this.cWs = new CollageWebServices(context, "");
		this.handler = editTaskHandler;
		this.context = context;
		
	}

	@Override
	public void run() {
		sendStartMsg(taskId);
		Message msg = handler.obtainMessage();

		ProductLayerLocalInfo layerLocalInfo = null;
		// if this layer is not in local layer infos map, put it into the map
		if (layer != null && layer.type.equals(Layer.TYPE_IMAGE)&& contextRef.get() != null&& contextRef.get() instanceof CollageEditActivity) {
			CollageEditActivity activity = (CollageEditActivity) contextRef.get();
			ProductLayerLocalInfos infos = AppContext.getApplication().getProductLayerLocalInfos();

			if (!infos.contains(layer.contentId)) {
				activity.addLayerLocalInfo(layer);
			} else {
				layerLocalInfo = infos.get(layer.contentId);
			}
			activity = null;// avoid memory leak
		}

		switch (taskId) {

		case CROP_IMAGE:
			try {
				ROI oldRoi = layer.location;
				ROI newROI = null;
				newROI = (ROI) params[0];
				if (newROI != null && newROI != oldRoi) {
					String cropString=cWs.pbSetImageCrop(context, layer.contentId, newROI);
					if (!TextUtils.isEmpty(cropString)) {
						updateCollagePage();
						layerLocalInfo.needRefresh();
						msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, true, page,layer);
					}
				
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer, e);
			}

			msg.sendToTarget();
			Log.e(TAG, "crop only");
			break;
		case ROTATE_CONTENT:
			try {
				int angle = (Integer) params[0];
				CollagePage collagePage = cWs.rotateCollageContentTask(page.id,layer.contentId, angle);
				CollageManager.getInstance().updataCurrentCollagePage(collagePage);
				layerLocalInfo.needRefresh();
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer, e);
			}

			msg.sendToTarget();
			Log.e(TAG, "rotate content");
			break;
		case CROP_AND_ROTATE:
			Log.e(TAG, "rotate and crop");
			boolean succeedCrop = false;
			try {
				ROI newRoi = (ROI) params[0];
				cWs.pbSetImageCrop(context, layer.contentId, newRoi);
				succeedCrop = true;
			} catch (Exception e) {
				e.printStackTrace();
				succeedCrop = false;
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer, e);
			}
			try {
				if (succeedCrop) {
					Log.e(TAG, "succeedCrop==" + succeedCrop);
					CollagePage collagePage = cWs.rotateCollageContentTask(page.id, layer.contentId, layer.angle);
					CollageManager.getInstance().updataCurrentCollagePage(collagePage);
					layerLocalInfo.needRefresh();
					msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, true, page,layer);

				}

			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer, e);
			}
			msg.sendToTarget();
			break;

		case ROTATE_PAGE:

			try {
				String rotatePageString=cWs.pbRotateImageDegree(context, layer.contentId, -90);
				Log.d(TAG, "rotatePageString="+rotatePageString);
				if (!TextUtils.isEmpty(rotatePageString)) {
					ROI oldRoi=ProductUtil.getImageCropROI(layer);
					Log.d(TAG, "oldROI="+oldRoi);
					ROI newRoi=new ROI();
					
					newRoi.ContainerW=oldRoi.ContainerH;
					newRoi.ContainerH=oldRoi.ContainerW;
					newRoi.w=oldRoi.w;
					newRoi.h=oldRoi.h;
					newRoi.x=oldRoi.y+oldRoi.h/2-oldRoi.w/2;
					newRoi.y=oldRoi.x+oldRoi.w/2-oldRoi.h/2;
					if (newRoi.x<0) {
						newRoi.x=0;
						newRoi.w=newRoi.ContainerW;
						newRoi.h=(oldRoi.h/oldRoi.w)*newRoi.w;
						newRoi.y=(oldRoi.h-newRoi.h)/2;
					}
					if (newRoi.y<0) {
						newRoi.y=0;
						newRoi.h=newRoi.ContainerH;
						newRoi.w=(oldRoi.w/oldRoi.h)*newRoi.h;
						newRoi.x=(oldRoi.w-newRoi.w)/2;
					}
					Log.d(TAG, "newROI="+newRoi);
					cWs.pbSetImageCrop(context, layer.contentId, newRoi);
					updateCollagePage();
					layerLocalInfo.isUseServerImage = true;
					layerLocalInfo.needRefresh();
					layerLocalInfo.rotate();
					msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer, e);
			}

			msg.sendToTarget();
			break;
		case ENHANCE:
			int level = (Integer) params[0];
			try {

				boolean succeed = cWs.setKPTLevelTask(layer.contentId, level);
				if (succeed) {
					updateCollagePage();
					layerLocalInfo.isUseServerImage = true;
					layerLocalInfo.needRefresh();
					layerLocalInfo.isEnhanced = level == 1;
					msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, succeed, page,layer);
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer, e);
			}

			msg.sendToTarget();
			Log.d(TAG, "ENHANCE");
			break;
		case RED_EYE:

			boolean redEye = (Boolean) params[0];
			try {
				boolean succeed =cWs.setAutoRedEyeTask(layer.contentId, (Boolean) params[0]);
				if (succeed) {
					updateCollagePage();
					layerLocalInfo.isUseServerImage = true;
					layerLocalInfo.needRefresh();
					layerLocalInfo.isRedEyed = redEye;
					msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer, e);
			}

			msg.sendToTarget();
			break;
		case COLOR_EFFECT:
			try {
				boolean succeed=cWs.setColorEffectTask(layer.contentId, (Integer) params[0]);
				if (succeed) {
					updateCollagePage();
					layerLocalInfo.isUseServerImage = true;
					layerLocalInfo.needRefresh();
					layerLocalInfo.colorEffectId = (Integer) params[0];
					msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer, e);
			}

			msg.sendToTarget();
			break;
		case REMOVE_IMAGE:
			try {
				Log.e(TAG, "REMOVE_IMAGE");
				CollagePage pageRemove = cWs.removeCollageContentTask(page.id,layer.contentId);
				if (pageRemove != null) {
					Collage currentCollage = CollageManager.getInstance().getCurrentCollage();
					CollagePage currentCollagePage = currentCollage.page;
					if (layer.type.equals("Image")&& currentCollagePage.getPhotosInCollagePage() != null&& currentCollagePage.getPhotosInCollagePage().size() > 0) {
						Iterator<PhotoInfo> iter = currentCollagePage.getPhotosInCollagePage().iterator();
						while (iter.hasNext()) {
							PhotoInfo photo = iter.next();
							if (photo.getContentId().equals(layer.contentId)) {
								iter.remove();
								break;
							}
						}
					}
					CollageManager.getInstance().updataCurrentCollagePage(pageRemove) ;
					page = pageRemove;
					layerLocalInfo.needRefresh();
					msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, true, page,layer);
				} else {
					msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer);
				}

			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer, e);
			}

			msg.sendToTarget();
			break;
		case PAGE_DELETE_PAGE_TEXT:
			try {
				Log.e(TAG, "REMOVE_IMAGE");
				CollagePage pageRemove = cWs.removeCollageContentTask(page.id,layer.contentId);
				if (pageRemove != null) {
					Collage currentCollage = CollageManager.getInstance().getCurrentCollage();
					CollagePage currentCollagePage = currentCollage.page;
					if (layer.type.equals("Image")&& currentCollagePage.getPhotosInCollagePage() != null&& currentCollagePage.getPhotosInCollagePage().size() > 0) {
						Iterator<PhotoInfo> iter = currentCollagePage.getPhotosInCollagePage().iterator();
						while (iter.hasNext()) {
							PhotoInfo photo = iter.next();
							if (photo.getContentId().equals(layer.contentId)) {
								iter.remove();
								break;
							}
						}
					}
					int currentTextLayerNumber = CollageManager.getInstance().getCurrentCollage().page.getTextLayerNumber();
					currentCollagePage.setTextLayerNumber(currentTextLayerNumber-1) ;
					CollageManager.getInstance().updataCurrentCollagePage(pageRemove) ;
					
					page = pageRemove;
					msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, true, page,layer);
				} else {
					msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer);
				}

			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer, e);
			}

			msg.sendToTarget();
			break;
		case FLIP_VERTICAL_OR_HORIZONTAL:
			ROI roiFlip = null;
			try {
				Log.e(TAG, "ROI="+params[0]);
				roiFlip = cWs.flipImageTask(layer.contentId,(Boolean) params[0]);
				if (null!=roiFlip) {
					
					page=CollageManager.getInstance().getCurrentCollage().page;
					layerLocalInfo.needRefresh();
				}
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, true, page, layer);
			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = new MsgData(taskId,CollageEditTaskHandler.STATUS_FINISH, false, page,layer, e);
			}

			msg.sendToTarget();
			break;
		}
	}

	private void sendStartMsg(int taskId) {
		if (handler != null) {
			Message msg = handler.obtainMessage();
			msg.obj = new MsgData(taskId, CollageEditTaskHandler.STATUS_START,true, page, layer);
			msg.sendToTarget();
		}
	}
	private void updateCollagePage(){
		String collageId=CollageManager.getInstance().getCurrentCollage().id;
		Collage  collage=cWs.getCollageTask(collageId);
		CollageManager.getInstance().updataCurrentCollagePage(collage.page);
	}

}
