package com.kodak.rss.tablet.activities;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kodak.rss.core.bean.OrderDetail;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.util.ShoppingCartUtil;
import com.kodak.rss.tablet.view.MapFragment;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class OrderSummaryActivity extends BaseNetActivity {
	private final String TAG = OrderSummaryActivity.class.getSimpleName();
	
	private TextView tvTitle;
	private TextView tvEmail;
	private TextView tvOrderId;
	private TextView tvOrderDate;
	private TextView tvOrderTotal;
	private TextView tvDesTitle;
	private TextView tvDesDetail;
	private TextView tvTaxes;
	private TextView tvOrderDetail;
	private Button btDone;
	private View mapPart;
	private GoogleMap googleMap;
	
	private OrderDetail orderDetail;
	private boolean justPreview = false;
	
	private RssTabletApp app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = RssTabletApp.getInstance();
		setContentView(R.layout.activity_order_summary_hd);
		if(getIntent()!=null){
			Bundle bundle = getIntent().getExtras();
			if(bundle!=null){
				justPreview = bundle.getBoolean("justPreview");
				String orderId = bundle.getString("orderId");
				orderDetail = OrderDetail.load(this, orderId);
			}
		}
		if(!justPreview){
			RSSLocalytics.recordLocalyticsPageView(this, RSSTabletLocalytics.LOCALYTICS_EVENT_ORDER_SUCCESS);
			RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_ORDER_SUCCESS);
		}
		initViews();
	}
	
	private void initViews(){
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvEmail = (TextView) findViewById(R.id.tv_order_email);
		tvOrderId = (TextView) findViewById(R.id.tv_order_id);
		tvOrderDate = (TextView) findViewById(R.id.tv_order_date);
		tvOrderTotal = (TextView) findViewById(R.id.tv_order_total);
		tvDesTitle = (TextView) findViewById(R.id.tv_des_title);
		tvDesDetail = (TextView) findViewById(R.id.tv_des_detail);
		tvOrderDetail = (TextView) findViewById(R.id.tv_order_detail);
		tvTaxes = (TextView) findViewById(R.id.tv_taxes);
		btDone = (Button) findViewById(R.id.btDone);
		mapPart = findViewById(R.id.mapview_container);
		googleMap = ((MapFragment) getSupportFragmentManager().findFragmentById(R.id.mv_store_mapview)).getMap();
		UiSettings mapSetting = googleMap.getUiSettings();
		mapSetting.setAllGesturesEnabled(false);
		mapSetting.setMyLocationButtonEnabled(false);
		mapSetting.setRotateGesturesEnabled(false);
		mapSetting.setScrollGesturesEnabled(false);
		mapSetting.setZoomControlsEnabled(false);
		mapSetting.setZoomGesturesEnabled(false);
		mapSetting.setTiltGesturesEnabled(false);
		
		tvOrderId.getPaint().setFakeBoldText(true);
		btDone.setVisibility(View.VISIBLE);
		
		tvTitle.setText(R.string.N2ROrderConfirmation_Title);
		if(orderDetail!=null){
			tvEmail.setText(orderDetail.getEmail());
			tvOrderId.setText(orderDetail.getOrderId());
			tvOrderDate.setText(orderDetail.getOrderTime());
			tvOrderTotal.setText(orderDetail.getOrderTotal());
			if(null != orderDetail.getStoreInfo()){
				StoreInfo store = orderDetail.getStoreInfo();
				tvDesTitle.setText(R.string.SettingsScreen_StoreInfo);
				tvDesDetail.setText(ShoppingCartUtil.formatStoreDetail(store));
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(store.latitude, store.longitude), 15));
				googleMap.clear();
				
				MarkerOptions marker=null;
				try {
					marker = StoreSelectActivity.createMapMaker(store);
					googleMap.addMarker(marker) ;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mapPart.setVisibility(View.VISIBLE);
				if(orderDetail.isTaxWillBeCalculatedByRetailer()){
					tvTaxes.setVisibility(View.VISIBLE);
				} else {
					tvTaxes.setVisibility(View.INVISIBLE);
				}
			} else {
				tvDesTitle.setText(R.string.N2RShoppingCart_ShippingInformationTitle);
				tvDesDetail.setText(ShoppingCartUtil.formatShippingDetail(orderDetail.getCustomerInfo()));
				mapPart.setVisibility(View.GONE);
				tvTaxes.setVisibility(View.INVISIBLE);
			}
			tvOrderDetail.setText(orderDetail.getOrderDetail());
		}
		
		btDone.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v.getId() == R.id.btDone){
			if(justPreview){
				finish();
			} else {
				/*DialogDismissListener listener = new DialogDismissListener() {
					
					@Override
					public void onDismiss() {
						RssTabletApp.getInstance().startOver();
					}
				};
				DialogFeedback feedback = new DialogFeedback();
				feedback.createDialog(this, getWindow().getWindowManager().getDefaultDisplay().getHeight(), listener);*/

				//start change by bing wang for fixed RSSMOBILEPDC-2089 on 2015-2-26
//				RssTabletApp.getInstance().startOver();			
				android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {							
						String packageName = OrderSummaryActivity.this.getPackageName();
						Uri uri = null;
						Intent goToMarket = null;						
						try {
							uri = Uri.parse("market://details?id=" + packageName);
							goToMarket = new Intent(Intent.ACTION_VIEW, uri);
							startActivity(goToMarket);
						} catch (ActivityNotFoundException e) {
							uri = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
							goToMarket = new Intent(Intent.ACTION_VIEW, uri);
							startActivity(goToMarket);
						}	
						
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								RssTabletApp.getInstance().startOver();
							}
						}, 200);
					}				
				};
				android.content.DialogInterface.OnClickListener cancelOnClickListener  = new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {							
						RssTabletApp.getInstance().startOver();			
					}		
				};
					
				String promptStr = getResources().getString(R.string.OrderScreen_RateApp);
				new InfoDialog.Builder(this).setMessage(promptStr)
				.setPositiveButton(getText(R.string.d_ok), yesOnClickListener)					
				.setNeturalButton(R.string.cancel, cancelOnClickListener)
				.create().show();
				//end change by bing wang for fixed RSSMOBILEPDC-2089 on 2015-2-26
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}
