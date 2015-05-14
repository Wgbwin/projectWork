package com.kodak.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.kodak.kodak_kioskconnect_n2r.PrintHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DownloadPicture {

	public byte[] downloadImageFromURL(String imageURL) throws IOException{
		HttpEntity httpEntity = getHttpEntity(imageURL);
		if(httpEntity == null)
			return null;
		ByteArrayOutputStream imageBytesStream = getImageBytesStream(httpEntity);
		return imageBytesStream.toByteArray();
	}
	
	/*private Bitmap convertBytesToBitmap(ByteArrayOutputStream baos){
		byte [] imageBytes = baos.toByteArray();
		return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
	}*/
	
	private HttpEntity getHttpEntity(String Url) throws IOException{
		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpGet get = new HttpGet(PrintHelper.escapeURL(Url));
		
		HttpResponse response = client.execute(get);
		int statusCode = response.getStatusLine().getStatusCode();
		Log.e("DownloadPicture", "DownloadPicture, statusCode = " + statusCode);
		
		HttpEntity entity = response.getEntity();
		if(entity == null){
			Log.e("DownloadPicture", "HttpEntity is null");
		}
		
		return entity;
	}
	
	private ByteArrayOutputStream getImageBytesStream(HttpEntity entity) 
		throws IOException{
		InputStream is = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try{
			is = entity.getContent();
			byte[] buffer = new byte[1024];
			int readBytes = -1;
			while((readBytes = is.read(buffer)) != -1){
				baos.write(buffer, 0, readBytes);
			}
		} finally{
			if(baos != null)
				baos.close();
			if(is != null)
				is.close();
		}
		return baos;
	}
}
