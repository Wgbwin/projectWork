package com.kodakalaris.kodakmomentslib.activity.orderconfirmation;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kodakalaris.kodakmomentslib.AppManager;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.adapter.mobile.OrderConfirmationProductsAdapter;
import com.kodakalaris.kodakmomentslib.bean.items.OrderConfirmationProductItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Cart;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Pricing.LineItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo.StoreAddress;
import com.kodakalaris.kodakmomentslib.widget.LinearListLayout;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;

public class MOrderConfirmationActivity extends BaseOrderConfirmationActivity {
	
	private TextView vTxtDate;
	private TextView vTxtTime;
	private TextView vTxtOrderID;
	private TextView vTxtDeliveryMethod;
	private TextView vTxtPickName;
	private TextView vTxtPickAddress1;
	private TextView vTxtPickAddress2;
	private TextView vTxtPickPhone;
	private TextView vTxtPickTime;
	private TextView vTxtTotalPrice;
	private MActionBar vActionBar;
	private LinearListLayout vLisvProducts;
	private GoogleMap vGoogleMap;
	
	
	private OrderConfirmationProductsAdapter mProductsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_order_confirmation);
		
		initViews();
		initData();
		initEvents();
	}
	
	private void initViews() {
		vTxtDate = (TextView) findViewById(R.id.txt_date);
		vTxtTime = (TextView) findViewById(R.id.txt_time);
		vTxtOrderID = (TextView) findViewById(R.id.txt_orderId);
		
		vTxtDeliveryMethod = (TextView) findViewById(R.id.txt_delivery_method);
		vTxtPickName = (TextView) findViewById(R.id.txt_pick_name);
		vTxtPickAddress1 = (TextView) findViewById(R.id.txt_pick_address1);
		vTxtPickAddress2 = (TextView) findViewById(R.id.txt_pick_address2);
		vTxtPickPhone = (TextView) findViewById(R.id.txt_pick_phone);
		vTxtTotalPrice = (TextView) findViewById(R.id.txt_total_price);
		vLisvProducts = (LinearListLayout) findViewById(R.id.listv_products);
		vActionBar = (MActionBar) findViewById(R.id.actionbar);
		if(isPreviewOrder){
			vActionBar.setRightButtonVisiable(false);
			vActionBar.setLeftButtonImage(R.drawable.icon_back);
			vActionBar.setOnLeftButtonClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
		
		
		vGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.fragment_map)).getMap();
		UiSettings mapSetting = vGoogleMap.getUiSettings();
		mapSetting.setAllGesturesEnabled(false);
		mapSetting.setMyLocationButtonEnabled(false);
		mapSetting.setRotateGesturesEnabled(false);
		mapSetting.setScrollGesturesEnabled(false);
		mapSetting.setZoomControlsEnabled(false);
		mapSetting.setZoomGesturesEnabled(false);
		mapSetting.setTiltGesturesEnabled(false);
		
		//mapFragment.getMap().addMarker(new MarkerOptions().position(new LatLng(20, 20)).title("Store"));
	}
	
	private void initData() {
		vTxtDate.setText(mOrderDetail.getOrderDate());
		vTxtTime.setText(mOrderDetail.getOrderTime());
		vTxtOrderID.setText(mOrderDetail.getOrderId());
		StoreInfo store = mOrderDetail.getStoreInfo();
		if(store != null){
			vTxtPickName.setText(store.name);
			StoreAddress address = store.address;
			vTxtPickAddress1.setText(address.address1);
			vTxtPickAddress2.setText(address.city + ", " + address.stateProvince + " " + address.postalCode);
			vGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(store.latitude, store.longitude), 15));
			vGoogleMap.clear();
			LatLng latlng = new LatLng(store.latitude, store.longitude);
			MarkerOptions markerOptions = new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location)).flat(true);
			vGoogleMap.addMarker(markerOptions);
		}
		
		
		
		List<OrderConfirmationProductItem> list = new ArrayList<OrderConfirmationProductItem>();
		Cart cart = mOrderDetail.getCart();
		for(LineItem item : cart.pricing.lineItems){
			list.add(new OrderConfirmationProductItem(item.name, item.quantity));
		}
		vTxtTotalPrice.setText(cart.pricing.totalPrice());
		
		mProductsAdapter = new OrderConfirmationProductsAdapter(this, list);
		vLisvProducts.setAdapter(mProductsAdapter);
	}
	
	private void initEvents() {
		vActionBar.setOnRightButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppManager.getInstance().startOver();
				finish();
			}
		});
	}

}
