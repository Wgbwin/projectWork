package com.kodakalaris.kodakmomentslib.util;

import com.kodakalaris.kodakmomentslib.broadcast.ScanFilesReceiver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

public class SdCardUtil {
	private static AlertDialog.Builder builder = null;
	private static AlertDialog ad = null;

	public static void scanFile(Context context, String filePath) {

		builder = new AlertDialog.Builder(context);
		builder.setMessage("scan the sdcard...");
		ad = builder.create();
		ad.show();
		ad.setCancelable(false);
		MediaScannerConnection.scanFile(context, new String[] { filePath }, null, new MediaScannerConnection.OnScanCompletedListener() {

			@Override
			public void onScanCompleted(String arg0, Uri arg1) {
				ad.cancel();

			}

		});
	}

	public static void scanSdCard(Context context) {

		IntentFilter intentfilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentfilter.addDataScheme("file");
		ScanFilesReceiver scanSdReceiver = new ScanFilesReceiver(context);
		context.registerReceiver(scanSdReceiver, intentfilter);
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath())));
	}

}
