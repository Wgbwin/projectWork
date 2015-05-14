package com.kodak.drag;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class DragLayer1 extends FrameLayout{

	public DragLayer1(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public DragLayer1(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	DragController controller;
	public void setController(DragController controller) {
		this.controller = controller;
	}

	public DragLayer1(Context context) {
		super(context);
	}

	/*@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}
*/
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return controller.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return controller.onTouchEvent(event);
	}

}
