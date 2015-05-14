package com.kodak.shareapi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class GalleryService {

	public String galleryURL = "https://ws.test.kdfse.com/ccs1/gallery";
	public String photoURL = "https://ws.test.kdfse.com/ccs1/photo";
	
	public String createAGallery(String url, String app, String app_version, String retailer,
			String partner, String country, String name, String token){
		String strResponse = null;
		String galleryUUID = "";

		url = url + "?app=" + app + "&app_version=" + app_version;
		if(retailer != null){
			url = url + "&retailer=" + retailer;
		}
		if(partner != null){
			url = url + "&partner=" + partner;
		}
		if(country != null){
			url = url + "&country=" + country;
		}
		if(name != null){
			url = url + "&name=" + name;
		}
		url = url + "&access_token=" + token;
		
		HttpsURLConnection conn = null;
		InputStream is = null;

		try {
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty(
					"Authorization",
					"Basic Y29tLWtkZnMtY2xpZW50LWV4dGVybmFsOmNvbS1rZGZzLWNsaWVudC1leHRlcm5hbC1wYXNz");
			conn.connect();
			int statusCode = conn.getResponseCode();
			Log.i("", "Status code:" + statusCode);
			switch(statusCode){
			case TokenGetter.OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				strResponse = sb.toString();
				Log.i("", "Response string:" + strResponse);
				try {

					//TODO
					JSONObject jsonObject = new JSONObject(strResponse);
					galleryUUID = jsonObject.getJSONObject("result")
					.getString("uuid");
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case TokenGetter.BAD_REQUEST:
				break;
			default: break;
			}
			return galleryUUID;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getAList(String url,String offset, String size, String token){
		String strResponse = null;
		url = url + "?access_token=" + token;
		if(offset != null && Integer.parseInt(offset) >= 0){
			url = url + "&offset=" + offset;
		}
		if(size != null && Integer.parseInt(size) > 0){
			url = url + "&size=" + size;
		}
		HttpsURLConnection conn = null;
		InputStream is = null;

		try {
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("GET");
			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.connect();
			int statusCode = conn.getResponseCode();
			Log.i("", "Status code:" + statusCode);
			switch(statusCode){
			case TokenGetter.OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				strResponse = sb.toString();
				Log.i("", "Response string:" + strResponse);
				try {

					//TODO
					JSONObject jsonObject = new JSONObject(strResponse);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case TokenGetter.BAD_REQUEST:
				break;
			default: break;
			}
			return strResponse;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getAGallery(String url, String galleryUUID, String token){
		String strResponse = null;

		url = url + "/" + galleryUUID + "?access_token=" + token;
		HttpsURLConnection conn = null;
		InputStream is = null;

		try {
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.connect();
			int statusCode = conn.getResponseCode();
			Log.i("", "Status code:" + statusCode);
			switch(statusCode){
			case TokenGetter.OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				strResponse = sb.toString();
				Log.i("", "Response string:" + strResponse);
				try {

					//TODO
					JSONObject jsonObject = new JSONObject(strResponse);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case TokenGetter.BAD_REQUEST:
				break;
			default: break;
			}
			return strResponse;	
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
	
	public String addAPhoto(String url, String galleryUUID, String token, String name, String filePath){
		String strResponse = null;

		url = url + "/" + galleryUUID + "/photo";
		String boundary = "AaB03x";
		String lineEnd = "\r\n";
		HttpsURLConnection conn = null;
		InputStream is = null;
		DataOutputStream dos = null;
		String status = "";

		Log.i("", "url:" + url);
		try {
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
//			conn.setUseCaches(false);
//			conn.setRequestProperty("Connection", "Keep-Alive");
//			conn.setRequestProperty("Charsert", "UTF-8");
			conn.setRequestProperty(
					"Authorization",
					"Basic Y29tLWtkZnMtY2xpZW50LWV4dGVybmFsOmNvbS1rZGZzLWNsaWVudC1leHRlcm5hbC1wYXNz");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=A1B2C3");
//			conn.connect();
			
			dos = new DataOutputStream(conn.getOutputStream());
			
			String reqLine = "--A1B2C3\r\nContent-Disposition: form-data; name=\"name\"\r\n\r\n";
		       dos.write(reqLine.getBytes());
		       dos.write("testpicture\r\n".getBytes());
		       
		       reqLine = "--A1B2C3\r\nContent-Disposition: form-data; name=\"access_token\"\r\n\r\n";
		       dos.write(reqLine.getBytes());
		       reqLine = token + "\r\n";
		       dos.write(reqLine.getBytes());
		       
		       reqLine = "--A1B2C3\r\nContent-Disposition: form-data; name=\"photo\"\r\n\r\n";
		       dos.write(reqLine.getBytes());
		       dos.flush();
			
			File file = null;
			FileInputStream fis = null;
			try{
				file = new File(filePath);
				fis = new FileInputStream(file);
				
				long totalRead = 0;
				byte[] buffer = new byte[4096];
				int readLen = fis.read(buffer);
				while(readLen >= 0){
					totalRead += readLen;
					dos.write(buffer, 0, readLen);
					dos.flush();
					readLen = fis.read(buffer);
				}
				Log.i("", "size:" + totalRead);
			}catch(Exception e){
				e.printStackTrace();
			}

			  reqLine = "\r\n--A1B2C3--\r\n";
		       dos.write(reqLine.getBytes());
		       dos.flush();
		       
			Log.i("", "add bytes done.");
			int statusCode = conn.getResponseCode();
			Log.i("", "Status code:" + statusCode + ",message:" + strResponse);
			switch(statusCode){
			case TokenGetter.OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				strResponse = sb.toString();
				Log.i("", "Response string:" + strResponse);
				try {
					//TODO
					JSONObject jsonObject = new JSONObject(strResponse);
					status = jsonObject.getJSONObject("status")
					.getString("code");
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case TokenGetter.BAD_REQUEST:
				break;
			default: break;
			}
			return status;
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}finally{
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getAListOfPhotos(String url, String galleryUUID, String token){
		String strResponse = null;

		url = url + "/" + galleryUUID + "/photo?access_token=" + token;
		HttpsURLConnection conn = null;
		InputStream is = null;

		try {
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("GET");
			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.connect();
			int statusCode = conn.getResponseCode();
			Log.i("", "Status code:" + statusCode);
			switch(statusCode){
			case TokenGetter.OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				strResponse = sb.toString();
				Log.i("", "Response string:" + strResponse);
				try {
					//TODO
					JSONObject jsonObject = new JSONObject(strResponse);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case TokenGetter.BAD_REQUEST:
				break;
			default: break;
			}
			return strResponse;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String updateAGallery(String url, String galleryUUID, String token){
		String strResponse = null;

		url = url + "/" + galleryUUID + "?access_token" + token;
		HttpsURLConnection conn = null;
		InputStream is = null;

		try {
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("PUT");
			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.connect();
			int statusCode = conn.getResponseCode();
			Log.i("", "Status code:" + statusCode);
			switch(statusCode){
			case TokenGetter.OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				strResponse = sb.toString();
				Log.i("", "Response string:" + strResponse);
				try {
					//TODO
					JSONObject jsonObject = new JSONObject(strResponse);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case TokenGetter.BAD_REQUEST:
				break;
			default: break;
			}
			return strResponse;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String deleteAGallery(String url, String galleryUUID, String token){
		String strResponse = null;

		url = url + "/" + galleryUUID + "?access_token" + token;
		HttpsURLConnection conn = null;
		InputStream is = null;

		try {
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("DELETE");
			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.connect();
			int statusCode = conn.getResponseCode();
			Log.i("", "Status code:" + statusCode);
			switch(statusCode){
			case TokenGetter.OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				strResponse = sb.toString();
				Log.i("", "Response string:" + strResponse);
				try {
					//TODO
					JSONObject jsonObject = new JSONObject(strResponse);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case TokenGetter.BAD_REQUEST:
				break;
			default: break;
			}
			return strResponse;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public Bitmap getAPhoto(String url, String photoUUID){
		String strResponse = null;
		Bitmap bitmap = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		url = url + "/" + photoUUID;
		HttpsURLConnection conn = null;
		InputStream is = null;

		try {
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("GET");
			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.connect();
			int statusCode = conn.getResponseCode();
			Log.i("", "Status code:" + statusCode);
			switch(statusCode){
			case TokenGetter.OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				byte[] buffer = new byte[1024];
				int readBytes = -1;
				while ((readBytes = is.read(buffer)) != -1) {
					baos.write(buffer, 0, readBytes);
				}
				Log.i("", "Response string:" + strResponse);
				byte[] imageBytes = baos.toByteArray();
				try {
					bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				break;
			case TokenGetter.BAD_REQUEST:
				break;
			default: break;
			}
			return bitmap;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try {
				if(baos != null){
					baos.close();
				}
				if (is != null) {
					is.close();
					is = null;
				}
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String deleteAPhoto(String url, String photoUUID, String token){
		String strResponse = null;

		url = url + "/" + photoUUID + "?access_token=" + token;
		HttpsURLConnection conn = null;
		InputStream is = null;

		try {
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("DELETE");
			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.connect();
			int statusCode = conn.getResponseCode();
			Log.i("", "Status code:" + statusCode);
			switch(statusCode){
			case TokenGetter.OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				strResponse = sb.toString();
				Log.i("", "Response string:" + strResponse);
				try {
					//TODO
					JSONObject jsonObject = new JSONObject(strResponse);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case TokenGetter.BAD_REQUEST:
				break;
			default: break;
			}
			return strResponse;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
