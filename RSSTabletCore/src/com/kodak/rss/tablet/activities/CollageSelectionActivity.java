package com.kodak.rss.tablet.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.View;

import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.CalendarProductSelectionAdapter;
import com.kodak.rss.tablet.adapter.CalendarProductSelectionAdapter.SelectCalendarListener;
import com.kodak.rss.tablet.thread.collage.CreateCollageTask;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.view.CalendarGridView;

public class CollageSelectionActivity extends BaseNetActivity {
	
	private CalendarGridView gvCPView;
	private List<RssEntry> collageList = new ArrayList<RssEntry>();
	private RssTabletApp app;
	public CalendarProductSelectionAdapter adapter;
	private CreateCollageTask creatCollageTask;
		
	public LruCache<String, Bitmap> mMemoryCache; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_collage);		
		mMemoryCache = MemoryCacheUtil.generMemoryCache(16);
		initData();
		initViews();				
	}
	
	private void initData(){
		app = RssTabletApp.getInstance();
		List<Catalog> catalogs = app.getCatalogList();
		if(catalogs != null){
			for(Catalog catalog : catalogs){
				List<RssEntry> tempEntry = catalog.getProducts(ProductInfo.PRO_TYPE_COLLAGES);
				collageList.addAll(tempEntry);
			}				
		}
	}
	
	private void initViews(){									
		gvCPView = (CalendarGridView) findViewById(R.id.gv_calendar);
		gvCPView.setNumColumns(3);		
		gvCPView.setHorizontalSpacing((int)dm.density*15);
		gvCPView.setVerticalSpacing((int)dm.density*15);
		int height = 0;			
		if (collageList.size() <= 3) {			
			height = (int) (dm.heightPixels - dm.density*90);	
		}else {			
			height = (int) ((dm.heightPixels - dm.density*90)/1.3f);	
		}
		
		findViewById(R.id.previous_button).setOnClickListener(this);	
		adapter = new CalendarProductSelectionAdapter(CollageSelectionActivity.this, height,collageList,mMemoryCache);	
		adapter.setSelectCalendarListener(new SelectCalendarListener() {		
			@Override
			public void onselectCalendar(int position) {				
				if(creatCollageTask == null || creatCollageTask.getStatus().equals(AsyncTask.Status.FINISHED)){
					RssEntry entry = collageList.get(position);
					creatCollageTask = new CreateCollageTask(CollageSelectionActivity.this,entry.proDescription.id);
					creatCollageTask.execute();
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

}
