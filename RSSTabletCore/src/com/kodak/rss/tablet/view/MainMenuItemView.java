package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.bean.MainMenuItem;

public class MainMenuItemView extends RelativeLayout{
	private ImageView mIv;
	private TextView mTv;

	public MainMenuItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public MainMenuItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MainMenuItemView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		inflate(context, R.layout.main_menu_item, this);
		mIv = (ImageView) findViewById(R.id.iv_image);
		mTv = (TextView) findViewById(R.id.tv_text);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//make h = w
		int w = MeasureSpec.getSize(widthMeasureSpec);
		
		int measureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
		super.onMeasure(measureSpec, measureSpec);
	}
	
	public void setInfo(MainMenuItem item) {
		mTv.setText(item.getText());
		mIv.setImageResource(item.getImageId());
	}
	
}
