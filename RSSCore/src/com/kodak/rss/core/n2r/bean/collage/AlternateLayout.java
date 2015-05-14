package com.kodak.rss.core.n2r.bean.collage;

import java.io.Serializable;
import java.util.List;

public class AlternateLayout implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_LAYOUT_ID = "LayoutId";
	public static final String FLAG_ELEMENTS = "Elements";
	
	public String layoutId;
	public List<Element> elements;
	
	//add judge is or not check
	public boolean isCheck;
	
}
