package com.kodakalaris.video.activities;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.utils.RSSLocalytics;
import com.kodakalaris.video.SquareImageViewClickListener;
import com.kodakalaris.video.activities.TMSSelectPhotosActivity.FilePersisterHelper;
import com.kodakalaris.video.storydoc_format.VideoGenParams;
import com.kodakalaris.video.storydoc_format.VideoGenParams.Vignette;
import com.kodakalaris.video.views.ROIImageView;
import com.kodakalaris.video.views.SquareImageView;

public class AddAudioActivity extends BaseActivity implements SquareImageViewClickListener {

	private static final String TAG = AddAudioActivity.class.getSimpleName();
	private static final String PARAM_SELECTED_INDEX = "PARAM_SELECTED_INDEX";
	private static final String PARAM_ROIS = "PARAM_ROIS";
	private static final String SHOW_ADD_AUDIO_HELP = "SHOW_ADD_AUDIO_HELP";
	private static final int AUDIO_MAX_DURATION = 10000;//10 seconds
	private static String AUDIO_FILE_EXTENTION = ".mp4";

	private ImageView mStartRecordingButton;
	private MediaRecorder mMediaRecorder;
	private boolean isRecording;
	private boolean isPlaying;
	private ImageView mPlaybackRecordingButton;
	private MediaPlayer mMediaPlayer;
	private View mDoneButton;
	private int mCurrentlySelectedIndex = 0;
	private ViewGroup mRootView;
	private ArrayList<View> mBackgroundImageViews;
	private ArrayList<ImageView> mIndicatorViews;
	private ArrayList<ROIImageView> mLargePreviewViews;
	ArrayList<SquareImageView> mSelectedImagesViews;
	private int[] mRecordTimerMilliseconds = new int[3];
	private TextView mTimerView;
	private Timer mTimer;
	private ImageView mNextImageArrow;
	private ImageView mPreviousImageArrow;
	private TextView mStartRecordingButtonLabel;
	private ProgressBar mTimerProgress;
	private TextView mTimeLeftView;
	private View mHelpView;
	private View mHelpAllSetButton;
	private CheckBox mDontShowHelpCkBox;
	public static final String TMS_CREATE_NARRATE="TMS Create -  Narrate";
	public static final String TMS_RECORD="TMS Record";
	public static final String TMS_RECORD_AUDIO="TMS Record Audio";
	public static HashMap<String, String>attr;
	public static HashMap<String, String>recordTimeHashMap;
	private boolean IsEditActivity=false;
	public static final String TMS_EDIT_NARRATE="TMS Edit -  Narrate";
	public static final String TMS_AUDIO_TIME="TMS Audio Time";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentLayout(R.layout.activity_add_audio);
		Intent intent=getIntent();
		IsEditActivity=intent.getBooleanExtra("TMS_EDIT", false);
		if(IsEditActivity){
			RSSLocalytics.recordLocalyticsPageView(this, TMS_EDIT_NARRATE);
		}else{
			RSSLocalytics.recordLocalyticsPageView(this, TMS_CREATE_NARRATE);}
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		headerBar_tex.setText(R.string.TMS_add_audio_title);
		
		mSelectedImagesViews = new ArrayList<SquareImageView>();
		mSelectedImagesViews.add((SquareImageView) findViewById(R.id.three_across_top_1));
		mSelectedImagesViews.add((SquareImageView) findViewById(R.id.three_across_top_2));
		mSelectedImagesViews.add((SquareImageView) findViewById(R.id.three_across_top_3));
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			SquareImageView v = mSelectedImagesViews.get(i);
			v.setImagePosition(i);
		}

		mLargePreviewViews = new ArrayList<ROIImageView>();
		mLargePreviewViews.add((ROIImageView) findViewById(R.id.add_audio_large_preview_0));
		mLargePreviewViews.add((ROIImageView) findViewById(R.id.add_audio_large_preview_1));
		mLargePreviewViews.add((ROIImageView) findViewById(R.id.add_audio_large_preview_2));

		mStartRecordingButton = (ImageView) findViewById(R.id.add_audio_start_recording_button);
		mStartRecordingButtonLabel = (TextView) findViewById(R.id.add_audio_record_text_field);
		mTimerView = (TextView) findViewById(R.id.activity_add_audio_timer_time);
		mTimeLeftView = (TextView) findViewById(R.id.add_audio_timer_time_left);
		mTimerProgress = (ProgressBar) findViewById(R.id.add_audio_timer_bar);
		mTimerProgress.setMax(AUDIO_MAX_DURATION);

		mRootView = (ViewGroup) findViewById(R.id.add_audio_root_container);
		mBackgroundImageViews = new ArrayList<View>();
		mBackgroundImageViews.add(findViewById(R.id.three_across_top_1_background));
		mBackgroundImageViews.add(findViewById(R.id.three_across_top_2_background));
		mBackgroundImageViews.add(findViewById(R.id.three_across_top_3_background));

		mIndicatorViews = new ArrayList<ImageView>();
		mIndicatorViews.add((ImageView) findViewById(R.id.three_across_top_1_indicator));
		mIndicatorViews.add((ImageView) findViewById(R.id.three_across_top_2_indicator));
		mIndicatorViews.add((ImageView) findViewById(R.id.three_across_top_3_indicator));

		mNextImageArrow = (ImageView) findViewById(R.id.add_audio_next_image_arrow);
		
		mHelpView = findViewById(R.id.add_audio_help_tips);
		mHelpView.setVisibility(isNeedShowHelpTips() ? View.VISIBLE : View.GONE);
		mHelpAllSetButton = findViewById(R.id.add_audio_help_all_set_button);
		mHelpAllSetButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mHelpView.setVisibility(View.GONE);
			}
		});
		mDontShowHelpCkBox = (CheckBox) findViewById(R.id.add_audio_dont_show_help_again);
		mDontShowHelpCkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				PreferenceManager.getDefaultSharedPreferences(AddAudioActivity.this).edit().putBoolean(SHOW_ADD_AUDIO_HELP, !isChecked).commit();
				mHelpView.setVisibility(View.GONE);
			}
		});
		
		mPreviousImageArrow = (ImageView) findViewById(R.id.add_audio_previous_image_arrow);
		mPreviousImageArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentlySelectedIndex != 0) {
					setActivePreview(mCurrentlySelectedIndex - 1);
				}
			}
		});
		mNextImageArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentlySelectedIndex != mLargePreviewViews.size() - 1) {
					setActivePreview(mCurrentlySelectedIndex + 1);
				}
			}
		});
		mStartRecordingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRecording) {
					stopRecording();
				} else {
					attr=new HashMap<String, String>();
					if(IsEditActivity){
						attr.put(TMS_RECORD_AUDIO, TMSSelectPhotosActivity.DURING_EDIT);
					}else{
					attr.put(TMS_RECORD_AUDIO, TMSSelectPhotosActivity.DURING_CREATE);}
					RSSLocalytics.recordLocalyticsEvents(AddAudioActivity.this, TMS_RECORD, attr);
					startRecording();
				}
				// start audio clicking
				// set image drawable to that of being recording

			}
		});
		mPlaybackRecordingButton = (ImageButton) findViewById(R.id.add_audio_play_circle);
		mPlaybackRecordingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isPlaying) {
					startPlayback();
				} else {
					stopPlayback();
				}
				// start audio clicking
				// set image drawable to that of being recording

			}
		});

		mDoneButton = (View) findViewById(R.id.add_audio_done_button);
		for (int i = 0; i < mSelectedImagesViews.size() && i < mVideoGenParams.mVignettes.size(); i++) {
			final VideoGenParams.Vignette vignette = mVideoGenParams.mVignettes.get(i);
			final SquareImageView view = mSelectedImagesViews.get(i);
			ViewTreeObserver obs = view.getViewTreeObserver();
			obs.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					BaseActivity.removeViewTreeObserverVersionSafe(this, view);
					view.setImageBitmapAndFilePath(vignette.mImagePath);

				}
			});
			mLargePreviewViews.get(i).setImageBitmapAndFilePath(vignette.mImagePath);
		}
		if (DELETE_AUDIO_FILES) {
			for (int i = 0; i < mVideoGenParams.mVignettes.size(); i++) {
				VideoGenParams.Vignette vignette = mVideoGenParams.mVignettes.get(i);
				new File(vignette.mAudioPath).delete();
			}
		}

		for (int i = 0; i < mLargePreviewViews.size(); i++) {
			mLargePreviewViews.get(i).setROI(mVideoGenParams.mVignettes.get(i).mEndBounds);
		}

		initTimerTime();
		onImageClick(mSelectedImagesViews.get(mCurrentlySelectedIndex), mVideoGenParams.mVignettes.get(mCurrentlySelectedIndex).mImagePath);
	}
	
	private void initTimerTime() {
		for (int i = 0; i < mRecordTimerMilliseconds.length; i++) {
			if (new File(mVideoGenParams.mVignettes.get(i).mAudioPath).exists()) {
				mRecordTimerMilliseconds[i] = AddTitleActivity.calculateLength(mVideoGenParams.mVignettes.get(i).mAudioPath);
			}
		}
		
		updateTimerTime();
	}

	@Override
	protected void onResume() {
		super.onResume();
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			Log.v(TAG, "Restoreing image:" + mVideoGenParams.mVignettes.get(i).mImagePath);
			Log.v(TAG, "Restoreing audio:" + mVideoGenParams.mVignettes.get(i).mAudioPath);
			mLargePreviewViews.get(i).setROI(mVideoGenParams.mVignettes.get(i).mEndBounds);
		}
		refreshIndicatorState();
	}

	private void refreshIndicatorState() {
		for (int i = 0; i < mVideoGenParams.mVignettes.size(); i++) {
			// Log.w(TAG, "refreshIndicatorState:" +
			// mVideoGenParams.mVignettes.get(i).mAudioPath);
			// if (isRecording && new
			// File(mVideoGenParams.mVignettes.get(i).mAudioPath).exists(){}
			if (new File(mVideoGenParams.mVignettes.get(i).mAudioPath).exists()) {
				setIndicatorState(i, INDICATOR_STATE_RECORDED);
			} else {
				setIndicatorState(i, INDICATOR_STATE_NOT_RECORDED);
			}
		}

		boolean allComplete = true;
		for (int i = 0; i < mVideoGenParams.mVignettes.size(); i++) {
			if (!new File(mVideoGenParams.mVignettes.get(i).mAudioPath).exists()) {
				allComplete = false;
			}
		}
		if (allComplete) {
			mDoneButton.setEnabled(true);
			mDoneButton.setVisibility(View.VISIBLE);
		} else {
			mDoneButton.setEnabled(false);
			mDoneButton.setVisibility(View.INVISIBLE);
		}
	}
	
	public void onDoneButton(View v) {
		Intent intent = new Intent(this, AddTitleActivity.class);
		Bundle options = new Bundle();
		// for (int i = 0; i < mSelectedImagesViews.size(); i++) {
		// mVideoGenParams.mVignettes.get(i).mImagePath =
		// mSelectedImagesViews.get(i).getFilePath();
		// }
		// options.putStringArrayList(PreviewActivity.PARAM_SELECTED_IMAGES_PATHS,
		// selectedImagesPath);
		// options.putStringArrayList(PreviewActivity.PARAM_AUDIO_PATHS,
		// mVideoGenerationParams.mAudioPaths);
		persistROIs();
		persistImages();
		// options.putParcelableArrayList(PreviewActivity.PARAM_ROIS, rois);
		// options.putParcelable(INSTANCE_STATE_KEY_VIDEO_PARAMATERS,
		// mVideoGenParams);
		options.putString(INSTANCE_STATE_KEY_VIDEO_PARAMATERS, mVideoGenParams.mUUID.toString());
		intent.putExtras(options);
		intent.putExtra("TMS_EDIT", IsEditActivity);
		startActivity(intent);

	}
	private void persistImages() {
		persistFiles(new FilePersisterHelper() {
			@Override
			public String getCurrentPath(int i) {
				Log.e(TAG, "Current:" + mSelectedImagesViews.get(i).getFilePath());
				return mSelectedImagesViews.get(i).getFilePath();
			}

			@Override
			public String getOldPath(Vignette vig) {
				Log.e(TAG, "Old:" + vig.mImagePath);
				return vig.mImagePath;
			}

			@Override
			public void setNewPath(String path, Vignette vig) {
				vig.mImagePath = path;
			}

			@Override
			public String getFilePrefix() {
				return "image";
			}

			@Override
			public boolean compressFilesAsBitmaps() {
				return false;// this would kill their quality. No need since
								// this must be a swap
			}

			@Override
			public boolean areFilesinBitmapCache() {
				return true;
			}
		});
	}

	private void persistROIs() {
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			RectF endROI = mLargePreviewViews.get(i).getROI();
			// TODO There is some sort of thread problem where endROI can become
			// null
			// making the log statement fail.
			// Log.e(TAG, "endROI:" + endROI == null ? "null" :
			// endROI.toShortString());
			mVideoGenParams.mVignettes.get(i).mEndBounds = endROI;
		}
		Log.e(TAG, "ROI persisted");
	}

	private void startRecording() {
		mNextImageArrow.setEnabled(false);
		mPreviousImageArrow.setEnabled(false);
		if (mMediaRecorder == null) {
			mMediaRecorder = new MediaRecorder();
		}
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// mMediaRecorder.set
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		mMediaRecorder.setAudioEncodingBitRate(64000);
		mMediaRecorder.setAudioSamplingRate(44100);
		mMediaRecorder.setMaxDuration(AUDIO_MAX_DURATION);
		mMediaRecorder.setOnInfoListener(new OnInfoListener() {
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
					// Log.e(TAG,"Stopping W:"+what+" Extra:"+extra);
					stopRecording();
				}
				Log.i(TAG, "Media Info W:" + what + " Extra:" + extra);
			}
		});
		mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				// switch (what) {
				// case MediaRecorder.MEDIA_ERROR_SERVER_DIED :
				mMediaRecorder.release();
				mMediaRecorder = null;
				Log.e(TAG, "MediaRecorderError W:" + what + " E:" + extra);

			}
		});
		
		final int TIME_STEP = 100;
		mRecordTimerMilliseconds[mCurrentlySelectedIndex] = -TIME_STEP;
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				mRecordTimerMilliseconds[mCurrentlySelectedIndex] += TIME_STEP;
				mHandler.obtainMessage(1).sendToTarget();
			}
		}, 0, TIME_STEP);

		String filePath = mVideoGenParams.getAssetPath() + "audio-" + mCurrentlySelectedIndex + AddAudioActivity.AUDIO_FILE_EXTENTION;
		Log.i(TAG, "AudioFilePath:" + filePath);
		File f = new File(filePath);
		if (f.exists()) {
			f.delete();
		}
		f.getParentFile().mkdirs();
		try {
			// f.createNewFile();
			// if (f.exists()) {
			// Set to Readable and MODE_WORLD_READABLE
			// f.setReadable(true, false);
			// }
			// FileInputStream fileInputStream = new FileInputStream(f);
			// mMediaRecorder.setOutputFile(fileInputStream.getFD());
			mMediaRecorder.setOutputFile(filePath);
			// fileInputStream.close();
			// f.delete();
			mMediaRecorder.prepare();
			mMediaRecorder.start();
			isRecording = true;
			mStartRecordingButton.setImageResource(R.drawable.tms_recording);
			mStartRecordingButtonLabel.setText(R.string.TMS_add_audio_recording);
			// ((AnimationDrawable)
			// mStartRecordingButton.getDrawable()).start();
			mPlaybackRecordingButton.setVisibility(View.GONE);
			setIndicatorState(mCurrentlySelectedIndex, INDICATOR_STATE_RECORDING);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			updateTimerTime();
		}
	};

	protected void updateTimerTime() {
		int recordTime = mRecordTimerMilliseconds[mCurrentlySelectedIndex];
		if (isRecording) {
			mTimerProgress.setProgress(recordTime);
		}
		
		int seconds = recordTime / 1000 % 60;
		int minutes = recordTime /1000 / 60;
		String timeString = String.format("%02d:%02d", minutes, seconds);
		mTimerView.setText(timeString);
		
		if (isRecording) {
			showRecorderTimer();
			int timeLeft = AUDIO_MAX_DURATION - recordTime;
			timeLeft = timeLeft < 0 ? 0 : timeLeft;
			String timeLeftString = String.format(getString(R.string.TMS_add_audio_time_left), timeLeft % 1000 != 0 ? timeLeft / 1000 + 1 : timeLeft / 1000);
			mTimeLeftView.setText(timeLeftString);
			
		} else {
			hideRecorderTimer();
		}
	}
	
	private void showRecorderTimer() {
		if (mTimeLeftView.getVisibility() != View.VISIBLE) {
			mTimeLeftView.setVisibility(View.VISIBLE);
		}
		
		if (mTimerProgress.getVisibility() != View.VISIBLE) {
			mTimerProgress.setVisibility(View.VISIBLE);
		}
	}
	
	private void hideRecorderTimer() {
		if (mTimeLeftView.getVisibility() != View.INVISIBLE) {
			mTimeLeftView.setVisibility(View.INVISIBLE);
		}
		
		if (mTimerProgress.getVisibility() != View.INVISIBLE) {
			mTimerProgress.setVisibility(View.INVISIBLE);
		}
	}

	private void stopRecording() {
		isRecording = false;
		hideRecorderTimer();
		mStartRecordingButton.setImageResource(R.drawable.tms_record);
		mStartRecordingButtonLabel.setText(R.string.TMS_add_audio_rerecord);
		mPlaybackRecordingButton.setVisibility(View.VISIBLE);
		try {
			mMediaRecorder.stop();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		mMediaRecorder.reset();
		mMediaRecorder.release();
		mMediaRecorder = null;
		mVideoGenParams.mVignettes.get(mCurrentlySelectedIndex).mAudioPath = mVideoGenParams.getAssetPath() + "audio-" + mCurrentlySelectedIndex + AUDIO_FILE_EXTENTION;
		recordTimeHashMap=new HashMap<String, String>();
		int AudioTime=AddTitleActivity.calculateLength(mVideoGenParams.mVignettes.get(mCurrentlySelectedIndex).mAudioPath)/1000;
		if (AudioTime==0) {
			AudioTime=1;
		}
		recordTimeHashMap.put(TMS_AUDIO_TIME, AudioTime+"s");
		Log.d(TMS_AUDIO_TIME, AudioTime+"s");
		RSSLocalytics.recordLocalyticsEvents(AddAudioActivity.this, TMS_AUDIO_TIME, recordTimeHashMap);
		refreshIndicatorState();
		refreshArrowState();
		mTimer.cancel();
	}

	private void startPlayback() {
		mNextImageArrow.setEnabled(false);
		mPreviousImageArrow.setEnabled(false);
		mStartRecordingButton.setEnabled(false);
		mPlaybackRecordingButton.setVisibility(View.GONE);
		isPlaying = true;
		setIndicatorState(mCurrentlySelectedIndex, INDICATOR_STATE_PLAYING_BACK);
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
		}
		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				onStopPlayback();
			}
		});
		mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e(TAG, "MediaPlayerError W:" + what + " E:" + extra);

				mMediaPlayer.reset();
				mMediaPlayer = null;
				return false;
			}
		});
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			FileInputStream fileInputStream = new FileInputStream(mVideoGenParams.mVignettes.get(mCurrentlySelectedIndex).mAudioPath);
			FileDescriptor des = fileInputStream.getFD();
			mMediaPlayer.setDataSource(des);
			fileInputStream.close();
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void stopPlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
		}
		// This will eventually call onStopPlayback when playback has ended.
	}

	protected void onStopPlayback() {
		Log.e(TAG, "On Stop playback called");
		mPlaybackRecordingButton.setVisibility(View.VISIBLE);
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		refreshArrowState();
		setIndicatorState(mCurrentlySelectedIndex, INDICATOR_STATE_RECORDED);
		isPlaying = false;
		mStartRecordingButton.setEnabled(true);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ArrayList<Parcelable> rois = new ArrayList<Parcelable>();
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			rois.add(mLargePreviewViews.get(i).getScreenBasedRectF());
			// Warning this is not the 0 to 1 based ROI.
		}
		outState.putParcelableArrayList(PARAM_ROIS, rois);
		outState.putInt(PARAM_SELECTED_INDEX, mCurrentlySelectedIndex);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		ArrayList<Parcelable> rois = savedInstanceState.getParcelableArrayList(PARAM_ROIS);
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			mLargePreviewViews.get(i).setScreenBasedRectF((RectF) rois.get(i));
			// Warning this is not the 0 to 1 based ROI.
		}
		int index = savedInstanceState.getInt(PARAM_SELECTED_INDEX);
		onImageClick(mSelectedImagesViews.get(index), mSelectedImagesViews.get(index).getFilePath());
		super.onRestoreInstanceState(savedInstanceState);
	}

	private static final int INDICATOR_STATE_RECORDING = 0;
	private static final int INDICATOR_STATE_RECORDED = 1;
	private static final int INDICATOR_STATE_NOT_RECORDED = 2;
	private static final int INDICATOR_STATE_PLAYING_BACK = 3;

	private void setIndicatorState(int index, int state) {
		ImageView view = mIndicatorViews.get(index);
		int id = 0;
		if (state == INDICATOR_STATE_NOT_RECORDED) {
			id = R.drawable.tms_audio_gray;
		} else if (state == INDICATOR_STATE_RECORDED) {
			id = R.drawable.tms_audio_green;
		} else if (state == INDICATOR_STATE_RECORDING) {
			id = R.drawable.tms_audio_red;
		} else if (state == INDICATOR_STATE_PLAYING_BACK) {
			id = R.drawable.tms_audio_green;
		}
		view.setImageResource(id);

	}

	@Override
	protected void onPause() {
		if (isPlaying) {
			// stopPlayback();
			onStopPlayback();
		}
		if (isRecording) {
			stopRecording();
		}
		persistROIs();
		super.onPause();
	}

	@Override
	public void onImageClick(SquareImageView view, String filePath) {
		// You can't change images while recording or playing back, it causes
		// problems knowing what is playing or recording
		if (!isRecording && !isPlaying) {
			int index = mSelectedImagesViews.indexOf(view);
			// being -1 means we didn't click on once of the top three images
			if (index != -1) {
				setActivePreview(index);
			}
		}
	}

	private void setActivePreview(int index) {
		mBackgroundImageViews.get(mCurrentlySelectedIndex).setEnabled(false);
		mBackgroundImageViews.get(index).setEnabled(true);
		mCurrentlySelectedIndex = index;
		mLargePreviewViews.get(index).setVisibility(View.VISIBLE);
		for (int i = 0; i < mLargePreviewViews.size(); i++) {
			if (i != index) {
				View view = mLargePreviewViews.get(i);
				View v = mBackgroundImageViews.get(i);
				v.setEnabled(false);
				view.setVisibility(View.INVISIBLE);
			}
		}
		updateTimerTime();
		updatePlaybackRecordingButtonAndRecordLabel();
		refreshArrowState();

	}

	private void refreshArrowState() {
		if (mCurrentlySelectedIndex == 0) {
			mPreviousImageArrow.setEnabled(false);
		} else {
			mPreviousImageArrow.setEnabled(true);
		}
		if (mCurrentlySelectedIndex == mLargePreviewViews.size() - 1) {
			mNextImageArrow.setEnabled(false);
		} else {
			mNextImageArrow.setEnabled(true);
		}
	}
	
	private boolean isNeedShowHelpTips() {
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SHOW_ADD_AUDIO_HELP, true);
	}
	
	@Override
	public ViewGroup getRootView() {
		return mRootView;
	}

	@Override
	public int getShadowWidth() {
		return mSelectedImagesViews.get(1).getWidth();
	}

	@Override
	public int getShadowHeight() {
		return mSelectedImagesViews.get(1).getHeight();
	}

	@Override
	public void onImageDrop(SquareImageView dropSource, SquareImageView dropedOn, boolean isTargetDropable, boolean wasSwap) {
		// Warning, these have already had their filePaths changed.
		if (isTargetDropable) {
			Log.e(TAG, "Got drop");
			int dropOnIndex = -1;
			int dropSourceIndex = -1;
			for (int i = 0; i < mSelectedImagesViews.size(); i++) {
				SquareImageView view = mSelectedImagesViews.get(i);
				if (view == dropedOn) {
					dropOnIndex = i;
					// Log.e(TAG, "Match dropOn" + i);
				}
				if (view == dropSource) {
					dropSourceIndex = i;
					// Log.e(TAG, "Match dropSource" + i);
				}
			}
			
			ROIImageView dropSourcePreview = mLargePreviewViews.get(dropSourceIndex);
			ROIImageView dropOnPreview = mLargePreviewViews.get(dropOnIndex);
			mLargePreviewViews.set(dropSourceIndex, dropOnPreview);
			mLargePreviewViews.set(dropOnIndex, dropSourcePreview);
			int visOn = dropOnPreview.getVisibility();
			dropOnPreview.setVisibility(dropSourcePreview.getVisibility());
			dropSourcePreview.setVisibility(visOn);

			VideoGenParams.Vignette vigSource = mVideoGenParams.mVignettes.get(dropSourceIndex);
			VideoGenParams.Vignette vigOn = mVideoGenParams.mVignettes.get(dropOnIndex);
			mVideoGenParams.mVignettes.set(dropSourceIndex, vigOn);
			mVideoGenParams.mVignettes.set(dropOnIndex, vigSource);
			vigOn.mIndex = dropSourceIndex;
			vigSource.mIndex = dropOnIndex;
			updatePlaybackRecordingButtonAndRecordLabel();
			swapFiles(vigSource, vigOn);

			// String tempImage = vigOn.mImagePath;
			// vigOn.mImagePath = vigSource.mImagePath;
			// vigSource.mImagePath = tempImage;
			refreshIndicatorState();
			initTimerTime();
		} else {
			// we shouldn't do anything if they drag the image off
			// At this point they can't remove it
			// Swap is ok however.
		}
	}

	private void updatePlaybackRecordingButtonAndRecordLabel() {
		if (new File(mVideoGenParams.mVignettes.get(mCurrentlySelectedIndex).mAudioPath).exists()) {
			mPlaybackRecordingButton.setVisibility(View.VISIBLE);
			mStartRecordingButtonLabel.setText(R.string.TMS_add_audio_rerecord);
		} else {
			mPlaybackRecordingButton.setVisibility(View.GONE);
			mStartRecordingButtonLabel.setText(R.string.TMS_add_audio_record);
		}
	}

	@Override
	public boolean areViewsDragable() {
		return !isPlaying && !isRecording;
	}

	@Override
	public void onImageDoubleClick(SquareImageView squareImageView) {
		// TODO Auto-generated method stub
		
	}
}
