package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.kodak.rss.tablet.adapter.CalendarProductAdapter;

public class CalendarPageView extends View {

	private Bitmap mBitmap;		
	private int vWidth,vHeight;

	private CalendarProductAdapter adapter;
	public int position;
	private boolean isUp;	
	private boolean isSimplex;	
	
	private RectF contentRect;		
	private RectF borderRect;
	private Paint borderPaint;
	private final static float borderWidth = 5, borderMargin = borderWidth/2; 

	public CalendarPageView(Context context) {
		super(context,null);		
	}

	public CalendarPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {				
		contentRect = new RectF();			
		borderRect = new RectF();		
		borderPaint = new Paint();
		borderPaint.setColor(0xFFFBBA06);
		borderPaint.setStrokeWidth(borderWidth);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {		
		if (adapter == null) return;								
		
		vWidth = getWidth();
		vHeight = getHeight();
		initBorderRect(vWidth, vHeight);
		
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, null, contentRect, null);
		}
					
		if (position == adapter.selectedPostions) {
			drawHightFrame(canvas);
		}

	}
	
	public void setImageBitmap(Bitmap bitmap){					
		mBitmap = bitmap;	
		postInvalidate();
	}
	
	public void setBasicInfo(CalendarProductAdapter mAdapter,int position,boolean isSimplex,boolean isUp){
		this.adapter = mAdapter;
		this.position = position;
		this.isSimplex = isSimplex;		
		this.isUp = isUp;					
		postInvalidate();
	}

	private void initBorderRect(int w,int h){				
		borderRect.left = 0;
		borderRect.right = w;
		if (isSimplex) {			
			borderRect.top = 0;			
			borderRect.bottom = h;
		}else {
			if (isUp) {				
				borderRect.top = 0;				
				borderRect.bottom = h;
			}else {				
				borderRect.top = h;				
				borderRect.bottom = 2*h;			
			}
		}
		
		contentRect.left = 0;
		contentRect.top = 0;
		contentRect.right = w;
		contentRect.bottom = h;		
	}

	private void drawHightFrame(Canvas canvas) {
		canvas.restore();
		canvas.save();

		canvas.drawLine(borderRect.left + borderMargin, borderRect.top, borderRect.left + borderMargin, borderRect.bottom, borderPaint);
		canvas.drawLine(borderRect.right - borderMargin, borderRect.bottom, borderRect.right - borderMargin, borderRect.top, borderPaint);
		if (isSimplex) {
			canvas.drawLine(borderRect.left, borderRect.top + borderMargin, borderRect.right, borderRect.top + borderMargin, borderPaint);			
			canvas.drawLine(borderRect.right, borderRect.bottom - borderMargin, borderRect.left, borderRect.bottom - borderMargin, borderPaint);			
		}else {
			if (isUp) {
				canvas.drawLine(borderRect.left, borderRect.top + borderMargin, borderRect.right, borderRect.top + borderMargin, borderPaint);
			}else {
				canvas.drawLine(borderRect.right, borderRect.bottom - borderMargin, borderRect.left, borderRect.bottom - borderMargin, borderPaint);
			}
		}

		canvas.restore();
		canvas.save();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();	
		System.gc();
	}

		
}
