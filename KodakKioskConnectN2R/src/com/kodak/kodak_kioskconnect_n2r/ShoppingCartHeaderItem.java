package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ShoppingCartHeaderItem extends LinearLayout {
	public TextView quantityTV;
	public TextView priceTV;
	public TextView productSizeTV;
	public TextView pricingEstimated;
	public TextView dividerDash;
	public ImageView groupIndicator;
	public ProgressBar bar;
	public View viewShoppingCartGroupItem, viewAutomaticUpload2Albums;

	public ShoppingCartHeaderItem(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoppingcar_group_item, this);
		viewAutomaticUpload2Albums = findViewById(R.id.automaticUpload_Albums);
		viewShoppingCartGroupItem = findViewById(R.id.shoppingCartGroup_item);
		quantityTV = (TextView) findViewById(R.id.productQuantity_tex);
		priceTV = (TextView) findViewById(R.id.productPrice_tex);
		productSizeTV = (TextView) findViewById(R.id.productSize_tex);
		pricingEstimated = (TextView) findViewById(R.id.pricingEstimated_tex);
		dividerDash = (TextView) findViewById(R.id.dividerDash);
		bar = (ProgressBar) findViewById(R.id.progressBar1);
		groupIndicator = (ImageView) findViewById(R.id.imageView1);
	}
}
