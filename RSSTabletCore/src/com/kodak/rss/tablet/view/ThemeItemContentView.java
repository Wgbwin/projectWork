package com.kodak.rss.tablet.view;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.bean.content.Theme.BackGround;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.tablet.adapter.PhotobookThemeSelectionAdapter;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;

/**
 * Purpose: 
 * Author: Bing Wang 
 */
public class ThemeItemContentView extends View {

	private Context mContext;	
	public int canvasHeight;
	public int canvasWidth;
	public int photoHeight;
	public int photoWidth;
	private PhotobookThemeSelectionAdapter adapter;
	private Photobook currentPhotoBook = null;
	private Theme theme;
	private int position;
	
    public ThemeItemContentView(Context context) {
		super(context);
		init(context);
	}

    public ThemeItemContentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}  
    
    private void init(Context context){
    	this.mContext = context;    
    	currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
    }
    
    public void setPagesPhotos(Theme theme,PhotobookThemeSelectionAdapter mAdapter,int position){
    	this.theme = theme;
    	this.adapter = mAdapter;
    	this.position = position;
    }   
    
    @SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {    	
    	canvasHeight = getHeight();
    	canvasWidth = getWidth();
    	Bitmap mBitmap = null;
    	if (theme == null) {
    		ArrayList<ImageInfo> mPagesPics = currentPhotoBook.chosenpics;
    		if (mPagesPics != null) {
    			int max = 3;
    			if (mPagesPics.size() < 4 ) {
    				max = mPagesPics.size()-1;
            	}   			
    			for (int i = max; i >= 0; i--) {
    	    		ImageInfo info = mPagesPics.get(i);    	    		
    				if (info.isfromNative) {
    					mBitmap  = ImageUtil.getThumbnail(mContext.getContentResolver(),Integer.valueOf(info.id));
    				}else {
    					if (info.thumbnailUrl != null) {   						
        					mBitmap = BitmapFactory.decodeFile(info.thumbnailUrl);
						}   					
    				}		 	    
    	    		if (mBitmap != null) {	    			
    	                float ratio =  i*1f/6;
    	        		int left = (int) (canvasWidth*ratio);
    	        		int top = (int) (canvasHeight*(1f/2 - ratio));
    	        		canvas.drawBitmap(mBitmap, left, top,null);
    				}   	
    			}
			}
		}else {	
			BackGround[] backGrounds = theme.backGrounds;			
			if (backGrounds == null || theme.backGrounds.length < 1) return;  	
			int length = backGrounds.length;			
			URI pictureURI = null ;
			if (length >= 4 ) {					
    	    	for (int i = 3; i >= 0; i--) {   	    		   	    		
    	    		try {
    	    			pictureURI = new URI(backGrounds[i].glyphURL);
    	    		} catch (URISyntaxException e) {
    	    			pictureURI = null;
    	    		}
    	    		if (pictureURI != null) { 
    	    			String backGroudId = backGrounds[i].id;
    	    			mBitmap = adapter.getBitmap(backGroudId, pictureURI, ThemeItemContentView.this, position);    
    	    	        drawContent(canvas, mBitmap,i);
    	    	    }		      		 	
    			}   	    	
        	}else if (length > 1) {
	    		for (int i = length; i > 0; i--) {
	    			try {
    	    			pictureURI = new URI(backGrounds[i-1].glyphURL);
    	    		} catch (URISyntaxException e) {
    	    			pictureURI = null;
    	    		}   	    		
    	    		if (pictureURI != null) {    	    			   	    			
    	    			String backGroudId = backGrounds[i-1].id;
    	    			mBitmap = adapter.getBitmap(backGroudId, pictureURI, ThemeItemContentView.this, position);    
    	    	        drawContent(canvas, mBitmap,i-1);
    	    	    }		      		
				}	    		
			}else {			
				try {
	    			pictureURI = new URI(backGrounds[0].glyphURL);
	    		} catch (URISyntaxException e) {
	    			pictureURI = null;
	    		}   	    		
	    		if (pictureURI != null) {
	    			String backGroudId = backGrounds[0].id;
	    			mBitmap = adapter.getBitmap(backGroudId, pictureURI, ThemeItemContentView.this, position);       
	    	        if (mBitmap != null) {
	    	        	Bitmap contentBitmap = Bitmap.createScaledBitmap(mBitmap,canvasWidth, canvasHeight, true);
	    	    		if (contentBitmap != null) {               
	    	        		canvas.drawBitmap(contentBitmap, 0, 0,null);   	        		
	    				}	 
					}   
	    	    }		    		
			}			
		}	
    }   
    
    private void drawContent(Canvas canvas,Bitmap mBitmap,int i){
    	if (mBitmap != null) {	    			
            float ratio =  i*1f/6;
    		int left = (int) (canvasWidth*ratio);
    		int top = (int) (canvasHeight*(1f/2 - ratio));
    		canvas.drawBitmap(mBitmap, left, top,null);
		}   	       	
    }

}

