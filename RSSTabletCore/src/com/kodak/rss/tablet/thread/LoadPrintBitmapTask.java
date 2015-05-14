package com.kodak.rss.tablet.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.kodak.rss.core.util.ImageUtil;

public class LoadPrintBitmapTask extends Thread {
	
	private Handler handler;
	private String photoId;
	private String filePath;
	private double picCanvasHeight;	
	
	public static String PHOTOID = "photoId";
	public static String LANDSCAPE = "landscape";
	public static String WHRATIO = "wHratio";
	public static String BITMAP = "bitmap";	
	public static int FLAG = 1;	

	public LoadPrintBitmapTask(Handler handler, String photoId, String filePath, double picCanvasHeight) {
		super();	
		this.handler = handler;
		this.photoId = photoId;
		this.filePath = filePath;
		this.picCanvasHeight = picCanvasHeight;
	}

	public String getPhotoId() {
		return photoId;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setPicCanvasHeight(double picCanvasHeight) {
		this.picCanvasHeight = picCanvasHeight;
	}

	@Override
	public void run() {
		readUsePath(photoId,filePath,picCanvasHeight,handler);
	}

	private void readUsePath(String id,String filePath,double picCanvasHeight,Handler handler) {
		if (handler == null) return;
		if (id == null) return;		
		if (picCanvasHeight <= 0) return;
		Bitmap initBitmap = null;
		boolean landscape = true;
		double picCanvasWidth = 0;	
		double wHratio = 0;
		if (filePath != null) {			
			Bitmap mBitmap = null;			
			int downsample = 1;
			try {										
				int rotate = ImageUtil.getDegreesExifOrientation(filePath);  
				
				BitmapFactory.Options options = new Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(filePath, options);
				int origW = options.outWidth;
				int origH = options.outHeight;

				if (origH > 0) {
					wHratio = origW * 1.0 / origH;
					picCanvasWidth = picCanvasHeight * wHratio;
				}
				
				downsample = (int) Math.ceil((origH*1.0)/(picCanvasHeight*1.0));
				if (picCanvasWidth > 0) {
					int downsampleW = (int) Math.ceil((origW*1.0)/(picCanvasWidth*1.0));
					downsample = downsample > downsampleW ? downsample : downsampleW;
				}

				options.inJustDecodeBounds = false;
				options.inSampleSize = downsample;
				options.inPreferredConfig = Bitmap.Config.RGB_565; 
				mBitmap = BitmapFactory.decodeFile(filePath, options);
		
				if(rotate > 0 && mBitmap != null) { 					
					Bitmap rotateBitmap = ImageUtil.rotateBitmap(mBitmap,rotate);		             
		            if(rotateBitmap != null) {   
		            	mBitmap.recycle();   
		            	mBitmap = rotateBitmap;   
		             }   		             
				}
				if (mBitmap == null) return;
				int height = mBitmap.getHeight();
	            int width = mBitmap.getWidth();
	             
	             if (height > 0) {
					wHratio = width * 1.0 / height;
					picCanvasWidth = picCanvasHeight * wHratio;
				}

	            if (height > width) {
	            	landscape = false;
				}else {
					landscape = true;
				}			

				double scaleHeight = (picCanvasHeight * 1.0)/(height * 1.0);
				double scaleWidth = (picCanvasWidth * 1.0)/(width * 1.0);
				if (scaleHeight < scaleWidth) {
					initBitmap = Bitmap.createScaledBitmap(mBitmap,(int)(width*scaleHeight),(int)(height*scaleHeight),true);
				} else {
					initBitmap = Bitmap.createScaledBitmap(mBitmap,(int)(width*scaleWidth),(int)(height*scaleWidth),true);
				}
				mBitmap.recycle();

			} catch (OutOfMemoryError oom) {				
				initBitmap = null;
				System.gc();
			}
						
			Bundle data = new Bundle();
			data.putString(PHOTOID, id);
			data.putBoolean(LANDSCAPE, landscape);
			data.putDouble(WHRATIO, wHratio);			
			data.putParcelable(BITMAP, initBitmap);
			
			Message msg = new Message();
			msg.what = FLAG;
			msg.setData(data);
			handler.sendMessage(msg);						
		}
	}

}
