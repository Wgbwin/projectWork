package com.kodak.kodak_kioskconnect_n2r.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FrameImageView extends ImageView {
	
	private Bitmap bitmap;
	private boolean needDrawFrame = false;
	private int width,height,bmpWidth,bmpHeight;
	private Paint borderPaint;
	private final static float borderWidth = 2, borderMargin = borderWidth/2;
	
	private Paint mTransparentPaint;

	public FrameImageView(Context context) {
		super(context);
		init(context, true);
	}
	
	public FrameImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, false);
	}
	
	public FrameImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, false);
	}
	
	private void init(Context cotnext, boolean isNew){
		if (isNew) {
			setLayerType(LAYER_TYPE_SOFTWARE, null);
		}
		
		mTransparentPaint = new Paint();
		mTransparentPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mTransparentPaint.setStrokeWidth(3f);
		mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));		
		mTransparentPaint.setARGB(128, 255, 0, 0);	
		
		borderPaint = new Paint();
		borderPaint.setColor(0xFFFBBA06);
		borderPaint.setStrokeWidth(borderWidth);
	}
	
	public void setImageBitmap(Bitmap bitmap, boolean needDrawFrame){
		this.bitmap = bitmap;
		if(bitmap!=null && !bitmap.isRecycled()){
			bmpWidth = bitmap.getWidth();
			bmpHeight = bitmap.getHeight();
		}
		this.needDrawFrame = needDrawFrame;
		super.setImageBitmap(bitmap);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(bitmap == null) return;
		super.onDraw(canvas);
		if(!(bmpWidth>0 && bmpHeight>0 && width>0 && height>0)) return;
		float wRatio = bmpWidth*1f/width;
		float hRatio = bmpHeight*1f/height;
		int left = 0, right = width, top = 0, bottom = height;
		if (wRatio > hRatio) {
			float distance = (height -  width*1f*bmpHeight/bmpWidth) / 2;
			top = (int) distance;
			bottom = (int) (height - distance);						
		}else {
			float distance = (width -  height*1f*bmpWidth/bmpHeight) / 2;
			left = (int) distance;
			right = (int) (width - distance);					
		}
		
		if(needDrawFrame){
			drawHightFrame(canvas, left, top, right, bottom);
		}
	}
	
	private void drawHightFrame(Canvas canvas ,int left,int top ,int right,int bottom) {
		canvas.restore();
		canvas.save();			
		canvas.drawLine(left, top + borderMargin, right, top + borderMargin, borderPaint);
		canvas.drawLine(left + borderMargin, top, left + borderMargin, bottom, borderPaint);
		canvas.drawLine(right - borderMargin, bottom, right - borderMargin, top, borderPaint);
		canvas.drawLine(right, bottom - borderMargin, left, bottom - borderMargin, borderPaint);
		canvas.restore();
		canvas.save();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.width = w;
		this.height = h;
	}
	

}
