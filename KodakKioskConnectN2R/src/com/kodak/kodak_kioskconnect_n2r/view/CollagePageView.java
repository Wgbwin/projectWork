package com.kodak.kodak_kioskconnect_n2r.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;

public class CollagePageView extends MainPageView<CollagePage, Layer> {
	private int vWidth, vHeight;
	private Paint mPaint;
	private Bitmap mBitmap;
	private RectF contentRectF;
	private final static float borderWidth = 5;
	
	public CollagePageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);

	}

	public CollagePageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CollagePageView(Context context) {
		super(context);
		init(context);

	}

	private void init(Context context) {
		contentRectF = new RectF();
		mPaint = new Paint();
		mPaint.setColor(0xFFFBBA06);
		mPaint.setStrokeWidth(borderWidth);

	}

	private void initContentRectF(int w, int h) {
		contentRectF.left = 0;
		contentRectF.top = 0;
		contentRectF.right = w;
		contentRectF.bottom = h;
	};
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		vWidth = getWidth();
		vHeight = getHeight();
		initContentRectF(vWidth, vHeight);
		if (this.mBitmap != null) {
			canvas.drawBitmap(mBitmap, null, contentRectF, null);
		}
	}

	public void setImageBitmap(Bitmap bitmap) {
		this.mBitmap = bitmap;
		invalidate();
	}
	
	@Override
	public Bitmap getImageBitmap() {
		return this.mBitmap;
	}
	
}

