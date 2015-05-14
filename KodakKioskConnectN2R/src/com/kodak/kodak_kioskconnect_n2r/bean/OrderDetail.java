package com.kodak.kodak_kioskconnect_n2r.bean;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class OrderDetail {
	
	private final static String FLAG_EMAIL = "email";
	private final static String FLAG_ORDER_ID = "orderId";
	private final static String FLAG_ORDER_TIME = "orderTime";
	private final static String FLAG_ORDER_TOTAL = "orderTotal";
	private final static String FLAG_DETAIL = "orderDetail";
	private final static String FLAG_SHIPPING_ADDRESS = "shippingAddress";
	
	public String email = "";
	public String orderId = "";
	public String orderTime = "";
	public String orderTotal = "";
	public String orderDetail = "";
	public String shippingAddress = "";
	public String storeInfo = "";
	
	public void saveOrder(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		String detail = convertObject2JSON();
		editor.putString(orderId, detail);
		editor.commit();
	}
	
	public static OrderDetail loadOrderDetail(Context context, String orderId){
		OrderDetail orderDetail = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String jsonDetail = prefs.getString(orderId, "");
		if(!"".equals(jsonDetail)){
			try {
				JSONObject jsObj = new JSONObject(jsonDetail);
				orderDetail = new OrderDetail();
				if(jsObj.has(FLAG_EMAIL)){
					orderDetail.email = jsObj.getString(FLAG_EMAIL);
				}
				if(jsObj.has(FLAG_ORDER_ID)){
					orderDetail.orderId = jsObj.getString(FLAG_ORDER_ID);
				}
				if(jsObj.has(FLAG_ORDER_TIME)){
					orderDetail.orderTime = jsObj.getString(FLAG_ORDER_TIME);
				}
				if(jsObj.has(FLAG_ORDER_TOTAL)){
					orderDetail.orderTotal = jsObj.getString(FLAG_ORDER_TOTAL);
				}
				if(jsObj.has(FLAG_DETAIL)){
					orderDetail.orderDetail = jsObj.getString(FLAG_DETAIL);
				}
				if(jsObj.has(FLAG_SHIPPING_ADDRESS)){
					orderDetail.shippingAddress = jsObj.getString(FLAG_SHIPPING_ADDRESS);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}			
		}
		return orderDetail;
	}
	
	private String convertObject2JSON(){
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put(FLAG_EMAIL, email);
			jsObj.put(FLAG_ORDER_ID, orderId);
			jsObj.put(FLAG_ORDER_TIME, orderTime);
			jsObj.put(FLAG_ORDER_TOTAL, orderTotal);
			jsObj.put(FLAG_DETAIL, orderDetail);
			jsObj.put(FLAG_SHIPPING_ADDRESS, shippingAddress);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsObj.toString();
	}
	
}
