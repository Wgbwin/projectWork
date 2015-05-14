package com.kodak.rss.tablet.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.LocalCustomerInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.content.SearchStarterCategory;
import com.kodak.rss.core.n2r.bean.project.Project;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.ProductDescription;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.core.n2r.webservice.GreetingCardWebService;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.ConnectionUtil;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.AppManager;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.MainMenuAdapter;
import com.kodak.rss.tablet.bean.MainMenuItem;
import com.kodak.rss.tablet.thread.PrepareBaseInfoTask;
import com.kodak.rss.tablet.thread.SendingOrderTask;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.view.dialog.DialogCountrySelector;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

/**
 * purpose: home page for tablet. 
 * @author bigtotoro
 */
public class MainActivity extends BaseNetActivity implements OnClickListener {
	private String TAG = "MainActivity";
	
	private GridView gvMainMenu;
	private MainMenuAdapter mainMenuAdapter;

	private InfoDialog waitingDialog;
	private InfoDialog getSupportDataDialog;
	
	private static final int HANDLER_PRINTS = 2;
	private static final int HANDLER_PHOTOBOOK = 3;
	private static final int HANDLER_COUNTRY_TASK =5;
	private static final int HANDLER_MY_PROJECTS =6;
	private static final int HANDLER_MY_CARDS =7;
	private static final int HANDLER_ERROR = 8;
	private static final int HANDLER_CALENDARS = 9;
	private static final int HANDLER_COLLAGE = 10;
	private static final int HANDLER_GET_SUPPORT_DATA = 11;
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Log.e(TAG, "handler msg:" + msg.what);
			if(isFinishing()){
				return;
			}
			Intent intent = null;
			
			if(waitingDialog!=null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}
			if(getSupportDataDialog!=null && getSupportDataDialog.isShowing()){
				getSupportDataDialog.dismiss();
			}
			
			switch (msg.what) {
			case HANDLER_PRINTS:
				if(showNoProductsRegionWarning(AppConstants.printType, "", R.string.l_print)){
					return;
				}
				intent = new Intent(MainActivity.this,PrintsActivity.class);
				startActivity(intent);
				break;
			case HANDLER_PHOTOBOOK:
				if(showNoProductsRegionWarning(AppConstants.bookType, "", R.string.l_book)){
					return;
				}
				intent = new Intent(MainActivity.this,PhotobookSelectionActivity.class);
				startActivity(intent);
				break;
			case HANDLER_MY_PROJECTS:
				intent = new Intent(MainActivity.this,MyProjectsActivity.class);
				startActivity(intent);
				break;
			case HANDLER_MY_CARDS:
				if(showNoProductsRegionWarning(AppConstants.cardType_GC, AppConstants.cardType_DMG, R.string.l_card)){
					return;
				}
				intent = new Intent(MainActivity.this,GCSSCategorySelectActivity.class);
				startActivity(intent);
				break;
				
			case HANDLER_CALENDARS:	
				intent = new Intent(MainActivity.this,CalendarSelectionActivity.class);
				startActivity(intent);
				break;	
				
			case HANDLER_COLLAGE:
				intent = new Intent(MainActivity.this,CollageSelectionActivity.class);
				startActivity(intent);
				break;	
				
			case HANDLER_COUNTRY_TASK:
				MainMenuItem target = (MainMenuItem) msg.obj;
				showCountryDialog(target);
				break;
			case HANDLER_ERROR:
				showErrorWarning((RssWebServiceException) msg.obj);
				break;
			case HANDLER_GET_SUPPORT_DATA:	
				mainMenuAdapter = new MainMenuAdapter(MainActivity.this, getApp().getCatalogList());
				gvMainMenu.setAdapter(mainMenuAdapter);				
				break;					
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main_menu);
		
		gvMainMenu = (GridView) findViewById(R.id.grid_main_menu);

		mainMenuAdapter = new MainMenuAdapter(this, getApp().getCatalogList());
		gvMainMenu.setAdapter(mainMenuAdapter);
		gvMainMenu.setOnItemClickListener(onItemSelectListener);
			
		setActionBarEvents();	
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		if(waitingDialog == null){
			waitingDialog = new InfoDialog.Builder(this).setMessage(R.string.Task_GettingProducts).setProgressBar(true).create();
		}					
		//RSSMIBOLEPDC-1840 auto connect to kiosk
		if (ConnectionUtil.isConnectedKioskWifi(this)) {
			startActivity(new Intent(this, WiFiSelectWorkflowActivity.class));
		} 
		
		getSupportProductDatas();
	}
	
	@Override
	public void judgeHaveItems(){
		if (judgeSelectHavedProductInfo()) {			
			Intent mIntent = new Intent(this, ShoppingCartActivity.class);
			startActivity(mIntent);			
		}else {
			popNoItemDialog();
		}		
	}	
	
	private OnItemClickListener onItemSelectListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
			MainMenuItem item = mainMenuAdapter.getItem(position);
			
			HashMap<String,String> localyticsMap = new HashMap<String, String>();
			switch (item.getProductType()) {
			case MainMenuItem.TYPE_MY_PROJECTS:
				doNavigateButton(item);
				break;
			case MainMenuItem.TYPE_CALENDARS:
				localyticsMap.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_WORKFLOW_TYPE, RSSTabletLocalytics.LOCALYTICS_VALUE_WORKFLOW_CALENDARS);
				doNavigateButton(item);
				break;
			case MainMenuItem.TYPE_COLLAGE:
				localyticsMap.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_WORKFLOW_TYPE, RSSTabletLocalytics.LOCALYTICS_VALUE_WORKFLOW_COLLAGES);
				doNavigateButton(item);				
				break;
			case MainMenuItem.TYPE_GREETING_CARDS:
				localyticsMap.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_WORKFLOW_TYPE, RSSTabletLocalytics.LOCALYTICS_VALUE_WORKFLOW_GREETING_CARDS);
				doNavigateButton(item);
				break;
			case MainMenuItem.TYPE_PHOTOBOOK:
				localyticsMap.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_WORKFLOW_TYPE, RSSTabletLocalytics.LOCALYTICS_VALUE_WORKFLOW_PHOTOBOOKS);
				doNavigateButton(item);
				break;
			case MainMenuItem.TYPE_PRINT:
				localyticsMap.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_WORKFLOW_TYPE, RSSTabletLocalytics.LOCALYTICS_VALUE_WORKFLOW_PRINTS);
				doNavigateButton(item);
				break;
			case MainMenuItem.TYPE_KIOSK:
				localyticsMap.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_WORKFLOW_TYPE, RSSTabletLocalytics.LOCALYTICS_VALUE_WORKFLOW_KIOSK);
				startActivity(new Intent(MainActivity.this, WiFiSelectWorkflowActivity.class));
				break;
			}
			
			if (!localyticsMap.isEmpty()) {
				RSSLocalytics.recordLocalyticsEvents(MainActivity.this, RSSTabletLocalytics.LOCALYTICS_EVENT_WORKFLOW_SELECTED, localyticsMap);
			}
			
		}		
	};	
	
	/**
	 * This popup will be displayed any time the user starts the N2R work flow
	 * when Wi-Fi is not running. it will be shown every time unless "don't ask"
	 * is selected
	 */
	private void showNoWifiConnectionDialog(final MainMenuItem mainMenuItem){
		new InfoDialog.Builder(this)
			.setMessage(R.string.nowificonnection)
			.setPositiveButton(R.string.donotaskagain, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferrenceUtil.saveNeedShowCellularDataWarning(MainActivity.this, false);
					RssTabletApp.getInstance().setNeedShowCellularDataWarning(false);
					doNaigateButtonAfterCheckConnection(mainMenuItem);
				}
			})
			.setNegativeButton(R.string.d_ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					doNaigateButtonAfterCheckConnection(mainMenuItem);
				}
			})
			.create()
			.show();
	}
	

	public void doNavigateButton(final MainMenuItem mainMenuItem){
		Log.d(TAG, " doNavigateButtion ");
		if(!ConnectionUtil.isConnected(MainActivity.this)){		
			new InfoDialog.Builder(this).setMessage(R.string.TitlePage_Error_NoInternet)
			.setPositiveButton(R.string.d_ok, null)	
			.create().show();					
			return;
		}
		
		if(RssTabletApp.getInstance().isNeedShowCellularDataWarning() && ConnectionUtil.isConnectedCellular(MainActivity.this)){
			showNoWifiConnectionDialog(mainMenuItem);					
			return;
		}
		
		doNaigateButtonAfterCheckConnection(mainMenuItem);
		
	}
	
	public void doNaigateButtonAfterCheckConnection(final MainMenuItem mainMenuItem){
		doNavigateButtionAfterBaseInfoPrepared(mainMenuItem);		
	}
	
	private void doNavigateButtionAfterBaseInfoPrepared(final MainMenuItem mainMenuItem){
		String countryCode = RssTabletApp.getInstance().getCountrycodeCurrentUsed();
		if(countryCode == null){
			countryCode = RssTabletApp.getInstance().getDefaultCountryCode();
		}
		
		if(!RssTabletApp.getInstance().isCountryCodeValid(countryCode)){
			Log.i(TAG,"isCurrentCountryCodeValid false ... ");
			Log.i(TAG,"RssTabletApp.getInstance().countries false ... "+RssTabletApp.getInstance().getCountries());
			if(RssTabletApp.getInstance().getCountries()!=null && RssTabletApp.getInstance().getCountries().size()!=0){
				waitingDialog.dismiss();
				showCountryDialog(mainMenuItem);
			}else{				
				waitingDialog.show();			
				new PrepareBaseInfoTask(this,new PrepareBaseInfoTask.OnCompleteListener() {
					
					@Override
					public void onSucceed() {
						Message message = mHandler.obtainMessage();
						message.what = HANDLER_COUNTRY_TASK;
						message.obj = mainMenuItem;
						mHandler.sendMessage(message);
					}
					
					@Override
					public void onFailed(final RssWebServiceException e) {
						mHandler.obtainMessage(HANDLER_ERROR, e).sendToTarget();
					}
				}).start();
				
			}
		}else{
			RssTabletApp.getInstance().setCountryCodeCurrentUsed(countryCode);
			waitForTask(mainMenuItem);
		}
	}
	
	/**
	 * show people a progress dialog when he click the button by the bottom.
	 * @param target
	 */
	private void waitForTask(final MainMenuItem mainMenuItem){
		final int type = mainMenuItem.getProductType();
		if (type <= 0) return;
		if (type == MainMenuItem.TYPE_PRINT || type == MainMenuItem.TYPE_GREETING_CARDS
				|| type == MainMenuItem.TYPE_PHOTOBOOK || type == MainMenuItem.TYPE_MY_PROJECTS 
				|| type == MainMenuItem.TYPE_CALENDARS || type == MainMenuItem.TYPE_COLLAGE	){
			waitingDialog.show();
			/*
			 *  fixed for RSSMOBILEPDC-2126 by song
			 *  get the country info by current used.
			 */
			new Thread(new Runnable() {
				public void run() {
					RssTabletApp app = RssTabletApp.getInstance();
					WebService webService = new WebService(MainActivity.this);	
					try {
						webService.getCountryInfoTask(app.getCountrycodeCurrentUsed());
					} catch (RssWebServiceException e1) {
						e1.printStackTrace();
					}	
					if(type == MainMenuItem.TYPE_GREETING_CARDS || type == MainMenuItem.TYPE_MY_PROJECTS || 
							(app.getCatalogList() ==null || app.getCatalogList().size()==0) || app.getRetailers() == null || app.getColorEffectList() == null){
	//						if(app.getCountryInfoList()==null){
	//							HashMap<String, String> countries = app.getCountries();
	//							String codes = "";
	//							if(countries != null){
	//								Iterator<String> iter = countries.keySet().iterator();
	//								while(iter.hasNext()){
	//									codes += iter.next().toString();
	//									if(iter.hasNext()){
	//										codes += ",";
	//									}
	//								}
	//							}											
	//							try {
	//								webService.getCountryInfoTask(codes);
	//							} catch (RssWebServiceException e) {
	//								e.printStackTrace();
	//								mHandler.obtainMessage(HANDLER_ERROR, e).sendToTarget();
	//								return ;
	//							}							    
	//						}
						
						if (getSupportData(webService)) return;	
						
						if(app.getRetailers() == null){
							try {
								webService.getRetailersTask();
							} catch (RssWebServiceException e) {
								e.printStackTrace();
								mHandler.obtainMessage(HANDLER_ERROR, e).sendToTarget();
								return;
							}
						}
						
						if(app.getColorEffectList() == null){
							try {
								webService.getAvailableColorEffect2Task();
							} catch (RssWebServiceException e) {
								e.printStackTrace();
								mHandler.obtainMessage(HANDLER_ERROR, e).sendToTarget();
								return;
							}
						}
						
						if(type == MainMenuItem.TYPE_PRINT){
							mHandler.sendEmptyMessage(HANDLER_PRINTS);
						} else if(type == MainMenuItem.TYPE_PHOTOBOOK){
							mHandler.sendEmptyMessage(HANDLER_PHOTOBOOK);
						} else if(type == MainMenuItem.TYPE_CALENDARS){
							mHandler.sendEmptyMessage(HANDLER_CALENDARS);
						} else if(type == MainMenuItem.TYPE_COLLAGE){
							mHandler.sendEmptyMessage(HANDLER_COLLAGE);
						} else if (type == MainMenuItem.TYPE_MY_PROJECTS) {
							//get The facebook Id userName 						
							List<Project> projects = null;
							try {
								projects = webService.getProjectsTask();
							} catch (RssWebServiceException e) {
								e.printStackTrace();
								mHandler.obtainMessage(HANDLER_ERROR, e).sendToTarget();
								return;
							}
							List <Project> projectList = null;
							if (projects != null) {
								int size = projects.size();
								projectList = new ArrayList<Project>(size);
								for (int i = size-1; i >= 0 ; i--) {
									projectList.add(projects.get(i));				    			
								}
							}
							app.projects = projectList;
							mHandler.sendEmptyMessage(HANDLER_MY_PROJECTS);						
						}else if (type == MainMenuItem.TYPE_GREETING_CARDS){
							GreetingCardWebService gcWebService = new GreetingCardWebService(MainActivity.this);							
							List<SearchStarterCategory> sSCategoryList = null;
							String proDesIds = "";
							List<Catalog> catalogs = app.getCatalogList();
							if (catalogs != null) {
								for (Catalog catalog : catalogs) {
									if (catalog == null) continue;
									if (catalog.rssEntries == null) continue;	
									for (RssEntry rssEntry : catalog.rssEntries) {	
										if (rssEntry == null) continue;
										if (rssEntry.proDescription == null) continue;
										String type = rssEntry.proDescription.type;
										if (type == null) continue;
										if (ProductDescription.GREETINGCARDS.equals(type) || ProductDescription.DUPLEXMYGREETING.equals(type)) {
											String proId = rssEntry.proDescription.id;
											if (proId != null && !"".equals(proId)) {
												proDesIds += proId +",";
											}											
										}										
									}	
								}	
							}
							if (proDesIds.endsWith(",")) {
								int end = proDesIds.lastIndexOf(",");
								proDesIds = proDesIds.substring(0, end);
							}
							
							try {
								sSCategoryList = gcWebService.getGreetingCardsCategorizedTask(proDesIds);
							} catch (RssWebServiceException e) {
								e.printStackTrace();
								mHandler.obtainMessage(HANDLER_ERROR, e).sendToTarget();
								return;
							}							
							app.sSCategorys = sSCategoryList;
							mHandler.sendEmptyMessage(HANDLER_MY_CARDS);		
						}
					} 
					else{
						if(type == MainMenuItem.TYPE_PRINT){
							mHandler.sendEmptyMessage(HANDLER_PRINTS);
						} else if(type == MainMenuItem.TYPE_PHOTOBOOK){
							mHandler.sendEmptyMessage(HANDLER_PHOTOBOOK);
						}else if(type == MainMenuItem.TYPE_CALENDARS){
							mHandler.sendEmptyMessage(HANDLER_CALENDARS);
						}else if(type == MainMenuItem.TYPE_COLLAGE){
							mHandler.sendEmptyMessage(HANDLER_COLLAGE);
						}
					}
				}
			}).start();
		}		
	}
	
	private void getSupportProductDatas(){
		RssTabletApp app = RssTabletApp.getInstance();
		
		if (app.appForbidden) {
			return;
		}
		
		if (app.isUseDoMore && app.skipMainFromShoppingCart) {
			app.skipMainFromShoppingCart = false;
			app.clearCatalogList();
		}
		if(!ConnectionUtil.isConnected(MainActivity.this)){		
			new InfoDialog.Builder(this).setMessage(R.string.TitlePage_Error_NoInternet)
			.setPositiveButton(R.string.d_ok, null)	
			.create().show();					
			return;
		}
		if((app.getCatalogList() == null || app.getCatalogList().size()==0) ){			
			if(getSupportDataDialog == null){
				getSupportDataDialog = new InfoDialog.Builder(this).setMessage(R.string.Common_Wait).setProgressBar(true).create();
			}	
			getSupportDataDialog.show();
			new Thread(new Runnable() {
				public void run() {
					WebService webService = new WebService(MainActivity.this);
					if (getSupportData(webService)) return;		
					mHandler.obtainMessage(HANDLER_GET_SUPPORT_DATA).sendToTarget();
				}
			}).start();
		}
	}
	
	private boolean  getSupportData(WebService webService){
		boolean isStop = true;
		RssTabletApp app = RssTabletApp.getInstance();					
				
		if(app.getCatalogList() == null || app.getCatalogList().size()==0){
			String products = getString(R.string.cumulus_support_products);
			if(!app.isUseDoMore){
				try {
					webService.getMSRPCatalog3Task(products, null, null);
				} catch (RssWebServiceException e) {
					e.printStackTrace();
					mHandler.obtainMessage(HANDLER_ERROR, e).sendToTarget();
					return isStop;
				}
			} else {
				int orderType = app.orderType;
				if(orderType == SendingOrderTask.ORDER_TYPE_HOME){
					try {
						webService.getMSRPCatalog3Task(products, app.getHomeDeliveryRetailerId(app.getRetailers()), null);
					} catch (RssWebServiceException e) {
						e.printStackTrace();
						mHandler.obtainMessage(HANDLER_ERROR, e).sendToTarget();
						return isStop;
					}
				} else if(orderType == SendingOrderTask.ORDER_TYPE_STORE){
					StoreInfo store = StoreInfo.loadSelectedStore(MainActivity.this);
					if(store != null){
						try {
							webService.getMSRPCatalog3Task(products, store.retailerID, store.id);
						} catch (RssWebServiceException e) {
							e.printStackTrace();
							mHandler.obtainMessage(HANDLER_ERROR, e).sendToTarget();
							return isStop;
						}
					} else {
						Log.e(TAG, "Is there any store selected?");
					}
				}
					
				//change by bing for some the store price	
				int size;
				List<Catalog> catalogs = app.getCatalogList();
				if(catalogs != null && app.products != null && (size = catalogs.size())>0){																		
					for (ProductInfo pInfo : app.products) {
						if (pInfo == null) continue;
						if (pInfo.descriptionId == null) continue;
						if (pInfo.chosenImageList == null) continue;
						if (pInfo.chosenImageList.size() == 0) continue;
						if (!AppConstants.printType.equals(pInfo.productType)) continue;																							
						ImageInfo imageInfo = pInfo.chosenImageList.get(0);
						if (imageInfo == null) continue;
						if (imageInfo.typeMap == null) continue;
						List <ProductInfo> productInfoList = imageInfo.typeMap.get(AppConstants.printType);
						if (productInfoList == null) continue;						
						List <ProductInfo> haveList = new ArrayList<ProductInfo>(productInfoList.size());						
						for (ProductInfo iPInfo : productInfoList) {
							if (iPInfo == null) continue;
							if (iPInfo.descriptionId == null) continue;
							for (int i = 0; i < size; i++) {
								Catalog catalog = catalogs.get(i);
								if (catalog == null) continue;
								if (catalog.rssEntries == null) continue;																													
								for (int j = 0; j < catalog.rssEntries.size(); j++) {
									RssEntry rssEntry = catalog.rssEntries.get(j);
									if (rssEntry == null) continue;
									if (rssEntry.proDescription == null) continue;											
									if (rssEntry.proDescription.id == null) continue;	
									if (rssEntry.proDescription.type == null) continue;											
									if (!AppConstants.printType.equalsIgnoreCase(rssEntry.proDescription.type.trim())) continue;
									if (rssEntry.maxUnitPrice == null) continue;
									if (rssEntry.maxUnitPrice.priceStr == null) continue;
									if (iPInfo.descriptionId.equals(rssEntry.proDescription.id)){
										iPInfo.price = rssEntry.maxUnitPrice.priceStr;	
										haveList.add(iPInfo);
									}																																																																																								
								}
							}								
						}
						imageInfo.typeMap.clear();
						imageInfo.typeMap.put(AppConstants.printType, haveList);	
					}																	
				}																			
			}
		}		
		return false;
	}
	
	private boolean isBaseInfoPrepared(){
		String token = SharedPreferrenceUtil.authorizationToken(this);
		return !"".equals(token) && RssTabletApp.getInstance().getCountries()!=null && RssTabletApp.getInstance().getCountries().size()!=0;
	}
	
	private void showCountryDialog(final MainMenuItem mainMenuItem){
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
		Iterator iter = RssTabletApp.getInstance().getCountries().entrySet().iterator();   
        while (iter != null && iter.hasNext()) {  
        	Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();            
            currentCountryCode = entry.getKey();  
            if (currentCountryCode != null) break;
        }  

		new DialogCountrySelector().initCountrySelectorMessage(MainActivity.this, propmtStr,
				getResources().getString(R.string.d_ok),RssTabletApp.getInstance().getCountries(), currentCountryCode, new DialogCountrySelector.onDialogErrorListener() {
			public void onYes(String countryName, String countryCode, String oriCountryName, String oriCountryCode) {
				if(!oriCountryCode.equals(countryCode)){
					RssTabletApp app = RssTabletApp.getInstance();
					LocalCustomerInfo cus = new LocalCustomerInfo(MainActivity.this);
					if(cus != null){
						cus.setShipState("");
						cus.save(MainActivity.this);
						app.clearLastCountryData();
					}
					StoreInfo.clearSelectedStore(MainActivity.this);
				}

				SharedPreferrenceUtil.saveSelectedCountryCode(MainActivity.this, countryCode);
				RssTabletApp.getInstance().setCountryCodeCurrentUsed(countryCode);
				waitForTask(mainMenuItem);
			}
			
			public void onDismiss(){
				
			}
		});
	}
	
	private boolean showNoProductsRegionWarning(String prodcutType1, String productType2, int proNameRes){
		if(!RssTabletApp.getInstance().isProductAvaible(prodcutType1, productType2)){		
			String message ="";
			// change by bing for 1804
			StoreInfo store = StoreInfo.loadSelectedStore(MainActivity.this);
			if (store == null) {
				message = getString(R.string.TitlePage_Error_NoProductsRegion);				
				String localLanguage = Locale.getDefault().toString();
				if (Locale.FRANCE.getLanguage() != null && localLanguage != null && Locale.FRANCE.getLanguage().equalsIgnoreCase(localLanguage)) {
					message = message.replaceFirst("%%", getString(proNameRes));					
				}else {
					message = message.replaceFirst("%%", getString(proNameRes));
					message = message.replaceFirst("%%", RssTabletApp.getInstance().getCountryNameCurrentUsed());	
				}
			}else if(RssTabletApp.getInstance().orderType == SendingOrderTask.ORDER_TYPE_HOME){
				message = getString(R.string.TitlePage_Error_NoProductsHomeDelivery);
				message = message.replaceFirst("%%", getString(proNameRes));				
			}else {
				message = getString(R.string.TitlePage_Error_NoProductsRetailer);
				message = message.replaceFirst("%%", getString(proNameRes));				
			}
			
			new InfoDialog.Builder(MainActivity.this)
			.setCancelable(false)
			.setCanceledOnTouchOutside(false)
			.setMessage(message)
			.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).create().show();
			return true;
		}
		return false;
	}
	
	@Override
	public void onBackPressed() {
		AppManager.getInstance().goToStartupActivity();
	}
}
