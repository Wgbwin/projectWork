package com.kodak.rss.tablet.util.load;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.util.load.ImageDownloader.Downloader;
import com.kodak.rss.tablet.util.load.ImageDownloader.RequestKey;

public class DownloadWorkItem implements Runnable {

	private Context context;
	private RequestKey key;
	private boolean allowCached;
	private String profileId;
	private String saveType;	
	private boolean isThumbnail;
	private int[] viewParameters;
	private int refreshCount;

	private String tempFolder;
	private String fileSaveName;

	DownloadWorkItem(String profileId,Context context, RequestKey key,boolean allowCached, String saveType,int refreshCount, boolean isThumbnail,int[] viewParameters) {
		this.context = context;
		this.profileId = profileId;
		this.key = key;
		this.allowCached = allowCached;		
		this.saveType = saveType;		
		this.isThumbnail = isThumbnail;
		this.viewParameters = viewParameters;
		this.refreshCount = refreshCount;

		tempFolder = FilePathConstant.tempSaveFolder + saveType;		
		if (refreshCount > 0) {
			String count = "_"+String.valueOf(refreshCount);
			fileSaveName = profileId+ count + FilePathConstant.postfix;
		}else {
			fileSaveName = profileId + FilePathConstant.postfix;
		}		
	}

	@Override
	public void run() {		
		download(key, context, allowCached, refreshCount,viewParameters);
	}
	
	private boolean isCancel(RequestKey key){
		boolean isCancelled = false;
		Downloader dealDownloader = ImageDownloader.getRequest(key);
	    if (dealDownloader != null && dealDownloader.isCancelled) {
	    	isCancelled = true;
	    }
		return isCancelled;
	}

	private void download(RequestKey key, Context context, boolean allowCached,int refreshCount,int[] viewParameters) {
	    if (isCancel(key)) {
	    	Log.d("DownloadWorkItem Cancel", "keyId: "+key.uri);
	    	ImageDownloader.issueResponse(key, null, null, false);
	    	return;
	    }
	    Log.d("DownloadWorkItem Down", "keyId: "+key.uri);
		HttpURLConnection connection = null;
		InputStream stream = null;
		Exception error = null;
		Bitmap bitmap = null;
		boolean issueResponse = true;

		try {
			URL url = new URL(key.uri.toString());
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Accept-Encoding", "identity"); 
			connection.setInstanceFollowRedirects(false);
			connection.setConnectTimeout(30000);  			
			connection.setReadTimeout(90000);  
			
			if (isCancel(key)) {				
				LoaderUtil.disconnectQuietly(connection);
			    ImageDownloader.issueResponse(key, null, null, false);
			    return;
			 }
					
			switch (connection.getResponseCode()) {
			case HttpURLConnection.HTTP_MOVED_PERM:
			case HttpURLConnection.HTTP_MOVED_TEMP:
				issueResponse = false;
				String redirectLocation = connection.getHeaderField("location");
				if (!LoaderUtil.isNullOrEmpty(redirectLocation)) {
					URI redirectUri = new URI(redirectLocation);
					Downloader downloader = ImageDownloader.removePendingRequest(key);
					if (downloader != null && !downloader.isCancelled) {
						ImageDownloader.enqueueCacheRead(profileId,downloader.request, new RequestKey(profileId,redirectUri,key.tag), allowCached, saveType,isThumbnail, viewParameters);
					}
				}
				break;

			case HttpURLConnection.HTTP_OK:
				Log.d("DownloadWorkItem Success", "keyId: "+key.uri);
				String filePath = null;
				boolean downloadSuccessFlag = true;
				int fileLength = 0;
				File localResourceFile = null;
				FileOutputStream fileOutputStream = null;
				InputStream inputStream = null;
				String fileSavePath = fileSaveName;
				if (isThumbnail) {
					fileSavePath = FilePathConstant.thumbnail + fileSaveName;
				}
				String downFileSavePath = "down_" + fileSavePath;			
				localResourceFile = new File(tempFolder, downFileSavePath);

				try {
					fileOutputStream = new FileOutputStream(localResourceFile);

					inputStream = connection.getInputStream();
					fileLength = (int) connection.getContentLength();
					
					int byteReadTotal = 0;
					byte[] buffer = new byte[2048];
					int bytesRead = -1;
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						fileOutputStream.write(buffer, 0, bytesRead);
						byteReadTotal += bytesRead;						
						if (isCancel(key)) {
							Log.d("DownloadWorkItem Cancel", "keyId: "+key.uri);
							LoaderUtil.closeQuietly(stream);
							LoaderUtil.disconnectQuietly(connection);
						    ImageDownloader.issueResponse(key, null, null, false);						    
						    return;
						 }					
					}
					fileOutputStream.flush();
					if (fileLength != -1 && Math.abs(fileLength - byteReadTotal) >= 512) {
						downloadSuccessFlag = false;
						localResourceFile.delete();
					}
				} catch (Exception e) {
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

				int sampleSize = 1;
				if (downloadSuccessFlag) {	
					
					//delete the old file
					for (int i = 0; i < refreshCount; i++) {
						String deleteFilePath = null;
						if (i == 0) {
							deleteFilePath = profileId + FilePathConstant.postfix;
						}else {
							String count = "_"+String.valueOf(i);
							deleteFilePath = profileId + count + FilePathConstant.postfix;
						}
						if (isThumbnail) {
							deleteFilePath = FilePathConstant.thumbnail + deleteFilePath;
						}
						File downLoadedFile = new File(tempFolder, deleteFilePath);
						if (downLoadedFile.exists()) {
							downLoadedFile.delete();
						}						
					}

					File downLoadedFile = new File(tempFolder, fileSavePath);
					localResourceFile.renameTo(downLoadedFile);
					filePath = downLoadedFile.getAbsolutePath();
					try {
						BitmapFactory.Options opts = new Options();
						if (!isThumbnail && (viewParameters != null && viewParameters[0] > 1 && viewParameters[1] > 1)) {
							opts.inJustDecodeBounds = true;
							BitmapFactory.decodeFile(filePath, opts);
												
							int sampleSizeH = opts.outHeight / viewParameters[1];							
							int sampleSizeW = opts.outWidth / viewParameters[0];

							if (sampleSizeH > sampleSizeW) {
								sampleSize = sampleSizeH;
							} else {
								sampleSize = sampleSizeW;
							}
							opts.inJustDecodeBounds = false;
							opts.inSampleSize = sampleSize;
							opts.inPreferredConfig = Bitmap.Config.RGB_565;
						}
						bitmap = BitmapFactory.decodeFile(filePath, opts);
					}catch (OutOfMemoryError oom) {
		    	    	Log.e("readFromCache", oom);
		    	    	bitmap = null;
		    	    	System.gc();
					}					
				} else {
					bitmap = null;
				}
				break;

			default:
				Log.d("DownloadWorkItem Error", "keyId: "+key.uri);
				stream = connection.getErrorStream();
				StringBuilder errorMessageBuilder = new StringBuilder();
				if (stream != null) {
					InputStreamReader reader = new InputStreamReader(stream);
					char[] buffer = new char[128];
					int bufferLength;					
					while ((bufferLength = reader.read(buffer, 0, buffer.length)) > 0) {
						errorMessageBuilder.append(buffer, 0, bufferLength);
					}
					LoaderUtil.closeQuietly(reader);
				}			
				error = new Exception(errorMessageBuilder.toString());
				break;
			}
		} catch (IOException e) {
			error = e;
		} catch (URISyntaxException e) {
			error = e;
		} finally {
			LoaderUtil.closeQuietly(stream);
			LoaderUtil.disconnectQuietly(connection);
		}

		if (issueResponse) {
			ImageDownloader.issueResponse(key, error, bitmap, false);
		}
	}

}
