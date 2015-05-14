package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.kodak.rss.tablet.adapter.PhotoBooksProductAdapter;
import com.kodak.rss.tablet.adapter.PhotoBooksProductPagesAdapter;
import com.kodak.rss.tablet.adapter.PhotoBooksProductRearrangeSimplexAdapter;

public class PhotoBookPageView extends View {

	private Context mContext;	
	private Bitmap mBitmap;		
	private int vWidth,vHeight;

	private PhotoBooksProductAdapter adapter;
	public int position;
	private boolean isLeft;
	private boolean isHide;
	
	private RectF contentRect;	
	private RectF leftShadowRect;
	private RectF rightShadowRect;
	private RectF borderRect;
	private Paint borderPaint;
	private final static float borderWidth = 5, borderMargin = borderWidth/2; 

	public PhotoBookPageView(Context context) {
		super(context);
		init(context);
	}

	public PhotoBookPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;	
		isHide = false;
		contentRect = new RectF();	
		leftShadowRect = new RectF();
		rightShadowRect = new RectF();
		borderRect = new RectF();		
		borderPaint = new Paint();
		borderPaint.setColor(0xFFFBBA06);
		borderPaint.setStrokeWidth(borderWidth);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {		
		if (adapter == null ) return;				
		if (adapter instanceof PhotoBooksProductPagesAdapter){				
			if (isLeft && position == 0) return;
			if (!isLeft && position == adapter.itemSize - 1) return;
		}
		if (mBitmap == null) {
			return;			
		}
		
		vWidth = getWidth();
		vHeight = getHeight();
		initBorderRect(vWidth, vHeight);
		
		canvas.drawBitmap(mBitmap, null, contentRect, null);
		
		drawShadow(canvas,isLeft);
				
		if (!isHide && (position == adapter.selectedPostions[0] || position == adapter.selectedPostions[1]) ) {
			drawHightFrame(canvas);
		}

	}
	
	public void setImageBitmap(Bitmap bitmap){					
		mBitmap = bitmap;	
		postInvalidate();
	}
	
	public void setBasicInfo(PhotoBooksProductAdapter mAdapter,int position,boolean isLeft){			
		this.adapter = mAdapter;
		this.position = position;
		this.isLeft = isLeft;			
		
		postInvalidate();
	}
	
	public void setHideHight(boolean isHide,boolean isWantInvalidate){					
		this.isHide = isHide;
		if (isWantInvalidate) {
			postInvalidate();
		}
	}
	
	private void initBorderRect(int w,int h){
		borderRect.left = 0;
		borderRect.top = 0;
		borderRect.right = w;
		borderRect.bottom = h;
		
		contentRect.left = 0;
		contentRect.top = 0;
		contentRect.right = w;
		contentRect.bottom = h;

		leftShadowRect.left = w-w/15f;
		leftShadowRect.top = 0;
		leftShadowRect.right = w;
		leftShadowRect.bottom = h;
		
		rightShadowRect.left = 0;
		rightShadowRect.top = 0;
		rightShadowRect.right = w/15f;
		rightShadowRect.bottom = h;	
	}
	
	private void drawShadow(Canvas canvas,boolean isLeft) {
		canvas.restore();
		canvas.save();
		if (adapter instanceof PhotoBooksProductRearrangeSimplexAdapter) return;
		if (position == 0 &&  position == adapter.itemSize - 1) return;
		if (adapter instanceof PhotoBooksProductPagesAdapter  && position == 1 ) return;
		Bitmap shadow = null;
		if (isLeft) {
			shadow = adapter.leftShadowBitmap;
			if (shadow != null) {
				canvas.drawBitmap(shadow, null, leftShadowRect, null);
			}				
		}else {
			shadow = adapter.rightShadowBitmap;
			if (shadow != null) {
				canvas.drawBitmap(shadow, null, rightShadowRect, null);
			}	
		}						
		canvas.restore();
		canvas.save();
	}

	private void drawHightFrame(Canvas canvas) {
		canvas.restore();
		canvas.save();

		canvas.drawLine(borderRect.left, borderRect.top + borderMargin, borderRect.right, borderRect.top + borderMargin, borderPaint);
		canvas.drawLine(borderRect.left + borderMargin, borderRect.top, borderRect.left + borderMargin, borderRect.bottom, borderPaint);
		canvas.drawLine(borderRect.right - borderMargin, borderRect.bottom, borderRect.right - borderMargin, borderRect.top, borderPaint);
		canvas.drawLine(borderRect.right, borderRect.bottom - borderMargin, borderRect.left, borderRect.bottom - borderMargin, borderPaint);

		canvas.restore();
		canvas.save();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();	
		System.gc();
	}

}
