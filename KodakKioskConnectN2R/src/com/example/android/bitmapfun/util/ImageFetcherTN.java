/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.bitmapfun.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.kodak.kodak_kioskconnect_n2r.BuildConfig;
import com.kodak.kodak_kioskconnect_n2r.ImageSelectionDatabase;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.R;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images
 * fetched from a URL.
 */
public class ImageFetcherTN extends ImageResizerTN
{
	private static final String TAG = "ImageFetcher";
	private static final int HTTP_CACHE_SIZE = 4 * 1024 * 1024; // 4MB
	public static final String HTTP_CACHE_DIR = "http";
	public Context mContext;
	ImageSelectionDatabase mImageSelectionDatabase = null;
	private int outWidth;
	private int outHeight;

	/**
	 * Initialize providing a target image width and height for the processing
	 * images.
	 * 
	 * @param context
	 * @param imageWidth
	 * @param imageHeight
	 */
	public ImageFetcherTN(Context context, int imageWidth, int imageHeight)
	{
		super(context, imageWidth, imageHeight);
		init(context);
		mContext = context;
		mImageSelectionDatabase = new ImageSelectionDatabase(mContext);
	}

	/**
	 * Initialize providing a single target image size (used for both width and
	 * height);
	 * 
	 * @param context
	 * @param imageSize
	 */
	public ImageFetcherTN(Context context, int imageSize)
	{
		super(context, imageSize);
		init(context);
		mContext = context;
		mImageSelectionDatabase = new ImageSelectionDatabase(mContext);
	}

	private void init(Context context)
	{
		checkConnection(context);
	}

	/**
	 * Simple network connection check.
	 * 
	 * @param context
	 */
	private void checkConnection(Context context)
	{
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnectedOrConnecting())
		{
			Toast.makeText(context, "No network connection found.", Toast.LENGTH_LONG).show();
			Log.e(TAG, "checkConnection - no connection found");
		}
	}

	private void decodeFile(File f)
	{
		try
		{
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
			outWidth = o.outWidth;
			outHeight = o.outHeight;
		}
		catch (FileNotFoundException e)
		{
		}
	}

	@Override
	protected Bitmap processBitmap(Object data)
	{
		String uri = (data.toString());
		if (BuildConfig.DEBUG)
		{
			Log.d(TAG, "processBitmap - " + data);
		}
		// Download a bitmap, write it to a file
		/*
		 * final File f = downloadBitmap(mContext, data); if (f != null) { //
		 * Return a sampled down version return
		 * decodeSampledBitmapFromFile(f.toString(), mImageWidth, mImageHeight);
		 * } return null;
		 */
		String filename = "";
		if (PrintHelper.selectedFileNames == null)
		{
			Log.e(TAG, "ImageSelectionActivity: selectedFileNames is null");
		}
		else
		{
			try
			{
				filename = PrintHelper.selectedFileNames.get(uri).toString();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		//options.inJustDecodeBounds = false;
		//options.inSampleSize = 2;
		//Bitmap img = PrintHelper.loadThumbnailImage(uri, MediaStore.Images.Thumbnails.MINI_KIND, options, mContext);
		Bitmap scaledBitmap = null;
		Bitmap fileBitmap = null;
		Bitmap thumbnailImage = null ;
		Bitmap rotated = null ;
		try
		{
			thumbnailImage = PrintHelper.loadThumbnailImage(uri, MediaStore.Images.Thumbnails.MICRO_KIND, options, mContext) ;
			ExifInterface exif = new ExifInterface(filename) ;
			if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90)
			{
				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				rotated = Bitmap.createBitmap(thumbnailImage, 0, 0, 
						thumbnailImage.getWidth(), thumbnailImage.getHeight(), 
				                              matrix, true);

				
			}
			else if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270)
			{
				Matrix matrix = new Matrix();
				matrix.postRotate(270);
				rotated = Bitmap.createBitmap(thumbnailImage, 0, 0, 
						thumbnailImage.getWidth(), thumbnailImage.getHeight(), 
				                              matrix, true);
			}else if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_180){
				Matrix matrix = new Matrix();
				matrix.postRotate(180);
				rotated = Bitmap.createBitmap(thumbnailImage, 0, 0, 
						thumbnailImage.getWidth(), thumbnailImage.getHeight(), 
				                              matrix, true);
			}
			
			if(rotated!=null){
				thumbnailImage = null ;
				thumbnailImage = rotated ;
			}
			
			scaledBitmap = Bitmap.createScaledBitmap(thumbnailImage,
				mContext.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size),mContext.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size),true);
			
			
		}
		catch(OutOfMemoryError oome)
		{
			oome.printStackTrace();
		}
		catch(Exception ex)
		{
			Log.e(TAG, "Failure creating scaled bitmap");
			ex.printStackTrace();
		}finally{
			thumbnailImage = null ;
		}
		
		if(scaledBitmap != null)
		{
			return scaledBitmap;
		}
		else
		{
			try
			{
				File photo = new File(filename);
				int sampleScale = 1;
				decodeFile(photo);
				if(outWidth > outHeight)
				{
					sampleScale = (int) Math.ceil(outWidth / mContext.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size));
				}
				else
				{
					sampleScale = (int) Math.ceil(outWidth / mContext.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size));
				}
				options.inSampleSize = sampleScale;
				fileBitmap = BitmapFactory.decodeFile(filename, options);
				
				if(fileBitmap != null)
				{
					return fileBitmap;
				}
				else
				{				
					Bitmap draw = overlay(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.imagewait96x96),BitmapFactory.decodeResource(mContext.getResources(), R.drawable.alertred16x16));	
					return draw;
				}
			}
			catch(OutOfMemoryError oome)
			{
				oome.printStackTrace();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return scaledBitmap;
	}
	
	public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2)
	{
		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(bmp1, new Matrix(), null);
		canvas.drawBitmap(bmp2, (canvas.getWidth()/2)-bmp2.getWidth(), (canvas.getHeight()/2)-bmp2.getHeight(), null);
		return bmOverlay;
	}
	
	/**
	 * Download a bitmap from a URL, write it to a disk and return the File
	 * pointer. This implementation uses a simple disk cache.
	 * 
	 * @param context
	 *            The context to use
	 * @param urlString
	 *            The URL to fetch
	 * @return A File pointing to the fetched bitmap
	 */
	public static File downloadBitmap(Context context, String urlString)
	{
		final File cacheDir = DiskLruCache.getDiskCacheDir(context, HTTP_CACHE_DIR);
		final DiskLruCache cache = DiskLruCache.openCache(context, cacheDir, HTTP_CACHE_SIZE);
		final File cacheFile = new File(cache.createFilePath(urlString));
		if (cache.containsKey(urlString))
		{
			if (BuildConfig.DEBUG)
			{
				Log.d(TAG, "downloadBitmap - found in http cache - " + urlString);
			}
			return cacheFile;
		}
		if (BuildConfig.DEBUG)
		{
			Log.d(TAG, "downloadBitmap - downloading - " + urlString);
		}
		Utils.disableConnectionReuseIfNecessary();
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		try
		{
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			final InputStream in = new BufferedInputStream(urlConnection.getInputStream(), Utils.IO_BUFFER_SIZE);
			out = new BufferedOutputStream(new FileOutputStream(cacheFile), Utils.IO_BUFFER_SIZE);
			int b;
			while ((b = in.read()) != -1)
			{
				out.write(b);
			}
			return cacheFile;
		}
		catch (final IOException e)
		{
			Log.e(TAG, "Error in downloadBitmap - " + e);
		}
		finally
		{
			if (urlConnection != null)
			{
				urlConnection.disconnect();
			}
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (final IOException e)
				{
					Log.e(TAG, "Error in downloadBitmap - " + e);
				}
			}
		}
		return null;
	}
}
