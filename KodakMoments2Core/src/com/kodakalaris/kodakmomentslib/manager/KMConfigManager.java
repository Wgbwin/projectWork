package com.kodakalaris.kodakmomentslib.manager;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.activity.home.MHomeActivity;
import com.kodakalaris.kodakmomentslib.activity.kioskconnection.MKioskConnectionActivity;
import com.kodakalaris.kodakmomentslib.activity.kioskscanconnect.MKioskScanConnectActivity;
import com.kodakalaris.kodakmomentslib.activity.printhubsizeselection.MPrintHubSizeSelectionActivity;
import com.kodakalaris.kodakmomentslib.activity.printsizeselection.MPrintSizeSelectionActivity;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig.Property;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfigEntry;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.Catalog;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.CountryInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.culumus.parse.Parse;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.interfaces.IKM2Manager;
import com.kodakalaris.kodakmomentslib.interfaces.SaveRestoreAble;
import com.kodakalaris.kodakmomentslib.util.FileDownloader;
import com.kodakalaris.kodakmomentslib.util.FileUtil;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;
import com.kodakalaris.kodakmomentslib.util.StringUtils;

public class KMConfigManager implements SaveRestoreAble, IKM2Manager{
	private static final String TAG = "KMConfigManager";
	private static final String KEY_PREFIX = "KMConfigManager_";
	
	private static KMConfigManager sInstance;
	
	private Map<KMConfig.Property, List<KMConfig>> mConfigsMap;
	
	private String mDirectory;
	
	public static KMConfigManager getInstance() {
		if (sInstance == null) {
			sInstance = new KMConfigManager();
		}
		
		return sInstance;
	}
	
	private KMConfigManager() {
		mDirectory = KM2Application.getInstance().getDataFolderPath() + "/.kmconfigs";
		mConfigsMap = new LinkedHashMap<KMConfig.Property, List<KMConfig>>();
		
		FileUtil.createDirectoryIfNotExist(mDirectory);
	}
	
	@Override
	public void startOver() {
		
	}

	@Override
	public void saveGlobalVariables(Map<String, Serializable> saveMaps) {
		saveMaps.put(KEY_PREFIX + "mConfigs", (Serializable) mConfigsMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void restoreGlobalVariables(Map<String, Serializable> restoreMaps) {
		try {
			mConfigsMap = (Map<Property, List<KMConfig>>) restoreMaps.get(KEY_PREFIX + "mConfigsMap");
		} catch (Exception e) { Log.e(TAG, e); }
	}
	
	public void setConfig(KMConfig.Property property, List<KMConfig> configs) {
		mConfigsMap.put(property, configs);
	}
	
	public Map<KMConfig.Property, List<KMConfig>> getAllConfigs() {
		if (mConfigsMap.get(Property.WELCOME_CAROUSEL) == null) {
			mConfigsMap.put(Property.WELCOME_CAROUSEL, getConfigs(Property.WELCOME_CAROUSEL));
		}
		if (mConfigsMap.get(Property.HOME_CAROUSEL) == null) {
			mConfigsMap.put(Property.HOME_CAROUSEL, getConfigs(Property.HOME_CAROUSEL));
		}
		if (mConfigsMap.get(Property.HOME_RIBBON_CAROUSEL) == null) {
			mConfigsMap.put(Property.HOME_RIBBON_CAROUSEL, getConfigs(Property.HOME_RIBBON_CAROUSEL));
		}
		if (mConfigsMap.get(Property.PRINTS_WORKFLOW_CAROUSEL) == null) {
			mConfigsMap.put(Property.PRINTS_WORKFLOW_CAROUSEL, getConfigs(Property.PRINTS_WORKFLOW_CAROUSEL));
		}
		return mConfigsMap;
	}
	
	public List<KMConfig> getConfigs(KMConfig.Property name) {
		List<KMConfig> configs = mConfigsMap.get(name);
		
		if (configs == null) {
			configs = getLocalConfigs(name.contentDataKey);
		}
		
		return configs;
		
	}
	
	/**
	 * @param name
	 * @param needFilter filter by catalog and country info attributes
	 * @return
	 */
	public List<KMConfig> getConfigs(KMConfig.Property name, boolean needFilter) {
		List<KMConfig> configs = getConfigs(name);
		if (!needFilter || configs == null || configs.size() == 0) {
			return configs;
		}
		
		//copy list
		List<KMConfig> newConfigs = new ArrayList<KMConfig>();
		for (KMConfig config : configs) {
			newConfigs.add(config);
		}
		
		CountryInfo countryInfo = KM2Application.getInstance().getCountryInfo();
		List<Catalog> catalogs = KM2Application.getInstance().getCatalogs();
		
		for (KMConfig config : newConfigs) {
			if (config.id.equals(KMConfig.Property.HOME_CAROUSEL.serverId) || config.id.equals(KMConfig.Property.HOME_RIBBON_CAROUSEL.serverId)) {
				for (Iterator<KMConfigEntry> iterator = config.configData.entries.iterator(); iterator.hasNext(); ) {
					KMConfigEntry entry = iterator.next();
					
					if (countryInfo == null) {
						iterator.remove();
						continue;
					}
					
					if (StringUtils.isEmpty(entry.action)) {
						continue;
					} else if (KMConfigEntry.ACTION_KIOSK_CONNECT_WORKFLOW.equals(entry.action) && !countryInfo.hasKiosks()) {
						iterator.remove();
					} else if (KMConfigEntry.ACTION_PRINT_HUB_WORKFLOW.equals(entry.action) && !countryInfo.hasPrintHubs()) {
						iterator.remove();
					} else if (KMConfigEntry.ACTION_PRINTS_WORKFLOW.equals(entry.action) && !countryInfo.hasN2R()) {
						iterator.remove();
					} else {
						if (!isOffLineAction(entry.action) && !isActionInCatalogs(catalogs, entry.action)) {
							iterator.remove();
						}
					}
					
				}
			}
		}
		
		return newConfigs;
	}
	
	private List<KMConfig> getLocalConfigs(String key) {
		String data = SharedPreferrenceUtil.getString(KM2Application.getInstance(), key);
		
		if (!"".equals(data)) {
			try {
				Parse parse = new Parse();
				return parse.parseConfigs(data);
			} catch (WebAPIException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	private String getConfigImageFileName(String url) {
		return url.hashCode() + url.substring(url.lastIndexOf("/") + 1);
	}
	
	public String getConfigImageFilePath(String url) {
		return mDirectory + "/" + getConfigImageFileName(url);
	}
	
	public String getConfigImageFilePath(KMConfigEntry entry) {
		return getConfigImageFilePath(entry.imageUrl);
	}
	
	public Class getActionTargetActivity(String action) {
		if (StringUtils.isEmpty(action)) {
			return null;
		}
		
		//TODO need update for tablet and ...
		boolean isTablet = KM2Application.getInstance().isIsTablet();
		
		if (action.equals(KMConfigEntry.ACTION_GET_STARTED)) {
			return MHomeActivity.class;
		} else if (action.equals(KMConfigEntry.ACTION_KIOSK_CONNECT_WORKFLOW)) {
			return MKioskScanConnectActivity.class;
		} else if (action.equals(KMConfigEntry.ACTION_PRINT_HUB_WORKFLOW)) {
			return MPrintHubSizeSelectionActivity.class;
		} else if (action.equals(KMConfigEntry.ACTION_PRINTS_WORKFLOW)) {
			return MPrintSizeSelectionActivity.class;
		} else if (action.equals(KMConfigEntry.ACTION_WIFI_STORE_FINDER_WORKFLOW)) {
			return null;
		} else if (action.equals(KMConfigEntry.ACTION_NO_ACTION)) {
			return null;
		}
		
		return null;
		
	}
	
	public static boolean isOffLineAction(String action) {
		return KMConfigEntry.ACTION_KIOSK_CONNECT_WORKFLOW.equals(action) 
				|| KMConfigEntry.ACTION_PRINT_HUB_WORKFLOW.equals(action)
				|| KMConfigEntry.ACTION_WIFI_STORE_FINDER_WORKFLOW.equals(action)
				|| KMConfigEntry.ACTION_NO_ACTION.equals(action)
				;
	}
	
	public static boolean isActionInCatalogs(List<Catalog> catalogs, String action) {
		if (catalogs == null) {
			return false;
		}
		
		boolean contains = false;
		for (Catalog catalog : catalogs) {
			for (RssEntry entry : catalog.rssEntries) {
				String type = entry.proDescription.type.toLowerCase();
				String match = null;
				if (action.equals(KMConfigEntry.ACTION_PRINTS_WORKFLOW)) {
					match = "print";
				} 
				//TODO other case for photobook, greetingcard, etc
				
				if (match != null && type.contains(match)) {
					contains = true;
					break;
				}
			}
			
			if (contains) {
				break;
			}
		}
		
		return contains;
	}
	
	public boolean is1stImageDownloaded(Property name) {
		List<KMConfig> configs = getConfigs(name);
		if (!configs.isEmpty() && configs.get(0).configData.entries.size() > 0) {
			String path = getConfigImageFilePath(configs.get(0).configData.entries.get(0));
			return new File(path).exists();
		}
		
		return true;
	}
	
}
