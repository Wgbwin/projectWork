package com.kodakalaris.video.views;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodakalaris.video.VideoAnimationEvaluator;
import com.kodakalaris.video.VideoAnimationProperty;
import com.kodakalaris.video.activities.BaseActivity;
import com.kodakalaris.video.storydoc_format.VideoGenParams;
import com.kodakalaris.video.storydoc_format.VideoGenParams.Vignette;

/*
 * This class is used to show a preview of a video.
 * This is done by using animations and animating
 * a set of AnimatedVideoImages's ImageMatrix and
 * alpha values.
 */
public class AnimatedVideoView extends RelativeLayout {

	private static final String TAG = AnimatedVideoView.class.getSimpleName();
	// private Animation mAnimationSet;
	private ArrayList<AnimatedVideoImage> mVignetteViews;
	private AnimatedVideoViewHoldingActivity mActivity;
	private ArrayList<Animator> mAnimations;
	private AnimatorSet mAnimatorSet;
	private boolean mIsAnimationEnding;
	private ObjectAnimator mFadeOutTextAnimation;
	private boolean[] mViewsLayedOut;
	public static final int FADE_LENGTH_MS = 1000;
	private static final int MIN_VIGGNETTE_LENGTH = 4000;
	private TextView mTitleText;
	private TextView mSubTitleText;

	public interface AnimatedVideoListener {
		public void animationEnd(AnimatedVideoView animatedVideoView, int index);
	}

	public AnimatedVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/*
	 * This interface must be implemented by
	 * by any activity which holds an
	 * AnimatedVideoView.
	 * 
	 * It allows the playing of audio and notifies
	 * the activity of start and stop events.
	 */
	public interface AnimatedVideoViewHoldingActivity {
		public void onShouldStartAudio(int index);

		public void onReadyToPlay();

		public void onVideoAnimationEnded();
	}

	private static final int TITLE_SCREEN_INDEX = -1;
	private static final int SPLASH_SCREEN_INDEX = -2;

	public void initAnimation(AnimatedVideoViewHoldingActivity activity, final VideoGenParams params) {
		mActivity = activity;
		ArrayList<Vignette> vigs = new ArrayList<VideoGenParams.Vignette>();
		Vignette titleScreen = new Vignette(TITLE_SCREEN_INDEX, "", "" , "", 4000, new RectF(0, 0, 1, 1), new RectF(0, 0, 1, 1));
		vigs.add(titleScreen);
		vigs.addAll(params.mVignettes);
		Vignette endScreen = new Vignette(SPLASH_SCREEN_INDEX, "", "", "", 400, new RectF(0, 0, 1, 1), new RectF(0, 0, 1, 1));
		vigs.add(endScreen);
		doinitAnimation(params.mProjectTitle, params.mProjectSubTitleTimeDate, params.mProjectSubTitleLocation, vigs);

	}

	private void doinitAnimation(final String title, final String date, final String location, final ArrayList<Vignette> vigs) {
		/*
		 * int delay = 0; int i = 0; ArrayList<Vignette> vigs =
		 * params.mVignettes; while (i < index) { delay += (vigs.get(i).mLength
		 * + PreviewActivity.FADE_LENGTH_MS); i++; }
		 */
		mVignetteViews = new ArrayList<AnimatedVideoImage>();
		mAnimations = new ArrayList<Animator>();
		mViewsLayedOut = new boolean[vigs.size()];
		for (int i = 0; i < vigs.size(); i++) {
			final int index = i;
			final Vignette vig = vigs.get(i);
			LayoutInflater lay = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View v;
			if (vig.mIndex == TITLE_SCREEN_INDEX) {
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
							if (vig.mIndex == TITLE_SCREEN_INDEX) {
								titleTextViewsHolder = v.findViewById(R.id.activity_preview_vignette_section_title_text_views_holder);
								mTitleText = (TextView) v.findViewById(R.id.activity_preview_vignette_section_title_text);
								mSubTitleText = (TextView) v.findViewById(R.id.activity_preview_vignette_section_subtitle_text);
								updateTitleText(title, date, location);
								mTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.getHeight() / 20.0f);
								mSubTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.getHeight() / 26.0f);
							} else if (vig.mIndex == SPLASH_SCREEN_INDEX) {
								view.setBackgroundResource(android.R.color.black);
								view.setImageResource(R.drawable.tms_last_frame);
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

	private int getVignetteLength(Vignette vig) {
		int vigLength;
		if (vig.mIndex >= 0) {
			vigLength = Math.max(vig.mLength, MIN_VIGGNETTE_LENGTH);
		} else {
			vigLength = vig.mLength;
		}
		return vigLength;
	}

	private void onLayoutComplete(ArrayList<Vignette> vigs, int i, View titleTextViewsHolder) {
		final Vignette vig = vigs.get(i);
		int vigLength = getVignetteLength(vig);
		AnimatedVideoImage view = mVignetteViews.get(i);
		float alphaStart = 0.0f;
		float alphaEnd = 1.0f;
		view.initConstantMatrix(vig.mStartBounds);
		long initialStartDelay = 0;
		for (int j = 0; j < i; j++) {
			initialStartDelay += getVignetteLength(vigs.get(j));
		}
		if (i != 0) {
			initialStartDelay += FADE_LENGTH_MS * (i - 1);
		}
		// initialStartDelay += FADE_LENGTH_MS;
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
		mViewsLayedOut[i] = true;
		for (int index = 0; index < mViewsLayedOut.length; index++) {
			if (!mViewsLayedOut[index]) {
				return;
			}
		}
		mActivity.onReadyToPlay();
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
				mActivity.onVideoAnimationEnded();
			}
		});
		mAnimatorSet.start();
		Log.e(TAG, "Starting animation");

	}

	public void onShouldStartAudio(int index) {
		mActivity.onShouldStartAudio(index);

	}

	public void stopAnimation() {
		if (mAnimatorSet != null) {
			mIsAnimationEnding = true;
			mAnimatorSet.end();
		}

	}

	public void updateTitleText(String title, String date, String loc) {
		String subTitle = (date + "\n" + loc).trim();
		if (mTitleText != null) {
			mTitleText.setText(title);
		}
		if (mSubTitleText != null) {
			mSubTitleText.setText(subTitle);
		}
	}
}
