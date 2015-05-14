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
import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.utils.ImageUtil;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images
 * fetched from a URL.
 */
public class ImageFetcher extends ImageResizer
{
	private static final String TAG = "ImageFetcher";
	private static final int HTTP_CACHE_SIZE = 4 * 1024 * 1024; // 4MB
	public static final String HTTP_CACHE_DIR = "http";
	public Context mContext;
	ImageSelectionDatabase mImageSelectionDatabase = null;


	/**
	 * Initialize providing a target image width and height for the processing
	 * images.
	 * 
	 * @param context
	 * @param imageWidth
	 * @param imageHeight
	 */
	public ImageFetcher(Context context, int imageWidth, int imageHeight)
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
	public ImageFetcher(Context context, int imageSize)
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
		}
		catch (FileNotFoundException e)
		{
		}
	}

	@Override
	protected Bitmap processBitmap(Object data)
	{
		ProductInfo tempCartItem = (ProductInfo) data;
		PhotoInfo photoInfo = tempCartItem.photoInfo;
		String uri = (photoInfo.getLocalUri().toString());
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
				filename = photoInfo.getPhotoPath();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		int width = 0;
		int height = 0;
		ROI tempRoi2 = new ROI();
		ROI roi = new ROI();
		Bitmap img = null;
		Bitmap bit = null;
		Bitmap rotated = null;
		try
		{
			/*if (filename.toUpperCase().endsWith(".PNG")){
				img = PrintHelper.getThumbOfPNG(uri);
			}else {*/
				if (photoInfo.getPhotoSource().isFromPhone()){
					img = PrintHelper.loadThumbnailImage(uri, MediaStore.Images.Thumbnails.MINI_KIND, options, mContext);
				}else {
					img = ImageUtil.getBitmapOfPhotoInfo(photoInfo, mContext);
					if (img ==null){
						ImageUtil.downloadUrlToStream(photoInfo, mContext);
						img = ImageUtil.getBitmapOfPhotoInfo(photoInfo, mContext);
					}
				}
				
			/*}*/
			File photo = new File(filename);
			decodeFile(photo);
			roi = tempCartItem.roi;
			
			ExifInterface exif = new ExifInterface(filename);
			
			if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90)
			{
				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				rotated = Bitmap.createBitmap(img, 0, 0, 
						img.getWidth(), img.getHeight(), 
				                              matrix, true);

				
			}
			else if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270)
			{
				Matrix matrix = new Matrix();
				matrix.postRotate(270);
				rotated = Bitmap.createBitmap(img, 0, 0, 
						img.getWidth(), img.getHeight(), 
				                              matrix, true);
			}else if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_180){
				Matrix matrix = new Matrix();
				matrix.postRotate(180);
				rotated = Bitmap.createBitmap(img, 0, 0, 
						img.getWidth(), img.getHeight(), 
				                              matrix, true);
			}
			if(rotated != null)
			{
				img = null;
				img = rotated;
			}
			if (roi != null)
			{
				width = (int) (img.getWidth() * roi.w);
				height = (int) (img.getHeight() * roi.h);
			}
			
			if (width == 0 || height == 0)
			{
				double productWidth = Double.parseDouble(tempCartItem.width);
				double productHeight = Double.parseDouble(tempCartItem.height);
				double ratio = 1.0;
				if (productWidth > productHeight)
				{
					ratio = productWidth / productHeight;
				}
				else
				{
					ratio = productHeight / productWidth;
				}
				roi = PrintHelper.CalculateDefaultRoi(1.0 * img.getWidth(), 1.0 * img.getHeight(), ratio);
				tempRoi2.h = roi.h / img.getHeight();
				tempRoi2.w = roi.w / img.getWidth();
				tempRoi2.y = roi.y / img.getHeight();
				tempRoi2.x = roi.x / img.getWidth();
			}
			else
			{
				tempRoi2.h = roi.h;
				tempRoi2.w = roi.w;
				tempRoi2.y = roi.y;
				tempRoi2.x = roi.x;
			}
			tempCartItem.roi = tempRoi2;
		}
		catch (Exception ex)
		{
		}

		try
		{
			bit = Bitmap.createBitmap(img, (int) ((tempRoi2.x) * img.getWidth()), (int) ((tempRoi2.y) * img.getHeight()), (int) ((tempRoi2.w) * img.getWidth()), (int) ((tempRoi2.h) * img.getHeight()));
		}
		catch (java.lang.Exception e)
		{
			e.printStackTrace();
		}
		
		if(rotated != null)
		{
			img = null;
			img = rotated;
		}
		
		Bitmap scaledBitmap = null;
		int cartimagesize = (int) mContext.getResources().getDimension(R.dimen.image_cart_size);
		if (bit != null)
		{
			double scaleFactor = 1.0;
			if(bit.getHeight() > bit.getWidth())
			{
//				if(bit.getHeight() > cartimagesize)
//				{
					scaleFactor =  bit.getHeight() / cartimagesize;
					Log.d(TAG, "scaleFactor: "+scaleFactor);
					if(scaleFactor <=0)
					{
						scaleFactor = 1;
					}
					
					scaledBitmap = Bitmap.createScaledBitmap(bit, (int)(bit.getWidth() / scaleFactor), (int)(bit.getHeight() / scaleFactor), true);
//				}
			}
			else
			{
//				if(bit.getWidth() > cartimagesize)
//				{
					scaleFactor =  bit.getWidth() / cartimagesize;
					Log.d(TAG, "scaleFactor: "+scaleFactor);
					if(scaleFactor <=0)
					{
						scaleFactor = 1;
					}
					
					int imgWidth = 0;
					int imgHeight = 0;
					imgWidth = (int)(bit.getWidth() / scaleFactor);
					imgHeight = (int)(bit.getHeight() / scaleFactor);
					scaledBitmap = Bitmap.createScaledBitmap(bit, imgWidth, imgHeight, true);
//				}
			}
		}
		
		if(scaledBitmap != null)
			return scaledBitmap;
		else		
			return bit;
		
//		boolean success = false;
//		Bitmap scaledBitmap = null;
//		Bitmap fileBitmap = null;
//		try
//		{
//			int imgWidth = bit.getWidth();
//			int imgHeight = bit.getHeight();
//			int cartimagesize = mContext
//					.getResources().getDimensionPixelSize(R.dimen.image_cart_size);
//			if(imgWidth > imgHeight)
//			{
//				if(imgWidth < cartimagesize)
//				{
//					int diff = cartimagesize - imgWidth;
//					double percentage = 1 + (diff / imgWidth);
//					imgWidth *= percentage;
//					imgHeight *= percentage;
//				}
//				else
//				{
//					int diff = imgWidth - cartimagesize;
//					double percentage = 1 - (diff / imgWidth);
//					imgWidth *= percentage;
//					imgHeight *= percentage;
//				}
//			}
//			else
//			{
//				if(imgHeight < cartimagesize)
//				{
//					int diff = cartimagesize - imgHeight;
//					double percentage = 1 + (diff/imgHeight);
//					imgWidth *= percentage;
//					imgHeight *= percentage;
//				}
//				else
//				{
//					int diff = imgHeight - cartimagesize;
//					double percentage = 1 - (diff/imgHeight);
//					imgWidth *= percentage;
//					imgHeight *= percentage;
//				}
//			}
//			scaledBitmap = Bitmap.createScaledBitmap(bit, imgWidth, imgHeight, true);
//		}
//		catch (Exception ex)
//		{
//			Log.e(TAG, "Failure creating scaled bitmap");
//			ex.printStackTrace();
//		}
//		if (scaledBitmap != null)
//		{
//			return scaledBitmap;
//		}
//		else
//		{
//			try
//			{
//			//	photo = new File(filename);
//				int sampleScale = 1;
//				//decodeFile(photo);
//				if (outWidth > outHeight)
//				{
//					sampleScale = (int) Math.ceil(outWidth / mContext.getResources().getDimensionPixelSize(R.dimen.image_cart_size));
//				}
//				else
//				{
//					sampleScale = (int) Math.ceil(outWidth / mContext.getResources().getDimensionPixelSize(R.dimen.image_cart_size));
//				}
//				options.inSampleSize = sampleScale;
//				fileBitmap = BitmapFactory.decodeFile(filename, options);
//				if (fileBitmap != null)
//				{
//					return fileBitmap;
//				}
//				else
//				{
//					Bitmap draw = overlay(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.imagewait60x60), BitmapFactory.decodeResource(mContext.getResources(), R.drawable.alertred16x16));
//					return draw;
//				}
//			}
//			catch (Exception ex)
//			{
//			}
//		}
//		return scaledBitmap;
	}

	public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2)
	{
		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(bmp1, new Matrix(), null);
		canvas.drawBitmap(bmp2, (canvas.getWidth() / 2) - bmp2.getWidth(), (canvas.getHeight() / 2) - bmp2.getHeight(), null);
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
