package com.kodakalaris.kodakmomentslib.activity.countryselection;

import android.os.Bundle;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.BaseActivity;

public abstract class BaseCountrySelectionActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_country_selection);
		showCountrySelectionDialog();
	}
	
	protected abstract void showCountrySelectionDialog();
}
