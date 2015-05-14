package com.kodak.rss.tablet.util.load;

import android.content.Context;
import android.graphics.Bitmap;

import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.util.load.ImageDownloader.Downloader;
import com.kodak.rss.tablet.util.load.ImageDownloader.RequestKey;

public class ResolverReadWorkItem implements Runnable{

	private Context context;
	private RequestKey key;
    private int thumId;
	
	ResolverReadWorkItem(int thumId,Context context,RequestKey key) {
        this.context = context;
        this.key = key; 
        this.thumId = thumId;      
    }	
	
	@Override
	public void run() {
		readFromCache(key,thumId, context);		
	}
	
	private boolean isCancel(RequestKey key){
		boolean isCancelled = false;
		Downloader dealDownloader = ImageDownloader.getRequest(key);
	    if (dealDownloader != null && dealDownloader.isCancelled) {
	    	isCancelled = true;
	    }
		return isCancelled;
	}
	
	private void readFromCache(RequestKey key,int Id, Context context) {
		if (isCancel(key)) {
	    	Log.d("ReadWorkItem Cancel", "Id: "+ Id);
	    	ImageDownloader.issueResponse(key, null, null,true);
	    	return;
	    }
		
		 Bitmap bitmap = null;
		 try {	 
			 try {
				 bitmap = ImageUtil.getThumbnail(context.getContentResolver(), Id);
			} catch (OutOfMemoryError oom) {
				Log.e("ReadWorkItem", oom);
	    		bitmap = null;
	    		System.gc();
			}	    		
	    	ImageDownloader.issueResponse(key, null, bitmap,true);
		} catch (Exception e) {
			ImageDownloader.issueResponse(key, e, null,true);
		}			
	}	
}
