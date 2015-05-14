package com.kodak.rss.tablet.handler;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.thread.PhotoBookEditTask;

/**
 * @author Robin.Qian
 *
 */
public class PhotoBookEditTaskHandler extends Handler{
	private static final String TAG = "PhotoBookEditTaskHandler";
	public static final int STATUS_START = 1;
	public static final int STATUS_FINISH = 2;
	
	private WeakReference<PhotoBooksProductActivity> activityRef;
	
	public PhotoBookEditTaskHandler(PhotoBooksProductActivity activity){
		this.activityRef = new WeakReference<PhotoBooksProductActivity>(activity);
	}
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		PhotoBooksProductActivity activity = activityRef.get();
		
		if(activity == null || activity.isFinishing()){
			return;
		}
		
		if(msg.obj != null){
			MsgData data = (MsgData) msg.obj;
			
			if(data.taskId == PhotoBookEditTask.PAGE_BACKGROND_COPY
					|| data.taskId == PhotoBookEditTask.PAGE_BACKGROND_EXTEND
					|| data.taskId == PhotoBookEditTask.PAGE_BACKGROND_REMOVE //page background options
					|| data.taskId == PhotoBookEditTask.MOVE_CONTENT
					|| data.taskId == PhotoBookEditTask.SWAP_CONTENT
					|| data.taskId == PhotoBookEditTask.ADD_PAGE
					|| data.taskId == PhotoBookEditTask.DELETE_PAGE
					){
				if(data.status == STATUS_START){
					activity.dialogPleaseWait.show();
				}else if(data.status == STATUS_FINISH){
					activity.dialogPleaseWait.dismiss();
					if(data.succeed){
						//notify changed
						switch(data.taskId){
						case PhotoBookEditTask.ADD_PAGE:
						case PhotoBookEditTask.DELETE_PAGE:
							activity.notifyPhotoBookChanged();
							break;
						default:
							activity.notifyPhotoBookPagesChanged();
						}
					}else{
						showErrorWarning(activity, data);
					}
				}
			}else if(data.status == STATUS_START){
				if(data.layer == null){//editing page
					activity.layerEdit.showPageEditProgress(data.page);
				}else{
					activity.layerEdit.showLayerEditProgress(data.page,data.layer);
				}
			}else if(data.status == STATUS_FINISH && !data.succeed){
				activity.layerEdit.dismissEditProgress();
				activity.layerEdit.dismiss();
				
				showErrorWarning(activity, data);
			}else if(data.status == STATUS_FINISH && data.succeed){
				activity.layerEdit.dismissEditProgress();
				activity.layerEdit.dismiss();
				activity.notifyPhotoBookPagesChanged();
				
				//if task is add page text, app should show edit text dialog after task succeed.
				if(data.taskId == PhotoBookEditTask.ADD_PAGE_TEXT){
					boolean left = (Boolean) data.params[0];
					Layer layer = (Layer) data.params[1];
					activity.showFontEditView(left, data.page, layer, false);
				}
			}
			
		}
		
	}
	
	public static class MsgData{
		int taskId;
		int status;
		boolean succeed;
		PhotobookPage page;
		Layer layer;
		Object[] params;
		
		public MsgData(int taskId,int status,boolean succeed,PhotobookPage page, Layer layer,Object... params){
			this.taskId = taskId;
			this.status = status;
			this.succeed = succeed;
			this.page = page;
			this.layer = layer;
			this.params = params;
		}
		
		public MsgData(int taskId,int status,boolean succeed,PhotobookPage page,Layer layer){
			this(taskId, status, succeed, page,layer,new Object[]{});
		}
		
	}
	
	private void showErrorWarning(PhotoBooksProductActivity activity, MsgData data){
		//show error warning
		boolean shown = false;
		if(data.params != null){
			for(int i=0;i<data.params.length;i++){
				if(data.params[i] instanceof RssWebServiceException){
					activity.showErrorWarning((RssWebServiceException) data.params[0]);
					shown = true;
				}
			}
		}
		
		if(!shown){
			Log.e(TAG, "Havn't find the exception, there must be something wrong");
		}
	}
}
