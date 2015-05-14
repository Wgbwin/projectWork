package com.kodak.rss.tablet.view;

import com.kodak.rss.tablet.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class RoundBorderView extends LinearLayout {
	
	private Bitmap windowFrame;
	private int borderColor;
	private int debugColor;

	public RoundBorderView(Context context) {
		super(context);
	}
	
	public RoundBorderView(Context context, int borderColor, int backgroundColor){
		super(context);
		this.borderColor = borderColor;
		this.debugColor = backgroundColor;
	}
	
	public RoundBorderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initColors(context, attrs);
	}
	
	public RoundBorderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initColors(context, attrs);
	}
	
	private void initColors(Context context, AttributeSet attrs){
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundBorderView);
		borderColor = typedArray.getColor(R.styleable.RoundBorderView_borderColor, Color.GRAY);
		debugColor = typedArray.getColor(R.styleable.RoundBorderView_solidColor, Color.RED);
	}
	
	/**
	 * Add content view
	 * @param view
	 */
	public void refreshContentView(View view){
		view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
		addView(view);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas); // Call super first (this draws the map) we then draw on top of it
		
		if(windowFrame == null){
			createWindowFrame(); // Lazy creation of the window frame, this is needed as we don't know the width & height of the screen until draw time
		}
		
		canvas.drawBitmap(windowFrame, 0, 0, null);
	}
	
	protected void createWindowFrame() {
		windowFrame = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888); // Create a new image we will draw over the map
		Canvas osCanvas = new Canvas(windowFrame); // Create a	 canvas to draw onto the new image
		
		RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());
		RectF innerRectangle = new RectF(0, 0, getWidth(), getHeight());
		
		float cornerRadius = 10; // The angle of your corners
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); // Anti alias allows for smooth corners
		paint.setColor(borderColor); // This is the color of your activity background
		osCanvas.drawRect(outerRectangle, paint);

		paint.setColor(debugColor); // An obvious color to help debugging
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)); // A out B http://en.wikipedia.org/wiki/File:Alpha_compositing.svg
		osCanvas.drawRoundRect(innerRectangle, cornerRadius, cornerRadius, paint); 
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		windowFrame = null; // If the layout changes null our frame so it can be recreated with the new width and height
	}

	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
	}

	public void setDebugColor(int debugColor) {
		this.debugColor = debugColor;
	}

}
