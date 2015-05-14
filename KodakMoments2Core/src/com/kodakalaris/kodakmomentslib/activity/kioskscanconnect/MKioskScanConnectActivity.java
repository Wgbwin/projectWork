package com.kodakalaris.kodakmomentslib.activity.kioskscanconnect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.Result;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.wifi.WifiConfigManager;
import com.google.zxing.client.result.ParsedResult;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.home.MHomeActivity;
import com.kodakalaris.kodakmomentslib.activity.kioskconnection.MKioskConnectionActivity;
import com.kodakalaris.kodakmomentslib.activity.kioskmanualconnect.MKioskManualConnectActivity;
import com.kodakalaris.kodakmomentslib.manager.KioskManager;
import com.kodakalaris.kodakmomentslib.util.ConnectionUtil;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.GeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;

public class MKioskScanConnectActivity extends BaseKioskScanConnectActivity{
	private static final String TAG = "MKioskScanConnectActivity";
	
	private View vViewResult;
	private ViewGroup vVgroupBottomBar;
	private Button vBtnManualConnect;
	private TextView vTxtStatus;
	private TextView vTxtInstruction;
	private MActionBar vActionBar;
	private GeneralAlertDialogFragment mDialogPoorLink;
	public String mSsid = "";
	
	private boolean mIsConnecting = false;

	private BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && ConnectionUtil.isConnectedKioskWifi(context))
			{
				connectingHandler.sendEmptyMessage(CONNECT_SUCCESS);
			}
		}
	};

	private static final int TIME_OUT = 60000;
	private final static int CONNECT_SUCCESS = 0;
	private final static int CONNECT_FAILD = 1;
	private final static int CONNECT_START = 2;
	private Handler connectingHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case CONNECT_SUCCESS:
				mIsConnecting = false;
				vTxtStatus.setText(getString(R.string.KioskScanConnect_scanconnectedtokiosk) + "\n" + mSsid);
				postDelayed(new Runnable() {
					
					@Override
					public void run() {
						Intent intent = new Intent(MKioskScanConnectActivity.this, MKioskConnectionActivity.class);
						finish();
						startActivity(intent);
					}
				}, 2000);
				break;
			case CONNECT_FAILD:	
				mIsConnecting = false;
				if (!MKioskScanConnectActivity.this.isFinishing()) {
					new GeneralAlertDialogFragment(MKioskScanConnectActivity.this)
					.setMessage(R.string.KioskManualInput_nonetworkfound)
					.setPositiveButton(R.string.Common_OK, new BaseGeneralAlertDialogFragment.OnClickListener() {
						
						@Override
						public void onClick(BaseGeneralAlertDialogFragment dialogFragment ,View v) {
							restartPreviewAfterDelay(100);
						}
					})
					.show(getSupportFragmentManager(), "CONNECT_FAILD");
				}
				break;
			case CONNECT_START:	
				mIsConnecting = true;
				viewfinderView.setVisibility(View.GONE);
				vTxtInstruction.setVisibility(View.GONE);
				vViewResult.setVisibility(View.VISIBLE);
				
				//if connect time out, show error dialog
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						int time = 0;
						while(mIsConnecting){
							if(time>=TIME_OUT){
								sendEmptyMessage(CONNECT_FAILD);
								break;
							}
							time = time + 5000;
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
				break;
			}
		};
	};
	

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		vViewResult = findViewById(R.id.view_result);
		vTxtInstruction = (TextView) findViewById(R.id.txt_instruction);
		vViewResult.setVisibility(android.view.View.INVISIBLE);
		vTxtStatus = (TextView) findViewById(R.id.txt_ssid);
		vBtnManualConnect = (Button) findViewById(R.id.btn_manual_connect);
		vVgroupBottomBar = (ViewGroup) findViewById(R.id.vgroup_bottom_bar);
		vActionBar = (MActionBar) findViewById(R.id.actionbar);
		
		vActionBar.setOnLeftButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		vBtnManualConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsConnecting = false;
				Intent intent = new Intent(MKioskScanConnectActivity.this, MKioskManualConnectActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		
	}
	
	@Override
	protected int getContentViewLayoutId(){
		return R.layout.activity_m_kiosk_scan_connect;
	}
	
	@Override
	protected int getViewFinderViewId() {
		return R.id.view_viewfinder;
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		if(KioskManager.isNeedDisablePoorNetworkAvoidance(this)){
			if(mDialogPoorLink == null){
				mDialogPoorLink = new GeneralAlertDialogFragment(MKioskScanConnectActivity.this, false);
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
						goToTopLevelActivity(MKioskScanConnectActivity.this);
					}
				});
				
			}
			
			if(!mDialogPoorLink.isShowing()){
				mDialogPoorLink.show(getSupportFragmentManager(), "mDialogPoorLink");
			}
		}else if(mDialogPoorLink != null && mDialogPoorLink.isShowing()){
			mDialogPoorLink.dismiss();
		}
		
		// Turn on WiFi now to prevent waiting later
		Thread enableWIFI = new Thread() {
			public void run() {
				WifiManager wifiManager = (WifiManager) MKioskScanConnectActivity.this.getSystemService(Context.WIFI_SERVICE);
				wifiManager.setWifiEnabled(true);
			}
		};
		if (ConnectionUtil.isConnectedKioskWifi(this)) {
			Intent intent = new Intent(this, MKioskConnectionActivity.class);
			startActivity(intent);
			finish();
		} else {
			enableWIFI.start();
			this.registerReceiver(this.mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
		
		Thread thrd = new Thread() {
			public void run() {
				try {
					sleep(10000);
					if(handler != null){
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								showBottomBar();
							}
						});
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thrd.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			unregisterReceiver(mReceiver);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	protected int getPreviewId() {
		return R.id.view_preview;
	}
	

	public void hideBottomBar() {
		if(vVgroupBottomBar != null){
			vVgroupBottomBar.setVisibility(View.GONE);
		}
	}
	
	public void showBottomBar() {
		if(vVgroupBottomBar != null){
			vVgroupBottomBar.setVisibility(View.VISIBLE);
		}
	}

	// Put up our own UI for how to handle the decoded contents.
	@Override
	protected void handleDecodeWifi(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
		ParsedResult parsedRawResult = parseResult(rawResult);
		if (!"WIFI".equals(parsedRawResult.getType().toString())) {
			new GeneralAlertDialogFragment(MKioskScanConnectActivity.this)
					.setMessage(R.string.KioskScanConnect_notavalidbarcode)
					.setPositiveButton(R.string.Common_OK, new BaseGeneralAlertDialogFragment.OnClickListener() {
						
						@Override
						public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
							restartPreviewAfterDelay(0);
						}
					})
					.show(getSupportFragmentManager(), "");
		} else {
			ImageView barcodeImageView = (ImageView) findViewById(R.id.img_barcode);

			if (barcode == null) {
				barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(
						getResources(), R.drawable.icon));
			} else {
				Log.i(TAG, "Width: " + barcode.getWidth() + " Height: "
						+ barcode.getHeight());
				barcodeImageView.setImageBitmap(barcode);
			}

			boolean failure = false;
			try {
				String[] result = rawResult.getText().toString().split(";");
				String ssid = null, networkType = null, password = null;
				ssid = result[0].split(":")[2].toString();
				if (result.length == 1) {
				} else if (result.length == 2) {
					int index = result[1].indexOf(":");
					if (index != -1) {
						networkType = result[1].substring(index + 1);
					}
				} else {
					int index1 = result[1].indexOf(":");
					if (index1 != -1) {
						networkType = result[1].substring(index1 + 1);
					}
					int index2 = result[2].indexOf(":");
					if (index2 != -1) {
						password = result[2].substring(index2 + 1);
					}
				}
				vTxtStatus.setText(getString(R.string.KioskScanConnect_scanconnectingtokiosk)
						+ "\n" + ssid);
				if (ssid.contains(".kodak.")) {
					Log.d(TAG, "Configuring WiFi: " + ssid);
					WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					WifiConfigManager.configure(wifiManager, ssid, password, networkType);
					connectingHandler.sendEmptyMessage(CONNECT_START);
					Thread thrd = new Thread() {
						public void run() {
							try {
								sleep(10000);
								if (handler != null) {
									handler.post(new Runnable() {

										@Override
										public void run() {
											showBottomBar();
										}
									});
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
					thrd.start();
				} else {
					failure = true;
				}
			} catch (Exception ex) {
				failure = true;
			}
			if (failure) {
				new GeneralAlertDialogFragment(MKioskScanConnectActivity.this)
					.setMessage(R.string.KioskScanConnect_notavalidkodakwirelessnetwork)
					.setPositiveButton(R.string.Common_OK, new BaseGeneralAlertDialogFragment.OnClickListener() {
						
						@Override
						public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
							Intent myIntent = new Intent(MKioskScanConnectActivity.this, MKioskManualConnectActivity.class);
							startActivity(myIntent);
							finish();
						}
					})
					.show(getSupportFragmentManager(), "");
				
			}
		}
	}
	
	@Override
	public void restartPreviewAfterDelay(long delayMS){
		super.restartPreviewAfterDelay(delayMS);
		
		viewfinderView.setVisibility(View.VISIBLE);
		vTxtInstruction.setVisibility(View.VISIBLE);
		vViewResult.setVisibility(View.INVISIBLE);
	}
	
	public static void goToTopLevelActivity(Activity activity){
		Intent myIntent = new Intent(activity,MHomeActivity.class);
		myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(myIntent);
		activity.finish();
	}
}
