package com.kodak.rss.tablet.bean;

import java.io.Serializable;

public class PrintEditInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private int btn_id;
	private int txt_id;
	private boolean Enabled;
	private String name;
	
	public PrintEditInfo(int btn_id, int txt_id, boolean enabled,String name) {
		this.btn_id = btn_id;
		this.txt_id = txt_id;
		Enabled = enabled;
		this.name = name;
	}
	
	public int getBtn_id() {
		return btn_id;
	}

	public int getTxt_id() {
		return txt_id;
	}
	
	public void setTxt_id(int id) {
		txt_id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isEnabled() {
		return Enabled;
	}

	public void setEnabled(boolean enabled) {
		Enabled = enabled;
	}
	
}
