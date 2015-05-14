package com.kodakalaris.kodakmomentslib.culumus.bean.upload;

import java.util.List;

import android.graphics.Bitmap;

import com.kodakalaris.kodakmomentslib.culumus.bean.project.Resource;

public class ImageResource extends Resource {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public transient Bitmap fetchBitmap;

	public List<String> copyIds;

	public String fetchPreviewURL() {
		if (baseURI != null && id != null) {
			return baseURI + id + "/preview";
		} else {
			return "";
		}
	}

	@Override
	public String toString() {
		return "ImageResource [fetchBitmap=" + fetchBitmap + ", copyIds="
				+ copyIds + "]";
	}

}
