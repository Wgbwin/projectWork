package com.kodak.rss.tablet.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kodak.rss.core.n2r.bean.greetingcard.GCCategory;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.view.GCItemView;

public class GCCategorySelectionAdapter extends GCBaseAdapter {	
	private int totleItemNum;
	private int size;	
	private boolean isLoadZore;		
	private List<GCCategory> gCCategorys;
	private LinearLayout.LayoutParams layoutParams;
	
	public GCCategorySelectionAdapter(Context context,int width,int height,List<GCCategory> gCCategorys,LruCache<String, Bitmap> mMemoryCache){ 
		super(context, mMemoryCache);	
		this.mContext = context;		
		this.gCCategorys = gCCategorys;
		size = 0;
		if (gCCategorys != null) {
			totleItemNum = gCCategorys.size();
			size = totleItemNum%2 == 0 ? totleItemNum/2 : totleItemNum/2 +1;			
		}			
		layoutParams = new LinearLayout.LayoutParams(width,height);
		layoutParams.setMargins(0, (int)dm.density*10, (int)dm.density*20, (int)dm.density*10);
		
		this.imageDownloader = new ImageUseURIDownloader(mContext, pendingRequests, this);	
		this.imageDownloader.setSaveType(FilePathConstant.cardType);
		this.imageDownloader.setIsThumbnail(true);	
		this.imageDownloader.setViewParameters(width, height);
	}

	@Override
	public int getCount() {		
		return size;		
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ThemeItemsHolder themeItemsHolder = null;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.gc_category_select_item, null,false);	
			themeItemsHolder = new ThemeItemsHolder();			
			themeItemsHolder.itemView_one = (GCItemView) convertView.findViewById(R.id.item_one);	
			themeItemsHolder.itemView_two = (GCItemView) convertView.findViewById(R.id.item_two);			
			convertView.setTag(themeItemsHolder);
		} else {
			themeItemsHolder = (ThemeItemsHolder) convertView.getTag();
		}
		
		if (position == 0 ) {			
			if (!isLoadZore) {	
				GCCategory gcc = gCCategorys.get(0);					
				themeItemsHolder.itemView_one.setValue(GCCategorySelectionAdapter.this,gcc,0);					
				if (totleItemNum > 1) {
					gcc = gCCategorys.get(1);
					themeItemsHolder.itemView_two.setVisibility(View.VISIBLE);
					themeItemsHolder.itemView_two.setValue(GCCategorySelectionAdapter.this,gcc,1);
				}else {
					themeItemsHolder.itemView_two.setVisibility(View.GONE);
				}												
			}
			isLoadZore = true;
		}else {				
			isLoadZore = false;			
			int pos = 2* position;
			GCCategory gcc = gCCategorys.get(pos);					
			themeItemsHolder.itemView_one.setValue(GCCategorySelectionAdapter.this,gcc,pos);
			
			pos = 2*position+1;
			if (totleItemNum > pos) {
				gcc = gCCategorys.get(pos);
				themeItemsHolder.itemView_two.setVisibility(View.VISIBLE);
				themeItemsHolder.itemView_two.setValue(GCCategorySelectionAdapter.this,gcc,pos);
			}else {
				themeItemsHolder.itemView_two.setVisibility(View.GONE);
			}						
		}
		
		themeItemsHolder.itemView_one.setLayoutParams(layoutParams);		
		themeItemsHolder.itemView_two.setLayoutParams(layoutParams);	
		return convertView;
	}
	
	class ThemeItemsHolder {		
		GCItemView itemView_one;
		GCItemView itemView_two;		
	}
	
	public void cancelRequest(){
		if (gCCategorys == null) return;		
		for (int i = 0; i < gCCategorys.size(); i++) {
			GCCategory gcc = gCCategorys.get(i); 					
			dealRequest(gcc, true);						
		}		
	}
	
	private void dealRequest(GCCategory gcc,boolean isCancel){
		if (imageDownloader == null) return;
		if (gcc == null) return;
		if (isCancel) {
			imageDownloader.cancelRequest(gcc.id, null, 0);
		}else {
			imageDownloader.prioritizeRequest(gcc.id, null, 0);
		}
	}

}
