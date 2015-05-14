package com.kodak.rss.tablet.facebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;

@SuppressWarnings("deprecation")
public class FBKWrapper {
	private static String TAG = "FBKWrapper :";
	public Facebook facebook = new Facebook(AppConstants.APPID);
	public Handler handler;
	public Handler loginHandler;
	private static FBKWrapper instance = null;
	private Bundle parameters;
	public static FBKWrapper getWrapper() {		
		if (instance == null) {
			instance = new FBKWrapper();
		}
		return instance;
	}
	
	public Boolean isFBSessionValid(Context context){				 		
        String access_token = SharedPreferrenceUtil.getFacebookToken(context);
        long expires = SharedPreferrenceUtil.getFacebookAccessExpires(context);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }		
		return facebook.isSessionValid();		
	}
	
	public void authorize(Activity activity, DialogListener listener){
		facebook.authorize(activity, AppConstants.PERMISSIONS, listener);
	}

	public void refreshFBSession(Context context) {
		SharedPreferrenceUtil.saveFacebookToken(context, facebook.getAccessToken());
		SharedPreferrenceUtil.saveFacebookAccessExpires(context, facebook.getAccessExpires());
	}
	
	public void getMainUser(final Handler handler,final ArrayList<FbkObject> users,final boolean isFromToken,final boolean isGetUserPhotos) {	
		if (handler == null ) return;		
		AsyncFacebookRunner requestor = new AsyncFacebookRunner(facebook);		
		Bundle arg = new Bundle();		
		arg.putCharSequence("fields", "id,name,first_name,last_name");		
		requestor.request("me",arg, new RequestListener() {
			@Override
			public void onIOException(IOException e, Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";							
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";				
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";				
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";				
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}
			
			@Override
			public void onComplete(String response, Object state) {
				try {					
					FbkUser user = new FbkUser();
					user.ID = Util.parseJson(response).getString("id");
					user.name = Util.parseJson(response).getString("name");					
					user.frist_name = Util.parseJson(response).getString("first_name");
					user.last_name = Util.parseJson(response).getString("last_name");
					users.add(user);	
					
					if (isFromToken) {					
						handler.obtainMessage(HandlerConstant.GET_FBK_TOKEN_INFO_END,users).sendToTarget();
					}else {
						if (isGetUserPhotos) {							
							handler.obtainMessage(HandlerConstant.GET_FBK_USER_END,users).sendToTarget();
						}else {							
							handler.obtainMessage(HandlerConstant.GET_FBK_MAIN_INFO_END,users).sendToTarget();
						}						
					}

				} catch (JSONException e) {					
					String errorStr = e!= null ? e.getMessage() : "error";
					handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
				} catch (FacebookError e) {						
					String errorStr = e!= null ? e.getMessage() : "error";
					handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
				}
			}
		});							
	}
	
	public void getFriendUsers(boolean isFrist,final ArrayList<FbkObject> users) {
		AsyncFacebookRunner requestor = new AsyncFacebookRunner(facebook);	
		if (isFrist || parameters == null) {			
			parameters = new Bundle();			
		}
		parameters.putString("limit", "400");	
		requestor.request("me/friends",parameters, new RequestListener() {
			@Override
			public void onMalformedURLException(MalformedURLException e,Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}

			@Override
			public void onIOException(IOException e, Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}

			@Override
			public void onComplete(String response, Object state) {
				try {
					String nextUrl = null;
					JSONObject jsfriends = Util.parseJson(response);
					JSONArray arrayFriends = jsfriends.getJSONArray("data");
					try {	
						JSONObject arrayPage = jsfriends.getJSONObject("paging");
						nextUrl = arrayPage.getString("next");	
					}catch (Exception e) {
						nextUrl = null;
					} 		
					
					for (int i = 0; i < arrayFriends.length(); i++) {
						JSONObject friend = arrayFriends.getJSONObject(i);
						FbkUser user = new FbkUser();
						user.ID = friend.getString("id");
						user.name = friend.getString("name");
						users.add(user);
					}
					
					if (nextUrl != null && !"".equalsIgnoreCase(nextUrl)) {
						parameters = new Bundle();	
						String[] params = nextUrl.split("&");
						for (int i = 0; i < params.length; i++) {
							if (!params[i].contains("limit")&&!params[i].contains("format") && !params[i].contains("access_token")) {	
								if (params[i].contains("?")) {									
									int d = params[i].lastIndexOf("?")+1;										
									params[i] = params[i].substring(d);									
								}
								parameters.putString(params[i].split("=")[0], params[i].split("=")[1]);								
							}							
						}
						getFriendUsers(false,users);
					}else {
						Comparater comparater = new Comparater();
						Collections.sort(users,comparater);
						handler.obtainMessage(HandlerConstant.GET_FBK_FRIENDS_INFO_END,users).sendToTarget();
					}
					
				} catch (JSONException e) {
					String errorStr = e!= null ? e.getMessage() : "error";
					handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
				} catch (FacebookError e) {
					String errorStr = e!= null ? e.getMessage() : "error";
					handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
				}
			}
		});		
	}
		
	public void getGroups(boolean isFrist,final ArrayList<FbkObject> groups) {
		AsyncFacebookRunner requestor = new AsyncFacebookRunner(facebook);
		if (isFrist || parameters == null) {
			parameters = new Bundle();			
		}	
		parameters.putString("limit", "400");	
		requestor.request("me/groups", parameters, new RequestListener() {
			@Override
			public void onMalformedURLException(MalformedURLException e,Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}

			@Override
			public void onIOException(IOException e, Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}

			@Override
			public void onComplete(String response, Object state) {
				try {
					String nextUrl = null;
					JSONObject jsGroups = Util.parseJson(response);
					JSONArray arrayGroups = jsGroups.getJSONArray("data");

					try {	
						JSONObject arrayPage = jsGroups.getJSONObject("paging");
						nextUrl = arrayPage.getString("next");	
					}catch (Exception e) {
						nextUrl = null;
					} 		
					
					for (int i = 0; i < arrayGroups.length(); i++) {
						JSONObject jsGroup = arrayGroups.getJSONObject(i);
						FbkGroup group = new FbkGroup();
						group.ID = jsGroup.getString("id");
						group.name = jsGroup.getString("name");
						groups.add(group);
					}
					
					if (nextUrl != null && !"".equalsIgnoreCase(nextUrl)) {
						parameters = new Bundle();	
						String[] params = nextUrl.split("&");
						for (int i = 0; i < params.length; i++) {
							if (!params[i].contains("limit")&&!params[i].contains("format") && !params[i].contains("access_token")) {								
								parameters.putString(params[i].split("=")[0], params[i].split("=")[1]);								
							}							
						}
						getGroups(false,groups);
					}else {
						handler.obtainMessage(HandlerConstant.GET_FBK_GROUPS_INFO_END,groups).sendToTarget();	
					}
					
				} catch (JSONException e) {
					String errorStr = e!= null ? e.getMessage() : "error";
					handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
				} catch (FacebookError e) {
					String errorStr = e!= null ? e.getMessage() : "error";
					handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
				}
			}
		});		
	}	
	
	public void onCancel(boolean isLoginOnly){
		if (isLoginOnly) {
			if (loginHandler != null){
				loginHandler.sendEmptyMessage(HandlerConstant.GET_FBKINFO_ERROR);
			}		
		}else {
			if (handler != null) {			
				handler.sendEmptyMessage(HandlerConstant.GET_FBKINFO_ERROR);
			}
		}		
	}
	
	public void onFacebookError(FacebookError e,boolean isLoginOnly){
		String errorStr = e!= null ? e.getMessage() : "error";
		if (isLoginOnly) {
			if (loginHandler != null){
				loginHandler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}		
		}else {
			if (handler != null) {			
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();
			}
		}						
	}

	public void onError(DialogError e,boolean isLoginOnly){
		String errorStr = e!= null ? e.getMessage() : "error";
		if (isLoginOnly) {
			if (loginHandler != null){
				loginHandler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}		
		}else {
			if (handler != null) {			
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();
			}
		}				
	}

}
