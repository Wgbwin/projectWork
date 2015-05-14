package com.kodak.rss.tablet.bean;

import java.io.Serializable;

import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;

public class ProductLayerLocalInfo implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * If true, it means that we should use image downloaded from server in some case(e.g. Edit photobook layer image when the image is enhanced)
	 */
	public boolean isUseServerImage = false;
	/**
	 * If true, it means that this layer cached image should refresh(re-download) instead of using cached low resolution image.
	 */
	public boolean isNeedRefreshForLowRes = false;
	/**
	 * If true, it means that this layer cached image should refresh(re-download) instead of using cached super high resolution image.
	 */
	public boolean isNeedRefreshForSuperHighRes = false;
	/**
	 * TODO: when crop image, it also use super high, we don't need to redownload it, need to update the code
	 */
	public boolean isNeedRefreshForCropImage = false;
	
	/**
	 * The time you set need refresh flag true.
	 * This value can ensure app always download the latest layer image
	 */
	private long latestTimeForNeedRefresh = 0;
	
	public int colorEffectId=-1;
	public boolean isEnhanced;
	public boolean isRedEyed;
	/**
	 * can be 90,180,270
	 * After Image rotate(not layer rotate), this value need to be updated
	 */
	private int rotateAngle=0;
	
	public ProductLayerLocalInfo(){}
	
	public ProductLayerLocalInfo(boolean useServerImage, boolean needRefresh){
		this.isUseServerImage = useServerImage;
		this.isNeedRefreshForSuperHighRes = needRefresh;
		this.isNeedRefreshForLowRes = needRefresh;
		this.isNeedRefreshForCropImage = needRefresh;
	}
	
	/**
	 * set all needRefresh flag to true
	 */
	public void needRefresh(){
		this.isNeedRefreshForSuperHighRes = true;
		this.isNeedRefreshForLowRes = true;
		this.isNeedRefreshForCropImage = true;
		this.latestTimeForNeedRefresh = System.currentTimeMillis();
	}
	
	public void rotate(){
		rotateAngle += 90;
		if(rotateAngle >= 360){
			rotateAngle -= 360;
		}
	}
	
	public int getRotateAngle(){
		return rotateAngle;
	}
	
	public long getLatestTimeForNeedRefresh() {
		return latestTimeForNeedRefresh;
	}
	
}
