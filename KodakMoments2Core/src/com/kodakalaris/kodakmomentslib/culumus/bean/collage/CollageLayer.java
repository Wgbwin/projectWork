package com.kodakalaris.kodakmomentslib.culumus.bean.collage;

import com.kodakalaris.kodakmomentslib.culumus.bean.product.Data;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.Layer;
import com.kodakalaris.kodakmomentslib.culumus.bean.text.TextBlock;

public class CollageLayer extends Layer {
	private static final long serialVersionUID = 1L;
	public int fontSize;
	public String defaultSampleText = null;
	
	public TextBlock getTextBlock() {
		if(TYPE_TEXT_BLOCK.equals(type)){
			TextBlock textBlock = new TextBlock();
			if(data != null){
				for(Data d : data){
					textBlock.id = contentId;
					String name = d.name==null?"":d.name;
					if(Data.TYPE_ALIGNMENT.equals(name)){
						textBlock.alignment = d.value + "";
					}
					else if(Data.TYPE_FONT.equals(name)){
						textBlock.fontName = d.value + "";
					}
					else if(Data.TYPE_JUSTIFICATION.equals(name)){
						textBlock.justification = d.value + "";
					}
					else if(Data.TYPE_LANGUAGE.equals(name)){
						textBlock.language = d.value + "";
					}
					else if(Data.TYPE_FONTPOINTSIZEMIN.equals(name)){
						textBlock.sizeMin = valueOf(d.value + "");
					}
					else if(Data.TYPE_FONTPOINTSIZEMAX.equals(name)){
						textBlock.sizeMax = valueOf(d.value + "");
					}
					else if(Data.TYPE_FONTPOINTSIZEMINMAXUSED.equals(name)){
						textBlock.fontSize = d.value + "";					
					}
					else if(Data.TYPE_DEFAULTTEXT.equals(name)){
						textBlock.defaultText = d.value + "" == null ? "" : d.value + "";
					} 
					else if(Data.TYPE_TEXT.equals(name)){
						textBlock.text = d.value + "";
					} 					
					else if(Data.TYPE_SAMPLETEXT.equals(name)){
						textBlock.sampleText = d.value + "";
					} else if(Data.TYPE_COLOR.equals(name)){
						textBlock.color = d.value + "";
					}else if(Data.TYPE_ISAPPEND.equals(name)){
						textBlock.isAppendable = (Boolean) d.value ;
					}
				}
			}
			if (defaultSampleText != null && !"".equals(defaultSampleText)) {
				textBlock.defaultText = defaultSampleText;
			}else if (textBlock.defaultText == null) {
				textBlock.defaultText = "";
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
			
			//fixed RSSMOBILEPDC-1715 
			if (defaultSampleText == null) {
				String sampleText = "";
				for(Data d : data){
					String name = d.name==null?"":d.name;						
					if(Data.TYPE_SAMPLETEXT.equals(name)){
						sampleText = d.value + "";
						break;
					}		
				}
				defaultSampleText = sampleText;
			}
			
			for(Data d : data){
				textBlock.id = contentId;	
				if (textBlock.font.sizeMinMaxUsed > 0) {
					fontSize = textBlock.font.sizeMinMaxUsed;
				}else {
					fontSize = textBlock.font.size;						
				}
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
						if (textBlock.font.sizeMinMaxUsed > 0) {
							d.value = textBlock.font.sizeMinMaxUsed;
						}else {
							d.value = textBlock.font.size;						
						}
					}					
				}								
				else if(Data.TYPE_FONTPOINTSIZEMINMAXUSED.equals(name)){
					if (textBlock.font != null) {
						if (textBlock.font.sizeMinMaxUsed > 0) {
							d.value = textBlock.font.sizeMinMaxUsed;
						}else {
							d.value = textBlock.font.size;						
						}
					}													
				}			
				else if(Data.TYPE_SAMPLETEXT.equals(name)){
					d.value = textBlock.text;
				}			
				
				else if(Data.TYPE_TEXT.equals(name) ){
					d.value = textBlock.text;
				} 
				
				else if(Data.TYPE_COLOR.equals(name)){
					d.value = textBlock.color;
				}
			}
		}
	}
	
	
	private int valueOf(String value){
		int result = -1;
		if (value == null ) return -1;
		if ("".equals(value)) return -1;
		try {
			if (value.contains(".")) {
				result = Double.valueOf(value).intValue();
			}else {
				result = Integer.valueOf(value);
			}
		} catch (Exception e) {
			result = -1;
		}
		return result;
	}
	
}
