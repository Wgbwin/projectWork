package com.kodak.rss.tablet.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.content.SearchStarter;
import com.kodak.rss.core.n2r.bean.content.SearchStarterCategory;
import com.kodak.rss.core.n2r.bean.greetingcard.GCCategory;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.GCSSCategoryAdapter;
import com.kodak.rss.tablet.adapter.GCSubButtonAdapter;
import com.kodak.rss.tablet.thread.GCLoadCategoryTask;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.view.HorizontalListView;

public class GCSSCategorySelectActivity extends BaseNetActivity implements OnClickListener{	

	RssTabletApp app;	
	
	private Button previousButton;
	private TextView propmtTxt;
	
	private RelativeLayout mainLayout;
	private RelativeLayout subLayout;
	
	private HorizontalListView mHListView;
	private GCSSCategoryAdapter mainAdapter;
	
	private HorizontalListView sBHListView;
	private GCSubButtonAdapter gCSBadapter;
	private int selectSBIndex;
	private HorizontalListView sHListView;	
	
	public LruCache<String, Bitmap> mMemoryCache; 
	
	private SearchStarterCategory mainCategory;
	private List <SearchStarterCategory> subCategorys;
	private List <GCSSCategoryAdapter> subAdapters;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);								
		setContentView(R.layout.activity_sscategory_select_card);			
		mMemoryCache = MemoryCacheUtil.generMemoryCache(16);
		initData();			
		initView();
	}
	
	private void initData(){
		mainCategory = null;
		subCategorys = null;
		app = RssTabletApp.getInstance();
		if (app.sSCategorys == null) return;
		int size = app.sSCategorys.size();
		if (size == 0) return;	
		subCategorys =  new ArrayList<SearchStarterCategory>(size -1);
		for (int i = 0; i < size; i++) {
			SearchStarterCategory sSC = app.sSCategorys.get(i);
			if (sSC== null) continue;
			if (i == 0) {
				mainCategory = sSC;
			}else {
				subCategorys.add(sSC);
			}
		}
	}
		
	private void initView(){						
		previousButton =(Button) findViewById(R.id.previous_button);
		previousButton.setOnClickListener(this);
		propmtTxt = (TextView) findViewById(R.id.select_propmt);
		
		mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
		subLayout = (RelativeLayout) findViewById(R.id.sub_layout);		
		
		mHListView = (HorizontalListView) findViewById(R.id.main_hlv);
		sHListView = (HorizontalListView) findViewById(R.id.sub_hlv);		
		sBHListView = (HorizontalListView) findViewById(R.id.sub_buttons_hlv);
		
		int mItemHeight = (int) ((dm.heightPixels*1.0 - dm.density*90)*2/3);			
		LayoutParams mainParams = (LayoutParams) mainLayout.getLayoutParams();
		mainParams.height = (int) (mItemHeight - dm.density*20);
		mainLayout.setLayoutParams(mainParams);
		
		RelativeLayout.LayoutParams mrl =  (android.widget.RelativeLayout.LayoutParams) mHListView.getLayoutParams();
		int mainHeight = (int) ((mItemHeight - dm.density*20)*3f/4);
		mrl.height = mainHeight;
		mHListView.setLayoutParams(mrl);
		
		LayoutParams subParams = (LayoutParams) subLayout.getLayoutParams();
		subParams.height = (int) (mItemHeight/2 + dm.density*20);
		subLayout.setLayoutParams(subParams);
		
		RelativeLayout.LayoutParams srl =  (android.widget.RelativeLayout.LayoutParams) sHListView.getLayoutParams();
		int subHeight = mItemHeight/2;
		srl.height = subHeight;
		sHListView.setLayoutParams(srl);
		
		if (mainCategory != null) {
			propmtTxt.setText(mainCategory.name);
			mainAdapter = new GCSSCategoryAdapter(GCSSCategorySelectActivity.this,mainHeight,mainCategory.searchStarters,mMemoryCache,true);
			mHListView.setAdapter(mainAdapter);			
		}
			
		if (subCategorys != null) {
			int subNum = subCategorys.size();			
			if (subNum > 0) {
				subAdapters = new ArrayList<GCSSCategoryAdapter>(subNum);										
				for (int i = 0; i < subNum; i++) {										
					SearchStarterCategory ssc = subCategorys.get(i);													
					GCSSCategoryAdapter adapter = new GCSSCategoryAdapter(GCSSCategorySelectActivity.this,subHeight,ssc.searchStarters,mMemoryCache,false);
					subAdapters.add(adapter);																												
				}
				
				if (subNum >= 5) {
					sBHListView.setX(0);
				}else {
					int disSpace = dm.widthPixels*(5-subNum)/10;									
					sBHListView.setX(disSpace);
				}											
			}							
		}
			
		if (subAdapters != null) {
			gCSBadapter = new GCSubButtonAdapter(GCSSCategorySelectActivity.this,subCategorys);
			sBHListView.setAdapter(gCSBadapter);
				
			selectSBIndex = 0;
			GCSSCategoryAdapter adapter = subAdapters.get(0);
			sHListView.setAdapter(adapter);	
		}			
		mHListView.setOnItemClickListener(onMainItemSelectListener);
		sBHListView.setOnItemClickListener(onSubBItemSelectListener);
		sHListView.setOnItemClickListener(onSubItemSelectListener);		
	}	
	
	@Override
	public void startOver() {
		super.startOver();
	}
	
	@Override
	public void judgeHaveItems(){
		
	}
	
	@Override
	protected void onPause() {			
		MemoryCacheUtil.evictAll(mMemoryCache);
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (subAdapters != null) {
			subAdapters.clear();
			subAdapters = null;
		}
		if (subCategorys != null) {
			subCategorys.clear();
			subCategorys = null;
		}
		mMemoryCache = null;
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);		
		if(v.getId()==R.id.previous_button){				
			dealBeforeSkip();
			finish();					
		}
	}	
	
	private OnItemClickListener onMainItemSelectListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
			onItem(true,position);
		}
	};
	
	private OnItemClickListener onSubBItemSelectListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
			selectSBIndex = position;
			gCSBadapter.setSelectedItem(selectSBIndex);
			GCSSCategoryAdapter adapter = subAdapters.get(position);
			sHListView.setAdapter(adapter);	
			adapter.notifyDataSetChanged();
		}
	};
	
	private OnItemClickListener onSubItemSelectListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
			onItem(false,position);
		}
	};
	
	private void onItem(boolean isMain,int position){
		SearchStarter ss = null;
		if (isMain) {
			ss = mainCategory.searchStarters.get(position);
		}else {
			SearchStarterCategory ssc = subCategorys.get(selectSBIndex);
			ss = ssc.searchStarters.get(position);
		}
		if (ss == null) return;
		if (ss.filters == null) return;
		GCLoadCategoryTask task = new GCLoadCategoryTask(GCSSCategorySelectActivity.this,ss);
		task.execute();
	}	
	
	public void dealSkip(SearchStarter ss,List<GCCategory> result){
		dealBeforeSkip();
		Intent mIntent = new Intent(GCSSCategorySelectActivity.this, GCCategorySelectActivity.class);		
		Bundle bundle = new Bundle();
		bundle.putString("SSName", ss.name);	
		bundle.putSerializable("gCCategory", (Serializable)result);
		mIntent.putExtras(bundle);		
		startActivity(mIntent);				
		GCSSCategorySelectActivity.this.finish();		
	}	
	
	private void dealBeforeSkip(){
		if (mainAdapter != null) {
			mainAdapter.cancelRequest();
		}	
		if (subAdapters != null) {
			for (GCSSCategoryAdapter adapter : subAdapters) {
				if (adapter == null) continue;
				adapter.cancelRequest();
			}
		}		
		mHListView.setAdapter(null);
		sBHListView.setAdapter(null);
		sHListView.setAdapter(null);			
	}
		
}
