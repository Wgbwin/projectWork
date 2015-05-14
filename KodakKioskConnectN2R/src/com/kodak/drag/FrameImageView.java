package com.kodak.drag;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class FrameImageView extends ImageView{

	private boolean drawFrame = false;
	
	public void setDrawFrame(boolean drawFrame) {
		this.drawFrame = drawFrame;
	}

	private boolean drawPositionFront = false;
	private boolean drawPositionBehind = false;
	
	public void setDrawPositionFront(boolean drawPositionFront) {
		this.drawPositionFront = drawPositionFront;
	}
	
	public void setDrawPositionBehind(boolean drawPositionBehind) {
		this.drawPositionBehind = drawPositionBehind;
	}

	public FrameImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FrameImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FrameImageView(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(drawFrame){
			Paint paint = new Paint();
			paint.setColor(Color.YELLOW);
			paint.setStrokeWidth(5);
			canvas.drawLine(0, 0, this.getWidth() - 1, 0, paint);
			canvas.drawLine(0, 0, 0, this.getHeight() -1, paint);
			canvas.drawLine(this.getWidth() -1, 0, this.getWidth() -1, this.getHeight() -1, paint);
			canvas.drawLine(0, this.getHeight() -1, this.getWidth() - 1, this.getHeight() -1 , paint);
		}
		if(drawPositionFront){
			Paint paint = new Paint();
			paint.setColor(Color.YELLOW);
			paint.setStrokeWidth(10);
			canvas.drawLine(0, 0, 0, this.getHeight() -1, paint);
//			canvas.drawLine(this.getWidth() - 1, 0, this.getWidth() -1, this.getHeight() -1, paint);
		}else if(drawPositionBehind){
			Paint paint = new Paint();
			paint.setColor(Color.YELLOW);
			paint.setStrokeWidth(10);
			canvas.drawLine(this.getWidth() - 1, 0, this.getWidth() -1, this.getHeight() -1, paint);
		}
	}

	
}
