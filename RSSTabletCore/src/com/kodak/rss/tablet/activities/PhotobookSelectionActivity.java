package com.kodak.rss.tablet.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.util.DeviceInfoUtil;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.PhotobookSelectionAdapter;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.view.HorizontalListView;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class PhotobookSelectionActivity extends BaseNetActivity implements OnClickListener{
	private final String TAG = PhotobookSelectionActivity.class.getSimpleName();
	
	private HorizontalListView gvPhotobooks;
	private List<RssEntry> photobooks = new ArrayList<RssEntry>();
	private RssTabletApp app;
	private PhotobookSelectionAdapter adapter;
	private CreatePhotobookTask createPhotobook;
	private InfoDialog waitingDialog;
	private PhotobookWebService mService;	
	
	public LruCache<String, Bitmap> mMemoryCache; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_photo_books);		
		mMemoryCache = MemoryCacheUtil.generMemoryCache(16);
		initData();
		initViews();				
	}
	
	private void initData(){
		app = RssTabletApp.getInstance();
		List<Catalog> catalogs = app.getCatalogList();
		if(catalogs != null){
			for(Catalog catalog : catalogs){
				List<RssEntry> tempEntry = catalog.getProducts(ProductInfo.PRO_TYPE_PHOTOBOOK);
				photobooks.addAll(tempEntry);
			}			
		}
	}

	@SuppressWarnings("deprecation")
	private void initViews(){			
		float columns = (float) (photobooks.size()<=3?photobooks.size():3.3);
		int statusBarHeight = DeviceInfoUtil.getStatusHeight(PhotobookSelectionActivity.this);
		int columnWidth = (int) (dm.widthPixels / columns);	
		int columnHeight = (int) (dm.heightPixels - dm.density * 90  - statusBarHeight);		
		findViewById(R.id.previous_button).setOnClickListener(this);
		gvPhotobooks = (HorizontalListView) findViewById(R.id.gv_photobooks);
		gvPhotobooks.setHiddenScrollRect(columnWidth, 2.5, dm.density * 90 + columnHeight*0.1f/2);		
		adapter = new PhotobookSelectionAdapter(PhotobookSelectionActivity.this,columnWidth, columnHeight,photobooks,mMemoryCache);	
		
		gvPhotobooks.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				if(createPhotobook == null || createPhotobook.getStatus().equals(AsyncTask.Status.FINISHED)){
					RssEntry entry = photobooks.get(position);
					
					//Localytics
					HashMap<String,String> map = new HashMap<String, String>();
					map.put(RSSTabletLocalytics.LOCALYTICS_KEY_PHOTOBOOK_TYPE, entry.proDescription.id);
					RSSLocalytics.recordLocalyticsEvents(PhotobookSelectionActivity.this, RSSTabletLocalytics.LOCALYTICS_EVENT_PHOTOBOOK_TYPE_SELECTED, map);
					
					createPhotobook = new CreatePhotobookTask(PhotobookSelectionActivity.this);
					createPhotobook.execute(entry);
				}
			}
		});		
	}

	@Override
	protected void onResume() {
		super.onResume();		
		new Handler().postDelayed(new Runnable() {		
			@Override
			public void run() {			
				gvPhotobooks.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		}, 200);	
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		gvPhotobooks.setAdapter(null);	
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
			gvPhotobooks.setAdapter(null);
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
		gvPhotobooks.setAdapter(null);
		super.startOver();
	}
	
	@Override
	public void previousDoMoreOver() {		
		gvPhotobooks.setAdapter(null);
		super.previousDoMoreOver();
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v.getId() == R.id.previous_button){		
			if (app.isUseDoMore) {
				previousDoMoreOver();
			}else {
				android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startOver();				
					}		
				};		
				new InfoDialog.Builder(this).setMessage(R.string.privious_layout_content)
				.setPositiveButton(getText(R.string.d_no), null)
				.setNegativeButton(R.string.d_yes, yesOnClickListener).create()
				.show();			
			}	
		}
	}	
	
	private class CreatePhotobookTask extends AsyncTask<RssEntry, Void, Object> {
		
		private Context mContext;
		public CreatePhotobookTask(Context context){
			mContext = context;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.i(TAG, "Start creating photobook...");
			waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Task_CreatingProduct)
			.setProgressBar(true)				
			.create();
			waitingDialog.show();
		}

		@Override
		protected Object doInBackground(RssEntry... params) {
			RssEntry entry = params[0];
			if(mService==null){
				mService = new PhotobookWebService(mContext);
			}
			Photobook photobook = null;
			try {
				photobook = mService.createPhotobookTask(entry.proDescription.id);
			} catch (com.kodak.rss.core.exception.RssWebServiceException e) {
				e.printStackTrace();
				return e;
			}				
			return photobook;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if(waitingDialog!=null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}
			if(!((Activity)mContext).isFinishing()){
				if(result instanceof RssWebServiceException){
					Log.e(TAG, "Creating photobook failed.");
					showErrorWarning((RssWebServiceException) result);
				} else {
					
					Log.i(TAG, "Creating photobook succeed.");					
					PhotoBookProductUtil.addCurrentPhotoBook((Photobook) result);							
					PhotoBookProductUtil.dealWithItem(mContext,0);					
					PictureUploadService.flowType = AppConstants.bookType;					
					Intent mIntent = new Intent(mContext, PhotoBooksPicSelectActivity.class);
					startActivity(mIntent);	
					System.gc();
				}
			}
		}
		
	}

}
