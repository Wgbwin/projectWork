package com.kodakalaris.kodakmomentslib.fragment.mobile.menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.eulaprivacy.MEulaPrivacyActivity;
import com.kodakalaris.kodakmomentslib.interfaces.menu.OnHeadlineSelectedListener;

public class MMenuSettingFragment extends Fragment {
	private RelativeLayout vRealLyDefSize,vRealLyDefStore
	,vRealLyLegal,vRealLyCountry,vRealLyAbout;
	private OnHeadlineSelectedListener mCallback ;
	private Activity mActivity;

	public static MMenuSettingFragment getSettingFragmentInstance(){
		MMenuSettingFragment fileViewFragment = new MMenuSettingFragment();
		return fileViewFragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (OnHeadlineSelectedListener) activity;
		mActivity = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_m_menu_settings, null);
		initView(view);
		setEvent();
		return view;
	}

	private void initView(View view) {
		vRealLyDefStore = (RelativeLayout) view.findViewById(R.id.realLy_MenuSetting_defautlStore);
		vRealLyLegal = (RelativeLayout) view.findViewById(R.id.realLy_MenuSetting_Legal);
		vRealLyCountry = (RelativeLayout) view.findViewById(R.id.realLy_MenuSetting_Country);
		vRealLyAbout = (RelativeLayout) view.findViewById(R.id.realLy_MenuSetting_About);
	}

	private void setEvent() {
		vRealLyCountry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mCallback.onItemSelected(AppConstants.SETTING_COUNTRY, AppConstants.MMENU_SETTING_FRAGMENT);				
			}
		});

		vRealLyAbout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mCallback.onItemSelected(AppConstants.SETTING_ABOUT, AppConstants.MMENU_SETTING_FRAGMENT);
			}
		});
		
		vRealLyLegal.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCallback.onItemSelected(AppConstants.SETTING_LICENSE, AppConstants.MMENU_SETTING_FRAGMENT);
			}
		});
	}

}
