package com.kodakalaris.kodakmomentslib.bean;
import java.io.Serializable;

public class OptionsModel implements Serializable {
	
	private static final long serialVersionUID = 3968542009279541559L;
	boolean isSelected = false;
	String textValue;

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	public String getTextValue() {
		return textValue;
	}
}
