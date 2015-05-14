package com.kodak.kodak_kioskconnect_n2r;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.activity.ShoppingCartActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.utils.RSSLocalytics;

public class AlbumSelectionActivity extends Activity
{
	/*private final String TAG = this.getClass().getSimpleName();
	private Button next;
	private Button startOver;
	private Button info;
	private Button settings;
	
	private TextView headerBarText;
	private TextView totalSelectedTV;
	
	private ProgressBar progress;
	ProgressDialog dialog;
	int selectedPosition;
	int width = 0;
	int selectedCount = 0;
	private int image_column_index;
	
	public Activity act = this;
	// Pseudo albums for Android
	private ListView mAlbumList = null;
	AlbumListAdapter mAlbumListAdapter = null;
	// Image Selection Database
	private ImageSelectionDatabase mImageSelectionDatabase = null;
	SharedPreferences prefs;
	long startTime;
	long endTime;
	long sleepTime;
	long adjTime;
	
	String selector;	
	final String[] columns = { MediaColumns.DATA, BaseColumns._ID,ImageColumns.BUCKET_ID,ImageColumns.BUCKET_DISPLAY_NAME };
	final String orderBy = BaseColumns._ID + " desc";
	private final String SCREEN_NAME_PB = "PB Image Source";
	private final String SCREEN_NAME_PRT= "Prt Image Source";
	private final String PHOTOS_SELECTED = "Photos Selected";
	private final String EVENT = "Source Selection";
	private final String YES = "yes";
	
	private HashMap<String, String> attr ;
	
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	private static boolean doneAlbums = false;
	//private boolean doneCreatePB = false;
	
	private Cursor imagecursor;
	
	Thread findAlbums = null;
	
	private InfoDialog.InfoDialogBuilder connectBuilder;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mImageSelectionDatabase = new ImageSelectionDatabase(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.albumselection);
		if (PrintHelper.inQuickbook){
			Localytics.recordLocalyticsPageView(this, SCREEN_NAME_PB);
			attr = new HashMap<String, String>();
			attr.put(PHOTOS_SELECTED, YES);
			Localytics.recordLocalyticsEvents(this, EVENT, attr);
		}else if (PrintHelper.inPrint){
			attr = new HashMap<String, String>();
			Localytics.recordLocalyticsPageView(this, SCREEN_NAME_PRT);	
			attr.put(PHOTOS_SELECTED, YES);
			Localytics.recordLocalyticsEvents(this, EVENT, attr);
		}
		PrintHelper.handleUncaughtException(AlbumSelectionActivity.this,this);
		progress = (ProgressBar) findViewById(R.id.progressBar1);
		// PrintHelper.tracker.trackPageView("/AlbumSelectionActivity");
		mAlbumList = (ListView) findViewById(R.id.albumgridview);
		info = (Button) findViewById(R.id.infoButton);
		Display display = getWindowManager().getDefaultDisplay();
		width = display.getWidth();
		headerBarText = (TextView) findViewById(R.id.headerBarText);
		totalSelectedTV = (TextView) findViewById(R.id.totalSelectedTextView);
		headerBarText.setTypeface(PrintHelper.tf);
		totalSelectedTV.setTypeface(PrintHelper.tfb);
		next = (Button) findViewById(R.id.nextButton);
		startOver = (Button) findViewById(R.id.backButton);
		settings = (Button) findViewById(R.id.settingsButton);
		startOver.setTypeface(PrintHelper.tf);
		next.setTypeface(PrintHelper.tf);
		settings.setVisibility(android.view.View.VISIBLE);
		mAlbumListAdapter = new AlbumListAdapter();
		
		//if(PrintHelper.inQuickbook && PreferenceManager.getDefaultSharedPreferences(this).getString(PrintHelper.sPhotoBookID, "").equals("") && getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getBoolean("isFromMainMenu", false)){
		if(PrintHelper.inQuickbook && AppContext.getApplication().getPhotobook() == null && getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getBoolean("isFromMainMenu", false)){
			
			mAlbumList.setVisibility(View.INVISIBLE);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					doneCreatePB = false;
					PrintMakerWebService mPrintMakerWebService = new PrintMakerWebService(AlbumSelectionActivity.this, "");
					int iCount = 0;
					String sResult = "";
					while (iCount < 5 && sResult.equals(""))
					{
						sResult = mPrintMakerWebService.pbCreatePhotoBook(AlbumSelectionActivity.this);
						iCount++;
					}
					Log.d(TAG, "Done createPhotoBook");
					doneCreatePB = true;
					findAlbumsHandler.sendEmptyMessage(1);
				}
			}).start();
		}else{
			doneCreatePB = true;
		}
		
		if (findAlbums == null)
		{
			findAlbums = new Thread()
			{
				@Override
				public void run()
				{
					doneAlbums = false;
					try
					{
						setupAlbums();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
					doneAlbums = true;
					findAlbumsHandler.sendEmptyMessage(0);
				}
			};
		}
		settings.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent myIntent = null;
				if (!PrintHelper.wififlow)
				{
					myIntent = new Intent(AlbumSelectionActivity.this, NewSettingActivity.class);
					Bundle b = new Bundle();
					String name = "";
					if(PrintHelper.inQuickbook){
						name = SCREEN_NAME_PB;
					} else if (PrintHelper.inPrint){
						name = SCREEN_NAME_PRT;
					}
					if(!name.equals("")){
						b.putString(NewSettingActivity.SETTINGS_LOCATION, name);
					}
					myIntent.putExtras(b);
					startActivity(myIntent);
				}
				else
				{
					myIntent = new Intent(AlbumSelectionActivity.this, WiFiSettingsActivity.class);
					startActivity(myIntent);
				}
			}
		});
		startOver.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				if (!PrintHelper.wififlow)
				{
					if(selectedCount != 0){
						InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(AlbumSelectionActivity.this);
						builder.setTitle("");
						builder.setMessage(getString(R.string.losework));
						builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								mImageSelectionDatabase.handleDeleteAllUrisN2R();
								boolean success = PrintHelper.StartOver();
								if (!success)
								{
									new PrintHelper(getApplicationContext());
								}
								// PrintHelper.items.clear();
								System.gc();
								//This is for QucikBook.
								PrintHelper.hasQuickbook = false;
								Intent intent = null;
								if(PrintHelper.inQuickbook){
									intent = new Intent(AlbumSelectionActivity.this, QuickBookSelectionActivity.class);
								} else {
									intent = new Intent(AlbumSelectionActivity.this, ProductsSelectionActivity.class);
								}
								PrintHelper.inQuickbook = false;
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}
						});
						builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
							}
						});
						builder.create().show();
					}else{
						mImageSelectionDatabase.handleDeleteAllUrisN2R();
						boolean success = PrintHelper.StartOver();
						if (!success)
						{
							new PrintHelper(getApplicationContext());
						}
						// PrintHelper.items.clear();
						System.gc();
						//This is for QucikBook.
						PrintHelper.hasQuickbook = false;
						Intent intent = null;
						if(PrintHelper.inQuickbook){
							intent = new Intent(AlbumSelectionActivity.this, QuickBookSelectionActivity.class);
						} else {
							intent = new Intent(AlbumSelectionActivity.this, ProductsSelectionActivity.class);
						}
						PrintHelper.inQuickbook = false;
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
				}
				else
				{
					if (mImageSelectionDatabase.getSelectedCountWiFi() > 0)
					{
						Intent intent = new Intent(AlbumSelectionActivity.this, ManageTaggedImagesActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					else
					{
						Intent intent = new Intent(AlbumSelectionActivity.this, WiFiSelectWorkflowActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
				}
			}
		});
		next.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (!Connection.isConnected(AlbumSelectionActivity.this) && !PrintHelper.wififlow)
				{
					if(connectBuilder!=null){
						return;
					}
					connectBuilder = new InfoDialog.InfoDialogBuilder(AlbumSelectionActivity.this);
					connectBuilder.setTitle("");
					connectBuilder.setMessage(getString(R.string.nointernetconnection));
					connectBuilder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							connectBuilder = null;
						}
					});
					connectBuilder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							connectBuilder = null;
							next.performClick();
						}
					});
					connectBuilder.setCancelable(false);
					connectBuilder.create().show();
					
					return;
				} 
				if (!PrintHelper.wififlow)
				{
					boolean cantNavigate = true;
					try
					{
						for (Map.Entry<String, String> entry : PrintHelper.selectedHash.entrySet())
						{
							if (entry.getValue().equals("1"))
							{
								cantNavigate = false;
								break;
							}
						}
					}
					catch (Exception ex)
					{
						if (PrintHelper.mLoggingEnabled)
						{
							Log.e(TAG, "Error setting total selected");
							ex.printStackTrace();
						}
					}
					
					SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(AlbumSelectionActivity.this);
					//int iMin = mSharedPreferences.getInt(PrintHelper.sMinNumberOfImages, 0);// - (mSharedPreferences.getString(PrintHelper.sPhotoBookTitleID, "").equals("") ? 0 : 1);
					//int iMax = mSharedPreferences.getInt(PrintHelper.sMaxNumberOfImages, 0);// - (mSharedPreferences.getString(PrintHelper.sPhotoBookTitleID, "").equals("") ? 0 : 1);
					Photobook photobook = AppContext.getApplication().getPhotobook();
					int iMin = photobook==null ? 0 : photobook.minNumberOfImages;
					int iMax = photobook==null ? 0 : photobook.maxNumberOfImages;
					PrintProduct photoBookProduct = null;
					if(PrintHelper.inQuickbook){
						for(PrintProduct product : PrintHelper.products){
							if(product.getId().contains(PrintHelper.PhotoBook) && !product.getId().contains(PrintHelper.AdditionalPage) && product.getId().equals(photobook.proDescId)){
								photoBookProduct = product;
								break;
							}
						}
					}
					//String sSelectRange = iMin + "-" + iMax;
					if(iMin!=0 && iMax!=0 && PrintHelper.inQuickbook && (selectedCount<iMin || selectedCount>iMax))
						cantNavigate = true;
					if (cantNavigate)
					{
						InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(AlbumSelectionActivity.this);
						if(iMin!=0 && iMax!=0 && PrintHelper.inQuickbook && (selectedCount<iMin || selectedCount>iMax)){
							//builder.setTitle(getString(R.string.selected_images_range).replace("%%", sSelectRange));
							builder.setTitle(String.format(getString(R.string.selected_images_range), iMin, iMax, photoBookProduct.getName()));
						}else
							builder.setTitle(getString(R.string.selectatleastoneimage));
						builder.setMessage("");
						builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
							}
						});
						builder.setNegativeButton("", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								finish();
							}
						});
						builder.create().show();
					}
					else
					{
						boolean isDuplex = photobook == null ? false : photobook.isDuplex;
						//if(PrintHelper.inQuickbook && mSharedPreferences.getBoolean(PrintHelper.sIsDuplex, false) && !mSharedPreferences.getBoolean(PrintHelper.HAS_ACCEPT_BLANK_PAGE, false)){
						if(PrintHelper.inQuickbook && isDuplex && !photobook.hasAcceptBlankPage){
							int totalNum = PrintHelper.selectedImageUrls.size();
							if(totalNum%2 != 0){
								next.setEnabled(true);
								showBlankPageWarning();
								return;
							}
						}
						if(PrintHelper.inQuickbook){
							PictureUploadService2.isDoneSelectPics = true;
							Intent myIntent = new Intent(AlbumSelectionActivity.this, QuickBookFlipperActivity.class);
							myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(myIntent);
						} else {
							Intent myIntent = new Intent(AlbumSelectionActivity.this, ShoppingCartActivity.class);
							startActivity(myIntent);
						}
					}
				}
				else
				{
					if (mImageSelectionDatabase == null)
						mImageSelectionDatabase = new ImageSelectionDatabase(AlbumSelectionActivity.this);
					mImageSelectionDatabase.open();
					if (mImageSelectionDatabase.getSelectedCountWiFi() > 0)
					{
						Intent intent = new Intent(AlbumSelectionActivity.this, ManageTaggedImagesActivity.class);
						startActivity(intent);
					}
					else
					{
						InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(AlbumSelectionActivity.this);
						builder.setTitle(getString(R.string.selectatleastoneimage));
						builder.setMessage("");
						builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
							}
						});
						builder.setNegativeButton("", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								finish();
							}
						});
						builder.create().show();
					}
				}
			}
		});
		info.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent myIntent;
				myIntent = new Intent(AlbumSelectionActivity.this, HelpActivity.class);
				Bundle bundle = new Bundle();
				String name = "";
				if(PrintHelper.inQuickbook){
					name = SCREEN_NAME_PB;
				} else if (PrintHelper.inPrint){
					name = SCREEN_NAME_PRT;
				}
				if(!"".equals(name)){
					bundle.putString(HelpActivity.HELP_LOCATION, name);
				}
				myIntent.putExtras(bundle);
				startActivity(myIntent);
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (PrintHelper.mActivities.contains(this)) {
			PrintHelper.mActivities.remove(this);
		}
	}
	private void showBlankPageWarning(){
		if(connectBuilder == null){
			connectBuilder = new InfoDialog.InfoDialogBuilder(AlbumSelectionActivity.this);
		}
		connectBuilder.setTitle("");
		connectBuilder.setMessage(getString(R.string.qb_page_blank_warning));
		connectBuilder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				connectBuilder = null;
				Photobook photobook = AppContext.getApplication().getPhotobook();
				photobook.hasAcceptBlankPage = true;
			}
		});
		connectBuilder.create().show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			if(AppContext.getApplication().getFlowType().isGreetingCardWorkFlow()){
				return false;
			}
			if (!PrintHelper.wififlow)
			{
				if(selectedCount != 0){//
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(AlbumSelectionActivity.this);
					builder.setTitle("");
					builder.setMessage(getString(R.string.losework));
					builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							mImageSelectionDatabase.handleDeleteAllUrisN2R();
							PrintHelper.StartOver();
							System.gc();
							PrintHelper.hasQuickbook = false;
							Intent intent = null;
							if(PrintHelper.inQuickbook){
								intent = new Intent(AlbumSelectionActivity.this, QuickBookSelectionActivity.class);
							} else {
								intent = new Intent(AlbumSelectionActivity.this, ProductsSelectionActivity.class);
							}
							PrintHelper.inQuickbook = false;
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					});
					builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.create().show();
				}else{
					mImageSelectionDatabase.handleDeleteAllUrisN2R();
					PrintHelper.StartOver();
					System.gc();
					PrintHelper.hasQuickbook = false;
					Intent intent = null;
					if(PrintHelper.inQuickbook){
						intent = new Intent(AlbumSelectionActivity.this, QuickBookSelectionActivity.class);
					} else {
						intent = new Intent(AlbumSelectionActivity.this, ProductsSelectionActivity.class);
					}
					PrintHelper.inQuickbook = false;
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			}
			else
			{
				if (mImageSelectionDatabase.getSelectedCountWiFi() > 0)
				{
					Intent intent = new Intent(AlbumSelectionActivity.this, ManageTaggedImagesActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
				else
				{
					Intent intent = new Intent(AlbumSelectionActivity.this, WiFiSelectWorkflowActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			}
		}
		return false;
	}

	public void setupDatabase()
	{
		final String[] columns = { MediaColumns.DATA, BaseColumns._ID };
		final String orderBy = BaseColumns._ID;
		imagecursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
		if (imagecursor != null)
		{
			image_column_index = imagecursor.getColumnIndex(BaseColumns._ID);
			if (PrintHelper.mLoggingEnabled)
				Log.d(TAG, "Count=" + imagecursor.getCount());
			PrintHelper.count = imagecursor.getCount();
			PrintHelper.imageFilePaths = new ArrayList<String>();
			PrintHelper.uriEncodedPaths = new ArrayList<String>();
			PrintHelper.allUriEncodedPaths = new ArrayList<String>();
			int i = 0;
			imagecursor.moveToFirst();
			while (i < imagecursor.getCount())
			{
				int id = imagecursor.getInt(image_column_index);
				Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));
				if (PrintHelper.selectedHash == null)
				{
					PrintHelper.selectedHash = new HashMap<String, String>();
				}
				if (PrintHelper.selectedFileNames == null)
				{
					PrintHelper.selectedFileNames = new HashMap<String, String>();
				}
				PrintHelper.selectedHash.put(uri.toString(), "0");
				int column_index = imagecursor.getColumnIndexOrThrow(MediaColumns.DATA);
				PrintHelper.selectedFileNames.put(uri.toString(), imagecursor.getString(column_index));
				PrintHelper.uriEncodedPaths.add(uri.toString());
				PrintHelper.allUriEncodedPaths.add(uri.toString());
				i++;
				if (PrintHelper.mLoggingEnabled)
					Log.d(TAG, "" + i);
				if (!imagecursor.isClosed())
					imagecursor.moveToNext();
			}
		}
		else
		{
			Log.e(TAG, "imagecursor was null!");
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onResume()
	{
		Localytics.onActivityResume(this);
		PictureUploadService2.isDoneSelectPics = false;
		PictureUploadService2.isDoneUploadThumbnails = false;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
		//if (PrintHelper.selectedHash == null || PrintHelper.mAlbumButton == null && PrintHelper.selectedHash.size() <= 0)
		if (PrintHelper.selectedHash == null || PrintHelper.mAlbumButton == null)
		{
			Log.e(TAG, "ALBUM SELECTION ACTIVITY 05");
			new PrintHelper(getApplicationContext());
			if (PrintHelper.products.size() == 0)
			{
				Thread printPrices = new Thread() {
					@Override
					public void run() {
						try {
							PrintMakerWebService service = new PrintMakerWebService(AlbumSelectionActivity.this, "");
							int count = 0;
							String result = "";
							while (count < 5 && result.equals("")) {
								result = service.getPrintProducts(false,"");
								count++;
							}
							count = 0;
							result = "";
							while (count < 5 && result.equals("")) {
								result = service.GetRequiredContactInformation(AlbumSelectionActivity.this);
								count++;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				printPrices.start();
			}
		}
		
		if(PrintHelper.selectedHash != null && PrintHelper.selectedHash.size() <= 0)
			setupDatabase();
		
		Log.e(TAG, "ALBUM SELECTION ACTIVITY 06");
		for(int i = 0; i < PrintHelper.mAlbumButton.size(); i++){
			Log.e(TAG, "ALBUM SELECTION ACTIVITY 06:i = " + i + ",selected num = " + PrintHelper.mAlbumButton.get(i).selected);
		}
		if (findAlbums != null && !findAlbums.isAlive() && PrintHelper.mAlbumButton.size() == 0)
		{
			settings.setEnabled(false);
			info.setEnabled(false);
			next.setEnabled(false);
			startOver.setEnabled(false);
			findAlbums.start();
		}
		else
		{
			findAlbumsHandler.sendEmptyMessage(0);
		}
		// backCount = 0;
		if (prefs.getBoolean("analytics", false))
		{
			try
			{
				PrintHelper.mTracker.trackPageView("Page-Albums_All");
				PrintHelper.mTracker.dispatch();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		try
		{
			headerBarText.setText(getString(R.string.albums));
			Log.i("quickbook", "inQuickbook:" + PrintHelper.inQuickbook + ", hasQuickbook: " + PrintHelper.hasQuickbook);
			if(PrintHelper.inQuickbook && PrintHelper.hasQuickbook){
				next.setVisibility(View.VISIBLE);
				// because of spec changed, change the text back to next
				next.setText(getString(R.string.next));
				startOver.setVisibility(android.view.View.VISIBLE);
				startOver.setText(getString(R.string.Back));
			} else if (PrintHelper.inQuickbook && !PrintHelper.hasQuickbook){
				next.setVisibility(View.VISIBLE);
				next.setText(getString(R.string.next));
				startOver.setVisibility(android.view.View.VISIBLE);
				startOver.setText(getString(R.string.Back));
			}
			else if (!PrintHelper.wififlow)
			{
				next.setVisibility(android.view.View.VISIBLE);
				next.setText(getString(R.string.cart));
				startOver.setVisibility(android.view.View.VISIBLE);
				startOver.setText(getString(R.string.Back));
			}
			else
			{
				next.setVisibility(android.view.View.VISIBLE);
				next.setText(getString(R.string.selected_set));
				if (PrintHelper.infoEnabled)
				{
					if (Connection.isConnectedWifi(AlbumSelectionActivity.this))
					{
						info.setVisibility(android.view.View.INVISIBLE);
					}
					else
					{
						info.setVisibility(android.view.View.VISIBLE);
					}
				}
				else
				{
					info.setVisibility(View.INVISIBLE);
				}
			}
			if (!Connection.isConnected(AlbumSelectionActivity.this) && !PrintHelper.wififlow)
			{
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(AlbumSelectionActivity.this);
				builder.setTitle("");
				builder.setMessage(getString(R.string.nointernetconnection));
				builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
				builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
				builder.setCancelable(false);
				builder.create().show();
			}
			onResumeCheckConnection();
			super.onResume();
		}
		catch (Exception ex)
		{
		}
		try
		{
			mImageSelectionDatabase.open();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		if (PrintHelper.wififlow)
		{
			if (mImageSelectionDatabase.getSelectedCountWiFi() > 0)
			{
				startOver.setVisibility(android.view.View.INVISIBLE);
			}
			else
			{
				startOver.setVisibility(android.view.View.VISIBLE);
			}
		}
		try
		{
			//If not initialize selectedCount, the number of selected picture will doubled after back from Tagged set
			selectedCount = 0;
			for (Map.Entry<String, String> entry : PrintHelper.selectedHash.entrySet())
			{
				if (entry.getValue().equals("1"))
				{
					selectedCount++;
				}
			}
		}
		catch (Exception ex)
		{
			Log.e(TAG, "Error setting total selected");
			ex.printStackTrace();
		}
		totalSelectedTV.setVisibility(android.view.View.INVISIBLE);
		progress.setVisibility(View.VISIBLE);
		
		if(mAlbumList != null && mAlbumListAdapter!=null){
			for(int i=0; i<PrintHelper.mAlbumButton.size(); i++){
				if(PrintHelper.albumName!=null && PrintHelper.albumName.equals(PrintHelper.mAlbumButton.get(i).albumNameStr)){
					mAlbumList.smoothScrollToPosition(i, 50);
					break;
				}
			}
		}
		
		if(AppContext.getApplication().getFlowType().isGreetingCardWorkFlow()){
			startOver.setVisibility(View.INVISIBLE);
			next.setVisibility(View.INVISIBLE);
			totalSelectedTV.setVisibility(View.INVISIBLE);
		}
		super.onResume();
	}
	
	private void onResumeCheckConnection(){
		if (!Connection.isConnected(AlbumSelectionActivity.this) && !PrintHelper.wififlow)
		{
			if(connectBuilder!=null){
				return;
			}
			connectBuilder = new InfoDialog.InfoDialogBuilder(AlbumSelectionActivity.this);
			connectBuilder.setTitle("");
			connectBuilder.setMessage(getString(R.string.nointernetconnection));
			connectBuilder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					connectBuilder = null;
				}
			});
			connectBuilder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					connectBuilder = null;
					onResumeCheckConnection();
				}
			});
			connectBuilder.setCancelable(false);
			connectBuilder.create().show();
		}
	}

	@Override
	public void onPause()
	{
		Localytics.onActivityPause(this);
		if (findAlbums != null)
			findAlbums.interrupt();
		mAlbumList.setAdapter(null);
		if (PrintHelper.mAlbumButton != null)
		{
			for (int i = 0; i < PrintHelper.mAlbumButton.size(); i++)
			{
				PrintHelper.mAlbumButton.get(i).albumName.setCompoundDrawables(null, null, null, null);
			}
		}
		Log.e(TAG, "ALBUM SELECTION ACTIVITY 04");
		for(int i = 0; i < PrintHelper.mAlbumButton.size(); i++){
			Log.e(TAG, "ALBUM SELECTION ACTIVITY 04:i = " + i + ",selected num = " + PrintHelper.mAlbumButton.get(i).selected);
		}
		mImageSelectionDatabase.close();
		super.onPause();
	}

	@Override
	public void onRestoreInstanceState(Bundle bundle)
	{
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		{
		}
	}

	public String getRealPathFromURI(Uri contentUri)
	{
		String[] proj = { MediaColumns.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public class AlbumListAdapter extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return PrintHelper.mAlbumButton.size();
		}

		@Override
		public Object getItem(int position)
		{
			return position;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			try
			{
				Bitmap img = PrintHelper.loadThumbnailImage(PrintHelper.mAlbumButton.get(position).uri, MediaStore.Images.Thumbnails.MICRO_KIND, null, AlbumSelectionActivity.this);
				if (img != null)
				{
					Drawable d = new BitmapDrawable(img);
					d.setBounds(new Rect(0, 0, 100, 100));
					PrintHelper.mAlbumButton.get(position).targetTex.setCompoundDrawables(d, null, null, null);
					PrintHelper.mAlbumButton.get(position).targetTex.setBackgroundColor(Color.WHITE);
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
			if(PrintHelper.mAlbumButton.get(position).selected.equals("")){
				Log.e(TAG, "selected == \"\"");
			}else if(PrintHelper.mAlbumButton.get(position).selected.equals("0")){
				Log.e(TAG, "selected == \"0\"");
			}
			if (!PrintHelper.mAlbumButton.get(position).selected.equals("") && !PrintHelper.mAlbumButton.get(position).selected.equals("0"))
			{
				PrintHelper.mAlbumButton.get(position).sizeTV.setTextColor(Color.parseColor("#FBBA06"));
				PrintHelper.mAlbumButton.get(position).sizeTV.setText("(" + PrintHelper.mAlbumButton.get(position).selected + "/" + PrintHelper.mAlbumButton.get(position).size + ")");
			}
			else
			{
				PrintHelper.mAlbumButton.get(position).sizeTV.setTextColor(Color.parseColor("#C2C2C2"));
				PrintHelper.mAlbumButton.get(position).sizeTV.setText("(" + PrintHelper.mAlbumButton.get(position).size + ")");
			}
			return PrintHelper.mAlbumButton.get(position);
		}
	}

	private Handler findAlbumsHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			try
			{
				//if(msg.what == 1 && doneAlbums && doneCreatePB){
				if(msg.what == 1 && doneAlbums){
					progress.setVisibility(View.INVISIBLE);
					
					if(PrintHelper.inQuickbook){
						SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(AlbumSelectionActivity.this);
						if(mSharedPreferences.getBoolean("isFirstTimeLaunchAlbum", true)){
							SharedPreferences.Editor editor = mSharedPreferences.edit();
							editor.putBoolean("isFirstTimeLaunchAlbum", false);
							editor.commit();
							
							//int iMin = mSharedPreferences.getInt(PrintHelper.sMinNumberOfImages, 0);// - (mSharedPreferences.getString(PrintHelper.sPhotoBookTitleID, "").equals("") ? 0 : 1);
							//int iMax = mSharedPreferences.getInt(PrintHelper.sMaxNumberOfImages, 0);// - (mSharedPreferences.getString(PrintHelper.sPhotoBookTitleID, "").equals("") ? 0 : 1);
							Photobook photobook = AppContext.getApplication().getPhotobook();
							int iMin = photobook==null ? 0 : photobook.minNumberOfImages;
							int iMax = photobook==null ? 0 : photobook.maxNumberOfImages;
							PrintProduct photoBookProduct = null;
							if(PrintHelper.inQuickbook){
								for(PrintProduct product : PrintHelper.products){
									if(product.getId().contains(PrintHelper.PhotoBook) && !product.getId().contains(PrintHelper.AdditionalPage) && product.getId().equals(photobook.proDescId)){
										photoBookProduct = product;
										break;
									}
								}
							}
							//String sSelectRange = iMin + "-" + iMax;
							
							if(iMin!=0 && iMax!=0){
								InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(AlbumSelectionActivity.this);
								builder.setTitle("");
								//builder.setMessage(getString(R.string.selected_images_range).replace("%%", sSelectRange));
								builder.setTitle(String.format(getString(R.string.selected_images_range), iMin, iMax, photoBookProduct.getName()));
								builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										dialog.dismiss();
									}
								});
								builder.create().show();
							}
							
						}
					}
					mAlbumList.setVisibility(View.VISIBLE);
					return;
				}else if(msg.what == 1){
					mAlbumList.setVisibility(View.VISIBLE);
					return;
				}
				settings.setEnabled(true);
				info.setEnabled(true);
				next.setEnabled(true);
				startOver.setEnabled(true);
				Log.e(TAG, "ALBUM SELECTION ACTIVITY 03");
				for(int i = 0; i < PrintHelper.mAlbumButton.size(); i++){
					Log.e(TAG, "ALBUM SELECTION ACTIVITY 03:i = " + i + ",selected num = " + PrintHelper.mAlbumButton.get(i).selected);
				}
				headerBarText.setText(getString(R.string.albums) + " (" + PrintHelper.mAlbumButton.size() + ")");
				mAlbumList.setAdapter(mAlbumListAdapter);
				mAlbumList.invalidate();
				if(mAlbumList != null && mAlbumListAdapter!=null){
					for(int i=0; i<PrintHelper.mAlbumButton.size(); i++){
						if(PrintHelper.albumName!=null && PrintHelper.albumid == PrintHelper.mAlbumButton.get(i).albumID){
							mAlbumListAdapter.notifyDataSetChanged();
							mAlbumList.setSelection(i);
							break;
						}
					}
				}
				PrintHelper.albumsLoaded = true;
				//if(doneAlbums && doneCreatePB || getApplicationContext().getPackageName().contains("kodakprintmaker"))
				if(doneAlbums || getApplicationContext().getPackageName().contains("kodakprintmaker"))
					progress.setVisibility(View.INVISIBLE);
				if (PrintHelper.mNumberOfAlbums == 0)
				{
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(AlbumSelectionActivity.this);
					builder.setTitle("");
					builder.setMessage(getString(R.string.noimages));
					builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							if (!PrintHelper.wififlow)
							{
								mImageSelectionDatabase.handleDeleteAllUrisN2R();
								PrintHelper.StartOver();
								System.gc();
								PrintHelper.hasQuickbook = false;
								Intent intent = new Intent(AlbumSelectionActivity.this, MainMenu.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								finish();
							}
							else
							{
								PrintHelper.StartOver();
								System.gc();
								Intent intent = new Intent(AlbumSelectionActivity.this, WiFiSelectWorkflowActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								finish();
							}
						}
					});
					builder.setNegativeButton("", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							mImageSelectionDatabase.handleDeleteAllUrisN2R();
							PrintHelper.StartOver();
							System.gc();
							PrintHelper.hasQuickbook = false;
							Intent intent = new Intent(AlbumSelectionActivity.this, MainMenu.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}
					});
					builder.setCancelable(false);
					builder.create().show();
				}
				int total = 0;
				for (int i = 0; i < PrintHelper.mAlbumButton.size(); i++)
				{
					total += Integer.parseInt(PrintHelper.mAlbumButton.get(i).size);
				}
				totalSelectedTV.setText("(" + selectedCount + "/" + total + ")");
				totalSelectedTV.setVisibility(android.view.View.VISIBLE);
				if(AppContext.getApplication().getFlowType().isGreetingCardWorkFlow()){
					startOver.setVisibility(View.INVISIBLE);
					next.setVisibility(View.INVISIBLE);
					totalSelectedTV.setVisibility(View.INVISIBLE);
				}
			}
			catch (Exception ex)
			{
			}
		}
	};

	
	private void setupAlbums(){
		PrintHelper.mNumberOfAlbums = 0;
		if(PrintHelper.mAlbumButton == null){
			PrintHelper.mAlbumButton = new ArrayList<Album>();
		}
		if(PrintHelper.SHOW_PNG){
			selector = ImageColumns.MIME_TYPE + " = " + DatabaseUtils.sqlEscapeString("image/jpeg") + " OR " + ImageColumns.MIME_TYPE + " = " + DatabaseUtils.sqlEscapeString("image/jpg") + " OR " + ImageColumns.MIME_TYPE + " = " + DatabaseUtils.sqlEscapeString("image/png");
		}else{
			selector = ImageColumns.MIME_TYPE + " = " + DatabaseUtils.sqlEscapeString("image/jpeg") + " OR " + ImageColumns.MIME_TYPE + " = " + DatabaseUtils.sqlEscapeString("image/jpg");
		}
		Cursor c = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, selector, null, orderBy);
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){			
			//ignore cache directory
			if(isCacheDir(c.getString(c.getColumnIndex(ImageColumns.DATA)))){
				continue;
			}
			
			int bucketId = c.getInt(c.getColumnIndex(ImageColumns.BUCKET_ID));
			String bucketName = c.getString(c.getColumnIndex(ImageColumns.BUCKET_DISPLAY_NAME));
			
			//check if the album has been added to the PrintHelper.mAlbumButton
			boolean added = false;
			for(Album al : PrintHelper.mAlbumButton){
				if(al.getAlbumID() == bucketId){
					//has been added
					added = true;
					al.size = String.valueOf(Integer.parseInt(al.size)+1);
					break;
				}
			}
			
			//the album hasn't been added, create a new album
			if(!added){
				Album al = new Album(this);
				al.setId(bucketId);
				al.setAlbumID(bucketId);
				al.size = "1";
				al.albumNameStr = bucketName;
				al.albumName.setText(bucketName);
				al.albumName.setTypeface(PrintHelper.tfb);
				al.sizeTV.setTypeface(PrintHelper.tfb);
				
				if (PrintHelper.albumSelected.containsKey(String.valueOf(bucketId)))
					al.selected = PrintHelper.albumSelected.get(String.valueOf(bucketId));
				else
					al.selected = "";
				
				//set thumbnail uri
				int id = c.getInt(c.getColumnIndex(BaseColumns._ID));
				Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));
				al.uri = uri.toString();
				
				PrintHelper.mAlbumButton.add(al);
			}
		}
			
		
		PrintHelper.mNumberOfAlbums = PrintHelper.mAlbumButton.size();
		for(Album al : PrintHelper.mAlbumButton){
			al.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					PrintHelper.albumid = v.getId();
					PrintHelper.albumName = ((Album) v).albumNameStr;
					
					Intent intent = new Intent(AlbumSelectionActivity.this, ImageSelectionActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivity(intent);
				}
			});
			PrintHelper.albums.append("<album><id>" + al.getId() + "</id>" + "<title>" + al.albumNameStr + "</title>" + "<number_of_photos>" + al.size + "</number_of_photos></album>");
		}
		
		if (PrintHelper.mLoggingEnabled){
			Log.d(TAG, "Number of Albums=" + PrintHelper.mNumberOfAlbums);
		}
	}
	
	private static boolean isCacheDir(String path){
		if(path==null){
			return false;
		}
		
		int i = path.lastIndexOf("/");
		if(i==-1){
			return false;
		}
		
		String dir = path.substring(0, i+1);
		return dir.toLowerCase(Locale.ENGLISH).contains("/cache");
	}
*/
}
