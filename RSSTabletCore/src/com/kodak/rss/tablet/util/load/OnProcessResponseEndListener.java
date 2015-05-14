package com.kodak.rss.tablet.util.load;

import com.kodak.rss.core.bean.ImageInfo;

public interface OnProcessResponseEndListener {
	public void onProcessEnd(ImageInfo imageInfo,boolean isEdit);
		
}
