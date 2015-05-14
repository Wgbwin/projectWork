package com.kodak.rss.tablet.adapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.OnProcessResponseEndListener;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.SelectImageView;

public class PhotoBooksProductPicturesAdapter extends PhotoBooksProductAdapter implements onProcessImageResponseListener{
		
	private Bitmap addimage;
	public ArrayList<ImageInfo> mPagesPics;
	public ArrayList<Layer> layerList;

	public PhotoBooksProductPicturesAdapter(Context context, float ratio, LruCache<String, Bitmap> mMemoryCache) {
		super(context,ratio,mMemoryCache);				
		this.mContext = context;							
		addimage = BitmapFactory.decodeResource(context.getResources(),R.drawable.addimage);		
		this.imageDownloader.setIsThumbnail(true);
		if (mLayoutParams != null) {
			this.imageDownloader.setViewParameters(mLayoutParams.width, mLayoutParams.height);
		}		
		mPhotobook = PhotoBookProductUtil.getCurrentPhotoBook();				
		itemSize = 1;		
		mPagesPics = mPhotobook.chosenpics;
		layerList = mPhotobook.chosenLayers;										
		itemSize = mPhotobook.chosenLayers.size()+ mPhotobook.chosenpics.size()+1;		
	}
	
	public void refresh(){		
		refreshItem();
		notifyDataSetChanged();	
	}
	
	public void refreshItem(){		
		mPhotobook = PhotoBookProductUtil.getCurrentPhotoBook();	
		mPagesPics = mPhotobook.chosenpics;
		layerList = mPhotobook.chosenLayers;					
		itemSize = mPhotobook.chosenLayers.size() + mPhotobook.chosenpics.size() + 1;			
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
	
	private void getView(int position,SelectImageView imageView ) {	
		if (end_index > itemSize - 1) {
			end_index = itemSize - 1;
		}	
		if (position <= mPhotobook.chosenLayers.size()) {
			int pos = position - 1;
			Layer layer = mPhotobook.chosenLayers.get(pos);
			boolean isInlayer = PhotoBookProductUtil.isInPagelayer(layer);		
			URI pictureURI = PhotoBookProductUtil.getURI(layer,mLayoutParams.width,mLayoutParams.height);
			if (pictureURI == null) {
				imageView.setTag(position);					
				imageView.setImageBitmap(getWaitBitmap(),false); 
			}else {
				String layerId = layer.contentId;		
				Bitmap mBitmap = MemoryCacheUtil.getBitmap(mMemoryCache, layerId);				
				imageView.setTag(layerId);					
				imageView.setImageBitmap((mBitmap == null ? getWaitBitmap() : mBitmap),isInlayer); 					
				if (((start_index > 0 && end_index > 0) && start_index <= position && end_index >= position) ||(start_index == 0 && end_index >= 0)) {					
					if (!lock && mBitmap == null) {	
						imageDownloader.downloadProfilePicture(layerId, pictureURI, imageView,position,true,mPhotobook.id);
					}
				}
			}
		}else {
			int pos = position - mPhotobook.chosenLayers.size() - 1;
			ImageInfo info = mPhotobook.chosenpics.get(pos);
			if (info == null) {
				imageView.setTag(position);					
				imageView.setImageBitmap(getWaitBitmap(),false); 
			}else {
				String infoId = info.id;
				boolean isInlayer = PhotoBookProductUtil.isInlayer(info.imageThumbnailResource);	
				Bitmap mBitmap = MemoryCacheUtil.getBitmap(mMemoryCache, infoId);	
				imageView.setTag(infoId);					
				imageView.setImageBitmap((mBitmap == null ? getWaitBitmap() : mBitmap),isInlayer); 					
				if (((start_index > 0 && end_index > 0) && start_index <= position && end_index >= position) ||(start_index == 0 && end_index >= 0)) {					
					if (!lock && mBitmap == null) {	
						if (info.isfromNative) {									
							imageDownloader.downloadProfilePicture(infoId, null, imageView, pos);															
						}else {
							String thumbnailPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, infoId, true);
							if (thumbnailPath != null) {
								imageDownloader.downloadProfilePicture(infoId, thumbnailPath, imageView, pos);			
							}else {
								URI pictureURI = null ;			
								try {
									pictureURI = new URI(info.downloadThumbnailUrl);
						    	} catch (URISyntaxException e) {
						    		pictureURI = null;
						    	}  
								if (pictureURI != null) {
									RssTabletApp.getInstance().imageDownloader.downloadProfilePicture(infoId, pictureURI, imageView, position,true,true,FilePathConstant.bookType,mPhotobook.id);
							   		RssTabletApp.getInstance().setOnProcessResponseEndListener(new OnProcessResponseEndListener() {						
										@Override
										public void onProcessEnd(ImageInfo imageInfo,boolean isEdit) {
											if (isEdit) return;
											notifyDataSetChanged();						
										}
									});			
								}
							}
						}
					}
				}				
			}						
		}					
	}
	
	private Bitmap getWaitBitmap(){
		if (waitBitmap == null ||waitBitmap.isRecycled()) {
            waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);
    	}
		return waitBitmap;
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
			if(addimage != null){
				imageView.setImageBitmap(addimage,false);		
			}			
		} else{
			getView(position,imageView);		
		}
		imageView.setLayoutParams(mLayoutParams);
		return convertView;
	}

	@Override
	public void onProcess(Response response, String profileId, View view,int position,String flowTpye,String productId) {			
		if (response == null || imageDownloader == null) return;
		if (mPhotobook == null || mPhotobook.chosenLayers == null) return;	
		if (productId != null && !productId.equals(mPhotobook.id)) return;					
		if (response.getError() != null) {
			
		}else{
			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);	
			if (bitmap != null && view != null) {
				if (view instanceof SelectImageView ) {	
					if (profileId.equals(view.getTag().toString())) {
						if (view.getVisibility() == View.VISIBLE) {
							boolean isInlayer = false;
							if (productId != null) {
								isInlayer = PhotoBookProductUtil.isInPagelayer(mPhotobook.chosenLayers.get(position-1));
							}else {
								ImageInfo info = mPhotobook.chosenpics.get(position);								
								if (info != null) {
									isInlayer = PhotoBookProductUtil.isInlayer(info.imageThumbnailResource);	
								}		
							}						
							((SelectImageView) view).setImageBitmap(bitmap, isInlayer);	
						}	
					}	
				} else {
					view.postInvalidate();
				}
			}	
		}	
	}

	public void cancelRequest(){
		if (imageDownloader == null) return;
		if (mPhotobook.chosenLayers == null) return;		
		for (int j = 0; j < mPhotobook.chosenLayers.size(); j++) {
			Layer layer = mPhotobook.chosenLayers.get(j);       
			cancelRequest(layer);
		}	
		for (int j = 0; j < mPhotobook.chosenpics.size(); j++) {
			ImageInfo info = mPhotobook.chosenpics.get(j);			
			cancelRequest(info);			
		}			
	}
	
	private void cancelRequest(ImageInfo info){
		if (info == null) return;
		String infoId = info.id;		
		imageDownloader.cancelRequest(infoId, null, 0);		
	}

}
