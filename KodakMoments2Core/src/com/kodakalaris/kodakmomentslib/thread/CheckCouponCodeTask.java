package com.kodakalaris.kodakmomentslib.thread;

import android.content.Context;
import android.os.AsyncTask;

import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Cart;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;

public class CheckCouponCodeTask extends AsyncTask<Void, Void, Object> {
	private String mCode;
	private Context mContext;
	
	public CheckCouponCodeTask(Context context, String code) {
		mContext = context;
		mCode = code;
	}
	
	@Override
	protected Object doInBackground(Void... params) {
		GeneralAPI api = new GeneralAPI(mContext);
		try {
			Cart cart = api.checkCouponCodeTask(ShoppingCartManager.getInstance().getCart().cartId, mCode);
			ShoppingCartManager.getInstance().setCart(cart);
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
			onFinish(false, (WebAPIException) result);
		} else {
			onFinish(true, null);
		}
	}
	
	protected void onFinish(boolean success, WebAPIException e) {
		
	}

}
