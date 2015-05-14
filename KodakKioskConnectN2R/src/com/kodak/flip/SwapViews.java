package com.kodak.flip;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class SwapViews implements Runnable {
	private boolean mIsFirstView;
	ImageView image1;
	ImageView image2;
	float centerX;
	float centerY;

	public SwapViews(boolean isFirstView, ImageView image1, ImageView image2, float x, float y) {
		mIsFirstView = isFirstView;
		this.image1 = image1;
		this.image2 = image2;
		this.centerX = x;
		this.centerY = y;
	}
	@Override
	public void run() {
		Flip3dAnimation rotation;
		if (mIsFirstView) {
			//image1.setVisibility(View.GONE);
			image2.setVisibility(View.VISIBLE);
			image2.requestFocus();
			rotation = new Flip3dAnimation(90, 0, centerX, centerY);
		} else {
			//image2.setVisibility(View.GONE);
			image1.setVisibility(View.VISIBLE);
			image1.requestFocus();
			rotation = new Flip3dAnimation(0, 90, centerX, centerY);
		}
		rotation.setDuration(1000);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new DecelerateInterpolator());
		/*if (mIsFirstView) {
			image2.startAnimation(rotation);
		} else {
			image1.startAnimation(rotation);
		}*/
	}

}
