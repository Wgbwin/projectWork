package com.kodakalaris.kodakmomentslib.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OptionsDialogModel implements Serializable {
	
	private static final long serialVersionUID = -5090579697625098850L;
	String title;
	ArrayList<OptionsModel> objectsArraylist;
	int selecterNum;
	public void setObjectsArraylist(List<OptionsModel> optionModelArrayList) {
		this.objectsArraylist = (ArrayList<OptionsModel>) optionModelArrayList;
	}

	public ArrayList<OptionsModel> getObjectsArraylist() {
		return objectsArraylist;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public int getSelecterNum() {
		return selecterNum;
	}

	public void setSelecterNum(int selecterNum) {
		this.selecterNum = selecterNum;
	}

}