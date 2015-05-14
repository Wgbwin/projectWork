package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;

import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.tablet.adapter.PhotoBookEditPopAdapter;
import com.kodak.rss.tablet.adapter.ProductEditPopAdapter;

public class PhotoBookEditPopView extends ProductEditPopView<PhotobookPage, Layer>{

	public PhotoBookEditPopView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public PhotoBookEditPopView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PhotoBookEditPopView(Context context) {
		super(context);
	}

	@Override
	protected ProductEditPopAdapter initAdapter() {
		return new PhotoBookEditPopAdapter(getContext());
	}
}
