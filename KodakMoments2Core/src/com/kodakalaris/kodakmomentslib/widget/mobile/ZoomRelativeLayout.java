package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.kodakalaris.kodakmomentslib.R;

public class ZoomRelativeLayout extends RelativeLayout {
	private ZoomImageView mZoomImageView;

	public ZoomRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mZoomImageView = (ZoomImageView) this
				.findViewById(R.id.img_photo_edit_picture);
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_POINTER_DOWN:
			if (event.getPointerCount() == 2) {
				// When there are two finger on the screen, the calculation of
				// the distance between two fingers

				mZoomImageView.centerPointX = mZoomImageView.getWidth() / 2
						+ mZoomImageView.getPaddingLeft();
				mZoomImageView.centerPointY = mZoomImageView.getHeight() / 2
						+ mZoomImageView.getPaddingTop();
				mZoomImageView.lastFingerDis = mZoomImageView
						.distanceBetweenFingers(event);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() == 1) {
				// Only a single refers to press on the screen moves, to drag
				// the state
				float xMove = event.getX();
				float yMove = event.getY();
				mZoomImageView.oneFingerMoveView(xMove, yMove);
			} else if (event.getPointerCount() == 2) {
				// Where two finger moving on the screen is for scaling

				double fingerDis = mZoomImageView.distanceBetweenFingers(event);
				mZoomImageView.twoFingerZoomView(fingerDis);
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (event.getPointerCount() == 2) {
				// Finger leave the screen will be temporarily value reduction
				mZoomImageView.lastXMove = -1;
				mZoomImageView.lastYMove = -1;
			}
			break;
		case MotionEvent.ACTION_UP:
			// Finger leave the screen will be temporarily value restored
			mZoomImageView.lastXMove = -1;
			mZoomImageView.lastYMove = -1;
			break;
		}
		return true;
	}

}
