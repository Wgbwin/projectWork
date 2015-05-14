package com.kodakalaris.kodakmomentslib.culumus.bean.retailer;

import java.io.Serializable;
import java.util.HashMap;

public class CountryInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_COUNTRIES = "Countries";
	public static final String FLAG_COUNTRY_INFOS = "CountryInfos";
	public static final String FLAG_COUNTRY_CODE = "CountryCode";
	public static final String FLAG_LOCALIZED_COUNTRY_NAME = "LocalizedCountryName";
	public static final String FLAG_COUNTRY_SUBREGIONS = "CountrySubregions";
	public static final String FLAG_COUNTRY_ATTRIBUTES = "Attributes";
	public static final String FLAG_CS_ABBREVIATION = "Abbreviation";
	public static final String FLAG_CS_NAME = "Name";
	public static final String FLAG_CS_VALUE = "Value";
	public static final String FLAG_LOCALIZED_SN = "LocalizedSubregionName";
	public static final String FLAG_LOCALIZED_PCN = "LocalizedPostalCodeName";
	public static final String FLAG_PCAE = "PostalCodeAuditExpression";
	public static final String FLAG_LOCALIZED_PCAEM = "LocalizedPostalCodeAuditErrorMessage";
	public static final String FLAG_ADDRESS_STYLE = "AddressStyle";
	
	
	public static final String CITY = "City";
	public static final String STATE = "State";
	public static final String ZIP = "Zip";
	
	public String countryCode = "";
	public String countryName = "";
	public HashMap<String, String> countrySubregions;
	public HashMap<String, String> countryAttributes;
	public String localizedSubregionName = "";
	public String localizedPostalCodeName = "";
	public String postalCodeAuditExpression = "";
	public String localizedPostalCodeAuditErrorMessage = "";
	public String addressStyle = "";
	
	public boolean showsMSRPPricing(){
		boolean showsMSRPPricing = true;
		if(countryAttributes != null){
			String attr = countryAttributes.get("ShowsMSRPPricing");
			if(attr.equalsIgnoreCase("false")){
				showsMSRPPricing = false;
			}
		}
		return showsMSRPPricing;
	}
	
	public boolean hasKiosks() {
		String attr = "";
		if (countryAttributes != null) {
			attr = countryAttributes.get("HasKiosks");
		}
		
		return "true".equalsIgnoreCase(attr);
	}
	
	public boolean hasPrintHubs() {
		String attr = "";
		if (countryAttributes != null) {
			attr = countryAttributes.get("HasPrintHubs");
		}
		
		return "true".equalsIgnoreCase(attr);
	}
	
	public boolean hasN2R() {
		String attr = "";
		if (countryAttributes != null) {
			attr = countryAttributes.get("HasN2R");
		}
		
		return "true".equalsIgnoreCase(attr);
	}
	
}
