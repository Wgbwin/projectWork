package com.kodakalaris.video;

import android.animation.TypeEvaluator;
import android.graphics.RectF;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.kodakalaris.video.views.AnimatedVideoView;
/**
 * This is the property animated by AnimatedVideoView to
 * interpolate between VideoAnimationProperties.
 */
public class VideoAnimationEvaluator implements TypeEvaluator<VideoAnimationProperty> {

	public static final String TAG = VideoAnimationEvaluator.class.getSimpleName();
	private AnimatedVideoView mAnimatedView;
	private AccelerateDecelerateInterpolator mAccDec;
	private AccelerateInterpolator mAcc;
	private DecelerateInterpolator mDec;
	private LinearInterpolator mLin;

	public VideoAnimationEvaluator(AnimatedVideoView animatedVideoView) {
		this.mAnimatedView = animatedVideoView;
		mAccDec = new AccelerateDecelerateInterpolator();
		mAcc = new AccelerateInterpolator();
		mDec = new DecelerateInterpolator();
		mLin = new LinearInterpolator();
	}
	@Override
	public VideoAnimationProperty evaluate(float fraction, VideoAnimationProperty startValue, VideoAnimationProperty endValue) {
		return doEvaluate(fraction, startValue, endValue, true);
	}
	public VideoAnimationProperty doEvaluate(float fraction, VideoAnimationProperty startValue, VideoAnimationProperty endValue, boolean updateMemberVars) {

		float left = interpolate(mLin.getInterpolation(fraction), startValue.mCurRec.left, endValue.mCurRec.left);
		float top = interpolate(mLin.getInterpolation(fraction), startValue.mCurRec.top, endValue.mCurRec.top);
		float right = interpolate(mLin.getInterpolation(fraction), startValue.mCurRec.right, endValue.mCurRec.right);
		float bottom = interpolate(mLin.getInterpolation(fraction), startValue.mCurRec.bottom, endValue.mCurRec.bottom);
		return new VideoAnimationProperty(new RectF(left, top, right, bottom));
	}
	/**
	 * Inperpolates without changing member variables
	 * */
	public VideoAnimationProperty interpVals(float fraction, VideoAnimationProperty startValue, VideoAnimationProperty endValue) {
		return doEvaluate(fraction, startValue, endValue, false);
	}

	// Do a linear interpolation
	public static float interpolate(float fraction, float start, float end) {
		return (float) start + fraction * (end - start);
	}
}
