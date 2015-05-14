package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;

public class ShoppingCartItemChildView extends RelativeLayout{
	
	private TextView vTxtQuantity;
	private TextView vTxtContent1;// 1st content
	private TextView vTxtContent2;// 2nd content
	private TextView vTxtPrice;
	
	private Type mType = Type.PRODUCT_INFO;
	private boolean mAmountChangeAble = false;
	
	public ShoppingCartItemChildView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ShoppingCartItemChildView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ShoppingCartItemChildView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		inflate(context, R.layout.item_shopping_cart_child, this);
		
		vTxtQuantity = (TextView) findViewById(R.id.txt_product_quantity);
		vTxtContent1 = (TextView) findViewById(R.id.txt_content_1st);
		vTxtContent2 = (TextView) findViewById(R.id.txt_content_2nd);
		vTxtPrice = (TextView) findViewById(R.id.txt_product_price);
	}
	
	public void setQuantity(int quantity) {
		vTxtQuantity.setText(String.valueOf(quantity));
	}
	
	public void setContentFor1stLine(String text) {
		vTxtContent1.setText(text);
	}
	
	public void setContentFor2ndLine(String text) {
		vTxtContent2.setText(text);
		vTxtContent2.setVisibility(View.VISIBLE);
	}
	
	public void setPrice(String text) {
		vTxtPrice.setText(text);
	}
	
	public void setType(Type type) {
		mType = type;
		
		if (mType == Type.DISCOUNT) {
			vTxtQuantity.setVisibility(View.INVISIBLE);
			vTxtPrice.setTextColor(getResources().getColor(R.color.grey));
		}
	}
	
	public void setAmountChangeAble(boolean amountChangeAble) {
		//TODO update UI for amount view
		if (amountChangeAble) {
			
		} else {
			
		}
	}
	
	enum Type {
		PRODUCT_INFO, DISCOUNT, ADITIONAL_PAGE;
	}
}
