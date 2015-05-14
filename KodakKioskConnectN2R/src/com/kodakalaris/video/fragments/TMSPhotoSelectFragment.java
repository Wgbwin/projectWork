package com.kodakalaris.video.fragments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants;
import com.AppConstants.LoadImageType;
import com.AppConstants.PhotoSource;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.example.android.displayingbitmaps.util.Utils;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.utils.EfficientAdapter;
import com.kodakalaris.video.views.TMSImageCheckBoxView;

public class TMSPhotoSelectFragment extends Fragment {
	private TextView vTextAlbumName ;
	private GridView vGridViewPhotos ;
	private PhotosAdapter mPhotosAdapter;
	private AlbumInfo mAlbum;
	private PhotoSource photoSource;
	private boolean isNeedShowAlbumName ;
	private List<PhotoInfo> mPhotosInAlbum ;
	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageFetcher mImageFetcher;
	private static final String IMAGE_CACHE_DIR = "thumbs";
	ICommunicatingForTMS mListener;
	
	
	public TMSPhotoSelectFragment() {

	}

	public static TMSPhotoSelectFragment newInstance(Bundle b) {
		TMSPhotoSelectFragment f = new TMSPhotoSelectFragment() ;

		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mListener = (ICommunicatingForTMS) activity;
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initData() ;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.tms_imagegridview, container, false);
		vTextAlbumName = (TextView) v.findViewById(R.id.album_text) ;
		vGridViewPhotos = (GridView) v.findViewById(R.id.tms_gridview) ;
		if(isNeedShowAlbumName){
			vTextAlbumName.setVisibility(View.VISIBLE) ;
			vTextAlbumName.setText(mAlbum.getmAlbumName()+"("+mAlbum.getPhotoNum()+")") ;
		}else {
			vTextAlbumName.setVisibility(View.GONE) ;
		}
		
		vGridViewPhotos.setAdapter(mPhotosAdapter);
		setEvents();
		return v ;
	} ;
	
	
	private void initData(){
		Bundle bundle = getArguments();
		mAlbum = (AlbumInfo) bundle.getSerializable("album");
		photoSource = (PhotoSource) bundle.getSerializable(AppConstants.KEY_PHOTO_SOURCE);
		isNeedShowAlbumName = bundle.getBoolean("isNeedShowAlbumName", false) ;
		mPhotosInAlbum = mAlbum.getmPhotosInAlbum();
		mPhotosAdapter = new PhotosAdapter(getActivity(), mPhotosInAlbum);

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.imagewait96x96);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
		
		
	}
	
	
	private void setEvents(){
		vGridViewPhotos.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					// Before Honeycomb pause image loading on scroll to help
					// with performance
					if (!Utils.hasHoneycomb()) {
						mImageFetcher.setPauseWork(true);
					}
				} else {
					mImageFetcher.setPauseWork(false);
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}

		});
		
		
		
		vGridViewPhotos.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@TargetApi(16)
			@Override
			public void onGlobalLayout() {

				if (mPhotosAdapter!=null && mPhotosAdapter.getNumColumns() == 0) {
					final int numColumns = 3;
					if (numColumns > 0) {
						final int columnWidth = (vGridViewPhotos.getWidth() / numColumns) - mImageThumbSpacing;
						mPhotosAdapter.setNumColumns(numColumns);
						mPhotosAdapter.setItemHeight(columnWidth);

						if (Utils.hasJellyBean()) {
							// vGridViewAlbums.getViewTreeObserver().removeOnGlobalLayoutListener(this);
							Class<?> c = vGridViewPhotos.getViewTreeObserver().getClass();
							try {
								Method method = c.getDeclaredMethod("removeOnGlobalLayoutListener", OnGlobalLayoutListener.class);
								method.invoke(vGridViewPhotos.getViewTreeObserver(), this);

							} catch (NoSuchMethodException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} else {
							vGridViewPhotos.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						}
					}
				}

			}

		});
		
		
		
//		vGridViewPhotos.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				PhotoInfo photo = mPhotosInAlbum.get(position) ;
//				PhotosAdapter.ViewHolder holder = (PhotosAdapter.ViewHolder) view.getTag();
//				ImageCheckBoxView imageView = holder.vImageViewPhoto;
//				
//				mListener.onPhotoSelected(imageView, photo) ;
//				
//				
//				
//			}
//		}) ;
		
		
		
		
		
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		notifyDataSetChanged();
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mImageFetcher.closeCache();
		mPhotosAdapter=null;
		vGridViewPhotos=null;
	}
	
	public void notifyDataSetChanged() {
		if (mPhotosAdapter != null) {
			mPhotosAdapter.notifyDataSetChanged();
		}
	}
	
	
	
	class PhotosAdapter extends EfficientAdapter<PhotoInfo> {
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private RelativeLayout.LayoutParams mImageViewLayoutParams;
		
		public PhotosAdapter(Context context, List<PhotoInfo> dataList) {
			super(context, dataList);
			mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT) ;
		}

		@Override
		protected int getItemLayout() {
			// TODO Auto-generated method stub
			return R.layout.tms_photo_grid_item;
		}

		@Override
		protected void initView(View v) {
			// TODO Auto-generated method stub
			ViewHolder holder = new ViewHolder();
			holder.vImageViewPhoto = (TMSImageCheckBoxView) v.findViewById(R.id.image_photo);
			
			holder.vImageViewPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
			holder.vImageViewPhoto.setLayoutParams(mImageViewLayoutParams);
			v.setTag(holder);
		}

		@Override
		protected void bindView(View v, PhotoInfo data, int position) {
			// TODO Auto-generated method stub
			if (data == null) {
				return;
			}
			final ViewHolder holder = (ViewHolder) v.getTag();
			if (holder.vImageViewPhoto.getLayoutParams().height != mItemHeight) {

				holder.vImageViewPhoto.setLayoutParams(mImageViewLayoutParams) ;

			}
//			mImageFetcher.loadImage( data.getPhotoId(), holder.vImageViewPhoto,true ) ;
			mImageFetcher.loadImage(data.getPhotoId(), data.getPhotoPath(),  holder.vImageViewPhoto, LoadImageType.MEDIA_IMAGE) ;
			holder.vImageViewPhoto.setTMSChecked(data.isSelected()) ;
			holder.vImageViewPhoto.setPhoto(data) ;
			holder.vImageViewPhoto.setAlbum(mAlbum) ;
			holder.vImageViewPhoto.setICommunicatingForTMS(mListener) ;
			
			
		}
		
		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
			mImageFetcher.setImageSize(height);
			notifyDataSetChanged();
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}
		
		private class ViewHolder {
			private TMSImageCheckBoxView vImageViewPhoto;
		}
		
	}
	

}
