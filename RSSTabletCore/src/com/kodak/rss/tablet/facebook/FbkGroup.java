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

@SuppressWarnings("deprecation")
public class FbkGroup extends FbkObject{
	public Handler handler;		
	private Bundle parameters;
	
	public String getProfilePictureLink(){
		// small, normal, large, square
		return AppConstants.SCOPE + this.ID + "/picture"+"?type="+ "normal" ;
	}
	
	public URI getGroupUri(){
		URI pictureURI;			
		try {
			pictureURI = new URI(getProfilePictureLink());
		} catch (URISyntaxException e) {
			pictureURI = null;
		}
		return pictureURI;
	}
	
	public void retriveAlbumList(boolean isFrist,final ArrayList<FbkObject> albums){
		FBKWrapper wrapper = FBKWrapper.getWrapper();
		AsyncFacebookRunner requestor = new AsyncFacebookRunner(wrapper.facebook);
		if (isFrist || parameters == null) {
			parameters = new Bundle();			
		}
		parameters.putString("limit", "400");
		requestor.request(this.ID + "/albums", parameters,new RequestListener() {
			
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
					JSONArray arrayFriends = jsAlbums.getJSONArray("data");
					
					try {	
						JSONObject arrayPage = jsAlbums.getJSONObject("paging");
						nextUrl = arrayPage.getString("next");	
					}catch (Exception e) {
						nextUrl = null;
					} 	
					
					for (int i = 0; i < arrayFriends.length(); i++) {
						JSONObject obj = arrayFriends.getJSONObject(i);
						FbkAlbum album = new FbkAlbum();
						album.ID = obj.getString("id");
						album.name = obj.getString("name");
						album.bucketName = name;
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
						retriveAlbumList(false,albums);
					}else {
						handler.obtainMessage(HandlerConstant.GET_FBK_GROUP_ALBUMS_END,albums).sendToTarget();
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
	
	public void retrivePhotoList(boolean isFrist,final ArrayList<FbkObject> albums){
		FBKWrapper wrapper = FBKWrapper.getWrapper();
		AsyncFacebookRunner requestor = new AsyncFacebookRunner(wrapper.facebook);		
		if (isFrist || parameters == null) {
			parameters = new Bundle();			
		}
		parameters.putString("limit", "400");
		requestor.request(this.ID + "/feed", parameters,new RequestListener() {
			
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
					JSONArray arrayPhotos = jsAlbums.getJSONArray("data");
					try {	
						JSONObject arrayPage = jsAlbums.getJSONObject("paging");
						nextUrl = arrayPage.getString("next");	
					}catch (Exception e) {
						nextUrl = null;
					} 								
					
					for (int i = 0; i < arrayPhotos.length(); i++) {						
						JSONObject obj = arrayPhotos.getJSONObject(i);
						if (obj.getString("type") != null && "photo".equalsIgnoreCase(obj.getString("type"))) {
							FbkAlbum album = new FbkAlbum();
							album.ID = obj.getString("object_id");								
							albums.add(album);
						}					
					}
					
					if (nextUrl != null && !"".equalsIgnoreCase(nextUrl)) {
						parameters = new Bundle();	
						String[] params = nextUrl.split("&");
						for (int i = 0; i < params.length; i++) {
							if (!params[i].contains("limit")&&!params[i].contains("format") && !params[i].contains("access_token")) {								
								parameters.putString(params[i].split("=")[0], params[i].split("=")[1]);								
							}							
						}
						retrivePhotoList(false,albums);
					}else {	
						int size = albums.size();
						if (size > 0) {							
							ArrayList<FbkObject> photos = new ArrayList<FbkObject>(size);
							for (int i = 0; i <size; i++) {
								FbkAlbum album = (FbkAlbum) albums.get(i);
								retrivePhotoList(album, photos,size);								
							}
						}else {
							handler.obtainMessage(HandlerConstant.GET_FBK_GROUP_IMAGE_END,albums).sendToTarget();
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
	
	
	public void retrivePhotoList(FbkAlbum album,final ArrayList<FbkObject> photos,final int size){
		FBKWrapper wrapper = FBKWrapper.getWrapper();
		AsyncFacebookRunner requestor = new AsyncFacebookRunner(wrapper.facebook);												
		requestor.request(album.ID, new Bundle(),new RequestListener() {
			
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
					JSONObject jsPhotos = Util.parseJson(response);
					if (jsPhotos != null) {
						FbkPhoto photo = new FbkPhoto();
						photo.ID = jsPhotos.getString("id");					
						photo.bucketName = name;
						photo.UserName = bucketName;
						JSONArray arrayPhotoSouces = jsPhotos.getJSONArray("images");
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
						photos.add(photo);
					}
					
					if (photos.size() == size) {
						handler.obtainMessage(HandlerConstant.GET_FBK_GROUP_IMAGE_END,photos).sendToTarget();
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
