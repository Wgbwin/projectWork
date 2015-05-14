package com.kodakalaris.kodakmomentslib.activity.orderhistory;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.kodakalaris.kodakmomentslib.DataKey;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.BaseActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.OrderHistroyAdapter;
import com.kodakalaris.kodakmomentslib.bean.OrderDetail;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;

public abstract class BaseOrderHistoryActivity extends BaseActivity {
	
	private ListView vLvOrders;
	private OrderHistroyAdapter mOrdersAdapter;
	private List<OrderDetail> mOrders;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView();
		initView();
		initData();
	}
	
	protected abstract void setContentView();
	
	private void initData(){
		String orders = SharedPreferrenceUtil.getString(this, DataKey.ORDER_HISTORY);
		try{
			String[] orderIds = orders.split(",");
			if(orderIds != null){
				mOrders = new ArrayList<OrderDetail>();
				// order the orders by date
				for(int i=orderIds.length-1; i>=0; i--){
					if(!"".equals(orderIds[i])){
						mOrders.add(OrderDetail.load(this, orderIds[i]));
					}
				}
			}
		} catch (Exception e) {
			// TODO: nothing need to do
		}
		mOrdersAdapter = new OrderHistroyAdapter(this, mOrders);
		vLvOrders.setAdapter(mOrdersAdapter);
		mOrdersAdapter.notifyDataSetChanged();
	}
	
	private void initView() {
		vLvOrders = (ListView) findViewById(R.id.lv_orderhistory);
		MActionBar actionBar = (MActionBar) findViewById(R.id.actionbar);
		actionBar.setOnLeftButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
