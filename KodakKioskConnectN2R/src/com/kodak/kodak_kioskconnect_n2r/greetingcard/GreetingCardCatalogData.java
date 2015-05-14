package com.kodak.kodak_kioskconnect_n2r.greetingcard;

public class GreetingCardCatalogData {
	private static String TAG = GreetingCardCatalogData.class.getSimpleName();
	
	public static final String CATALOGS = "Catalogs";
	
	public static final String CURRENCY = "Currency";
	public static final String CURRENCY_SYMBOL = "CurrencySymbol";
	public static final String PRICES_INCLUDE_TAX = "PricesIncludeTax";
	public static final String ENTRIES = "Entries";
	
	public String currency;
	public String currencySymbol;
	public boolean pricesIncludeTax;
	public GreetingCardCatalogDataEntry[] entries;
	
	public GreetingCardCatalogDataEntry getEntry(String entryId){
		if(entries!=null){
			for(GreetingCardCatalogDataEntry entry : entries){
				if(entry.description.id.equals(entryId)){
					return entry;
				}
			}
		}
		return null;
	}
	
}
