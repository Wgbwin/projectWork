package com.kodakalaris.kodakmomentslib.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.WindowManager;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.util.ConnectionUtil;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.mobile.GeneralAlertDialogFragment;

/**
 * The activity which extends from this class will detect the network.
 * When there is no network available, it will show a dialog.
 * @author Robin.Qian
 *
 */
public class BaseNetActivity extends BaseActivity{
	protected BroadcastReceiver mNetworkChangeReceiver;
	protected boolean mIsNetworkChangeReceiverRegistered = false;
	protected GeneralAlertDialogFragment mNoNetworkDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mNoNetworkDialog = new GeneralAlertDialogFragment(this);
		mNoNetworkDialog.setMessage(R.string.Task_No_Internet);
		mNoNetworkDialog.setPositiveButton(R.string.Common_OK, null);
		
		mNetworkChangeReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				detectNetWork();
			}
		};
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		registerReceiver(mNetworkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		try {
			unregisterReceiver(mNetworkChangeReceiver);
		} catch (Exception e) {
			Log.e(BaseNetActivity.class.getSimpleName(),"unregister networkReceiver error",e);
		}
	}
	
	protected boolean isNetworkAvaiable() {
		return ConnectionUtil.isConnected(this);
	}
	
	protected void detectNetWork(){
		if(!isNetworkAvaiable()){
			showNoNetworkDialog();
		} else {
			dismissNoNetworkDialog();
		}
		
	}
	
}
