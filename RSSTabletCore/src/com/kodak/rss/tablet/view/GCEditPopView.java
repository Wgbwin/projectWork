package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kodak.rss.core.n2r.bean.greetingcard.GCLayer;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.tablet.adapter.GCEditPopAdapter;
import com.kodak.rss.tablet.adapter.ProductEditPopAdapter;

public class GCEditPopView extends ProductEditPopView<GCPage, GCLayer>{
	
	
	public GCEditPopView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GCEditPopView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GCEditPopView(Context context) {
		super(context);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	protected ProductEditPopAdapter initAdapter() {
		return new GCEditPopAdapter(getContext());
	}

}
