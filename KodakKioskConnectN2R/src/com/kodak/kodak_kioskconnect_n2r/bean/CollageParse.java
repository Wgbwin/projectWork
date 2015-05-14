package com.kodak.kodak_kioskconnect_n2r.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.AlternateLayout;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.AlternateLayout.Element;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Data;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;
import com.kodak.kodak_kioskconnect_n2r.bean.text.Font;
import com.kodak.kodak_kioskconnect_n2r.bean.text.TextBlock;

public class CollageParse extends Parse {
	
	public Collage parseCollage(String result){
		Collage collage = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			JSONObject jsCollage = jsObj.optJSONObject(Collage.FLAG_Collage);
			if(jsCollage != null){
				collage = new Collage();
				collage.id = jsCollage.optString(Collage.FLAG_ID);
				collage.proDescId = jsCollage.optString(Collage.FLAG_PRO_DESC_ID);
				collage.setTheme(jsCollage.optString(Collage.FLAG_THEME, "")) ;
				String page = jsCollage.optString(Collage.FLAG_Page);
				collage.page = parseCollagePage(page);
				collage.suggestedCaptionVisibility = jsCollage.optBoolean(Collage.FLAG_SuggestedCaptionVisibility);
				collage.canSetOrientation = jsCollage.optBoolean(Collage.FLAG_CanSetOrientation);
				collage.canSetTitle = jsCollage.optBoolean(Collage.FLAG_CanSetTitle);
				collage.canSetSubtitle = jsCollage.optBoolean(Collage.FLAG_CanSetSubtitle);
				collage.canSetAuthor = jsCollage.optBoolean(Collage.FLAG_CanSetAuthor);
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return collage;
	}
	
	public CollagePage parseCollagePage(String result){
		CollagePage page = null;
		try {
			JSONObject resultObj  =  new JSONObject(result);
			
			
			if(resultObj!=null){
				JSONObject jsonPage = null ;
				if(resultObj.has(Collage.FLAG_Page)){
					jsonPage = resultObj.optJSONObject(Collage.FLAG_Page) ;
				}else {
					jsonPage = resultObj ;
				}
				page = new CollagePage();
				if(jsonPage.has(CollagePage.FLAG_BASE_URI)){
					page.baseURI = jsonPage.getString(CollagePage.FLAG_BASE_URI);
				}
				if(jsonPage.has(CollagePage.FLAG_ID)){
					page.id = jsonPage.getString(CollagePage.FLAG_ID);
				}
				if(jsonPage.has(CollagePage.FLAG_SEQUENCE_NUMBER)){
					page.sequenceNumber = jsonPage.getInt(CollagePage.FLAG_SEQUENCE_NUMBER);
				}
				if(jsonPage.has(CollagePage.FLAG_PAGE_TYPE)){
					page.pageType = jsonPage.getString(CollagePage.FLAG_PAGE_TYPE);
				}
				if(jsonPage.has(CollagePage.FLAG_LAYOUT_TYPE)){
					page.layoutType = jsonPage.getString(CollagePage.FLAG_LAYOUT_TYPE);
				}
				if(jsonPage.has(CollagePage.FLAG_WIDTH)){
					page.width = (float) jsonPage.getDouble(CollagePage.FLAG_WIDTH);
				}
				if(jsonPage.has(CollagePage.FLAG_HEIGHT)){
					page.height = (float) jsonPage.getDouble(CollagePage.FLAG_HEIGHT);
				}
				if(jsonPage.has(CollagePage.FLAG_MIN_NUM_OF_IMAGES)){
					page.minNumberOfImages = jsonPage.getInt(CollagePage.FLAG_MIN_NUM_OF_IMAGES);
				}
				if(jsonPage.has(CollagePage.FLAG_MAX_NUM_OF_IMAGES)){
					page.maxNumberOfImages = jsonPage.getInt(CollagePage.FLAG_MAX_NUM_OF_IMAGES);
				}
				if(jsonPage.has(CollagePage.FLAG_LAYERS)){
					page.layers = parseLayers(jsonPage.getJSONArray(CollagePage.FLAG_LAYERS),Integer.valueOf(page.maxNumberOfImages));
				}
				if(jsonPage.has(CollagePage.FLAG_MARGIN)){
					JSONObject jsonMargin = jsonPage.getJSONObject(CollagePage.FLAG_MARGIN);
					if(jsonMargin.has(CollagePage.FLAG_MARGIN_TOP)){
						page.margin[0] = (float) jsonMargin.getDouble(CollagePage.FLAG_MARGIN_TOP);
					}
					if(jsonMargin.has(CollagePage.FLAG_MARGIN_LEFT)){
						page.margin[1] = (float) jsonMargin.getDouble(CollagePage.FLAG_MARGIN_LEFT);
					}
					if(jsonMargin.has(CollagePage.FLAG_MARGIN_BOTTOM)){
						page.margin[2] = (float) jsonMargin.getDouble(CollagePage.FLAG_MARGIN_BOTTOM);
					}
					if(jsonMargin.has(CollagePage.FLAG_MARGIN_RIGHT)){
						page.margin[3] = (float) jsonMargin.getDouble(CollagePage.FLAG_MARGIN_RIGHT);
					}
				}
				if(jsonPage.has(CollagePage.FLAG_BackgroundImageBaseURI)){
					page.backgroundImageBaseURI = jsonPage.getString(CollagePage.FLAG_BackgroundImageBaseURI);
				}
				if(jsonPage.has(CollagePage.FLAG_BackgroundImageId)){
					page.backgroundImageId = jsonPage.getString(CollagePage.FLAG_BackgroundImageId);
				}
				
				JSONArray jsAlternateLayouts = jsonPage.optJSONArray(CollagePage.FLAG_AlternateLayouts);
				if(jsAlternateLayouts != null){
					page.alternateLayouts = new ArrayList<AlternateLayout>();
					for(int i=0; i<jsAlternateLayouts.length(); i++){
						AlternateLayout layout = parseAlternateLayout(jsAlternateLayouts.getJSONObject(i));
						page.alternateLayouts.add(layout);
					}
				}
			}
		} catch(JSONException je){
			je.printStackTrace();
		}
		return page;
	}
	
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
	
	public Layer parseLayer(String result) {
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
	
	private AlternateLayout parseAlternateLayout(JSONObject jsLayout){
		AlternateLayout layout = new AlternateLayout();
		layout.layoutId = jsLayout.optString(AlternateLayout.FLAG_LayoutId);
		layout.elements = new ArrayList<AlternateLayout.Element>();
		JSONArray jsElements = jsLayout.optJSONArray(AlternateLayout.FLAG_Elements);
		if(jsElements != null){
			for(int i=0; i<jsElements.length(); i++){
				Element element = new Element();
				try {
					element.contentId = jsElements.getJSONObject(i).optString(Element.FLAG_ContentId);
					JSONObject jsLocation = jsElements.getJSONObject(i).optJSONObject(Element.FLAG_Location);
					if(jsLocation != null){
						element.location = new ROI();
						element.location.x = jsLocation.optDouble(Element.LOCATION_X);
						element.location.y = jsLocation.optDouble(Element.LOCATION_Y);
						element.location.w = jsLocation.optDouble(Element.LOCATION_W);
						element.location.h = jsLocation.optDouble(Element.LOCATION_H);
						element.location.ContainerW = jsLocation.optDouble(Element.LOCATION_CONTAINER_W);
						element.location.ContainerH = jsLocation.optDouble(Element.LOCATION_CONTAINER_H);
					}
					element.angle = jsElements.getJSONObject(i).optInt(Element.FLAG_Angle);
					layout.elements.add(element);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		return layout;
	}
	
	public void checkError(String result){
		final String ERROR = "Error";
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(ERROR)){
				String jsResult = jsObj.getString(ERROR);
				if(jsResult.equals("null")){
					return;
				} else {
					JSONObject jsError = new JSONObject(jsResult);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
