package com.kodakalaris.kodakmomentslib.culumus.bean.config;

import java.io.Serializable;

import android.content.Context;

import com.kodakalaris.kodakmomentslib.DataKey;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;

public class KMConfig implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_CONFIG = "Config";
	public static final String FLAG_CONFIG_ID = "ConfigID";
	public static final String FLAG_CONFIG_DATA = "ConfigData";
	
	public String id;
	public KMConfigData configData;
	
	/**
	 * Local info
	 */
	public static enum Property {
		WELCOME_CAROUSEL(
				KM2Application.getInstance().getString(R.string.cumulus_welcome_carousel),
				DataKey.WELCOME_CAROUSEL_CONFIGS_ETAG,
				DataKey.WELCOME_CAROUSEL_CONFIGS
				),
		HOME_CAROUSEL(
				KM2Application.getInstance().getString(R.string.cumulus_home_carousel),
				DataKey.HOME_CAROUSEL_CONFIGS_ETAG,
				DataKey.HOME_CAROUSEL_CONFIGS
				),
		HOME_RIBBON_CAROUSEL(
				KM2Application.getInstance().getString(R.string.cumulus_homeRibbon_carousel),
				DataKey.HOME_RIBBON_CAROUSEL_CONFIGS_ETAG,
				DataKey.HOME_RIBBON_CAROUSEL_CONFIGS
				),
		PRINTS_WORKFLOW_CAROUSEL(
				KM2Application.getInstance().getString(R.string.cumulus_printsWorkflow_carousel),
				DataKey.PRINT_WORKFLOW_CAROUSEL_CONFIGS_ETAG,
				DataKey.PRINT_WORKFLOW_CAROUSEL_CONFIGS
				);
		
		public String serverId;
		public String eTagDataKey;
		public String contentDataKey;
		
		private Property(String serverId, String eTagDataKey, String contentDataKey) {
			this.serverId = serverId;
			this.eTagDataKey = eTagDataKey;
			this.contentDataKey = contentDataKey;
		}
	}
	
	public static void clearProperties(Context context){
		SharedPreferrenceUtil.setString(context, DataKey.WELCOME_CAROUSEL_CONFIGS_ETAG, "");
		SharedPreferrenceUtil.setString(context, DataKey.WELCOME_CAROUSEL_CONFIGS, "");
		
		SharedPreferrenceUtil.setString(context, DataKey.HOME_CAROUSEL_CONFIGS_ETAG, "");
		SharedPreferrenceUtil.setString(context, DataKey.HOME_CAROUSEL_CONFIGS, "");
		
		SharedPreferrenceUtil.setString(context, DataKey.HOME_RIBBON_CAROUSEL_CONFIGS_ETAG, "");
		SharedPreferrenceUtil.setString(context, DataKey.HOME_RIBBON_CAROUSEL_CONFIGS, "");
		
		SharedPreferrenceUtil.setString(context, DataKey.PRINT_WORKFLOW_CAROUSEL_CONFIGS_ETAG, "");
		SharedPreferrenceUtil.setString(context, DataKey.PRINT_WORKFLOW_CAROUSEL_CONFIGS, "");
	}
	
}
