package com.kodak.kodak_kioskconnect_n2r.view;

import android.content.Context;
import android.util.AttributeSet;

import com.kodak.kodak_kioskconnect_n2r.adapter.CollageEditPopAdapter;
import com.kodak.kodak_kioskconnect_n2r.adapter.ProductEditPopAdapter;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;

public class CollageEditPopView extends ProductEditPopView<CollagePage,Layer>{

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
