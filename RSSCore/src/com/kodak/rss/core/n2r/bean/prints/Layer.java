package com.kodak.rss.core.n2r.bean.prints;

import java.io.Serializable;
import java.util.List;

import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.text.TextBlock;

public class Layer implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_TYPE = "Type";
	public static final String FLAG_LOCATION = "Location";
	public static final String FLAG_ANGLE = "Angle";
	public static final String FLAG_PINNED = "Pinned";
	public static final String FLAG_CONTENT_BASE_URI = "ContentBaseURI";
	public static final String FLAG_CONTENT_Id = "ContentId";
	public static final String FLAG_DATA = "Data";
	
	public static final String TYPE_IMAGE = "Image";
	public static final String TYPE_TEXT_BLOCK = "TextBlock";	
	
	/*
	 * for location ROI
	 */
	public static final String FLAG_LOCATION_X = "X";
	public static final String FLAG_LOCATION_Y = "Y";
	public static final String FLAG_LOCATION_W = "W";
	public static final String FLAG_LOCATION_H = "H";
	public static final String FLAG_LOCATION_CONTAINER_W = "ContainerW";
	public static final String FLAG_LOCATION_CONTAINER_H = "ContainerH";
	
	public String type;
	public ROI location;
	public int angle;
	public boolean pinned;
	public String contentBaseURI = "";
	public String contentId = "";
	public Data[] data;	
	
	//add bing wang on 14-3-7 for my projects
	public List<String> copyIds;
	
	public TextBlock getTextBlock() {
		if(TYPE_TEXT_BLOCK.equals(type)){
			TextBlock textBlock = new TextBlock();
			if(data != null){
				for(Data d : data){
					textBlock.id = contentId;
					String name = d.name==null?"":d.name;
					if(Data.TYPE_ALIGNMENT.equals(name)){
						textBlock.alignment = d.getValue();
					}
					else if(Data.TYPE_FONT.equals(name)){
						textBlock.fontName = d.getValue();
					}
					else if(Data.TYPE_JUSTIFICATION.equals(name)){
						textBlock.justification = d.getValue();
					}
					else if(Data.TYPE_LANGUAGE.equals(name)){
						textBlock.language = d.getValue();
					}
					else if(Data.TYPE_SIZE.equals(name)){
						textBlock.fontSize = d.getValue();
					}
					else if(Data.TYPE_TEXT.equals(name)){
						textBlock.text = d.getValue();
					} else if(Data.TYPE_COLOR.equals(name)){
						textBlock.color = d.getValue();
					}
				}
			}
			return textBlock;
		}
		return null;
	}
	
	/**
	 * 
	 * @param textBlock is get from server.
	 */
	public void updateTextBlockData(TextBlock textBlock){		
		if(data != null){
			for(Data d : data){
				textBlock.id = contentId;
				String name = d.name==null?"":d.name;
				if(Data.TYPE_ALIGNMENT.equals(name)){
					d.value = textBlock.alignment;
				}
				else if(Data.TYPE_FONT.equals(name)){
					if (textBlock.font != null) {
						d.value = textBlock.font.name;
					}					
				}
				else if(Data.TYPE_JUSTIFICATION.equals(name)){
					d.value = textBlock.justification;
				}
				else if(Data.TYPE_LANGUAGE.equals(name)){
					d.value = textBlock.language;
				}
				else if(Data.TYPE_SIZE.equals(name)){
					if (textBlock.font != null) {
						d.value = textBlock.font.size;
					}					
				}
				else if(Data.TYPE_TEXT.equals(name)){
					d.value = textBlock.text;
				} else if(Data.TYPE_COLOR.equals(name)){
					d.value = textBlock.color;
				}
			}
		}
	}
	
	public String getCaptionText(){
		String caption = "";;
		if(data != null){
			for(Data d : data){
				String name = d.name==null?"":d.name;
				if(Data.TYPE_CAPTIONTEXT.equals(name)){
					caption = d.getValue();
				}
			}
		}
		return caption;
	}
	
	public boolean isCaptionable() {
		boolean result = false;
		if (data != null) {
			for(Data d : data){
				String name = d.name==null?"":d.name;
				if(Data.VALUE_TYPE_IS_CAPTIONABLE.equals(name)){
					result = (Boolean) d.getSerializableValue();
					break;
				}
			}
		}
		
		return result;
	}
	
	
}
