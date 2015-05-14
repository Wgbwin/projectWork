package com.kodak.rss.tablet.thread;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.project.Project;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.MyProjectsActivity;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class DeleteProjectTask extends AsyncTask<String, Void, Object>{
	
	private static final String TAG = "DeleteProjectTask:";
	private Context mContext;		
	private InfoDialog waitingDialog;
	private Project project;
		
	public DeleteProjectTask(Context context,Project project) {
		this.mContext = context;
		this.project = project;			
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
		String projectId = project.id;		
		try {
			pbService.deleteProjectTask(projectId);
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}         	
		return null;
	}

	@Override
	protected void onPostExecute(Object  result) {
		super.onPostExecute(result);
		if(mContext != null && !((Activity)mContext).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}			
		
			if(!(result instanceof RssWebServiceException)){	
				List <Project> projects = RssTabletApp.getInstance().projects;
				if (projects != null) {
					for (Project item : projects) {
						if (item.id.equals(project.id)) {
							projects.remove(item);
							break;
						}				
					}
				}
				if (mContext instanceof MyProjectsActivity) {
					((MyProjectsActivity)mContext).initActionAndData();
				}
				Log.i(TAG, "succeed.");								
			}else {
				if (mContext instanceof MyProjectsActivity) {
					((MyProjectsActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			}
		}
	}	

}
