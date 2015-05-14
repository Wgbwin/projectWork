package com.kodakalaris.kodakmomentslib.culumus.bean.config;

import java.io.Serializable;
import java.util.List;

public class KMConfigData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_ID = "ID";
	public static final String FLAG_ENTRIES = "Entries";
	
	public String id;
	public List<KMConfigEntry> entries;
}
