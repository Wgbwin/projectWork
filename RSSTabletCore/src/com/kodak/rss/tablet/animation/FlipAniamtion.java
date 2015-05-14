package com.kodak.rss.tablet.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.kodak.rss.tablet.bean.Flippable;
import com.kodak.rss.tablet.view.GCDualCardView;

public class FlipAniamtion extends Animation{
	private float mFromDegrees;
	private float mToDegrees;
	private float mCenterX;
	private float mCenterY;
	private Camera mCamera;
	private Flippable mFlipView;
	private boolean mSwapped;
	private boolean mIsFlipHorizontal;
	private boolean mIsNeedSwapInMid;
	private boolean mChangeCenterXYAfterDegrees = false;
	private float mDegreesForChangeCenterXY;
	private float mNewCenterXForChangeCenterXY;
	private float mNewCenterYForChangeCenterXY;
	
	private int mWidth;
	private int mHeight;
	
	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
		
		if (mFlipView instanceof GCDualCardView) {
			GCDualCardView cardView = (GCDualCardView) mFlipView;
			mWidth = cardView.getExactWidth();
			mHeight = cardView.getExactHeight();
		} else {
			mWidth = width;
			mHeight = height;
		}
	}
	
	public FlipAniamtion(Flippable flipView, float fromDegrees, float toDegrees, float centerX, float centerY, boolean isFlipHorizontal, boolean isNeedSwapInMid) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
		mCenterX = centerX;
		mCenterY = centerY;
		mIsFlipHorizontal = isFlipHorizontal;
		mIsNeedSwapInMid = isNeedSwapInMid;
		mFlipView = flipView;
	}
	
	public void setCenterXYAfterDegrees(float degrees, float centerX, float centerY) {
		mChangeCenterXYAfterDegrees = true;
		mDegreesForChangeCenterXY = degrees;
		mNewCenterXForChangeCenterXY = centerX;
		mNewCenterYForChangeCenterXY = centerY;
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		float degrees = mFromDegrees + (mToDegrees - mFromDegrees) * interpolatedTime;
		Matrix matrix = t.getMatrix();
		
		boolean isAfterMid = isAfterMid(mFromDegrees, mToDegrees, degrees);
		if (mIsNeedSwapInMid && isAfterMid) {
			if (!mSwapped) {
				mFlipView.swapFrontAndBack();
				mSwapped = true;
			}
		}
		mCamera.save();
		
		
		if (mIsFlipHorizontal) {
			mCamera.rotateY(degrees);
		} else {
			mCamera.rotateX(degrees);
		}
		mCamera.getMatrix(matrix);
		mCamera.restore();
		
		if (mChangeCenterXYAfterDegrees 
				&& ( (mFromDegrees > mToDegrees && degrees < mDegreesForChangeCenterXY) || (mFromDegrees < mToDegrees && degrees > mDegreesForChangeCenterXY)  ) ) {
			mCenterX = mNewCenterXForChangeCenterXY;
			mCenterY = mNewCenterYForChangeCenterXY;
		}
		
		matrix.preTranslate(-mCenterX, -mCenterY);
		matrix.postTranslate(mCenterX, mCenterY);
		
		if (mIsNeedSwapInMid && !isAfterMid) {
			if (mIsFlipHorizontal) {
				matrix.preScale(-1, 1, mWidth / 2, mHeight / 2);
			} else {
				matrix.preScale(1, -1, mWidth / 2, mHeight / 2);
			}
		}
	}
	
	private boolean isAfterMid(float fromDegrees, float toDegrees, float currentDegrees) {
		//When card flip cross degrees 90, -90, sometimes it need to swap card
		//This method is to check whether the card is flip cross 90
		//In this case , we ignore flip cross 90 -90 more than twice
		if (fromDegrees >= -270 && fromDegrees < -90 && toDegrees > -90) {
			return currentDegrees > -90;
		}
		
		if (fromDegrees > -90 && fromDegrees <= 90 && toDegrees < -90) {
			return currentDegrees < -90;
		}
		
		if (fromDegrees >= -90 && fromDegrees < 90 && toDegrees > 90) {
			return currentDegrees > 90;
		}
		
		if (fromDegrees > 90 && fromDegrees <= 270 && toDegrees < 90) {
			return currentDegrees < 90;
		}
		
		return false;
	}
	
}
