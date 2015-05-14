package com.kodakalaris.video.activities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.utils.RSSLocalytics;
import com.kodakalaris.video.MediaStoreUtils;
import com.kodakalaris.video.VideoAnimationProperty;
import com.kodakalaris.video.storydoc_format.VideoGenParams;
import com.kodakalaris.video.storydoc_format.VideoGenParamsUploader;
import com.kodakalaris.video.video_gen.VideoGenIntentService;
import com.kodakalaris.video.video_gen.VideoGenResultReceiver;
import com.kodakalaris.video.views.AnimatedVideoDialog;
import com.kodakalaris.video.views.AnimatedVideoImage;
import com.kodakalaris.video.views.AnimatedVideoView;
import com.kodakalaris.video.views.AnimatedVideoView.AnimatedVideoViewHoldingActivity;
import com.kodakalaris.video.views.CustomProgressView;

public class PreviewActivity extends BaseActivity implements AnimatedVideoViewHoldingActivity {

	protected static final String TAG = PreviewActivity.class.getSimpleName();
	protected boolean mIsPlaying = false;
	private View mPlayCircle;
	private TextView mTextTitle;
	private TextView mTextLocation;
	protected boolean mHasAlreadyRequestedPermissions = false;
	private AnimatedVideoView mAnimatedVideoView;
	private TextView mTextDateTime;
	private AnimatedVideoImage mVideoThumbnail;
	private TextView headBarText;
	private View shareStatusContainer;
	private TextView tvShareStatus;
	private VideoGenResultReceiver mUploadBroadcastReciever;
	private FrameLayout mConformation;
	public static final String TMS_CREATE_PREVIEW = "TMS Create - Preview";
	public static final String TMS_SHARE = "TMS Share";
	public static HashMap<String, String> attr ;
	public static final String TMS_EDIT_PREVIEW="TMS Edit - Preview";
	private boolean IsEditActivity=false;
	private CustomProgressView vCustomProgressView ;
	private Timer mTimer ;
	private CustomHandler mHandler ;
//	private ProgressBar pbWaiting;
	private Button btOK;
	private int uploadType = -1;
	private Dialog mVideoDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_preview);
		setContentLayout(R.layout.activity_preview);
		Intent intent=getIntent();
		IsEditActivity=intent.getBooleanExtra("TMS_EDIT", false);
		if(IsEditActivity){
			RSSLocalytics.recordLocalyticsPageView(this, TMS_EDIT_PREVIEW);
		}else{
			RSSLocalytics.recordLocalyticsPageView(this, TMS_CREATE_PREVIEW);}
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mTextTitle = (TextView) findViewById(R.id.preview_text_title);
		mTextDateTime = (TextView) findViewById(R.id.preview_text_date);
		mTextLocation = (TextView) findViewById(R.id.preview_text_location);
		mPlayCircle = (ImageView) findViewById(R.id.preview_play_circle);
		mVideoThumbnail = (AnimatedVideoImage) findViewById(R.id.preview_video_thumbnail);
		mAnimatedVideoView = (AnimatedVideoView) findViewById(R.id.preview_annimatedVideo);
		headBarText = (TextView) findViewById(R.id.headerBar_tex);
		headBarText.setText(getString(R.string.TMS_preview_title));
		shareStatusContainer = findViewById(R.id.flShareProgress);
		vCustomProgressView = (CustomProgressView) findViewById(R.id.custom_progressbar_view) ;
//		pbWaiting = (ProgressBar) findViewById(R.id.pbSharing);
		tvShareStatus = (TextView) findViewById(R.id.tvShareStatus);
		btOK = (Button) findViewById(R.id.btOK);
		findViewById(R.id.preview_share_holder).setVisibility(View.VISIBLE);
		mConformation = (FrameLayout) findViewById(R.id.flConformation);
		if (AddTitleActivity.ISFromAddTitle) {
			mConformation.setVisibility(View.VISIBLE);
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mConformation.setVisibility(View.GONE);
					AddTitleActivity.ISFromAddTitle = false;
				}
			}, 2000);
		}
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
		IntentFilter filter = new IntentFilter(VideoGenResultReceiver.UPLOAD_COMPLETE_RESPONCE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		mUploadBroadcastReciever = new VideoGenResultReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				//Warning, this can happen before the activity is laied out, before its
				//mVideoGenParams are set or after it has persisted itself to the file system
				//Basically dont edit the project format here.
				int resultCode = intent.getExtras().getInt(VideoGenResultReceiver.RESULT_CODE);
				super.onReceive(context, intent);
				// Warning, this could be from video upload or video generation
				if (resultCode == VideoGenParamsUploader.SHARE_UPLOADING) {
					shareStatusContainer.setVisibility(View.VISIBLE);
					tvShareStatus.setVisibility(View.VISIBLE);
					vCustomProgressView.showProgressBar(View.VISIBLE) ;
                    //TODO
				//	pbWaiting.setVisibility(View.VISIBLE);
					btOK.setVisibility(View.INVISIBLE);
					tvShareStatus.setText(R.string.TMS_preview_progress_uploading);
				} 
				else if (resultCode == VideoGenParamsUploader.SHARE_RENDERING) {
					//TODO  start rending
					if(uploadType == VideoGenParamsUploader.PARAM_UPLOAD_TYPE_HD){
						if(mTimer==null){
							mTimer = new Timer() ;
					       
							TimerTask timeTask  = new TimerTask() {
								
								@Override
								public void run() {
									if(mHandler==null) {
										mHandler = new CustomHandler(getMainLooper()) ;
									}
									
									Message msg  = mHandler.obtainMessage() ;
									msg.what = CustomHandler.START_RENDING ;
									msg.sendToTarget() ;
									
								}
							};
							mTimer.scheduleAtFixedRate(timeTask, 1000, 1000) ;
							
						}
					}
					
					tvShareStatus.setText(R.string.TMS_preview_progress_rendering);
				} 
				else if (resultCode == VideoGenParamsUploader.SHARE_SAVING) {
					//TODO  end rending start saving
					if(uploadType==VideoGenParamsUploader.PARAM_UPLOAD_TYPE_HD){
						if(mHandler==null) {
							mHandler = new CustomHandler(getMainLooper()) ;
						}
						Message msg  = mHandler.obtainMessage() ;
						msg.what = CustomHandler.END_RENDING ;
						msg.sendToTarget() ;
					}
					
					
					tvShareStatus.setText(R.string.TMS_preview_progress_saving);
				} 
				else if (resultCode == VideoGenIntentService.RESULT_SUCCESS) {
					vCustomProgressView.showProgressBar(View.INVISIBLE) ;
					//TODO
//					pbWaiting.setVisibility(View.INVISIBLE);
					if(uploadType == VideoGenParamsUploader.PARAM_UPLOAD_TYPE_FACEBOOK){
						tvShareStatus.setText(String.format(getString(R.string.TMS_share_success_social), getString(R.string.TMS_preview_share_facebook)));
						btOK.setVisibility(View.VISIBLE);
					} else if(uploadType == VideoGenParamsUploader.PARAM_UPLOAD_TYPE_HD){
						tvShareStatus.setText(String.format(getString(R.string.TMS_share_success_local), getString(R.string.TMS_preview_share_gallery)));
						btOK.setVisibility(View.VISIBLE);
					} else {
						shareStatusContainer.setVisibility(View.GONE);
					}
				} 
				else {
					onSharingError();
					if(mTimer!=null){
						mTimer.cancel() ;
						mTimer = null ;
						
						vCustomProgressView.finishCircleProgressBar() ;
						if(mHandler!=null){
							mHandler.setCount(0) ;
						}
						
					}
					
					
					
				}
			}
		};
		registerReceiver(mUploadBroadcastReciever, filter);
	}

	
	
	private void initAnimatedVideo() {
		for (int i = 0; i < 3; i++) {
			VideoGenParams.Vignette vig = mVideoGenParams.mVignettes.get(i);
			vig.mStartBounds = calculateStartBounds(vig.mImagePath);
			// Log.e(TAG, "Start bounds calculated:" +
			// vig.mStartBounds.toShortString());
			vig.mLength = AddTitleActivity.calculateLength(vig.mAudioPath);
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
	
	private void onSharingError(){
		shareStatusContainer.setVisibility(View.VISIBLE);
		tvShareStatus.setText(R.string.problemSendingOrder);
		tvShareStatus.setVisibility(View.VISIBLE);
		findViewById(R.id.btOK).setVisibility(View.VISIBLE);
		findViewById(R.id.pbSharing).setVisibility(View.INVISIBLE);
	}

	private void saveUIProjectChanges() {
		if (mVideoGenParams != null) {
			mVideoGenParams.mProjectTitle = mTextTitle.getText().toString();
			mVideoGenParams.mProjectSubTitleLocation = mTextLocation.getText().toString();
			mVideoGenParams.mProjectSubTitleTimeDate = mTextDateTime.getText().toString();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mTextTitle.setText(mVideoGenParams.mProjectTitle);
		if (mVideoGenParams.mProjectSubTitleTimeDate == null) {
			mVideoGenParams.mProjectSubTitleTimeDate = MediaStoreUtils.getExifTimeDateUserFriendly(mVideoGenParams.mVignettes.get(0).mImagePath);
		}
		if (mVideoGenParams.mProjectSubTitleLocation == null) {
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
		Log.e(TAG, "" + resultCode);
		if(resultCode == VideoGenParamsUploader.PARAM_UPLOAD_TYPE_FACEBOOK){
			saveUIProjectChanges();
			mVideoGenParams.persistToFileSystem(this);
			getFacebookUserID(new PreviewActivity.GetFacebookUserCallback() {
				@Override
				public void onCompleted(String userID, String accessToken) {
					startFaceBookUpload(userID, accessToken);
				}
			});
			uploadType = VideoGenParamsUploader.PARAM_UPLOAD_TYPE_FACEBOOK;
		} else if(resultCode == VideoGenParamsUploader.PARAM_UPLOAD_TYPE_HD){
			saveUIProjectChanges();
			mVideoGenParams.persistToFileSystem(this);
			startUploadForHD();
			uploadType = VideoGenParamsUploader.PARAM_UPLOAD_TYPE_HD;
		} else if(resultCode == VideoGenParamsUploader.PARAM_UPLOAD_TYPE_MMS){
			saveUIProjectChanges();
			mVideoGenParams.persistToFileSystem(this);
			startUploadForMMS();
		} else {
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
	}

	public void onStartButtonClicked(View v) {
		
		if (mVideoDialog == null) {
			mVideoDialog = new AnimatedVideoDialog(this, mVideoGenParams,PreviewActivity.this,R.style.DropDownDialog);
			mVideoDialog.show();
		}
		// Log.i(TAG, "mIsPlaying is:" + mIsPlaying);
		/*if (!mIsPlaying) {
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

		}*/
	}

	public void onShareButton(View v) {
		uploadType = -1;
		new CheckIntentTask().execute();
		attr=new HashMap<String, String>();
		if(IsEditActivity){
		attr.put(TMS_SHARE, TMSSelectPhotosActivity.DURING_CREATE);}
		else {
			attr.put(TMS_SHARE, TMSSelectPhotosActivity.DURING_EDIT);
		}
		RSSLocalytics.recordLocalyticsEvents(PreviewActivity.this, TMS_SHARE, attr);
	}
	
	private void showShareDestinations(){
		Intent intent = new Intent(this, ShareDestinationSelectActivity.class);
		startActivityForResult(intent, 1); // request code is 1 means switch to select Share destination
		shareStatusContainer.setVisibility(View.GONE);
	}
	
	private void startCheckInternet() {
		shareStatusContainer.setVisibility(View.VISIBLE);
		tvShareStatus.setVisibility(View.INVISIBLE);
		//TODO
		vCustomProgressView.showProgressBar(View.VISIBLE) ;
//		pbWaiting.setVisibility(View.VISIBLE);
		btOK.setVisibility(View.INVISIBLE);
	}
	
	private void internetWeak(int errorMsgId){
		shareStatusContainer.setVisibility(View.VISIBLE);
		tvShareStatus.setVisibility(View.VISIBLE);
		tvShareStatus.setText(errorMsgId);
		//TODO
	    vCustomProgressView.showProgressBar(View.INVISIBLE) ;
//		pbWaiting.setVisibility(View.INVISIBLE);
		btOK.setVisibility(View.VISIBLE);
	}
	
	public void onErrorOKClick(View v){
		shareStatusContainer.setVisibility(View.GONE);
	}

	private void startFaceBookUpload(String userID, String accessToken) {
		// Starting
		Intent intent = new Intent(PreviewActivity.this, VideoGenParamsUploader.class);
		intent.putExtra(VideoGenParamsUploader.PARAM_VIDEO_PARAMS, mVideoGenParams);
		intent.putExtra(VideoGenParamsUploader.PARAM_UPLOAD_TYPE, VideoGenParamsUploader.PARAM_UPLOAD_TYPE_FACEBOOK);
		intent.putExtra(VideoGenParamsUploader.PARAM_FACEBOOK_USERID, userID);
		intent.putExtra(VideoGenParamsUploader.PARAM_FACEBOOK_ACCESS_TOKEN, accessToken);
		this.startService(intent);
	}

	private void startUploadForMMS() {
		Intent intent = new Intent(PreviewActivity.this, VideoGenParamsUploader.class);
		intent.putExtra(VideoGenParamsUploader.PARAM_VIDEO_PARAMS, mVideoGenParams);
		intent.putExtra(VideoGenParamsUploader.PARAM_UPLOAD_TYPE, VideoGenParamsUploader.PARAM_UPLOAD_TYPE_MMS);
		this.startService(intent);
	}

	private void startUploadForHD() {
		Intent intent = new Intent(PreviewActivity.this, VideoGenParamsUploader.class);
		intent.putExtra(VideoGenParamsUploader.PARAM_VIDEO_PARAMS, mVideoGenParams);
		intent.putExtra(VideoGenParamsUploader.PARAM_UPLOAD_TYPE, VideoGenParamsUploader.PARAM_UPLOAD_TYPE_HD);
		this.startService(intent);
	}

	private interface GetFacebookUserCallback {
		void onCompleted(String userID, String accessToken);
	}

	private void getFacebookUserID(final GetFacebookUserCallback callback) {
		mHasAlreadyRequestedPermissions = false;
		Session.openActiveSession(this, true, Arrays.asList("public_profile"), new Session.StatusCallback() {
			// callback when session changes state
			@Override
			public void call(final Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {
					if (session.isPermissionGranted("publish_actions")) {
						Request.newMeRequest(session, new Request.GraphUserCallback() {
							// callback after Graph API response with
							// user
							// object
							@Override
							public void onCompleted(GraphUser user, Response response) {
								if (user != null) {
									callback.onCompleted(user.getId(), session.getAccessToken());
								} else {
									onSharingError();
									Log.e(TAG, "Facebook Error, trying again might work:" + response.toString());
								}
							}
						}).executeAsync();
					} else {
						if (!mHasAlreadyRequestedPermissions) {
							mHasAlreadyRequestedPermissions = true;
							Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(PreviewActivity.this, Arrays.asList("publish_actions"));
							session.requestNewPublishPermissions(newPermissionsRequest);
						}

					}
				} else if(state == SessionState.CLOSED_LOGIN_FAILED){
					onSharingError();
				}

			}
		});
	}

	public static String getKeyHash(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
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

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(mUploadBroadcastReciever);
		if(mTimer!=null ){
			mTimer.cancel() ;
			mTimer = null ;
		}
		
		super.onDestroy();
	}
	
	
	  class CustomHandler extends Handler{
		
		public static final int START_RENDING =1 ;
		public static final int END_RENDING =2 ;
		private static final int MAX_SECONDS =120 ;  //120S
		private int count = 0 ;
		
		public CustomHandler(Looper mainLooper) {
			// TODO Auto-generated constructor stub
			 super(mainLooper);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case START_RENDING :
				count++ ;
				int value = (int) ((count% MAX_SECONDS)*100.0 / MAX_SECONDS )  ;
				if(count%2==0){
					vCustomProgressView.updateCircleProgressBar(value) ;
				}
				
				break;
				
			case END_RENDING :
				
				if(mTimer!=null){
					mTimer.cancel() ;
					mTimer = null ;
				}
				count=0 ;
				vCustomProgressView.finishCircleProgressBar() ;
				
				
				break ;

			default:
				break;
			}
			
			super.handleMessage(msg);
		}
		
		public void setCount(int count){
			this.count = count ;
		}
	}
	
	
	class CheckIntentTask extends AsyncTask<Void, Void, Integer> {
		
		private static final int CONNECTED = 0;
		private static final int WIFI_INCONNECTED = 1;
		private static final int INTERNET_INCONNECTED = 2;

		@Override
		protected void onPreExecute() {
			startCheckInternet();
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			if(!Connection.isConnectedInternet(PreviewActivity.this)){
				if(Connection.isConnectedAnyWifi(PreviewActivity.this)){
					return INTERNET_INCONNECTED;
				} else {
					return WIFI_INCONNECTED;
				}
			} else {
				return CONNECTED;
			}	
		}
		
		@Override
		protected void onPostExecute(Integer status) {
			if(status == CONNECTED){
				showShareDestinations();
			} else if(status == WIFI_INCONNECTED){
				internetWeak(R.string.notconnected);
			} else if(status == INTERNET_INCONNECTED){
				internetWeak(R.string.error_cannot_connect_to_internet);
			}
		}
		
	}

	@Override
	public void onShouldStartAudio(int index) {
		playAudioFile(mVideoGenParams.mVignettes.get(index).mAudioPath);
	}

	@Override
	public void onVideoAnimationEnded() {
		//The dialog dismisses it self right before calling onVideoAnimationEnded
		//mVideoDialog.dismiss();
		mVideoDialog = null;
	}

	@Override
	public void onReadyToPlay() {
	}
}
