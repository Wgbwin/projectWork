package com.kodakalaris.kodakmomentslib.manager;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import android.support.annotation.Nullable;

import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfigEntry;
import com.kodakalaris.kodakmomentslib.util.FileDownloader;
import com.kodakalaris.kodakmomentslib.util.FileUtil;
import com.kodakalaris.kodakmomentslib.util.Log;

public class KMConfigImageDownloader {
	private static final String TAG = "KMConfigImageDownloader";
	
	public static KMConfigImageDownloader sInstance;
	private KMConfigManager mConfigManager;
	/**
	 * key: url
	 * value: true: downloading, false:downloaded
	 */
	private Hashtable<String, Boolean> mDownloadingImages;
	
	private KMConfigImageDownloader() {
		mConfigManager = KMConfigManager.getInstance();
		mDownloadingImages = new Hashtable<String, Boolean>();
	}
	
	public static KMConfigImageDownloader getInstance() {
		if (sInstance == null) {
			sInstance = new KMConfigImageDownloader();
		}
		return sInstance;
	}
	
	public static interface DownloadListener {
		void startDownload(String url);
		void onDownloaded(String url, String localPath, boolean success);
	}
	
	
	public void downloadConfigImageSync(KMConfig config, @Nullable DownloadListener listener) {
		List<KMConfigEntry> list = config.configData.entries;
		for (KMConfigEntry entry : list) {
			downloadSync(entry.imageUrl, listener);
		}
	}
	
	public void download1stImageSync(KMConfig config, @Nullable DownloadListener listener) {
		List<KMConfigEntry> list = config.configData.entries;
		if (list != null && !list.isEmpty()) {
			downloadSync(list.get(0).imageUrl, listener);
		}
	}
	
	private boolean isNeedDownload(String url) {
		String path = mConfigManager.getConfigImageFilePath(url);
		File file = new File(path);
		
		if (file.exists() && file.isFile()) {
			return false;
		}
		
		return true;
	}
	
	private boolean isDownloading(String url) {
		if (mDownloadingImages.containsKey(url)) {
			return mDownloadingImages.get(url);
		}
		
		return false;
	}
	
	public boolean downloadSync(String url, @Nullable DownloadListener listener) {
		if (!isNeedDownload(url) || isDownloading(url)) {
			return true;
		}
		
		String newPath = mConfigManager.getConfigImageFilePath(url);
		String downloadPath = mConfigManager.getConfigImageFilePath(url) + ".tmp";
		FileUtil.deleteFileIfExist(downloadPath);
		
		mDownloadingImages.put(url, true);
		
		if (listener != null) {
			listener.startDownload(url);
		}
		Log.i(TAG, "start download: from " + url + "==temp path:" + downloadPath);
		boolean success = FileDownloader.download(url, downloadPath);
		mDownloadingImages.put(url, false);
		synchronized (KMConfigManager.getInstance()) {
			//add synchronized to avoid app load image when do replace
			success = FileUtil.renameTo(downloadPath, newPath, true);
		}
		
		if (listener != null) {
			listener.onDownloaded(url, newPath, success);
		}
		Log.i(TAG, "download finish success:" + success + ":::::" + newPath);
		
		return success;
	}
}
