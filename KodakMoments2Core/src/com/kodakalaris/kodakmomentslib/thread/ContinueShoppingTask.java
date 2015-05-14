package com.kodakalaris.kodakmomentslib.thread;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.Catalog;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;

public class ContinueShoppingTask extends AsyncTask<Void, Void, Object> {
	private Context mContext;
	
	public ContinueShoppingTask(Context context) {
		mContext = context;
	}

	@Override
	protected Object doInBackground(Void... params) {
		String productTypes = mContext.getString(R.string.cumulus_support_products);
		String retailerId = ShoppingCartManager.getInstance().getCurrentRetailer().id;
		String storeId = null;
		if (!ShoppingCartManager.getInstance().isShipToHome) {
			StoreInfo storeInfo = StoreInfo.loadSelectedStore(mContext);
			storeId = storeInfo.id;
		}
		
		GeneralAPI api = new GeneralAPI(mContext);
		try {
			List<Catalog> catalogs = api.getMSRPCatalog3Task(productTypes, retailerId, storeId);
			KM2Application.getInstance().setCatalogs(catalogs);
			return catalogs;
		} catch (WebAPIException e) {
			e.printStackTrace();
			return e;
		}
		
	}
	
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if (result instanceof WebAPIException) {
			onFinished(false, (WebAPIException) result);
		} else {
			onFinished(true, null);
		}
	}
	
	protected void onFinished(boolean success, WebAPIException e) {
		
	}

}
