package com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart;

import java.io.Serializable;

public class Discount implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_CODE = "Code";
	public static final String FLAG_LOCALIZED_NAME = "LocalizedName";
	public static final String FLAG_TERMS_AND_CONDITIONS_URL = "TermsAndConditionsURL";
	public static final String FLAG_STATUS = "Status";
	public static final String FLAG_LOCALIZED_STATUS_DESCRIPTION = "LocalizedStatusDescription";
	
	public static final int  Applied = 0;	
	public static final int  NotApplied_AlreadyApplied = 1;
	public static final int  NotApplied_OthersAlreadyApplied = 2;
	public static final int  NotApplied_Expired = 3;
	public static final int  NotApplied_Invalid = 4;//The code entered is not valid.
	public static final int  NotApplied_Inactive = 5;
	public static final int  NotApplied_Inapplicable = 6;//this code is valid but not all conditions have been met.
		
	public String code = "";
	public String localizedName = "";
	public String termsAndConditionsURL = "";
	public int status = -1;
	public String localizedStatusDescription = "";
	
	public String terms = "";
	
	public boolean isApplied(){
		if (status == Applied) {
			return true;
		}
		return false;
	}
		
	public boolean isError(){
		boolean isError = false;
		switch (status) {
		case NotApplied_AlreadyApplied:
		case NotApplied_OthersAlreadyApplied:
		case NotApplied_Expired:
		case NotApplied_Invalid:
		case NotApplied_Inactive:
			isError = true;			
			break;
		}	
		return isError;
	}
	
}
