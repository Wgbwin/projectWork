package com.kodak.rss.tablet.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.kodak.rss.core.n2r.bean.collage.AlternateLayout;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.view.collage.AlternateLayoutImageView;

public class CollageProductLayoutsAdapter extends CollageProductAdapter{
				
	private AbsListView.LayoutParams lp;
	public String oldCheckLayoutId;	
	
	public CollageProductLayoutsAdapter(Context context, float ratio,LruCache<String, Bitmap> mMemoryCache) {
		super(context,ratio,mMemoryCache);								
		lp = new AbsListView.LayoutParams(pageWidth, pageHeight);
	}

	public void refreash(float ratio){
		currentCollage = CollageUtil.getCurrentCollage();
		if (ratio > 0 && (ratio - wHRatio != 0)) {
			wHRatio = ratio;
			pageHeight = (int) (pageWidth*wHRatio);	
			lp.height = pageHeight;
		}
		notifyDataSetChanged();
	}	
	
	@Override
	public int getCount() {
		itemSize = 0;
		if (currentCollage != null && currentCollage.page != null && currentCollage.page.alternateLayouts != null) {
			itemSize = currentCollage.page.alternateLayouts.size();
		}
		return itemSize;
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
		AlternateLayoutImageView imageView = null;
		if (convertView == null) {			
			convertView = new AlternateLayoutImageView(mContext);
			imageView = (AlternateLayoutImageView) convertView;			
		}else {
			imageView = (AlternateLayoutImageView) convertView;
		}
					
		AlternateLayout alternateLayout = currentCollage.page.alternateLayouts.get(position);
		
		String layoutId = alternateLayout.layoutId;		
		imageView.setTag(layoutId);	
		imageView.setmAlternateLayout(alternateLayout, pageWidth, pageHeight);
		
		imageView.setLayoutParams(lp);
		return convertView;
	}
		
}
