package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.kodak.kodak_kioskconnect_n2r.BuildConfig;
import com.kodak.kodak_kioskconnect_n2r.ImageInfo;
import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardCatalogDataEntry.GreetingCardProductDescription;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardCatalogDataEntry.GreetingCardProductDescription.Attributes;

public class GreetingCardParser {
	private static String TAG = GreetingCardParser.class.getSimpleName();

	public GreetingCardTheme[] parseGreetingCardThemes(String result){
		GreetingCardTheme[] themes = null;
		try{
			JSONObject object = new JSONObject(result);
			JSONArray array = object.getJSONArray(GreetingCardTheme.CONTENT_SEARCH_STARTER_COLLECTION);
			if(array!=null){
				themes = new GreetingCardTheme[array.length()];
				for(int i=0; i<array.length(); i++){
					GreetingCardTheme theme = new GreetingCardTheme();
					JSONObject item = array.getJSONObject(i);
					if(item.has(GreetingCardTheme.NAME)){
						theme.name = item.getString(GreetingCardTheme.NAME);
					}
					if(item.has(GreetingCardTheme.GLYPH_URL)){
						theme.glyphURL = item.getString(GreetingCardTheme.GLYPH_URL);
					}
					if(item.has(GreetingCardTheme.SHORT_DESC_TEXT_FILE)){
						theme.shortDescTextFile = item.getString(GreetingCardTheme.SHORT_DESC_TEXT_FILE);
					}
					if(item.has(GreetingCardTheme.LONG_DESC_TEXT_FILE)){
						theme.longDescTextFile = item.getString(GreetingCardTheme.LONG_DESC_TEXT_FILE);
					}
					if(item.has(GreetingCardTheme.FILTERS)){
						theme.filters = item.getString(GreetingCardTheme.FILTERS);
					}
					if(item.has(GreetingCardTheme.LANGUAGE)){
						theme.language = item.getString(GreetingCardTheme.LANGUAGE);
					}
					/*if(BuildConfig.DEBUG){
						Log.e(TAG, theme.toString());
					}*/
					themes[i] = theme;
				}
			}
		} catch(JSONException je){
			je.printStackTrace();
		}
		if(themes == null){
			themes = new GreetingCardTheme[0];
		}
		if(themes.length == 0){
			Log.e(TAG, "no themes found");
		}
		return themes;
	}
	
	public GreetingCard[] parseGreetingCards(String language, String result){
		GreetingCard[] cards = null;
		try {
			JSONObject object = new JSONObject(result);
			JSONObject contentResults = object.getJSONObject(GreetingCard.CONTENT_RESULTS);
			JSONArray contents = contentResults.getJSONArray(GreetingCard.CONTENTS);
			if(contents != null){
				cards = new GreetingCard[contents.length()];
				for(int i=0; i<contents.length(); i++){
					GreetingCard card = new GreetingCard(language);
					JSONObject obj = contents.getJSONObject(i);
					if(obj.has(GreetingCard.ID)){
						card.id = obj.getString(GreetingCard.ID);
					}
					if(obj.has(GreetingCard.LOCALIZED_NAME)){
						card.localizedName = obj.getString(GreetingCard.LOCALIZED_NAME);
					}
					if(obj.has(GreetingCard.USAGE)){
						card.usage = obj.getString(GreetingCard.USAGE);
					}
					if(obj.has(GreetingCard.CLASS)){
						card.card_class = obj.getString(GreetingCard.CLASS);
					}
					if(obj.has(GreetingCard.VENDOR)){
						card.vendor = obj.getString(GreetingCard.VENDOR);
					}
					if(obj.has(GreetingCard.GLYPH_URL)){
						card.glyphURL = obj.getString(GreetingCard.GLYPH_URL);
					}
					if(obj.has(GreetingCard.NUM_ASSET_GROUPS)){
						card.numAssetGroups = obj.getInt(GreetingCard.NUM_ASSET_GROUPS);
					}
					if(obj.has(GreetingCard.BEARS_ROYALTY)){
						card.bearsRoyalty = obj.getBoolean(GreetingCard.BEARS_ROYALTY);
					}
					if(obj.has(GreetingCard.WIDTH)){
						card.width = Float.parseFloat(obj.getString(GreetingCard.WIDTH));
					}
					if(obj.has(GreetingCard.HEIGHT)){
						card.height = Float.parseFloat(obj.getString(GreetingCard.HEIGHT));
					}
					if(obj.has(GreetingCard.PRODUCT_IDENTIFIERS)){
						JSONArray arr = obj.getJSONArray(GreetingCard.PRODUCT_IDENTIFIERS);
						if(arr!=null){
							String[] indentifiers = new String[arr.length()];
							for(int j=0; j<arr.length(); j++){
								indentifiers[j] = arr.get(j).toString();
							}
							card.productIdentifiers = indentifiers;
						}
					}
					//Log.e(TAG, card.toString());
					cards[i] = card;					
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(cards==null){
			cards = new GreetingCard[0];
			Log.e(TAG, "no cards found");
		}
		return cards;
	}
	
	public GreetingCardCatalogData[] parseGreetingCardCatalogData(String result){
		GreetingCardCatalogData[] data = null;
		try {
			JSONObject jsonDescription = new JSONObject(result);
			JSONArray jsonCatalog = jsonDescription.getJSONArray(GreetingCardCatalogData.CATALOGS);
			if(jsonCatalog!=null){
				data = new GreetingCardCatalogData[jsonCatalog.length()];
				for(int i=0; i<jsonCatalog.length(); i++){
					GreetingCardCatalogData _data = new GreetingCardCatalogData();
					JSONObject obj = jsonCatalog.getJSONObject(i);
					if(obj.has(GreetingCardCatalogData.CURRENCY)){
						_data.currency = obj.getString(GreetingCardCatalogData.CURRENCY);
					}
					if(obj.has(GreetingCardCatalogData.CURRENCY_SYMBOL)){
						_data.currencySymbol = obj.getString(GreetingCardCatalogData.CURRENCY_SYMBOL);
					}
					if(obj.has(GreetingCardCatalogData.PRICES_INCLUDE_TAX)){
						_data.pricesIncludeTax = obj.getBoolean(GreetingCardCatalogData.PRICES_INCLUDE_TAX);
					}
					if(obj.has(GreetingCardCatalogData.ENTRIES)){
						_data.entries = parseGreetingCardCatalogDataEntries(obj.getJSONArray(GreetingCardCatalogData.ENTRIES));
					}
					data[i] = _data;
				}
			}
		} catch(JSONException je ){
			je.printStackTrace();
		}
		if(data==null){
			data = new GreetingCardCatalogData[0];
		}
		if(data.length == 0){
			Log.e(TAG, "no GreetingCardCatalogData found.");
		}
		return data;
	}
	
	private GreetingCardCatalogDataEntry[] parseGreetingCardCatalogDataEntries(JSONArray jsonEntries){
		GreetingCardCatalogDataEntry[] enties = null;
		try {
			if(jsonEntries != null){
				enties = new GreetingCardCatalogDataEntry[jsonEntries.length()];
				for(int i=0; i<jsonEntries.length(); i++){
					GreetingCardCatalogDataEntry entry = new GreetingCardCatalogDataEntry();
					JSONObject obj = jsonEntries.getJSONObject(i);
					if(obj.has(GreetingCardCatalogDataEntry.PRODUCT_DESCRIPTION)){
						entry.description = parseGreetingCardProductDescription(obj.getJSONObject(GreetingCardCatalogDataEntry.PRODUCT_DESCRIPTION));
					}
					if(obj.has(GreetingCardCatalogDataEntry.MAX_UNIT_PRICE)){
						JSONObject jsonMax = obj.getJSONObject(GreetingCardCatalogDataEntry.MAX_UNIT_PRICE);
						if(jsonMax.has(GreetingCardCatalogDataEntry.PRICE)){
							entry.maxUnitPrice.price = jsonMax.getDouble(GreetingCardCatalogDataEntry.PRICE);
						}
						if(jsonMax.has(GreetingCardCatalogDataEntry.PRICE_STR)){
							entry.maxUnitPrice.priceStr = jsonMax.getString(GreetingCardCatalogDataEntry.PRICE_STR);
						}
					}
					if(obj.has(GreetingCardCatalogDataEntry.MIN_UNIT_PRICE)){
						JSONObject jsonMax = obj.getJSONObject(GreetingCardCatalogDataEntry.MIN_UNIT_PRICE);
						if(jsonMax.has(GreetingCardCatalogDataEntry.PRICE)){
							entry.minUnitPrice.price = jsonMax.getDouble(GreetingCardCatalogDataEntry.PRICE);
						}
						if(jsonMax.has(GreetingCardCatalogDataEntry.PRICE_STR)){
							entry.minUnitPrice.priceStr = jsonMax.getString(GreetingCardCatalogDataEntry.PRICE_STR);
						}
					}
					enties[i] = entry;
				}
			}
		} catch(JSONException je){
			je.printStackTrace();
		}
		if(enties==null){
			enties = new GreetingCardCatalogDataEntry[0];
		}
		if(enties.length == 0){
			Log.e(TAG, "no GreetingCardCatalogDataEntry found.");
		}
		return enties;
	}
	
	private GreetingCardProductDescription parseGreetingCardProductDescription(JSONObject jsonDesc){
		GreetingCardProductDescription desc = null;
		try{
			if(jsonDesc!=null){
				desc = new GreetingCardCatalogDataEntry().description;
				if(jsonDesc.has(GreetingCardCatalogDataEntry.PRO_BASE_URI)){
					desc.baseUri = jsonDesc.getString(GreetingCardCatalogDataEntry.PRO_BASE_URI);
				}
				if(jsonDesc.has(GreetingCardCatalogDataEntry.PRO_ID)){
					desc.id = jsonDesc.getString(GreetingCardCatalogDataEntry.PRO_ID);
				}
				if(jsonDesc.has(GreetingCardCatalogDataEntry.PRO_NAME)){
					desc.name = jsonDesc.getString(GreetingCardCatalogDataEntry.PRO_NAME);
				}
				if(jsonDesc.has(GreetingCardCatalogDataEntry.PRO_SHORT_NAME)){
					desc.shortName = jsonDesc.getString(GreetingCardCatalogDataEntry.PRO_SHORT_NAME);
				}
				if(jsonDesc.has(GreetingCardCatalogDataEntry.PRO_TYPE)){
					desc.type = jsonDesc.getString(GreetingCardCatalogDataEntry.PRO_TYPE);
				}
				if(jsonDesc.has(GreetingCardCatalogDataEntry.PRO_PAGE_WIDTH)){
					desc.pageWidth = jsonDesc.getDouble(GreetingCardCatalogDataEntry.PRO_PAGE_WIDTH);
				}
				if(jsonDesc.has(GreetingCardCatalogDataEntry.PRO_PAGE_HEIGHT)){
					desc.pageHeight = jsonDesc.getDouble(GreetingCardCatalogDataEntry.PRO_PAGE_HEIGHT);
				}
				if(jsonDesc.has(GreetingCardCatalogDataEntry.PRO_LG_GLYPH_URL)){
					desc.lgGlyphURL = jsonDesc.getString(GreetingCardCatalogDataEntry.PRO_LG_GLYPH_URL);
				}
				if(jsonDesc.has(GreetingCardCatalogDataEntry.PRO_SM_GLYPH_URL)){
					desc.smGlyphURL = jsonDesc.getString(GreetingCardCatalogDataEntry.PRO_SM_GLYPH_URL);
				}
				if(jsonDesc.has(GreetingCardCatalogDataEntry.PRO_ATTRIBUTES)){
					JSONArray jsonAttr = jsonDesc.getJSONArray(GreetingCardCatalogDataEntry.PRO_ATTRIBUTES);
					if(jsonAttr!=null){
						desc.attributes = new GreetingCardCatalogDataEntry.GreetingCardProductDescription.Attributes[jsonAttr.length()];
						for(int i=0; i<jsonAttr.length(); i++){
							GreetingCardCatalogDataEntry.GreetingCardProductDescription.Attributes attr = new GreetingCardCatalogDataEntry().new GreetingCardProductDescription().new Attributes();
							JSONObject jsonObj = jsonAttr.getJSONObject(i);
							if(jsonObj.has(GreetingCardCatalogDataEntry.PRO_ATTR_NAME)){
								attr.name = jsonObj.getString(GreetingCardCatalogDataEntry.PRO_ATTR_NAME);
							}
							if(jsonObj.has(GreetingCardCatalogDataEntry.PRO_ATTR_VALUE)){
								attr.value = jsonObj.getString(GreetingCardCatalogDataEntry.PRO_ATTR_VALUE);
							}
							desc.attributes[i] = attr;
						}
					}
				}
				
			}
		} catch(JSONException je){
			je.printStackTrace();
		}
		return desc;
	}
	
	public GreetingCardProduct parseGreetingCardProduct(String result){
		try {
			GreetingCardProduct detail = new GreetingCardProduct();
			JSONObject object = new JSONObject(result);
			JSONObject jsonDetail = object.getJSONObject(GreetingCardProduct.GREETING_CARD);
			if(jsonDetail.has(GreetingCardProduct.ID)){
				detail.id = jsonDetail.getString(GreetingCardProduct.ID);
			}
			if(jsonDetail.has(GreetingCardProduct.PRODUCT_DESC_BASE_URI)){
				detail.productDescriptionBaseURI = jsonDetail.getString(GreetingCardProduct.PRODUCT_DESC_BASE_URI);
			}
			if(jsonDetail.has(GreetingCardProduct.PRODUCT_DESC_ID)){
				detail.productDescriptionId = jsonDetail.getString(GreetingCardProduct.PRODUCT_DESC_ID);
			}
			if(jsonDetail.has(GreetingCardProduct.THEME)){
				detail.theme = jsonDetail.getString(GreetingCardProduct.THEME);
			}
			if(jsonDetail.has(GreetingCardProduct.PAGES)){
				detail.pages = parseGreetingCardPages(jsonDetail.getJSONArray(GreetingCardProduct.PAGES));
			}
			if(jsonDetail.has(GreetingCardProduct.IS_DUPLEX)){
				detail.isDuplex = jsonDetail.getBoolean(GreetingCardProduct.IS_DUPLEX);
			}
			if(jsonDetail.has(GreetingCardProduct.MIN_NUM_OF_PAGES)){
				detail.minNumberOfPages = jsonDetail.getInt(GreetingCardProduct.MIN_NUM_OF_PAGES);
			}
			if(jsonDetail.has(GreetingCardProduct.MAX_NUM_OF_PAGES)){
				detail.maxNumberOfPages = jsonDetail.getInt(GreetingCardProduct.MAX_NUM_OF_PAGES);
			}
			if(jsonDetail.has(GreetingCardProduct.NUM_OF_PAGES_PER_BASE_CARD)){
				detail.numberOfPagesPerBaseCard = jsonDetail.getInt(GreetingCardProduct.NUM_OF_PAGES_PER_BASE_CARD);
			}
			if(jsonDetail.has(GreetingCardProduct.MIN_NUM_OF_IMAGES)){
				detail.minNumberOfImages = jsonDetail.getInt(GreetingCardProduct.MIN_NUM_OF_IMAGES);
			}
			if(jsonDetail.has(GreetingCardProduct.MAX_NUM_OF_IMAGES)){
				detail.maxNumberOfImages = jsonDetail.getInt(GreetingCardProduct.MAX_NUM_OF_IMAGES);
			}
			if(jsonDetail.has(GreetingCardProduct.MAX_NUM_OF_IMAGES_PER_ADDED_PAGE)){
				// TODO: return object right now don't know what the value will be returned
				detail.maxNumberOfImagesPerAddedPage = jsonDetail.getString(GreetingCardProduct.MAX_NUM_OF_IMAGES_PER_ADDED_PAGE).toString().equals("null")?null:jsonDetail.getString(GreetingCardProduct.MAX_NUM_OF_IMAGES_PER_ADDED_PAGE);
			}
			if(jsonDetail.has(GreetingCardProduct.MAX_NUM_OF_IMAGES_PER_BASE_CARD)){
				detail.maxNumberOfImagesPerBaseCard = jsonDetail.getInt(GreetingCardProduct.MAX_NUM_OF_IMAGES_PER_BASE_CARD);
			}
			if(jsonDetail.has(GreetingCardProduct.IDEAL_NUM_OF_IMAGES_PER_BASE_CARD)){
				detail.idealNumberOfImagesPerBaseCard = jsonDetail.getInt(GreetingCardProduct.IDEAL_NUM_OF_IMAGES_PER_BASE_CARD);
			}
			if(jsonDetail.has(GreetingCardProduct.NUM_OF_IMAGES_IN_CARD)){
				detail.numberOfImagesInCard = jsonDetail.getInt(GreetingCardProduct.NUM_OF_IMAGES_IN_CARD);
			}
			if(jsonDetail.has(GreetingCardProduct.NUM_OF_UNASSIGNED_IMAGES)){
				// TODO: return object right now don't know what the value will be returned
				detail.numberOfUnassignedImages = jsonDetail.getString(GreetingCardProduct.NUM_OF_UNASSIGNED_IMAGES).toString().equals("null")?null:jsonDetail.getString(GreetingCardProduct.NUM_OF_UNASSIGNED_IMAGES);
			}
			if(jsonDetail.has(GreetingCardProduct.SUGGESTED_CAPTION_VISIBILITY)){
				detail.suggestedCaptionVisibility = jsonDetail.getBoolean(GreetingCardProduct.SUGGESTED_CAPTION_VISIBILITY);
			}
			if(jsonDetail.has(GreetingCardProduct.CAN_SET_TITLE)){
				detail.canSetTitle = jsonDetail.getBoolean(GreetingCardProduct.CAN_SET_TITLE);
			}
			if(jsonDetail.has(GreetingCardProduct.CAN_SET_SUBTITLE)){
				detail.canSetSubtitle = jsonDetail.getBoolean(GreetingCardProduct.CAN_SET_SUBTITLE);
			}
			if(jsonDetail.has(GreetingCardProduct.CAN_SET_AUTHOR)){
				detail.canSetAuthor = jsonDetail.getBoolean(GreetingCardProduct.CAN_SET_AUTHOR);
			}
			for(GreetingCardPage page:detail.pages){
				int index = 0;
				for(GreetingCardPageLayer layer: page.layers){
					layer.holeIndex = index;
					index ++;
				}
			}
			return detail;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	private GreetingCardPage[] parseGreetingCardPages(JSONArray jsonPages){
		try {
			GreetingCardPage[] pages = null;
			if(jsonPages!=null){
				pages = new GreetingCardPage[jsonPages.length()];
				for(int i=0; i<jsonPages.length(); i++){
					JSONObject jsonPage = jsonPages.getJSONObject(i);
					GreetingCardPage page = parseGreetingCardPage(jsonPage);	
					pages[i] = page;
				}
			}
			return pages;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public GreetingCardPage parseGreetingCardPage(String result){
		try {
			JSONObject jsonObj = new JSONObject(result);
			JSONObject needParse = null;
			if(jsonObj.has(GreetingCardProduct.PAGE)){
				needParse = jsonObj.getJSONObject(GreetingCardProduct.PAGE);
			} else {
				needParse = jsonObj;
			}
			return parseGreetingCardPage(needParse);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private GreetingCardPage parseGreetingCardPage(JSONObject jsonPage){
		GreetingCardPage page = null;
		if(jsonPage!=null){
			try {
				page = new GreetingCardPage();
				if(jsonPage.has(GreetingCardPage.BASE_URI)){
					page.baseURI = jsonPage.getString(GreetingCardPage.BASE_URI);
				}
				if(jsonPage.has(GreetingCardPage.ID)){
					page.id = jsonPage.getString(GreetingCardPage.ID);
				}
				if(jsonPage.has(GreetingCardPage.SEQUENCE_NUMBER)){
					page.sequenceNumber = jsonPage.getInt(GreetingCardPage.SEQUENCE_NUMBER);
				}
				if(jsonPage.has(GreetingCardPage.PAGE_TYPE)){
					page.pageType = jsonPage.getString(GreetingCardPage.PAGE_TYPE);
				}
				if(jsonPage.has(GreetingCardPage.LAYOUT_TYPE)){
					page.layoutType = jsonPage.getString(GreetingCardPage.LAYOUT_TYPE);
				}
				if(jsonPage.has(GreetingCardPage.WIDTH)){
					page.width = (float) jsonPage.getDouble(GreetingCardPage.WIDTH);
				}
				if(jsonPage.has(GreetingCardPage.HEIGHT)){
					page.height = (float) jsonPage.getDouble(GreetingCardPage.HEIGHT);
				}
				if(jsonPage.has(GreetingCardPage.MIN_NUM_OF_IMAGES)){
					page.minNumberOfImages = jsonPage.getInt(GreetingCardPage.MIN_NUM_OF_IMAGES);
				}
				if(jsonPage.has(GreetingCardPage.MAX_NUM_OF_IMAGES)){
					page.maxNumberOfImages = jsonPage.getInt(GreetingCardPage.MAX_NUM_OF_IMAGES);
				}
				if(jsonPage.has(GreetingCardPage.LAYERS)){
					page.layers = parseGreetingCardLayer(jsonPage.getJSONArray(GreetingCardPage.LAYERS));
				}
				if(jsonPage.has(GreetingCardPage.MARGIN)){
					JSONObject jsonMargin = jsonPage.getJSONObject(GreetingCardPage.MARGIN);
					if(jsonMargin.has(GreetingCardPage.MARGIN_TOP)){
						page.margin[0] = (float) jsonMargin.getDouble(GreetingCardPage.MARGIN_TOP);
					}
					if(jsonMargin.has(GreetingCardPage.MARGIN_LEFT)){
						page.margin[1] = (float) jsonMargin.getDouble(GreetingCardPage.MARGIN_TOP);
					}
					if(jsonMargin.has(GreetingCardPage.MARGIN_BOTTOM)){
						page.margin[2] = (float) jsonMargin.getDouble(GreetingCardPage.MARGIN_TOP);
					}
					if(jsonMargin.has(GreetingCardPage.MARGIN_RIGHT)){
						page.margin[3] = (float) jsonMargin.getDouble(GreetingCardPage.MARGIN_TOP);
					}
				}
			} catch(JSONException je){
				je.printStackTrace();
			}
		}
		return page;
	}
	
	private GreetingCardPageLayer[] parseGreetingCardLayer(JSONArray jsonLayers){
		try{
			GreetingCardPageLayer[] layers = null;
			if(jsonLayers != null){
				layers = new GreetingCardPageLayer[jsonLayers.length()];
				for(int i=0; i<jsonLayers.length(); i++){
					JSONObject jsonLayer = jsonLayers.getJSONObject(i);
					GreetingCardPageLayer layer = new GreetingCardPageLayer();
					if(jsonLayer.has(GreetingCardPageLayer.TYPE)){
						layer.type = jsonLayer.getString(GreetingCardPageLayer.TYPE);
					}
					if(jsonLayer.has(GreetingCardPageLayer.LOCATION)){
						JSONObject jsonLocation = jsonLayer.getJSONObject(GreetingCardPageLayer.LOCATION);
						ROI location = new ROI();
						if(jsonLocation.has(GreetingCardPageLayer.LOCATION_X)){
							location.x = jsonLocation.getDouble(GreetingCardPageLayer.LOCATION_X);
						}
						if(jsonLocation.has(GreetingCardPageLayer.LOCATION_Y)){
							location.y = jsonLocation.getDouble(GreetingCardPageLayer.LOCATION_Y);
						}
						if(jsonLocation.has(GreetingCardPageLayer.LOCATION_W)){
							location.w = jsonLocation.getDouble(GreetingCardPageLayer.LOCATION_W);
						}
						if(jsonLocation.has(GreetingCardPageLayer.LOCATION_H)){
							location.h = jsonLocation.getDouble(GreetingCardPageLayer.LOCATION_H);
						}
						if(jsonLocation.has(GreetingCardPageLayer.LOCATION_CONTAINER_W)){
							location.ContainerW = jsonLocation.getDouble(GreetingCardPageLayer.LOCATION_CONTAINER_W);
						}
						if(jsonLocation.has(GreetingCardPageLayer.LOCATION_CONTAINER_H)){
							location.ContainerH = jsonLocation.getDouble(GreetingCardPageLayer.LOCATION_CONTAINER_H);
						}
						layer.location = location;
					}
					if(jsonLayer.has(GreetingCardPageLayer.ANGLE)){
						layer.angle = jsonLayer.getInt(GreetingCardPageLayer.ANGLE);
					}
					if(jsonLayer.has(GreetingCardPageLayer.PINNED)){
						layer.pinned = jsonLayer.getBoolean(GreetingCardPageLayer.PINNED);
					}
					if(jsonLayer.has(GreetingCardPageLayer.CONTENT_BASE_URI)){
						layer.contentBaseURI = jsonLayer.getString(GreetingCardPageLayer.CONTENT_BASE_URI);
					}
					if(jsonLayer.has(GreetingCardPageLayer.CONTENT_Id)){
						layer.contentId = jsonLayer.getString(GreetingCardPageLayer.CONTENT_Id);
					}
					if(jsonLayer.has(GreetingCardPageLayer.DATA)){
						JSONArray jsonData = jsonLayer.getJSONArray(GreetingCardPageLayer.DATA);
						layer.data = parseGreetingCardLayerData(jsonData);
					}
					layers[i] = layer;
				}
			}
			return layers;
		} catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private GreetingCardPageLayerData[] parseGreetingCardLayerData(JSONArray jsonLayerData){
		try {
			GreetingCardPageLayerData[] layerData = null;
			if(jsonLayerData != null){
				layerData = new GreetingCardPageLayerData[jsonLayerData.length()];
				for(int i=0; i<jsonLayerData.length(); i++){
					GreetingCardPageLayerData data = new GreetingCardPageLayerData();
					JSONObject jsonData = jsonLayerData.getJSONObject(i);
					if(jsonData.has(GreetingCardPageLayerData.NAME)){
						data.name = jsonData.getString(GreetingCardPageLayerData.NAME);
					}
					if(jsonData.has(GreetingCardPageLayerData.TYPE)){
						data.type = jsonData.getInt(GreetingCardPageLayerData.TYPE);
					}
					if(jsonData.has(GreetingCardPageLayerData.STRING_VAL)){
						data.valueType = GreetingCardPageLayerData.STRING_VAL;
						if(jsonData.getJSONObject(GreetingCardPageLayerData.STRING_VAL).has(GreetingCardPageLayerData.VALUE)){
							data.value = jsonData.getJSONObject(GreetingCardPageLayerData.STRING_VAL).getString(GreetingCardPageLayerData.VALUE);
						}
					}
					if(jsonData.has(GreetingCardPageLayerData.BOOL_VAL)){
						data.valueType = GreetingCardPageLayerData.BOOL_VAL;
						if(jsonData.getJSONObject(GreetingCardPageLayerData.BOOL_VAL).has(GreetingCardPageLayerData.VALUE)){
							data.value = jsonData.getJSONObject(GreetingCardPageLayerData.BOOL_VAL).getBoolean(GreetingCardPageLayerData.VALUE);
						}
					}
					if(jsonData.has(GreetingCardPageLayerData.DOUBLE_VAL)){
						data.valueType = GreetingCardPageLayerData.DOUBLE_VAL;
						if(jsonData.getJSONObject(GreetingCardPageLayerData.DOUBLE_VAL).has(GreetingCardPageLayerData.VALUE)){
							data.value = jsonData.getJSONObject(GreetingCardPageLayerData.DOUBLE_VAL).getDouble(GreetingCardPageLayerData.VALUE);
						}
					}
					if(jsonData.has(GreetingCardPageLayerData.ROI_VAL)){
						data.valueType = GreetingCardPageLayerData.ROI_VAL;
						JSONObject jsonRoi = jsonData.getJSONObject(GreetingCardPageLayerData.ROI_VAL);
						ROI roi = new ROI();
						if(jsonRoi.has(GreetingCardPageLayer.LOCATION_X)){
							roi.x = jsonRoi.getDouble(GreetingCardPageLayer.LOCATION_X);
						}
						if(jsonRoi.has(GreetingCardPageLayer.LOCATION_Y)){
							roi.y = jsonRoi.getDouble(GreetingCardPageLayer.LOCATION_Y);
						}
						if(jsonRoi.has(GreetingCardPageLayer.LOCATION_W)){
							roi.w = jsonRoi.getDouble(GreetingCardPageLayer.LOCATION_W);
						}
						if(jsonRoi.has(GreetingCardPageLayer.LOCATION_H)){
							roi.h = jsonRoi.getDouble(GreetingCardPageLayer.LOCATION_H);
						}
						if(jsonRoi.has(GreetingCardPageLayer.LOCATION_CONTAINER_W)){
							roi.ContainerW = jsonRoi.getDouble(GreetingCardPageLayer.LOCATION_CONTAINER_W);
						}
						if(jsonRoi.has(GreetingCardPageLayer.LOCATION_CONTAINER_H)){
							roi.ContainerH = jsonRoi.getDouble(GreetingCardPageLayer.LOCATION_CONTAINER_H);
						}
						data.value = roi;
					}
					layerData[i] = data;
				}
			}
			return layerData;
		} catch(JSONException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public ImageInfo parseImageInfo(String result){
		ImageInfo imageInfo = null;
		try {
			JSONObject jsonObj = new JSONObject(result);
			if(jsonObj.has(ImageInfo.IMAGE_INFO)){
				imageInfo = new ImageInfo();
				JSONObject jsonImgInfo = jsonObj.getJSONObject(ImageInfo.IMAGE_INFO);
				if(jsonImgInfo.has(ImageInfo.BASE_URI)){
					imageInfo.baseURI = jsonImgInfo.getString(ImageInfo.BASE_URI);
				}
				if(jsonImgInfo.has(ImageInfo.ID)){
					imageInfo.id = jsonImgInfo.getString(ImageInfo.ID);
				}
				if(jsonImgInfo.has(ImageInfo.WIDTH)){
					imageInfo.width = jsonImgInfo.getInt(ImageInfo.WIDTH);
				}
				if(jsonImgInfo.has(ImageInfo.HEIGHT)){
					imageInfo.height = jsonImgInfo.getInt(ImageInfo.HEIGHT);
				}
				if(jsonImgInfo.has(ImageInfo.ANGLE)){
					imageInfo.angle = jsonImgInfo.getInt(ImageInfo.ANGLE);
				}
				if(jsonImgInfo.has(ImageInfo.CROP)){
					ROI crop = new ROI();
					JSONObject jsonCrop = jsonImgInfo.getJSONObject(ImageInfo.CROP);
					if(jsonCrop.has(ImageInfo.CROP_X)){
						crop.x = jsonCrop.getDouble(ImageInfo.CROP_X);
					}
					if(jsonCrop.has(ImageInfo.CROP_Y)){
						crop.y = jsonCrop.getDouble(ImageInfo.CROP_Y);
					}
					if(jsonCrop.has(ImageInfo.CROP_W)){
						crop.w = jsonCrop.getDouble(ImageInfo.CROP_W);
					}
					if(jsonCrop.has(ImageInfo.CROP_H)){
						crop.h = jsonCrop.getDouble(ImageInfo.CROP_H);
					}
					if(jsonCrop.has(ImageInfo.CROP_CONTAINER_W)){
						crop.ContainerW = jsonCrop.getDouble(ImageInfo.CROP_CONTAINER_W);
					}
					if(jsonCrop.has(ImageInfo.CROP_CONTAINER_H)){
						crop.ContainerH = jsonCrop.getDouble(ImageInfo.CROP_CONTAINER_H);
					}
					imageInfo.crop = crop;
				}
				if(jsonImgInfo.has(ImageInfo.KPT_LEVEL)){
					imageInfo.kptLevel = jsonImgInfo.getInt(ImageInfo.KPT_LEVEL);
				}
				if(jsonImgInfo.has(ImageInfo.COLOR_EFFECT)){
					imageInfo.colorEffect = jsonImgInfo.getInt(ImageInfo.COLOR_EFFECT);
				}
				if(jsonImgInfo.has(ImageInfo.AUTO_RED_EYE)){
					imageInfo.autoRedEye = jsonImgInfo.getBoolean(ImageInfo.AUTO_RED_EYE);
				}
				if(jsonImgInfo.has(ImageInfo.MANUAL_RED_EYE)){
					imageInfo.manualRedEye = jsonImgInfo.getBoolean(ImageInfo.MANUAL_RED_EYE);
				}
				if(jsonImgInfo.has(ImageInfo.PET_EYE)){
					imageInfo.petEye = jsonImgInfo.getBoolean(ImageInfo.PET_EYE);
				}
				if(jsonImgInfo.has(ImageInfo.CAPTION_LANGUAGE)){
					imageInfo.captionLanguage = jsonImgInfo.getString(ImageInfo.CAPTION_LANGUAGE);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			imageInfo = null;
		}
		return imageInfo;
	}
}
