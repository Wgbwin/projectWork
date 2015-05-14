package com.kodak.kodak_kioskconnect_n2r.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductEditPopItem;

public class ProductEditPopItemView extends FrameLayout{
	private ImageView ivIcon;
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
		tvText = (TextView) findViewById(R.id.tv_text);
		
		setFocusable(false);
		setFocusableInTouchMode(false);
	}
	
	public void setInfo(ProductEditPopItem item){
		if(item.imageResId != -1){
			ivIcon.setImageResource(item.imageResId);
		}
		tvText.setText(item.strResId);

	}

}
