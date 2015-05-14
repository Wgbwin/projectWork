package com.kodakalaris.kodakmomentslib.activity.PrintHubOrderConfirmationActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.AppManager;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.adapter.mobile.OrderConfirmationProductsAdapter;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.OrderConfirmationProductItem;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.widget.LinearListLayout;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;


public class MPrintHubOrderConfirmationActivity extends BasePrintHubOrderConfirmationActivity {
	public static final String INTENT_JOB_ID = "intent_job_id";
	
	private TextView vTxtDate;
	private TextView vTxtTime;
	private TextView vTxtOrderID;
	private MActionBar vActionBar;
	private LinearListLayout vLisvProducts;
	private long mOrderTime;
	private String mOrderId;
	
	private OrderConfirmationProductsAdapter mProductsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_printhub_order_confirmation);
		
		initViews();
		initData();
		initEvents();
	}
	
	private void initViews() {
		vTxtDate = (TextView) findViewById(R.id.txt_date);
		vTxtTime = (TextView) findViewById(R.id.txt_time);
		vTxtOrderID = (TextView) findViewById(R.id.txt_orderId);
		
		vLisvProducts = (LinearListLayout) findViewById(R.id.listv_products);
		vActionBar = (MActionBar) findViewById(R.id.actionbar);
		
	}
	
	private void initData() {
		mOrderTime = System.currentTimeMillis();
		mOrderId = getIntent().getStringExtra(INTENT_JOB_ID);
		vTxtDate.setText(getOrderDate());
		vTxtTime.setText(getOrderTime());
		vTxtOrderID.setText(mOrderId);
		
		
		List<OrderConfirmationProductItem> list = getProductList();
		
		mProductsAdapter = new OrderConfirmationProductsAdapter(this, list);
		vLisvProducts.setAdapter(mProductsAdapter);
	}
	
	private void initEvents() {
		vActionBar.setOnRightButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}
	
	public String getOrderDate() {
		return getCurrentTime(mOrderTime, "MMMM dd, yyyy");
	}

	public String getOrderTime() {
		if(DateFormat.is24HourFormat(this)) {  
	    	return getCurrentTime(mOrderTime, "hh:mm");
	    } else {
	    	return getCurrentTime(mOrderTime, "hh:mm a");
	    }
	}
	
	private String getCurrentTime(long time, String strFormat) {
		SimpleDateFormat format = new SimpleDateFormat(strFormat);
		String str = format.format(new Date(time));
		return str;
	}
	
	private List<OrderConfirmationProductItem> getProductList() {
		List<OrderConfirmationProductItem> list = new ArrayList<OrderConfirmationProductItem>();
		
		List<RssEntry> selectedEntries = new ArrayList<RssEntry>();

		HashMap<RssEntry, Integer> itemSelectedCounts = new HashMap<RssEntry, Integer>();
		
		int tempCount = 0;
		for (RssEntry entry : PrintHubManager.getInstance().getPrintProducts()) {
			int count = 0;
			for (PrintItem item : PrintHubManager.getInstance().getPrintItems()) {
				if (item.getEntry().equals(entry)) {
					tempCount = item.getCount();
					count = count + tempCount;
				}
			}
			if (count > 0) {
				selectedEntries.add(entry);
				itemSelectedCounts.put(entry, count);
			}
		}
		
		for (RssEntry entry : selectedEntries) {
			OrderConfirmationProductItem item = new OrderConfirmationProductItem(entry.proDescription.shortName, itemSelectedCounts.get(entry));
			list.add(item);
		}
		
		return list;
	}
	
	@Override
	public void onBackPressed() {
		AppManager.getInstance().startOver();
		finish();
	}
	
}
