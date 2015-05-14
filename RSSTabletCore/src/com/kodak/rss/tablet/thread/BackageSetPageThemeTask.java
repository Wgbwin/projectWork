package com.kodak.rss.tablet.thread;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class BackageSetPageThemeTask extends AsyncTask<String, Void, Object>{
	private Context mContext;		
	private InfoDialog waitingDialog;	
	private PhotobookPage[] mPages;
	private String mBackgroudId;
	private String  mThemeId;
	
	public BackageSetPageThemeTask(Context context,PhotobookPage[] pages, String backgroudId, String themeId) {
		this.mContext = context;		
		this.mPages = pages;
		this.mBackgroudId = backgroudId;
		this.mThemeId = themeId;		
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
		PhotobookPage[] result = new PhotobookPage[2];
		if (PhotoBookProductUtil.getPhotobookPageEditable(mPages[0])) {						
			try {
				result[0] = pbService.setPageBackgroundFromThemeTask2(mPages[0].id, mThemeId, mBackgroudId);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				return e;
			}					
		}
		if (PhotoBookProductUtil.getPhotobookPageEditable(mPages[1])) {							
			try {
				result[1] = pbService.setPageBackgroundFromThemeTask2(mPages[1].id, mThemeId, mBackgroudId);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				return e;
			}			
		}		
		return result;
	}

	@Override
	protected void onPostExecute(Object  result) {
		super.onPostExecute(result);
		if(mContext != null && !((Activity)mContext).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}			
		
			if(result instanceof RssWebServiceException){				
				((BaseNetActivity) mContext).showErrorWarning((RssWebServiceException) result);
			}else if(result instanceof PhotobookPage[]){
				PhotobookPage[] dealResult = (PhotobookPage[]) result;
				if(dealResult[0] != null){	
					PhotoBookProductUtil.updatePageInPhotobook(dealResult[0], true);				
				}
				if(dealResult[1] != null){				
					PhotoBookProductUtil.updatePageInPhotobook(dealResult[1], true);	
				}
				
				if (mContext instanceof PhotoBooksProductActivity) {
					((PhotoBooksProductActivity)mContext).notifyPhotoBookPagesChanged();
				}
			}
		}
	}	

}
