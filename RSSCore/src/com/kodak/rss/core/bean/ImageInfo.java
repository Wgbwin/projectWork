package com.kodak.rss.core.bean;

import java.io.Serializable;
import java.util.List;

import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;
import com.kodak.rss.core.n2r.bean.upload.ImageResource;
import com.kodak.rss.core.util.SortableHashMap;

public class ImageInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public String id; 	
	//edit image return path
	public String editUrl; 	
	public boolean isfromNative = true;
	
	/**Facebook Instagram Flickr Photos Dropbox*/
	public String fromSource;
	
	public String bucketId; 
	public String bucketDisplayName; 	
	
	public String originalUrl;
	public String uploadOriginalUrl;
	public String downloadOriginalUrl;
	public int origHeight;
	public int origWidth;
	public boolean isHavedOriginalUpload;	
	public ImageResource imageOriginalResource;	
	public long uploadOriginalSucTime;	
	
	public String thumbnailUrl;
	public String uploadThumbnailUrl;
	public String downloadThumbnailUrl;
	public boolean isHavedThumbnailUpload;	
	public ImageResource imageThumbnailResource;	
	
	/**print*/
	public boolean isCurrentChosen = false;
	public ColorEffect colorEffects;
	public boolean isUseRedEye;
	public boolean isUseEnhance;	
	public SortableHashMap<String,List<ProductInfo>> typeMap;  	
		
	public ImageInfo() {}

	public ImageInfo(String id, String url,String uploadOriginalUrl,String uploadThumbnailUrl) {		
		this.id = id;
		this.uploadOriginalUrl = uploadOriginalUrl;
		this.uploadThumbnailUrl = uploadThumbnailUrl;
		this.originalUrl = url;
		this.thumbnailUrl = url;
	}
	
}
