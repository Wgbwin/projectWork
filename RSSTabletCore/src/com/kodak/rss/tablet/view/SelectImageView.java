package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.kodak.rss.tablet.R;

public class SelectImageView extends ImageView{

	private Bitmap selectedCheckbox;
	private Bitmap alertredBitmap;	
	private Bitmap bitmap;
	private boolean isChice;	
	private boolean isDirty;
	
	private boolean isNewChice;
	private boolean isDrawTran;
	private Paint mTransparentPaint;
	
	private Paint borderPaint;	
	private final static float borderWidth = 2, borderMargin = borderWidth/2;
	private boolean isUseBorderPaint;
	
	public SelectImageView(Context context) {
		super(context);	
		init(context,true);
	}

	public SelectImageView(Context context, AttributeSet attrs) {
		super(context,attrs);
		init(context,false);
	}
	
	public SelectImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context,false);
	}	

	private void init(Context context,boolean isNew){
		if (isNew) {
			setLayerType(LAYER_TYPE_SOFTWARE, null);
		}		
		selectedCheckbox = BitmapFactory.decodeResource(context.getResources(),R.drawable.checkbox_sel);
		alertredBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.alertred16x16);
		
		mTransparentPaint = new Paint();
		mTransparentPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mTransparentPaint.setStrokeWidth(3f);
		mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));		
		mTransparentPaint.setARGB(128, 255, 0, 0);	
		
		borderPaint = new Paint();
		borderPaint.setColor(0xFFFBBA06);
		borderPaint.setStrokeWidth(borderWidth);
	}
	
	public Bitmap getImageBitmap() {
		return bitmap;
	}
	
	public void setImageBitmap(Bitmap bm) {
		this.bitmap = bm;	
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmapW = bitmap.getWidth();
			bitmapH = bitmap.getHeight();
		}
		super.setImageBitmap(bm);
	}

	public void setIsChice(boolean chice,boolean useBorderPaint) {		
		this.isChice = chice;	
		this.isUseBorderPaint = useBorderPaint;
		super.setImageBitmap(bitmap);
	}
	
	public void setIsUseBorderPaint(boolean useBorderPaint) {				
		this.isUseBorderPaint = useBorderPaint;
		super.setImageBitmap(bitmap);
	}

	public void setImageBitmap(Bitmap bm,boolean chice) {
		this.bitmap = bm;
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmapW = bitmap.getWidth();
			bitmapH = bitmap.getHeight();
		}
		this.isChice = chice;
		this.isUseBorderPaint = false;
		this.isDirty = false;
		super.setImageBitmap(bm);
	}
	
	public void setImageBitmap(Bitmap bm,boolean chice,boolean useBorderPaint) {
		this.bitmap = bm;
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmapW = bitmap.getWidth();
			bitmapH = bitmap.getHeight();
		}
		this.isChice = chice;
		this.isUseBorderPaint = useBorderPaint;
		this.isDirty = false;
		super.setImageBitmap(bm);
	}
	
	public void setImageBitmap(Bitmap bm,boolean chice,boolean isNewChice,boolean isDrawTran) {
		this.bitmap = bm;
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmapW = bitmap.getWidth();
			bitmapH = bitmap.getHeight();
		}
		this.isChice = chice;
		this.isUseBorderPaint = false;
		this.isNewChice = isNewChice;
		this.isDrawTran = isDrawTran;
		this.isDirty = false;
		super.setImageBitmap(bm);
	}
	
	public void setDirty(Bitmap bm) {
		this.bitmap = bm;
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmapW = bitmap.getWidth();
			bitmapH = bitmap.getHeight();
		}
		this.isChice = false;
		this.isUseBorderPaint = false;
		this.isNewChice = false;
		this.isDirty = true;
		super.setImageBitmap(bm);
	}
	
	private int w,h,bitmapW,bitmapH;
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);		
		this.w = w;		
		this.h = h;
	}	

	@Override
	protected void onDraw(Canvas canvas) {
		if (bitmap == null) return;
		super.onDraw(canvas);		
		if (!isDirty &&!isChice) return;
		if (!(bitmapW > 0 && bitmapH > 0)) return; 
		if (!(w > 0 && h > 0)) return;			
		float wRatio = bitmapW*1f/w;
		float hRatio = bitmapH*1f/h;
		int left = 0,right = w,top =0,bottom = h;
		if (wRatio > hRatio) {
			float distance = (h -  w*1f*bitmapH/bitmapW)/2;
			top = (int) distance;
			bottom = (int) (h - distance);						
		}else {
			float distance = (w -  h*1f*bitmapW/bitmapH)/2;
			left = (int) distance;
			right = (int) (w - distance);					
		}
		
		if (isUseBorderPaint) {
			drawHightFrame(canvas, left, top, right, bottom);	
		}else {
			Bitmap drawBitmap = selectedCheckbox;
			if (isDirty) {
				drawBitmap = alertredBitmap;
			}
			if (isNewChice && isDrawTran) {
				canvas.drawBitmap(drawBitmap, left, top, mTransparentPaint);
			}else {
				canvas.drawBitmap(drawBitmap, left, top, null);	
			}		
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
		
}
