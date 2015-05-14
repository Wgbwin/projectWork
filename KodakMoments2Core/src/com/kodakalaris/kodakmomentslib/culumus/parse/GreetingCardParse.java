package com.kodakalaris.kodakmomentslib.culumus.parse;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kodakalaris.kodakmomentslib.culumus.bean.content.Content;
import com.kodakalaris.kodakmomentslib.culumus.bean.content.SearchStarter;
import com.kodakalaris.kodakmomentslib.culumus.bean.content.SearchStarterCategory;
import com.kodakalaris.kodakmomentslib.culumus.bean.greetingcard.GCCategory;
import com.kodakalaris.kodakmomentslib.culumus.bean.greetingcard.GCLayer;
import com.kodakalaris.kodakmomentslib.culumus.bean.greetingcard.GCPage;
import com.kodakalaris.kodakmomentslib.culumus.bean.greetingcard.GreetingCard;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;

public class GreetingCardParse extends Parse {

	public List<SearchStarterCategory> parseSearchStarterCategories(String result){
		List<SearchStarterCategory> searchStarterCategories = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			JSONArray jsCategories = jsObj.optJSONArray(SearchStarterCategory.FLAG_ContentSearchStarterCategories);
			if(jsCategories != null){
				searchStarterCategories = new ArrayList<SearchStarterCategory>();
				for(int i=0; i<jsCategories.length(); i++){
					SearchStarterCategory category = parseSearchStarterCategory(jsCategories.getJSONObject(i));
					searchStarterCategories.add(category);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return searchStarterCategories;
	}
	
	private SearchStarterCategory parseSearchStarterCategory(JSONObject jsCategory){
		SearchStarterCategory category = new SearchStarterCategory();
		category.name = jsCategory.optString(SearchStarterCategory.FLAG_Name);
		category.searchStarters = parseSearchStarters(jsCategory.optJSONArray(SearchStarterCategory.FLAG_SearchStarters));
		return category;
	}
	
	private List<SearchStarter> parseSearchStarters(JSONArray jsStarters){
		if(jsStarters==null){
			return null;
		}
		List<SearchStarter> starters = new ArrayList<SearchStarter>();
		for(int i=0; i<jsStarters.length(); i++){
			try {
				SearchStarter starter = parseSearchStarter(jsStarters.getJSONObject(i));
				starters.add(starter);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return starters;
	}
	
	private SearchStarter parseSearchStarter(JSONObject jsStarter){
		SearchStarter starter = new SearchStarter();
		starter.name = jsStarter.optString(SearchStarter.FLAG_Name);
		starter.glyphUrl = jsStarter.optString(SearchStarter.FLAG_GlyphURL);
		starter.filters = jsStarter.optString(SearchStarter.FLAG_Filters);
		starter.language = jsStarter.optString(SearchStarter.FLAG_Language);
		starter.rollOnDayOfYear = jsStarter.optString(SearchStarter.FLAG_RollOnDayOfYear);
		starter.rollOffDayOfYear = jsStarter.optString(SearchStarter.FLAG_RollOffDayOfYear);
		return starter;
	}
	
	public List<GCCategory> parseGCCategory(String result){
		List<GCCategory> contents = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.optJSONObject(Content.FLAG_ContentResults)!=null){
			JSONArray jsContents = jsObj.optJSONObject(Content.FLAG_ContentResults).optJSONArray(Content.FLAG_Contents);
			if(jsContents != null){
				contents = new ArrayList<GCCategory>();
				for(int i=0; i<jsContents.length(); i++){
					GCCategory content = parseContent(jsContents.getJSONObject(i));
					contents.add(content);
				}
			}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return contents;
	}
	
	private GCCategory parseContent(JSONObject jsContent) throws JSONException{
		GCCategory content = new GCCategory();
		content.baseURI = jsContent.optString(Content.FLAG_BaseURI);
		content.id = jsContent.optString(Content.FLAG_Id);
		content.localizedName = jsContent.optString(Content.FLAG_LocalizedName);
		content.usage = jsContent.optString(Content.FLAG_Usage);
		content.strClass = jsContent.optString(Content.FLAG_Class);
		content.vendor = jsContent.optString(Content.FLAG_Vendor);
		content.glyphURL = jsContent.optString(Content.FLAG_GlyphURL);
		content.numAssetGroups = jsContent.optInt(Content.FLAG_NumAssetGroups);
		content.bearsRoyalty = jsContent.optBoolean(Content.FLAG_BearsRoyalty);
		content.width = jsContent.optInt(Content.FLAG_Width);
		content.height = jsContent.optInt(Content.FLAG_Height);
		content.contentCount = jsContent.optInt(Content.FLAG_ContentCount);
		JSONArray jsIdentifiers = jsContent.optJSONArray(Content.FLAG_ProductIdentifiers);
		if(jsIdentifiers != null){
			for(int i=0; i<jsIdentifiers.length(); i++){
				content.productIdentifiers.add(jsIdentifiers.get(i).toString());
			}
		}
		
		return content;
	}
	
	public GreetingCard parseGreetingCard(String result){
		try {
			GreetingCard card = new GreetingCard();
			JSONObject object = new JSONObject(result);
			JSONObject jsonDetail = object.getJSONObject(GreetingCard.GREETING_CARD);
			if(jsonDetail.has(GreetingCard.FLAG_ID)){
				card.id = jsonDetail.getString(GreetingCard.FLAG_ID);
			}
			if(jsonDetail.has(GreetingCard.FLAG_PRODUCT_DESC_BASE_URI)){
				card.productDescriptionBaseURI = jsonDetail.getString(GreetingCard.FLAG_PRODUCT_DESC_BASE_URI);
			}
			if(jsonDetail.has(GreetingCard.FLAG_PRO_DESC_ID)){
				card.proDescId = jsonDetail.getString(GreetingCard.FLAG_PRO_DESC_ID);
			}
			if(jsonDetail.has(GreetingCard.FLAG_THEME)){
				card.theme = jsonDetail.getString(GreetingCard.FLAG_THEME);
			}
			if(jsonDetail.has(GreetingCard.FLAG_PAGES)){
				card.pages = parseGreetingCardPages(jsonDetail.getJSONArray(GreetingCard.FLAG_PAGES));
			}
			if(jsonDetail.has(GreetingCard.FLAG_IS_DUPLEX)){
				card.isDuplex = jsonDetail.getBoolean(GreetingCard.FLAG_IS_DUPLEX);
			}
			if(jsonDetail.has(GreetingCard.FLAG_MIN_NUM_OF_PAGES)){
				card.minNumberOfPages = jsonDetail.getInt(GreetingCard.FLAG_MIN_NUM_OF_PAGES);
			}
			if(jsonDetail.has(GreetingCard.FLAG_MAX_NUM_OF_PAGES)){
				card.maxNumberOfPages = jsonDetail.getInt(GreetingCard.FLAG_MAX_NUM_OF_PAGES);
			}
			if(jsonDetail.has(GreetingCard.FLAG_NUM_OF_PAGES_PER_BASE_CARD)){
				card.numberOfPagesPerBaseCard = jsonDetail.getInt(GreetingCard.FLAG_NUM_OF_PAGES_PER_BASE_CARD);
			}
			if(jsonDetail.has(GreetingCard.FLAG_MIN_NUM_OF_IMAGES)){
				card.minNumberOfImages = jsonDetail.getInt(GreetingCard.FLAG_MIN_NUM_OF_IMAGES);
			}
			if(jsonDetail.has(GreetingCard.FLAG_MAX_NUM_OF_IMAGES)){
				card.maxNumberOfImages = jsonDetail.getInt(GreetingCard.FLAG_MAX_NUM_OF_IMAGES);
			}
			if(jsonDetail.has(GreetingCard.FLAG_MAX_NUM_OF_IMAGES_PER_ADDED_PAGE)){
				card.maxNumberOfImagesPerAddedPage = jsonDetail.getString(GreetingCard.FLAG_MAX_NUM_OF_IMAGES_PER_ADDED_PAGE).toString().equals("null")?null:jsonDetail.getInt(GreetingCard.FLAG_MAX_NUM_OF_IMAGES_PER_ADDED_PAGE);
			}
			if(jsonDetail.has(GreetingCard.FLAG_MAX_NUM_OF_IMAGES_PER_BASE_CARD)){
				card.maxNumberOfImagesPerBaseCard = jsonDetail.getInt(GreetingCard.FLAG_MAX_NUM_OF_IMAGES_PER_BASE_CARD);
			}
			if(jsonDetail.has(GreetingCard.FLAG_IDEAL_NUM_OF_IMAGES_PER_BASE_CARD)){
				card.idealNumberOfImagesPerBaseCard = jsonDetail.getInt(GreetingCard.FLAG_IDEAL_NUM_OF_IMAGES_PER_BASE_CARD);
			}
			if(jsonDetail.has(GreetingCard.FLAG_NUM_OF_IMAGES_IN_CARD)){
				card.numberOfImagesInCard = jsonDetail.getInt(GreetingCard.FLAG_NUM_OF_IMAGES_IN_CARD);
			}
			if(jsonDetail.has(GreetingCard.FLAG_NUM_OF_UNASSIGNED_IMAGES)){
				card.numberOfUnassignedImages = jsonDetail.getString(GreetingCard.FLAG_NUM_OF_UNASSIGNED_IMAGES).toString().equals("null")?null:jsonDetail.getInt(GreetingCard.FLAG_NUM_OF_UNASSIGNED_IMAGES);
			}
			if(jsonDetail.has(GreetingCard.FLAG_SUGGESTED_CAPTION_VISIBILITY)){
				card.suggestedCaptionVisibility = jsonDetail.getBoolean(GreetingCard.FLAG_SUGGESTED_CAPTION_VISIBILITY);
			}
			if(jsonDetail.has(GreetingCard.FLAG_CAN_SET_TITLE)){
				card.canSetTitle = jsonDetail.getBoolean(GreetingCard.FLAG_CAN_SET_TITLE);
			}
			if(jsonDetail.has(GreetingCard.FLAG_CAN_SET_SUBTITLE)){
				card.canSetSubtitle = jsonDetail.getBoolean(GreetingCard.FLAG_CAN_SET_SUBTITLE);
			}
			if(jsonDetail.has(GreetingCard.FLAG_CAN_SET_AUTHOR)){
				card.canSetAuthor = jsonDetail.getBoolean(GreetingCard.FLAG_CAN_SET_AUTHOR);
			}
			/*for(GCPage page : card.pages){
				int index = 0;
				for(GCLayer layer: page.layers){
					layer.holeIndex = index;
					index ++;
				}
			}*/
			return card;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	private GCPage[] parseGreetingCardPages(JSONArray jsonPages){
		try {
			GCPage[] pages = null;
			if(jsonPages!=null){
				pages = new GCPage[jsonPages.length()];
				for(int i=0; i<jsonPages.length(); i++){
					JSONObject jsonPage = jsonPages.getJSONObject(i);
					GCPage page = parseGreetingCardPage(jsonPage);	
					pages[i] = page;
				}
			}
			return pages;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public GCPage parseGreetingCardPage(String result){
		try {
			JSONObject jsonObj = new JSONObject(result);
			JSONObject needParse = null;
			if(jsonObj.has(GreetingCard.FLAG_PAGE)){
				needParse = jsonObj.getJSONObject(GreetingCard.FLAG_PAGE);
			} else {
				needParse = jsonObj;
			}
			return parseGreetingCardPage(needParse);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private GCPage parseGreetingCardPage(JSONObject jsonPage){
		GCPage page = null;
		if(jsonPage!=null){
			try {
				page = new GCPage();
				if(jsonPage.has(GCPage.FLAG_BASE_URI)){
					page.baseURI = jsonPage.getString(GCPage.FLAG_BASE_URI);
				}
				if(jsonPage.has(GCPage.FLAG_ID)){
					page.id = jsonPage.getString(GCPage.FLAG_ID);
				}
				if(jsonPage.has(GCPage.FLAG_SEQUENCE_NUMBER)){
					page.sequenceNumber = jsonPage.getInt(GCPage.FLAG_SEQUENCE_NUMBER);
				}
				if(jsonPage.has(GCPage.FLAG_PAGE_TYPE)){
					page.pageType = jsonPage.getString(GCPage.FLAG_PAGE_TYPE);
				}
				if(jsonPage.has(GCPage.FLAG_LAYOUT_TYPE)){
					page.layoutType = jsonPage.getString(GCPage.FLAG_LAYOUT_TYPE);
				}
				if(jsonPage.has(GCPage.FLAG_WIDTH)){
					page.width = (float) jsonPage.getDouble(GCPage.FLAG_WIDTH);
				}
				if(jsonPage.has(GCPage.FLAG_HEIGHT)){
					page.height = (float) jsonPage.getDouble(GCPage.FLAG_HEIGHT);
				}
				if(jsonPage.has(GCPage.FLAG_MIN_NUM_OF_IMAGES)){
					page.minNumberOfImages = jsonPage.getInt(GCPage.FLAG_MIN_NUM_OF_IMAGES);
				}
				if(jsonPage.has(GCPage.FLAG_MAX_NUM_OF_IMAGES)){
					page.maxNumberOfImages = jsonPage.getInt(GCPage.FLAG_MAX_NUM_OF_IMAGES);
				}
				if(jsonPage.has(GCPage.FLAG_LAYERS)){
					page.layers = parseGreetingCardLayer(jsonPage.getJSONArray(GCPage.FLAG_LAYERS));
				}
				if(jsonPage.has(GCPage.FLAG_MARGIN)){
					JSONObject jsonMargin = jsonPage.getJSONObject(GCPage.FLAG_MARGIN);
					if(jsonMargin.has(GCPage.FLAG_MARGIN_TOP)){
						page.margin[0] = (float) jsonMargin.getDouble(GCPage.FLAG_MARGIN_TOP);
					}
					if(jsonMargin.has(GCPage.FLAG_MARGIN_LEFT)){
						page.margin[1] = (float) jsonMargin.getDouble(GCPage.FLAG_MARGIN_LEFT);
					}
					if(jsonMargin.has(GCPage.FLAG_MARGIN_BOTTOM)){
						page.margin[2] = (float) jsonMargin.getDouble(GCPage.FLAG_MARGIN_BOTTOM);
					}
					if(jsonMargin.has(GCPage.FLAG_MARGIN_RIGHT)){
						page.margin[3] = (float) jsonMargin.getDouble(GCPage.FLAG_MARGIN_RIGHT);
					}
				}
			} catch(JSONException je){
				je.printStackTrace();
			}
		}
		return page;
	}
	
	private List<GCLayer> parseGreetingCardLayer(JSONArray jsonLayers){
		try{
			List<GCLayer> layers = null;
			if(jsonLayers != null){
				layers = new ArrayList<GCLayer>();
				for(int i=0; i<jsonLayers.length(); i++){
					JSONObject jsonLayer = jsonLayers.getJSONObject(i);
					GCLayer layer = new GCLayer();
					if(jsonLayer.has(GCLayer.FLAG_TYPE)){
						layer.type = jsonLayer.getString(GCLayer.FLAG_TYPE);
					}
					if(jsonLayer.has(GCLayer.FLAG_LOCATION)){
						JSONObject jsonLocation = jsonLayer.getJSONObject(GCLayer.FLAG_LOCATION);
						ROI location = new ROI();
						if(jsonLocation.has(GCLayer.FLAG_LOCATION_X)){
							location.x = jsonLocation.getDouble(GCLayer.FLAG_LOCATION_X);
						}
						if(jsonLocation.has(GCLayer.FLAG_LOCATION_Y)){
							location.y = jsonLocation.getDouble(GCLayer.FLAG_LOCATION_Y);
						}
						if(jsonLocation.has(GCLayer.FLAG_LOCATION_W)){
							location.w = jsonLocation.getDouble(GCLayer.FLAG_LOCATION_W);
						}
						if(jsonLocation.has(GCLayer.FLAG_LOCATION_H)){
							location.h = jsonLocation.getDouble(GCLayer.FLAG_LOCATION_H);
						}
						if(jsonLocation.has(GCLayer.FLAG_LOCATION_CONTAINER_W)){
							location.ContainerW = jsonLocation.getDouble(GCLayer.FLAG_LOCATION_CONTAINER_W);
						}
						if(jsonLocation.has(GCLayer.FLAG_LOCATION_CONTAINER_H)){
							location.ContainerH = jsonLocation.getDouble(GCLayer.FLAG_LOCATION_CONTAINER_H);
						}
						layer.location = location;
					}
					if(jsonLayer.has(GCLayer.FLAG_ANGLE)){
						layer.angle = jsonLayer.getInt(GCLayer.FLAG_ANGLE);
					}
					if(jsonLayer.has(GCLayer.FLAG_PINNED)){
						layer.pinned = jsonLayer.getBoolean(GCLayer.FLAG_PINNED);
					}
					if(jsonLayer.has(GCLayer.FLAG_CONTENT_BASE_URI)){
						layer.contentBaseURI = jsonLayer.getString(GCLayer.FLAG_CONTENT_BASE_URI);
					}
					if(jsonLayer.has(GCLayer.FLAG_CONTENT_Id)){
						layer.contentId = jsonLayer.getString(GCLayer.FLAG_CONTENT_Id);
					}
					if(jsonLayer.has(GCLayer.FLAG_DATA)){
						JSONArray jsonData = jsonLayer.getJSONArray(GCLayer.FLAG_DATA);
						layer.data = parseData(jsonData);
					}
					layers.add(layer);
				}
			}
			return layers;
		} catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}

