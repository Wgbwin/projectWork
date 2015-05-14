package com.kodakalaris.kodakmomentslib.thread;

import java.util.List;
import java.util.Map;

import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig;
import com.kodakalaris.kodakmomentslib.manager.KMConfigImageDownloader;
import com.kodakalaris.kodakmomentslib.manager.KMConfigManager;
import com.kodakalaris.kodakmomentslib.util.Log;

public class DownloadKMConfigsImagesTask extends Thread {
	private static final String TAG = "DownloadKMConfigsImagesTask";
	
	
	public DownloadKMConfigsImagesTask() {
	}
	
	@Override
	public void run() {
		downloadAllConfigImages();
	}

	private void downloadAllConfigImages() {
		Map<KMConfig.Property, List<KMConfig>> map = KMConfigManager.getInstance().getAllConfigs();
		for (Map.Entry<KMConfig.Property, List<KMConfig>> entry : map.entrySet()) {
			if (entry == null || entry.getValue() == null) continue;
			
			for (KMConfig config : entry.getValue()) {
				if (config != null) {
					Log.i(TAG, "download :" + config.id);
					KMConfigImageDownloader.getInstance().downloadConfigImageSync(config, null);
				}
			}
		}
		
	}
	
}
