package com.kodak.rss.tablet.view.collage;

import android.content.Context;
import android.util.AttributeSet;

import com.kodak.rss.core.n2r.bean.collage.CollageLayer;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.tablet.adapter.CollageEditPopAdapter;
import com.kodak.rss.tablet.adapter.ProductEditPopAdapter;
import com.kodak.rss.tablet.view.ProductEditPopView;

public class CollageEditPopView extends ProductEditPopView<CollagePage, CollageLayer>{

	public CollageEditPopView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CollageEditPopView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CollageEditPopView(Context context) {
		super(context);
	}

	@Override
	protected ProductEditPopAdapter initAdapter() {
		return new CollageEditPopAdapter(getContext());
	}
	
}
