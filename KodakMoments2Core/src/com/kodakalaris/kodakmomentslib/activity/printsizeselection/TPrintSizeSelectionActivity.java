package com.kodakalaris.kodakmomentslib.activity.printsizeselection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.adapter.mobile.SimpleCarouselAdapter;
import com.kodakalaris.kodakmomentslib.widget.Carousel;

public class TPrintSizeSelectionActivity extends BasePrintSizeSelectionActivity {
	private Carousel vCarousel;
	private int[] mImgs;
	private SimpleCarouselAdapter vCarouselAdapter;
	private LinearLayout vLinearLayout;
	private Button mPreSelectedBt;
	private TextView vTxtTitleBar;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_t_print_size_selection);
		initView();
		initData();
		initEvent();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (vCarousel != null) {
			vCarousel.startAutoFlip();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (vCarousel != null) {
			vCarousel.stopAutoFlip();
		}
	}

	private void initEvent() {
		vCarousel.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (mPreSelectedBt != null){  
					mPreSelectedBt .setBackgroundResource(R.drawable. icon_pageindicator_shopping);  
				}  
				Button currentBt = (Button)vLinearLayout .getChildAt(position);  
				currentBt.setBackgroundResource(R.drawable. icon_pageindicator_shopping_sel );  
				mPreSelectedBt = currentBt;  
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}
			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	private void initData() {
		mImgs = new int[]{R.drawable.image_marketing_prints_1, R.drawable.image_marketing_prints_1, R.drawable.image_marketing_prints_1};
		vCarouselAdapter = new SimpleCarouselAdapter(this, mImgs);
		vCarousel.setAdapter(vCarouselAdapter);
		vCarousel.setCurrentItem(0);

		Bitmap bitmap = BitmapFactory. decodeResource(getResources(), R.drawable.icon_pageindicator_shopping );  
		//loading pages
		for (int i = 0; i < mImgs.length; i++) { 
			Button bt = new Button(this );  
			if(i==0){
				bt.setLayoutParams( new ViewGroup.LayoutParams(bitmap.getWidth(),bitmap.getHeight()));  
				bt.setBackgroundResource(R.drawable. icon_pageindicator_shopping_sel ); 
				mPreSelectedBt=bt;
			}else{
				bt.setLayoutParams( new ViewGroup.LayoutParams(bitmap.getWidth(),bitmap.getHeight()));  
				bt.setBackgroundResource(R.drawable. icon_pageindicator_shopping );  
			}
			bt.setId(i);
			
			vLinearLayout .addView(bt);  
		} 
		vTxtTitleBar.setText(R.string.Common_Prints);
	}

	private void initView() {
		vCarousel  = (Carousel) findViewById(R.id.carousel_print_size_selection);
		vLinearLayout  = (LinearLayout) findViewById(R.id.lineLy_pageindicator_shopping_sel);
		vTxtTitleBar = (TextView) findViewById(R.id.txt_title_bar);
	}
	
}
