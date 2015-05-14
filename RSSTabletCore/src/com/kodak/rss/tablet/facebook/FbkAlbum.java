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
public class FbkAlbum extends FbkObject{
	public String Type;
	public Handler handler;
	private Bundle parameters;
	
	public ArrayList<FbkObject> userPhotos;
	
	public String getAlbumCoverPhotoLink(){
		FBKWrapper wrapper = FBKWrapper.getWrapper();
		// thumbnail, album , normal
		return AppConstants.SCOPE + this.ID + "/picture" + "?type="+ "album" +"&&access_token="+ wrapper.facebook.getAccessToken();
	}
	
	public URI getAlbumUri(){
		URI pictureURI;			
		try {
			pictureURI = new URI(getAlbumCoverPhotoLink());
		} catch (URISyntaxException e) {
			pictureURI = null;
		}
		return pictureURI;
	}
	
	public void retrivePhotos(boolean isFrist,final ArrayList<FbkObject> photos,final String fbkObjectType){
		FBKWrapper wrapper = FBKWrapper.getWrapper();		
		AsyncFacebookRunner requestor = new AsyncFacebookRunner(wrapper.facebook);	
		if (isFrist || parameters == null) {
			parameters = new Bundle();			
		}
		parameters.putString("limit", "400");	
		parameters.putCharSequence("fields", "id,images");//id ,source
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
						photo.ID = aPhotos.getString("id");					
						photo.bucketName = name;
						photo.UserName = bucketName;
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
						photos.add(photo);
					}
					
					if (nextUrl != null && !"".equalsIgnoreCase(nextUrl)) {
						parameters = new Bundle();	
						String[] params = nextUrl.split("&");
						for (int i = 0; i < params.length; i++) {
							if (!params[i].contains("limit")&&!params[i].contains("format") && !params[i].contains("access_token") && !params[i].contains("id")) {
								if (params[i].contains("?")) {									
									int d = params[i].lastIndexOf("?")+1;										
									params[i] = params[i].substring(d);									
								}
								parameters.putString(params[i].split("=")[0], params[i].split("=")[1]);								
							}							
						}
						retrivePhotos(false,photos,fbkObjectType);
					}else {
						
						if (AppConstants.isfbkMain.equalsIgnoreCase(fbkObjectType)) {
							handler.obtainMessage(HandlerConstant.GET_FBK_MAIN_IMAGE_END,photos).sendToTarget();
						}else if (AppConstants.isfbkFriend.equalsIgnoreCase(fbkObjectType)) {
							handler.obtainMessage(HandlerConstant.GET_FBK_FRIEND_IMAGE_END,photos).sendToTarget();
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