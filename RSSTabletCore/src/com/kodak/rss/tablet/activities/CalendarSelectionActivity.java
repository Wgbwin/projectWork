package com.kodak.rss.tablet.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.View;

import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.CalendarTheme;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.core.n2r.webservice.CalendarWebService;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.CalendarProductSelectionAdapter;
import com.kodak.rss.tablet.adapter.CalendarProductSelectionAdapter.SelectCalendarListener;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.view.CalendarGridView;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class CalendarSelectionActivity extends BaseNetActivity {
	private final String TAG = CalendarSelectionActivity.class.getSimpleName();
	
	private CalendarGridView gvCPView;
	private List<RssEntry> calendarsList = new ArrayList<RssEntry>();
	private RssTabletApp app;
	private CalendarProductSelectionAdapter adapter;
	private GetCalendarThemesTask getCalendarThemesTask;
	private InfoDialog waitingDialog;
	private CalendarWebService mService;	
	
	public LruCache<String, Bitmap> mMemoryCache; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_calendar);		
		mMemoryCache = MemoryCacheUtil.generMemoryCache(16);
		initData();
		initViews();				
	}
	
	private void initData(){
		app = RssTabletApp.getInstance();
		List<Catalog> catalogs = app.getCatalogList();
		if(catalogs != null){
			for(Catalog catalog : catalogs){
				List<RssEntry> tempEntry = catalog.getProducts(ProductInfo.PRO_TYPE_DUPLEXCALENDAR);
				calendarsList.addAll(tempEntry);
			}	
			for(Catalog catalog : catalogs){
				List<RssEntry> tempEntry = catalog.getProducts(ProductInfo.PRO_TYPE_SIMPLEXCALENDAR);
				calendarsList.addAll(tempEntry);
			}	
			for(Catalog catalog : catalogs){
				List<RssEntry> tempEntry = catalog.getProducts(ProductInfo.PRO_TYPE_ANNUALCALENDAR);
				calendarsList.addAll(tempEntry);
			}	
		}
	}
	
	private void initViews(){									
		gvCPView = (CalendarGridView) findViewById(R.id.gv_calendar);
		gvCPView.setNumColumns(3);		
		gvCPView.setHorizontalSpacing((int)dm.density*15);
		gvCPView.setVerticalSpacing((int)dm.density*15);
		int height = 0;			
		if (calendarsList.size() <= 3) {			
			height = (int) (dm.heightPixels - dm.density*90);	
		}else {			
			height = (int) ((dm.heightPixels - dm.density*90)/1.3f);	
		}
		
		findViewById(R.id.previous_button).setOnClickListener(this);	
		adapter = new CalendarProductSelectionAdapter(CalendarSelectionActivity.this, height,calendarsList,mMemoryCache);	
		adapter.setSelectCalendarListener(new SelectCalendarListener() {		
			@Override
			public void onselectCalendar(int position) {				
				if(getCalendarThemesTask == null || getCalendarThemesTask.getStatus().equals(AsyncTask.Status.FINISHED)){
					RssEntry entry = calendarsList.get(position);
					getCalendarThemesTask = new GetCalendarThemesTask(CalendarSelectionActivity.this,entry.proDescription.id);
					getCalendarThemesTask.execute();
				}
			}
		});			
	}

	@Override
	protected void onResume() {
		super.onResume();		
		gvCPView.setAdapter(adapter);	
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		gvCPView.setAdapter(null);	
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
			gvCPView.setAdapter(null);
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
		gvCPView.setAdapter(null);
		if (adapter != null) {
			adapter.cancelRequest();
		}
		super.startOver();
	}
	
	@Override
	public void onClick(View v) {		
		super.onClick(v);
		if(v.getId()==R.id.previous_button){
			previousDoMoreOver();
		}
	}
	
	
	@Override
	public void previousDoMoreOver() {		
		gvCPView.setAdapter(null);
		if (adapter != null) {
			adapter.cancelRequest();
		}
		super.previousDoMoreOver();
	}
		
	private class GetCalendarThemesTask extends AsyncTask<RssEntry, Void, Object> {
		
		private Context mContext;
		private String  proDescriptionId;
		public GetCalendarThemesTask(Context context,String id){
			mContext = context;
			proDescriptionId = id;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.i(TAG, "Start get calendarThemes...");
			waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
			.setProgressBar(true)				
			.create();
			waitingDialog.show();
		}

		@Override
		protected Object doInBackground(RssEntry... params) {			
			if(mService==null){
				mService = new CalendarWebService(mContext);
			}
			List<CalendarTheme> calendarThemes = null;	
			String language = RssTabletApp.getInstance().getCurrentLanguage();		
			try {
				calendarThemes = mService.getCalendarsTask(proDescriptionId,language);
			} catch (com.kodak.rss.core.exception.RssWebServiceException e) {
				e.printStackTrace();
				return e;
			}				
			return calendarThemes;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if(mContext!= null && !((Activity)mContext).isFinishing()){
				if(waitingDialog!=null && waitingDialog.isShowing()){
					waitingDialog.dismiss();
				}
				if(result == null){
					Log.e(TAG, "Creating calendarThemes failed.");				
				}else if(result instanceof RssWebServiceException){
					Log.e(TAG, "Getting calendarThemes failed.");
					showErrorWarning((RssWebServiceException) result);
				} else {					
					Log.i(TAG, "Getting calendarThemes succeed.");	
					
					HashMap<String,String> map = new HashMap<String, String>();					
					map.put(RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_CALENDAR_TYPE, proDescriptionId);
					RSSLocalytics.recordLocalyticsEvents(mContext, RSSTabletLocalytics.LOCALYTICS_EVENT_CALENDAR_TYPE_SELECTED, map);											

					Intent mIntent = new Intent(mContext, CalendarThemeSelectionActivity.class);					
					mIntent.putExtra("calendarThemes", (Serializable)result);
					startActivity(mIntent);
					if (adapter != null) {
						adapter.cancelRequest();
					}
					CalendarSelectionActivity.this.finish();
					System.gc();
				}
			}
		}
		
	}

}
