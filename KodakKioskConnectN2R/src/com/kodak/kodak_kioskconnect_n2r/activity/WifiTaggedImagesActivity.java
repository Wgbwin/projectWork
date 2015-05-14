package com.kodak.kodak_kioskconnect_n2r.activity;

import java.util.HashMap;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.AppConstants;
import com.AppConstants.FlowType;
import com.AppConstants.LoadImageType;
import com.AppContext;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.google.zxing.client.android.CaptureActivity;
import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.ImageCheckBoxView;
import com.kodak.kodak_kioskconnect_n2r.ImageSelectionDatabase;
import com.kodak.kodak_kioskconnect_n2r.InfoDialog;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.WiFiConnectionActivity;
import com.kodak.kodak_kioskconnect_n2r.WiFiSelectWorkflowActivity;
import com.kodak.kodak_kioskconnect_n2r.WifiManualInputActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.utils.AsyncTask;
import com.kodak.utils.RSSLocalytics;

public class WifiTaggedImagesActivity extends BaseActivity{
	private GridView gridPhotos;
	private Button btnBack;
	private Button btnNext;
	private ImageButton imgBtnDeselectAll;
	private ImageButton imgBtnAddPhoto;
	private TextView tvTitle;
	private View viewProgerss;
	private PhotosAdpter photosAdpter;
	
	/** if go from photoSelectActivity, this value been inited */
	private AlbumInfo album;
	
	private int mImageThumbSize;
    private int mImageThumbSpacing;
//	private ThumbnailLoader imageLoader;
    private ImageFetcher mImageFetcher;
	private static final String IMAGE_CACHE_DIR = "thumbs";
	private ImageSelectionDatabase db;
	
	public static final String INTENT_KEY_FROM_PHOTOS_SCREEN = "from_photos_screen";
	private final String SCREEN_NAME  = "Wifi Select and Send";
	private final String EVENT_WIFI_SELECT_AND_SEND_SELECTED = "Wifi Select and Send Selected";
	private final String KEY_IMAGES_SENT_TO_KIOSK = "Images Sent to Kiosk";
	private HashMap<String, String> attr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentLayout(R.layout.activity_wifi_tagged_images) ;
		RSSLocalytics.onActivityCreate(this);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		getViews() ;
		initData() ;
		setEvents() ;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		RSSLocalytics.onActivityResume(this);
		List<PhotoInfo> list = AppContext.getApplication().getmTempSelectedPhotos();
		imgBtnDeselectAll.setVisibility(list != null && list.size()>0 ? View.VISIBLE : View.INVISIBLE);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		RSSLocalytics.onActivityPause(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache() ;
		photosAdpter = null;
	}
	
	@Override
	public void getViews() {
		btnNext = (Button) findViewById(R.id.next_btn);
		btnBack = (Button) findViewById(R.id.back_btn);
		tvTitle = (TextView) findViewById(R.id.albums_sum_tex);
		imgBtnDeselectAll = (ImageButton) findViewById(R.id.invert_select_all_imagebtn);
		imgBtnAddPhoto = (ImageButton) findViewById(R.id.add_imagebtn);
		viewProgerss = findViewById(R.id.progressbarLayout);
		gridPhotos = (GridView) findViewById(R.id.picturesGrid);
		
		imgBtnDeselectAll.setVisibility(View.VISIBLE);
		imgBtnAddPhoto.setVisibility(View.VISIBLE);
		
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setTypeface(PrintHelper.tf);
		tvTitle.setText(R.string.selected_pictures);
		
		btnNext.setVisibility(View.VISIBLE);
		btnNext.setTypeface(PrintHelper.tf);
		btnNext.setText(R.string.save);
		
		btnBack.setVisibility(View.GONE);
	}

	@Override
	public void initData() {
		AppContext.getApplication().setFlowType(FlowType.WIFI);
		
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(WifiTaggedImagesActivity.this, IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(WifiTaggedImagesActivity.this, mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.imagewait96x96);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		
        db = new ImageSelectionDatabase(this);
        
        if(getIntent()!= null && getIntent().hasExtra(INTENT_KEY_FROM_PHOTOS_SCREEN)){
        	album = (AlbumInfo) getIntent().getSerializableExtra("album");
        }
        
        gridPhotos.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (photosAdpter != null
								&& photosAdpter.getmNumColumns() == 0) {
							final int numColumns = (int) Math.floor(gridPhotos
									.getWidth()
									/ (mImageThumbSize + mImageThumbSpacing));
							if (numColumns > 0) {
								final int columnWidth = (gridPhotos.getWidth() / numColumns)
										- mImageThumbSpacing;
								photosAdpter.setmNumColumns(numColumns);
								photosAdpter.setmItemHeight(columnWidth);
							}
						}
					}
				});
        
		new InitPhotosTask().execute();
	}

	@Override
	public void setEvents() {		
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(WifiTaggedImagesActivity.this);
				builder.setTitle(getString(R.string.saveTaggedDialogText));
				builder.setMessage("");
				builder.setPositiveButton(getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								if(db.getSelectedCountWiFi()>0){
									Intent myIntent;
									if (Connection.isConnectedKioskWifi(WifiTaggedImagesActivity.this)) {
										myIntent = new Intent(WifiTaggedImagesActivity.this,WiFiConnectionActivity.class);
										startActivity(myIntent);
									} else {
										if (WiFiSelectWorkflowActivity.FindBackCamera()) {
											myIntent = new Intent(WifiTaggedImagesActivity.this,CaptureActivity.class);
											startActivity(myIntent);
										} else {
											myIntent = new Intent(WifiTaggedImagesActivity.this,WifiManualInputActivity.class);
											startActivity(myIntent);
										}
									}
									
									// Localytics track
									int imageNumber = db.getSelectedCountWiFi();
									String trackValue = "0";
									if (imageNumber > 500) {
										trackValue = "501+";
									} else if (imageNumber > 400) {
										trackValue = "401 - 500";
									} else if (imageNumber > 300) {
										trackValue = "301 - 400";
									} else if (imageNumber > 200) {
										trackValue = "201 - 300";
									} else if (imageNumber > 100) {
										trackValue = "101 - 200";
									} else if (imageNumber > 75) {
										trackValue = "076 - 100";
									} else if (imageNumber > 50) {
										trackValue = "051 - 075";
									} else if (imageNumber > 25) {
										trackValue = "026 - 050";
									} else if (imageNumber > 0) {
										trackValue = "001 - 025";
									}
									attr = new HashMap<String, String>();
									attr.put(KEY_IMAGES_SENT_TO_KIOSK, trackValue);
									RSSLocalytics.recordLocalyticsEvents(WifiTaggedImagesActivity.this, EVENT_WIFI_SELECT_AND_SEND_SELECTED, attr);
									AppContext.getApplication().getmTempSelectedPhotos().clear();
								}else{
									InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(WifiTaggedImagesActivity.this);
									builder.setTitle(getString(R.string.selectatleastoneimage));
									builder.setMessage("");
									builder.setPositiveButton(getString(R.string.OK),
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog,	int which) {
													dialog.dismiss();
												}
											});
									builder.create().show();
								}
								
							}
						});
				builder.setNegativeButton(getString(R.string.no),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,	int which) {
								dialog.dismiss();
								Intent intent = new Intent(WifiTaggedImagesActivity.this,WiFiSelectWorkflowActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}
						});
				builder.create().show();
			}
				
		});
		
		imgBtnDeselectAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(WifiTaggedImagesActivity.this);
				builder.setTitle(getString(R.string.removealltaggedset));
				builder.setMessage("");
				builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						dialog.dismiss();
						AppContext.getApplication().getmTempSelectedPhotos().clear();
						db.handleDeleteAllUrisWiFi();
						
						gridPhotos.setAdapter(null);
						Intent intent = new Intent(WifiTaggedImagesActivity.this, WiFiSelectWorkflowActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
				});
				builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
		});
		
		imgBtnAddPhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WifiTaggedImagesActivity.this, PhotoSelectMainFragmentActivity.class);
//				Bundle bundle = new Bundle();
//				bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.PHONE);
//				bundle.putString(AppConstants.KEY_PRODUCT_ID, "") ;
				intent.putExtra(AppConstants.KEY_PRODUCT_ID, "") ;
//				intent.putExtra("bundle", bundle);
				startActivity(intent);
				finish();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			doBack();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	private void doBack(){
		return;
	}
	
	private class PhotosAdpter extends BaseAdapter{
		private List<PhotoInfo> list;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private GridView.LayoutParams mImageViewLayoutParams;
		
		public PhotosAdpter(List<PhotoInfo> list) {
			this.list = list;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public PhotoInfo getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		public int getmNumColumns() {
			return mNumColumns;
		}
		
		public void setmNumColumns(int mNumColumns) {
			this.mNumColumns = mNumColumns;
		}
		
		public void setmItemHeight(int mItemHeight) {
			this.mItemHeight = mItemHeight;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, mItemHeight);
			notifyDataSetChanged() ;
		}

		@Override
		public View getView(final int position, View cv, ViewGroup parent) {
			ImageCheckBoxView icbv;
			if(cv==null){
				icbv = new ImageCheckBoxView(WifiTaggedImagesActivity.this);
				icbv.setBackgroundColor(Color.WHITE);
				cv = icbv;
				cv.setTag(icbv);
			}else{
				icbv = (ImageCheckBoxView) cv.getTag();
			}
			
			icbv.setLayoutParams(mImageViewLayoutParams);
			icbv.setScaleType(ScaleType.CENTER_CROP);
			
			final PhotoInfo info = getItem(position);
//			mImageFetcher.loadImage(info.getPhotoId(), icbv, true) ;
			mImageFetcher.loadImage(info.getPhotoId(),info.getPhotoPath(), icbv, LoadImageType.MEDIA_IMAGE) ;
			
//			imageLoader.loadImage(icbv, Long.parseLong(info.getPhotoId()));
			icbv.setWifiChecked(true);
			
			icbv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view) {
					list.remove(getItem(position));
					db.handleDeleteWifiUri(info.getLocalUri());
					notifyDataSetChanged();
					
					if(list.size() == 0){
						imgBtnDeselectAll.setVisibility(View.INVISIBLE);
					}
				}
			});
			
			return icbv;
		}
		
	}
	
	private class InitPhotosTask extends AsyncTask<Void, Void, List<PhotoInfo>>{

		@Override
		protected void onPreExecute() {
			viewProgerss.setVisibility(View.VISIBLE);
		}
		@Override
		protected List<PhotoInfo> doInBackground(Void... params) {
			List<PhotoInfo> list = db.getTaggedSetPhotos();
			AppContext.getApplication().setmTempSelectedPhotos(list);
			return list;
		}
		
		@Override
		protected void onPostExecute(List<PhotoInfo> result) {
			if(!isFinishing()){
				viewProgerss.setVisibility(View.GONE);
				photosAdpter = new PhotosAdpter(result);
				gridPhotos.setAdapter(photosAdpter);
				gridPhotos.setVisibility(View.VISIBLE);
				
				if(result.size()==0){
					imgBtnDeselectAll.setVisibility(View.INVISIBLE);
				}else{
					imgBtnDeselectAll.setVisibility(View.VISIBLE);
				}
			}
		}
		
	}
	

}
