package com.kodakalaris.photokinavideotest.video_gen;

import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class VideoGenIntentService extends IntentService {

	public static final String PARAM_VIDEO_GEN = "PARAM_VIDEO_GEN";
	public static final int RESULT_SUCCESS = 1;
	public static final int RESULT_FAILURE = 2;
	private static final String TAG = VideoGenIntentService.class.getSimpleName();

	public VideoGenIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle options = intent.getExtras();
		VideoGenParams params = (VideoGenParams) options.getParcelable(PARAM_VIDEO_GEN);
		try {
			makeTheVideo(params);
		} catch (Exception e) {
			returnResult(params, RESULT_FAILURE);
			e.printStackTrace();
		}
	}

	private void returnResult(VideoGenParams params, int resultCode) {
		Bundle retunValues = new Bundle();
		// retunValues.putString(VideoGenerationService.RESULT_VIDEO_GEN,
		// videoFilePath);
		params.mReviever.send(resultCode, retunValues);

	}
	/**
	 * This method is called on a worker thread. There should not be a reason to
	 * modify params. File paths are likely on the app internal storage
	 * directory /data/data/com.kodakalaris.photokinavideotest/files/projects/
	 * <Project_Guid>/Assets/<File_Name>
	 * 
	 * 
	 * @param params
	 */
	private void makeTheVideo(VideoGenParams params) {
		try {
			Thread.sleep(2000);// TODO Make a video, don't just sleep.....
			params.getVideoPath();// TODO write a file here
			returnResult(params, RESULT_SUCCESS);
		} catch (InterruptedException e) {
			returnResult(params, RESULT_FAILURE);
		}
	}
}
