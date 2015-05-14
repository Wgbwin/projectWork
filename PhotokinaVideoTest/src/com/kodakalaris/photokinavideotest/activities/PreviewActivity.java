package com.kodakalaris.photokinavideotest.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.kodakalaris.photokinavideotest.MediaStoreUtils;
import com.kodakalaris.photokinavideotest.R;
import com.kodakalaris.photokinavideotest.adapters.ResolveInfoAdapter;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParamsUploader;
import com.kodakalaris.photokinavideotest.video_gen.VideoGenIntentService;
import com.kodakalaris.photokinavideotest.video_gen.VideoGenResultReceiver;
import com.kodakalaris.photokinavideotest.views.AnimatedVideoView;

public class PreviewActivity extends BaseActivity implements VideoGenResultReceiver.Receiver {

	protected static final String TAG = PreviewActivity.class.getSimpleName();
	protected boolean mIsPlaying = false;
	private MediaPlayer mMediaPlayer;
	private View mPlayCircle;
	private TextView mTextTitle;
	private View mFocusClearingView;
	private TextView mTextLocation;
	protected boolean mHasAlreadyRequestedPermissions = false;
	private AnimatedVideoView mAnimatedVideoView;
	private TextView mTextDateTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mTextTitle = (TextView) findViewById(R.id.preview_text_title);
		mTextDateTime = (TextView) findViewById(R.id.preview_text_date);
		mTextLocation = (TextView) findViewById(R.id.preview_text_location);
		mFocusClearingView = findViewById(R.id.preview_dummy_focus);
		mPlayCircle = (ImageView) findViewById(R.id.preview_play_circle);
		mAnimatedVideoView = (AnimatedVideoView) findViewById(R.id.preview_annimatedVideo);
	}
	private void initAnimatedVideo() {
		for (int i = 0; i < 3; i++) {
			VideoGenParams.Vignette vig = mVideoGenParams.mVignettes.get(i);
			vig.mStartBounds = calculateStartBounds(vig.mImagePath, i);
			Log.e(TAG, "Start bounds calculated:" + vig.mStartBounds.toShortString());
			vig.mLength = calculateLength(vig.mAudioPath);
		}
		mAnimatedVideoView.initAnimation(mVideoGenParams);
	}
	private void startAnimating() {
		// mViews.get(0).setVisibility(View.VISIBLE);
		// mViews.get(0).startZoom();
		mAnimatedVideoView.startVideoAnimation();
		if ("".equals("")) {
			return;
		}
		final Handler h = new Handler();
		playAudioFile(mVideoGenParams.mVignettes.get(0).mAudioPath);
		// Log.e(TAG,"Start Zoom 0");
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				h.removeCallbacks(this);
				Log.e(TAG, "Start Fade into 1");
				h.postDelayed(new Runnable() {

					@Override
					public void run() {
						Log.e(TAG, "End Fade into 1");
						h.removeCallbacks(this);
						playAudioFile(mVideoGenParams.mVignettes.get(1).mAudioPath);
					}
				}, AnimatedVideoView.FADE_LENGTH_MS);

			}
		}, mVideoGenParams.mVignettes.get(0).mLength);
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				h.removeCallbacks(this);
				Log.e(TAG, "Start Fade into 2");
				h.postDelayed(new Runnable() {

					@Override
					public void run() {
						Log.e(TAG, "End Fade into 2");
						h.removeCallbacks(this);
						playAudioFile(mVideoGenParams.mVignettes.get(2).mAudioPath);
					}
				}, AnimatedVideoView.FADE_LENGTH_MS);

			}
		}, mVideoGenParams.mVignettes.get(0).mLength + mVideoGenParams.mVignettes.get(1).mLength + AnimatedVideoView.FADE_LENGTH_MS);
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				Log.e(TAG, "End Animation");
				onStartButtonClicked(null);
			}
		}, mVideoGenParams.mVignettes.get(0).mLength + AnimatedVideoView.FADE_LENGTH_MS * 2 + mVideoGenParams.mVignettes.get(1).mLength + mVideoGenParams.mVignettes.get(2).mLength);

	}

	protected void playAudioFile(String filePath) {
		Log.e(TAG, "Playing audio file preview:" + filePath);
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			FileInputStream fileInputStream = new FileInputStream(filePath);
			mMediaPlayer.setDataSource(fileInputStream.getFD());
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
	private static RectF calculateStartBounds(String imagePath, int index) {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, o);
		// Matrix m = MediaStoreUtils.getMatrix(imagePath);
		ExifInterface exif;
		int orientation;
		try {
			exif = new ExifInterface(imagePath);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
		} catch (IOException e) {
			orientation = ExifInterface.ORIENTATION_UNDEFINED;
			e.printStackTrace();
		}
		int imageWidth;
		int imageHeight;
		switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90 :
			case ExifInterface.ORIENTATION_ROTATE_270 :
				imageWidth = o.outHeight;
				imageHeight = o.outWidth;
				break;
			default :
				imageWidth = o.outWidth;
				imageHeight = o.outHeight;
				break;
		}

		Log.i(TAG, "ImageSize W:" + imageWidth + " H:" + imageHeight);
		float min = Math.min(imageWidth, imageHeight);
		//float max = Math.max(imageWidth, imageHeight);
		float tempX;
		float tempY;
		if (imageWidth > imageHeight) {
			// If landscape, start bounds are 50 50
			tempX = (imageWidth - min) / imageWidth * 0.5f;
			tempY = (imageHeight - min) / imageHeight * 0.5f;
		} else {
			// if portrait, bounds are 20 80
			tempX = (imageWidth - min) / imageWidth * 0.2f;
			tempY = (imageHeight - min) / imageHeight * 0.2f;
		}

		float tempW = min / imageWidth;
		float tempH = min / imageHeight;
		// x,y,w,h

		RectF startBounds = new RectF(tempX, tempY, tempX + tempW, tempY + tempH);
		return startBounds;
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
		//String seconds = String.valueOf((dur % 60000) / 1000);

		Log.v(TAG, "Seconds:" + dur / 1000.0f);
		//String minutes = String.valueOf(dur / 60000);
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
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		Log.e(TAG, "onPausePreview");
		((VideoGenResultReceiver) mVideoGenParams.mReviever).setReceiver(null);
		mVideoGenParams.mReviever = null;
		mAnimatedVideoView.stopAnimation();
		super.onPause();
	}
	private void saveUIProjectChanges() {
		mVideoGenParams.mProjectTitle = mTextTitle.getText().toString();
		mVideoGenParams.mProjectSubTitleLocation = mTextLocation.getText().toString();
		mVideoGenParams.mProjectSubTitleTimeDate = mTextDateTime.getText().toString();
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume PreviewActivity");
		super.onResume();
		mVideoGenParams.mReviever = new VideoGenResultReceiver(new Handler());
		((VideoGenResultReceiver) mVideoGenParams.mReviever).setReceiver(this);
		mTextTitle.setText(mVideoGenParams.mProjectTitle);
		// TODO Empty string isn't the right way to check if we should do this.
		// If a user deletes the text it should stay deleted. This means
		// we should pre-populate it only when we know it hasn't
		// been pre-populated before
		if (mVideoGenParams.mProjectSubTitleTimeDate == "") {
			mVideoGenParams.mProjectSubTitleTimeDate = MediaStoreUtils.getExifTimeDate(mVideoGenParams.mVignettes.get(0).mImagePath);
		}
		if (mVideoGenParams.mProjectSubTitleLocation == "") {
			mVideoGenParams.mProjectSubTitleLocation = MediaStoreUtils.getExifLatLong(this, mVideoGenParams.mVignettes.get(0).mImagePath);
		}

		Log.i(TAG, "Location:" + mVideoGenParams.mProjectSubTitleLocation);
		mTextDateTime.setText(mVideoGenParams.mProjectSubTitleTimeDate);
		mTextLocation.setText(mVideoGenParams.mProjectSubTitleLocation);
		mAnimatedVideoView.removeAllViews();
		initAnimatedVideo();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.e(TAG, "onReceiveResultPreview");
		// Warning, this could be from video upload or video generation
		if (resultCode == VideoGenIntentService.RESULT_SUCCESS) {
			// initJavaScript(new VideoGenerationParamater(mSelectedImagesPaths,
			// mAudioPaths, mReciever));
			// mVideoPath =
			// resultData.getString(VideoGenerationService.RESULT_VIDEO_PATH);
		} else {
			Log.e(TAG, "Video Generation responded with failure");
		}
	}

	public void onStartButtonClicked(View v) {
		// Log.i(TAG, "mIsPlaying is:" + mIsPlaying);
		if (!mIsPlaying) {
			Log.w(TAG, "Starting");
			mPlayCircle.setVisibility(View.GONE);
			mIsPlaying = true;
			startAnimating();

		} else {
			Log.w(TAG, "Stopping");
			mIsPlaying = false;
			mPlayCircle.setVisibility(View.VISIBLE);
			mMediaPlayer.stop();

		}
	}

	public void onShareButtonClick(View v) {
		Log.e(TAG, "Start 0:" + mVideoGenParams.mVignettes.get(0).mStartBounds.toShortString());
		saveUIProjectChanges();
		mVideoGenParams.persistToFileSystem(this);
		final Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setAction(Intent.ACTION_SEND);
		sharingIntent.setType("video/mp4");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mTextTitle.getText());
		String path = "";
		sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));

		// sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
		final List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(sharingIntent, PackageManager.MATCH_DEFAULT_ONLY);
		final List<ResolveInfo> resolveInfoSubset = new ArrayList<ResolveInfo>();
		for (ResolveInfo item : resolveInfo) {
			String packageName = item.activityInfo.applicationInfo.packageName;
			// Log.e(TAG, "packageName:" + packageName);
			if (packageName.equals("com.facebook.katana") || (packageName.equals("com.android.mms"))) {
				resolveInfoSubset.add(item);
			}
		}
		resolveInfoSubset.add(null);
		AlertDialog.Builder customDialog = new AlertDialog.Builder(this);
		// customDialog.setTitle(getString(R.string.activity_preview_share_dialog_title));
		LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// customDialog.setCustomTitle(layoutInflater.inflate(R.layout.activity_preview_share_title,
		// null));
		customDialog.setTitle(R.string.activity_preview_share_dialog_title);
		View view = layoutInflater.inflate(R.layout.activity_preview_share_dialog, null);
		final ListView list = (ListView) view.findViewById(R.id.preview_dialog_list_view);
		customDialog.setView(view);
		final AlertDialog d = customDialog.show();
		if (resolveInfoSubset.size() == 1) {
			Log.e(TAG, "Facebook or other special ones not on device");
			list.setAdapter(new ResolveInfoAdapter(PreviewActivity.this, resolveInfo));
			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					sharingIntent.setClassName(resolveInfo.get(arg2).activityInfo.applicationInfo.packageName, resolveInfo.get(arg2).activityInfo.name);
					startActivity(sharingIntent);
				}
			});
		} else {
			list.setAdapter(new ResolveInfoAdapter(this, resolveInfoSubset));
			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
					if (index != resolveInfoSubset.size() - 1) {
						d.cancel();
						Log.e(TAG, "KeyHash:" + getKeyHash());
						getFacebookUserID(new PreviewActivity.GetFacebookUserCallback() {
							@Override
							public void onCompleted(String userID, String accessToken) {
								Log.e(TAG, "UserID1:" + userID + " AccessToken:" + accessToken);
								startFaceBookUpload(userID, accessToken);
							}
						});
					} else {
						list.setAdapter(new ResolveInfoAdapter(PreviewActivity.this, resolveInfo));
						list.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
								sharingIntent.setClassName(resolveInfo.get(index).activityInfo.applicationInfo.packageName, resolveInfo.get(index).activityInfo.name);
								startActivity(sharingIntent);
							}
						});
					}

				}
			});
		}
		int textViewId = d.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
		TextView tv = (TextView) d.findViewById(textViewId);
		tv.setTextColor(Color.BLACK);
	}
	private void startFaceBookUpload(String userID, String accessToken) {
		// Starting
		Intent intent = new Intent(PreviewActivity.this, VideoGenParamsUploader.class);
		intent.putExtra(VideoGenParamsUploader.PARAM_VIDEO_PARAMS, mVideoGenParams);
		intent.putExtra(VideoGenParamsUploader.PARAM_FACEBOOK_USERID, userID);
		intent.putExtra(VideoGenParamsUploader.PARAM_FACEBOOK_ACCESS_TOKEN, accessToken);
		this.startService(intent);
	}
	public void focusOnSomethingElse(TextView v) {
		mFocusClearingView.requestFocus();
		v.clearFocus();

	}
	private interface GetFacebookUserCallback {
		void onCompleted(String userID, String accessToken);
	}
	private void getFacebookUserID(final GetFacebookUserCallback callback) {
		mHasAlreadyRequestedPermissions = false;
		Session.openActiveSession(this, true, new Session.StatusCallback() {
			// callback when session changes state
			@Override
			public void call(final Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {
					if (session.isPermissionGranted("publish_actions")) {
						Request.newMeRequest(session, new Request.GraphUserCallback() {
							// callback after Graph API response with user
							// object
							@Override
							public void onCompleted(GraphUser user, Response response) {
								if (user != null) {
									// Log.e(TAG, "Facebook UserName:" +
									// user.getName());
									callback.onCompleted(user.getId(), session.getAccessToken());
								} else {
									Toast.makeText(PreviewActivity.this, "Facebook Error", Toast.LENGTH_SHORT).show();
								}
							}
						}).executeAsync();
					} else {
						// Toast.LENGTH_LONG).show();
						if (!mHasAlreadyRequestedPermissions) {
							mHasAlreadyRequestedPermissions = true;
							Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(PreviewActivity.this, Arrays.asList("publish_actions"));
							session.requestNewPublishPermissions(newPermissionsRequest);
						}

					}
				}

			}
		});
	}
	private String getKeyHash() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				return Base64.encodeToString(md.digest(), Base64.DEFAULT);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	public void onShouldStartAudio(int index) {
		playAudioFile(mVideoGenParams.mVignettes.get(index).mAudioPath);

	}
	public void onVideoAnimationEnded() {
		onStartButtonClicked(null);

	}
}
