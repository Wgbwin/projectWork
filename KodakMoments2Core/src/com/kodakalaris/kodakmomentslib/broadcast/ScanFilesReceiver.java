package com.kodakalaris.kodakmomentslib.broadcast;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;

public class ScanFilesReceiver extends BroadcastReceiver {

	Context mContext = null;
	ScanFilesReceiver receiver;
	Thread thread;
	private AlertDialog.Builder builder = null;
	private AlertDialog ad = null;
	private int count1;
	private int count2;
	private int count;

	public ScanFilesReceiver(Context context) {
		mContext = context;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		scanFiles(intent);
	}

	private void scanFiles(Intent intent) {
		String action = intent.getAction();
		String[] Values = new String[] { MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME };
		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
			Cursor c1 = mContext.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Values, null,
					null, null);
			count1 = c1.getCount();
			System.out.println("count:" + count);
			builder = new AlertDialog.Builder(mContext);
			builder.setMessage("scan the file..." + count1);
			ad = builder.create();
			ad.show();
		} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
			Cursor c2 = mContext.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Values, null,
					null, null);
			count2 = c2.getCount();
			count = count2 - count1;
			ad.cancel();
			if (count >= 0) {
			} else {
			}
		}

	}
}
