package com.kodak.rss.tablet.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.zxing.client.android.wifi.WifiConfigManager;
import com.kodak.rss.core.util.ConnectionUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.db.ImageSelectionDatabase;
import com.kodak.rss.tablet.view.dialog.DialogSsidSelector;
import com.kodak.rss.tablet.view.dialog.DialogSsidSelector.SsidAdapter;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class WifiManualInputActivity extends BaseActivity{
	private final static String TAG = WifiManualInputActivity.class.getSimpleName();
	
	private Button btnConnect;
	private Button btnBack;
	private EditText etPassword;
	private Button spinnerSsid;
	private SsidAdapter adapter;
	private List<String> ssidList;
	private ProgressBar progressBar;
	private DialogSsidSelector dialogSsidSelector;
	private BroadcastReceiver scanReceiver;//receive wifi scan result
	private BroadcastReceiver connectReceiver;//receive wifi connect result
	private WifiManager wifiManager;
	private Runnable connectingRunnable;
	private boolean isRegistedScanReceiver=false;
	private boolean isRegistedConnectReceiver=false;
	private boolean connecting = false;
	private InfoDialog poorLinkDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_manual_input);
		
		btnConnect = (Button) findViewById(R.id.btn_connect);
		btnBack = (Button) findViewById(R.id.btn_back);
		etPassword = (EditText) findViewById(R.id.et_password);
		TextUtil.addEmojiFilter(etPassword);
		spinnerSsid = (Button) findViewById(R.id.spinner_ssid);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		ssidList = new ArrayList<String>();
		adapter = new SsidAdapter(this,ssidList);
		
		wifiManager = (WifiManager) WifiManualInputActivity.this.getSystemService(Context.WIFI_SERVICE);
		
		spinnerSsid.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(dialogSsidSelector == null){
					dialogSsidSelector = new DialogSsidSelector(WifiManualInputActivity.this, adapter,new OnItemClickListener() {
						
						@Override
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							spinnerSsid.setText(ssidList.get(position));
							dialogSsidSelector.dismiss();
						}
					});
				}
				dialogSsidSelector.show();
			}
		});
		
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		btnConnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if("".equals(spinnerSsid.getText().toString())){
					//not valid ssid
					InfoDialog dialog = new InfoDialog.Builder(WifiManualInputActivity.this)
											.setMessage(R.string.validwifiinfo)
											.setPositiveButton(R.string.d_ok, null)
											.create();
					dialog.show();
				}else{
					try {
						if(scanReceiver != null && isRegistedScanReceiver){
							unregisterReceiver(scanReceiver);
							isRegistedScanReceiver = false;
						}
					} catch (Exception e) {
						Log.e(TAG,"unregister receiver error",e);
					}
					
					if(!connecting){
						connecting = true;
						
						//save wifi enable state before change wifi network
						//if enabled,we need to enable wifi after kiosk connect work flow finished
						RssTabletApp.getInstance().isWifiEnabledBeforeConnectKioskWifi = wifiManager.isWifiEnabled();
						
						Log.i(TAG,"try to connect wifi "+spinnerSsid.getText().toString());
						if("".equals(etPassword.getText().toString())){
							WifiConfigManager.configure(wifiManager, spinnerSsid.getText().toString(), null, null);
						}else{
							WifiConfigManager.configure(wifiManager, spinnerSsid.getText().toString(), etPassword.getText().toString(), "WPA");
						}
						new Thread(connectingRunnable).start();
					}
				}
			}
		});
		
		scanReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				List<ScanResult> list = wifiManager.getScanResults();
				Log.i(TAG,"Receive wifi scan result");
				ssidList = new ArrayList<String>();
				for (ScanResult scanResult : list) {
					//check is kiosk wifi
					if(scanResult.SSID.contains(".kodak.")){
						ssidList.add(scanResult.SSID);
					}
				}
				
				if(ssidList.size()>0){
					Log.i(TAG,"found avaiable wifi");
					scanHandler.sendEmptyMessage(SCAN_FINISH);
					
					//if the current spinner ssid (or null) is not in the new scan result, change it 
					if(!ssidList.contains(spinnerSsid.getText().toString())){
						spinnerSsid.setText(ssidList.get(0));
					}
					
					adapter.setList(ssidList);
					adapter.notifyDataSetChanged();
					progressBar.setVisibility(View.INVISIBLE);
				}else{
					Log.i(TAG,"no network found");
					InfoDialog dialog = new InfoDialog.Builder(WifiManualInputActivity.this)
												.setMessage(R.string.nonetworkfound)
												.setPositiveButton(R.string.d_ok, new DialogInterface.OnClickListener() {
													
													@Override
													public void onClick(DialogInterface dialog, int which) {
														finish();
													}
												})
												.create();
					dialog.show();
				}
			}
		};
		
		connectReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = cm.getActiveNetworkInfo();
				
				if(ConnectionUtil.isConnectedKioskWifi(context) && info.getState().equals(NetworkInfo.State.CONNECTED)){
					Log.i(TAG,"Reveiver called,networkinfo.state.connected is true");
					Intent intent2= new Intent(WifiManualInputActivity.this,WiFiConnectionActivity.class);
					startActivity(intent2);
					finish();
				}
			}
		};
		
		connectingRunnable = new Runnable() {
			
			@Override
			public void run() {
				Log.i(TAG,"new thread(connecting wifi) start...");
				final int TIMEOUT = 30;
				int time = 0;
				connectingHandler.sendEmptyMessage(START);
				ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				while(connecting){
					if(ConnectionUtil.isConnectedKioskWifi(WifiManualInputActivity.this)){
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
		boolean isNeedDisablePoorNetworkAvoidance = getApp().isNeedDisablePoorNetworkAvoidance();
		if(ConnectionUtil.isConnectedKioskWifi(WifiManualInputActivity.this)){
			Intent intent = new Intent(WifiManualInputActivity.this,WiFiConnectionActivity.class);
			startActivity(intent);
			finish();
		}else if(!isNeedDisablePoorNetworkAvoidance){
			scanHandler.sendEmptyMessage(SCANING);
			registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			isRegistedScanReceiver = true;
			registerReceiver(connectReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
			isRegistedConnectReceiver = true;
			
			wifiManager.startScan();
			scanHandler.sendEmptyMessage(SCANING);
		}
		
		if(isNeedDisablePoorNetworkAvoidance){
			if(poorLinkDialog == null){
				poorLinkDialog = new InfoDialog.Builder(this).setMessage(R.string.disablePoorNetworkAvoidance)
						.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
								startActivity(intent);
							}
						})
						.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								poorLinkDialog.dismiss();
								WiFiQRCodeScanActivity.goToTopLevelActivity(WifiManualInputActivity.this);
							}
						})
						.create();
				poorLinkDialog.setCanceledOnTouchOutside(false);
				poorLinkDialog.setCancelable(false);
			}
			
			if(!poorLinkDialog.isShowing()){
				poorLinkDialog.show();
			}
		}else if(poorLinkDialog != null && poorLinkDialog.isShowing()){
			poorLinkDialog.dismiss();
		}
	}
	
	@Override
	protected void onPause() {
		try {
			if(scanReceiver != null && isRegistedScanReceiver){
				unregisterReceiver(scanReceiver);
				isRegistedScanReceiver = false;
			}
			if(connectReceiver != null && isRegistedConnectReceiver){
				unregisterReceiver(connectReceiver);
				isRegistedConnectReceiver = false;
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
	
	@Override
	public void startOver() {
		new ImageSelectionDatabase(this).handleDeleteAllUrisWiFi();
		super.startOver();
	}
	
	static final int SCANING = 0;
	static final int SCAN_FINISH = 1;
	private Handler scanHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch(action){
			case SCANING:
				progressBar.setVisibility(View.VISIBLE);
				etPassword.setEnabled(false);
				spinnerSsid.setEnabled(false);
				break;
			case SCAN_FINISH:
				progressBar.setVisibility(View.GONE);
				etPassword.setEnabled(true);
				spinnerSsid.setEnabled(true);
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
				progressBar.setVisibility(View.VISIBLE);
				etPassword.setEnabled(false);
				spinnerSsid.setEnabled(false);
				break;
			case FAILED:
				Log.i(TAG,"Connect wifi failed");
				connecting = false;
				progressBar.setVisibility(View.GONE);
				etPassword.setEnabled(true);
				spinnerSsid.setEnabled(true);
				
				//show error dialog
				InfoDialog dialog = new InfoDialog.Builder(WifiManualInputActivity.this)
										.setMessage(R.string.nonetworkfound)
										.setPositiveButton(R.string.d_ok, new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												if(!isRegistedScanReceiver){
													registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
													isRegistedScanReceiver = true;
												}
												
												if(!isRegistedConnectReceiver){
													registerReceiver(connectReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
													isRegistedConnectReceiver = true;
												}
											}
										})
										.create();
				dialog.show();
										
				break;
			case SUCCESS:
				Log.i(TAG,"Connect wifi success");
				connecting = false;
				progressBar.setVisibility(View.GONE);
				etPassword.setEnabled(true);
				spinnerSsid.setEnabled(true);
				Intent intent = new Intent(WifiManualInputActivity.this,WiFiConnectionActivity.class);
				startActivity(intent);
				finish();
				break;
			default:
				break;
			}
		};
	};
	
}
