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

import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.view.SelectImageView;

public class CollageProductThemesAdapter extends CollageProductAdapter{
		
	private Collage currentCollage;
	public List<Theme> themeList;
	
	public String oldCheckThemeId;	
		
	public CollageProductThemesAdapter(Context context,List<Theme> themeList,float ratio,LruCache<String, Bitmap> mMemoryCache) {
		super(context,ratio,mMemoryCache);						
		this.imageDownloader.setIsThumbnail(true);
		if (mLayoutParams != null) {
			this.imageDownloader.setViewParameters(mLayoutParams.width, mLayoutParams.height);
		}	
		currentCollage = CollageUtil.getCurrentCollage();
		itemSize = 0;
		this.themeList = themeList;
		if (themeList != null) {
			itemSize = themeList.size();
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
					
		if (end_index > itemSize - 1) {
			end_index = itemSize - 1;
		}
		Theme theme =  themeList.get(position);
		if (theme == null) {
			imageView.setTag(position);	
			imageView.setImageBitmap(getWaitBitmap(),false); 
		}else {
			String themeId = theme.id;
			Bitmap mBitmap = MemoryCacheUtil.getBitmap(mMemoryCache, themeId);	
			imageView.setTag(themeId);	
			boolean isCheck = CollageUtil.getThemeCheckState(themeId);
			imageView.setImageBitmap((mBitmap == null ? getWaitBitmap() : mBitmap),isCheck); 						
			if (((start_index > 0 && end_index > 0) && start_index <= position && end_index >= position) ||(start_index == 0 && end_index >= 0)) {					
				if (!lock && mBitmap == null) {									
					URI pictureURI = null ;			
					try {
						pictureURI = new URI(theme.glyph);
			    	} catch (URISyntaxException e) {
			    		pictureURI = null;
			    	}   	       	
			    	if (pictureURI != null) { 							
						imageDownloader.downloadProfilePicture(themeId, pictureURI, imageView,position,true, currentCollage.id);	
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
		if (currentCollage == null) return;	
		if (productId != null && !productId.equals(currentCollage.id)) return;							
		if (response.getError() != null) {
			
		}else{
			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);		
			if (bitmap != null && view != null) {
				if (view instanceof SelectImageView ) {
					if (profileId.equals(view.getTag().toString())) {
						if (view.getVisibility() == View.VISIBLE) {
							boolean isCheck = CollageUtil.getThemeCheckState(profileId);
							((SelectImageView) view).setImageBitmap(bitmap,isCheck);
						}							
					}else {
						notifyDataSetChanged();
					}						
				} 
			}	
		}				
	}

	public void cancelRequest(){
		if (themeList == null) return;				
		for (int j = 0; j < themeList.size(); j++) {
			Theme theme = themeList.get(j);       
			dealRequest(theme, true);
		}		
	}
	
	private void dealRequest(Theme theme,boolean isCancel){
		if (theme == null) return;
		if (currentCollage == null) return;
		if (imageDownloader == null) return;
		if (pendingRequests == null) return;	
		if (isCancel) {
			imageDownloader.cancelRequest(theme.id, currentCollage.id, 0);
		}else {
			imageDownloader.prioritizeRequest(theme.id, currentCollage.id, 0);
		}
	}
		
}
