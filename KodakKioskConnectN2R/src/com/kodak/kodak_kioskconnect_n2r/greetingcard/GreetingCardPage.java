package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import android.util.Log;

public class GreetingCardPage implements Cloneable{
	private final String TAG = GreetingCardPage.class.getSimpleName();
	
	public static final String BASE_URI = "BaseURI";
	public static final String ID = "Id";
	public static final String SEQUENCE_NUMBER = "SequenceNumber";
	public static final String PAGE_TYPE = "PageType";
	public static final String LAYOUT_TYPE = "LayoutType";
	public static final String WIDTH = "Width";
	public static final String HEIGHT = "Height";
	public static final String MIN_NUM_OF_IMAGES = "MinNumberOfImages";
	public static final String MAX_NUM_OF_IMAGES = "MaxNumberOfImages";
	public static final String LAYERS = "Layers";
	public static final String MARGIN = "Margin";
	
	public static final String MARGIN_TOP = "Top";
	public static final String MARGIN_LEFT = "Left";
	public static final String MARGIN_BOTTOM = "Bottom";
	public static final String MARGIN_RIGHT = "Right";
	
	public String baseURI = "";
	public String id = "";
	public int sequenceNumber;
	public String pageType = "";
	public String layoutType = "";
	public float width;
	public float height;
	public int minNumberOfImages;
	public int maxNumberOfImages;
	public GreetingCardPageLayer[] layers;
	
	public String imageURI="";
	public String imageContentId = "";
	public int imageRotateDegree = 0;
	
	private int pageWidth = 0;
	private int pageHeight = 0;
	private boolean needDownloadPre = false;
	
	/**
	 * margin
	 * 		{top, left, bottom, right}
	 */
	public float[] margin = new float[4];
	
	public GreetingCardPage(){
		
	}
	
	public GreetingCardPageLayer getLayer(String contentId){
		for(GreetingCardPageLayer layer : layers){
			   if(!layer.contentId.equals("") && layer.contentId.equals(contentId)){
			    return layer;
			   }
			  }
			  return null;
	}
	
	public float getMarginTop(){
		return margin[0];
	}
	
	public float getMarginLeft(){
		return margin[1];
	}
	
	public float getMarginBottom(){
		return margin[2];
	}
	
	public float getMarginRight(){
		return margin[3];
	}

	public boolean isNeedDownloadPre() {
		return needDownloadPre;
	}
	public void setNeedDownloadPre(boolean needDownloadPre) {
		this.needDownloadPre = needDownloadPre;
	}
	public int getPageWidth() {
		return pageWidth;
	}

	public void setPageWidth(int pageWidth) {
		this.pageWidth = pageWidth;
	}

	public int getPageHeight() {
		return pageHeight;
	}

	public void setPageHeight(int pageHeight) {
		this.pageHeight = pageHeight;
	}

	@Override
	public String toString() {
		String toString = "page:[id:" + id + ", sequenceNumber:" + sequenceNumber + ", pageType:" + pageType + ", layoutType:" + layoutType + ", width:"
							+ width + ", height:" + height + ", minNumberOfImages:" + minNumberOfImages + ", maxNumberOfImages:" + maxNumberOfImages;
		if(layers!=null){
			toString += "\n layers[";
			for(GreetingCardPageLayer layer : layers){
				toString += layer.toString();
			}
			toString += "]\n";
		} else {
			toString += "\n layers:null";
		}
		toString += ", imageURI:" + imageURI + ", imageContentId" + imageContentId.toString();
		toString += ", margin{top:" + margin[0] + ", left:" + margin[1] + ", bottom:" + margin[2] + ", right:" + margin[3] + "}";
		toString += "]";
		return toString;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		GreetingCardPage clone = (GreetingCardPage) super.clone();
		if(layers!=null){
			clone.layers = layers.clone();
			/*Log.i(TAG, "clone layers length " + clone.layers.length);
			for(GreetingCardPageLayer layer : clone.layers){
				Log.i(TAG, "layer:" + layer.toString());
			}*/
		}
		return clone;
	}

}
