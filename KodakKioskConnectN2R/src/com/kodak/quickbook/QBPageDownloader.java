package com.kodak.quickbook;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.QuickBookFlipperActivity;
import com.kodak.quickbook.database.ThumbnailProvider;

public class QBPageDownloader {
	private static QBPageDownloader instance;

	private static final String TAG = "QBPageDownloader";
	private static final int MAX_REQ_ALLOWED = 5;

	private String[] reqUrlCache;
	private String[] reqIdCache;

	private int curReqIndex;
	private int curReqCount = 0;

	private boolean isDownloading = false;
	private QuickBookFlipperActivity curContext;
	private boolean isAllTaskCancelled = false;

	public static QBPageDownloader getInstance(QuickBookFlipperActivity context) {
		if (instance == null) {
			instance = new QBPageDownloader(context);
		} else {
			instance.curContext = context;
		}

		return instance;
	}

	private QBPageDownloader(QuickBookFlipperActivity context) {
		curContext = context;

		reqUrlCache = new String[MAX_REQ_ALLOWED];
		reqIdCache = new String[MAX_REQ_ALLOWED];
		curReqIndex = 0;
		curReqCount = 0;
	}

	public void requestDownload(String url, String id) {
		Log.d(TAG, "** requestDownload [url:" + url + "] id:" + id);
		if (url != null && url.length() > 0 && id != null && id.length() > 0) {
			if (curReqCount == MAX_REQ_ALLOWED) {
				// To replace the oldest request
				if (curReqIndex == 0) {
					curReqIndex = MAX_REQ_ALLOWED - 1;
				} else {
					curReqIndex--;
				}
				curContext.downloadCancelled(reqIdCache[curReqIndex]);

			} else if (curReqCount == 0) {
				// Add req directly
				curReqCount++;
			} else {
				curReqCount++;
				if (curReqIndex == 0) {
					curReqIndex = MAX_REQ_ALLOWED - 1;
				} else {
					curReqIndex--;
				}
			}
			reqUrlCache[curReqIndex] = new String(url);
			reqIdCache[curReqIndex] = new String(id);

			isAllTaskCancelled = false;
			if (!isDownloading) {
				startNewDownloadTask();
			}
		}
	}

	// placeIndex start from 0
	public void appendDownloadAtPlace(int placeIndex, String url, String id) {
		Log.d(TAG, "** appendDownloadAtPlace:" + placeIndex + "[url:" + url + "] id:" + id);
		if (placeIndex < 0)
			return;
		if (placeIndex >= MAX_REQ_ALLOWED) {
			placeIndex = MAX_REQ_ALLOWED - 1;
		}
		if (url != null && url.length() > 0 && id != null && id.length() > 0) {
			if (curReqCount < placeIndex + 1) {
				// Add req directly
				int targetIndex = (curReqIndex + curReqCount) % MAX_REQ_ALLOWED;
				reqUrlCache[targetIndex] = new String(url);
				reqIdCache[targetIndex] = new String(id);
				curReqCount++;
			} else {
				int targetIndex = (curReqIndex + placeIndex) % MAX_REQ_ALLOWED;
				curContext.downloadCancelled(reqIdCache[(targetIndex + MAX_REQ_ALLOWED - placeIndex) % MAX_REQ_ALLOWED]);

				for (int i = targetIndex + (MAX_REQ_ALLOWED - placeIndex); i > targetIndex; i--) {
					reqUrlCache[i % MAX_REQ_ALLOWED] = reqUrlCache[(i - 1) % MAX_REQ_ALLOWED];
					reqIdCache[i % MAX_REQ_ALLOWED] = reqIdCache[(i - 1) % MAX_REQ_ALLOWED];
				}
				reqUrlCache[targetIndex] = new String(url);
				reqIdCache[targetIndex] = new String(id);
				if (curReqCount < MAX_REQ_ALLOWED) {
					curReqCount++;
				}
			}

			isAllTaskCancelled = false;
			if (!isDownloading) {
				startNewDownloadTask();
			}
		}
	}

	private void clearDuplicatedRequest() {
		boolean found = true;
		int chkIndex1 = 0;
		int chkIndex2 = 0;
		try{
		while (found) {
			found = false;
			for (int i = curReqIndex; i < curReqIndex + curReqCount - 1; i++) {
				chkIndex1 = i % MAX_REQ_ALLOWED;
				for (int j = i + 1; j < curReqIndex + curReqCount; j++) {
					chkIndex2 = j % MAX_REQ_ALLOWED;
					if (reqIdCache[chkIndex1].equals(reqIdCache[chkIndex2])) {
						found = true;
						Log.w(TAG, "Found duplicated request before download, remove it. Id=: " + reqIdCache[chkIndex2]);

						break;
					}
				}
				if (found)
					break;
			}
			if (found) {
				// shift one position to remove the duplicated one
				for (int i = chkIndex2; i < curReqIndex + curReqCount - 1; i++) {
					reqIdCache[i % MAX_REQ_ALLOWED] = reqIdCache[(i + 1) % MAX_REQ_ALLOWED];
				}
				curReqCount--;
			}
		}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void cancelAllDownloads() {
		isAllTaskCancelled = true;
		for (int i = curReqIndex; i < curReqCount; i++) {
			curContext.downloadCancelled(reqIdCache[i % MAX_REQ_ALLOWED]);
		}
		curReqIndex = 0;
		curReqCount = 0;
		for (int i = 0; i < MAX_REQ_ALLOWED; i++) {
			reqUrlCache[i] = null;
			reqIdCache[i] = null;
		}
	}

	private void startNewDownloadTask() {
		new Thread(new PageDownloadThread()).start();
	}

	private class PageDownloadThread implements Runnable {
		private String reqUrl;
		private String reqId;

		public void run() {
			isDownloading = true;
			while (curReqCount > 0 && (!isAllTaskCancelled)) {
				reqUrl = reqUrlCache[curReqIndex];
				reqId = reqIdCache[curReqIndex];
				//clearDuplicatedRequest();

				curReqCount--;
				curReqIndex++;
				if (curReqIndex >= MAX_REQ_ALLOWED) {
					curReqIndex = 0;
				}

				// download the pictures now
				byte[] imgRawData = startDownload();

				if (imgRawData != null && (!isAllTaskCancelled)) {
					// check whether there are new same request arrived
					int index = curReqIndex;
					boolean found = false;
					for (int i = 0; i < curReqCount; i++) {
						if (reqId.equals(reqIdCache[index])) {
							found = true;
							break;
						}
						index++;
						if (index >= MAX_REQ_ALLOWED) {
							index = 0;
						}
					}
					if (!found && (!isAllTaskCancelled)) {
						// insert image to download cache, and notify listener to do the UI refresh job
						try {
							cacheImage2DB(imgRawData);
						} catch (Exception e) {
							e.printStackTrace();
						}
						curContext.downloadFinish(reqId);

					}
					Log.d(TAG, "download done for: " + reqUrl);

				} else {
					Log.w(TAG, "Fail to download: " + reqUrl);
				}

			}

			isDownloading = false;
		}

		private void cacheImage2DB(byte[] imgRawData) throws Exception {

			ThumbnailProvider mProvider = ThumbnailProvider.obtainInstance(curContext);
			if (mProvider == null) {
				Log.e(TAG, "DB Provider is null.");
			} else {
				// mProvider.deleteMini(reqId);
				mProvider.cacheMini(reqId, imgRawData);
			}

		}

		private byte[] startDownload() {
			Log.d(TAG, "start downloading " + reqUrl);

			byte[] imgRawData = null;
			InputStream is = null;
			HttpURLConnection conn = null;
			try {
				URL url = new URL(reqUrl);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5 * 1000);
				conn.setReadTimeout(10 * 1000);
				is = conn.getInputStream();// new BufferedInputStream(conn.getInputStream(), Utils.IO_BUFFER_SIZE);

				/* bitmap = BitmapFactory.decodeStream(is); */
				int length = (int) conn.getContentLength();
				if (length > 0) {
					byte[] imgData = new byte[length];
					byte[] buffer = new byte[4098];
					int readLen = 0;
					int destPos = 0;
					while ((readLen = is.read(buffer)) >= 0) {
						if (readLen > 0) {
							System.arraycopy(buffer, 0, imgData, destPos,
									readLen);
							destPos += readLen;
						} else {
							Log.w(TAG,
									"read 0 bytes from input stream while downloading --" + reqId);
						}
						if (isAllTaskCancelled) {
							return null;
						}
					}
					imgRawData = imgData;
				}

			} catch (Exception e) {
				curContext.downloadCancelled(reqId);

				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
						is = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (conn != null) {
					conn.disconnect();
				}
			}
			return imgRawData;
		}

	}

}
