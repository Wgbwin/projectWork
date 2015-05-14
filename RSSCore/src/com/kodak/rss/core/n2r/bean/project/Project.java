package com.kodak.rss.core.n2r.bean.project;

import java.io.Serializable;

public class Project implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String SearchResults = "SearchResults";
	public static final String Projects = "Projects";
	public static final String Project = "Project";
	public static final String Id = "Id";
	public static final String Type = "Type";
	public static final String ProductDescriptionId = "ProductDescriptionId";
	public static final String ProductDescriptionIdLocalized = "ProductDescriptionIdLocalized";
	public static final String ProjectName = "ProjectName";
	public static final String GlyphURL = "GlyphURL";
	public static final String ApplicationName = "ApplicationName";
	public static final String CreationDate = "CreationDate";
	public static final String ModifiedDate = "ModifiedDate";
	public static final String ExpirationDate = "ExpirationDate";
	public static final String Public = "Public";
	
	public String id = "";
	public String type = "";
	public String productDescriptionId = "";
	public String productDescriptionIdLocalized = "";
	public String projectName = "";
	public String glyphURL = "";
	public String applicationName = "";
	public String creationDate = "";
	public String modifiedDate = "";
	public String expirationDate = "";
	public boolean isPublic = false;

}
