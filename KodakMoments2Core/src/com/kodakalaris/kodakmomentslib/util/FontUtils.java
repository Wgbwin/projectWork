package com.kodakalaris.kodakmomentslib.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;

public class FontUtils {
	public static final String NAME_LIGHT = "HelveticaNeueLTW1G-Lt.ttf";
	public static final String NAME_REGULAR = "HelveticaNeueLTW1G-Roman.ttf";
	public static final String NAME_MEDIUM = "HelveticaNeueLTW1G-Md.ttf";
	public static final String NAME_THIN = "HelveticaNeueLTW1G-Th.ttf";
	
	private static Map<String, Typeface> sTypeFaces = new HashMap<String, Typeface>();

	public static Typeface getFont(Context context, String name) { 
	    Typeface typeface = sTypeFaces.get(name);
	    if (typeface == null) {
	        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + name);
	        sTypeFaces.put(name, typeface);
	    }
	    return typeface;
	}
}
