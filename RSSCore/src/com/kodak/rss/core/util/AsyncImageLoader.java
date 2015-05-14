package com.kodak.rss.core.util;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.kodak.rss.RssApp;

public class AsyncImageLoader {
	private ExecutorService threadPool;
	private HashMap<String, SoftReference<Bitmap>> cache;
	private Config config;
	private WeakReference<Context> contextRef;
	private Hashtable<String, LoadImageTask> mapDownloading;
	
	public AsyncImageLoader(Context context){
		this(context,new Config());
	}
	
	public AsyncImageLoader(Context context,Config config){
		contextRef = new WeakReference<Context>(context);
		this.config = config;
		cache = new HashMap<String, SoftReference<Bitmap>>();
		threadPool = Executors.newFixedThreadPool(config.maxThreadSize);
		mapDownloading = new Hashtable<String, AsyncImageLoader.LoadImageTask>();
	}
	
	public interface ImageDownloaderCallBack{
		void OnImageDownloaded(View view, Bitmap bitmap);
	}
	
	public static class Config{
		private String cacheFolderPath;
		private int maxThreadSize;
		public Config(){
			cacheFolderPath = RssApp.getInstance().getTempFolderPath();
			maxThreadSize = 5;
		}
		
		public Config setCacheFolderPath(String cacheFolderPath) {
			this.cacheFolderPath = cacheFolderPath;
			return this;
		}
		
		public Config setMaxThreadSize(int maxThreadSize) {
			this.maxThreadSize = maxThreadSize;
			return this;
		}
	}
	
	private class LoadImageTask implements Runnable{
		private View view;
		private String url;
		private Vector<ImageDownloaderCallBack> callbacks;
		private ImageDownloaderCallBack callback;
		
		public LoadImageTask(final View view,String url,ImageDownloaderCallBack callback){
			this.url = url;
			this.view = view;
			this.callback = callback;
			callbacks = new Vector<AsyncImageLoader.ImageDownloaderCallBack>();
			callbacks.add(callback);
		}
		
		public void addCallBack(ImageDownloaderCallBack callback){
			callbacks.add(callback);
		}
		
		@Override
		public void run() {
			Bitmap bitmap = getBitmapFromCache(url);
			if(bitmap == null){
				boolean isDownloading = false;
				synchronized (mapDownloading) {
					if(mapDownloading.contains(url)){
						isDownloading = true;
						LoadImageTask task = mapDownloading.get(url);
						task.addCallBack(callback);
						return;
					}
				}
				
				if(!isDownloading){
					synchronized (mapDownloading) {
						mapDownloading.put(url, this);
					}
					String path = getFilePathFromKey(url);
					if(FileDownloader.download(url, path)){
						mapDownloading.remove(url);
						bitmap = BitmapFactory.decodeFile(path);
						if(bitmap != null)
							putInCache(url, bitmap);
					}
				}
			}
			
			if(bitmap != null && view!=null && contextRef.get()!=null){
				final Bitmap resultBitmap = bitmap;
				view.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						for(ImageDownloaderCallBack cb : callbacks){
							cb.OnImageDownloaded(view, resultBitmap);
						}
					}
				}, 100);
			}
		}
		
	}
	
	public void loadImageAsync(final View view,final String url,final ImageDownloaderCallBack imageDownloaderCallBack){
		threadPool.execute(new LoadImageTask(view, url, imageDownloaderCallBack));
	}
	
	public Bitmap getBitmapFromCache(String key){
		Bitmap bitmap = null;
		if(cache.containsKey(key)){
			bitmap = cache.get(key).get();
		}
		
		if(bitmap == null){
			String path = getFilePathFromKey(key);
			if(new File(path).exists()){
				bitmap = BitmapFactory.decodeFile(path);
			}
			
			if(bitmap != null){
				putInCache(key, bitmap);
			}
		}
		
		return bitmap;
	}
	
	private String getFilePathFromKey(String key){
		return config.cacheFolderPath + "/"+ EncryptUtil.stringToMd5(key);
	}
	
	private synchronized void putInCache(String key,Bitmap bitmap){
		cache.put(key, new SoftReference<Bitmap>(bitmap));
	}
	
}
