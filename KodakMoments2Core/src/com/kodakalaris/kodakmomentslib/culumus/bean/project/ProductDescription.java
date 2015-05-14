package com.kodakalaris.kodakmomentslib.culumus.bean.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductDescription implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_ProductDescription = "ProductDescription";
	public static final String FLAG_ID = "Id";
	public static final String FLAG_NAME = "Name";
	public static final String FLAG_SHORT_NAME = "ShortName";
	public static final String FLAG_TYPE = "Type";
	public static final String FLAG_PAGE_WIDTH = "PageWidth";
	public static final String FLAG_PAGE_HEIGHT = "PageHeight";
	public static final String FLAG_LG_GLYPH_URL = "LgGlyphURL";
	public static final String FLAG_Sm_GLYPH_URL = "SmGlyphURL";
	public static final String FLAG_ATTIBUTES = "Attributes";
	public static final String FLAG_ATT_NAME = "Name";
	public static final String FLAG_ATT_VALUE = "Value";
	
	public static final String GREETINGCARDS = "Greeting Cards";
	public static final String DUPLEXMYGREETING = "DuplexMyGreeting";
	
	private final String MARKETING = "Marketing";
	private final String MIN_IMAGE_SIZE_LONG_DIM = "MinImageSizeLongDim";
	private final String ProductDeliveryPrompt = "ProductDeliveryPrompt";
	private final String ENABLE_RETAILERS = "EnabledRetailers";
	private final String QUANTITY_INCRMENT = "QuantityIncrement";
	
	public String id = "";
	public String name = "";
	public String shortName = "";
	public String type = "";
	public int pageWidth;
	public int pageHeight;
	public String lgGlyphURL = "";
	public String smGlyphURL = "";
	public Map<String, String> attributes;
	
	public String getMarketing(){
		String result = null;
		if(attributes!=null){
			result = attributes.get(MARKETING);
		}
		return result == null ? "" : result;
	}
	
	public int getMinImageSizeLongDim(){
		int dim = -1;
		String result = null;
		if(attributes!=null){
			result = attributes.get(MIN_IMAGE_SIZE_LONG_DIM);
		}
		if (result == null) return dim;
		try {			
			dim = Integer.valueOf(result);			
		} catch (Exception e) {}				
		return dim;
	}
	
	public int getQuantityIncrement(){
		int quantityIncrement = 1;
		if(attributes != null){
			String result = attributes.get(QUANTITY_INCRMENT);
			if(result != null && "".equals(result)){
				try{
					quantityIncrement = Integer.valueOf(result);
				} catch (NumberFormatException e){
					e.printStackTrace();
				}
			}
		}
		return quantityIncrement;
	}
	
	public String getDelivery(){
		String delivery = null;
		if(attributes != null){
			delivery = attributes.get(ProductDeliveryPrompt);
		}
		return delivery==null ? "" : delivery;
	}
	
	public List<String> getEnableRetailers(){
		String[] retailers = null;
		String strRetailers = null;
		if(attributes != null){
			strRetailers = attributes.get(ENABLE_RETAILERS);
			if(strRetailers != null && !"".equals(strRetailers)){
				retailers = strRetailers.split(",");
			}
		}
		List<String> arrRetailers = new ArrayList<String>();
		if(retailers != null){
			for(String retailer : retailers){
				arrRetailers.add(retailer);
			}
		}
		return arrRetailers;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductDescription other = (ProductDescription) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
