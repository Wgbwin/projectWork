package com.kodakalaris.kodakmomentslib.fragment.mobile.menu;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.interfaces.menu.OnHeadlineSelectedListener;

public class MSettingsAboutFragment extends Fragment{
	private OnHeadlineSelectedListener mCallback;
	private TextView tvVersion;
	
	public static MSettingsAboutFragment getAboutFragmentInstance(){
		MSettingsAboutFragment fileViewFragment = new MSettingsAboutFragment();
		return fileViewFragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (OnHeadlineSelectedListener) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_m_setting_about, null);
		initView(view);
		setEvent();
		return view;
	}

	
	private void initView(View view) {
		tvVersion = (TextView) view.findViewById(R.id.tv_version);
		try {
			PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			String versionDetail = getString(R.string.Version_String) + " " + packageInfo.versionName + " ";
			tvVersion.setText(versionDetail);
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	private void setEvent() {
		
	}

}
