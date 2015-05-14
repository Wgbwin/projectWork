package com.kodak.flip;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class IconImageView extends ImageView {

	public IconImageView(Context context) {
		super(context);
	}
	
	public IconImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public IconImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		getParent().requestDisallowInterceptTouchEvent(true);
		if(event.getAction() == MotionEvent.ACTION_UP){
			return performClick();
		}
		return true;
	}
	
	

}
