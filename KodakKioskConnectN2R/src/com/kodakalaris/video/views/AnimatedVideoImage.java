package com.kodakalaris.video.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import com.kodakalaris.video.VideoAnimationProperty;

public class AnimatedVideoImage extends SquareImageView {

	private static final String TAG = AnimatedVideoImage.class.getSimpleName();
	Matrix mConstantMatrix = new Matrix();
	Matrix mTransformMatrix = new Matrix();

	private RectF mConstantBounds;
	private float[] mBuffer = new float[9];
	private RectF mInitialRectF;

	public AnimatedVideoImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setMatrixProperty(VideoAnimationProperty property) {
		// mTransformMatrix.reset();

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

	/**
	 * This must be called after: 1) The view is layed out (use
	 * ViewTreeObserver.addOnGlobalLayoutListener) 2) The view has its bitmap
	 * set.
	 */
	public void initConstantMatrix(RectF initialRectF) {
		mInitialRectF = initialRectF;
		Drawable d = this.getDrawable();
		Rect b = d.getBounds();
		float scaleXto01 = (float) this.getWidth() / (float) b.width();
		float scaleYto01 = (float) this.getHeight() / (float) b.height();
		mConstantMatrix.reset();
		mConstantBounds = new RectF(0, 0, b.width(), b.height());
		mConstantMatrix.postScale(scaleXto01, scaleYto01);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		Log.e(TAG, "Bitmap set causing matrix to get set too!!");
		super.setImageBitmap(bm);
		if (mInitialRectF != null) {
			initConstantMatrix(mInitialRectF);
			setMatrixProperty(new VideoAnimationProperty(mInitialRectF));
		}
		invalidate();
	}

}
