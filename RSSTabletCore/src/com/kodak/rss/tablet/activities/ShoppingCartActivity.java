package com.kodak.rss.tablet.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.LocalCustomerInfo;
import com.kodak.rss.core.bean.OrderDetail;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.retailer.Retailer;
import com.kodak.rss.core.n2r.bean.shoppingcart.Cart;
import com.kodak.rss.core.n2r.bean.shoppingcart.Discount;
import com.kodak.rss.core.n2r.bean.shoppingcart.NewOrder;
import com.kodak.rss.core.n2r.bean.shoppingcart.Pricing;
import com.kodak.rss.core.n2r.bean.shoppingcart.Pricing.LineItem;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.AppManager;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.ShoppingCartProductsAdapter;
import com.kodak.rss.tablet.adapter.ShoppingCartProductsAdapter.ShoppingCartListener;
import com.kodak.rss.tablet.bean.StorePriceInfo;
import com.kodak.rss.tablet.thread.SendingOrderTask;
import com.kodak.rss.tablet.thread.SendingOrderTask.SendingOrderTaskListener;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.util.ShoppingCartUtil;
import com.kodak.rss.tablet.util.UploadProgressUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.view.CouponView;
import com.kodak.rss.tablet.view.MapFragment;
import com.kodak.rss.tablet.view.PaymentView;
import com.kodak.rss.tablet.view.ShoppingCartExpandableListView;
import com.kodak.rss.tablet.view.dialog.DialogConfirmDestination;
import com.kodak.rss.tablet.view.dialog.DialogSendingOrder;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class ShoppingCartActivity extends BaseNetActivity {
	private final String TAG = ShoppingCartActivity.class.getSimpleName();
	
	//private RelativeLayout container;
	private ScrollView scrollView;
	private ShoppingCartExpandableListView lvProducts;
	
	private Button btBuy;
	private Button btDoMore;
	private Button btDelivery;
	private Button btCoupons;
	private Button btChangeCus;
	private Button btChangeShip;
	private Button btChangeStore;
	
	private TextView tvCusInfo;
	private TextView tvShipAdd;
	private TextView tvStoreInfo;
	
	//private TextView tvTopTotalPrice;
	private TextView tvBottomTotalPrice;
	//private ProgressBar pbWaitingTop;
	private ProgressBar pbWaitingBottom;
	private TextView tv_order_subtotalLable_bottom;
	private TextView tv_order_notice_bottom;
	
	private Button btHome;
	private Button btStore;
	
	private TextView tvTaxDetail;
	
	private GoogleMap googleMap;
	
	private ShoppingCartProductsAdapter adapter;
	
	private static final int margin = 0;
	private int totalSpacing = 0;
	
	public String cartId = "";
	// this is used for control how many thread run when pricing
	public List<String> waitForPricing = new ArrayList<String>();
	
	private int orderType = 0;
	private NewOrder order = null;
	private List<Retailer> availableRetailers = null;
	
	private RssTabletApp app;
	private LocalCustomerInfo customer;
	private StoreInfo store;
	
	private List<ProductInfo> productInfos;
	private Cart successfulCart;
	
	/**
	 * the height of the products list view you want
	 */
	private int expectHeight = 0;
	
	private DialogSendingOrder sendingOrderDialog;
	private SendingOrderTask sendingOrder;
	
	public LruCache<String, Bitmap> mMemoryCache; 
	
	private PaymentView payView;
	private List<StorePriceInfo> StorePriceInfoList;
	
	private double orderTotelPrice;
	private CouponView couponView;	
	private boolean couponApplied = false;
	private boolean isShoppingCardLive = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shoppingcart_hd);
		app = RssTabletApp.getInstance();
		mMemoryCache = MemoryCacheUtil.generMemoryCache(16);
		productInfos = app.products;
		initViews();
		availableRetailers = new ArrayList<Retailer>();
		//change by bing on 2014-11-10 for 1516
//		RssTabletApp.getInstance().orderType = SendingOrderTask.ORDER_TYPE_DEFAULT;
		destroied = false;
		waitForPricing.add(ShoppingCartUtil.getProductsWithCount(productInfos));
		new Thread(pricing).start();
		RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_SHOPPING_CART);	
	}
	
	private void initViews(){
		//container = (RelativeLayout) findViewById(R.id.container);
		btBuy = (Button) findViewById(R.id.bt_buy);
		btDoMore = (Button) findViewById(R.id.bt_do_more);
		btDelivery = (Button) findViewById(R.id.bt_select_delivery);
		btCoupons = (Button) findViewById(R.id.bt_coupons);
		btChangeCus = (Button) findViewById(R.id.bt_change_cus_info);
		btChangeShip = (Button) findViewById(R.id.bt_change_shipping_info);
		btChangeStore = (Button) findViewById(R.id.bt_change_store);
		
		//tvTopTotalPrice = (TextView) findViewById(R.id.tv_order_subtotal_top);
		tvBottomTotalPrice = (TextView) findViewById(R.id.tv_order_subtotal_bottom);
		//pbWaitingTop = (ProgressBar) findViewById(R.id.pb_waiting_top);
		pbWaitingBottom = (ProgressBar) findViewById(R.id.pb_waiting_bottom);
		tv_order_subtotalLable_bottom = (TextView) findViewById(R.id.tv_order_subtotalLable_bottom);
		tv_order_notice_bottom = (TextView) findViewById(R.id.tv_order_notice_bottom);
		tvTaxDetail = (TextView) findViewById(R.id.tv_tax_detail);
		
		scrollView = (ScrollView) findViewById(R.id.products_part);
		lvProducts = (ShoppingCartExpandableListView) findViewById(R.id.lv_products);
		lvProducts.setDivider(null);
		/*
		 * update by song fixed for RSSMOBILEPDC-1143
		 * activity_shoppingcart_hd.xml also changed
		 */
		findViewById(R.id.detail_container).setVisibility(View.VISIBLE);
		tvCusInfo = (TextView) findViewById(R.id.tv_cus_info);
		tvShipAdd = (TextView) findViewById(R.id.tv_shipping_info);
		tvStoreInfo = (TextView) findViewById(R.id.tv_store_info);
		googleMap = ((MapFragment) getSupportFragmentManager().findFragmentById(R.id.mv_store_mapview)).getMap();
		
		adapter = new ShoppingCartProductsAdapter(this, mMemoryCache,productInfos, shoppingCartListener, lvProducts, scrollView);
		lvProducts.setAdapter(adapter);
		totalSpacing = 0;//(int)getResources().getDimension(R.dimen.shopping_cart_item_spacing) * (adapter.getGroupCount()-1);
		expectHeight = getWindow().getWindowManager().getDefaultDisplay().getHeight() * 2 /5;
		ShoppingCartUtil.setListViewHeightBasedOnChildren(lvProducts, margin, totalSpacing, expectHeight);
		
		payView = (PaymentView) findViewById(R.id.payment_view);
		couponView = (CouponView) findViewById(R.id.coupon_view);
		
		findViewById(R.id.cart).setEnabled(false);			
		setupEvents();
		
		btBuy.setEnabled(false);
	}
	
	private void updateProductList(List<ProductInfo> proInfos){
		ShoppingCartUtil.updateImageInfoList(proInfos);
		adapter.initData(proInfos);
		adapter.setProductPrice(null);
		adapter.notifyDataSetChanged();
		ShoppingCartUtil.setListViewHeightBasedOnChildren(lvProducts, margin, totalSpacing, expectHeight);
		
		waitForPricing.add(ShoppingCartUtil.getProductsWithCount(proInfos));
		if(adapter.getProductGroupCount() == 0){
			RssTabletApp.getInstance().startOver();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void resizeDetailPartSize(){
		RelativeLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		params.height = (int) (display.getWidth()/2*0.9);
		params.addRule(RelativeLayout.BELOW, R.id.bottom_total_part);
		params.addRule(RelativeLayout.ALIGN_LEFT, R.id.lv_products);
		params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.lv_products);
		findViewById(R.id.detail_container).setLayoutParams(params);
		findViewById(R.id.detail_container).invalidate();
	}
	
	private void setupEvents(){
		btBuy.setOnClickListener(this);
		btDoMore.setOnClickListener(this);
		btDelivery.setOnClickListener(this);
		btCoupons.setOnClickListener(this);
		btChangeCus.setOnClickListener(this);
		btChangeShip.setOnClickListener(this);
		btChangeStore.setOnClickListener(this);
		
		lvProducts.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				scrollView.requestDisallowInterceptTouchEvent(true);
				return false;
			}
		});
		
	}
	
	private void updateData(){
		updateCustomerInfo();
		updateShippingAddress();
		updateStoreInfo();
		updateBuyButton();
	}
	
	private void updateCustomerInfo(){
		customer = new LocalCustomerInfo(this);
		String cusInfo = "";
		if(!customer.getCusFirstName().equals("") || !customer.getCusLastName().equals("")){
			cusInfo += customer.getCusFirstName() + " " + customer.getCusLastName() + "\n";
		}
		if(!customer.getCusEmail().equals("")){
			cusInfo += customer.getCusEmail() + "\n";
		}
		if(!customer.getCusPhone().equals("")){
			cusInfo += customer.getCusPhone() + "\n";
		}
		
		tvCusInfo.setText(cusInfo);
		tvCusInfo.invalidate();
	}
	
	private void updateShippingAddress(){
		customer = new LocalCustomerInfo(this);
		String shipInfo = ShoppingCartUtil.formatShippingDetail(customer);
		tvShipAdd.setText(shipInfo);
		tvShipAdd.invalidate();
	}
	
	private void updateStoreInfo(){
		store = StoreInfo.loadSelectedStore(this);
		if(store == null){
			store = new StoreInfo();
		} else {
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(store.latitude, store.longitude), 15));
			UiSettings mapSetting = googleMap.getUiSettings();
			mapSetting.setAllGesturesEnabled(false);
			mapSetting.setMyLocationButtonEnabled(false);
			mapSetting.setRotateGesturesEnabled(false);
			mapSetting.setScrollGesturesEnabled(false);
			mapSetting.setZoomControlsEnabled(false);
			mapSetting.setZoomGesturesEnabled(false);
			mapSetting.setTiltGesturesEnabled(false);
			googleMap.clear();
			
			MarkerOptions marker=null;
			try {
				
				marker = StoreSelectActivity.createMapMaker(store);
				googleMap.addMarker(marker);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String storeInfo = ShoppingCartUtil.formatStoreDetail(store);
		tvStoreInfo.setText(storeInfo);
		tvStoreInfo.invalidate();		
	}
	
	private void updateBuyButton(){
		if(orderType == SendingOrderTask.ORDER_TYPE_HOME){
			if(ShoppingCartUtil.isCustomerInfoValid(this, true) && ShoppingCartUtil.isShippingAddressValid(this)){
				btBuy.setText(R.string.N2RShoppingCart_Buy);
			} else {
				btBuy.setText(R.string.next);
			}
		} else if(orderType == SendingOrderTask.ORDER_TYPE_STORE){
			if(ShoppingCartUtil.isCustomerInfoValid(this, false) && ShoppingCartUtil.isStoreInfoValid(this)){
				btBuy.setText(R.string.N2RShoppingCart_Buy);
			} else {
				btBuy.setText(R.string.next);
			}
		} else {
			btBuy.setText(R.string.next);
		}
	}

	@Override
	protected void onResume() {
		isShoppingCardLive = true;
		super.onResume();		
		if (btCart != null) {
			btCart.setEnabled(false);
		}
		
		updateData();
		resizeDetailPartSize();
		if(adapter!=null){
			adapter.notifyDataSetChanged();
		}
		if(availableRetailers==null || availableRetailers.isEmpty()){
			String descriptionIds = ShoppingCartUtil.getProductDescriptionIDs(productInfos);
			new Thread(new FetchAvailableRetailer(this, descriptionIds)).start();
		}
	}

	@Override
	protected void onPause() {
		isShoppingCardLive = false;
		super.onPause();
		MemoryCacheUtil.evictAll(mMemoryCache);		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroied = true;		
		mMemoryCache = null;
		if(dialog!=null && dialog.isShowing()){
			dialog.dismiss() ;
		}
	}
	
	@Override
	public void startOver() {			
		ShoppingCartProductsAdapter mAdapter = null;
		lvProducts.setAdapter(mAdapter);
		RssTabletApp.getInstance().clearTempImageFolder();
		super.startOver();
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		int viewId = v.getId();
		if(viewId == R.id.bt_buy){
			if(orderType == SendingOrderTask.ORDER_TYPE_DEFAULT){
				btDelivery.performClick();
				return;
			} else if(orderType == SendingOrderTask.ORDER_TYPE_STORE){
				if(!ShoppingCartUtil.isStoreInfoValid(this)){
					btChangeStore.performClick();
					return;
				} else if(!ShoppingCartUtil.isCustomerInfoValid(this, false)){
					btChangeCus.performClick();
					return;
				}
			} else if(orderType == SendingOrderTask.ORDER_TYPE_HOME){
				if(!ShoppingCartUtil.isCustomerInfoValid(this, true)){
					btChangeCus.performClick();
					return;
				} else if(!ShoppingCartUtil.isShippingAddressValid(this)){
					btChangeShip.performClick();
					return;
				}
			}
			if(orderType == SendingOrderTask.ORDER_TYPE_HOME){
				showOrderDestinationDialog(null, customer);
			} else if(orderType == SendingOrderTask.ORDER_TYPE_STORE){
				showOrderDestinationDialog(store, null);
			}
		} else if(viewId == R.id.bt_do_more){
			if(orderType == SendingOrderTask.ORDER_TYPE_DEFAULT){
				showDestinationWaring();
				return;
			} else if(orderType == SendingOrderTask.ORDER_TYPE_STORE){
				if(!ShoppingCartUtil.isStoreInfoValid(this)){
					InfoDialog infoDialog = new InfoDialog.Builder(this)
					.setMessage(R.string.N2RShoppingCart_SelectStore)
					.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							btChangeStore.performClick();
						}
					})
					.create();
					infoDialog.show();
					return;
				}
			}
			RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_DO_MORE);
			RssTabletApp.getInstance().isUseDoMore = true;
			RssTabletApp.getInstance().skipMainFromShoppingCart = true;
			AppManager.getInstance().goToHomeActivity();						
		} else if(viewId == R.id.bt_coupons){
			if(orderType == SendingOrderTask.ORDER_TYPE_DEFAULT){
				showDestinationWaring();
				return;
			} else if(orderType == SendingOrderTask.ORDER_TYPE_STORE){
				if(!ShoppingCartUtil.isStoreInfoValid(this)){
					InfoDialog infoDialog = new InfoDialog.Builder(this)
					.setMessage(R.string.N2RShoppingCart_SelectStore)
					.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							btChangeStore.performClick();
						}
					}).create();
					infoDialog.show();
					return;
				}
			}
			couponView.setViewSize().setNeededInfo(cartId, getCurrentRetailerID(), ShoppingCartUtil.getProductsWithCount(productInfos)).setViewShow();	
		} 
		else if(viewId == R.id.bt_select_delivery){
			updateDeliverySelectionWindow(v, v.getWidth(), v.getHeight());
		} 
		else if(viewId == R.id.bt_change_cus_info){
			Intent mIntent = new Intent(this, SettingsActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt("item", SettingsActivity.Customer_Info_ID);
			mIntent.putExtras(bundle);
			startActivity(mIntent);
			app.localytics.setUserInfoChanged(true);
		} 
		else if(viewId == R.id.bt_change_shipping_info){
			Intent mIntent = new Intent(this, SettingsActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt("item", SettingsActivity.Shipping_Info_ID);
			mIntent.putExtras(bundle);
			startActivity(mIntent);
			app.localytics.setShipAddChanged(true);
		} 
		else if(viewId == R.id.bt_change_store){
			Intent mIntent = new Intent(this, StoreSelectActivity.class);
			mIntent.putExtra("fromCart", true);
			startActivityForResult(mIntent, 0);
		}
	}
	
	private void showDestinationWaring(){
		InfoDialog infoDialog = new InfoDialog.Builder(this)
		.setMessage(R.string.N2RShoppingCart_SelectDestinationError)
		.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				btDelivery.performClick();
			}
		})
		.create();
		infoDialog.show();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {		
		super.onActivityResult(requestCode, resultCode, intent);		
		if(resultCode == 1){
			destinationChangedListener.onDestinationChangd(SendingOrderTask.ORDER_TYPE_HOME);
		} else {
			updateStoreInfo();
			destinationChangedListener.onDestinationChangd(SendingOrderTask.ORDER_TYPE_STORE);
		}	
	}

	private PopupWindow window;
	private void updateDeliverySelectionWindow(View button, int width, int height){
		if(window!=null && window.isShowing()){
			window.dismiss();
			return;
		}
		View v = LayoutInflater.from(this).inflate(R.layout.shoppingcart_delivery_selection, null);
		btStore = (Button) v.findViewById(R.id.bt_pickInStore);
		btHome = (Button) v.findViewById(R.id.bt_pickInHome);
		btStore.setVisibility(View.INVISIBLE);
		btHome.setVisibility(View.INVISIBLE);
		refreshDeliveryButtons();
		
		btStore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				destinationChangedListener.onDestinationChangd(SendingOrderTask.ORDER_TYPE_STORE);
			}
		});
		
		btHome.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				destinationChangedListener.onDestinationChangd(SendingOrderTask.ORDER_TYPE_HOME);
			}
		});
		window = new PopupWindow(v, width, height*3);
		window.showAsDropDown(button);
	}
	
	@Override
	public void onDrawerStateChanged(int arg0) {
		super.onDrawerStateChanged(arg0);
		
		if(window!= null && window.isShowing()){
			window.dismiss();
		}
	}
	
	private void refreshDeliveryButtons(){
		if(btHome==null || btStore==null){
			return;
		}
		if(availableRetailers==null || availableRetailers.isEmpty()){
			btHome.setVisibility(View.INVISIBLE);
			btStore.setVisibility(View.INVISIBLE);
		} else {
			for(Retailer retailer : availableRetailers){
				if(retailer.shipToHome && !retailer.cloLite){
					btHome.setVisibility(View.VISIBLE);
				} else if(retailer.cloLite && SharedPreferrenceUtil.getBoolean(this, SharedPreferrenceUtil.ACCEPT_CLOLITE)){
					btHome.setVisibility(View.VISIBLE);
				} else {
					btStore.setVisibility(View.VISIBLE);
				}
			}
		}
	}
	
	public DestinationChangedListener destinationChangedListener = new DestinationChangedListener() {
		
		@Override
		public void onDestinationChangd(int type) {
			orderType = type;
			View shippingPart = findViewById(R.id.shipping_container);
			View storePart = findViewById(R.id.store_container);
			View mapPart = findViewById(R.id.mapview_container);
			String retailerId = "";
			switch (orderType) {
			case SendingOrderTask.ORDER_TYPE_HOME:
				RssTabletApp.getInstance().orderType = SendingOrderTask.ORDER_TYPE_HOME;
				btDelivery.setText(getString(R.string.N2RShoppingCart_DestinationHome));
				shippingPart.setVisibility(View.VISIBLE);
				storePart.setVisibility(View.GONE);
				mapPart.setVisibility(View.INVISIBLE);
				adapter.notifyDataSetChanged();
				retailerId = app.getHomeDeliveryRetailerId(availableRetailers);
				app.localytics.setDelivery(RSSTabletLocalytics.LOCALYTICS_VALUE_HOME);
				break;
			case SendingOrderTask.ORDER_TYPE_STORE:
				RssTabletApp.getInstance().orderType = SendingOrderTask.ORDER_TYPE_STORE;
				btDelivery.setText(getString(R.string.N2RShoppingCart_DestinationStore));
				shippingPart.setVisibility(View.GONE);
				storePart.setVisibility(View.VISIBLE);
				mapPart.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
				if(store != null){
					retailerId = store.retailerID;
				}
				app.localytics.setDelivery(RSSTabletLocalytics.LOCALYTICS_VALUE_STORE);			
				break;
			}
			SharedPreferrenceUtil.setString(ShoppingCartActivity.this, SharedPreferrenceUtil.SELECTED_RETAILER_ID, retailerId);
			shoppingCartListener.onProductsUpdated(null, ShoppingCartProductsAdapter.ACTION_UPDATE);
			updateBuyButton();
			findViewById(R.id.detail_container).setVisibility(View.VISIBLE);
			if(window!=null){
				window.dismiss();
			}
		}
	};
	
	ShoppingCartListener shoppingCartListener = new ShoppingCartListener() {
		
		@Override
		public void onSelected(int groupPosition, boolean isExpanded) {
			if(isExpanded){
				lvProducts.collapseGroup(groupPosition);
			} else {
				lvProducts.expandGroup(groupPosition, true);
			}
			ShoppingCartUtil.setListViewHeightBasedOnChildren(lvProducts, margin, totalSpacing, expectHeight);
		}
		
		@Override
		public void onProductsUpdated(final ProductInfo product, int action) {
			if(action == ShoppingCartProductsAdapter.ACTION_DELETE){
				String productName = ShoppingCartUtil.getProductName(app.getCatalogList(), product.descriptionId);
				dialog = new InfoDialog.Builder(ShoppingCartActivity.this)
				.setCancelable(false)
				.setCanceledOnTouchOutside(false)
				.setMessage(getString(R.string.N2RShoppingCart_RemovePrint).replace("%@", productName))
				.setPositiveButton(R.string.d_no, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setNegativeButton(R.string.d_yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						app.localytics.addCartRemovals();
						ShoppingCartUtil.delItem(product);
						productInfos = app.products;
						adapter.initData(productInfos);
						updateProductList(productInfos);
						dialog.dismiss();
					}
				})
				.create();
				dialog.show();
			} else {
				updateProductList(productInfos);
			}
			
		}

		@Override
		public void onEdit(ProductInfo proInfo) {
			if(proInfo.productType.equalsIgnoreCase(AppConstants.printType)){				
				Intent mIntent = new Intent(ShoppingCartActivity.this, PrintsActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("category", proInfo.category);
				bundle.putString(AppConstants.imageId, proInfo.chosenImageList.get(0).id);
				bundle.putSerializable("storePrice", (Serializable) StorePriceInfoList);
				mIntent.putExtras(bundle);
				startActivity(mIntent);				
			} else if(proInfo.productType.equalsIgnoreCase(AppConstants.bookType)){				
				Intent mIntent = new Intent(ShoppingCartActivity.this, PhotoBooksProductActivity.class);
				Bundle bundle = new Bundle();				
				bundle.putString(AppConstants.bookId, proInfo.correspondId);
				mIntent.putExtras(bundle);
				startActivity(mIntent);
			} else if(proInfo.productType.equalsIgnoreCase(AppConstants.cardType)){				
				Intent mIntent = new Intent(ShoppingCartActivity.this, GCEditActivity.class);
				Bundle bundle = new Bundle();				
				bundle.putString(AppConstants.cardId, proInfo.correspondId);
				mIntent.putExtras(bundle);
				startActivity(mIntent);
			} else if(proInfo.productType.equalsIgnoreCase(AppConstants.calendarType)){				
				Intent mIntent = new Intent(ShoppingCartActivity.this, CalendarEditActivity.class);
				Bundle bundle = new Bundle();				
				bundle.putString(AppConstants.calendarId, proInfo.correspondId);
				mIntent.putExtras(bundle);
				startActivity(mIntent);
			} else if(proInfo.productType.equalsIgnoreCase(AppConstants.collageType)){				
				Intent mIntent = new Intent(ShoppingCartActivity.this, CollageEditActivity.class);
				Bundle bundle = new Bundle();				
				bundle.putString(AppConstants.collageId, proInfo.correspondId);
				mIntent.putExtras(bundle);
				startActivity(mIntent);
			}
			
			finish();
		}

		@Override
		public void onRoundaboutSelected() {
			dialog = new InfoDialog.Builder(ShoppingCartActivity.this)
			.setCancelable(false)
			.setCanceledOnTouchOutside(false)
			.setMessage(getString(R.string.OrderSummary_ShippingDetails), true)
			.setPositiveButton(R.string.d_ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}, true)
			.create();
			dialog.show();
		}

		@Override
		public void onRemoveCouponSelected() {	
			app.setCouponCode("");
			updateProductList(productInfos);
		}
	};
	
	public void refreshPrice(){
		updateProductList(productInfos);
	}
	
	private String getCurrentRetailerID(){
		String retailerId = "";
		if(orderType == SendingOrderTask.ORDER_TYPE_HOME){
			retailerId = app.getHomeDeliveryRetailerId(availableRetailers);
		} else {
			if(store != null){
				retailerId = store.retailerID;
			}
		}
		return retailerId;
	}
	
	static boolean destroied = false;
	Runnable pricing = new Runnable(){

		@Override
		public void run() {
			while(!destroied){
				if(waitForPricing.size()>0){
					handler.obtainMessage(PRICING_START).sendToTarget();
					WebService service = new WebService(ShoppingCartActivity.this);
					String retailerId = getCurrentRetailerID();
					String productsWithCount = waitForPricing.get(waitForPricing.size()-1);
					waitForPricing.clear();
				//	Pricing price = null;
					Cart pricingCart = null;
					if(cartId.equals("")){
						Cart cart = null;
						try {
							cart = service.createCartTask();
						} catch (RssWebServiceException e) {
							e.printStackTrace();
						}
						if(cart != null){
							cartId = cart.cartId;
						}
					}
					StoreInfo store = StoreInfo.loadSelectedStore(ShoppingCartActivity.this);
					if (!needGetPriceFromServer()){
						handler.obtainMessage(PRICING_FINISHED, orderType, 0, pricingCart).sendToTarget();
						continue;
					}
					//fix 1620 by bing 
					if(orderType == SendingOrderTask.ORDER_TYPE_STORE){
						if (store != null && store.id != null && !"".equals(store.id)) {												
							try {
								service.setStoreTask(cartId, retailerId, store.id);
							} catch (RssWebServiceException e) {						
								e.printStackTrace();
							}
						}
					}
					
					try {
						String discounts = "";
						if (btCoupons.getVisibility() == View.VISIBLE) {
							discounts = app.getCouponCode();
						}
						pricingCart = service.priceProduct3Task(cartId, retailerId, productsWithCount,discounts);
//						price = service.priceProduct3Task(cartId, retailerId, productsWithCount,discounts);
					} catch (RssWebServiceException e) {
						e.printStackTrace();
					}
					if (pricingCart == null) {
						handler.obtainMessage(CAN_NOT_GETPRICE).sendToTarget();
					}
					handler.obtainMessage(PRICING_FINISHED, orderType, 0, pricingCart).sendToTarget();
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	};
	
	class FetchAvailableRetailer implements Runnable {
		
		private String ids = "";
		private Context mContext;

		public FetchAvailableRetailer(Context context, String productDescriptionIds){
			this.ids = productDescriptionIds;
			this.mContext = context;
		}
		
		@Override
		public void run() {
			WebService webService = new WebService(mContext);
			try {
				availableRetailers = webService.getRetailersOfferingProductsTask(ids);
			} catch (com.kodak.rss.core.exception.RssWebServiceException e) {
				e.printStackTrace();
			}
			handler.sendEmptyMessage(REFRESH_DELIVERY);
		}	
	}
	
	private final int PRICING_FINISHED = 0;
	private final int PRICING_START = 1;
	private final int PREPARED_ORDER_COMPLETELY = 2;
	private final int PREPARED_ORDER_START = 3;
	private final int REFRESH_DELIVERY = 4;
	private final int SENDING_ORRDER_ERROR = 5;
	private final int STORE_NOT_SUPPORTED = 6;
	private final int CAN_NOT_GETPRICE = 7;
	
	private InfoDialog dialog;	
	private InfoDialog notSupportStoreDialog;	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case PRICING_FINISHED:
				int type = msg.arg1;
				tv_order_notice_bottom.setVisibility(View.INVISIBLE);
				if (!RssTabletApp.getInstance().needGetPriceFromServer){
					if(type == SendingOrderTask.ORDER_TYPE_DEFAULT){
						tvTaxDetail.setVisibility(View.GONE);
						tv_order_subtotalLable_bottom.setVisibility(View.GONE);
						pbWaitingBottom.setVisibility(View.GONE);
						tv_order_notice_bottom.setVisibility(View.VISIBLE);
						tv_order_notice_bottom.setText(ShoppingCartActivity.this.getString(R.string.NoDeliver_Price));
						adapter.setShowShippingAndHandling(false);						
						//adapter.notifyDataSetChanged();
						//break;
					} else if(type == SendingOrderTask.ORDER_TYPE_STORE){
						pbWaitingBottom.setVisibility(View.GONE);
						if(!ShoppingCartUtil.isStoreInfoValid(ShoppingCartActivity.this)){
							tvTaxDetail.setVisibility(View.GONE);
							tv_order_subtotalLable_bottom.setVisibility(View.GONE);
							tv_order_notice_bottom.setVisibility(View.VISIBLE);
							tv_order_notice_bottom.setText(ShoppingCartActivity.this.getString(R.string.NoStore_Price));
							adapter.setShowShippingAndHandling(false);
							//adapter.notifyDataSetChanged();
							//break;
						}else {
							tv_order_notice_bottom.setVisibility(View.GONE);
							tv_order_subtotalLable_bottom.setVisibility(View.VISIBLE);
						}
					}else if(type == SendingOrderTask.ORDER_TYPE_HOME){
						tvTaxDetail.setVisibility(View.VISIBLE);
						tv_order_subtotalLable_bottom.setVisibility(View.VISIBLE);
						pbWaitingBottom.setVisibility(View.VISIBLE);
						tv_order_notice_bottom.setVisibility(View.GONE);
					}
				}
				
				// change and add code for add coupon function by bing
//				Pricing price = (Pricing) msg.obj;	
				Cart pricingCart = (Cart) msg.obj;	
				Pricing price = null;
				Discount pricingDiscount = null;
				if (pricingCart != null ) {
					price = pricingCart.pricing;
					if (pricingCart.discounts != null && app.getCouponCode() != null && !"".equals(app.getCouponCode())) {
						for (Discount discount : pricingCart.discounts) {
							if (discount == null) continue;
							if (discount.code == null) continue;	
							if (discount.code.equals(app.getCouponCode())) {
								pricingDiscount = discount;
								if(pricingDiscount.status == Discount.Applied){
									couponApplied = true;
								} else {
									couponApplied = false;
								}
								break;
							}
						}
					}	
				}
				
				if (tv_order_notice_bottom.getVisibility() ==View.VISIBLE){
					tvBottomTotalPrice.setVisibility(View.INVISIBLE);
					tv_order_subtotalLable_bottom.setVisibility(View.INVISIBLE);
				}else {
					tvBottomTotalPrice.setVisibility(View.VISIBLE);
					tv_order_subtotalLable_bottom.setVisibility(View.VISIBLE);
				}
				if(waitForPricing.size()==0){
					if(price != null && price.shipAndHandling!=null){
						adapter.setShowShippingAndHandling(true);
					} else {
						adapter.setShowShippingAndHandling(false);
					}
														
					adapter.setProductDiscount(pricingDiscount);
					
					adapter.setProductPrice(price);
					adapter.notifyDataSetChanged();
					if(price != null){
						//tvTopTotalPrice.setText(price.totalPrice());
						tvBottomTotalPrice.setText(price.totalPrice());
						//pbWaitingTop.setVisibility(View.GONE);
						pbWaitingBottom.setVisibility(View.GONE);
						if(type == SendingOrderTask.ORDER_TYPE_HOME){
							if(price.taxWillBeCalculatedByRetailer){
								tvTaxDetail.setText(R.string.OrderSummary_TaxesS2H);
								tvTaxDetail.setVisibility(View.VISIBLE);
							} else {
								tvTaxDetail.setVisibility(View.INVISIBLE);
							}
						} else if(type == SendingOrderTask.ORDER_TYPE_STORE){
							if(price.taxWillBeCalculatedByRetailer){
								tvTaxDetail.setText(R.string.OrderSummary_Taxes);
								tvTaxDetail.setVisibility(View.VISIBLE);
							} else {
								tvTaxDetail.setVisibility(View.INVISIBLE);
							}
						}
						if (price.grandTotal != null) {
							orderTotelPrice =  price.grandTotal.price;
						}						
					}
					
					btBuy.setEnabled(true);
					
					//add by bing for set price some store
					StorePriceInfoList = null;
					if (price != null && price.lineItems != null) {
						StorePriceInfoList = new ArrayList<StorePriceInfo>(price.lineItems.size());
						for (LineItem lItem : price.lineItems) {
							if (lItem == null) continue;								
							if (lItem.productDescriptionId == null) continue;
							if (lItem.unitPrice == null) continue; 
							if (lItem.unitPrice.priceStr == null) continue; 
							StorePriceInfoList.add(new StorePriceInfo(lItem.productDescriptionId, lItem.unitPrice.priceStr));
							List<ProductInfo> products = app.products;
							if (products != null) {
								synchronized (products){
									for (ProductInfo pInfo : products) {
										if (pInfo == null) continue;
										if (pInfo.descriptionId == null) continue;																						
										if (pInfo.descriptionId.equals(lItem.productDescriptionId)) {											
											pInfo.price = lItem.unitPrice.priceStr;											
											break;
										}															
									}
								}								
							}
						}					
					}
				}				
				break;
			case PRICING_START:
				adapter.notifyDataSetChanged();
				//tvTopTotalPrice.setText("");
				tvBottomTotalPrice.setText("");
				//pbWaitingTop.setVisibility(View.VISIBLE);
				pbWaitingBottom.setVisibility(View.VISIBLE);
				btBuy.setEnabled(false);
				break;
			case PREPARED_ORDER_COMPLETELY:
				if(dialog != null){
					dialog.dismiss();
				} else if(sendingOrderDialog != null){
					sendingOrderDialog.dismiss();
				}
				String url = (String) msg.obj;

				//fixed RSSMOBILEPDC-1610 by bing wang on 2014-8-18
				payView.setViewSize().initWebViewData(url, client).setViewVisible();
				break;
			case PREPARED_ORDER_START:
				List<ImageInfo> images = UploadProgressUtil.allImages();
				boolean hasImageWait2Upload = UploadProgressUtil.isImageUploading(images,false);
				if(!hasImageWait2Upload){
					dialog = new InfoDialog.Builder(ShoppingCartActivity.this)
					.setCancelable(false)
					.setCanceledOnTouchOutside(false)
					.setMessage(getString(R.string.N2RUpload_SendingOrder))
					.setProgressBar(true)
					.create();
					dialog.show();
				} else {
					int totalImageNumber = images.size();
					int totalStep = 0;
					if(orderType == SendingOrderTask.ORDER_TYPE_HOME){
						totalStep = totalImageNumber + SendingOrderTask.getHomeOrderSteps(app.products);
					} else {
						totalStep = totalImageNumber + SendingOrderTask.getStoreOrderSteps(app.products);
					}
					sendingOrderDialog = new DialogSendingOrder(ShoppingCartActivity.this, totalImageNumber, totalStep, sendingOrderListener);
					sendingOrderDialog.show();
					Handler mHandler = sendingOrderDialog.getHandler();
					int sendingImageNumber = UploadProgressUtil.getUploadPicSuccessNum(images,false);
					ImageInfo image = UploadProgressUtil.getRunningUploadInfo(images,false);;
					Message message = new Message();
					message.what = DialogSendingOrder.STATE_UPLOADING;
					message.obj = image;
					message.arg1 = sendingImageNumber;
					mHandler.sendMessage(message);
				}
				break;
			case REFRESH_DELIVERY:
				refreshDeliveryButtons();
				int orderType = orderTypeRetailersSupported();
				if(orderType != SendingOrderTask.ORDER_TYPE_DEFAULT){
					destinationChangedListener.onDestinationChangd(orderType);
				}
				break;
			case SENDING_ORRDER_ERROR:
				showSendingOrderErrorDialog(R.string.N2RUpload_OrderUploadError);
				break;
			case STORE_NOT_SUPPORTED:
				showProductNotSupportDialog();
				break;
			case CAN_NOT_GETPRICE:
				showCanNotGetPriceDialog();
				break;	
			}
		}
		
	};
	
	private int orderTypeRetailersSupported(){
		boolean shipToHome = false;
		boolean pickInStore = false;
		for(Retailer retailer : availableRetailers){
			if(retailer.shipToHome && !retailer.cloLite){
				shipToHome = true;
			} else if(retailer.cloLite && SharedPreferrenceUtil.getBoolean(this, SharedPreferrenceUtil.ACCEPT_CLOLITE)){
				shipToHome = true;
			} else {
				pickInStore = true;
			}
		}

		//change by bing on 2014-11-10 for 1516 use the last selected delivery if is in Continue Shopping
		if(app.isUseDoMore){
			if ((app.orderType == SendingOrderTask.ORDER_TYPE_HOME && shipToHome) || 
					(app.orderType == SendingOrderTask.ORDER_TYPE_STORE && pickInStore)	||
						app.orderType == SendingOrderTask.ORDER_TYPE_DEFAULT ) {
				return app.orderType;
			}
		}

		if(shipToHome && !pickInStore){
			return SendingOrderTask.ORDER_TYPE_HOME;
		} else if(!shipToHome && pickInStore){
			return SendingOrderTask.ORDER_TYPE_STORE;
		} else {
			return SendingOrderTask.ORDER_TYPE_DEFAULT;
		}
	}

	WebViewClient client = new WebViewClient(){

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.e(TAG, "onPageFinished url:" + url);
			HashMap<String, String> attr = new HashMap<String, String>();
			if(url.contains("PaymentDone.aspx")){
				String status = ShoppingCartUtil.getPaymentStatus(url);
				if(ShoppingCartUtil.PAYMENT_SUCCESS.equals(status)){
					saveOrder(order, orderType, successfulCart);
					switchToOrderSummary(order);
				} 
				else if(ShoppingCartUtil.PAYMENT_ABORTED.contains(status)){
					attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_ORD_NOT_COMP_REASON, RSSTabletLocalytics.LOCALYTICS_VALUE_PAYMENT_ERROR);
					showPaymentErrorDialog(R.string.N2RShoppingCart_PaymentAborted);
				} 
				else if(ShoppingCartUtil.PAYMENT_CANCELED.contains(status)){
					attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_ORD_NOT_COMP_REASON, RSSTabletLocalytics.LOCALYTICS_VALUE_PAYMENT_CANCELLED);
					showPaymentErrorDialog(R.string.N2RShoppingCart_PaymentCancelled);
				} 
				else if(ShoppingCartUtil.PAYMENT_FAILED.contains(status)){
					attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_ORD_NOT_COMP_REASON, RSSTabletLocalytics.LOCALYTICS_VALUE_PAYMENT_ERROR);
					showPaymentErrorDialog(R.string.N2RShoppingCart_PaymentFailed);
				} 
				else if(ShoppingCartUtil.PAYMENT_ERROR.contains(status)){
					attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_ORD_NOT_COMP_REASON, RSSTabletLocalytics.LOCALYTICS_VALUE_PAYMENT_ERROR);
					showPaymentErrorDialog(R.string.N2RShoppingCart_PaymentCumulusError);
				} 
				else if(ShoppingCartUtil.PAYMENT_UNEXPECTED.contains(status)){
					attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_ORD_NOT_COMP_REASON, RSSTabletLocalytics.LOCALYTICS_VALUE_PAYMENT_ERROR);
				}
				if(!ShoppingCartUtil.PAYMENT_SUCCESS.equals(status)){
					localyticsTrackOrderFailed(attr);
				}
			}
		}
		
	};
	
	private void showPaymentErrorDialog(int messageId){
		if(payView.isShowing()){
			payView.dismiss();
		}
		if(notSupportStoreDialog!=null && notSupportStoreDialog.isShowing()) {
			notSupportStoreDialog.dismiss();
		}
		if(dialog!=null && dialog.isShowing()){
			return;
		}
		dialog = new InfoDialog.Builder(ShoppingCartActivity.this)
		.setCancelable(false)
		.setCanceledOnTouchOutside(false)
		.setMessage(messageId)
		.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
		if(!dialog.isShowing() && !isFinishing()){
			dialog.show();
		}
	}
	
	
	
	private void showSendingOrderErrorDialog(int messageId){
		if(this!=null && !this.isFinishing() && dialog!=null && dialog.isShowing()){
			dialog.dismiss();			
		}
		if(notSupportStoreDialog!=null && notSupportStoreDialog.isShowing()) {
			notSupportStoreDialog.dismiss();
		}
		
		if(sendingOrderDialog!=null && sendingOrderDialog.isShowing()){
			sendingOrderDialog.dismiss();
		}
		dialog = new InfoDialog.Builder(ShoppingCartActivity.this)
		.setCancelable(false)
		.setCanceledOnTouchOutside(false)
		.setMessage(messageId)
		.setNegativeButton(R.string.d_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				btBuy.performClick();
			}
		})
		.create();
		if(!dialog.isShowing() && !isFinishing()){
			dialog.show();
		}
	}
	
	private void showProductNotSupportDialog(){
		if(dialog!=null && dialog.isShowing()){
			dialog.dismiss();
		}
		if(sendingOrderDialog!=null && sendingOrderDialog.isShowing()){
			sendingOrderDialog.dismiss();
		}
		
		// change by bing on 2014-11-11 for reduce the dialog pop more number
		if(notSupportStoreDialog!=null && notSupportStoreDialog.isShowing()) return;				
		notSupportStoreDialog = new InfoDialog.Builder(ShoppingCartActivity.this)
		.setCancelable(false)
		.setCanceledOnTouchOutside(false)
		.setMessage(R.string.ProductNotAvailableAtStore)
		.setNegativeButton(R.string.findstore, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent mIntent = new Intent(ShoppingCartActivity.this, StoreSelectActivity.class);
				mIntent.putExtra("fromCart", true);
				startActivityForResult(mIntent, 0);
			}
		}).setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
		if(!notSupportStoreDialog.isShowing() && !isFinishing()){
			notSupportStoreDialog.show();
		}		
	}
	
	private void showCanNotGetPriceDialog() {
		if(dialog!=null && dialog.isShowing()){
			dialog.dismiss();
		}
		if(notSupportStoreDialog!=null && notSupportStoreDialog.isShowing()) {
			notSupportStoreDialog.dismiss();
		}
		if(sendingOrderDialog!=null && sendingOrderDialog.isShowing()){
			sendingOrderDialog.dismiss();
		}
		dialog = new InfoDialog.Builder(ShoppingCartActivity.this)
		.setCancelable(false)
		.setCanceledOnTouchOutside(false)
		.setMessage(R.string.N2RShoppingCart_ErrorGettingPrice)
		.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
		if(!dialog.isShowing() && !isFinishing()){
			dialog.show();
		}
		
	}
	
	private boolean checkOrderMaximumCost(String currentRetailerId){
		boolean isReTval = false;
		if (orderTotelPrice <= 0) return isReTval;
		if (currentRetailerId == null) return isReTval;
		if ("".equals(currentRetailerId)) return isReTval;
		if (availableRetailers == null) return isReTval;
		for(Retailer retailer : availableRetailers){
			if (retailer == null ) continue;
			if (retailer.id == null ) continue;	
			if (!retailer.id.equals(currentRetailerId)) continue;
			if (retailer.cartLimit == null) break;
			float priceLimit = 	retailer.cartLimit.price;
			if (priceLimit != 0 && orderTotelPrice > priceLimit) {
				String prompt = getResources().getString(R.string.N2RShoppingCart_TooExpensive);
				prompt = prompt.replaceFirst("%@", retailer.cartLimit.PriceStr);
				new InfoDialog.Builder(this).setMessage(prompt)
				.setNeturalButton(R.string.d_ok, null)
				.create().show();
				isReTval = true;
				return isReTval;
			}
			break;
		}
		return isReTval;
	}
	
	private boolean checkOrderMinimumCost(String currentRetailerId){	
		boolean isReTval = false;
		if (orderTotelPrice <= 0) return isReTval;
		if (currentRetailerId == null) return isReTval;
		if ("".equals(currentRetailerId)) return isReTval;
		if (availableRetailers == null) return isReTval;
		for(Retailer retailer : availableRetailers){
			if (retailer == null ) continue;
			if (retailer.id == null ) continue;	
			if (!retailer.id.equals(currentRetailerId)) continue;
			if (retailer.cartMinimumLimit == null) break;
			float priceMinimumLimit = 	retailer.cartMinimumLimit.price;
			if (priceMinimumLimit != 0 && orderTotelPrice < priceMinimumLimit) {					
				String prompt = getResources().getString(R.string.N2RShoppingCart_NotEnoughOrdered);
				prompt = prompt.replaceFirst("%@", retailer.cartMinimumLimit.PriceStr);
				new InfoDialog.Builder(this).setMessage(prompt)
				.setNeturalButton(R.string.d_ok, null)
				.create().show();		
				isReTval = true;
				return isReTval;
			}
			break;
		}					
		return isReTval;
	}
	
	private void showOrderDestinationDialog(StoreInfo store, LocalCustomerInfo customer){		
		//fixed RSSMOBILEPDC-1853
		String currentRetailerId = SharedPreferrenceUtil.getString(ShoppingCartActivity.this, SharedPreferrenceUtil.SELECTED_RETAILER_ID);				
		if (checkOrderMaximumCost(currentRetailerId)) return;
		if (checkOrderMinimumCost(currentRetailerId)) return;		
		
		final DialogConfirmDestination confirmDestDialog = store!=null ? 
				new DialogConfirmDestination(this, R.layout.dialog_confirm_destination, store) :
					new DialogConfirmDestination(this, R.layout.dialog_confirm_destination, customer);
		
		confirmDestDialog.getBtYes().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				confirmDestDialog.dismiss();
				ShoppingCartUtil.judgeImageDownload(ShoppingCartActivity.this,false,true);
				ShoppingCartUtil.judgeImageUpload(ShoppingCartActivity.this,false,true);
				sendingOrder = new SendingOrderTask(ShoppingCartActivity.this, orderType, sendingOrderListener);
				new Thread(sendingOrder).start();
			}
		});
		confirmDestDialog.getBtNo().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				confirmDestDialog.dismiss();
			}
		});
		confirmDestDialog.show();
	}
	
	private void localyticsTrackOrderSuccess(){
		HashMap<String, String> attr = new HashMap<String, String>();
		if(app.localytics.getQuantityChagnes()>=0){
			
		}
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_QUANTITY_CHANGES, RSSTabletLocalytics.LOCALYTICS_VALUE_UNKNOWN);
		String cartRemovals = "0";
		if(app.localytics.getCartRemovals()>5){
			cartRemovals = "6 +";
		} else if(app.localytics.getCartRemovals() == 5){
			cartRemovals = "5";
		} else if(app.localytics.getCartRemovals() == 5){
			cartRemovals = "4";
		} else if(app.localytics.getCartRemovals() == 5){
			cartRemovals = "3";
		} else if(app.localytics.getCartRemovals() == 5){
			cartRemovals = "2";
		} else if(app.localytics.getCartRemovals() == 5){
			cartRemovals = "1";
		}
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_CART_REMOVALS, cartRemovals);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_DELIVERY, app.localytics.getDelivery());
		if(app.localytics.isUserInfoChanged()){
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_USER_INFO_CHANGED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		} else {
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_USER_INFO_CHANGED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		}
		if(app.localytics.isShipAddChanged()){
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_SHIP_ADD_CHANGED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		} else {
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_SHIP_ADD_CHANGED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		}
		RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_ORDER_ACTIVITY_SUMMARY, attr);
	}
	
	private void localyticsTrackOrderFailed(HashMap<String, String> attr){
		RSSLocalytics.recordLocalyticsPageView(ShoppingCartActivity.this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_ORDER_NOT_COMPLETE);
		RSSLocalytics.recordLocalyticsEvents(ShoppingCartActivity.this, RSSTabletLocalytics.LOCALYTICS_EVENT_ORDER_NOT_COMPLETE, attr);
	}
	
	SendingOrderTaskListener sendingOrderListener = new SendingOrderTaskListener() {
		
		@Override
		public void onTaskFailed(int errorCode) {
			Log.e(TAG, "Sending Order failed: " + errorCode);
			handler.obtainMessage(SENDING_ORRDER_ERROR).sendToTarget();
			HashMap<String, String> attr = new HashMap<String, String>();
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_ORD_NOT_COMP_REASON, RSSTabletLocalytics.LOCALYTICS_UNCLASSIFIED_ERROR);
			localyticsTrackOrderFailed(attr);
		}
		
		@Override
		public void onStoreOrderTaskSucceed(final NewOrder order, String url, Cart cart) {
			ShoppingCartActivity.this.stopUploadService();
			FilePathConstant.isExternalExceed(ShoppingCartActivity.this,AppConstants.EXTERNAL_MAX_SIZE);
			ShoppingCartActivity.this.order = order;
			successfulCart = cart;
			Retailer retailer = app.getCurrentRetailer(ShoppingCartActivity.this);
			if(retailer.payOnline){
				handler.obtainMessage(PREPARED_ORDER_COMPLETELY, url).sendToTarget();
			} else {
				saveOrder(order, SendingOrderTask.ORDER_TYPE_STORE, successfulCart);
				switchToOrderSummary(order);
			}
		}
		
		@Override
		public void onHomeOrderTaskSucceed(NewOrder order, String url, Cart cart) {
			ShoppingCartActivity.this.stopUploadService();
			FilePathConstant.isExternalExceed(ShoppingCartActivity.this,AppConstants.EXTERNAL_MAX_SIZE);
			ShoppingCartActivity.this.order = order;
			successfulCart = cart;
			Retailer retailer = app.getCurrentRetailer(ShoppingCartActivity.this);
			if(retailer.payOnline){
				handler.obtainMessage(PREPARED_ORDER_COMPLETELY, url).sendToTarget();
			} else {
				saveOrder(order, SendingOrderTask.ORDER_TYPE_HOME, successfulCart);
				switchToOrderSummary(order);
			}
		}

		@Override
		public void onSendingOrderStart() {
			handler.obtainMessage(PREPARED_ORDER_START).sendToTarget();
			localyticsTrackImageSource();
			ArrayList<String> products =  (ArrayList<String>) ShoppingCartUtil.getProductDescriptionIDList(productInfos);
			ArrayList<List<ProductInfo>> groupProducts = new ArrayList<List<ProductInfo>>();
			for(String type : products){
				groupProducts.add(ShoppingCartUtil.getGroupProductInfoList(productInfos, type));
			}
			RSSLocalytics.recordLocalyticsPageView(ShoppingCartActivity.this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_ORDER_START);
			RSSLocalytics.recordLocalyticsEvents(ShoppingCartActivity.this, RSSTabletLocalytics.LOCALYTICS_EVENT_ORDER_START);
			localyticsTrackOrderLineItem(products, groupProducts);
		}

		@Override
		public void progress(int step, int totalStep) {
			if(sendingOrderDialog!=null && sendingOrderDialog.isShowing()){
				List<ImageInfo> images = UploadProgressUtil.allImages();
				Handler mHandler = sendingOrderDialog.getHandler();
				Message message = new Message();
				if(step == 0){
					int sendingImageNumber = UploadProgressUtil.getUploadPicSuccessNum(images,false);
					ImageInfo image = UploadProgressUtil.getRunningUploadInfo(images,false);
					message.what = DialogSendingOrder.STATE_UPLOADING;
					message.obj = image;
					message.arg1 = sendingImageNumber;
				} else {
					message.what = DialogSendingOrder.STATE_SENDING_ORDER;
					message.arg1 = images.size() + step;
				}
				mHandler.sendMessage(message);
			}
		}

		@Override
		public void onTaskCanceled() {
			if(sendingOrder != null){
				sendingOrder.cancelTask();
			}
		}

		@Override
		public void onStoreNotSupportAllProducts() {
			handler.obtainMessage(STORE_NOT_SUPPORTED).sendToTarget();
		}
	};
	
	private void localyticsTrackImageSource(){
		boolean facebookUsed = false;
		boolean localPhotoUsed = false;
		if(app.chosenList != null){
			for(ImageInfo image : app.chosenList){
				String source = image.fromSource;
				if("Facebook".equalsIgnoreCase(source)){
					facebookUsed = true;
				}
				if("Photos".equalsIgnoreCase(source)){
					localPhotoUsed = true;
				}
				// TODO unknown image source
			}
		}
		HashMap<String, String> attr = new HashMap<String, String>();
		if(facebookUsed){
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_FACEBOOK_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		} else {
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_FACEBOOK_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		}
		if(localPhotoUsed){
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_LOCAL_PHOTOS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		} else {
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_LOCAL_PHOTOS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		}
		if(couponApplied){
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_COUPON_APPLIED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		} else {
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_COUPON_APPLIED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		}
		Log.e(TAG, RSSTabletLocalytics.LOCALYTICS_KEY_COUPON_APPLIED + ":" + attr.get(RSSTabletLocalytics.LOCALYTICS_KEY_COUPON_APPLIED));
		RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_CREATION_SUMMARY, attr);
	}
	
	// Track Order Line Item events
	private void localyticsTrackOrderLineItem(List<String> products, ArrayList<List<ProductInfo>> groupProducts){
		for(int i=0; i<products.size(); i++){
			HashMap<String, String> attr = new HashMap<String, String>();
			String proID = products.get(i);
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_PRODUCT_ID, proID);
			List<ProductInfo> pros = groupProducts.get(i);
			int proQuantity = 0;
			for(ProductInfo p : pros){
				proQuantity += p.num;
			}
			String strProQuantity = "0";
			if(proQuantity>0){
				if(proQuantity>50){
					strProQuantity = "51 +";
				} else if(proQuantity>20){
					strProQuantity = "21 - 50";
				} else if(proQuantity>15){
					strProQuantity = "16 - 20";
				} else if(proQuantity>10){
					strProQuantity = "11 - 15";
				} else if(proQuantity>5){
					strProQuantity = "06 - 10";
				} else if(proQuantity==5){
					strProQuantity = "5";
				} else if(proQuantity==4){
					strProQuantity = "4";
				} else if(proQuantity==3){
					strProQuantity = "3";
				} else if(proQuantity==2){
					strProQuantity = "2";
				} else if(proQuantity==1){
					strProQuantity = "1";
				}
			}
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_PRODUCT_QUANTITY, strProQuantity);
			String productImages = "000";
			int numOfImages = 0;
			if(pros.size()>0 && pros.get(0).productType.equals(AppConstants.printType)){
				numOfImages = 1;
			} else if(pros.size()>0 && pros.get(0).productType.equals(AppConstants.bookType)){
				List<Photobook> photobooks = RssTabletApp.getInstance().chosenBookList;
				for(Photobook photobook : photobooks){
					if(photobook.proDescId.equals(pros.get(0).descriptionId)){
						numOfImages += photobook.chosenpics.size();
					}
				}
				if(proQuantity!=0){
					numOfImages = numOfImages / proQuantity;
				}
			}
			if(numOfImages>400){
				productImages = "400 +";
			} else if(numOfImages>350){
				productImages = "351 - 400";
			} else if(numOfImages>300){
				productImages = "301 - 350";
			} else if(numOfImages>250){
				productImages = "251 - 300";
			} else if(numOfImages>200){
				productImages = "201 - 250";
			} else if(numOfImages>150){
				productImages = "151 - 200";
			} else if(numOfImages>100){
				productImages = "101 - 150";
			} else if(numOfImages>90){
				productImages = "091 - 100";
			} else if(numOfImages>80){
				productImages = "081 - 090";
			} else if(numOfImages>70){
				productImages = "071 - 080";
			} else if(numOfImages>60){
				productImages = "061 - 070";
			} else if(numOfImages>50){
				productImages = "051 - 060";
			} else if(numOfImages>40){
				productImages = "041 - 050";
			} else if(numOfImages>30){
				productImages = "031 - 040";
			} else if(numOfImages>20){
				productImages = "021 - 030";
			} else if(numOfImages>10){
				productImages = "011 - 020";
			} else if(numOfImages>0){
				productImages = "001 - 010";
			} else {
				productImages = "000";
			}
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_PRODUCT_IMAGES, productImages);
			RSSLocalytics.recordLocalyticsEvents(ShoppingCartActivity.this, RSSTabletLocalytics.LOCALYTICS_EVENT_ORDER_LINE_ITEM, attr);
		}
	}
	
	public interface DestinationChangedListener{
		void onDestinationChangd(int type);
	}
	
	private void saveOrder(NewOrder order, int orderType, Cart cart){
		// when this method is called, that means the Order has been sent successfully, and should clear the last failed cart id
		app.setLastFailedCartID("");
		OrderDetail detail = new OrderDetail();
		detail.setEmail(customer.getCusEmail());
		
		String tempOrderDetail = "";
		if(successfulCart != null){
			if(successfulCart.pricing != null){
				Pricing price = successfulCart.pricing;
				for(LineItem item :price.lineItems){
					tempOrderDetail += item.quantity + " - " + item.name + "\n";
					if(item.included != null){
						for(LineItem tempItem : item.included){
							if(cart.discounts!=null && cart.discounts.length>0){
								if(tempItem.name.equals(cart.discounts[0].localizedName)){
									continue;
								}
							}
							tempOrderDetail += tempItem.quantity + " - " + tempItem.name + "\n";
						}
					}
				}
			}
		}
		detail.setOrderDetail(tempOrderDetail);
		detail.setTaxWillBeCalculatedByRetailer(cart.pricing.taxWillBeCalculatedByRetailer);
		detail.setOrderId(order.orderId);
		detail.setOrderTime(new Date().toLocaleString());
		detail.setOrderTotal(order.totalPrice());
		if(orderType == SendingOrderTask.ORDER_TYPE_HOME){
			detail.setCustomerInfo(customer);
		} else if(orderType == SendingOrderTask.ORDER_TYPE_STORE){
			detail.setStoreInfo(store);
		}
		
		String orderHistroy = SharedPreferrenceUtil.getString(this, AppConstants.OrderHistory);
		if(orderHistroy.contains(detail.getOrderId())){
			return;
		}
		if("".equals(orderHistroy)){
			orderHistroy += detail.getOrderId();
		} else {
			orderHistroy += "," + detail.getOrderId();
		}
		detail.save(this);
		SharedPreferrenceUtil.setString(this, AppConstants.OrderHistory, orderHistroy);
	}
	
	private void switchToOrderSummary(final NewOrder order){
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				localyticsTrackOrderSuccess();				
				if(payView !=null && payView.isShowing()){
					payView.dismiss();
				}
				if(dialog!=null && dialog.isShowing()){
					dialog.dismiss();
				}
				if(notSupportStoreDialog!=null && notSupportStoreDialog.isShowing()) {
					notSupportStoreDialog.dismiss();
				}
				if(sendingOrderDialog!=null && sendingOrderDialog.isShowing()){
					sendingOrderDialog.dismiss();
				}
				Intent mIntent = new Intent(ShoppingCartActivity.this, OrderSummaryActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("orderId", order.orderId);
				mIntent.putExtras(bundle);
				startActivity(mIntent);
				ShoppingCartActivity.this.finish();
			}
		});
	}
	/**
	 * fixed for RSSMOBILEPDC-1867
	 * before get price need check the store available;
	 * add by song
	 */
	private boolean checkStoreAvailable() {
		WebService webService = new WebService(ShoppingCartActivity.this);
		try {
			boolean validOrder = webService.checkStoreTask(store.retailerID, store.id, ShoppingCartUtil.getProductDescriptionIDs(app.products));
			if(!validOrder && isShoppingCardLive){
				handler.obtainMessage(STORE_NOT_SUPPORTED).sendToTarget();
				return validOrder;
			}
		} catch (RssWebServiceException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private boolean needGetPriceFromServer(){
		boolean needGetPriceFromServer = true;
		switch (orderType){
		case SendingOrderTask.ORDER_TYPE_DEFAULT:
			if (ShoppingCartUtil.isShowsMSRPPricing()){
				needGetPriceFromServer = true;
			}else {
				needGetPriceFromServer = false;
			}	
			break;
		case SendingOrderTask.ORDER_TYPE_STORE:
			if (store != null && store.id != null && !"".equals(store.id)) {	
				if (!checkStoreAvailable()){
					needGetPriceFromServer = false;
				}else {
					needGetPriceFromServer = true;
				}
			}else {
				if (ShoppingCartUtil.isShowsMSRPPricing()){
					needGetPriceFromServer = true;
				}else {
					needGetPriceFromServer = false;
				}	
			}			
			break;
		case SendingOrderTask.ORDER_TYPE_HOME:
			needGetPriceFromServer = true;
			break;
		}
		RssTabletApp.getInstance().needGetPriceFromServer = needGetPriceFromServer;
		return needGetPriceFromServer;
	}
	
	public void showCouponTerms(String url){
		Intent intent = new Intent(this, CouponTermsActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("coupon_terms", url);
		intent.putExtras(bundle);
		startActivity(intent);
	}
}
