package com.kodak.rss.core.n2r.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.AuthorizationToken;
import com.kodak.rss.core.n2r.bean.content.ServerPhoto;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.bean.content.Theme.BackGround;
import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;
import com.kodak.rss.core.n2r.bean.prints.Data;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.prints.Page;
import com.kodak.rss.core.n2r.bean.prints.StandardPrint;
import com.kodak.rss.core.n2r.bean.project.Project;
import com.kodak.rss.core.n2r.bean.project.ProjectSearchResult;
import com.kodak.rss.core.n2r.bean.project.Resource;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.CountryInfo;
import com.kodak.rss.core.n2r.bean.retailer.ProductDescription;
import com.kodak.rss.core.n2r.bean.retailer.Retailer;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry.UnitPrice;
import com.kodak.rss.core.n2r.bean.shoppingcart.Cart;
import com.kodak.rss.core.n2r.bean.shoppingcart.CartItem;
import com.kodak.rss.core.n2r.bean.shoppingcart.Discount;
import com.kodak.rss.core.n2r.bean.shoppingcart.NewOrder;
import com.kodak.rss.core.n2r.bean.shoppingcart.Pricing;
import com.kodak.rss.core.n2r.bean.shoppingcart.Pricing.LineItem;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo.StoreHour;
import com.kodak.rss.core.n2r.bean.text.Font;
import com.kodak.rss.core.n2r.bean.text.TextBlock;
import com.kodak.rss.core.n2r.bean.upload.ImageResource;

public class Parse {
	
	public Parse(){};

	public AuthorizationToken parseAuthorizationToken(String result) throws RssWebServiceException{
		checkError(result);
		AuthorizationToken authorizationToken = null;
		try {
			JSONObject jsonObject = new JSONObject(result);
			authorizationToken = new AuthorizationToken();
			JSONObject jsonAccessTokenResponse = jsonObject.getJSONObject(AuthorizationToken.ACCESS_TOKEN_RESPONSE);
			authorizationToken.accessToken = jsonAccessTokenResponse.getString(AuthorizationToken.ACCESS_TOKEN);
			authorizationToken.tokenType = jsonAccessTokenResponse.getString(AuthorizationToken.TOKEN_TYPE);
			authorizationToken.expiresIn = jsonAccessTokenResponse.getInt(AuthorizationToken.EXPIRES_IN);
			authorizationToken.refreshToken = jsonAccessTokenResponse.getString(AuthorizationToken.REFRESH_TOKEN);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return authorizationToken;
	}
	
	public List<Retailer> parseRetailers(String result) throws RssWebServiceException{
		checkError(result);
		List<Retailer> retailers = new ArrayList<Retailer>();
		try {
			JSONObject jsObj = new JSONObject(result);
			JSONArray jsRetailers = jsObj.getJSONArray(Retailer.FLAG_RETAILERS);
			for(int i=0; i<jsRetailers.length(); i++){
				JSONObject jsRetailer = jsRetailers.getJSONObject(i);
				Retailer retailer = new Retailer();
				if(jsRetailer.has(Retailer.FLAG_ID)){
					retailer.id = jsRetailer.getString(Retailer.FLAG_ID);
				}
				if(jsRetailer.has(Retailer.FLAG_NAME)){
					retailer.name = jsRetailer.getString(Retailer.FLAG_NAME);
				}
				if(jsRetailer.has(Retailer.FLAG_SHIP_TO_HOME)){
					retailer.shipToHome = jsRetailer.getBoolean(Retailer.FLAG_SHIP_TO_HOME);
				}
				if(jsRetailer.has(Retailer.FLAG_LG_GLYPH_URL)){
					retailer.lgGlyphURL = jsRetailer.getString(Retailer.FLAG_LG_GLYPH_URL);
				}
				if(jsRetailer.has(Retailer.FLAG_SM_GLYPH_URL)){
					retailer.smGlyphURL = jsRetailer.getString(Retailer.FLAG_SM_GLYPH_URL);
				}
				if(jsRetailer.has(Retailer.FLAG_COUNTRY)){
					retailer.country = jsRetailer.getString(Retailer.FLAG_COUNTRY);
				}
				if(jsRetailer.has(Retailer.FLAG_REQUIRED_CUSTOMER_INFO)){
					JSONArray jsRequiredInfos = jsRetailer.getJSONArray(Retailer.FLAG_REQUIRED_CUSTOMER_INFO);
					retailer.requiredCustomerInfo = new int[jsRequiredInfos.length()];
					for(int j=0; j<jsRequiredInfos.length(); j++){
						retailer.requiredCustomerInfo[j] = Integer.parseInt(jsRequiredInfos.get(j).toString());
					}
				}
				if(jsRetailer.has(Retailer.FLAG_PAY_ONLINE)){
					retailer.payOnline = jsRetailer.getBoolean(Retailer.FLAG_PAY_ONLINE);
				}
				if(jsRetailer.has(Retailer.FLAG_CART_LIMIT)){
					JSONObject jsCartLimit = jsRetailer.getJSONObject(Retailer.FLAG_CART_LIMIT);
					retailer.cartLimit = new Retailer.CartLimit();
					if(jsCartLimit.has(Retailer.FLAG_CL_CURRENCY)){
						retailer.cartLimit.currency = jsCartLimit.getString(Retailer.FLAG_CL_CURRENCY);
					}
					if(jsCartLimit.has(Retailer.FLAG_CL_CURRENCY_SYMBOL)){
						retailer.cartLimit.currencySymbol = jsCartLimit.getString(Retailer.FLAG_CL_CURRENCY_SYMBOL);
					}
					if(jsCartLimit.has(Retailer.FLAG_CL_PRICE)){
						retailer.cartLimit.price = jsCartLimit.getInt(Retailer.FLAG_CL_PRICE);
					}
					if(jsCartLimit.has(Retailer.FLAG_CL_PRICE_STR)){
						retailer.cartLimit.PriceStr = jsCartLimit.getString(Retailer.FLAG_CL_PRICE_STR);
					}
				}
				if(jsRetailer.has(Retailer.FLAG_CART_MINIMUM_LIMIT)){
					JSONObject jsCartMinLimit = jsRetailer.getJSONObject(Retailer.FLAG_CART_MINIMUM_LIMIT);
					retailer.cartMinimumLimit = new Retailer.CartLimit();
					if(jsCartMinLimit.has(Retailer.FLAG_CL_CURRENCY)){
						retailer.cartMinimumLimit.currency = jsCartMinLimit.getString(Retailer.FLAG_CL_CURRENCY);
					}
					if(jsCartMinLimit.has(Retailer.FLAG_CL_CURRENCY_SYMBOL)){
						retailer.cartMinimumLimit.currencySymbol = jsCartMinLimit.getString(Retailer.FLAG_CL_CURRENCY_SYMBOL);
					}
					if(jsCartMinLimit.has(Retailer.FLAG_CL_PRICE)){
						retailer.cartMinimumLimit.price = jsCartMinLimit.getInt(Retailer.FLAG_CL_PRICE);
					}
					if(jsCartMinLimit.has(Retailer.FLAG_CL_PRICE_STR)){
						retailer.cartMinimumLimit.PriceStr = jsCartMinLimit.getString(Retailer.FLAG_CL_PRICE_STR);
					}
				}

				if(jsRetailer.has(Retailer.FLAG_CLOLITE)){
					retailer.cloLite = jsRetailer.getBoolean(Retailer.FLAG_CLOLITE);
				}
				retailers.add(retailer);
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return retailers;
	}
	
	public List<Catalog> parseCatalogs(String result) throws RssWebServiceException{
		checkError(result);
		List<Catalog> catalogs = new ArrayList<Catalog>();
		try {
			JSONObject jsObj = new JSONObject(result);
			JSONArray jsCatalogs = jsObj.getJSONArray(Catalog.FLAG_CATALOGS);
			for(int i=0; i<jsCatalogs.length(); i++){
				JSONObject jsCata = jsCatalogs.getJSONObject(i);
				Catalog catalog = new Catalog();
				if(jsCata.has(Catalog.FLAG_CURRENCY)){
					catalog.currency = jsCata.getString(Catalog.FLAG_CURRENCY);
				}
				if(jsCata.has(Catalog.FLAG_CURRENCY_SYMBOL)){
					catalog.currencySymbol = jsCata.getString(Catalog.FLAG_CURRENCY_SYMBOL);
				}
				if(jsCata.has(Catalog.FLAG_PRICES_INCLUDE_TAX)){
					catalog.pricesIncludeTax = jsCata.getBoolean(Catalog.FLAG_PRICES_INCLUDE_TAX);
				}
				if(jsCata.has(Catalog.FLAG_ENTRIES)){
					JSONArray jsEntries = jsCata.getJSONArray(Catalog.FLAG_ENTRIES);
					catalog.rssEntries = new ArrayList<RssEntry>();
					for(int j=0; j<jsEntries.length(); j++){
						JSONObject jsEntry = jsEntries.getJSONObject(j);
						RssEntry entry = new RssEntry();
						if(jsEntry.has(RssEntry.FLAG_PRODUCT_DESCRIPTION)){
							JSONObject jsProDes = jsEntry.getJSONObject(RssEntry.FLAG_PRODUCT_DESCRIPTION);
							entry.proDescription = new ProductDescription();
							if(jsProDes.has(ProductDescription.FLAG_ID)){
								entry.proDescription.id = jsProDes.getString(ProductDescription.FLAG_ID);
							}
							if(jsProDes.has(ProductDescription.FLAG_NAME)){
								entry.proDescription.name = jsProDes.getString(ProductDescription.FLAG_NAME);
							}
							if(jsProDes.has(ProductDescription.FLAG_SHORT_NAME)){
								entry.proDescription.shortName = jsProDes.getString(ProductDescription.FLAG_SHORT_NAME);
							}
							if(jsProDes.has(ProductDescription.FLAG_TYPE)){
								entry.proDescription.type = jsProDes.getString(ProductDescription.FLAG_TYPE);
							}
							if(jsProDes.has(ProductDescription.FLAG_PAGE_WIDTH)){
								entry.proDescription.pageWidth = jsProDes.getInt(ProductDescription.FLAG_PAGE_WIDTH);
							}
							if(jsProDes.has(ProductDescription.FLAG_PAGE_HEIGHT)){
								entry.proDescription.pageHeight = jsProDes.getInt(ProductDescription.FLAG_PAGE_HEIGHT);
							}
							if(jsProDes.has(ProductDescription.FLAG_LG_GLYPH_URL)){
								entry.proDescription.lgGlyphURL = jsProDes.getString(ProductDescription.FLAG_LG_GLYPH_URL);
							}
							if(jsProDes.has(ProductDescription.FLAG_Sm_GLYPH_URL)){
								entry.proDescription.smGlyphURL = jsProDes.getString(ProductDescription.FLAG_Sm_GLYPH_URL);
							}
							if(jsProDes.has(ProductDescription.FLAG_ATTIBUTES)){
								JSONArray jsAtts = jsProDes.getJSONArray(ProductDescription.FLAG_ATTIBUTES);
								entry.proDescription.attributes = new HashMap<String, String>();
								for(int n=0; n<jsAtts.length(); n++){
									String name = "", value = "";
									JSONObject jsAtt = jsAtts.getJSONObject(n);
									if(jsAtt.has(ProductDescription.FLAG_ATT_NAME)){
										name = jsAtt.getString(ProductDescription.FLAG_ATT_NAME);
									}
									if(jsAtt.has(ProductDescription.FLAG_ATT_VALUE)){
										value = jsAtt.get(ProductDescription.FLAG_ATT_VALUE).toString();
										//value = jsAtt.getString(ProductDescription.FLAG_ATT_VALUE);
									}
									if(!name.equals("") && !value.equals("")){
										entry.proDescription.attributes.put(name, value);
									}
								}
							}
						}
						if(jsEntry.has(RssEntry.FLAG_MAX_UNIT_PRICE)){
							entry.maxUnitPrice = new RssEntry.UnitPrice();
							JSONObject jsPrice = jsEntry.getJSONObject(RssEntry.FLAG_MAX_UNIT_PRICE);
							if(jsPrice.has(RssEntry.UnitPrice.FLAG_PRICE)){
								entry.maxUnitPrice.price = jsPrice.getDouble(RssEntry.UnitPrice.FLAG_PRICE);
							}
							if(jsPrice.has(RssEntry.UnitPrice.FLAG_PRICE_STR)){
								entry.maxUnitPrice.priceStr = jsPrice.getString(RssEntry.UnitPrice.FLAG_PRICE_STR);
							}
						}
						if(jsEntry.has(RssEntry.FLAG_MIN_UNIT_PRICE)){
							entry.minUnitPrice = new RssEntry.UnitPrice();
							JSONObject jsPrice = jsEntry.getJSONObject(RssEntry.FLAG_MIN_UNIT_PRICE);
							if(jsPrice.has(RssEntry.UnitPrice.FLAG_PRICE)){
								entry.minUnitPrice.price = jsPrice.getDouble(RssEntry.UnitPrice.FLAG_PRICE);
							}
							if(jsPrice.has(RssEntry.UnitPrice.FLAG_PRICE_STR)){
								entry.minUnitPrice.priceStr = jsPrice.getString(RssEntry.UnitPrice.FLAG_PRICE_STR);
							}
						}
						catalog.rssEntries.add(entry);
					}
				}
				if(jsCata.has(Catalog.FLAG_CURRENCY)){
					catalog.currency = jsCata.getString(Catalog.FLAG_CURRENCY);
				}
				if(jsCata.has(Catalog.FLAG_CURRENCY)){
					catalog.currency = jsCata.getString(Catalog.FLAG_CURRENCY);
				}
				if(jsCata.has(Catalog.FLAG_CURRENCY)){
					catalog.currency = jsCata.getString(Catalog.FLAG_CURRENCY);
				}
				catalogs.add(catalog);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return catalogs;
	}
	
	public HashMap<String, String> parseCountries(String result) throws RssWebServiceException{
		checkError(result);
		HashMap<String, String> countries = new HashMap<String, String>();
		try {
			JSONObject jsonObject = new JSONObject(result);
			if(jsonObject.has("Countries")){
				JSONArray jsonArrCountries = jsonObject.getJSONArray("Countries");
				for(int i=0; i<jsonArrCountries.length(); i++){
					JSONObject jsonCountry = jsonArrCountries.getJSONObject(i);
					String countryCode = "", countryName = "";
					if(jsonCountry.has("CountryCode")){
						countryCode = jsonCountry.getString("CountryCode");
					}
					if(jsonCountry.has("LocalizedCountryName")){
						countryName = jsonCountry.getString("LocalizedCountryName");
					}
					if(!countryCode.equals("")&&!countryName.equals("")){
						countries.put(countryCode, countryName);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return countries;
	}
	
	public List<CountryInfo> parseCountryInfo(String result) throws RssWebServiceException{
		checkError(result);
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
				/*
				 *  for RSSMOBILEPDC-1566
				 */
				if(jsonInfo.has(CountryInfo.FLAG_COUNTRY_ATTRIBUTES)){
					JSONArray jsonSubs = jsonInfo.getJSONArray(CountryInfo.FLAG_COUNTRY_ATTRIBUTES);
					info.countryAttributes = new HashMap<String, String>();
					for(int j=0; j<jsonSubs.length(); j++){
						JSONObject jsonSub = jsonSubs.getJSONObject(j);
						String value = "", name = "";
						if(jsonSub.has(CountryInfo.FLAG_CS_VALUE)){
							value = jsonSub.getString(CountryInfo.FLAG_CS_VALUE);
						}
						if(jsonSub.has(CountryInfo.FLAG_CS_NAME)){
							name = jsonSub.getString(CountryInfo.FLAG_CS_NAME);
						}
						if(!value.equals("") && !name.equals("")){
							info.countryAttributes.put(name,value);
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

	public List<Theme> parseThemes(String result) throws RssWebServiceException{
		checkError(result);
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
	
	public List<ColorEffect> parseColorEffects(String result) throws RssWebServiceException{
		checkError(result);
		List<ColorEffect> colorEffects = new ArrayList<ColorEffect>();
		try{
			JSONObject jsonObj = new JSONObject(result);
			JSONArray jsonEffects = jsonObj.getJSONArray(ColorEffect.FLAG_AVAILABLE_COLOR_EFFECTS);
			for(int i=0; i<jsonEffects.length(); i++){
				JSONObject jsonEffect = jsonEffects.getJSONObject(i);
				ColorEffect colorEffect = new ColorEffect();
				if(jsonEffect.has(ColorEffect.FLAG_ID)){
					colorEffect.id = jsonEffect.getInt(ColorEffect.FLAG_ID);
				}
				if(jsonEffect.has(ColorEffect.FLAG_NAME)){
					colorEffect.name = jsonEffect.getString(ColorEffect.FLAG_NAME);
				}
				if(jsonEffect.has(ColorEffect.FLAG_GLYPH_PATH_URL)){
					colorEffect.glyphPathUrl = jsonEffect.getString(ColorEffect.FLAG_GLYPH_PATH_URL);
				}
				colorEffects.add(colorEffect);
			}
		} catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		return colorEffects;
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
	
	public TextBlock parseTextBlock(String result) throws RssWebServiceException{
		checkError(result);
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
	
	public TextBlock parseTextBlock(JSONObject jsTextBlock) {
		TextBlock textBlock = new TextBlock();
		try {
			if(jsTextBlock.has(TextBlock.Id)){
				textBlock.id = jsTextBlock.getString(TextBlock.Id);
			}
			if(jsTextBlock.has(TextBlock.Alignment)){
				Object o = jsTextBlock.get(TextBlock.Alignment);
				try{
					int index = Integer.valueOf(o.toString());
					textBlock.alignment = TextBlock.fontAlignments[index];
				} catch (NumberFormatException e) {
					textBlock.alignment = (String) o;
				}
			}
			if(jsTextBlock.has(TextBlock.Color)){
				textBlock.color = jsTextBlock.getString(TextBlock.Color);
			}
			if(jsTextBlock.has(TextBlock.Font)){
				JSONObject jsFont = jsTextBlock.getJSONObject(TextBlock.Font);
				textBlock.font = parseFont(jsFont);
			}
			if(jsTextBlock.has(TextBlock.Justification)){
				Object o = jsTextBlock.get(TextBlock.Justification);
				try{
					int index = Integer.valueOf(o.toString());
					textBlock.justification = TextBlock.fontJustifications[index];
				} catch (NumberFormatException e) {
					textBlock.justification = (String) o;
				}
				//textBlock.justification = jsTB.getString(TextBlock.Justification);
			}
			if(jsTextBlock.has(TextBlock.Language)){
				textBlock.language = jsTextBlock.getString(TextBlock.Language);
			}
			if(jsTextBlock.has(TextBlock.SIZE_MIN)){
				textBlock.sizeMin = jsTextBlock.getInt(TextBlock.SIZE_MIN);
			}
			if(jsTextBlock.has(TextBlock.SIZE_MAX)){
				textBlock.sizeMax = jsTextBlock.getInt(TextBlock.SIZE_MAX);
			}
			if(jsTextBlock.has(TextBlock.TEXT)){
				textBlock.text = jsTextBlock.getString(TextBlock.TEXT);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return textBlock;
	}
	
	public List<TextBlock> parseTextBlocks(String result) throws RssWebServiceException {
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
	
	public List<StoreInfo> parseStoresInfo(String result) throws RssWebServiceException{
		checkError(result);
		List<StoreInfo> stores = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			JSONArray jsStores = jsObj.getJSONArray(StoreInfo.FLAG_STORES);
			stores = new ArrayList<StoreInfo>();
			for(int i=0; i<jsStores.length(); i++){
				JSONObject jsStore = jsStores.getJSONObject(i);
				StoreInfo store = parseStoreInfo(jsStore);
				stores.add(store);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return stores;
	}
	
	public StoreInfo parseStoreInfo(JSONObject jsStore) throws JSONException{
		StoreInfo store = new StoreInfo();
		if(jsStore.has(StoreInfo.FLAG_ID)){
			store.id = jsStore.getString(StoreInfo.FLAG_ID);
		}
		
		if(jsStore.has(StoreInfo.FLAG_RETAILER_ID)){
			store.retailerID = jsStore.getString(StoreInfo.FLAG_RETAILER_ID);
		}
		if(jsStore.has(StoreInfo.FLAG_NAME)){
			store.name = jsStore.getString(StoreInfo.FLAG_NAME);
		}
		if(jsStore.has(StoreInfo.FLAG_ADDRESS)){
			JSONObject jsAddress = jsStore.getJSONObject(StoreInfo.FLAG_ADDRESS);
			store.address = new StoreInfo.StoreAddress();
			if(jsAddress.has(StoreInfo.FLAG_ADD_ADDRESS1)){
				store.address.address1 = jsAddress.getString(StoreInfo.FLAG_ADD_ADDRESS1);
			}
			if(jsAddress.has(StoreInfo.FLAG_ADD_ADDRESS2)){
				store.address.address2 = jsAddress.getString(StoreInfo.FLAG_ADD_ADDRESS2);
			}
			if(jsAddress.has(StoreInfo.FLAG_ADD_ADDRESS3)){
				store.address.address3 = jsAddress.getString(StoreInfo.FLAG_ADD_ADDRESS3);
			}
			if(jsAddress.has(StoreInfo.FLAG_ADD_CITY)){
				store.address.city = jsAddress.getString(StoreInfo.FLAG_ADD_CITY);
			}
			if(jsAddress.has(StoreInfo.FLAG_ADD_STATE_PROVINCE)){
				store.address.stateProvince = jsAddress.getString(StoreInfo.FLAG_ADD_STATE_PROVINCE);
			}
			if(jsAddress.has(StoreInfo.FLAG_ADD_POSTAL_CODE)){
				store.address.postalCode = jsAddress.getString(StoreInfo.FLAG_ADD_POSTAL_CODE);
			}
			if(jsAddress.has(StoreInfo.FLAG_ADD_COUNTRY)){
				store.address.country = jsAddress.getString(StoreInfo.FLAG_ADD_COUNTRY);
			}
		}
		if(jsStore.has(StoreInfo.FLAG_PHONE)){
			store.phone = jsStore.getString(StoreInfo.FLAG_PHONE);
		}
		
		if(jsStore.has(StoreInfo.FLAG_EMAIL)){
			store.email = jsStore.getString(StoreInfo.FLAG_EMAIL);
		}
		if(jsStore.has(StoreInfo.FLAG_LON)){
			store.longitude = jsStore.getDouble(StoreInfo.FLAG_LON);
		}
		if(jsStore.has(StoreInfo.FLAG_LAT)){
			store.latitude = jsStore.getDouble(StoreInfo.FLAG_LAT);
		}
		if(jsStore.has(StoreInfo.FLAG_MILES)){
			store.miles = jsStore.getDouble(StoreInfo.FLAG_MILES);
		}
		if(jsStore.has(StoreInfo.FLAG_HOURS)){
			JSONArray jsHours = jsStore.getJSONArray(StoreInfo.FLAG_HOURS);
			store.hours = new ArrayList<StoreInfo.StoreHour>();
			for(int j=0; j<jsHours.length(); j++){
				JSONObject jsHour = jsHours.getJSONObject(j);
				StoreHour hour = new StoreInfo.StoreHour();
				if(jsHour.has(StoreInfo.FLAG_H_DAY)){
					hour.day = jsHour.getInt(StoreInfo.FLAG_H_DAY);
				}
				if(jsHour.has(StoreInfo.FLAG_H_OPEN)){
					hour.open = jsHour.getString(StoreInfo.FLAG_H_OPEN);
				}
				if(jsHour.has(StoreInfo.FLAG_H_CLOSE)){
					hour.close = jsHour.getString(StoreInfo.FLAG_H_CLOSE);
				}
				store.hours.add(hour);
			}
		}
		if(jsStore.has(StoreInfo.FLAG_IS_A_TEST_STORE)){
			store.isATestStore = jsStore.getBoolean(StoreInfo.FLAG_IS_A_TEST_STORE);
		}
		
		return store;
	}
	
	public Resource parseResource(String result) throws RssWebServiceException{
		checkError(result);
		Resource res = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(Resource.FLAG_RESOURCE)){
				JSONObject jsImgRes = jsObj.getJSONObject(Resource.FLAG_RESOURCE);
				res = new Resource();
				if(jsImgRes.has(Resource.FLAG_BASE_URI)){
					res.baseURI = jsImgRes.getString(Resource.FLAG_BASE_URI);
				}
				if(jsImgRes.has(Resource.FLAG_ID)){
					res.id = jsImgRes.getString(Resource.FLAG_ID);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public ImageResource parseImageResource(String result) throws RssWebServiceException{
		checkError(result);
		ImageResource res = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(Resource.FLAG_RESOURCE)){
				JSONObject jsImgRes = jsObj.getJSONObject(Resource.FLAG_RESOURCE);
				res = new ImageResource();
				if(jsImgRes.has(Resource.FLAG_BASE_URI)){
					res.baseURI = jsImgRes.getString(Resource.FLAG_BASE_URI);
				}
				if(jsImgRes.has(Resource.FLAG_ID)){
					res.id = jsImgRes.getString(Resource.FLAG_ID);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public ImageResource parseImageResource(String result,boolean isUseUrl) throws RssWebServiceException{
		checkError(result);
		final String IMAGES = "Images";
		ImageResource res = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(IMAGES)){
				JSONArray  jsImgRes = jsObj.getJSONArray(IMAGES);
				if(jsImgRes.length()>0){
					res = new ImageResource();
					JSONObject jsImage = jsImgRes.getJSONObject(0);
					if(jsImage.has(Resource.FLAG_BASE_URI)){
						res.baseURI = jsImage.getString(Resource.FLAG_BASE_URI);
					}
					if(jsImage.has(Resource.FLAG_ID)){
						res.id = jsImage.getString(Resource.FLAG_ID);
					}
					if(jsImage.has(Resource.FLAG_ORIGINALURL)){
						res.originalURL = jsImage.getString(Resource.FLAG_ORIGINALURL);
					}
				}				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	
	public String parseImage(String result) throws RssWebServiceException{
		checkError(result);
		final String IMAGE = "Image";
		final String ID = "Id";
		String imageId = "";
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(IMAGE)){
				JSONObject jsImg = jsObj.getJSONObject(IMAGE);
				if(jsImg.has(ID)){
					imageId = jsImg.getString(ID);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return imageId;
	}
	
	public Pricing parsePricing(String result) throws RssWebServiceException{
		checkError(result);
		Pricing pricing = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			pricing = parsePricing(jsObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return pricing;
	}
	
	public Pricing parsePricing3(String result) throws RssWebServiceException{
		checkError(result);
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
				if(jsPri.has(Pricing.FLAG_TOTAL_SAVINGS)){
					JSONObject jsSt = jsPri.getJSONObject(Pricing.FLAG_TOTAL_SAVINGS);
					pricing.totalSavings = new UnitPrice();
					if(jsSt.has(UnitPrice.FLAG_PRICE)){
						pricing.totalSavings.price = jsSt.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsSt.has(UnitPrice.FLAG_PRICE_STR)){
						pricing.totalSavings.priceStr = jsSt.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				
				if(jsPri.has(Pricing.FLAG_SHIP_AND_HAN)){
					JSONObject jsSah = jsPri.getJSONObject(Pricing.FLAG_SHIP_AND_HAN);
					pricing.shipAndHandling = new UnitPrice();
					if(jsSah.has(UnitPrice.FLAG_PRICE)){
						pricing.shipAndHandling.price = jsSah.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsSah.has(UnitPrice.FLAG_PRICE_STR)){
						pricing.shipAndHandling.priceStr = jsSah.getString(UnitPrice.FLAG_PRICE_STR);
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
	
	public Cart parseCart(String result) throws RssWebServiceException{
		checkError(result);
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
	
	public CartItem[] parseCartItems(String result) throws RssWebServiceException{
		checkError(result);
		CartItem[] items = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(CartItem.FLAG_CART_ITEMS)){
				JSONArray jsItems = jsObj.getJSONArray(CartItem.FLAG_CART_ITEMS);
				items = parseCartItem(jsItems);
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
	
	
	public List<StandardPrint> parseStandardSevicePrints(String result) throws RssWebServiceException{
		checkError(result);
		List<StandardPrint> prints = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(StandardPrint.FLAG_STANDAR_PRINTS)){
				prints = new ArrayList<StandardPrint>();
				JSONArray jsPrints = jsObj.getJSONArray(StandardPrint.FLAG_STANDAR_PRINTS);
				for(int i=0; i<jsPrints.length(); i++){
					StandardPrint print = new StandardPrint();
					JSONObject jsPrint = jsPrints.getJSONObject(i);
					if(jsPrint.has(StandardPrint.FLAG_ID)){
						print.id = jsPrint.getString(StandardPrint.FLAG_ID);
					}
					if(jsPrint.has(StandardPrint.FLAG_DATE)){
						print.date = jsPrint.getString(StandardPrint.FLAG_DATE);
					}
					if(jsPrint.has(StandardPrint.FLAG_PRO_DESC_ID)){
						print.proDescId = jsPrint.getString(StandardPrint.FLAG_PRO_DESC_ID);
					}
					if(jsPrint.has(StandardPrint.FLAG_PAGE)){
						print.page = parsePage(jsPrint.getJSONObject(StandardPrint.FLAG_PAGE));
					}
					prints.add(print);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return prints;
	}
	
	private Page[] parsePages(JSONArray jsonPages){
		try {
			Page[] pages = null;
			if(jsonPages!=null){
				pages = new Page[jsonPages.length()];
				for(int i=0; i<jsonPages.length(); i++){
					JSONObject jsonPage = jsonPages.getJSONObject(i);
					Page page = parsePage(jsonPage);	
					pages[i] = page;
				}
			}
			return pages;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Page parsePage(JSONObject jsonPage){
		Page page = null;
		if(jsonPage!=null){
			try {
				page = new Page();
				if(jsonPage.has(Page.FLAG_BASE_URI)){
					page.baseURI = jsonPage.getString(Page.FLAG_BASE_URI);
				}
				if(jsonPage.has(Page.FLAG_ID)){
					page.id = jsonPage.getString(Page.FLAG_ID);
				}
				if(jsonPage.has(Page.FLAG_SEQUENCE_NUMBER)){
					page.sequenceNumber = jsonPage.getInt(Page.FLAG_SEQUENCE_NUMBER);
				}
				if(jsonPage.has(Page.FLAG_WIDTH)){
					page.width = (float) jsonPage.getDouble(Page.FLAG_WIDTH);
				}
				if(jsonPage.has(Page.FLAG_HEIGHT)){
					page.height = (float) jsonPage.getDouble(Page.FLAG_HEIGHT);
				}
				if(jsonPage.has(Page.FLAG_MIN_NUM_OF_IMAGES)){
					page.minNumberOfImages = jsonPage.getString(Page.FLAG_MIN_NUM_OF_IMAGES);
				}
				if(jsonPage.has(Page.FLAG_MAX_NUM_OF_IMAGES)){
					page.maxNumberOfImages = jsonPage.getString(Page.FLAG_MAX_NUM_OF_IMAGES);
				}
				if(jsonPage.has(Page.FLAG_LAYERS)){
					// just for Print products
					String length = "1";
					if(!"null".equals(page.maxNumberOfImages)){
						length = page.maxNumberOfImages;
					}
					page.layers = parseLayers(jsonPage.getJSONArray(Page.FLAG_LAYERS),Integer.valueOf(length));
				}
				if(jsonPage.has(Page.FLAG_MARGIN)){
					JSONObject jsonMargin = jsonPage.getJSONObject(Page.FLAG_MARGIN);
					if(jsonMargin.has(Page.FLAG_MARGIN_TOP)){
						page.margin[0] = (float) jsonMargin.getDouble(Page.FLAG_MARGIN_TOP);
					}
					if(jsonMargin.has(Page.FLAG_MARGIN_LEFT)){
						page.margin[1] = (float) jsonMargin.getDouble(Page.FLAG_MARGIN_LEFT);
					}
					if(jsonMargin.has(Page.FLAG_MARGIN_BOTTOM)){
						page.margin[2] = (float) jsonMargin.getDouble(Page.FLAG_MARGIN_BOTTOM);
					}
					if(jsonMargin.has(Page.FLAG_MARGIN_RIGHT)){
						page.margin[3] = (float) jsonMargin.getDouble(Page.FLAG_MARGIN_RIGHT);
					}
				}
			} catch(JSONException je){
				je.printStackTrace();
			}
		}
		return page;
	}
	
	/**set page layer can exchange by bing wang*/
	protected Layer[] parseLayers(JSONArray jsonLayers,int length){
		try{
			Layer[] layers = null;
			if(jsonLayers != null && length > 0){				
				layers = new Layer[length];
				for(int i=0; i<jsonLayers.length(); i++){
					JSONObject jsonLayer = jsonLayers.getJSONObject(i);
					Layer layer = parseLayer(jsonLayer);
					layers[i] = layer;
				}
			}
			return layers;
		} catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Layer parseLayer(String result) throws RssWebServiceException {
		checkError(result);
		Layer layer = null;
		try {
			JSONObject jsonLayer = new JSONObject(result);
			layer = parseLayer(jsonLayer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return layer;
	}
	
	protected Layer parseLayer(JSONObject jsonLayer) {
		Layer layer = null;
		try {
			layer = new Layer();
			if(jsonLayer.has(Layer.FLAG_TYPE)){
				layer.type = jsonLayer.getString(Layer.FLAG_TYPE);
			}
			if(jsonLayer.has(Layer.FLAG_LOCATION)){
				JSONObject jsonLocation = jsonLayer.getJSONObject(Layer.FLAG_LOCATION);
				ROI location = new ROI();
				if(jsonLocation.has(Layer.FLAG_LOCATION_X)){
					location.x = jsonLocation.getDouble(Layer.FLAG_LOCATION_X);
				}
				if(jsonLocation.has(Layer.FLAG_LOCATION_Y)){
					location.y = jsonLocation.getDouble(Layer.FLAG_LOCATION_Y);
				}
				if(jsonLocation.has(Layer.FLAG_LOCATION_W)){
					location.w = jsonLocation.getDouble(Layer.FLAG_LOCATION_W);
				}
				if(jsonLocation.has(Layer.FLAG_LOCATION_H)){
					location.h = jsonLocation.getDouble(Layer.FLAG_LOCATION_H);
				}
				if(jsonLocation.has(Layer.FLAG_LOCATION_CONTAINER_W)){
					location.ContainerW = jsonLocation.getDouble(Layer.FLAG_LOCATION_CONTAINER_W);
				}
				if(jsonLocation.has(Layer.FLAG_LOCATION_CONTAINER_H)){
					location.ContainerH = jsonLocation.getDouble(Layer.FLAG_LOCATION_CONTAINER_H);
				}
				layer.location = location;
			}
			if(jsonLayer.has(Layer.FLAG_ANGLE)){
				layer.angle = jsonLayer.getInt(Layer.FLAG_ANGLE);
			}
			if(jsonLayer.has(Layer.FLAG_PINNED)){
				layer.pinned = jsonLayer.getBoolean(Layer.FLAG_PINNED);
			}
			if(jsonLayer.has(Layer.FLAG_CONTENT_BASE_URI)){
				layer.contentBaseURI = jsonLayer.getString(Layer.FLAG_CONTENT_BASE_URI);
			}
			if(jsonLayer.has(Layer.FLAG_CONTENT_Id)){
				layer.contentId = jsonLayer.getString(Layer.FLAG_CONTENT_Id);
			}
			if(jsonLayer.has(Layer.FLAG_DATA)){
				JSONArray jsonData = jsonLayer.getJSONArray(Layer.FLAG_DATA);
				layer.data = parseData(jsonData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return layer;
	}
	
	protected Data[] parseData(JSONArray jsonArrData){
		try {
			Data[] arrData = null;
			if(jsonArrData != null){
				arrData = new Data[jsonArrData.length()];
				for(int i=0; i<jsonArrData.length(); i++){
					Data data = new Data();
					JSONObject jsonData = jsonArrData.getJSONObject(i);
					if(jsonData.has(Data.FLAG_NAME)){
						data.name = jsonData.getString(Data.FLAG_NAME);
					}
					if(jsonData.has(Data.FLAG_TYPE)){
						data.type = jsonData.getInt(Data.FLAG_TYPE);
					}
					if(jsonData.has(Data.FLAG_STRING_VAL)){
						data.valueType = Data.FLAG_STRING_VAL;
						if(jsonData.getJSONObject(Data.FLAG_STRING_VAL).has(Data.FLAG_VALUE)){
							data.value = jsonData.getJSONObject(Data.FLAG_STRING_VAL).getString(Data.FLAG_VALUE);
						}
					}
					if(jsonData.has(Data.FLAG_BOOL_VAL)){
						data.valueType = Data.FLAG_BOOL_VAL;
						if(jsonData.getJSONObject(Data.FLAG_BOOL_VAL).has(Data.FLAG_VALUE)){
							data.value = jsonData.getJSONObject(Data.FLAG_BOOL_VAL).getBoolean(Data.FLAG_VALUE);
						}
					}
					if(jsonData.has(Data.FLAG_DOUBLE_VAL)){
						data.valueType = Data.FLAG_DOUBLE_VAL;
						if(jsonData.getJSONObject(Data.FLAG_DOUBLE_VAL).has(Data.FLAG_VALUE)){
							data.value = jsonData.getJSONObject(Data.FLAG_DOUBLE_VAL).getDouble(Data.FLAG_VALUE);
						}
					}
					
					if(jsonData.has(Data.FLAG_INT_VAL)){
						data.valueType = Data.FLAG_INT_VAL;
						if(jsonData.getJSONObject(Data.FLAG_INT_VAL).has(Data.FLAG_VALUE)){
							data.value = jsonData.getJSONObject(Data.FLAG_INT_VAL).getInt(Data.FLAG_VALUE);
						}
					}
					
					if(jsonData.has(Data.FLAG_ROI_VAL)){
						data.valueType = Data.FLAG_ROI_VAL;
						JSONObject jsonRoi = jsonData.getJSONObject(Data.FLAG_ROI_VAL);
						ROI roi = new ROI();
						if(jsonRoi.has(Layer.FLAG_LOCATION_X)){
							roi.x = jsonRoi.getDouble(Layer.FLAG_LOCATION_X);
						}
						if(jsonRoi.has(Layer.FLAG_LOCATION_Y)){
							roi.y = jsonRoi.getDouble(Layer.FLAG_LOCATION_Y);
						}
						if(jsonRoi.has(Layer.FLAG_LOCATION_W)){
							roi.w = jsonRoi.getDouble(Layer.FLAG_LOCATION_W);
						}
						if(jsonRoi.has(Layer.FLAG_LOCATION_H)){
							roi.h = jsonRoi.getDouble(Layer.FLAG_LOCATION_H);
						}
						if(jsonRoi.has(Layer.FLAG_LOCATION_CONTAINER_W)){
							roi.ContainerW = jsonRoi.getDouble(Layer.FLAG_LOCATION_CONTAINER_W);
						}
						if(jsonRoi.has(Layer.FLAG_LOCATION_CONTAINER_H)){
							roi.ContainerH = jsonRoi.getDouble(Layer.FLAG_LOCATION_CONTAINER_H);
						}
						data.value = roi;
					}
					arrData[i] = data;
				}
			}
			return arrData;
		} catch(JSONException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public List<ServerPhoto> parseServerPhotos(String result) throws RssWebServiceException {
		List<ServerPhoto> photos = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			JSONArray jsPhotos = jsObj.optJSONArray(ServerPhoto.Photos);
			if(jsPhotos != null){
				photos = new ArrayList<ServerPhoto>();
				for(int i=0; i<jsPhotos.length(); i++){
					ServerPhoto photo = parseServerPhoto(jsPhotos.getJSONObject(i));
					photos.add(photo);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return photos;
	}
	
	private ServerPhoto parseServerPhoto(JSONObject jsPhoto){
		ServerPhoto serverPhoto = new ServerPhoto();
		serverPhoto.sourceImageBaskURI = jsPhoto.optString(ServerPhoto.SourceImageBaseURI);
		serverPhoto.sourceImageId = jsPhoto.optString(ServerPhoto.SourceImageId);
		return serverPhoto;
	}
	
	public NewOrder parseNewOrder(String result) throws RssWebServiceException{
		checkError(result);
		NewOrder newOrder = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(NewOrder.FLAG_NEW_ORDER)){
				newOrder = new NewOrder();
				JSONObject jsOrder = jsObj.getJSONObject(NewOrder.FLAG_NEW_ORDER);
				if(jsOrder.has(NewOrder.FLAG_ORDER_ID)){
					newOrder.orderId = jsOrder.getString(NewOrder.FLAG_ORDER_ID);
				}
				if(jsOrder.has(NewOrder.FLAG_CURRENCY)){
					newOrder.currency = jsOrder.getString(NewOrder.FLAG_CURRENCY);
				}
				if(jsOrder.has(NewOrder.FLAG_CURRENCY_SYMBOL)){
					newOrder.currencySymbol = jsOrder.getString(NewOrder.FLAG_CURRENCY_SYMBOL);
				}
				if(jsOrder.has(NewOrder.FLAG_SUB_TOTAL)){
					JSONObject jsSt = jsOrder.getJSONObject(NewOrder.FLAG_SUB_TOTAL);
					newOrder.subTotal = new UnitPrice();
					if(jsSt.has(UnitPrice.FLAG_PRICE)){
						newOrder.subTotal.price = jsSt.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsSt.has(UnitPrice.FLAG_PRICE_STR)){
						newOrder.subTotal.priceStr = jsSt.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if(jsOrder.has(NewOrder.FLAG_TAX)){
					JSONObject jsTax = jsOrder.getJSONObject(NewOrder.FLAG_TAX);
					newOrder.tax = new UnitPrice();
					if(jsTax.has(UnitPrice.FLAG_PRICE)){
						newOrder.tax.price = jsTax.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsTax.has(UnitPrice.FLAG_PRICE_STR)){
						newOrder.tax.priceStr = jsTax.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if(jsOrder.has(NewOrder.FLAG_GRAND_TOTAL)){
					JSONObject jsGt = jsOrder.getJSONObject(NewOrder.FLAG_GRAND_TOTAL);
					newOrder.grandTotal = new UnitPrice();
					if(jsGt.has(UnitPrice.FLAG_PRICE)){
						newOrder.grandTotal.price = jsGt.getDouble(UnitPrice.FLAG_PRICE);
					}
					if(jsGt.has(UnitPrice.FLAG_PRICE_STR)){
						newOrder.grandTotal.priceStr = jsGt.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if(jsOrder.has(NewOrder.FLAG_TAX_BE_CAL_BY_RETAILER)){
					newOrder.taxWillBeCalculatedByRetailer = jsOrder.getBoolean(NewOrder.FLAG_TAX_BE_CAL_BY_RETAILER);
				}
				if(jsOrder.has(NewOrder.FLAG_TAXES_ARE_ESTIMATED)){
					newOrder.taxesAreEstimated = jsOrder.getBoolean(NewOrder.FLAG_TAXES_ARE_ESTIMATED);
				}
				if(jsOrder.has(NewOrder.FLAG_INITIATED)){
					newOrder.initiated = jsOrder.getBoolean(NewOrder.FLAG_INITIATED);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return newOrder;
	}
	
	public Boolean parseStoreAvailability(String result) throws RssWebServiceException{
		checkError(result);
		Boolean available = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has("StoreAvailability")){
				JSONObject jsObj2 = jsObj.getJSONObject("StoreAvailability");
				if(jsObj2.has("Available")){
					available = jsObj2.getBoolean("Available");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return available;
	}
	
	public ROI parseROI(String result) throws RssWebServiceException{
		checkError(result);
		ROI roi = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has("ROI")){
				JSONObject jsonRoi = jsObj.getJSONObject("ROI");
				roi = new ROI();
				if(jsonRoi.has(Layer.FLAG_LOCATION_X)){
					roi.x = jsonRoi.getDouble(Layer.FLAG_LOCATION_X);
				}
				if(jsonRoi.has(Layer.FLAG_LOCATION_Y)){
					roi.y = jsonRoi.getDouble(Layer.FLAG_LOCATION_Y);
				}
				if(jsonRoi.has(Layer.FLAG_LOCATION_W)){
					roi.w = jsonRoi.getDouble(Layer.FLAG_LOCATION_W);
				}
				if(jsonRoi.has(Layer.FLAG_LOCATION_H)){
					roi.h = jsonRoi.getDouble(Layer.FLAG_LOCATION_H);
				}
				if(jsonRoi.has(Layer.FLAG_LOCATION_CONTAINER_W)){
					roi.ContainerW = jsonRoi.getDouble(Layer.FLAG_LOCATION_CONTAINER_W);
				}
				if(jsonRoi.has(Layer.FLAG_LOCATION_CONTAINER_H)){
					roi.ContainerH = jsonRoi.getDouble(Layer.FLAG_LOCATION_CONTAINER_H);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return roi;
	}
	
	public ProjectSearchResult parseProjectsSearchResult(String result) throws RssWebServiceException{
		checkError(result);
		ProjectSearchResult projectsResult = null;
		try {
			projectsResult = new ProjectSearchResult();
			JSONObject jsResults = null;
			JSONArray jsProjects = null;
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(Project.SearchResults)){
				jsResults = jsObj.getJSONObject(Project.SearchResults);
				projectsResult.totalMatchingProjects = jsResults.optInt(ProjectSearchResult.TotalMatchingProjects);
				projectsResult.numberReturned = jsResults.optInt(ProjectSearchResult.NumberReturned);
				projectsResult.startingIndex = jsResults.optInt(ProjectSearchResult.StartingIndex);
				if(jsResults.has(Project.Projects)){
					jsProjects = jsResults.getJSONArray(Project.Projects);
					if(jsProjects != null){
						projectsResult.projects = new ArrayList<Project>();
						for(int i=0; i<jsProjects.length(); i++){
							JSONObject jsPro = jsProjects.getJSONObject(i);
							Project project = parseProject(jsPro);
							projectsResult.projects.add(project);
						}
					}
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return projectsResult;
	}
	
	public Project parseProject(String result) throws RssWebServiceException{
		checkError(result);
		Project project = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(Project.Project)){
				JSONObject jsProject = jsObj.getJSONObject(Project.Project);
				project = parseProject(jsProject);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return project;
	}
	
	private Project parseProject(JSONObject jsPro) throws JSONException{
		Project project = new Project();
		if(jsPro.has(Project.Id)){
			project.id = jsPro.getString(Project.Id);
		}
		if(jsPro.has(Project.Type)){
			project.type = jsPro.getString(Project.Type);
		}
		if(jsPro.has(Project.ProductDescriptionId)){
			project.productDescriptionId = jsPro.getString(Project.ProductDescriptionId);
		}
		if(jsPro.has(Project.ProductDescriptionIdLocalized)){
			project.productDescriptionIdLocalized = jsPro.getString(Project.ProductDescriptionIdLocalized);
		}
		if(jsPro.has(Project.ProjectName)){
			project.projectName = jsPro.getString(Project.ProjectName);
		}
		if(jsPro.has(Project.GlyphURL)){
			project.glyphURL = jsPro.getString(Project.GlyphURL);
		}
		if(jsPro.has(Project.ApplicationName)){
			project.applicationName = jsPro.getString(Project.ApplicationName);
		}
		if(jsPro.has(Project.CreationDate)){
			project.creationDate = jsPro.getString(Project.CreationDate);
		}
		if(jsPro.has(Project.ModifiedDate)){
			project.modifiedDate = jsPro.getString(Project.ModifiedDate);
		}
		if(jsPro.has(Project.ExpirationDate)){
			project.expirationDate = jsPro.getString(Project.ExpirationDate);
		}
		if(jsPro.has(Project.Public)){
			project.isPublic = jsPro.getBoolean(Project.Public);
		}
		return project;
	}
	
	public void checkError(String result) throws RssWebServiceException{
		final String ERROR = "Error";
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(ERROR)){
				String jsResult = jsObj.getString(ERROR);
				if(jsResult.equals("null")){
					return;
				} else {
					JSONObject jsError = new JSONObject(jsResult);
					throw RssWebServiceException.server(jsError.getString("ErrorCode"), jsError.toString());
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw RssWebServiceException.server("", result);
		}
	}
}
