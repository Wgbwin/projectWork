package com.kodak.kodak_kioskconnect_n2r;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.AppConstants;
import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardPage;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardPageLayer;
import com.kodak.utils.ImageUtil;
import com.kodak.utils.RSSLocalytics;
import com.kodak.utils.PhotobookUtil;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SendingOrderActivity extends Activity
{
	protected static final String TAG = null;
	// private final String TAG = this.getClass().getSimpleName();
	ProgressBar dialog;
	int increment;
	boolean nav = true;
	boolean success = true;
	Button next;
	Button back;
	Button information;
	Button settings;
	TextView headerBarText;
	TextView progressDescriptionTextView;
	TextView changeStore;
	ImageView pictureUploading;
	Thread setupCart;
	Thread createPrints;
	ImageSelectionDatabase mImageSelectionDatabase = null;
	SharedPreferences prefs;
	private final String SCREEN_NAME = "Order Start";
	private final String EVENT_ORDER_START = "Order Start";
	private final String EVENT_CREATION_SUMMARY = "Creation Summary";
	private final String EVENT_ORDER_FAILED = "Order Not Completed";
	private final  String SCREEN_NAME_ORDER_FAIL = "Order Not Completed";
	private final String LOCAL_PHOTOS_USED = "Local Photos Used";
	private final String KEY_FACEBOOK_USED = "Facebook Used";
	
	private final String YES = "yes";
	private final String NO = "no";
	private final String UNCLASSFIELD_ERROR= "Unclassified Error";
	private final String ORDER_FAIL_REASON = "Order Not Completed Reason";
	
	private HashMap<String, String> attr;
	private HashMap<String, String> attrOrderFailed;
	

	@Override
	public void onResume()
	{
		RSSLocalytics.onActivityResume(this);
		super.onResume();
		try
		{
			if (prefs.getBoolean("analytics", false))
			{
				if (PrintHelper.wififlow)
				{
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "Wifi_At_Kiosk", 3);
				}
				else
				{
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "Prints_To_Store", 3);
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		try
		{
			if (mImageSelectionDatabase == null)
				mImageSelectionDatabase = new ImageSelectionDatabase(this);
				mImageSelectionDatabase.open();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		try
		{
			scaleImage();
		}
		catch (OutOfMemoryError oome)
		{
			oome.printStackTrace();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		AppContext app = AppContext.getApplication();
		int max = app.getUploadSucceedImages().size() + app.getmUploadPhotoList().size();
		dialog.setMax(max);
		if (app.getmUploadPhotoList().size() == 0)
		{
			if (nav)
			{
				nav = false;
				Intent intent = new Intent(SendingOrderActivity.this, SendingOrderActivity2.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		}
		else
		{
			
			setupCart = new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						
						while (dialog != null && dialog.getProgress() < dialog.getMax() && !PrintHelper.uploadError) {
							Thread.sleep(500);
							progressHandler.sendMessage(progressHandler.obtainMessage());
						}
						progressHandler.sendEmptyMessage(-2);
					}
					catch (InterruptedException ex)
					{
					}
				}
			};
			setupCart.start();
		}
		if (prefs.getBoolean("analytics", false))
		{
			try
			{
				PrintHelper.mTracker.trackPageView("Page-Sending_Photos");
				PrintHelper.mTracker.dispatch();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		nav = true;
	}

	@Override
	public void onPause()
	{
		RSSLocalytics.onActivityPause(this);
		super.onPause();
		try
		{
			mImageSelectionDatabase.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		try
		{
			if (setupCart.isAlive())
				setupCart.interrupt();
		}
		catch (Exception ex)
		{
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(TAG,"send order activity On create");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.sendingorder);
		RSSLocalytics.onActivityCreate(this);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		RSSLocalytics.recordLocalyticsEvents(this, EVENT_ORDER_START);
		localyticsTrackCreationSummary();
		
		back = (Button) findViewById(R.id.backButton);
		next = (Button) findViewById(R.id.nextButton);
		settings = (Button) findViewById(R.id.setupButton);
		changeStore = (TextView) findViewById(R.id.storeTV);
		information = (Button) findViewById(R.id.infoButton);
		dialog = (ProgressBar) findViewById(R.id.progressBar);
		headerBarText = (TextView) findViewById(R.id.headerBarText);
		progressDescriptionTextView = (TextView) findViewById(R.id.progressDescription);
		pictureUploading = (ImageView) findViewById(R.id.pictureUploading);
		changeStore.setVisibility(View.VISIBLE);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		changeStore.setText(prefs.getString("selectedStoreName", ""));
		information.setVisibility(View.GONE);
		back.setVisibility(View.INVISIBLE);
		next.setVisibility(View.INVISIBLE);
		settings.setVisibility(View.GONE);
		headerBarText.setText(getString(R.string.sendingOrder));
		headerBarText.setTypeface(PrintHelper.tf);
		back.setTypeface(PrintHelper.tf);
		next.setTypeface(PrintHelper.tf);
		changeStore.setTypeface(PrintHelper.tf);
		progressDescriptionTextView.setTypeface(PrintHelper.tf);
		Log.i(TAG,"send order activity On create end");
	}
	
	private void localyticsTrackCreationSummary(){
		attr = new HashMap<String, String>();
		attr.put(LOCAL_PHOTOS_USED, NO);
		attr.put(KEY_FACEBOOK_USED, NO);
		List<ProductInfo> products = AppContext.getApplication().getProductInfos();
		if(products!=null){
			for(ProductInfo pro : products){
				if(pro.productType.equalsIgnoreCase(AppConstants.PRINT_TYPE)){
					updateLocalyticsSourceState(attr, pro.photoInfo);
				} else if(pro.productType.equalsIgnoreCase(AppConstants.BOOK_TYPE)){
					Photobook photobook = PhotobookUtil.getPhotobookFromList(pro.ProductId);
					if(photobook != null && photobook.selectedImages!=null){
						for(PhotoInfo photo : photobook.selectedImages){
							updateLocalyticsSourceState(attr, photo);
						}
					}
				} else if(pro.productType.equalsIgnoreCase(AppConstants.CARD_TYPE)){
					GreetingCardManager manager = getGreetingCardManager(pro.ProductId);
					GreetingCardPage[] pages = manager.getGreetingCardProduct().pages;
					GreetingCardPageLayer[] layers = null;
					for (int i =0;i<pages.length;i++){
						layers = pages[i].layers;
						for (int j=0;j<layers.length;j++){
							PhotoInfo photo = layers[j].getPhotoInfo();
							if(photo!=null){
								updateLocalyticsSourceState(attr, photo);
							}
						}
					}
				}
			}
		}
		
		RSSLocalytics.recordLocalyticsEvents(this, EVENT_CREATION_SUMMARY, attr);
	}
	
	private GreetingCardManager getGreetingCardManager (String productId){
		GreetingCardManager manager = null;
		for (GreetingCardManager managersTemp : AppContext.getApplication().getmGreetingCardManagers()){
			if (null == managersTemp.getGreetingCardProductCardProduct() || null == managersTemp.getGreetingCardProductCardProduct().id){
				continue;
			}
			if (managersTemp.getGreetingCardProductCardProduct().id.equals(productId)){
				manager = managersTemp;
				break;
			}	
		}
		
		return manager;
	}
	
	private void updateLocalyticsSourceState(HashMap<String, String> attr, PhotoInfo photo){
		if(photo.getPhotoSource()!=null){
			if(photo.getPhotoSource().isFromFaceBook()){
				attr.put(KEY_FACEBOOK_USED, YES);
			} else if(photo.getPhotoSource().isFromPhone()){
				attr.put(LOCAL_PHOTOS_USED, YES);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			nav = false;
		}
		return nav;
	}

	private void scaleImage()
	{
		String uri = null;
		if ( AppContext.getApplication().getmUploadPhotoList().size() == 0) {
			return;
		}
		else {
			uri = AppContext.getApplication().getmUploadPhotoList().get(0).getLocalUri();
			boolean isSecondUpload = uri.startsWith(PictureUploadService2.FIRST_UPLOAD_THUMBNAILS);
			uri = (isSecondUpload ? uri.substring(PictureUploadService2.FIRST_UPLOAD_THUMBNAILS.length()) : uri);
		}
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(PrintHelper.selectedFileNames.get(uri), o);
		o.inSampleSize = o.outWidth>o.outHeight ? o.outWidth/400 : o.outHeight/400;
		o.inJustDecodeBounds = false;
		Bitmap bit = null;
		Bitmap rotated = null;
		try
		{
			String fileName = AppContext.getApplication().getmUploadPhotoList().get(0).getPhotoPath();
			PhotoInfo photoInfo = AppContext.getApplication().getmUploadPhotoList().get(0);
			if (fileName.toUpperCase().endsWith(".PNG")){ //add by song for png file
				String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER;
				String cFilePath = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length())+ "newLC" + ".jpg";
				if(new File(cFilePath).exists()){
					bit = BitmapFactory.decodeStream(new FileInputStream(cFilePath), null, o);							
				}else {
					bit  = PrintHelper.loadThumbnailImage(uri.toString(),
							MediaStore.Images.Thumbnails.MICRO_KIND, null, SendingOrderActivity.this);
				}
			}else {
				//bit = BitmapFactory.decodeStream(new FileInputStream(fileName), null, o);
				bit = ImageUtil.getBitmapOfPhotoInfo(photoInfo, SendingOrderActivity.this);
			}
			if(photoInfo.getPhotoSource().isFromPhone()){
				ExifInterface exif = null;
				try {
					exif = new ExifInterface(photoInfo.getPhotoPath());
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				if (exif != null && exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90) {
					Matrix matrix = new Matrix();
					matrix.postRotate(90);
					rotated = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
				}
				else if (exif != null && exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270){
					Matrix matrix = new Matrix();
					matrix.postRotate(270);
					rotated = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
				}
				if (rotated != null) {
					bit = null;
					bit = rotated;
				}
			}
			
		}
		catch (OutOfMemoryError oome)
		{
			oome.printStackTrace();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		boolean failure = false;
		final float scale = getBaseContext().getResources().getDisplayMetrics().density;
		try
		{
			int scaleFactor = 1;
			if (bit.getHeight() > (100 * scale))
			{
				scaleFactor = (int) Math.ceil((bit.getHeight() / (100 * scale)));
			}
			Log.d(TAG, "original dimensions: " + bit.getWidth() + "x" + bit.getHeight());
			Log.d(TAG, "Scaled Dimensions: " + bit.getWidth() / scaleFactor + "x" + bit.getHeight() / scaleFactor);
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bit, (int) ((bit.getWidth() / scaleFactor) * scale), (int) ((bit.getHeight() / scaleFactor) * scale), true);
			pictureUploading.setImageBitmap(scaledBitmap);
		}
		catch (OutOfMemoryError oome)
		{
			oome.printStackTrace();
		}
		catch (Exception ex)
		{
			failure = true;
		}
		if (failure)
			pictureUploading.setImageBitmap(bit);
	}

	private Handler progressHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (PrintHelper.uploadError)
			{	
				RSSLocalytics.recordLocalyticsPageView(SendingOrderActivity.this, SCREEN_NAME_ORDER_FAIL);
				attrOrderFailed = new HashMap<String, String>();
				attrOrderFailed.put(ORDER_FAIL_REASON, UNCLASSFIELD_ERROR);
				RSSLocalytics.recordLocalyticsEvents(SendingOrderActivity.this, EVENT_ORDER_FAILED, attrOrderFailed);
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(SendingOrderActivity.this);
				builder.setTitle(R.string.n2r_upload_order_fail);
				builder.setMessage("");
				builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						PrintHelper.uploadError = false;
						finish();
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						PrintHelper.uploadError = false;
					}
				});
				builder.setCancelable(false);
				builder.create().show();
			}
			else
			{
				if (msg.what == -2)
				{
					try
					{
						double totalTime = 0.0;
						double totalSize = 0.0;
						for (int i = 0; i < PrintHelper.uploadTimes.size(); i++)
						{
							totalTime += PrintHelper.uploadTimes.get(i);
						}
						for (int j = 0; j < PrintHelper.uploadFileSize.size(); j++)
						{
							totalSize += PrintHelper.uploadFileSize.get(j);
						}
						int time = Integer.parseInt("" + totalTime);
						if (prefs.getBoolean("analytics", false))
						{
							try
							{
								PrintHelper.mTracker.trackEvent("Order", "Upload_Time", "" + totalSize, time);
								PrintHelper.mTracker.dispatch();
							}
							catch (Exception ex)
							{
								ex.printStackTrace();
							}
						}
					}
					catch (Exception ex)
					{
					}
					Intent myIntent = new Intent(SendingOrderActivity.this, SendingOrderActivity2.class);
					startActivity(myIntent);
					finish();
				}
				else
				{
					// progressDescriptionTextView.setText("Uploading Picture "+PrintHelper.numUploaded+" of "+PrintHelper.numToUpload+" Prints: "+PrintHelper.numPrintsCreated);
					int uploadedFiles = AppContext.getApplication().getUploadSucceedImages().size();
					if (uploadedFiles != dialog.getProgress())
					{
						try
						{
							scaleImage();
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
					dialog.setProgress(uploadedFiles);
					String progressDescMsg;
					if ((uploadedFiles + 1) > dialog.getMax())
					{
					    progressDescMsg = String.format(getString(R.string.order_sending_progress), uploadedFiles, dialog.getMax());
					}
					else
					{
					    progressDescMsg = String.format(getString(R.string.order_sending_progress), (uploadedFiles + 1), dialog.getMax());
					}
					progressDescriptionTextView.setText(progressDescMsg);
					if (dialog.getProgress() >= dialog.getMax())
					{
						progressDescriptionTextView.setText(getString(R.string.order_upload_complete));
					}
				}
			}
		}
	};
}
