package com.kodakalaris.kodakmomentslib.activity.sendingorder;

import android.os.Bundle;

import com.kodakalaris.kodakmomentslib.activity.BaseNetActivity;

public abstract class BaseSendingOrderActivity extends BaseNetActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setKMContentView();
		initViews();
		initData();
		setEvents();
	}
	
	protected abstract void setKMContentView();
	protected abstract void setEvents() ;
	protected abstract void initData() ;
	protected abstract void initViews() ;
	
	
}
