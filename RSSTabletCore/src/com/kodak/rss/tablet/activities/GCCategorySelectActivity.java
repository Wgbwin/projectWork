package com.kodak.rss.tablet.activities;

import java.io.Serializable;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.greetingcard.GCCategory;
import com.kodak.rss.core.util.DeviceInfoUtil;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.GCCategorySelectionAdapter;
import com.kodak.rss.tablet.thread.GCLoadDesignCategoryTask;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.view.GCCreateView;
import com.kodak.rss.tablet.view.HorizontalListView;
import com.kodak.rss.tablet.view.HorizontalListView.OnItemClickSelfListener;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class GCCategorySelectActivity extends BaseNetActivity implements OnClickListener{		
	private HorizontalListView hListView;	
	private Button previousButton;
	private TextView select_propmt;	
		
	private GCCategorySelectionAdapter adapter;
	public LruCache<String, Bitmap> mMemoryCache;
	
	private GCCreateView gCCreateView;
	private int itemHeight;
	private View waitView;
	private String dispalyStr;
	
	private List<GCCategory> gccList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);								
		setContentView(R.layout.activity_category_select_card);			
		mMemoryCache = MemoryCacheUtil.generMemoryCache(16);
		initData();			
		initView();
	}
	
	@SuppressWarnings("unchecked")
	private void initData(){	
		if (getIntent() != null) {
			dispalyStr = getIntent().getStringExtra("SSName");
			gccList = (List<GCCategory>) getIntent().getSerializableExtra("gCCategory");
		}
	}
		
	private void initView(){		
		int width = (int) (dm.widthPixels / 4.8f);				
		int statusBarHeight = DeviceInfoUtil.getStatusHeight(GCCategorySelectActivity.this);
		itemHeight = (int) ((dm.heightPixels - dm.density * 130  - statusBarHeight)/2f);
		
		previousButton =(Button) findViewById(R.id.previous_button);
		previousButton.setOnClickListener(this);
		
		select_propmt = (TextView) findViewById(R.id.select_propmt);
		select_propmt.setText(dispalyStr);
		
		hListView = (HorizontalListView) findViewById(R.id.hlv_themes);
		
		gCCreateView = (GCCreateView) findViewById(R.id.creat_gc); 
		waitView = findViewById(R.id.waitPar); 
		
		adapter = new GCCategorySelectionAdapter(GCCategorySelectActivity.this, width, itemHeight, gccList,mMemoryCache);
		hListView.setAdapter(adapter);
		
		hListView.setOnItemClickSelfListener(onItemClickSelfListener);
	}	
	
	OnItemClickSelfListener onItemClickSelfListener = new OnItemClickSelfListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id, float rawX, float rawY) {
			if (gccList == null) return;	
			int pos = 2* position;						
			float height = dm.density * 130 + itemHeight;
			if (rawY > height) {
				pos += 1;
			}			
			if (gccList.size() <= pos) return;
			GCCategory gcc = gccList.get(pos);
			if (gcc == null) return;	
			GCLoadDesignCategoryTask loadInfoTask = new GCLoadDesignCategoryTask(GCCategorySelectActivity.this,gcc,waitView);
			loadInfoTask.execute();			
		}		
	};
			
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
		mMemoryCache = null;
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v.getId()==R.id.previous_button){	
			dealBeforeSkip();
			startActivity(new Intent(GCCategorySelectActivity.this, GCSSCategorySelectActivity.class));	
			GCCategorySelectActivity.this.finish();						
		}			
	}	
		
	public void dealDesignCategory(List<GCCategory> designGCCList){	
		if(designGCCList == null || (designGCCList != null && designGCCList.isEmpty())){
			InfoDialog warningDialog = new InfoDialog.Builder(this).setMessage(R.string.CardSelection_NoCards)
					.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();	
						}
					}).create();
			warningDialog.show();
		} else {
			RSSLocalytics.recordLocalyticsPageView(this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_GC_TYPE);
			gCCreateView.setValue(designGCCList).showAt();
		}
	}
	
	public void dealCreateCard(){
		dealBeforeSkip();
		Intent mIntent = new Intent(GCCategorySelectActivity.this, GCEditActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("SSName", dispalyStr);	
		bundle.putSerializable("gCCategory", (Serializable)gccList);
		mIntent.putExtras(bundle);		
		startActivity(mIntent);				
		GCCategorySelectActivity.this.finish();
	}
	
	private void dealBeforeSkip(){
		if (adapter != null) {
			adapter.cancelRequest();
		}			
		hListView.setAdapter(null);					
	}
}
