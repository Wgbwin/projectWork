package com.kodak.kodak_kioskconnect_n2r.activity;

import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.AppConstants;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.WiFiSelectWorkflowActivity;
import com.kodak.kodak_kioskconnect_n2r.fragments.WifiSettingLeftFragment.IOnSettingRadioGroupSelected;
import com.kodak.kodak_kioskconnect_n2r.fragments.WifiSettingRightAboutFragment;
import com.kodak.kodak_kioskconnect_n2r.fragments.WifiSettingRightLegalFragment;
import com.kodak.utils.RSSLocalytics;

/**
 * for KC setting activity
 * 
 * @author sunny
 * 
 */
public class WifiNewSettingFragmentActivity extends FragmentActivity implements IOnSettingRadioGroupSelected {
	private Button vBackBtn;
	private Button vNextBtn;
	private TextView vTitleText;
	private int currentCheckedId;
	public static final String ENABLE_ALLOW_COOKIES = "enable_allow_cookies";
	private HashMap<String, String> attr = new HashMap<String, String>();
	private final String SETTINGS_SUMMARY = "Settings Summary";
	private final String EVENT_CUSINFO = "Customer Info Changed";
	private final String EVENT_SHIPINFO = "Shipping Address Changed";
	private final String EVENT_PRINT_SIZE_CHANGED = "User Changed Default Print Size";
	private final String EVENT_LICENSE = "License viewed";
	private final String EVENT_POLICY = "Privacy Policy viewed";
	private final String EVENT_ORDHISTORY = "Order History viewed";
	private final String EVENT_COOKIE = "Agree to Tracking";
	private final String EVENT_ABOUT = "About Screen viewed";
	private final String EVENT_RATE = "App Rated";
	private final String YES = "yes";
	private final String NO = "no";
	private String settingsLocation = "";
	public static final String SETTINGS_LOCATION = "Settings Location";
	private boolean isAgreementChecked;
	private boolean isPreviousAllowcookie ;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_new_setting);

		findViews();

		setEvents();

		initData();

	}

	private void findViews() {
		vBackBtn = (Button) findViewById(R.id.backButton);
		vNextBtn = (Button) findViewById(R.id.nextButton);
		vTitleText = (TextView) findViewById(R.id.headerBarText);
		vBackBtn.setVisibility(View.INVISIBLE);
		vTitleText.setText(R.string.setup);
		vNextBtn.setText(R.string.done);
		vTitleText.setTypeface(PrintHelper.tf);
		vNextBtn.setTypeface(PrintHelper.tf);
	}

	private void setEvents() {
		vNextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				SharedPreferences.Editor editor = prefs.edit();
				if (isAgreementChecked) {
					// attr.put(EVENT_COOKIE, YES);
				
					RSSLocalytics.onActivityCreate(WifiNewSettingFragmentActivity.this);
					RSSLocalytics.openLocalytics(WifiNewSettingFragmentActivity.this);
					
				}else{
					RSSLocalytics.closeLocalytics(WifiNewSettingFragmentActivity.this);
				}
				// editor.putBoolean("analytics", allowCookiesCB1.isChecked());
//				editor.putBoolean(AppConstants.KEY_LOCALYTICS, isAgreementChecked);

//				editor.commit();
				if(isPreviousAllowcookie){
					RSSLocalytics.recordLocalyticsEventsIgnoreTheSwitch(WifiNewSettingFragmentActivity.this, SETTINGS_SUMMARY, attr);
				}else {
					RSSLocalytics.recordLocalyticsEvents(WifiNewSettingFragmentActivity.this, SETTINGS_SUMMARY, attr);

				}
			
				if (!prefs.getBoolean("privacyAccepted", false)) {
					prefs.edit().putBoolean("privacyAccepted", true).commit();
					Intent myIntent = new Intent(WifiNewSettingFragmentActivity.this, WiFiSelectWorkflowActivity.class);
					startActivity(myIntent);
					finish();
				} else {
					finish();
				}

			}
		});

	}

	private void initData() {
		// TODO Auto-generated method stub

		initLocalyticsAttr();
		RSSLocalytics.onActivityCreate(this);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		isAgreementChecked = prefs.getBoolean(AppConstants.KEY_LOCALYTICS, getResources().getBoolean(R.bool.localytics));
		isPreviousAllowcookie = isAgreementChecked ;
		if(isAgreementChecked){
			attr.put(EVENT_COOKIE, YES);
		}else {
			attr.put(EVENT_COOKIE, NO);
		}
	
		
		Bundle b = getIntent().getExtras();
		if (b != null) {
			settingsLocation = b.getString(SETTINGS_LOCATION);
			if(settingsLocation != null){
				attr.put(SETTINGS_LOCATION, settingsLocation);
			}
		}
		
	}

	private void initLocalyticsAttr() {
		attr.put(EVENT_CUSINFO, NO);
		attr.put(EVENT_SHIPINFO, NO);
		attr.put(EVENT_PRINT_SIZE_CHANGED, NO);
		attr.put(EVENT_LICENSE, NO);
		attr.put(EVENT_POLICY, NO);
		attr.put(EVENT_ORDHISTORY, NO);
		attr.put(EVENT_COOKIE, YES);
		attr.put(EVENT_ABOUT, NO);
		attr.put(EVENT_RATE, NO);

	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		RSSLocalytics.onActivityResume(this);
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		RSSLocalytics.onActivityPause(this);
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (PrintHelper.mActivities.contains(this)) {
			PrintHelper.mActivities.remove(this);
		}
	}
	

	@Override
	public void onSettingRadioGroupSelected(int checkedId) {

		if (checkedId == R.id.wifi_setting_legal_radion_button) {
			showLegal(checkedId);

		} else if (checkedId == R.id.wifi_setting_about_radion_button) {
			showAbout(checkedId);
		}
		currentCheckedId = checkedId;
	}

	private void showLegal(int checkedId) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment rightLegalFragment = fragmentManager.findFragmentById(R.id.fragment_container);
		if (rightLegalFragment instanceof WifiSettingRightLegalFragment) {
			if (rightLegalFragment == null || checkedId != currentCheckedId) {
				rightLegalFragment = new WifiSettingRightLegalFragment();

				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

				fragmentTransaction.replace(R.id.fragment_container, rightLegalFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.commit();

			}
		} else {

			rightLegalFragment = new WifiSettingRightLegalFragment();

			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

			fragmentTransaction.replace(R.id.fragment_container, rightLegalFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();

		}

	}

	private void showAbout(int checkedId) {
		attr.put(EVENT_ABOUT, YES);
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment rightAboutFragment = fragmentManager.findFragmentById(R.id.fragment_container);

		if (rightAboutFragment instanceof WifiSettingRightAboutFragment) {
			if (rightAboutFragment == null || checkedId != currentCheckedId) {
				rightAboutFragment = new WifiSettingRightAboutFragment();

				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

				fragmentTransaction.replace(R.id.fragment_container, rightAboutFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.commit();

			}
		} else {
			rightAboutFragment = new WifiSettingRightAboutFragment();

			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

			fragmentTransaction.replace(R.id.fragment_container, rightAboutFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
		}

	}

	public HashMap<String, String> getAttr() {
		return attr;
	}

	public void setAttr(HashMap<String, String> attr) {
		this.attr = attr;
	}

	public void putAttr(String key, String value) {
		attr.put(key, value);
	}

	public boolean isAgreementChecked() {
		return isAgreementChecked;
	}

	public void setAgreementChecked(boolean isAgreementChecked) {
		this.isAgreementChecked = isAgreementChecked;
		if(isAgreementChecked){
			attr.put(EVENT_COOKIE, YES) ;
		}else {
			attr.put(EVENT_COOKIE, NO) ;
		}
		
	}

}
