package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Purpose: 
 * Author: Bing Wang
 * Created Time: Aug 29, 2014 
 */
public class AutoPopWebView extends WebView {
	
	private DisplayMetrics dm;
	public AutoPopWebView(Context context) {
		super(context);
		init(context);
	}
	
	public AutoPopWebView(Context context, AttributeSet attrs) {
		super(context, attrs);	
		init(context);
	}
	
	public AutoPopWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);	
		init(context);
	}

	private void init(Context context){		
		dm = context.getResources().getDisplayMetrics();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {		
		super.onDraw(canvas);		
		scrollToY(rectT,touchY);	
	}
	
    private  boolean isScroll = false;
    private  int softHeight;
    public void setScroll(boolean isS,int softHeight){
		this.isScroll = isS;
		this.softHeight = softHeight;
	}

	private void scrollToY(int rT, float tY){
		if (!isScroll) return;		
		if (tY <= 0) return;
		Rect rect = new Rect();
		getFocusedRect(rect);					
		if (rect != null ) {
			int space = (int) (rT + dm.heightPixels + 2*getScale() - rect.bottom);
			if (space > 0) {
				int scrollY = (int) (rT + softHeight+  2*getScale() - (dm.heightPixels - tY - softHeight));							
				int contentH = (int) (getContentHeight()*getScale() - getHeight());
				if (scrollY > contentH) {
					scrollY = contentH;
				}
				scrollTo(0, scrollY);
				this.rectT = 0;
				this.touchY = 0;
				this.isScroll = false;
			}
		}		
	}
	
	private int rectT;
	@Override
	public void computeScroll() {
		super.computeScroll();		
		if (!isScroll) {
			Rect rect = new Rect();
			getFocusedRect(rect);	
			if (rect != null) {
				rectT = rect.top;
			}		
		}			
	}
	
	private float touchY;
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!isScroll && ev.getAction() == MotionEvent.ACTION_UP) {
			touchY = ev.getRawY();	
		}		
		return super.onTouchEvent(ev);
	}

}
