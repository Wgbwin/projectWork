package com.kodak.kodak_kioskconnect_n2r.fragments;

import java.io.Serializable;
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
import com.kodak.kodak_kioskconnect_n2r.bean.UserHolder;

public class FacebookFriendsFragment extends Fragment{
	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageFetcher mImageFetcher;
	private static final String IMAGE_CACHE_DIR = "thumbs";
	
	private GridView vGridViewFriends;
	private FriendsAdapter mFriendsListAdapter  ;
	private List<User> mFriendsList;
	private String productId = "";
	private PhotoSource photoSource ;
	
	private ProgressDialog vProgressDialog;
	ICommunicating mListener ;
	public FacebookFriendsFragment() {
		
	}
	
	public static FacebookFriendsFragment newInstance(Bundle b) {
		FacebookFriendsFragment f = new FacebookFriendsFragment();
       
        f.setArguments(b);
        return f;
     }	
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (ICommunicating) activity ;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData() ;
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.imagegridview, container, false);
		vGridViewFriends = (GridView) v.findViewById(R.id.common_gridview) ;
		vGridViewFriends.setAdapter(mFriendsListAdapter) ;
		
		setEvents() ;
		
		return v ;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mListener.setTitleText(getString(R.string.Facebook_YourFriends)) ;
		mImageFetcher.setExitTasksEarly(false);
		mFriendsListAdapter.notifyDataSetChanged() ;
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
	}
	
	private void initData() {
		Bundle bundle = getArguments() ;
		if (bundle != null) {
			UserHolder userHolder = (UserHolder) bundle.getSerializable("friendsListHolder") ;
			mFriendsList = userHolder.getUsers();
			productId = bundle.getString(AppConstants.KEY_PRODUCT_ID) ;
			photoSource = (PhotoSource) bundle.getSerializable(AppConstants.KEY_PHOTO_SOURCE) ;
		}
		
		if(photoSource==null){
			photoSource = PhotoSource.FACEBOOK ;
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
		
		mFriendsListAdapter = new FriendsAdapter(getActivity(), mFriendsList);
	}
	
	private void setEvents() {
		
		vGridViewFriends.setOnScrollListener(new AbsListView.OnScrollListener() {

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
			 
		 } );
		
		vGridViewFriends.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@TargetApi(16)
			@Override
			public void onGlobalLayout() {

                if (mFriendsListAdapter.getNumColumns() == 0) {
                    final int numColumns = (int) Math.floor(
                    		vGridViewFriends.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                    if (numColumns > 0) {
                        final int columnWidth =
                                (vGridViewFriends.getWidth() / numColumns) - mImageThumbSpacing;
                        mFriendsListAdapter.setNumColumns(numColumns);
                        mFriendsListAdapter.setItemHeight(columnWidth);
                        
                        if (Utils.hasJellyBean()) {
//							vGridViewAlbums.getViewTreeObserver().removeOnGlobalLayoutListener(this);
							Class<?> c =vGridViewFriends.getViewTreeObserver().getClass() ;
							try {
								Method method = c.getDeclaredMethod("removeOnGlobalLayoutListener", OnGlobalLayoutListener.class) ;
								method.invoke(vGridViewFriends.getViewTreeObserver(), this);
								
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
                        	vGridViewFriends.getViewTreeObserver()
                                    .removeGlobalOnLayoutListener(this);
                        }
                    }
                }
            
				
			}
			
		});	
	
		vGridViewFriends.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(position < mFriendsListAdapter.getNumColumns()){
					return ;
				}
				if(!Connection.isConnected(getActivity())){
					((PhotoSelectMainFragmentActivity)getActivity()).showNoConnectionDialog();
				}else {
					User user  = mFriendsList.get(position-mFriendsListAdapter.getNumColumns()) ;
					
					if (vProgressDialog == null) {
						vProgressDialog = new ProgressDialog(getActivity());
						vProgressDialog.setCancelable(true);
						vProgressDialog.show();
					} else {
						if (!vProgressDialog.isShowing()) {
							vProgressDialog.show();
						}
					}
					
					RequestBatch requestBatch = new RequestBatch() ;
					Bundle parameters = new Bundle() ;
					parameters.putInt("limit", AppConstants.LIMIT_COUNT) ;
					getUserAlbums(getActivity(),requestBatch, parameters, user, null) ;
					
					
				}
				
				
				
			}
		}) ;
	}
	
	
	/**
	 * get the user's albums from facebook
	 * 
	 */
	private void getUserAlbums(Context context , RequestBatch requestBatch, Bundle parameters , User user ,List<AlbumInfo> albumsList) {
		
		if(requestBatch==null){
	        requestBatch = new RequestBatch();
				
		}
		
		if(albumsList==null){
			albumsList = new ArrayList<AlbumInfo>() ;
		}
		
		String graphPath = String.format("/%s/albums",user.getmUserId()) ;
		
		Request request = new Request(
				Session.getActiveSession(),
				graphPath, 
				parameters, 
				HttpMethod.GET,
				new GetUserAlbumsListRequestCallback(context,requestBatch, user ,albumsList));
//		request.setVersion("v1.0") ;
		requestBatch.add(request) ;
		requestBatch.executeAsync();

	}
	
	/**
	 * get photos of the user ,then use the photos to build an album name "Photos Of XXX"
	 * add the album to the first location in albumsList
	 * @param context
	 * @param requestBatch
	 * @param parameters
	 * @param user
	 * @param album
	 * @param photosList
	 * @param albumsList
	 */
	private void getPhotosOfUser(Context context , RequestBatch requestBatch, Bundle parameters, 
			  User user , AlbumInfo album ,List<PhotoInfo> photosList ,List<AlbumInfo> albumsList ){
		if(requestBatch==null){
	        requestBatch = new RequestBatch();
		}
		
		if(photosList==null){
			photosList = new ArrayList<PhotoInfo>() ;
		}
		
		
		
		if(album==null){
			album = new AlbumInfo() ;
			String albumIdAndName = String.format("Photos Of %s", user.getmName()) ;
			album.setmAlbumId(albumIdAndName) ;
			album.setmAlbumName(albumIdAndName) ;
			album.setVirtualAlbum(true) ;
		}
		
		if(albumsList==null){
			albumsList = new ArrayList<AlbumInfo>() ;
		}
		
		String graphPath = String.format("/%s/photos",user.getmUserId()) ;
		
		Request request = new Request(
				Session.getActiveSession(),
				graphPath, 
				parameters, 
				HttpMethod.GET, new GetPhotosOfUserRequestCallback(context ,requestBatch, user ,album ,photosList ,albumsList )) ;
		
		requestBatch.add(request) ;
		requestBatch.executeAsync();
		
	}
	
	
	
	/** 
	 *  user adapter
	 */
	class FriendsAdapter extends  BaseAdapter{
        private LayoutInflater mInflater ;
		
		private List<User> mDataList ;
		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private RelativeLayout.LayoutParams mImageViewLayoutParams;
		private int mTopViewHeight = 0;
		
		public FriendsAdapter(Context context, List<User> dataList) {
			mDataList = dataList ;
			mContext = context ;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT) ;
			mTopViewHeight =  getResources().getDimensionPixelSize(R.dimen.top_view_height) ;
		}
		
		@Override
		public int getCount() {
			 // If columns have yet to be determined, return no items
            if (getNumColumns() == 0) {
                return 0;
            }
            
            
			return mDataList.size()+mNumColumns;
		}

		@Override
		public User getItem(int position) {
			 return position < mNumColumns ?
	                    null : mDataList.get(position - mNumColumns) ;
		}

		@Override
		public long getItemId(int position) {
			return position < mNumColumns ? 0 : position - mNumColumns;
		}

		@Override
		public int getViewTypeCount() {
			 // Two types of views, the normal ImageView and the top row of empty views
            return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			return (position < mNumColumns) ? 1 : 0;
		}
		 
		@Override
		public boolean hasStableIds() {
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
			final ViewHolder holder ;
			
			
			
			if (convertView == null) { // if it's not recycled, instantiate and initialize
				convertView = mInflater.inflate(R.layout.album_grid_item, parent, false) ;
				holder = new ViewHolder() ;
				holder.vImageUserCoverPhoto = (ImageCheckBoxView) convertView.findViewById(R.id.image_album_cover);
				holder.vTextUserName = (TextView) convertView.findViewById(R.id.text_name);
				holder.vTextCount = (TextView) convertView.findViewById(R.id.text_photos_num);
				holder.vTextCount.setVisibility(View.INVISIBLE) ;
				holder.vImageUserCoverPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
				holder.vImageUserCoverPhoto.setLayoutParams(mImageViewLayoutParams);
				convertView.setTag(holder) ;
			}else { // Otherwise re-use the converted view
				holder = (ViewHolder) convertView.getTag() ;  
	        }
			
			User data = (User)getItem(position) ;
			if (holder.vImageUserCoverPhoto.getLayoutParams().height != mItemHeight) {

				holder.vImageUserCoverPhoto.setLayoutParams(mImageViewLayoutParams);

			}
			holder.vTextUserName.setText(data.getmName());
			
//			mImageFetcher.loadImage(data.getmProfilePictureUrl(), holder.vImageUserCoverPhoto,false);
			mImageFetcher.loadImage(data.getmProfilePictureUrl(), data.getmProfilePictureUrl() , holder.vImageUserCoverPhoto,LoadImageType.WEB_IMAGE);
			
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
			private ImageCheckBoxView vImageUserCoverPhoto;
			private TextView vTextUserName;
			private TextView vTextCount;
		}
		
	}

	
	
	/**
	 * get Album List Request Callback
	 */
	class GetUserAlbumsListRequestCallback implements Request.Callback{
		private Context context  ;
		private RequestBatch requestBatch  ;
		private User user ;
		private List<AlbumInfo> albumsList ;
		

		public GetUserAlbumsListRequestCallback(Context context ,RequestBatch requestBatch, User user, List<AlbumInfo> albumsList) {
			super();
			this.context = context ;
			this.requestBatch = requestBatch;
			this.user = user ;
			this.albumsList = albumsList;
		}

		@Override
		public void onCompleted(Response response) {
			requestBatch.clear() ;
			

			GraphObject graphObject = response.getGraphObject();
//			Log.v("sunny", "sunny_response :" + response.toString());
			if(graphObject!=null){
				JSONObject jsonObj = graphObject.getInnerJSONObject();
				List<AlbumInfo> albumsListPage = null ;
				if (jsonObj != null) {
					albumsListPage  = getAlbumsFromResponse(jsonObj);
					JSONObject jsonPaging = jsonObj.optJSONObject("paging") ;
					
					if(albumsListPage!=null){
						if(albumsList==null){
							albumsList = albumsListPage ;
						}else {
							albumsList.addAll(albumsListPage) ;
						}
					}
					
					if(jsonPaging!=null && jsonPaging.has("next")){
						String next  = jsonPaging.optString("next") ;
	            		Bundle parametersBundle =((PhotoSelectMainFragmentActivity)context).parseNextNode(next) ;
	            		getUserAlbums(context ,requestBatch,  parametersBundle, user, albumsList) ;
					}else {
						
						Bundle parameters = new Bundle() ;
						parameters.putInt("limit", AppConstants.LIMIT_COUNT) ;
						
						AlbumInfo photosOfUserAlbum = new AlbumInfo() ;
						String albumIdAndName = String.format("Photos Of %s", user.getmName()) ;
						photosOfUserAlbum.setmAlbumId(albumIdAndName) ;
						photosOfUserAlbum.setmAlbumName(albumIdAndName) ;
						photosOfUserAlbum.setVirtualAlbum(true) ;
						
						getPhotosOfUser(context , requestBatch, parameters, user, photosOfUserAlbum , null , albumsList) ;
						
					}
					
					
				}
			}
			
		
		
			
		}
		
		/**
		 * parse JSON then get the album
		 */
		private List<AlbumInfo> getAlbumsFromResponse(JSONObject jsonObj) {
			List<AlbumInfo> albumsFromFacebook = null;
			if (jsonObj != null) {
				JSONArray dataArray = jsonObj.optJSONArray("data");
				if (dataArray != null && dataArray.length() > 0) {
					albumsFromFacebook = new ArrayList<AlbumInfo>();
					for (int i = 0; i < dataArray.length(); i++) {
						JSONObject albumJSONObj = dataArray.optJSONObject(i);
						if (albumJSONObj != null) {
							
							AlbumInfo album = new AlbumInfo();
							album.setmAlbumId(albumJSONObj.optString("id", ""));
							album.setmAlbumName(albumJSONObj.optString("name", ""));
							album.setPhotoNum(albumJSONObj.optInt("count", 0));
							album.setCoverId(albumJSONObj.optString("cover_photo", ""));
							album.setCoverPath(albumJSONObj.optString("cover_photo", "")) ;
							albumsFromFacebook.add(album);
							

						}

					}
				}
			}

			return albumsFromFacebook;

		}
		
	}
	
	
	/**
	 * get Photos of user then use the photos to build an album , add it to the friend's album list
	 * @author sunny
	 *
	 */
	class GetPhotosOfUserRequestCallback implements Request.Callback{
		private Context context ;
		private RequestBatch requestBatch  ;
		private User user ;
		private AlbumInfo album ;
		private List<PhotoInfo> photosList ;
		private List<AlbumInfo> albumsList ;
		

		public GetPhotosOfUserRequestCallback(Context context ,RequestBatch requestBatch, User user, AlbumInfo album,
				 List<PhotoInfo> photosList ,List<AlbumInfo> albumList ) {
			super();
			this.context = context ;
			this.requestBatch = requestBatch;
			this.user = user;
			this.album = album;
			this.photosList = photosList;
			this.albumsList = albumList ;

		}


		@Override
		public void onCompleted(Response response) {
			requestBatch.clear() ;
			GraphObject graphObject = response.getGraphObject();
			if(graphObject!=null){
//				Log.v("sunny", "sunny_response :" + response.toString());
				JSONObject jsonObj = graphObject.getInnerJSONObject();
//				Log.v("sunny", "photos of you: "+jsonObj.toString()) ;
				List<PhotoInfo> mPhotosOfUserPage = null ;
				
				if (jsonObj != null) {
					mPhotosOfUserPage = getPhotosOfUserFromResponse(jsonObj, album);
					JSONObject jsonPaging = jsonObj.optJSONObject("paging");

					if (mPhotosOfUserPage != null) {

						if (photosList == null) {
							photosList = mPhotosOfUserPage;
						} else {
							photosList.addAll(mPhotosOfUserPage);
						}

					}
					
					if(jsonPaging!=null && jsonPaging.has("next")){
	                	String next  = jsonPaging.optString("next") ;
	            		Bundle parametersBundle =((PhotoSelectMainFragmentActivity)context).parseNextNode(next) ;
	            		getPhotosOfUser(context ,requestBatch, parametersBundle, user, album, photosList ,albumsList) ;
					}else {
						if(vProgressDialog!=null && vProgressDialog.isShowing()){
	    				    vProgressDialog.cancel() ;
	    			    }
						album.setmPhotosInAlbum(photosList) ;
                		album.setPhotoNum(photosList.size()) ;
                		//add the photos of user album to the first index
                		albumsList.add(0, album) ;

						
						
						if (albumsList != null ) {
							//TODO REPLACE WITH ALBUM FRAGMENT
							
							Bundle bundle = new Bundle();
							AlbumHolder albumHolder = new AlbumHolder() ;
							albumHolder.setAlbums(albumsList) ;
							bundle.putSerializable("albumsHolder", (Serializable) albumHolder);
							bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, photoSource);
							bundle.putString(AppConstants.KEY_PRODUCT_ID, productId) ;
							bundle.putSerializable("user_friend", user);
							Fragment fragment = AlbumSelectFragment.newInstance(bundle) ;
							mListener.repalceWithNewFragment(fragment) ;
							
							
//							Intent intent = new Intent(getActivity(), AlbumSelectActivity.class);
//							Bundle bundle = new Bundle();
//							AlbumHolder albumHolder = new AlbumHolder() ;
//							albumHolder.setAlbums(albumsList) ;
//							bundle.putSerializable("albumsHolder", (Serializable) albumHolder);
//							bundle.putSerializable("photoSource", photoSource);
//							bundle.putString(AppConstants.KEY_PRODUCT_ID, productId) ;
//							bundle.putSerializable("user_friend", user);
//							intent.putExtra("bundle", bundle);
//							startActivity(intent);
						}
						
						
						
					}

				}
				
			}
			
		}
		
		
		/**
		 * get photo list of the user
		 * 
		 * @param jsonObj
		 * @param album
		 * @return
		 */
		private List<PhotoInfo> getPhotosOfUserFromResponse (JSONObject jsonObj ,AlbumInfo album){
			List<PhotoInfo> mPhotosOfYou= null ;
			if(jsonObj!=null){
				JSONArray photosArray = jsonObj.optJSONArray("data") ;
				if(photosArray!=null && photosArray.length()>0){
					mPhotosOfYou = new ArrayList<PhotoInfo>() ;
					for (int i = 0; i < photosArray.length(); i++) {
						JSONObject jsonObject = photosArray.optJSONObject(i) ;
						if(jsonObject!=null){
							PhotoInfo photoInfo = buidlerPhoto(jsonObject, album) ;
							mPhotosOfYou.add(photoInfo) ;
						}
						
					}
				}
			}
			
			
			return mPhotosOfYou ;
			
		}
		
		/**
		 * parse JSON to build a photo object
		 * @param jsonObject
		 * @param album
		 * @return
		 */
		private PhotoInfo buidlerPhoto(JSONObject jsonObject,AlbumInfo album){
			PhotoInfo photoInfo = new PhotoInfo() ;
			String photoId = jsonObject.optString("id") ;
			String bucketId = album.getmAlbumId() ;
			String bucketName = album.getmAlbumName() ;
			photoInfo.setPhotoId(photoId) ;
			photoInfo.setBucketId(bucketId) ;
			photoInfo.setBucketName(bucketName) ;
			photoInfo.setPhotoSource(photoSource) ;
			photoInfo.setFlowType(AppContext.getApplication().getFlowType()) ;
			photoInfo.setProductId(productId==null ? "" : productId);
			chooseBestQualityImage(jsonObject, photoInfo) ;
			chooseClosestThumnailImage(jsonObject, photoInfo, 
					AppConstants.THUMNAIL_STANDARD_HEIGHT, AppConstants.THUMNAIL_STANDARD_WIDTH ) ;
			photoInfo.setPhotoPath(photoInfo.getThumbnailUrl()) ;
			return photoInfo ;
		}
		
		
		/**
		 * choose best image for source url
		 * @param jsonObject
		 * @param photoInfo
		 */
		private void chooseBestQualityImage(JSONObject jsonObject,PhotoInfo photoInfo){
			JSONArray images = jsonObject.optJSONArray("images")  ;
			if(images!=null && images.length()>0){
				
				JSONObject bestImage = images.optJSONObject(0) ;
				
                for (int i = 0; i < images.length(); i++) {
					
					int bestWidth = bestImage.optInt("width") ;
					int bestHeight = bestImage.optInt("height") ;
					int bestSize  = bestWidth * bestHeight ;
					
					
					JSONObject imageObj = images.optJSONObject(i) ;
					int imageWidth = imageObj.optInt("width") ;
					int imageHeight = imageObj.optInt("height") ;
					int imageSize  = imageWidth * imageHeight ;
					
					if(bestSize < imageSize){
						bestImage = imageObj ;
					}
					
				}
				
				photoInfo.setWidth(bestImage.optInt("width")) ;
				photoInfo.setHeight(bestImage.optInt("height")) ;
				photoInfo.setSourceUrl(bestImage.optString("source")) ;
				
				
				
			}else {
				
				photoInfo.setWidth(jsonObject.optInt("width")) ;
				photoInfo.setHeight(jsonObject.optInt("height")) ;
				photoInfo.setSourceUrl(jsonObject.optString("source")) ;
				
				
				
			}
			
		}
		
		/**
		 * choose the closest size to the request size
		 * @param jsonObject
		 * @param photoInfo
		 * @param reqHeight   the request height
		 * @param reqWidth    the request width
		 */
		private void chooseClosestThumnailImage(JSONObject jsonObject ,PhotoInfo photoInfo , int reqHeight , int reqWidth){
			JSONArray images = jsonObject.optJSONArray("images")  ;
			if(images!=null && images.length()>0){
				JSONObject closestImage = images.optJSONObject(0) ;
				int reqSize = reqHeight * reqWidth ;
				
				 for (int i = 0; i < images.length(); i++) {
					 int closestWidth = closestImage.optInt("width") ;
					 int closestHeight = closestImage.optInt("height") ;
					 int closestSize  = closestWidth * closestHeight ;
					 int closestDiffAbs = Math.abs(closestSize - reqSize ) ;
					 
					 JSONObject imageObj = images.optJSONObject(i) ;
					 int imageWidth = imageObj.optInt("width") ;
					 int imageHeight = imageObj.optInt("height") ;
					 int imageSize  = imageWidth * imageHeight ;
					 int diffAbs = Math.abs(imageSize - reqSize) ;
					 
					 
					 if( diffAbs < closestDiffAbs ){
						 closestImage = imageObj ;
					 }
					 
					 
				 }
				 
				 photoInfo.setThumbnailUrl(closestImage.optString("source")) ;
				
				
			}else {
				 photoInfo.setThumbnailUrl(jsonObject.optString("picture")) ;
				
			}
		}
	}
	
	
	
	
}
