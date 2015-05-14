package com.kodak.kodak_kioskconnect_n2r.bean.text;

import java.io.Serializable;

public class Font implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String FLAG_Fonts = "Fonts";
	public static final String FLAG_Name = "Name";
	public static final String FLAG_DisplayName = "DisplayName";
	public static final String FLAG_SampleURL = "SampleURL";
	public static final String FLAG_Size = "Size";
	public static final String FLAG_SizeMin = "SizeMin";
	public static final String FLAG_SizeMax = "SizeMax";
	public static final String FLAG_SizeMinMaxUsed = "SizeMinMaxUsed";
	
	public String name = "";
	public String displayName = "";
	public String sampleURL = "";
	public int size;
	public int sizeMin = -1;
	public int sizeMax = -1;
	
	public int sizeMinMaxUsed = -1;
}
