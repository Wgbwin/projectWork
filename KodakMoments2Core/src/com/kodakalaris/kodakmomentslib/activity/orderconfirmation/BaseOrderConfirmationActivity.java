package com.kodakalaris.kodakmomentslib.activity.orderconfirmation;

import android.os.Bundle;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.activity.BaseActivity;
import com.kodakalaris.kodakmomentslib.bean.OrderDetail;

public class BaseOrderConfirmationActivity extends BaseActivity {
	protected boolean isPreviewOrder = false;
	protected OrderDetail mOrderDetail;
	
	public static final String KEY_PREVIEW = "preview";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getIntent() != null){
			isPreviewOrder = getIntent().getBooleanExtra(KEY_PREVIEW, false);
			String orderId = getIntent().getStringExtra(AppConstants.KEY_ORDER);
			mOrderDetail = OrderDetail.load(this, orderId);
		}
	}

	@Override
	public void onBackPressed() {
		if(isPreviewOrder){
			finish();
		}
	}
}
