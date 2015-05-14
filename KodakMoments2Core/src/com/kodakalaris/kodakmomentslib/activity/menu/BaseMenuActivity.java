package com.kodakalaris.kodakmomentslib.activity.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.activity.BaseActivity;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;

public class BaseMenuActivity extends BaseActivity {
	protected static final String TAG = BaseMenuActivity.class.getSimpleName();
	
	protected List<RssEntry> mPrintProducts;
	protected HashMap<String, String> mCountries;
	
	public static final String ACTION_KEY = "action";
	public static final String ACTION_VALUE_CART = "cart";
	public static final String ACTION_VALUE_ORDER = "order";
	public static final String ACTION_VALUE_PROFILE = "profile";
	public static final String ACTION_VALUE_SETTINGS = "settings"; 
	public static final String ACTION_VALUE_CLEARCART = "clear_cart";
	public static final String ACTION_VALUE_GALLERY = "gallery";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	protected void initData(){
		KM2Application app = KM2Application.getInstance();
		if(app.getCatalogs()!=null && app.getCatalogs().size()>0) {
			mPrintProducts = app.getCatalogs().get(0).getProducts(AppConstants.PRO_TYPE_PRINT);
		}
		mCountries = app.getCountries();
	}
	
	protected List<String> getCountryNames(){
		List<String> countryNames = new ArrayList<String>();
		if(mCountries != null){
			countryNames.addAll(mCountries.values());
		}
		return countryNames;
	}
	
	protected String getCountryNameByCode(String countryCode) {
		String countryName = "";
		if(mCountries != null){
			countryName = mCountries.get(countryCode);
		}
		return countryName;
	}

}
