package com.kodak.rss.tablet.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.JudgeImageFileTypeUtil;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.CanZoomBaseAdapter;
import com.kodak.rss.tablet.adapter.ImageAdapter;
import com.kodak.rss.tablet.adapter.PhotosAdapter;
import com.kodak.rss.tablet.db.ImageSelectionDatabase;
import com.kodak.rss.tablet.facebook.AdpaterConstant;
import com.kodak.rss.tablet.thread.FindNativeAlbumsTask;
import com.kodak.rss.tablet.util.GridViewParamSetUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.view.SearchButton;
import com.kodak.rss.tablet.view.SourcePanel;
import com.kodak.rss.tablet.view.dialog.DialogShowPic;
import com.kodak.rss.tablet.view.dialog.DialogShowPic.onDialogListener;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

/*
 * Purpose:Select Image for KioskConnent upload
 * Author:Bing Wang
 * Created Time:20131104;
 */
public class PicSelectKioskActivity extends BaseActivity implements OnClickListener{
		
	private volatile SortableHashMap<String, SortableHashMap<Integer, String[]>> collection = new SortableHashMap<String, SortableHashMap<Integer, String[]>>();
	
	private SortableHashMap<Integer, String[]> imageBuckets;
	private PhotosAdapter photosAdapter;
	private ImageAdapter imageAdapter;
	
	private int photosSize;	
	private int adapterTpyeFlag;	
	private String currentDisplayName;	
	
	private ImageView dispalyImage;
	private TextView photoNum;
	Bitmap picturestack;		
	
	private SearchButton searchButton;
	private Button allDeleteButton;
	private Button allSelectButton;
	private Button backButton;
	private TextView backButtonName;
	private SourcePanel panel;
	private GridView photoGridView;
	private TextView sourceFolderNameView;
	
	private DialogShowPic dialogShowPic;	
	private GridViewParamSetUtil gridViewParamUtil;		
		
	private ImageSelectionDatabase imageSelectDB;	
	private SortableHashMap<Integer, String> kioskchosenpics;
	
	Thread findAlbums = null;
	private int selectPhotosPostion;
	private String sourceHeadName = "/All Photos";
	public LruCache<String, Bitmap> mMemoryCache; 
	
	public Handler findAlbumsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			final int action = msg.what;
			switch (action) {
			case 0:				
				if ((imageAdapter != null && imageBuckets != null) && adapterTpyeFlag == AdpaterConstant.IMAGE_ADAPTER_TPYE) {			
					imageAdapter.notifyDataSetChanged();
					photoGridView.setAdapter(imageAdapter);				
					adapterTpyeFlag = AdpaterConstant.IMAGE_ADAPTER_TPYE;
					backButton.setVisibility(View.VISIBLE);
					backButtonName.setVisibility(View.VISIBLE);
					allSelectButton.setVisibility(View.VISIBLE);
					allDeleteButton.setVisibility(View.VISIBLE);
					gridViewParamUtil.initGridViewMargin(imageBuckets.size(),adapterTpyeFlag,imageAdapter);	
					findViewById(R.id.panelContent).setOnTouchListener(gridViewParamUtil.gridViewController);
					photoGridView.setOnTouchListener(gridViewParamUtil.gridViewController);	
					currentDisplayName = sourceHeadName+"/"+imageBuckets.valueAt(0)[1];	
					sourceFolderNameView.setText(currentDisplayName);
					imageAdapter.notifyDataSetChanged();
				}else {
					if (collection != null && collection.size() > 0) {
						photosAdapter = new PhotosAdapter(PicSelectKioskActivity.this,collection,mMemoryCache);
						photosSize = collection.size();
						adapterTpyeFlag = AdpaterConstant.PHOTOS_ADAPTER_TPYE;	
						backButton.setVisibility(View.GONE);
						backButtonName.setVisibility(View.GONE);
						allSelectButton.setVisibility(View.GONE);
						allDeleteButton.setVisibility(View.GONE);
						photoGridView.setAdapter(photosAdapter);
						gridViewParamUtil.initGridViewMargin(photosSize,adapterTpyeFlag,photosAdapter);						
						findViewById(R.id.panelContent).setOnTouchListener(gridViewParamUtil.gridViewController);
						photoGridView.setOnTouchListener(gridViewParamUtil.gridViewController);	
						sourceFolderNameView.setText(sourceHeadName);
					}else {						
						new InfoDialog.Builder(PicSelectKioskActivity.this).setMessage(R.string.photos_no_photos_find)						
						.setNegativeButton(R.string.d_ok, null).create()
						.show();		
					}			
				}													
				break;
			case 1:	
				if (collection != null && collection.size() > 0) {
					photosAdapter = new PhotosAdapter(PicSelectKioskActivity.this,collection,mMemoryCache);
					photosSize = collection.size();
					adapterTpyeFlag = AdpaterConstant.PHOTOS_ADAPTER_TPYE;	
					backButton.setVisibility(View.GONE);
					backButtonName.setVisibility(View.GONE);
					allSelectButton.setVisibility(View.GONE);
					allDeleteButton.setVisibility(View.GONE);
					photoGridView.setAdapter(photosAdapter);
					gridViewParamUtil.initGridViewMargin(photosSize,adapterTpyeFlag,photosAdapter);
					findViewById(R.id.panelContent).setOnTouchListener(gridViewParamUtil.gridViewController);
					photoGridView.setOnTouchListener(gridViewParamUtil.gridViewController);
					sourceFolderNameView.setText(sourceHeadName);
				}else {
					new InfoDialog.Builder(PicSelectKioskActivity.this).setMessage(R.string.photos_no_photos_find)						
					.setNegativeButton(R.string.d_ok, null).create()
					.show();	
				}
				break;			
			}
		}
	};
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pic_select_kiosk);
				
		mMemoryCache = MemoryCacheUtil.generMemoryCache(16);
		
		sourceHeadName = "/"+getString(R.string.native_all_photos);
		if (findAlbums == null) {
			findAlbums = new FindNativeAlbumsTask(findAlbumsHandler, PicSelectKioskActivity.this, collection);						
		}
		initView();
		
		int orgHeight = (int) (Math.floor(((19*screenWidth)/20 -48*dm.density)/6)*1.15+3*dm.density);
		if (panel.maxContentHeight >= orgHeight) {
			panel.setOpenContentHeight(orgHeight);		
		}else {
			panel.setOpenAndClose();
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (findAlbums != null && !findAlbums.isAlive()&& collection.size() == 0) {
			findAlbums.start();
		} else {
			findAlbumsHandler.sendEmptyMessage(0);
		}	
		//Get the latest result from database
		kioskchosenpics = imageSelectDB.getTaggedSetMap();		
		setPhotoNum();		
	}
	
	@Override
	protected void onPause() {
		if (findAlbums != null) {
			findAlbums.interrupt();
			findAlbums = null;
		}
		if (kioskchosenpics != null ) {
			imageSelectDB.batchInsertOrUpdateUriWIFI(kioskchosenpics);	
		}	
		MemoryCacheUtil.evictAll(mMemoryCache);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();		
		collection.clear();
		mMemoryCache = null;
	}		

	private void initView() {	
		imageSelectDB = new ImageSelectionDatabase(PicSelectKioskActivity.this);		
		int statusBarHeight = RssTabletApp.getInstance().statusBarHeight;
		panel = (SourcePanel) findViewById(R.id.bottomPanel);
		panel.setpanelContentHeight(350 +statusBarHeight);		
		dispalyImage = (ImageView) findViewById(R.id.dispalyImage);
		photoNum = (TextView) findViewById(R.id.photoNum);
		photoGridView = (GridView) findViewById(R.id.photoGrid);		
		gridViewParamUtil = new GridViewParamSetUtil(PicSelectKioskActivity.this, photoGridView);
		
		searchButton = (SearchButton) findViewById(R.id.search_button);
		searchButton.setSourcePanel(panel);
		
        findViewById(R.id.magnify_button).setOnClickListener(this);  
        backButton = (Button) findViewById(R.id.back_button);
		backButton.setOnClickListener(this);
		backButtonName =  (TextView) findViewById(R.id.back_button_name);		
		backButtonName.setOnClickListener(this);
		
        allSelectButton = (Button) findViewById(R.id.all_select_button);
        allSelectButton.setOnClickListener(this);
        allDeleteButton = (Button) findViewById(R.id.all_delete_button);
        allDeleteButton.setOnClickListener(this);
        
		sourceFolderNameView = (TextView) findViewById(R.id.source_name);		
		if (screenWidth > 0) {
			sourceFolderNameView.setWidth(screenWidth/3);
		}				
		sourceFolderNameView.setOnClickListener(this);
		findViewById(R.id.previous_button).setOnClickListener(this);
		findViewById(R.id.save_button).setOnClickListener(this);
		
        photoGridView.setOnScrollListener(new OnScrollListener() {			
			int start_index,end_index;
        	
        	@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {	
        		CanZoomBaseAdapter adapter = (CanZoomBaseAdapter) photoGridView.getAdapter();	
        		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE){	
					 gridViewParamUtil.highlightPosition = start_index;					
				     adapter.loadContentRange(start_index, end_index);				    
				}else  {
					 adapter.lock = true;
					 
				}	
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				start_index = firstVisibleItem;  
	            end_index = firstVisibleItem + visibleItemCount;  
	            if (end_index >= totalItemCount) {  
	                end_index = totalItemCount - 1;  
	            }  	
			}
		});
		
		photoGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {							
				switch (adapterTpyeFlag) {				
					case AdpaterConstant.PHOTOS_ADAPTER_TPYE:
						selectPhotosPostion = position;
						imageBuckets = collection.valueAt(position);
						imageAdapter = new ImageAdapter(PicSelectKioskActivity.this,imageBuckets,kioskchosenpics,null,mMemoryCache);
						imageAdapter.notifyDataSetChanged();
						photoGridView.setAdapter(imageAdapter);	
						adapterTpyeFlag = AdpaterConstant.IMAGE_ADAPTER_TPYE;
						backButton.setVisibility(View.VISIBLE);
						backButtonName.setVisibility(View.VISIBLE);
						allSelectButton.setVisibility(View.VISIBLE);
						allDeleteButton.setVisibility(View.VISIBLE);
						gridViewParamUtil.initGridViewMargin(imageBuckets.size(),adapterTpyeFlag,imageAdapter);							
						currentDisplayName = sourceHeadName+"/"+imageBuckets.valueAt(0)[1];					
						break;
					case AdpaterConstant.IMAGE_ADAPTER_TPYE:	
						int key = imageBuckets.keyAt(position);	
						if (kioskchosenpics.containsKey(key)) {	
							kioskchosenpics.remove(key);
							imageAdapter.chiceDeleteState(position);	
						}else {
							if (!(imageAdapter.dirtyList != null && imageAdapter.dirtyList.contains(key))) {
								kioskchosenpics.put(key, imageBuckets.valueAt(position)[0]);
								imageAdapter.chiceSelectState(position);	
							}		
						}					    								
						setPhotoNum();
						break;
				}
				sourceFolderNameView.setText(currentDisplayName);
			}
		});								
	}
				
	@Override
	public void startOver() {
		kioskchosenpics = null;
		clearDownDataRequest();
		new ImageSelectionDatabase(this).handleDeleteAllUrisWiFi();
		super.startOver();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v.getId()==R.id.previous_button){
			photoGridView.setAdapter(null);	
			clearDownDataRequest();
			this.finish();
			Intent intent = new Intent(PicSelectKioskActivity.this, WiFiSelectWorkflowActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);	
		}else if(v.getId()==R.id.save_button){
			if(kioskchosenpics==null || kioskchosenpics.size()==0){
				//if no pics are selected , show dialog
				InfoDialog dialogSelectPics = new InfoDialog.Builder(this)
												.setMessage(R.string.AlbumSelection_Error_SelectAnImage)
												.setPositiveButton(R.string.d_ok, null)
												.create();
				dialogSelectPics.show();
			}else{
				photoGridView.setAdapter(null);								
				
				//locallytics log
				HashMap<String,String> map = new HashMap<String, String>();
				String count;
				int size = kioskchosenpics!=null ? kioskchosenpics.size() : 0;
				if(size <= 25){
					count = "001 - 025";
				}else if(size<=50){
					count = "026 - 050";
				}else if(size<=75){
					count = "051 - 075";
				}else if(size<=100){
					count = "076 - 100";
				}else if(size<=200){
					count = "101 - 200";
				}else if(size<=300){
					count = "201 - 300";
				}else if(size<=400){
					count = "301 - 400";
				}else if(size<=500){
					count = "401 - 500";
				}else{
					count = "501+";
				}
				map.put(RSSTabletLocalytics.LOCALYTICS_KEY_WIFI_SELECT_IMAGES_SENT, count);
				RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_WIFI_SELECT_AND_SEND, map);
				clearDownDataRequest();
				if(WiFiSelectWorkflowActivity.hasBackCamera()){
					startActivity(new Intent(PicSelectKioskActivity.this, WiFiQRCodeScanActivity.class));	
				}else{
					startActivity(new Intent(PicSelectKioskActivity.this, WifiManualInputActivity.class));	
				}
			}
		}else if(v.getId()==R.id.magnify_button){
			if ((kioskchosenpics != null && kioskchosenpics.size() > 0)) {
				if(dialogShowPic == null){
					dialogShowPic = new DialogShowPic();
				}
				
				if(!dialogShowPic.isShowing()){
					dialogShowPic.setObjectMap(kioskchosenpics,mMemoryCache);
					dialogShowPic.createDialog(PicSelectKioskActivity.this, getString(R.string.TaggedSet), new onDialogListener() {						
						@Override
						public void onDone() {
							
						}
					});				
					
				}
			}		
		}else if(v.getId()==R.id.source_name){			
			panel.setOpenAndClose();
		}else if(v.getId()==R.id.back_button || v.getId()==R.id.back_button_name){
			switch (adapterTpyeFlag) {							
				case AdpaterConstant.IMAGE_ADAPTER_TPYE:
					photoGridView.setAdapter(photosAdapter);
					adapterTpyeFlag = AdpaterConstant.PHOTOS_ADAPTER_TPYE;
					backButton.setVisibility(View.GONE);
					backButtonName.setVisibility(View.GONE);
					allSelectButton.setVisibility(View.GONE);
					allDeleteButton.setVisibility(View.GONE);
					gridViewParamUtil.initGridViewMargin(photosSize,adapterTpyeFlag,photosAdapter);	
					currentDisplayName = sourceHeadName;
					photoGridView.setSelection(selectPhotosPostion);
					break;
			}
			sourceFolderNameView.setText(currentDisplayName);
		}else if(v.getId()==R.id.all_select_button && imageBuckets != null){
			if (kioskchosenpics == null ) return;
			int size = imageBuckets.size();
			for (int i = 0; i < size; i++) {
				int key = imageBuckets.keyAt(i);
				if (imageAdapter.dirtyList != null && imageAdapter.dirtyList.contains(key)) continue;
				if (!kioskchosenpics.containsKey(key)) {
					if (!(imageAdapter.goodList != null && imageAdapter.goodList.contains(key))) {
						String[] filePath = imageBuckets.valueAt(i);
						if (filePath != null && filePath.length > 0) {
							boolean isWebP =  JudgeImageFileTypeUtil.isFilter(filePath[0]);
							if (isWebP) {
								if (imageAdapter.dirtyList == null) {
									imageAdapter.dirtyList = new ArrayList<Integer>(2);
								}
								imageAdapter.dirtyList.add(key);
								continue;
							}else {
								if (imageAdapter.goodList == null) {
									imageAdapter.goodList = new ArrayList<Integer>(2);
								}
								imageAdapter.goodList.add(key);
							}					
						}						
					}
					kioskchosenpics.put(key, imageBuckets.valueAt(i)[0]);
					imageAdapter.chiceSelectState(i);	
				}	    		
			}
			setPhotoNum();
		}else if(v.getId()==R.id.all_delete_button && imageBuckets != null){
			if (kioskchosenpics != null && kioskchosenpics.size()> 0){
				int size = imageBuckets.size();
				for (int i = 0; i < size; i++) {				
					int key = imageBuckets.keyAt(i);
					if (kioskchosenpics.containsKey(key)) {	
						kioskchosenpics.remove(key);
						imageAdapter.chiceDeleteState(i);	
					}					    		
				}
			}			
			setPhotoNum();
		}
	}
	
	private void setPhotoNum(){
		if (kioskchosenpics == null ) kioskchosenpics = new SortableHashMap<Integer, String>();	
		int size = kioskchosenpics.size();
		photoNum.setText(String.valueOf(size));
		if (size > 0) {
			Bitmap bitmap = ImageUtil.getThumbnail(PicSelectKioskActivity.this.getContentResolver(), kioskchosenpics.keyAt(size-1));				
    		if(bitmap != null){
    			if (picturestack ==null || picturestack.isRecycled()) {
    				picturestack=BitmapFactory.decodeResource(PicSelectKioskActivity.this.getResources(),R.drawable.picturestack);
				}
    			dispalyImage.setImageBitmap(ImageUtil.overlay(bitmap, picturestack));
    			bitmap.isRecycled();
    		} 
		}else {
			dispalyImage.setImageBitmap(picturestack);
		}				
	}

	public void clearDownDataRequest(){		
		if (imageAdapter != null) {
			imageAdapter.cancelRequest();				
		}
		if (photosAdapter != null) {
			photosAdapter.cancelRequest();				
		}
		System.gc();		
	}
	
}
