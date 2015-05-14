package com.kodak.kodak_kioskconnect_n2r.receiver;

import com.AppConstants;
import com.localytics.android.ReferralReceiver;
import com.mobileapptracker.Tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// MAT (Mobile App Tracker)
		if(AppConstants.KODAK_MY_KODAK_MOMENTS.equals(context.getPackageName())){
			Tracker tracker = new Tracker();
			tracker.onReceive(context, intent);
		}
				
		// Localytics
		ReferralReceiver recevicer = new ReferralReceiver();
		recevicer.onReceive(context, intent);
	}

}
