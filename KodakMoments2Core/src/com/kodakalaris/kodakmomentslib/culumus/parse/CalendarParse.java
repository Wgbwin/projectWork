package com.kodakalaris.kodakmomentslib.culumus.parse;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kodakalaris.kodakmomentslib.culumus.bean.calendar.Calendar;
import com.kodakalaris.kodakmomentslib.culumus.bean.calendar.CalendarContent;
import com.kodakalaris.kodakmomentslib.culumus.bean.calendar.CalendarLayer;
import com.kodakalaris.kodakmomentslib.culumus.bean.calendar.CalendarPage;
import com.kodakalaris.kodakmomentslib.culumus.bean.calendar.CalendarTheme;
import com.kodakalaris.kodakmomentslib.culumus.bean.calendar.CalendarTheme.BackGround;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;

public class CalendarParse extends Parse {

	public List<CalendarTheme> parseCalendarThemes(String result,String productDescriptionId) throws WebAPIException{
		checkError(result);
		List<CalendarTheme> calendarThemes = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(CalendarTheme.FLAG_CalendarResults) && jsObj.getJSONObject(CalendarTheme.FLAG_CalendarResults).has(CalendarTheme.FLAG_Calendars)){
				calendarThemes = new ArrayList<CalendarTheme>();
				JSONArray jsCalendars = jsObj.getJSONObject(CalendarTheme.FLAG_CalendarResults).getJSONArray(CalendarTheme.FLAG_Calendars);
				for(int i=0; i<jsCalendars.length(); i++){
					CalendarTheme calendarTheme = new CalendarTheme();
					JSONObject jsCalendar = jsCalendars.getJSONObject(i);
					if(jsCalendar.has(CalendarTheme.FLAG_Id)){
						calendarTheme.id = jsCalendar.getString(CalendarTheme.FLAG_Id);
					}
					if(jsCalendar.has(CalendarTheme.FLAG_Name)){
						calendarTheme.name = jsCalendar.getString(CalendarTheme.FLAG_Name);
					}
					if(jsCalendar.has(CalendarTheme.FLAG_GlyphURL)){
						calendarTheme.glyphUrl = jsCalendar.getString(CalendarTheme.FLAG_GlyphURL);
					}					
					if(jsCalendar.has(CalendarTheme.FLAG_Backgrounds)){
						JSONArray jsBacks = jsCalendar.getJSONArray(CalendarTheme.FLAG_Backgrounds);
						calendarTheme.backGrounds = new BackGround[jsBacks.length()];
						for(int j=0; j<jsBacks.length(); j++){
							BackGround backGround = new BackGround();
							JSONObject jsBack = jsBacks.getJSONObject(j);
							if(jsBack.has(CalendarTheme.FLAG_Id)){
								backGround.id = jsBack.getString(CalendarTheme.FLAG_Id);
							}
							if(jsBack.has(CalendarTheme.FLAG_Name)){
								backGround.name = jsBack.getString(CalendarTheme.FLAG_Name);
							}
							if(jsBack.has(BackGround.FLAG_ImageURL)){
								backGround.imageURL = jsBack.getString(BackGround.FLAG_ImageURL);
							}
							if(jsBack.has(BackGround.FLAG_GlyphURL)){
								backGround.glyphURL = jsBack.getString(BackGround.FLAG_GlyphURL);
							}
							calendarTheme.backGrounds[j] = backGround;
						}
					}
					calendarTheme.productDescriptionId = productDescriptionId;
					calendarThemes.add(calendarTheme);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return calendarThemes;
	}
	
	public Calendar parseCalendar(String result) throws WebAPIException{
		checkError(result);
		Calendar calendar = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(Calendar.FLAG_CALENDAR)){
				calendar = new Calendar();
				JSONObject jsCalendar = jsObj.getJSONObject(Calendar.FLAG_CALENDAR);				
				if(jsCalendar.has(Calendar.FLAG_ID)){
					calendar.id = jsCalendar.getString(Calendar.FLAG_ID);
				}
				if(jsCalendar.has(Calendar.FLAG_PRODUCT_DESC_BASE_URI)){
					calendar.productDescriptionBaseURI = jsCalendar.getString(Calendar.FLAG_PRODUCT_DESC_BASE_URI);
				}
				if(jsCalendar.has(Calendar.FLAG_PRO_DESC_ID)){
					calendar.proDescId = jsCalendar.getString(Calendar.FLAG_PRO_DESC_ID);
				}	
				if(jsCalendar.has(Calendar.FLAG_CALENDARTYPE)){
					calendar.calendarType = jsCalendar.getString(Calendar.FLAG_CALENDARTYPE);
				}
				if(jsCalendar.has(Calendar.FLAG_CONTENT_BASE_URI)){
					calendar.contentBaseURI = jsCalendar.getString(Calendar.FLAG_CONTENT_BASE_URI);
				}	
				if(jsCalendar.has(Calendar.FLAG_CONTENT_ID)){
					calendar.contentId = jsCalendar.getString(Calendar.FLAG_CONTENT_ID);
				}
				
				if(jsCalendar.has(Calendar.FLAG_LANGUAGE)){
					calendar.language = jsCalendar.getString(Calendar.FLAG_LANGUAGE);
				}					
				if(jsCalendar.has(Calendar.FLAG_STARTING_MONTH)){
					calendar.startingMonth = jsCalendar.getInt(Calendar.FLAG_STARTING_MONTH);
				}
				if(jsCalendar.has(Calendar.FLAG_STARTING_YEAR)){
					calendar.startingYear = jsCalendar.getInt(Calendar.FLAG_STARTING_YEAR);
				}	
				if(jsCalendar.has(Calendar.FLAG_STARTING_DAY_OF_WEEK)){
					calendar.startingDayOfWeek = jsCalendar.getInt(Calendar.FLAG_STARTING_DAY_OF_WEEK);
				}
				
				if(jsCalendar.has(Calendar.FLAG_PAGES)){
					calendar.pages = parseCalendarPages(jsCalendar.getJSONArray(Calendar.FLAG_PAGES));
				}

				if(jsCalendar.has(Calendar.FLAG_IS_DUPLEX)){
					calendar.isDuplex = jsCalendar.getBoolean(Calendar.FLAG_IS_DUPLEX);
				}	
				if(jsCalendar.has(Calendar.FLAG_MIN_NUM_OF_PAGES)){
					calendar.minNumberOfPages = jsCalendar.getInt(Calendar.FLAG_MIN_NUM_OF_PAGES);
				}					
				if(jsCalendar.has(Calendar.FLAG_MAX_NUM_OF_PAGES)){
					calendar.maxNumberOfPages = jsCalendar.getInt(Calendar.FLAG_MAX_NUM_OF_PAGES);
				}
				if(jsCalendar.has(Calendar.FLAG_NUM_OF_PAGES_PER_BASE_Product)){
					calendar.numberOfPagesPerBaseProduct = jsCalendar.getInt(Calendar.FLAG_NUM_OF_PAGES_PER_BASE_Product);
				}	
				if(jsCalendar.has(Calendar.FLAG_MIN_NUM_OF_IMAGES)){
					calendar.minNumberOfImages = jsCalendar.getInt(Calendar.FLAG_MIN_NUM_OF_IMAGES);
				}
				if(jsCalendar.has(Calendar.FLAG_MAX_NUM_OF_IMAGES)){
					calendar.maxNumberOfImages = jsCalendar.getInt(Calendar.FLAG_MAX_NUM_OF_IMAGES);
				}
				if(jsCalendar.has(Calendar.FLAG_IDEAL_NUM_OF_IMAGES)){
					calendar.idealNumberOfImages = jsCalendar.getInt(Calendar.FLAG_IDEAL_NUM_OF_IMAGES);
				}	
				if(jsCalendar.has(Calendar.FLAG_NUM_OF_IMAGES)){
					calendar.numberOfImages = jsCalendar.getInt(Calendar.FLAG_NUM_OF_IMAGES);
				}
				
				if(jsCalendar.has(Calendar.FLAG_SUGGESTED_CAPTION_VISIBILITY)){
					calendar.suggestedCaptionVisibility = jsCalendar.getBoolean(Calendar.FLAG_SUGGESTED_CAPTION_VISIBILITY);
				}
				if(jsCalendar.has(Calendar.FLAG_CAN_SET_TITLE)){
					calendar.canSetTitle = jsCalendar.getBoolean(Calendar.FLAG_CAN_SET_TITLE);
				}
				if(jsCalendar.has(Calendar.FLAG_CAN_SET_SUBTITLE)){
					calendar.canSetSubtitle = jsCalendar.getBoolean(Calendar.FLAG_CAN_SET_SUBTITLE);
				}	
				if(jsCalendar.has(Calendar.FLAG_CAN_SET_AUTHOR)){
					calendar.canSetAuthor = jsCalendar.getBoolean(Calendar.FLAG_CAN_SET_AUTHOR);
				}

			}				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return calendar;
	}
	
	public CalendarPage[] parseCalendarPages(String result) throws WebAPIException{
		checkError(result);
		CalendarPage[] pages = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj!=null && jsObj.has(Calendar.FLAG_PAGES)){
				JSONArray jsonPages = jsObj.getJSONArray(Calendar.FLAG_PAGES);
				pages = new CalendarPage[jsonPages.length()];
				for(int i=0; i<jsonPages.length(); i++){
					JSONObject jsonPage = jsonPages.getJSONObject(i);
					CalendarPage page = parseCalendarPage(jsonPage);	
					pages[i] = page;
				}
			}
			return pages;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	private List<CalendarPage> parseCalendarPages(JSONArray jsonPages){
		try {
			List<CalendarPage> pages = null;
			if(jsonPages!=null){
				pages = new ArrayList<CalendarPage>(jsonPages.length());
				for(int i=0; i<jsonPages.length(); i++){
					JSONObject jsonPage = jsonPages.getJSONObject(i);
					CalendarPage page = parseCalendarPage(jsonPage);	
					pages.add(page);
				}
			}
			return pages;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public CalendarPage parseCalendarPage(String result)throws WebAPIException{
		checkError(result);
		try {
			JSONObject jsonObj = new JSONObject(result);
			JSONObject needParse = null;
			if(jsonObj.has(CalendarPage.FLAG_PAGE)){
				needParse = jsonObj.getJSONObject(CalendarPage.FLAG_PAGE);
			} else {
				needParse = jsonObj;
			}
			return parseCalendarPage(needParse);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private CalendarPage parseCalendarPage(JSONObject jsonPage){
		CalendarPage page = null;
		if(jsonPage!=null){
			try {
				page = new CalendarPage();
				if(jsonPage.has(CalendarPage.FLAG_BASE_URI)){
					page.baseURI = jsonPage.getString(CalendarPage.FLAG_BASE_URI);
				}
				if(jsonPage.has(CalendarPage.FLAG_ID)){
					page.id = jsonPage.getString(CalendarPage.FLAG_ID);
				}
				if(jsonPage.has(CalendarPage.FLAG_SEQUENCE_NUMBER)){
					page.sequenceNumber = jsonPage.getInt(CalendarPage.FLAG_SEQUENCE_NUMBER);
				}
				if(jsonPage.has(CalendarPage.FLAG_PAGE_TYPE)){
					page.pageType = jsonPage.getString(CalendarPage.FLAG_PAGE_TYPE);
				}
				if(jsonPage.has(CalendarPage.FLAG_THEME_BACKGROUND)){
					page.themeBackground = jsonPage.getString(CalendarPage.FLAG_THEME_BACKGROUND);
				}
				if(jsonPage.has(CalendarPage.FLAG_LAYOUT_TYPE)){
					page.layoutType = jsonPage.getString(CalendarPage.FLAG_LAYOUT_TYPE);
				}
				if(jsonPage.has(CalendarPage.FLAG_WIDTH)){
					page.width = (float) jsonPage.getDouble(CalendarPage.FLAG_WIDTH);
				}
				if(jsonPage.has(CalendarPage.FLAG_HEIGHT)){
					page.height = (float) jsonPage.getDouble(CalendarPage.FLAG_HEIGHT);
				}
				if(jsonPage.has(CalendarPage.FLAG_MIN_NUM_OF_IMAGES)){
					page.minNumberOfImages = jsonPage.getInt(CalendarPage.FLAG_MIN_NUM_OF_IMAGES);
				}
				if(jsonPage.has(CalendarPage.FLAG_MAX_NUM_OF_IMAGES)){
					page.maxNumberOfImages = jsonPage.getInt(CalendarPage.FLAG_MAX_NUM_OF_IMAGES);
				}
				if(jsonPage.has(CalendarPage.FLAG_LAYERS)){
					page.layers = parseCalendarLayer(jsonPage.getJSONArray(CalendarPage.FLAG_LAYERS));
				}
				if(jsonPage.has(CalendarPage.FLAG_MARGIN)){
					JSONObject jsonMargin = jsonPage.getJSONObject(CalendarPage.FLAG_MARGIN);
					if(jsonMargin.has(CalendarPage.FLAG_MARGIN_TOP)){
						page.margin[0] = (float) jsonMargin.getDouble(CalendarPage.FLAG_MARGIN_TOP);
					}
					if(jsonMargin.has(CalendarPage.FLAG_MARGIN_LEFT)){
						page.margin[1] = (float) jsonMargin.getDouble(CalendarPage.FLAG_MARGIN_LEFT);
					}
					if(jsonMargin.has(CalendarPage.FLAG_MARGIN_BOTTOM)){
						page.margin[2] = (float) jsonMargin.getDouble(CalendarPage.FLAG_MARGIN_BOTTOM);
					}
					if(jsonMargin.has(CalendarPage.FLAG_MARGIN_RIGHT)){
						page.margin[3] = (float) jsonMargin.getDouble(CalendarPage.FLAG_MARGIN_RIGHT);
					}
				}
			} catch(JSONException je){
				je.printStackTrace();
			}
		}
		return page;
	}
	
	private List<CalendarLayer> parseCalendarLayer(JSONArray jsonLayers){
		try{
			List<CalendarLayer> layers = null;
			if(jsonLayers != null){
				layers = new ArrayList<CalendarLayer>();
				for(int i = 0; i<jsonLayers.length(); i++){
					JSONObject jsonLayer = jsonLayers.getJSONObject(i);
					CalendarLayer layer = new CalendarLayer();
					if(jsonLayer.has(CalendarLayer.FLAG_TYPE)){
						layer.type = jsonLayer.getString(CalendarLayer.FLAG_TYPE);
					}
					if(jsonLayer.has(CalendarLayer.FLAG_LOCATION)){
						JSONObject jsonLocation = jsonLayer.getJSONObject(CalendarLayer.FLAG_LOCATION);
						ROI location = new ROI();
						if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_X)){
							location.x = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_X);
						}
						if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_Y)){
							location.y = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_Y);
						}
						if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_W)){
							location.w = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_W);
						}
						if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_H)){
							location.h = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_H);
						}
						if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_CONTAINER_W)){
							location.ContainerW = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_CONTAINER_W);
						}
						if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_CONTAINER_H)){
							location.ContainerH = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_CONTAINER_H);
						}
						layer.location = location;
					}
					if(jsonLayer.has(CalendarLayer.FLAG_ANGLE)){
						layer.angle = jsonLayer.getInt(CalendarLayer.FLAG_ANGLE);
					}
					if(jsonLayer.has(CalendarLayer.FLAG_PINNED)){
						layer.pinned = jsonLayer.getBoolean(CalendarLayer.FLAG_PINNED);
					}
					if(jsonLayer.has(CalendarLayer.FLAG_CONTENT_BASE_URI)){
						layer.contentBaseURI = jsonLayer.getString(CalendarLayer.FLAG_CONTENT_BASE_URI);
					}
					if(jsonLayer.has(CalendarLayer.FLAG_CONTENT_Id)){
						layer.contentId = jsonLayer.getString(CalendarLayer.FLAG_CONTENT_Id);
					}
					if(jsonLayer.has(CalendarLayer.FLAG_LAYERS)){
						JSONArray jsonSubLayers = jsonLayer.getJSONArray(CalendarLayer.FLAG_LAYERS);
						layer.sublayers = parseLayers(jsonSubLayers);
					}
					
					if(jsonLayer.has(CalendarLayer.FLAG_DATA)){
						JSONArray jsonData = jsonLayer.getJSONArray(CalendarLayer.FLAG_DATA);
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
	
	protected CalendarLayer[] parseLayers(JSONArray jsonLayers){
		try{
			CalendarLayer[] layers = null;
			if(jsonLayers != null && jsonLayers.length() > 0){				
				int length = jsonLayers.length();
				layers = new CalendarLayer[length];
				for(int i=0; i<length; i++){
					JSONObject jsonLayer = jsonLayers.getJSONObject(i);
					CalendarLayer layer = parseLayer(jsonLayer);
					layers[i] = layer;
				}
			}
			return layers;
		} catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected CalendarLayer parseLayer(JSONObject jsonLayer) {
		CalendarLayer layer = null;
		try {
			layer = new CalendarLayer();
			if(jsonLayer.has(CalendarLayer.FLAG_TYPE)){
				layer.type = jsonLayer.getString(CalendarLayer.FLAG_TYPE);
			}
			if(jsonLayer.has(CalendarLayer.FLAG_LOCATION)){
				JSONObject jsonLocation = jsonLayer.getJSONObject(CalendarLayer.FLAG_LOCATION);
				ROI location = new ROI();
				if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_X)){
					location.x = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_X);
				}
				if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_Y)){
					location.y = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_Y);
				}
				if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_W)){
					location.w = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_W);
				}
				if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_H)){
					location.h = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_H);
				}
				if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_CONTAINER_W)){
					location.ContainerW = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_CONTAINER_W);
				}
				if(jsonLocation.has(CalendarLayer.FLAG_LOCATION_CONTAINER_H)){
					location.ContainerH = jsonLocation.getDouble(CalendarLayer.FLAG_LOCATION_CONTAINER_H);
				}
				layer.location = location;
			}
			if(jsonLayer.has(CalendarLayer.FLAG_ANGLE)){
				layer.angle = jsonLayer.getInt(CalendarLayer.FLAG_ANGLE);
			}
			if(jsonLayer.has(CalendarLayer.FLAG_PINNED)){
				layer.pinned = jsonLayer.getBoolean(CalendarLayer.FLAG_PINNED);
			}
			if(jsonLayer.has(CalendarLayer.FLAG_CONTENT_BASE_URI)){
				layer.contentBaseURI = jsonLayer.getString(CalendarLayer.FLAG_CONTENT_BASE_URI);
			}
			if(jsonLayer.has(CalendarLayer.FLAG_CONTENT_Id)){
				layer.contentId = jsonLayer.getString(CalendarLayer.FLAG_CONTENT_Id);
			}
			if(jsonLayer.has(CalendarLayer.FLAG_DATA)){
				JSONArray jsonData = jsonLayer.getJSONArray(CalendarLayer.FLAG_DATA);
				layer.data = parseData(jsonData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return layer;
	}	
	
	public List<CalendarContent> parseCalendarContents(String result) throws WebAPIException{
		checkError(result);
		List<CalendarContent> calendarContents = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(CalendarContent.FLAG_CONTENTS)){
				calendarContents = new ArrayList<CalendarContent>();
				JSONArray jsCalendarContents = jsObj.getJSONArray(CalendarContent.FLAG_CONTENTS);
				for(int i=0; i<jsCalendarContents.length(); i++){
					CalendarContent calendarContent = new CalendarContent();
					JSONObject jsCalendarContent = jsCalendarContents.getJSONObject(i);
					if(jsCalendarContent.has(CalendarContent.FLAG_CONTENT_BASE_URI)){
						calendarContent.contentBaseURI = jsCalendarContent.getString(CalendarContent.FLAG_CONTENT_BASE_URI);
					}
					if(jsCalendarContent.has(CalendarContent.FLAG_CONTENT_ID)){
						calendarContent.contentId = jsCalendarContent.getString(CalendarContent.FLAG_CONTENT_ID);
					}
					if(jsCalendarContent.has(CalendarContent.FLAG_CONTENT_TYPE)){
						calendarContent.contentType = jsCalendarContent.getString(CalendarContent.FLAG_CONTENT_TYPE);
					}									
					calendarContents.add(calendarContent);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return calendarContents;
	}	
	
}

