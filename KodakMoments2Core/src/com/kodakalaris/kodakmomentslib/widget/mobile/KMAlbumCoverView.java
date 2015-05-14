package com.kodakalaris.kodakmomentslib.widget.mobile;

import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * A simple imageView to show the image in the image list 
 * This image will fade out and a new image from the album will fade in every ~5 seconds.
 * @author sunny
 *
 */
public class KMAlbumCoverView extends ImageView{
	private List<PhotoInfo> photos;
	private Handler handler = new Handler();
	private Runnable showPhotoRunnable ; 
	private DisplayImageOptions options;
	public KMAlbumCoverView(Context context) {
		this(context, null);
	}
	public KMAlbumCoverView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public KMAlbumCoverView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init(){
		 options = new DisplayImageOptions.Builder()
		  .cacheInMemory(true)
		  .cacheOnDisk(true)
		  .considerExifParams(true)
		  .bitmapConfig(Bitmap.Config.RGB_565)
		  .displayer(new FadeInBitmapDisplayer(1000))
		  .imageScaleType(ImageScaleType.EXACTLY)
		  .build();
	}
	
    public void startShowCover(){
    	if(photos.size()==1){
    		PhotoInfo photoInfo = photos.get(0);
    		ImageLoader.getInstance().displayImage("file://"+photoInfo.getPhotoPath(), this,options);
    	}else{
    		
    		 if(showPhotoRunnable==null){
    			 showPhotoRunnable = new Runnable() {
    					
    					@Override
    					public void run() {
    						int pos = new Random().nextInt(photos.size());
    						ImageLoader.getInstance().displayImage( "file://"+photos.get(pos).getPhotoPath(), KMAlbumCoverView.this,options);
    						handler.postDelayed(this,5000); 
    					}
    				}; 
    		 }
    		 handler.removeCallbacks(showPhotoRunnable);
    		 handler.post(showPhotoRunnable);
    		
    	};
    	
    }
    
    
    
	public List<PhotoInfo> getPhotos() {
		return photos;
	}
	public void setPhotos(List<PhotoInfo> photos) {
		this.photos = photos;
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		handler.removeCallbacks(showPhotoRunnable);
	}
	

}
