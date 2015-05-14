package com.kodak.kodak_kioskconnect_n2r;

import java.util.Calendar;

import com.AppConstants;
import com.AppContext;
import com.kodak.shareapi.AccessTokenResponse;
import com.kodak.shareapi.ClientTokenResponse;
import com.kodak.shareapi.GalleryService;
import com.kodak.shareapi.TokenGetter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ShareLoginActivity extends Activity {
	private static final String tag = ShareLoginActivity.class.getSimpleName();
	
	private TextView tvTitle;
	private Button btCancel;
	private static Button btSignIn; // after orientation changed on SDK4.0, this button would be changed, so add static property
	private TextView tvSignIn;
	private TextView tvAutoUpload;
	private EditText etEmailAddress;
	private EditText etPassword;
	private CheckBox cbUpload;
	private String packName;
	public static ProgressBar waitingDialog;
	
	private boolean launchByOther = false;
	private static String intentFlag = "launchByOther";
	private String oriChangedFlag = "ori_changed";
	private static boolean isSignIn = false;
	public static final String CLIENT_TOKEN = "https://ws.test.kdfse.com/auth/client?app=CUMMOBANDWMC&app_version=1.0&retailer=walmart-CAN";
	public static final String ACCESS_TOKEN_HOST = "https://ws.test.kdfse.com/auth/token";
	private SharedPreferences prefs;
	private boolean isFromShopCart;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_sign_in);
		
		isFromShopCart = getIntent().getBooleanExtra(AppConstants.IS_FORM_SHOPPINGCART, false);
		
		tvTitle = (TextView) findViewById(R.id.headerBarText);
		btCancel = (Button) findViewById(R.id.backButton);
		btSignIn = (Button) findViewById(R.id.nextButton);
		tvSignIn = (TextView) findViewById(R.id.signin_text);
		etEmailAddress = (EditText) findViewById(R.id.share_email_account);
		etPassword = (EditText) findViewById(R.id.share_email_password);
		AppContext.getApplication().setEmojiFilter(etEmailAddress);
		AppContext.getApplication().setEmojiFilter(etPassword);
		cbUpload = (CheckBox) findViewById(R.id.share_checkbox);
		tvAutoUpload = (TextView) findViewById(R.id.auto_upload_label);
		waitingDialog = (ProgressBar) findViewById(R.id.signin_waiting);
		cbUpload.setTypeface(PrintHelper.tf);
		
		tvTitle.setText(R.string.setup);
		btCancel.setText(R.string.Cancel);
		btSignIn.setText(R.string.done);
		
		packName = getApplicationContext().getPackageName();
		if(packName.contains("wmc")){
			tvSignIn.setText(getString(R.string.wmc_share));
		}
		
		String pwdHint = (String) etPassword.getHint();
		if(pwdHint.contains(":")){
			etPassword.setHint(pwdHint.replace(":", " ").trim());
		}
		String autoLabel = (String) tvAutoUpload.getText();
		if(autoLabel.contains(":")){
			autoLabel = autoLabel.replace(":", " ").trim();
			tvAutoUpload.setText(autoLabel);
		}
		
		prefs = PreferenceManager.getDefaultSharedPreferences(ShareLoginActivity.this);
		launchByOther = getIntent().getBooleanExtra(intentFlag, false);
		if(savedInstanceState!=null && savedInstanceState.getBoolean(oriChangedFlag)){
			etEmailAddress.setText(savedInstanceState.getString("email"));
			etPassword.setText(savedInstanceState.getString("password"));
			if(isSignIn){
				waitingHandler.sendEmptyMessage(SIGNIN_START);
			}
		} else {
			etEmailAddress.setText(prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, ""));
			etPassword.setText(prefs.getString(PrintHelper.SHARE_PASSWORD_FLAG, ""));
		}
		
		setupEvents();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		cbUpload.setChecked(prefs.getBoolean(PrintHelper.IS_AUTO_UPLOAD2ALBUMS, false));
		if(launchByOther){
		} else {
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}	
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		String email = etEmailAddress.getText().toString();
		String password = etPassword.getText().toString();
		outState.putBoolean(oriChangedFlag, true);
		outState.putString("email", email);
		outState.putString("password", password);
	}

	Runnable signInRunnable = new Runnable(){

		@Override
		public void run() {
			waitingHandler.sendEmptyMessage(SIGNIN_START);
			TokenGetter tokenGetter = new TokenGetter();
			ClientTokenResponse clientTokenResponse = tokenGetter.httpClientTokenUrlPost(CLIENT_TOKEN);
			if(clientTokenResponse != null){
				String username = prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "");
				String userPwd = prefs.getString(PrintHelper.SHARE_PASSWORD_FLAG, "");
				//AccessTokenResponse accessTokenResponse = null;
				try {
					PrintHelper.setAccessTokenResponse(tokenGetter.httpAccessTokenUrlPost(ACCESS_TOKEN_HOST, clientTokenResponse.client_token, username, userPwd, clientTokenResponse.client_secret), getApplicationContext());
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(PrintHelper.getAccessTokenResponse(getApplicationContext()).status.equals("OK")){
					Log.d(tag, "Get access token successfully.");
					//PrintHelper.setAccessTokenResponse(accessTokenResponse);
					if(isFromShopCart){
						//Create Gallery.
						getAlbumName();
						GalleryService galleryService = new GalleryService();
						String retailer = null, partner = null, country = null;//"walmart-CAN";
						String name = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("share_album_name", null);
						int count = 0;
						while (count < 3 && PrintHelper.galleryUUID.equals(""))
						{
							PrintHelper.galleryUUID = galleryService.createAGallery(galleryService.galleryURL, "CUMMOBANDWMC", "1.0", retailer, partner, country, name, PrintHelper.getAccessTokenResponse(getApplicationContext()).access_token);
							Log.d(tag, "create gallery response: " + PrintHelper.galleryUUID);
							count++;
						}
						if(PrintHelper.galleryUUID.equals("")){
							// TODO if failed should show error dialog
							waitingHandler.sendEmptyMessage(SIGNIN_FINISH);
						} else {
							waitingHandler.sendEmptyMessage(SIGNIN_FINISH);
						}
					}else{
						waitingHandler.sendEmptyMessage(SIGNIN_FINISH);
					}
				} else {
					Log.e(tag, "Can not get access token response.");
					waitingHandler.sendEmptyMessage(SIGNIN_FAILED);
				}
			} else {
				Log.e(tag, "Can not get client token response.");
				waitingHandler.sendEmptyMessage(SIGNIN_FAILED);
			}
		}};
	
	private String getAlbumName() {
		String albumName = "";
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		albumName = "" + year + (month<10 ? ("0"+month) : month) + (date<10 ? ("0"+date) : date) + (hour<10 ? ("0"+hour) : hour) + (min<10 ? ("0"+min) : min);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString("share_album_name", albumName);
		editor.commit();
		return albumName;
	}
		
	private static final int SIGNIN_START = 0;
	private static final int SIGNIN_FINISH= 1;
	private static final int SIGNIN_FAILED = 2;
	public Handler waitingHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case SIGNIN_START:
				Log.d(tag, "SIGNIN_START");
				isSignIn = true;
				btSignIn.setEnabled(false);
				waitingDialog.setVisibility(View.VISIBLE);
				waitingDialog.bringToFront();
				break;
			case SIGNIN_FINISH:
				Log.d(tag, "SIGNIN_FINISH");
				isSignIn = false;
				btSignIn.setEnabled(true);
				waitingDialog.setVisibility(View.GONE);
				forward();
				break;
			case SIGNIN_FAILED:
				Log.d(tag, "SIGNIN_FAILED");
				isSignIn = false;
				btSignIn.setEnabled(true);
				Log.e(tag, "status " + PrintHelper.getAccessTokenResponse(getApplicationContext()).status);
				waitingDialog.setVisibility(View.GONE);
				if(PrintHelper.getAccessTokenResponse(getApplicationContext()).status.equals("502")){
					showErrorDialog(BAD_GATE_WAY_ERROR);
				} else {
					PrintHelper.getAccessTokenResponse(getApplicationContext()).status.endsWith("400");
					showErrorDialog(USER_PASSWORD_INVALIDED);
				}
				break;
			}
		}
		
	};
	
	public void forward(){
		if(isFromShopCart){
			if(!PrintHelper.galleryUUID.equals(""))
				PictureUploadService2.isAutoStartShare =  true;
			Intent intent = new Intent(ShareLoginActivity.this, SendingOrderActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			PrintHelper.hasQuickbook = false;
			finish();
		}else{
			Intent intent = new Intent(this, ShareVerifyActivity.class);
			if(ShareLoginActivity.this.getIntent().getBooleanExtra(intentFlag, false)){
				Uri uri = (Uri) ShareLoginActivity.this.getIntent().getExtras().get(Intent.EXTRA_STREAM);
				intent.putExtra(Intent.EXTRA_STREAM, uri);
				intent.putExtra(intentFlag, true);
			}					
			startActivity(intent);
			finish();
		}
	}
	
	private static final int USER_PASSWORD_INVALIDED = 0;
	private static final int BAD_GATE_WAY_ERROR = 1;
	public void showErrorDialog(int errorId){
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ShareLoginActivity.this);
		String errorMessage = "";
		builder.setTitle("");
		switch (errorId) {
		case USER_PASSWORD_INVALIDED:
			errorMessage = getString(R.string.share_signin_error_usernamepassword);
			break;
		case BAD_GATE_WAY_ERROR:
			errorMessage = getString(R.string.share_signin_error_usernamepassword);
//			errorMessage = getString(R.string.share_auth_bad_gateway);  // to make it consistent with iOS.. sigh...
			break;
		default:
			errorMessage = getString(R.string.share_signin_error_usernamepassword);
			break;
		}
		builder.setMessage(errorMessage);
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
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}

	private void setupEvents(){
		btCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		btSignIn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String email = etEmailAddress.getText().toString().trim();
				String password = etPassword.getText().toString().trim();
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				Editor editor = prefs.edit();
				editor.putString(PrintHelper.SHARE_EMAIL_FLAG, email);
				editor.putString(PrintHelper.SHARE_PASSWORD_FLAG, password);
				editor.commit();
				
				if(!email.contains("@") || email.equals("") || password.equals("")){
					showErrorDialog(USER_PASSWORD_INVALIDED);
				} else {
					new Thread(signInRunnable).start();
				}
			}
		});
		
		etEmailAddress.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				
			}
		});
		
		cbUpload.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor editor = prefs.edit();
				editor.putBoolean(PrintHelper.IS_AUTO_UPLOAD2ALBUMS, isChecked);
				editor.commit();
			}
		});
	}

}
