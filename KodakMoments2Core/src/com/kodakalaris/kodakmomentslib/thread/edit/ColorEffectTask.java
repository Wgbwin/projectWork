package com.kodakalaris.kodakmomentslib.thread.edit;

import java.util.List;

import android.content.Context;
import android.os.Handler;

import com.kodakalaris.kodakmomentslib.AppConstants.PhotoUploadingState;
import com.kodakalaris.kodakmomentslib.activity.photoedit.MPhotoEditActivity;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.imageedit.ColorEffect;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.util.Log;

public class ColorEffectTask implements Runnable {
	private String TAG = LevelEditTask.class.getSimpleName();
	private PrintItem printItem;
	private Handler editHandler;
	private PhotoInfo photoInfo = null;;
	private GeneralAPI service = null;
	private List<ColorEffect> list;
	private int index;

	public ColorEffectTask(Context context, PrintItem printItem,
			Handler editHandler, GeneralAPI service, List<ColorEffect> list,
			int index) {
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
		this.list = list;
		this.index = index;
	}

	@Override
	public void run() {
		// Todo change the printItem add the original printItem now is wrong
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
					service.setColorEffectTask(photoInfo.getImageResource().id,
							index);
					succeed = true;
				} catch (WebAPIException e) {
					e.printStackTrace();
					succeed = false;
				}
				if (succeed) {
					printItem.colorEffect = list.get(index);
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