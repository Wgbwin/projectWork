package com.kodakalaris.photokinavideotest;

import android.content.Context;
import android.graphics.Bitmap;

public interface BitmapOwner {
	public Context getContext();

	public Bitmap getCachedBitmap(String filePath);
	public int getImageSize();
	public void doOnBitmapReady(Bitmap fullRes);

	public int getImageWidth();

	public int getImageHeight();

	public void doOnBitmapFailure();
}
