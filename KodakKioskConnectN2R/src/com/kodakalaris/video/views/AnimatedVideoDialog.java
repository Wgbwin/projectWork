package com.kodakalaris.video.views;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodakalaris.video.activities.AddTitleActivity;
import com.kodakalaris.video.activities.BaseActivity;
import com.kodakalaris.video.storydoc_format.VideoGenParams;
import com.kodakalaris.video.views.AnimatedVideoView.AnimatedVideoViewHoldingActivity;

public class AnimatedVideoDialog extends Dialog {

	private AnimatedVideoView mVideo;
	private AnimatedVideoViewHoldingActivity mCallback;
	private ImageButton activity_preview_dialog_cancel;
	public AnimatedVideoDialog(AnimatedVideoViewHoldingActivity callback, VideoGenParams params,Context context, int theme) {
		//super(activity);
		super(context, theme);
		mCallback = callback;
		setTitle(params.mProjectTitle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_my_stories_play_dialog_parent);
		activity_preview_dialog_cancel = (ImageButton) findViewById(R.id.activity_preview_dialog_cancel);
		activity_preview_dialog_cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AnimatedVideoDialog.this.dismiss();
				
			}
		});
		mVideo = (AnimatedVideoView) findViewById(R.id.my_stories_play_dialog_video);
		for (int i = 0; i < 3; i++) {
			VideoGenParams.Vignette vig = params.mVignettes.get(i);
			vig.mStartBounds = BaseActivity.calculateStartBounds(vig.mImagePath);

			vig.mLength = AddTitleActivity.calculateLength(vig.mAudioPath);
		}
		mVideo.initAnimation(new AnimatedVideoViewHoldingActivity() {
			@Override
			public void onVideoAnimationEnded() {
				dismiss();
			}

			@Override
			public void onShouldStartAudio(int index) {
				mCallback.onShouldStartAudio(index);
			}

			@Override
			public void onReadyToPlay() {
				mVideo.startVideoAnimation();
				mCallback.onReadyToPlay();
			}
		}, params);
	}

	@Override
	protected void onStop() {
		mCallback.onVideoAnimationEnded();
		mVideo.stopAnimation();
		mVideo = null;
		super.onStop();
	}

	@Override
	public void show() {
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
	    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
	    super.show();
	    getWindow().setAttributes(lp);
		
	}
	

}
