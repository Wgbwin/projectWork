package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public abstract class PhotobookTextEditView extends FrameLayout {
	
	protected Context mContext;
	public DisplayMetrics dm;		
	public int topMSpace = 0;
	
	public PhotobookTextEditView(Context context) {
		super(context);
		init(context);
		dm = getResources().getDisplayMetrics();
	}
	
	public PhotobookTextEditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		dm = getResources().getDisplayMetrics();
	}
	
	public PhotobookTextEditView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		dm = getResources().getDisplayMetrics();
	}
	
	protected abstract void init(Context context);
	
	public PhotobookTextEditView setViewSize(Point point){
		int width = dm.widthPixels * 11 / 24;
		int height = (int) (width/2 - dm.density*5);
		ViewGroup.LayoutParams params = getLayoutParams();
		params.height = height;
		params.width = width;
		topMSpace = dm.heightPixels - height;
		setLayoutParams(params);
		return this;
	}
	
	public void showAtLeft(boolean showAtLeft, int topMargin, int sideMargin){
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
		if(showAtLeft){
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			params.leftMargin = sideMargin;
		} else {
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			params.rightMargin = sideMargin;
		}
		params.topMargin = topMargin;
		setLayoutParams(params);
		((View)getParent()).setVisibility(View.VISIBLE);
		initFocusView();
	}
	
	public abstract void initFocusView();

}
