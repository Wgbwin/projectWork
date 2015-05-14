package com.kodak.kodak_kioskconnect_n2r;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.kodak.shareapi.AccessTokenResponse;
import com.kodak.shareapi.ClientTokenResponse;
import com.kodak.shareapi.GalleryService;
import com.kodak.shareapi.TokenGetter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ShareUploadActivity extends Activity {
	private static final String tag = ShareUploadActivity.class.getSimpleName();

	private TextView tvTitle;

	public static TextView tvUploadStatus;
	private TextView tvEmail;
	public static ProgressBar pbProgress;
	public static ImageView currentImage;

	public static String stUploadStatus;
	private String packName;

	private int windowHeight, windowWidth;
	private boolean launchByOther = false;
	private String intentFlag = "launchByOther";
	private String oriChangedFlag = "ori_changed";
	private String showProgress = "showProgress";
	private String status = "status";
	public static boolean isUploading = false;
	private int orientation;
	public static boolean needGc = false;
	public static int currentImageId = -1;
	public static Bitmap currentBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(tag, "onCreate....");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shareupload);

		tvTitle = (TextView) findViewById(R.id.headerBarText);
		tvUploadStatus = (TextView) findViewById(R.id.share_upload_status);
		tvEmail = (TextView) findViewById(R.id.share_type);
		pbProgress = (ProgressBar) findViewById(R.id.share_upload_progress);
		currentImage = (ImageView) findViewById(R.id.share_upload_image);

		launchByOther = getIntent().getBooleanExtra(intentFlag, false);
		orientation = getResources().getConfiguration().orientation;
		if (launchByOther) {
			if (savedInstanceState != null && savedInstanceState.getInt(oriChangedFlag)!=orientation) {
				Log.e(tag, "savedInstanceState is not null");
				Log.e(tag, "currentId: " + currentImageId + ", imageView: " + (currentImage==null) + ", bitmap: " + (currentBitmap==null));
				initUploadStatus();
				pbProgress.setVisibility(savedInstanceState.getInt(showProgress));
				pbProgress.setProgress(PrintHelper.uploadedShare2WmcQueue.size());
				stUploadStatus = savedInstanceState.getString(status);
				tvUploadStatus.setText(stUploadStatus);
				if(currentBitmap!=null){
					currentImage.setImageBitmap(currentBitmap);
				}
			} else {
				prepareOtherAppShare();
				initUploadStatus();
				if (!isUploading) {
					new Thread(uploadRunnable).start();
				}
			}
		} else {
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			initUploadStatus();
			if (!isUploading) {
				PrintHelper.uploadedShare2WmcQueue = new ArrayList<String>();
				new Thread(uploadRunnable).start();
			}
		}
	}
	
	private void prepareOtherAppShare(){
		PrintHelper.uploadShare2WmcQueue = new ArrayList<String>();
		PrintHelper.uploadedShare2WmcQueue = new ArrayList<String>();
		PrintHelper.selectedFileNames = new HashMap<String, String>();
		//PrintHelper.uploadedImageIDs = new HashMap<String, String>();
		PrintHelper.uploadQueue = new ArrayList<String>();
		String extra = getIntent().getExtras().get(Intent.EXTRA_STREAM).toString();
		String[] s = extra.split(":");
		if(s.length>0 && s[0].equals("file")){
			PrintHelper.uploadShare2WmcQueue.add("null");
			PrintHelper.selectedFileNames.put("null", URLDecoder.decode(extra.split("//")[1]));
		} else {
			Uri uri = Uri.parse(extra);
			PrintHelper.uploadShare2WmcQueue.add(uri.toString());
			ContentResolver cr = this.getContentResolver();
			Cursor cursor = null;
			try{
				cursor = cr.query(uri, null, null, null, null);
				cursor.moveToFirst();
				String fileName = cursor.getString(cursor.getColumnIndex(MediaColumns.DATA));
				PrintHelper.selectedFileNames.put(uri.toString(), fileName);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(cursor != null && !cursor.isClosed()){
					cursor.close();
				}
			}
		}
	}

	@Override
	protected void onResume() {
		Log.d(tag, "onResume....");
		super.onResume();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(tag, "onConfigurationChanged....");
		if (launchByOther) {
			initSize();
		} else {
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			initSize();
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(tag, "onSaveInstanceState....");
		super.onSaveInstanceState(outState);
		outState.putInt(oriChangedFlag, orientation);
		outState.putInt(showProgress, pbProgress.getVisibility());
		outState.putString(status, stUploadStatus);
		Log.e(tag, stUploadStatus);
	}

	@Override
	protected void onDestroy() {
		Log.d(tag, "onDestroy....");
		if(needGc){
			Log.d(tag, "calling System.gc()....");
			stUploadStatus = null;
			currentImageId = -1;
			tvUploadStatus = null;
			pbProgress = null;
			currentImage = null;
			if(currentBitmap!=null){
				currentBitmap.recycle();
				currentBitmap = null;
			}
			System.gc();
			needGc = false;
		}
		super.onDestroy();
		
	}

	private void initUploadStatus() {
		Log.d(tag, "initUploadStatus....");
		initSize();
		initTexts();
	}

	private void initTexts() {
		Log.d(tag, "initTexts....");
		// title
		packName = getApplicationContext().getPackageName();
		if (packName.contains("wmc")) {
			tvTitle.setText(getString(R.string.sendingOrder));
		}
		// upload status text
		stUploadStatus = getString(R.string.share_upload_status);
		stUploadStatus = stUploadStatus.replace("##", PrintHelper.uploadedShare2WmcQueue.size() + "");
		stUploadStatus = stUploadStatus.replace("%%", (PrintHelper.uploadShare2WmcQueue.size() + PrintHelper.uploadedShare2WmcQueue.size()) + "");
		tvUploadStatus.setText(stUploadStatus);
		// progress bar
		pbProgress.setMax(PrintHelper.uploadShare2WmcQueue.size() + PrintHelper.uploadedShare2WmcQueue.size());
		pbProgress.setProgress(0);
		// album text
		tvEmail.setText(getString(R.string.wmc_upload_type));
	}

	private void initSize() {
		Log.d(tag, "initSize....");
		// gallery and progress bar part
		windowWidth = getWindowManager().getDefaultDisplay().getWidth();
		windowHeight = getWindowManager().getDefaultDisplay().getHeight();
	}

	@Override
	public void onBackPressed() {
		if (launchByOther) {
			needGc = true;
			PrintHelper.uploadShare2WmcQueue.clear();
			PrintHelper.uploadedShare2WmcQueue.clear();
			finish();
		} else {
			
		}
	}

	private static final int UPLOAD_FINISH = 0x000001;
	private static final int UPLOAD_REFRESH = 0x000002;
	private static final int UPLOAD_FAILED = 0x000003;
	Handler statusHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case UPLOAD_FINISH:
				isUploading = false;
				if (launchByOther) {
					stUploadStatus = getString(R.string.share_upload_complete);
					tvUploadStatus.setText(stUploadStatus);
				} else {
					needGc = true;
					PrintHelper.StartOver();
					Intent intent = new Intent(ShareUploadActivity.this, ImageSelectionActivity.class);
					intent.putExtra("share_success", true);
					startActivity(intent);
					finish();
				}
				break;
			case UPLOAD_REFRESH:
				stUploadStatus = getString(R.string.share_upload_status);
				stUploadStatus = stUploadStatus.replace("##", PrintHelper.uploadedShare2WmcQueue.size() + "");
				stUploadStatus = stUploadStatus.replace("%%", (PrintHelper.uploadedShare2WmcQueue.size() + PrintHelper.uploadShare2WmcQueue.size()) + "");
				tvUploadStatus.setText(stUploadStatus);

				pbProgress.setProgress(PrintHelper.uploadedShare2WmcQueue.size());
				Log.e(tag, "PrintHelper.uploadedShare2WmcQueue: " + PrintHelper.uploadedShare2WmcQueue.size());
				if(PrintHelper.uploadedShare2WmcQueue!=null && PrintHelper.uploadedShare2WmcQueue.size()>0 && currentImageId != PrintHelper.uploadedShare2WmcQueue.size()-1){
					if(currentBitmap!=null){
						currentBitmap.recycle();
						currentBitmap = null;
					}
					currentImageId = PrintHelper.uploadedShare2WmcQueue.size()-1;
					String uri = PrintHelper.uploadedShare2WmcQueue.get(currentImageId);
					String fileName = PrintHelper.selectedFileNames.get(uri);
					currentBitmap = getThumbnail(uri, fileName);
					currentImage.setImageBitmap(currentBitmap);
				}
				break;
			case UPLOAD_FAILED:
				isUploading = false;
				stUploadStatus = getString(R.string.share_upload_failed_title);
				tvUploadStatus.setText(stUploadStatus);
				pbProgress.setVisibility(View.GONE);

				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ShareUploadActivity.this);
				builder.setTitle("");
				String message = getString(R.string.share_upload_failed_title)+ "\n";
				message += getString(R.string.share_upload_error_no_responding);
				builder.setMessage(message);
				builder.setPositiveButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which) {
						dialog.dismiss();
						if(!launchByOther){
							PrintHelper.uploadShare2WmcQueue = PrintHelper.selectedImageUrls;
						} else {
							prepareOtherAppShare();
						}
						initUploadStatus();
						pbProgress.setVisibility(View.VISIBLE);
						new Thread(uploadRunnable).start();
					}
				});
				builder.setNegativeButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						needGc = true;
						PrintHelper.StartOver();
						finish();
					}
				});
				builder.setCancelable(false);
				builder.create().show();
				break;
			}

		}

	};
	
	private Bitmap getThumbnail(String uri, String fileName) {
		Bitmap img = null;
		BitmapFactory.Options options = new Options();
		if(uri.equals("null")){
			Log.i(tag, "fileName: " + fileName);
			/*options.inJustDecodeBounds = true;
			img = BitmapFactory.decodeFile(fileName, options);
			 float realWidth = options.outWidth;  
		     float realHeight = options.outHeight;
		     int sampleSize = 1;
		     if(windowWidth>windowHeight){
		    	 if(realWidth>windowWidth/2){
		    		 sampleSize = (int) (realWidth/(windowWidth/2));
		    	 }
		    	 if(sampleSize < 1){
		    		 sampleSize = 1;
		    	 }
		     } else {
		    	 if(realWidth > windowHeight/2){
		    		 sampleSize = (int) (realWidth/(windowHeight/2));
		    	 }
		    	 if(sampleSize < 1){
		    		 sampleSize = 1;
		    	 }
		     }*/
		     options.inSampleSize = 1;
		     options.inJustDecodeBounds = false;
		     img = BitmapFactory.decodeFile(fileName, options);  
		} else {
			img = PrintHelper.loadThumbnailImage(uri, MediaStore.Images.Thumbnails.MINI_KIND, options, ShareUploadActivity.this);
		}
		return img;
	}
	
	Runnable uploadRunnable = new Runnable() {

		@Override
		public void run() {
			//TODO Refresh Token.
			isUploading = true;
			boolean refreshTokenFailed = false;
			long now = new Date().getTime()/1000;
			long expire = Long.parseLong(PrintHelper.getAccessTokenResponse(getApplicationContext()).expire_in);
			long pass = now - PrintHelper.getAccessTokenResponse(getApplicationContext()).getAccessTokenTime;
			Log.e(tag, "AccessToken expire in: " + expire + "; Time have pass: " + pass);
			if(((pass + 60) > expire) || PrintHelper.getAccessTokenResponse(getApplicationContext()).access_token.equals("")){
				//Refresh Token.
				TokenGetter tokenGetter = new TokenGetter();
				ClientTokenResponse clientTokenResponse = tokenGetter.httpClientTokenUrlPost(ShareLoginActivity.CLIENT_TOKEN);
				int count = 0;
				while (count < 3 && clientTokenResponse == null)
				{
					clientTokenResponse = tokenGetter.httpClientTokenUrlPost(ShareLoginActivity.CLIENT_TOKEN);
					count++;
				}
				if(clientTokenResponse != null){
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					String username = prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "");
					String userPwd = prefs.getString(PrintHelper.SHARE_PASSWORD_FLAG, "");
					AccessTokenResponse accessTokenResponse = null;
					try {
						int count1 = 0;
						while (count1 < 3 && accessTokenResponse == null)
						{
							Log.e(tag, "Account: " + username + " Password: " + userPwd);
							accessTokenResponse = tokenGetter.httpAccessTokenUrlPost(ShareLoginActivity.ACCESS_TOKEN_HOST, clientTokenResponse.client_token, username, userPwd, clientTokenResponse.client_secret);
							count1++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (accessTokenResponse != null	&& accessTokenResponse.status.equals("OK")) {
						Log.d(tag, "Refresh access token successfully.");
						PrintHelper.setAccessTokenResponse(accessTokenResponse, getApplicationContext());
					} else {
						refreshTokenFailed = true;
						Log.e(tag, "Refresh access token failed.");
						//PrintHelper.setAccessTokenResponse(accessTokenResponse, getApplicationContext());
					}
				} else {
					refreshTokenFailed = true;
					Log.e(tag, "Can not get client token response.");
				}
			}

			if (refreshTokenFailed) {
				statusHandler.sendEmptyMessage(UPLOAD_FAILED);
			} else {
				Log.d(tag, "create gallery....");
				GalleryService galleryService = new GalleryService();
				String url = galleryService.galleryURL;
				String app = "CUMMOBANDWMC";
				String app_version = "1.0";
				String retailer = null;// "walmart-CAN";
				String partner = null;
				String country = null;
				String name = PreferenceManager.getDefaultSharedPreferences(ShareUploadActivity.this).getString("share_album_name",	null);
				String token = PrintHelper.getAccessTokenResponse(getApplicationContext()).access_token;

				PrintHelper.galleryUUID = galleryService.createAGallery(url,app, app_version, retailer, partner, country, name,token);
				Log.d(tag, "create gallery response: " + PrintHelper.galleryUUID);
				// TODO if faild should show error dialog
				if (PrintHelper.galleryUUID == null || PrintHelper.galleryUUID.equals("")) {
					statusHandler.sendEmptyMessage(UPLOAD_FAILED);
				} else {
					Log.d(tag, "start share pictures....");
					Class<com.kodak.kodak_kioskconnect_n2r.PictureUploadService2> pictureUploadService2 = com.kodak.kodak_kioskconnect_n2r.PictureUploadService2.class;
					Intent serviceIntent = new Intent(ShareUploadActivity.this, pictureUploadService2);
					try
					{
						ComponentName serviceComponentName = startService(serviceIntent);
						if (serviceComponentName != null)
						{
							Log.i(tag, "startService called CompnentName=" + serviceComponentName.toString());
						}
					}
					catch (SecurityException se)
					{
						se.printStackTrace();
					}
					try {
						stopService(serviceIntent);
					} catch (SecurityException sex) {
						sex.printStackTrace();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					PictureUploadService2.isManualStartShare = true;
					while (PrintHelper.uploadShare2WmcQueue.size() > 0 && !PrintHelper.uploadShare2WmcError) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						statusHandler.sendEmptyMessage(UPLOAD_REFRESH);
					}
					if (PrintHelper.uploadShare2WmcError) {
						PrintHelper.uploadShare2WmcError = false;
						statusHandler.sendEmptyMessage(UPLOAD_FAILED);
					} else {
						statusHandler.sendEmptyMessage(UPLOAD_FINISH);
					}
				}
			}

		}
	};
}
