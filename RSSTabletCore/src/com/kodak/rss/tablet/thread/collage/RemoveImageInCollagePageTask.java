package com.kodak.rss.tablet.thread.collage;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.webservice.CollageWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CollageEditActivity;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class RemoveImageInCollagePageTask extends AsyncTask<Void, Void, Object> {
	
	private static final String TAG = "RemoveImageInPageTask";
	
	private Context mContext;
	private InfoDialog waitingDialog;
	private String pageId;
	private String contentId;
	
	public RemoveImageInCollagePageTask(Context context, String pageId,String contentId){
		this.mContext = context;
		this.pageId = pageId;
		this.contentId = contentId;
	}

	@Override
	protected void onPreExecute() {
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true).create();
		waitingDialog.show();
	}
	
	@Override
	protected Object doInBackground(Void... params) {
		CollageWebService service = new CollageWebService(mContext);		
		try {			
			CollagePage newPage = service.removeCollageContentTask(pageId, contentId,true); 
			if (newPage != null) {
				CollageUtil.deleteImageInfo(Layer.TYPE_IMAGE,contentId);	
				CollageUtil.updatePageInCollage(newPage, true, true);	
				return true;
			} else {
				return false;
			}
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if(mContext!= null && !((Activity)mContext).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}
			if(result instanceof RssWebServiceException){			
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			}else if(result instanceof Boolean){
				if((Boolean)result){			
					Log.i(TAG, "succeed.");
					if (mContext instanceof CollageEditActivity) {
						((CollageEditActivity)mContext).notifyCollagePageChanged();
					}
				}else {
					Log.i(TAG, "failed.");						
				}
			}
		}
	}

}
