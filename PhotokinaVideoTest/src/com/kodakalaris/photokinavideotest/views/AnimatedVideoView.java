package com.kodakalaris.photokinavideotest.views;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.photokinavideotest.R;
import com.kodakalaris.photokinavideotest.VideoAnimationEvaluator;
import com.kodakalaris.photokinavideotest.VideoAnimationProperty;
import com.kodakalaris.photokinavideotest.activities.BaseActivity;
import com.kodakalaris.photokinavideotest.activities.PreviewActivity;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams.Vignette;

public class AnimatedVideoView extends RelativeLayout {

	private static final String TAG = AnimatedVideoView.class.getSimpleName();
	// private Animation mAnimationSet;
	private ArrayList<AnimatedVideoImage> mVignetteViews;
	private PreviewActivity mPreviewActivity;
	private ArrayList<Animator> mAnimations;
	private AnimatorSet mAnimatorSet;
	private boolean mIsAnimationEnding;
	private ObjectAnimator mFadeOutTextAnimation;
	public static final int FADE_LENGTH_MS = 1000;
	public interface AnimatedVideoListener {
		public void animationEnd(AnimatedVideoView animatedVideoView, int index);
	}
	public AnimatedVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (context instanceof PreviewActivity) {
			mPreviewActivity = (PreviewActivity) context;
		} else {
			Log.e(TAG, "Error:" + TAG + " created in non- " + PreviewActivity.class.getSimpleName());
		}
	}
	public void initAnimation(final VideoGenParams params) {
		ArrayList<Vignette> vigs = new ArrayList<VideoGenParams.Vignette>();
		Vignette titleScreen = new Vignette(-1, "", "", 4000, new RectF(0, 0, 1, 1), new RectF(0, 0, 1, 1));
		vigs.add(titleScreen);
		vigs.addAll(params.mVignettes);
		Vignette endScreen = new Vignette(-2, "", "", 400, new RectF(-1.2f, -10, 1, 1), new RectF(-1.2f, -10, 1, 1));
		vigs.add(endScreen);
		String title = params.mProjectTitle;
		String subTitle = (params.mProjectSubTitleTimeDate + "\n" + params.mProjectSubTitleLocation).trim();
		doinitAnimation(title, subTitle, vigs);

	}
	private void doinitAnimation(final String title, final String subTitle, final ArrayList<Vignette> vigs) {
		/*
		 * int delay = 0; int i = 0; ArrayList<Vignette> vigs =
		 * params.mVignettes; while (i < index) { delay += (vigs.get(i).mLength
		 * + PreviewActivity.FADE_LENGTH_MS); i++; }
		 */
		mVignetteViews = new ArrayList<AnimatedVideoImage>();
		mAnimations = new ArrayList<Animator>();
		for (int i = 0; i < vigs.size(); i++) {
			final int index = i;
			final Vignette vig = vigs.get(i);
			LayoutInflater lay = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View v;
			if (vig.mIndex == -1) {
				v = lay.inflate(R.layout.activity_preview_vignette_section_title, this, false);
			} else {
				v = lay.inflate(R.layout.activity_preview_vignette_section, this, false);
			}
			final AnimatedVideoImage view = (AnimatedVideoImage) v.findViewById(R.id.preview_annimated_video_section);
			if (i != 0) {
				view.setAlpha(0.0f);
			}
			ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
			if (viewTreeObserver.isAlive()) {
				viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						BaseActivity.removeViewTreeObserverVersionSafe(this, view);
						View titleTextViewsHolder = null;
						if (vig.mIndex >= 0) {
							view.setImageBitmapAndFilePath(vig.mImagePath);
						} else {
							// Needed so the image drawable is not null
							// which we need to access its matrix.
							view.setImageResource(android.R.color.transparent);
							if (vig.mIndex == -1) {
								titleTextViewsHolder = v.findViewById(R.id.activity_preview_vignette_section_title_text_views_holder);
								TextView titleText = (TextView) v.findViewById(R.id.activity_preview_vignette_section_title_text);
								TextView subTitleText = (TextView) v.findViewById(R.id.activity_preview_vignette_section_subtitle_text);
								titleText.setText(title);
								subTitleText.setText(subTitle);
								titleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.getWidth() / 14.0f);
								subTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.getWidth() / 17.0f);

							} else if (vig.mIndex == -2) {
								view.setBackgroundResource(android.R.color.black);
								view.setImageResource(R.drawable.kodak_alaris_logo);
							}
						}
						onLayoutComplete(vigs, index, titleTextViewsHolder);
					}
				});
			}
			mVignetteViews.add(view);
			addView(v, i);
		}
	}
	private void onLayoutComplete(ArrayList<Vignette> vigs, int i, View titleTextViewsHolder) {
		final Vignette vig = vigs.get(i);
		int vigLength = vig.mLength;
		AnimatedVideoImage view = mVignetteViews.get(i);
		float alphaStart = 0.0f;
		float alphaEnd = 1.0f;
		Drawable d = view.getDrawable();
		Rect b = d.getBounds();
		// Log.i(TAG, "L:" + b.left + " R:" + b.right + " T:" + b.top + " B:" +
		// b.bottom);

		float scaleXto01 = (float) view.getWidth() / (float) b.width();
		float scaleYto01 = (float) view.getHeight() / (float) b.height();
		view.initConstantMatrix(scaleXto01, scaleYto01, b.width(), b.height());

		long initialStartDelay = 0;
		for (int j = 0; j < i; j++) {
			initialStartDelay += vigs.get(j).mLength;
		}
		if (i != 0) {
			initialStartDelay += FADE_LENGTH_MS * (i - 1);
		}
		ObjectAnimator fadeInAnimation = ObjectAnimator.ofFloat(view, "alpha", (i == 0 ? alphaEnd : alphaStart), alphaEnd);
		fadeInAnimation.setDuration((i == 0 ? 0 : FADE_LENGTH_MS));
		fadeInAnimation.setStartDelay(initialStartDelay);

		int indexOfLastVig = vigs.size() - 1;

		// int fullDuration = (i == indexOfLastVig) ?
		// PreviewActivity.FADE_LENGTH_MS * 1 + vigLength :
		// PreviewActivity.FADE_LENGTH_MS * 2 + vigLength;
		int fullDuration = FADE_LENGTH_MS * 2 + vigLength;

		RectF first;
		RectF second;
		if (i % 2 == 1) {
			first = vig.mStartBounds;
			second = vig.mEndBounds;
		} else {
			first = vig.mEndBounds;
			second = vig.mStartBounds;
		}

		ValueAnimator animation = ObjectAnimator.ofObject(view, "matrixProperty", new VideoAnimationEvaluator(AnimatedVideoView.this),//
				new VideoAnimationProperty(first),//
				new VideoAnimationProperty(second));

		animation.setDuration(fullDuration);
		animation.setStartDelay(initialStartDelay);

		ObjectAnimator fadeOutAnimation = ObjectAnimator.ofFloat(view, "alpha", alphaEnd, (i == indexOfLastVig ? alphaEnd : alphaStart));
		fadeOutAnimation.setDuration((i == indexOfLastVig ? 0 : FADE_LENGTH_MS));

		// ObjectAnimator fadeOutAnimation =
		// ObjectAnimator.ofFloat(mVignetteViews.get(i), "alpha", alphaEnd,
		// alphaStart);
		// fadeOutAnimation.setDuration(PreviewActivity.FADE_LENGTH_MS);
		fadeOutAnimation.setStartDelay(initialStartDelay + FADE_LENGTH_MS * 1 + vigLength);
		fadeInAnimation.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if (!mIsAnimationEnding) {
					Log.e(TAG, "Starting audio:" + vig.mIndex);
					if (vig.mIndex >= 0) {// If this vignette is a real image
						onShouldStartAudio(vig.mIndex);
					}
				}
			}
		});
		mAnimations.add(fadeInAnimation);
		if (titleTextViewsHolder != null) {
			mFadeOutTextAnimation = ObjectAnimator.ofFloat(titleTextViewsHolder, "alpha", alphaEnd, (i == indexOfLastVig ? alphaEnd : alphaStart));
			mFadeOutTextAnimation.setDuration(i == indexOfLastVig ? 0 : FADE_LENGTH_MS);
			mFadeOutTextAnimation.setStartDelay(initialStartDelay + FADE_LENGTH_MS * 1 + vigLength);
			mAnimations.add(mFadeOutTextAnimation);

		}
		mAnimations.add(animation);
		mAnimations.add(fadeOutAnimation);

	}
	public void startVideoAnimation() {
		mIsAnimationEnding = false;
		((View) mFadeOutTextAnimation.getTarget()).setAlpha(1.0f);
		for (int i = 0; i < mVignetteViews.size(); i++) {
			SquareImageView view = mVignetteViews.get(i);
			view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			view.setAlpha(0.0f);
		}
		mAnimatorSet = new AnimatorSet();
		mAnimatorSet.playTogether(mAnimations.toArray(new ObjectAnimator[0]));
		mAnimatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				for (int i = 0; i < mVignetteViews.size(); i++) {
					mVignetteViews.get(i).setLayerType(View.LAYER_TYPE_NONE, null);
				}
				Log.e(TAG, "Full animation ended");
				mPreviewActivity.onVideoAnimationEnded();
			}
		});
		mAnimatorSet.start();
		Log.e(TAG, "Starting animation");

	}

	public void onShouldStartAudio(int index) {
		mPreviewActivity.onShouldStartAudio(index);

	}
	public void stopAnimation() {
		if (mAnimatorSet != null) {
			mIsAnimationEnding = true;
			mAnimatorSet.end();
		}

	}
}
