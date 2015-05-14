package com.kodak.rss.tablet.util.load;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.util.load.ImageDownloader.Downloader;
import com.kodak.rss.tablet.util.load.ImageDownloader.RequestKey;

public class ResolverPhotoWorkItem implements Runnable{
	
    private RequestKey key; 
    private String filePath;   
    private int[] viewParameters;

    ResolverPhotoWorkItem(RequestKey key,String filePath,int[] viewParameters) {    
        this.key = key;                        
        this.filePath = filePath;
        this.viewParameters = viewParameters;
    }

    @Override
    public void run() {
        readFromCache(key,filePath);
    }
    
    private boolean isCancel(RequestKey key){
		boolean isCancelled = false;
		Downloader dealDownloader = ImageDownloader.getRequest(key);
	    if (dealDownloader != null && dealDownloader.isCancelled) {
	    	isCancelled = true;
	    }
		return isCancelled;
	}
    
    private void readFromCache(RequestKey key, String filePath) {
    	if (isCancel(key)) {
	    	Log.d("ResolverPhotoWorkItem Cancel", "filePath: "+ filePath);
	    	ImageDownloader.issueResponse(key, null, null,true);
	    	return;
	    }
    	
    	try {   		
    	    if (filePath != null ) {
    	    	Bitmap origBitmap = null;	
    	    	int downsample = 1;	 
    	    	try {	    	    	
	    	        BitmapFactory.Options options = new Options();	    	    	
	    	        options.inJustDecodeBounds = true;				
		    	    BitmapFactory.decodeFile(filePath, options);
		    	    int origW = options.outWidth;
		    	    int origH = options.outHeight;

				    int downsampleW = 1;	
				    if(origH > viewParameters[1] && viewParameters[1] > 0){		    				
				    	downsample = (int) Math.ceil((origH * 1.0)/ viewParameters[1]);		    						
			    	}					    	    
				    if(origW > viewParameters[0] && viewParameters[0] > 0){								
				    	downsampleW = (int) Math.ceil((origW * 1.0)/ viewParameters[0]);
				    	downsample = downsample > downsampleW ? downsample : downsampleW;
			    	}

		    		options.inJustDecodeBounds = false;
		    		options.inSampleSize = downsample;
		    		options.inPreferredConfig = Bitmap.Config.RGB_565;   
		    		origBitmap = BitmapFactory.decodeFile(filePath, options);
		    		
		    		int rotate = ImageUtil.getDegreesExifOrientation(filePath);  
		    		if(rotate > 0 && origBitmap != null) {   
		    			Bitmap rotateBitmap = ImageUtil.rotateBitmap(origBitmap,rotate);     
		                if(rotateBitmap != null) {   
		                	origBitmap.recycle();   
		                	origBitmap = rotateBitmap;   
		                }             
		    		}
		    		
    	    	}catch (OutOfMemoryError oom) {
    	    		Log.e("readFromCache", oom);
    	    		origBitmap = null;
    	    		System.gc();
				}
    	    	ImageDownloader.issueResponse(key, null, origBitmap,true);
    		}	    	
		} catch (Exception e) {
			ImageDownloader.issueResponse(key, e, null,true);
		}			
    }
   
}
