package com.kodakalaris.kodakmomentslib.thread.edit;

import java.util.List;

import android.content.Context;
import android.os.Handler;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.activity.photoedit.MPhotoEditActivity;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.imageedit.ColorEffect;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.util.FileDownloader;
import com.kodakalaris.kodakmomentslib.util.Log;

public class RestoreData implements Runnable {
	private String TAG = RestoreData.class.getSimpleName();
	private Context mContext;
	private PrintItem mPrintItem;
	private GeneralAPI mGeneralAPI;
	private Handler mHandler;
	private boolean retry = true;
	private boolean isWantedShowDiaglog = true;

	public RestoreData(Context context, PrintItem printItem, Handler handler,
			GeneralAPI generalAPI) {
		mContext = context;
		mPrintItem = printItem;
		mHandler = handler;
		mGeneralAPI = generalAPI;

	}

	@Override
	public void run() {
		Object[] array = new Object[2];
		if (mPrintItem.getImage().getImageResource() == null) {
			array[1] = false;
			mHandler.obtainMessage(MPhotoEditActivity.CONTINUE, array)
					.sendToTarget();
		}
		for (PrintItem item : PrintManager.getInstance(mContext)
				.getPrintItems()) {

			if (item.equals(mPrintItem)) {

				if (item.isUseRedEye != mPrintItem.isUseRedEye) {
					if (isWantedShowDiaglog) {
						mHandler.obtainMessage(MPhotoEditActivity.START, array)
								.sendToTarget();
						isWantedShowDiaglog = false;
					}
					while (retry) {
						try {
							mGeneralAPI.setAutoRedEyeTask(mPrintItem.getImage()
									.getImageResource().id, item.isUseEnhance);
							retry = false;
							Log.i(TAG, " do with RedEye success,go on");
						} catch (WebAPIException e) {
							Log.e(TAG, " do with RedEye fail,try again");
						}
					}
				} else {
					retry = false;
				}

				if (item.isUseEnhance != mPrintItem.isUseEnhance) {
					if (isWantedShowDiaglog) {
						mHandler.obtainMessage(MPhotoEditActivity.START, array)
								.sendToTarget();
						isWantedShowDiaglog = false;
					}
					int num = item.isUseEnhance ? 0 : 1;
					retry = true;
					while (retry) {
						try {
							mGeneralAPI.setKPTLevelTask(mPrintItem.getImage()
									.getImageResource().id, num);
							retry = false;
							Log.i(TAG, " do with Enhance success,go on");
						} catch (WebAPIException e) {
							Log.e(TAG, " do with Enhance fail,try again");
						}
					}

				} else {
					retry = false;
				}

				if (item.colorEffect == null && mPrintItem.colorEffect != null
						|| item.colorEffect != null
						&& item.colorEffect.id != mPrintItem.colorEffect.id) {
					if (isWantedShowDiaglog) {
						mHandler.obtainMessage(MPhotoEditActivity.START, array)
								.sendToTarget();
						isWantedShowDiaglog = false;
					}
					if (item.colorEffect == null) {
						retry = true;
						while (retry) {
							try {
								mGeneralAPI.setColorEffectTask(mPrintItem
										.getImage().getImageResource().id, 0);
								retry = false;
								Log.i(TAG, " do with colorEffect success,go on");
							} catch (WebAPIException e) {
								Log.e(TAG,
										" do with colorEffect fail,try again");
							}
						}
					} else {
						List<ColorEffect> list = KM2Application.getInstance()
								.getColorEffects();
						for (int i = 0; i < list.size(); i++) {
							if (item.colorEffect.id == list.get(i).id) {
								retry = true;
								while (retry) {
									try {
										mGeneralAPI.setColorEffectTask(
												mPrintItem.getImage()
														.getImageResource().id,
												i);
										retry = false;
										Log.i(TAG,
												" do with colorEffect success,go on");
									} catch (WebAPIException e) {
										Log.e(TAG,
												" do with colorEffect fail,try again");
									}
								}
								break;
							}
						}
					}

				} else {
					retry = false;
				}

				if (0 != mPrintItem.rotateDegree) {
					if (isWantedShowDiaglog) {
						mHandler.obtainMessage(MPhotoEditActivity.START, array)
								.sendToTarget();
						isWantedShowDiaglog = false;
					}
					retry = true;
					while (retry) {
						try {
							mGeneralAPI.rotateImageTask(mPrintItem.getImage()
									.getImageResource().id,
									mPrintItem.rotateDegree);
							retry = false;
							Log.i(TAG, " do with Rotate success,go on");
						} catch (WebAPIException e) {
							Log.e(TAG, " do with Rotate fail,try again");
						}
					}
				} else {
					retry = false;
				}

				if (!retry && !isWantedShowDiaglog) {
					boolean isOngoing = true;
					String imageUrl = mPrintItem.getImage().getImageResource()
							.fetchPreviewURL();
					String saveImagePath = KM2Application.getInstance()
							.getTempImageFolderPath()
							+ "/"
							+ mPrintItem.getImage().getImageResource().id
							+ ".jpg";
					while (isOngoing) {
						if (FileDownloader.download(imageUrl, saveImagePath)) {
							array[1] = true;
							item.getImage().setPhotoEditPath(saveImagePath);
							item.rotateDegree = 0;
							Log.i(TAG,
									"FileDownloader.download(imageUrl, saveImagePath)-->success");
							mHandler.obtainMessage(MPhotoEditActivity.CONTINUE,
									array).sendToTarget();
							isOngoing = false;
						}
					}
				} else if (!retry) {
					array[1] = false;
					mHandler.obtainMessage(MPhotoEditActivity.CONTINUE, array)
							.sendToTarget();
				}
				break;
			}
		}
	}
}
