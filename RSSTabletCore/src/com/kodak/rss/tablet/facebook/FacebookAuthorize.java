package com.kodak.rss.tablet.facebook;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.facebook.android.DialogError;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.LoaderUtil;

public class FacebookAuthorize {
	private FBKWrapper fbkWrapper;
	private Context  mContext;
	private Activity  activity;
	
	public FacebookAuthorize(Context context,FBKWrapper wrapper) {
		this.fbkWrapper = wrapper;
		this.mContext = context;
		activity = (Activity) this.mContext;
	}
	
	public boolean isValid(){		
		boolean isValid = false;
		String access_token = SharedPreferrenceUtil.getFacebookToken(mContext);
        long expires = SharedPreferrenceUtil.getFacebookAccessExpires(mContext);
        if (access_token != null &&(expires == 0 ||(System.currentTimeMillis() < expires))) {
        	isValid = true;    
        } else {
        	LoaderUtil.clearCaches(mContext.getApplicationContext(),"."+FilePathConstant.externalType);
        	SharedPreferrenceUtil.saveFacebookToken(mContext, null);
        	SharedPreferrenceUtil.saveFacebookAccessExpires(mContext, -1);
        	SharedPreferrenceUtil.saveFacebookUserId(mContext, "");
        	SharedPreferrenceUtil.saveFacebookFristName(mContext, "");
        	SharedPreferrenceUtil.saveFacebookLastName(mContext, "");
		}		
		return isValid;
	}
	
	public void getInfoList(final ProgressDialog mSpinner,final Handler handler,final String getInfoType, final boolean isFromToken,final boolean isGetUserPhotos){
		final ArrayList<FbkObject> arrayList = new ArrayList<FbkObject>();
		if(fbkWrapper.isFBSessionValid(mContext)){			
			if (AppConstants.getMainUser.equalsIgnoreCase(getInfoType)) {
				if (mSpinner != null) mSpinner.show();
				fbkWrapper.getMainUser(handler,arrayList,isFromToken,isGetUserPhotos);
			}else if (AppConstants.getFriendUsers.equalsIgnoreCase(getInfoType)) {
				fbkWrapper.getFriendUsers(true,arrayList);		
			}else if (AppConstants.getGroups.equalsIgnoreCase(getInfoType)) {
				fbkWrapper.getGroups(true,arrayList);		
			}
		}else {
			fbkWrapper.authorize(activity, new DialogListener(){			
				@Override
				public void onComplete(Bundle values) {								
					fbkWrapper.refreshFBSession(mContext);								
					if (AppConstants.getMainUser.equalsIgnoreCase(getInfoType)) {
						if (mSpinner != null) mSpinner.show();						
						fbkWrapper.getMainUser(handler,arrayList,isFromToken,isGetUserPhotos);
					}else if (AppConstants.getFriendUsers.equalsIgnoreCase(getInfoType)) {
						fbkWrapper.getFriendUsers(true,arrayList);		
					}else if (AppConstants.getGroups.equalsIgnoreCase(getInfoType)) {
						fbkWrapper.getGroups(true,arrayList);		
					}		
				}			
				@Override
				public void onFacebookError(FacebookError e) {
					boolean isOnlyLogin = false;
					if (mSpinner != null) isOnlyLogin = true;
					fbkWrapper.onFacebookError(e,isOnlyLogin);
				}			
				@Override
				public void onError(DialogError e) {
					boolean isOnlyLogin = false;
					if (mSpinner != null) isOnlyLogin = true;
					fbkWrapper.onError(e,isOnlyLogin);
				}			
				@Override
				public void onCancel() {
					boolean isOnlyLogin = false;
					if (mSpinner != null) isOnlyLogin = true;
					fbkWrapper.onCancel(isOnlyLogin);
				}							
			});	
		}		
	}
	
	public void getAlbumInfoList(final String fbkObjectType,final FbkObject fbkObject){
		final ArrayList<FbkObject> albumList = new ArrayList<FbkObject>();
		if(fbkWrapper.isFBSessionValid(mContext)){
			if (AppConstants.fbkMainUser.equalsIgnoreCase(fbkObjectType)) {				
				((FbkUser) fbkObject).retriveAlbumList(true,true,albumList);		
			}else if (AppConstants.fbkUser.equalsIgnoreCase(fbkObjectType)) {
				final ArrayList<FbkObject> userPhotoList = new ArrayList<FbkObject>();
				String displayHeader = mContext.getString(R.string.photos_of);
				((FbkUser) fbkObject).retrivePhotos(true,albumList,userPhotoList,false,displayHeader);		
			}else if (AppConstants.fbkGroup.equalsIgnoreCase(fbkObjectType)) {
				((FbkGroup) fbkObject).retriveAlbumList(true,albumList);			
			}						
		}else {
			fbkWrapper.authorize(activity, new DialogListener(){			
				@Override
				public void onComplete(Bundle values) {								
					fbkWrapper.refreshFBSession(mContext);								
					if (AppConstants.fbkMainUser.equalsIgnoreCase(fbkObjectType)) {
						((FbkUser) fbkObject).retriveAlbumList(true,true,albumList);		
					}else if (AppConstants.fbkUser.equalsIgnoreCase(fbkObjectType)) {
						final ArrayList<FbkObject> userPhotoList = new ArrayList<FbkObject>();
						String displayHeader = mContext.getString(R.string.photos_of);
						((FbkUser) fbkObject).retrivePhotos(true,albumList,userPhotoList,false,displayHeader);		
					}else if (AppConstants.fbkGroup.equalsIgnoreCase(fbkObjectType)) {
						((FbkGroup) fbkObject).retriveAlbumList(true,albumList);			
					}	
				}			
				@Override
				public void onFacebookError(FacebookError e) {
					fbkWrapper.onFacebookError(e,false);
				}			
				@Override
				public void onError(DialogError e) {
					fbkWrapper.onError(e,false);
				}			
				@Override
				public void onCancel() {
					fbkWrapper.onCancel(false);
				}							
				});	
		}		
	}
	
	public void getPhotoInfoList(final FbkUser fbkUser){
		final ArrayList<FbkObject> albumsList = new ArrayList<FbkObject>();
		final ArrayList<FbkObject> userPhotoList = new ArrayList<FbkObject>();		
		if(fbkWrapper.isFBSessionValid(mContext)){	
			String displayHeader = mContext.getString(R.string.photos_of);
			fbkUser.retrivePhotos(true,albumsList,userPhotoList,true,displayHeader);										
		}else {
			fbkWrapper.authorize(activity, new DialogListener(){			
				@Override
				public void onComplete(Bundle values) {								
					fbkWrapper.refreshFBSession(mContext);
					String displayHeader = mContext.getString(R.string.photos_of);
					fbkUser.retrivePhotos(true,albumsList,userPhotoList,true,displayHeader);		
				}			
				@Override
				public void onFacebookError(FacebookError e) {
					fbkWrapper.onFacebookError(e,false);
				}			
				@Override
				public void onError(DialogError e) {
					fbkWrapper.onError(e,false);
				}			
				@Override
				public void onCancel() {
					fbkWrapper.onCancel(false);
				}							
				});	
		}		
	}
	
	public void getPhotoInfoList(final FbkAlbum fbkAlbum,final String fbkObjectType){
		final ArrayList<FbkObject> arrayList = new ArrayList<FbkObject>();
		if(fbkWrapper.isFBSessionValid(mContext)){			
			fbkAlbum.retrivePhotos(true,arrayList,fbkObjectType);										
		}else {
			fbkWrapper.authorize(activity, new DialogListener(){			
				@Override
				public void onComplete(Bundle values) {								
					fbkWrapper.refreshFBSession(mContext);								
					fbkAlbum.retrivePhotos(true,arrayList,fbkObjectType);		
				}			
				@Override
				public void onFacebookError(FacebookError e) {
					fbkWrapper.onFacebookError(e,false);
				}			
				@Override
				public void onError(DialogError e) {
					fbkWrapper.onError(e,false);
				}			
				@Override
				public void onCancel() {
					fbkWrapper.onCancel(false);
				}							
				});	
		}		
	}
	
	public void getPhotoInfoList(final FbkGroup fbkGroup){
		final ArrayList<FbkObject> arrayList = new ArrayList<FbkObject>();
		if(fbkWrapper.isFBSessionValid(mContext)){			
			fbkGroup.retrivePhotoList(true,arrayList);										
		}else {
			fbkWrapper.authorize(activity, new DialogListener(){			
				@Override
				public void onComplete(Bundle values) {								
					fbkWrapper.refreshFBSession(mContext);								
					fbkGroup.retrivePhotoList(true,arrayList);		
				}			
				@Override
				public void onFacebookError(FacebookError e) {
					fbkWrapper.onFacebookError(e,false);
				}			
				@Override
				public void onError(DialogError e) {
					fbkWrapper.onError(e,false);
				}			
				@Override
				public void onCancel() {
					fbkWrapper.onCancel(false);
				}							
			});	
		}		
	}
	
}
