package com.kodak.rss.core.n2r.bean.content;

import java.io.Serializable;

public class ServerPhoto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String Photos = "Photos";
	public static final String SourceImageBaseURI = "SourceImageBaseURI";
	public static final String SourceImageId = "SourceImageId";
	/**
	 * As server always returns null, so it is used for now.
	 */
	public static final String ImageHoleIndex = "ImageHoleIndex";
	
	public String sourceImageBaskURI = "";
	public String sourceImageId = "";

}
