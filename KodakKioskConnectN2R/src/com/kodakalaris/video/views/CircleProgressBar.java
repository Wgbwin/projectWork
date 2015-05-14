package com.kodakalaris.video.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.kodak.kodak_kioskconnect_n2r.R;

public class CircleProgressBar extends View {
	
	private int curProgress;
	private int maxProgress;
	private CircleAttribute mCircleAttribute;
	private static final int DEFAULT_MAX_VALUE = 100;
	private static final int DEFAULT_PAINT_WIDTH = 10;
	private static final int DEFAULT_PAINT_COLOR = Color.BLUE;
	private static final boolean DEFAULT_FILL_MODE = true;
	private static final int DEFAULT_INSIDE_VALUE = 0;
	private Drawable mBackgroundPicture;

	public CircleProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCircleAttribute = new CircleAttribute();

        maxProgress = DEFAULT_MAX_VALUE;
        
        curProgress = 0;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
        maxProgress = array.getInteger(R.styleable.CircleProgressBar_max, DEFAULT_MAX_VALUE); 
        boolean bFill = array.getBoolean(R.styleable.CircleProgressBar_fill, DEFAULT_FILL_MODE); 
        int paintWidth = array.getInt(R.styleable.CircleProgressBar_Paint_Width, DEFAULT_PAINT_WIDTH); 
        mCircleAttribute.setFill(bFill);
        if (bFill == false)
        {
            mCircleAttribute.setPaintWidth(paintWidth);
        }

        int paintColor = array.getColor(R.styleable.CircleProgressBar_Paint_Color, DEFAULT_PAINT_COLOR); 

        Log.i("", "paintColor = " + Integer.toHexString(paintColor));
        mCircleAttribute.setPaintColor(paintColor);

        mCircleAttribute.mSidePaintInterval = array.getInt(R.styleable.CircleProgressBar_Inside_Interval, DEFAULT_INSIDE_VALUE);

        array.recycle(); 
        
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mBackgroundPicture = getBackground();
        if (mBackgroundPicture != null)
        {
            width = mBackgroundPicture.getMinimumWidth();
            height = mBackgroundPicture.getMinimumHeight();
        }

        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(width, heightMeasureSpec));
	}
	
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		mCircleAttribute.autoFix(w, h);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		 float rate = (float) curProgress / maxProgress;
	     float sweep = 360 * rate;
	     canvas.drawArc(mCircleAttribute.mRoundOval, mCircleAttribute.mDrawPos, sweep, mCircleAttribute.mBRoundPaintsFill, mCircleAttribute.mMainPaints);
	}
	
	
	
	
	class CircleAttribute {
		public RectF mRoundOval;
		public boolean mBRoundPaintsFill;
		public int mSidePaintInterval;
		public int mPaintWidth;
		public int mPaintColor;
		public int mDrawPos;

		public Paint mMainPaints;

		public Paint mBottomPaint;

		public CircleAttribute() {
			mRoundOval = new RectF();
			mBRoundPaintsFill = true;
			mSidePaintInterval = 0;
			mPaintWidth = 0;
			mPaintColor = 0;
			mDrawPos = -90;

			mMainPaints = new Paint();
			mMainPaints.setAntiAlias(true);
			mMainPaints.setStyle(Paint.Style.FILL);
			mMainPaints.setStrokeWidth(mPaintWidth);
			mMainPaints.setColor(mPaintColor);
		}

		public void setPaintWidth(int width) {
			mMainPaints.setStrokeWidth(width);
		}

		public void setPaintColor(int color) {
			mMainPaints.setColor(color);
		}

		public void setFill(boolean fill) {
			mBRoundPaintsFill = fill;
			if (fill) {
				mMainPaints.setStyle(Paint.Style.FILL);
			} else {
				mMainPaints.setStyle(Paint.Style.STROKE);
			}
		}

		public void autoFix(int w, int h) {
			if (mSidePaintInterval != 0) {
				mRoundOval.set(mPaintWidth / 2 + mSidePaintInterval, mPaintWidth / 2 + mSidePaintInterval, w - mPaintWidth / 2 - mSidePaintInterval,
						h - mPaintWidth / 2 - mSidePaintInterval);
			} else {
				int sl = getPaddingLeft();
				int sr = getPaddingRight();
				int st = getPaddingTop();
				int sb = getPaddingBottom();

				mRoundOval.set(sl + mPaintWidth / 2, st + mPaintWidth / 2, w - sr - mPaintWidth / 2, h - sb - mPaintWidth / 2);
			}
		}
	}
	
	public void setProgress(int progress){
        curProgress = progress;
        if (curProgress < 0)
        {
            curProgress = 0;
        }

        if (curProgress > maxProgress)
        {
            curProgress = maxProgress;
        }
        invalidate();
    }

}
