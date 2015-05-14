package com.kodak.rss.tablet.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;


/**
 * Purpose: 
 * Author: Bing Wang 
 * Created Time: Aug 15, 2013 3:13:42 PM 
 */
public class SourcesAdapter extends CanZoomBaseAdapter {
	private LayoutInflater mInflater;			
	SortableHashMap<String, String> sourcesBucket ;
	Bitmap facebookBitmap = null;	
	Bitmap photosBitmap = null;		
	
	public SourcesAdapter(Context context,SortableHashMap<String, String> sourcesBucket,LruCache<String, Bitmap> mMemoryCache) {
		super(context,mMemoryCache);			
		this.sourcesBucket = sourcesBucket;
		mInflater = LayoutInflater.from(context);		
		facebookBitmap=BitmapFactory.decodeResource(context.getResources(),R.drawable.facebook);	
		photosBitmap=BitmapFactory.decodeResource(context.getResources(),R.drawable.photos);		 
	}

	@Override
	public int getCount() {		
		return sourcesBucket.size();
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
		SourcesViewHolder sourcesViewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.grid_sources_item, null);
			sourcesViewHolder = new SourcesViewHolder();		
			sourcesViewHolder.text = (TextView) convertView.findViewById(R.id.photoName);			
			sourcesViewHolder.imageView = (ImageView) convertView.findViewById(R.id.photoContent);						
			convertView.setTag(sourcesViewHolder);
		} else {
			sourcesViewHolder = (SourcesViewHolder) convertView.getTag();
		}
	
		String  imageDisplayName = sourcesBucket.valueAt(position);
		sourcesViewHolder.text.setText(imageDisplayName);
		String SourceKey = sourcesBucket.keyAt(position);
		if (SourceKey == null) return convertView;
		if(AppConstants.FB_SOURCE.equals(SourceKey)){
			if(facebookBitmap == null || facebookBitmap.isRecycled()){
				facebookBitmap=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.facebook);	
			}
			sourcesViewHolder.imageView.setImageBitmap(facebookBitmap);	
		}else{
			if(photosBitmap == null || photosBitmap.isRecycled()){
				photosBitmap=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.photos);
			}
			sourcesViewHolder.imageView.setImageBitmap(photosBitmap);	
		}							
		sourcesViewHolder.imageView.setLayoutParams(mLayoutParams);						
		return convertView;
      }
	}

	class SourcesViewHolder {
		TextView text;
		ImageView imageView;
	}

