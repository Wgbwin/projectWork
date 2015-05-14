package com.kodak.rss.tablet.handler;

import java.lang.ref.WeakReference;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.activities.GCEditActivity;
import com.kodak.rss.tablet.util.GreetingCardUtil;

import android.os.Handler;
import android.os.Message;

public class GCEditTaskHandler extends Handler{
	private static final String TAG = "GCEditTaskHandler";
	public static final int STATUS_START = 1;
	public static final int STATUS_FINISH = 2;
	
	private WeakReference<GCEditActivity> activityRef;
	
	public GCEditTaskHandler(GCEditActivity activity){
		this.activityRef = new WeakReference<GCEditActivity>(activity);
	}
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		GCEditActivity activity = activityRef.get();
		
		if(activity == null || activity.isFinishing()){
			return;
		}
		
		if(msg.obj != null){
			MsgData data = (MsgData) msg.obj;
			
			if(data.status == STATUS_START){
				activity.layerEdit.showLayerEditProgress(data.page,data.layer);
			}else if(data.status == STATUS_FINISH && !data.succeed){
				activity.layerEdit.dismissEditProgress();
				activity.layerEdit.dismiss();
				
				showErrorWarning(activity, data);
			}else if(data.status == STATUS_FINISH && data.succeed){
				activity.layerEdit.dismissEditProgress();
				activity.layerEdit.dismiss();
				activity.notifyGCPagesChanged(data.page.id);
				activity.notifyGreetingCardChanged();
				GreetingCardUtil.updatePageInCard(data.page, true);	
			}
			
		}
		
	}
	
	public static class MsgData{
		int taskId;
		int status;
		boolean succeed;
		GCPage page;
		Layer layer;
		Object[] params;
		
		public MsgData(int taskId,int status,boolean succeed, GCPage page, Layer layer,Object... params){
			this.taskId = taskId;
			this.status = status;
			this.succeed = succeed;
			this.page = page;
			this.layer = layer;
			this.params = params;
		}
		
		public MsgData(int taskId,int status,boolean succeed,GCPage page,Layer layer){
			this(taskId, status, succeed, page,layer,new Object[]{});
		}
		
	}
	
	private void showErrorWarning(GCEditActivity activity, MsgData data){
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
