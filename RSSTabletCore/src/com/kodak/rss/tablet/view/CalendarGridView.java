package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.GridView;

import com.kodak.rss.tablet.R;

public class CalendarGridView extends GridView{

	public CalendarGridView(Context context) {
		this(context, null);		
	}

	public CalendarGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CalendarGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);	
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {						
		return super.dispatchTouchEvent(ev);		
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		for(int i=0; i < getChildCount();i++){
			View child = getChildAt(i);
			if (isEventWithinView(ev, child)) {
				return false;				
			}
		}
		return super.onInterceptTouchEvent(ev);		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}
	
	private boolean isEventWithinView(MotionEvent e, View pChild){
		 if (pChild == null ) return false;			 
		 View childView = pChild.findViewById(R.id.wv_photobook_detail);
		 if (childView == null ) return false;	
		 if (!(childView instanceof WebView)) return false;	
		 Rect viewRect = new Rect();
         int[] childPosition = new int[2];
         childView.getLocationOnScreen(childPosition);
         int left = childPosition[0];
         int right = left + childView.getWidth();
         int top = childPosition[1];
         int childHeight = childView.getHeight();
         int bottom = top + childHeight;
         viewRect.set(left, top, right, bottom);
         if (viewRect.contains((int) e.getRawX(), (int) e.getRawY())) {
        	 WebView webView = (WebView) childView;        	
        	 float contentDistance = webView.getContentHeight() * webView.getScale() - childHeight;        	
        	 if (contentDistance <= 0) {
    			return false;
        	 }
    		 float ScrollY = webView.getScrollY();   			 
    		 if (contentDistance >= ScrollY) {
				return true;
			 }		 
         }
         return false;
	}
		
}
