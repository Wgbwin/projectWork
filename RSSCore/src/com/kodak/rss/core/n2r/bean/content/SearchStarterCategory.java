package com.kodak.rss.core.n2r.bean.content;

import java.io.Serializable;
import java.util.List;

public class SearchStarterCategory implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_ContentSearchStarterCategories = "ContentSearchStarterCategories";
	public static final String FLAG_Name = "Name";
	public static final String FLAG_SearchStarters = "SearchStarters";
	
	public String name = "";
	public List<SearchStarter> searchStarters;
	
}
