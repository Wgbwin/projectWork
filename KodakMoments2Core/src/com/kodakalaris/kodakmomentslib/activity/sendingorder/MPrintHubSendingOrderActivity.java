package com.kodakalaris.kodakmomentslib.activity.sendingorder;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.PrintHubOrderConfirmationActivity.MPrintHubOrderConfirmationActivity;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.service.PrintHubUploadService;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.mobile.PrintHubSendOrderErrorDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class MPrintHubSendingOrderActivity extends BaseSendingOrderActivity {
	private UploadingPrintHubPhotoReceiver mUploadingPrintHubPhotoReceiver;
	private ImageView vImgSendingImage;
	private ProgressBar vProgressBar;
	private TextView vTxtSendingProgress;
	private TextView vTxtSendingPrompts;
	private List<PhotoInfo> allPhotos;
	private int currIndex = 0;
	private boolean uploadDone = false;
	private Context mContext;
	private DisplayImageOptions imageLoadOptions = null;

	// private int uploadErrorCounter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = MPrintHubSendingOrderActivity.this;
		registerPrintHubReceiver();
		startPrintHubUploadService();
		setInitState();
	}

	@Override
	protected void setKMContentView() {
		setContentView(R.layout.activity_m_sending_order);
	}

	@Override
	protected void initViews() {
		vImgSendingImage = (ImageView) findViewById(R.id.img_sending_image);
		vProgressBar = (ProgressBar) findViewById(R.id.progressbar_sending);
		vTxtSendingProgress = (TextView) findViewById(R.id.txt_sending_progress);
		vTxtSendingPrompts = (TextView) findViewById(R.id.txt_sending_prompts);
	}

	@Override
	protected void setEvents() {

	}

	@Override
	protected void initData() {
		allPhotos = PrintHubManager.getInstance().getmPrintPhotos();
		mUploadingPrintHubPhotoReceiver = new UploadingPrintHubPhotoReceiver();
		vProgressBar.setMax(allPhotos.size());
		imageLoadOptions = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.imageerror).showImageOnFail(R.drawable.imageerror).cacheInMemory(false)
				.cacheOnDisk(false).considerExifParams(true).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).build();
	}

	private void setInitState() {
		updateUploadingImageView();
		updateUploadingProgress();
	}

	private void updateUploadingImageView() {
		if (uploadDone) {
			vImgSendingImage.setVisibility(View.INVISIBLE);
		} else {
			PhotoInfo currentUploadingPhoto = allPhotos.get(currIndex);
			if (currentUploadingPhoto != null) {
				vImgSendingImage.setVisibility(View.VISIBLE);
				String filePathe = currentUploadingPhoto.getPhotoEditPath().equals("") ? currentUploadingPhoto.getPhotoPath() : currentUploadingPhoto.getPhotoEditPath();
				ImageLoader.getInstance().displayImage("file://" + (filePathe), vImgSendingImage, imageLoadOptions);
			}
		}
	}

	private void updateUploadingProgress() {
		int total = allPhotos.size();
		int successNum = currIndex;
		vTxtSendingProgress.setVisibility(View.VISIBLE);
		vTxtSendingProgress.setText(getString(R.string.SendingOrder_sendingProgress, successNum, total));
		if (uploadDone) {
			vTxtSendingPrompts.setText(R.string.SendingOrder_preparingOrder);
		} else {
			vTxtSendingPrompts.setText(R.string.SendingOrder_sendingPhotos);
		}
		vProgressBar.setProgress(currIndex);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(mUploadingPrintHubPhotoReceiver);
			stopHubUploadService();
		} catch (Exception e) {

		}
	}

	class UploadingPrintHubPhotoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (AppConstants.UPLOAD_PRINTHUB_PHOTO_ACTION.equals(action)) {
				int result = intent.getIntExtra(AppConstants.UPLOAD_PRINTHUB_PHOTO_RESULT, AppConstants.UPLOAD_PRINTHUB_PHOTO_FAIL);
				currIndex = intent.getIntExtra(AppConstants.UPLOAD_PRINTHUB_PHOTO_INDEX, 0);
				uploadDone = (currIndex == allPhotos.size() ? true : false);
				String currentJobId = intent.getStringExtra(AppConstants.UPLOAD_PRINTHUB_JOB_ID);
				switch (result) {
				case AppConstants.UPLOAD_PRINTHUB_PHOTO_SUCCESS:
					runOnUiThread(new Runnable() {
						public void run() {
							setInitState();
						}
					});
					if (uploadDone) {
						Intent intentConfirm = new Intent(MPrintHubSendingOrderActivity.this, MPrintHubOrderConfirmationActivity.class);
						intentConfirm.putExtra(MPrintHubOrderConfirmationActivity.INTENT_JOB_ID, currentJobId);
						startActivity(intentConfirm);
						finish();
					}
					break;
				case AppConstants.UPLOAD_PRINTHUB_PHOTO_FAIL:
					showErrorDialog(getString(R.string.PrintHub_ErrorDescription));
					break;
				case AppConstants.CREATE_PRINTJOB_FAIL:
					showErrorDialog(getString(R.string.PrintHub_ErrorDescription));
					break;
				case AppConstants.CONNECT_PRINTHUB_FAIL:
					showErrorDialog(getString(R.string.PrintHub_ErrorDescription));
					break;
				}

			}

		}
	}

	private void startPrintHubUploadService() {
		Intent serviceIntent = new Intent(this, PrintHubUploadService.class);
		try {
			ComponentName serviceComponentName = startService(serviceIntent);
			if (serviceComponentName != null) {
				Log.i("startPrintHubUploadService", "onCreate() startService called CompnentName=" + serviceComponentName.toString());
			}
		} catch (SecurityException se) {
			se.printStackTrace();
		}
	}

	private void stopHubUploadService() {
		Intent serviceIntent = new Intent(this, PrintHubUploadService.class);
		try {
			stopService(serviceIntent);
		} catch (SecurityException se) {
			se.printStackTrace();
		}
	}

	private void registerPrintHubReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(AppConstants.UPLOAD_PRINTHUB_PHOTO_ACTION);
		registerReceiver(mUploadingPrintHubPhotoReceiver, filter);
	}

	private void showErrorDialog(String errorMessage) {
		PrintHubSendOrderErrorDialog dialog = new PrintHubSendOrderErrorDialog(mContext, errorMessage, false);
		dialog.initDialog(mContext);
		dialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "nothing");
	}

}
