package com.kodak.shareapi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.kodak.utils.AESUtil;

import android.util.Log;

public class TokenGetter {

	public final static int OK = 200;
	public final static int BAD_REQUEST = 400;
	public final static int BAD_GATEWAY = 502;

	public ClientTokenResponse httpClientTokenUrlPost(String url) {
		String strResponse = null;
		ClientTokenResponse response = null;

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
			// conn.setRequestProperty("app", "CUMMOBANDWMC");
			// conn.setRequestProperty("app_version", "1.0");
			// conn.setRequestProperty("retailer", "walmart-CAN");
			conn.setRequestProperty(
					"Authorization",
					"Basic Y29tLWtkZnMtY2xpZW50LWV4dGVybmFsOmNvbS1rZGZzLWNsaWVudC1leHRlcm5hbC1wYXNz");

			conn.connect();
			int statusCode = conn.getResponseCode();
			Log.i("", "Status code:" + statusCode);
			switch (statusCode) {
			case OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						is, "UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				strResponse = sb.toString();
				Log.i("", "Response string:" + strResponse);
				response = new ClientTokenResponse();
				try {
					Log.i("", "client token" + response.client_token
							+ ",client secret:" + response.client_secret
							+ ",expires in:" + response.expires_in);
					JSONObject jsonObject = new JSONObject(strResponse);
					response.client_token = jsonObject.getJSONObject("result")
							.getString("client_token");
					response.expires_in = jsonObject.getJSONObject("result")
							.getString("expires_in");
					response.client_secret = jsonObject.getJSONObject("result")
							.getString("client_secret");
					Log.i("", "client token:" + response.client_token
							+ ",client secret:" + response.client_secret
							+ ",expires in:" + response.expires_in);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case BAD_REQUEST:
				break;
			default:
				break;
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		} finally {
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

	private String concatenateString(String s1, String s2) {
		if (s1 == null || s2 == null) {
			return null;
		}
		return s1 + ":" + s2;
	}

	public AccessTokenResponse httpAccessTokenUrlPost(String url, String clientToken,
			String username, String userPwd, String clientSecret)
			throws Exception {
		String iniVector = clientToken.substring(0, 16);
		String usernamePwd = concatenateString(username, userPwd);
		String signature = AESUtil
				.Encrypt(usernamePwd, clientSecret, iniVector);
		url = url + "?client_token=" + clientToken + "&signature=" + signature;

		String strResponse = null;
		AccessTokenResponse response = null;
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
			switch (statusCode) {
			case OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						is, "UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				strResponse = sb.toString();
				Log.i("", "Response string:" + strResponse);
				response = new AccessTokenResponse();
				try {
					Log.i("", "access token" + response.access_token
							+ ",refresh token:" + response.refresh_token
							+ ",expires in:" + response.expire_in);
					JSONObject jsonObject = new JSONObject(strResponse);
					response.access_token = jsonObject.getJSONObject("result")
							.getString("access_token");
					response.expire_in = jsonObject.getJSONObject("result")
							.getString("expires_in");
					response.refresh_token = jsonObject.getJSONObject("result")
							.getString("refresh_token");
					response.status = jsonObject.getJSONObject("status")
					.getString("code");
					response.getAccessTokenTime = new Date().getTime()/1000;
					Log.i("", "access token" + response.access_token
							+ ",refresh token:" + response.refresh_token
							+ ",expires in:" + response.expire_in
							+ ",status code:" + response.status);
					response.client_token = clientToken;
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case BAD_REQUEST:
				Log.e("TokenGetter", "user/password: " + statusCode);
				response = new AccessTokenResponse();
				response.status = "" + statusCode;
				break;
			case BAD_GATEWAY:
				Log.e("TokenGetter", "bad gateway " + statusCode);
				response = new AccessTokenResponse();
				response.status = "" + statusCode;
				break;
			default:
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
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

		return response;
	}

	public AccessTokenResponse httpRefreshTokenUrlPost(String url, String clientToken,
			String refreshToken)
			throws Exception {
		
		url = url + "?client_token=" + clientToken + "&refresh_token=" + refreshToken;

		String strResponse = null;
		AccessTokenResponse response = null;
		HttpsURLConnection conn = null;
		InputStream is = null;

		try {
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("PUT");
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
			switch (statusCode) {
			case OK:
				strResponse = conn.getResponseMessage();
				Log.i("", "Response string:" + strResponse);
				is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						is, "UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				strResponse = sb.toString();
				Log.i("", "Response string:" + strResponse);
				response = new AccessTokenResponse();
				try {
					Log.i("", "access token" + response.access_token
							+ ",refresh token:" + response.refresh_token
							+ ",expires in:" + response.expire_in);
					JSONObject jsonObject = new JSONObject(strResponse);
					response.access_token = jsonObject.getJSONObject("result")
							.getString("access_token");
					response.expire_in = jsonObject.getJSONObject("result")
							.getString("expires_in");
					response.refresh_token = jsonObject.getJSONObject("result")
							.getString("refresh_token");
					response.status = jsonObject.getJSONObject("status")
					.getString("code");
					response.getAccessTokenTime = new Date().getTime()/1000;
					response.client_token = clientToken;
					Log.i("", "access token" + response.access_token
							+ ",refresh token:" + response.refresh_token
							+ ",expires in:" + response.expire_in
							+ ",status code:" + response.status);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case BAD_REQUEST:
				break;
			default:
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
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

		return response;
	}

}
