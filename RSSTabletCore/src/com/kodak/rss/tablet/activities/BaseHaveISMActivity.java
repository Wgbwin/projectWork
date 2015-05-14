package com.kodak.rss.tablet.activities;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.util.ImageResources;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.CanZoomBaseAdapter;
import com.kodak.rss.tablet.adapter.SourcesAdapter;
import com.kodak.rss.tablet.facebook.AdpaterConstant;
import com.kodak.rss.tablet.facebook.FacebookAuthorize;
import com.kodak.rss.tablet.handler.GetFacebookGraphicsHandler;
import com.kodak.rss.tablet.handler.GetNativeGraphicsHandler;
import com.kodak.rss.tablet.thread.FindNativeAlbumsTask;
import com.kodak.rss.tablet.thread.LoadColorEffectSourcesTask;
import com.kodak.rss.tablet.util.GridViewParamSetUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class BaseHaveISMActivity extends BaseNetActivity implements OnClickListener{
	private Thread findAlbums;
	private LoadColorEffectSourcesTask loadColorEffectSourcesTask; 
	public ImageResources colorEffectResources; 
	
	public int adapterTpyeFlag;	
	public SortableHashMap<String, String> sourcesBucket;	
	public volatile SortableHashMap<String, SortableHashMap<Integer, String[]>> collection = new SortableHashMap<String, SortableHashMap<Integer, String[]>>();
	public SourcesAdapter sourcesAdapter;
		
	public RelativeLayout panelContent;
	public GridView photoGridView;
	public Button backButton;
	public TextView backButtonName;
	public ProgressBar progressBar;	
	public TextView sourceNameButton;
	public ProgressBar panelContentPBar;
	
	public Button allDeleteButton;
	public Button allSelectButton;

	public GridViewParamSetUtil gridViewParamUtil;
		
	public RssTabletApp app;	
	public boolean isOutMaxSelectNum;
	public int maxselectPhotoSize;
	
	public GetFacebookGraphicsHandler facebookGraphicsHandler;
	public GetNativeGraphicsHandler nativeGraphicsHandler;
	
	public String flowType;		
	public boolean selectFacebook = false;
	public boolean selectNative = false;
	
	public LruCache<String, Bitmap> mMemoryCache; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = RssTabletApp.getInstance();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				
		int ratio = 16; 
		if (this instanceof PhotoBookPicSelectMoreActivity) {
			ratio = ratio/2;
		} 
		mMemoryCache = MemoryCacheUtil.generMemoryCache(ratio);

	}
	
	public void initView() {		
		photoGridView = (GridView) findViewById(R.id.photoGrid);			
		gridViewParamUtil = new GridViewParamSetUtil(BaseHaveISMActivity.this, photoGridView);
		panelContent = (RelativeLayout) findViewById(R.id.panelContent);
		backButton = (Button) findViewById(R.id.back_button);
		backButtonName =  (TextView) findViewById(R.id.back_button_name);
		backButton.setOnClickListener(this);
		backButtonName.setOnClickListener(this);
		
		allSelectButton = (Button) findViewById(R.id.all_select_button);
		if (allSelectButton != null) {
			allSelectButton.setOnClickListener(this);
		}	    
	    allDeleteButton = (Button) findViewById(R.id.all_delete_button);
	    if (allDeleteButton != null) {
	    	allDeleteButton.setOnClickListener(this);
		}	 
	    		
		sourceNameButton = (TextView) findViewById(R.id.source_name);
		if (screenWidth > 0) {
			sourceNameButton.setWidth(screenWidth/3);
		}
		sourceNameButton.setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);						
		facebookGraphicsHandler = new GetFacebookGraphicsHandler(BaseHaveISMActivity.this, gridViewParamUtil);
		nativeGraphicsHandler = new GetNativeGraphicsHandler(BaseHaveISMActivity.this, gridViewParamUtil);
		panelContentPBar= (ProgressBar) findViewById(R.id.content_pbar);	
		fbkWrapper.handler = facebookGraphicsHandler;	
		fbkAuth = new FacebookAuthorize(this,fbkWrapper);
		
		if (findAlbums == null) {
			findAlbums = new FindNativeAlbumsTask(nativeGraphicsHandler, BaseHaveISMActivity.this, collection);
		}
		
		colorEffectResources = new ImageResources();
		if (loadColorEffectSourcesTask == null) {
			loadColorEffectSourcesTask = new LoadColorEffectSourcesTask(RssTabletApp.getInstance().getColorEffectList());
		}
	}
	
	public void initData() {     
		sourcesBucket = new SortableHashMap<String, String>();
		String imageDisplayName, imageUrl;
		for (int i = 0; i < 2; i++) {
			if (i == 0) {
				imageDisplayName = getString(R.string.facebook);
				imageUrl = AppConstants.FB_SOURCE;
			} else {
				imageDisplayName = getString(R.string.photos);
				imageUrl = AppConstants.NATIVE_SOURCE;
			}
			sourcesBucket.put(imageUrl, imageDisplayName);
		}
		sourcesAdapter = new SourcesAdapter(BaseHaveISMActivity.this, sourcesBucket,mMemoryCache);
		sourcesAdapter.notifyDataSetChanged();
		
		photoGridView.setOnScrollListener(new OnScrollListener() {	
			int start_index,end_index;
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {					
				CanZoomBaseAdapter adapter = (CanZoomBaseAdapter) photoGridView.getAdapter();	
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					 gridViewParamUtil.highlightPosition = start_index;						
				     adapter.loadContentRange(start_index, end_index);	
				}else {
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
				if (adapterTpyeFlag >= AdpaterConstant.SOURCES_ADAPTER_TPYE && adapterTpyeFlag <= AdpaterConstant.IMAGE_ADAPTER_TPYE ) {
					nativeGraphicsHandler.onItemClickOnNative(view,position);
				}else if (adapterTpyeFlag >= AdpaterConstant.FBK_PHOTOS_ADAPTER_TPYE && adapterTpyeFlag <= AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE) {
					facebookGraphicsHandler.onItemClickOnFacebook(view,position);
				}
			}
		});	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (findAlbums != null && !findAlbums.isAlive()&& collection.size() == 0) {			
			findAlbums.start();
		} else {
			nativeGraphicsHandler.sendEmptyMessage(0);
		}
		
		if (loadColorEffectSourcesTask != null && !loadColorEffectSourcesTask.isAlive()) {
			loadColorEffectSourcesTask.start();
		}
	}
	
	@Override
	protected void onPause() {
		if (findAlbums != null) {
			findAlbums.interrupt();
			findAlbums = null;
		}		
		if (loadColorEffectSourcesTask != null) {
			loadColorEffectSourcesTask.interrupt();
			loadColorEffectSourcesTask = null;
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
	
	public void clearDownDataRequest(){
		if (nativeGraphicsHandler != null) {
			if (nativeGraphicsHandler.imageAdapter != null) {
				nativeGraphicsHandler.imageAdapter.cancelRequest();				
			}
			if (nativeGraphicsHandler.photosAdapter != null) {
				nativeGraphicsHandler.photosAdapter.cancelRequest();				
			}
		}
		if (facebookGraphicsHandler != null) {
			if (facebookGraphicsHandler.fbkImageAdapter != null) {
				facebookGraphicsHandler.fbkImageAdapter.cancelRequest();				
			}
			if (facebookGraphicsHandler.fbkAlbumsAdapter != null) {
				facebookGraphicsHandler.fbkAlbumsAdapter.cancelRequest();				
			}
			if (facebookGraphicsHandler.fbkFriendsAdapter != null) {
				facebookGraphicsHandler.fbkFriendsAdapter.cancelRequest();				
			}
			if (facebookGraphicsHandler.fbkGroupsAdapter != null) {
				facebookGraphicsHandler.fbkGroupsAdapter.cancelRequest();				
			}
		}	
		System.gc();		
	}
					
	public int getPositionInList(ArrayList<ImageInfo> photobookschosenpics,String id){
		int position =-1;
		for (int j = 0; j < photobookschosenpics.size(); j++) {
			if (photobookschosenpics.get(j).id.equals(id)) {
				position = j;
				break;
			}
		}				
		return position;
	}		

	public void popUpPrompt(int messageId){		
		new InfoDialog.Builder(BaseHaveISMActivity.this).setMessage(messageId)						
		.setNegativeButton(R.string.d_ok, null).create()
		.show();		
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId()==R.id.back_button || v.getId()==R.id.back_button_name) {					
			if (adapterTpyeFlag >= AdpaterConstant.SOURCES_ADAPTER_TPYE && adapterTpyeFlag <= AdpaterConstant.IMAGE_ADAPTER_TPYE ) {
				nativeGraphicsHandler.onClickBackOnNative();
			}else if (adapterTpyeFlag >= AdpaterConstant.FBK_PHOTOS_ADAPTER_TPYE && adapterTpyeFlag <= AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE) {
				facebookGraphicsHandler.onClickBackOnFacebook();
			}
		}else if (v.getId()==R.id.all_select_button){
			if (adapterTpyeFlag >= AdpaterConstant.SOURCES_ADAPTER_TPYE && adapterTpyeFlag <= AdpaterConstant.IMAGE_ADAPTER_TPYE ) {
				nativeGraphicsHandler.onClickGetAll();		
			}else if (adapterTpyeFlag >= AdpaterConstant.FBK_PHOTOS_ADAPTER_TPYE && adapterTpyeFlag <= AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE) {
				facebookGraphicsHandler.onClickGetAll();
			}
		}else if (v.getId()==R.id.all_delete_button){
			if (adapterTpyeFlag >= AdpaterConstant.SOURCES_ADAPTER_TPYE && adapterTpyeFlag <= AdpaterConstant.IMAGE_ADAPTER_TPYE ) {
				nativeGraphicsHandler.onClickDeleteAll();		
			}else if (adapterTpyeFlag >= AdpaterConstant.FBK_PHOTOS_ADAPTER_TPYE && adapterTpyeFlag <= AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE) {
				facebookGraphicsHandler.onClickDeleteAll();
			}
		}
	}
			
}
