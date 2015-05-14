package com.kodakalaris.kodakmomentslib.thread;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.findstore.BaseDestinationStoreSelectionActivity;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.Retailer;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;
import com.kodakalaris.kodakmomentslib.widget.WaitingDialog;

@SuppressLint("NewApi")
public class DestinationMethodTask extends AsyncTask<String, Void, List<Retailer>> {
	public Context mContext;
	public List<Retailer> mRetailers = null;
	private WaitingDialog waitingDialog;

	public DestinationMethodTask(Context context) {
		this.mContext = context;
	}

	@Override
	protected void onPreExecute() {

		super.onPreExecute();

		mRetailers = new ArrayList<Retailer>();
		waitingDialog = new WaitingDialog(mContext, false);
		waitingDialog.initDialog(R.string.Common_please_wait);
		waitingDialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "");
	}

	@Override
	protected List<Retailer> doInBackground(String... params) {
		GeneralAPI api = new GeneralAPI(mContext);
		try {
			mRetailers = api.getRetailersOfferingProductsTask(params[0]);
	
		} catch (WebAPIException e) {

			e.printStackTrace();
		}
		return mRetailers;
	}

	@Override
	protected void onPostExecute(List<Retailer> result) {
		super.onPostExecute(result);
		for (Retailer retailer : result) {
			ShoppingCartManager.getInstance().setRetailers(result);
			Log.i("RetailerSize", result.size()+"");
			if (retailer.shipToHome && !retailer.cloLite) {
				ShoppingCartManager.getInstance().isShipToHome = true;
				((BaseDestinationStoreSelectionActivity)mContext).btnSelectMethod.setText(mContext.getResources().getString(R.string.FindStore_ShipToHome));
				((BaseDestinationStoreSelectionActivity)mContext).txtShipHome.setVisibility(View.VISIBLE);
				((BaseDestinationStoreSelectionActivity)mContext).lineLyAddress.setVisibility(View.VISIBLE);
				((BaseDestinationStoreSelectionActivity)mContext).lineLySearch.setVisibility(View.GONE);

			} else if (retailer.cloLite && SharedPreferrenceUtil.getBoolean(mContext, SharedPreferrenceUtil.ACCEPT_CLOLITE)) {
				ShoppingCartManager.getInstance().isShipToHome = false;
				((BaseDestinationStoreSelectionActivity)mContext).btnSelectMethod.setText(mContext.getResources().getString(R.string.FindStore_pickUpInStore));
				((BaseDestinationStoreSelectionActivity)mContext).txtPickStore.setVisibility(View.VISIBLE);
				((BaseDestinationStoreSelectionActivity)mContext).lineLyAddress.setVisibility(View.GONE);
				((BaseDestinationStoreSelectionActivity)mContext).lineLySearch.setVisibility(View.VISIBLE);

			} else {
				ShoppingCartManager.getInstance().isShipToHome = false;
				((BaseDestinationStoreSelectionActivity)mContext).btnSelectMethod.setText(mContext.getResources().getString(R.string.FindStore_pickUpInStore));
				((BaseDestinationStoreSelectionActivity)mContext).txtPickStore.setVisibility(View.VISIBLE);
				((BaseDestinationStoreSelectionActivity)mContext).lineLyAddress.setVisibility(View.GONE);
				((BaseDestinationStoreSelectionActivity)mContext).lineLySearch.setVisibility(View.VISIBLE);
			}
		}
		//TODO to replace dialog for a short time---Simon
		if (result.size()==0) {
			Toast.makeText(mContext, "There is no retailer to support this product", Toast.LENGTH_LONG).show();
		}
		waitingDialog.dismiss();

	}

}
