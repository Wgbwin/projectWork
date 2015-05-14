package com.kodakalaris.kodakmomentslib.activity.printsreview;

import android.content.Intent;
import android.os.Bundle;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.imageselection.MImageSelectionMainActivity;

public class MPrintsReviewActivity extends BasePrintsReviewActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_prints_review);
		getViews();
		initData();
		setEvents();

	}

	protected void getViews() {
		super.getViews();
	}

	protected void initData() {
		super.initData();

	}

	protected void setEvents() {
		super.setEvents();
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MImageSelectionMainActivity.class);
		startActivity(intent);
		finish();
	}
}
