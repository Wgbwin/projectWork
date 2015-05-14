package com.kodakalaris.kodakmomentslib.widget.mobile;


import com.kodakalaris.kodakmomentslib.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class HomeProductsListFooter extends RelativeLayout{

	public HomeProductsListFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.include_m_home_products_list_footer, this);
		
	}
	public HomeProductsListFooter(Context context) {
		super(context);
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.include_m_home_products_list_footer, this);
		
	}

}
