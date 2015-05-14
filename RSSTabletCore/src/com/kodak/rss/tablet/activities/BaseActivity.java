package com.kodak.rss.tablet.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.facebook.android.Facebook;
import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.AppManager;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.bean.SideMenuItem;
import com.kodak.rss.tablet.facebook.FBKWrapper;
import com.kodak.rss.tablet.facebook.FacebookAuthorize;
import com.kodak.rss.tablet.fragment.SideMenuFragment;
import com.kodak.rss.tablet.handler.FacebookLogInHandler;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.util.UploadProgressUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.LoaderUtil;
import com.kodak.rss.tablet.view.dialog.DialogEulaAndPrivate;
import com.kodak.rss.tablet.view.dialog.DialogEulaAndPrivate.onDialogEulaListener;
import com.kodak.rss.tablet.view.dialog.DialogUploadImageError;
import com.kodak.rss.tablet.view.dialog.InfoDialog;
import com.mobileapptracker.MobileAppTracker;

/**
 * If you modify the code, please make the same modification in BaseCaptureActivity
 * @author Robin
 *
 */
public class BaseActivity extends FragmentActivity implements OnClickListener,ActivityStateWatcher, DrawerListener{
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
	public SideMenuFragment sideMenu;
	private DrawerLayout drawerLayout;
	
	protected ActivityState state;
	public DisplayMetrics dm;
	public int screenWidth;
	public int screenHeight;
	
	protected String localyticsPageViewName = "";
	
	public FBKWrapper fbkWrapper = FBKWrapper.getWrapper();
	public Facebook facebook = FBKWrapper.getWrapper().facebook;
	public FacebookAuthorize fbkAuth = null;
	public FacebookLogInHandler facebookLogInHandler;
	public MobileAppTracker mobileAppTrack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		RSSLocalytics.onActivityCreate(this);
		recordLocalyticsPageOpen();
		
		AppManager.getInstance().addActivity(this);
		state = ActivityState.CREATED;
		//restore global values if needed
		if(getApp().isGloablVariablesRecyled() && savedInstanceState != null){
			getApp().restoreGlobalVariables();
		}
		facebookLogInHandler = new FacebookLogInHandler(BaseActivity.this);
		fbkWrapper.loginHandler = facebookLogInHandler;
		fbkAuth = new FacebookAuthorize(this,fbkWrapper);	
		dm = getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		if(RssTabletApp.getInstance().isBrandApp()){
			mobileAppTrack = MobileAppTracker.getInstance();
		}
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
		
		drawerLayout.setDrawerListener(this);
		
	}
	
	/**
	 * @author Robin
	 * @return
	 */
	protected boolean hasSideMenu(){
		return true;
	}
	
	////////////side menu drawListener method
	@Override
	public void onDrawerClosed(View arg0) {
		
	}
	@Override
	public void onDrawerOpened(View arg0) {
		
	}
	@Override
	public void onDrawerSlide(View arg0, float arg1) {
		
	}
	@Override
	public void onDrawerStateChanged(int arg0) {
		
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
	
	protected void recordLocalyticsPageOpen(){
		if(this instanceof MainActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_SELECT_WORKFLOW;
		}else if(this instanceof StoreSelectActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_STORE_LOCATOR;
		}else if(this instanceof MyProjectsActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_SAVED_PROJECTS;
		}else if(this instanceof WiFiSelectWorkflowActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_WIFI_CHOICE;
		}else if(this instanceof PhotobookSelectionActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_PHOTOBOOK_TYPE;
		}else if(this instanceof PhotoBooksThemeSelectActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_PB_THEME;
		}else if(this instanceof ShoppingCartActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_SHOPPING_CART;
		}else if(this instanceof PrintsActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_PRINTT_IMAGE_SOURCE;
		}else if(this instanceof OrderSummaryActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_ORDER_SUCCESS;
		}else if(this instanceof PhotoBooksProductActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_PB_PREVIEW;
		}else if(this instanceof PhotoBooksPicSelectActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_PB_IMAGE_SOURCE;
		}else if(this instanceof GCSSCategorySelectActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_GC_CATEGROY;
		}else if(this instanceof GCCategorySelectActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_GC_DESIGN;			
		}else if(this instanceof CalendarSelectionActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_CALENDAR_TYPE;
		}else if(this instanceof CalendarThemeSelectionActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_CALENDAR_THEME;
		}else if(this instanceof CollageSelectionActivity){
			localyticsPageViewName = RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_COLLAGE_TYPE;
		}
		
		if(!"".equals(localyticsPageViewName)){
			RSSLocalytics.recordLocalyticsPageView(this, localyticsPageViewName);
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
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		state = ActivityState.STARTED;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(RssTabletApp.getInstance().isBrandApp()){
			mobileAppTrack.setReferralSources(this);
			mobileAppTrack.measureSession();
		}
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
			new DialogUploadImageError().initDialogUploadImageError(BaseActivity.this,failInfo);
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
	
	@Override
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (hasSideMenu() && facebook != null) {
			if (Facebook.DEFAULT_AUTH_ACTIVITY_CODE == requestCode) {
				facebook.authorizeCallback(requestCode, resultCode, data);
				return;
			}			
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
					ImageInfo imageInfo = getDealWithInfo(picUploadSuceessId,flowType, productId);
					if (imageInfo != null && !isHaveUploadErrorDialog) {							
						new DialogUploadImageError().initDialogUploadImageError(BaseActivity.this,imageInfo);													
					}					
				}else {
					dealWithUploadSuceess(picUploadSuceessId, isThumbnail, flowType, productId);
				}				
			}	
		}
	};
	
	public void dealWithUploadSuceess(String picUploadSuceessId,boolean isThumbnail,String flowType,String productId){}

	public ImageInfo getDealWithInfo(String picUploadSuceessId,String flowType,String productId){
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
			if (BaseActivity.this instanceof MainActivity) {
				AppManager.getInstance().goToStartupActivity();
			} else {
				android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						RSSLocalytics.recordLocalyticsEvents(BaseActivity.this, RSSTabletLocalytics.LOCALYTICS_EVENT_START_OVER);
						startOver();				
					}		
				};		
				new InfoDialog.Builder(this).setMessage(getHomePrompt())
				.setPositiveButton(getText(R.string.d_no), null)
				.setNegativeButton(R.string.d_yes, yesOnClickListener)
				.create()
				.show();
			}
			break;
		case SideMenuItem.ITEM_SETTINGS:
			Intent mIntent = new Intent(this, SettingsActivity.class);
			String className = this.getClass().getSimpleName();
			Log.e("ClassName", className);
			if(BaseActivity.this instanceof MainActivity){
				Bundle bundle = new Bundle();
				bundle.putBoolean("fromMain", RssTabletApp.getInstance().isFacebookCanLogout());				
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
			if (sideMenu.isFacebookLogin()) {	
				if(RssTabletApp.getInstance().isFacebookCanLogout()){
					LoaderUtil.clearCaches(getApplicationContext(),"."+FilePathConstant.externalType);
					SharedPreferrenceUtil.saveFacebookToken(BaseActivity.this, null);
					SharedPreferrenceUtil.saveFacebookAccessExpires(BaseActivity.this, -1);
					SharedPreferrenceUtil.saveFacebookUserId(BaseActivity.this, "");
					SharedPreferrenceUtil.saveFacebookFristName(BaseActivity.this, "");
					SharedPreferrenceUtil.saveFacebookLastName(BaseActivity.this, "");
					sideMenu.notifyLoginStatusChanged();
					
					if (BaseActivity.this != null && !BaseActivity.this.isFinishing()) {
						CookieSyncManager.createInstance(BaseActivity.this);   
				    	CookieSyncManager.getInstance().startSync();   
				    	CookieManager.getInstance().removeAllCookie();
					}	
					
				}else{
					new InfoDialog.Builder(this)
						.setMessage(R.string.facebook_can_not_logout)
						.setPositiveButton(R.string.d_ok, null)
						.create()
						.show();
				}
			}else {
	        	facebookLogInHandler.logIn();	
			}
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
	
	public void previousDoMoreOver(){
		AppManager.getInstance().goToHomeActivity();
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
		if (judgeSelectHavedProductInfo()) {
			
		}else {
			popNoItemDialog();	
		}					
	}
	
	public boolean judgeSelectHavedProductInfo(){		
		if (RssTabletApp.getInstance().products == null){
			RssTabletApp.getInstance().products = new ArrayList<ProductInfo>();
			return false;
		} 			
		List<ProductInfo> delList = new ArrayList<ProductInfo>();
		for(ProductInfo pInfo : RssTabletApp.getInstance().products ) {
			if (pInfo != null && pInfo.num < 1) {						
				delList.add(pInfo);
			}
		}
		RssTabletApp.getInstance().products.removeAll(delList);			
		delList = new ArrayList<ProductInfo>();
		for(ProductInfo pInfo : RssTabletApp.getInstance().products) {
			if (pInfo == null) continue;
			if (AppConstants.printType.equals(pInfo.productType)) {	
				if (pInfo.chosenImageList!= null) {
					ImageInfo imageInfo = pInfo.chosenImageList.get(0);
					if (imageInfo != null) {
						if (imageInfo.imageOriginalResource != null) {
							pInfo.correspondId = imageInfo.imageOriginalResource.id;
						}	
						pInfo.displayImageUrl = imageInfo.editUrl;	
						pInfo.downloadDisplayImageUrl=imageInfo.downloadOriginalUrl;
					}
				}else {
					delList.add(pInfo);					
				}
			}
			if (AppConstants.bookType.equals(pInfo.productType)) {	
				if (pInfo.cartItemId != null) {
					Photobook photobook = getBook(pInfo.cartItemId);
					if (photobook == null ) {
						delList.add(pInfo);		
					}					
				}else {
					delList.add(pInfo);		
				}
			}	
			if (AppConstants.calendarType.equals(pInfo.productType)) {	
				if (pInfo.cartItemId != null) {
					Calendar calendar = getCalendar(pInfo.cartItemId);
					if (calendar == null ) {
						delList.add(pInfo);		
					}					
				}else {
					delList.add(pInfo);		
				}
			}	
			if (AppConstants.collageType.equals(pInfo.productType)) {	
				if (pInfo.cartItemId != null) {
					Collage collage = getCollage(pInfo.cartItemId);
					if (collage == null ) {
						delList.add(pInfo);		
					}					
				}else {
					delList.add(pInfo);		
				}
			}	
			
			if (AppConstants.cardType.equals(pInfo.productType)) {	
				if (pInfo.cartItemId != null) {
					GreetingCard card = getGreetingCard(pInfo.cartItemId);
					if (card == null ) {
						delList.add(pInfo);		
					}					
				}else {
					delList.add(pInfo);		
				}
			}				
		}	
		RssTabletApp.getInstance().products.removeAll(delList);			
		boolean isHave = true;
		if (RssTabletApp.getInstance().products.size() < 1) {
			isHave = false ;
		}		
		return isHave;
	}	
	
	private Photobook getBook(String id){
		Photobook book = null;
		if (id == null) return book;
		if ("".equals(id)) return book;
		List<Photobook> chosenBookList = RssTabletApp.getInstance().chosenBookList;	
		if (chosenBookList == null) return book;
		for (Photobook photobook : chosenBookList) {
			if (photobook != null && id.equals(photobook.id)) {
				book = photobook;
				break;
			}
		}	
		return book;
	}
	
	private Calendar getCalendar(String id){
		Calendar calendar = null;
		if (id == null) return calendar;
		if ("".equals(id)) return calendar;
		List<Calendar> chosenCalendarList = RssTabletApp.getInstance().calendarList;	
		if (chosenCalendarList == null) return calendar;
		for (Calendar chosenCalendar  : chosenCalendarList) {
			if (chosenCalendar != null && id.equals(chosenCalendar.id)) {
				calendar = chosenCalendar;
				break;
			}
		}	
		return calendar;
	}
	
	private Collage getCollage(String id){
		Collage collage = null;
		if (id == null) return collage;
		if ("".equals(id)) return collage;
		List<Collage> chosenCollageList = RssTabletApp.getInstance().collageList;	
		if (chosenCollageList == null) return collage;
		for (Collage chosenCollage  : chosenCollageList) {
			if (chosenCollage != null && id.equals(chosenCollage.id)) {
				collage = chosenCollage;
				break;
			}
		}	
		return collage;
	}
	
	private GreetingCard getGreetingCard(String id){
		GreetingCard card = null;
		if (id == null) return card;
		if ("".equals(id)) return card;
		List<GreetingCard> chosenCardList = RssTabletApp.getInstance().gCardList;	
		if (chosenCardList == null) return card;
		for (GreetingCard chosenCard  : chosenCardList) {
			if (chosenCard != null && id.equals(chosenCard.id)) {
				card = chosenCard;
				break;
			}
		}	
		return card;
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
