package com.kodakalaris.photokinavideotest.activities;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kodakalaris.photokinavideotest.R;
import com.kodakalaris.photokinavideotest.SquareImageViewClickListener;
import com.kodakalaris.photokinavideotest.activities.SelectPhotosActivity.FilePersisterHelper;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams.Vignette;
import com.kodakalaris.photokinavideotest.views.ROIImageView;
import com.kodakalaris.photokinavideotest.views.SquareImageView;

public class AddAudioActivity extends BaseActivity implements SquareImageViewClickListener {

	private static final String TAG = AddAudioActivity.class.getSimpleName();
	private static final String PARAM_SELECTED_INDEX = "PARAM_SELECTED_INDEX";
	private static final String PARAM_ROIS = "PARAM_ROIS";
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
	private int mRecordTimerSeconds;
	private TextView mTimerView;
	private Timer mTimer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_audio);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

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
		mPlaybackRecordingButton = (ImageView) findViewById(R.id.add_audio_playback_recording);
		mTimerView = (TextView) findViewById(R.id.activity_add_audio_timer_time);

		mRootView = (ViewGroup) findViewById(R.id.add_audio_root_container);
		mBackgroundImageViews = new ArrayList<View>();
		mBackgroundImageViews.add(findViewById(R.id.three_across_top_1_background));
		mBackgroundImageViews.add(findViewById(R.id.three_across_top_2_background));
		mBackgroundImageViews.add(findViewById(R.id.three_across_top_3_background));

		mIndicatorViews = new ArrayList<ImageView>();
		mIndicatorViews.add((ImageView) findViewById(R.id.three_across_top_1_indicator));
		mIndicatorViews.add((ImageView) findViewById(R.id.three_across_top_2_indicator));
		mIndicatorViews.add((ImageView) findViewById(R.id.three_across_top_3_indicator));

		mStartRecordingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRecording) {
					stopRecording();
				} else {
					startRecording();
				}
				// start audio clicking
				// set image drawable to that of being recording

			}
		});
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
			// TODO mEnd bounds might not be the right view, since the 2nd view
			// zooms out
			mLargePreviewViews.get(i).setROI(mVideoGenParams.mVignettes.get(i).mEndBounds);
		}
		updateTimerTime();
		onImageClick(mSelectedImagesViews.get(mCurrentlySelectedIndex), mVideoGenParams.mVignettes.get(mCurrentlySelectedIndex).mImagePath);
	}
	@Override
	protected void onResume() {
		super.onResume();
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			Log.e(TAG, "Restoreing image:" + mVideoGenParams.mVignettes.get(i).mImagePath);
			Log.e(TAG, "Restoreing audio:" + mVideoGenParams.mVignettes.get(i).mAudioPath);
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
		Intent intent = new Intent(this, PreviewActivity.class);
		Bundle options = new Bundle();
		// for (int i = 0; i < mSelectedImagesViews.size(); i++) {
		// mVideoGenParams.mVignettes.get(i).mImagePath =
		// mSelectedImagesViews.get(i).getFilePath();
		// }
		for (int i = 0; i < mVideoGenParams.mVignettes.size(); i++) {
			File f = new File(mVideoGenParams.mVignettes.get(i).mAudioPath);
			if (!f.exists()) {
				Toast.makeText(this, R.string.activity_add_audio_record_3_audio_tracks_warning, Toast.LENGTH_LONG).show();
				return;
			}
		}
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
		if (mMediaRecorder == null) {
			mMediaRecorder = new MediaRecorder();
		}
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// mMediaRecorder.set
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		mMediaRecorder.setAudioEncodingBitRate(64000);
		mMediaRecorder.setAudioSamplingRate(44100);
		mMediaRecorder.setMaxDuration(15000);
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
		mRecordTimerSeconds = -1;
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				mRecordTimerSeconds += 1;
				mHandler.obtainMessage(1).sendToTarget();
			}
		}, 0, 1000);

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
			mStartRecordingButton.setImageResource(R.drawable.recording_audio_animation_list);
			((AnimationDrawable) mStartRecordingButton.getDrawable()).start();
			mPlaybackRecordingButton.setEnabled(false);
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
		int seconds = mRecordTimerSeconds % 60;
		int minutes = mRecordTimerSeconds / 60;
		String timeString = String.format("%02d:%02d", minutes, seconds);
		mTimerView.setText(timeString);

	}
	private void stopRecording() {
		isRecording = false;
		mStartRecordingButton.setImageResource(R.drawable.recordaudio);
		mPlaybackRecordingButton.setEnabled(true);
		try {
			mMediaRecorder.stop();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		mMediaRecorder.reset();
		mMediaRecorder.release();
		mMediaRecorder = null;
		mVideoGenParams.mVignettes.get(mCurrentlySelectedIndex).mAudioPath = mVideoGenParams.getAssetPath() + "audio-" + mCurrentlySelectedIndex + AUDIO_FILE_EXTENTION;
		refreshIndicatorState();
		mTimer.cancel();
	}
	private void startPlayback() {

		mPlaybackRecordingButton.setEnabled(false);
		mStartRecordingButton.setEnabled(false);
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
		Log.e(TAG,"On Stop playback called");
		mPlaybackRecordingButton.setEnabled(true);
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
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
			id = R.drawable.status_color_gray;
		} else if (state == INDICATOR_STATE_RECORDED) {
			id = R.drawable.status_color_green;
		} else if (state == INDICATOR_STATE_RECORDING) {
			id = R.drawable.status_color_red;
		} else if (state == INDICATOR_STATE_PLAYING_BACK) {
			id = R.drawable.status_color_green;
		}
		view.setImageResource(id);

	}

	@Override
	protected void onPause() {
		if (isPlaying) {
			//stopPlayback();
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
				updatePlaybackRecordingButton();

			}
		}
	}

	private void setActivePreview(int index) {
		mBackgroundImageViews.get(mCurrentlySelectedIndex).setEnabled(false);
		mBackgroundImageViews.get(index).setEnabled(true);
		mCurrentlySelectedIndex = index;
		mLargePreviewViews.get(index).setVisibility(View.VISIBLE);
		for (int i = 0; i < mLargePreviewViews.size(); i++) {
			ROIImageView view = mLargePreviewViews.get(i);
			View v = mBackgroundImageViews.get(i);
			if (i != index) {
				v.setEnabled(false);
				view.setVisibility(View.INVISIBLE);
			}
		}

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
			updatePlaybackRecordingButton();
			swapFiles(vigSource, vigOn);

			// String tempImage = vigOn.mImagePath;
			// vigOn.mImagePath = vigSource.mImagePath;
			// vigSource.mImagePath = tempImage;
			refreshIndicatorState();

		} else {
			// we shouldn't do anything if they drag the image off
			// At this point they can't remove it
			// Swap is ok however.
		}
	}
	private void updatePlaybackRecordingButton() {
		if (new File(mVideoGenParams.mVignettes.get(mCurrentlySelectedIndex).mAudioPath).exists()) {
			mPlaybackRecordingButton.setEnabled(true);
		} else {
			mPlaybackRecordingButton.setEnabled(false);
		}
	}
	@Override
	public boolean areViewsDragable() {
		return !isPlaying && !isRecording;
	}
}
