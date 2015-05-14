package com.kodak.rss.tablet.thread;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class MovePageTask extends AsyncTask<String, Void, Object>{
	private String TAG = "MovePageTask :";
	private Context mContext;		
	private InfoDialog waitingDialog;	
	private String fromPageId;
	private String toPageId;
	private PhotoBooksProductActivity activity;
	
	public MovePageTask(Context context,String fromPageId , String toPageId) {
		this.mContext = context;	
		this.activity = (PhotoBooksProductActivity) context;
		this.fromPageId = fromPageId;
		this.toPageId = toPageId;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true)				
		.create();
		waitingDialog.show();				
	}
	
	@Override
	protected Object doInBackground(String... params) {				
		PhotobookWebService pbService = new PhotobookWebService(mContext);
		Photobook photobook = null;	
		Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();	
		try {
			photobook = (Photobook) pbService.movePage2Task(currentPhotoBook.id, fromPageId, toPageId);
			return photobook;
		} catch (RssWebServiceException e) {
			e.printStackTrace();				
			return e;
		}		
	}

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if(mContext != null && !((Activity)mContext).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}			
		
			if(result instanceof RssWebServiceException){
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity) mContext).showErrorWarning((RssWebServiceException) result);
				}
				if (activity.pagesAdapter != null) activity.pagesAdapter.refresh();						
			}else if(result != null){			
				Log.i(TAG, "succeed.");				
				PhotoBookProductUtil.setCurrentPhotoBook((Photobook) result);								
				if (mContext instanceof PhotoBooksProductActivity) {
					((PhotoBooksProductActivity)mContext).notifyPhotoBookChanged();
//					((PhotoBooksProductActivity)mContext).notifyPhotoBookPagesChanged();
				}
			}
		}
	}	

}
