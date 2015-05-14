package com.kodak.rss;

import java.util.HashSet;

import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.mobile.R;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * @author Robin
 */
public class RssApp extends Application{
	private static final String TEMP_FOLDER = "/temp/.kodak";
	protected static RssApp app;
	private boolean brandApp = false;
	public boolean appForbidden = false;
	
	/**
	 * Android system will save the wifi you connected, so we need to remove these kiosk wifi added 
	 */
	protected HashSet<Integer> wifiKioskAdded = new HashSet<Integer>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		String packageName = getPackageName();
		if(packageName.contains("com.kodak.rss.mkmhd")){
			brandApp = true;
		} else {
			brandApp = false;
		}
	}
	
	public static RssApp getInstance(){
		return app;
	}
	
	/**
	 * Data folder path.You can override this method as you want.
	 * @return
	 */
	public String getDataFolderPath(){
		return getExternalFilesDir(null).getAbsolutePath();
	}
	
	/**
	 * Temp folder path. You can override this method as you want.
	 * @return
	 */
	public String getTempFolderPath(){
		return getDataFolderPath() + TEMP_FOLDER;
	}
	
	/**
	 * Temp image folder path.  You can override this method as you want.
	 * @return
	 */
	public String getTempImageFolderPath(){
		return getTempFolderPath() + "/.image";
	}
	
	
	/**
	 * It's best to override this method for using your own logic.
	 * @return
	 */
	public String getCountrycodeCurrentUsed(){
		String countryCode = SharedPreferrenceUtil.currentCountryCode(this);
		if("".equals(countryCode)){
			countryCode = SharedPreferrenceUtil.selectedCountryCode(this);
		}
		
		return countryCode;
	}
	
	/**
	 * add wifi network id to set, so we can remove it after disconnect to kiosk
	 * @param id
	 */
	public void addWifiKiosk(int id){
		synchronized (wifiKioskAdded) {
			wifiKioskAdded.add(id);
		}
	}
	
	/**
	 * Android system will save the wifi you connected, so we need to remove these kiosk wifi added 
	 */
	public void removeWifiKioskAdded(){
		if(wifiKioskAdded != null && wifiKioskAdded.size()>0){
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			synchronized (wifiKioskAdded) {
				for(int id : wifiKioskAdded){
					wifiManager.removeNetwork(id);
				}
				wifiKioskAdded.clear();
			}
		}
	}
	
	/**
	 * get size{w,h} for resize
	 * Override this method if you want to do resize
	 * if return null , it mean the image don't need resize
	 * 
	 * add productType and proDescriptionId for minImageSizeLongDim attr by bing wang on 2014-12-23
	 * 
	 * @return
	 */
	public int[] getSizeForResize(int width, int height,String productType, String proDescriptionId){
		return null;
	}

	public boolean isBrandApp() {
		return brandApp;
	}
	
	public String getCulumusServer(){
		String firstName = SharedPreferrenceUtil.getString(this, SharedPreferrenceUtil.BACK_DOOR_NAME);
		if(firstName.equals("RSS_Staging")){
			return "mykodakmomentsstage.kodak.com";
		}else if(firstName.equals("RSS_Production")){
			return "mykodakmoments.kodak.com";
		}else if(firstName.equals("RSS_Development")){
			return "rssdev.kodak.com";
		}else if("RSS_Env1".equalsIgnoreCase(firstName)){
			return "rssdev1.kodak.com";
		}else if("RSS_Env2".equalsIgnoreCase(firstName)){
			return "rssdev2.kodak.com";
		} else {
			return getString(R.string.cumulus_check_internet);
		}
	}
	
}
