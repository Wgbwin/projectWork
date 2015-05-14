package com.kodakalaris.kodakmomentslib.widget.mobile;

import com.kodakalaris.kodakmomentslib.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class PrintsReviewProductsListHead extends RelativeLayout {
	public PrintsReviewProductsListHead(Context context, AttributeSet a) {
		super(context, a);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.include_m_prints_review_products_list_head, this);
	}

	public PrintsReviewProductsListHead(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.include_m_prints_review_products_list_head, this);
	}

}
