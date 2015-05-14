package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.bean.ProductEditPopItem;

public class ProductEditPopItemView extends FrameLayout{
	private ImageView ivIcon;
	private ImageView ivEntry;
	private TextView tvText;

	public ProductEditPopItemView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ProductEditPopItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ProductEditPopItemView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		inflate(context, R.layout.product_edit_pop_item, this);
		ivIcon = (ImageView) findViewById(R.id.iv_icon);
		ivEntry = (ImageView) findViewById(R.id.btn_entry);
		tvText = (TextView) findViewById(R.id.tv_text);
		
		setFocusable(false);
		setFocusableInTouchMode(false);
	}
	
	public void setInfo(ProductEditPopItem item){
		if(item.imageResId != -1){
			ivIcon.setImageResource(item.imageResId);
		}
		tvText.setText(item.strResId);
		if(item.entryAble){
			ivEntry.setVisibility(View.VISIBLE);
		}else{
			ivEntry.setVisibility(View.INVISIBLE);
		}
	}

}
