package com.kodakalaris.kodakmomentslib.activity.home;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.AppConstants.FlowType;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.BaseNetActivity;
import com.kodakalaris.kodakmomentslib.activity.shoppingcart.MShoppingCartActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.HomeProductsListAdapter;
import com.kodakalaris.kodakmomentslib.adapter.mobile.SimpleCarouselAdapter;
import com.kodakalaris.kodakmomentslib.adapter.mobile.SimpleCarouselAdapter.OnPageClickListener;
import com.kodakalaris.kodakmomentslib.bean.SimpleKPPCommandCallback;
import com.kodakalaris.kodakmomentslib.bean.items.HomeRibbonItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfigEntry;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.manager.KMConfigManager;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;
import com.kodakalaris.kodakmomentslib.util.SdCardUtil;
import com.kodakalaris.kodakmomentslib.util.StringUtils;
import com.kodakalaris.kodakmomentslib.widget.Carousel;
import com.kodakalaris.kodakmomentslib.widget.WaitingDialog;
import com.kodakalaris.kodakmomentslib.widget.mobile.GeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.HomeProductsListHead;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;
import com.kodakalaris.kpp.EPrintHubEventId;
import com.kodakalaris.kpp.IKPPEventsListener;
import com.kodakalaris.kpp.KodakPrintPlace;
import com.kodakalaris.kpp.PrinterInfo;

public class BaseHomeActivity extends BaseNetActivity {
	private static final String TAG = "BaseHomeActivity";
	
	protected Carousel vCarousel;
	protected SimpleCarouselAdapter mCarouselAdapter;
	protected RelativeLayout vRelaLyContent, vRelaLyHomeHead;
	protected ListView vLisvProducts;
	private HomeProductsListAdapter mListAdapter;
	private Context mContext;
	protected List<FlowType> products;
	protected int[] mDefaultImgs = null;
	protected Button vBtnCarouselAction;
	protected TextView vTxtCarouselTitle;
	protected TextView vTxtCarouselsubtitle;
	protected View vRelaLyCartBanner;
	protected List<KMConfig> mHomeConfigs;

	// ----joker_chen ---
	private ImageButton vBtnCloseSlideMenu;
	private MActionBar vActionBar;
	protected RelativeLayout vRelaLyCart, vRelaLyClearCart, vRelaLySettings, vRelaLyOrder, vRelaLyProfile, vRelaLyGallery;
	protected DrawerLayout vDrawerLayout;
	private static final int PHOTO_GRAPH = 1;
	private File imageFile = null;
	private static final String CAPTURE_APK_NAME = "kodakcapture.apk";
	
	protected KM2Application app;

	protected void initBaseHomeData() {
		getViews();
		initData();
		setEvents();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (vCarousel != null) {
			vCarousel.startAutoFlip();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		resetMenuItemStatus();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (vCarousel != null) {
			vCarousel.stopAutoFlip();
		}
	}
	
	private void getViews() {
		mContext = BaseHomeActivity.this;

		HomeProductsListHead productsHead = new HomeProductsListHead(mContext);
		vLisvProducts = (ListView) findViewById(R.id.lisv_home_products);
		vCarousel = (Carousel) productsHead.findViewById(R.id.carousel_home_productPage);
		vRelaLyContent = (RelativeLayout) productsHead.findViewById(R.id.relaLy_home_content);
		vBtnCarouselAction = (Button) productsHead.findViewById(R.id.btn_carousel_action);
		vTxtCarouselTitle = (TextView) productsHead.findViewById(R.id.txt_carousel_title);
		vTxtCarouselsubtitle = (TextView) productsHead.findViewById(R.id.txt_carousel_subtitle);
		vLisvProducts.addHeaderView(productsHead);
		
		vRelaLyCartBanner = findViewById(R.id.relaLy_cart_banner);

		// ----joker_chen ---
		vDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_home);
		vDrawerLayout.setScrimColor(getResources().getColor(R.color.warm_grey_90alpha));
		vBtnCloseSlideMenu = (ImageButton) findViewById(R.id.ibtn_home_close_slideMenun);
		vActionBar = (MActionBar) findViewById(R.id.actionbar);
		
		// ---- Kane ----
		vRelaLyCart = (RelativeLayout) findViewById(R.id.relaLy_slidemenu_cart);
		vRelaLyOrder = (RelativeLayout) findViewById(R.id.relaLy_slidemenu_orders);
		vRelaLyProfile = (RelativeLayout) findViewById(R.id.relaLy_slidemenu_profile);
		vRelaLyGallery = (RelativeLayout) findViewById(R.id.relaLy_slidemenu_gallery);
		vRelaLyClearCart = (RelativeLayout) findViewById(R.id.relaLy_slidemenu_clearcart);
		vRelaLySettings = (RelativeLayout) findViewById(R.id.relaLy_slidemenu_settings);
	}

	private void initData() {
		app = KM2Application.getInstance();
		if (app.isScanSDCard()) {
			if (Build.VERSION.SDK_INT < 19) {
				SdCardUtil.scanSdCard(mContext);
			}
			app.setScanSDCard(false);
		}
		products = new ArrayList<FlowType>();
		final List<RssEntry> prodcutEntries = PrintManager.getInstance(this).getPrintProducts();
		if(prodcutEntries != null) {
			for(RssEntry entry : prodcutEntries){
				FlowType type = getFlowType(entry.proDescription.type);
				if(type != null && !products.contains(type)){
					products.add(type);
				}
			}
		}
		products.add(FlowType.KIOSK);
		initCarouselImageSource();
		
		if (ShoppingCartManager.getInstance().isInDoMoreMode()) {
			vRelaLyCartBanner.setVisibility(View.VISIBLE);
		} else {
			vRelaLyCartBanner.setVisibility(View.INVISIBLE);
		}
		
		//setup listview
		List<KMConfig> listConfig = KMConfigManager.getInstance().getConfigs(KMConfig.Property.HOME_RIBBON_CAROUSEL, true);
		if (listConfig != null && listConfig.size() > 0) {
			mListAdapter = new HomeProductsListAdapter(mContext, listConfig.get(0), R.layout.home_item_list_products);
		} else {
			mListAdapter = new HomeProductsListAdapter(mContext, products, R.layout.home_item_list_products);
		}
		vLisvProducts.setAdapter(mListAdapter);
		
		//setup carousel
		mHomeConfigs = KMConfigManager.getInstance().getConfigs(KMConfig.Property.HOME_CAROUSEL, true);
		if (mHomeConfigs != null && !mHomeConfigs.isEmpty()) {
			mCarouselAdapter = new SimpleCarouselAdapter(mContext, mHomeConfigs.get(0));
		} else {
			mCarouselAdapter = new SimpleCarouselAdapter(mContext, mDefaultImgs);
		}
		mCarouselAdapter.setContainerSize(KM2Application.getInstance().getScreenW(), KM2Application.getInstance().getScreenH() / 2);
		vCarousel.setOffscreenPageLimit(3);
		vCarousel.setAdapter(mCarouselAdapter);
		
	}
	
	private FlowType getFlowType(String type){
		if(type.equals(AppConstants.PRO_TYPE_PRINT)){
			return FlowType.PRINT;
		}
		return null;
	}
	
	private void initCarouselImageSource(){
		//R.drawable.image_kioskconnect, R.drawable.image_threeframestory, R.drawable.image_photobooks, R.drawable.image_prints, R.drawable.image_cards, R.drawable.image_collages
		mDefaultImgs = new int[products.size()];
		for(int i=0; i<mDefaultImgs.length; i++){
			FlowType type = products.get(i);
			int resId = -1;
			switch (type) {
			case PRINT:
				resId = R.drawable.image_prints;
				break;
			case KIOSK:
				resId = R.drawable.image_kioskconnect;
				break;
			}
			mDefaultImgs[i] = resId;
		}
	}

	private void setEvents() {
		vCarousel.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (mHomeConfigs != null && mHomeConfigs.size() > 0) {
					if (mHomeConfigs.get(0).configData.entries == null || mHomeConfigs.get(0).configData.entries.size() == 0) {
						return;
					}
					
					final KMConfigEntry info = mHomeConfigs.get(0).configData.entries.get(position);
					vBtnCarouselAction.setText(info.actionText);
					vTxtCarouselTitle.setText(info.title);
					vTxtCarouselsubtitle.setText(info.subtitle);
					
					vBtnCarouselAction.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							doConfigAction(info.action);
						}
					});
				} else {
					//TODO:hard code
					vBtnCarouselAction.setOnClickListener(new OnClickListener(){
						
						@Override
						public void onClick(View v) {
							doConfigAction(KMConfigEntry.ACTION_KIOSK_CONNECT_WORKFLOW);
						}
					});
				}
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		mCarouselAdapter.setOnPageClickListener(new OnPageClickListener() {
			
			@Override
			public void OnPageClicked(int position) {
				List<KMConfig> topConfigs = KMConfigManager.getInstance().getConfigs(KMConfig.Property.HOME_CAROUSEL);
				if (topConfigs != null) {
					KMConfigEntry info = topConfigs.get(0).configData.entries.get(position);
					
					Class target = KMConfigManager.getInstance().getActionTargetActivity(info.action);
					if (target != null) {
						Intent intent = new Intent(BaseHomeActivity.this, target);
						startActivity(intent);
					}
					
				}
			}
		});
		
		vRelaLyCartBanner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BaseHomeActivity.this, MShoppingCartActivity.class);
				startActivity(intent);
			}
		});
		
		vLisvProducts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				HomeRibbonItem item = mListAdapter.getItem(position - vLisvProducts.getHeaderViewsCount());
				doConfigAction(item.action);
			}
		});
		
		vActionBar.setOnLeftButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				vDrawerLayout.openDrawer(Gravity.LEFT);
			}
		});

		vBtnCloseSlideMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				vDrawerLayout.closeDrawer(Gravity.LEFT);
			}
		});

		vDrawerLayout.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int arg0) {

			}

			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				
			}

			@Override
			public void onDrawerOpened(View arg0) {
				//vBtnOpenSlideMenun.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onDrawerClosed(View arg0) {
				//vBtnOpenSlideMenun.setVisibility(View.VISIBLE);
			}
		});

		vActionBar.setOnRightButtonClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
				// Uri iamgeFileUri = Uri.fromFile(imageFile);
				// intent.putExtra(MediaStore.EXTRA_OUTPUT, iamgeFileUri);
				// startActivityForResult(intent, PHOTO_GRAPH);
				try {
					dispatchTakePictureIntent();
				} catch (Exception e) {
					saveAPK2SDCard();
					installApkarchive();
				}

			}
		});

	}
	
	private void resetMenuItemStatus() {
		int visible = ShoppingCartManager.getInstance().getShoppingCartItems().size() == 0 ? View.GONE : View.VISIBLE;
		vRelaLyCart.setVisibility(visible);
		vRelaLyClearCart.setVisibility(visible);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCarouselAdapter = null;
		mDefaultImgs = null;
		vCarousel.setAdapter(null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTO_GRAPH) {
			if (resultCode == RESULT_OK) {
				SdCardUtil.scanFile(mContext, imageFile.toString());
			}
		}
	}

	/**
	 * Launch camera without Specific action.
	 */
	protected void dispatchTakePictureIntent() {

		// Check if there is a camera.
		PackageManager packageManager = mContext.getPackageManager();
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
			Toast.makeText(mContext, "This device does not have a camera.", Toast.LENGTH_SHORT).show();
			return;
		}

		// Camera exists? Then proceed...

		Intent takePictureIntent = new Intent();
		ComponentName cn = new ComponentName("com.kodakalaris.capture", "com.kodakalaris.capture.CameraActivity");
		takePictureIntent.setComponent(cn);
		startActivityForResult(takePictureIntent, PHOTO_GRAPH);

	}

	void installApkarchive() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CAPTURE_APK_NAME;
		Uri uriPath = Uri.fromFile(new File(path));
		intent.setDataAndType(uriPath, "application/vnd.android.package-archive");
		startActivity(intent);
	}

	/*
	 * add by song save the KodakCapture.apk to the sdcard then need to install;
	 */
	public void saveAPK2SDCard() {
		if (isExist()) {
			return;
		}
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CAPTURE_APK_NAME;
		File apkFile = new File(path);
		FileOutputStream outputStream = null;
		InputStream in = null;
		try {
			outputStream = new FileOutputStream(apkFile);
			AssetManager assetManager = getAssets();
			in = assetManager.open(CAPTURE_APK_NAME);
			if (in.available() <= 0) {
				return;
			}
			byte[] buffer = new byte[512];
			int hasRead = 0;
			while ((hasRead = in.read(buffer)) > 0) {
				outputStream.write(buffer, 0, hasRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void doConfigAction(String action) {
		if (StringUtils.isEmpty(action)) {
			return;
		}
		
		final Class target = KMConfigManager.getInstance().getActionTargetActivity(action);
		if (action.equals(KMConfigEntry.ACTION_PRINTS_WORKFLOW)) {
			if (target != null) {
				KM2Application.getInstance().setFlowType(FlowType.PRINT);
				Intent intent = new Intent(BaseHomeActivity.this, target);
				startActivity(intent);
				finish();
			}
		} else if (action.equals(KMConfigEntry.ACTION_KIOSK_CONNECT_WORKFLOW)) {
			if (target != null) {
				KM2Application.getInstance().setFlowType(FlowType.KIOSK);
				Intent intent = new Intent(BaseHomeActivity.this, target);
				startActivity(intent);
			}
		} else if (action.equals(KMConfigEntry.ACTION_PRINT_HUB_WORKFLOW)) {
			doPrintHubAction(target);
		}
	}
	
	boolean mPrintHubConnectTimeOut = false;
	boolean mReceiveGetPrinterInfoDone = false;//print hub will send this meg twice, so add this flag
	boolean mConnectFinished = false;
	private void doPrintHubAction(final Class target) {
		final WaitingDialog waitingDialog = new WaitingDialog(this, false);
		waitingDialog.initDialog(R.string.Common_please_wait);
		waitingDialog.show(getSupportFragmentManager(), "action print hub");
		
		mPrintHubConnectTimeOut = false;
		mReceiveGetPrinterInfoDone = false;
		mConnectFinished = false;
		//if the connect and get print info time out, dismiss waiting dialog
		final long TIME_OUT = 20; //second
		final Handler handler = new Handler();
		final Thread timeOutTask = new Thread() {
			
			@Override
			public void run() {
				long time = 0;
				long step = 2;
				while (time < TIME_OUT && !mConnectFinished) {
					try {
						Thread.sleep(step * 1000);
					} catch (InterruptedException e) {
						Log.i(TAG,"timeOutTask interrupted");
					}
					
					time = time + step;
				}
				
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						if (waitingDialog.isShowing() && !isFinishing()) {
							waitingDialog.dismiss();
						}
						
						if (!mConnectFinished) {
							mPrintHubConnectTimeOut = true;
							if (!isFinishing()) {
								new GeneralAlertDialogFragment(BaseHomeActivity.this)
								.setTitle(R.string.PrintHub_ErrorTitle)
								.setMessage(R.string.PrintHub_ErrorDescription)
								.setPositiveButton(R.string.Common_OK, null)
								.show(getSupportFragmentManager(), "print hub connect error");
							}
						}
					}
				});
			}
		};
		timeOutTask.start();
		
		KodakPrintPlace kpp = new KodakPrintPlace(this, new IKPPEventsListener() {
			
			@Override
			public void onPrintHubFound() {
				Log.i(TAG, "Print Hub Found");
				PrintHubManager.getInstance().getKodakPrintPlace().getPrinterInformation();
			}
			
			@Override
			public void onPrintHubEvents(EPrintHubEventId event) {
				
			}
		});
		
		PrintHubManager.getInstance().setKodakPrintPlace(kpp);
		PrintHubManager.getInstance().getKodakPrintPlace().setCommandCallback(new SimpleKPPCommandCallback() {
			@Override
			public void onGetPrinterInfoDone(PrinterInfo printer) {
				super.onGetPrinterInfoDone(printer);
				Log.i(TAG, "onGetPrinterInfoDone");
				if (mReceiveGetPrinterInfoDone) {// this msg will receive twice, don't know why, so add this flag
					return;
				}
				
				mReceiveGetPrinterInfoDone = true;
				if (timeOutTask.isAlive()) {
					mConnectFinished = true;//this will also dismiss dialog
					timeOutTask.interrupt();
				}
				if (target != null && !mPrintHubConnectTimeOut) {
					KM2Application.getInstance().setFlowType(FlowType.PRINT_HUB);
					Intent intent = new Intent(BaseHomeActivity.this, target);
					startActivity(intent);
					finish();
				}
			}
			
			@Override
			public void onGetJobInfoFailed(String errMsg) {
				super.onGetJobInfoFailed(errMsg);
				Log.i(TAG, "onGetJobInfoFailed:" + errMsg);
				if (timeOutTask.isAlive()) {
					mConnectFinished = true;//this will also dismiss dialog
					timeOutTask.interrupt();
				}
			}
			
		});
		kpp.searchPrintHub();
	}

	private boolean isExist() {
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CAPTURE_APK_NAME);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}
	
	
	@Override
	public void onBackPressed() {
		if (ShoppingCartManager.getInstance().hasItemsInCartCumulus()) {
			//disable back button
//			new GeneralAlertDialogFragment(this)
//				.setTitle(R.string.ShoppingCart_start_over)
//				.setMessage(R.string.ShoppingCart_start_over_prompts)
//				.setNegativeButton(R.string.Common_Cancel, null)
//				.setPositiveButton(R.string.Common_OK, new BaseGeneralAlertDialogFragment.OnClickListener() {
//					
//					@Override
//					public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
//						AppManager.getInstance().exitApp();
//					}
//				})
//				.show(getSupportFragmentManager(), "");
		} else {
			super.onBackPressed();
		}
	}
}
