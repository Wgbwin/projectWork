/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kodak.rss.tablet.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.wifi.WifiConfigManager;
import com.google.zxing.client.result.ParsedResult;
import com.kodak.rss.core.util.ConnectionUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.db.ImageSelectionDatabase;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public class WiFiQRCodeScanActivity extends BaseCaptureActivity
{
	private static final String TAG = "WiFiQRCodeScanActivity";
	private View resultView;
	private Button btnBack;
	private ViewGroup bottomBar;
	private Button btnManualConnect;
	private TextView tvStatus;
	private TextView tvInstruction;
	private InfoDialog poorLinkDialog;
	public String ssid = "";
	
	private boolean connecting = false;

	private BroadcastReceiver receiver = new BroadcastReceiver()
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
				connecting = false;
				tvStatus.setText(getString(R.string.scanconnectedtokiosk) + "\n" + ssid);
				postDelayed(new Runnable() {
					
					@Override
					public void run() {
						Intent intent = new Intent(WiFiQRCodeScanActivity.this, WiFiConnectionActivity.class);
						finish();
						startActivity(intent);
					}
				}, 2000);
				break;
			case CONNECT_FAILD:	
				connecting = false;
				if (!WiFiQRCodeScanActivity.this.isFinishing()) {
					new InfoDialog.Builder(WiFiQRCodeScanActivity.this)
					.setMessage(R.string.nonetworkfound)
					.setPositiveButton(R.string.d_ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							restartPreviewAfterDelay(100);
						}
					})
					.create()
					.show();
				}
				break;
			case CONNECT_START:	
				connecting = true;
				viewfinderView.setVisibility(View.GONE);
				btnBack.setVisibility(View.INVISIBLE);
				tvInstruction.setVisibility(View.GONE);
				resultView.setVisibility(View.VISIBLE);
				
				//if connect time out, show error dialog
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						int time = 0;
						while(connecting){
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
		
		resultView = findViewById(R.id.result_view);
		tvInstruction = (TextView) findViewById(R.id.tv_instruction);
		resultView.setVisibility(android.view.View.INVISIBLE);
		tvStatus = (TextView) findViewById(R.id.ssidTV);
		btnBack = (Button) findViewById(R.id.btn_back);
		btnManualConnect = (Button) findViewById(R.id.btn_manual_connect);
		bottomBar = (ViewGroup) findViewById(R.id.bottom_bar);
		
		btnManualConnect.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				connecting = false;
				Intent intent = new Intent(WiFiQRCodeScanActivity.this, WifiManualInputActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		btnBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		
	}
	
	@Override
	protected int getContentViewLayoutId(){
		return R.layout.activity_capture;
	}
	
	@Override
	protected int getViewFinderViewId() {
		return R.id.viewfinder_view;
	}
	

	@Override
	protected void onResume()
	{
		super.onResume();
		if(RssTabletApp.getInstance().isNeedDisablePoorNetworkAvoidance()){
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
								goToTopLevelActivity(WiFiQRCodeScanActivity.this);
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
		
		// Turn on WiFi now to prevent waiting later
		Thread enableWIFI = new Thread()
		{
			public void run()
			{
				WifiManager wifiManager = (WifiManager) WiFiQRCodeScanActivity.this.getSystemService(Context.WIFI_SERVICE);
				wifiManager.setWifiEnabled(true);
			}
		};
		if (ConnectionUtil.isConnectedKioskWifi(this))
		{
			Intent intent = new Intent(this, WiFiConnectionActivity.class);
			startActivity(intent);
			finish();
		}
		else
		{
			enableWIFI.start();
			this.registerReceiver(this.receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
		
		Thread thrd = new Thread()
		{
			public void run()
			{
				try
				{
					sleep(10000);
					if(handler != null){
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								showBottomBar();
							}
						});
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		thrd.start();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		try
		{
			unregisterReceiver(receiver);
		}
		catch (IllegalArgumentException ex)
		{
			ex.printStackTrace();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	protected int getPreviewId(){
		return R.id.preview_view;
	}
	

	public void hideBottomBar(){
		if(bottomBar != null){
			bottomBar.setVisibility(View.GONE);
		}
	}
	
	public void showBottomBar(){
		if(bottomBar != null){
			bottomBar.setVisibility(View.VISIBLE);
		}
	}

	// Put up our own UI for how to handle the decoded contents.
	@Override
	protected void handleDecodeWifi(Result rawResult,
			ResultHandler resultHandler, Bitmap barcode) {
		ParsedResult parsedRawResult = parseResult(rawResult);
		if (!"WIFI".equals(parsedRawResult.getType().toString())) {
			InfoDialog dialog = new InfoDialog.Builder(this)
					.setMessage(R.string.notavalidbarcode)
					.setPositiveButton(R.string.d_ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									restartPreviewAfterDelay(0);
								}
							}).create();
			dialog.show();
		} else {
			ImageView barcodeImageView = (ImageView) findViewById(R.id.barcode_image_view);

			if (barcode == null) {
				barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(
						getResources(), R.drawable.temp));
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
				tvStatus.setText(getString(R.string.scanconnectingtokiosk)
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
				InfoDialog dialog = new InfoDialog.Builder(this)
						.setMessage(R.string.notavalidkodakwirelessnetwork)
						.setPositiveButton(R.string.d_ok,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										Intent myIntent = new Intent(WiFiQRCodeScanActivity.this, WifiManualInputActivity.class);
										startActivity(myIntent);
										finish();
									}
								}).create();
				dialog.show();
			}
		}
	}
	
	
	
	@Override
	public void restartPreviewAfterDelay(long delayMS){
		super.restartPreviewAfterDelay(delayMS);
		
		viewfinderView.setVisibility(View.VISIBLE);
		btnBack.setVisibility(View.VISIBLE);
		tvInstruction.setVisibility(View.VISIBLE);
		resultView.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void startOver() {
		new ImageSelectionDatabase(this).handleDeleteAllUrisWiFi();
		super.startOver();
	}

	public static void goToTopLevelActivity(Activity activity){
		Intent myIntent = new Intent(activity,MainActivity.class);
		myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(myIntent);
		activity.finish();
	}
	
}
