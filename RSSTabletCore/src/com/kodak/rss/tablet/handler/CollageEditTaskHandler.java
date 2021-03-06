package com.kodak.rss.tablet.handler;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.collage.CollageLayer;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.activities.CollageEditActivity;

public class CollageEditTaskHandler extends Handler{
	private static final String TAG = "CollageEditTaskHandler";
	public static final int STATUS_START = 1;
	public static final int STATUS_FINISH = 2;
	
	private WeakReference<CollageEditActivity> activityRef;
	
	public CollageEditTaskHandler(CollageEditActivity activity){
		this.activityRef = new WeakReference<CollageEditActivity>(activity);
	}
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		CollageEditActivity activity = activityRef.get();
		
		if(activity == null || activity.isFinishing()){
			return;
		}
		
		if(msg.obj != null){
			MsgData data = (MsgData) msg.obj;
			
			if (data.status == STATUS_START) {
				if (data.layer != null) {
					activity.collageEditLayer.showLayerEditProgress(data.page, data.layer);
				} else {
					activity.collageEditLayer.showPageEditProgress(data.page);
				}
			} else if (data.status == STATUS_FINISH) {
				activity.collageEditLayer.dismissEditProgress();
				activity.collageEditLayer.dismiss();
			
				if (!data.succeed) {
					showErrorWarning(activity, data);
				} else {
					activity.notifyCollagePageChanged();
				}
			}
			
		}
		
	}
	
	public static class MsgData{
		int taskId;
		int status;
		boolean succeed;
		CollagePage page;
		CollageLayer layer;
		Object[] params;
		
		public MsgData(int taskId,int status,boolean succeed,CollagePage page, CollageLayer layer,Object... params){
			this.taskId = taskId;
			this.status = status;
			this.succeed = succeed;
			this.page = page;
			this.layer = layer;
			this.params = params;
		}
		
		public MsgData(int taskId,int status,boolean succeed,CollagePage page,CollageLayer layer){
			this(taskId, status, succeed, page,layer,new Object[]{});
		}
		
	}
	
	private void showErrorWarning(CollageEditActivity activity, MsgData data){
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
