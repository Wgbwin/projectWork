package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class OrderSummaryWidget extends LinearLayout {
	Context mContext;

	public OrderSummaryWidget(Context context) {
		super(context);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.ordersummarywidget, this);
	}

	public OrderSummaryWidget(Context context, AttributeSet a) {
		super(context, a);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.ordersummarywidget, this);
	}
}
