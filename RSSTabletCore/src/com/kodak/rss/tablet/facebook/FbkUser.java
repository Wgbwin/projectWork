package com.kodak.rss.tablet.facebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.kodak.rss.tablet.AppConstants;

public class FbkUser extends FbkObject{
	public Handler handler;
	private Bundle parameters;	
	public String last_name;
	public String frist_name;
	
	public String getProfilePictureLink(){
		// small, normal, large, square
		return AppConstants.SCOPE + this.ID + "/picture"+"?type="+ "normal" ;
	}
	
	public URI getUserUri(){
		URI pictureURI;			
		try {
			pictureURI = new URI(getProfilePictureLink());
		} catch (URISyntaxException e) {
			pictureURI = null;
		}
		return pictureURI;
	}
	
	public void retriveAlbumList(final boolean isMain,boolean isFrist,final ArrayList<FbkObject> albums){		
		FBKWrapper wrapper = FBKWrapper.getWrapper();		
		AsyncFacebookRunner requestor = new AsyncFacebookRunner(wrapper.facebook);
		if (isFrist || parameters == null) {
			parameters = new Bundle();			
		}
		parameters.putString("limit", "400");	
		requestor.request(this.ID + "/albums", parameters, new RequestListener() {
			@Override
			public void onMalformedURLException(MalformedURLException e, Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}
			
			@Override
			public void onIOException(IOException e, Object state) {				
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}
			
			@Override
			public void onFileNotFoundException(FileNotFoundException e, Object state) {			
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
					JSONObject jsAlbums = Util.parseJson(response);
					JSONArray arrayAlbums = jsAlbums.getJSONArray("data");
										
					try {	
						JSONObject arrayPage = jsAlbums.getJSONObject("paging");
						nextUrl = arrayPage.getString("next");	
					}catch (Exception e) {
						nextUrl = null;
					} 			
					for (int i = 0; i < arrayAlbums.length(); i++) {
						JSONObject obj = arrayAlbums.getJSONObject(i);						
						FbkAlbum album = new FbkAlbum();
						album.ID = obj.getString("id");
						//album.Type = obj.getString("type");
						album.name = obj.getString("name");
						album.bucketName = name;
						try {	
							album.count = obj.getInt("count");	
						}catch (Exception e) {
							continue;
						} 													
						albums.add(album);											
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
						retriveAlbumList(isMain,false,albums);
					}else {
						if (isMain) {
							handler.obtainMessage(HandlerConstant.GET_FBK_MAIN_ALBUMS_END,albums).sendToTarget();
						}else {
							handler.obtainMessage(HandlerConstant.GET_FBK_FRIEND_ALBUMS_END,albums).sendToTarget();
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
	
	/**
	 * isMainUser false the step is only ready data
	 * */	
	public void retrivePhotos(boolean isFrist,final ArrayList<FbkObject> albumList,final ArrayList<FbkObject> userPhotoList,final boolean isMainUser,final String displayHeader){
		FBKWrapper wrapper = FBKWrapper.getWrapper();
		AsyncFacebookRunner requestor = new AsyncFacebookRunner(wrapper.facebook);	
		if (isFrist || parameters == null) {
			parameters = new Bundle();			
		}
		parameters.putString("limit", "400");	
		final String id = this.ID;
		final String name = this.name;
		parameters.putCharSequence("fields", "id,images");
		requestor.request(this.ID + "/photos", parameters,new RequestListener() {
			
			@Override
			public void onMalformedURLException(MalformedURLException e, Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}
			
			@Override
			public void onIOException(IOException e, Object state) {
				String errorStr = e!= null ? e.getMessage() : "error";
				handler.obtainMessage(HandlerConstant.GET_FBKINFO_ERROR,errorStr).sendToTarget();	
			}
			
			@Override
			public void onFileNotFoundException(FileNotFoundException e, Object state) {
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
					JSONObject jsPhotos = Util.parseJson(response);
					JSONArray arrayPhotos = jsPhotos.getJSONArray("data");
					try {	
						JSONObject arrayPage = jsPhotos.getJSONObject("paging");
						nextUrl = arrayPage.getString("next");	
					}catch (Exception e) {
						nextUrl = null;
					} 			
					
					for (int i = 0; i < arrayPhotos.length(); i++) {
						JSONObject aPhotos = arrayPhotos.getJSONObject(i);
						FbkPhoto photo = new FbkPhoto();
						photo.ID = (aPhotos.getString("id"));												
						photo.bucketName = displayHeader+" "+name;
						photo.UserName = name;
						JSONArray arrayPhotoSouces = aPhotos.getJSONArray("images");
						photo.photoSources = new PhotoSource[2];
						int lenght = 0;
						if (arrayPhotoSouces != null ) {
							lenght = arrayPhotoSouces.length();
						}
						if (lenght >= 2) {
							JSONObject aPhotoSource = arrayPhotoSouces.getJSONObject(0);
							PhotoSource source = new PhotoSource();
							source.height = aPhotoSource.getInt("height");	
							source.width = aPhotoSource.getInt("width");	
							source.source = aPhotoSource.getString("source");
							photo.photoSources[0] = source;
							
							JSONObject aPhotoSource1 = arrayPhotoSouces.getJSONObject(lenght-1);
							PhotoSource source1 = new PhotoSource();
							source1.height = aPhotoSource1.getInt("height");	
							source1.width = aPhotoSource1.getInt("width");	
							source1.source = aPhotoSource1.getString("source");	
							photo.photoSources[1] = source1;
						}
						userPhotoList.add(photo);
					}					
					if (nextUrl != null && !"".equalsIgnoreCase(nextUrl)) {
						parameters = new Bundle();	
						String[] params = nextUrl.split("&");
						for (int i = 0; i < params.length; i++) {
							if (!params[i].contains("limit")&&!params[i].contains("format") && !params[i].contains("access_token")&& !params[i].contains("images") && !params[i].contains("id")) {	
								if (params[i].contains("?")) {									
									int d = params[i].lastIndexOf("?")+1;										
									params[i] = params[i].substring(d);									
								}
								if (!params[i].contains("uploaded")) {
									parameters.putString(params[i].split("=")[0], params[i].split("=")[1]);	
								}								
							}									
						}													
						retrivePhotos(false,albumList,userPhotoList,isMainUser,displayHeader);
					}else {
						if (isMainUser) {							
							handler.obtainMessage(HandlerConstant.GET_FBK_MAIN_USER_PHOTOS_END,userPhotoList).sendToTarget();							
						}else {
							FbkAlbum album = new FbkAlbum();
							album.ID = id;							
							album.name = displayHeader+" "+name;
							album.bucketName = name;
							album.userPhotos = userPhotoList;
							album.count = userPhotoList.size();																			
							albumList.add(album);							
							retriveAlbumList(isMainUser, true, albumList);
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
	
}
