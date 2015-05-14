package com.kodakalaris.video.fragments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.kodak.kodak_kioskconnect_n2r.ImageCheckBoxView;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumHolder;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumInfo;
import com.kodak.utils.EfficientAdapter;

public class TMSAlbumSelectFragment extends Fragment{
	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageFetcher mImageFetcher;
	private static final String IMAGE_CACHE_DIR = "thumbs";
	private GridView vGridViewAlbum;
	private List<AlbumInfo> mAlbums;
	private AlbumAdapter mAdapter;
	private PhotoSource photoSource;
	private ProgressDialog vProgressDialog;
	ICommunicatingForTMS mListener;
	
	public TMSAlbumSelectFragment() {

	}

	public static TMSAlbumSelectFragment newInstance(Bundle b) {
		TMSAlbumSelectFragment f = new TMSAlbumSelectFragment();

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.tms_imagegridview, container, false);

		vGridViewAlbum = (GridView) v.findViewById(R.id.tms_gridview);
		vGridViewAlbum.setAdapter(mAdapter);
		setEvents();
		return v;
	}
	
	private void initData() {
		Bundle bundle = getArguments();
		if (bundle != null) {
			AlbumHolder albumHolder = (AlbumHolder) bundle.getSerializable("albumsHolder");
			if (albumHolder != null) {
				mAlbums = albumHolder.getAlbums();
			}
			photoSource = (PhotoSource) bundle.getSerializable(AppConstants.KEY_PHOTO_SOURCE);

		}
		
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

		mAdapter = new AlbumAdapter(getActivity(), mAlbums);
		
	}
	
	private void setEvents() {
		vGridViewAlbum.setOnScrollListener(new AbsListView.OnScrollListener() {

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

		vGridViewAlbum.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@TargetApi(16)
			@Override
			public void onGlobalLayout() {

				if (mAdapter!=null && mAdapter.getNumColumns() == 0) {
					final int numColumns = 3 ;
					if (numColumns > 0) {
						final int columnWidth = (vGridViewAlbum.getWidth() / numColumns) - mImageThumbSpacing;
						mAdapter.setNumColumns(numColumns);
						mAdapter.setItemHeight(columnWidth);

						if (Utils.hasJellyBean()) {
							// vGridViewAlbums.getViewTreeObserver().removeOnGlobalLayoutListener(this);
							Class<?> c = vGridViewAlbum.getViewTreeObserver().getClass();
							try {
								Method method = c.getDeclaredMethod("removeOnGlobalLayoutListener", OnGlobalLayoutListener.class);
								method.invoke(vGridViewAlbum.getViewTreeObserver(), this);

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
							vGridViewAlbum.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						}
					}
				}

			}

		});
		
		vGridViewAlbum.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				AlbumInfo album = mAlbums.get(position);
				if (vProgressDialog == null) {
					vProgressDialog = new ProgressDialog(getActivity());
					vProgressDialog.setCancelable(true);
					vProgressDialog.show();
				} else {
					if (!vProgressDialog.isShowing()) {
						vProgressDialog.show();
					}
				}
				if (photoSource.isFromPhone()) {

					if (vProgressDialog != null && vProgressDialog.isShowing()) {
						vProgressDialog.cancel();
					}
					// TODO REPALCE WITH PHOTO SELECT

					showPhotosInAlbum(album);

					// Intent intent = new
					// Intent(getActivity(),PhotoSelectActivity.class) ;
					// Bundle bundle = new Bundle() ;
					// bundle.putSerializable("album", album) ;
					// bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE,
					// photoSource) ;
					// intent.putExtra("bundle", bundle) ;
					// startActivity(intent);

				} 

			}
		}) ;
		
		
		
		
	}
	
	
	
	private void showPhotosInAlbum(AlbumInfo album) {
		Bundle bundle = new Bundle();
		bundle.putSerializable("album", album);
		bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, photoSource);
		bundle.putBoolean("isNeedShowAlbumName", true) ;
		Fragment f = TMSPhotoSelectFragment.newInstance(bundle);
		mListener.repalceWithNewFragment(f);
	}

	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		mListener.setTitleText(getString(R.string.albums));
		
		mImageFetcher.setExitTasksEarly(false);
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
		mAdapter=null;
		vGridViewAlbum=null;
	}
	
	
	class AlbumAdapter extends EfficientAdapter<AlbumInfo>{

		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private RelativeLayout.LayoutParams mImageViewLayoutParams;

		public AlbumAdapter(Context context, List<AlbumInfo> dataList) {
			super(context, dataList);
			mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}

		@Override
		protected int getItemLayout() {
			return R.layout.album_grid_item;
		}

		@Override
		protected void initView(View v) {
			ViewHolder holder = new ViewHolder();
			holder.albumImage = (ImageCheckBoxView) v.findViewById(R.id.image_album_cover);
			holder.albumName = (TextView) v.findViewById(R.id.text_name);
			holder.albumCount = (TextView) v.findViewById(R.id.text_photos_num);
			holder.albumImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
			holder.albumImage.setLayoutParams(mImageViewLayoutParams);
			v.setTag(holder);
			
		}

		@Override
		protected void bindView(View v, AlbumInfo data, int position) {
			if (data == null) {
				return;
			}
			final ViewHolder holder = (ViewHolder) v.getTag();

			if (holder.albumImage.getLayoutParams().height != mItemHeight) {

				holder.albumImage.setLayoutParams(mImageViewLayoutParams);

			}

			holder.albumName.setText(data.getmAlbumName());
			holder.albumCount.setText(data.getPhotoNum() +" "+ getString(R.string.ImageSelection_Photos));
            if (photoSource.isFromPhone()) {
				
//				mImageFetcher.loadImage(data.getCoverId(), holder.albumImage,true);
				
            	mImageFetcher.loadImage(data.getCoverId(), data.getCoverPath(),holder.albumImage,LoadImageType.MEDIA_IMAGE) ;
			} 
			
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
			private ImageCheckBoxView albumImage;
			private TextView albumName;
			private TextView albumCount;
		}


		
	}
	

}
