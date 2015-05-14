package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

public class ShoppingCartExpandableListView extends ExpandableListView {

	public ShoppingCartExpandableListView(Context context) {
		super(context);
		setGroupIndicator(null);
	}
	
	public ShoppingCartExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setGroupIndicator(null);
	}
	
	public ShoppingCartExpandableListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setGroupIndicator(null);
	}

	/*@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST); 
		super.onMeasure(widthMeasureSpec, expandSpec);
	}*/
}
