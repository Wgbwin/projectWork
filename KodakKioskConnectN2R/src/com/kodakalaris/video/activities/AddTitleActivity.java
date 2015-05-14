package com.kodakalaris.video.activities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.Session;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.utils.RSSLocalytics;
import com.kodak.utils.SoftKeyboardUtil;
import com.kodak.utils.SoftKeyboardUtil.OnSoftKeyboardChangeListener;
import com.kodakalaris.video.MediaStoreUtils;
import com.kodakalaris.video.VideoAnimationProperty;
import com.kodakalaris.video.storydoc_format.VideoGenParams;
import com.kodakalaris.video.views.AnimatedVideoImage;
import com.kodakalaris.video.views.AnimatedVideoView;
import com.kodakalaris.video.views.AnimatedVideoView.AnimatedVideoViewHoldingActivity;
import com.kodakalaris.video.views.FocusSupportingEditText;

public class AddTitleActivity extends BaseActivity {

	private static String TAG = AddTitleActivity.class.getSimpleName();
	protected boolean mIsPlaying = false;
	private View mPlayCircle;
	private FocusSupportingEditText mEditTextTitle;
	private View mFocusClearingView;
	private FocusSupportingEditText mEditTextLocation;
	private AnimatedVideoView mAnimatedVideoView;
	private FocusSupportingEditText mEditTextDateTime;
	private boolean showDate = true;
	private boolean showLocation = true;
	private AnimatedVideoImage mVideoThumbnail;
	public static final String TMS_CREATE_TITLE="TMS Create - Title";
	private boolean IsEdit=false;
	public static final String TMS_EDIT_TITLE="TMS Edit - Title";
	public static int EndTime=0;
	public static final String TMS_CREATE_TIME="TMS Create Time";
	public static HashMap<String, String>attr;
	private boolean isNewStory = true;
	private String previousName = "";
	private boolean hasCopied = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentLayout(R.layout.activity_add_title);
		Intent intent=getIntent();
		IsEdit=intent.getBooleanExtra("TMS_EDIT", false);
		if(IsEdit){
			RSSLocalytics.recordLocalyticsPageView(this, TMS_EDIT_TITLE);
		}else{
			RSSLocalytics.recordLocalyticsPageView(this, TMS_CREATE_TITLE);}
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mEditTextTitle = (FocusSupportingEditText) findViewById(R.id.add_title_text_title);
		mEditTextDateTime = (FocusSupportingEditText) findViewById(R.id.add_title_text_date);
		mEditTextLocation = (FocusSupportingEditText) findViewById(R.id.add_title_text_location);
		mFocusClearingView = findViewById(R.id.add_title_dummy_focus);
		mPlayCircle = (ImageView) findViewById(R.id.add_title_play_circle);
		mVideoThumbnail = (AnimatedVideoImage) findViewById(R.id.add_title_video_thumbnail);
		mAnimatedVideoView = (AnimatedVideoView) findViewById(R.id.add_title_annimatedVideo);
		headerBar_tex.setText(R.string.TMS_add_title_title);
		setEvents();
		ViewTreeObserver viewTreeObserver = mVideoThumbnail.getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					BaseActivity.removeViewTreeObserverVersionSafe(this, mVideoThumbnail);
					mVideoThumbnail.setImageBitmapAndFilePath(mVideoGenParams.mVignettes.get(0).mImagePath);
					mVideoThumbnail.initConstantMatrix(mVideoGenParams.mVignettes.get(0).mStartBounds);
					mVideoThumbnail.setMatrixProperty(new VideoAnimationProperty(mVideoGenParams.mVignettes.get(0).mStartBounds));
				}
			});
		}
		isNewStory = mVideoGenParams.mProjectTitle==null || mVideoGenParams.mProjectTitle.equals("");
		previousName = mVideoGenParams.mProjectTitle;
		
		setupKeyboardLayoutWhenEdit();
	}

	private void initAnimatedVideo() {
		for (int i = 0; i < 3; i++) {
			VideoGenParams.Vignette vig = mVideoGenParams.mVignettes.get(i);
			vig.mStartBounds = calculateStartBounds(vig.mImagePath);
			Log.e(TAG, "Start bounds calculated:" + vig.mStartBounds.toShortString());
			vig.mLength = calculateLength(vig.mAudioPath);
		}
		mAnimatedVideoView.initAnimation(new AnimatedVideoViewHoldingActivity() {
			@Override
			public void onShouldStartAudio(int index) {
				playAudioFile(mVideoGenParams.mVignettes.get(index).mAudioPath);
			}

			@Override
			public void onVideoAnimationEnded() {
				onStartButtonClicked(null);
			}

			@Override
			public void onReadyToPlay() {
			}
		}, mVideoGenParams);
	}

	private void startAnimating() {
		mAnimatedVideoView.startVideoAnimation();
	}
	
	private int previousKeyboradHeight = -1;
	private void setupKeyboardLayoutWhenEdit() {
		SoftKeyboardUtil.observeSoftKeyboard(this, new OnSoftKeyboardChangeListener() {

			@Override
			public void onSoftKeyBoardChange(int softkeybardHeight, boolean visible) {
				Log.d(TAG,"softkeyboard height:" + softkeybardHeight + "  Visible:" + visible);
				
				if (previousKeyboradHeight == softkeybardHeight ) {
					return;
				}
				
				previousKeyboradHeight = softkeybardHeight;
				
				final View v = findViewById(R.id.preview_bottom_buttons);
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)v.getLayoutParams();
				if (visible) {
					params.addRule(RelativeLayout.BELOW, 0);
					params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					params.bottomMargin = softkeybardHeight + 20;
				} else {
					params.addRule(RelativeLayout.BELOW, R.id.add_title_video_holder);
					params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
					params.bottomMargin = 0;
				}
				v.requestLayout();
			}
		});
	}

	public static int calculateLength(String audioPath) {
		MediaMetadataRetriever m = new MediaMetadataRetriever();
		Log.i(TAG, "AudioPath:" + audioPath);
		try {
			FileInputStream fileInputStream = new FileInputStream(audioPath);
			m.setDataSource(fileInputStream.getFD());
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String duration = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		long dur = Long.parseLong(duration);
		// String seconds = String.valueOf((dur % 60000) / 1000);

		Log.v(TAG, "Seconds:" + dur / 1000.0f);
		// String minutes = String.valueOf(dur / 60000);
		// Log.v("minutes", minutes);
		// close object
		m.release();
		return (int) (dur);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		
		saveUIProjectChanges();
		Log.e(TAG, "onPausePreview");
		mAnimatedVideoView.stopAnimation();
		super.onPause();
		
	}

	private void saveUIProjectChanges() {
		if (mVideoGenParams != null) {
			String mProjectTitle = mEditTextTitle.getText().toString() ;
			mVideoGenParams.mProjectTitle  = mProjectTitle ;
			mVideoGenParams.mProjectSubTitleTimeDate = showDate ? mEditTextDateTime.getText().toString() : "";
			mVideoGenParams.mProjectSubTitleLocation = showLocation ? mEditTextLocation.getText().toString() : "";
			if(!hasCopied && !isNewStory && !previousName.equals(mVideoGenParams.mProjectTitle)){
				mVideoGenParams = VideoGenParams.makeCopy(this, mVideoGenParams);
				hasCopied = true;
			}
			
			
		}
		
		
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume PreviewActivity");
		super.onResume();
		mEditTextTitle.setText(mVideoGenParams.mProjectTitle == null ? "" : mVideoGenParams.mProjectTitle);
		if (mVideoGenParams.mProjectSubTitleTimeDate == null) {
			mVideoGenParams.mProjectSubTitleTimeDate = MediaStoreUtils.getExifTimeDateUserFriendly(mVideoGenParams.mVignettes.get(0).mImagePath);
		}
		if (mVideoGenParams.mProjectSubTitleLocation == null) {
			mVideoGenParams.mProjectSubTitleLocation = MediaStoreUtils.getExifLatLong(this, mVideoGenParams.mVignettes.get(0).mImagePath);
		}

		Log.i(TAG, "Location:" + mVideoGenParams.mProjectSubTitleLocation);
		mEditTextDateTime.setText(mVideoGenParams.mProjectSubTitleTimeDate == null || mVideoGenParams.mProjectSubTitleTimeDate.equals("null") ? "" : mVideoGenParams.mProjectSubTitleTimeDate);
		mEditTextLocation.setText(mVideoGenParams.mProjectSubTitleLocation == null ? "" : mVideoGenParams.mProjectSubTitleLocation);
		mAnimatedVideoView.removeAllViews();
		initAnimatedVideo();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	public void onDateImageButton(View v) {
		showDate = !showDate;
		if (showDate) {
			mEditTextDateTime.setText(mVideoGenParams.mProjectSubTitleTimeDate);
			mEditTextDateTime.setHint(R.string.TMS_add_title_hint_enabled_time_date);
			v.setBackgroundResource(R.drawable.tms_text_deselect_button);
		} else {
			mVideoGenParams.mProjectSubTitleTimeDate = mEditTextDateTime.getText().toString();
			mEditTextDateTime.setText("");
			mEditTextDateTime.setHint(R.string.TMS_add_title_hint_disabled_time_date);
			v.setBackgroundResource(R.drawable.tms_text_select_button);
		}
		mEditTextDateTime.changeStatus(showDate);
	}

	public void onLocationImageButton(View v) {
		showLocation = !showLocation;
		if (showLocation) {
			mEditTextLocation.setText(mVideoGenParams.mProjectSubTitleLocation);
			mEditTextLocation.setHint(R.string.TMS_add_title_hint_enabled_location);
			v.setBackgroundResource(R.drawable.tms_text_deselect_button);
		} else {
			mVideoGenParams.mProjectSubTitleLocation = mEditTextLocation.getText().toString();
			mEditTextLocation.setText("");
			mEditTextLocation.setHint(R.string.TMS_add_title_hint_disabled_location);
			v.setBackgroundResource(R.drawable.tms_text_select_button);
		}
		mEditTextLocation.changeStatus(showLocation);
	}

	public void onStartButtonClicked(View v) {
		// Log.i(TAG, "mIsPlaying is:" + mIsPlaying);
		if (!mIsPlaying) {
			Log.w(TAG, "Starting");
			mPlayCircle.setVisibility(View.GONE);
			mVideoThumbnail.setVisibility(View.GONE);
			mIsPlaying = true;
			startAnimating();

		} else {
			Log.w(TAG, "Stopping");
			mIsPlaying = false;
			mPlayCircle.setVisibility(View.VISIBLE);
			mVideoThumbnail.setVisibility(View.VISIBLE);
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
			}

		}
	}

	public void focusOnSomethingElse(TextView v) {
		mFocusClearingView.requestFocus();
		v.clearFocus();
	}

	public void setEvents() {
		TextWatcher textWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				
				mAnimatedVideoView.updateTitleText(mEditTextTitle.getText().toString(), mEditTextDateTime.getText().toString(), mEditTextLocation.getText().toString());
			    
			
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		};
		
		InputFilter emojiFilter = new InputFilter() {
			Pattern emoji = Pattern.compile(
				      "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
				       Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
				
				
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				Matcher emojiMatcher = emoji.matcher(source);

				if (emojiMatcher.find()) {
					return "";
				}
				
				return null ;
			}
			
		} ;
		
		mEditTextTitle.addTextChangedListener( textWatcher);
		mEditTextDateTime.addTextChangedListener( textWatcher);
		mEditTextLocation.addTextChangedListener(textWatcher);
		
		mEditTextTitle.setFilters(new InputFilter[]{emojiFilter}) ;
		mEditTextDateTime.setFilters(new InputFilter[]{emojiFilter}) ;
		mEditTextLocation.setFilters(new InputFilter[]{emojiFilter}) ;
		
		
	}
	protected static boolean ISFromAddTitle=false;
	
	public void onDoneButton(View v) {
		
		saveUIProjectChanges();
		Time time=new Time();
		time.setToNow();
		int EndHour=time.hour;
		int EndMinute=time.minute;
		int EndSecond=time.second;
		EndTime=EndHour*60*60+EndMinute*60+EndSecond;
		int TotalTime=EndTime-MyStoriesActivity.CreateTime;
		if (TotalTime>0) {
			attr=new HashMap<String, String>();
			if (TotalTime<=30) {
				attr.put(TMS_CREATE_TIME, "000-030s");
			}else if (TotalTime>30&TotalTime<=60) {
				attr.put(TMS_CREATE_TIME, "031-060s");
			}else if (TotalTime>60&TotalTime<120) {
				attr.put(TMS_CREATE_TIME, "060-119s");
			}else if (TotalTime>=120&TotalTime<300) {
				attr.put(TMS_CREATE_TIME, "120-299s");
			}else if (TotalTime>=300&TotalTime<600) {
				attr.put(TMS_CREATE_TIME, "300-599s");
			}else if (TotalTime>=600) {
				attr.put(TMS_CREATE_TIME, "600s+");
			}
			RSSLocalytics.recordLocalyticsEvents(AddTitleActivity.this, TMS_CREATE_TIME, attr);
			
		}
		ISFromAddTitle=true;
		Intent intent = new Intent(this, PreviewActivity.class);
		Bundle options = new Bundle();
		options.putString(INSTANCE_STATE_KEY_VIDEO_PARAMATERS, mVideoGenParams.mUUID.toString());
		intent.putExtras(options);
		intent.putExtra("TMS_EDIT", IsEdit);
		startActivity(intent);	
	    
	
	}
 
	 


}
