package com.kodakalaris.video;

import android.content.Context;
import android.graphics.Bitmap;
/**
 * The purpose of this interface is to allow other 
 * types of views that don't extend SquareImageView
 * to use the same asynchronous bitmap loading logic.
 * 
 * Currently the only views that implement it extend
 * SquareImageVIew so its not that useful.
 */
public interface BitmapOwner {
	public Context getContext();

	public Bitmap getCachedBitmap(String filePath);
	public int getImageSize();
	public void doOnBitmapReady(Bitmap fullRes);

	public int getImageWidth();

	public int getImageHeight();

	public void doOnBitmapFailure();
}
