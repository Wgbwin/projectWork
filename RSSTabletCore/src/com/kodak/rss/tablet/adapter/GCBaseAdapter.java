package com.kodak.rss.tablet.adapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;

public abstract class GCBaseAdapter extends BaseAdapter implements onProcessImageResponseListener{

	public Context mContext;
	public Bitmap waitBitmap;		
	public ImageUseURIDownloader imageDownloader;			
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();
	public LruCache<String, Bitmap> mMemoryCache;  	
	public DisplayMetrics dm;
	
	public GCBaseAdapter(Context mContext,LruCache<String, Bitmap> mMemoryCache) {
		this.mContext = mContext;
		this.mMemoryCache = mMemoryCache;	
		this.dm = mContext.getResources().getDisplayMetrics();
		this.waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);       		
	}

	public Bitmap getBitmap(String Id,URI pictureURI,View view,int position){
		if (waitBitmap ==null || waitBitmap.isRecycled()) {
    		waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);
 		}		
    	Bitmap mBitmap = MemoryCacheUtil.getBitmap(mMemoryCache,Id);         	        
        if (mBitmap == null) {  
        	if (imageDownloader == null) {
        		imageDownloader = new ImageUseURIDownloader(mContext, pendingRequests, this);
        	}        				
			view.setTag(Id);
			imageDownloader.downloadProfilePicture(Id, pictureURI, view,position,true);   								  	 						       	  	   	    	        	
		}
        if (mBitmap == null) {        	
        	mBitmap = waitBitmap;
		}
        return mBitmap;
    }
	
	public URI getUri(String url){
		URI pictureURI = null;
		if (url == null) return pictureURI;
		if ("".equals(url)) return pictureURI;	
		url= url.replaceAll(" ", "%20");
		try {
			pictureURI = new URI(url);
		} catch (URISyntaxException e) {
			pictureURI = null;
		}
		return pictureURI;
	}
	
	public String getId(String name){
		if (name == null) return "";		
		String id = name.replaceAll(" ", "");
		id = id.replaceAll("/", "");
		return id;
	}
	
	@Override
	public void onProcess(Response response, String profileId, View view,int position, String flowType, String productId) {
		if (response == null || imageDownloader == null) return;									
		if (response.getError() != null) {
			
		}else{			
			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);				
			if (bitmap != null && view != null) {
				if (view instanceof ImageView) {
					if (view.getVisibility() == View.VISIBLE) {
						if (view.getTag().toString().equals(profileId)) {							
							((ImageView)view).setImageBitmap(bitmap);						
						}else {
							notifyDataSetChanged();
						}
					}
				}
			}					
		}								
	}	
	
}
