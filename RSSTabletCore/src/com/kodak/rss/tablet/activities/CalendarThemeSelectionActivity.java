package com.kodak.rss.tablet.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.calendar.CalendarTheme;
import com.kodak.rss.core.n2r.webservice.CalendarWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.CalendarThemeSelectionAdapter;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.view.CalendarThemeItemView;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class CalendarThemeSelectionActivity extends BaseNetActivity implements OnClickListener{
	private final String TAG = CalendarThemeSelectionActivity.class.getSimpleName();
	
	private GridView gvCollageTheme;
	List<CalendarTheme> calendarThemes;	
	private CalendarThemeSelectionAdapter adapter;	
	private InfoDialog waitingDialog;
	private CalendarWebService mService;	
	
	public LruCache<String, Bitmap> mMemoryCache; 
	public CalendarTheme curSelectedTheme;	
	public CalendarThemeItemView preItemView;
	public boolean isExistShowPicDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_calendar_theme);		
		mMemoryCache = MemoryCacheUtil.generMemoryCache(16);
		initData();
		initViews();				
	}
	
	@SuppressWarnings("unchecked")
	private void initData(){		
		if (getIntent() != null) {			
			calendarThemes = (List<CalendarTheme>) getIntent().getSerializableExtra("calendarThemes");			
		}
		if (calendarThemes == null) {
			calendarThemes = new ArrayList<CalendarTheme>(2);
		}
	}
	
	private void initViews(){									
		gvCollageTheme = (GridView) findViewById(R.id.gv_calendar_theme);
		gvCollageTheme.setNumColumns(4);
		gvCollageTheme.setVerticalSpacing((int)dm.density*10);
		gvCollageTheme.setHorizontalSpacing((int)dm.density*10);
		
		int height = (int) ((dm.heightPixels - dm.density*90)/2.3f);	
		adapter = new CalendarThemeSelectionAdapter(CalendarThemeSelectionActivity.this,height,calendarThemes,mMemoryCache);
		
		findViewById(R.id.previous_button).setOnClickListener(this);
		findViewById(R.id.continue_button).setOnClickListener(this);		
	}

	@Override
	protected void onResume() {
		super.onResume();				
		gvCollageTheme.setAdapter(adapter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		gvCollageTheme.setAdapter(null);		
		MemoryCacheUtil.evictAll(mMemoryCache);	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMemoryCache = null;
	}		
	
	@Override
	public void judgeHaveItems(){
		if (judgeSelectHavedProductInfo()) {			
			gvCollageTheme.setAdapter(null);
			Intent mIntent = new Intent(this, ShoppingCartActivity.class);
			startActivity(mIntent);	
			this.finish();
			System.gc();
		}else {
			popNoItemDialog();
		}		
	}
	
	@Override
	public void startOver() {
		if (adapter != null) {
			adapter.cancelRequest();
		}
		gvCollageTheme.setAdapter(null);
		super.startOver();
	}
	
	@Override
	public void previousDoMoreOver() {	
		if (adapter != null) {
			adapter.cancelRequest();
		}
		gvCollageTheme.setAdapter(null);
		super.previousDoMoreOver();
	}
	
	@Override
	public void onClick(View v) {		
		super.onClick(v);
		if(v.getId()==R.id.continue_button){
			if (curSelectedTheme != null && curSelectedTheme.id != null && curSelectedTheme.productDescriptionId != null) {
				CreateCalendarTask task = new CreateCalendarTask(CalendarThemeSelectionActivity.this,curSelectedTheme.productDescriptionId,curSelectedTheme.id);
				task.execute();
			}else {
				String prompt = getResources().getString(R.string.want_theme_propmt);
				android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();			
					}		
				};		
				new InfoDialog.Builder(CalendarThemeSelectionActivity.this).setMessage(prompt)			
				.setNegativeButton(R.string.d_ok, yesOnClickListener)
				.create()
				.show();		
			}							
		}else if(v.getId()==R.id.previous_button){
			Intent mIntent = new Intent(CalendarThemeSelectionActivity.this, CalendarSelectionActivity.class);								
			startActivity(mIntent);	
			CalendarThemeSelectionActivity.this.finish();
		}
	}
	
		
	private class CreateCalendarTask extends AsyncTask<String, Void, Object> {		
		private Context mContext;
		private String productDescriptionId;
		private String contentId;
		public CreateCalendarTask(Context context,String productDescriptionId,String contentId){
			this.mContext = context;
			this.productDescriptionId = productDescriptionId;
			this.contentId = contentId;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.i(TAG, "Start creating calendar...");
			waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Task_CreatingCalendar)
			.setProgressBar(true)				
			.create();
			waitingDialog.show();
		}

		@Override
		protected Object doInBackground(String ... params) {			
			if(mService==null){
				mService = new CalendarWebService(mContext);
			}
			Calendar calendar = null;
			String language = RssTabletApp.getInstance().getCurrentLanguage();
			try {
				calendar = mService.createCalendarTask(productDescriptionId, contentId, language);
			} catch (com.kodak.rss.core.exception.RssWebServiceException e) {
				e.printStackTrace();
				return e;
			}				
			return calendar;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if(mContext!= null && !((Activity)mContext).isFinishing()){
				if(waitingDialog!=null && waitingDialog.isShowing()){
					waitingDialog.dismiss();
				}
				if(result == null){
					Log.e(TAG, "Creating calendar failed.");				
				}else if(result instanceof RssWebServiceException){
					Log.e(TAG, "Creating calendar failed.");
					showErrorWarning((RssWebServiceException) result);
				} else {
					Log.i(TAG, "Creating calendar succeed.");		
					CalendarUtil.addCurrentCalendar((Calendar)result);
					Intent mIntent = new Intent(mContext, CalendarEditActivity.class);										
					startActivity(mIntent);	
					if (adapter != null) {
						adapter.cancelRequest();
					}
					CalendarThemeSelectionActivity.this.finish();
				}
			}
		}

	}

}
