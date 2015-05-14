package com.kodak.rss.core.n2r.bean.imageedit;

import java.io.Serializable;

public class ColorEffect implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public static String FLAG_AVAILABLE_COLOR_EFFECTS = "AvailableColorEffects";
	public static String FLAG_ID = "ColorEffectID";
	public static String FLAG_NAME = "ColorEffectName";
	public static String FLAG_GLYPH_PATH_URL = "ColorEffectGlyphPathURL";
	
	public int id;
	public String name;
	public String glyphPathUrl;		
	public boolean isChecked;
	
}
