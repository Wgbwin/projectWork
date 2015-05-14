package com.kodak.rss.tablet.thread;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.project.Project;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class SaveProjectTask extends AsyncTask<String, Void, Object>{
	
	private static final String TAG = "SaveProjectTask:";
	private Context mContext;		
	private InfoDialog waitingDialog;
	private String projectName;
	private String saveId;
	
	public SaveProjectTask(Context context,String name,String saveId) {
		this.mContext = context;
		this.projectName = name;
		this.saveId = saveId;
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
		WebService pbService = new WebService(mContext);
		Project project = null;						
		if (projectName == null || "".equals(projectName)) {
			projectName = "Kodak Moments HD";	
		}		
		try {
			project =  pbService.saveProjectTask(saveId, projectName);
			return project;
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}		
	}

	@Override
	protected void onPostExecute(Object  result) {
		super.onPostExecute(result);
		if(mContext != null && !((Activity)mContext).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}			
				
			if(result instanceof RssWebServiceException){
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			}else if(result != null){			
				Log.i(TAG, "succeed.");		
				try{
					if (mContext instanceof CalendarEditActivity) {
						CalendarEditActivity activity = (CalendarEditActivity) mContext;
						CalendarUtil.getCurrentCalendar().projectName = projectName;
						activity.showProjectName(projectName);
					}else if (mContext instanceof PhotoBooksProductActivity){
						PhotoBooksProductActivity activity = (PhotoBooksProductActivity) mContext;
						PhotoBookProductUtil.getCurrentPhotoBook().projectName = projectName;
						activity.showProjectName(projectName);
					}
				}catch(Exception e){
					Log.e(TAG, e);
				}
			}
		}
	}	

}
