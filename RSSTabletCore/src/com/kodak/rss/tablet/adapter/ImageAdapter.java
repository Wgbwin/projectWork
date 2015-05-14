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

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.JudgeImageFileTypeUtil;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.PhotoBookPicSelectMoreActivity;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.SelectImageView;

public class ImageAdapter extends CanZoomBaseAdapter implements onProcessImageResponseListener{
	
	private LayoutInflater mInflater;	
	
	public SortableHashMap<Integer, String[]> imageBuckets;		
	private int size;
	public boolean isChice[];
	public boolean isOldChice[];
	public boolean isDeal[];
	RssTabletApp app;	
	private Bitmap waitBitmap;			
	public List<Integer> dirtyList;
	public List<Integer> goodList;
	public SortableHashMap<Integer, String> keyPMap;
		
	private SortableHashMap<Integer, String> SelectImages;
	private String flowType;
	
	private boolean isDrawTran;
	
	public ImageAdapter(Context context,SortableHashMap<Integer, String[]> imageBuckets,SortableHashMap<Integer, String> SelectImages,String flowType,LruCache<String, Bitmap> mMemoryCache) {
		super(context,mMemoryCache);			
		app = RssTabletApp.getInstance();
		this.imageBuckets = imageBuckets;
		this.SelectImages = SelectImages;
		this.flowType = flowType;
		
		mInflater = LayoutInflater.from(context);	
		waitBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.imagewait60x60);		
		pendingRequests = new HashMap<String, Request>();
		imageDownloader = new ImageUseURIDownloader(context, pendingRequests,this);
		
		size = imageBuckets.size();
		isChice = new boolean[size];
		isOldChice = new boolean[size];
		keyPMap = new SortableHashMap<Integer, String>();
		for (int i = 0; i < size; i++) {
			isOldChice[i] = true;
		}
		isDeal = new boolean[size];
		
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
	
	private void dealWith(int position,int key){
		if (isDeal[position]) return;
		int pos = -1;
		if (SelectImages != null) {
			pos = getPositionInList(SelectImages, key);			
		}else {
			if (AppConstants.printType.equalsIgnoreCase(flowType)) {				
				pos = getPositionInList(app.chosenList, key);								
			}else if (AppConstants.bookType.equalsIgnoreCase(flowType)) {									
				pos = getPositionInList(PhotoBookProductUtil.getCurrentPhotoBook().chosenpics, key);					
			}else if (AppConstants.collageType.equalsIgnoreCase(flowType)) {									
				pos = getPositionInList(CollageUtil.getCurrentCollage().chosenpics, key);					
			}
		}
		if (pos != -1) {			
			isChice[position] = true;
			if (!keyPMap.containsKey(position)) {
				keyPMap.put(position,String.valueOf(key));
			}
		}
		isDeal[position] = true;		
	}

	private int getPositionInList(SortableHashMap<Integer, String> SelectImages,int key){
		int position =-1;
		if (SelectImages == null) return position;		
		for (int j = 0; j < SelectImages.size(); j++) {
			if (SelectImages.keyAt(j) - key == 0) {
				position = j;
				break;
			}
		}				
		return position;
	}

	private int getPositionInList(List<ImageInfo> chosenpics,int id){
		int position =-1;
		if (chosenpics == null) return position;		
		for (int j = 0; j < chosenpics.size(); j++) {
			if (chosenpics.get(j).id.equals(String.valueOf(id))) {
				position = j;
				break;
			}
		}				
		return position;
	}
	
	private int getPositionInList(List<ImageInfo> chosenpics, String id){
		int position =-1;
		if (chosenpics == null) return position;		
		for (int j = 0; j < chosenpics.size(); j++) {
			if (chosenpics.get(j).id.equals(id)) {
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
	
	public void chiceSelectState(int position) {		
		isChice[position] = true;
		isOldChice[position] = false;
		isDeal[position] = true;		
		this.notifyDataSetChanged();
	}

	public void chiceDeleteState(int position) {
		isChice[position] = false;
		isOldChice[position] = true;
		isDeal[position] = true;
		if (keyPMap.containsKey(position)) {
			keyPMap.remove(position);
		}		
		this.notifyDataSetChanged();
	}

	private boolean isWebp(int id, int position){
		boolean isWebp = false;
		if (imageBuckets == null)return isWebp;
		if (!(goodList != null && goodList.contains(id))){
			String[] filePath = imageBuckets.valueAt(position);	
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
		
	private Bitmap getBitmap(int position,SelectImageView imageView) {		
		Bitmap result = null;		
		int id = imageBuckets.keyAt(position);	
		if (id <= 0) return result;
		if (dirtyList != null && dirtyList.contains(id)) return result;
		
		String key = String.valueOf(id);		
		result = MemoryCacheUtil.getBitmap(mMemoryCache,key);			
		if (result != null) return result;
				
		if (position == 0) {			
			if (isWebp(id, position)) return result;
			result = ImageUtil.getThumbnail(mContext.getContentResolver(), id);
			if (result != null) {
				MemoryCacheUtil.putBitmap(mMemoryCache, key, result);				
			}else {
				if (dirtyList == null) {
					dirtyList = new ArrayList<Integer>(2);
				}
				dirtyList.add(id);
			}			
			return result;
		}else {			
			if (end_index > size-1) {
				end_index = size-1;
			}			
			if (((start_index > 0 && end_index > 0)&&start_index <= position && end_index >= position)||(start_index == 0 && end_index >= 0)) {								
				if (lock) return result;
				if (isWebp(id, position)) return result;
				imageView.setTag(key);				
				imageDownloader.downloadProfilePicture(key,null,imageView, position);									
			}
		}
		return result;
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
		SelectImageView imageView = null;		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.grid_image_item, null);
			imageView = (SelectImageView) convertView.findViewById(R.id.photoContent);			
			convertView.setTag(imageView);
		}else {
			imageView = (SelectImageView) convertView.getTag();
		}	
		int id = imageBuckets.keyAt(position);	
		Bitmap bitmap = getBitmap(position,imageView);
		if (bitmap == null) {
			if (waitBitmap ==null || waitBitmap.isRecycled()) {
				waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);
			}
			if (dirtyList != null && dirtyList.contains(id)) {
				imageView.setDirty(waitBitmap);	
			}else {
				dealWith(position, id);
				imageView.setImageBitmap(waitBitmap,isChice[position],isOldChice[position],isDrawTran);
			}
		}else {
			dealWith(position, id);
			imageView.setImageBitmap(bitmap,isChice[position],isOldChice[position],isDrawTran);	
		}											
		imageView.setLayoutParams(mLayoutParams);
		return convertView;
	}

	@Override
	public void onProcess(Response response, String profileId, View view,int position, String flowType, String productId) {		
		if (response == null || pendingRequests == null) return;
		int Id;
		try {
			Id = Integer.valueOf(profileId);
		} catch (Exception e) {
			 return;	
		}
			
		if (response.getError() != null) {
			
		}else{			
			Bitmap bitmap = response.getBitmap();				
			if (bitmap != null) {				
				MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);					
				if (view != null) {
					if (view instanceof SelectImageView) {
						if (view.getTag().toString().equals(profileId)) {
							dealWith(position, Id);
							if (view.getVisibility() == View.VISIBLE) {
								((SelectImageView)view).setImageBitmap(bitmap,isChice[position],isOldChice[position],isDrawTran);	
							}							
						}		
					}
				}	 
			}else {
				if (dirtyList == null) {
					dirtyList = new ArrayList<Integer>(2);
				}
				dirtyList.add(Id);
				if (waitBitmap ==null || waitBitmap.isRecycled()) {
					waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);
				}
				if (view != null) {
					if (view instanceof SelectImageView) {
						if (view.getTag().toString().equals(profileId)) {
							isDeal[position] = true;
							if (view.getVisibility() == View.VISIBLE) {
								((SelectImageView)view).setDirty(waitBitmap);	
							}							
						}		
					}
				}	 							
			}
		}			
	}
	
	@Override
	public void cancelRequest(){		
		if (imageBuckets == null) return;
		int size = imageBuckets.size();
		if (size == 0) return;		
		for (int i = size-1; i >= 0; i--) {
			int thumId = imageBuckets.keyAt(i);				
			cancelNativeRequest(thumId);
		}	
	}
	
}

