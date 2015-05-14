package com.kodak.kodak_kioskconnect_n2r.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class InterceptFrameLayout extends FrameLayout {

	public InterceptFrameLayout(Context context) {
		super(context);
	}
	
	public InterceptFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public InterceptFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}
	
}
