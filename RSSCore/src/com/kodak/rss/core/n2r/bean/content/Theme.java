package com.kodak.rss.core.n2r.bean.content;

import java.io.Serializable;

public class Theme implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_ThemeResults = "ThemeResults";
	public static final String FLAG_Themes = "Themes";
	
	public static final String FLAG_Id = "Id";
	public static final String FLAG_Name = "Name";
	public static final String FLAG_Glyph = "Glyph";
	public static final String FLAG_Music = "Music";
	public static final String FLAG_Backgrounds = "Backgrounds";
	
	public String id = "";
	public String name = "";
	public String glyph = "";
	public String music = "";
	public BackGround[] backGrounds;
	
	public static class BackGround implements Serializable{
		private static final long serialVersionUID = 1L;
		public static final String FLAG_ImageURL = "ImageURL";
		public static final String FLAG_GlyphURL = "GlyphURL";
		
		public String id = "";
		public String name = "";
		public String imageURL = "";
		public String glyphURL = "";
	}
	
}
