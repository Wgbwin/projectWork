package com.kodakalaris.kodakmomentslib.culumus.bean.config;

import java.io.Serializable;

public class KMConfigEntry implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_Localized_Action_Text = "LocalizedActionText";
	public static final String FLAG_Action = "Action";
	public static final String FLAG_Localized_Title = "LocalizedTitle";
	public static final String FLAG_Localized_Subtitle = "LocalizedSubtitle";
	public static final String FLAG_ImageURL = "ImageURL";
	
	public static final String ACTION_NO_ACTION = "no-action";
	public static final String ACTION_GET_STARTED = "get-started";
	public static final String ACTION_PRINTS_WORKFLOW = "prints-workflow";
	public static final String ACTION_KIOSK_CONNECT_WORKFLOW = "kiosk-connect-workflow";
	public static final String ACTION_PRINT_HUB_WORKFLOW = "print-hub-workflow";
	public static final String ACTION_WIFI_STORE_FINDER_WORKFLOW = "wifi-store-finder-workflow";
	
	public String actionText = "";
	public String action = "";
	public String title = "";
	public String subtitle = "";
	public String imageUrl = "";
	
}
