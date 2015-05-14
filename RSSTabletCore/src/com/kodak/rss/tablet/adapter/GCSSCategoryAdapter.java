package com.kodak.rss.tablet.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;

import com.kodak.rss.core.n2r.bean.content.SearchStarter;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageDownloader;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.view.GCItemView;

public class GCSSCategoryAdapter extends GCBaseAdapter{
	
	private int itemHeight;
	public int size;
	private List<SearchStarter> searchStarters;
	private boolean isMain;

	public GCSSCategoryAdapter(Context context,int itemHeight,List<SearchStarter> searchStarters,LruCache<String, Bitmap> mMemoryCache,boolean isMain) {
		super(context, mMemoryCache);		
		this.itemHeight = itemHeight;
		this.isMain = isMain;
		this.searchStarters = searchStarters;
		size = searchStarters.size();		
		this.imageDownloader = new ImageUseURIDownloader(mContext, pendingRequests, this);	
		this.imageDownloader.setSaveType(FilePathConstant.cardType);
		this.imageDownloader.setIsThumbnail(true);	
		
		int mItemWidth = (int) (dm.widthPixels*1.0/5.2f);		
		this.imageDownloader.setViewParameters(mItemWidth, itemHeight);		
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
		GCItemView itemView;
		if (convertView == null) {
			itemView = new GCItemView(mContext,itemHeight,size,5.2);	
			convertView = itemView;
			convertView.setTag(itemView);
		}else {
			itemView = (GCItemView) convertView.getTag();
		}		
		
		SearchStarter ss = searchStarters.get(position);		
		itemView.setValue(GCSSCategoryAdapter.this,ss,position,isMain);	
		return convertView;
	}

	public void cancelRequest(){
		if (searchStarters == null) return;		
		for (int i = 0; i < searchStarters.size(); i++) {
			SearchStarter ss = searchStarters.get(i); 					
			dealRequest(ss, true);						
		}		
	}
	
	private void dealRequest(SearchStarter ss,boolean isCancel){
		if (ss == null) return;
		if (ss.name == null) return; 
		if("".equals(ss.name)) return;
		String id = getId(ss.name);	
		if (imageDownloader == null) return;		
		if (isCancel) {
			imageDownloader.cancelRequest(id, null, 0);
		}else {
			imageDownloader.prioritizeRequest(id, null, 0);
		}
	}
	
	
}
