package com.kodak.kodak_kioskconnect_n2r;

import java.util.HashMap;

public class Store
{
	public String phone;
	public String baseURI;
	public String miles;
	public String id;
	public String retailerBaseURI;
	public String retailerID;
	public String name;
	public String email;
	public String longitude;
	public String latitude;
	public String address1;
	public String city;
	public String postalCode;
	public String country;
	public String stateProvince;
	public boolean displayHours = false;
	public boolean isATestStore = false;
    public HashMap<Integer, String> hoursMap;
    
    public static final String TEST_STORE_ON = "TESTSTORES:ON";
    public static final String TEST_STORE_OFF = "TESTSTORES:OFF";
    
	public HashMap<Integer, String> getHoursMap() {
		return hoursMap;
	}

	public void setHoursMap(HashMap<Integer, String> hoursMap) {
		this.hoursMap = hoursMap;
	}

	public boolean isDisplayHours() {
		return displayHours;
	}

	public void setDisplayHours(boolean displayHours) {
		this.displayHours = displayHours;
	}

	public Store()
	{
	}
}
