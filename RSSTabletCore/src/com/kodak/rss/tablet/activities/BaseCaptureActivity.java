package com.kodak.rss.tablet.activities;

import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.zxing.client.android.CaptureActivity;
import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.AppManager;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.bean.SideMenuItem;
import com.kodak.rss.tablet.fragment.SideMenuFragment;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.util.UploadProgressUtil;
import com.kodak.rss.tablet.view.dialog.DialogEulaAndPrivate;
import com.kodak.rss.tablet.view.dialog.DialogEulaAndPrivate.onDialogEulaListener;
import com.kodak.rss.tablet.view.dialog.DialogUploadImageError;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public abstract class BaseCaptureActivity extends CaptureActivity implements OnClickListener,ActivityStateWatcher{
public static final String INTENT_KEY_LOCALYTICS_PAGE_VIEW_NAME = "page_view";
	
	private final String TAG = getClass().getSimpleName();
	
	public boolean isHaveUploadErrorDialog;
	public boolean isHaveNotItemDialog;
	 
	public ImageButton btCart = null;
	ImageButton btSetup = null;
	ImageButton btTips = null;
	ImageButton btMenu = null;
	ImageButton btShare = null;
	private FrameLayout contentContainer;
	private SideMenuFragment sideMenu;
	private DrawerLayout drawerLayout;
	
	protected ActivityState state;
	public DisplayMetrics dm;
	public int screenWidth;
	public int screenHeight;
	
	protected String localyticsPageViewName = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		RSSLocalytics.onActivityCreate(this);
		
		AppManager.getInstance().addActivity(this);
		state = ActivityState.CREATED;
		//restore global values if needed
		if(getApp().isGloablVariablesRecyled() && savedInstanceState != null){
			getApp().restoreGlobalVariables();
		}
		
		dm = getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
	}
	
	@Override
	public void setContentView(View view) {
		if(hasSideMenu()){
			super.setContentView(R.layout.activity_base);
			initContentView(view);
		}else{
			super.setContentView(view);
		}
	}
	
	@Override
	public void setContentView(int layoutResID) {
		if(hasSideMenu()){
			super.setContentView(R.layout.activity_base);
			initContentView(LayoutInflater.from(this).inflate(layoutResID, null));
		}else{
			super.setContentView(layoutResID);
		}
	}
	
	@Override
	public void setContentView(View view, LayoutParams params) {
		if(hasSideMenu()){
			super.setContentView(R.layout.activity_base);
			initContentView(view);
		}else{
			super.setContentView(view, params);
		}
	}
	
	private void initContentView(View view){
		contentContainer = (FrameLayout) findViewById(R.id.content_container);
		contentContainer.removeAllViews();
		contentContainer.addView(view);
		
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		sideMenu = (SideMenuFragment) getSupportFragmentManager().findFragmentById(R.id.side_menu);
		
		sideMenu.setOnItemClickListner(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SideMenuItem item = sideMenu.getSideMenuAdapter().getItem(position);
				onSideMenuItemClick(item);
			}
		});
	}
	
	protected boolean hasSideMenu(){
		return true;
	}
	
	/**
	 * update the ui for side menu
	 * @author Robin
	 */
	public void updateSideMenu(){
		if(sideMenu != null){
			sideMenu.notifyLoginStatusChanged();
		}
	}
	
	
	protected void setLocalyticsPageViewName(String pageViewName){
		this.localyticsPageViewName = pageViewName;
	}
	
	protected String getLocalyticsPageViewName(){
		return localyticsPageViewName;
	}
	
	protected void setActionBarEvents(){
		btCart = (ImageButton) findViewById(R.id.cart);
		btSetup = (ImageButton) findViewById(R.id.setup);
		btTips = (ImageButton) findViewById(R.id.tips);
		btMenu = (ImageButton) findViewById(R.id.menu);
		btShare = (ImageButton) findViewById(R.id.share);
		
		if(btCart != null){
			btCart.setOnClickListener(this);
		}
		if(btSetup != null){
			btSetup.setOnClickListener(this);
		}
		if(btTips != null){
			btTips.setOnClickListener(this);
		}
		if(btMenu != null){
			btMenu.setOnClickListener(this);
		}
		if(btShare != null){
			btShare.setOnClickListener(this);
		}
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(this == AppManager.getInstance().currentActivity()){
			getApp().saveGlobalVariables();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		state = ActivityState.STARTED;
	}

	@Override
	protected void onResume() {
		super.onResume();
		RSSLocalytics.onActivityResume(this);
		AppManager.getInstance().setCurrentActivity(this);
		state = ActivityState.RESUMED;
		if (dm == null) {
			dm = getResources().getDisplayMetrics();
			screenWidth = dm.widthPixels;
			screenHeight = dm.heightPixels;
		}				
		setActionBarEvents();
		registerReceiver(mPicUploadReceiver, new IntentFilter(AppConstants.uploadPicAction));
		JudgeShowUploadErrorDialog();
		updateSideMenu();
	}	
	
	private void JudgeShowUploadErrorDialog(){	
		ImageInfo failInfo = null;		
		List<ImageInfo>  imageInfoList =  UploadProgressUtil.allImages();	
		if (imageInfoList != null) {
			for (int i =0 ;i< imageInfoList.size();i++) {
				ImageInfo info = imageInfoList.get(i);
				if (info != null && ((info.isHavedThumbnailUpload && info.imageThumbnailResource == null)||(info.isHavedOriginalUpload && info.imageOriginalResource == null))){			
					
					failInfo = info;				
					break;			
				}
			}		
		}
		if (failInfo != null && !isHaveUploadErrorDialog) {
			new DialogUploadImageError().initDialogUploadImageError(this,failInfo);
		}					
	}	

	@Override
	protected void onPause() {
		super.onPause();
		RSSLocalytics.onActivityPause(this);
		state = ActivityState.PAUSED;
	}
	
	@Override
	protected void onStop() {
		try {
			unregisterReceiver(mPicUploadReceiver);
		} catch (Exception e) {
			android.util.Log.e(TAG, e.getMessage());
		}	
		super.onStop();
		state = ActivityState.STOPPED;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getInstance().removeActivity(this);
		state = ActivityState.DESTROYED;
	}
	
	public void closeSideMenu(){
		if(drawerLayout != null && sideMenu != null){
			drawerLayout.closeDrawer(Gravity.START);
		}
	}
	
	public void openSideMenu(){
		if(drawerLayout != null && sideMenu != null){
			drawerLayout.openDrawer(Gravity.START);
		}
	}
	

	public void startUploadService() {     
		Class<com.kodak.rss.tablet.services.PictureUploadService> pictureUploadService2 = com.kodak.rss.tablet.services.PictureUploadService.class;
		Intent serviceIntent = new Intent(this, pictureUploadService2);		
		if (PictureUploadService.mTerminated) {
			PictureUploadService.mTerminated = false;
		}		
		if (PictureUploadService.isRunning && PictureUploadService.UploadImagesThread.mUploadImagesThread != null &&PictureUploadService.UploadImagesThread.mUploadImagesThread.isAlive()) return;
		try{						
			stopService(serviceIntent);			
		}catch (SecurityException se){
			se.printStackTrace();
		}
		try{			
			ComponentName serviceComponentName = startService(serviceIntent);
			if (serviceComponentName != null){
				Log.i(TAG, "onCreate() startService called CompnentName=" + serviceComponentName.toString());
			}
		}catch (SecurityException se){
			se.printStackTrace();
		}
	}
	
	public void stopUploadService() {     
		Class<com.kodak.rss.tablet.services.PictureUploadService> pictureUploadService2 = com.kodak.rss.tablet.services.PictureUploadService.class;
		Intent serviceIntent = new Intent(this, pictureUploadService2);		
		try{			
			PictureUploadService.mTerminated = true;
			stopService(serviceIntent);			
		}catch (SecurityException se){
			se.printStackTrace();
		}		
	}

	private BroadcastReceiver mPicUploadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(AppConstants.uploadPicAction)) {
				boolean PicUploadFailFlag = intent.getBooleanExtra(AppConstants.PicUploadFailFlag, false); 
				String picUploadSuceessId = intent.getStringExtra(AppConstants.PicUploadSuceessId);				
				boolean isThumbnail = intent.getBooleanExtra(AppConstants.isThumbnail, false);
				String productId = intent.getStringExtra(AppConstants.productId);
				String flowType = intent.getStringExtra(AppConstants.flowType);

				if (PicUploadFailFlag) {
					ImageInfo imageInfo = getDealWithInfo(picUploadSuceessId, flowType, productId);
					if (imageInfo != null && !isHaveUploadErrorDialog) {							
						new DialogUploadImageError().initDialogUploadImageError(BaseCaptureActivity.this,imageInfo);													
					}					
				}else {
					dealWithUploadSuceess(picUploadSuceessId, isThumbnail, flowType, productId);
				}				
			}	
		}
	};
	
	public void dealWithUploadSuceess(String picUploadSuceessId,boolean isThumbnail,String flowType,String productId){}

	public ImageInfo getDealWithInfo(String picUploadSuceessId, String flowType,String productId){
		ImageInfo imageInfo = null;
		List<ImageInfo> chosenList = null;
		if (AppConstants.bookType.equalsIgnoreCase(flowType)&& productId != null) {
			List<Photobook> chosenBookList = RssTabletApp.getInstance().chosenBookList;
			if (chosenBookList != null) {
				Photobook dealBook = null;
				for(int i = 0; i < chosenBookList.size();i++){
					dealBook = chosenBookList.get(i);					
					if (dealBook != null && dealBook.id.equals(productId)) {
						break;
					}			
				}
				if (dealBook != null) {
					chosenList = dealBook.chosenpics;						
				}	
			}
		}else if (AppConstants.printType.equalsIgnoreCase(flowType)) {
			chosenList = RssTabletApp.getInstance().chosenList;			
		}
		
		if (chosenList != null) {
			for(int i = 0; i < chosenList.size();i++){
			    ImageInfo info = chosenList.get(i);					
				if (info != null && picUploadSuceessId.equals(info.id)) {
					imageInfo = info;
					break;
				}		
			}
		}		
		return imageInfo;
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.cart) {
			judgeHaveItems();
		}else if(v.getId() == R.id.menu){
			openSideMenu();
		}
	}
	
	public void onSideMenuItemClick(SideMenuItem item){
		closeSideMenu();
		switch(item.getId()){
		case SideMenuItem.ITEM_HOME:
			android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					RSSLocalytics.recordLocalyticsEvents(BaseCaptureActivity.this, RSSTabletLocalytics.LOCALYTICS_EVENT_START_OVER);
					startOver();				
				}		
			};		
			new InfoDialog.Builder(this).setMessage(getHomePrompt())
			.setPositiveButton(getText(R.string.d_no), null)
			.setNegativeButton(R.string.d_yes, yesOnClickListener)
			.create()
			.show();
			break;
		case SideMenuItem.ITEM_SETTINGS:
			Intent mIntent = new Intent(this, SettingsActivity.class);
			String className = this.getClass().getSimpleName();
			Log.e("ClassName", className);
			if(className.equals("MainActivity")){
				Bundle bundle = new Bundle();
				bundle.putBoolean("fromMain", true);
				bundle.putString(INTENT_KEY_LOCALYTICS_PAGE_VIEW_NAME, getLocalyticsPageViewName());
				mIntent.putExtras(bundle);
			}
			startActivity(mIntent);
			Log.e(TAG, "Setup");
			break;
		case SideMenuItem.ITEM_INFO:
			HashMap<String,String> map = new HashMap<String, String>();
			if(null != getLocalyticsPageViewName() && !"".equals(getLocalyticsPageViewName())){
				map.put(RSSTabletLocalytics.LOCALYTICS_KEY_HELP_TYPE, RSSTabletLocalytics.LOCALYTICS_VALUE_HELP_TYPE_HELP);
				map.put(RSSTabletLocalytics.LOCALYTICS_KEY_HELP_LOCATION, getLocalyticsPageViewName());
				RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_HELP_ACCESS, map);
			}
			
			new DialogEulaAndPrivate().craeteDialog(this, getResources().getString(R.string.helpURL),
					getResources().getString(R.string.tip_title), null,getResources().getString(R.string.tip_exit_help),null,new onDialogEulaListener() {
				@Override
				public void onYes() {
				}
				@Override
				public void onNo() {
				}
			});
			break;
		case SideMenuItem.ITEM_TIPS:
			break;
		case SideMenuItem.ITEM_FACEBOOK:
			break;
		}
		
	}
	
	@Override
	public void onBackPressed() {	
		return ;
	}
	
	public RssTabletApp getApp(){
		return (RssTabletApp) getApplication();
	}
	
	public String getHomePrompt(){	
		if (RssTabletApp.getInstance().products != null && RssTabletApp.getInstance().products.size() > 0) {
			return getString(R.string.start_over_prompt);			
		}else {
			return	getString(R.string.privious_layout_content);			
		}			
	};
	
	public void startOver(){
		getApp().startOver();
	}

	@Override
	public ActivityState getActivityState() {
		return state;
	};
	
	public String getProductNum(){
		int num = 0 ;
		String unit = getString(R.string.cart_unit);
		List<ProductInfo> products = RssTabletApp.getInstance().products;
		if (products != null) {
			int size = products.size();
			for (int i = 0; i < size ; i++) {
				if (products.get(i) != null && products.get(i).num > 0) {
					num = num + products.get(i).num;
				}
			}			
		}		
		return num+" "+unit;
	}
	
	public void removeMenuItem(int itemId){
		sideMenu.removeItem(itemId);
	}
	
	public String getProductPriceUnit(){ 
		String unit = "$";
		List<ProductInfo> products = RssTabletApp.getInstance().products;
		if (products != null) {
			int size = products.size();
			for (int i = 0; i < size ; i++) {
				ProductInfo productInfo = products.get(i);
				if (productInfo != null && productInfo.num > 0 && productInfo.price != null) {
					unit = productInfo.price.substring(0, 1);
					break;
				}
			}			
		}		
		return unit;
	}
	
	public void judgeHaveItems(){
		popNoItemDialog();
	}
	
	public void popNoItemDialog(){		
		if (!isHaveNotItemDialog) {				
			android.content.DialogInterface.OnClickListener okOnClickListener  = new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					isHaveNotItemDialog = false;				
				}		
			};					
			new InfoDialog.Builder(this).setMessage(R.string.no_items_in_cart_prompt)
			.setPositiveButton(getText(R.string.d_ok), okOnClickListener).create()
			.show();	
			isHaveNotItemDialog = true;
		}
	}
}
