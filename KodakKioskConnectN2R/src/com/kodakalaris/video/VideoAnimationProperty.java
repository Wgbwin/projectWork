package com.kodakalaris.video;

import android.graphics.RectF;
/**
 * This is the property animated by AnimatedVideoView.
 * It is jut a wrapper around a RectF
 */
public class VideoAnimationProperty {
	private static final String TAG = VideoAnimationProperty.class.getSimpleName();
	public RectF mCurRec;
	public VideoAnimationProperty(RectF curRec) {
		mCurRec = curRec;

	}
}
