package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.FloatMath;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public abstract class DragHelper {
	
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mWindowLayoutParams;
	public View mDragImageView;
	private View popUpImageView;	
	public int width;
	public int height;	
	public int ox,oy;
	
	public DragHelper(Context context,View popView) {
		this.popUpImageView = popView;
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT;
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;	
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		mWindowLayoutParams.windowAnimations = 0;					
	}
	
	public void setWH(int viewWidth,int viewHeight){
		this.width = viewWidth;
		this.height = viewHeight;
	}
	
	public void setXY(float rawX,float rawY){
		this.ox = (int) (rawX - this.width*1f/2);
		this.ox =  this.ox > 0 ? this.ox : 0;		
		this.oy = (int) (rawY - this.height*1f/2);
		this.oy =  this.oy > 0 ? this.oy : 0;		
	}	
	
	public void createDragImage(Bitmap bitmap,String... promptStr) {	
		removeDragImage();		
		mWindowLayoutParams.x = ox;
		mWindowLayoutParams.y = oy;
		mWindowLayoutParams.alpha = 0.95f;
		mWindowLayoutParams.width = width;
		mWindowLayoutParams.height = height;
		mDragImageView = popUpImageView;
		setCreateDragView(bitmap, promptStr);		
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);
	}
	
	public abstract void setCreateDragView(Bitmap bitmap,String... promptStr);
	public abstract void setOnDragView(int moveX, int moveY,float rawX,float rawY);
	
	
	public void removeDragImage() {
		if (mDragImageView != null) {
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}
	}	
	
	public void onDrag(int moveX, int moveY,float rawX,float rawY) {		
		setXY(rawX, rawY);		
		mWindowLayoutParams.x = ox;
		mWindowLayoutParams.y = oy;
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams);	
		setOnDragView(moveX, moveY, rawX, rawY);
	}
	
	public boolean isTouchInItem(int x, int y) {
		if (mDragImageView == null) return true;
		int leftOffset = mDragImageView.getLeft();
		int topOffset = mDragImageView.getTop();
		if (leftOffset < 0 || topOffset < 0) return true;
		if (x < leftOffset || x > leftOffset + width) return false;
		if (y < topOffset || y > topOffset + height) return false;
		return true;
	}
	
	public float spacing(float x1,float x2, float y1,float y2) {
		float space = (x2 - x1)*(x2 - x1) + (y2-y1)*(y2-y1);
		return FloatMath.sqrt(space);
	}

}
