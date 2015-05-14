package com.kodak.rss.tablet.adapter;

import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;

public abstract class CanZoomBaseAdapter extends BaseAdapter{

	public int mItemWidth = 0;	
	public RelativeLayout.LayoutParams mLayoutParams;	
	BitmapDrawable bd;	
	public int start_index = 0;	
	public int end_index = 0;
	public boolean lock = false;
	
	public Context mContext;
	public ImageUseURIDownloader imageDownloader;	
	public Map<String, Request> pendingRequests;
	public LruCache<String, Bitmap> mMemoryCache;  	

	public CanZoomBaseAdapter(Context mContext,LruCache<String, Bitmap> mMemoryCache) {
		this.mContext = mContext;
		this.mMemoryCache = mMemoryCache;		
	}

	public void setItemWidth(int width) {
		lock = false;
		if (width == mItemWidth) {
			return;
		}		
		mItemWidth = width;
		mLayoutParams = new RelativeLayout.LayoutParams(mItemWidth, mItemWidth);
		notifyDataSetChanged();
	}

	public void loadContentRange(int start_index, int end_index){
		this.start_index = start_index;
		this.end_index = end_index;		
		lock = false;
		notifyDataSetChanged();
	}
	
	public void setRange(int width){
		this.start_index = 0;
		if (width != mItemWidth) {
			mItemWidth = width;
			mLayoutParams = new RelativeLayout.LayoutParams(mItemWidth, mItemWidth);			
		}
		lock = false;
		notifyDataSetChanged();	
	}
	
	public void cancelRequest(){
		if (mMemoryCache != null) {
			mMemoryCache.evictAll();
		}	
	}		
	
	public void cancelServerRequest(String id){	
		if (pendingRequests == null) return;
		if (imageDownloader == null) return;
		imageDownloader.cancelRequest(id, null, 0);		
	}      
	
	public void cancelNativeRequest(int thumId){
		if (pendingRequests == null) return;
		if (imageDownloader == null) return;
		String imageId = String.valueOf(thumId);		
		imageDownloader.cancelRequest(imageId, null, 0);		
	}      

}
