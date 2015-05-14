package com.kodak.kodak_kioskconnect_n2r.greetingcard;

public class GreetingCardTheme {
	
	public static final String CONTENT_SEARCH_STARTER_COLLECTION = "ContentSearchStarterCollection";

	public static final String NAME = "Name";
	public static final String GLYPH_URL = "GlyphURL";
	public static final String SHORT_DESC_TEXT_FILE = "ShortDescTextFile";
	public static final String LONG_DESC_TEXT_FILE = "LongDescTextFile";
	public static final String FILTERS = "Filters";
	public static final String LANGUAGE = "Language";

	public String name;
	public String glyphURL;
	public String shortDescTextFile;
	public String longDescTextFile;
	public String filters;
	public String language;

	@Override
	public String toString() {
		return "GreetingCardTheme[\nName: " + name + "\nGlyphURL: " + glyphURL + "\nShortDescTextFile: " + shortDescTextFile + 
		"\nLongDescTextFile: " + longDescTextFile + "\nFilters: " + filters + "\nLanguage: " + language + "\n]";
	}
	
	

}
