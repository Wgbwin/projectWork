package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class RoundedWebView extends LinearLayout {
	
	private Bitmap windowFrame;
	
	public RoundedWebView(Context context) {
		super(context);
	}

	public RoundedWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void refreshWebView(WebView webView) {
		webView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        addView(webView);
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
		paint.setColor(Color.BLACK); // This is the color of your activity background
		osCanvas.drawRect(outerRectangle, paint);

		paint.setColor(Color.RED); // An obvious color to help debugging
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)); // A out B http://en.wikipedia.org/wiki/File:Alpha_compositing.svg
		osCanvas.drawRoundRect(innerRectangle, cornerRadius, cornerRadius, paint); 
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		windowFrame = null; // If the layout changes null our frame so it can be recreated with the new width and height
	}
	
}
