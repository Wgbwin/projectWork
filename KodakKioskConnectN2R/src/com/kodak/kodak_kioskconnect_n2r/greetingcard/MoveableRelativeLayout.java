package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import java.util.Date;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class MoveableRelativeLayout extends RelativeLayout {

	private Context mContext;
	private static final int INVALID_POINTER_ID = -1;
	private static final String TAG = "MoveableRelativeLayout"; 
	private MoveableRelativeLayout tempLayout;
	private float mPosX;
	private float mPosY;
	private float mLastTouchX;
	private float mLastTouchY;
	private int mActivePointerId = INVALID_POINTER_ID;
	private long downTime = 0;
	private int pCount = 0;
	private ZoomableRelativeLayout mZoomLayout;
	
	public ZoomableRelativeLayout getmZoomLayout() {
		return mZoomLayout;
	}

	public void setmZoomLayout(ZoomableRelativeLayout mZoomLayout) {
		this.mZoomLayout = mZoomLayout;
	}

	public float getmLastTouchX() {
		return mLastTouchX;
	}

	public void setmLastTouchX(float mLastTouchX) {
		this.mLastTouchX = mLastTouchX;
	}

	public float getmLastTouchY() {
		return mLastTouchY;
	}

	public void setmLastTouchY(float mLastTouchY) {
		this.mLastTouchY = mLastTouchY;
	}

	public float getmPosX() {
		return mPosX;
	}

	public void setmPosX(float mPosX) {
		this.mPosX = mPosX;
	}

	public float getmPosY() {
		return mPosY;
	}

	public void setmPosY(float mPosY) {
		this.mPosY = mPosY;
	}

	public MoveableRelativeLayout(Context context) {
		this(context,null,0);
	}
	
	public MoveableRelativeLayout(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}
	
	public MoveableRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		tempLayout = this;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		final int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
				Log.i(TAG, "touchtouch Moveable ACTION_DOWN");
				pCount = 1;
				final float x = event.getX();
				final float y = event.getY();
				downTime = new Date().getTime();
				mLastTouchX = x;
				mLastTouchY = y;
				mActivePointerId = event.getPointerId(0);
				break;
			}
		
		case MotionEvent.ACTION_POINTER_DOWN: {
			Log.i(TAG, "touchtouch Moveable ACTION_POINTER_DOWN");
			pCount ++;
			return false;
		}
		
		case MotionEvent.ACTION_POINTER_UP: {
			Log.i(TAG, "touchtouch Moveable ACTION_POINTER_UP");
			pCount --;
			return false;
		}
		
		case MotionEvent.ACTION_UP: {
			Log.i(TAG, "touchtouch Moveable ACTION_UP");
			mActivePointerId = INVALID_POINTER_ID;
			long time = new Date().getTime() - downTime;
			if (time < 200) {
				//Do Something dispatch
			}
			break;
		}
		
		case MotionEvent.ACTION_CANCEL: {
			Log.i(TAG, "touchtouch Moveable ACTION_CANCEL");
			mActivePointerId = INVALID_POINTER_ID;
			break;
		}
		
		case MotionEvent.ACTION_MOVE: {
			Log.i(TAG, "touchtouch Moveable ACTION_MOVE");
			if (getmZoomLayout() != null && getmZoomLayout().getmScaleFactor() == 2.0f && pCount == 1) {
				final int pointerIndex = event.findPointerIndex(mActivePointerId);
				final float x = event.getX(0);
				final float y = event.getY(0);
				final float disX = x - mLastTouchX;
				final float disY = y - mLastTouchY;
				setmPosX(getmPosX() + disX);
				setmPosY(getmPosY() + disY);
				mLastTouchX = x;
				mLastTouchY = y;
				invalidate();
			}
			break;
		}
		
		}
		
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.translate(getmPosX(), getmPosY());
		int count = getChildCount();
		for (int i = 0 ; i < count ; i++) {
			View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				childView.draw(canvas);
			}
		}
		canvas.restore();
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
	}
}
