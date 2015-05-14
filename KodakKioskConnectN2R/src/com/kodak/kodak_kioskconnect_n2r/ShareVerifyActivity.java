package com.kodak.kodak_kioskconnect_n2r;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ShareVerifyActivity extends Activity {
	private static final String tag = ShareVerifyActivity.class.getSimpleName();
	
	private TextView tvTitle;
	private Button btCancel;
	private Button btUpload;
	private Button btSetting;
	private Button btInfo;
	
	private TextView tvEmailType;
	private TextView tvEmail;
	private TextView tvAlbumName;
	private String packName;
	private String stAlbumName;
	
	private boolean launchByOther = false;
	private String intentFlag = "launchByOther";
	private String oriChangedFlag = "ori_changed";
	private String albumNameFlag = "album_name";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shareverify);
		
		tvTitle = (TextView) findViewById(R.id.headerBarText);
		btCancel = (Button) findViewById(R.id.backButton);
		btUpload = (Button) findViewById(R.id.nextButton);
		btSetting = (Button) findViewById(R.id.settingsButton);
		btInfo = (Button) findViewById(R.id.infoButton);
		
		tvEmailType = (TextView) findViewById(R.id.share_email_type);
		tvEmail = (TextView) findViewById(R.id.share_verify_email);
		tvAlbumName = (TextView) findViewById(R.id.share_verify_album);
		
		tvTitle.setText(getString(R.string.share_verify));
		btCancel.setText(getString(R.string.Back));
		btUpload.setText(getString(R.string.share_upload));
		
		packName = getApplicationContext().getPackageName();
		if(packName.contains("wmc")){
			tvEmailType.setText(getString(R.string.wmc_verify_email));
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);		
		tvEmail.setText(prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, ""));
		if(savedInstanceState!=null && savedInstanceState.getBoolean(oriChangedFlag)){
			stAlbumName = savedInstanceState.getString(albumNameFlag);
		} else {
			stAlbumName = getAlbumName();
		}
		tvAlbumName.setText(stAlbumName);
		
		setupEvents();
		
		launchByOther = getIntent().getBooleanExtra(intentFlag, false);
		if(launchByOther){
			// do nothing
		} else if(getIntent()!=null&&getIntent().getExtras()!=null&&getIntent().getExtras().get(Intent.EXTRA_STREAM) != null){
			launchByOther = true;
			Uri uri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
			if(!checkAccountValid()){
				Intent intent = new Intent(this, ShareLoginActivity.class);
				intent.putExtra(Intent.EXTRA_STREAM, uri);
				intent.putExtra(intentFlag, true);
				startActivity(intent);
				finish();			
			} 
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(launchByOther){
			
		} else {
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(oriChangedFlag, true);
		outState.putString(albumNameFlag, stAlbumName);
	}

	private String getAlbumName() {
		String albumName = "";
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH)+1;
		int date = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		albumName = "" + year + (month<10 ? ("0"+month) : month) + (date<10 ? ("0"+date) : date) + (hour<10 ? ("0"+hour) : hour) + (min<10 ? ("0"+min) : min);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString("share_album_name", albumName);
		editor.commit();
		return albumName;
	}

	private boolean checkAccountValid() {
		Log.d(tag, "checkAccountValid....");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String email = prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "");
		String password = prefs.getString(PrintHelper.SHARE_PASSWORD_FLAG, "");
		if(!email.contains("@") || email.equals("") || password.equals("") || PrintHelper.getAccessTokenResponse(getApplicationContext()).access_token.equals("")){
			return false;
		}
		return true;
	}
	
	private void setupEvents() {
		btCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		btUpload.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ShareVerifyActivity.this, ShareUploadActivity.class);
				if(launchByOther){
					Uri uri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
					intent.putExtra(Intent.EXTRA_STREAM, uri);
					intent.putExtra(intentFlag, true);
				}
				startActivity(intent);
				/*if(launchByOther){
					finish();
				}*/
				finish();
			}
		});
		
		btSetting.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent myIntent = new Intent(ShareVerifyActivity.this, NewSettingActivity.class);
				startActivity(myIntent);
			}
		});
		
		btInfo.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent myIntent;
				myIntent = new Intent(ShareVerifyActivity.this, HelpActivity.class);
				startActivity(myIntent);
			}
		});
	}

	@Override
	public void onBackPressed() {
		/*if(launchByOther){
			// TODO email and password is valid, then this is come from Login activity
			Intent intent = new Intent(ShareVerifyActivity.this, ShareLoginActivity.class);
			if(launchByOther){
				Uri uri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
				intent.putExtra(Intent.EXTRA_STREAM, uri);
				intent.putExtra(intentFlag, true);
			}
			startActivity(intent);
			finish();
		}*/
		super.onBackPressed();
	}

}
