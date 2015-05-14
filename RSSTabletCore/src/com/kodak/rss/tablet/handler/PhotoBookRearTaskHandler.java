package com.kodak.rss.tablet.handler;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;

/**
 * @author Bing
 */
public class PhotoBookRearTaskHandler extends Handler{
	private static final String TAG = "PhotoBookRearTaskHandler";
	public static final int STATUS_START = 1;
	public static final int STATUS_FINISH = 2;
	public static final int STATUS_EXCHANGE = 3;
	
	private WeakReference<PhotoBooksProductActivity> activityRef;
	
	public PhotoBookRearTaskHandler(PhotoBooksProductActivity activity){
		this.activityRef = new WeakReference<PhotoBooksProductActivity>(activity);
	}
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		PhotoBooksProductActivity activity = activityRef.get();		
		if(activity == null)return;
		
		if(msg.obj != null){
			MsgRearData data = (MsgRearData) msg.obj;					
			if(data.status == STATUS_START){
				activity.dialogPleaseWait.show();
			}else if(data.status == STATUS_FINISH){
				activity.dialogPleaseWait.dismiss();				
				if(data.succeed){
					activity.notifyPhotoBookPagesChanged();
					activity.notifyDragRelativeLayoutView();					
				}else{
					showErrorWarning(activity, data);
				}
			}else if(data.status == STATUS_EXCHANGE){			
				activity.rearrangeSimplexAdapter.notifyDataSetChanged();			
			}			
		}

	}
	
	public static class MsgRearData{		
		int status;
		boolean succeed;		
		Object[] params;
		
		public MsgRearData(int status,boolean succeed,Object... params){			
			this.status = status;
			this.succeed = succeed;
			
			this.params = params;
		}
	}
	
	private void showErrorWarning(PhotoBooksProductActivity activity, MsgRearData data){
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
