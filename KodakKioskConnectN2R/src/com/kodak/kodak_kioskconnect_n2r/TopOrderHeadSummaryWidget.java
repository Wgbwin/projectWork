package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class TopOrderHeadSummaryWidget extends LinearLayout
{
	TextView orderSubtotalLabelTV;
	TextView orderSubtotalTV;
	TextView estimated;
	ExpandableListView expandableList;
	Context mContext;
	public TopOrderHeadSummaryWidget(Context context, AttributeSet a )
	{
		super(context,a);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.toporderheadsummarywidget, this);
	}
	public TopOrderHeadSummaryWidget(Context context)
	{
		super(context);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.toporderheadsummarywidget, this);
	}
}
