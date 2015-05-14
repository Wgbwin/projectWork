package com.kodakalaris.video.activities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants;
import com.AppConstants.PhotoSource;
import com.AppContext;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumHolder;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.view.TabIndicator;
import com.kodak.kodak_kioskconnect_n2r.view.TabIndicator.TabView;
import com.kodak.utils.RSSLocalytics;
import com.kodakalaris.video.MediaStoreUtils;
import com.kodakalaris.video.SquareImageViewClickListener;
import com.kodakalaris.video.adapters.GridPreviewAdapter;
import com.kodakalaris.video.fragments.ICommunicatingForTMS;
import com.kodakalaris.video.fragments.TMSAlbumSelectFragment;
import com.kodakalaris.video.fragments.TMSPhotoSelectFragment;
import com.kodakalaris.video.roifacedetect.FaceDetectorTask;
import com.kodakalaris.video.roifacedetect.FaceDetectorThread;
import com.kodakalaris.video.roifacedetect.FaceDetectorThreadPool;
import com.kodakalaris.video.roifacedetect.FinishedFindingFacesEvent;
import com.kodakalaris.video.storydoc_format.VideoGenParams;
import com.kodakalaris.video.storydoc_format.VideoGenParams.Vignette;
import com.kodakalaris.video.views.SquareImageView;
import com.kodakalaris.video.views.TMSImageCheckBoxView;

public class TMSSelectPhotosActivity extends BaseActivity  implements SquareImageViewClickListener,ICommunicatingForTMS {
	protected static final String TAG = TMSSelectPhotosActivity.class.getSimpleName();
	private static final String SHOW_SELECT_PHOTO_HELP = "SHOW_SELECT_PHOTO_HELP";
	private static final String SHOW_REMOVE_PHOTO_HELP = "SHOW_REMOVE_PHOTO_HELP";
	
	private ViewGroup mRootView ; 
	List<SquareImageView> mSelectedImagesViews;
	private List<View> mSelectedImagesRemoveBtns;
	private Map<Integer, PhotoInfo> mSelectedPhotoMap = new HashMap<Integer, PhotoInfo>();
	private TabIndicator vTabIndicator ;
	private ViewPager vPreViewViewPager ;
	
	private LinearLayout vHelpLayout;
	private CheckBox vHelpCheckBox;
	private Button vHelpSetButton;
	private TextView vHelpText1, vHelpText2;
	private ImageButton vDelImgButton;
	private View vDoneButton;
	
	private List<AlbumInfo> mAlbums;
	private AlbumInfo mCameraAlbum ;
	public static final int QUERY_TOKEN = 34;
	private List<PhotoInfo> mAllPhotosInPhone; // all photos in local
	private FaceDetectorTask mFaceDetectorTask;
	
	//added by robin
	//when add image, image need to do face detect, if we put it in Main thread, it may take a long time
	//So I put it in another thread
	private FaceDetectorThreadPool mFaceDetectorThreadPool;
	
	private AsyncTask<Void, Void, Void> mImageFilePersistor;
	
	private boolean mAreImagesDragable = true;
	
	private ProgressDialog vProgressDialog;
	private QueryPhotoHandler mQueryPhotoHandler;
	
	private RelativeLayout mAnimLayer;
	
	
	
	private ImageFetcher mImageFetcher;
	private static final String IMAGE_CACHE_DIR = "preview_image";
	public static final String TMS_CREATE_SELECT_3_PHOTOS="TMS Create - Select 3 Photos";
	public static final String TMS_ADD_IMAGE = "TMS Add Image";
	private static HashMap<String, String>attr=new HashMap<String, String>();
	public static final String DURING_CREATE="During Create";
	public static boolean IsEditActivity=false;
	public static final String DURING_EDIT="During Edit";
	public static final String TMS_EDIT_SELECT_3_PHOTOS="TMS Edit - Select 3 Photos";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentLayout(R.layout.activity_tms_select_photos);
		Intent intent=getIntent();
		IsEditActivity=intent.getBooleanExtra("TMS_EDIT", false);
		if(IsEditActivity){
			RSSLocalytics.recordLocalyticsPageView(this, TMS_EDIT_SELECT_3_PHOTOS);
		}else{
			RSSLocalytics.recordLocalyticsPageView(this, TMS_CREATE_SELECT_3_PHOTOS);}
		getViews();
		initData();
		setEvents();
		mQueryPhotoHandler = new QueryPhotoHandler(this);
		startQueryPhotoPhone() ;
	}

	private void getViews() {
		mRootView = (ViewGroup) findViewById(R.id.select_photos_root_container) ;
		vTabIndicator = (TabIndicator) findViewById(R.id.tab_indicator);
		vPreViewViewPager = (ViewPager) findViewById(R.id.select_photos_preview_view_pager) ;
		vDoneButton = findViewById(R.id.select_photos_done_button);
		
		mSelectedImagesViews = new ArrayList<SquareImageView>();
		mSelectedImagesViews.add((SquareImageView) findViewById(R.id.three_across_top_1));
		mSelectedImagesViews.add((SquareImageView) findViewById(R.id.three_across_top_2));
		mSelectedImagesViews.add((SquareImageView) findViewById(R.id.three_across_top_3));
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			// Log.e(TAG,"Index: "+i);
			SquareImageView v = mSelectedImagesViews.get(i);
			v.setImagePosition(i);
			
		}
		
		mSelectedImagesRemoveBtns = new ArrayList<View>();
		mSelectedImagesRemoveBtns.add(findViewById(R.id.three_across_top_remove_1));
		mSelectedImagesRemoveBtns.add(findViewById(R.id.three_across_top_remove_2));
		mSelectedImagesRemoveBtns.add(findViewById(R.id.three_across_top_remove_3));
		
		vHelpLayout = (LinearLayout) findViewById(R.id.select_photo_help_tips);
		vHelpLayout.setVisibility(isNeedShowSelectTips() ? View.VISIBLE : View.GONE);
		vHelpCheckBox = (CheckBox) findViewById(R.id.select_photo_dont_show_help_again);
		vHelpSetButton = (Button) findViewById(R.id.select_photo_help_set_button);
		vDelImgButton = (ImageButton) findViewById(R.id.TMS_select_photos_help_dialog_imgBtn);
		vHelpText1 = (TextView) findViewById(R.id.TMS_select_photos_help_dialog_tex1);
		vHelpText2 = (TextView) findViewById(R.id.TMS_select_photos_help_dialog_tex2);
		headerBar_tex.setText(getString(R.string.TMS_select_photos_title));
		
		mAnimLayer = (RelativeLayout) findViewById(R.id.select_photo_anim_layer);
		
		
		
	}

	private void initData() {
		
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(this, 800);
		mImageFetcher.setLoadingImage(R.drawable.imagewait96x96);
		mImageFetcher.addImageCache(this.getSupportFragmentManager(), cacheParams);
	}

	private void setEvents() {
		vHelpCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				String buttonString = (String) vHelpSetButton.getText();
				if (buttonString.equals(getString(R.string.TMS_select_photos_help_dialog_got_it))) {
					boolean isNeedHelp = !vHelpCheckBox.isChecked();
					PreferenceManager.getDefaultSharedPreferences(TMSSelectPhotosActivity.this).edit().putBoolean(SHOW_SELECT_PHOTO_HELP, isNeedHelp).commit();
					vHelpLayout.setVisibility(View.GONE);
				}

			}
		});
		vTabIndicator.setOnTabSelectListener(new TabListener());
		
		for (int i = 0; i < mSelectedImagesRemoveBtns.size(); i++) {
			final int index = i;
			mSelectedImagesRemoveBtns.get(index).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					deselectPhoto(index);
					updatePhotoAdapter() ;
				}
			});
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			String imageFilePath = mVideoGenParams.mVignettes.get(i).mImagePath;
			// Log.e(TAG, "Restoreing image:" + imageFilePath);
			mSelectedImagesViews.get(i).setImageBitmapAndFilePath(imageFilePath);
			mSelectedImagesRemoveBtns.get(i).setVisibility(imageFilePath == null || "".equals(imageFilePath) ? View.GONE : View.VISIBLE);
		}
		// onImageDrop(null, null, true, false);
		updateDoneButton();
		
		mImageFetcher.setExitTasksEarly(false);
	}
	
	@Override
	protected void onPause() {
		// for (int i = 0; i < mSelectedImagesViews.size(); i++) {
		// String imageFilePath = mSelectedImagesViews.get(i).getFilePath();
		// Log.e(TAG, "Persisting image:" + imageFilePath);
		// mVideoGenParams.mVignettes.get(i).mImagePath = imageFilePath;
		// }
		if (mFaceDetectorTask != null) {
			try {
				mFaceDetectorTask.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		mFaceDetectorTask = null;
		
		if (mFaceDetectorThreadPool != null && mFaceDetectorThreadPool.isAlive()) {
			try {
				mFaceDetectorThreadPool.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mFaceDetectorThreadPool = null;
		super.onPause();
		if(mVideoGenParams.validate() == VideoGenParams.INVALID_NO_IMAGE_SELECTED){
			VideoGenParams.deleteProject(this, mVideoGenParams.mUUID.toString());
			finish();
		} else {
			persistImages(false);
			try {
				mImageFilePersistor.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			mImageFilePersistor = null;
		}
		
		
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mImageFetcher.closeCache();
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ArrayList<String> selectedImagesPath = new ArrayList<String>();
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			selectedImagesPath.add(mSelectedImagesViews.get(i).getFilePath());
		}
		if (mSelectedImagesViews.size() == 0) {
			Log.e(TAG, "Strange Error2");

		}
		if (selectedImagesPath.size() == 0) {
			Log.e(TAG, "Strange Error3");

		}
		super.onSaveInstanceState(outState);

	}
	
	private void persistImages(final boolean wasSwap) {

		if (mImageFilePersistor != null) {
			try {
				mImageFilePersistor.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mImageFilePersistor = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				mAreImagesDragable = false;
				super.onPreExecute();
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				mAreImagesDragable = true;
				updateSelectImageRemobeBtns();
			}

			@Override
			protected Void doInBackground(Void... params) {
				persistFiles(new FilePersisterHelper() {
					@Override
					public String getCurrentPath(int i) {
						// Log.e(TAG, "Current:" +
						// mSelectedImagesViews.get(i).getFilePath());
						return mSelectedImagesViews.get(i).getFilePath();
					}

					@Override
					public String getOldPath(Vignette vig) {
						// Log.e(TAG, "Old:" + vig.mImagePath);
						return vig.mImagePath;
					}

					@Override
					public void setNewPath(String path, Vignette vig) {
						vig.mImagePath = path;
						SquareImageView view = mSelectedImagesViews.get(vig.mIndex);
						//if the file path in view is empty, it means the image has been deselect.
						if (view.getFilePath() != null && !view.getFilePath().equals("")) {
							view.setFilePath(path);
						}
						
						
					}

					@Override
					public String getFilePrefix() {
						return "image";
					}

					@Override
					public boolean compressFilesAsBitmaps() {
						return !wasSwap;
					}

					@Override
					public boolean areFilesinBitmapCache() {
						return true;
					}
				});
				return null;
			}
		};
		mImageFilePersistor.execute();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState == null) {
		} else {
			for (int i = 0; i < mSelectedImagesViews.size() && i < mVideoGenParams.mVignettes.size(); i++) {
				VideoGenParams.Vignette vig = mVideoGenParams.mVignettes.get(i);
				mSelectedImagesViews.get(i).setImageBitmapAndFilePath(vig.mImagePath);
			}
			updateDoneButton();
		}

	}
	
	
	@Override
	public void onBackPressed() {
		
		if(vPreViewViewPager!=null && vPreViewViewPager.getVisibility()==View.VISIBLE){
			vPreViewViewPager.setAdapter(null) ;
			vPreViewViewPager.setVisibility(View.INVISIBLE) ;
			
			
		}else {
			
			FragmentManager fm = getSupportFragmentManager() ;
			int fragmentInStackCount = fm.getBackStackEntryCount() ;
			if(fragmentInStackCount > 0){
			
				super.onBackPressed();
			}else {
				finish();
			}
			
		}
		
		
		
		
		
		
		
	}
	
	/**
	 * Since we can swap image, this logic is more confusing. We can't just
	 * overwrite the old image 1 with the new image 1 because the new image 2
	 * might have old image 1 as its source.... This also means we can't delete
	 * any images until all of them have been saved to a temporary file. This
	 * logic must work with any number of images and NEVER delete a users image.
	 **/
	public interface FilePersisterHelper {
		public String getCurrentPath(int i);

		public String getOldPath(Vignette vig);

		public void setNewPath(String path, Vignette vig);

		public String getFilePrefix();

		public boolean compressFilesAsBitmaps();

		public boolean areFilesinBitmapCache();

	}
	
	public void onDoneButton(View v) {
		Intent intent = new Intent(this, AddAudioActivity.class);
		persistImages(false);
		Bundle options = new Bundle();
		// options.putStringArrayList(AddAudioActivity.PARAM_SELECTED_IMAGES,
		// selectedImagesPath);
		options.putString(INSTANCE_STATE_KEY_VIDEO_PARAMATERS, mVideoGenParams.mUUID.toString());
		intent.putExtras(options);
		intent.putExtra("TMS_EDIT", IsEditActivity);
		startActivity(intent);
	}
	
	
	/**
	 * query local photos
	 */
	private void startQueryPhotoPhone() {

		if (vProgressDialog == null) {
			vProgressDialog = new ProgressDialog(this);
			vProgressDialog.setCancelable(true);
			vProgressDialog.show();
		} else {
			if (!vProgressDialog.isShowing()) {
				vProgressDialog.show();
			}
		}
		mQueryPhotoHandler.cancelOperation(QUERY_TOKEN);
		mQueryPhotoHandler.startQuery(QUERY_TOKEN, null, Images.Media.EXTERNAL_CONTENT_URI, null, Images.Media.DATA + "  not like ?  and "
				+ Images.Media.MIME_TYPE + " in (\'image/jpeg\',\'image/jpg\',\'image/png\') "+" and "+ Images.Media.SIZE +" > 0 ", new String[] { "%cache%" }, Images.Media._ID
				+ " DESC");
	}
	
	
	
	
	/**
	 * get all photos in the phone
	 * 
	 * @author sunny
	 * 
	 */
	private final class QueryPhotoHandler extends AsyncQueryHandler {
		private final WeakReference<TMSSelectPhotosActivity> mActivity;

		public QueryPhotoHandler(Context context) {
			super(context.getContentResolver());
			mActivity = new WeakReference<TMSSelectPhotosActivity>((TMSSelectPhotosActivity) context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
			final TMSSelectPhotosActivity activity = mActivity.get();
			if (activity != null && !activity.isFinishing()) {
				if (cursor != null) {
					if (mAllPhotosInPhone != null) {
						mAllPhotosInPhone.clear();
						mAllPhotosInPhone = null;
					}

					if (mAlbums != null) {
						mAlbums.clear();
						mAlbums = null;
					}
					mAllPhotosInPhone = new ArrayList<PhotoInfo>();
					mAlbums = new ArrayList<AlbumInfo>();
					
					try {
						while (cursor.moveToNext()) {
							PhotoInfo photoInfo = new PhotoInfo();
							String photoPath = cursor.getString(cursor.getColumnIndex(Images.Media.DATA));
							String photoId = cursor.getLong(cursor.getColumnIndex(Images.Media._ID)) + "";
							String bucketId = cursor.getString(cursor.getColumnIndex(Images.Media.BUCKET_ID));
							String bucketName = cursor.getString(cursor.getColumnIndex(Images.Media.BUCKET_DISPLAY_NAME));
							String localUri = Uri.withAppendedPath(Images.Media.EXTERNAL_CONTENT_URI, photoId).toString();

							photoInfo.setPhotoSource(PhotoSource.PHONE);
							photoInfo.setPhotoId(photoId);
							photoInfo.setPhotoPath(photoPath);
							photoInfo.setBucketId(bucketId);
							photoInfo.setBucketName(bucketName);

							photoInfo.setFlowType(AppContext.getApplication().getFlowType());
							photoInfo.setLocalUri(localUri);
							photoInfo.setProductId("");
							
							for (int i = 0; i < mVideoGenParams.mVignettes.size(); i++) {
								if (photoId.equals(mVideoGenParams.mVignettes.get(i).mImageId)) {
									photoInfo.setSelected(true);
									mSelectedPhotoMap.put(i, photoInfo);
								}
								
							}

							AlbumInfo album = new AlbumInfo();
							album.setmAlbumId(bucketId);
							String mAlbumPath = photoPath.substring(0, photoPath.lastIndexOf(File.separator));
							album.setmAlbumPath(mAlbumPath);
							if (!mAlbums.contains(album)) {
								album.setmAlbumName(bucketName);
								mAlbums.add(album);
							}
							mAllPhotosInPhone.add(photoInfo);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (cursor != null && !cursor.isClosed()) {
							cursor.close();
						}
					}
					String cameraPath = "" ;
					File cameraAlbum = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) ;
					if(cameraAlbum!=null){
						cameraPath = cameraAlbum.getAbsolutePath() ;
					}
					
					if(TextUtils.isEmpty(cameraPath)){
						cameraPath = "DCIM/Camera" ;
					}
					
					List<PhotoInfo> photosInAlbum = null;
					if (mAlbums != null && mAlbums.size() > 0) {
						for (AlbumInfo album : mAlbums) {
							if(mCameraAlbum==null){
								String albumPath = album.getmAlbumPath() ;
								if(albumPath!=null && !"".equals(albumPath)){
									if(albumPath.contains(cameraPath)){
										mCameraAlbum = album ;
									}
								}
							}
							
							
							photosInAlbum = new ArrayList<PhotoInfo>();
							for (PhotoInfo photoInfo : mAllPhotosInPhone) {
								if (photoInfo.getBucketId().equals(album.getmAlbumId())) {
									photosInAlbum.add(photoInfo);
								}
							}
							album.setmPhotosInAlbum(photosInAlbum);
							album.setCoverId(photosInAlbum.get(0).getPhotoId());
							album.setCoverPath(photosInAlbum.get(0).getPhotoPath()) ;
							album.setPhotoNum(photosInAlbum.size());
						}
					}


				}
			} else {
				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
			}

			if (vProgressDialog != null && vProgressDialog.isShowing()) {
				vProgressDialog.cancel();
			}
             
			
			
			
			// May be handler will be better
			if(mCameraAlbum==null){
				mCameraAlbum = new AlbumInfo() ;
				mCameraAlbum.setmAlbumId(getString(R.string.camera));
				mCameraAlbum.setmAlbumName(getString(R.string.camera)) ;
				
			}
			
			
			String cameraTab = getString(R.string.camera)+"("+mCameraAlbum.getPhotoNum()+")" ;
			String albumTab = getString(R.string.albums)+"("+mAlbums.size()+")" ;
			String[] mTabStrings = {cameraTab,albumTab} ;
			vTabIndicator.setTabStringResStrings(mTabStrings);
			vTabIndicator.setCurrentItem(0) ;
			Bundle bundle = new Bundle();
			bundle.putSerializable("album", mCameraAlbum);
			bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.PHONE);
			FragmentManager fm =  TMSSelectPhotosActivity.this.getSupportFragmentManager() ;
			FragmentTransaction ft = fm.beginTransaction();
			Fragment newFragment = TMSPhotoSelectFragment.newInstance(bundle);
			ft.replace(R.id.relativelayout_container, newFragment);
			ft.commit();
//		

		}

	}
	
	
	
	public class TabListener implements TabIndicator.ITabSelectListener {

		@Override
		public void onTabSelected(TabView tab) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment fragment = null;
			Bundle bundle = null;

			switch (tab.getIndex()) {
			case 0:
				bundle = new Bundle();
				bundle.putSerializable("album", mCameraAlbum);
				bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.PHONE);
				fragment = TMSPhotoSelectFragment.newInstance(bundle);
				
			    break ;
			case 1 :
				bundle = new Bundle();
				AlbumHolder albumHolder = new AlbumHolder();
				albumHolder.setAlbums(mAlbums);
				bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.PHONE);
				bundle.putSerializable("albumsHolder", albumHolder);
				fragment = TMSAlbumSelectFragment.newInstance(bundle);
				
				break ;
			
			default :
				break ;
			}
			if (null != fragment) {
				if(fm.getBackStackEntryCount()>0){
					
					fm.popBackStack(fm.getBackStackEntryAt(0).getId(),
	                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
				}
				
				ft.replace(R.id.relativelayout_container, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//				ft.addToBackStack(null) ;
			
				ft.commit();
				
			   
			}
			
		}

		@Override
		public void onTabReselected(TabView tab) {
			// TODO Auto-generated method stub
			if(tab.getIndex()==1 ){
				FragmentManager fm = getSupportFragmentManager();
				Fragment fragment = fm.findFragmentById(R.id.relativelayout_container) ;
				if(fragment instanceof TMSPhotoSelectFragment){
					fm.popBackStack();
					
				}
			}
		}
		
	}


	public void onsSelectPhotoHelpSetButton(View v) {
		String buttonString = (String) vHelpSetButton.getText();
		if (buttonString.equals(getString(R.string.TMS_select_photos_help_dialog_got_it))) {
			boolean isNeedHelp = !vHelpCheckBox.isChecked();
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(SHOW_SELECT_PHOTO_HELP, isNeedHelp).commit();
			vHelpLayout.setVisibility(View.GONE);
		} else if (buttonString.equals(getString(R.string.TMS_help_all_set))) {
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(SHOW_REMOVE_PHOTO_HELP, false).commit();
			vHelpLayout.setVisibility(View.GONE);
		}

	}

	@Override
	public void onImageClick(SquareImageView squareImageView, String filePath) {
		
	}
    
	@Override
	public void onImageDoubleClick(SquareImageView squareImageView) {
		if(squareImageView.getImageType() == SquareImageView.IMAGE_TYPE_TOP_THREE){
			if (!TextUtils.isEmpty(squareImageView.getFilePath()) ) {
				int position  = squareImageView.getImagePosition() ;
				String photoId = mVideoGenParams.mVignettes.get(position).mImageId ;
				PhotoInfo currentPhoto = null ;
				List<PhotoInfo> selectedPhotos = new ArrayList<PhotoInfo>() ;
				Iterator<Entry<Integer, PhotoInfo>> iter = mSelectedPhotoMap.entrySet().iterator() ;
				while (iter.hasNext()) { 
					Entry<Integer, PhotoInfo> entry = iter.next() ;
					int key = entry.getKey() ;
					PhotoInfo photoValue = entry.getValue() ;
					if (photoValue == null) {
						continue;
					}
					selectedPhotos.add(photoValue) ;
					
					if(photoValue.getPhotoId().equals(photoId)){
						currentPhoto = photoValue ;
					}
				}
				
				GridPreviewAdapter adapter = new GridPreviewAdapter(this, selectedPhotos , mImageFetcher ,this) ;
				vPreViewViewPager.setAdapter(adapter) ;
				vPreViewViewPager.setVisibility(View.VISIBLE) ;
					
				if(currentPhoto!=null){
					vPreViewViewPager.setCurrentItem(selectedPhotos.indexOf(currentPhoto), true) ;
				}
				
			}
		}
		
		
	}
	
	@Override
	public ViewGroup getRootView() {
		return mRootView;
	}

	@Override
	public int getShadowWidth() {
		return mSelectedImagesViews.get(1).getWidth();
	}

	@Override
	public int getShadowHeight() {
		return mSelectedImagesViews.get(1).getHeight();
	}

	@Override
	public void onImageDrop(SquareImageView dropSource, SquareImageView dropedOn, boolean isTargetDropable, boolean wasSwap) {
		if (mFaceDetectorTask != null) {
			try {
				mFaceDetectorTask.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		if (mImageFilePersistor != null) {
			try {
				mImageFilePersistor.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		mFaceDetectorTask = null;

		if (isTargetDropable) {
			// dropSource or dropedOn could be null
//			if (mLargePreviewFileNameAdapter == mLargePreviewPager.getAdapter()) {
//				// Log.e(TAG, "Using top three images adapter");
//				refreshViewPager();
//			}
			if (wasSwap) {
				int dropOnIndex = -1;
				int dropSourceIndex = -1;
				for (int i = 0; i < mSelectedImagesViews.size(); i++) {
					SquareImageView view = mSelectedImagesViews.get(i);
					if (view == dropedOn) {
						dropOnIndex = i;
						// Log.e(TAG, "Match dropOn" + i);
					}
					if (view == dropSource) {
						dropSourceIndex = i;
						// Log.e(TAG, "Match dropSource" + i);
					}
				}
				VideoGenParams.Vignette vigSource = mVideoGenParams.mVignettes.get(dropSourceIndex);
				VideoGenParams.Vignette vigOn = mVideoGenParams.mVignettes.get(dropOnIndex);
				mVideoGenParams.mVignettes.set(dropSourceIndex, vigOn);
				mVideoGenParams.mVignettes.set(dropOnIndex, vigSource);
				vigOn.mIndex = dropSourceIndex;
				vigSource.mIndex = dropOnIndex;
				
				PhotoInfo temp = mSelectedPhotoMap.get(dropSourceIndex);
				mSelectedPhotoMap.put(dropSourceIndex, mSelectedPhotoMap.get(dropOnIndex));
				mSelectedPhotoMap.put(dropOnIndex, temp);
				
				swapFiles(vigSource, vigOn);
			} else {
				// Log.w(TAG, "Image added");
				for (int i = 0; i < mSelectedImagesViews.size(); i++) {
					SquareImageView view = mSelectedImagesViews.get(i);
					if (dropedOn == view) {
						// Log.w(TAG, "Clearing Bounds:" + i);
						setVignetteParams(i, dropedOn.getFilePath());
					}
				}
				persistImages(wasSwap);
			}
		} else {
			// Log.e(TAG, "Image: " + dropSource.getFilePath());
			// Log.w(TAG, "Image removed");
			for (int i = 0; i < mSelectedImagesViews.size(); i++) {
				SquareImageView view = mSelectedImagesViews.get(i);
				if (dropSource == view) {
					Log.e(TAG, "Equal to to three view");
					view.setImageBitmapAndFilePath("");
//					refreshViewPager();
					// } else if (dropedOn == view) {
					// Log.w(TAG, "Clearing Bounds:" + i);
					mVideoGenParams.mVignettes.get(i).mEndBounds = null;
					mVideoGenParams.mVignettes.get(i).mStartBounds = null;
				}
			}
		}
		// Log.e(TAG, "Image Changes Swap:" + wasSwap);
		
//		persistImages(wasSwap);
		updateSelectImageRemobeBtns();
		updateDoneButton();
	}
	
	private String mLastAnimationID;
	private void selectAndAnimatePhoto(final TMSImageCheckBoxView view, final PhotoInfo photo, final int position) {
		//Generate a view for animation
		final ImageView iv = new ImageView(this);
		final Bitmap bm = getBitmapFromView(view);
		iv.setImageBitmap(bm);
		
		int[] locationSrc = new int[2];
		view.getLocationInWindow(locationSrc);
		int[] locationAnimLayer = new int[2];
		mAnimLayer.getLocationInWindow(locationAnimLayer);
		int[] locationDest = new int[2];
		final SquareImageView destView = mSelectedImagesViews.get(position);
		destView.getLocationInWindow(locationDest);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(view.getWidth(), view.getHeight());
		
		//Be careful, if we add imageView in the edge of the animLayer, the size may be incorrect
		//Here we add this imageView in left-top avoid this issue
		mAnimLayer.addView(iv,params);
		TranslateAnimation anim = new TranslateAnimation(TranslateAnimation.ABSOLUTE, locationSrc[0] - locationAnimLayer[0], TranslateAnimation.ABSOLUTE, locationDest[0] - locationAnimLayer[0],
				TranslateAnimation.ABSOLUTE, locationSrc[1] - locationAnimLayer[1], TranslateAnimation.ABSOLUTE, locationDest[1] - locationAnimLayer[1]);
		anim.setDuration(500);
		anim.setFillAfter(false);
		
		//avoid repeat animation to the same select image
		destView.setFilePath(photo.getPhotoPath());
		mSelectedPhotoMap.put(position, photo);
		
		final String animationId =  anim.toString();
		mLastAnimationID = animationId;
		anim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				//this method will take a lot of time ,so commented it
//				destView.setImageBitmapAndFilePath(photo.getPhotoPath()); 
				iv.setVisibility(View.GONE);
				destView.setImageBitmap(bm);
				mSelectedImagesRemoveBtns.get(position).setVisibility(View.VISIBLE);
				view.setTMSChecked(true);
				photo.setSelected(true);
				final Vignette v = mVideoGenParams.mVignettes.get(position);
				v.mImageId = photo.getPhotoId();
				
				v.mEndBounds = null;
				v.mStartBounds = calculateStartBounds(photo.getPhotoPath());
				FaceDetectorThread thread = generateFaceDetectorThread(position, photo.getPhotoPath(), new FinishedFindingFacesEvent() {
					
					@Override
					public void onFinish(RectF result) {
						//Note: this callback is not run in Main Thread
						v.mEndBounds = result;
						Log.d(TAG, "Setting Face ROI:" + (result == null ? "null" : result.toShortString()));
					}
				});
				
				if (mFaceDetectorThreadPool != null && mFaceDetectorThreadPool.isAlive()) {
					mFaceDetectorThreadPool.addThread(thread);
				} else {
					mFaceDetectorThreadPool = new FaceDetectorThreadPool();
					mFaceDetectorThreadPool.addThread(thread);
					mFaceDetectorThreadPool.start();
				}
				
				//when all animation is finshed
				if (mLastAnimationID != null && mLastAnimationID.equals(animationId)) {
					mAnimLayer.removeAllViews();
					persistImages(false);
					updateDoneButton();
				}
			}
		});
		
		iv.startAnimation(anim);
	}
	
	private void deselectPhoto(int position) {
	// Log.w(TAG, "Image removed");
		SquareImageView view = mSelectedImagesViews.get(position);
		view.setImageBitmapAndFilePath("");
		mSelectedImagesRemoveBtns.get(position).setVisibility(View.GONE);
		if (mSelectedPhotoMap.containsKey(position)) {
			if (mSelectedPhotoMap.get(position) != null) {
				mSelectedPhotoMap.get(position).setSelected(false);
			}
			mSelectedPhotoMap.remove(position) ;
//			updatePhotoAdapter();
		}
//							refreshViewPager();
		// } else if (dropedOn == view) {
		// Log.w(TAG, "Clearing Bounds:" + i);
		mVideoGenParams.mVignettes.get(position).mEndBounds = null;
		mVideoGenParams.mVignettes.get(position).mStartBounds = null;
		persistImages(false);
		updateDoneButton();
	}
	
	private void deselectPhoto(TMSImageCheckBoxView view, PhotoInfo photo) {
		photo.setSelected(false);
		view.setTMSChecked(false);
		int position = findPositionByPhotoInfo(photo);
		if (position != -1) {
			deselectPhoto(position);
		}
	}
	
	private int findPositionByPhotoInfo(PhotoInfo info) {
		for (int i = 0; i < mVideoGenParams.mVignettes.size(); i++) {
			// Log.e(TAG,"Index: "+i);
			Vignette v = mVideoGenParams.mVignettes.get(i);
			if (v.mImageId != null && v.mImageId.equals(info.getPhotoId())) {
				return i;
			}
		}
		return -1;
	}
	
	private FaceDetectorThread generateFaceDetectorThread(int position, String filePath,FinishedFindingFacesEvent event) {
		FaceDetectorThread thread = new FaceDetectorThread(null);
		thread.setId(position);
		thread.setImagePath(filePath);
		thread.setFinishEvent(event);
		return thread;
	}
	
	private void setVignetteParams(final int position, final String filePath) {
		final Vignette v = mVideoGenParams.mVignettes.get(position);
		v.mEndBounds = null;
		v.mStartBounds = calculateStartBounds(filePath);
		mFaceDetectorTask = new FaceDetectorTask(null);
		mFaceDetectorTask.setFinishEvent(new FinishedFindingFacesEvent() {
			@Override
			public void onFinish(RectF result) {
				v.mEndBounds = result;
				Log.e(TAG, "Setting Face ROI:" + (result == null ? "null" : result.toShortString()));
			}
		});
		Bitmap b = MediaStoreUtils.getFullRes(TMSSelectPhotosActivity.this, filePath, AppConstants.TMS_IMAGE_MAX_DIMENSION, AppConstants.TMS_IMAGE_MAX_DIMENSION,
				SquareImageView.RESOLUTION_HIGHER);
		// b = Bitmap.createScaledBitmap(b, AppConstants.TMS_IMAGE_MAX_DIMENSION, AppConstants.TMS_IMAGE_MAX_DIMENSION, true);
		mFaceDetectorTask.execute(b);
	}
	
	private void updateSelectImageRemobeBtns() {
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			SquareImageView view = mSelectedImagesViews.get(i);
			String filePath = view.getFilePath();
			mSelectedImagesRemoveBtns.get(i).setVisibility(filePath == null || filePath.equals("") ? View.INVISIBLE : View.VISIBLE);
		}
	}
	
	private void updateDoneButton() {
		boolean allImagesComplete = true;
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			SquareImageView view = mSelectedImagesViews.get(i);
			String filePath = view.getFilePath();
			if (filePath == null || filePath.equals("")) {
				allImagesComplete = false;
				break;
			}
		}
		if (allImagesComplete) {
			vDoneButton.setEnabled(true);
			vDoneButton.setVisibility(View.VISIBLE);
		} else {
			vDoneButton.setEnabled(false);
			vDoneButton.setVisibility(View.INVISIBLE);
		}
	}
	
	private boolean isNeedShowSelectTips() {
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SHOW_SELECT_PHOTO_HELP, true);
	}
	
	private boolean isNeedShowRemoveTips() {
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SHOW_REMOVE_PHOTO_HELP, true) && mVideoGenParams.allImagesSelected();
	}

	@Override
	public boolean areViewsDragable() {
		return true;
	}
	
	private void updatePhotoAdapter(){
		
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.relativelayout_container) ;
		if ( fragment instanceof TMSPhotoSelectFragment ){
			((TMSPhotoSelectFragment) fragment).notifyDataSetChanged() ;
		}
		
	}
	

	@Override
	public void repalceWithNewFragment(Fragment newFragment) {

		// TODO Auto-generated method stub
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.relativelayout_container, newFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(null) ;
		ft.commit();
		
	}
	
	private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) 
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
//        else 
            //does not have background drawable, then draw white background on the canvas
//            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
	
	private int getFirstEmptyPosition() {
		int position = -1;
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			String filePath = mSelectedImagesViews.get(i).getFilePath();
			
			if (filePath == null || filePath .equals("")) {
				position = i;
				break;
			}
		}
		
		return position;
	}

	@Override
	public void onPhotoSelected(TMSImageCheckBoxView imageView, PhotoInfo photo) {
		if(IsEditActivity){
			attr.put(TMS_ADD_IMAGE, DURING_EDIT);
		}else{
		attr.put(TMS_ADD_IMAGE, DURING_CREATE);
		}
		RSSLocalytics.recordLocalyticsEvents(TMSSelectPhotosActivity.this, TMS_ADD_IMAGE, attr);
		if (isNeedShowRemoveTips()) {
			vHelpCheckBox.setVisibility(View.GONE);
			vHelpSetButton.setText(getString(R.string.TMS_help_all_set));
			vHelpLayout.setVisibility(View.VISIBLE);
			vDelImgButton.setVisibility(View.VISIBLE);
			vDoneButton.setVisibility(View.VISIBLE);
			vHelpText1.setVisibility(View.GONE);
			vHelpText2.setText(getString(R.string.TMS_select_photos_help_continue_tapping));
			return;
		}
		String photoId = photo.getPhotoId();
		boolean isBitmapInErrorList = AppContext.getApplication().isBitmapInErrorList(photoId);
		if (isBitmapInErrorList && !imageView.getTMSChecked()) {
			return;
		}
		if (!photo.isSelected()) {
			//Photo not select, try to add photo
			int position = getFirstEmptyPosition();
			if (position != -1 ) {
				//because animation has a duration, and the photo is set selected after animation
				//So we also need to check whether the image has been added
				boolean added = false;
				for (Map.Entry<Integer, PhotoInfo> entry: mSelectedPhotoMap.entrySet()) {
					PhotoInfo i = entry.getValue();
					if (i != null && photo.getPhotoId().equals(i.getPhotoId())) {
						added = true;
						break;
					}
				}
				
				if (!added) {
					selectAndAnimatePhoto(imageView, photo, position);
				}
			}
			
		} else {
			deselectPhoto(imageView, photo);
		}
		
	}

	@Override
	public void onPhotoDoubleClick(TMSImageCheckBoxView imageView, AlbumInfo album , PhotoInfo photo) {
		//show preview viewpager
		List<PhotoInfo> photosInAlbum = album.getmPhotosInAlbum() ;
		
		GridPreviewAdapter adapter = new GridPreviewAdapter(this, photosInAlbum , mImageFetcher ,this) ;
		vPreViewViewPager.setAdapter(adapter) ;
		vPreViewViewPager.setVisibility(View.VISIBLE) ;
		vPreViewViewPager.setCurrentItem(photosInAlbum.indexOf(photo),true) ;
	}

	@Override
	public void onViewPagerPhotoDoubleClick() {
		if(vPreViewViewPager!=null && vPreViewViewPager.getVisibility()==View.VISIBLE) {
			vPreViewViewPager.setAdapter(null) ;
			vPreViewViewPager.setVisibility(View.INVISIBLE) ;
			
		}
		
	}


}
