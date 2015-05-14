package com.kodak.rss.tablet.activities;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.AppManager;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.db.ImageSelectionDatabase;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class WiFiSelectWorkflowActivity extends BaseActivity implements OnClickListener{
	private final static String TAG = "WiFiSelectWorkflowActivity";
	public final static String PREFERENCE_KEY_IS_DISPLAY_ALL = "is_display_all";

	private ImageSelectionDatabase imageSelectionDb;
	private TextView tvTaggedSet;
	
	private boolean isFromWifiD = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_select_workflow);

		imageSelectionDb = new ImageSelectionDatabase(this);
		initViews(savedInstanceState);
		
		if (getIntent() != null) {
			isFromWifiD = getIntent().getBooleanExtra("fromWifiDisconnect", false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//reset preference is_display_all to default value false
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.edit().putBoolean(PREFERENCE_KEY_IS_DISPLAY_ALL, false).commit();
		
		SortableHashMap<Integer, String> map = imageSelectionDb.getTaggedSetMap();
		if(map != null && map.size()>0){
			tvTaggedSet.setText(R.string.viewtaggedset);
		}else{
			tvTaggedSet.setText(R.string.WiFiWorkflow_CreateTaggedSet);
		}
		
		if (isFromWifiD) {
			android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {							
						String packageName = WiFiSelectWorkflowActivity.this.getPackageName();
						Uri uri = null;
						Intent goToMarket = null;						
						try {
							uri = Uri.parse("market://details?id=" + packageName);
							goToMarket = new Intent(Intent.ACTION_VIEW, uri);
							goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
							startActivity(goToMarket);
						} catch (ActivityNotFoundException e) {
							uri = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
							goToMarket = new Intent(Intent.ACTION_VIEW, uri);
							goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
							startActivity(goToMarket);
						}	
				}						
			};			

			String promptStr = getResources().getString(R.string.OrderScreen_RateApp);
			new InfoDialog.Builder(WiFiSelectWorkflowActivity.this).setMessage(promptStr)
			.setPositiveButton(getText(R.string.d_ok), yesOnClickListener)
			.setNeturalButton(R.string.cancel, null)
			.create().show();
			isFromWifiD = false;
		}
		
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v.getId()==R.id.previous_button){
			AppManager.getInstance().finishAllActivity();
			startActivity(new Intent(this,MainActivity.class));
		}else if(v.getId()==R.id.wifiContent){
			RSSLocalytics.recordLocalyticsPageView(this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_WIFI_DISPLAY_ALL);
			RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_WIFI_DISPLAY_ALL);
			
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			sp.edit().putBoolean(PREFERENCE_KEY_IS_DISPLAY_ALL, true).commit();
			
			Intent intent = new Intent(WiFiSelectWorkflowActivity.this,hasBackCamera() ? WiFiQRCodeScanActivity.class : WifiManualInputActivity.class);
			startActivity(intent);
		}else if(v.getId()==R.id.selectPic){
			RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_WIFI_SELECT_AND_SEND);
			
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			sp.edit().putBoolean(PREFERENCE_KEY_IS_DISPLAY_ALL, false).commit();
			
			startActivity(new Intent(WiFiSelectWorkflowActivity.this, PicSelectKioskActivity.class));
			this.finish();
		}else if(v.getId()==R.id.findStore){
			Intent mIntent = new Intent(WiFiSelectWorkflowActivity.this, StoreSelectActivity.class);
			mIntent.putExtra(StoreInfo.IS_WIFI_LOCATOR, true);
			startActivity(mIntent);
		}
	}

	public static boolean hasBackCamera() {
		int num = Camera.getNumberOfCameras();
		Log.i(TAG, "CAMERA NUM:" + num);

		for (int i = 0; i < num; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void startOver() {
		new ImageSelectionDatabase(this).handleDeleteAllUrisWiFi();
		super.startOver();
	}

	private void initViews(Bundle savedInstanceState) {
		tvTaggedSet = (TextView) findViewById(R.id.tv_taggedSet);

		findViewById(R.id.previous_button).setOnClickListener(this);
		findViewById(R.id.selectPic).setOnClickListener(this);
		findViewById(R.id.wifiContent).setOnClickListener(this);
		findViewById(R.id.findStore).setOnClickListener(this);
	}
}
