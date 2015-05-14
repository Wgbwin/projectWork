package com.kodak.rss.tablet.adapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;

import com.kodak.rss.core.n2r.bean.content.Theme.BackGround;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.view.SelectImageView;

public class PhotoBooksProductBackgroundsAdapter extends PhotoBooksProductAdapter{

	private Bitmap bitmap;
	public  List<BackGround> mBackGrounds;	
	private Photobook currentPhotoBook;
		
	public PhotoBooksProductBackgroundsAdapter(Context context,List<BackGround> backGroundList,float ratio,LruCache<String, Bitmap> mMemoryCache) {
		super(context,ratio,mMemoryCache);				
		bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.changethemeup);		
		this.imageDownloader.setIsThumbnail(true);
		if (mLayoutParams != null) {
			this.imageDownloader.setViewParameters(mLayoutParams.width, mLayoutParams.height);
		}	
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();		
		itemSize = 1;
		this.mBackGrounds = backGroundList;
		if (mBackGrounds != null) {
			itemSize = mBackGrounds.size() +1;
		}						
	}
	
	@Override
	public int getCount() {
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
		SelectImageView imageView = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.grid_image_item, null);
			imageView =(SelectImageView) convertView.findViewById(R.id.photoContent);
			convertView.setTag(imageView);
		}else {
			imageView = (SelectImageView) convertView.getTag();
		}
			
		if (position == 0) {
			if (bitmap == null) {
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.changethemeup);	
			}
			imageView.setTag(0);	
			imageView.setImageBitmap(bitmap,false);									
		}else {
			if (end_index > itemSize - 1) {
				end_index = itemSize - 1;
			}
			BackGround groud =  mBackGrounds.get(position-1);
			if (groud == null) {
				imageView.setTag(position);	
				imageView.setImageBitmap(getWaitBitmap(),false); 
			}else {
				String groundId = groud.id;
				Bitmap mBitmap = MemoryCacheUtil.getBitmap(mMemoryCache, groundId);	
				imageView.setTag(groundId);	
				imageView.setImageBitmap((mBitmap == null ? getWaitBitmap() : mBitmap),false); 						
				if (((start_index > 0 && end_index > 0) && start_index <= position && end_index >= position) ||(start_index == 0 && end_index >= 0)) {					
					if (!lock && mBitmap == null) {									
						URI pictureURI = null ;			
						try {
							pictureURI = new URI(groud.glyphURL);
			    		} catch (URISyntaxException e) {
			    			pictureURI = null;
			    		}   	       	
			    		if (pictureURI != null) { 							
							imageDownloader.downloadProfilePicture(groundId, pictureURI, imageView,position,true,currentPhotoBook.id);	
						}							
					}						 			
				}		
			}
		}
		imageView.setLayoutParams(mLayoutParams);
		return convertView;
	}

	private Bitmap getWaitBitmap(){
		if (waitBitmap == null || waitBitmap.isRecycled()) {
            waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);
    	}
		return waitBitmap;
	}
	
	@Override
	public void onProcess(Response response, String profileId, View view,int position,String flowTpye,String productId) {			
		if (response == null || imageDownloader == null) return;
		if (currentPhotoBook == null || currentPhotoBook.chosenLayers == null) return;	
		if (productId != null && !productId.equals(currentPhotoBook.id)) return;							
		if (response.getError() != null) {
			
		}else{
			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);		
			if (bitmap != null && view != null) {
				if (view instanceof SelectImageView ) {
					if (profileId.equals(view.getTag().toString())) {
						if (view.getVisibility() == View.VISIBLE) {
							((SelectImageView) view).setImageBitmap(bitmap,false);
						}							
					}else {
						notifyDataSetChanged();
					}						
				} 
			}	
		}				
	}

	public void cancelRequest(){
		if (mBackGrounds == null) return;				
		for (int j = 0; j < mBackGrounds.size(); j++) {
			BackGround backGroud = mBackGrounds.get(j);       
			dealRequest(backGroud, true);
		}		
	}
	
	private void dealRequest(BackGround backGroud,boolean isCancel){
		if (backGroud == null) return;
		if (currentPhotoBook == null) return;
		if (imageDownloader == null) return;
		if (pendingRequests == null) return;	
		if (isCancel) {
			imageDownloader.cancelRequest(backGroud.id, currentPhotoBook.id, 0);
		}else {
			imageDownloader.prioritizeRequest(backGroud.id, currentPhotoBook.id, 0);
		}
	}
		
}
