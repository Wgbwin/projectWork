package com.kodak.kodak_kioskconnect_n2r;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PictureUploadService extends IntentService
{
	long startTime;
	long endTime;
	String id = "";
	String size = "";
	NotificationManager mManager = null;

	// ImageSelectionDatabase mImageSelectionDatabase;
	// Intent intent = new
	// Intent(getApplicationContext(),ImageSelectionActivity.class);
	public PictureUploadService()
	{
		super("PictureUploadService");
	}

	public PictureUploadService(String name)
	{
		super(name);
	}

	// private static final String TAG = "Picture Upload Service";
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		long time1 = System.currentTimeMillis();
		endTime = (time1 - startTime) / 1000;
	}

	@Override
	public void onStart(Intent intent, int startid)
	{
		super.onStart(intent, startid);
		/*
		 * mManager = (NotificationManager)
		 * getSystemService(Context.NOTIFICATION_SERVICE); Notification
		 * notification = new Notification(R.drawable.mapkiosk, "Notify",
		 * System.currentTimeMillis());
		 * notification.setLatestEventInfo(getBaseContext(),
		 * "Kodak Print Maker - Upload Started","",
		 * PendingIntent.getActivity(getBaseContext(), 0, intent,
		 * PendingIntent.FLAG_CANCEL_CURRENT)); mManager.notify(0,
		 * notification);
		 */
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
	
		mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.notificationupload, "Notify", System.currentTimeMillis());
	
		PrintMakerWebService service = new PrintMakerWebService(this.getApplicationContext(), "");
		startTime = System.currentTimeMillis();
		String filename = intent.getExtras().getString("filename");
		String uri = intent.getExtras().getString("uri");
		// mImageSelectionDatabase = new
		// ImageSelectionDatabase(getApplicationContext());
		// mImageSelectionDatabase.open();
		PrintHelper.currentUploadingFile = filename;
		int count = 0;
		id = "Error";
		while(count < 5 && id.equals("Error"))
		{
			Log.d("PictureUploadService","Uploading file: "+filename+" for the "+count+" time");
			//id = service.UploadPicture(getApplicationContext(),filename,uri,false);
			count++;
			if(count == 5)
			{
				notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), "Problem Uploading Image", PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0));
			}
		}

		if(!id.equals("Error"))
		{
			Log.d("PictureUploadService","Uploaded: "+filename+" id: "+id);
			//PrintHelper.uploadedImageIDs.put(filename, id);
			PrintHelper.uploadQueue.remove(filename);
			//PrintHelper.numUploaded++;
			//notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), "Uploaded " + PrintHelper.uploadedImageIDs.size() + " of " + PrintHelper.uploadedImageIDs.size()+PrintHelper.uploadQueue.size(), PendingIntent.getActivity(getApplicationContext(), 0, new Intent(),
			//		0));
		}	
		else
		{
			Log.d("PictureUploadService","Failed to upload: "+filename);
			PrintHelper.uploadError = true;
		}
		mManager.notify(0, notification);

	}
}