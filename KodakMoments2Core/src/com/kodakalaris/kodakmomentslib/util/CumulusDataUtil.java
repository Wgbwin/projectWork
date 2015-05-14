package com.kodakalaris.kodakmomentslib.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.CountryInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;

/**
 * 
 * @author Kane
 *
 */
public class CumulusDataUtil {
	
	public static String findCountryCodeByName(HashMap<String, String> countries, String name){
		String countryCode = ""	;
		if(countries != null && countries.size() > 0){
			Iterator<Entry<String, String>> iter = countries.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, String> entry = iter.next();
				if(entry.getValue().equals(name)){
					return entry.getKey();
				}
			}
		}
		
		return countryCode;
	}
	
	public static CountryInfo getCountryInfo(String countryCode) {
		List<CountryInfo> countryInfos = KM2Application.getInstance().getCountryInfoList();
		if(countryInfos != null){
			for(CountryInfo countryInfo : countryInfos){
				if(countryInfo.countryCode.equalsIgnoreCase(countryCode)){
					return countryInfo;
				}
			}
		}
		return null;
	}
	
	/**
	 * validate the specified country whether is supported by server
	 * @param countryCode
	 * @return true is valid, otherwise false.
	 */
	public static boolean isCountryCodeValid(String countryCode){
		boolean isValid = false;
		HashMap<String, String> countries = KM2Application.getInstance().getCountries();
		if(countries == null || countries.size() == 0){
			return isValid;
		}
		
		Iterator<Entry<String, String>> iter = countries.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> entry = iter.next();
			if(entry.getKey().equalsIgnoreCase(countryCode)){
				isValid = true;
				break;
			}
		}
		return isValid;
	}
	
	public static String[] getAvailableCounts(RssEntry entry){
		List<String> listCounts = new ArrayList<String>();
		final int maxNumber = 99;
		final int quantityIncrement = entry.proDescription.getQuantityIncrement();
		int temp = maxNumber / quantityIncrement;
		for(int i=0; i<=temp; i++){
			listCounts.add(i*quantityIncrement + "");
		}
		if(maxNumber % entry.proDescription.getQuantityIncrement() > 0){
			listCounts.add("99");
		}
		String[] counts = new String[listCounts.size()];
		for(int i=0; i<counts.length; i++){
			counts[i] = listCounts.get(i);
		}
		return counts;
	}
	
}
