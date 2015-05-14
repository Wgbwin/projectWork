package com.kodakalaris.photokinavideotest.views;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.kodakalaris.photokinavideotest.VideoAnimationProperty;

public class AnimatedVideoImage extends SquareImageView {

	private static final String TAG = AnimatedVideoImage.class.getSimpleName();
	Matrix mConstantMatrix = new Matrix();
	Matrix mTransformMatrix = new Matrix();

	private RectF mConstantBounds;

	public AnimatedVideoImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public void setMatrixProperty(VideoAnimationProperty property) {
		//mTransformMatrix.reset();
		
		RectF movingBounds = new RectF(//
				property.mCurRec.left * mConstantBounds.width(),//
				property.mCurRec.top * mConstantBounds.height(),//
				property.mCurRec.right * mConstantBounds.width(),//
				property.mCurRec.bottom * mConstantBounds.height());

		mTransformMatrix.setRectToRect(movingBounds, mConstantBounds, ScaleToFit.FILL);
		mTransformMatrix.postConcat(mConstantMatrix);

		// Setting the image matrix directly skips optimizations using hardware
		// layer caching.
		// Setting the view properties directly would improve
		// performance.
		setImageMatrix(mTransformMatrix);
	}
	public void initConstantMatrix(float scaleXto01, float scaleYto01, float imageWidth, float imageHeight) {
		mConstantMatrix.reset();
		mConstantBounds = new RectF(0, 0, imageWidth, imageHeight);
		mConstantMatrix.postScale(scaleXto01, scaleYto01);
	}
}
