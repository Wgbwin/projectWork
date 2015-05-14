package com.kodak.rss.tablet.thread;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.project.Project;
import com.kodak.rss.core.n2r.bean.project.Resource;
import com.kodak.rss.core.n2r.bean.text.Font;
import com.kodak.rss.core.n2r.webservice.CalendarWebService;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.activities.MyProjectsActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class LoadProjectTask extends AsyncTask<String, Void, Object>{
	
	private static final String TAG = "LoadProjectTask:";
	private Context mContext;		
	private InfoDialog waitingDialog;
	private Project project;
	RssTabletApp app;
	
	public LoadProjectTask(Context context,Project project) {
		this.mContext = context;
		this.project = project;	
		app = RssTabletApp.getInstance();
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Load)
		.setProgressBar(true)				
		.create();
		waitingDialog.show();				
	}
		
	@Override
	protected Object doInBackground(String... params) {	
		if (project.type == null) return null;
		String lowType = project.type.toLowerCase();
		if ("".equals(lowType)) return null;
				
		String projectId = project.id;
		Resource resource = null;
		try {
			if (lowType.contains("book")){
				PhotobookWebService pbService = new PhotobookWebService(mContext);		
				resource = pbService.loadProjectTask(projectId);
				if (resource == null) return null;						
				Photobook book = pbService.getPhotobookTask(resource.id);
				if (book != null) {
					String theme = book.proDescId;
					String currentCountrycode = RssTabletApp.getInstance().getCountrycodeCurrentUsed();
					pbService.getThemesTask(theme, currentCountrycode);
						
					if(RssTabletApp.getInstance().fonts == null){				    	
				    	List<Font> fonts = pbService.getAvailableFontsTask(RssTabletApp.getInstance().getCurrentLanguage());
				    	RssTabletApp.getInstance().fonts = fonts;
				    }
				}
				return book;
			}else if (lowType.contains("calendar")){
				CalendarWebService pbService = new CalendarWebService(mContext);		
				resource = pbService.loadProjectTask(projectId);
				if (resource == null) return null;										
				Calendar calendar = pbService.getCalendarTask(resource.id);
				return calendar;
			} 	
		
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
			if (result != null) {
				if (result instanceof Photobook || result instanceof Calendar) {
					Log.i(TAG, "succeed.");	
					if (mContext instanceof MyProjectsActivity) {
						((MyProjectsActivity)mContext).viewpager.setAdapter(null);
					}
					Intent mIntent = null;
					if(result instanceof Photobook){			
						PhotoBookProductUtil.addCurrentPhotoBook((Photobook) result);							
						PhotoBookProductUtil.dealWithItem(mContext,0);											
						PictureUploadService.flowType = AppConstants.bookType;
						mIntent = new Intent(mContext, PhotoBooksProductActivity.class);				
						
					}else if (result instanceof Calendar) {
						CalendarUtil.addCurrentCalendar((Calendar) result);																								
						PictureUploadService.flowType = AppConstants.calendarType;
						mIntent = new Intent(mContext, CalendarEditActivity.class);										
					}	
					mIntent.putExtra(AppConstants.isFromMyProject, true);
					mIntent.putExtra(AppConstants.projectName, project.projectName);
					mContext.startActivity(mIntent);							
					((Activity)mContext).finish();	
				}else if(result instanceof RssWebServiceException){
					if(mContext instanceof BaseNetActivity){
						((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
					}
				}
			}								
		}		
	}

}
