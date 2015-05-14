package com.kodak.rss.core.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.kodak.rss.core.bean.LocalCustomerInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.core.n2r.parse.Parse;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class OrderDetail implements Serializable {

	private static final String EMAIL = "email";
	private static final String ORDER_ID = "orderId";
	private static final String ORDER_TIME = "orderTime";
	private static final String ORDER_TOTAL = "orderTotal";
	private static final String ORDER_DETAIL = "orderDetail";
	private static final String TaxWillBeCalculatedByRetailer = "TaxWillBeCalculatedByRetailer";
	private static final String CUSTOMER_INFO = "customerInfo";
	private static final String STORE_INFO = "storeInfo";

	private String email = "";
	private String orderId = "";
	private String orderTime = "";
	private String orderTotal = "";
	private String orderDetail = "";
	private boolean taxWillBeCalculatedByRetailer = false;
	private LocalCustomerInfo customerInfo = null;
	private StoreInfo storeInfo = null;
	
	public static OrderDetail load(Context context, String orderId){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String strData = prefs.getString(orderId, "");		
		return parseOrderDetail(strData);
	}

	private static OrderDetail parseOrderDetail(String strData) {
		OrderDetail order = new OrderDetail();
		try {
			JSONObject jsData = new JSONObject(strData);
			order.email = jsData.optString(EMAIL);
			order.orderId = jsData.optString(ORDER_ID);
			order.orderTime = jsData.optString(ORDER_TIME);
			order.orderTotal = jsData.optString(ORDER_TOTAL);
			order.orderDetail = jsData.optString(ORDER_DETAIL);
			order.taxWillBeCalculatedByRetailer = jsData.optBoolean(TaxWillBeCalculatedByRetailer);
			if(jsData.has(CUSTOMER_INFO)){
				order.customerInfo = new LocalCustomerInfo(jsData.optJSONObject(CUSTOMER_INFO).toString());
			}
			if(jsData.has(STORE_INFO)){
				order.storeInfo = new Parse().parseStoreInfo(new JSONObject(jsData.optString(STORE_INFO)));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return order;
	}
	
	public void save(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString(orderId, convertToStringWithJsonFormat()).commit();
	}

	private String convertToStringWithJsonFormat() {
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put(EMAIL, email);
			jsObj.put(ORDER_ID, orderId);
			jsObj.put(ORDER_TIME, orderTime);
			jsObj.put(ORDER_TOTAL, orderTotal);
			jsObj.put(ORDER_DETAIL, orderDetail);
			jsObj.put(TaxWillBeCalculatedByRetailer, taxWillBeCalculatedByRetailer);
			if(customerInfo!=null){
				jsObj.put(CUSTOMER_INFO, customerInfo.convertToStringWithJson());
			}
			if(storeInfo!=null){
				jsObj.put(STORE_INFO, storeInfo.convertToStringWithJson());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsObj.toString();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}

	public String getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(String orderTotal) {
		this.orderTotal = orderTotal;
	}

	public String getOrderDetail() {
		return orderDetail;
	}

	public void setOrderDetail(String orderDetail) {
		this.orderDetail = orderDetail;
	}

	public LocalCustomerInfo getCustomerInfo() {
		return customerInfo;
	}

	public void setCustomerInfo(LocalCustomerInfo customerInfo) {
		this.customerInfo = customerInfo;
	}

	public StoreInfo getStoreInfo() {
		return storeInfo;
	}

	public void setStoreInfo(StoreInfo storeInfo) {
		this.storeInfo = storeInfo;
	}

	public boolean isTaxWillBeCalculatedByRetailer() {
		return taxWillBeCalculatedByRetailer;
	}

	public void setTaxWillBeCalculatedByRetailer(
			boolean taxWillBeCalculatedByRetailer) {
		this.taxWillBeCalculatedByRetailer = taxWillBeCalculatedByRetailer;
	}

}
