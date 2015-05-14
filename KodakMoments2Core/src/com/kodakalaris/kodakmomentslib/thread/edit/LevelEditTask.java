package com.kodakalaris.kodakmomentslib.thread.edit;

import android.content.Context;
import android.os.Handler;

import com.kodakalaris.kodakmomentslib.AppConstants.PhotoUploadingState;
import com.kodakalaris.kodakmomentslib.activity.photoedit.MPhotoEditActivity;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.util.Log;

public class LevelEditTask implements Runnable {
	private String TAG = LevelEditTask.class.getSimpleName();
	private PrintItem printItem;
	private Handler editHandler;
	private GeneralAPI service = null;
	private PhotoInfo photoInfo;

	public LevelEditTask(Context context, PrintItem printItem,
			Handler editHandler, GeneralAPI service) {
		super();
		for (PrintItem item : PrintManager.getInstance(context).getPrintItems()) {
			if (item.isCheckedInstance) {
				this.photoInfo = item.getImage();
				break;
			}
		}
		this.printItem = printItem;
		this.editHandler = editHandler;
		this.service = service;
	}

	@Override
	public void run() {
		Object[] array = new Object[2];
		array[0] = printItem.getImage();
		editHandler.obtainMessage(MPhotoEditActivity.START, array)
				.sendToTarget();
		while (photoInfo.getPhotoUploadingState() == PhotoUploadingState.UPLOADING
				|| photoInfo.getPhotoUploadingState() == PhotoUploadingState.INITIAL) {
			continue;
		}
		if (photoInfo.getPhotoUploadingState() == PhotoUploadingState.UPLOADED_SUCCESS) {
			if (photoInfo.getImageResource() != null) {
				Log.i(TAG, "photoInfo.getImageResource()");
				boolean succeed;
				try {
					if (printItem.isUseEnhance) {
						service.setKPTLevelTask(
								photoInfo.getImageResource().id, 0);
					} else {
						service.setKPTLevelTask(
								photoInfo.getImageResource().id, 1);
					}
					succeed = true;
				} catch (WebAPIException e) {
					e.printStackTrace();
					succeed = false;
				}
				if (succeed) {
					printItem.isUseEnhance = !printItem.isUseEnhance;
					if (printItem.getImage().getImageResource() == null) {
						printItem.getImage().setImageResource(
								photoInfo.getImageResource());
					}
					Log.i(TAG, "succeed:" + succeed);
				}
				array[1] = succeed;
				editHandler.obtainMessage(MPhotoEditActivity.CONTINUE, array)
						.sendToTarget();
			} else {
				Log.i(TAG, "photoInfo.getImageResource()==null");
			}
		} else {
			array[1] = false;
			editHandler.obtainMessage(MPhotoEditActivity.CONTINUE, array)
					.sendToTarget();
		}

	}

}