package com.kodak.rss.tablet.thread.calendar;

import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

import android.os.AsyncTask;

public class CheckTextUpdateStatusTask extends AsyncTask<Void, Void, Object> {
	
	private CalendarEditActivity activity;
	private InfoDialog waitingDialog;
	
	public CheckTextUpdateStatusTask(CalendarEditActivity activity){
		this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		boolean isEnd = true;
		if (activity.dateTaskGroup != null) {
			isEnd = activity.dateTaskGroup.isAllTasksFinished();
		}
		
		if (!isEnd) {
			waitingDialog = new InfoDialog.Builder(activity).setMessage(R.string.Common_Wait)
			.setProgressBar(true).create();
			waitingDialog.show();
		}
	}

	@Override
	protected Object doInBackground(Void... params) {
		boolean isEnd = true;
		if (activity.dateTaskGroup != null) {
			isEnd = activity.dateTaskGroup.isAllTasksFinished();
		}	
		while(!isEnd) {
			if (activity.dateTaskGroup != null) {
				isEnd = activity.dateTaskGroup.isAllTasksFinished();
				if(isEnd){
					break;
				}
			}
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {}
		}			
		return true;
	}

	@Override
	protected void onPostExecute(Object result) {
		if(activity!= null && !activity.isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}
		}
	}
	
	

}
