package com.kodak.kodak_kioskconnect_n2r.fragments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

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
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants;
import com.AppConstants.LoadImageType;
import com.AppConstants.PhotoSource;
import com.AppContext;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.example.android.displayingbitmaps.util.Utils;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.ImageCheckBoxView;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.activity.PhotoSelectMainFragmentActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumHolder;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.User;

public class AlbumSelectFragment extends Fragment {
	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageFetcher mImageFetcher;
	private static final String IMAGE_CACHE_DIR = "thumbs";
	private GridView vGridViewAlbum;
	private List<AlbumInfo> mAlbums;
	private AlbumAdapter mAdapter;
	private PhotoSource photoSource;
	private User friend;
	private String productId = "";
	private ProgressDialog vProgressDialog;
	ICommunicating mListener;

	public AlbumSelectFragment() {

	}

	public static AlbumSelectFragment newInstance(Bundle b) {
		AlbumSelectFragment f = new AlbumSelectFragment();

		f.setArguments(b);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mListener = (ICommunicating) activity;
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initData();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final View v = inflater.inflate(R.layout.imagegridview, container, false);

		vGridViewAlbum = (GridView) v.findViewById(R.id.common_gridview);
		vGridViewAlbum.setAdapter(mAdapter);
		setEvents();
		return v;

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (photoSource.isFromFaceBook()) {
			if (friend != null) {
				mListener.setTitleText(friend.getmName());
			} else {
				mListener.setTitleText(getString(R.string.Facebook_YourPhotos));
			}
		}else if(photoSource.isFromPhone()){
			mListener.setTitleText(getString(R.string.albums));
		}
		
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
		mAdapter = null;
		vGridViewAlbum=null;
		
	}

	private void initData() {

		Bundle bundle = getArguments();
		if (bundle != null) {
			AlbumHolder albumHolder = (AlbumHolder) bundle.getSerializable("albumsHolder");
			if (albumHolder != null) {
				mAlbums = albumHolder.getAlbums();
			}
			photoSource = (PhotoSource) bundle.getSerializable(AppConstants.KEY_PHOTO_SOURCE);
			productId = bundle.getString(AppConstants.KEY_PRODUCT_ID);
			friend = (User) bundle.getSerializable("user_friend");

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
					final int numColumns = (int) Math.floor(vGridViewAlbum.getWidth() / (mImageThumbSize + mImageThumbSpacing));
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
				if (position < mAdapter.getNumColumns()) {
					return;
				}
				
				if(photoSource.isFromPhone()){
					((PhotoSelectMainFragmentActivity)getActivity())
					   .setLocalyticsEventAttr(PhotoSelectMainFragmentActivity.KEY_PHOTOS_ALBUM, PhotoSelectMainFragmentActivity.VALUE_YES) ;
				}
				AlbumInfo album = mAlbums.get(position - mAdapter.getNumColumns());
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

				} else if (photoSource.isFromFaceBook()) {

					if (!album.isVirtualAlbum()) {

						if (!Connection.isConnected(getActivity())) {
							if (vProgressDialog != null && vProgressDialog.isShowing()) {
								vProgressDialog.cancel();
							}
							((PhotoSelectMainFragmentActivity) getActivity()).showNoConnectionDialog();

						} else {
							RequestBatch requestBatch = new RequestBatch();
							Bundle parameters = new Bundle();
							parameters.putInt("limit", AppConstants.LIMIT_COUNT);
							getPhotosInAlbum(getActivity(), requestBatch, parameters, album, null);
						}

					} else {
						if (vProgressDialog != null && vProgressDialog.isShowing()) {
							vProgressDialog.cancel();
						}
						// TODO relace with photoSelect fragment
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

			}
		});

	}

	private void showPhotosInAlbum(AlbumInfo album) {
		Bundle bundle = new Bundle();
		bundle.putSerializable("album", album);
		bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, photoSource);
		Fragment f = PhotoSelectFragment.newInstance(bundle);
		mListener.repalceWithNewFragment(f);
	}

	/**
	 * get photos in album from facebook
	 * 
	 * @param requestBatch
	 * @param parameters
	 * @param album
	 * @param photosList
	 */
	private void getPhotosInAlbum(Context context, RequestBatch requestBatch, Bundle parameters, AlbumInfo album, List<PhotoInfo> photosList) {
		if (requestBatch == null) {
			requestBatch = new RequestBatch();

		}

		if (photosList == null) {
			photosList = new ArrayList<PhotoInfo>();
		}

		String graphPath = String.format("/%s/photos", album.getmAlbumId());

		Request request = new Request(Session.getActiveSession(), graphPath, parameters, HttpMethod.GET, new GetPhotosInUserAlbumRequestCallback(
				context, requestBatch, album, photosList));
		// request.setVersion("v1.0") ;
		requestBatch.add(request);
		requestBatch.executeAsync();

	}

	/**
	 * get photos in user's named album
	 * 
	 * @author sunny
	 * 
	 */
	class GetPhotosInUserAlbumRequestCallback implements Request.Callback {
		private Context context;
		private RequestBatch requestBatch;
		private AlbumInfo album;
		private List<PhotoInfo> photosList;

		public GetPhotosInUserAlbumRequestCallback(Context context, RequestBatch requestBatch, AlbumInfo album, List<PhotoInfo> photosList) {
			super();
			this.context = context;
			this.requestBatch = requestBatch;
			this.photosList = photosList;
			this.album = album;
		}

		@Override
		public void onCompleted(Response response) {

			requestBatch.clear();

			GraphObject graphObject = response.getGraphObject();
			if (graphObject != null) {
//				Log.v("sunny", "sunny_response :" + response.toString());

				JSONObject jsonObj = graphObject.getInnerJSONObject();
				List<PhotoInfo> mPhotosInAlbum = null;
				if (jsonObj != null) {
					mPhotosInAlbum = getPhotosInAlbumFromResponse(jsonObj, album);
					JSONObject jsonPaging = jsonObj.optJSONObject("paging");

					if (mPhotosInAlbum != null) {

						if (photosList == null) {
							photosList = mPhotosInAlbum;
						} else {
							photosList.addAll(mPhotosInAlbum);
						}

					}

					if (jsonPaging != null && jsonPaging.has("next")) {
						String next = jsonPaging.optString("next");
						Bundle parametersBundle = ((PhotoSelectMainFragmentActivity) context).parseNextNode(next);
						getPhotosInAlbum(context, requestBatch, parametersBundle, album, photosList);
					} else {

						if (vProgressDialog != null && vProgressDialog.isShowing()) {
							vProgressDialog.cancel();
						}
						if (photosList != null) {
							album.setmPhotosInAlbum(photosList);
							// TODO.....replace with photoselect fragment
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

				}
			}

		}

		private List<PhotoInfo> getPhotosInAlbumFromResponse(JSONObject jsonObj, AlbumInfo album) {
			List<PhotoInfo> mPhotosInAlbum = null;
			if (jsonObj != null) {
				JSONArray photosArray = jsonObj.optJSONArray("data");
				if (photosArray != null && photosArray.length() > 0) {
					mPhotosInAlbum = new ArrayList<PhotoInfo>();
					for (int i = 0; i < photosArray.length(); i++) {
						JSONObject jsonObject = photosArray.optJSONObject(i);
						if (jsonObject != null) {
							PhotoInfo photoInfo = buidlerPhoto(jsonObject, album);
							mPhotosInAlbum.add(photoInfo);
						}
					}

				}

			}
			return mPhotosInAlbum;

		}

		private PhotoInfo buidlerPhoto(JSONObject jsonObject, AlbumInfo album) {
			PhotoInfo photoInfo = new PhotoInfo();

			String photoId = jsonObject.optString("id");
			String bucketId = album.getmAlbumId();
			String bucketName = album.getmAlbumName();

			photoInfo.setPhotoId(photoId);
			photoInfo.setBucketId(bucketId);
			photoInfo.setBucketName(bucketName);
			photoInfo.setPhotoSource(photoSource);
			photoInfo.setProductId(productId == null ? "" : productId);
			photoInfo.setFlowType(AppContext.getApplication().getFlowType());
			chooseBestQualityImage(jsonObject, photoInfo);
			chooseClosestThumnailImage(jsonObject, photoInfo, AppConstants.THUMNAIL_STANDARD_HEIGHT, AppConstants.THUMNAIL_STANDARD_WIDTH);
			photoInfo.setPhotoPath(photoInfo.getThumbnailUrl());

			return photoInfo;
		}

		/**
		 * choose for source
		 * 
		 * @param jsonObject
		 * @param photoInfo
		 */
		private void chooseBestQualityImage(JSONObject jsonObject, PhotoInfo photoInfo) {
			JSONArray images = jsonObject.optJSONArray("images");
			if (images != null && images.length() > 0) {

				JSONObject bestImage = images.optJSONObject(0);
				for (int i = 0; i < images.length(); i++) {

					int bestWidth = bestImage.optInt("width");
					int bestHeight = bestImage.optInt("height");
					int bestSize = bestWidth * bestHeight;

					JSONObject imageObj = images.optJSONObject(i);
					int imageWidth = imageObj.optInt("width");
					int imageHeight = imageObj.optInt("height");
					int imageSize = imageWidth * imageHeight;

					if (bestSize < imageSize) {
						bestImage = imageObj;
					}

				}

				photoInfo.setWidth(bestImage.optInt("width"));
				photoInfo.setHeight(bestImage.optInt("height"));
				photoInfo.setSourceUrl(bestImage.optString("source"));

			} else {

				photoInfo.setWidth(jsonObject.optInt("width"));
				photoInfo.setHeight(jsonObject.optInt("height"));
				photoInfo.setSourceUrl(jsonObject.optString("source"));

			}

		}

		/**
		 * choose the closest size to the request size
		 * 
		 * @param jsonObject
		 * @param photoInfo
		 * @param reqHeight
		 *            the request height
		 * @param reqWidth
		 *            the request width
		 */
		private void chooseClosestThumnailImage(JSONObject jsonObject, PhotoInfo photoInfo, int reqHeight, int reqWidth) {
			JSONArray images = jsonObject.optJSONArray("images");
			if (images != null && images.length() > 0) {
				JSONObject closestImage = images.optJSONObject(0);
				int reqSize = reqHeight * reqWidth;

				for (int i = 0; i < images.length(); i++) {
					int closestWidth = closestImage.optInt("width");
					int closestHeight = closestImage.optInt("height");
					int closestSize = closestWidth * closestHeight;
					int closestDiffAbs = Math.abs(closestSize - reqSize);

					JSONObject imageObj = images.optJSONObject(i);
					int imageWidth = imageObj.optInt("width");
					int imageHeight = imageObj.optInt("height");
					int imageSize = imageWidth * imageHeight;
					int diffAbs = Math.abs(imageSize - reqSize);

					if (diffAbs < closestDiffAbs) {
						closestImage = imageObj;
					}

				}

				photoInfo.setThumbnailUrl(closestImage.optString("source"));

			} else {
				photoInfo.setThumbnailUrl(jsonObject.optString("picture"));

			}
		}

	}

	/**
	 * album adapter
	 * 
	 * @author sunny
	 * 
	 */
	class AlbumAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		private List<AlbumInfo> mDataList;
		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private RelativeLayout.LayoutParams mImageViewLayoutParams;
		private int mTopViewHeight = 0;

		public AlbumAdapter(Context context, List<AlbumInfo> dataList) {
			mDataList = dataList;
			mContext = context;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mTopViewHeight = getResources().getDimensionPixelSize(R.dimen.top_view_height);
		}

		@Override
		public int getCount() {
			// If columns have yet to be determined, return no items
			if (getNumColumns() == 0) {
				return 0;
			}
            if(mDataList!=null){
            	return mDataList.size() + mNumColumns;
            }else {
            	return 0 ; 
            }
			
		}

		@Override
		public AlbumInfo getItem(int position) {
			return position < mNumColumns ? null : mDataList.get(position - mNumColumns);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position < mNumColumns ? 0 : position - mNumColumns;
		}

		@Override
		public int getViewTypeCount() {
			// Two types of views, the normal ImageView and the top row of empty
			// views
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			// TODO Auto-generated method stub
			return (position < mNumColumns) ? 1 : 0;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (position < mNumColumns) {
				if (convertView == null) {
					convertView = new View(mContext);
				}
				// Set empty view with height
				convertView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mTopViewHeight));
				return convertView;
			}
			final ViewHolder holder;

			if (convertView == null) { // if it's not recycled, instantiate and
										// initialize
				convertView = mInflater.inflate(R.layout.album_grid_item, parent, false);
				holder = new ViewHolder();
				holder.albumImage = (ImageCheckBoxView) convertView.findViewById(R.id.image_album_cover);
				holder.albumName = (TextView) convertView.findViewById(R.id.text_name);
				holder.albumCount = (TextView) convertView.findViewById(R.id.text_photos_num);
				holder.albumImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
				holder.albumImage.setLayoutParams(mImageViewLayoutParams);
				convertView.setTag(holder);
			} else { // Otherwise re-use the converted view
				holder = (ViewHolder) convertView.getTag();
			}

			AlbumInfo data = (AlbumInfo) getItem(position);
			if (holder.albumImage.getLayoutParams().height != mItemHeight) {

				holder.albumImage.setLayoutParams(mImageViewLayoutParams);

			}
			holder.albumName.setText(data.getmAlbumName());
			holder.albumCount.setText(data.getPhotoNum() + " " + getString(R.string.ImageSelection_Photos));

			if (photoSource.isFromPhone()) {

//				mImageFetcher.loadImage(data.getCoverId(), holder.albumImage, true);
				
				mImageFetcher.loadImage(data.getCoverId(), data.getCoverPath(), holder.albumImage, LoadImageType.MEDIA_IMAGE) ;

			} else {
				if (!data.isVirtualAlbum()) {
					String url = "https://graph.facebook.com/" + data.getmAlbumId() + "/picture" + "?access_token="
							+ Session.getActiveSession().getAccessToken() ;
//					Log.v("sunny", url);
//					mImageFetcher.loadImage(url, holder.albumImage, false);
					mImageFetcher.loadImage(url, url, holder.albumImage, LoadImageType.WEB_IMAGE) ;
				} else {
					if (data.getmPhotosInAlbum() != null && data.getmPhotosInAlbum().size() > 0) {
						String url = data.getmPhotosInAlbum().get(0).getThumbnailUrl();
//						Log.v("sunnyvi", "sunnyvi" + url);
//						mImageFetcher.loadImage(url, holder.albumImage, false);
						mImageFetcher.loadImage(url, url, holder.albumImage, LoadImageType.WEB_IMAGE) ;
					}
				}

			}

			return convertView;
		}

		/**
		 * Sets the item height. Useful for when we know the column width so the
		 * height can be set to match.
		 * 
		 * @param height
		 */
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
