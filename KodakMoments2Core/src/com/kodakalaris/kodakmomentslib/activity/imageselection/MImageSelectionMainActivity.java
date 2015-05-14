package com.kodakalaris.kodakmomentslib.activity.imageselection;


import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.AppConstants.PhotoSizeType;
import com.kodakalaris.kodakmomentslib.AppConstants.PhotoSource;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.printsizeselection.MPrintSizeSelectionActivity;
import com.kodakalaris.kodakmomentslib.adapter.FragmentTabAdapter;
import com.kodakalaris.kodakmomentslib.adapter.FragmentTabAdapter.FragmentInfo;
import com.kodakalaris.kodakmomentslib.bean.AlbumHolder;
import com.kodakalaris.kodakmomentslib.bean.AlbumInfo;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.fragment.mobile.AlbumSelectFragment;
import com.kodakalaris.kodakmomentslib.fragment.mobile.FacebookFragment;
import com.kodakalaris.kodakmomentslib.fragment.mobile.PhotoSelectFragment;
import com.kodakalaris.kodakmomentslib.interfaces.ICommunicating;
import com.kodakalaris.kodakmomentslib.interfaces.IPhotoOperationInterface;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;
import com.kodakalaris.kodakmomentslib.util.ImageUtil;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.TabPageIndicator;
import com.kodakalaris.kodakmomentslib.widget.mobile.GeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.KMImageView;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;
import com.kodakalaris.kodakmomentslib.widget.mobile.MImageTray;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MImageSelectionMainActivity extends BaseImageSelectionMainActivity 
        implements IPhotoOperationInterface,ICommunicating{
	private static String TAG = MImageSelectionMainActivity.class.getSimpleName() ;
	private MActionBar vActionBar;
	private TabPageIndicator vTabPageIndicator;
	private ViewPager vViewPager;
	private RelativeLayout vRelativeLayoutContainer;
	private FragmentTabAdapter mTabAdapter;
	private List<AlbumInfo> mAlbums;
	public static final int QUERY_TOKEN = 34;
	private List<PhotoInfo> mAllPhotosInPhone; // all photos in local
	private AlbumInfo mCameraAlbum ;
	private String productId = "";
	private QueryPhotoHandler mQueryPhotoHandler;
	private ProgressDialog vProgressDialog;
	private MImageTray vImageTray;
	private View vDisableScreen;
	
	private RelativeLayout vRelativeLayoutPreview;
	private ImageView vImageViewPreview;
	private int currentPosition = 0;

	
	@Override
	protected void setKMContentView() {
		setContentView(R.layout.activity_m_image_selection_main);
		
	}

	@Override
	protected void initViews() {
		vActionBar = (MActionBar) findViewById(R.id.title_bar);
		vViewPager = (ViewPager) findViewById(R.id.viewpager_imageselection);
		vTabPageIndicator = (TabPageIndicator) findViewById(R.id.tab_imageselection_indicator);
		vRelativeLayoutContainer = (RelativeLayout) findViewById(R.id.relativelayout_imageselection_container);
		vImageTray = (MImageTray) findViewById(R.id.it_image_tray);
		vDisableScreen = findViewById(R.id.v_disable_screen);
		vRelativeLayoutPreview = (RelativeLayout) findViewById(R.id.relative_preview);
		vImageViewPreview = (ImageView) findViewById(R.id.image_preview);
		vImageTray.setShandow(vDisableScreen);
	}
	
	protected  void initData() {
		mTabAdapter = new FragmentTabAdapter(MImageSelectionMainActivity.this, null);
		mQueryPhotoHandler = new QueryPhotoHandler(this);
		startQueryPhotoPhone();
		vImageTray.initialize(mImageSelector.getmTempSelectedPhotos());
		
	}
	
	protected  void setEvents(){
		vActionBar.setOnLeftButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		vRelativeLayoutPreview.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				vRelativeLayoutPreview.setVisibility(View.GONE);
				vImageViewPreview.setImageBitmap(null);
				
			}
		});

		vTabPageIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				currentPosition = position;
				showDialogIfEmptyPhoto(currentPosition);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

	}

	private void showDialogIfEmptyPhoto(int position) {
		if ((position == 0 && (mAllPhotosInPhone == null || mAllPhotosInPhone
				.size() <= 0))
				|| (position == 1 && (mAlbums == null || mAlbums.size() <= 0))) {
			new GeneralAlertDialogFragment(MImageSelectionMainActivity.this)
					.setMessage(R.string.ImageSelection_EmptyPhoto_Alert)
					.setPositiveButton(R.string.ImageSelection_AlertButton_OK,
							null).show(getSupportFragmentManager(), TAG);
		}
	}

	protected void onResume() {
		super.onResume();
		vImageTray.refresh(mImageSelector.getmTempSelectedPhotos());
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		startQueryPhotoPhone();
	}

	@Override
	public void onBackPressed() {
		// if preview image ,the back key should be disabled
		if(vRelativeLayoutPreview.getVisibility()==View.VISIBLE
				|| vDisableScreen.getVisibility()==View.VISIBLE ){
			return ;
		}
		
		FragmentManager fm = getSupportFragmentManager() ;
		int fragmentInStackCount = fm.getBackStackEntryCount() ;
		Log.v(TAG, "COUNT---BACK "+ fragmentInStackCount ) ; 
		if(fragmentInStackCount==0){
			
			if(KM2Application.getInstance().getFlowType().isPrintWorkFlow()){
				if(PrintManager.getInstance(MImageSelectionMainActivity.this).getPrintItems().size()>0
						|| mImageSelector.getmTempSelectedPhotos().size()>0){
					//show a dialog
					GeneralAlertDialogFragment startOverDialog = new GeneralAlertDialogFragment(MImageSelectionMainActivity.this);
					startOverDialog.setTitle(R.string.ImageSelection_Start_Over);
					startOverDialog.setMessage(R.string.ImageSelection_Start_Over_LoseAllWork);
					startOverDialog.setPositiveButton(R.string.Common_OK, new BaseGeneralAlertDialogFragment.OnClickListener() {
						
						@Override
						public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
							PrintManager.getInstance(MImageSelectionMainActivity.this).startOver();
							ShoppingCartManager.getInstance().startOver();
							mImageSelector.destroy();
							Intent intent = new Intent(MImageSelectionMainActivity.this, MPrintSizeSelectionActivity.class);
							startActivity(intent);
							finish();
							
						}
					});
					
					startOverDialog.setNegativeButton(R.string.Common_Cancel, null);
					startOverDialog.show(getSupportFragmentManager(), "ImageSelection_Start_Over");
					
					
					
				}else{
					Intent intent = new Intent(this, MPrintSizeSelectionActivity.class);
				    startActivity(intent);
					finish();
				}
				
			}
			//.....other work flow should add  below 
			
			
		}else{
			vRelativeLayoutContainer.setVisibility(View.INVISIBLE);
			super.onBackPressed();
		}
	}
	
	
	public void refreshImageTray(){
		vImageTray.refresh(mImageSelector.getmTempSelectedPhotos());
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
				+ Images.Media.MIME_TYPE + " in (\'image/jpeg\',\'image/jpg\',\'image/png\') "+ " and "+ Images.Media.SIZE +" > 0 ", new String[] { "%cache%" }, Images.Media._ID
				+ " DESC");
	}
	
	/**
	 * get all photos in the phone
	 * 
	 * @author sunny
	 * 
	 */
	private final class QueryPhotoHandler extends AsyncQueryHandler {
		private final WeakReference<MImageSelectionMainActivity> mActivity;

		public QueryPhotoHandler(Context context) {
			super(context.getContentResolver());
			mActivity = new WeakReference<MImageSelectionMainActivity>((MImageSelectionMainActivity) context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			super.onQueryComplete(token, cookie, cursor);
			final MImageSelectionMainActivity activity = mActivity.get();
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
					
					if(mCameraAlbum!=null){
						mCameraAlbum = null;
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
							int width =cursor.getInt(cursor.getColumnIndex(Images.Media.WIDTH));
							int height =cursor.getInt(cursor.getColumnIndex(Images.Media.HEIGHT));
							int orientation = cursor.getInt(cursor.getColumnIndex(Images.Media.ORIENTATION));
							
							if(width==0 || height==0){
								ExifInterface exifInterface = ImageUtil.getFileExifInterface(activity, photoPath);
								width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
								height = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
								if(width==0 || height==0){
									continue ;
								}
								
							}
							
							if(ImageUtil.isFilter(photoPath)){
								continue;
							}

							photoInfo.setPhotoSource(PhotoSource.PHONE);
							photoInfo.setPhotoId(photoId);
							photoInfo.setPhotoPath(photoPath);
							photoInfo.setBucketId(bucketId);
							photoInfo.setBucketName(bucketName);

							photoInfo.setFlowType(KM2Application.getInstance().getFlowType());
							photoInfo.setLocalUri(localUri);
							photoInfo.setWidth(width);
							photoInfo.setHeight(height);
							photoInfo.setOrientation(orientation);
							photoInfo.setProductId(productId == null ? "" : productId);
							if(KM2Application.getInstance().getFlowType().isPrintWorkFlow()){
								photoInfo.setDesId(PrintManager.getInstance(activity).getDefaultPrintSize().proDescription.id);
							}
							
							if(orientation==90 || orientation ==270){
								int temp = width ;
								width = height;
								height = temp;
							}
							
							if(width>height){
								if(width>height*2){
									photoInfo.setPhotoSizeType(PhotoSizeType.PANAROMA);
								}else{
									photoInfo.setPhotoSizeType(PhotoSizeType.LANDSCAPE);
								}
							}else{
								photoInfo.setPhotoSizeType(PhotoSizeType.PORTRAIT);
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
							album.setCoverUri(photosInAlbum.get(0).getLocalUri());
							album.setCoverPath(photosInAlbum.get(0).getPhotoPath()) ;
							album.setPhotoNum(photosInAlbum.size());
						}
					}

					// mAdapter.setDataSource(mAlbums) ;

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
				mCameraAlbum.setmAlbumId("CAMERA");
				mCameraAlbum.setmAlbumName("CAMERA") ;
				
			}
			
			Bundle bundlePhoto = new Bundle();
			bundlePhoto.putSerializable("album", mCameraAlbum);
			bundlePhoto.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.PHONE);
			
			Bundle bundleAlbum = new Bundle();
			AlbumHolder albumHolder = new AlbumHolder();
			albumHolder.setAlbums(mAlbums);
			bundleAlbum.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.PHONE);
			bundleAlbum.putSerializable("albumsHolder", albumHolder);
			bundleAlbum.putString(AppConstants.KEY_PRODUCT_ID, productId);
			
			Bundle bundleFacebook = new Bundle();
			bundleFacebook.putString("string", "Facebook");
			
			Bundle bundleFlickr = new Bundle();
			bundleFlickr.putString("string", "Flickr");
			
			Bundle bundleInstagram = new Bundle();
			bundleInstagram.putString("string", "Instagram");
			
			Bundle bundleDropbox = new Bundle();
			bundleDropbox.putString("string", "Dropbox");
			if(mTabAdapter.getCount()==0){
				mTabAdapter.addFragmentTabs(PhotoSelectFragment.class,getString(R.string.ImageSelection_Camera),bundlePhoto);
				mTabAdapter.addFragmentTabs(AlbumSelectFragment.class,getString(R.string.ImageSelection_Album), bundleAlbum);
				mTabAdapter.addFragmentTabs(FacebookFragment.class,getString(R.string.ImageSelection_Facebook), bundleFacebook);
				mTabAdapter.addFragmentTabs(FacebookFragment.class, getString(R.string.ImageSelection_Flickr),bundleFlickr);
				mTabAdapter.addFragmentTabs(FacebookFragment.class, getString(R.string.ImageSelection_Instagram),bundleInstagram);
				mTabAdapter.addFragmentTabs(FacebookFragment.class, getString(R.string.ImageSelection_Dropbox),bundleDropbox);
				vViewPager.setAdapter(mTabAdapter);
				vTabPageIndicator.setViewPager(vViewPager);
			}else{
				List<FragmentInfo> mFragmentInfoes = mTabAdapter.getmFragmentInfoes();
				mFragmentInfoes.get(0).setBundle(bundlePhoto);
				mFragmentInfoes.get(1).setBundle(bundleAlbum);
				mTabAdapter.notifyDataSetChanged();
			
			}
			showDialogIfEmptyPhoto(currentPosition);
		}

	}

	@Override
	public void onPhotoClick(KMImageView imageView ,PhotoInfo photo) {
		mImageSelector.selectPhoto(this, imageView ,photo);
	}

	@Override
	public void onPhotoLongClick(KMImageView imageView ,PhotoInfo photo) {
		vRelativeLayoutPreview.setVisibility(View.VISIBLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if(photo.getPhotoSource().isFromPhone()){
			ImageLoader.getInstance().displayImage("file://"+photo.getPhotoPath(), vImageViewPreview);
		}
	}

	@Override
	public void showPhotosInAlbum(Bundle bundle) {
		vRelativeLayoutContainer.setVisibility(View.VISIBLE);
		FragmentManager fm =  MImageSelectionMainActivity.this.getSupportFragmentManager() ;
		FragmentTransaction ft = fm.beginTransaction();
		Fragment newFragment = PhotoSelectFragment.newInstance(bundle);
		ft.replace(R.id.relativelayout_imageselection_container, newFragment);
		ft.addToBackStack(null) ;
		ft.commit();
		
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		ImageLoader.getInstance().clearMemoryCache();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		//ImageLoader.getInstance().clearDiskCache();
		mImageSelector.removeAllCheckedImageView();
		
	}
	

}
