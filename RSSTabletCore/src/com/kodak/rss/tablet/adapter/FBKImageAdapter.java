package com.kodak.rss.tablet.adapter;

import java.net.URI;
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

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.PhotoBookPicSelectMoreActivity;
import com.kodak.rss.tablet.facebook.FbkObject;
import com.kodak.rss.tablet.facebook.FbkPhoto;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.SelectImageView;

public class FBKImageAdapter extends CanZoomBaseAdapter implements onProcessImageResponseListener{
	
	private LayoutInflater mInflater;		
	public ArrayList<FbkObject> fbkImageList;
	Bitmap waitBitmap;	
	Bitmap bitmap;
	public boolean isChice[];
	public boolean isDeal[];
		
	RssTabletApp app;
	private int size;	
	private String flowType;
	public SortableHashMap<Integer, String> keyPMap;
	public boolean isOldChice[];
	private boolean isDrawTran;
	
	public FBKImageAdapter(Context context, ArrayList<FbkObject> fbkImages,String flowType,LruCache<String, Bitmap> mMemoryCache) {
		super(context,mMemoryCache);			
		app = RssTabletApp.getInstance();
		this.fbkImageList = fbkImages;
		this.flowType = flowType;
		mInflater = LayoutInflater.from(context);
		pendingRequests = new HashMap<String, Request>();
		imageDownloader = new ImageUseURIDownloader(context,pendingRequests,this);
		imageDownloader.setSaveType(FilePathConstant.externalType);
		imageDownloader.setIsThumbnail(true);		
		waitBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.imagewait60x60);
			
		size = fbkImageList.size();
		isChice = new boolean[size];		
		isDeal = new boolean[size];
		keyPMap = new SortableHashMap<Integer, String>();
		isOldChice = new boolean[size];
		for (int i = 0; i < size; i++) {
			isOldChice[i] = true;
		}
		if (AppConstants.bookType.equalsIgnoreCase(flowType) && context instanceof PhotoBookPicSelectMoreActivity) {				
			isDrawTran = true;					
		}
		
		if (AppConstants.printType.equalsIgnoreCase(flowType)) {
			List<ImageInfo> delList = new ArrayList<ImageInfo>();
			for (int j = 0; j < app.chosenList.size(); j++) {
				ImageInfo info = app.chosenList.get(j);
				boolean isNotHave = true;
				if (info != null) {
					if (info.typeMap == null) {
						isNotHave = true;
					}else {
						List<ProductInfo> products = info.typeMap.get(AppConstants.printType);
						for (ProductInfo productInfo : products) {
							if (productInfo != null && productInfo.num > 0) {
								isNotHave = false;
								break;
							}
						}
					}
				}
				if (isNotHave) {
					delList.add(info);
				}
			}
			app.chosenList.removeAll(delList);	
		}
	}
	
	private void dealWith(int position,String key){
		if (isDeal[position]) return;
		int pos = -1;	
		if (AppConstants.printType.equalsIgnoreCase(flowType)) {				
			pos = getPositionInList(app.chosenList, key);								
		}else if (AppConstants.bookType.equalsIgnoreCase(flowType)) {									
			pos = getPositionInList(PhotoBookProductUtil.getCurrentPhotoBook().chosenpics, key);					
		}else if (AppConstants.collageType.equalsIgnoreCase(flowType)) {									
			pos = getPositionInList(CollageUtil.getCurrentCollage().chosenpics, key);					
		}		
		if (pos != -1) {
			isChice[position] = true;
			if (!keyPMap.containsKey(position)) {
				keyPMap.put(position,key);
			}
		}
		isDeal[position] = true;
	}
	
	private int getPositionInList(List<ImageInfo> chosenpics,String key){
		int position =-1;
		if (chosenpics == null) return position;		
		for (int j = 0; j < chosenpics.size(); j++) {
			if (chosenpics.get(j).id.equals(key)) {
				position = j;
				break;
			}
		}				
		return position;
	}
	
	public void chiceSelectState(int position,String key) {		
		isChice[position] = true;
		isOldChice[position] = false;
		isDeal[position] = true;
		if (!keyPMap.containsKey(position)) {
			keyPMap.put(position,key);
		}
		this.notifyDataSetChanged();
	}

	public void chiceSelectState(int position) {
		isChice[position] = true;
		isOldChice[position] = false;
		isDeal[position] = true;
		notifyDataSetChanged();
	}

	public void chiceDeleteState(int position) {
		isChice[position] = false;
		isOldChice[position] = true;
		isDeal[position] = true;
		if (keyPMap.containsKey(position)) {
			keyPMap.remove(position);
		}	
		notifyDataSetChanged();
	}
	
	public void refreash(){
		if (AppConstants.collageType.equalsIgnoreCase(flowType)) {	
			int size = keyPMap.size();
			if (size > 0) {
				List<Integer> keyList = new ArrayList<Integer>();
				for (int i = 0; i < size; i++) {
					int position = keyPMap.keyAt(i);
					String key = keyPMap.valueAt(i);
					
					int pos = getPositionInList(CollageUtil.getCurrentCollage().chosenpics, key);
					if (pos != -1) {
						isChice[position] = true;								
					}else {
						isChice[position] = false;
						keyList.add(position);						
					}
				}
				if (keyList.size() > 0) {
					for (int position : keyList) {
						keyPMap.remove(position);
					}
				}				
			}			
		}
		this.notifyDataSetChanged();
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
	
	private Bitmap getBitmap(FbkPhoto fbkPhoto,int position,SelectImageView imageView) {	
		Bitmap result = null;
		if (fbkPhoto == null) return result;
		String key = fbkPhoto.ID;
		dealWith(position, key);
		result = MemoryCacheUtil.getBitmap(mMemoryCache,key);				
		if (result != null) return result;

		if (position == 0) {
			imageView.setTag(key);
			URI pictureURI = fbkPhoto.getThumbnailUri();
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
				URI pictureURI = fbkPhoto.getThumbnailUri();
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
		SelectImageView imageView = null;		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.grid_image_item, null);
			imageView = (SelectImageView) convertView.findViewById(R.id.photoContent);			
			convertView.setTag(imageView);
		}else {
			imageView = (SelectImageView) convertView.getTag();
		}			
		FbkPhoto fbkPhoto = (FbkPhoto) fbkImageList.get(position);
		Bitmap bitmap = getBitmap(fbkPhoto, position,imageView);		
		if (bitmap == null) {
			if (waitBitmap == null || waitBitmap.isRecycled()) {
				waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);
			}	
			bitmap = waitBitmap;	 
		}	
        imageView.setImageBitmap(bitmap,isChice[position],isOldChice[position],isDrawTran);	                         
        imageView.setLayoutParams(mLayoutParams); 
		return convertView;
	}

	@Override
	public void onProcess(Response response, String profileId, View imageView,int position,String flowTpye, String productId) {			
		if (response == null || imageDownloader == null) return;					
		if (response.getError() != null) {
			
		}else{			
			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);	
			if (bitmap != null && imageView != null) {
				if (imageView instanceof SelectImageView) {
					if (imageView.getTag().toString().equals(profileId)) {
						dealWith(position, profileId);
						if (imageView.getVisibility() == View.VISIBLE) {
							((SelectImageView)imageView).setImageBitmap(bitmap,isChice[position],isOldChice[position],isDrawTran);	
						}							
					}		
				} 										 
			}
		}			
	}	
	
	@Override
	public void cancelRequest(){		
		if (fbkImageList == null) return; 		
		int size = fbkImageList.size();
		if (size == 0) return; 
		for (int i = size - 1; i >= 0; i--) {
			FbkPhoto fbkPhoto = (FbkPhoto) fbkImageList.get(i);
			if (fbkPhoto == null) continue; 
			cancelServerRequest(fbkPhoto.ID);
		}
	}	

}
