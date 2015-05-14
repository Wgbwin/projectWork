package com.kodak.kodak_kioskconnect_n2r.bean.retailer;

import java.util.HashMap;

public class CountryInfo {
	
	public static final String FLAG_COUNTRIES = "Countries";
	public static final String FLAG_COUNTRY_INFOS = "CountryInfos";
	public static final String FLAG_COUNTRY_CODE = "CountryCode";
	public static final String FLAG_LOCALIZED_COUNTRY_NAME = "LocalizedCountryName";
	public static final String FLAG_COUNTRY_SUBREGIONS = "CountrySubregions";
	public static final String FLAG_ATTRIBUTES = "Attributes";
	public static final String FLAG_CS_ABBREVIATION = "Abbreviation";
	public static final String FLAG_CS_VALUE = "Value";
	public static final String FLAG_CS_NAME = "Name";
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
}
