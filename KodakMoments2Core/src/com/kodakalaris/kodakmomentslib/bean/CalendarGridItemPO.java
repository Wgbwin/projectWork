package com.kodakalaris.kodakmomentslib.bean;

import java.io.Serializable;
import java.util.List;

public class CalendarGridItemPO implements Serializable{	
	private static final long serialVersionUID = 1L;
		
	public  int year;
	public int month;
	public int day = -1;
	public int holdIndex = -1;	
	public String textContent;
	public List<ImageInfo> imageInfos;	
	public List<String> contentIds;	
	
}