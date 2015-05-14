package com.kodak.rss.tablet.thread.calendar;

import android.os.AsyncTask;

import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.activities.CollageEditActivity;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class SkipShoppingCartTask extends AsyncTask<Void, Void, Object> {
	
	private BaseNetActivity activity;
	private InfoDialog waitingDialog;
	private boolean[] params;//is Save Project Flag
		
	public SkipShoppingCartTask(BaseNetActivity activity,boolean ... params){
		this.activity = activity;	
		this.params = params;		
	}

	@Override
	protected void onPreExecute() {		
		
		if (!isEnd()) {
			waitingDialog = new InfoDialog.Builder(activity).setMessage(R.string.Common_Wait)
			.setProgressBar(true).create();
			waitingDialog.show();
		}
	}
	
	private boolean isEnd(){
		boolean isEnd = true;
		if (activity instanceof CalendarEditActivity) {
			isEnd = ((CalendarEditActivity)activity).addImagesView.endTasks();
			if (((CalendarEditActivity)activity).dateTaskGroup != null) {
				isEnd = isEnd && ((CalendarEditActivity)activity).dateTaskGroup.isAllTasksFinished();
			}
		}else if (activity instanceof CollageEditActivity) {
			isEnd = ((CollageEditActivity)activity).addImagesView.endTasks();			
		}
		return isEnd;
	}
	
	@Override
	protected Object doInBackground(Void... params) {		
		boolean isEnd = isEnd();		
		while(!isEnd) {
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {}			
			isEnd = isEnd();
		}
		
		if (activity instanceof CalendarEditActivity){
			if (((CalendarEditActivity)activity).dateTaskGroup != null) {
				((CalendarEditActivity)activity).dateTaskGroup.destroyThread();	
				((CalendarEditActivity)activity).dateTaskGroup = null;
			}		
		}
		return true;				
	}

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if(activity != null && !activity.isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}			
			if (activity instanceof CalendarEditActivity) {
				if (params != null && params.length > 0 && params[0]) {
					((CalendarEditActivity)activity).showSaveProject();
				}else {
					((CalendarEditActivity)activity).skipShoppingCart();
				}
			}else if (activity instanceof CollageEditActivity) {
				((CollageEditActivity)activity).skipShoppingCart();	
			}		
		}
	}
}
