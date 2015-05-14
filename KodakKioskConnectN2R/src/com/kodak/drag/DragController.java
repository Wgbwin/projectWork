package com.kodak.drag;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class DragController {

	int checkNum = 0;
	ImageView dragImageView;
	private final int[] mCoordinatesTemp = new int[2];
	private DragLayer1 mDragLayer;
	private IDragDropPosition dragPosition; 
	int width;
	int height;
	int downX;
	int downY;
    private int upScrollBounce;
    private int downScrollBounce;
    private int offsetY;
	
	private int gridHeight = -1;

	private boolean mDragging;

	public DragController(DragLayer1 mDragLayer, IDragDropPosition dragPosition) {
		this.mDragLayer = mDragLayer;
		this.dragPosition = dragPosition;
	}
	
	public void setGridHeight(int height) 
	{
		gridHeight = height;
	}

//	private Vibrator mVibrator;
//	private static final int VIBRATE_DURATION = 35;

	public void startDrag(View v) {
		mDragging = true;

		Bitmap b = getViewBitmap(v);

		if (b == null) {
			return;
		}

		int[] loc = mCoordinatesTemp;
		v.getLocationOnScreen(loc);
		width = v.getWidth();
		height = v.getHeight();
		Log.i("DragController", "width = " + width + ", height = " + height);
		int screenX = loc[0];
		int screenY = loc[1];

		startDrag(v, b, screenX, screenY, width, height);
	}

	private void startDrag(View v, Bitmap b, int screenX, int screenY, int with, int height) {
//		mVibrator = (Vibrator) mContext
//				.getSystemService(Context.VIBRATOR_SERVICE);
//		mVibrator.vibrate(VIBRATE_DURATION);

		ImageView imageView = new ImageView(v.getContext());
		imageView.setImageBitmap(b);
		
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.rightMargin = 0;
		params.bottomMargin = 0;
		
		mDragLayer.addView(imageView, params);
		Log.i("DragController", "screenX = " + screenX + ", screenY = " + screenY);
//		imageView.setPadding(screenX, screenY - height*2/3, 0, 0);
		imageView.setPadding(downX - with/2, downY - height/2, 0, 0);
		dragImageView = imageView;
	}

	
	
	private Bitmap getViewBitmap(View v) {
		v.clearFocus();
		v.setPressed(false);

		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing(false);

		int color = v.getDrawingCacheBackgroundColor();

		if (color != 0) {
			v.destroyDrawingCache();
		}
		v.buildDrawingCache();
		Bitmap cacheBitmap = v.getDrawingCache();
		if (cacheBitmap == null) {
			Log.e("DragController", "failed getViewBitmap(" + v + ")", new RuntimeException());
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
		
//		Bitmap bitmap = Bitmap.createScaledBitmap(cacheBitmap,
//				(int) (cacheBitmap.getWidth() * 1),
//				(int) (cacheBitmap.getHeight() * 1), false);
		
		v.destroyDrawingCache();
		v.setWillNotCacheDrawing(willNotCache);
		v.setDrawingCacheBackgroundColor(color);

		return bitmap;
	}
	
	private void stopDrag(int x, int y){
		if(dragImageView != null){
			mDragLayer.removeView(dragImageView);
			dragImageView = null;
		}

		if (mDragging)
			mDragging = false;
	}
	
	private void drop(int x, int y){
		dragPosition.dropOnGrid(x, y, offsetY);
		stopDrag(x, y);
	}

	private void scroll(int moveX, int moveY){
		//if((moveY < upScrollBounce || moveY > downScrollBounce) && (checkNum % 10 == 0)){
			dragPosition.scroll(moveX, moveY, upScrollBounce, downScrollBounce);
			Log.i("DragController", "up bounce:" + upScrollBounce + ", down bounce:" + downScrollBounce + ", moveY:" + moveY);
	   // }
	}
	
	
	public boolean onTouchEvent(MotionEvent ev) {

		if (! mDragging )
			return false;
		
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			int moveX = (int)ev.getX();
			int moveY = (int)ev.getY();
			scroll(moveX, moveY);
			offsetY = (int)(ev.getRawY() - ev.getY());
			
			if (checkNum % 10 == 0) {
				dragPosition.resetPostionIndicator(moveX, moveY);
			}
			if(checkNum++ == 10) {
				checkNum = 0;
			}
			
			if(dragImageView != null) {
				dragImageView.setPadding(moveX - width/2, moveY - height/2, 0, 0);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			int upX = (int)ev.getX();
			int upY = (int)ev.getY();
			drop(upX, upY);
			break;
		default:
			break;
		}
		return false;
	}

	public boolean onInterceptTouchEvent(MotionEvent ev) {

		int action = ev.getAction();
		upScrollBounce = Math.min((int)ev.getY(), gridHeight/6);
        downScrollBounce = Math.max((int)ev.getY(), gridHeight*5/6);
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			downX = (int)ev.getX();
			downY = (int)ev.getY();
			Log.i("DragController", "downX = " + downX + ",downY = " + downY);
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			int x =(int) ev.getX();
			int y =(int) ev.getY();
			stopDrag(x, y);
			break;
		default:
			break;
		}
		return mDragging;
	}
}
