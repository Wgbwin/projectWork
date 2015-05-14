package com.kodakalaris.kodakmomentslib.culumus.bean.text;

import java.io.Serializable;

public class TextBlock implements Serializable {

	public static final String TextBlocks = "TextBlocks";
	public static final String TextBlock = "TextBlock";
	public static final String Id = "Id";
	public static final String Alignment = "Alignment";
	public static final String Color = "Color";
	public static final String Font = "Font";
	public static final String Justification = "Justification";
	public static final String Language = "Language";
	public static final String Opacity = "Opacity";
	public static final String Opacity_Value = "Value";

	public static final String SIZE_MIN = "SizeMin";
	public static final String SIZE_MAX = "SizeMax";
	public static final String TEXT = "Text";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * returned from server. if the get text block from layer, this will be "";
	 */
	public String id = "";
	/**
	 * returned from server.
	 */
	public String alignment = "";
	/**
	 * returned from server.
	 */
	public String color = "";
	/**
	 * returned from server.
	 */
	public Font font;
	/**
	 * returned from server.
	 */
	public String justification = "";
	/**
	 * returned from server.
	 */
	public String language = "";
	/**
	 * returned from server.
	 */
	public int opacity = 1;
	/**
	 * returned from server.
	 */
	public int sizeMin = -1;/**
	 * returned from server.
	 */
	public int sizeMax = -1;
	
	/**
	 * returned from server.
	 */
	public String defaultText = "";

	/**
	 * initial at locale.
	 */
	public String text = "";
	/**
	 * initial at locale.
	 */
	public String fontSize = "";
	/**
	 * initial at locale.
	 */
	public String fontName = "";
	
	public String sampleText = "";
	
	public boolean isAppendable = false ;
	
	public boolean smallTextSize = false;
	
	public int getFontAlignmentIndex(String strAlignment){
		for(int i=0; i<fontAlignments.length; i++){
			if(strAlignment.equalsIgnoreCase(fontAlignments[i])){
				return i;
			}
		}
		return -1;
	}
	
	public int getFontJustificationIndex(String strJustification){
		for(int i=0; i<fontJustifications.length; i++){
			if(strJustification.equalsIgnoreCase(fontJustifications[i])){
				return i;
			}
		}
		return -1;
	}
	
	public String formatText(){
		//These string is put in json, and in json, it will do the format automatic, so we don't need to do the format here
//		if(text.contains("\\")){
//			text = text.replace("\\", "\\\\");
//		}
//		if(text.contains("\"")){
//			text = text.replace("\"", "\\\"");
//		}
//		if(text.contains("\n")){
//			text = text.replace("\n", "\\n");
//		}
//		if(text.contains("\r")){
//			text = text.replace("\r", "\\r");
//		}
		return text;
	}
	
	public static final String[] fontSizes = new String[]{"Auto", "8", "9", "10", "12", "14", "16", "18", "20", "22", "24", "28", "32", "36", "40", "44", "48", "72"};
	public static final String[] fontAlignments = new String[]{"TopLeft", "TopCenter", "TopRight", "CenterLeft", "Centered", "CenterRight", "BottomLeft", "BottomCenter", "BottomRight"};
	public static final String[] fontJustifications = new String[]{"Left", "Center", "Right"};
	public static final String[] fontColors = new String[]{	
		"#ffffffff", "#ffcccccc", "#ffcccccc", "#ff999999", "#ff999999", "#ff666666", "#ff666666", "#ff333333", "#ff000000",
        "#ffccffff", "#ff99ffff", "#ff66ffff", "#ff33ffff", "#ff00ffff", "#ff00cccc", "#ff009999", "#ff006666", "#ff003333",
        "#ffccccff", "#ff99ccff", "#ff66ccff", "#ff33ccff", "#ff00ccff", "#ff00cccc", "#ff009999", "#ff006666", "#ff003333",
        "#ffccccff", "#ff9999ff", "#ff6699ff", "#ff3399ff", "#ff0099ff", "#ff0099cc", "#ff009999", "#ff006666", "#ff003333",
        "#ffccccff", "#ff9999ff", "#ff6666ff", "#ff3366ff", "#ff0066ff", "#ff0066cc", "#ff006699", "#ff006666", "#ff003333",
        "#ffccccff", "#ff9999ff", "#ff6666ff", "#ff3333ff", "#ff0033ff", "#ff0033cc", "#ff003399", "#ff003366", "#ff003333",
        "#ffccccff", "#ff9999ff", "#ff6666ff", "#ff3333ff", "#ff0000ff", "#ff0000cc", "#ff000099", "#ff000066", "#ff000033",
        "#ffccccff", "#ff9999ff", "#ff6666ff", "#ff3333ff", "#ff3300ff", "#ff3300cc", "#ff330099", "#ff330066", "#ff330033",
        "#ffccccff", "#ff9999ff", "#ff6666ff", "#ff6633ff", "#ff6600ff", "#ff6600cc", "#ff660099", "#ff660066", "#ff330033",
        "#ffccccff", "#ff9999ff", "#ff9966ff", "#ff9933ff", "#ff9900ff", "#ff9900cc", "#ff990099", "#ff660066", "#ff330033",
        "#ffccccff", "#ffccccff", "#ffcc99ff", "#ffcc66ff", "#ffcc33ff", "#ffcc00cc", "#ff990099", "#ff660066", "#ff330033",
        "#ffffccff", "#ffff99ff", "#ffff66ff", "#ffff33ff", "#ffff00ff", "#ffcc00cc", "#ff990099", "#ff660066", "#ff330033",
        "#ffffcccc", "#ffff99cc", "#ffff66cc", "#ffff33cc", "#ffff00cc", "#ffcc00cc", "#ff990099", "#ff660066", "#ff330033",
        "#ffffcccc", "#ffff9999", "#ffff6699", "#ffff3399", "#ffff0099", "#ffcc0099", "#ff990099", "#ff660066", "#ff330033",
        "#ffffcccc", "#ffff9999", "#ffff6666", "#ffff3366", "#ffff0066", "#ffcc0066", "#ff990066", "#ff660066", "#ff330033",
        "#ffffcccc", "#ffff9999", "#ffff6666", "#ffff3333", "#ffff0033", "#ffcc0033", "#ff990033", "#ff660033", "#ff330033",
        "#ffffcccc", "#ffff9999", "#ffff6666", "#ffff3333", "#ffff0000", "#ffcc0000", "#ff990000", "#ff660000", "#ff330000",
        "#ffffcccc", "#ffff9999", "#ffff6666", "#ffff3333", "#ffff3300", "#ffcc3300", "#ff993300", "#ff663300", "#ff333300",
        "#ffffcccc", "#ffff9999", "#ffff6666", "#ffff6633", "#ffff6600", "#ffcc6600", "#ff996600", "#ff666600", "#ff333300",
        "#ffffcccc", "#ffff9999", "#ffff9966", "#ffff9933", "#ffff9900", "#ffcc9900", "#ff999900", "#ff666600", "#ff333300",
        "#ffffcccc", "#ffffcc99", "#ffffcc66", "#ffffcc33", "#ffffcc00", "#ffcccc00", "#ff999900", "#ff666600", "#ff333300",
        "#ffffffcc", "#ffffff99", "#ffffff66", "#ffffff33", "#ffffff00", "#ffcccc00", "#ff999900", "#ff666600", "#ff333300",
        "#ffccffcc", "#ffccff99", "#ffccff66", "#ffccff33", "#ffccff00", "#ffcccc00", "#ff999900", "#ff666600", "#ff333300",
        "#ffccffcc", "#ff99ff99", "#ff99ff66", "#ff99ff33", "#ff99ff00", "#ff99cc00", "#ff999900", "#ff666600", "#ff333300",
        "#ffccffcc", "#ff99ff99", "#ff66ff66", "#ff66ff33", "#ff66ff00", "#ff66cc00", "#ff669900", "#ff666600", "#ff333300",
        "#ffccffcc", "#ff99ff99", "#ff66ff66", "#ff33ff33", "#ff33ff00", "#ff33cc00", "#ff339900", "#ff336600", "#ff003300",
        "#ffccffcc", "#ff99ff99", "#ff66ff66", "#ff33ff33", "#ff00ff00", "#ff00cc00", "#ff009900", "#ff006600", "#ff003333",
        "#ffccffcc", "#ff99ff99", "#ff66ff66", "#ff33ff33", "#ff00ff33", "#ff00cc33", "#ff009933", "#ff006633", "#ff003333",
        "#ffccffcc", "#ff99ff99", "#ff66ff66", "#ff33ff66", "#ff00ff66", "#ff00cc66", "#ff009966", "#ff006666", "#ff003333",
        "#ffccffcc", "#ff99ff99", "#ff66ff99", "#ff33ff99", "#ff00ff99", "#ff00cc99", "#ff009999", "#ff006666", "#ff003333",
        "#ffccffcc", "#ff99ffcc", "#ff66ffcc", "#ff33ffcc", "#ff00ffcc", "#ff00cccc", "#ff009999", "#ff006666", "#ff333333"
};
}
