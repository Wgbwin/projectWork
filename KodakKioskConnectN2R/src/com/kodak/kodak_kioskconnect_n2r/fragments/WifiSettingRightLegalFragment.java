package com.kodak.kodak_kioskconnect_n2r.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.AppConstants;
import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.HelpActivity;
import com.kodak.kodak_kioskconnect_n2r.InfoDialog;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.activity.WifiNewSettingFragmentActivity;

public class WifiSettingRightLegalFragment extends Fragment{
	private Button vReadLicenseBtn ;
	private Button vReadPolicyBtn ;
	private CheckBox vAgreementCheckBox ;
	private InfoDialog.InfoDialogBuilder connectBuilder;
	
	private final String EVENT_LICENSE = "License viewed";
	private final String EVENT_POLICY = "Privacy Policy viewed";
	private final String YES = "yes";
	private SharedPreferences prefs ;
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.legal,container, false);
		vReadLicenseBtn = (Button) v.findViewById(R.id.btn_readLicense) ;
		vReadPolicyBtn = (Button) v.findViewById(R.id.btn_readPolicy) ;
		vAgreementCheckBox = (CheckBox) v.findViewById(R.id.checkOne);
		
		vAgreementCheckBox.setTypeface(PrintHelper.tf);
		
		vReadLicenseBtn.setOnClickListener(new HelpClickListener("eula")) ;
		vReadPolicyBtn.setOnClickListener(new HelpClickListener("privacy"));
			
		
		
		if (getActivity().getApplicationContext().getPackageName().contains("dm") || getActivity().getApplicationContext().getPackageName().contains("wmc")) {
			vAgreementCheckBox.setVisibility(View.GONE);
		} else {
			vAgreementCheckBox.setVisibility(View.VISIBLE);
		}
		vAgreementCheckBox.setChecked(((WifiNewSettingFragmentActivity)getActivity()).isAgreementChecked());
		if(prefs.getBoolean(WifiNewSettingFragmentActivity.ENABLE_ALLOW_COOKIES, false)){
			vAgreementCheckBox.setClickable(true);
		} else {
			vAgreementCheckBox.setClickable(false);
			vAgreementCheckBox.setButtonDrawable(R.drawable.checkbox_disable);
		}
		
		
		
		vAgreementCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				((WifiNewSettingFragmentActivity)getActivity()).setAgreementChecked(isChecked) ;
				
			}
		}) ;
		
		return  v;
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

	
	
	class HelpClickListener implements OnClickListener {

		private String strExtra;
		
		public HelpClickListener(String strExtra) {
			this.strExtra = strExtra;
		}
		
		@Override
		public void onClick(View v) {
			if (!Connection.isConnected(getActivity()))
			{
				connectBuilder = new InfoDialog.InfoDialogBuilder(getActivity());
				connectBuilder.setTitle("");
				connectBuilder.setMessage(getString(R.string.nointernetconnection));
				connectBuilder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						connectBuilder = null;
					}
				});
				connectBuilder.setNegativeButton("", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
				connectBuilder.setCancelable(false);
				connectBuilder.create().show();
			} else {
				if(strExtra.equals("eula")){
					((WifiNewSettingFragmentActivity)getActivity()).putAttr(EVENT_LICENSE, YES) ;
				} else if (strExtra.equals("privacy")){
					((WifiNewSettingFragmentActivity)getActivity()).putAttr(EVENT_POLICY, YES);
				}
				Intent myIntent = new Intent(getActivity(), HelpActivity.class);
				myIntent.putExtra(strExtra, true);
				startActivity(myIntent);
			}
		}
		
	}
	
	
	
	

}
