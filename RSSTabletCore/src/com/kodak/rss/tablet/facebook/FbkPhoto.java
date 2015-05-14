package com.kodak.rss.tablet.facebook;

import java.net.URI;
import java.net.URISyntaxException;

import com.kodak.rss.tablet.AppConstants;

public class FbkPhoto extends FbkObject {	
	public static int DOWNDLOAD_NOT_START = 1;
	public static int DOWNLOAD_ONGOING = 2;
	public static int DOWNLOAD_DONE = 3;
	public boolean isChosen = false;
	public int downloadState = DOWNDLOAD_NOT_START;	
	public String UserName;
	public String UserId;	
	public PhotoSource[] photoSources;
	
	public int origHeight;
	public int origWidth;

	public String getThumbnailLink(){
		String thumbnailSource = null;
		if (photoSources != null && photoSources.length == 2) {
			PhotoSource source = photoSources[0];
			PhotoSource source1 = photoSources[1];
			if (source != null && source1 != null) {
				if (source.width > source1.width || source.height > source1.height) {
					thumbnailSource = source1.source;
				}else {
					thumbnailSource = source.source;
				}		
			}	
		}
		if (thumbnailSource != null && !"".equals(thumbnailSource) && thumbnailSource.startsWith("http")) {
			return thumbnailSource;
		}		
		FBKWrapper wrapper = FBKWrapper.getWrapper();
		return AppConstants.SCOPE + this.ID + "/picture" + "?type="+ "album" +"&&access_token="+ wrapper.facebook.getAccessToken();				
	}
	
	public String getOriginalLink(){
		String originalSource = null;
		if (photoSources != null && photoSources.length == 2) {
			PhotoSource source = photoSources[0];
			PhotoSource source1 = photoSources[1];
			if (source != null && source1 == null) originalSource = source.source;
			if (source == null && source1 != null) originalSource = source1.source;	
			if (source != null && source1 != null) {
				if (source.width > source1.width || source.height > source1.height) {
					origHeight = source.height;
					origWidth = source.width;
					originalSource = source.source;
				}else {
					origHeight = source1.height;
					origWidth = source1.width;
					originalSource = source1.source;
				}		
			}				
		}
		if (originalSource != null && !"".equals(originalSource) && originalSource.startsWith("http")) {
			return originalSource;
		}		
		FBKWrapper wrapper = FBKWrapper.getWrapper();
		// thumbnail,  album ,normal
		return AppConstants.SCOPE + this.ID + "/picture" + "?type="+ "normal" +"&&access_token="+ wrapper.facebook.getAccessToken();
	}
	
	public URI getThumbnailUri(){
		URI pictureURI;			
		try {
			pictureURI = new URI(getThumbnailLink());
		} catch (URISyntaxException e) {
			pictureURI = null;
		}
		return pictureURI;
	}

}