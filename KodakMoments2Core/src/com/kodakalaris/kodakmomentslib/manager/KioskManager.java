package com.kodakalaris.kodakmomentslib.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.os.Build.VERSION;
import android.support.annotation.Nullable;

import com.kodakalaris.kodakmomentslib.db.KioskImageSelectionDatabase;
import com.kodakalaris.kodakmomentslib.interfaces.IKM2Manager;
import com.kodakalaris.kodakmomentslib.interfaces.SaveRestoreAble;
import com.kodakalaris.kodakmomentslib.util.ConnectionUtil;
import com.kodakalaris.kodakmomentslib.util.Log;

public class KioskManager implements SaveRestoreAble, IKM2Manager {
	private static final String TAG = "KioskManager";
	private static final String KEY_PREFIX = "KioskManager_";
	
	private static KioskManager sInstance;
	
	public boolean isWifiEnabledBeforeConnectKioskWifi = true;
	public boolean isDisplayAll = true;
	
	
	private KioskManager() {
		
	}
	
	public static KioskManager getInstance() {
		if (sInstance == null) {
			sInstance = new KioskManager();
		}
		
		return sInstance;
	}
	
	/**
	 * Some samsung device can not connect to kiosk wifi if  DisablePoorNetworkAvoidance is enabled.
	 * @return
	 */
	public static boolean isNeedDisablePoorNetworkAvoidance(Context context){
		return VERSION.SDK_INT >= 18 
				&& ConnectionUtil.isPoorNetworkAvoidanceEnabled(context)
				&& "samsung".equalsIgnoreCase(android.os.Build.MANUFACTURER);
	}
	
	public @Nullable ArrayList<String> getSelectedImgUris(Context context) {
		KioskImageSelectionDatabase db = new KioskImageSelectionDatabase(context);
		return db.getTaggedSetURIs();
	}
	
	@Override
	public void saveGlobalVariables(Map<String, Serializable> saveMaps) {
		saveMaps.put(KEY_PREFIX + "isWifiEnabledBeforeConnectKioskWifi", isWifiEnabledBeforeConnectKioskWifi);
		saveMaps.put(KEY_PREFIX + "isDisplayAll", isDisplayAll);
	}
	
	@Override
	public void restoreGlobalVariables(Map<String, Serializable> restoreMaps) {
		try {
			isWifiEnabledBeforeConnectKioskWifi = (Boolean) restoreMaps.get(KEY_PREFIX + "isWifiEnabledBeforeConnectKioskWifi");
		} catch (Exception e) { Log.e(TAG, e); }
		try {
			isDisplayAll = (Boolean) restoreMaps.get(KEY_PREFIX + "isDisplayAll");
		} catch (Exception e) { Log.e(TAG, e); }
	}

	@Override
	public void startOver() {
		isDisplayAll = true;
		isWifiEnabledBeforeConnectKioskWifi = true;
	}
}
