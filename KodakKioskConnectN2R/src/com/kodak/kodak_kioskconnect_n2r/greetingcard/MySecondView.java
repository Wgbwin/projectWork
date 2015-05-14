package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.kodak.kodak_kioskconnect_n2r.PrintHelper;



public class MySecondView extends View {
	private Paint paint;
	private Shader mShader;
	private Bitmap mBitmap3 = null;	
	public MySecondView(Context context) {
		super(context);
		initView();
	}
	
	public MySecondView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	public MySecondView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}
	
	private void initView(){
		paint = new Paint();
		paint.setAntiAlias(true);
		this.setFocusable(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		addShadow(canvas);

	
	}
	
	private void addShadow(Canvas canvas){		
		if (PrintHelper.model == 3) {
			return;
		}		
		mShader = null;
		Paint mMaint = null; 
        switch (PrintHelper.lastStepTo){
        case 1:
        		if (PrintHelper.model == 1) {
        			mBitmap3 = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ALPHA_8);
        			Canvas c = new Canvas(mBitmap3);
        	        c.drawARGB(100, 255, 255, 255);	
        	        mShader = new LinearGradient(0, 0, this.getWidth(), 0, new int[] {
        				Color.BLACK,Color.WHITE},
        				new float[]{ 0.9f,1f}, Shader.TileMode.MIRROR);
        	       mMaint = new Paint();
        	       mMaint.setShader(mShader);
        	       canvas.drawBitmap(mBitmap3, 0, 0, mMaint);
        		}else if (PrintHelper.model == 2){
        			mBitmap3 = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ALPHA_8);
        			Canvas c = new Canvas(mBitmap3);
        	        c.drawARGB(100, 255, 255, 255);	
        			mShader = new LinearGradient(0, this.getHeight(), 0, 0, new int[] {
        				Color.BLACK,Color.WHITE},
        				new float[]{ 0.85f,1f}, Shader.TileMode.MIRROR);
        			mMaint = new Paint();
                  	mMaint.setShader(mShader);
                  	canvas.drawBitmap(mBitmap3, 0, 0, mMaint);    
        		}       		             	   	
        	break;
        case 2:       	
        	if (PrintHelper.model == 1) {
        		mBitmap3 = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ALPHA_8);
        		Canvas c = new Canvas(mBitmap3);
                c.drawARGB(100, 255, 255, 255);	
        		mShader = new LinearGradient(0, 0, 0, this.getHeight(), new int[] {
    	       		Color.WHITE,Color.BLACK},
    	            new float[]{ 0.9f,1f}, Shader.TileMode.MIRROR); 
        			mMaint = new Paint();
                	mMaint.setShader(mShader);
                 	canvas.drawBitmap(mBitmap3, 0, 0, mMaint);     
        	}else if (PrintHelper.model == 2){
        		mBitmap3 = Bitmap.createBitmap(canvas.getWidth()/10, canvas.getHeight(), Bitmap.Config.ALPHA_8);
        		Canvas c = new Canvas(mBitmap3);
                c.drawARGB(50, 255, 255, 255);	
	        		mShader = new LinearGradient(this.getWidth() - this.getWidth()/2, 0, -this.getWidth()/2, 0, new int[] {
	        			Color.WHITE,Color.BLACK},
	              		new float[]{ 0.8f,1f}, Shader.TileMode.MIRROR);
	        		mMaint = new Paint();
	               	mMaint.setShader(mShader);
	              	canvas.drawBitmap(mBitmap3, 0, 0, mMaint);           			
        		}       		             	         	
        	break;
        case 3:        	
        	if (PrintHelper.model == 1) {
        		mBitmap3 = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight()/10, Bitmap.Config.ALPHA_8);		 
                Canvas c = new Canvas(mBitmap3);
                c.drawARGB(100, 255, 255, 255);
                Shader mShader = new LinearGradient(0, this.getHeight(), 0, 0, new int[] {
        	       		Color.WHITE,Color.BLACK},
        	             new float[]{ 0.8f,1f}, Shader.TileMode.MIRROR); 
        			mMaint = new Paint();
	             	mMaint.setShader(mShader);
	            	canvas.drawBitmap(mBitmap3, 0, 0, mMaint);      			
        	}else if (PrintHelper.model == 2){
        		mBitmap3 = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ALPHA_8);
        		Canvas c = new Canvas(mBitmap3);
                c.drawARGB(100, 255, 255, 255);	
	        		mShader = new LinearGradient(0, this.getHeight(), 0, 0, new int[] {
	        			Color.BLACK,Color.WHITE},
	                	new float[]{ 0.9f,1f}, Shader.TileMode.MIRROR);
	        		mMaint = new Paint();
	             	mMaint.setShader(mShader);
	            	canvas.drawBitmap(mBitmap3, 0, 0, mMaint);
        	}               	
        	break;
        case 4: 
        	mBitmap3 = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ALPHA_8);
        	Canvas c = new Canvas(mBitmap3);
            c.drawARGB(100, 255, 255, 255);	
            Shader mShader = new LinearGradient(this.getWidth(), 0,0, 0,  new int[] {
        		Color.BLACK,Color.WHITE},
        		new float[]{ 0.9f,1f}, Shader.TileMode.MIRROR);
        	mMaint = new Paint();
        	mMaint.setShader(mShader);
         	canvas.drawBitmap(mBitmap3, 0, 0, mMaint);  
	        break;
        }  
        if(mBitmap3 != null){
        	mBitmap3.recycle();
        	mBitmap3 = null;
		}
	}
}
