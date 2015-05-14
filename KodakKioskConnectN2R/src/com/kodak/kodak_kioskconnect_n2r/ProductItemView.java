package com.kodak.kodak_kioskconnect_n2r;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

public class ProductItemView extends RelativeLayout implements AnimationListener {
	
	private PrintProduct product;
	private boolean hasAnimation = false;
	private SharedPreferences prefs;
	private ProgressDialog detailDialog;

	public ProductItemView(Context context) {
		super(context);
		initProductItem(context);
	}
	
	public ProductItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initProductItem(context);
	}
	
	public ProductItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initProductItem(context);
	}
	
	private void initProductItem(Context context){
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.getString("", "");
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		
	}
	
	private void showDialog(){
		
	}

}
