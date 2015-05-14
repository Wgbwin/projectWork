package com.kodak.flip;

import android.view.animation.Animation;
import android.widget.ImageView;

public class DisplayNextView implements Animation.AnimationListener {
	private boolean mCurrentView;
	ImageView image1;
	ImageView image2;
	float centerX;
	float centerY;

	public DisplayNextView(boolean currentView, ImageView image1, ImageView image2, float x, float y) {
		mCurrentView = currentView;
		this.image1 = image1;
		this.image2 = image2;
		this.centerX = x;
		this.centerY = y;
	}
	
	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		//image1.post(new SwapViews(mCurrentView, image1, image2, centerX, centerY));
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	}

}
