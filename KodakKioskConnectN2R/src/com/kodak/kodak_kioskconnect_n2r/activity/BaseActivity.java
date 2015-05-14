package com.kodak.kodak_kioskconnect_n2r.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.AppManager;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.GreetingCardProductActivity;
import com.kodak.kodak_kioskconnect_n2r.GreetingCardSelectionActivity;
import com.kodak.kodak_kioskconnect_n2r.GreetingCardThemeSelectionActivity;
import com.kodak.kodak_kioskconnect_n2r.HelpActivity;
import com.kodak.kodak_kioskconnect_n2r.ImageSelectionDatabase;
import com.kodak.kodak_kioskconnect_n2r.InfoDialog;
import com.kodak.kodak_kioskconnect_n2r.NewSettingActivity;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintMakerWebService;
import com.kodak.kodak_kioskconnect_n2r.QuickBookFlipperActivity;
import com.kodak.kodak_kioskconnect_n2r.QuickBookSelectionActivity;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.WiFiSelectWorkflowActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.SlideMenuEntity;
import com.kodak.kodak_kioskconnect_n2r.view.InfoDialogWindow;
import com.kodak.kodak_kioskconnect_n2r.view.InfoDialogWindow.Builder;
import com.kodak.utils.RSSLocalytics;
import com.mobileapptracker.MobileAppTracker;

/**
 * Base Activity
 * 
 * @author song
 * @version 1.0
 * @created 2014-3-26
 */
public abstract class BaseActivity extends FragmentActivity {
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private View contentView;
	private LinearLayout ly_content;
	private Button slideMenuOpen;
	private Button slideMenuClose_btn;
	private TextView sideMenuHome_tex;
	private TextView sideMenuSetting_tex;
	private TextView sideMenuInfo_tex;
	private TextView sideMenuFacebook_tex ;
	private TextView sideMenuVersion_tex;
	private DrawerLayout mDrawerLayout;
	protected RelativeLayout sideMenuHome_lay;
	protected RelativeLayout sideSetting_lay;
	protected RelativeLayout sideInfo_lay ;
	protected RelativeLayout sideFacebook_lay ;

	private List<SlideMenuEntity> slideMenuEnters;
	private SlideMenuEntity slideMenuObj;
	private String SCREEN_NAME = "";
	private static final String START_OVER = "Start Over";

	private int resId = 0;

	public abstract void getViews();

	public abstract void initData();

	public abstract void setEvents();
	protected InfoDialog noConnectionDialog = null;
	protected InfoDialog gotoHomeDialog = null;
	
	private BaseActivity mActivity ;
	public MobileAppTracker mobileAppTrack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base);
		mActivity = this ;
		RSSLocalytics.onActivityCreate(this);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		PrintHelper.handleUncaughtException(BaseActivity.this, this);
		// add Activity to stack
		AppManager.getAppManager().addActivity(this);
		findViews();
		
		setOnclickers();
		initSlideMenuList();
		initBaseData();
		if(!isNeedHideFacebook()){
			sideFacebook_lay.setVisibility(View.VISIBLE) ;
			initFacebook(savedInstanceState) ;
			
		}else {
			sideFacebook_lay.setVisibility(View.GONE) ;
		}
		if(getPackageName().equals(AppConstants.KODAK_MY_KODAK_MOMENTS)){
			mobileAppTrack = MobileAppTracker.getInstance();
		}
	}
	
	 @Override
	 protected void onStart() {
	        super.onStart();
	        if(!isNeedHideFacebook()){
	        	Session.getActiveSession().addCallback(statusCallback);
	        }
	     
	    }

	    @Override
	    protected void onStop() {
	        super.onStop();
	        if(!isNeedHideFacebook()){
	        	 Session.getActiveSession().removeCallback(statusCallback);
	        }
	       
	    }

	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	        if(!isNeedHideFacebook()){
	        	 Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	        }
	       
	    }

	    @Override
	    protected void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	        if(!isNeedHideFacebook()){
	        	Session session = Session.getActiveSession();
	 	        Session.saveSession(session, outState);
	        }
	       
	    }

	private void initFacebook(Bundle savedInstanceState) {
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
//			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
//	               session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
//	        }
		}
		
	
		
	}

	protected void updateFacebookView() {
		 Session session = Session.getActiveSession();
		   if (session.isOpened()) {
			   sideMenuFacebook_tex.setText(R.string.Common_Logout);
			   sideFacebook_lay.setOnClickListener(new OnClickListener() {
	                public void onClick(View view) { 
	                	onClickLogout(); 
	                }

					private void onClickLogout() {
				        Session session = Session.getActiveSession();
				        if (!session.isClosed()) {
				            session.closeAndClearTokenInformation();
				            AppContext.getApplication().setFacebook_photos_of_you_cover("") ;
				            AppContext.getApplication().setFacebook_your_photos_cover("") ;
				            AppContext.getApplication().setFacebook_your_friends_cover("") ;
				        }
				     // close
						mDrawerLayout.closeDrawer(Gravity.LEFT);
					}
	            });
	        }else {
	        	
	        	sideMenuFacebook_tex.setText(R.string.Common_Login) ;
	        	sideFacebook_lay.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(!Connection.isConnected(BaseActivity.this)){
							showNoConnectionDialog() ;
							// close
							mDrawerLayout.closeDrawer(Gravity.LEFT);
						}else {
							onClickLogin() ;
						}
						
						
						
					}

				private void onClickLogin() {
					Session session = Session.getActiveSession();
					List<String> permissions = Arrays.asList("public_profile" , "user_friends", "user_photos","friends_photos","user_status","friends_status");
					if (!session.isOpened() && !session.isClosed()) {
						session.openForRead(new Session.OpenRequest(BaseActivity.this).setCallback(statusCallback)
								.setPermissions(permissions));
					} else {
						Session.openActiveSession(BaseActivity.this, true, permissions ,statusCallback);
					}
					// close
					mDrawerLayout.closeDrawer(Gravity.LEFT);

				}
				}) ;
	        	
	        }
		
	}

	private void findViews() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		ly_content = (LinearLayout) findViewById(R.id.content);
		sideMenuHome_lay = (RelativeLayout)findViewById(R.id.sideMenuHome_lay);
		sideSetting_lay = (RelativeLayout)findViewById(R.id.sideSetting_lay);
		sideInfo_lay = (RelativeLayout)findViewById(R.id.sideInfo_lay);
		sideFacebook_lay = (RelativeLayout) findViewById(R.id.sideFacebook_lay) ;
		slideMenuOpen = (Button) findViewById(R.id.slideMenuOpen_btn);
		slideMenuOpen.setVisibility(View.VISIBLE);
		slideMenuClose_btn = (Button) findViewById(R.id.slideMenuClose_btn);
		sideMenuHome_tex = (TextView) findViewById(R.id.sideMenuHome_tex);
		sideMenuSetting_tex = (TextView) findViewById(R.id.sideMenuSetting_tex);
		sideMenuInfo_tex = (TextView) findViewById(R.id.sideMenuInfo_tex);
		sideMenuFacebook_tex = (TextView) findViewById(R.id.sideMenuFacebook_tex) ;
		sideMenuVersion_tex = (TextView) findViewById(R.id.sideMenuVersion_tex);
	}

	private void setOnclickers() {
		slideMenuOpen.setOnClickListener(openMenu());

		slideMenuClose_btn.setOnClickListener(closeMenu());

		sideMenuHome_lay.setOnClickListener(gotoHome());
		sideSetting_lay.setOnClickListener(gotoSettings());
		sideInfo_lay.setOnClickListener(gotoInfo());
		
		sideMenuHome_tex.setOnClickListener(gotoHome());

		sideMenuSetting_tex.setOnClickListener(gotoSettings());

		sideMenuInfo_tex.setOnClickListener(gotoInfo());
		mDrawerLayout.setDrawerListener(new DrawerListener() {
			
			@Override
			public void onDrawerStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDrawerOpened(View arg0) {
				slideMenuOpen.setVisibility(View.INVISIBLE);
				
			}
			
			@Override
			public void onDrawerClosed(View arg0) {
				slideMenuOpen.setVisibility(View.VISIBLE);
				
			}
		});

	}

	protected OnClickListener gotoHome() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// close
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(BaseActivity.this);
				builder.setTitle("");
				builder.setMessage(getString(R.string.Home_LoseAllWorkCart));
				builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent();
						if (AppContext.getApplication().getFlowType()!= null && AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
							PrintHelper.clearDataForWifi();
							if (getPackageName().contains("kodakprintmaker") || getPackageName().contains("wmc")) {
								intent.setClass(BaseActivity.this, WiFiSelectWorkflowActivity.class);
							} else if (BaseActivity.this instanceof WiFiSelectWorkflowActivity) {
								intent.setClass(BaseActivity.this, MainMenu.class);
							} else {
								intent.setClass(BaseActivity.this, WiFiSelectWorkflowActivity.class);
							}
						} else {
							intent.setClass(BaseActivity.this, MainMenu.class);
						}
						
						AppManager.getAppManager().finishAllActivity();
						startActivity(intent);
						
						AppContext.getApplication().setContinueShopping(false);
						PrintHelper.clearDataForDoMore();
						if (resId == R.layout.shoppingcartfield) {
							PrintHelper.isClearDataForDoMore = true;
							ImageSelectionDatabase mImageSelectionDatabase = new ImageSelectionDatabase(BaseActivity.this);
							mImageSelectionDatabase.open();
							mImageSelectionDatabase.handleDeleteAllUrisN2R();
							mImageSelectionDatabase.close();
							AppContext.getApplication().setContinueShopping(false);
						}
												
						boolean success = PrintHelper.StartOver();
						PrintHelper.clearDataForDoMore();
						if (!success) {
							new PrintHelper(getApplicationContext());
						}
						RSSLocalytics.recordLocalyticsEvents(BaseActivity.this, START_OVER);
					}
				});
				builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				gotoHomeDialog = builder.create();
				gotoHomeDialog.show();
				
			}
		};
		return listener;

	}

	private OnClickListener gotoSettings() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// close
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				SCREEN_NAME = getChildActivityScreenName();
				Intent myIntent = new Intent(BaseActivity.this, NewSettingActivity.class);
				Bundle b = new Bundle();
				
				if(mActivity instanceof MainMenu){
					b.putBoolean(NewSettingActivity.SHOW_CHANGE_COUNTRY, true);
				}else if(mActivity instanceof ShoppingCartActivity ){
					myIntent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
				}else if(mActivity instanceof PhotoSelectMainFragmentActivity ){
					if (AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
						myIntent = new Intent(BaseActivity.this, WifiNewSettingFragmentActivity.class);
					} 
				}else if(mActivity instanceof WiFiSelectWorkflowActivity){
					myIntent = new Intent(BaseActivity.this, WifiNewSettingFragmentActivity.class);
				}else if(mActivity instanceof WifiTaggedImagesActivity ){
					myIntent = new Intent(BaseActivity.this, WifiNewSettingFragmentActivity.class);
				}
				
				if (!"".equals(SCREEN_NAME)) {
					b.putString(NewSettingActivity.SETTINGS_LOCATION, SCREEN_NAME);
					
				}
				myIntent.putExtras(b);
				startActivity(myIntent);	
				
			}
		};
		return listener;

	}

	private OnClickListener gotoInfo() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// close
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				SCREEN_NAME = getChildActivityScreenName();
				Intent myIntent = new Intent(BaseActivity.this, HelpActivity.class);
				Bundle b = new Bundle();
				if (!"".equals(SCREEN_NAME)) {
					b.putString(HelpActivity.HELP_LOCATION, SCREEN_NAME);
				}
				myIntent.putExtras(b);
				startActivity(myIntent);
				
			}
		};
		return listener;

	}

	private OnClickListener openMenu() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// open
				mDrawerLayout.openDrawer(Gravity.LEFT);

			}
		};
		return listener;

	}

	private OnClickListener closeMenu() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// close
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			}
		};
		return listener;

	}

	private String getChildActivityScreenName(){
		String SCREEN_NAME = "" ;
		if(mActivity instanceof MainMenu){
			SCREEN_NAME = "Workflow Choice";
			
		}else if(mActivity instanceof ProductSelectActivity ){
			SCREEN_NAME = "Workflow Choice";
		}else if(mActivity instanceof ShoppingCartActivity){
			SCREEN_NAME = "Shopping Cart";
		}else if(mActivity instanceof PhotoSelectMainFragmentActivity){
			if (AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()) {
				SCREEN_NAME = "PB Image Source";
			} else if (AppContext.getApplication().getFlowType().isPrintWorkFlow()) {
				SCREEN_NAME = "Prt Image Source";
			}
		}else if(mActivity instanceof WiFiSelectWorkflowActivity){
			SCREEN_NAME = "Wifi Choice";
		}else if(mActivity instanceof WifiTaggedImagesActivity){
			SCREEN_NAME="Wifi Select and Send" ;
		}else if (mActivity instanceof QuickBookFlipperActivity){
			SCREEN_NAME = "PB Preview" ;
		}else if(mActivity instanceof QuickBookSelectionActivity){
			SCREEN_NAME = "Photobook Type" ;
		}else if (mActivity instanceof GreetingCardThemeSelectionActivity ){
			SCREEN_NAME = "GC Design" ;
		}else if (mActivity instanceof GreetingCardSelectionActivity ){
			SCREEN_NAME = "GC Category" ;
		}else if(mActivity instanceof GreetingCardProductActivity ){
			SCREEN_NAME = "GC Preview" ; 
		}
		
		return SCREEN_NAME ;
		
	}
	
	
	private void initSlideMenuList() {
		getSlideMenuListData();
	}

	private void getSlideMenuListData() {
		slideMenuEnters = new ArrayList<SlideMenuEntity>();
		for (int i = 0; i < 10; i++) {
			slideMenuObj = new SlideMenuEntity();
			slideMenuObj.setName("slide menu ----------" + i);
			slideMenuEnters.add(slideMenuObj);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		RSSLocalytics.onActivityPause(this);
	}

	@Override
	protected void onDestroy() {
		AppManager.getAppManager().finishActivity(this);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(getPackageName().equals(AppConstants.KODAK_MY_KODAK_MOMENTS)){
			mobileAppTrack.setReferralSources(this);
			mobileAppTrack.measureSession();
		}
		if(!isNeedHideFacebook()){
			updateFacebookView();
		}
		
		RSSLocalytics.onActivityResume(this);
	}
	
	/***
	 * set the content layout
	 * 
	 * @param resId
	 * 
	 */
	@SuppressLint("InlinedApi")
	public void setContentLayout(int resId) {
		this.resId = resId;
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentView = inflater.inflate(resId, null);
		LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		contentView.setLayoutParams(mLayoutParams);
		if (null != ly_content) {
			ly_content.removeAllViews();
			ly_content.addView(contentView);
		}
	}

	/***
	 * set the content layout
	 * 
	 * @param view
	 *            View object
	 */
	public void setContentLayout(View view) {
		if (null != ly_content) {
			ly_content.addView(view);
		}
	}

	/**
	 * get the content View
	 * 
	 * @return
	 */
	public View getLyContentView() {
		return contentView;
	}

	public BaseActivity() {

	}

	private void initBaseData() {
		PackageInfo packageInfo;
		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String versionName = getString(R.string.mainMenuVersion) + " " + packageInfo.versionName;// + " " + getString(R.string.mainMenuCopyright);
			sideMenuVersion_tex.setText(versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	
	protected void showNoConnectionDialog(final View view){
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.nointernetconnection));
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				view.performClick();
			}
		});
		noConnectionDialog = builder.create();
		noConnectionDialog.show();
	}
	
	public void showNoConnectionDialog(){
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.nointernetconnection));
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		
		builder.create().show();
	}
	
	public void showNoRespondDialog() {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		String message = getString(R.string.share_upload_error_no_responding);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	
		builder.setCancelable(false);
		builder.create().show();
	}
	/**
	 * Kiosk Connect App needn't show facebook
	 * @return
	 */
	private boolean isNeedHideFacebook(){
		String packageName = this.getPackageName() ;
		if(packageName!=null && 
				( packageName.contains("com.kodak.kodak.rsscombinedapp") ||
						packageName.contains("com.kodak.dm.rsscombinedapp") )){
			return false ;
		}else {
			return true ;
		}
		
		
//		return AppConstants.KODAK_PRINT_MAKER_PACKAGE_NAME.equals(this.getPackageName());
	}
	
	
	private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        	updateFacebookView() ;
        	
        }
    }
	
	public boolean appForbidden(final Context contex) {
		try {
			Thread tokenThred = new Thread(new Runnable() {
				
				@Override
				public void run() {
					PrintMakerWebService service = new PrintMakerWebService(contex, "");
					PrintMakerWebService.mAuthorizationToken = service.getAuthorizationToken();			
				}
			});
			tokenThred.start();
			tokenThred.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (PrintHelper.appForbidden){
			InfoDialogWindow biddenDialog = null;
			InfoDialogWindow.Builder builder = new Builder(contex);
			builder.setMessage(R.string.updatedMessage).setCancelable(false).create();
			builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Uri uri = Uri.parse(PrintHelper.appInfoUrl);  
					Intent it = new Intent(Intent.ACTION_VIEW, uri);  
					contex.startActivity(it);
				}
			});
			biddenDialog = builder.create();
			biddenDialog.show();
		}
		
		return PrintHelper.appForbidden;
	}
	
	
	
}
