package com.kodak.rss.tablet.thread;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class SetPhotobookTitleTask extends AsyncTask<Photobook, Void, Object> {
	
	private String author;
	private String title;
	private String subtitle;
	private String projectName;
	private Photobook photobook;
	private Context context;
	
	private InfoDialog waitingDialog;
	private PhotobookWebService webService;
	
	public SetPhotobookTitleTask(Context context, String author, String title, String subtitle,String projectName) {
		this.context = context;
		this.author = author;
		this.title = title;
		this.subtitle = subtitle;
		this.projectName = projectName;
		this.webService = new PhotobookWebService(context);
	}
	
	@Override
	protected void onPreExecute() {
		waitingDialog = new InfoDialog.Builder(context).setMessage(R.string.Common_Wait)
			.setProgressBar(true)				
			.create();
		waitingDialog.show();
	}

	@Override
	protected Object doInBackground(Photobook... photobook) {
		this.photobook = photobook[0];
		PhotobookPage page = null;
		try {
			page = webService.setAuthorTitleSubtitleTask(this.photobook.id, author, title, subtitle);
			return page;
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
	}
	
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if(context != null && !((Activity)context).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}
		
			PhotoBooksProductActivity activity = (PhotoBooksProductActivity) context;
			activity.canDownloadTitlePage = true;
			
			if(result instanceof RssWebServiceException){
				activity.showErrorWarning((RssWebServiceException) result);
			}else if(result!=null){
				PhotoBookProductUtil.updatePageInPhotobook((PhotobookPage) result, true);	
	
				activity.notifyPhotoBookPagesChanged();
				
				//show projectName
				String str = "";
				if (" ".equals(projectName)) {
					str = "";
				}else if(!"".equals(projectName) && projectName.trim().length() == 0){//is all space
					str = projectName;
				}else if(projectName != null && !"".equals(projectName.trim())){
					str = projectName;
				}else if(title !=null && !"".equals(title.trim())){
					str = title;
				}else{
					str = AppConstants.PROJECT_DEFAULT_NAME;
				}
				activity.showProjectName(str);
				
				photobook.projectName = str;
				photobook.author = author;
				photobook.title = title;
				photobook.subTitle = subtitle;
			} else {
				Log.e("setTitle", "failed...");
			}
		}
		
	}

}
