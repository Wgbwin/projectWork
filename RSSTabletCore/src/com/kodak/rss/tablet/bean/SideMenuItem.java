package com.kodak.rss.tablet.bean;

public class SideMenuItem {
	public static final int ITEM_HOME = 1;
	public static final int ITEM_TIPS = 2;
	public static final int ITEM_SETTINGS = 3;
	public static final int ITEM_INFO = 4;
	public static final int ITEM_FACEBOOK = 5;
	
	private int id;
	private int imageResId;
	private String text;
	
	public SideMenuItem(int id, int imageResId, String text) {
		this.id = id;
		this.imageResId = imageResId;
		this.text = text;
	}

	public int getId() {
		return id;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public int getImageResId() {
		return imageResId;
	}
}
