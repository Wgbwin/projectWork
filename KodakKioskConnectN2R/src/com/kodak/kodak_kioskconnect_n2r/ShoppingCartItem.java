package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShoppingCartItem extends RelativeLayout {
	public ImageView preview = null;
	public ImageView lowRes = null;
	public TextView quantity = null;
	public TextView price = null;
	public TextView change = null;
	public TextView additionalQuantity_tex = null;
	public TextView additionalSize_tex = null;
	public TextView additionalPrice_tex = null;
	
	public TextView additionalCouponsQuantity_tex = null;
	public TextView additionalCouponsPrice_tex= null;
	public TextView additionalCouponsSize_tex = null;
	
	public TextView normalOriginalQuantity_tex = null;
	public TextView normalOriginalPrice_tex = null;
	public TextView normalOriginalSize_tex = null;
	
	public TextView normalCouponsQuantity_tex = null;
	public TextView normalCouponsPrice_tex = null;
	public TextView normalCouponsSize_tex = null;
	

	public Button delete;
	public Button quantityMinusButton;
	public Button quantityPlusButton;
	public ImageView lowResWarning;

	public LinearLayout photoBookAdditionLy;
	public LinearLayout normalProductLy;
	
	public int position = 0;
	public int id = 0;
	public int cursorPosition = 0;
	String uri;

	public ShoppingCartItem(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoppingcart_child_item, this);
		preview = (ImageView) findViewById(R.id.productPreview_img);
		quantity = (TextView) findViewById(R.id.productQuantity_tex);
		price = (TextView) findViewById(R.id.productPrice_tex);
		change = (TextView) findViewById(R.id.changeTV);
		//for additional product
		additionalQuantity_tex = (TextView) findViewById(R.id.additionalQuantity_tex);
		additionalSize_tex = (TextView) findViewById(R.id.additionalSize_tex);
		additionalPrice_tex = (TextView) findViewById(R.id.additionalPrice_tex);
		
		additionalCouponsQuantity_tex = (TextView) findViewById(R.id.additionalCouponsQuantity_tex);
		additionalCouponsPrice_tex = (TextView) findViewById(R.id.additionalCouponsPrice_tex);
		additionalCouponsSize_tex = (TextView) findViewById(R.id.additionalCouponsSize_tex);
		
		//for normal product
		normalOriginalQuantity_tex = (TextView) findViewById(R.id.normalOriginalQuantity_tex);
		normalOriginalPrice_tex = (TextView) findViewById(R.id.normalOriginalPrice_tex);
		normalOriginalSize_tex = (TextView) findViewById(R.id.normalOriginalSize_tex);
		
		normalCouponsQuantity_tex = (TextView) findViewById(R.id.normalCouponsQuantity_tex);
		normalCouponsPrice_tex = (TextView) findViewById(R.id.normalCouponsPrice_tex);
		normalCouponsSize_tex = (TextView) findViewById(R.id.normalCouponsSize_tex);
		preview.setScaleType(ScaleType.FIT_CENTER);
		delete = (Button) findViewById(R.id.delete_btn);
		quantityMinusButton = (Button) findViewById(R.id.quantityMinus_btn);
		quantityPlusButton = (Button) findViewById(R.id.quantityPlus_btn);
		lowResWarning = (ImageView) findViewById(R.id.img2);
		photoBookAdditionLy = (LinearLayout) findViewById(R.id.photoBookAdditionLy);
		normalProductLy = (LinearLayout) findViewById(R.id.normalProductLy);
	}
}
