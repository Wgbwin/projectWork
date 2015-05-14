package com.kodakalaris.kodakmomentslib.activity.printsreview;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.BaseNetActivity;
import com.kodakalaris.kodakmomentslib.activity.findstore.MDestinationStoreSelectionActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.PrintsReviewProductsListAdapter;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.interfaces.IKM2Manager;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.widget.mobile.ChangeAllPhotoSizesDialog;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;
import com.kodakalaris.kodakmomentslib.widget.mobile.PrintHubSendOrderDialog;
import com.kodakalaris.kodakmomentslib.widget.mobile.PrintSizesDialog.PrintItemsChangedListener;
import com.kodakalaris.kodakmomentslib.widget.mobile.PrintsReviewProductsListHead;

public class BasePrintsReviewActivity extends BaseNetActivity {
	protected ListView vLisvProducts;
	private PrintsReviewProductsListAdapter mAdapter;
	private Context mContext;
	private IKM2Manager printManager = null;
	private List<PrintItem> printItems = null;
	private MActionBar vActionBar;
	private Button vBtnChangeAllSize;
	private Button vBtnCart;
	private PrintsReviewProductsListHead photoReviewProductsListHead;
	private TextView vTxtPrintTotalNum;
	private boolean updateScreen = true; // if the activity is create by new one need not update screen .else updateScreen is true;
	
	private PrintItemsChangedListener mPrintItemsChangedListener;

	protected void getViews() {
		mContext = BasePrintsReviewActivity.this;
		vLisvProducts = (ListView) findViewById(R.id.lisv_PhotoReview_products);
		vActionBar = (MActionBar) findViewById(R.id.PhotoReview_actionbar);
		photoReviewProductsListHead = new PrintsReviewProductsListHead(mContext);
		vLisvProducts.addHeaderView(photoReviewProductsListHead);
		vBtnChangeAllSize = (Button) photoReviewProductsListHead.findViewById(R.id.btn_PrintsReview_ChangeAllPhotoSizes);
		vBtnCart = (Button) findViewById(R.id.btn_PhotoReview_cart);
		vTxtPrintTotalNum = (TextView) findViewById(R.id.txt_PhotoReview_printTotalNum);
	}

	protected void initData() {
		if(KM2Application.getInstance().getFlowType().isPrintWorkFlow()){
			printManager = PrintManager.getInstance(mContext);
			printItems = ((PrintManager) printManager).getPrintItems();
		}else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()){
			printManager = PrintHubManager.getInstance();
			printItems = ((PrintHubManager) printManager).getPrintItems();
			vBtnCart.setText(mContext.getString(R.string.Common_Next));
		}	
		mAdapter = new PrintsReviewProductsListAdapter(mContext, printItems, R.layout.item_prints_review);
		mPrintItemsChangedListener = new PrintItemsChangedListener() {
			
			@Override
			public void onPrintItemsChanged() {
				refreshScreen();
				startUploadService();
				
			}
		};
		
		mAdapter.setmPrintItemsChangedListener(mPrintItemsChangedListener);
		vLisvProducts.setAdapter(mAdapter);
		setPrintTotal();
		updateScreen = false;
	}

	protected void setEvents() {
		vActionBar.setOnLeftButtonClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		vBtnChangeAllSize.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				ChangeAllPhotoSizesDialog dialog = new ChangeAllPhotoSizesDialog(mContext, printItems, mPrintItemsChangedListener, false);
				dialog.initDialog(mContext);
				dialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "nothing");

			}
		});

		vBtnCart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(KM2Application.getInstance().getFlowType().isPrintWorkFlow()){
					Intent intent = new Intent(BasePrintsReviewActivity.this, MDestinationStoreSelectionActivity.class);
					startActivity(intent);
					finish();
				}else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()){
					PrintHubSendOrderDialog dialog = new PrintHubSendOrderDialog(mContext, printItems, mPrintItemsChangedListener, false);
					dialog.initDialog(mContext);
					dialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "nothing");
				}	
				
			}
		});
	}

	private void setPrintTotal() {
		int total = 0;
		for (PrintItem item : printItems) {
			total = total + item.getCount();
		}
		vTxtPrintTotalNum.setText(total + "");
		if(total==0){
			vBtnCart.setEnabled(false);
		}else{
			vBtnCart.setEnabled(true);
		}
	}

	@Override
	protected void onResume() {
		if (updateScreen) {
			refreshScreen();
		}
		updateScreen = true;// reset the vale the true;
		super.onResume();
	}

	private void refreshScreen() {
		mAdapter.getLatestData();
		mAdapter.initPhotoReviewProductsData();
		mAdapter.notifyDataSetChanged();
		setPrintTotal();
	}
}
