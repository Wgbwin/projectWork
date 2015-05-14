package com.kodak.kodak_kioskconnect_n2r.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.R;

public class WifiSettingRightAboutFragment extends Fragment{
	private TextView mTxtVersion;
	private Button mBtnRate;
	private WebView mWebView;
	private String versionName = "";
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		try
		{
			PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			if(AppContext.getApplication().isBrandedApp()){
				versionName = getString(R.string.mainMenuVersion) + " " + packageInfo.versionName + " " + getString(R.string.Copyright_String);
			} else {
				versionName = getString(R.string.mainMenuVersion) + " " + packageInfo.versionName + " " + getString(R.string.Cobranded_Copyright_String);
			}
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.about,container, false);
		mTxtVersion = (TextView) v.findViewById(R.id.txt_version);
		mBtnRate = (Button) v.findViewById(R.id.btn_rate);
		mBtnRate.setVisibility(View.VISIBLE);
		mWebView = (WebView) v.findViewById(R.id.web);
		mTxtVersion.setText(versionName);
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl(getResources().getString(R.string.helpURL));
		mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		mWebView.requestFocus() ;
		
		mBtnRate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//start change by bing for fixed RSSMOBILEPDC-2184 on 2015-3-10
				String packageName = WifiSettingRightAboutFragment.this.getActivity().getPackageName();
				Uri uri = null;
				Intent goToMarket = null;						
				try {
					uri = Uri.parse("market://details?id=" + packageName);
					goToMarket =new Intent(Intent.ACTION_VIEW, uri);
					goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
					startActivity(goToMarket);
				} catch (ActivityNotFoundException e) {
					uri = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
					goToMarket =new Intent(Intent.ACTION_VIEW, uri);
					goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
					startActivity(goToMarket);
				}
				//end change by bing for fixed RSSMOBILEPDC-2184 on 2015-3-10
			}
		});
				
		return v ;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	

}
