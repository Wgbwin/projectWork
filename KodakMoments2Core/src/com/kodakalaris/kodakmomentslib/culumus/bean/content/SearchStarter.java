package com.kodakalaris.kodakmomentslib.culumus.bean.content;

import java.io.Serializable;

public class SearchStarter implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_Name = "Name";
	public static final String FLAG_GlyphURL = "GlyphURL";
	public static final String FLAG_Filters = "Filters";
	public static final String FLAG_Language = "Language";
	public static final String FLAG_RollOnDayOfYear = "RollOnDayOfYear";
	public static final String FLAG_RollOffDayOfYear = "RollOffDayOfYear";
	
	public String name = "";
	public String glyphUrl = "";
	public String filters = "";
	public String language = "";
	public String rollOnDayOfYear = "";
	public String rollOffDayOfYear = "";

}
