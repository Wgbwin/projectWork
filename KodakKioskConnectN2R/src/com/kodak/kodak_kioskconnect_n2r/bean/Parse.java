package com.kodak.kodak_kioskconnect_n2r.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kodak.kodak_kioskconnect_n2r.Pricing;
import com.kodak.kodak_kioskconnect_n2r.Pricing.LineItem;
import com.kodak.kodak_kioskconnect_n2r.Pricing.UnitPrice;
import com.kodak.kodak_kioskconnect_n2r.bean.content.Theme;
import com.kodak.kodak_kioskconnect_n2r.bean.content.Theme.BackGround;
import com.kodak.kodak_kioskconnect_n2r.bean.retailer.CountryInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.retailer.Retailer;
import com.kodak.kodak_kioskconnect_n2r.bean.retailer.Retailer.CartLimit;
import com.kodak.kodak_kioskconnect_n2r.bean.shoppingcart.Cart;
import com.kodak.kodak_kioskconnect_n2r.bean.shoppingcart.CartItem;
import com.kodak.kodak_kioskconnect_n2r.bean.text.Font;
import com.kodak.kodak_kioskconnect_n2r.bean.text.TextBlock;

public class Parse {

	public List<CountryInfo> parseCountryInfo(String result){
		List<CountryInfo> infos = new ArrayList<CountryInfo>();
		try {
			JSONObject jsonObj = new JSONObject(result);
			JSONArray jsonInfos = jsonObj.getJSONArray(CountryInfo.FLAG_COUNTRY_INFOS);
			for(int i=0; i<jsonInfos.length(); i++){
				JSONObject jsonInfo = jsonInfos.getJSONObject(i);
				CountryInfo info = new CountryInfo();
				if(jsonInfo.has(CountryInfo.FLAG_COUNTRY_CODE)){
					info.countryCode = jsonInfo.getString(CountryInfo.FLAG_COUNTRY_CODE);
				}
				if(jsonInfo.has(CountryInfo.FLAG_LOCALIZED_COUNTRY_NAME)){
					info.countryName = jsonInfo.getString(CountryInfo.FLAG_LOCALIZED_COUNTRY_NAME);
				}
				if(jsonInfo.has(CountryInfo.FLAG_COUNTRY_SUBREGIONS)){
					JSONArray jsonSubs = jsonInfo.getJSONArray(CountryInfo.FLAG_COUNTRY_SUBREGIONS);
					info.countrySubregions = new HashMap<String, String>();
					for(int j=0; j<jsonSubs.length(); j++){
						JSONObject jsonSub = jsonSubs.getJSONObject(j);
						String abbre = "", name = "";
						if(jsonSub.has(CountryInfo.FLAG_CS_ABBREVIATION)){
							abbre = jsonSub.getString(CountryInfo.FLAG_CS_ABBREVIATION);
						}
						if(jsonSub.has(CountryInfo.FLAG_CS_NAME)){
							name = jsonSub.getString(CountryInfo.FLAG_CS_NAME);
						}
						if(!abbre.equals("") && !name.equals("")){
							info.countrySubregions.put(abbre, name);
						}
					}
				}
				
				if(jsonInfo.has(CountryInfo.FLAG_ATTRIBUTES)){
					JSONArray jsonSubs = jsonInfo.getJSONArray(CountryInfo.FLAG_ATTRIBUTES);
					info.countryAttributes = new HashMap<String, String>();
					for(int j=0; j<jsonSubs.length(); j++){
						JSONObject jsonSub = jsonSubs.getJSONObject(j);
						String name = "", value = "";
						if(jsonSub.has(CountryInfo.FLAG_CS_VALUE)){
							value = jsonSub.getString(CountryInfo.FLAG_CS_VALUE);
						}
						if(jsonSub.has(CountryInfo.FLAG_CS_NAME)){
							name = jsonSub.getString(CountryInfo.FLAG_CS_NAME);
						}
						if(!name.equals("") && !name.equals("")){
							info.countryAttributes.put(name, value);
						}
					}
				}
				if(jsonInfo.has(CountryInfo.FLAG_LOCALIZED_SN)){
					info.localizedSubregionName = jsonInfo.getString(CountryInfo.FLAG_LOCALIZED_SN);
				}
				if(jsonInfo.has(CountryInfo.FLAG_LOCALIZED_PCN)){
					info.localizedPostalCodeName = jsonInfo.getString(CountryInfo.FLAG_LOCALIZED_PCN);
				}
				if(jsonInfo.has(CountryInfo.FLAG_PCAE)){
					info.postalCodeAuditExpression = jsonInfo.getString(CountryInfo.FLAG_PCAE);
				}
				if(jsonInfo.has(CountryInfo.FLAG_LOCALIZED_PCAEM)){
					info.localizedPostalCodeAuditErrorMessage = jsonInfo.getString(CountryInfo.FLAG_LOCALIZED_PCAEM);
				}
				if(jsonInfo.has(CountryInfo.FLAG_ADDRESS_STYLE)){
					info.addressStyle = jsonInfo.getString(CountryInfo.FLAG_ADDRESS_STYLE);
				}
				infos.add(info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return infos;
	}
	
	public List<Retailer> parseRetailers(String result){
		List<Retailer> retailers = new ArrayList<Retailer>();
		try {
			JSONObject jsObj = new JSONObject(result);
			JSONArray jsRetailers = jsObj.getJSONArray(Retailer.FLAG_RETAILERS);
			for(int i=0; i<jsRetailers.length(); i++){
				JSONObject jsRetailer = jsRetailers.getJSONObject(i);
				Retailer retailer = new Retailer();
				if(jsRetailer.has(Retailer.FLAG_ID)){
					retailer.setId(jsRetailer.getString(Retailer.FLAG_ID));
				}
				if(jsRetailer.has(Retailer.FLAG_NAME)){
					retailer.setName(jsRetailer.getString(Retailer.FLAG_NAME));
				}
				if(jsRetailer.has(Retailer.FLAG_SHIP_TO_HOME)){
					retailer.setShipToHome(jsRetailer.getBoolean(Retailer.FLAG_SHIP_TO_HOME));
				}
				if(jsRetailer.has(Retailer.FLAG_LG_GLYPH_URL)){
					retailer.setLgGlyphURL(jsRetailer.getString(Retailer.FLAG_LG_GLYPH_URL));
				}
				if(jsRetailer.has(Retailer.FLAG_SM_GLYPH_URL)){
					retailer.setSmGlyphURL(jsRetailer.getString(Retailer.FLAG_SM_GLYPH_URL));
				}
				if(jsRetailer.has(Retailer.FLAG_COUNTRY)){
					retailer.setCountry(jsRetailer.getString(Retailer.FLAG_COUNTRY));
				}
				if(jsRetailer.has(Retailer.FLAG_REQUIRED_CUSTOMER_INFO)){
					JSONArray jsRequiredInfos = jsRetailer.getJSONArray(Retailer.FLAG_REQUIRED_CUSTOMER_INFO);
					int[] requiredCustomerInfo = new int[jsRequiredInfos.length()];
					for(int j=0; j<jsRequiredInfos.length(); j++){
						requiredCustomerInfo[j] = Integer.parseInt(jsRequiredInfos.get(j).toString());
					}
					retailer.setRequiredCustomerInfo(requiredCustomerInfo);
				}
				if(jsRetailer.has(Retailer.FLAG_PAY_ONLINE)){
					retailer.setPayOnline(jsRetailer.getBoolean(Retailer.FLAG_PAY_ONLINE));
				}
				if(jsRetailer.has(Retailer.FLAG_CART_LIMIT)){
					JSONObject jsCartLimit = jsRetailer.getJSONObject(Retailer.FLAG_CART_LIMIT);
					CartLimit cartLimit = new Retailer.CartLimit();
					if(jsCartLimit.has(Retailer.FLAG_CL_CURRENCY)){
						cartLimit.currency = jsCartLimit.getString(Retailer.FLAG_CL_CURRENCY);
					}
					if(jsCartLimit.has(Retailer.FLAG_CL_CURRENCY_SYMBOL)){
						cartLimit.currencySymbol = jsCartLimit.getString(Retailer.FLAG_CL_CURRENCY_SYMBOL);
					}
					if(jsCartLimit.has(Retailer.FLAG_CL_PRICE)){
						cartLimit.price = jsCartLimit.getDouble(Retailer.FLAG_CL_PRICE);
					}
					if(jsCartLimit.has(Retailer.FLAG_CL_PRICE_STR)){
						cartLimit.PriceStr = jsCartLimit.getString(Retailer.FLAG_CL_PRICE_STR);
					}
					retailer.setCartLimit(cartLimit);
				}
				if(jsRetailer.has(Retailer.FLAG_CART_MINIMUM_LIMIT)){
					JSONObject jsCartMinLimit = jsRetailer.optJSONObject(Retailer.FLAG_CART_MINIMUM_LIMIT);
					CartLimit cartMinimumLimit = new CartLimit() ;
					if(jsCartMinLimit.has(Retailer.FLAG_CL_CURRENCY)){
						cartMinimumLimit.currency = jsCartMinLimit.getString(Retailer.FLAG_CL_CURRENCY);
					}
					if(jsCartMinLimit.has(Retailer.FLAG_CL_CURRENCY_SYMBOL)){
						cartMinimumLimit.currencySymbol = jsCartMinLimit.getString(Retailer.FLAG_CL_CURRENCY_SYMBOL);
					}
					if(jsCartMinLimit.has(Retailer.FLAG_CL_PRICE)){
						cartMinimumLimit.price = jsCartMinLimit.getDouble(Retailer.FLAG_CL_PRICE);
					}
					if(jsCartMinLimit.has(Retailer.FLAG_CL_PRICE_STR)){
						cartMinimumLimit.PriceStr = jsCartMinLimit.getString(Retailer.FLAG_CL_PRICE_STR);
					}
					retailer.setCartMinimumLimit(cartMinimumLimit) ;
				}
				
				if(jsRetailer.has(Retailer.FLAG_CLOLite)){
					retailer.setCLOLite(jsRetailer.getBoolean(Retailer.FLAG_CLOLite));
				}
				if(jsRetailer.has(Retailer.FLAG_IN_STORE)){
					retailer.setInStore(jsRetailer.getBoolean(Retailer.FLAG_IN_STORE));
				}
				retailers.add(retailer);
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return retailers;
	}
	
	public Pricing parsePricing(String result){
		Pricing pricing = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			pricing = parsePricing(jsObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return pricing;
	}
	
	public Pricing parsePricing3(String result){
		Pricing pricing = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(Cart.FLAG_CART)){
				JSONObject jsObj2 = jsObj.getJSONObject(Cart.FLAG_CART);
				pricing = parsePricing(jsObj2);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return pricing;
	}

	private Pricing parsePricing(JSONObject jsObj){
		Pricing pricing = null;
		try {
			if(jsObj.has(Pricing.FLAG_PRICING)){
				JSONObject jsPri = jsObj.getJSONObject(Pricing.FLAG_PRICING);
				pricing = new Pricing();
				if(jsPri.has(Pricing.FLAG_CURRENCY)){
					pricing.currency = jsPri.getString(Pricing.FLAG_CURRENCY);
				}
				if(jsPri.has(Pricing.FLAG_CURRENCY_SYMBOL)){
					pricing.currencySymbol = jsPri.getString(Pricing.FLAG_CURRENCY_SYMBOL);
				}
				if(jsPri.has(Pricing.FLAG_LINE_ITEMS)){
					JSONArray jsLineItems = jsPri.getJSONArray(Pricing.FLAG_LINE_ITEMS);
					pricing.lineItems = new ArrayList<Pricing.LineItem>();
					for(int i=0; i<jsLineItems.length(); i++){
						JSONObject jsItem = jsLineItems.getJSONObject(i);
						LineItem item = parseLineItem(jsItem);
						pricing.lineItems.add(item);
					}
				}
				if(jsPri.has(Pricing.FLAG_SUB_TOTAL)){
					JSONObject jsSt = jsPri.getJSONObject(Pricing.FLAG_SUB_TOTAL);
					pricing.subTotal = new UnitPrice();
					if(jsSt.has(UnitPrice.FLAG_PRICE)){
						pricing.subTotal.price = jsSt.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsSt.has(UnitPrice.FLAG_PRICE_STR)){
						pricing.subTotal.priceStr = jsSt.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if(jsPri.has(Pricing.FLAG_SAH_TOTAL)){
					JSONObject jsUp = jsPri.getJSONObject(Pricing.FLAG_SAH_TOTAL);
					pricing.shipAndHandling = new UnitPrice();
					if(jsUp.has(UnitPrice.FLAG_PRICE)){
						pricing.shipAndHandling.price = jsUp.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsUp.has(UnitPrice.FLAG_PRICE_STR)){
						pricing.shipAndHandling.priceStr = jsUp.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if(jsPri.has(Pricing.FLAG_TOTAL_SAVINGS)){
					JSONObject jsTs = jsPri.getJSONObject(Pricing.FLAG_TOTAL_SAVINGS);
					pricing.totalSavings = new UnitPrice();
					if(jsTs.has(UnitPrice.FLAG_PRICE)){
						pricing.totalSavings.price = jsTs.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsTs.has(UnitPrice.FLAG_PRICE_STR)){
						pricing.totalSavings.priceStr = jsTs.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if(jsPri.has(Pricing.FLAG_GRAND_TOTAL)){
					JSONObject jsGt = jsPri.getJSONObject(Pricing.FLAG_GRAND_TOTAL);
					pricing.grandTotal = new UnitPrice();
					if(jsGt.has(UnitPrice.FLAG_PRICE)){
						pricing.grandTotal.price = jsGt.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsGt.has(UnitPrice.FLAG_PRICE_STR)){
						pricing.grandTotal.priceStr = jsGt.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if(jsPri.has(Pricing.FLAG_TAX_BE_CAL_BY_RETAILER)){
					pricing.taxWillBeCalculatedByRetailer = jsPri.getBoolean(Pricing.FLAG_TAX_BE_CAL_BY_RETAILER);
				}
				if(jsPri.has(Pricing.FLAG_TAXES_ARE_ESTIMATED)){
					pricing.taxesAreEstimated = jsPri.getBoolean(Pricing.FLAG_TAXES_ARE_ESTIMATED);
				}
				if(jsPri.has(Pricing.FLAG_LIRU)){
					JSONArray jsLiru = jsPri.getJSONArray(Pricing.FLAG_LIRU);
					pricing.lineItemRollUps = new ArrayList<Pricing.LineItem>();
					for(int i=0; i<jsLiru.length(); i++){
						pricing.lineItemRollUps.add(parseLineItem(jsLiru.getJSONObject(i)));
					}
				}
			}
		} catch(JSONException e){
			e.printStackTrace();
		}
		return pricing;
		
	}
	
	private LineItem parseLineItem(JSONObject jsItem){
		LineItem item = new LineItem();
		try {
			if(jsItem.has(Pricing.FLAG_LI_NAME)){
				item.name = jsItem.getString(Pricing.FLAG_LI_NAME);
			}
			if(jsItem.has(Pricing.FLAG_LI_PRODUCT_DESCRIPTION_ID)){
				item.productDescriptionId = jsItem.getString(Pricing.FLAG_LI_PRODUCT_DESCRIPTION_ID);
			}
			if(jsItem.has(Pricing.FLAG_LI_QUANTITY)){
				item.quantity = jsItem.getInt(Pricing.FLAG_LI_QUANTITY);
			}
			if(jsItem.has(Pricing.FLAG_LI_UNIT_PRICE)){
				JSONObject jsUp = jsItem.getJSONObject(Pricing.FLAG_LI_UNIT_PRICE);
				item.unitPrice = new UnitPrice();
				if(jsUp.has(UnitPrice.FLAG_PRICE)){
					item.unitPrice.price = jsUp.getDouble(UnitPrice.FLAG_PRICE);
				}
				if(jsUp.has(UnitPrice.FLAG_PRICE_STR)){
					item.unitPrice.priceStr = jsUp.getString(UnitPrice.FLAG_PRICE_STR);
				}
			}
			if(jsItem.has(Pricing.FLAG_SUB_TOTAL)){
				JSONObject jsSubPri = jsItem.getJSONObject(Pricing.FLAG_SUB_TOTAL);
				item.subtotalPrice = new UnitPrice();
				if(jsSubPri.has(UnitPrice.FLAG_PRICE)){
					item.subtotalPrice.price = jsSubPri.getDouble(UnitPrice.FLAG_PRICE);
				}
				if(jsSubPri.has(UnitPrice.FLAG_PRICE_STR)){
					item.subtotalPrice.priceStr = jsSubPri.getString(UnitPrice.FLAG_PRICE_STR);
				}
			}
			if(jsItem.has(Pricing.FLAG_LI_TOTAL)){
				JSONObject jsUp = jsItem.getJSONObject(Pricing.FLAG_LI_TOTAL);
				item.totalPrice = new UnitPrice();
				if(jsUp.has(UnitPrice.FLAG_PRICE)){
					item.totalPrice.price = jsUp.getDouble(UnitPrice.FLAG_PRICE);
				}
				if(jsUp.has(UnitPrice.FLAG_PRICE_STR)){
					item.totalPrice.priceStr = jsUp.getString(UnitPrice.FLAG_PRICE_STR);
				}
			}
			if(jsItem.has(Pricing.FLAG_LI_CII)){
				JSONArray jsCii = jsItem.getJSONArray(Pricing.FLAG_LI_CII);
				item.cartItemIndices = new ArrayList<Integer>();
				for(int i=0; i<jsCii.length(); i++){
					item.cartItemIndices.add(jsCii.getInt(i));
				}
			}
			if(jsItem.has(Pricing.FLAG_LI_INCLUDED)){
				JSONArray jsIncluded = jsItem.getJSONArray(Pricing.FLAG_LI_INCLUDED);
				item.included = new ArrayList<Pricing.LineItem>();
				for(int i=0; i<jsIncluded.length(); i++){
					LineItem lineItem = parseLineItem(jsIncluded.getJSONObject(i));
					item.included.add(lineItem);
				}
			}
		} catch (JSONException e){
			e.printStackTrace();
		}
		return item;
	}
	
	/*
	 * for RSSMOBILEPDC-1380
	 */
	
	public Cart parseCart(String result){
		Cart cart = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			JSONObject jsCart = jsObj.getJSONObject(Cart.FLAG_CART);
			if(jsCart != null){
				cart = new Cart();
				cart.cartId = jsCart.optString(Cart.FLAG_ID);
				cart.retailerId = jsCart.optString(Cart.FLAG_RETAILER_ID);
				cart.storeId = jsCart.optString(Cart.FLAG_STORE_ID);
				if(jsCart.has(Cart.FLAG_PRICING)){
					cart.pricing = parsePricing(new JSONObject().put(Cart.FLAG_PRICING, jsCart.getJSONObject(Cart.FLAG_PRICING)).toString());
				}
				if(jsCart.has(Cart.FLAG_CART_ITEMS)){
					cart.cartItems = parseCartItem(jsCart.getJSONArray(Cart.FLAG_CART_ITEMS));
				}
				if(jsCart.has(Cart.FLAG_DISCOUNTS)){
					cart.discounts = parseDiscount(jsCart.getJSONArray(Cart.FLAG_DISCOUNTS),  cart.pricing);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return cart;
	}
	
	/*
	 * for RSSMOBILEPDC-1380
	 */
	private CartItem[] parseCartItem(JSONArray jsItems){
		CartItem[] items = null;
		try {
			items = new CartItem[jsItems.length()];
			for(int i=0; i<jsItems.length(); i++){
				JSONObject jsItem = jsItems.getJSONObject(i);
				CartItem item = new CartItem();
				if(jsItem.has(CartItem.FLAG_PRO_ID)){
					item.productId = jsItem.getString(CartItem.FLAG_PRO_ID);
				}
				if(jsItem.has(CartItem.FLAG_RES_TYPE)){
					item.resourceType = jsItem.getString(CartItem.FLAG_RES_TYPE);
				}
				if(jsItem.has(CartItem.FLAG_NAME)){
					item.name = jsItem.getString(CartItem.FLAG_NAME);
				}
				if(jsItem.has(CartItem.FLAG_QUANTITY)){
					item.quantity = jsItem.getInt(CartItem.FLAG_QUANTITY);
				}
				items[i] = item;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return items;
	}
	
	private Discount[] parseDiscount(JSONArray jsItems,Pricing pricing){
		Discount[] items = null;
		try {
			items = new Discount[jsItems.length()];
			for(int i=0; i<jsItems.length(); i++){
				JSONObject jsItem = jsItems.getJSONObject(i);
				Discount item = new Discount();
				if(jsItem.has(Discount.FLAG_CODE)){
					item.code = jsItem.getString(Discount.FLAG_CODE);
				}
				if(jsItem.has(Discount.FLAG_LOCALIZED_NAME)){
					item.localizedName = jsItem.getString(Discount.FLAG_LOCALIZED_NAME);
				}
				if(jsItem.has(Discount.FLAG_TERMS_AND_CONDITIONS_URL)){
					item.termsAndConditionsURL = jsItem.getString(Discount.FLAG_TERMS_AND_CONDITIONS_URL);
				}			
				if(jsItem.has(Discount.FLAG_STATUS)){
					item.status = jsItem.getInt(Discount.FLAG_STATUS);
				}
				if(jsItem.has(Discount.FLAG_LOCALIZED_STATUS_DESCRIPTION)){
					item.localizedStatusDescription = jsItem.getString(Discount.FLAG_LOCALIZED_STATUS_DESCRIPTION);
				}
							
				items[i] = item;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return items;
	}
	
	public List<Theme> parseThemes(String result) {
		List<Theme> themes = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(Theme.FLAG_ThemeResults) && jsObj.getJSONObject(Theme.FLAG_ThemeResults).has(Theme.FLAG_Themes)){
				themes = new ArrayList<Theme>();
				JSONArray jsThemes = jsObj.getJSONObject(Theme.FLAG_ThemeResults).getJSONArray(Theme.FLAG_Themes);
				for(int i=0; i<jsThemes.length(); i++){
					Theme theme = new Theme();
					JSONObject jsTheme = jsThemes.getJSONObject(i);
					if(jsTheme.has(Theme.FLAG_Id)){
						theme.id = jsTheme.getString(Theme.FLAG_Id);
					}
					if(jsTheme.has(Theme.FLAG_Name)){
						theme.name = jsTheme.getString(Theme.FLAG_Name);
					}
					if(jsTheme.has(Theme.FLAG_Glyph)){
						theme.glyph = jsTheme.getString(Theme.FLAG_Glyph);
					}
					if(jsTheme.has(Theme.FLAG_Music)){
						theme.music = jsTheme.getString(Theme.FLAG_Music);
					}
					if(jsTheme.has(Theme.FLAG_Backgrounds)){
						JSONArray jsBacks = jsTheme.getJSONArray(Theme.FLAG_Backgrounds);
						theme.backGrounds = new BackGround[jsBacks.length()];
						for(int j=0; j<jsBacks.length(); j++){
							BackGround backGround = new BackGround();
							JSONObject jsBack = jsBacks.getJSONObject(j);
							if(jsBack.has(Theme.FLAG_Id)){
								backGround.id = jsBack.getString(Theme.FLAG_Id);
							}
							if(jsBack.has(Theme.FLAG_Name)){
								backGround.name = jsBack.getString(Theme.FLAG_Name);
							}
							if(jsBack.has(BackGround.FLAG_ImageURL)){
								backGround.imageURL = jsBack.getString(BackGround.FLAG_ImageURL);
							}
							if(jsBack.has(BackGround.FLAG_GlyphURL)){
								backGround.glyphURL = jsBack.getString(BackGround.FLAG_GlyphURL);
							}
							theme.backGrounds[j] = backGround;
						}
					}
					themes.add(theme);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return themes;
	}
	
	private Font parseFont(JSONObject jsFont){
		try{
			Font font = new Font();
			if(jsFont.has(Font.FLAG_Name)){
				font.name = jsFont.getString(Font.FLAG_Name);
			}
			if(jsFont.has(Font.FLAG_DisplayName)){
				font.displayName = jsFont.getString(Font.FLAG_DisplayName);
			}
			if(jsFont.has(Font.FLAG_SampleURL)){
				font.sampleURL = jsFont.getString(Font.FLAG_SampleURL);
			}			
			if(jsFont.has(Font.FLAG_SizeMinMaxUsed)){
				try {
					font.sizeMinMaxUsed = jsFont.getInt(Font.FLAG_SizeMinMaxUsed);
				} catch (Exception e) {}			
			}			
			if(jsFont.has(Font.FLAG_Size)){				
				try {
					font.size = jsFont.getInt(Font.FLAG_Size);
				} catch (Exception e) {}								
			}
			if(jsFont.has(Font.FLAG_SizeMin)){
				font.sizeMin = jsFont.getInt(Font.FLAG_SizeMin);
			}
			if(jsFont.has(Font.FLAG_SizeMax)){
				font.sizeMax = jsFont.getInt(Font.FLAG_SizeMax);
			}
			return font;
		} catch(JSONException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Font> parseFonts(String result){
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(Font.FLAG_Fonts)){
				JSONArray jsFonts = jsObj.getJSONArray(Font.FLAG_Fonts);
				List<Font> fonts = new ArrayList<Font>();
				for(int i=0; i<jsFonts.length(); i++){
					JSONObject jsFont = jsFonts.getJSONObject(i);
					Font font = parseFont(jsFont);
					fonts.add(font);
				}
				return fonts;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public TextBlock parseTextBlock(String result){
		try{
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(TextBlock.TextBlock)){
				JSONObject jsTB = jsObj.getJSONObject(TextBlock.TextBlock);
				TextBlock textBlock = parseTextBlock(jsTB);
				return textBlock;
			}
		} catch (JSONException e){
			e.printStackTrace();
		}
		return null;
	}
	
	private TextBlock parseTextBlock(JSONObject jsTB) throws JSONException{
		TextBlock textBlock = new TextBlock();
		if(jsTB.has(TextBlock.Id)){
			textBlock.id = jsTB.getString(TextBlock.Id);
		}
		if(jsTB.has(TextBlock.Alignment)){
			Object o = jsTB.get(TextBlock.Alignment);
			try{
				int index = Integer.valueOf(o.toString());
				textBlock.alignment = TextBlock.fontAlignments[index];
			} catch (NumberFormatException e) {
				textBlock.alignment = (String) o;
			}
		}
		if(jsTB.has(TextBlock.Color)){
			textBlock.color = jsTB.getString(TextBlock.Color);
		}
		if(jsTB.has(TextBlock.Font)){
			JSONObject jsFont = jsTB.getJSONObject(TextBlock.Font);
			textBlock.font = parseFont(jsFont);
		}
		if(jsTB.has(TextBlock.Justification)){
			Object o = jsTB.get(TextBlock.Justification);
			try{
				int index = Integer.valueOf(o.toString());
				textBlock.justification = TextBlock.fontJustifications[index];
			} catch (NumberFormatException e) {
				textBlock.justification = (String) o;
			}
			//textBlock.justification = jsTB.getString(TextBlock.Justification);
		}
		if(jsTB.has(TextBlock.Language)){
			textBlock.language = jsTB.getString(TextBlock.Language);
		}
		if(jsTB.has(TextBlock.SIZE_MIN)){
			textBlock.sizeMin = jsTB.getInt(TextBlock.SIZE_MIN);
		}
		if(jsTB.has(TextBlock.SIZE_MAX)){
			textBlock.sizeMax = jsTB.getInt(TextBlock.SIZE_MAX);
		}
		if(jsTB.has(TextBlock.TEXT)){
			textBlock.text = jsTB.getString(TextBlock.TEXT);
		}
		return textBlock;
	}
	
	public List<TextBlock> parseTextBlocks(String result) {
		List<TextBlock> textBlocks = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			JSONArray jsTextBlocks = jsObj.optJSONArray(TextBlock.TextBlocks);
			if(jsTextBlocks != null){
				textBlocks = new ArrayList<TextBlock>();
				for(int i=0; i<jsTextBlocks.length(); i++){
					TextBlock textBlock = parseTextBlock(jsTextBlocks.getJSONObject(i));
					textBlocks.add(textBlock);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return textBlocks;
	}
}
