package com.kodakalaris.kodakmomentslib.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;

public class UploadPhotoReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(AppConstants.UPLOAD_PHOTO_ACTION.equals(action)){
			PhotoInfo photo = (PhotoInfo) intent.getSerializableExtra(AppConstants.UPLOAD_PHOTO_FLAG);
			if(photo.getPhotoUploadingState().isUploadedSuccess()){
//				Toast.makeText(context, "succeed to upload photo : "+photo.toString(), Toast.LENGTH_SHORT).show();
			}else if (photo.getPhotoUploadingState().isUploadedFailed()){
//				Toast.makeText(context, "failed to upload photo : "+photo.toString(), Toast.LENGTH_SHORT).show();
			}
		
		}
		
	}

}
