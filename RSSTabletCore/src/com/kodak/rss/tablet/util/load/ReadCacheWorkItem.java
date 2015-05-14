package com.kodak.rss.tablet.util.load;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.util.load.ImageDownloader.Downloader;
import com.kodak.rss.tablet.util.load.ImageDownloader.RequestKey;

public class ReadCacheWorkItem implements Runnable{
	private Context context;
    private RequestKey key;
    private boolean allowCached;
    private String profileId;
    private String saveType;  
    private boolean isThumbnail;
    private int[] viewParameters;
    private int refreshCount;   
    private String tempFolder;   

    ReadCacheWorkItem(String profileId,Context context, RequestKey key, boolean allowCached,String saveType,int refreshCount,boolean isThumbnail, int[] viewParameters) {
        this.context = context;
        this.key = key;
        this.allowCached = allowCached;
        this.profileId = profileId;
        this.saveType = saveType;      
        this.isThumbnail = isThumbnail;        
        this.viewParameters = viewParameters;
        this.refreshCount = refreshCount;  
        
        tempFolder =  FilePathConstant.tempSaveFolder + saveType;
    }

    @Override
    public void run() {
        readFromCache(key, context, allowCached, refreshCount,viewParameters);
    }
    
    private boolean isCancel(RequestKey key){
		boolean isCancelled = false;
		Downloader dealDownloader = ImageDownloader.getRequest(key);
	    if (dealDownloader != null && dealDownloader.isCancelled) {
	    	isCancelled = true;
	    }
		return isCancelled;
	}
    
    private void readFromCache(RequestKey key, Context context, boolean allowCached ,int refreshCount, int[] viewParameters) {
    	try {
    		File baseDir = new File(tempFolder);
    		if (!baseDir.exists()) {
    			baseDir.mkdirs();
    		}
    		String filePath = null;
    		if (allowCached) {
    			filePath = FilePathConstant.getLoadFilePath(saveType, profileId, isThumbnail, refreshCount, 0);
			}
    		
    		if (isCancel(key)) {
    	    	Log.d("ReadCacheWorkItem Cancel", "keyId: "+ key.uri);
    	    	ImageDownloader.issueResponse(key, null, null, true);
    	    	return;
    	    }
    		
    	    if (filePath != null ) {
    	    	Bitmap bitmap = null;	
    	    	int sampleSize = 1;	 
    	    	try {	    	    	
	    	        BitmapFactory.Options opts = new Options();
	    	    	if (!isThumbnail && (viewParameters != null && viewParameters[0] > 1 && viewParameters[1] > 1)) {
		    	    	opts.inJustDecodeBounds = true;				
		    	    	BitmapFactory.decodeFile(filePath, opts);
		    	    	int origW = opts.outWidth;
		    	    	int origH = opts.outHeight;
		    	    			        				
		    	    	if (origW > origH) {
		    	    		sampleSize = opts.outHeight/viewParameters[1];	    		
		    	    	}else {
		    	    		sampleSize = opts.outWidth/viewParameters[0];
		    	    	}
		    	    	opts.inJustDecodeBounds = false;
		    	    	opts.inSampleSize = sampleSize;	  
		    	    	opts.inPreferredConfig = Bitmap.Config.RGB_565;
					}
	    	    	bitmap = BitmapFactory.decodeFile(filePath, opts);
    	    	}catch (OutOfMemoryError oom) {
    	    		Log.e("readFromCache", oom);
    	    		bitmap = null;
    	    		System.gc();
				}
    	    	ImageDownloader.issueResponse(key, null, bitmap, true);
    		}else {           
    	        Downloader downloader = ImageDownloader.removePendingRequest(key);
    	        if (downloader != null && !downloader.isCancelled) {
    	        	ImageDownloader.enqueueDownload(profileId,downloader.request, key, allowCached,saveType,isThumbnail,viewParameters);
    	        }
    	     }					    	
		} catch (Exception e) {
			ImageDownloader.issueResponse(key, null, null, true);
		}			
    }
   
}
