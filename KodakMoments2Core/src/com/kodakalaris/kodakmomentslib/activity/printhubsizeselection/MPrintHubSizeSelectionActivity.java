package com.kodakalaris.kodakmomentslib.activity.printhubsizeselection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.imageselection.MImageSelectionMainActivity;
import com.kodakalaris.kodakmomentslib.activity.printsizeselection.MPrintSizeSelectionActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.PrintHubSizeAdapter;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.widget.LinearListLayout;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;
import com.kodakalaris.kpp.KodakPrintPlace;
import com.kodakalaris.kpp.PrintSize;
import com.kodakalaris.kpp.PrinterInfo;

public class MPrintHubSizeSelectionActivity extends BasePrintHubSizeSelectionActivity {
	private final String TAG = "MPrintHubSizeSelectionActivity";
	
	private KodakPrintPlace mKpp;
	private MActionBar vActionBar;
	private LinearListLayout vListvSize;
	private PrintHubSizeAdapter mSizeAdapter;
	private PrintHubManager mPrintHubManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_printhub_size_selection);
		
		initViews();
		initData();
		setupEvents();
	}
	
	private void initViews() {
		vActionBar = (MActionBar) findViewById(R.id.actionbar);
		vListvSize = (LinearListLayout) findViewById(R.id.listv_size);
	}
	
	private void initData() {
		mPrintHubManager = PrintHubManager.getInstance();
		mKpp = PrintHubManager.getInstance().getKodakPrintPlace();
		PrinterInfo info = mKpp.getCachedPrinterInformation();
		mPrintHubManager.setSupportPrintSizes(info.getSupportedPrintSizes());
		mSizeAdapter = new PrintHubSizeAdapter(this, info);
		vListvSize.setAdapter(mSizeAdapter);
	}
	
	private void setupEvents() {
		vActionBar.setOnLeftButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mSizeAdapter.setOnSizeClickListener(new PrintHubSizeAdapter.OnSizeClickListener() {
			
			@Override
			public void onClick(View v, int position, PrintSize size) {
				Log.i(TAG, "onClick size:" + size.getSize().name());
				Intent intent = new Intent(MPrintHubSizeSelectionActivity.this, MImageSelectionMainActivity.class);
				mPrintHubManager.setDefaultPrintSize(mPrintHubManager.getPrintProducts().get(position));
				startActivity(intent);
			}
		});
	}
	
}
