package com.kodak.rss.core.n2r.bean.collage;

import java.io.Serializable;

import com.kodak.rss.core.bean.ROI;

public class Element implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_CONTENT_ID = "ContentId";
	public static final String FLAG_LOCATION = "Location";
	public static final String FLAG_ANGLE = "Angle";
	
	public static final String FLAG_LOCATION_X = "X";
	public static final String FLAG_LOCATION_Y = "Y";
	public static final String FLAG_LOCATION_W = "W";
	public static final String FLAG_LOCATION_H = "H";
	public static final String FLAG_LOCATION_CONTAINER_W = "ContainerW";
	public static final String FLAG_LOCATION_CONTAINER_H = "ContainerH";

	public String contentId;
	public ROI location;
	public int angle;	
	
}
