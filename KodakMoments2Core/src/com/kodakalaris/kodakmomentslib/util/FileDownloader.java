package com.kodakalaris.kodakmomentslib.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

public class FileDownloader {
	private static final String TAG = "FileDownloader";
	public static void download(boolean isStrongLoad, final String url,
			final String filePath, final OnProcessComplete<String> onComplete) {
		if (isStrongLoad || !new File(filePath).exists()) {
			new AsyncTask<Void, Void, String>() {
				@Override
				protected String doInBackground(Void... params) {
					try {
						boolean succeed = download(url, filePath);
						if (succeed)
							return filePath;
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void onPostExecute(String result) {
					if (onComplete != null)
						onComplete.onComplete(result);
				}

			}.execute();
		} else {
			if (onComplete != null) {
				String result = null;
				if (new File(filePath).exists()) {
					result = filePath;
				}
				onComplete.onComplete(result);
			}
		}

	}

	/**
	 * Download file from url
	 * 
	 * @param url
	 *            download from url
	 * @param path
	 *            full file path for the file downloaded
	 * @return
	 */
	public static boolean download(String url, String filePath) {
		boolean downloadSuccessFlag = true;
		int fileLength = 0;
		File localResourceFile = null;
		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;

		try {
			File baseDir = new File(FileUtil.getParentDirectoryPath(filePath));
			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}
			HttpURLConnection conn = (HttpURLConnection) new URL(url)
					.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(6 * 1000);
			
			fileLength = conn.getContentLength();
			Log.d(TAG, "download file from:" + url);
			Log.d(TAG, "download file to:" + filePath);
			Log.d(TAG, "file length:" + fileLength);
			
			int responseCode = conn.getResponseCode();
			
			Log.d(TAG, "conn.getResponseCode():" + responseCode);
			if (responseCode == 200) {
				localResourceFile = new File(filePath);
				fileOutputStream = new FileOutputStream(localResourceFile);

				inputStream = conn.getInputStream();

				int byteReadTotal = 0;
				byte[] buffer = new byte[1024];
				int bytesRead = -1;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, bytesRead);
					byteReadTotal += bytesRead;
				}
				fileOutputStream.flush();
				if (Math.abs(fileLength - byteReadTotal) >= 512) {
					downloadSuccessFlag = false;
					localResourceFile.delete();
				}
			}
		} catch (Exception e) {
			Log.i(TAG, "Exception:" + e.getMessage());
			if (localResourceFile != null) {
				localResourceFile.delete();
			}
			downloadSuccessFlag = false;
		} finally {
			try {
				if (fileOutputStream != null)
					fileOutputStream.close();
				if (inputStream != null)
					inputStream.close();
			} catch (IOException ex) {
				if (localResourceFile != null) {
					localResourceFile.delete();
				}
				downloadSuccessFlag = false;
			}
		}
		Log.d(TAG, "downloadSuccess-->" + downloadSuccessFlag + ":" + filePath);
		return downloadSuccessFlag;
	}

	public interface OnProcessComplete<T> {
		public void onComplete(T result);
	}

}
