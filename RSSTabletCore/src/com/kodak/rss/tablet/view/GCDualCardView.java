package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.bean.Flippable;

public class GCDualCardView extends RelativeLayout implements Flippable{
	private static final String TAG = "GCDualCardView";
	
	private GCMainPageView mFrontView;
	private GCMainPageView mBackView;
	private boolean mIsSwapped;
	private float mCardWidth, mCardHeight;
	private int mMaxWidth, mMaxHeight;
	private boolean mIsFlipHorizontal;

	public GCDualCardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public GCDualCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public GCDualCardView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		inflate(context, R.layout.gc_dual_card_view, this);
		mFrontView = (GCMainPageView) findViewById(R.id.view_front);
		mBackView = (GCMainPageView) findViewById(R.id.view_back);
		
		mMaxWidth = Integer.MAX_VALUE;
		mMaxHeight = Integer.MAX_VALUE;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int h = MeasureSpec.getSize(heightMeasureSpec);
		int[] size = getSize(h);
		int w = size[0];
		h = size[1];
		
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	/**
	 * @param h
	 * @return {w,h}
	 */
	public int[] getSize(int h) {
		if (h >= mMaxHeight) {
			h = mMaxHeight;
		}
		
		int w = getWidthByHeight(h);
		
		if (w > mMaxWidth) {
			w = mMaxWidth;
			h = getHeightByWidth(w);
		}
		
		return new int[]{w,h};
	}
	
	public static int[] getSize(int h , int maxHeight, int maxWidth, float cardWidth, float cardHeight){
		if (h >= maxHeight) {
			h = maxHeight;
		}
		
		int w = getWidthByHeight(h, cardWidth, cardHeight);
		
		if (w > maxWidth) {
			w = maxWidth;
			h = getHeightByWidth(w, cardWidth, cardHeight);
		}
		
		return new int[]{w,h};
	}
	
	private static int getWidthByHeight(int height, float cardWidth, float cardHeight) {
			return (int) ((float) height * cardWidth / cardHeight);
	}
	
	private static int getHeightByWidth(int width, float cardWidth, float cardHeight) {
			return (int) ((float) width * cardHeight / cardWidth);
	}
	
	private int getWidthByHeight(int height) {
		if (mCardWidth != 0 && mCardHeight != 0) {
			return (int) ((float) height * mCardWidth / mCardHeight);
		} else {
			return getWidth();
		}
	}
	
	private int getHeightByWidth(int width) {
		if (mCardWidth != 0 && mCardHeight != 0) {
			return (int) ((float) width * mCardHeight / mCardWidth);
		} else {
			return getWidth();
		}
	}
	
	/**
	 * In some device getMeasuredWidth is not equal to getWidth, and it will cause some bug
	 * So we use this method to get exact width
	 * @return
	 */
	public int getExactWidth() {
		Log.d(TAG, "measure width:" + getMeasuredWidth() + " , width:"+ getWidth());
		return getMeasuredWidth();
	}
	
	public int getExactHeight() {
		Log.d(TAG, "measure height:" + getMeasuredHeight() + " , height:"+ getHeight());
		return getMeasuredHeight();
	}
	
	public int getExactLeft() {
		return 0;
	}
	
	public int getExactRight() {
		return getExactLeft() + getExactWidth();
	}
	
	public int getExactTop() {
		return 0;
	}
	
	public int getExactBottom() {
		return getExactTop() + getExactHeight();
	}
	
	public int getExactCenterX() {
		return getExactLeft() + getExactWidth() / 2;
	}
	
	public int getExactCenterY() {
		return getExactTop() + getExactBottom() / 2;
	}
	
	@Override
	public synchronized void swapFrontAndBack() {
		mIsSwapped = !mIsSwapped;
		getFrontView().setVisibility(View.VISIBLE);
		getBackView().setVisibility(View.INVISIBLE);
	}
	
	public void setFlipOrientation(boolean isFlipHorizontal) {
		mIsFlipHorizontal = isFlipHorizontal;
	}
	
	public void setCardSize(float w, float h) {
		mCardWidth = w;
		mCardHeight = h;
	}
	
	public void setMaxWidth(int maxWidth) {
		mMaxWidth = maxWidth;
	}
	
	public void setMaxHeight(int maxHeight) {
		mMaxHeight = maxHeight;
	}
	
	public GCMainPageView getFrontView() {
		return mIsSwapped ? mBackView : mFrontView;
	}
	
	public GCMainPageView getBackView() {
		return mIsSwapped ? mFrontView : mBackView;
	}
	
	public GCMainPageView getFirstView() {
		return mFrontView;
	}
	
	public GCMainPageView getSecondView() {
		return mBackView;
	}
	
	public Bitmap getImageBitmapForFront() {
		return mFrontView.getImageBitmap();
	}
	
	public void setImageBitmapForFront(Bitmap bitmap) {
		mFrontView.setImageBitmap(bitmap);
	}
	
	public Bitmap getImageBitmapForBack() {
		return mBackView.getImageBitmap();
	}
	
	public void setImageBitmapForBack(Bitmap bitmap) {
		mBackView.setImageBitmap(bitmap);
	}
	
	public boolean isSwapped() {
		return mIsSwapped;
	}
	
}
