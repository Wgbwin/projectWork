package com.kodakalaris.kodakmomentslib.activity.findstore;

import android.os.Bundle;

import com.kodakalaris.kodakmomentslib.R;

/**
 * find store
 * 
 * @author Simon
 * 
 */
public class MDestinationStoreSelectionActivity extends BaseDestinationStoreSelectionActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_store);
		initBaseDSSData();
		getView();
		initData();
		setEvents();
	}

	private void getView() {

	}

	private void initData() {

	}

	private void setEvents() {

		

	}



}
