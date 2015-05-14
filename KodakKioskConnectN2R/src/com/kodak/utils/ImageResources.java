package com.kodak.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.WeakHashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

public final class ImageResources {
	
	protected static final String TAG = "ImageResource:";	
	private static final String TEMP_FOLDER = "/temp/.colorEffect";
	public final static String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + TEMP_FOLDER;
	private WeakHashMap<String, WeakReference<Bitmap>> mBitmaps;
	private WeakHashMap<String, WeakReference<Drawable>> mDrawables;
	
	public ImageResources() {
		mBitmaps = new WeakHashMap<String, WeakReference<Bitmap>>();
		mDrawables = new WeakHashMap<String, WeakReference<Drawable>>();			
	}

	public static Bitmap getBitmap(String imgName) {
		return BitmapFactory.decodeFile(tempFolder +File.separator+imgName);
	}
	
	public static Drawable getDrawable(String imgName) {
		return BitmapDrawable.createFromPath(tempFolder +File.separator+imgName);
	}

	public static boolean downloadPic(String imgName,String urlpath) {
		boolean downloadSuccessFlag = true;		
		File baseDir = new File(tempFolder);
		if (!baseDir.exists()) {
			baseDir.mkdirs();
		}
		imgName = imgName+".jpg";
		File picFile = new File(tempFolder, imgName);
		if (picFile.exists()) return downloadSuccessFlag;

		int fileLength = 0;
		InputStream inputStream = null;
		File localResourceFile = null;
		FileOutputStream fileOutputStream = null;		
		try {			
			URL url = new URL(urlpath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(6 * 1000);
			if (conn.getResponseCode() == 200) {							
				localResourceFile = new File(tempFolder, imgName);
				fileOutputStream = new FileOutputStream(localResourceFile);
							
				inputStream = conn.getInputStream();
				fileLength = (int) conn.getContentLength();
				
				int byteReadTotal = 0;
				byte[] buffer = new byte[1024];			
				int bytesRead = -1;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, bytesRead);				
					byteReadTotal +=bytesRead;								
				}
				fileOutputStream.flush();
				if(Math.abs(fileLength - byteReadTotal)>= 512) {
					downloadSuccessFlag = false;
					localResourceFile.delete();
				}
			  }
			} catch (Exception e) {				
				if(localResourceFile != null){
					localResourceFile.delete();
				}
				downloadSuccessFlag = false;
			} finally {
				try {
					if (fileOutputStream != null)
						fileOutputStream.close();
					if (inputStream != null)
						inputStream.close();
				} catch (IOException ex) {					
					if(localResourceFile != null){
						localResourceFile.delete();
					}
					downloadSuccessFlag = false;
				}
			}					
		return downloadSuccessFlag;		
	}

	public static void deletePic(String imgName) {
		File baseDir = new File(tempFolder);
		imgName = imgName+".jpg";
		if (baseDir.exists()) {
			File localResourceFile = new File(tempFolder, imgName);
			if (localResourceFile.exists()) {
				localResourceFile.delete();
			}			
		}		
	}

	public Bitmap getCacheBitmap(String imgName) {
		imgName = imgName+".jpg";
		if (mBitmaps.containsKey(imgName)) {
			WeakReference<Bitmap> softReference = mBitmaps.get(imgName);
			Bitmap drawable = softReference.get();
			if (drawable != null && !drawable.isRecycled()) {
				return drawable;
			}
		}
		Bitmap sd_img = getBitmap(imgName);
		if (sd_img != null && !sd_img.isRecycled()) { 
			mBitmaps.put(imgName, new WeakReference<Bitmap>(sd_img));
		}
		return sd_img;
	}

	public Drawable getCacheDrawable(String imgFileName) {
		if (mDrawables.containsKey(imgFileName)) {
			WeakReference<Drawable> softReference = mDrawables.get(imgFileName);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}

		Drawable drawImg = getDrawable(imgFileName);
		if (drawImg != null) {
			mDrawables.put(imgFileName, new WeakReference<Drawable>(drawImg));
		}
		return drawImg;
	}

	private static boolean isImageFile(String fileName) {
		fileName = fileName.toLowerCase();
		return fileName.endsWith("png") || fileName.endsWith("jpg") || fileName.endsWith("jpeg")
			|| fileName.endsWith("gif") || fileName.equals("bmp");
	}

}
