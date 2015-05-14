package com.kodak.rss.tablet.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.JudgeImageFileTypeUtil;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;

public class PhotosAdapter extends CanZoomBaseAdapter implements onProcessImageResponseListener{
	
	private LayoutInflater mInflater;		
	SortableHashMap<String, SortableHashMap<Integer, String[]>> photosBuckets ;
	Bitmap picturestack;		
	private int size ;	
	private String albumUnit = "Photos" ;		
	private String IdInZero;
	public List<Integer> dirtyList;
	public List<Integer> goodList;
	
	public PhotosAdapter(Context context,SortableHashMap<String, SortableHashMap<Integer, String[]>> photosBuckets,LruCache<String, Bitmap> mMemoryCache) {
		super(context,mMemoryCache);
		this.photosBuckets = photosBuckets;
		size =photosBuckets.size();
		mInflater = LayoutInflater.from(context);		
		picturestack=BitmapFactory.decodeResource(context.getResources(),R.drawable.picturestack);
		mLayoutParams = new RelativeLayout.LayoutParams(150,150);
		pendingRequests = new HashMap<String, Request>();
		imageDownloader = new ImageUseURIDownloader(context, pendingRequests, this);		
		albumUnit = context.getString(R.string.photos);
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

	private Bitmap getBitmap(SortableHashMap<Integer, String[]> imgList,int position,ImageView imageView) {	
		Bitmap result = null;			
		if (imgList == null) return result;		
		int id = imgList.keyAt(0);
		if (id <= 0) return result;
		if (dirtyList != null && dirtyList.contains(id)) return result;

		String key = String.valueOf(id);
		result =  MemoryCacheUtil.getBitmap(mMemoryCache, key);					
		if (result != null) return result;

		if (IdInZero != null && key != null && IdInZero.equals(key)) return result;
		if (position == 0) {			
			if (isWebp(id, imgList)) return result;
			IdInZero = key;
			result = ImageUtil.getThumbnail(mContext.getContentResolver(), id);
			if (result == null) {
				if (dirtyList == null) {
					dirtyList = new ArrayList<Integer>(2);
				}
				dirtyList.add(id);
				return result;
			}else {
				MemoryCacheUtil.putBitmap(mMemoryCache, key, result);				
			}						
			return result;
		}else {			
			if (end_index > size-1) {
				end_index = size-1;
			}	
			if (((start_index > 0 && end_index > 0)&&start_index <= position && end_index >= position)||(start_index == 0 && end_index >= 0)) {							
				if (lock) return result;
				if (isWebp(id, imgList)) return result;
				imageView.setTag(key);				
				imageDownloader.downloadProfilePicture(key,null,imageView, position);				
			}
		}		
		return result;
	}

	private boolean isWebp(int id, SortableHashMap<Integer, String[]> imgList){
		boolean isWebp = false;
		if (imgList == null)return isWebp;
		if (!(goodList != null && goodList.contains(id))){
			String[] filePath = imgList.valueAt(0);	
			if (filePath != null && filePath.length > 0) {
				boolean isWebP =  JudgeImageFileTypeUtil.isFilter(filePath[0]);
				if (isWebP) {
					if (dirtyList == null) {
						dirtyList = new ArrayList<Integer>(2);
					}
					dirtyList.add(id);
					isWebp = true;
					return isWebp;
				}else {
					if (goodList == null) {
						goodList = new ArrayList<Integer>(2);
					}
					goodList.add(id);
				}
			}
		}
		return isWebp;
	}	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {							
		PhotosViewHolder photosViewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.grid_photos_item, null);
			photosViewHolder = new PhotosViewHolder();		
			photosViewHolder.photoName = (TextView) convertView.findViewById(R.id.photoName);
			photosViewHolder.photoNum = (TextView) convertView.findViewById(R.id.photoNum);
			photosViewHolder.imageView = (ImageView) convertView.findViewById(R.id.photoContent);			
			convertView.setTag(photosViewHolder);
		} else {
			photosViewHolder = (PhotosViewHolder) convertView.getTag();
		}
		
		SortableHashMap<Integer, String[]> imgList = photosBuckets.valueAt(position);		
		String[] picInfo= imgList.valueAt(0);	
        photosViewHolder.photoName.setText(picInfo[1]);	
        photosViewHolder.photoNum.setText(imgList.size()+" "+albumUnit);	
        
        Bitmap bitmap = getBitmap(imgList, position,photosViewHolder.imageView);
        if (bitmap != null) {       	
        	if (picturestack ==null || picturestack.isRecycled()) {
        		picturestack=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.picturestack);
    		}
        	photosViewHolder.imageView.setImageBitmap(ImageUtil.overlay(bitmap, picturestack));        		       				
		}else {
			if (picturestack ==null || picturestack.isRecycled()) {
        		picturestack=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.picturestack);
    		}
        	photosViewHolder.imageView.setImageBitmap(picturestack);   
		}
        photosViewHolder.imageView.setLayoutParams(mLayoutParams);     
		return convertView;
      }

		@Override
		public void onProcess(Response response, String profileId, View view,int position, String flowType, String productId) {
			if (response == null || pendingRequests == null) return;	
			if (response.getRequest() == null) return;
			String key = response.getRequest().getImageId();
			pendingRequests.remove(key);
			int Id = Integer.valueOf(key);
			if (Id <= 0) return;		
			if (response.getError() != null) {
				
			}else{			
				Bitmap bitmap = response.getBitmap();
				if (bitmap != null) {										
					MemoryCacheUtil.putBitmap(mMemoryCache, key, bitmap);	
					if (view != null && view instanceof ImageView){
						if (view.getTag().toString().equals(key)) {
							if (picturestack ==null || picturestack.isRecycled()) {
					        	picturestack=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.picturestack);
					    	}
							((ImageView)view).setImageBitmap(ImageUtil.overlay(bitmap, picturestack));   
						}		
					}				
				}else {
					if (dirtyList == null) {
						dirtyList = new ArrayList<Integer>(2);
					}
					dirtyList.add(Id);
				}		 				
			}					
		}
		
		@Override
		public void cancelRequest(){		
			if (photosBuckets == null) return;
			int size = photosBuckets.size();
			if (size == 0) return;		
			for (int i = size-1; i >= 0; i--) {
				SortableHashMap<Integer, String[]> imgList = photosBuckets.valueAt(i);	
				if (imgList == null) continue;
				if (imgList.size() == 0) continue;
				int id = imgList.keyAt(0);		
				cancelNativeRequest(id);
			}	
		}
		
   }

	class PhotosViewHolder {
		TextView photoName;
		TextView photoNum;
		ImageView imageView;
	}