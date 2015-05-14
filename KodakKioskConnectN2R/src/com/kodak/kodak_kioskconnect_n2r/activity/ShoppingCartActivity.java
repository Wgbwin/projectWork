package com.kodak.kodak_kioskconnect_n2r.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.example.android.bitmapfun.util.ImageCache.ImageCacheParams;
import com.example.android.bitmapfun.util.Utils;
import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.CouponsEditTestInputDialog;
import com.kodak.kodak_kioskconnect_n2r.HelpActivity;
import com.kodak.kodak_kioskconnect_n2r.ImageSelectionDatabase;
import com.kodak.kodak_kioskconnect_n2r.InfoDialog;
import com.kodak.kodak_kioskconnect_n2r.NewSettingActivity;
import com.kodak.kodak_kioskconnect_n2r.OrderSummaryWidget;
import com.kodak.kodak_kioskconnect_n2r.PictureUploadService2;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintMakerWebService;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.SendingOrderActivity;
import com.kodak.kodak_kioskconnect_n2r.ShareLoginActivity;
import com.kodak.kodak_kioskconnect_n2r.StoreFinder;
import com.kodak.kodak_kioskconnect_n2r.TopOrderHeadSummaryWidget;
import com.kodak.kodak_kioskconnect_n2r.adapter.ExpandableListAdapter;
import com.kodak.kodak_kioskconnect_n2r.adapter.ExpandableListAdapter.ExpandableListListener;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.retailer.Retailer;
import com.kodak.kodak_kioskconnect_n2r.bean.retailer.Retailer.CartLimit;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.kodak_kioskconnect_n2r.view.CouponInputDialog;
import com.kodak.shareapi.AccessTokenResponse;
import com.kodak.shareapi.ClientTokenResponse;
import com.kodak.shareapi.GalleryService;
import com.kodak.shareapi.TokenGetter;
import com.kodak.utils.RSSLocalytics;

@TargetApi(Build.VERSION_CODES.ECLAIR)
public class ShoppingCartActivity extends BaseActivity implements ExpandableListListener {
	private final String TAG = this.getClass().getSimpleName();
	private static final String IMAGE_CACHE_DIR = "shoppingcartimages";
	public static final String EXTRA_IMAGE = "extra_image";
	public static String PACKAGE_NAME;
	public static boolean isShoppingCardLive = false;
	private final String SCREEN_NAME = "Shopping Cart";
	private final String EVENT = "Shopping Cart";
	private final String EVENT_DOMORE = "Do More";
	private String productDescIds = "";
	private String selectedDeliveryName = "";
	private int selectedPosition = 0;
	private int deliveryModel = 0; // 1: pick up in store,2:Home Delivery
	public int image_column_index;
	public int count = 0;
	int selectedCount = 0;
	double outWidth = 0.0;
	double outHeight = 0.0;

	private boolean isCustomerInfoValid = false;
	private boolean isStoreSelected = false;
	private boolean isShippingAddressValid = false;
	private boolean isFirstGetRetailers = true;
	private boolean isContextFinished = false; //when the Activity finished the "isContextFinished" will be true;

	private List<String> deliveryLists;
	private List<Retailer> retailers = new ArrayList<Retailer>();
	private List<ProductInfo> productInfoList;
	private List<String> groupItemList = new ArrayList<String>();
	private List<List<ProductInfo>> childItemList = new ArrayList<List<ProductInfo>>();

	private Button next;
	private Button info;
	private Button settings;
	private Button delivery_button; // add by song
	private Button continue_btn;

	public Cursor imagecursor;
	private SharedPreferences prefs;
	private ImageSelectionDatabase mImageSelectionDatabase = null;
	private ExpandableListAdapter expandableListAdapter;
	private DeliveryAdapter adapter;
	private ListView lvDelivery;
	private View popContentView = null;
	private ExpandableListView expandableList;
	private PopupWindow popupWindow = null;
	private ProgressDialog dialog;
	private ProgressDialog dialogRetails;
	private CouponsEditTestInputDialog.EditTestInputDialogBuilder couponsBuilder;
	private AppContext appContex;
	
	private final String EVENT_SUMMARY = "Order Activity Summary";
	private final String KEY_USER_INFO_CHANGED = "User Info Changed";
	private final String KEY_SHIPPING_ADDRESS_CHANGED = "Shipping Address Changed";
	private final String KEY_COUPON_APPLIED = "Coupon Applied";
	private final String KEY_CART_EDIT = "Cart Edits";
	private final String KEY_SEND_EMAIL = "Send Emails";
	private final String KEY_QUANTITY_CHANGES = "Quantity Changes";
	private final String KEY_CART_REMOVALS = "Cart Removals";
	private final String KEY_DELIVERY = "Delivery";
	private final String VALUE_HOME = "Home";
	private final String VALUE_STORE = "Store";
	private final String VALUE_UNKNOWN = "Unknown";
	private final String VALUE_NO = "no";
	private static HashMap<String, String> attr;
	private static boolean needResetAttrValue = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.shoppingcart);
		setContentLayout(R.layout.shoppingcartfield);
		RSSLocalytics.onActivityCreate(this);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		RSSLocalytics.recordLocalyticsEvents(this, EVENT);
		getViews(); // update by song
		initData();
		setEvents(); // update by song
	}

	@Override
	public void initData() {
		appContex = AppContext.getApplication();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		ImageCacheParams cacheParams = new ImageCacheParams(IMAGE_CACHE_DIR);
		/*
		 * Allocate a third of the per-app memory limit to the bitmap memory
		 * cache. This value should be chosen carefully based on a number of
		 * factors. Refer to the corresponding Android Training class for more
		 * discussion: http://developer.android.com/training/displaying-bitmaps/
		 * In this case, we aren't using memory for much else other than this
		 * activity and the ImageDetailActivity so a third lets us keep all our
		 * sample image thumbnails in memory at once.
		 */
		cacheParams.memCacheSize = 1024 * 1024 * (Utils.getMemoryClass(this) / 3);
		// The ImageWorker takes care of loading images into our ImageView
		// children asynchronously
		mImageSelectionDatabase = new ImageSelectionDatabase(ShoppingCartActivity.this);
		mImageSelectionDatabase.open();
		PACKAGE_NAME = getApplicationContext().getPackageName();
		adapter = new DeliveryAdapter(this);
		initDeliveryLists(); // add by song
		expandableListAdapter = new ExpandableListAdapter(ShoppingCartActivity.this, appContex);
		expandableList.setAdapter(expandableListAdapter);
		initLocalyticsData();
	}

	private void initLocalyticsData(){
		if(needResetAttrValue){
			attr = new HashMap<String, String>();
			needResetAttrValue = false;
			PrintHelper.cartRemovals = 0;
			PrintHelper.quantityChanges = 0;
			attr.put(KEY_CART_EDIT, VALUE_NO);
			attr.put(KEY_CART_REMOVALS, "0");
			attr.put(KEY_DELIVERY, "");
			attr.put(KEY_QUANTITY_CHANGES, VALUE_UNKNOWN);
			attr.put(KEY_SEND_EMAIL, VALUE_NO);
			attr.put(KEY_SHIPPING_ADDRESS_CHANGED, VALUE_NO);
			attr.put(KEY_USER_INFO_CHANGED, VALUE_NO);
			attr.put(KEY_COUPON_APPLIED, VALUE_NO);
		}
	}
	
	@Override
	public void getViews() {
		expandableList = (ExpandableListView) findViewById(R.id.ShoppingCart_expandableList);
		popContentView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.popup_listview, null);
		next = (Button) findViewById(R.id.next_btn);
		next.setText(getResources().getString(R.string.buy));
		next.setVisibility(View.VISIBLE);
		info = (Button) findViewById(R.id.info_btn);
		settings = (Button) findViewById(R.id.settings_btn);
		delivery_button = (Button) findViewById(R.id.delivery_btn);
		lvDelivery = (ListView) popContentView.findViewById(R.id.delivery_list);
		continue_btn = (Button) findViewById(R.id.continue_btn);
		continue_btn.setVisibility(View.VISIBLE);
	}

	@Override
	public void setEvents() {
		settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearListData();
				Intent myIntent = new Intent(ShoppingCartActivity.this, NewSettingActivity.class);
				myIntent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
				Bundle b = new Bundle();
				b.putString(NewSettingActivity.SETTINGS_LOCATION, SCREEN_NAME);
				myIntent.putExtras(b);
				startActivity(myIntent);
			}
		});
		info.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearListData();
				Intent myIntent = new Intent(ShoppingCartActivity.this, HelpActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(HelpActivity.HELP_LOCATION, SCREEN_NAME);
				myIntent.putExtras(bundle);
				startActivity(myIntent);
			}
		});
		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (prefs.contains("selectedStoreName") && !prefs.getString("selectedStoreName", "").equals("")) {
					isStoreSelected = true;
				} else {
					isStoreSelected = false;
				}
				
				String currentRetailerId = AppContext.getApplication().getCurrentRetailerID() ;
				if(AppContext.getApplication().isInStoreCloud()){
					Intent intent = null;
					if(!isCustomerInfoValid){
						intent = new Intent(ShoppingCartActivity.this, NewSettingActivity.class);
						intent.putExtra("requireInfoEntry", true);
						startActivity(intent);
					} else {
						int count = 0;
						for (int i = 0; i < groupItemList.size(); i++) {
							for (int j = 0; j < childItemList.get(i).size(); j++) {
								count++;
							}
						}
						if (count > 0) {
							
							int maxPrice = prefs.getInt(prefs.getString("selectedRetailerId", "") + "maxPrice", 0);
							if (maxPrice == 0 || maxPrice > expandableListAdapter.subTotal) {
								InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
								builder.setTitle("");
								boolean isDM = PACKAGE_NAME.contains("dm");
								if (isDM) {
									builder.setMessage(R.string.place_your_order);
									builder.setPositiveButton(getString(R.string.buy), new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											clearListData();
											dialog.dismiss();
											checkStore();
										}
									});
									builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
										}
									});
									builder.create().show();
								} else {
									if (getApplicationContext().getPackageName().contains("wmc")
											&& prefs.getBoolean(PrintHelper.IS_AUTO_UPLOAD2ALBUMS, false)) {
										String email = prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "");
										if (!email.contains("@") || !email.contains(".") || email.equals("")
												|| PrintHelper.getAccessTokenResponse(getApplicationContext()).access_token.equals("")) {
											intent = new Intent(ShoppingCartActivity.this, ShareLoginActivity.class);
											intent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
											startActivity(intent);
										} else {
											new Thread(new CreateGallery()).start();
											checkStore();
										}
									} else {
										checkStore();
									}
								}

							} else {// total price out of limit
								try {
									PrintHelper.mTracker.trackEvent("Dialog", "Cost_Warning", "Total_Cost",
											Integer.parseInt("" + expandableListAdapter.subTotal));
									PrintHelper.mTracker.dispatch();
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
								builder.setTitle("");
								Log.d(TAG,
										(getString(R.string.pricelimit1)
												+ prefs.getString(prefs.getString("selectedRetailerId", "") + "maxPriceStr", "") + getString(R.string.pricelimit2)));
								builder.setMessage(getString(R.string.pricelimit1)
										+ prefs.getString(prefs.getString("selectedRetailerId", "") + "maxPriceStr", "")
										+ getString(R.string.pricelimit2));
								builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								builder.setNegativeButton("", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								builder.create().show();
							}
						}					
					}
				} else {
					switch (deliveryModel) {
					//
					case 0:
						showWarningDialog(R.string.not_select_a_delivery_option);
						break;
					case 1:
						if (next.getText().equals(getString(R.string.next))) {
							Intent intent = null;
							if (!isStoreSelected) {
								intent = new Intent(ShoppingCartActivity.this, StoreFinder.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								intent.putExtra("productStringCheckStore", getDesIDs(AppContext.getApplication().getProductInfos()));
								intent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
							} else if (!isCustomerInfoValid) {
								intent = new Intent(ShoppingCartActivity.this, NewSettingActivity.class);
								intent.putExtra("requireInfoEntry", true);
							}
							startActivity(intent);
						} else {
							if (!Connection.isConnected(ShoppingCartActivity.this)) {
								InfoDialog.InfoDialogBuilder connectBuilder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
								connectBuilder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
								connectBuilder.setTitle("");
								connectBuilder.setMessage(getString(R.string.nointernetconnection));
								connectBuilder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								connectBuilder.setNegativeButton("", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								connectBuilder.setCancelable(false);
								connectBuilder.create().show();
							} else {
								Log.d(TAG, "next click");
								int count = 0;
								for (int i = 0; i < groupItemList.size(); i++) {
									for (int j = 0; j < childItemList.get(i).size(); j++) {
										count++;
									}
								}
								if (count > 0) {
									if (isStoreSelected) {

										
										boolean isDM = PACKAGE_NAME.contains("dm");
										if (isDM) {
											InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
											builder.setTitle("");
											builder.setMessage(R.string.place_your_order);
											builder.setPositiveButton(getString(R.string.buy), new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													clearListData();
													dialog.dismiss();
													checkStore();
												}
											});
											builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													dialog.dismiss();
												}
											});
											builder.create().show();
										}else if(PACKAGE_NAME.contains("wmc")){
											
											if(prefs.getBoolean(PrintHelper.IS_AUTO_UPLOAD2ALBUMS, false)){
												
												String email = prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "");
												if (!email.contains("@") || !email.contains(".") || email.equals("")
														|| "".equals(PrintHelper.getAccessTokenResponse(getApplicationContext()).access_token)) {
													Intent intent = new Intent(ShoppingCartActivity.this, ShareLoginActivity.class);
													intent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
													startActivity(intent);
												} else {
													new Thread(new CreateGallery()).start();
													checkStore();
												}
												
											}else {
												checkStore(); 
											}
											
										}else { //MKM
											checkStore(); 
										}
										
									} else {// no store selected
										Log.e(TAG, "on create: infoDialog start");
										InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
										builder.setTitle("");
										builder.setMessage(getString(R.string.selectstore));
										builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												Intent intent = new Intent(ShoppingCartActivity.this, StoreFinder.class);
												intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
												intent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
												intent.putExtra("productStringCheckStore", getDesIDs(AppContext.getApplication().getProductInfos()));
												startActivity(intent);
												dialog.dismiss();
											}
										});
										builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										});
										builder.create().show();
									}

								}
							}
						}
						break;
					case 2:
						if (next.getText().equals(getString(R.string.next))) {
							Intent intent = null;
							intent = new Intent(ShoppingCartActivity.this, NewSettingActivity.class);
							if (!isCustomerInfoValid) {
								intent.putExtra("requireInfoEntry", true);
								startActivity(intent);
							} else if (!isShippingAddressValid) {
								intent.putExtra("requireInfoEntry", true);
								intent.putExtra("currentItem", R.id.radio_address);
								startActivity(intent);
							}

						} else {
							if (!Connection.isConnected(ShoppingCartActivity.this)) {
								InfoDialog.InfoDialogBuilder connectBuilder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
								connectBuilder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
								connectBuilder.setTitle("");
								connectBuilder.setMessage(getString(R.string.nointernetconnection));
								connectBuilder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								connectBuilder.setNegativeButton("", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								connectBuilder.setCancelable(false);
								connectBuilder.create().show();
							} else {
								Log.d(TAG, "next click");
								int count = 0;
								for (int i = 0; i < groupItemList.size(); i++) {
									for (int j = 0; j < childItemList.get(i).size(); j++) {
										count++;
									}
								}
								if (count > 0) {
								
									if( PrintHelper.price==null || PrintHelper.price.subTotal==null){
										return  ;
									}
									
									if(checkOrderMaximumCost(currentRetailerId)){
										return  ;
									}
									
									
									if(checkOrderMinimumCost(currentRetailerId)){
										return  ;
									}
									
									boolean isDM = PACKAGE_NAME.contains("dm");
									if(isDM){
										InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
										builder.setTitle("");

										builder.setMessage(R.string.place_your_order);
										builder.setPositiveButton(getString(R.string.buy), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												clearListData();
												dialog.dismiss();
												//Intent intent = new Intent(ShoppingCartActivity.this, SendingOrderActivity.class);
												//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
												//startActivity(intent);
												confirmSendOrder();
												PrintHelper.hasQuickbook = false;

											}
										});
										builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										});
										builder.create().show();
										
									
									}else if(PACKAGE_NAME.contains("wmc")){
										if( prefs.getBoolean(PrintHelper.IS_AUTO_UPLOAD2ALBUMS, false)){
											
											String email = prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "");
											if (!email.contains("@") || !email.contains(".") || email.equals("")
													|| "".equals(PrintHelper.getAccessTokenResponse(getApplicationContext()).access_token)) {
												// Incorrect email address, go
												// to Login UI.
												Intent intent = new Intent(ShoppingCartActivity.this, ShareLoginActivity.class);
												intent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
												startActivity(intent);
											}else {
												new Thread(new CreateGallery()).start();
												confirmSendOrder();
												//Intent intent = new Intent(ShoppingCartActivity.this, SendingOrderActivity.class);
												//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
												//startActivity(intent);
												PrintHelper.hasQuickbook = false;
											}
											
										}else {
											confirmSendOrder();
											//Intent intent = new Intent(ShoppingCartActivity.this, SendingOrderActivity.class);
											//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
											//startActivity(intent);
											PrintHelper.hasQuickbook = false;
											
										}
									}else { //MKM
										confirmSendOrder();
										//Intent intent = new Intent(ShoppingCartActivity.this, SendingOrderActivity.class);
										//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										//startActivity(intent);
										PrintHelper.hasQuickbook = false;
									}
								}
							}
						}
						break;
					}
				}
			}
		});
		

		delivery_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				adapter.notifyDataSetChanged();
				// deliveryContainer.setVisibility(View.VISIBLE);
				initPopWindow();
			}
		});

		lvDelivery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectedDeliveryName = deliveryLists.get(position);
				delivery_button.setText(selectedDeliveryName);
				selectedPosition = position;
				popupWindow.dismiss();
				String des = (String) ((TextView) view.findViewById(R.id.tvTest)).getText();
				if (des.equals(ShoppingCartActivity.this.getString(R.string.N2RShoppingCart_DestinationHome))) {
					PrintHelper.orderType = deliveryModel = 2;
				} else if (des.equals(ShoppingCartActivity.this.getString(R.string.N2RShoppingCart_DestinationStore))) {
					PrintHelper.orderType = deliveryModel = 1;
				} else {
					PrintHelper.orderType = deliveryModel = 0;
				}
				expandableListAdapter.refreshSubTotal();
				expandableListAdapter.changeScreenShow();
			}

		});

		continue_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(!isDeliveryValid()){
					return;
				}
				/* couponsTextEdit(); */
				RSSLocalytics.recordLocalyticsEvents(ShoppingCartActivity.this, EVENT_DOMORE);
				Intent i = new Intent();
				AppContext.getApplication().setContinueShopping(true);
				i.setClass(ShoppingCartActivity.this, MainMenu.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				ShoppingCartActivity.this.finish();
				List<GreetingCardManager> mGreetingCardManagers = AppContext.getApplication().getmGreetingCardManagers();
				GreetingCardManager managerNew = new GreetingCardManager(ShoppingCartActivity.this);
				mGreetingCardManagers.add(managerNew);
				PrintHelper.GreetingCardProductID = "";				
			}
		});

	}
	
	
	private boolean checkOrderMaximumCost(String currentRetailerId){
		boolean isExceedMaximum = false ;
		if(!TextUtils.isEmpty(currentRetailerId)){
			if(retailers!=null && retailers.size()>0){
				for (Retailer retailer : retailers) {
					if(currentRetailerId.equals(retailer.getId())){
						
						CartLimit cartLimit = retailer.getCartLimit() ;
						
						if(cartLimit!=null){
							double priceLimit = cartLimit.price ;
							if( priceLimit!=0 &&  PrintHelper.price.subTotal.price > priceLimit){
								
								InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
								builder.setTitle("").setMessage(getString(R.string.N2RShoppingCart_TooExpensive, cartLimit.PriceStr)) ;
								builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();

									}
								});
								builder.create().show();
							
								isExceedMaximum = true ;
								
							}
							
						}
						
						
						break ;
					}
				}
				
				
			}
		}
		
		return isExceedMaximum ;
		
	}
	
	
	
	private boolean  checkOrderMinimumCost(String currentRetailerId){
		boolean isOrderNotEnough = false ;
		if(!TextUtils.isEmpty(currentRetailerId)){
			if(retailers!=null && retailers.size()>0){
				for (Retailer retailer : retailers) {
					if(currentRetailerId.equals(retailer.getId())){
						
						CartLimit cartMiniLimit = retailer.getCartMinimumLimit() ;
						
						if(cartMiniLimit!=null){
							double priceLimit = cartMiniLimit.price ;
							
							if( priceLimit!=0 &&  PrintHelper.price.subTotal.price < priceLimit){
								
								InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
								builder.setTitle("").setMessage(getString(R.string.N2RShoppingCart_NotEnoughOrdered, cartMiniLimit.PriceStr)) ;
								builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();

									}
								});
								builder.create().show();
								
								isOrderNotEnough = true ;
								
							}
							
						}
						
						
						break ;
					}
					
				}
			}
		}
		
		
		
		
		
		return isOrderNotEnough ;
		
	}
	
 
	/**
	 * confirm send order dialog
	 */
	 Dialog confirmDialog;
	private void confirmSendOrder(){
		DisplayMetrics dm=new DisplayMetrics();
		dm=getResources().getDisplayMetrics();
		int widthDialog=(dm.widthPixels*2)/3;
		int heightDialog=(dm.heightPixels*4)/5;
		confirmDialog=new Dialog(ShoppingCartActivity.this,R.style.Dialog);
		confirmDialog.setContentView(R.layout.confirm_send_order_dialog);
		confirmDialog.setCancelable(false);
		confirmDialog.show();
		confirmDialog.getWindow().setLayout(widthDialog, heightDialog);
		Button positiveButton=(Button)confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_positiveButton);
		Button negativeButton=(Button)confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_negativeButton);
		TextView Name=(TextView) confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_Name);
		TextView add1=(TextView) confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_add1);
		TextView add2=(TextView) confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_add2);
		TextView city=(TextView) confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_city);
		TextView zipeCode=(TextView) confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_zipeCode);
		TextView storeName=(TextView) confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_storeName);
		TextView storeAdd=(TextView) confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_storeAdd);
		TextView cityAndZip=(TextView) confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_cityAndZipe);
		TextView postalCode=(TextView) confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_postalCode);
		LinearLayout storeLayout=(LinearLayout) confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_StoreLayout);
		LinearLayout homeLayout=(LinearLayout) confirmDialog.getWindow().findViewById(R.id.confirmSendOrder_homeDeliveryLayout);
		if (PrintHelper.orderType==1) {
			attr.put(KEY_DELIVERY, VALUE_STORE);
			storeLayout.setVisibility(View.VISIBLE);
			homeLayout.setVisibility(View.GONE);
			//get store informations.
			String store = prefs.getString("selectedStoreName", "");
			String address = prefs.getString("selectedStoreAddress", "");
			String cityAndZipStr = prefs.getString("selectedCity", "") + " " + prefs.getString("selectedPostalCode", "");
			String postal=prefs.getString("selectedPostalCode", "");
			//set store informations.
			storeName.setText(store);
			storeAdd.setText(address);
			cityAndZip.setText(cityAndZipStr);
			postalCode.setText(postal);
			}else if (PrintHelper.orderType==2) {
			attr.put(KEY_DELIVERY, VALUE_HOME);
			storeLayout.setVisibility(View.GONE);
			homeLayout.setVisibility(View.VISIBLE);
			// get shipping address informations.
			String firstNameShip = prefs.getString("firstNameShip", "");
			String lastNameShip = prefs.getString("lastNameShip", "");
			String addressOneShip = prefs.getString("addressOneShip", "");
			String addressTwoShip = prefs.getString("addressTwoShip", "");
			String cityShip = prefs.getString("cityShip", "");
			String stateShip = prefs.getString("stateShip", "");
			String zipcodeShip = prefs.getString("zipcodeShip", "");
			//set the shipping address information.
			Name.setText(firstNameShip+"  "+lastNameShip);
			add1.setText(addressOneShip);
			add2.setText(addressTwoShip);
			city.setText(cityShip+","+stateShip);
			zipeCode.setText(zipcodeShip);
		}
		positiveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				PrintHelper.PayOnline = isPayOnline();
				RSSLocalytics.recordLocalyticsEvents(ShoppingCartActivity.this, EVENT_SUMMARY, attr);
				needResetAttrValue = true;
				Intent intent = new Intent(ShoppingCartActivity.this, SendingOrderActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				confirmDialog.dismiss();
			}
		});
		negativeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				confirmDialog.dismiss();
			}
		});
		
			}


	/**
	 * just for Continue Shopping
	 */
	private boolean isDeliveryValid(){
		boolean valid = false;
		if(AppContext.getApplication().isInStoreCloud()){
			isStoreSelected = true;
			valid = true;
			return valid;
		}
		switch (deliveryModel) {
		case 0:
			showWarningDialog(R.string.not_select_a_delivery_option);
			break;
		case 1:
			if (prefs.contains("selectedStoreName") && !prefs.getString("selectedStoreName", "").equals("")) {
				isStoreSelected = true;
				valid = true;
			} else {
				isStoreSelected = false;
			}
			if(!isStoreSelected){
				showWarningDialog(R.string.N2RShoppingCart_SelectStore);
			}
			break;
		case 2:
			valid = true;
			break;
		}
		return valid;
	}
	
	public void showWarningDialog(final int stringId){
		InfoDialog.InfoDialogBuilder deliverSelectBuilder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
		deliverSelectBuilder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
		deliverSelectBuilder.setTitle("");
		deliverSelectBuilder.setMessage(getString(stringId));
		deliverSelectBuilder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if(stringId == R.string.not_select_a_delivery_option){
					initPopWindow();
				} else if(stringId == R.string.N2RShoppingCart_SelectStore){
					Intent intent = new Intent(ShoppingCartActivity.this, StoreFinder.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("productStringCheckStore", getDesIDs(AppContext.getApplication().getProductInfos()));
					intent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
					startActivity(intent);
				}
			}
		});
		deliverSelectBuilder.setNegativeButton("", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		deliverSelectBuilder.setCancelable(false);
		deliverSelectBuilder.create().show();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		isContextFinished = true;
		super.onDestroy();
		PrintHelper.StartOver();
		expandableListAdapter = null;
		expandableList.setAdapter(expandableListAdapter);
		expandableList = null;
	}

	@Override
	public void onPause() {
		RSSLocalytics.onActivityPause(this);
		if (dialog != null) {
			try {
				dialog.dismiss();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (confirmDialog!=null) {
			confirmDialog.dismiss();
		}
		expandableListAdapter.saveProductInfoListData();
		clearListData();
		mImageSelectionDatabase.close();
		isShoppingCardLive = false;
		super.onPause();
	}

	@Override
	public void onResume() {
		isShoppingCardLive = true;
		RSSLocalytics.onActivityResume(this);

		if (prefs.getBoolean("analytics", false)) {
			try {
				if (PrintHelper.wififlow) {
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "Wifi_At_Kiosk", 3);
				} else {
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "Prints_To_Store", 3);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		productInfoList = appContex.getProductInfos();
		childItemList = appContex.getChildItemList();
		groupItemList = appContex.getGroupItemList();
		expandableListAdapter.sortProductInfo(productInfoList);
		if (isFirstGetRetailers) {
			getRetailers();
		}else {
			expandableListAdapter.refreshSubTotal();
		}
		expandableListAdapter.changeScreenShow();
		expandableListAdapter.notifyDataSetChanged();
		isShippingAddressValid = expandableListAdapter.isShippingAddressValid(ShoppingCartActivity.this);
		isCustomerInfoValid = expandableListAdapter.isCustomerInfoValid(ShoppingCartActivity.this, false);
		productDescIds = getProductDescriptionIds();
		if (expandableListAdapter.getGroupCount() >= 2) {
			expandableList.expandGroup(0);
		}
		/*try {
			if (mImageWorker != null && mImageWorker.getImageCache() != null && PrintHelper.selectedImage != null) {
				mImageWorker.getImageCache().replaceBitmapInCache(PrintHelper.selectedImage.photoInfo.getLocalUri(), processBitmap(PrintHelper.selectedImage.photoInfo.getLocalUri()));
				expandableListAdapter.notifyDataSetChanged();
				PrintHelper.selectedImage = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			if (mImageWorker != null && mImageWorker.getImageCache() != null && PrintHelper.selectedImage != null)
				mImageWorker.getImageCache().removeBitmap(PrintHelper.selectedImage.photoInfo.getLocalUri());
		} catch (Exception ex) {
			ex.printStackTrace();
		}*/
		try {
			PrintHelper.mTracker.trackPageView("Page-Shopping_Cart");
			PrintHelper.mTracker.dispatch();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		super.onResume();
	}

	private void getRetailers() {
		if (null == retailers || retailers.isEmpty()) {
			dialogRetails = new ProgressDialog(ShoppingCartActivity.this);
			dialogRetails.setCancelable(false);
			dialogRetails.show();
			final PrintMakerWebService service = new PrintMakerWebService(ShoppingCartActivity.this, "");
			new Thread() {
				@Override
				public void run() {
					super.run();
					retailers = service.getRetailersOfferingProductsTask(getDesIDs(AppContext.getApplication().getProductInfos()));
					isFirstGetRetailers = false;
					if (retailers == null || retailers.isEmpty()) {
						handler.sendEmptyMessage(0);
					} else {
						handler.sendEmptyMessage(1);
					}
				}
			}.start();
		}
	}

	@Override
	public void onBackPressed() {
	}

//	@SuppressLint("NewApi")
//	protected Bitmap processBitmap(String data) {
//		String uri = data;
//		if (BuildConfig.DEBUG) {
//			Log.d(TAG, "processBitmap - " + data);
//		}
//		// Download a bitmap, write it to a file
//		String filename = "";
//		if (PrintHelper.selectedFileNames == null) {
//			Log.e(TAG, "ImageSelectionActivity: selectedFileNames is null");
//		} else {
//			try {
//				filename = PrintHelper.selectedFileNames.get(uri).toString();
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inJustDecodeBounds = false;
//		int width = 0;
//		int height = 0;
//		ROI tempRoi2 = new ROI();
//		ROI roi = new ROI();
//		Bitmap img = null;
//		Bitmap bit = null;
//		Bitmap rotated = null;
//		try {
//			/*
//			 * //add by song for png file if
//			 * (filename.toUpperCase().endsWith(".PNG")){ img =
//			 * PrintHelper.getMiniOfPNG(filename); }else {
//			 */
//			img = PrintHelper.loadThumbnailImage(uri, MediaStore.Images.Thumbnails.MINI_KIND, options, ShoppingCartActivity.this);
//			/* } */
//			File photo = new File(filename);
//			decodeFile(photo);
//			roi = PrintHelper.selectedImage.roi;
//			ExifInterface exif = new ExifInterface(filename);
//			if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90) {
//				Matrix matrix = new Matrix();
//				matrix.postRotate(90);
//				rotated = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
//			} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270) {
//				Matrix matrix = new Matrix();
//				matrix.postRotate(270);
//				rotated = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
//			}
//			if (rotated != null) {
//				img = null;
//				img = rotated;
//			}
//			if (roi != null) {
//				width = (int) (img.getWidth() * roi.w);
//				height = (int) (img.getHeight() * roi.h);
//			}
//			if (width == 0 || height == 0) {
//				double productWidth = Double.parseDouble(PrintHelper.selectedImage.width);
//				double productHeight = Double.parseDouble(PrintHelper.selectedImage.height);
//				double ratio = 1.0;
//				if (productWidth > productHeight) {
//					ratio = productWidth / productHeight;
//				} else {
//					ratio = productHeight / productWidth;
//				}
//				roi = PrintHelper.CalculateDefaultRoi(1.0 * img.getWidth(), 1.0 * img.getHeight(), ratio);
//				tempRoi2.h = roi.h / img.getHeight();
//				tempRoi2.w = roi.w / img.getWidth();
//				tempRoi2.y = roi.y / img.getHeight();
//				tempRoi2.x = roi.x / img.getWidth();
//			} else {
//				tempRoi2.h = roi.h;
//				tempRoi2.w = roi.w;
//				tempRoi2.y = roi.y;
//				tempRoi2.x = roi.x;
//			}
//			PrintHelper.selectedImage.roi = tempRoi2;
//		} catch (Exception ex) {
//		}
//		try {
//			bit = Bitmap.createBitmap(img, (int) ((tempRoi2.x) * img.getWidth()), (int) ((tempRoi2.y) * img.getHeight()),
//					(int) ((tempRoi2.w) * img.getWidth()), (int) ((tempRoi2.h) * img.getHeight()));
//		} catch (java.lang.Exception e) {
//			e.printStackTrace();
//		}
//		Bitmap scaledBitmap = null;
//		int cartimagesize = (int) getResources().getDimension(R.dimen.image_cart_size);
//		if (bit != null) {
//			double scaleFactor = 1;
//			if (bit.getHeight() > bit.getWidth()) {
//				scaleFactor = bit.getHeight() / cartimagesize;
//				int imgWidth = 0;
//				int imgHeight = 0;
//				imgWidth = (int) (bit.getWidth() / scaleFactor);
//				imgHeight = (int) (bit.getHeight() / scaleFactor);
//				scaledBitmap = Bitmap.createScaledBitmap(bit, imgWidth, imgHeight, true);
//			} else {
//				scaleFactor = bit.getWidth() / cartimagesize;
//				int imgWidth = 0;
//				int imgHeight = 0;
//				imgWidth = (int) (bit.getWidth() / scaleFactor);
//				imgHeight = (int) (bit.getHeight() / scaleFactor);
//				scaledBitmap = Bitmap.createScaledBitmap(bit, imgWidth, imgHeight, true);
//			}
//			Log.d(TAG, "scaleFactor: " + scaleFactor);
//		}
//		if (scaledBitmap != null)
//			return scaledBitmap;
//		else
//			return bit;
//	}

	String subtotalStr = "";
	int totalQuantity2 = 0;
	String products = "";

	private void decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
			outWidth = o.outWidth;
			outHeight = o.outHeight;
		} catch (FileNotFoundException e) {
		}
	}

	class CreateGallery implements Runnable {

		@Override
		public void run() {

			// TODO Refresh Token.
			long now = new Date().getTime() / 1000;
			long expire = Long.parseLong(PrintHelper.getAccessTokenResponse(getApplicationContext()).expire_in);
			long pass = now - PrintHelper.getAccessTokenResponse(getApplicationContext()).getAccessTokenTime;
			Log.e(TAG, "AccessToken expire in: " + expire + "; Time have pass: " + pass);
			if (((pass + 60) > expire) || PrintHelper.getAccessTokenResponse(getApplicationContext()).access_token.equals("")) {
				// Refresh Token.
				TokenGetter tokenGetter = new TokenGetter();
				ClientTokenResponse clientTokenResponse = tokenGetter.httpClientTokenUrlPost(ShareLoginActivity.CLIENT_TOKEN);
				int count = 0;
				while (count < 3 && clientTokenResponse == null) {
					clientTokenResponse = tokenGetter.httpClientTokenUrlPost(ShareLoginActivity.CLIENT_TOKEN);
					count++;
				}
				if (clientTokenResponse != null) {
					String username = prefs.getString(PrintHelper.SHARE_EMAIL_FLAG, "");
					String userPwd = prefs.getString(PrintHelper.SHARE_PASSWORD_FLAG, "");
					AccessTokenResponse accessTokenResponse = null;
					try {
						int count1 = 0;
						while (count1 < 3 && accessTokenResponse == null) {
							Log.e(TAG, "Account: " + username + " Password: " + userPwd);
							accessTokenResponse = tokenGetter.httpAccessTokenUrlPost(ShareLoginActivity.ACCESS_TOKEN_HOST,
									clientTokenResponse.client_token, username, userPwd, clientTokenResponse.client_secret);
							count1++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (accessTokenResponse != null) {
						PrintHelper.setAccessTokenResponse(accessTokenResponse, getApplicationContext());
					} else {
						Log.e(TAG, "Can not get access token response.");
						return;
					}
				} else {
					Log.e(TAG, "Can not get client token response.");
					return;
				}
			}

			// Create Gallery.
			getAlbumName();
			GalleryService galleryService = new GalleryService();
			String retailer = null, partner = null, country = null;// "walmart-CAN";
			String name = prefs.getString("share_album_name", null);
			int count1 = 0;
			while (count1 < 3 && PrintHelper.galleryUUID.equals("")) {
				PrintHelper.galleryUUID = galleryService.createAGallery(galleryService.galleryURL, "CUMMOBANDWMC", "1.0", retailer, partner, country,
						name, PrintHelper.getAccessTokenResponse(getApplicationContext()).access_token);
				Log.d(TAG, "create gallery response: " + PrintHelper.galleryUUID);
				count1++;
			}
			if (!PrintHelper.galleryUUID.equals(""))
				PictureUploadService2.isAutoStartShare = true;
		}
	}

	private String getAlbumName() {
		String albumName = "";
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		albumName = "" + year + (month < 10 ? ("0" + month) : month) + (date < 10 ? ("0" + date) : date) + (hour < 10 ? ("0" + hour) : hour)
				+ (min < 10 ? ("0" + min) : min);
		Editor editor = prefs.edit();
		editor.putString("share_album_name", albumName);
		editor.commit();
		return albumName;
	}

	private void checkStore() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				(ShoppingCartActivity.this).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog = ProgressDialog.show(ShoppingCartActivity.this, "", "", true, false);
					}
				});
				// TODO check store
				PrintMakerWebService service = new PrintMakerWebService(ShoppingCartActivity.this, "");
				final boolean isAvailable = service.checkStores("", prefs.getString("selectedStoreId", ""), getDesIDs(AppContext.getApplication().getProductInfos()));

				(ShoppingCartActivity.this).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!isAvailable) {
							InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
							builder.setTitle("");
							builder.setMessage(getString(R.string.product_not_available_at_store));
							builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									PrintHelper.stores.clear();
									Intent intent = new Intent(ShoppingCartActivity.this, StoreFinder.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.putExtra("productStringCheckStore", getDesIDs(AppContext.getApplication().getProductInfos()));
									intent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
									startActivity(intent);
									dialog.dismiss();
								}
							});
							builder.create().show();
						} else {
							String currentRetailerId = 	AppContext.getApplication().getCurrentRetailerID() ;

							if( PrintHelper.price==null || PrintHelper.price.subTotal==null){
								if(dialog!=null && dialog.isShowing()){
									dialog.dismiss();
								}
								
								return  ;
							}
							
							if(checkOrderMaximumCost(currentRetailerId)){
								if(dialog!=null && dialog.isShowing()){
									dialog.dismiss();
								}
								return  ;
							}
							
							
							if(checkOrderMinimumCost(currentRetailerId)){
								if(dialog!=null && dialog.isShowing()){
									dialog.dismiss();
								}
								return  ;
							}
							
							confirmSendOrder();
							//Intent intent = new Intent(ShoppingCartActivity.this, SendingOrderActivity.class);
							//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							//startActivity(intent);
							PrintHelper.hasQuickbook = false;
						}
						if(dialog!=null && dialog.isShowing()){
							dialog.dismiss();
						}
					}
				});
			}
		}).start();
	}

	class DeliveryHolder {
		TextView deliveryTV;
		ImageView imageView;
	}

	private class DeliveryAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public DeliveryAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			if (deliveryLists != null) {
				return deliveryLists.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			DeliveryHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_with_textview_imageview, null);

				holder = new DeliveryHolder();
				holder.deliveryTV = (TextView) convertView.findViewById(R.id.tvTest);
				holder.imageView = (ImageView) convertView.findViewById(R.id.ivTest);
				convertView.setTag(holder);
			} else {
				holder = (DeliveryHolder) convertView.getTag();
			}

			holder.imageView.setVisibility(View.GONE);
			holder.deliveryTV.setVisibility(View.VISIBLE);

			holder.deliveryTV.setText(deliveryLists.get(position));
			holder.deliveryTV.setTypeface(PrintHelper.tf);
			if (position == selectedPosition && selectedDeliveryName.equals(holder.deliveryTV.getText())) {
				holder.deliveryTV.setTextColor(Color.parseColor("#FBBA06"));
			} else {
				holder.deliveryTV.setTextColor(Color.parseColor("#FFFFFF"));
			}

			LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) holder.deliveryTV.getLayoutParams();
			params.topMargin = 8;
			params.bottomMargin = 8;
			holder.deliveryTV.setLayoutParams(params);
			return convertView;
		}

	}

	private void initDeliveryLists() {
		deliveryLists = new ArrayList<String>();
		boolean ifCanShipToHome = false;
		boolean ifCanPayOnStore = false;
		boolean ifFollowCLO = prefs.getBoolean("ifFollowCLO", false);
		Boolean isFromStroeFinder = getIntent().getBooleanExtra("isFromStroeFinder", false);
		String pickUpInStore = getString(R.string.N2RShoppingCart_DestinationStore);
		String shipHome = getString(R.string.N2RShoppingCart_DestinationHome);
		AppContext.getApplication().setIsInStoreCloud(false);
		for (Retailer retailer : retailers) {
			if (retailer.isShipToHome()) {
				ifCanShipToHome = true;
				if (retailer.isCLOLite()) {
					Editor editor = prefs.edit();
					editor.putBoolean("ifCanFollowCLOLite", true);
					editor.commit();
					ifCanShipToHome = false;
					ifCanPayOnStore = true;
					break;
				}
			} else {
				ifCanPayOnStore = true;
			}
			if(retailer.isInStore()){
				AppContext.getApplication().setIsInStoreCloud(true);
				AppContext.getApplication().setInStoreCloundRetailerID(retailer.getId());
			}
		}
		/*
		 * add CLO flow for unitedKingdom. date : 2013-12-19 add : song
		 */
		if(AppContext.getApplication().isInStoreCloud()){
			deliveryLists.add(pickUpInStore);
			PrintHelper.orderType = deliveryModel = 1;
			prefs.edit().putString("selectedRetailerId", AppContext.getApplication().getInStoreCloudRetailerID());
			prefs.edit().putString("selectedStoreId", AppConstants.IN_STORE_ID);
		} else if (ifFollowCLO) {
			deliveryLists.add(pickUpInStore);
			deliveryLists.add(shipHome);
			if (isFromStroeFinder) {
				delivery_button.setText(shipHome);
				PrintHelper.orderType = deliveryModel = 2;
			}
		} else {
			if (ifCanShipToHome && ifCanPayOnStore) {
				deliveryLists.add(pickUpInStore);
				deliveryLists.add(shipHome);
			} else if (ifCanShipToHome && !ifCanPayOnStore) {
				deliveryLists.add(shipHome);
				delivery_button.setText(shipHome);
				PrintHelper.orderType = deliveryModel = 2;
			} else if (!ifCanShipToHome && ifCanPayOnStore) {
				deliveryLists.add(pickUpInStore);
				delivery_button.setText(pickUpInStore);
				PrintHelper.orderType = deliveryModel = 1;
			} else {
				deliveryLists.add("");
				deliveryLists.add("");
				deliveryLists.add("");
				deliveryLists.add("");
			}
		}
		// if selected before
		switch (PrintHelper.orderType){
		case 1 :
			deliveryModel = 1;
			delivery_button.setText(pickUpInStore);
			break;
		case 2 :
			deliveryModel = 2;
			delivery_button.setText(shipHome);
		}
		lvDelivery.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	private void initPopWindow() {
		if (null != popupWindow && popupWindow.isShowing()) {
			popupWindow.dismiss();
		} else {
			popupWindow = new PopupWindow(popContentView, delivery_button.getWidth(), delivery_button.getHeight() * 4, true);
			popupWindow.setFocusable(true);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setBackgroundDrawable(new PaintDrawable(Color.TRANSPARENT));
			popupWindow.showAsDropDown(delivery_button);
		}
	}
	
	public void showEnterCouponDialog(){
		String retailerID = AppContext.getApplication().getCurrentRetailerID();
		String products = expandableListAdapter.getProductIDs();
		CouponInputDialog dialog = new CouponInputDialog(this, R.style.Theme_Translucent, retailerID, products, expandableListAdapter.getCart());
		dialog.show();
	}

	private void couponsTextEdit() {
		couponsBuilder = new CouponsEditTestInputDialog.EditTestInputDialogBuilder(ShoppingCartActivity.this, 2);
		couponsBuilder.setTitle(R.string.OrderSummary_Coupons);
		couponsBuilder.setMessage("");
		couponsBuilder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				couponsBuilder = null;
			}
		});
		couponsBuilder.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				couponsBuilder = null;

			}
		});
		couponsBuilder.setCancelable(false);
		couponsBuilder.create().show();
	}

	private void clearListData() {
		/*
		 * if (groupItemList.size() > 0 && groupItemList.size() > 0) { if
		 * (groupItemList.get(groupItemList.size() - 1).toString().equals("")) {
		 * groupItemList.remove(groupItemList.size() - 1);
		 * childItemList.remove(childItemList.size() - 1); } if
		 * (groupItemList.get(groupItemList.size() - 1).toString()
		 * .equals(getResources
		 * ().getString(R.string.orderConfirmationEstimated))) {
		 * groupItemList.remove(groupItemList.size() - 1);
		 * childItemList.remove(childItemList.size() - 1); } }
		 */
	}

	// get Products Ids.
	private String getProductDescriptionIds() {
		String ids = "";

		if (PrintHelper.productWithId == null) {
			PrintHelper.productWithId = new HashMap<String, String>();
		} else {
			PrintHelper.productWithId.clear();
		}
		for (int i = 0; i < groupItemList.size()-1; i++) {
			if (childItemList.get(i).size() ==0 ){
				continue;
			}
			String descriptionId = childItemList.get(i).get(0).descriptionId;
			if (i < groupItemList.size() - 2) {
				ids += descriptionId + ",";
			} else {
				ids += descriptionId;
			}

		}
		return ids;
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg != null) {
				Intent intent = new Intent();
				intent.setAction("com.rss.trans");
				Log.v(TAG, "getRetailers success " + msg.what);
				switch (msg.what) {
				case 0: // failed
					// show dialog
					dialogRetails.dismiss();
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ShoppingCartActivity.this);
					builder.setMessage(R.string.share_upload_error_no_responding);
					builder.setNegativeButton(R.string.share_upload_retry, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							getRetailers();

						}

					});

					builder.setPositiveButton(getString(R.string.Back), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							ShoppingCartActivity.this.finish();
						}
					});

					builder.create().show();

					break;

				case 1: // success
					dialogRetails.dismiss();
					initDeliveryLists();
					expandableListAdapter.changeScreenShow();
					expandableListAdapter.refreshSubTotal();
					break;

				default:
					break;

				}

			}

		};

	};
	
	public String getDesIDs(List<ProductInfo> products){
		String ids = "";
		if(products!=null){
			for(int i=0; i<products.size(); i++){
				ProductInfo pro = products.get(i);
				if(!ids.contains(pro.descriptionId)){
					if(i!=products.size()-1){
						ids += pro.descriptionId + ",";
					} else {
						ids += pro.descriptionId;
					}
				} else {
					continue;
				}
			}
		}
		
		if(ids.endsWith(",")){
			ids=ids.substring(0, ids.length()-1);
		}
		
		return ids;
	}
	
	// for RSSMOBILEPDC-1557
	//when user select the retailer, the the payOnline value from server
	private boolean isPayOnline (){
		String selectedRetailerId = prefs.getString("selectedRetailerId","");
		for (Retailer retailer : retailers) {
			if (retailer.getId().equalsIgnoreCase(selectedRetailerId)){
				return retailer.isPayOnline();
			}
		}
		return false;
	}

	@Override
	public void refreshExpandableList() {
		if (!isContextFinished){
			expandableListAdapter.setExpandableListData();
			expandableListAdapter.notifyDataSetChanged();
		}

	}
	
	// used for refresh price when Enter Coupon dialog dismissed.
	public void refreshPrice(){
		expandableListAdapter.refreshSubTotal();
	}

	@Override
	public void addHeader(TopOrderHeadSummaryWidget topOrderSummaryHeadWidget) {
		expandableList.addHeaderView(topOrderSummaryHeadWidget);

	}

	@Override
	public void addFooter(OrderSummaryWidget orderSummaryFootWidget) {
		expandableList.addFooterView(orderSummaryFootWidget);

	}

	@Override
	public void recordLocalyticsOASEvents(String key, String value) {
		attr.put(key, value);
	}

}
