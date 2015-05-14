package com.kodak.kodak_kioskconnect_n2r;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

/**
 * Usage Example
 * 
 * BitmapUtility.SampleResult sampleResult = new SampleResult(); try { Uri uri =
 * Uri.fromFile(file); img = BitmapUtility.getBitmapFromUri(this, uri, 1,
 * sampleResult, true); Log.d(TAG, "Is bitmap valid? " +
 * sampleResult.getBitmapValidity().toString()); } catch (NullPointerException
 * npe) { npe.printStackTrace(); }
 */
public class BitmapUtility
{
	static private final int resampleConstant = 2;

	public static Bitmap getBitmapFromUri(Context context, Uri uri, int sampleSize, SampleResult sampleResult, Boolean logging)
	{
		Bitmap bm = null;
		String appName = "";
		try
		{
			appName = context.getResources().getString(R.string.app_name);
		}
		catch (NotFoundException nfe)
		{
			nfe.printStackTrace();
		}
		if (uri == null)
		{
			Log.e(appName, "uri == null");
			return null;
		}
		else
		{
			if (logging)
			{
				Log.d(appName, "BitmapUtility.getBitmapFromFilename() sampleSize=" + sampleSize);
			}
			try
			{
				InputStream is = context.getContentResolver().openInputStream(uri);
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inTempStorage = new byte[128 * 128];
				opts.inSampleSize = sampleSize;
				try
				{
					bm = BitmapFactory.decodeStream(is, null, opts);
					if (bm != null)
					{
						sampleResult.setBitmapValidity(true);
					}
				}
				catch (java.lang.OutOfMemoryError oomError)
				{
					System.gc();
					// Try again with a decimated bitmap
					sampleResult.setSampleResult(sampleSize * resampleConstant);
					if (logging)
					{
						Log.d(appName, "BitmapUtility.getBitmapFromUri() Java out of memory error. Resampling with constant=" + sampleResult.getSampleResult());
					}
					return BitmapUtility.getBitmapFromUri(context, uri, sampleSize * resampleConstant, sampleResult, logging);
				}
			}
			catch (FileNotFoundException fnfException)
			{
				fnfException.printStackTrace();
			}
		}
		return bm;
	}

	public static class SampleResult
	{
		private int mSampleResult = 0;
		private boolean mBitmapValid = false;

		void setSampleResult(int sampleResult)
		{
			mSampleResult = sampleResult;
		}

		int getSampleResult()
		{
			return mSampleResult;
		}

		void setBitmapValidity(Boolean bitmapValid)
		{
			mBitmapValid = bitmapValid;
		}

		Boolean getBitmapValidity()
		{
			return mBitmapValid;
		}
	}
}
