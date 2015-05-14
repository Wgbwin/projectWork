package com.kodakalaris.kodakmomentslib.fragment.mobile.menu;

import com.kodakalaris.kodakmomentslib.DataKey;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.menu.MMenuActivity;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.interfaces.menu.OnHeadlineSelectedListener;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;


public class MSettingsLicenseFragment extends Fragment{

	private OnHeadlineSelectedListener mCallback;
	private WebView vWvLicense;
	private Button vBtnAgree;
	
	public static MSettingsLicenseFragment getLicenseFragmentInstance(){
		MSettingsLicenseFragment fileViewFragment = new MSettingsLicenseFragment();
		return fileViewFragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (OnHeadlineSelectedListener) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_m_setting_license, null);
		initView(view);
		setEvent();
		return view;
	}

	
	private void initView(View view) {
		vWvLicense = (WebView) view.findViewById(R.id.wv_license);
		WebSettings webSettings = vWvLicense.getSettings();
		webSettings.setJavaScriptEnabled(true);
		vWvLicense.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			
		});
		vWvLicense.loadUrl(GeneralAPI.getEulaUrl(getActivity()));
		
		vBtnAgree = (Button) view.findViewById(R.id.btn_agree);
	}
	
	private void setEvent() {
		vBtnAgree.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferrenceUtil.setBoolean(getActivity(), DataKey.EULA_ACCEPTED, true);
				((MMenuActivity)getActivity()).onBackRunning();
			}
		});
	}
	
}
