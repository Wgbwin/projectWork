package com.kodak.rss.tablet.activities;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kodak.rss.core.bean.LocalCustomerInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.core.n2r.webservice.LocationService;
import com.kodak.rss.core.n2r.webservice.LocationService.OnLocationChangedListener;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.ConnectionUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.core.util.StringUtils;
import com.kodak.rss.tablet.AppManager;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.bean.SideMenuItem;
import com.kodak.rss.tablet.thread.PrepareBaseInfoTask;
import com.kodak.rss.tablet.view.PopImageLayout;
import com.kodak.rss.tablet.view.RainPicView;
import com.kodak.rss.tablet.view.RainPicView.RainImage;
import com.kodak.rss.tablet.view.dialog.DialogCountrySelector;
import com.kodak.rss.tablet.view.dialog.DialogEulaAndPrivate;
import com.kodak.rss.tablet.view.dialog.DialogEulaAndPrivate.onDialogEulaListener;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class StartupActivity extends BaseNetActivity{
	private RainPicView mRainPicView;
	private TextView mTvStart;
	private ImageView mIvStart;
	private View mViewStart;
	private PopImageLayout mPopImageLayout;
	private DialogEulaAndPrivate mDialogAggrement;
	
	private LocationService mLocationService;
	private PrepareBaseInfoTask mPrepareBaseInfoTask;
	
	private RssWebServiceException mExceptionWhenGettingInfo;
	private InfoDialog mDialogError;
	private boolean mCanceled = false;
	
	private static final int HANDLER_UPDATE_START_BUTTON = 1;
	private static final int HANDLER_SHOW_COUNTRY_DIALOG = 2;
	private static final int HANDLER_ERROR = 3;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (isFinishing()) {
				return;
			}
			
			switch (msg.what) {
			case HANDLER_UPDATE_START_BUTTON:
				updateStartButton(isInfoPrepared());
				break;
			case HANDLER_SHOW_COUNTRY_DIALOG:
				showSelectCountryDialog();
				break;
			case HANDLER_ERROR:
				mExceptionWhenGettingInfo = (RssWebServiceException) msg.obj;
				showErrorWarning(mExceptionWhenGettingInfo);
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		
		setupViews(savedInstanceState);
		setupData(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mRainPicView.start();
		
		if (getApp().getCountrycodeCurrentUsed() == null) {
			if (mLocationService == null) {
				mLocationService = new LocationService(this);
			}
			mLocationService.registerLocationProvider(mLocationChangedListener);
		}
		
		updateStartButton(isInfoPrepared());
		
		if (ConnectionUtil.isConnectedKioskWifi(this)) {
			startActivity(new Intent(this, WiFiSelectWorkflowActivity.class));
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mRainPicView.stop();
	}
	
	private void setupViews(Bundle savedInstanceState) {
		mRainPicView = (RainPicView) findViewById(R.id.rainView);
		mTvStart = (TextView) findViewById(R.id.tv_start);
		mViewStart = findViewById(R.id.view_start);
		mIvStart = (ImageView) findViewById(R.id.iv_start);
		mPopImageLayout = (PopImageLayout) findViewById(R.id.show_image);
		
		removeMenuItem(SideMenuItem.ITEM_HOME);
		
		mViewStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isInfoPrepared()) {
					Intent intent = new Intent(StartupActivity.this, MainActivity.class);
					startActivity(intent);
					//finish this activty so if user come back to this activity, app can re-get info
					finish();
				}
			}
		});
		
		mRainPicView.setOnItemClickListener(new RainPicView.OnItemClickListener() {
			
			@Override
			public void onItemClick(RainPicView view, RainImage image) {
				mPopImageLayout.popImageView(image);
			}
		});
	}
	
	private void setupData(Bundle savedInstanceState) {
		boolean prepared = isInfoPrepared();
		if (prepared) {
			updateStartButton(prepared);
			return;
		}
		
		//check Agreement
		if (!SharedPreferrenceUtil.getBoolean(this, SharedPreferrenceUtil.SBELUA_ACCEPTED)) {
			if (mDialogAggrement == null) {
				mDialogAggrement = new DialogEulaAndPrivate();
			}
			
			if (!mDialogAggrement.isShowing()) {
				mDialogAggrement.craeteDialog(this, RssTabletApp.getInstance().getEulaURL(), getResources().getString(R.string.EULAScreen_Title),getResources().getString(R.string.N2RShoppingCart_ConfirmEula),getResources().getString(R.string.d_yes),getResources().getString(R.string.d_no),new onDialogEulaListener() {
					@Override
					public void onYes() {
						SharedPreferrenceUtil.setBoolean(StartupActivity.this, SharedPreferrenceUtil.SBELUA_ACCEPTED, true);
						doAfterAcceptAgreement();
					}
					
					@Override
					public void onNo() {
					}
				});
			}
			return;
		} else {
			doAfterAcceptAgreement();
		}
	}
	
	private boolean isInfoPrepared() {
		return (getApp().getCatalogList() != null && getApp().getCatalogList().size() != 0) || mCanceled ;
	}
	
	private void updateStartButton(boolean prepared) {
		if (prepared) {
			mIvStart.setVisibility(View.VISIBLE);
			mTvStart.setText(R.string.startup_get_start);
		} else {
			mIvStart.setVisibility(View.INVISIBLE);
			mTvStart.setText(R.string.startup_getting_products);
		}
	}
	
	private boolean isContryListAndTokenGetted() {
		String token = SharedPreferrenceUtil.authorizationToken(this);
		return !"".equals(token) && RssTabletApp.getInstance().getCountries()!=null && RssTabletApp.getInstance().getCountries().size()!=0;
	}
	
	private void doAfterAcceptAgreement() {
		if (!isContryListAndTokenGetted()) {
			mPrepareBaseInfoTask = new PrepareBaseInfoTask(this, new PrepareBaseInfoTask.OnCompleteListener() {
				
				@Override
				public void onSucceed() {
					doAfterContryListGet();
				}
				
				@Override
				public void onFailed(RssWebServiceException e) {
					e.printStackTrace();
					mHandler.obtainMessage(HANDLER_ERROR, e).sendToTarget();
				}
			});
			mPrepareBaseInfoTask.start();
		} else {
			doAfterContryListGet();
		}
	}
	
	private void doAfterContryListGet() {
		String countryCode = getApp().getCountrycodeCurrentUsed();
		if (StringUtils.isEmpty(countryCode)) {
			countryCode = getApp().getDefaultCountryCode();
		}
		
		if (getApp().isCountryCodeValid(countryCode)) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					if (StringUtils.isEmpty(getApp().getCountrycodeCurrentUsed())) {
						getApp().setCountryCodeCurrentUsed(getApp().getDefaultCountryCode());
					}
					WebService ws = new WebService(StartupActivity.this);
					try {
						ws.getMSRPCatalog3Task(getString(R.string.cumulus_support_products), null, null);
						mHandler.obtainMessage(HANDLER_UPDATE_START_BUTTON).sendToTarget();
					} catch (RssWebServiceException e) {
						e.printStackTrace();
						mHandler.obtainMessage(HANDLER_ERROR, e).sendToTarget();
					}
					
				}
			}).start();
		} else {
			mHandler.obtainMessage(HANDLER_SHOW_COUNTRY_DIALOG).sendToTarget();
		}
	}
	
	private void showSelectCountryDialog() {
		if(RssTabletApp.getInstance().getCountries()==null)return;
		if(RssTabletApp.getInstance().getCountries().size() == 0)return;
	
		String currentCountryCode = SharedPreferrenceUtil.currentCountryCode(this);
		if("".equals(currentCountryCode)){
			currentCountryCode = SharedPreferrenceUtil.selectedCountryCode(this);
		}
		
		/**
		 * change by bing on 2014-9-1 for RSSMOBILEPDC-1808 
		 * 1. location country and the result server list have   
		 * 2. location country and the result server list not have 
		 * 3. not location country and the result server list have 
		 */ 
		String propmtStr = "";
		if ("".equals(currentCountryCode)) {
			propmtStr = getResources().getString(R.string.titlepage_error_location_not_determined);
		}else {
			propmtStr = getResources().getString(R.string.TitlePage_Error_No_Products_In_Country);			
		}
		Iterator<Map.Entry<String, String>> iter = RssTabletApp.getInstance().getCountries().entrySet().iterator();   
        while (iter != null && iter.hasNext()) {  
        	Map.Entry<String, String> entry = iter.next();            
            currentCountryCode = entry.getKey();  
            if (currentCountryCode != null) break;
        }  

		new DialogCountrySelector().initCountrySelectorMessage(StartupActivity.this, propmtStr,
				getResources().getString(R.string.d_ok),RssTabletApp.getInstance().getCountries(), currentCountryCode, new DialogCountrySelector.onDialogErrorListener() {
			public void onYes(String countryName, String countryCode, String oriCountryName, String oriCountryCode) {
				if(!oriCountryCode.equals(countryCode)){
					RssTabletApp app = RssTabletApp.getInstance();
					LocalCustomerInfo cus = new LocalCustomerInfo(StartupActivity.this);
					if(cus != null){
						cus.setShipState("");
						cus.save(StartupActivity.this);
						app.clearLastCountryData();
					}
					StoreInfo.clearSelectedStore(StartupActivity.this);
				}

				SharedPreferrenceUtil.saveSelectedCountryCode(StartupActivity.this, countryCode);
				RssTabletApp.getInstance().setCountryCodeCurrentUsed(countryCode);
				doAfterContryListGet();
			}
			
			public void onDismiss(){
				
			}
		});
	}
	
	private OnLocationChangedListener mLocationChangedListener = new OnLocationChangedListener() {
		final String TAG = "LocationListener";
		@Override
		public void onLocationChanged(Location location) {
			Log.d("Location","onLocationChanged Latitude" + location.getLatitude()+" Longitude "+location.getLongitude());
			final int latitude = (int) location.getLatitude();
			final int longitude = (int) location.getLongitude();
			new Thread(new Runnable() {
				public void run() {
					Geocoder geoCoder = new Geocoder(StartupActivity.this);
					List<Address> list;
					try {
						list = geoCoder.getFromLocation(latitude, longitude, 2);
						for (int i = 0; i < list.size(); i++) {
							Address address = list.get(i);
							String countryCode = address.getCountryCode();
							Log.d(TAG, "onLocationChanged countryCode" + countryCode);
							SharedPreferrenceUtil.saveCurrentCountryCode(StartupActivity.this, countryCode);
							if (RssTabletApp.getInstance().isCountryCodeValid(countryCode) && RssTabletApp.getInstance().getCountrycodeCurrentUsed() == null) {
								RssTabletApp.getInstance().setCountryCodeCurrentUsed(countryCode);
								if (mLocationService != null) {
									mLocationService.unRegisterLocationProvider();
								}
							}
						}
					} catch (IOException e) {
						Log.e(TAG, e);
					}
				}
			}).start();
		}
	};
	
	@Override
	protected void doOnClickOkForAppObsoleteDialog(RssWebServiceException e) {
		mCanceled = true;
		updateStartButton(isInfoPrepared());
		
		super.doOnClickOkForAppObsoleteDialog(e);
	};
	
	@Override
	public void showErrorWarning(RssWebServiceException e) {
		if (e != null) {
			String errMsg = null;
			if (e.isNetworkWeak()) {
				errMsg = getString(R.string.error_cannot_connect_to_internet);
			} else if (e.isServerError()) {
				errMsg = getString(R.string.error_server);
			} else if (e.isAppObsolete() && !isFinishing()) {
				showAppObsoleteWarning(e);
			}
			
			if (errMsg != null) {
				mDialogError = new InfoDialog.Builder(this)
				.setMessage(errMsg)
				.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						doAfterAcceptAgreement();
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mCanceled = true;
						updateStartButton(isInfoPrepared());
					}
				})
				.setCancelable(false)
				.setCanceledOnTouchOutside(false)
				.create();
				
				if (!mDialogError.isShowing() && !isFinishing()) {
					mDialogError.show();
				}
			}
		}
	};
	
	private int tapBackCount = 0;
	private long firstTapTime = 0;
	@Override
	public void onBackPressed() {
		if(tapBackCount==0){
			tapBackCount ++;
			firstTapTime = System.currentTimeMillis();
			Toast.makeText(this, R.string.Common_Exit, Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					tapBackCount = 0;
				}
			}, 2000);
			return;
		}
		if(tapBackCount==1){
			if(System.currentTimeMillis()-firstTapTime<2000){
				AppManager.getInstance().exitApp();
			} else {
				tapBackCount = 0;
			}
		}
	}
	
	@Override
	protected boolean detectNetWork() {
		//Because there is error warning when get info from server failed,
		//we ignore this step to avoid two dialog(one error warning dialog and the other no network dialog) 
		return true;
	}
	
}
