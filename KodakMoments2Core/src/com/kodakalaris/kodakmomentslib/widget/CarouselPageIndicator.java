package com.kodakalaris.kodakmomentslib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kodakalaris.kodakmomentslib.R;

public class CarouselPageIndicator extends LinearLayout {
	private Context mContext;
	private int mPageIndicatorImgResId = R.drawable.intro_dots;
	private int mSize = 0;

	public CarouselPageIndicator(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CarouselPageIndicator);
		mPageIndicatorImgResId = a.getResourceId(R.styleable.CarouselPageIndicator_drawable, mPageIndicatorImgResId);
		mSize = a.getInteger(R.styleable.CarouselPageIndicator_size, mSize);
		a.recycle();
		
		init(context);
	}

	public CarouselPageIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CarouselPageIndicator(Context context) {
		super(context);
		LayoutParams params = (LayoutParams) getLayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		
		init(context);
	}
	
	private void init(Context context) {
		mContext = context;
		
		setOrientation(LinearLayout.HORIZONTAL);
		setPageIndicator(mSize, mPageIndicatorImgResId);
	}
	
	
	private void setPageIndicator(int size, int imgResId) {
		mSize = size;
		mPageIndicatorImgResId = imgResId;
		
		removeAllViews();
		
		if (size <= 1) return;
		
		for (int i = 0; i < size; i++) {
			ImageView iv = new ImageView(mContext);
			iv.setImageResource(mPageIndicatorImgResId);
			iv.setSelected(i == 0 ? true : false);
			addView(iv);
		}
	}
	
	public void setSize(int size) {
		setPageIndicator(size, mPageIndicatorImgResId);
	}
	
	public void setPosition(int position) {
		if (mSize <= 1) {
			return;
		}
		
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			v.setSelected(position == i ? true : false);
		}
	}
	
}
