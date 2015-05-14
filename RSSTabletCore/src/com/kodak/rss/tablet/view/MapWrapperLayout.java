package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class MapWrapperLayout extends FrameLayout {
	
	public interface OnDragListener {
        public void onDrag(MotionEvent motionEvent);
    }
	
	private OnDragListener mOnDragListener;
	
	public MapWrapperLayout(Context context) {
		super(context);
	}
	
	public MapWrapperLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MapWrapperLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mOnDragListener != null) {
            mOnDragListener.onDrag(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setOnDragListener(OnDragListener mOnDragListener) {
        this.mOnDragListener = mOnDragListener;
    }

}
