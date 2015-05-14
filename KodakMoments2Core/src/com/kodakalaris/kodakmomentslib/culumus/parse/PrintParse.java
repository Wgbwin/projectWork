package com.kodakalaris.kodakmomentslib.culumus.parse;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kodakalaris.kodakmomentslib.culumus.bean.prints.PrintLayer;
import com.kodakalaris.kodakmomentslib.culumus.bean.prints.PrintPage;
import com.kodakalaris.kodakmomentslib.culumus.bean.prints.StandardPrint;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.Layer;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.Page;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;

public class PrintParse extends Parse{
	public List<StandardPrint> parseStandardSevicePrints(String result) throws WebAPIException{
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
						print.page = parsePrintPage(jsPrint.getJSONObject(StandardPrint.FLAG_PAGE));
					}
					prints.add(print);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return prints;
	}
	
	protected PrintPage parsePrintPage(JSONObject jsonPage) {
		PrintPage page = null;
		if(jsonPage!=null){
			try {
				page = new PrintPage();
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
					page.layers = parsePrintLayers(jsonPage.getJSONArray(Page.FLAG_LAYERS),Integer.valueOf(length));
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
	
	protected List<PrintLayer> parsePrintLayers(JSONArray jsonLayers,int length){
		try{
			List<PrintLayer> layers = null;
			if(jsonLayers != null && length > 0){				
				layers = new ArrayList<PrintLayer>();
				for(int i=0; i<jsonLayers.length(); i++){
					JSONObject jsonLayer = jsonLayers.getJSONObject(i);
					PrintLayer layer = parseLayer(jsonLayer);
					layers.add(layer);
				}
			}
			return layers;
		} catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public PrintLayer parsePrintLayer(String result) throws WebAPIException {
		checkError(result);
		PrintLayer layer = null;
		try {
			JSONObject jsonLayer = new JSONObject(result);
			layer = parseLayer(jsonLayer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return layer;
	}
	
	protected PrintLayer parseLayer(JSONObject jsonLayer) {
		PrintLayer layer = null;
		try {
			layer = new PrintLayer();
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
	
}
