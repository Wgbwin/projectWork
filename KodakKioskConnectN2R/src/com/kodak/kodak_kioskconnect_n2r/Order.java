package com.kodak.kodak_kioskconnect_n2r;


public class Order
{
	public String orderID;
	public String orderDetails;
	public String orderTime;
	public String orderStore;
	public String orderSubtotal;
	public String orderEmail;
	
	public String orderLatitude;
	public String orderLongitude;
	public String orderStoreName;
	public String orderAddress;
	public String orderPhonenumber;
	public String orderPersonEmail;
	public String oderSelectedStoreName;
	public String orderSelectedStoreAddress;
	public String orderSelectedCityAndZip;
	public String orderSelectedStorePhone;
	
	// add by song for ship address information
	public String orderFirstnameShip ;
	public String orderLastnameShip ;
	public String orderAddressoneShip ;
	public String orderAddresstwoShip ;
	public String orderCityShip;
	public String orderStateShip;
	public String orderZipShip ;
	
	public String isTaxWillBeCalculatedByRetailer; //add by song if the Calculated string will be show?
	public Order()
	{
		
	}
}
