package com.kodakalaris.video.video_gen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class VideoGenResultReceiver extends BroadcastReceiver {

	private static final String TAG = VideoGenResultReceiver.class.getSimpleName();
	public static final String UPLOAD_COMPLETE_RESPONCE = "com.kodak.kodak.rsscombinedapp.UPLOAD_COMPLETE_RESPONCE";
	public static final String RESULT_CODE = "com.kodak.kodak.rsscombinedapp.RESULT_CODE";
	
	@Override
	public void onReceive(Context context, Intent intent) {

	}
}
