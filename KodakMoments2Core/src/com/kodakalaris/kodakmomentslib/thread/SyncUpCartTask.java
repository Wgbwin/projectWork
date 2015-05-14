package com.kodakalaris.kodakmomentslib.thread;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.Retailer;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Cart;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Placeholders;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;

public class SyncUpCartTask extends AsyncTask<Void, Void, Object> {
	
	private GeneralAPI mApi;
	private ShoppingCartManager mManager;
	private Context mContext;
	
	public SyncUpCartTask(Context context) {
		mContext = context;
		mApi = new GeneralAPI(context);
		mManager = ShoppingCartManager.getInstance();
	}
	
	@Override
	protected Object doInBackground(Void... params) {
			try {
				String retailerId = getRetailerID();
				if (mManager.getCart() == null) {
					Cart cart = mApi.createCartTask(retailerId);
					mManager.setCart(cart);
				} else {
					Cart cart = mApi.setRetailerIdTask(mManager.getCart().cartId, retailerId);
					mManager.updateCart(cart);
				}
				
				if (!ShoppingCartManager.getInstance().isShipToHome) {
					StoreInfo storeInfo = StoreInfo.loadSelectedStore(mContext);
					if (storeInfo != null) {
						Cart cart = mApi.setStoreTask(mManager.getCart().cartId, retailerId, storeInfo.id);
						mManager.updateCart(cart);
					} else {
						Log.e("SaveToCartTask", "Error! Store info is null");
					}
				}
				
				Placeholders placeholders = mApi.createPlaceHolderTask(mManager.getCart().cartId, mManager.getShoppingCartItems());
				
				if (placeholders != null && placeholders.ids != null) {
					for (int i = 0; i < placeholders.ids.size(); i++) {
						mManager.getShoppingCartItems().get(i).setServerId(placeholders.ids.get(i));
					}
				}
				
				mManager.updateCart(mApi.getCartTask(mManager.getCart().cartId));
				
				Cart cart = mApi.updateCartQuantities(mManager.getCart().cartId, mManager.getShoppingCartItems());
				
				mManager.updateCart(cart);
				
			} catch (WebAPIException e) {
				e.printStackTrace();
				return e;
			}
		return null;
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
	
	private String getRetailerID(){
		String retailerId = "";
		if(ShoppingCartManager.getInstance().isShipToHome){
			for(Retailer retailer : ShoppingCartManager.getInstance().getRetailers()){
				if(retailer.shipToHome){
					retailerId = retailer.id;
					break;
				}
			}
		} else {
			StoreInfo storeInfo = StoreInfo.loadSelectedStore(mContext);
			if (storeInfo != null) {
				retailerId = storeInfo.retailerID;
			}
		}
		return retailerId;
	}

}
