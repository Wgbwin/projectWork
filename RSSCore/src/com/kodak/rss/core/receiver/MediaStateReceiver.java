package com.kodak.rss.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kodak.rss.core.util.Log;

public class MediaStateReceiver extends BroadcastReceiver {
	private static final String tag = MediaStateReceiver.class.getSimpleName();
	public static boolean isMediaMounted = false;
	
	public static final int MEDIA_EJECTED = 7001;
	public static final int MEDIA_MOUNTED = 7002;
	public static final int MEDIA_SCANNED = 7003;
	
	public static int MEDIA_STATE = MEDIA_SCANNED;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
	    Log.i(tag, action);
	    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
	    	MEDIA_STATE = MEDIA_EJECTED;
	    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
	    	MEDIA_STATE = MEDIA_MOUNTED;
		} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
			MEDIA_STATE = MEDIA_SCANNED;
		}
	    Log.i(tag, MEDIA_STATE + "");
	}

}