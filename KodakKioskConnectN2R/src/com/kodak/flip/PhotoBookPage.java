package com.kodak.flip;

import java.util.ArrayList;

import android.graphics.Matrix;

public class PhotoBookPage{
	public String sPhotoBookPageID;
	public String sPhotoBookPageURL;
	public int iMaxNumberOfImages;
	public String sPhotoBookPageName;
	public int iSequenceNumber;
	
	public final static String DUPLEX_FILLER = "DuplexFiller";
	
	/** not used */
	public String sUploadImageID_ContentId;
	
	public boolean bPhotoBookPageEditable;
	public int iNumOfInputImages;
	public ArrayList<PhotoDefinition> PhotoBookPageImages;
	//public EditParam editParam = new EditParam();
	
	public boolean isDownloading = false;
	public String databaseID = 0+"";
	
	public boolean isEdited = false;
	public float mOffsetX = 0f;
	public float mOffsetY = 0f;
	public float mScale = 1.0f;
	public float mRotateDegree = 0;
//	public float mAnoterX;
//	public float mAnoterY;
//	public Matrix mMatrix;
	public float getmOffsetX() {
		return mOffsetX;
	}
	public void setmOffsetX(float mOffsetX) {
		this.mOffsetX = mOffsetX;
	}
	public float getmOffsetY() {
		return mOffsetY;
	}
	public void setmOffsetY(float mOffsetY) {
		this.mOffsetY = mOffsetY;
	}
	public float getmScale() {
		return mScale;
	}
	public void setmScale(float mScale) {
		this.mScale = mScale;
	}
	public float getmRotateDegree() {
		return mRotateDegree;
	}
	public void setmRotateDegree(float mRotateDegree) {
		this.mRotateDegree = mRotateDegree;
	}
	public boolean isEdited() {
		return isEdited;
	}
	public void setEdited(boolean isEdited) {
		this.isEdited = isEdited;
	}
//	public float getmAnoterX() {
//		return mAnoterX;
//	}
//	public void setmAnoterX(float mAnoterX) {
//		this.mAnoterX = mAnoterX;
//	}
//	public float getmAnoterY() {
//		return mAnoterY;
//	}
//	public void setmAnoterY(float mAnoterY) {
//		this.mAnoterY = mAnoterY;
//	}
//	public Matrix getmMatrix() {
//		return mMatrix;
//	}
//	public void setmMatrix(Matrix mMatrix) {
//		this.mMatrix = mMatrix;
//	}
	
}
