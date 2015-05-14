package com.kodak.rss.tablet.util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class MemoryCacheUtil {
	
	public static LruCache<String, Bitmap> generMemoryCache(int ratio) {
		if (ratio <= 1) return null;
		LruCache<String, Bitmap> mMemoryCache = null;
		int maxMemory = (int) Runtime.getRuntime().maxMemory();  
        int cacheSize = maxMemory / ratio;       
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {  
            @Override  
            protected int sizeOf(String key, Bitmap bitmap) {  
            	if (bitmap != null) {
            		int size = bitmap.getByteCount();
            		if (size == 0) {
            			size = bitmap.getRowBytes() * bitmap.getHeight();
					}
            		return size;
				}else {
					return 0;
				}
            }

//			@Override
//			protected void entryRemoved(boolean evicted, String key,Bitmap oldValue, Bitmap newValue) {				
//				if(evicted && oldValue != null && !oldValue.isRecycled()) {  
//                    oldValue.recycle(); 
//                    oldValue = null;  
//                } else {
//					super.entryRemoved(evicted, key, oldValue, newValue);
//				} 
//			}            
        };               		
		return mMemoryCache;
	}
	
	public static void evictAll(LruCache<String, Bitmap> mMemoryCache){
		if (mMemoryCache == null) return;		
		mMemoryCache.evictAll();								
	}	
	
	public static void putBitmap(LruCache<String, Bitmap> mMemoryCache,String Id,Bitmap bitmap){
		if (mMemoryCache == null) return;
		if (Id == null) return;
		if ("".equals(Id)) return;
		if (bitmap == null) return;		
		mMemoryCache.put(Id, bitmap);								
	}
	
	public static void removeBitmap(LruCache<String, Bitmap> mMemoryCache,String Id){
		if (mMemoryCache == null) return;
		if (Id == null) return;
		if ("".equals(Id)) return;			
		mMemoryCache.remove(Id);					
	}
	
	public static Bitmap getBitmap(LruCache<String, Bitmap> mMemoryCache,String Id){
		Bitmap mBitmap = null;
		if (mMemoryCache == null) return null;
		if (Id == null) return null;
		if ("".equals(Id)) return null;		
		mBitmap = mMemoryCache.get(Id);					
		return mBitmap;
	}
	
}
