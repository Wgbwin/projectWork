package com.kodakalaris.kodakmomentslib.activity.countryselection;

import android.view.View;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment.OnClickListener;
import com.kodakalaris.kodakmomentslib.widget.mobile.GeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.MCountrySelectionDialog;
import com.kodakalaris.kodakmomentslib.widget.mobile.MCountrySelectionDialog.OnCountrySelectedListener;

public class MCountrySelectionActivity extends BaseCountrySelectionActivity {
	
	private MCountrySelectionDialog countrySelectionDialog;

	@Override
	protected void showCountrySelectionDialog() {
		countrySelectionDialog = new MCountrySelectionDialog(this, false, false);
		countrySelectionDialog.initDialog(this, new OnCountrySelectedListener() {
		
			@Override
			public void onCountrySelected() {
				GeneralAlertDialogFragment dialog = new GeneralAlertDialogFragment(MCountrySelectionActivity.this);
				dialog.setMessage(R.string.KMMenu_Country_SaveMessage);
				dialog.setPositiveButton(R.string.Common_OK, new OnClickListener() {
					
					@Override
					public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
						dialogFragment.dismiss();
						finish();
					}
				});
				dialog.show(getSupportFragmentManager(), "mCountrySelectionWarning");
			}
		});
		countrySelectionDialog.show(getSupportFragmentManager(), "mCountrySelection");
	}
}
