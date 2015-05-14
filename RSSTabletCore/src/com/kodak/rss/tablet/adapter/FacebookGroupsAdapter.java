package com.kodak.rss.tablet.adapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.facebook.FbkGroup;
import com.kodak.rss.tablet.facebook.FbkObject;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;

public class FacebookGroupsAdapter extends CanZoomBaseAdapter implements onProcessImageResponseListener{
	private LayoutInflater mInflater;		
	ArrayList<FbkObject> fbkGroupList;
	Bitmap picturestack;		
	private int size ;			
		
	public FacebookGroupsAdapter(Context context,ArrayList<FbkObject> fbkGroups,LruCache<String, Bitmap> mMemoryCache) {
		super(context,mMemoryCache);			
		this.fbkGroupList = fbkGroups;
		size =fbkGroupList.size();		
		mInflater = LayoutInflater.from(context);
		picturestack = BitmapFactory.decodeResource(context.getResources(),R.drawable.picturestack);
		pendingRequests = new HashMap<String, Request>();
		imageDownloader = new ImageUseURIDownloader(context,pendingRequests,this);
		imageDownloader.setSaveType(FilePathConstant.externalType);
		imageDownloader.setIsThumbnail(true);		
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
	
	private Bitmap getBitmap(FbkGroup group,int position,ImageView imageView) {	
		Bitmap result = null;
		if (group == null) return result;
		String key = group.ID;
		result = MemoryCacheUtil.getBitmap(mMemoryCache,key);
		if (result != null) return result;

		if (position == 0) {
			imageView.setTag(key);			
			URI pictureURI = group.getGroupUri();
			if (pictureURI != null) {					
				imageDownloader.downloadProfilePicture(key, pictureURI, imageView, position,true); 
			}		 								
			return result;
		}else{			
			if (end_index > size-1) {
				end_index = size-1;
			}	
			if (((start_index > 0 && end_index > 0)&&start_index <= position && end_index >= position)||(start_index == 0 && end_index >= 0)) {
				if (lock) return result;
				URI pictureURI = group.getGroupUri();
				if (pictureURI != null) {
					imageView.setTag(key);
					imageDownloader.downloadProfilePicture(key, pictureURI, imageView, position,true); 
				}			  			 			
			}						
		}
		return result;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {							
		FacebookGroupsHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.grid_facebook_photos_item, null);	
			holder = new FacebookGroupsHolder();
			holder.photoName = (TextView) convertView.findViewById(R.id.photoName);
			holder.imageView = (ImageView) convertView.findViewById(R.id.fbk_photoContent);	
			convertView.setTag(holder);
		}else {
			holder = (FacebookGroupsHolder) convertView.getTag();
		}				
		FbkGroup group = (FbkGroup) fbkGroupList.get(position);
		holder.photoName.setText(group.name);	
		
		Bitmap bitmap = getBitmap(group, position,holder.imageView);
		if (picturestack ==null || picturestack.isRecycled()) {
    		picturestack=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.picturestack);
		}
		if (bitmap != null) {
			holder.imageView.setImageBitmap(ImageUtil.overlay(bitmap, picturestack));
		}else {
			holder.imageView.setImageBitmap(picturestack);			
		}
		holder.imageView.setLayoutParams(mLayoutParams);      	  
		return convertView;
      }

	@Override
	public void onProcess(Response response, String profileId,View imageView,int position,String flowTpye, String productId) {			
		if (response == null || imageDownloader == null) return;				
		if (response.getError() != null) {
			
		}else{			
			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);	
			if (bitmap != null && imageView != null) {					
				if (imageView.getTag().toString().equals(profileId)) {							
					if (imageView.getVisibility() == View.VISIBLE) {
						if (picturestack ==null || picturestack.isRecycled()) {
							picturestack=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.picturestack);
						}
						((ImageView)imageView).setImageBitmap(ImageUtil.overlay(bitmap, picturestack));								
					}							
				}	 							
			}
		}	
	}

	@Override
	public void cancelRequest(){		
		if (fbkGroupList == null) return; 		
		int size = fbkGroupList.size();
		if (size == 0) return; 
		for (int i = size - 1; i >= 0; i--) {
			FbkGroup group = (FbkGroup) fbkGroupList.get(i);
			if (group == null) continue; 
			cancelServerRequest(group.ID);
		}
	}	
		
	class FacebookGroupsHolder {		
		TextView photoName;	
		ImageView imageView;
	}
	
  }
		