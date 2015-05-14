package com.kodakalaris.kodakmomentslib.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import android.content.Context;
import android.content.res.Configuration;
import android.telephony.TelephonyManager;

public class DeviceInfoUtil {

	public static int getStatusHeight(Context context) {
		int statusBarHeight = 0;
		try {
			if (isTabletDevice(context)) {
				Class<?> cl = Class.forName("com.android.internal.R$dimen");
				Object obj = cl.newInstance();
				Field field = cl.getField("status_bar_height");
				int x = Integer.parseInt(field.get(obj).toString());
				statusBarHeight = context.getResources().getDimensionPixelSize(x);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return statusBarHeight;
	}

	private static boolean isTabletDevice(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= 11 ) {
			if (android.os.Build.VERSION.SDK_INT >= 16 || android.os.Build.VERSION.SDK_INT == 13) {
				return false;
			}
			Configuration con = context.getResources().getConfiguration();
			try {
				Method mIsLayoutSizeAtLeast = con.getClass().getMethod("isLayoutSizeAtLeast", int.class);
				Boolean r = (Boolean) mIsLayoutSizeAtLeast.invoke(con,0x00000004);
				return r;
			} catch (Exception x) {
				x.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	public static String getDeviceUUID(Context context){
		final String UUID = "uuid";
		String uniqueId = SharedPreferrenceUtil.getString(context, UUID);
		if(uniqueId.equals("")){
			final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);   
			final String tmDevice, tmSerial, androidId;   
			tmDevice = "" + tm.getDeviceId();  
			tmSerial = "" + tm.getSimSerialNumber();   
			androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);   
			UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());   
			uniqueId = deviceUuid.toString();
			SharedPreferrenceUtil.setString(context, UUID, uniqueId);
		}
		return uniqueId;

	}

}
