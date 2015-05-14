package com.kodakalaris.kodakmomentslib.broadcast;

import com.kodakalaris.kodakmomentslib.KM2Application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class UsbReceiver extends BroadcastReceiver {

	Context mContext = null;
	UsbReceiver receiver;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		checkSDCard(intent);

	}

	private void checkSDCard(Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
			KM2Application.getInstance().setScanSDCard(true);
			Toast.makeText(mContext, "away sdcard", Toast.LENGTH_SHORT).show();
		} else if (action.equals(Intent.ACTION_MEDIA_CHECKING)) {
			KM2Application.getInstance().setScanSDCard(true);
			Toast.makeText(mContext, "input sdcard", Toast.LENGTH_SHORT).show();
		}

	}
}
