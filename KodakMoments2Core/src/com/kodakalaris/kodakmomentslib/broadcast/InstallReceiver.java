package com.kodakalaris.kodakmomentslib.broadcast;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.localytics.android.ReferralReceiver;
import com.mobileapptracker.Tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// MAT (Mobile App Tracker)
		if(context.getPackageName().equals(AppConstants.PACK_NAME_KODAKMOMENT2)){
			Tracker tracker = new Tracker();
			tracker.onReceive(context, intent);
		}
		
		// Localytics
		ReferralReceiver recevicer = new ReferralReceiver();
		recevicer.onReceive(context, intent);
	}

}
