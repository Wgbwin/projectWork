package com.kodakalaris.kodakmomentslib.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.menu.MMenuActivity;
import com.kodakalaris.kodakmomentslib.activity.orderhistory.MOrderHistoryActivity;

public class MHomeActivity extends BaseHomeActivity{
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_m_home);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initBaseHomeData();
		getViews();
		initData();
		setEvents();
	}

	private void getViews() {
	}

	private void initData() {
		
	}

	private void setEvents() {
		vRelaLyCart.setOnClickListener(menuItemClickListener);
		vRelaLyOrder.setOnClickListener(menuItemClickListener);
		vRelaLyProfile.setOnClickListener(menuItemClickListener);
		vRelaLySettings.setOnClickListener(menuItemClickListener);
		vRelaLyClearCart.setOnClickListener(menuItemClickListener);
		vRelaLyGallery.setOnClickListener(menuItemClickListener);
	}

	private OnClickListener menuItemClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			Intent intent = null;
			String action_value = "";
			if(id == vRelaLyCart.getId()){
				action_value = MMenuActivity.ACTION_VALUE_CART;
			} 
			else if(id == vRelaLyOrder.getId()){
				action_value = MMenuActivity.ACTION_VALUE_ORDER;
				intent = new Intent(MHomeActivity.this, MOrderHistoryActivity.class);
			} 
			else if(id == vRelaLyProfile.getId()){
				action_value = MMenuActivity.ACTION_VALUE_PROFILE;
			} 
			else if(id == vRelaLySettings.getId()){
				action_value = MMenuActivity.ACTION_VALUE_SETTINGS;
				intent = new Intent(MHomeActivity.this,MMenuActivity.class);
				intent.putExtra(MMenuActivity.ACTION_KEY, action_value);
				vDrawerLayout.closeDrawer(Gravity.LEFT);
			} 
			else if(id == vRelaLyClearCart.getId()){
				action_value = MMenuActivity.ACTION_VALUE_CLEARCART;
			} 
			else if(id == vRelaLyGallery.getId()){
				action_value = MMenuActivity.ACTION_VALUE_GALLERY;
			}
			if(intent != null){
				startActivity(intent);
			}
			
		}
	};
		
}
