package com.kodakalaris.video.storydoc_format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodakalaris.video.activities.ShareDestinationSelectActivity;
import com.kodakalaris.video.video_gen.VideoGenResultReceiver;

public class VideoGenParamsUploader extends IntentService {

	public static final String PARAM_VIDEO_PARAMS = "PARAM_VIDEO_PARAMS";
	public static final String PARAM_FACEBOOK_USERID = "PARAM_FACEBOOK_USERID";
	public static final String PARAM_FACEBOOK_ACCESS_TOKEN = "PARAM_FACEBOOK_ACCESS_TOKEN";
	public static final String PARAM_UPLOAD_TYPE = "PARAM_UPLOAD_TYPE";
	public static final int PARAM_UPLOAD_TYPE_FACEBOOK = 0x000010;
	public static final int PARAM_UPLOAD_TYPE_MMS = 0x000011;
	public static final int PARAM_UPLOAD_TYPE_HD = 0x000012;
	/* These must be unique */
	public static final int RESULT_SUCCESS = 1;
	public static final int RESULT_FAILURE = 2;
	public static int SHARE_START = 3;
	public static int SHARE_UPLOADING = 4;
	public static int SHARE_RENDERING = 5;
	public static int SHARE_SAVING = 6;
	/* These must be unique */
	public static final String RESULT_VIDEO_PATH = "RESULT_VIDEO_PATH";
	private static final String TAG = VideoGenParamsUploader.class.getSimpleName();
	private NotificationManager mNotifyManager;
	private Builder mBuilder;
	private final int mNotificationID = 1;
	private String mVideoPath;
	public static final String mVideoOutputPathBase = Environment.getExternalStorageDirectory().getAbsolutePath() + "/KodakOutput/";
	public static final String mVideoOutputPathBase4Gallery = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/";

	public VideoGenParamsUploader() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle options = intent.getExtras();
		final VideoGenParams params = (VideoGenParams) options.getParcelable(PARAM_VIDEO_PARAMS);
		String proJectTitle = params.mProjectTitle ;
		if( !TextUtils.isEmpty(proJectTitle)){
			proJectTitle = filenameFilter(proJectTitle) ;
			if(!TextUtils.isEmpty(proJectTitle)){
				proJectTitle = proJectTitle.trim() ;
			}
		}
		String title = TextUtils.isEmpty(proJectTitle) ? "Tell My Story" :proJectTitle.trim() ;
//		String title = "".equals(params.mProjectTitle) ? "Tell My Story" : params.mProjectTitle.trim();
		int uploadType = options.getInt(PARAM_UPLOAD_TYPE);
		String dirPath = uploadType == PARAM_UPLOAD_TYPE_HD ? mVideoOutputPathBase4Gallery : mVideoOutputPathBase;
		mVideoPath = dirPath + title + ".mp4";
		for (int i = 1; isVideoExist(mVideoPath); i++) {
			mVideoPath = dirPath + title + " (" + i + ")" + ".mp4";
		}
		onMakeVideoUploading();
		showNotification(params.mProjectTitle);
		String zipFilePath = params.persistToZipFile(this);
		Log.e(TAG, "Uploading file:" + zipFilePath);
		String language = Locale.getDefault().toString();
		String result = "";
		int intentType = INTENT_TYPE_NONE;
		try {

			switch (uploadType) {
			case PARAM_UPLOAD_TYPE_FACEBOOK:
				String userID = options.getString(PARAM_FACEBOOK_USERID);
				String accessToken = options.getString(PARAM_FACEBOOK_ACCESS_TOKEN);
				result = postFileFacebook(zipFilePath, userID, params.mUUID.toString(), accessToken, language);
				intentType = INTENT_TYPE_NONE;
				break;
			case PARAM_UPLOAD_TYPE_HD:
				result = postFileForHD(zipFilePath, params.mUUID.toString(), language);
				intentType = INTENT_TYPE_PLAY;
				break;
			case PARAM_UPLOAD_TYPE_MMS:
				result = postFileForMMS(zipFilePath, params.mUUID.toString(), language);
				intentType = INTENT_TYPE_SHARE;
				break;
			}
			returnResult(RESULT_SUCCESS, true);
		} catch (Exception e) {
			//Fix for RSSMOBILEPDC-1818
			//Delete error file
			try {
				File file = new File(mVideoPath);
				if (file.exists()) {
					file.delete();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			
			intentType = INTENT_TYPE_NONE;
			result = e.toString();
			e.printStackTrace();
			returnResult(RESULT_FAILURE, false);
		} finally {
			endNotification(intentType, params);
			Log.i(TAG, "Upload result:" + result);
		}

	}
	
	
	private String filenameFilter(String str) { 
		Pattern filePattern = Pattern.compile("[\\\\/:*?\",<>|]");  
		return str==null?null:filePattern.matcher(str).replaceAll("");  
	}

	private void showNotification(String projectTitle) {
		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("Uploading: " + projectTitle).setContentText("Upload in progress").setSmallIcon(android.R.drawable.stat_sys_upload);
	}

	private void updateNotification(int incr) {
		mBuilder.setProgress(100, incr, false);
		mNotifyManager.notify(mNotificationID, mBuilder.build());
	}

	private static final int INTENT_TYPE_NONE = 0;
	private static final int INTENT_TYPE_SHARE = 1;
	private static final int INTENT_TYPE_PLAY = 2;

	private void endNotification(int intentType, VideoGenParams params) {
		if (intentType != INTENT_TYPE_NONE) {
			Intent intent = new Intent();
			if (intentType == INTENT_TYPE_SHARE) {
				ResolveInfo resoInfo = ShareDestinationSelectActivity.getSmsApp(this);
				intent.setClassName(resoInfo.activityInfo.packageName, resoInfo.activityInfo.name);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(Intent.ACTION_SEND);
				//In some photo(ex:samsung s4), EXTRA_SUBJECT will cover sms_body, so I commented it
//				intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.TMS_mms_message, params.mProjectTitle));
				intent.putExtra(Intent.EXTRA_TEXT, "");
				intent.putExtra("sms_body", getString(R.string.TMS_mms_message, params.mProjectTitle));
				intent.setType("video/mp4");
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mVideoPath)));
				startActivity(intent);
			} else if (intentType == INTENT_TYPE_PLAY) {
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(mVideoPath)), "video/mp4");
				scanFileForAdd2Media(mVideoPath);
			}
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, Intent.createChooser(intent, "Choose Title"), PendingIntent.FLAG_CANCEL_CURRENT);
			mBuilder.setContentIntent(pendingIntent);
		}
		mBuilder.setContentText("Upload complete").setProgress(0, 0, false).setSmallIcon(android.R.drawable.stat_sys_upload_done);
		mNotifyManager.notify(mNotificationID, mBuilder.build());
	}

	private void returnResult(int resultCode, boolean returnFile) {
		Bundle retunValues = new Bundle();
		if (returnFile) {
			retunValues.putString(RESULT_VIDEO_PATH, mVideoPath);
		}
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(VideoGenResultReceiver.UPLOAD_COMPLETE_RESPONCE);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(VideoGenResultReceiver.RESULT_CODE, resultCode);
		sendBroadcast(broadcastIntent);

	}

	private String postFileFacebook(String fileName, String userID, String guid, String accessToken, String lang) throws Exception {
//		String postString = getResources().getString(R.string.cumulus_tms_project_upload_social_url) + "?userId=" + userID + "&guid=" + guid + "&lang=" + lang + "&dest=" + "facebook" + "&token="
//				+ accessToken + "&mode=" + "SocialPost";
		//String postString = getCurrentUrl4Social() + "socialPost?userId=" + userID + "&guid=" + guid + "&dest=" + "facebook" + "&token=" + accessToken;
		String postString = getCurrentUrl4Social() + "socialPost?userId=" + userID + "&guid=" + guid + "&lang=" + lang + "&dest=" + "facebook" + "&token=" + accessToken;
		return postFile(postString, fileName);
	}

	private String postFileForHD(String fileName, String guid, String lang) throws Exception {
//		String postString = getResources().getString(R.string.cumulus_tms_project_upload_render_url) + "?guid=" + guid + "&lang=" + lang + "&height=" + 720 + "&width=" + 720 + "&mode=" + "Download";
//		String postString = getResources().getString(R.string.cumulus_tms_project_upload_render_url) + "?userId=" + guid + "&guid=" + guid + "&height=" + 720 + "&width=" + 720 + "&mode=" + "Download";
		String postString = getCurrentUrl4Render() + "render?userId=" + guid + "&guid=" + guid + "&mode=" + "Download";
		return postFile(postString, fileName);
	}

	private String postFileForMMS(String fileName, String guid, String lang) throws Exception {
//		String postString = getResources().getString(R.string.cumulus_tms_project_upload_render_url) + "?uid=" + guid +"&guid=" + guid + "&lang=" + lang + "&height=" + 208 + "&width=" + 208 + "&mode=" + "MMS";
//		String postString = getResources().getString(R.string.cumulus_tms_project_upload_render_url) + "?userId=" + guid + "&guid=" + guid + "&height=" + 208 + "&width=" + 208 + "&mode=" + "MMS";
		String postString = getCurrentUrl4Render() + "render?userId=" + guid + "&guid=" + guid + "&mode=" + "MMS";
		return postFile(postString, fileName);
	}

	private String postFile(String postString, final String fileName) throws Exception {
		/*
		 * Uri uri = new Uri(ServerURL + method);
            WebRequest webRequest = (HttpWebRequest)WebRequest.Create(uri);
            string authInfo = UserName + ":" + Password;
            authInfo = Convert.ToBase64String(Encoding.UTF8.GetBytes(authInfo));
            webRequest.UseDefaultCredentials = false;
            webRequest.Headers["Authorization"] = "Basic " + authInfo;

            webRequest.Method = "POST";
            webRequest.ContentType = "application/octet-stream";
		 * */
		
		HttpClient client = new DefaultHttpClient();

		Log.e(TAG, "PostString:" + postString);
		HttpPost post = new HttpPost(postString);
		
		//add auth info
		String authInfo = getAuthorizationToken();
		post.addHeader("Authorization", "Basic " + authInfo);
		
		final HttpEntity yourEntity = new FileEntity(new File(fileName), "application/octet-stream");

		class ProgressiveEntity implements HttpEntity {
			@Override
			public void consumeContent() throws IOException {
				yourEntity.consumeContent();
			}

			@Override
			public InputStream getContent() throws IOException, IllegalStateException {
				return yourEntity.getContent();
			}

			@Override
			public org.apache.http.Header getContentEncoding() {
				return yourEntity.getContentEncoding();
			}

			@Override
			public long getContentLength() {
				return yourEntity.getContentLength();
			}

			@Override
			public Header getContentType() {
				return yourEntity.getContentType();
			}

			@Override
			public boolean isChunked() {
				return yourEntity.isChunked();
			}

			@Override
			public boolean isRepeatable() {
				return yourEntity.isRepeatable();
			}

			@Override
			public boolean isStreaming() {
				return yourEntity.isStreaming();
			} // CONSIDER put a _real_ delegator into here!

			@Override
			public void writeTo(OutputStream outstream) throws IOException {

				class ProxyOutputStream extends FilterOutputStream {
					public ProxyOutputStream(OutputStream proxy) {
						super(proxy);
					}

					public void write(int idx) throws IOException {
						out.write(idx);
					}

					public void write(byte[] bts) throws IOException {
						out.write(bts);
					}

					public void write(byte[] bts, int st, int end) throws IOException {
						out.write(bts, st, end);
					}

					public void flush() throws IOException {
						out.flush();
					}

					public void close() throws IOException {
						out.close();
					}
				}
				class ProgressiveOutputStream extends ProxyOutputStream {
					private int mBytesWritten;
					private long mBytesTotal;

					public ProgressiveOutputStream(OutputStream proxy) {
						super(proxy);
						this.mBytesWritten = 0;
						this.mBytesTotal = new File(fileName).length();
					}

					public void write(byte[] bts, int st, int end) throws IOException {
						// Log.e(TAG, "Progress S:" + st + " E:" + end + " L:" +
						// bts.length);
						mBytesWritten += end;
						double percent = ((double) mBytesWritten / (double) mBytesTotal * 100.0f);
						// Log.e(TAG, "Percent:" + percent);
						updateNotification((int) percent);
						out.write(bts, st, end);
						if (percent == 100) {
							onMakeVideoRendering();
						}
					}
				}

				yourEntity.writeTo(new ProgressiveOutputStream(outstream));
			}

		}
		;
		ProgressiveEntity myEntity = new ProgressiveEntity();
		post.setEntity(myEntity);
		HttpResponse response = client.execute(post);
		Log.i(TAG,"result:" + response.getStatusLine().toString());
		String resultStatusLine = getContent(response) ;
		if(post!=null && !post.isAborted()){
			post.abort() ;
		}
		
		return resultStatusLine;
	}

	public String getContent(HttpResponse response) throws Exception  {
		
		onMakeVideoSaving();
		InputStream in = null ;
		FileOutputStream out = null ;
		String resultStatusLine = "" ;
		try {
			in = response.getEntity().getContent() ;
			File f = new File(mVideoPath);
			f.createNewFile();
			out = new FileOutputStream(f);
			int len ;
			byte[] buf = new byte[8*1024];
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				
				SystemClock.sleep(5) ;
			}
			
			resultStatusLine = response.getStatusLine().toString() ;
			
		} catch (IllegalStateException e) {
			throw e ;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw e ;
		} catch (IOException e) {
			throw e ;
			
		}finally{
			if(out!=null){
				try {
					out.close() ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(in!=null){
				try {
					in.close() ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		return resultStatusLine ;
	}

	private boolean isVideoExist(String path) {
		File file = new File(path);
		return file.exists();
	}

	public void onMakeVideoUploading() {
		returnResult(SHARE_UPLOADING, false);
	};

	public void onMakeVideoRendering() {
		returnResult(SHARE_RENDERING, false);
	};

	public void onMakeVideoSaving() {
		returnResult(SHARE_SAVING, false);
	};
	
	private void scanFileForAdd2Media(String path) {
		Uri contentUri = Uri.fromFile(new File(path));
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); 
		mediaScanIntent.setData(contentUri);
		sendBroadcast(mediaScanIntent);
	}
	
	private String getCurrentServer() {
		String firstName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("firstName", "");
		String server = "rssdev.kodak.com";
		
		try {
			URL url = new URL(getString(R.string.cumulus_tms_project_upload_render_url));
			server = url.getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		if ("RSS_Staging".equalsIgnoreCase(firstName)) {
			server = "mykodakmomentsstage.kodak.com";
		} else if ("RSS_Production".equalsIgnoreCase(firstName)) {
			server = "mykodakmoments.kodak.com";
		} else if ("RSS_Development".equalsIgnoreCase(firstName)) {
			server = "rssdev.kodak.com";
		} else if ("RSS_ENV1".equalsIgnoreCase(firstName)) {
			server = "RSSDEV1.KODAK.COM";
		} else if ("RSS_ENV2".equalsIgnoreCase(firstName)) {
			server = "RSSDEV2.KODAK.COM";
		}
		
		return server;
		
	}
	
	private String getCurrentUrl4Social() {
		String url = getString(R.string.cumulus_tms_project_upload_social_url);;
		try {
			String host = new URL(url).getHost();
			return url.replace(host, getCurrentServer());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return url;
		}
	}
	
	private String getCurrentUrl4Render() {
		String url = getString(R.string.cumulus_tms_project_upload_render_url);
		try {
			String host = new URL(url).getHost();
			return url.replace(host, getCurrentServer());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return url;
		}
	}
	
	private String getAuthorizationToken() {
		String webServicesAppID = getString(R.string.cumulus_appid);
		String webServicesAppPassword = getString(R.string.cumulus_password);
		String authorizationServiceURL = getString(R.string.cumulus_authorizationserviceurl) + webServicesAppID + "&scope=all";
		try {
			String currentTMSServer = getCurrentServer();
			String currentAuthServer = new URL(authorizationServiceURL).getHost();
			
			authorizationServiceURL = authorizationServiceURL.replace(currentAuthServer, currentTMSServer);
			
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		String result = "";
		StringBuilder concatenatedAppIdPasswordSB = new StringBuilder();
		concatenatedAppIdPasswordSB.append(webServicesAppID);
		concatenatedAppIdPasswordSB.append(':');
		concatenatedAppIdPasswordSB.append(webServicesAppPassword);
		String concatenated = concatenatedAppIdPasswordSB.toString();
		String encodedUsernamePassword = Base64.encodeToString(concatenated.getBytes(), Base64.NO_WRAP);
		HttpPost httpPost = new HttpPost(authorizationServiceURL);
		httpPost.setHeader("Authorization", "Basic " + encodedUsernamePassword);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "application/json");
		InputStream is = null;
		HttpParams myParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(myParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(myParams, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(myParams, 10000);
		HttpConnectionParams.setSoTimeout(myParams, 12000);
		DefaultHttpClient httpClient = new DefaultHttpClient(myParams);
		BasicHttpContext localContext = new BasicHttpContext();
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPost, localContext);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (response == null) {
			Log.w(TAG, "response == null");
		} else {
			StatusLine sl = response.getStatusLine();
			if (sl == null) {
				Log.e(TAG, "Status Line == null");
			} else {
				switch (sl.getStatusCode()) {
				case 200:
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
						StringBuilder sb = new StringBuilder();
						String line = null;
						while ((line = reader.readLine()) != null) {
							sb.append(line + "\n");
						}
						is.close();
						try {
							JSONObject jObject = new JSONObject(sb.toString());
							JSONObject accessTokenResponse = (JSONObject) jObject.get("AccessTokenResponse");
							String accessToken = (String) accessTokenResponse.get("AccessToken");
							StringBuilder authorizationTokenStringBuilder = new StringBuilder();
							authorizationTokenStringBuilder.append(webServicesAppID);
							authorizationTokenStringBuilder.append(':');
							authorizationTokenStringBuilder.append(accessToken);
							result = Base64.encodeToString(authorizationTokenStringBuilder.toString().getBytes(), Base64.NO_WRAP);
						} catch (JSONException jsone) {
							jsone.printStackTrace();
						}
					} catch (Exception ex) {
						Log.e(TAG, "Error parsing data " + ex.toString());
					}
					break;
				default:
					Log.e(TAG, "Unexpected response to Authorization Request: " + sl.getReasonPhrase());
					break;
				}
			}
		}
		return result;
	}
}
