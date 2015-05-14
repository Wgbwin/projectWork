package com.kodak.rss.tablet.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;

import com.kodak.rss.core.n2r.bean.greetingcard.GCCategory;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.view.GCItemView;

public class GCDesignCategorySelectionAdapter extends GCBaseAdapter {	
	
	private int size;		
	private int itemWidth;
	private int itemHeight;
	private List<GCCategory> gCCategorys;
	private int selectedPosition = 0;
		
	public GCDesignCategorySelectionAdapter(Context context,int itemWidth,int itemHeight,List<GCCategory> gCCategorys,LruCache<String, Bitmap> mMemoryCache){
		super(context, mMemoryCache);		
		this.itemWidth = itemWidth;
		this.itemHeight = itemHeight;
		this.gCCategorys = gCCategorys;
		size = 0;
		if (gCCategorys != null) {
			size = gCCategorys.size();	
		}
		this.imageDownloader = new ImageUseURIDownloader(mContext, pendingRequests, this);	
		this.imageDownloader.setSaveType(FilePathConstant.cardType);
		this.imageDownloader.setIsThumbnail(true);	
		this.imageDownloader.setViewParameters(itemWidth, itemHeight);
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
	
	public void setSelectedItem(int position){
		this.selectedPosition = position;
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		GCItemView itemView;
		if (convertView == null) {
			itemView = new GCItemView(mContext,itemWidth,itemHeight);	
			convertView = itemView;
			convertView.setTag(itemView);
		}else {
			itemView = (GCItemView) convertView.getTag();
		}
		
		GCCategory gcc = gCCategorys.get(position);		
		itemView.setValue(GCDesignCategorySelectionAdapter.this,gcc,position, position==selectedPosition);	
		return convertView;
	}
	
}
