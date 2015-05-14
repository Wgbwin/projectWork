package com.kodakalaris.kodakmomentslib.activity.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.printsizeselection.TPrintSizeSelectionActivity;

public class THomeActivity extends BaseHomeActivity {
	private Context mContext;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_t_home);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mContext = THomeActivity.this;
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
		vLisvProducts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				switch (position - 1) {
				case 0:
					Intent i = new Intent();
					i.setClass(mContext, TPrintSizeSelectionActivity.class);
					startActivity(i);
					((Activity) mContext).finish();
					break;
				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					break;
				case 5:
					break;
				}
			}
		});
	}
}
