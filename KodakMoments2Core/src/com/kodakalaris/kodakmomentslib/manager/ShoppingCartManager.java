package com.kodakalaris.kodakmomentslib.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.bean.LocalCustomerInfo;
import com.kodakalaris.kodakmomentslib.bean.items.ShoppingCartItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.project.ProductDescription;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.CountryInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.Retailer;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Cart;
import com.kodakalaris.kodakmomentslib.interfaces.IKM2Manager;
import com.kodakalaris.kodakmomentslib.interfaces.SaveRestoreAble;
import com.kodakalaris.kodakmomentslib.util.Log;

public class ShoppingCartManager implements SaveRestoreAble, IKM2Manager {
	private static final String TAG = "ShoppingCartManager";
	private static final String KEY_PREFIX = "ShoppingCartManager_";

	private volatile static ShoppingCartManager sInstance;

	private List<ShoppingCartItem> mShoppingCartItems;
	private Cart mCart; // The latest cart getting from the server.
	public boolean isShipToHome;
	private List<Retailer> mRetailers;
	private int mSelectedStorePosition;
	private Retailer mCurrentRetailer;
	private List<String> mStateKeyList;
	private List<String> mStateValueList;
	
	private boolean mIsInDoMoreMode; //continue shopping

	public List<String> getmStateValueList() {
		return mStateValueList;
	}

	public void setmStateValueList(List<String> mStateValueList) {
		this.mStateValueList = mStateValueList;
	}

	private int mStateNumberPicker;

	public int getmStateNumberPicker() {
		return mStateNumberPicker;
	}

	public void setmStateNumberPicker(int mStateNumberPicker) {
		this.mStateNumberPicker = mStateNumberPicker;
	}

	public List<String> getmStateKeyList() {
		return mStateKeyList;
	}

	public void setmStateKeyList(List<String> mStateList) {
		this.mStateKeyList = mStateList;
	}
	
	public boolean isInDoMoreMode() {
		return mIsInDoMoreMode;
	}
	
	public void setInDoMoreMode(boolean isInDoMoreMode) {
		mIsInDoMoreMode = isInDoMoreMode;
	}

	private ShoppingCartManager() {
		if (mShoppingCartItems == null) {
			mShoppingCartItems = new ArrayList<ShoppingCartItem>();
		}
		if (mRetailers == null) {
			mRetailers = new ArrayList<Retailer>();
		}
	}

	public static ShoppingCartManager getInstance() {
		if (sInstance == null) {
			synchronized (ShoppingCartManager.class) {
				if (sInstance == null) {
					sInstance = new ShoppingCartManager();
				}
			}
			
		}

		return sInstance;
	}

	@Override
	public void saveGlobalVariables(Map<String, Serializable> saveMaps) {
		saveMaps.put(KEY_PREFIX + "mShoppingCartItems", (Serializable) mShoppingCartItems);
		saveMaps.put(KEY_PREFIX + "mCart", (Serializable) mCart);
		saveMaps.put(KEY_PREFIX + "isShipToHome", isShipToHome);
		saveMaps.put(KEY_PREFIX + "mRetailers", (Serializable) mRetailers);
		saveMaps.put(KEY_PREFIX + "mIsInDoMoreMode", mIsInDoMoreMode);
	}

	@Override
	public void restoreGlobalVariables(Map<String, Serializable> restoreMaps) {
		try {
			mShoppingCartItems = (List<ShoppingCartItem>) restoreMaps.get(KEY_PREFIX + "mShoppingCartItems");
		} catch (Exception e) {
			Log.e(TAG, e);
		}
		try {
			mCart = (Cart) restoreMaps.get(KEY_PREFIX + "mCart");
		} catch (Exception e) {
			Log.e(TAG, e);
		}
		try {
			isShipToHome = (Boolean) restoreMaps.get(KEY_PREFIX + "isShipToHome");
		} catch (Exception e) {
			Log.e(TAG, e);
		}
		try {
			mRetailers = (List<Retailer>) restoreMaps.get(KEY_PREFIX + "mRetailers");
		} catch (Exception e) {
			Log.e(TAG, e);
		}
		try {
			mIsInDoMoreMode = (Boolean) restoreMaps.get(KEY_PREFIX + "mIsInDoMoreMode");
		} catch (Exception e) {
			Log.e(TAG, e);
		}
	}

	public List<ShoppingCartItem> getShoppingCartItems() {
		return mShoppingCartItems;
	}

	public String getProductDescriptionIDs() {
		List<String> desIds = getProductDescriptionIDList();
		String strDesIds = "";
		for (int i = 0; i < desIds.size(); i++) {
			if (i != desIds.size() - 1) {
				strDesIds += desIds.get(i) + ",";
			} else {
				strDesIds += desIds.get(i);
			}
		}

		return strDesIds;
	}

	public List<String> getProductDescriptionIDList() {
		List<RssEntry> mEntries = new ArrayList<RssEntry>();
		List<ProductDescription> mProductDescriptions = new ArrayList<ProductDescription>();
		List<String> products = new ArrayList<String>();
		List<ShoppingCartItem> mItems = new ArrayList<ShoppingCartItem>();
		mItems = getShoppingCartItems();
		for (int i = 0; i < mItems.size(); i++) {
			mEntries.add(mItems.get(i).getEntry());
		}
		for (int i = 0; i < mEntries.size(); i++) {
			mProductDescriptions.add(mEntries.get(i).proDescription);
		}
		for (int i = 0; i < mProductDescriptions.size(); i++) {
			products.add(mProductDescriptions.get(i).id);
		}

		return products;
	}

	public void setCart(Cart cart) {
		mCart = cart;
	}

	public void updateCart(Cart cart) {
		mCart = cart;
	}

	public Cart getCart() {
		return mCart;
	}

	public List<Retailer> getRetailers() {
		return mRetailers;
	}

	public void setRetailers(List<Retailer> retailers) {
		this.mRetailers = retailers;
	}

	public Retailer getCurrentRetailer() {
		if (mCart == null || mRetailers == null) {
			return null;
		}

		for (Retailer r : mRetailers) {
			if (r.id.equals(mCart.retailerId)) {
				return r;
			}
		}

		return null;
	}

	public int getmSelectedStorePosition() {
		return mSelectedStorePosition;
	}

	public void setmSelectedStorePosition(int mSelectedStorePosition) {
		this.mSelectedStorePosition = mSelectedStorePosition;
	}

	public boolean hasItemsInCartCumulus() {
		return mCart != null && mCart.cartItems != null && mCart.cartItems.length > 0;
	}

	@Override
	public void startOver() {
		mCart = null;
		isShipToHome = false;
		mRetailers = new ArrayList<Retailer>();
		mShoppingCartItems = new ArrayList<ShoppingCartItem>();
		mIsInDoMoreMode = false;
	}
	
	public boolean isCustomerInfoValid(Context context, Retailer currentRetailer){
		if (currentRetailer == null) {
			return false;
		}
		
		LocalCustomerInfo customerInfo = new LocalCustomerInfo(context);
		
		
		boolean valid = true;
		if (currentRetailer.requiredCustomerInfo != null) {
			for(int i=0; i<currentRetailer.requiredCustomerInfo.length; i++){
				switch (currentRetailer.requiredCustomerInfo[i]) {
				case 0:
					if(customerInfo.getCusFirstName().equals("")){
						valid = false;
					}
					break;
				case 1:
					if(customerInfo.getCusLastName().equals("")){
						valid = false;
					}
					break;
				case 2:
					if(customerInfo.getCusPhone().equals("")){
						valid = false;
					}
					break;
				case 6:
					if(customerInfo.getCusEmail().equals("")){
						valid = false;
					}
					break;
				}
			}
		}
		return valid;
	}
	
	public boolean isShippingAddressValid(Context context){
		KM2Application app = KM2Application.getInstance();
		CountryInfo countryInfo = app.getCountryInfo();
		LocalCustomerInfo customer = new LocalCustomerInfo(context);
		boolean valid = true;
		if(customer.getShipFirstName().equals("")){
			valid = false;
		}
		if(customer.getShipLastName().equals("")){
			valid = false;
		}
		if(customer.getShipAddress1().equals("")){
			valid = false;
		}
		if(countryInfo != null){
			if(countryInfo.addressStyle.contains(CountryInfo.CITY)){
				if(customer.getShipCity().equals("")){
					valid = false;
				}
			}
			if(countryInfo.addressStyle.contains(CountryInfo.STATE)){
				if(customer.getShipState().equals("")){
					valid = false;
				}
			}
			if(countryInfo.addressStyle.contains(CountryInfo.ZIP)){
				if(customer.getShipZip().equals("")){
					valid = false;
				}
			}
		}
		return valid;
	}

}
