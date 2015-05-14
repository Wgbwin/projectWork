package com.kodakalaris.kodakmomentslib.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.kodakalaris.kodakmomentslib.util.StringUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 
 * @author Kane
 * 
 */
public class LocalCustomerInfo implements Serializable {
	
	private static final String LocalCustomerInfo = "local_customer_info";

	private static final String C_FIRST_NAME = "cusFirstName";
	private static final String C_LAST_NAME = "cusLastName";
	private static final String C_PHONE = "cusPhone";
	private static final String C_EMAIL = "cusEmail";

	private static final String S_FIRST_NAME = "shipFirstName";
	private static final String S_LAST_NAME = "shipLastName";
	private static final String S_ADDRESS1 = "shipAddress1";
	private static final String S_ADDRESS2 = "shipAddress2";
	private static final String S_CITY = "shipCity";
	private static final String S_STATE = "shipState";
	private static final String S_ZIP = "shipZip";

	private String cusFirstName = "";
	private String cusLastName = "";
	private String cusPhone = "";
	private String cusEmail = "";

	private String shipFirstName = "";
	private String shipLastName = "";
	private String shipAddress1 = "";
	private String shipAddress2 = "";
	private String shipCity = "";
	private String shipState = "";
	private String shipZip = "";
	
	/**
	 * 
	 * @param context
	 */
	public LocalCustomerInfo(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String strData = prefs.getString(LocalCustomerInfo, "");
		parseStrData(strData);
	}

	/**
	 * For Customer Info saved in Order
	 * @param strData
	 */
	public LocalCustomerInfo(String strData){
		parseStrData(strData);
	}
	
	/**
	 * Save current local Customer Info
	 * @param context
	 */
	public void save(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString(LocalCustomerInfo, convertToStringWithJson().toString()).commit();
	}
	
	private void parseStrData(String strData){
		try {
			JSONObject jsData = new JSONObject(strData);
			cusFirstName = jsData.optString(C_FIRST_NAME);
			cusLastName = jsData.optString(C_LAST_NAME);
			cusPhone = jsData.optString(C_PHONE);
			cusEmail = jsData.optString(C_EMAIL);
			
			shipFirstName = jsData.optString(S_FIRST_NAME);
			shipLastName = jsData.optString(S_LAST_NAME);
			shipAddress1 = jsData.optString(S_ADDRESS1);
			shipAddress2 = jsData.optString(S_ADDRESS2);
			shipCity = jsData.optString(S_CITY);
			shipState = jsData.optString(S_STATE);
			shipZip = jsData.optString(S_ZIP);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject convertToStringWithJson(){
		JSONObject jsCusInfo = new JSONObject();
		try {
			jsCusInfo.put(C_FIRST_NAME, cusFirstName);
			jsCusInfo.put(C_LAST_NAME, cusLastName);
			jsCusInfo.put(C_PHONE, cusPhone);
			jsCusInfo.put(C_EMAIL, cusEmail);
			
			jsCusInfo.put(S_FIRST_NAME, shipFirstName);
			jsCusInfo.put(S_LAST_NAME, shipLastName);
			jsCusInfo.put(S_ADDRESS1, shipAddress1);
			jsCusInfo.put(S_ADDRESS2, shipAddress2);
			jsCusInfo.put(S_CITY, shipCity);
			jsCusInfo.put(S_STATE, shipState);
			jsCusInfo.put(S_ZIP, shipZip);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jsCusInfo;
	}

	public String getCusFirstName() {
		return cusFirstName;
	}

	public void setCusFirstName(String cusFirstName) {
		this.cusFirstName = cusFirstName;
	}

	public String getCusLastName() {
		return cusLastName;
	}

	public void setCusLastName(String cusLastName) {
		this.cusLastName = cusLastName;
	}

	public String getCusPhone() {
		return cusPhone;
	}

	public void setCusPhone(String cusPhone) {
		this.cusPhone = cusPhone;
	}

	public String getCusEmail() {
		return cusEmail;
	}

	public void setCusEmail(String cusEmail) {
		this.cusEmail = cusEmail;
	}

	public String getShipFirstName() {
		return shipFirstName;
	}

	public void setShipFirstName(String shipFirstName) {
		this.shipFirstName = shipFirstName;
	}

	public String getShipLastName() {
		return shipLastName;
	}

	public void setShipLastName(String shipLastName) {
		this.shipLastName = shipLastName;
	}

	public String getShipAddress1() {
		return shipAddress1;
	}

	public void setShipAddress1(String shipAddress1) {
		this.shipAddress1 = shipAddress1;
	}

	public String getShipAddress2() {
		return shipAddress2;
	}

	public void setShipAddress2(String shipAddress2) {
		this.shipAddress2 = shipAddress2;
	}

	public String getShipCity() {
		return shipCity;
	}

	public void setShipCity(String shipCity) {
		this.shipCity = shipCity;
	}

	public String getShipState() {
		return shipState;
	}

	public void setShipState(String shipState) {
		this.shipState = shipState;
	}

	public String getShipZip() {
		return shipZip;
	}

	public void setShipZip(String shipZip) {
		this.shipZip = shipZip;
	}

}
