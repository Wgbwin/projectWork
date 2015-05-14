package com.kodak.kodak_kioskconnect_n2r;

import java.util.ArrayList;

import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.kodak_kioskconnect_n2r.activity.ShoppingCartActivity;
import com.kodak.utils.RSSLocalytics;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;

public class ImageEditActivity extends Activity
{
	private final String TAG = this.getClass().getSimpleName();
	Gallery mPhotosGallery;
	public ArrayList<String> pictures;
	public Cursor imagecursor;
	public int image_column_index;
	public int count = 0;
	Button startOver;
	ImageView lastImageView = null;
	Button next;
	Button edit;
	QntWidget qnyWidget;
	private final int mSpacing = 20;
	boolean bypass = true;
	private Boolean mLoggingEnabled = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		RSSLocalytics.onActivityCreate(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.imageedit);
		mPhotosGallery = (Gallery) findViewById(R.id.pictures);
		startOver = (Button) findViewById(R.id.backButton);
		next = (Button) findViewById(R.id.nextButton);
		edit = (Button) findViewById(R.id.editBtn);
		qnyWidget = (QntWidget) findViewById(R.id.qnt1);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// String size = prefs.getString("defaultSize", "0");
		// String qnty = prefs.getString("defaultSizeQuantity", "0");
		mLoggingEnabled = prefs.getBoolean("LOGGING_ENABLED_KEY", true);
		qnyWidget.decrease.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				int quantity = Integer.parseInt(qnyWidget.qnty.getText().toString());
				if (quantity == 0)
				{
				}
				else
				{
					quantity--;
					qnyWidget.qnty.setText("" + quantity);
				}
				// ImageView view = (ImageView)
				// mPhotosGallery.getSelectedView();
				// int originalID = view.getId();
			}
		});
		qnyWidget.increase.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				int quantity = Integer.parseInt(qnyWidget.qnty.getText().toString());
				quantity++;
				qnyWidget.qnty.setText("" + quantity);
				// ImageView view = (ImageView)
				// mPhotosGallery.getSelectedView();
				// int originalID = view.getId();
			}
		});
		edit.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//PrintHelper.selectedImage = "";
			//	PrintHelper.selectedImagePos = mPhotosGallery.getSelectedItemPosition();
				if (mLoggingEnabled)
				{
					Log.d(TAG, "Edit button click Selected Image=" + PrintHelper.selectedImage);
				//	Log.d(TAG, "Edit button click Selected Image Position=" + PrintHelper.selectedImagePos);
				}
				Intent intent = new Intent(ImageEditActivity.this, SingleImageEditActivity.class);
				startActivity(intent);
			}
		});
		startOver.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				AlertDialog.Builder dlg = new AlertDialog.Builder(ImageEditActivity.this);
				dlg.setTitle("Are you Sure?");
				dlg.setMessage("You will lose all changes to this order!");
				dlg.setPositiveButton("Start Over", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						RSSLocalytics.recordLocalyticsEvents(ImageEditActivity.this, MainMenu.START_OVER);
						PrintHelper.StartOver();
						System.gc();
						Intent intent = new Intent(ImageEditActivity.this, MainMenu.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
				});
				dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
					}
				});
				dlg.show();
			}
		});
		next.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (bypass)
				{
					Intent intent = new Intent(ImageEditActivity.this, ShoppingCartActivity.class);
					startActivity(intent);
				}
//				else if (PrintHelper.selectedImage4x6.size() == 0 && PrintHelper.selectedImage5x7.size() == 0 && PrintHelper.selectedImage8x10.size() == 0)
//				{
//					AlertDialog.Builder dlg = new AlertDialog.Builder(ImageEditActivity.this);
//					dlg.setTitle("No Sizes Selected!");
//					dlg.setMessage("You need to select a print size for the images you want to order.");
//					dlg.setPositiveButton("OK", new DialogInterface.OnClickListener()
//					{
//						@Override
//						public void onClick(DialogInterface dialog, int which)
//						{
//						}
//					});
//					dlg.show();
//				}
				else
				{
					Log.d(TAG, "Next");
					Intent intent = new Intent(ImageEditActivity.this, ShoppingCartActivity.class);
					startActivity(intent);
				}
			}
		});
		final String[] columns = { MediaColumns.DATA, BaseColumns._ID };
		final String orderBy = BaseColumns._ID;
		imagecursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
		image_column_index = imagecursor.getColumnIndex(BaseColumns._ID);
		this.count = imagecursor.getCount();
		pictures = new ArrayList<String>();
		mPhotosGallery.setAdapter(new ImageAdapter(this));
		mPhotosGallery.setSpacing(mSpacing);
		mPhotosGallery.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View view, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_UP)
				{
					// Gallery gall = (Gallery) view;
					qnyWidget.setEnabled(true);
					// ImageView view2 = (ImageView) gall.getSelectedView();
					// int originalID = view2.getId();
				}
				else if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					qnyWidget.setEnabled(false);
				}
				return false;
			}
		});
		mPhotosGallery.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
		});
		mPhotosGallery.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				ImageCheckBoxView icbv = (ImageCheckBoxView) v;
				if (PrintHelper.thumbnailsselection[(int) id])
				{
					PrintHelper.thumbnailsselection[(int) id] = false;
					icbv.setChecked(false);
				}
				else
				{
					PrintHelper.thumbnailsselection[(int) id] = true;
					icbv.setChecked(true);
				}
				icbv.invalidate();
			}
		});
	}

	@Override
	public void onResume()
	{
		super.onResume();
		RSSLocalytics.onActivityResume(this);
	/*	if (PrintHelper.selectedImagePos > 0)
		{
			mPhotosGallery.setSelection(PrintHelper.selectedImagePos, true);
		}
		else
		{
			mPhotosGallery.setSelection(0, true);
		}*/
	}

	@Override
	public void onPause()
	{
		RSSLocalytics.onActivityPause(this);
		super.onPause();
	}

	public String getPath(Uri uri)
	{
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	class ImageAdapter extends BaseAdapter
	{
		int mGalleryItemBackground;
		private Context mContext;
		ArrayList<String> pictures = new ArrayList<String>();

		public ImageAdapter(Context c)
		{
			mContext = c;
			TypedArray attr = mContext.obtainStyledAttributes(R.styleable.GalleryBackground);
			mGalleryItemBackground = attr.getResourceId(R.styleable.GalleryBackground_android_galleryItemBackground, 0);
			attr.recycle();
		}

		@Override
		public int getCount()
		{
			try
			{
				// return PrintHelper.selectedImageIds.size();
				return PrintHelper.count;
			}
			catch (Exception ex)
			{
			}
			return 0;
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
			ImageCheckBoxView icbv = null;
			if (convertView == null)
			{
				icbv = new ImageCheckBoxView(ImageEditActivity.this);
				convertView = icbv;
				convertView.setTag(icbv);
			}
			else
			{
				convertView.getTag();
			}
			Boolean reachable = imagecursor.moveToPosition(position);
			if (reachable)
			{
				try
				{
					int id = imagecursor.getInt(image_column_index);
					BitmapFactory.Options opt = new BitmapFactory.Options();  
					opt.inPreferredConfig = Bitmap.Config.ALPHA_8;      
					opt.inPurgeable = true;     
					opt.inInputShareable = true;  
					Bitmap img = MediaStore.Images.Thumbnails.getThumbnail(mContext.getApplicationContext().getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, opt);
					if (img != null)
					{
						icbv.setImageBitmap(img);
					}
					else
					{
						Log.e(TAG, "Image was null");
					}
					icbv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					icbv.setBackgroundResource(mGalleryItemBackground);
					icbv.setId(position);
					if (PrintHelper.thumbnailsselection[position])
					{
						icbv.setChecked(true);
					}
				}
				catch (java.lang.Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				Log.e(TAG, "imagecursor.moveToPosition for position " + position + " failed");
			}
			return convertView;
		}
	}
}