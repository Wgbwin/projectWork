package com.kodakalaris.kodakmomentslib.thread;

import android.content.Context;
import android.os.AsyncTask;

import com.kodakalaris.kodakmomentslib.bean.LocalCustomerInfo;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Cart;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;

public abstract class SetCustomerInfoTask extends AsyncTask<Void, Void, Object> {
	private Context mContext;
	private LocalCustomerInfo mCustomerInfo;
	
	public SetCustomerInfoTask(Context context, LocalCustomerInfo customerInfo) {
		mContext = context;
		mCustomerInfo = customerInfo;
	}
	
	@Override
	protected Object doInBackground(Void... params) {
		GeneralAPI api = new GeneralAPI(mContext);
		try {
			Cart cart = api.setCustomerTask(ShoppingCartManager.getInstance().getCart().cartId,
								ShoppingCartManager.getInstance().isShipToHome,
								mCustomerInfo);
			return cart;
		} catch (WebAPIException e) {
			e.printStackTrace();
			return e;
		}
	}
	
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if (result instanceof WebAPIException) {
			OnFinished(false, (WebAPIException) result);
		} else {
			ShoppingCartManager.getInstance().updateCart((Cart) result);
			OnFinished(true, null);
		}
	}

	protected abstract void OnFinished(boolean success, WebAPIException exception);
	
}
