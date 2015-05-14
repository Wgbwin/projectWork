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
package com.google.zxing.client.android;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.result.ResultHandlerFactory;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.HelpActivity;
import com.kodak.kodak_kioskconnect_n2r.InfoDialog;
import com.kodak.kodak_kioskconnect_n2r.NewSettingActivity;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.WiFiConnectionActivity;
import com.kodak.kodak_kioskconnect_n2r.WiFiSelectWorkflowActivity;
import com.kodak.kodak_kioskconnect_n2r.WifiConfigManager;
import com.kodak.kodak_kioskconnect_n2r.WifiManualInputActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.kodak_kioskconnect_n2r.activity.WifiNewSettingFragmentActivity;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback
{
	private static final String TAG = CaptureActivity.class.getSimpleName();
	private static final long DEFAULT_INTENT_RESULT_DURATION_MS = 1500L;
	private static final String PRODUCT_SEARCH_URL_PREFIX = "http://www.google";
	private static final String PRODUCT_SEARCH_URL_SUFFIX = "/m/products/scan";
	private static final String[] ZXING_URLS = { "http://zxing.appspot.com/scan", "zxing://scan/" };
	public static final int HISTORY_REQUEST_CODE = 0x0000bacc;
	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	private TextView statusView;
	private View resultView;
	// private Result lastResult;
	private boolean hasSurface;
	private boolean copyToClipboard;
	private IntentSource source;
	private String sourceUrl;
	private Collection<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private Button settingsButton;
	private Button nextButton;
	private Button backButton;
	private Button infoButton;
	private InfoDialog poorLinkDialog;
	Button manualConnectButton;
	private TextView headerTV;
	TextView status;
	TextView totalSelectedTV;
	TextView instructionsTV;
	TextView takingtoolong;
	public String ssid = "";

	ViewfinderView getViewfinderView()
	{
		return viewfinderView;
	}

	public Handler getHandler()
	{
		return handler;
	}

	CameraManager getCameraManager()
	{
		return cameraManager;
	}

	private BroadcastReceiver receiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
			{
				DisplayWifiState();
			}
		}
	};
	
	private void DisplayWifiState()
	{
		if (Connection.isConnectedKioskWifi(this))
		{
			status.setText(getString(R.string.scanconnectedtokiosk) + "\n" + ssid);
			Thread thrd = new Thread()
			{
				public void run()
				{
					try
					{
						sleep(2000);
						handler.sendEmptyMessage(42);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
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
	}

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.barcodescan);
		resultView = findViewById(R.id.scanresult);
		statusView = (TextView) findViewById(R.id.status_view);
		instructionsTV = (TextView) findViewById(R.id.instructions);
		instructionsTV.setTypeface(PrintHelper.tfb);
		resultView.setVisibility(android.view.View.INVISIBLE);
		status = (TextView) findViewById(R.id.ssidTV);
		totalSelectedTV = (TextView) findViewById(R.id.totalSelectedTextView);
		totalSelectedTV.setVisibility(android.view.View.GONE);
		settingsButton = (Button) findViewById(R.id.settingsButton);
		nextButton = (Button) findViewById(R.id.nextButton);
		backButton = (Button) findViewById(R.id.backButton);
		infoButton = (Button) findViewById(R.id.infoButton);
		headerTV = (TextView) findViewById(R.id.headerBarText);
		takingtoolong = (TextView)findViewById(R.id.tooLongTV);
		manualConnectButton = (Button) findViewById(R.id.manualconnectButton);
		headerTV.setText(getString(R.string.scanconnect));
		nextButton.setVisibility(android.view.View.INVISIBLE);
		settingsButton.setVisibility(android.view.View.INVISIBLE);
		statusView.setTypeface(PrintHelper.tfb);
		
		takingtoolong.setVisibility(View.INVISIBLE);
		manualConnectButton.setVisibility(View.INVISIBLE);
		infoButton.setVisibility(View.INVISIBLE);
		infoButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent myIntent;
				myIntent = new Intent(CaptureActivity.this, HelpActivity.class);
				startActivity(myIntent);
			}
		});
		manualConnectButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setResult(RESULT_CANCELED);
				Intent intent = new Intent(CaptureActivity.this, WifiManualInputActivity.class);
				startActivity(intent);
			}
		});
		settingsButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setResult(RESULT_CANCELED);
				Intent myIntent = null;
				if(!PrintHelper.wififlow)
				{
				myIntent = new Intent(CaptureActivity.this, NewSettingActivity.class);
				startActivity(myIntent);
				}
				else
				{
					myIntent = new Intent(CaptureActivity.this, WifiNewSettingFragmentActivity.class);
					startActivity(myIntent);
				}
			}
		});
		backButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setResult(RESULT_CANCELED);
				Intent intent = new Intent(CaptureActivity.this, WiFiSelectWorkflowActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (prefs.getBoolean("acceptCookies", false))
		{
			try{
				PrintHelper.mTracker.trackPageView("/ScanConnectScreen");
				PrintHelper.mTracker.dispatch();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		if(isNeedDisablePoorNetworkAvoidance(this)){
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
								goToTopLevelActivity(CaptureActivity.this);
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
				WifiManager wifiManager = (WifiManager) CaptureActivity.this.getSystemService(Context.WIFI_SERVICE);
				wifiManager.setWifiEnabled(true);
			}
		};
//		Thread cantScanThread = new Thread()
//		{
//			public void run()
//			{
//				try
//				{
//					sleep(60000);
//					handler.sendEmptyMessage(20);
//				}
//				catch (InterruptedException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//			}
//		};
//		cantScanThread.start();
		
		if (Connection.isConnectedKioskWifi(CaptureActivity.this))
		{
			Intent intent = new Intent(CaptureActivity.this, WiFiConnectionActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		else
		{
			enableWIFI.start();
			this.registerReceiver(this.receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
			
			// CameraManager must be initialized here, not in onCreate(). This
			// is
			// necessary because we don't
			// want to open the camera driver and measure the screen size if
			// we're
			// going to show the help on
			// first launch. That led to bugs where the scanning rectangle was
			// the
			// wrong size and partially
			// off screen.
			cameraManager = new CameraManager(getApplication());
			handler = null;
			// lastResult = null;
			viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
			viewfinderView.setCameraManager(cameraManager);
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			if (hasSurface)
			{
				// The activity was paused but not stopped, so the surface still
				// exists. Therefore
				// surfaceCreated() won't be called, so init the camera here.
				initCamera(surfaceHolder);
			}
			else
			{
				// Install the callback and wait for surfaceCreated() to init
				// the
				// camera.
				surfaceHolder.addCallback(this);
				surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			}
			inactivityTimer.onResume();
			Intent intent = getIntent();
			source = IntentSource.NONE;
			decodeFormats = null;
			characterSet = null;
			if (intent != null)
			{
				String action = intent.getAction();
				String dataString = intent.getDataString();
				if (Intents.Scan.ACTION.equals(action))
				{
					// Scan the formats the intent requested, and return the
					// result
					// to the calling activity.
					source = IntentSource.NATIVE_APP_INTENT;
					decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
					if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT))
					{
						int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
						int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
						if (width > 0 && height > 0)
						{
							cameraManager.setManualFramingRect(width, height);
						}
					}
					String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
					if (customPromptMessage != null)
					{
						statusView.setText(customPromptMessage);
					}
				}
				else if (dataString != null && dataString.contains(PRODUCT_SEARCH_URL_PREFIX) && dataString.contains(PRODUCT_SEARCH_URL_SUFFIX))
				{
					// Scan only products and send the result to mobile Product
					// Search.
					source = IntentSource.PRODUCT_SEARCH_LINK;
					sourceUrl = dataString;
					decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;
				}
				else if (isZXingURL(dataString))
				{
					// Scan formats requested in query string (all formats if
					// none
					// specified).
					// If a return URL is specified, send the results there.
					// Otherwise, handle it ourselves.
					source = IntentSource.ZXING_LINK;
					sourceUrl = dataString;
					Uri inputUri = Uri.parse(sourceUrl);
					decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);
				}
				characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
			}
		}
		Thread thrd = new Thread()
		{
			public void run()
			{
				try
				{
					sleep(10000);
					handler.sendEmptyMessage(20);
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

	private static boolean isZXingURL(String dataString)
	{
		if (dataString == null)
		{
			return false;
		}
		for (String url : ZXING_URLS)
		{
			if (dataString.startsWith(url))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onPause()
	{
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
		
		if (handler != null)
		{
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface)
		{
			//SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			if(surfaceView!=null){
				SurfaceHolder surfaceHolder = surfaceView.getHolder();
				surfaceHolder.removeCallback(this);
			}
		}
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (resultView.getVisibility() == View.VISIBLE)
			{
				setResult(RESULT_CANCELED);
				Intent intent = new Intent(CaptureActivity.this, WiFiSelectWorkflowActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			}
			else
			{
				setResult(RESULT_CANCELED);
				Intent intent = new Intent(CaptureActivity.this, WiFiSelectWorkflowActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			}
		}
		else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA)
		{
			// Handle these events so they don't launch the Camera app
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if (resultCode == RESULT_OK)
		{
			// if (requestCode == HISTORY_REQUEST_CODE) {
			// int itemNumber = intent.getIntExtra(Intents.History.ITEM_NUMBER,
			// -1);
			// if (itemNumber >= 0) {
			// HistoryItem historyItem =
			// historyManager.buildHistoryItem(itemNumber);
			// decodeOrStoreSavedBitmap(null, historyItem.getResult());
			// }
			// }
		}
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result)
	{
		// Bitmap isn't used yet -- will be used soon
		if (handler == null)
		{
			savedResultToShow = result;
		}
		else
		{
			if (result != null)
			{
				savedResultToShow = result;
			}
			if (savedResultToShow != null)
			{
				Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if (holder == null)
		{
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface)
		{
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode)
	{
		inactivityTimer.onActivity();
		// lastResult = rawResult;
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
		drawResultPoints(barcode, rawResult);
		switch (source)
		{
		case NATIVE_APP_INTENT:
		case PRODUCT_SEARCH_LINK:
			handleDecodeExternally(rawResult, resultHandler, barcode);
			break;
		case ZXING_LINK:
			handleDecodeExternally(rawResult, resultHandler, barcode);
			break;
		case NONE:
			handleDecodeInternally(rawResult, resultHandler, barcode);
			break;
		}
	}

	/**
	 * Superimpose a line for 1D or dots for 2D to highlight the key features of
	 * the barcode.
	 * 
	 * @param barcode
	 *            A bitmap of the captured image.
	 * @param rawResult
	 *            The decoded results which contains the points to draw.
	 */
	private void drawResultPoints(Bitmap barcode, Result rawResult)
	{
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0)
		{
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_image_border));
			paint.setStrokeWidth(3.0f);
			paint.setStyle(Paint.Style.STROKE);
			Rect border = new Rect(2, 2, barcode.getWidth() - 2, barcode.getHeight() - 2);
			canvas.drawRect(border, paint);
			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2)
			{
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1]);
			}
			else if (points.length == 4 && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13))
			{
				// Hacky special case -- draw two lines, for the barcode and
				// metadata
				drawLine(canvas, paint, points[0], points[1]);
				drawLine(canvas, paint, points[2], points[3]);
			}
			else
			{
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points)
				{
					canvas.drawPoint(point.getX(), point.getY(), paint);
				}
			}
		}
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b)
	{
		canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
	}
	private static ParsedResult parseResult(Result rawResult)
	{
		return ResultParser.parseResult(rawResult);
	}
	// Put up our own UI for how to handle the decoded contents.
	private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode)
	{
		ParsedResult parsedRawResult = parseResult(rawResult);
		if(! "WIFI".equals(parsedRawResult.getType().toString()))
		{
			InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(CaptureActivity.this);
			builder.setTitle("");
			builder.setMessage(getString(R.string.notavalidbarcode));
			builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					restartPreviewAfterDelay(0);
					dialog.dismiss();
				}
			});
			builder.setNegativeButton("", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			builder.setCancelable(false);
			builder.create().show();
		}
		else
		{
		statusView.setVisibility(View.GONE);
		viewfinderView.setVisibility(View.GONE);
		backButton.setVisibility(View.INVISIBLE);
		// headerTV.setVisibility(View.GONE);
		instructionsTV.setVisibility(View.GONE);
		infoButton.setVisibility(View.INVISIBLE);
		settingsButton.setVisibility(View.INVISIBLE);
		resultView.setVisibility(View.VISIBLE);
		ImageView barcodeImageView = (ImageView) findViewById(R.id.barcode_image_view);
		if (barcode == null)
		{
			barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.temp));
		}
		else
		{
			Log.i(TAG, "Width: " + barcode.getWidth() + " Height: " + barcode.getHeight());
			barcodeImageView.setImageBitmap(barcode);
		}
		boolean failure = false;
		try
		{
			String[] result = rawResult.getText().toString().split(";");
			String ssid = null, networkType=null, password=null;
			ssid = result[0].split(":")[2].toString();
			if(result.length == 1){
			}else if(result.length == 2){
				int index = result[1].indexOf(":");
				if(index != -1){
					networkType = result[1].substring(index+1);
				}
			}else{
				int index1 = result[1].indexOf(":");
				if(index1 != -1){
					networkType = result[1].substring(index1+1);
				}
				int index2 = result[2].indexOf(":");
				if(index2 != -1){
					password = result[2].substring(index2+1);
				}
			}
			status.setText(getString(R.string.scanconnectingtokiosk) + "\n" + ssid);
			if (Connection.isKioskWifi(ssid))
			{
				Log.d(TAG, "Configuring WiFi: " + ssid);
				WifiManager wifiManager = (WifiManager) CaptureActivity.this.getSystemService(Context.WIFI_SERVICE);
				WifiConfigManager.configure(wifiManager, ssid, password, networkType);
				Thread thrd = new Thread()
				{
					public void run()
					{
						try
						{
							sleep(10000);
							handler.sendEmptyMessage(20);
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
			else
			{
				failure = true;
			}
		}
		catch (Exception ex)
		{
			failure = true;
		}
		if (failure)
		{
			InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(CaptureActivity.this);
			builder.setTitle("");
			builder.setMessage(getString(R.string.notavalidkodakwirelessnetwork));
			builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					Intent myIntent = new Intent(CaptureActivity.this, WifiManualInputActivity.class);
					startActivity(myIntent);
					finish();
				}
			});
			builder.setNegativeButton("", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			builder.setCancelable(false);
			builder.create().show();
		}
		}
		/*
		 * } else { InfoDialog.InfoDialogBuilder builder = new
		 * InfoDialog.InfoDialogBuilder(CaptureActivity.this);
		 * builder.setTitle("");
		 * builder.setMessage(getString(R.string.notavalidkodakwirelessnetwork
		 * )); builder.setPositiveButton(getString(R.string.OK), new
		 * DialogInterface.OnClickListener() {
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * dialog.dismiss(); } }); builder.setNegativeButton("", new
		 * DialogInterface.OnClickListener() {
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * dialog.dismiss(); } }); builder.setCancelable(false);
		 * builder.create().show(); }
		 */
	}

	
	
	// Briefly show the contents of the barcode, then handle the result outside
	// Barcode Scanner.
	private void handleDecodeExternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode)
	{
		viewfinderView.drawResultBitmap(barcode);
		// Since this message will only be shown for a second, just tell the
		// user what kind of
		// barcode was found (e.g. contact info) rather than the full contents,
		// which they won't
		// have time to read.
		if (copyToClipboard && !resultHandler.areContentsSecure())
		{
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(resultHandler.getDisplayContents());
		}
		// if (source == IntentSource.NATIVE_APP_INTENT) {
		// Hand back whatever action they requested - this can be changed to
		// Intents.Scan.ACTION when
		// the deprecated intent is retired.
		Intent intent = new Intent(CaptureActivity.this, WiFiConnectionActivity.class);
		intent.putExtra(Intents.Scan.RESULT, rawResult.toString());
		intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.getBarcodeFormat().toString());
		byte[] rawBytes = rawResult.getRawBytes();
		if (rawBytes != null && rawBytes.length > 0)
		{
			intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes);
		}
		Map<ResultMetadataType, ?> metadata = rawResult.getResultMetadata();
		if (metadata != null)
		{
			Integer orientation = (Integer) metadata.get(ResultMetadataType.ORIENTATION);
			if (orientation != null)
			{
				intent.putExtra(Intents.Scan.RESULT_ORIENTATION, orientation.intValue());
			}
			String ecLevel = (String) metadata.get(ResultMetadataType.ERROR_CORRECTION_LEVEL);
			if (ecLevel != null)
			{
				intent.putExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL, ecLevel);
			}
			@SuppressWarnings("unchecked")
			Iterable<byte[]> byteSegments = (Iterable<byte[]>) metadata.get(ResultMetadataType.BYTE_SEGMENTS);
			if (byteSegments != null)
			{
				int i = 0;
				for (byte[] byteSegment : byteSegments)
				{
					intent.putExtra(Intents.Scan.RESULT_BYTE_SEGMENTS_PREFIX + i, byteSegment);
					i++;
				}
			}
		}
		sendReplyMessage(R.id.return_scan_result, intent);
		/*
		 * } else if (source == IntentSource.PRODUCT_SEARCH_LINK) { //
		 * Reformulate the URL which triggered us into a query, so that the
		 * request goes to the same // TLD as the scan URL. int end =
		 * sourceUrl.lastIndexOf("/scan"); String replyURL =
		 * sourceUrl.substring(0, end) + "?q=" +
		 * resultHandler.getDisplayContents() + "&source=zxing";
		 * sendReplyMessage(R.id.launch_product_query, replyURL); } else if
		 * (source == IntentSource.ZXING_LINK) { // Replace each occurrence of
		 * RETURN_CODE_PLACEHOLDER in the returnUrlTemplate // with the scanned
		 * code. This allows both queries and REST-style URLs to work. if
		 * (returnUrlTemplate != null) { String codeReplacement =
		 * String.valueOf(resultHandler.getDisplayContents()); try {
		 * codeReplacement = URLEncoder.encode(codeReplacement, "UTF-8"); }
		 * catch (UnsupportedEncodingException e) { // can't happen; UTF-8 is
		 * always supported. Continue, I guess, without encoding } String
		 * replyURL = returnUrlTemplate.replace(RETURN_CODE_PLACEHOLDER,
		 * codeReplacement); sendReplyMessage(R.id.launch_product_query,
		 * replyURL); } }
		 */
	}

	private void sendReplyMessage(int id, Object arg)
	{
		Message message = Message.obtain(handler, id, arg);
		long resultDurationMS = getIntent().getLongExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS, DEFAULT_INTENT_RESULT_DURATION_MS);
		if (resultDurationMS > 0L)
		{
			handler.sendMessageDelayed(message, resultDurationMS);
			startActivity((Intent) arg);
		}
		else
		{
			handler.sendMessage(message);
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder)
	{
		try
		{
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null)
			{
				handler = new CaptureActivityHandler(this, decodeFormats, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		}
		catch (IOException ioe)
		{
			Log.w(TAG, ioe);
		}
		catch (RuntimeException e)
		{
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
		}
	}
	
	public static boolean isNeedDisablePoorNetworkAvoidance(Context context){
		return VERSION.SDK_INT >= 18 
				&& PrintHelper.isPoorNetworkAvoidanceEnabled(context)
				&& "samsung".equalsIgnoreCase(android.os.Build.MANUFACTURER);
			
	}
	
	public static void goToTopLevelActivity(Activity activity){
		Intent myIntent;
		String PACKAGE_NAME = activity.getPackageName();
		boolean isWMC = PACKAGE_NAME.contains("wmc");
		boolean isDMC = PACKAGE_NAME.contains(MainMenu.DM_COMBINED_PACKAGE_NAME);
		boolean isPrintMaker = PACKAGE_NAME.contains("kodakprintmaker");
		
		if(isWMC){
			myIntent = new Intent(activity, WiFiSelectWorkflowActivity.class);
		} else if(isPrintMaker){
			myIntent = new Intent(activity, WiFiSelectWorkflowActivity.class);
		} else if(isDMC) {
			myIntent = new Intent(activity, MainMenu.class);
		} else {
			myIntent = new Intent(activity, MainMenu.class);
		}
		myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(myIntent);
		activity.finish();
	}
	
	public void restartPreviewAfterDelay(long delayMS)
	{
		if (handler != null)
		{
			resultView.setVisibility(android.view.View.INVISIBLE);
			statusView.setVisibility(View.VISIBLE);
			viewfinderView.setVisibility(View.VISIBLE);
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
	}

	public void drawViewfinder()
	{
		viewfinderView.drawViewfinder();
	}
}
