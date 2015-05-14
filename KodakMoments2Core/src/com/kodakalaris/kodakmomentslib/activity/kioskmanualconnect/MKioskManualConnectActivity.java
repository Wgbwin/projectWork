package com.kodakalaris.kodakmomentslib.activity.kioskmanualconnect;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.zxing.client.android.wifi.WifiConfigManager;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.kioskconnection.MKioskConnectionActivity;
import com.kodakalaris.kodakmomentslib.activity.kioskscanconnect.MKioskScanConnectActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.WifiListAdapter;
import com.kodakalaris.kodakmomentslib.manager.KioskManager;
import com.kodakalaris.kodakmomentslib.util.ConnectionUtil;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.GeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;


public class MKioskManualConnectActivity extends BaseKioskManualConnectActivity {
	private static final String TAG = "MKioskManualConnectActivity";
	
	private Button vBtnConnect;
	private EditText vEtxtPassword;
	private Spinner vSpinnerSsid;
	private ViewGroup vVgroupWaiting;
	private WifiListAdapter mAdapter;
	private List<String> mSsidList;
	private MActionBar vActionBar;
	
	private BroadcastReceiver mScanReceiver;//receive wifi scan result
	private BroadcastReceiver mConnectReceiver;//receive wifi connect result
	private WifiManager mWifiManager;
	private Runnable mConnectingRunnable;
	private boolean mIsRegistedScanReceiver=false;
	private boolean mIsRegistedConnectReceiver=false;
	private boolean mConnecting = false;
	private GeneralAlertDialogFragment mDialogPoorLink;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_kiosk_manul_connect);
		
		vBtnConnect = (Button) findViewById(R.id.btn_connect);
		vEtxtPassword = (EditText) findViewById(R.id.etxt_password);
		vSpinnerSsid = (Spinner) findViewById(R.id.spinner_wifi_ssid);
		vVgroupWaiting = (ViewGroup) findViewById(R.id.vgroup_pbar);
		vActionBar = (MActionBar) findViewById(R.id.actionbar);
		
		mSsidList = new ArrayList<String>();
		mAdapter = new WifiListAdapter(this,mSsidList);
		vSpinnerSsid.setAdapter(mAdapter);
		
		mWifiManager = (WifiManager) MKioskManualConnectActivity.this.getSystemService(Context.WIFI_SERVICE);
		
		vSpinnerSsid.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		
		vBtnConnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String ssid = getCurrentSelectedWifiSsid();
				if (ssid == null) {
					//not valid ssid
					new GeneralAlertDialogFragment(MKioskManualConnectActivity.this)
						.setMessage(R.string.KioskManualInput_validwifiinfo)
						.setPositiveButton(R.string.Common_OK, null)
						.show(getSupportFragmentManager(), "");
				} else {
					try {
						if (mScanReceiver != null && mIsRegistedScanReceiver) {
							unregisterReceiver(mScanReceiver);
							mIsRegistedScanReceiver = false;
						}
					} catch (Exception e) {
						Log.e(TAG,"unregister receiver error",e);
					}
					
					if (!mConnecting) {
						mConnecting = true;
						
						//save wifi enable state before change wifi network
						//if enabled,we need to enable wifi after kiosk connect work flow finished
						KioskManager.getInstance().isWifiEnabledBeforeConnectKioskWifi = mWifiManager.isWifiEnabled();
						
						Log.i(TAG,"try to connect wifi " + ssid);
						if("".equals(vEtxtPassword.getText().toString())){
							WifiConfigManager.configure(mWifiManager, getCurrentSelectedWifiSsid(), null, null);
						}else{
							WifiConfigManager.configure(mWifiManager, getCurrentSelectedWifiSsid(), vEtxtPassword.getText().toString(), "WPA");
						}
						new Thread(mConnectingRunnable).start();
					}
				}
			}
		});
		
		vActionBar.setOnLeftButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mScanReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				List<ScanResult> list = mWifiManager.getScanResults();
				Log.i(TAG,"Receive wifi scan result");
				mSsidList = new ArrayList<String>();
				for (ScanResult scanResult : list) {
					//check is kiosk wifi
					if(scanResult.SSID.contains(".kodak.")){
						mSsidList.add(scanResult.SSID);
					}
				}
				
				if(mSsidList.size()>0){
					Log.i(TAG,"found avaiable wifi");
					scanHandler.sendEmptyMessage(SCAN_FINISH);
					
					mAdapter.setList(mSsidList);
					mAdapter.notifyDataSetChanged();
					dismissWaitingDialog();
				}else{
					Log.i(TAG,"no network found");
					new GeneralAlertDialogFragment(MKioskManualConnectActivity.this)
						.setMessage(R.string.KioskManualInput_nonetworkfound)
						.setPositiveButton(R.string.Common_OK, new BaseGeneralAlertDialogFragment.OnClickListener() {
							
							@Override
							public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
								finish();
							}
						})
						.show(getSupportFragmentManager(), "");
				}
			}
		};
		
		mConnectReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = cm.getActiveNetworkInfo();
				
				if(ConnectionUtil.isConnectedKioskWifi(context) && info.getState().equals(NetworkInfo.State.CONNECTED)){
					Log.i(TAG,"Reveiver called,networkinfo.state.connected is true");
					Intent des = new Intent(MKioskManualConnectActivity.this, MKioskConnectionActivity.class);
					startActivity(des);
					finish();
				}
			}
		};
		
		mConnectingRunnable = new Runnable() {
			
			@Override
			public void run() {
				Log.i(TAG,"new thread(connecting wifi) start...");
				final int TIMEOUT = 30;
				int time = 0;
				connectingHandler.sendEmptyMessage(START);
				ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				while(mConnecting){
					if(ConnectionUtil.isConnectedKioskWifi(MKioskManualConnectActivity.this)){
						State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
						if(wifi.equals(NetworkInfo.State.CONNECTED)){
							connectingHandler.sendEmptyMessage(SUCCESS);
						} else if(time>TIMEOUT){
							connectingHandler.sendEmptyMessage(FAILED);
						}
					}else if(time>TIMEOUT){
						connectingHandler.sendEmptyMessage(FAILED);
					}
					
					try {
						Thread.sleep(500);
						time++;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		boolean isNeedDisablePoorNetworkAvoidance = KioskManager.isNeedDisablePoorNetworkAvoidance(this);
		if (ConnectionUtil.isConnectedKioskWifi(this)) {
			Intent intent = new Intent(this,MKioskConnectionActivity.class);
			startActivity(intent);
			finish();
		} else if (!isNeedDisablePoorNetworkAvoidance) {
			scanHandler.sendEmptyMessage(SCANING);
			registerReceiver(mScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			mIsRegistedScanReceiver = true;
			registerReceiver(mConnectReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
			mIsRegistedConnectReceiver = true;
			
			mWifiManager.startScan();
			scanHandler.sendEmptyMessage(SCANING);
		}
		
		if (isNeedDisablePoorNetworkAvoidance) {
			if(mDialogPoorLink == null){
				mDialogPoorLink = new GeneralAlertDialogFragment(MKioskManualConnectActivity.this, false);
				mDialogPoorLink.setMessage(R.string.KioskScanConnect_disablePoorNetworkAvoidance);
				mDialogPoorLink.setPositiveButton(R.string.Common_Settings, new BaseGeneralAlertDialogFragment.OnClickListener() {
										
										@Override
										public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
											Intent intent = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
											startActivity(intent);
										}
									});
				mDialogPoorLink.setNegativeButton(R.string.Common_Cancel, new BaseGeneralAlertDialogFragment.OnClickListener() {
										
										@Override
										public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
											MKioskScanConnectActivity.goToTopLevelActivity(MKioskManualConnectActivity.this);
										}
									});
			}
			
			if(!mDialogPoorLink.isShowing()){
				mDialogPoorLink.show(getSupportFragmentManager(), "mDialogPoorLink");
			}
		}else if(mDialogPoorLink != null && mDialogPoorLink.isShowing()){
			mDialogPoorLink.dismiss();
		}
	}
	
	@Override
	protected void onPause() {
		try {
			if(mScanReceiver != null && mIsRegistedScanReceiver){
				unregisterReceiver(mScanReceiver);
				mIsRegistedScanReceiver = false;
			}
			if(mConnectReceiver != null && mIsRegistedConnectReceiver){
				unregisterReceiver(mConnectReceiver);
				mIsRegistedConnectReceiver = false;
			}
		} catch (Exception e) {
			Log.e(TAG,"unregister receiver error",e);
		}
		super.onPause();
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
	
	private String getCurrentSelectedWifiSsid() {
		if (vSpinnerSsid != null) {
			Object ob = vSpinnerSsid.getSelectedItem();
			if (ob != null) {
				return (String) ob;
			}
		}
		
		return null;
	}
	
	private void showWaitingDialog() {
		vVgroupWaiting.setVisibility(View.VISIBLE);
	}
	
	private void dismissWaitingDialog() {
		vVgroupWaiting.setVisibility(View.INVISIBLE);
	}
	
	static final int SCANING = 0;
	static final int SCAN_FINISH = 1;
	private Handler scanHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch(action){
			case SCANING:
				showWaitingDialog();
				vEtxtPassword.setEnabled(false);
				vSpinnerSsid.setEnabled(false);
				break;
			case SCAN_FINISH:
				dismissWaitingDialog();
				vEtxtPassword.setEnabled(true);
				vSpinnerSsid.setEnabled(true);
				break;
			}
		}
		
	};
	
	static final int SUCCESS = 0;
	static final int FAILED = 1;
	static final int START = 2;
	private Handler connectingHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case START:
				Log.i(TAG,"Connect wifi start");
				showWaitingDialog();
				vEtxtPassword.setEnabled(false);
				vSpinnerSsid.setEnabled(false);
				break;
			case FAILED:
				Log.i(TAG,"Connect wifi failed");
				mConnecting = false;
				dismissWaitingDialog();
				vEtxtPassword.setEnabled(true);
				vSpinnerSsid.setEnabled(true);
				
				//show error dialog
				new GeneralAlertDialogFragment(MKioskManualConnectActivity.this)
					.setMessage(R.string.KioskManualInput_nonetworkfound)
					.setPositiveButton(R.string.Common_OK, new BaseGeneralAlertDialogFragment.OnClickListener() {
						
						@Override
						public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
							if(!mIsRegistedScanReceiver){
								registerReceiver(mScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
								mIsRegistedScanReceiver = true;
							}
							
							if(!mIsRegistedConnectReceiver){
								registerReceiver(mConnectReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
								mIsRegistedConnectReceiver = true;
							}
						}
					})
					.show(getSupportFragmentManager(), "error");
					
				break;
			case SUCCESS:
				Log.i(TAG,"Connect wifi success");
				mConnecting = false;
				dismissWaitingDialog();
				vEtxtPassword.setEnabled(true);
				vSpinnerSsid.setEnabled(true);
				Intent intent = new Intent(MKioskManualConnectActivity.this,MKioskConnectionActivity.class);
				startActivity(intent);
				finish();
				break;
			default:
				break;
			}
		};
	};
}
