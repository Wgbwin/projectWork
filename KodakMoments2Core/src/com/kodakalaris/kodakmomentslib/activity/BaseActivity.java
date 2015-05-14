package com.kodakalaris.kodakmomentslib.activity;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kodakalaris.kodakmomentslib.AppConstants.ActivityState;
import com.kodakalaris.kodakmomentslib.AppManager;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.interfaces.ActivityStateWatcher;
import com.kodakalaris.kodakmomentslib.service.PictureUploadService;
import com.kodakalaris.kodakmomentslib.util.ConnectionUtil;
import com.kodakalaris.kodakmomentslib.util.FontUtils;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.util.TextViewUtil;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.GeneralAlertDialogFragment;
import com.mobileapptracker.MobileAppTracker;
import com.nostra13.universalimageloader.core.ImageLoader;

public abstract class BaseActivity extends FragmentActivity implements ActivityStateWatcher{
	protected ActivityState mState;
	public MobileAppTracker mobileAppTrack;
	private Handler mHandler;
	
	private GeneralAlertDialogFragment mAppObsoleteDialog;
	private GeneralAlertDialogFragment mNetworkWeakDialog;
	private GeneralAlertDialogFragment mServerErrorDialog;
	private GeneralAlertDialogFragment mNoNetworkDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		initForTextViewFactor();//this method must be run before onCreate
		super.onCreate(savedInstanceState);
		
		AppManager.getInstance().addActivity(this);
		mState = ActivityState.CREATED;
		mHandler = new Handler();
		
		//restore global values if needed
		if (KM2Application.getInstance().isGloablVariablesRecyled() && savedInstanceState != null) {
			KM2Application.getInstance().restoreGlobalVariables();
		}
		
		// TODO: if only some Applications need to add this function, there need a judgment. Added by Kane
		if (mobileAppTrack == null) {
			mobileAppTrack = MobileAppTracker.getInstance();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mState = ActivityState.STARTED;
	}

	@Override
	protected void onResume() {
		super.onResume();
		AppManager.getInstance().setCurrentActivity(this);
		mState = ActivityState.RESUMED;
		// TODO: if only some Applications need to add this function, there need a judgment. Added by Kane
		if(mobileAppTrack == null){
			mobileAppTrack.setReferralSources(this);
			mobileAppTrack.measureSession();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mState = ActivityState.PAUSED;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mState = ActivityState.STOPPED;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getInstance().removeActivity(this);
		mState = ActivityState.DESTROYED;
		ImageLoader.getInstance().clearMemoryCache();
		System.gc();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(this == AppManager.getInstance().currentActivity()){
			KM2Application.getInstance().saveGlobalVariables();
		}
	}
	
	@Override
	public ActivityState getActivityState() {
		return mState;
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.DONUT)
	public static boolean isTablet(Context context) {
		String user_agent = new WebView(context).getSettings().getUserAgentString();
		boolean mobile = user_agent.contains("Mobile");
		return ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) && (!mobile);
	}
	
	
	public void startUploadService() {
		Class<com.kodakalaris.kodakmomentslib.service.PictureUploadService> pictureUploadService2 = com.kodakalaris.kodakmomentslib.service.PictureUploadService.class;
		Intent serviceIntent = new Intent(this, pictureUploadService2);	
		
		if (PictureUploadService.mTerminated) {
			PictureUploadService.mTerminated = false;
		}	
		if (PictureUploadService.isRunning && PictureUploadService.UploadImagesThread.mUploadImagesThread != null &&PictureUploadService.UploadImagesThread.mUploadImagesThread.isAlive()) return;

		try{
			ComponentName serviceComponentName = startService(serviceIntent);
			if (serviceComponentName != null){
				Log.i("startUploadService", "onCreate() startService called CompnentName=" + serviceComponentName.toString());
			}
		}catch (SecurityException se){
			se.printStackTrace();
		}
	}
	
	public void stopUploadService() {     
		Class<com.kodakalaris.kodakmomentslib.service.PictureUploadService> pictureUploadService2 = com.kodakalaris.kodakmomentslib.service.PictureUploadService.class;
		Intent serviceIntent = new Intent(this, pictureUploadService2);		
		try{
			PictureUploadService.mTerminated = true;
			stopService(serviceIntent);			
		}catch (SecurityException se){
			se.printStackTrace();
		}		
	}
	
	public void showErrorWaring(final WebAPIException e) {
		if (!isFinishing()) {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					if (e.isNetworkWeak()) {
						if (ConnectionUtil.isConnected(BaseActivity.this)) {
							showNetworkWeakWaring(e);
						} else {
							showNoNetworkDialog();
						}
					} else if (e.isServerError()) {
						showServerError(e);
					} else if (e.isAppObsolete()) {
						showAppObsoleteWarning(e);
					} else {
						Toast.makeText(BaseActivity.this, "WebAPIException:" + e.getCode(), Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
		
	}
	
	public void showAppObsoleteWarning(final WebAPIException e) {
		if(mAppObsoleteDialog == null){
			mAppObsoleteDialog = new GeneralAlertDialogFragment(this);
			mAppObsoleteDialog.setMessage(R.string.TitlePage_Error_App_Obsolete);
			mAppObsoleteDialog.setPositiveButton(R.string.Common_OK, new BaseGeneralAlertDialogFragment.OnClickListener() {
				
				@Override
				public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
					doOnClickOkForAppObsoleteDialog(e);
				}
			});
		}
		
		if(!mAppObsoleteDialog.isShowing()  && !isFinishing()){
			mAppObsoleteDialog.show(getSupportFragmentManager(), "mAppObsoleteDialog");
		}
	}
	
	public void showNetworkWeakWaring(final WebAPIException e) {
		if(mNetworkWeakDialog == null){
			mNetworkWeakDialog = new GeneralAlertDialogFragment(this);
			mNetworkWeakDialog.setMessage(R.string.error_cannot_connect_to_internet);
			mNetworkWeakDialog.setPositiveButton(R.string.Common_OK, null);
		}
		
		if(!mNetworkWeakDialog.isShowing()  && !isFinishing()){
			mNetworkWeakDialog.show(getSupportFragmentManager(), "mNetWorkWeakDialog");
		}
	}
	
	protected void showNoNetworkDialog() {
		if (mNoNetworkDialog == null) {
			mNoNetworkDialog = new GeneralAlertDialogFragment(this);
			mNoNetworkDialog.setMessage(R.string.Task_No_Internet);
			mNoNetworkDialog.setPositiveButton(R.string.Common_OK, null);
		}
		
		if (!isFinishing() && !mNoNetworkDialog.isShowing()) {
			mNoNetworkDialog.show(getSupportFragmentManager(), "mNoNetWorkDialog");
		}
	}
	
	protected void dismissNoNetworkDialog() {
		if (!isFinishing() && mNoNetworkDialog != null && mNoNetworkDialog.isShowing()) {
			mNoNetworkDialog.dismiss();
		}
	}
	
	public void showServerError(final WebAPIException e) {
		if(mServerErrorDialog == null){
			mServerErrorDialog = new GeneralAlertDialogFragment(this);
			mServerErrorDialog.setMessage(R.string.error_server);
			mServerErrorDialog.setPositiveButton(R.string.Common_OK, null);
		}
		
		if(!mServerErrorDialog.isShowing()  && !isFinishing()){
			mServerErrorDialog.show(getSupportFragmentManager(), "mServerErrorDialog");
		}
	}
	
	protected void doOnClickOkForAppObsoleteDialog(WebAPIException e) {
		if (e != null && e.getMessage() != null) {
			KM2Application.getInstance().isAppObsolete = true;
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(e.getMessage()));
			startActivity(intent);
		}
	}
	

	
	/**
	 * this method must be run before Activity.onCreate
	 * @author Robin
	 */
	private void initForTextViewFactor() {
		getLayoutInflater().setFactory(new LayoutInflater.Factory() {
			
			@Override
			public View onCreateView(String name, Context context, AttributeSet attrs) {
				View v = tryInflateTextView(name, context, attrs);
				//set font
				if (v != null && v instanceof TextView) {
					TextView textView = (TextView) v;
					//according to style.xml
					int style = attrs.getStyleAttribute();
					if (style == R.style.h1_text) {
						textView.setTypeface(FontUtils.getFont(BaseActivity.this, FontUtils.NAME_THIN));
					} else if (style == R.style.h2_text) {
						textView.setTypeface(FontUtils.getFont(BaseActivity.this, FontUtils.NAME_THIN));
					} else if (style == R.style.button_text) {
						textView.setTypeface(FontUtils.getFont(BaseActivity.this, FontUtils.NAME_THIN));
					} else if (style == R.style.subheadline_text) {
						textView.setTypeface(FontUtils.getFont(BaseActivity.this, FontUtils.NAME_MEDIUM));
					} else if (style == R.style.navigation_text) {
						textView.setTypeface(FontUtils.getFont(BaseActivity.this, FontUtils.NAME_REGULAR));
					} else if (style == R.style.text_links_text) {
						textView.setTypeface(FontUtils.getFont(BaseActivity.this, FontUtils.NAME_REGULAR));
					} else if (style == R.style.small_button_text) {
						textView.setTypeface(FontUtils.getFont(BaseActivity.this, FontUtils.NAME_LIGHT));
					} else if (style == R.style.body_copy_text) {
						textView.setTypeface(FontUtils.getFont(BaseActivity.this, FontUtils.NAME_LIGHT));
					} else if (style == R.style.tip_text) {
						textView.setTypeface(FontUtils.getFont(BaseActivity.this, FontUtils.NAME_REGULAR));
					} else if (style == R.style.error_code_text) {
						textView.setTypeface(FontUtils.getFont(BaseActivity.this, FontUtils.NAME_MEDIUM));
					}
					
				}
				
				//filter emoji
				if (v != null && v instanceof EditText) {
					EditText editText = (EditText) v;
					TextViewUtil.addEmojiFilter(editText);
				}
				
				return v;
			}
		});
	}
	
	/**
	 * try inflate textview or its subclass(ex:edittext)
	 * @param name
	 * @param context
	 * @author Robin
	 * @return
	 */
	private View tryInflateTextView(String name, Context context, AttributeSet attrs) {
		Class clz = null;
		String commonPrefix = TextView.class.getPackage().getName() + ".";
	    try {
	    	//add prefix for the most common TextView name
	    	if (TextView.class.getSimpleName().equals(name) 
	    			|| EditText.class.getSimpleName().equals(name)
	    			|| Button.class.getSimpleName().equals(name)
	    			) {
	    		name = commonPrefix + name;
	    	}
	    	
			clz = Class.forName(name);
		} catch (ClassNotFoundException e2) {
			try {
				clz = Class.forName(commonPrefix + name);
			} catch (ClassNotFoundException e) {
				try {
					clz = Class.forName("android.widget." + name);
				} catch (ClassNotFoundException e1) {
				}
			}
		}
		
	    View v = null;
	    //if it is textview or subclass of textview
	    if (clz != null && TextView.class.isAssignableFrom(clz)) {
	    	LayoutInflater li = LayoutInflater.from(context);
	    	try {
	    		v = li.createView(clz.getName(), null, attrs); 
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    	
	    }
	    return v;
		    
	}
	
	
}
