package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class BigImageCheckBoxView extends ImageView implements OnTouchListener
{
	// This specific class for logging
	// private final String TAG = this.getClass().getSimpleName();
	// private Context mContext = null;
	Boolean mChecked = false;
	private Paint mSelectionPaint = null;
	private Paint mCropPaint = null;
	// private final int mBorderWidth = 48;
	boolean mShowCropBox = false;
	protected String originalID;
	protected String uriEncodedPath;
	boolean calculatePosition = true;
	int xOffset = 0;
	int yOffset = 0;
	Rect rect2 = null;
	Boolean mWifiChecked = false;

	public BigImageCheckBoxView(Context context)
	{
		super(context);
		// mContext = context;
		mSelectionPaint = new Paint();
		mSelectionPaint.setStyle(Paint.Style.STROKE);
		mSelectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		mSelectionPaint.setARGB(255, 255, 0, 0);
		mCropPaint = new Paint();
		mCropPaint.setStyle(Paint.Style.STROKE);
		mCropPaint.setStrokeWidth(3f);
		mCropPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		mCropPaint.setARGB(255, 255, 0, 0);
		this.setAdjustViewBounds(true);
	}

	public BigImageCheckBoxView(Context context, AttributeSet set)
	{
		super(context, set);
		// mContext = context;
		mSelectionPaint = new Paint();
		mSelectionPaint.setStyle(Paint.Style.STROKE);
		mSelectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		mSelectionPaint.setARGB(128, 255, 0, 0);
		mCropPaint = new Paint();
		mCropPaint.setStyle(Paint.Style.STROKE);
		mCropPaint.setStrokeWidth(3f);
		mCropPaint.setTextSize(20f);
		mCropPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		mCropPaint.setARGB(255, 255, 0, 0);
		this.setAdjustViewBounds(true);
	}

	public void setChecked(Boolean checked)
	{
		mChecked = checked;
		invalidate();
	}

	public Boolean getChecked()
	{
		return mChecked;
	}

	public void setWifiChecked(Boolean checked)
	{
		mWifiChecked = checked;
		invalidate();
	}

	public Boolean getWifiChecked()
	{
		return mWifiChecked;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
