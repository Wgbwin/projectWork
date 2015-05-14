package com.kodak.rss.core.n2r.bean.upload;

import java.util.List;

import com.kodak.rss.core.n2r.bean.project.Resource;

import android.graphics.Bitmap;

public class ImageResource extends Resource {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public transient Bitmap fetchBitmap;	
	
	public List<String> copyIds;
	
	public String fetchPreviewURL(){
		if(baseURI!=null && id!=null){
			return baseURI + id + "/preview";
		} else {
			return "";
		}
	}
}
