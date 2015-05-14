package com.kodak.kodak_kioskconnect_n2r;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.AppContext;
import com.google.zxing.client.android.CaptureActivity;

public class WifiManualInputActivity extends Activity
{
	TextView status;
	WifiConnectionSurfaceView surface;
	ProgressDialog dialog;
	Thread thrd;
	Button ssidButton;
	EditText passwordET;
	Button settingsButton;
	Button nextButton;
	Button backButton;
	TextView headerTV;
	TextView totalSelectedTV;
	Button infoButton;
	TextView version;
	TextView instructionsTV;
	TextView ssidTV;
	TextView passTV;
	List<WifiConfiguration> wifiNetworkList;
	ArrayList<String> wifiNetworks;
	ProgressBar progressBar;
	BroadcastReceiver scanReceiver = null;
	BroadcastReceiver connectingReceiver = null;
	BroadcastReceiver receiver = null;
	MyCustomAdapter adapter;
	InfoDialog.InfoDialogBuilder connRecBuilder;
	InfoDialog poorLinkDialog;
	boolean connecting;
	Runnable connectingRunnable;
	public class MyCustomAdapter extends ArrayAdapter<String>
	{
		public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<String> objects)
		{
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
			return getCustomView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			return getCustomView(position, convertView, parent);
		}

		public View getCustomView(int position, View convertView, ViewGroup parent)
		{
			// return super.getView(position, convertView, parent);
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.row, parent, false);
			TextView label = (TextView) row.findViewById(R.id.productSizeTextView);
			if (label != null)
			{
				if (wifiNetworks != null && wifiNetworks.size() > position)
				{
					label.setText(wifiNetworks.get(position).toString());
				}
				else
				{
					label.setText(getString(R.string.nonetworkfound));
				}
			}
			return row;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.wifimanualinput);
		settingsButton = (Button) findViewById(R.id.settingsButton);
		infoButton = (Button) findViewById(R.id.infoButton);
		nextButton = (Button) findViewById(R.id.nextButton);
		backButton = (Button) findViewById(R.id.backButton);
		instructionsTV = (TextView) findViewById(R.id.typeInCodeTV);
		headerTV = (TextView) findViewById(R.id.headerBarText);
		version = (TextView) findViewById(R.id.versionCopyrightTextView);
		totalSelectedTV = (TextView) findViewById(R.id.totalSelectedTextView);
		totalSelectedTV.setVisibility(android.view.View.GONE);
		headerTV.setText(getString(R.string.ManualConnect));
		settingsButton.setVisibility(android.view.View.INVISIBLE);
		nextButton.setText(getString(R.string.connect));
		ssidTV = (TextView)findViewById(R.id.ssidTextView);
		passTV = (TextView)findViewById(R.id.passwordTextView);
		instructionsTV.setTypeface(PrintHelper.tf);
		ssidTV.setTypeface(PrintHelper.tf);
		passTV.setTypeface(PrintHelper.tf);
		ssidButton = (Button) findViewById(R.id.ssidSpinner);

		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		nextButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (ssidButton.getText().toString().equals(""))
				{
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(WifiManualInputActivity.this);
					builder.setTitle(getString(R.string.validwifiinfo));
					builder.setMessage("");
					builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.setNegativeButton("", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							finish();
						}
					});
					builder.create().show();
				}
//				else if (passwordET.getText().toString().equals(""))
//				{
//					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(WifiManualInputActivity.this);
//					builder.setTitle(getString(R.string.validwifiinfo));
//					builder.setMessage("");
//					builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
//					{
//						@Override
//						public void onClick(DialogInterface dialog, int which)
//						{
//							dialog.dismiss();
//						}
//					});
//					builder.setNegativeButton("", new DialogInterface.OnClickListener()
//					{
//						@Override
//						public void onClick(DialogInterface dialog, int which)
//						{
//							dialog.dismiss();
//							finish();
//						}
//					});
//					builder.create().show();
//				}
				else
				{
					if (scanReceiver != null) {
						try {
							unregisterReceiver(scanReceiver);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					//if (connectingReceiver != null)
					if(!connecting)
					{
						connecting = true;
						//dialog = ProgressDialog.show(WifiManualInputActivity.this, "", getString(R.string.connectingtokiosk), true, false);
						WifiManager wifiManager = (WifiManager) WifiManualInputActivity.this.getSystemService(Context.WIFI_SERVICE);
						if("".equals(passwordET.getText().toString())){
							WifiConfigManager.configure(wifiManager, ssidButton.getText().toString(), null, null);
						}else{
							WifiConfigManager.configure(wifiManager, ssidButton.getText().toString(), passwordET.getText().toString(), "WPA");
						}
						//registerReceiver(connectingReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
						new Thread(connectingRunnable).start();
					}
					else
					{
						InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(WifiManualInputActivity.this);
						builder.setTitle(getString(R.string.validwifiinfo));
						builder.setMessage("");
						builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
							}
						});
						builder.setNegativeButton("", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								finish();
							}
						});
						builder.create().show();
					}
				}
			}
		});
		backButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(WiFiSelectWorkflowActivity.FindBackCamera()){
					Intent intent = new Intent(WifiManualInputActivity.this, CaptureActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}else{
					Intent intent = new Intent(WifiManualInputActivity.this, WiFiSelectWorkflowActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			}
		});
		passwordET = (EditText) findViewById(R.id.passwordEditText);
		AppContext.getApplication().setEmojiFilter(passwordET);
	}

	public Handler dialogUpdateHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			dialog.setMessage(PrintHelper.status);
		}
	};
	public Handler connectedHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			try
			{
				if (Connection.isConnectedKioskWifi(WifiManualInputActivity.this))
				{
					Log.i("ManualConnect", "connectedHandler called isConnectedWifi is true");
					dialog.dismiss();
					Intent intent = new Intent(WifiManualInputActivity.this, WiFiConnectionActivity.class);
					startActivity(intent);
				}
				else
				{
					dialog.dismiss();
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(WifiManualInputActivity.this);
					builder.setTitle(getString(R.string.problemconnectingwifi));
					builder.setMessage("");
					builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.setNegativeButton("", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							finish();
						}
					});
					builder.create().show();
				}
			}
			catch (Exception ex)
			{
			}
		}
	};
	
	static final int SUCCESS = 0;
	static final int FAILED = 1;
	static final int START = 2;
	public Handler connectingHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case START:
				progressBar.setVisibility(View.VISIBLE);
				break;
			case SUCCESS:
				connecting = false;
				progressBar.setVisibility(View.INVISIBLE);
				Intent myIntent = new Intent(WifiManualInputActivity.this, WiFiConnectionActivity.class);
				startActivity(myIntent);
				break;
			case FAILED:
				connecting = false;
				if(connRecBuilder == null){
					progressBar.setVisibility(View.INVISIBLE);
					connRecBuilder = new InfoDialog.InfoDialogBuilder(WifiManualInputActivity.this);
					connRecBuilder.setTitle(getString(R.string.nonetworkfound));
					connRecBuilder.setMessage("");
					connRecBuilder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							progressBar.setVisibility(View.INVISIBLE);
							dialog.dismiss();
							/*if(WiFiSelectWorkflowActivity.FindBackCamera()){
								Intent intent = new Intent(WifiManualInputActivity.this, CaptureActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}else{
								Intent intent = new Intent(WifiManualInputActivity.this, WiFiSelectWorkflowActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}*/
							registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
							registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
							connRecBuilder = null;
						}
					});
					connRecBuilder.setNegativeButton("", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							finish();
						}
					});
					connRecBuilder.setCancelable(false);
					connRecBuilder.create().show();
				}
				break;
			}
		}
		
	};

	@Override
	public void onResume()
	{
		PrintHelper.networkStatus = "";
		progressBar.setVisibility(View.VISIBLE);
		
		if(CaptureActivity.isNeedDisablePoorNetworkAvoidance(this)){
			if(poorLinkDialog == null){
				poorLinkDialog = new InfoDialog.InfoDialogBuilder(this).setMessage(R.string.disablePoorNetworkAvoidance)
						.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
								startActivity(intent);
							}
						})
						.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								poorLinkDialog.dismiss();
								CaptureActivity.goToTopLevelActivity(WifiManualInputActivity.this);
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
		
		scanReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				// TODO Auto-generated method stub
				/*
				 * NetworkInfo networkInfo = (NetworkInfo)
				 * intent.getParcelableExtra
				 * (ConnectivityManager.EXTRA_NETWORK_INFO); if
				 * (networkInfo.getState
				 * ().equals(NetworkInfo.State.DISCONNECTED)) { WifiManager
				 * wifiManager = (WifiManager)
				 * WifiManualInputActivity.this.getSystemService
				 * (Context.WIFI_SERVICE); wifiManager.setWifiEnabled(false);
				 * Intent intent2 = new Intent(WifiManualInputActivity.this,
				 * WiFiSelectWorkflowActivity.class);
				 * intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				 * startActivity(intent2); }
				 */

				WifiManager wifiManager = (WifiManager) WifiManualInputActivity.this.getSystemService(Context.WIFI_SERVICE);
				List<ScanResult> results = wifiManager.getScanResults();
				wifiNetworks = new ArrayList<String>();
				for (ScanResult scan : results)
				{
					if (Connection.isKioskWifi(scan.SSID.toString()))
						wifiNetworks.add(scan.SSID.toString());
				}
				if (wifiNetworks.size() > 0)
				{
					adapter = new MyCustomAdapter(WifiManualInputActivity.this, R.layout.row, wifiNetworks);
					ssidButton.setText(wifiNetworks.get(0).toString());
					progressBar.setVisibility(View.INVISIBLE);
				}
				else
				{
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(WifiManualInputActivity.this);
					builder.setTitle(getString(R.string.nonetworkfound));
					builder.setMessage("");
					builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							if(WiFiSelectWorkflowActivity.FindBackCamera()){
								Intent intent = new Intent(WifiManualInputActivity.this, CaptureActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}else{
								Intent intent = new Intent(WifiManualInputActivity.this, WiFiSelectWorkflowActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}
						}
					});
					builder.setNegativeButton("", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							progressBar.setVisibility(View.INVISIBLE);
							dialog.dismiss();
							finish();
						}
					});
					builder.create().show();
				}
			}
		};
		receiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				//if (progressBar != null)
				//	progressBar.setVisibility(View.INVISIBLE);
				if (dialog != null)
					dialog.hide();
				NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if (Connection.isConnectedKioskWifi(getApplicationContext()) && networkInfo.getState().equals(NetworkInfo.State.CONNECTED))
				{
					Log.i("Manual Screen", "receiver called, networkinfo.state.connected is true");
					Intent myIntent = new Intent(WifiManualInputActivity.this, WiFiConnectionActivity.class);
					startActivity(myIntent);
				}
			}
		};
		connectingReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				//if (progressBar != null)
				//	progressBar.setVisibility(View.INVISIBLE);
				if (dialog != null)
					dialog.hide();
				NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if (Connection.isConnectedKioskWifi(getApplicationContext()) && networkInfo.getState().equals(NetworkInfo.State.CONNECTED))
				{
					Log.i("Manual Screen", "connectingReceiver called, networkinfo.state.connected is true");
					Intent myIntent = new Intent(WifiManualInputActivity.this, WiFiConnectionActivity.class);
					startActivity(myIntent);
				}
				else
				{
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(WifiManualInputActivity.this);
					builder.setTitle(getString(R.string.nonetworkfound));
					builder.setMessage("");
					builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							progressBar.setVisibility(View.INVISIBLE);
							dialog.dismiss();
							if(WiFiSelectWorkflowActivity.FindBackCamera()){
								Intent intent = new Intent(WifiManualInputActivity.this, CaptureActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}else{
								Intent intent = new Intent(WifiManualInputActivity.this, WiFiSelectWorkflowActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}
						}
					});
					builder.setNegativeButton("", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							finish();
						}
					});
					builder.create().show();
				}
			}
		};
		connectingRunnable = new Runnable(){

			@Override
			public void run() {
				Log.i("Manual Connect", "new Thread start....");
				int TIMEOUT = 30;
				int time = 0;
				connectingHandler.sendEmptyMessage(START);
				PrintHelper.networkStatus = "";
				ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				State wifi = null;
				while(connecting){
					if (dialog != null)
						dialog.hide();
					if(PrintHelper.networkStatus.equals(WifiConfigManager.WIFI_CONNECT_SUCCESS))
					{
						wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
						if(wifi.equals(NetworkInfo.State.CONNECTED)){
							connectingHandler.sendEmptyMessage(SUCCESS);
						} else if(time>TIMEOUT){
							connectingHandler.sendEmptyMessage(FAILED);
						}
					}
					else if (PrintHelper.networkStatus.equals(WifiConfigManager.WIFI_CONNECT_FAILED) || time>TIMEOUT)
					{
						connectingHandler.sendEmptyMessage(FAILED);
					}
					try {
						Thread.sleep(500);
						time++;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				PrintHelper.networkStatus = "";
			}
			
		};
		if (Connection.isConnectedKioskWifi(WifiManualInputActivity.this))
		{
			Log.i("Manual Screen", "isConnectedWifi, navigation to toaster screen");
			Intent intent = new Intent(WifiManualInputActivity.this, WiFiConnectionActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
		else
		{
			this.registerReceiver(this.receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
			registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			infoButton.setVisibility(android.view.View.VISIBLE);
			version.setVisibility(View.INVISIBLE);
			version.setTypeface(PrintHelper.tf);
			version.setText(getString(R.string.wifi) + " " + getString(R.string.notconnected));
			version.setGravity(android.view.Gravity.LEFT);
			wifiNetworks = new ArrayList<String>();
			WifiManager wifiManager = (WifiManager) WifiManualInputActivity.this.getSystemService(Context.WIFI_SERVICE);
			if (wifiManager.startScan())
			{
				progressBar.setVisibility(View.VISIBLE);
			}
			infoButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent myIntent;
					myIntent = new Intent(WifiManualInputActivity.this, HelpActivity.class);
					startActivity(myIntent);
				}
			});
		}
		if (!PrintHelper.infoEnabled)
		{
			infoButton.setVisibility(View.INVISIBLE);
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (prefs.getBoolean("analytics", false))
		{
			try {
				PrintHelper.mTracker.trackPageView("/ManualConnectScreen");
				PrintHelper.mTracker.dispatch();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		ssidButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final Dialog dialog = new Dialog(WifiManualInputActivity.this,R.style.DropDownDialog);
				dialog.setContentView(R.layout.custom_dialog);
				dialog.setCancelable(true);

				ListView ssidLV = (ListView)dialog.findViewById(R.id.ssidLV);
				ssidLV.setAdapter(adapter);

				ssidLV.setOnItemClickListener(new OnItemClickListener()
				{
				    @Override 
				    public void onItemClick(AdapterView<?> arg0, View view,int position, long arg3)
				    { 
				    	String item = ((TextView)view.findViewById(R.id.productSizeTextView)).getText().toString();
				    	ssidButton.setText(item);
						dialog.dismiss();
				    }
				});
	 
				dialog.show();
			}
		});
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		if (scanReceiver != null)
		{
			try
			{
				unregisterReceiver(scanReceiver);
			}
			catch (Exception ex)
			{
			}
		}
		if (connectingReceiver != null)
		{
			try
			{
				unregisterReceiver(connectingReceiver);
			}
			catch (Exception ex)
			{
			}
		}
		if (receiver != null)
		{
			try
			{
				unregisterReceiver(receiver);
			}
			catch (Exception ex)
			{
			}
		}
		connecting = false;
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(connecting){
				connecting = false;
			}
			if(WiFiSelectWorkflowActivity.FindBackCamera()){
				Intent intent = new Intent(WifiManualInputActivity.this, CaptureActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}else{
				Intent intent = new Intent(WifiManualInputActivity.this, WiFiSelectWorkflowActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			return true;
		}
		return false;
	}
	
}
