package com.kodak.kodak_kioskconnect_n2r.fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.AppConstants;
import com.AppConstants.LoadImageType;
import com.AppConstants.PhotoSource;
import com.AppContext;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.activity.PhotoSelectMainFragmentActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumHolder;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.User;
import com.kodak.kodak_kioskconnect_n2r.bean.UserHolder;

public class FacebookSelectFragment extends Fragment {
	private View rootView ;
	private RelativeLayout vRelativeLayoutPhotosOfYou;
	private RelativeLayout vRelativeLayoutYourPhotos;
	private RelativeLayout vRelativeLayoutYourFriends;
	
	private ImageView vImageViewPhotosOfYou ;
	private ImageView vImageViewYourPhotos ;
	private ImageView vImageViewYourFriends ;
	
	private int mImageThumbSize;
	private ImageFetcher mImageFetcher;
	private static final String IMAGE_CACHE_DIR = "thumbs";
	
	private PhotoSource photoSource ;
	private String productId = "";
	private ProgressDialog vProgressDialog;
	ICommunicating mListener ;
	
	private boolean isNeedFacebookLogin ;
	public FacebookSelectFragment(){
		
	}
	public static FacebookSelectFragment newInstance(Bundle b) {
		FacebookSelectFragment f = new FacebookSelectFragment();
       
        f.setArguments(b);
        return f;
     }	
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mListener = (ICommunicating) activity ;
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
	public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
		rootView  = inflater.inflate(R.layout.fragment_facebook_select, container,false) ;
		vRelativeLayoutPhotosOfYou = (RelativeLayout) rootView.findViewById(R.id.relative_layout_photos_of_you);
		vRelativeLayoutYourPhotos = (RelativeLayout) rootView.findViewById(R.id.relative_layout_your_photos);
		vRelativeLayoutYourFriends = (RelativeLayout) rootView.findViewById(R.id.relative_layout_your_friends);
		vImageViewPhotosOfYou = (ImageView) rootView.findViewById(R.id.image_photos_of_you) ;
		vImageViewYourPhotos = (ImageView) rootView.findViewById(R.id.image_your_photos) ;
		vImageViewYourFriends = (ImageView) rootView.findViewById(R.id.image_your_friends) ;
		
		
		if(isNeedFacebookLogin){
			rootView.setVisibility(View.INVISIBLE) ;
		}else {
			rootView.setVisibility(View.VISIBLE) ;
		}
		
		setEvents() ;
		if(isNeedFacebookLogin){
			((PhotoSelectMainFragmentActivity)getActivity()).facebookLogin() ;
		}
		

       
		
		
		return rootView;
	}
	

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mListener.setTitleText(getString(R.string.Facebook_Input)) ;
		if(!TextUtils.isEmpty(AppContext.getApplication().getFacebook_photos_of_you_cover())){
//			mImageFetcher.loadImage(AppContext.getApplication().getFacebook_photos_of_you_cover(), vImageViewPhotosOfYou, false) ;
			mImageFetcher.loadImage(AppContext.getApplication().getFacebook_photos_of_you_cover(), 
					                AppContext.getApplication().getFacebook_photos_of_you_cover(),
					                vImageViewPhotosOfYou, 
					                LoadImageType.WEB_IMAGE) ;
			
		}
		
		if(!TextUtils.isEmpty(AppContext.getApplication().getFacebook_your_photos_cover())){
//			mImageFetcher.loadImage(AppContext.getApplication().getFacebook_your_photos_cover(), vImageViewYourPhotos, false) ;
			mImageFetcher.loadImage(AppContext.getApplication().getFacebook_your_photos_cover(), 
	                                AppContext.getApplication().getFacebook_your_photos_cover(),
	                                vImageViewYourPhotos, 
	                                LoadImageType.WEB_IMAGE) ;
		}
		
		if(!TextUtils.isEmpty(AppContext.getApplication().getFacebook_your_friends_cover())){
//			mImageFetcher.loadImage(AppContext.getApplication().getFacebook_your_friends_cover(), vImageViewYourFriends, false) ;
			mImageFetcher.loadImage(AppContext.getApplication().getFacebook_your_friends_cover(), 
                                    AppContext.getApplication().getFacebook_your_friends_cover(),
                                    vImageViewYourFriends, 
                                    LoadImageType.WEB_IMAGE) ;
			
		}
		
		mImageFetcher.setExitTasksEarly(false);
		
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
		super.onDestroy();
		mImageFetcher.closeCache();
	}
	
	private void initData() {
		Bundle bundle = getArguments() ;
		if(bundle!=null){
			photoSource = (PhotoSource) bundle.getSerializable(AppConstants.KEY_PHOTO_SOURCE);
			productId = bundle.getString(AppConstants.KEY_PRODUCT_ID) ;
			isNeedFacebookLogin = bundle.getBoolean("isNeedFacebookLogin",false) ;
		}
		if(photoSource==null){
			photoSource = PhotoSource.FACEBOOK ;
		}
		
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.imagewait96x96);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
		
	}
	
	private void setEvents() {
		/**
		 * GET PHOTOS OF YOU
		 */
		vRelativeLayoutPhotosOfYou.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(!Connection.isConnected(getActivity())){
					((PhotoSelectMainFragmentActivity)getActivity()).showNoConnectionDialog();
				}else {
					if(vProgressDialog==null){
						vProgressDialog = new ProgressDialog(getActivity());
						vProgressDialog.setCancelable(true);
						vProgressDialog.show();
					}else {
						if (!vProgressDialog.isShowing()) {
							vProgressDialog.show();
						}
					}
					
					((PhotoSelectMainFragmentActivity)getActivity())
					   .setLocalyticsEventAttr(PhotoSelectMainFragmentActivity.KEY_FACEBOOK_PHOTOS_OF_YOU_SELECTED, PhotoSelectMainFragmentActivity.VALUE_YES) ;
					
					RequestBatch requestBatch = new RequestBatch();
					Bundle parameters = new Bundle() ;
					parameters.putInt("limit", AppConstants.LIMIT_COUNT) ;
					
					AlbumInfo photosOfYouAlbum = new AlbumInfo() ;
					photosOfYouAlbum.setmAlbumId("Photos Of You") ;
					photosOfYouAlbum.setmAlbumName("Photos Of You") ;
					photosOfYouAlbum.setVirtualAlbum(true) ;
					getPhotosOfYou(getActivity() ,requestBatch, parameters , photosOfYouAlbum , null ) ;
					
				}
			}
		}) ;
		
		/**
		 * GET USER'S ALBUMS
		 */
		vRelativeLayoutYourPhotos.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!Connection.isConnected(getActivity())){
					((PhotoSelectMainFragmentActivity)getActivity()).showNoConnectionDialog();
				}else {
					
					if (vProgressDialog == null) {
						vProgressDialog = new ProgressDialog(getActivity());
						vProgressDialog.setCancelable(true);
						vProgressDialog.show();
					} else {
						if (!vProgressDialog.isShowing()) {
							vProgressDialog.show();
						}
					}
					
					((PhotoSelectMainFragmentActivity)getActivity())
					   .setLocalyticsEventAttr(PhotoSelectMainFragmentActivity.KEY_FACEBOOK_YOUR_PHOTOS_SELECTED, PhotoSelectMainFragmentActivity.VALUE_YES) ;
					RequestBatch requestBatch = new RequestBatch();
					Bundle parameters = new Bundle() ;
					parameters.putInt("limit", AppConstants.LIMIT_COUNT) ;
					getUserAlbums(getActivity(),requestBatch , parameters, null);
					
					
				}
				
			}
		}) ;
		
		vRelativeLayoutYourFriends.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!Connection.isConnected(getActivity())){
					((PhotoSelectMainFragmentActivity)getActivity()).showNoConnectionDialog();
				}else {
					if (vProgressDialog == null) {
						vProgressDialog = new ProgressDialog(getActivity());
						vProgressDialog.setCancelable(true);
						vProgressDialog.show();
					} else {
						if (!vProgressDialog.isShowing()) {
							vProgressDialog.show();
						}
					}
					((PhotoSelectMainFragmentActivity)getActivity())
					   .setLocalyticsEventAttr(PhotoSelectMainFragmentActivity.KEY_FACEBOOK_YOUR_FRIENDS_SELECTED, PhotoSelectMainFragmentActivity.VALUE_YES) ;
					RequestBatch requestBatch = new RequestBatch();
					Bundle parameters = new Bundle() ;
					parameters.putInt("limit", AppConstants.LIMIT_COUNT) ;
					getFriendsList(getActivity() ,requestBatch, parameters, null);
				}
				
			}
		}) ;
		
		
	}
	
	
	/**
	 * get photos of you
	 */
	private void getPhotosOfYou(Context context ,RequestBatch requestBatch, Bundle parameters,AlbumInfo album ,List<PhotoInfo> photosList ){
		if(requestBatch==null){
	        requestBatch = new RequestBatch();
		}
		
		if(photosList==null){
			photosList = new ArrayList<PhotoInfo>() ;
		}
		
		if(album==null){
			album = new AlbumInfo() ;
			album.setmAlbumId("Photos Of You") ;
			album.setmAlbumName("Photos Of You") ;
			album.setVirtualAlbum(true) ;
		}
		
		String graphPath = "/me/photos" ;
		
		Request request = new Request(
				Session.getActiveSession(),
				graphPath, 
				parameters, 
				HttpMethod.GET, new GetPhotosOfYouRequestCallback(context ,requestBatch, album ,photosList)) ;
		
		requestBatch.add(request) ;
		requestBatch.executeAsync();
		
		
	}
	
	/**
	 * get the self albums from Facebook
	 */
	private void getUserAlbums(Context context ,RequestBatch requestBatch, Bundle parameters ,List<AlbumInfo> albumsList) {
		
		if(requestBatch==null){
	        requestBatch = new RequestBatch();
				
		}
		
		if(albumsList==null){
			albumsList = new ArrayList<AlbumInfo>() ;
		}
		
	
		
		String graphPath = "/me/albums";
		Request request = new Request(
				Session.getActiveSession(),
				graphPath, 
				parameters, 
				HttpMethod.GET,
				new GetMineAlbumsListRequestCallback(context,requestBatch, albumsList));
//		request.setVersion("v1.0") ;
		requestBatch.add(request) ;
		requestBatch.executeAsync();

	}
	
	
	/**
	 * get your friends list
	 */
	private void getFriendsList(Context context , RequestBatch requestBatch , Bundle parameters ,List<User> friendsList){
		
		if(requestBatch==null){
	        requestBatch = new RequestBatch();
				
		}
		if(friendsList == null){
			 friendsList = new ArrayList<User>() ;
		}
		
		String graphPath = "/me/friends";
		Request request = new Request(
				Session.getActiveSession(), 
				graphPath, 
				parameters,
				HttpMethod.GET, 
				new GetFriendsRequestCallback(context , requestBatch, friendsList) );
		
//		request.setVersion("v1.0") ;
		 
		requestBatch.add(request);
		    
		requestBatch.executeAsync() ;
		
	}
	
	
	
	/**
	 * get photos of you Request Callback
	 */
	class GetPhotosOfYouRequestCallback  implements Request.Callback{
		private Context context ;
		private RequestBatch requestBatch  ;
		private AlbumInfo album ;
		private List<PhotoInfo> photosList ;
		

		public GetPhotosOfYouRequestCallback(Context context ,RequestBatch requestBatch, AlbumInfo album, List<PhotoInfo> photosList) {
			super();
			this.context = context ;
			this.requestBatch = requestBatch;
			this.album = album;
			this.photosList = photosList;
		}


		@Override
		public void onCompleted(Response response) {
		
			requestBatch.clear() ;
			GraphObject graphObject = response.getGraphObject();
			if(graphObject!=null){
//				Log.v("sunny", "sunny_response :" + response.toString());
				JSONObject jsonObj = graphObject.getInnerJSONObject();
//				Log.v("sunny", "photos of you: "+jsonObj.toString()) ;
				List<PhotoInfo> mPhotosOfYouPage = null ;
				if (jsonObj != null) {
					mPhotosOfYouPage = getPhotosOfYouFromResponse(jsonObj, album) ;
					JSONObject jsonPaging = jsonObj.optJSONObject("paging") ;
					
	                if(mPhotosOfYouPage!=null){
						
						if(photosList==null){
							photosList= mPhotosOfYouPage ;
						}else {
							photosList.addAll(mPhotosOfYouPage) ;
						}
						
					}
	                
	                if(jsonPaging!=null && jsonPaging.has("next")){
	                	String next  = jsonPaging.optString("next") ;
	            		Bundle parametersBundle = ((PhotoSelectMainFragmentActivity)context).parseNextNode(next) ;
	            		getPhotosOfYou(context ,requestBatch, parametersBundle, album, photosList) ;
	                }else {
	                	
	                	if(vProgressDialog!=null && vProgressDialog.isShowing()){
	    				    vProgressDialog.cancel() ;
	    			    }
	                	
	                	if(photosList!=null){
	                		//TODO REPLACE FRAGMENT
	                		album.setmPhotosInAlbum(photosList) ;
	                		album.setPhotoNum(photosList.size()) ;
	                		
	                		Bundle bundle = new Bundle() ;
	                		bundle.putSerializable("album", album) ;
	                		bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, photoSource) ;
	                	    Fragment f = PhotoSelectFragment.newInstance(bundle) ;
	                    	mListener.repalceWithNewFragment(f) ;
	                		
	                		if(TextUtils.isEmpty(AppContext.getApplication().getFacebook_photos_of_you_cover())){
	                			if(photosList.size()>0){
	                				AppContext.getApplication().setFacebook_photos_of_you_cover(photosList.get(0).getThumbnailUrl()) ;
	                			}
	                		}
	                		
	                		
	                	}
	                	
	                	
	                }
	                
					
					
				}
				
				
				
			}
		}
		
		
		private List<PhotoInfo> getPhotosOfYouFromResponse (JSONObject jsonObj ,AlbumInfo album){
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
	
	
	/**
	 * get  Album List Request Callback
	 */
	class GetMineAlbumsListRequestCallback implements Request.Callback{
		private Context context ;
		private RequestBatch requestBatch  ;
		private List<AlbumInfo> albumsList ;

		public GetMineAlbumsListRequestCallback(Context context ,RequestBatch requestBatch, List<AlbumInfo> albumsList) {
			super();
			this.context = context ;
			this.requestBatch = requestBatch;
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
//						Log.d("sunny", "sunny albumsListPage.size()=="+albumsListPage.size()) ;
						if(albumsList==null){
							albumsList = albumsListPage ;
						}else {
							albumsList.addAll(albumsListPage) ;
						}
					}
					
					if(jsonPaging!=null && jsonPaging.has("next")){
						String next  = jsonPaging.optString("next") ;
	            		Bundle parametersBundle =((PhotoSelectMainFragmentActivity)context).parseNextNode(next) ;
	            		getUserAlbums(context ,requestBatch, parametersBundle, albumsList) ;
					}else {
						
						if(vProgressDialog!=null && vProgressDialog.isShowing()){
	    				    vProgressDialog.cancel() ;
	    			    }
//						Log.v("sunny", "sunny end :"+System.currentTimeMillis()) ;
						
						if(albumsList!=null){
//							Log.d("sunny", "sunny albumlist.size()=="+albumsList.size()) ;
							// TODO REPLACE WITH FRAGMENT 
							
							Bundle bundle = new Bundle();
							AlbumHolder albumHolder = new AlbumHolder();
							albumHolder.setAlbums(albumsList);
							bundle.putSerializable("albumsHolder", albumHolder);
							bundle.putString(AppConstants.KEY_PRODUCT_ID, productId);
							bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, photoSource);
							Fragment fragment = AlbumSelectFragment.newInstance(bundle);
							mListener.repalceWithNewFragment(fragment) ;
							
							if(TextUtils.isEmpty(AppContext.getApplication().getFacebook_your_photos_cover())){
	                			if(albumsList.size()>0){
	                				AlbumInfo albumFirst = albumsList.get(0) ;
	                				String url = "https://graph.facebook.com/" + albumFirst.getmAlbumId() + "/picture" + "?access_token="
	            							+ Session.getActiveSession().getAccessToken() ;
	                				AppContext.getApplication().setFacebook_your_photos_cover(url) ;
	                			}
	                		}
							
						
						}
						
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
	 * getFriends Request CallBack
	 * @author sunny
	 *
	 */
	class GetFriendsRequestCallback implements Request.Callback{
		private Context context ;
		private RequestBatch requestBatch  ;
		private List<User> friendsList ;
		
	    protected GetFriendsRequestCallback(Context context ,RequestBatch requestBatch , List<User> friendsList){
	    	super();
	    	this.context = context ;
	    	this.requestBatch = requestBatch ;
	    	this.friendsList = friendsList ;
	    }
		

		@Override
		public void onCompleted(Response response) {
			requestBatch.clear() ;
            GraphObject graphObject = response.getGraphObject();
//            Log.v("sunny", "sunny_response :" + response.toString() ) ;
            if(graphObject!=null){
                JSONObject jsonObj = graphObject.getInnerJSONObject() ;
//                Log.v("sunny", "friends: "+jsonObj.toString()) ;
                if(jsonObj!=null){

                     List<User> friendsListPage  = getFriendsListFromResponse(jsonObj) ;
                     JSONObject jsonPaging = jsonObj.optJSONObject("paging") ;
                     
                     if(friendsListPage!=null){
                    	 
                    	 
                    	 if(friendsList==null){
                    		 friendsList= friendsListPage ;
                    	 }else {
                    		 friendsList.addAll(friendsListPage) ;
                    	 }
                     }
                    
                	if(jsonPaging!=null && jsonPaging.has("next")){
                		String next  = jsonPaging.optString("next") ;
                		Bundle parametersBundle =((PhotoSelectMainFragmentActivity)context).parseNextNode(next) ;
                		getFriendsList(context , requestBatch, parametersBundle,friendsList) ;
                	
                		
                	}else {
                		
                    	if(vProgressDialog!=null && vProgressDialog.isShowing()){
        				    vProgressDialog.cancel() ;
        			    }
                    	if(friendsList!=null ){

                    		Collections.sort(friendsList) ;
                    		//TODO REPLACE WITH FRIENDS FRAGMENT
                    		Bundle bundle = new Bundle();
        					UserHolder userHolder = new UserHolder() ;
        					userHolder.setUsers(friendsList) ;
        					bundle.putSerializable("friendsListHolder", (Serializable) userHolder);
        					bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, photoSource);
        					bundle.putString(AppConstants.KEY_PRODUCT_ID, productId) ;
                    		Fragment fragment = FacebookFriendsFragment.newInstance(bundle) ;
                    		mListener.repalceWithNewFragment(fragment);
                    		
                    		if(TextUtils.isEmpty(AppContext.getApplication().getFacebook_your_friends_cover())){
	                			if(friendsList.size()>0){
	                				AppContext.getApplication().setFacebook_your_friends_cover(friendsList.get(0).getmProfilePictureUrl()) ;
	                				
	                			}
	                		}
                    		
                    		
//                    		Intent intent = new Intent(context, FriendsListActivity.class);
//        					Bundle bundle = new Bundle();
//        					UserHolder userHolder = new UserHolder() ;
//        					userHolder.setUsers(friendsList) ;
//        					bundle.putSerializable("friendsListHolder", (Serializable) userHolder);
//        					bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, photoSource);
//        					bundle.putString(AppConstants.KEY_PRODUCT_ID, productId) ;
//        					intent.putExtra("bundle", bundle);
//        					startActivity(intent);
                    	}else {
                    		//show no friends dialog
                    		
                    		
                    	}
                    	
                		
                	}
                    
                    
                }
            }
            

            
		
		}
		
		private List<User> getFriendsListFromResponse(JSONObject jsonObj){
			List<User> friendsList = null ;
			if(jsonObj !=null){
				JSONArray dataArray = jsonObj.optJSONArray("data");
				if (dataArray != null && dataArray.length() > 0) {
					friendsList = new ArrayList<User>() ;
					for (int i = 0; i < dataArray.length(); i++) {
						JSONObject userObj = dataArray.optJSONObject(i);
						if(userObj!=null){
							
							User friend = new User() ;
							friend.setmUserId(userObj.optString("id")) ;
							friend.setmName(userObj.optString("name")) ;
							
							String mProfilePictureUrl =  "https://graph.facebook.com/" + friend.getmUserId() + "/picture" + "?access_token="
									+ Session.getActiveSession().getAccessToken() ;
							friend.setmProfilePictureUrl(mProfilePictureUrl) ;
							
							friendsList.add(friend) ;
							
						}
						
					}
				}
				
				
			}
			
			return friendsList ;
			
			
		}
		
	}
	
	
	
//    private class SessionStatusCallback implements Session.StatusCallback {
//        @Override
//        public void call(Session session, SessionState state, Exception exception) {
//           Session sessionResult = Session.getActiveSession() ;
//        	
//        	String token = sessionResult.getAccessToken() ;
//        	if(sessionResult.isOpened()){
//        		updateView(View.VISIBLE) ;
//        	}
//        }
//    }
	
}
