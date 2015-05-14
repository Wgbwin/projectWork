package com.kodak.rss.core.n2r.parse;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.collage.AlternateLayout;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.collage.CollageLayer;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.collage.Element;

public class CollageParse extends Parse {

	public Collage parseCollage(String result){
		try {
			Collage collage = new Collage();
			JSONObject object = new JSONObject(result);
			JSONObject jsonDetail = object.getJSONObject(Collage.COLLAGE);
			if(jsonDetail.has(Collage.FLAG_ID)){
				collage.id = jsonDetail.getString(Collage.FLAG_ID);
			}
			if(jsonDetail.has(Collage.FLAG_PRODUCT_DESC_BASE_URI)){
				collage.productDescriptionBaseURI = jsonDetail.getString(Collage.FLAG_PRODUCT_DESC_BASE_URI);
			}
			if(jsonDetail.has(Collage.FLAG_PRO_DESC_ID)){
				collage.proDescId = jsonDetail.getString(Collage.FLAG_PRO_DESC_ID);
			}			
			if(jsonDetail.has(Collage.FLAG_PAGE)){
				collage.page = parseCollagePage(jsonDetail.getJSONObject(Collage.FLAG_PAGE));
			}		
			if(jsonDetail.has(Collage.FLAG_SUGGESTED_CAPTION_VISIBILITY)){
				collage.suggestedCaptionVisibility = jsonDetail.getBoolean(Collage.FLAG_SUGGESTED_CAPTION_VISIBILITY);
			}
			if(jsonDetail.has(Collage.FLAG_CAN_SET_TITLE)){
				collage.canSetTitle = jsonDetail.getBoolean(Collage.FLAG_CAN_SET_TITLE);
			}
			if(jsonDetail.has(Collage.FLAG_CAN_SET_SUBTITLE)){
				collage.canSetSubtitle = jsonDetail.getBoolean(Collage.FLAG_CAN_SET_SUBTITLE);
			}
			if(jsonDetail.has(Collage.FLAG_CAN_SET_AUTHOR)){
				collage.canSetAuthor = jsonDetail.getBoolean(Collage.FLAG_CAN_SET_AUTHOR);
			}
			if(jsonDetail.has(Collage.FLAG_CAN_SET_ORIENTATION)){
				collage.canSetOrientation = jsonDetail.getBoolean(Collage.FLAG_CAN_SET_ORIENTATION);
			}
			return collage;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	private CollagePage parseCollagePage(JSONObject jsonPage){
		CollagePage collagePage = null;
		if(jsonPage!=null){
			try {
				collagePage = new CollagePage();
				if(jsonPage.has(CollagePage.FLAG_BASE_URI)){
					collagePage.baseURI = jsonPage.getString(CollagePage.FLAG_BASE_URI);
				}
				if(jsonPage.has(CollagePage.FLAG_ID)){
					collagePage.id = jsonPage.getString(CollagePage.FLAG_ID);
				}
				if(jsonPage.has(CollagePage.FLAG_SEQUENCE_NUMBER)){
					collagePage.sequenceNumber = jsonPage.getInt(CollagePage.FLAG_SEQUENCE_NUMBER);
				}
				if(jsonPage.has(CollagePage.FLAG_PAGE_TYPE)){
					collagePage.pageType = jsonPage.getString(CollagePage.FLAG_PAGE_TYPE);
				}
				if(jsonPage.has(CollagePage.FLAG_LAYOUT_TYPE)){
					collagePage.layoutType = jsonPage.getString(CollagePage.FLAG_LAYOUT_TYPE);
				}
				if(jsonPage.has(CollagePage.FLAG_WIDTH)){
					collagePage.width = (float) jsonPage.getDouble(CollagePage.FLAG_WIDTH);
				}
				if(jsonPage.has(CollagePage.FLAG_HEIGHT)){
					collagePage.height = (float) jsonPage.getDouble(CollagePage.FLAG_HEIGHT);
				}
				if(jsonPage.has(CollagePage.FLAG_MIN_NUM_OF_IMAGES)){
					collagePage.minNumberOfImages = jsonPage.getInt(CollagePage.FLAG_MIN_NUM_OF_IMAGES);
				}
				if(jsonPage.has(CollagePage.FLAG_MAX_NUM_OF_IMAGES)){
					collagePage.maxNumberOfImages = jsonPage.getInt(CollagePage.FLAG_MAX_NUM_OF_IMAGES);
				}
				if(jsonPage.has(CollagePage.FLAG_LAYERS)){
					collagePage.layers = parseCollageLayer(jsonPage.getJSONArray(CollagePage.FLAG_LAYERS));
				}
				if(jsonPage.has(CollagePage.FLAG_MARGIN)){
					JSONObject jsonMargin = jsonPage.getJSONObject(CollagePage.FLAG_MARGIN);
					if(jsonMargin.has(CollagePage.FLAG_MARGIN_TOP)){
						collagePage.margin[0] = (float) jsonMargin.getDouble(CollagePage.FLAG_MARGIN_TOP);
					}
					if(jsonMargin.has(CollagePage.FLAG_MARGIN_LEFT)){
						collagePage.margin[1] = (float) jsonMargin.getDouble(CollagePage.FLAG_MARGIN_LEFT);
					}
					if(jsonMargin.has(CollagePage.FLAG_MARGIN_BOTTOM)){
						collagePage.margin[2] = (float) jsonMargin.getDouble(CollagePage.FLAG_MARGIN_BOTTOM);
					}
					if(jsonMargin.has(CollagePage.FLAG_MARGIN_RIGHT)){
						collagePage.margin[3] = (float) jsonMargin.getDouble(CollagePage.FLAG_MARGIN_RIGHT);
					}
				}
				if(jsonPage.has(CollagePage.FLAG_ALTERNATE_LAYOUTS)){
					collagePage.alternateLayouts = parseCollageAlternateLayout(jsonPage.getJSONArray(CollagePage.FLAG_ALTERNATE_LAYOUTS));
				}
				
			} catch(JSONException je){
				je.printStackTrace();
			}
		}
		return collagePage;
	}
	
	public CollagePage parseCollagePage(String result){
		try {
			JSONObject jsonObject = new JSONObject(result);
			if(jsonObject.has(Collage.FLAG_PAGE)){
				return parseCollagePage(jsonObject.getJSONObject(Collage.FLAG_PAGE));
			}		
			
		} catch (JSONException e) {			
			e.printStackTrace();
		}		
		return null;
	}	
	
	private CollageLayer[] parseCollageLayer(JSONArray jsonLayers){
		try{
			CollageLayer[] layers = null;
			if(jsonLayers != null){
				int length = jsonLayers.length();
				layers = new CollageLayer[length];
				for(int i = 0; i<length; i++){
					JSONObject jsonLayer = jsonLayers.getJSONObject(i);
					CollageLayer layer = new CollageLayer();
					if(jsonLayer.has(CollageLayer.FLAG_TYPE)){
						layer.type = jsonLayer.getString(CollageLayer.FLAG_TYPE);
					}
					if(jsonLayer.has(CollageLayer.FLAG_LOCATION)){
						JSONObject jsonLocation = jsonLayer.getJSONObject(CollageLayer.FLAG_LOCATION);
						ROI location = new ROI();
						if(jsonLocation.has(CollageLayer.FLAG_LOCATION_X)){
							location.x = jsonLocation.getDouble(CollageLayer.FLAG_LOCATION_X);
						}
						if(jsonLocation.has(CollageLayer.FLAG_LOCATION_Y)){
							location.y = jsonLocation.getDouble(CollageLayer.FLAG_LOCATION_Y);
						}
						if(jsonLocation.has(CollageLayer.FLAG_LOCATION_W)){
							location.w = jsonLocation.getDouble(CollageLayer.FLAG_LOCATION_W);
						}
						if(jsonLocation.has(CollageLayer.FLAG_LOCATION_H)){
							location.h = jsonLocation.getDouble(CollageLayer.FLAG_LOCATION_H);
						}
						if(jsonLocation.has(CollageLayer.FLAG_LOCATION_CONTAINER_W)){
							location.ContainerW = jsonLocation.getDouble(CollageLayer.FLAG_LOCATION_CONTAINER_W);
						}
						if(jsonLocation.has(CollageLayer.FLAG_LOCATION_CONTAINER_H)){
							location.ContainerH = jsonLocation.getDouble(CollageLayer.FLAG_LOCATION_CONTAINER_H);
						}
						layer.location = location;
					}
					if(jsonLayer.has(CollageLayer.FLAG_ANGLE)){
						layer.angle = jsonLayer.getInt(CollageLayer.FLAG_ANGLE);
					}
					if(jsonLayer.has(CollageLayer.FLAG_PINNED)){
						layer.pinned = jsonLayer.getBoolean(CollageLayer.FLAG_PINNED);
					}
					if(jsonLayer.has(CollageLayer.FLAG_CONTENT_BASE_URI)){
						layer.contentBaseURI = jsonLayer.getString(CollageLayer.FLAG_CONTENT_BASE_URI);
					}
					if(jsonLayer.has(CollageLayer.FLAG_CONTENT_Id)){
						layer.contentId = jsonLayer.getString(CollageLayer.FLAG_CONTENT_Id);
					}
					if(jsonLayer.has(CollageLayer.FLAG_DATA)){
						JSONArray jsonData = jsonLayer.getJSONArray(CollageLayer.FLAG_DATA);
						layer.data = parseData(jsonData);
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
	
	private List<AlternateLayout> parseCollageAlternateLayout(JSONArray jsonAlternateLayouts){
		try{
			List<AlternateLayout> alternateLayouts = null;
			if(jsonAlternateLayouts != null){
				alternateLayouts = new ArrayList<AlternateLayout>();
				for(int i=0; i<jsonAlternateLayouts.length(); i++){
					JSONObject jsonjsonAlternateLayout = jsonAlternateLayouts.getJSONObject(i);
					AlternateLayout alternateLayout = new AlternateLayout();
					if(jsonjsonAlternateLayout.has(AlternateLayout.FLAG_LAYOUT_ID)){
						alternateLayout.layoutId = jsonjsonAlternateLayout.getString(AlternateLayout.FLAG_LAYOUT_ID);
					}
					if(jsonjsonAlternateLayout.has(AlternateLayout.FLAG_ELEMENTS)){
						JSONArray jsonElements = jsonjsonAlternateLayout.getJSONArray(AlternateLayout.FLAG_ELEMENTS);
						List<Element> elements = null;
						if(jsonElements != null){
							elements = new ArrayList<Element>();
							for(int j = 0; j < jsonElements.length(); j++){
								JSONObject jsonElement = jsonElements.getJSONObject(j);
								Element element = new Element();
								
								if(jsonElement.has(Element.FLAG_CONTENT_ID)){
									element.contentId = jsonElement.getString(Element.FLAG_CONTENT_ID);
								}
								if(jsonElement.has(Element.FLAG_LOCATION)){
									JSONObject jsonLocation = jsonElement.getJSONObject(Element.FLAG_LOCATION);
									ROI location = new ROI();
									if(jsonLocation.has(Element.FLAG_LOCATION_X)){
										location.x = jsonLocation.getDouble(Element.FLAG_LOCATION_X);
									}
									if(jsonLocation.has(Element.FLAG_LOCATION_Y)){
										location.y = jsonLocation.getDouble(Element.FLAG_LOCATION_Y);
									}
									if(jsonLocation.has(Element.FLAG_LOCATION_W)){
										location.w = jsonLocation.getDouble(Element.FLAG_LOCATION_W);
									}
									if(jsonLocation.has(Element.FLAG_LOCATION_H)){
										location.h = jsonLocation.getDouble(Element.FLAG_LOCATION_H);
									}
									if(jsonLocation.has(Element.FLAG_LOCATION_CONTAINER_W)){
										location.ContainerW = jsonLocation.getDouble(Element.FLAG_LOCATION_CONTAINER_W);
									}
									if(jsonLocation.has(Element.FLAG_LOCATION_CONTAINER_H)){
										location.ContainerH = jsonLocation.getDouble(Element.FLAG_LOCATION_CONTAINER_H);
									}
									element.location = location;
								}
								if(jsonElement.has(Element.FLAG_ANGLE)){
									element.angle = jsonElement.getInt(Element.FLAG_ANGLE);
								}
								elements.add(element);
							}	
						}
						alternateLayout.elements = elements;
					}					
					alternateLayouts.add(alternateLayout);
				}
			}
			return alternateLayouts;
		} catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}

