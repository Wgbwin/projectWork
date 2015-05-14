package com.kodak.rss.tablet.view;

import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.tablet.adapter.CalendarEditPopAdapter;
import com.kodak.rss.tablet.adapter.ProductEditPopAdapter;

import android.content.Context;
import android.util.AttributeSet;

public class CalendarEditPopView extends ProductEditPopView<CalendarPage, CalendarLayer>{

	public CalendarEditPopView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CalendarEditPopView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CalendarEditPopView(Context context) {
		super(context);
	}

	@Override
	protected ProductEditPopAdapter initAdapter() {
		return new CalendarEditPopAdapter(getContext());
	}
	
}
