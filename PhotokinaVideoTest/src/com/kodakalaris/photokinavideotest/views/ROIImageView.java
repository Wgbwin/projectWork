package com.kodakalaris.photokinavideotest.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.kodakalaris.photokinavideotest.R;
import com.kodakalaris.photokinavideotest.activities.BaseActivity;

public class ROIImageView extends SquareImageView {

	private static final String TAG = ROIImageView.class.getSimpleName();
	private RectF mRect;
	private float mStartX;
	private float mStartY;
	private float mCurX;
	private float mCurY;
	private float mDiffX;
	private float mDiffY;
	private float mPrevX;
	private float mPrevY;
	private float mStartDistance;
	private float mDistance;
	private float mStartWidth;
	private float mCurWidth;
	private float mStartHeight;
	private float mCurHeight;
	private boolean mIgnoreNextMove;
	private NinePatchDrawable mNinePatch;
	private Rect mDestRec = new Rect();
	private RectF mImageDrawableRec = new RectF();
	private OnGlobalLayoutListener mGlobalListener;
	private RectF mInitialROI;

	public ROIImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mNinePatch = (NinePatchDrawable) getContext().getResources().getDrawable(R.drawable.highlight_change);//box_frame_yellow

		mGlobalListener = new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				BaseActivity.removeViewTreeObserverVersionSafe(this, ROIImageView.this);
				Drawable d = getDrawable();
				Rect b = d.getBounds();
				// Log.i(TAG, "L:" + b.left + " R:" + b.right + " T:" + b.top +
				// " B:" + b.bottom);
				float imageViewWidth = b.width();
				float imageViewHeight = b.height();
				Matrix m = getImageMatrix();
				float[] v = new float[9];
				m.getValues(v);
				float imageX = v[Matrix.MTRANS_X];
				float imageY = v[Matrix.MTRANS_Y];
				float imageScaleX = v[Matrix.MSCALE_X];
				float imageScaleY = v[Matrix.MSCALE_Y];
				// Log.i(TAG, "TransX:" + v[Matrix.MTRANS_X] + " TransY" +
				// v[Matrix.MTRANS_Y] + " ScaleX" + v[Matrix.MSCALE_X] +
				// " ScaleY" + v[Matrix.MSCALE_Y]);
				float initialZoom = 0.10f;
				float imageDrawableWidth = imageViewWidth * imageScaleX;
				float imageDrawableHeight = imageViewHeight * imageScaleY;
				float imageDrawableX = getPaddingLeft() + imageX;
				float imageDrawableY = getPaddingTop() + imageY;
				mImageDrawableRec = new RectF(imageDrawableX, imageDrawableY, imageDrawableX + imageDrawableWidth, imageDrawableY + imageDrawableHeight);
				if (mInitialROI == null) {
					//Log.e(TAG, "Not Using mInitialROI");
					if (mRect == null) {

						// Initializing rect
						// TODO face detection. This can't be done right here because it is too slow.
						
						mCurWidth = mImageDrawableRec.width() - initialZoom * 2.0f * mImageDrawableRec.width();
						mCurHeight = mImageDrawableRec.height() - initialZoom * 2.0f * mImageDrawableRec.height();
						mCurWidth = Math.min(mCurHeight, mCurWidth);
						mCurHeight = mCurWidth;

						float startX = mImageDrawableRec.left + (mImageDrawableRec.width() - mCurWidth) * 0.5f;
						float startY;
						if (mImageDrawableRec.width() > mImageDrawableRec.height()) {
							Log.w(TAG, "Image is landscape");
							startY = mImageDrawableRec.top + (mImageDrawableRec.height() - mCurHeight) * 0.5f;
						} else {
							// startX = mImageDrawableRec.left - mCurWidth *
							// 0.5f +
							// (mImageDrawableRec.width()) * 0.5f;
							startY = mImageDrawableRec.top + (mImageDrawableRec.height() - mCurHeight) * 0.2f;
						}

						// Dont change this line, change the lines above it
						// setting
						// the fields;
						mRect = new RectF(startX, startY, startX + mCurWidth, startY + mCurHeight);
						//Log.e(TAG, "RectF initialized");
					} else {
						checkBounds();
					}
				} else {
					// Log.e(TAG, "Using mInitialROI");
					// mCurWidth = mImageDrawableRec.width() - initialZoom *
					// 2.0f * mImageDrawableRec.width();
					// mCurHeight = mImageDrawableRec.height() - initialZoom *
					// 2.0f * mImageDrawableRec.height();
					// mCurWidth = Math.min(mCurHeight, mCurWidth);
					// mCurHeight = mCurWidth;
					float tempX = mInitialROI.left;
					float tempY = mInitialROI.top;
					float tempW = mInitialROI.width();
					float tempH = mInitialROI.height();
					float numX = tempX * mImageDrawableRec.width();
					float numY = tempY * mImageDrawableRec.height();
					float numW = tempW * mImageDrawableRec.width();
					float numH = tempH * mImageDrawableRec.height();
					mRect = new RectF();
					// Log.e(TAG, "imageX:" + imageX + " imageY" + imageY);
					mRect.left = numX + imageX + getPaddingLeft();
					mRect.top = numY + imageY + getPaddingTop();
					mRect.right = numW + mRect.left;
					mRect.bottom = numH + mRect.top;
					Log.e(TAG, "mRectF mInitialROI:" + mInitialROI.toShortString());
					Log.e(TAG, "mRectF AftermInitialROI:" + mRect.toShortString());
					mInitialROI = null;
					mCurHeight = mRect.height();
					mCurWidth = mRect.width();
					// checkBounds();
				}

			}
		};
		ViewTreeObserver viewTreeObserver = this.getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver.addOnGlobalLayoutListener(mGlobalListener);
		}
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// Log.i(TAG, "H:" + h + " W:" + w + " OH:" + oldh + " OW:" + oldw);
		ViewTreeObserver viewTreeObserver = this.getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver.addOnGlobalLayoutListener(mGlobalListener);
		}
		this.requestLayout();
	}
	public RectF getROI() {
		if (mRect == null) {
			//If for some reason we are null 
			//(we havn't initialized yet)
			//but we are being asked to persist out state
			//Return null so we will initialize again next time.
			return null;
		}
		Matrix m = getImageMatrix();
		float[] v = new float[9];
		m.getValues(v);
		float imageX = v[Matrix.MTRANS_X];
		float imageY = v[Matrix.MTRANS_Y];
		float tempX = ((mRect.left - imageX - getPaddingLeft()) / mImageDrawableRec.width());
		float tempY = ((mRect.top - imageY - getPaddingTop()) / mImageDrawableRec.height());
		float tempW = ((mRect.width()) / mImageDrawableRec.width());
		float tempH = ((mRect.height()) / mImageDrawableRec.height());
		//Log.e(TAG, "mRectF before:" + mRect.toShortString());
		RectF result = new RectF(tempX, tempY, tempX + tempW, tempY + tempH);
		//Log.i(TAG, "ImageCoordsX:" + result.toShortString());
		return result;
	}
	public void setROI(RectF roi) {
		mInitialROI = roi;
		Log.i(TAG, "ROI Set:" + roi);
		ViewTreeObserver viewTreeObserver = this.getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver.addOnGlobalLayoutListener(mGlobalListener);
		}
		this.requestLayout();
	}

	private static float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}
	public void checkBounds() {
		if (mRect.left < mImageDrawableRec.left) {// too far left
			mRect.left = mImageDrawableRec.left;
			mRect.right = mCurWidth + mImageDrawableRec.left;
		}
		if (mRect.right > mImageDrawableRec.right) {
			mRect.right = mImageDrawableRec.right;
			mRect.left = mRect.right - mCurWidth;
		}

		if (mRect.top < mImageDrawableRec.top) {// too far up
			mRect.top = mImageDrawableRec.top;
			mRect.bottom = mCurHeight + mImageDrawableRec.top;
		}
		if (mRect.bottom > mImageDrawableRec.bottom) {
			mRect.bottom = mImageDrawableRec.bottom;
			mRect.top = mRect.bottom - mCurHeight;
		}
		// Log.i(TAG, "ImageCoords mRect.left:" + mRect.left + " mImageDrawableRec.left:" + mImageDrawableRec.left + " mRect.top:" + mRect.top + " mImageDrawableRec.top:" + mImageDrawableRec.top);

		if ((int) mRect.left < (int) mImageDrawableRec.left || (int) mRect.top < (int) mImageDrawableRec.top) {
			Log.e(TAG, "Very odd error, they must have changed the screen size... Reset to center square");
			float minDim = (int) Math.min(mImageDrawableRec.width(), mImageDrawableRec.height());
			mRect.left = mImageDrawableRec.left + (mImageDrawableRec.width() - minDim) / 2;
			mRect.right = mRect.left + minDim;
			mRect.top = mImageDrawableRec.top + (mImageDrawableRec.height() - minDim) / 2;
			mRect.bottom = mRect.top + minDim;
			mCurHeight = minDim;
			mCurWidth = minDim;

		}
		/*
		 * if (mRect.top < mImageDrawableRec.top) { Log.e(TAG,
		 * "Other top error"); mRect.bottom = mImageDrawableRec.top +
		 * Math.min(mImageDrawableRec.width(), mImageDrawableRec.height());;
		 * mRect.top = mImageDrawableRec.left; mRect.right =
		 * mImageDrawableRec.right + Math.min(mImageDrawableRec.width(),
		 * mImageDrawableRec.height());; mRect.left = mImageDrawableRec.left; //
		 * mRect.top = mImageY; // mRect.bottom = mImageY+Math.min(mImageWidth,
		 * mImageHeight);; }
		 */

	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int numPointers = event.getPointerCount();
		if (numPointers == 2) {// zoom
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_POINTER_UP :
					mIgnoreNextMove = true;
					// Log.i(TAG, "Ignoring next move");
				case MotionEvent.ACTION_POINTER_DOWN :
					mStartDistance = spacing(event);
					mStartWidth = mRect.width();
					mStartHeight = mRect.height();
					break;
				case MotionEvent.ACTION_MOVE :
					mDistance = spacing(event);
					mCurWidth = (mDistance / mStartDistance) * mStartWidth;
					mCurHeight = (mDistance / mStartDistance) * mStartHeight;
					if (mCurHeight > Math.min(mImageDrawableRec.width(), mImageDrawableRec.height())) {
						mCurHeight = Math.min(mImageDrawableRec.width(), mImageDrawableRec.height());
					}
					if (mCurWidth > Math.min(mImageDrawableRec.width(), mImageDrawableRec.height())) {
						mCurWidth = Math.min(mImageDrawableRec.width(), mImageDrawableRec.height());
					}
					mRect.left = mRect.left + (mRect.width() - mCurWidth) / 2;
					mRect.top = mRect.top + (mRect.height() - mCurHeight) / 2;
					mRect.right = mRect.right - (mRect.width() - mCurWidth) / 2;
					mRect.bottom = mRect.bottom - (mRect.height() - mCurHeight) / 2;
					checkBounds();
					getROI();
					invalidate();
					break;
			}
		} else if (numPointers == 1) {// pan
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN :
					mStartX = event.getX();
					mStartY = event.getY();
					mPrevX = mStartX;
					mPrevY = mStartY;
					break;
				case MotionEvent.ACTION_MOVE :
					mCurX = event.getX();
					mCurY = event.getY();
					if (!mIgnoreNextMove) {
						mDiffX = mCurX - mPrevX;
						mDiffY = mCurY - mPrevY;
						mRect.top += mDiffY;
						mRect.left += mDiffX;
						mRect.bottom += mDiffY;
						mRect.right += mDiffX;
					} else {
						mIgnoreNextMove = false;
					}
					mPrevX = mCurX;
					mPrevY = mCurY;
					checkBounds();
					getROI();
					invalidate();
					break;
			}
		}
		return true; // processed
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// canvas.drawRect(mRect, mCropPaint);
		mRect.roundOut(mDestRec);
		mNinePatch.setBounds(mDestRec);
		mNinePatch.draw(canvas);

	}
	public RectF getScreenBasedRectF() {
		return mRect;
	}
	public void setScreenBasedRectF(RectF rectF) {
		Log.e(TAG, "RectF set");
		mRect = rectF;
		mCurWidth = rectF.width();
		mCurHeight = rectF.height();
		invalidate();
	}

}