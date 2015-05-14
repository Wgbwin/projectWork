package com.kodakalaris.kodakmomentslib.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.kodakalaris.kodakmomentslib.DataKey;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Cart;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo;
import com.kodakalaris.kodakmomentslib.culumus.parse.Parse;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;

import android.content.Context;
import android.text.format.DateFormat;

/**
 * 
 * @author Kane
 *
 */
public class OrderDetail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String ORDER_ID = "orderId";
	private static final String ORDER_TIME = "orderTime";
	private static final String CART = "cart";
	private static final String CUSTOMER_INFO = "customerInfo";
	private static final String STORE_INFO = "storeInfo";

	private String orderId = "";
	private long time;
	private LocalCustomerInfo customerInfo = null;
	private StoreInfo storeInfo = null;
	private Cart cart = null;
	private String strCart = "";
	private boolean is24HourFormat = false;
	
	public OrderDetail(Context context){
		time = System.currentTimeMillis();
		is24HourFormat = DateFormat.is24HourFormat(context);
	}
	
	public static OrderDetail load(Context context, String orderId){
		String strData = SharedPreferrenceUtil.getString(context, orderId);	
		return parseOrderDetail(context, strData);
	}

	private static OrderDetail parseOrderDetail(Context context, String strData) {
		OrderDetail order = new OrderDetail(context);
		try {
			JSONObject jsData = new JSONObject(strData);
			order.orderId = jsData.optString(ORDER_ID);
			order.time = jsData.optLong(ORDER_TIME);
			String strCart = jsData.optString(CART);
			if(strCart != null){
				try {
					order.cart = new Parse().parseCart(strCart);
				} catch (WebAPIException e) {
					e.printStackTrace();
				}
			}
			
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
		String orderHistroy = SharedPreferrenceUtil.getString(context, DataKey.ORDER_HISTORY);
		if(orderHistroy.contains(orderId)){
			return;
		}
		if("".equals(orderHistroy)){
			orderHistroy += orderId;
		} else {
			orderHistroy += "," + orderId;
		}
		SharedPreferrenceUtil.setString(context, orderId, convertToStringWithJsonFormat());
		SharedPreferrenceUtil.setString(context, DataKey.ORDER_HISTORY, orderHistroy);
	}

	private String convertToStringWithJsonFormat() {
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put(ORDER_ID, orderId);
			jsObj.put(ORDER_TIME, time);
			jsObj.put(CART, strCart);
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

	private String getCurrentTime(long date, String strFormat) {
		SimpleDateFormat format = new SimpleDateFormat(strFormat);
		String str = format.format(new Date(date));
		return str;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderDate() {
		return getCurrentTime(time, "MMMM dd, yyyy");
	}

	public String getOrderTime() {
		if(is24HourFormat) {  
	    	return getCurrentTime(time, "hh:mm");
	    } else {
	    	return getCurrentTime(time, "hh:mm a");
	    }
	}

	public Cart getCart() {
		return cart;
	}

	public void setCart(String strCart) {
		this.strCart = strCart;
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

}
