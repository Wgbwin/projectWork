package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.tablet.view.RainPicView.RainImage;

public class PopImageLayout extends RelativeLayout{
	private ImageView mIv;
	private Bitmap mBitmap;
	public PopImageLayout(Context context) {
		super(context);	
		init(context);
	}

	public PopImageLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public PopImageLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		mIv = new ImageView(context);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
		addView(mIv, params);
	}

	public void popImageView(RainImage image){	
		if (image == null) return;
		if (image.imagePath == null) return;	
		if (image.imageWidth <= 0 || image.imageHeight <= 0) return;	
		
		
		int size = getWidth() / 7;
		mBitmap = ImageUtil.getImageLocal(image.imagePath, size, size);
		
		mIv.setImageBitmap(mBitmap);
		
		final float x = image.x;
		final float y = image.y;
		setVisibility(View.VISIBLE);	

		float sw = image.imageWidth / getWidth();
		float sh = image.imageHeight / getHeight();
		
		float scale, mx, my;
		if (sw > sh) {
			scale = sh;
			mx = x - (image.imageHeight * getWidth() / getHeight() - image.imageWidth) / 2;
			my = y;
		} else {
			scale = sw;
			mx = x;
			my = y - (image.imageWidth * getHeight() / getWidth() - image.imageHeight) / 2;
		}
		
		AlphaAnimation aa = new AlphaAnimation(0.5f, 1);
		ScaleAnimation sa = new ScaleAnimation(scale, 1, scale, 1, 0, 0);
		TranslateAnimation ta = new TranslateAnimation(Animation.ABSOLUTE, mx, Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, my, Animation.RELATIVE_TO_SELF, 0);
		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(aa);
		animSet.addAnimation(sa);
		animSet.addAnimation(ta);
		animSet.setInterpolator(new AccelerateDecelerateInterpolator());
		animSet.setDuration(500);
		
		mIv.startAnimation(animSet);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (getVisibility() == View.VISIBLE) {
			AlphaAnimation anim = new AlphaAnimation(1, 0);
			anim.setDuration(500);
			startAnimation(anim);
			
			anim.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					setVisibility(View.INVISIBLE);
					mIv.setImageBitmap(null);
					mBitmap.recycle();
					mBitmap = null;
				}
			});
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}
	
}
