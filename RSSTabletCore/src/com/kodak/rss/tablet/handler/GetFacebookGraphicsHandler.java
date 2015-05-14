package com.kodak.rss.tablet.handler;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseHaveISMActivity;
import com.kodak.rss.tablet.adapter.FBKImageAdapter;
import com.kodak.rss.tablet.adapter.FacebookAlbumsAdapter;
import com.kodak.rss.tablet.adapter.FacebookFriendsAdapter;
import com.kodak.rss.tablet.adapter.FacebookGroupsAdapter;
import com.kodak.rss.tablet.adapter.FacebookPhotosAdapter;
import com.kodak.rss.tablet.facebook.AdpaterConstant;
import com.kodak.rss.tablet.facebook.FbkAlbum;
import com.kodak.rss.tablet.facebook.FbkGroup;
import com.kodak.rss.tablet.facebook.FbkObject;
import com.kodak.rss.tablet.facebook.FbkPhoto;
import com.kodak.rss.tablet.facebook.FbkUser;
import com.kodak.rss.tablet.facebook.HandlerConstant;
import com.kodak.rss.tablet.util.GridViewParamSetUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class GetFacebookGraphicsHandler extends Handler{
		
	private FacebookPhotosAdapter fbkPhotosAdapter;	
	public FBKImageAdapter fbkImageAdapter;
	public FacebookAlbumsAdapter fbkAlbumsAdapter;
	public FacebookFriendsAdapter fbkFriendsAdapter;
	public FacebookGroupsAdapter fbkGroupsAdapter;
			
	private FbkUser fbkMainUser;
	private ArrayList<FbkObject> fbkMainAlbums;		
	private ArrayList<FbkObject> FriendsUsers;	
	private ArrayList<FbkObject> fbkgroups;		
	private ArrayList<FbkObject> albums;
	private ArrayList<FbkObject> fbkPhotos;
				
	private Context mContenxt;
	private BaseHaveISMActivity activity;
	private GridViewParamSetUtil gridViewParamUtil;
	private OnGetIamgeOnFacebookListener onGetIamgeOnFacebookListener;
	private boolean isNotAlertYourGroupsInFBK;	
	private String displayStr;	
	private int selectPhotosPostion;
	private int selectFriendsPostion;
	
	private String facebookPrompt = "/Facebook";
	private String fbkPhotosOfYou = "/Photos of You";
	private String fbkYourPhotos = "/Your Photos";
	private String fbkYourFriends = "/Your Friends";
	private String fbkYourGroups = "/Your Groups";
	private boolean isHaveDialog;	
		
	public GetFacebookGraphicsHandler(Context context,GridViewParamSetUtil gridViewParamUtil) {		
		this.mContenxt = context;
		this.activity = (BaseHaveISMActivity) context;
		this.gridViewParamUtil = gridViewParamUtil;	
		
		facebookPrompt = "/"+context.getString(R.string.facebook);
		fbkPhotosOfYou = "/"+context.getString(R.string.photos_of)+" "+context.getString(R.string.you);
		fbkYourPhotos = "/"+context.getString(R.string.your_photos);
		fbkYourFriends = "/"+context.getString(R.string.your_friends);
		fbkYourGroups = "/"+context.getString(R.string.your_groups);						
	}
	
	public void setOnGetIamgeOnFacebookListener(OnGetIamgeOnFacebookListener listener){	
		this.onGetIamgeOnFacebookListener = listener;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(Message msg) {
		if (activity == null) return;
		if (activity.isFinishing()) return;	
		final int action = msg.what;
		Object msgObject = msg.obj;			
		switch (action) {
		case HandlerConstant.GET_FBKINFO_ERROR:	
			selectPhotosPostion = 0;									
			setViewsVisible(true);
			if (activity.progressBar != null) {
				activity.progressBar.setVisibility(View.GONE);		
			}				
			if (activity.adapterTpyeFlag == AdpaterConstant.SOURCES_ADAPTER_TPYE) {
				setBackViewVisible(false);
			}						
			if ((activity != null && !activity.isFinishing()) && msgObject != null && !isHaveDialog) {							
				android.content.DialogInterface.OnClickListener okOnClickListener  = new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						isHaveDialog = false;				
					}		
				};					
				new InfoDialog.Builder(activity).setMessage(R.string.fail_connect_facebook)
				.setPositiveButton(activity.getText(R.string.d_ok), okOnClickListener).create()
				.show();	
				isHaveDialog = true;				
			}	
			break;
			
		case HandlerConstant.GET_FBK_TOKEN:						
			if (activity.fbkAuth.isValid()) {								
				activity.adapterTpyeFlag = AdpaterConstant.FBK_PHOTOS_ADAPTER_TPYE;
				displayStr = facebookPrompt;
				setViewsVisible(true);
				setAllViewVisible(false);
				fbkPhotosAdapter = new FacebookPhotosAdapter(mContenxt,activity.mMemoryCache);
				gridViewParamUtil.initGridViewMargin(4,activity.adapterTpyeFlag,fbkPhotosAdapter);	
				activity.photoGridView.setAdapter(fbkPhotosAdapter);
			}else {								
				GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_TOKEN_INFO_START);	
			}
			break;	
			
		case HandlerConstant.GET_FBK_TOKEN_INFO_START:
			setViewsVisible(false);	
			setAllViewVisible(false);
			activity.fbkAuth.getInfoList(null,GetFacebookGraphicsHandler.this,AppConstants.getMainUser,true,false);			
			break;
		case HandlerConstant.GET_FBK_TOKEN_INFO_END:				
			ArrayList<FbkObject> mainUsers = (ArrayList<FbkObject>) msgObject;
			fbkMainUser = (FbkUser) mainUsers.get(0);
			if (fbkMainUser != null) {
				SharedPreferrenceUtil.saveFacebookUserId(mContenxt,fbkMainUser.ID);
				SharedPreferrenceUtil.saveFacebookFristName(mContenxt, fbkMainUser.frist_name);
				SharedPreferrenceUtil.saveFacebookLastName(mContenxt, fbkMainUser.last_name);
			}
			if (activity.sideMenu != null) {
				activity.sideMenu.notifyLoginStatusChanged();
			}	
			activity.adapterTpyeFlag = AdpaterConstant.FBK_PHOTOS_ADAPTER_TPYE;
			displayStr = facebookPrompt;
			setViewsVisible(true);
			setAllViewVisible(false);
			fbkPhotosAdapter = new FacebookPhotosAdapter(mContenxt,activity.mMemoryCache);
			gridViewParamUtil.initGridViewMargin(4,activity.adapterTpyeFlag,fbkPhotosAdapter);	
			activity.photoGridView.setAdapter(fbkPhotosAdapter);				
			break;
			
		case HandlerConstant.GET_FBK_USER_START:			
			setViewsVisible(false);
			setAllViewVisible(false);
			activity.fbkAuth.getInfoList(null,GetFacebookGraphicsHandler.this,AppConstants.getMainUser,false,true);
			break;	
		
		case HandlerConstant.GET_FBK_USER_END:			
			if (fbkMainUser == null) {
				ArrayList<FbkObject> mainUsers2 = (ArrayList<FbkObject>) msgObject;
				fbkMainUser = (FbkUser) mainUsers2.get(0);
				if (fbkMainUser != null) {
					SharedPreferrenceUtil.saveFacebookUserId(mContenxt,fbkMainUser.ID);
					SharedPreferrenceUtil.saveFacebookFristName(mContenxt, fbkMainUser.frist_name);
					SharedPreferrenceUtil.saveFacebookLastName(mContenxt, fbkMainUser.last_name);
				}										
			}				
			GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_MAIN_USER_PHOTOS_START);
			break;		
						
		case HandlerConstant.GET_FBK_MAIN_USER_PHOTOS_START:									
			setViewsVisible(false);
			setAllViewVisible(false);			
			fbkMainUser.handler = GetFacebookGraphicsHandler.this;		
			activity.fbkAuth.getPhotoInfoList(fbkMainUser);										
			break;
			
		case HandlerConstant.GET_FBK_MAIN_USER_PHOTOS_END:		
			displayStr = facebookPrompt+fbkPhotosOfYou;
			fbkPhotos = (ArrayList<FbkObject>) msgObject;
			setViewsVisible(true);
			setAllViewVisible(true);			
			activity.adapterTpyeFlag = AdpaterConstant.FBK_MAIN_PHOTOS_ADAPTER_TPYE;
			fbkImageAdapter = new FBKImageAdapter(mContenxt,fbkPhotos,activity.flowType,activity.mMemoryCache);
			gridViewParamUtil.initGridViewMargin(fbkPhotos.size(),activity.adapterTpyeFlag,fbkImageAdapter);	
			activity.photoGridView.setAdapter(fbkImageAdapter);	
			if (activity.allSelectButton != null) {	
				Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
				int photoSize = fbkPhotos.size();
				int selectedPhotoSize = 0;
				for (int i = 0; i < photoSize; i++) {
					fbkImageAdapter.isDeal[i] = true;
					String key = fbkPhotos.get(i).ID;
					int pos = activity.getPositionInList(currentPhotoBook.chosenpics, key);
					if (pos != -1) {
						fbkImageAdapter.isChice[i] = true;	
						selectedPhotoSize++;
					}			
				}						
				photoSize = photoSize + currentPhotoBook.chosenpics.size() - selectedPhotoSize;																		
				if (photoSize > activity.maxselectPhotoSize) {
					activity.isOutMaxSelectNum = true;
					activity.allSelectButton.setVisibility(View.GONE);
				}											
			}
			break;	

		case HandlerConstant.GET_FBK_MAIN_INFO_START:								
			if (fbkMainUser != null) {					
				GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_MAIN_INFO_END);	
			}else {
				setViewsVisible(false);
				setAllViewVisible(false);
				activity.fbkAuth.getInfoList(null,GetFacebookGraphicsHandler.this,AppConstants.getMainUser,false,false);
			}
			break;	
			
		case HandlerConstant.GET_FBK_MAIN_INFO_END:	
			if (fbkMainUser == null) {
				ArrayList<FbkObject> mainUsers2 = (ArrayList<FbkObject>) msgObject;
				fbkMainUser = (FbkUser) mainUsers2.get(0);
				if (fbkMainUser != null) {
					SharedPreferrenceUtil.saveFacebookUserId(mContenxt,fbkMainUser.ID);
					SharedPreferrenceUtil.saveFacebookFristName(mContenxt, fbkMainUser.frist_name);
					SharedPreferrenceUtil.saveFacebookLastName(mContenxt, fbkMainUser.last_name);
				}										
			}			
			if (fbkMainAlbums != null) {
				GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_MAIN_ALBUMS_END);
			}else {
				setViewsVisible(false);
				setAllViewVisible(false);
				fbkMainUser.handler = GetFacebookGraphicsHandler.this;
				activity.fbkAuth.getAlbumInfoList(AppConstants.fbkMainUser,fbkMainUser);
			}					
			break;	
			
		case HandlerConstant.GET_FBK_MAIN_ALBUMS_END:
			displayStr = facebookPrompt + fbkYourPhotos ;	
			if (fbkMainAlbums == null) {
				fbkMainAlbums = (ArrayList<FbkObject>) msgObject;
			}
			setViewsVisible(true);
			setAllViewVisible(false);
			activity.adapterTpyeFlag = AdpaterConstant.FBK_MAIN_ALBUMS_ADAPTER_TPYE;
			fbkAlbumsAdapter = new FacebookAlbumsAdapter(mContenxt,fbkMainAlbums,activity.mMemoryCache);	
			gridViewParamUtil.initGridViewMargin(fbkMainAlbums.size(),activity.adapterTpyeFlag,fbkAlbumsAdapter);	
			activity.photoGridView.setAdapter(fbkAlbumsAdapter);							
			break;		
			
		case HandlerConstant.GET_FBK_MAIN_IMAGE_START:
			setViewsVisible(false);	
			setAllViewVisible(false);
			FbkAlbum fbkMainAlbum = (FbkAlbum) msgObject;			
			fbkMainAlbum.handler = GetFacebookGraphicsHandler.this;						
			activity.fbkAuth.getPhotoInfoList(fbkMainAlbum,AppConstants.isfbkMain);				
			break;	
			
		case HandlerConstant.GET_FBK_MAIN_IMAGE_END:			
			fbkPhotos = (ArrayList<FbkObject>) msgObject;
			setViewsVisible(true);			
			if (fbkPhotos != null && fbkPhotos.size() > 0) {
				displayStr = facebookPrompt + fbkYourPhotos+"/"+fbkPhotos.get(0).bucketName;
				setAllViewVisible(true);				
				activity.adapterTpyeFlag = AdpaterConstant.FBK_MAIN_IMAGES_ADAPTER_TPYE;
				fbkImageAdapter = new FBKImageAdapter(mContenxt,fbkPhotos,activity.flowType,activity.mMemoryCache);
				gridViewParamUtil.initGridViewMargin(fbkPhotos.size(),activity.adapterTpyeFlag,fbkImageAdapter);	
				activity.photoGridView.setAdapter(fbkImageAdapter);
				if (activity.allSelectButton != null) {
					Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
					int photoSize = fbkPhotos.size();
					int selectedPhotoSize = 0;
					for (int i = 0; i < photoSize; i++) {
						fbkImageAdapter.isDeal[i] = true;
						String key = fbkPhotos.get(i).ID;
						int pos = activity.getPositionInList(currentPhotoBook.chosenpics, key);
						if (pos != -1) {
							fbkImageAdapter.isChice[i] = true;	
							selectedPhotoSize++;
						}			
					}						
					photoSize = photoSize + currentPhotoBook.chosenpics.size() - selectedPhotoSize;																		
					if (photoSize > activity.maxselectPhotoSize) {
						activity.isOutMaxSelectNum = true;
						activity.allSelectButton.setVisibility(View.GONE);
					}											
				}							
			}else {
				activity.popUpPrompt(R.string.fbk_no_photos_find);
			}				
			break;
						
		case HandlerConstant.GET_FBK_FRIENDS_INFO_START:
			if (FriendsUsers != null) {				
				GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_FRIENDS_INFO_END);
			}else {
				setViewsVisible(false);
				setAllViewVisible(false);
				activity.fbkAuth.getInfoList(null,GetFacebookGraphicsHandler.this,AppConstants.getFriendUsers,false,false);		
			}		
			break;
			
		case HandlerConstant.GET_FBK_FRIENDS_INFO_END:
			if (FriendsUsers == null) {
				FriendsUsers = (ArrayList<FbkObject>) msgObject;
			}
			setViewsVisible(true);				
			setAllViewVisible(false);
			displayStr = facebookPrompt +fbkYourFriends ;
			activity.adapterTpyeFlag = AdpaterConstant.FBK_FRIENDS_ADAPTER_TPYE;
			fbkFriendsAdapter = new FacebookFriendsAdapter(mContenxt,FriendsUsers,activity.mMemoryCache);
			gridViewParamUtil.initGridViewMargin(FriendsUsers.size(),activity.adapterTpyeFlag,fbkFriendsAdapter);	
			activity.photoGridView.setAdapter(fbkFriendsAdapter);									
			break;
			
		case HandlerConstant.GET_FBK_FRIEND_ALBUMS_START:
			setViewsVisible(false);
			setAllViewVisible(false);
			FbkUser fbkFriendUser = (FbkUser) msgObject;			
			fbkFriendUser.handler = GetFacebookGraphicsHandler.this;				
			activity.fbkAuth.getAlbumInfoList(AppConstants.fbkUser,fbkFriendUser);
			break;		
			
		case HandlerConstant.GET_FBK_FRIEND_ALBUMS_END:
			albums = (ArrayList<FbkObject>) msgObject;
			setViewsVisible(true);	
			if (albums != null && albums.size() > 0) {
				setAllViewVisible(false);			
				displayStr = facebookPrompt + fbkYourFriends+"/"+albums.get(0).bucketName;							
				activity.adapterTpyeFlag = AdpaterConstant.FBK_FRIEND_ALBUMS_ADAPTER_TPYE;		
				fbkAlbumsAdapter = new FacebookAlbumsAdapter(mContenxt,albums,activity.mMemoryCache);	
				gridViewParamUtil.initGridViewMargin(albums.size(),activity.adapterTpyeFlag,fbkAlbumsAdapter);	
				activity.photoGridView.setAdapter(fbkAlbumsAdapter);		
			}else {
				activity.popUpPrompt(R.string.fbk_no_photos_find);
			}																					
			break;	
			
		case HandlerConstant.GET_FBK_FRIEND_IMAGE_START:
			setViewsVisible(false);	
			setAllViewVisible(false);
			FbkAlbum fbkFriensAlbum = (FbkAlbum) msgObject;				
			if (fbkFriensAlbum.userPhotos != null) {
				GetFacebookGraphicsHandler.this.obtainMessage(HandlerConstant.GET_FBK_FRIEND_IMAGE_END,fbkFriensAlbum.userPhotos).sendToTarget();				
			}else {
				fbkFriensAlbum.handler = GetFacebookGraphicsHandler.this;						
				activity.fbkAuth.getPhotoInfoList(fbkFriensAlbum,AppConstants.isfbkFriend);		
			}	
			break;	
			
		case HandlerConstant.GET_FBK_FRIEND_IMAGE_END:	
			fbkPhotos = (ArrayList<FbkObject>) msgObject;
			setViewsVisible(true);
			if (fbkPhotos != null && fbkPhotos.size() > 0) {
				setAllViewVisible(true);				
				FbkPhoto fbkPhoto = (FbkPhoto) fbkPhotos.get(0);
				displayStr = facebookPrompt + fbkYourFriends+"/"+fbkPhoto.UserName+"/"+fbkPhoto.bucketName;					
				activity.adapterTpyeFlag = AdpaterConstant.FBK_FRIEND_IMAGES_ADAPTER_TPYE;
				fbkImageAdapter = new FBKImageAdapter(mContenxt,fbkPhotos,activity.flowType,activity.mMemoryCache);
				gridViewParamUtil.initGridViewMargin(fbkPhotos.size(),activity.adapterTpyeFlag,fbkImageAdapter);	
				activity.photoGridView.setAdapter(fbkImageAdapter);	
				if (activity.allSelectButton != null) {	
					Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
					int photoSize = fbkPhotos.size();
					int selectedPhotoSize = 0;
					for (int i = 0; i < photoSize; i++) {
						fbkImageAdapter.isDeal[i] = true;
						String key = fbkPhotos.get(i).ID;
						int pos = activity.getPositionInList(currentPhotoBook.chosenpics, key);
						if (pos != -1) {
							fbkImageAdapter.isChice[i] = true;
							selectedPhotoSize++;
						}			
					}						
					photoSize = photoSize + currentPhotoBook.chosenpics.size() - selectedPhotoSize;																		
					if (photoSize > activity.maxselectPhotoSize) {
						activity.isOutMaxSelectNum = true;
						activity.allSelectButton.setVisibility(View.GONE);
					}											
				}
			}else {
				activity.popUpPrompt(R.string.fbk_no_photos_find);
			}						
			break;	
													
		case HandlerConstant.GET_FBK_GROUPS_INFO_START:
			if (fbkgroups != null) {
				GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_GROUPS_INFO_END);
			}else {
				setViewsVisible(false);	
				setAllViewVisible(false);
				activity.fbkAuth.getInfoList(null,GetFacebookGraphicsHandler.this,AppConstants.getGroups,false,false);	
			}								
			break;
			
		case HandlerConstant.GET_FBK_GROUPS_INFO_END:
			displayStr = facebookPrompt +fbkYourGroups ;	
			if (fbkgroups == null) {
				fbkgroups = (ArrayList<FbkObject>) msgObject;
			}
			setViewsVisible(true);
			setAllViewVisible(false);
			activity.adapterTpyeFlag = AdpaterConstant.FBK_GROUPS_ADAPTER_TPYE;
			fbkGroupsAdapter = new FacebookGroupsAdapter(mContenxt,fbkgroups,activity.mMemoryCache);	
			gridViewParamUtil.initGridViewMargin(fbkgroups.size(),activity.adapterTpyeFlag,fbkGroupsAdapter);	
			activity.photoGridView.setAdapter(fbkGroupsAdapter);							
			break;	

		case HandlerConstant.GET_FBK_GROUP_ALBUMS_START:
			FbkGroup fbkGroup = (FbkGroup) msgObject;			
			setViewsVisible(false);	
			setAllViewVisible(false);
			fbkGroup.handler = GetFacebookGraphicsHandler.this;			
			activity.fbkAuth.getAlbumInfoList(AppConstants.fbkGroup,fbkGroup);										
			break;		
			
		case HandlerConstant.GET_FBK_GROUP_ALBUMS_END:
			ArrayList<FbkObject> groupAlbums = (ArrayList<FbkObject>) msgObject;
			setViewsVisible(true);	
			if (groupAlbums != null && groupAlbums.size() > 0) {
				setAllViewVisible(false);				
				displayStr = facebookPrompt + fbkYourGroups+"/"+groupAlbums.get(0).bucketName;					
				activity.adapterTpyeFlag = AdpaterConstant.FBK_GROUP_ALBUMS_ADAPTER_TPYE;			
				fbkAlbumsAdapter = new FacebookAlbumsAdapter(mContenxt,groupAlbums,activity.mMemoryCache);	
				gridViewParamUtil.initGridViewMargin(groupAlbums.size(),activity.adapterTpyeFlag,fbkAlbumsAdapter);	
				activity.photoGridView.setAdapter(fbkAlbumsAdapter);		
			}else {
				activity.popUpPrompt(R.string.fbk_no_photos_find);
			}																
			break;
						
		case HandlerConstant.GET_FBK_GROUP_IMAGE_START:
			FbkAlbum fbkGroupAlbum = (FbkAlbum) msgObject;						
			setViewsVisible(false);
			setAllViewVisible(false);
			fbkGroupAlbum.handler = GetFacebookGraphicsHandler.this;							
			activity.fbkAuth.getPhotoInfoList(fbkGroupAlbum,AppConstants.isfbkGroud);				
			break;
			
		case HandlerConstant.GET_FBK_GROUP_IMAGE_END:	
			fbkPhotos = (ArrayList<FbkObject>) msgObject;
			setViewsVisible(true);
			if (fbkPhotos != null && fbkPhotos.size() > 0) {
				setAllViewVisible(true);				
				FbkPhoto fbkPhoto = (FbkPhoto) fbkPhotos.get(0);
				if (fbkPhoto.UserName != null) {
					displayStr = facebookPrompt + fbkYourGroups+"/"+fbkPhoto.UserName+"/"+fbkPhoto.bucketName;	
				}else {
					displayStr = facebookPrompt + fbkYourGroups+"/"+fbkPhoto.bucketName;	
				}								
				activity.adapterTpyeFlag = AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE;
				fbkImageAdapter = new FBKImageAdapter(mContenxt,fbkPhotos,activity.flowType,activity.mMemoryCache);
				gridViewParamUtil.initGridViewMargin(fbkPhotos.size(),activity.adapterTpyeFlag,fbkImageAdapter);	
				activity.photoGridView.setAdapter(fbkImageAdapter);		
				if (activity.allSelectButton != null) {	
					Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
					int photoSize = fbkPhotos.size();
					int selectedPhotoSize = 0;
					for (int i = 0; i < photoSize; i++) {
						fbkImageAdapter.isDeal[i] = true;
						String key = fbkPhotos.get(i).ID;
						int pos = activity.getPositionInList(currentPhotoBook.chosenpics, key);
						if (pos != -1) {
							fbkImageAdapter.isChice[i] = true;
							selectedPhotoSize++;
						}			
					}						
					photoSize = photoSize + currentPhotoBook.chosenpics.size() - selectedPhotoSize;																		
					if (photoSize > activity.maxselectPhotoSize) {
						activity.isOutMaxSelectNum = true;
						activity.allSelectButton.setVisibility(View.GONE);
					}											
				}			
			}else {
				activity.popUpPrompt(R.string.fbk_no_photos_find);
			}								
			break;	
			
		case HandlerConstant.GET_FBK_GROUPS_IMAGE_START:
			FbkGroup  fbkGroup2 = (FbkGroup) msgObject;			
			setViewsVisible(false);	
			setAllViewVisible(false);
			fbkGroup2.handler = GetFacebookGraphicsHandler.this;						
			activity.fbkAuth.getPhotoInfoList(fbkGroup2);				
			break;						
									
		case HandlerConstant.GET_FBK_ORIGINAL_IMAGE:			
			Object[] array =  (Object[]) msgObject;	
			View selectedView = (View) array[0];
			int fbkPhotoPosition  = (Integer) array[1];					
			FbkPhoto fbkPhoto = (FbkPhoto) fbkPhotos.get(fbkPhotoPosition);															
			if (activity.progressBar != null) {
				activity.progressBar.setVisibility(View.VISIBLE);	
			}		
			onGetIamgeOnFacebookListener.onGetIamgeOnFacebook(selectedView,fbkPhoto, fbkPhotoPosition);					
			break;
		case HandlerConstant.GET_FBK_ALL_ORIGINAL_IMAGE:				
			onGetIamgeOnFacebookListener.onGetAllImageOnFacebook(fbkPhotos);
			break;
		case HandlerConstant.DELETE_FBK_ALL_ORIGINAL_IMAGE:				
			onGetIamgeOnFacebookListener.onDeleteAllImageOnFacebook(fbkPhotos);
			break;	
		}
		activity.sourceNameButton.setText(displayStr);
	}
	
	public void onItemClickOnFacebook(View view,int position){
		switch (activity.adapterTpyeFlag) {			
		case AdpaterConstant.FBK_PHOTOS_ADAPTER_TPYE:								
			if (position == 0) {
				if (fbkMainUser == null) {
					GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_USER_START);	
				}else {
					GetFacebookGraphicsHandler.this.obtainMessage(HandlerConstant.GET_FBK_MAIN_USER_PHOTOS_START,fbkMainUser).sendToTarget();
				}
			}else if (position == 1) {
				GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_MAIN_INFO_START);	
			}else if (position == 2) {
				GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_FRIENDS_INFO_START);
			}else if (position == 3){
				GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_GROUPS_INFO_START);
			}
			break;
			
		case AdpaterConstant.FBK_MAIN_ALBUMS_ADAPTER_TPYE:
			selectPhotosPostion = position;
			FbkObject fbkMainAlbum = fbkMainAlbums.get(position);								
			GetFacebookGraphicsHandler.this.obtainMessage(HandlerConstant.GET_FBK_MAIN_IMAGE_START,fbkMainAlbum).sendToTarget();
			break;
		
		case AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE:		
		case AdpaterConstant.FBK_FRIEND_IMAGES_ADAPTER_TPYE:	
		case AdpaterConstant.FBK_MAIN_IMAGES_ADAPTER_TPYE:	
		case AdpaterConstant.FBK_MAIN_PHOTOS_ADAPTER_TPYE:
			Object[] array = new Object[2];
			array[0] = view;
			array[1] = position;			
			GetFacebookGraphicsHandler.this.obtainMessage(HandlerConstant.GET_FBK_ORIGINAL_IMAGE,array).sendToTarget();			
			break;				

		case AdpaterConstant.FBK_FRIENDS_ADAPTER_TPYE:
			selectFriendsPostion = position;
			FbkObject friendUser = FriendsUsers.get(position);								
			GetFacebookGraphicsHandler.this.obtainMessage(HandlerConstant.GET_FBK_FRIEND_ALBUMS_START,friendUser).sendToTarget();
			break;
			
		case AdpaterConstant.FBK_FRIEND_ALBUMS_ADAPTER_TPYE:	
			selectPhotosPostion = position;
			FbkAlbum fbkFriendAlbum = (FbkAlbum) albums.get(position);
			if ((fbkFriendAlbum.userPhotos != null && fbkFriendAlbum.userPhotos.size() < 1) ||(fbkFriendAlbum.userPhotos == null && fbkFriendAlbum.count < 1)) {				
				activity.popUpPrompt(R.string.fbk_no_photos_find);
			}else {
				GetFacebookGraphicsHandler.this.obtainMessage(HandlerConstant.GET_FBK_FRIEND_IMAGE_START,fbkFriendAlbum).sendToTarget();
			}
			break;		
	
		case AdpaterConstant.FBK_GROUPS_ADAPTER_TPYE:					
			final FbkObject fbkGroup = fbkgroups.get(position);
			if (isNotAlertYourGroupsInFBK) {																
				GetFacebookGraphicsHandler.this.obtainMessage(HandlerConstant.GET_FBK_GROUPS_IMAGE_START,fbkGroup).sendToTarget();					
			}else {
				android.content.DialogInterface.OnClickListener onClickListener  = new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						isNotAlertYourGroupsInFBK = true;														
						GetFacebookGraphicsHandler.this.obtainMessage(HandlerConstant.GET_FBK_GROUPS_IMAGE_START,fbkGroup).sendToTarget();			
					}		
				};		
				new InfoDialog.Builder(mContenxt).setMessage(R.string.fbk_your_groups_str)						
				.setNegativeButton(R.string.d_ok, onClickListener).create()
				.show();		
			}
			break;
			
		case AdpaterConstant.FBK_GROUP_ALBUMS_ADAPTER_TPYE:	
			FbkObject fbkGroupAlbum = albums.get(position);		
			GetFacebookGraphicsHandler.this.obtainMessage(HandlerConstant.GET_FBK_GROUP_IMAGE_START,fbkGroupAlbum).sendToTarget();
			break;							
		}			
	}

	public void onClickBackOnFacebook(){		
		switch (activity.adapterTpyeFlag) {		
		case AdpaterConstant.FBK_MAIN_IMAGES_ADAPTER_TPYE:							
			displayStr = facebookPrompt +fbkYourPhotos;
			if (fbkImageAdapter != null) {
				fbkImageAdapter.cancelRequest();
			}
			activity.adapterTpyeFlag = AdpaterConstant.FBK_MAIN_ALBUMS_ADAPTER_TPYE;
			gridViewParamUtil.initGridViewMargin(fbkMainAlbums.size(),activity.adapterTpyeFlag,fbkAlbumsAdapter);
			activity.photoGridView.setAdapter(fbkAlbumsAdapter);							
			activity.photoGridView.setSelection(selectPhotosPostion);
			break;
		
		case AdpaterConstant.FBK_GROUPS_ADAPTER_TPYE:		
		case AdpaterConstant.FBK_FRIENDS_ADAPTER_TPYE:		
		case AdpaterConstant.FBK_MAIN_ALBUMS_ADAPTER_TPYE:	
		case AdpaterConstant.FBK_MAIN_PHOTOS_ADAPTER_TPYE:
			displayStr =  facebookPrompt;	
			if (fbkGroupsAdapter != null) {
				fbkGroupsAdapter.cancelRequest();
			}
			if (fbkFriendsAdapter != null) {
				fbkFriendsAdapter.cancelRequest();
			}
			if (fbkAlbumsAdapter != null) {
				fbkAlbumsAdapter.cancelRequest();
			}
			if (fbkImageAdapter != null) {
				fbkImageAdapter.cancelRequest();
			}
			activity.adapterTpyeFlag = AdpaterConstant.FBK_PHOTOS_ADAPTER_TPYE;		
			gridViewParamUtil.initGridViewMargin(4,activity.adapterTpyeFlag,fbkPhotosAdapter);
			activity.photoGridView.setAdapter(fbkPhotosAdapter);				
			break;	
	
		case AdpaterConstant.FBK_PHOTOS_ADAPTER_TPYE:							
			setBackViewVisible(false);			
			displayStr = "";	
			activity.adapterTpyeFlag = AdpaterConstant.SOURCES_ADAPTER_TPYE;	
			gridViewParamUtil.initGridViewMargin(2,activity.adapterTpyeFlag,activity.sourcesAdapter);	
			activity.photoGridView.setAdapter(activity.sourcesAdapter);			
			break;		
						
		case AdpaterConstant.FBK_FRIEND_IMAGES_ADAPTER_TPYE:			
			int lastIndex = displayStr.lastIndexOf("/");
			if (fbkImageAdapter != null) {
				fbkImageAdapter.cancelRequest();
			}
			displayStr = displayStr.substring(0, lastIndex);
			activity.adapterTpyeFlag = AdpaterConstant.FBK_FRIEND_ALBUMS_ADAPTER_TPYE;	
			gridViewParamUtil.initGridViewMargin(albums.size(),activity.adapterTpyeFlag,fbkAlbumsAdapter);
			activity.photoGridView.setAdapter(fbkAlbumsAdapter);						
			activity.photoGridView.setSelection(selectPhotosPostion);
			break;	
			
		case AdpaterConstant.FBK_FRIEND_ALBUMS_ADAPTER_TPYE:
			displayStr = facebookPrompt +fbkYourFriends ;
			if (fbkAlbumsAdapter != null) {
				fbkAlbumsAdapter.cancelRequest();
			}
			activity.adapterTpyeFlag = AdpaterConstant.FBK_FRIENDS_ADAPTER_TPYE;
			gridViewParamUtil.initGridViewMargin(FriendsUsers.size(),activity.adapterTpyeFlag,fbkFriendsAdapter);	
			activity.photoGridView.setAdapter(fbkFriendsAdapter);							
			activity.photoGridView.setSelection(selectFriendsPostion);							
			break;							
					
		case AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE:	
			displayStr = facebookPrompt +fbkYourGroups ;			
			activity.adapterTpyeFlag = AdpaterConstant.FBK_GROUPS_ADAPTER_TPYE;		
			gridViewParamUtil.initGridViewMargin(fbkgroups.size(),activity.adapterTpyeFlag,fbkGroupsAdapter);	
			activity.photoGridView.setAdapter(fbkGroupsAdapter);									
			break;							
		}
		setAllViewVisible(false);
		setGridViewVisible(true);
		activity.sourceNameButton.setText(displayStr);
	}
	
	public void onClickGetAll(){		
		switch (activity.adapterTpyeFlag) {	
		case AdpaterConstant.FBK_MAIN_PHOTOS_ADAPTER_TPYE:	
		case AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE:		
		case AdpaterConstant.FBK_FRIEND_IMAGES_ADAPTER_TPYE:	
		case AdpaterConstant.FBK_MAIN_IMAGES_ADAPTER_TPYE:										
			GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_ALL_ORIGINAL_IMAGE);	
			break;					
		}
	}
	
	public void onClickDeleteAll(){		
		switch (activity.adapterTpyeFlag) {	
		case AdpaterConstant.FBK_MAIN_PHOTOS_ADAPTER_TPYE:	
		case AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE:		
		case AdpaterConstant.FBK_FRIEND_IMAGES_ADAPTER_TPYE:	
		case AdpaterConstant.FBK_MAIN_IMAGES_ADAPTER_TPYE:				    
			GetFacebookGraphicsHandler.this.sendEmptyMessage(HandlerConstant.DELETE_FBK_ALL_ORIGINAL_IMAGE);	
			break;					
		}
	}
		
	public interface OnGetIamgeOnFacebookListener {
		public void onGetIamgeOnFacebook(View view,FbkPhoto fbkPhoto, int fbkPhotoPosition);		
		public void onGetAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos);
		public void onDeleteAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos);		
	}
	
	private void setViewsVisible(boolean isVisible){
		setBackViewVisible(isVisible);
		setGridViewVisible(isVisible);
	}
	
	private void setBackViewVisible(boolean isVisible){
		if (isVisible) {
			activity.backButton.setVisibility(View.VISIBLE);
			activity.backButtonName.setVisibility(View.VISIBLE);		
		}else {
			activity.backButton.setVisibility(View.GONE);
			activity.backButtonName.setVisibility(View.GONE);			
		}
	}
	
	private void setGridViewVisible(boolean isVisible){
		if (isVisible) {			
			activity.photoGridView.setVisibility(View.VISIBLE);
			activity.panelContentPBar.setVisibility(View.GONE);
		}else {			
			activity.photoGridView.setVisibility(View.GONE);
			activity.panelContentPBar.setVisibility(View.VISIBLE);
		}		
	}
	
	private void setAllViewVisible(boolean isVisible){
		if (activity.allSelectButton != null) {
			activity.allSelectButton.setEnabled(true);
			activity.allDeleteButton.setEnabled(true);
			if (isVisible) {				
				activity.allSelectButton.setVisibility(View.VISIBLE);				
				activity.allDeleteButton.setVisibility(View.VISIBLE);			
			}else {
				activity.allSelectButton.setVisibility(View.GONE);
				activity.allDeleteButton.setVisibility(View.GONE);			
			}
		}
	}		
		
}
