package com.kodakalaris.photokinavideotest.storydoc_format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

public class VideoGenParamsUploader extends IntentService {

	public static final String PARAM_VIDEO_PARAMS = "PARAM_VIDEO_PARAMS";
	public static final String PARAM_FACEBOOK_USERID = "PARAM_FACEBOOK_USERID";
	public static final String PARAM_FACEBOOK_ACCESS_TOKEN = "PARAM_FACEBOOK_ACCESS_TOKEN";
	public static final int RESULT_SUCCESS = 1;
	public static final int RESULT_FAILURE = 2;
	private static final String TAG = VideoGenParamsUploader.class.getSimpleName();
	private NotificationManager mNotifyManager;
	private Builder mBuilder;
	private final int mNotificationID = 1;
	private static final String SERVER_URL = "http://mdw-vm1.cloudapp.net/3PicStory/VideoService/";

	public VideoGenParamsUploader() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle options = intent.getExtras();
		final VideoGenParams params = (VideoGenParams) options.getParcelable(PARAM_VIDEO_PARAMS);
		String userID = options.getString(PARAM_FACEBOOK_USERID);
		String accessToken = options.getString(PARAM_FACEBOOK_ACCESS_TOKEN);
		uploadTheFile(params, userID, accessToken);

	}

	private void uploadTheFile(VideoGenParams params, String userID, String accessToken) {
		// Toast.makeText(this, "Starting Facebook Upload",
		// Toast.LENGTH_LONG).show();
		showNotification(params.mProjectTitle);
		String zipFilePath = params.persistToZipFile(this);
		Log.e(TAG, "Uploading file:" + zipFilePath + " for user " + userID);

		try {
			String result = postFileFacebook(zipFilePath, userID, params.mUUID.toString(), accessToken);
			// String result = postFileForVideo(zipFilePath, userID,
			// params.mUUID.toString());
			Log.i(TAG, "Upload result:" + result);
			returnResult(params, RESULT_SUCCESS);
		} catch (Exception e1) {
			e1.printStackTrace();
			returnResult(params, RESULT_FAILURE);
		}
		endNotification();
	}

	private void showNotification(String projectTitle) {
		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("Uploading Project: " + projectTitle).setContentText("Upload in progress").setSmallIcon(android.R.drawable.stat_sys_upload);
	}
	private void updateNotification(int incr) {
		mBuilder.setProgress(100, incr, false);
		mNotifyManager.notify(mNotificationID, mBuilder.build());
	}
	private void endNotification() {
		mBuilder.setContentText("Upload complete").setProgress(0, 0, false).setSmallIcon(android.R.drawable.stat_sys_upload_done);
		mNotifyManager.notify(mNotificationID, mBuilder.build());
	}
	private void returnResult(VideoGenParams params, int resultCode) {
		Bundle retunValues = new Bundle();
		// retunValues.putString(VideoGenerationService.RESULT_VIDEO_GEN,
		// videoFilePath);
		params.mReviever.send(resultCode, retunValues);

	}
	private String postFileFacebook(String fileName, String userID, String guid, String accessToken) throws Exception {
		String postString = SERVER_URL + "socialPost?userId=" + userID + "&guid=" + guid + "&dest=" + "facebook" + "&token=" + accessToken;
		return postFile(postString, fileName);
	}
	private String postFileForVideo(String fileName, String userID, String guid) throws Exception {
		String postString = SERVER_URL + "render?userId=" + userID + "&guid=" + guid + "&height=" + 720 + "&width=" + 720;
		return postFile(postString, fileName);
	}

	private String postFile(String postString, final String fileName) throws Exception {

		HttpClient client = new DefaultHttpClient();
		// String postString = SERVER_URL + userID + "/story/" + guid +
		// "?dest=facebook&token=" + accessToken;

		Log.e(TAG, "PostString:" + postString);
		HttpPost post = new HttpPost(postString);

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
					}
				}

				yourEntity.writeTo(new ProgressiveOutputStream(outstream));
			}

		};
		ProgressiveEntity myEntity = new ProgressiveEntity();
		post.setEntity(myEntity);
		HttpResponse response = client.execute(post);
		return getContent(response);
	}
	public static String getContent(HttpResponse response) throws IOException {
		InputStream in = response.getEntity().getContent();
		File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/KodakOutput/video.mp4");
		f.createNewFile();
		FileOutputStream out = new FileOutputStream(f);
		int len;
		byte[] buf = new byte[1024];
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		return response.getStatusLine().toString();
	}
}
