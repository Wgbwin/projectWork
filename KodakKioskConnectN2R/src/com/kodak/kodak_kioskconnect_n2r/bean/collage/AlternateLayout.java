package com.kodak.kodak_kioskconnect_n2r.bean.collage;

import java.util.List;

import com.kodak.kodak_kioskconnect_n2r.ROI;

public class AlternateLayout {
	
	public static final String FLAG_LayoutId = "LayoutId";
	public static final String FLAG_Elements = "Elements";
	
	public String layoutId = "";
	public List<Element> elements;
	private boolean isSelected ;
	
	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public static class Element {
		public static final String FLAG_ContentId = "ContentId";
		public static final String FLAG_Location = "Location";
		
		public static final String LOCATION_X = "X";
		public static final String LOCATION_Y = "Y";
		public static final String LOCATION_W = "W";
		public static final String LOCATION_H = "H";
		public static final String LOCATION_CONTAINER_W = "ContainerW";
		public static final String LOCATION_CONTAINER_H = "ContainerH";
		
		public static final String FLAG_Angle = "Angle";
		
		public String contentId;
		public ROI location;
		public int angle = 0;
	}
}
