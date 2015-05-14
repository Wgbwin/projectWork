/**
 * PictureDowloadHandler.java
 * com.kodak.utils
 * Created by Sunny on Nov 15, 2013
 * Copyright (c) 2013 Kodak(China) All Rights Reserved
 */
package com.kodak.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.example.android.bitmapfun.util.Utils;

/**
 * this class will help us download picture from the URL and we will store the
 * bitmap in the memory cache but not any disk cache
 * 
 * @author Sunny
 * 
 */
@SuppressLint("NewApi")
public class PictureDowloadHandler {
	private static final String TAG = PictureDowloadHandler.class
			.getSimpleName();
	protected Context mContext;
	private static final int FADE_IN_TIME = 200;
	private boolean mFadeInBitmap = true;
	private LruCache<String, BitmapDrawable> mMemoryCache;
	private boolean mExitTasksEarly = false;
	private Bitmap mLoadingBitmap;
	private final Object mPauseWorkLock = new Object();
	protected boolean mPauseWork = false;
	private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 1024 * 5;
	public PictureDowloadHandler(Context context) {

		mContext = context;

	}

	public void downloadImage(String url, ImageView imageView) {
		if (url == null) {
			return;
		}
		BitmapDrawable value = null;
		if (mMemoryCache != null) {
			value = getBitmapFromMemCache(url);
		}
		if (value != null) {
			imageView.setImageDrawable(value);
		} else if (cancelPotentialWork(url, imageView)) {
			// we will download the picture
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(
					mContext.getResources(), mLoadingBitmap, task);
			imageView.setImageDrawable(asyncDrawable);

			// NOTE: This uses a custom version of AsyncTask that has been
			// pulled from the
			// framework and slightly modified. Refer to the docs at the top of
			// the class
			// for more info on what was changed.
			task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, url);
		}

	}

	/**
	 * Set placeholder bitmap that shows when the the background thread is
	 * running.
	 * 
	 * @param bitmap
	 */
	public void setLoadingImage(Bitmap bitmap) {
		mLoadingBitmap = bitmap;
	}

	/**
	 * Set placeholder bitmap that shows when the the background thread is
	 * running.
	 * 
	 * @param resId
	 */
	public void setLoadingImage(int resId) {
		mLoadingBitmap = BitmapFactory.decodeResource(mContext.getResources(),
				resId);
	}

	public static boolean cancelPotentialWork(Object data, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final Object bitmapData = bitmapWorkerTask.data;
			if (bitmapData == null || !bitmapData.equals(data)) {
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress.
				return false;
			}
		}
		return true;
	}

	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	public BitmapDrawable getBitmapFromMemCache(String data) {
		BitmapDrawable memValue = null;

		if (mMemoryCache != null) {
			memValue = mMemoryCache.get(data);
		}
		return memValue;
	}

	public void addBitmapToCache(String data, BitmapDrawable value) {
		if (data == null || value == null) {
			return;
		}

		// Add to memory cache
		if(mMemoryCache==null){
			mMemoryCache = new  LruCache<String, BitmapDrawable>(DEFAULT_MEM_CACHE_SIZE);
		}
		if (RecyclingBitmapDrawable.class.isInstance(value)) {
				// The removed entry is a recycling drawable, so notify it
				// that it has been added into the memory cache
			((RecyclingBitmapDrawable) value).setIsCached(true);
		}
		mMemoryCache.put(data, value);
		
	}

	public void setExitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly = exitTasksEarly;
		setPauseWork(false);
	}

	/**
	 * The actual AsyncTask that will asynchronously process the image.
	 */
	@SuppressLint("NewApi")
	private class BitmapWorkerTask extends
			AsyncTask<Object, Void, BitmapDrawable> {
		private Object data;
		private final WeakReference<ImageView> imageViewReference;

		public BitmapWorkerTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		/**
		 * Background processing.
		 */
		@Override
		protected BitmapDrawable doInBackground(Object... params) {

			data = params[0];
			final String dataString = String.valueOf(data);
			Bitmap bitmap = null;
			BitmapDrawable drawable = null;
			// Wait here if work is paused and the task is not cancelled
			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			if (bitmap == null && !isCancelled()
					&& getAttachedImageView() != null && !mExitTasksEarly) {
				bitmap = processBitmap(params[0]);
			}
			if (bitmap != null) {
				if (Utils.hasHoneycomb()) {
					// Running on Honeycomb or newer, so wrap in a standard
					// BitmapDrawable
					drawable = new BitmapDrawable(mContext.getResources(),
							bitmap);
				} else {
					// Running on Gingerbread or older, so wrap in a
					// RecyclingBitmapDrawable
					// which will recycle automagically
					drawable = new RecyclingBitmapDrawable(
							mContext.getResources(), bitmap);
				}

				addBitmapToCache(dataString, drawable);

			}

			return drawable;
		}

		private Bitmap processBitmap(Object obj) {
			URL url = null;
			InputStream ins = null;
			Bitmap bitmap = null;
			String urlStr = String.valueOf(obj);
			disableConnectionReuseIfNecessary();
			HttpURLConnection urlConnection = null;

			try {
				url = new URL(urlStr);
				urlConnection = (HttpURLConnection) url.openConnection();
				if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					ins = urlConnection.getInputStream();
					bitmap = BitmapFactory.decodeStream(ins);

				}

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (urlConnection != null) {
					urlConnection.disconnect();
				}

				if (ins != null) {
					try {
						ins.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			return bitmap;

		}

		/**
		 * Once the image is processed, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(BitmapDrawable value) {
			// if cancel was called on this task or the "exit early" flag is set
			// then we're done
			if (isCancelled() || mExitTasksEarly) {
				value = null;
			}

			final ImageView imageView = getAttachedImageView();
			if (value != null && imageView != null) {
				setImageDrawable(imageView, value);
			}
		}

		@Override
		protected void onCancelled(BitmapDrawable value) {
			super.onCancelled(value);
			synchronized (mPauseWorkLock) {
				mPauseWorkLock.notifyAll();
			}
		}

		/**
		 * Returns the ImageView associated with this task as long as the
		 * ImageView's task still points to this task as well. Returns null
		 * otherwise.
		 */
		private ImageView getAttachedImageView() {
			final ImageView imageView = imageViewReference.get();
			final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

			if (this == bitmapWorkerTask) {
				return imageView;
			}

			return null;
		}
	}

	/**
	 * Workaround for bug pre-Froyo, see here for more info:
	 * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	 */
	public static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	/**
	 * Called when the processing is complete and the final drawable should be
	 * set on the ImageView.
	 * 
	 * @param imageView
	 * @param drawable
	 */
	private void setImageDrawable(ImageView imageView, Drawable drawable) {
		if (mFadeInBitmap) {
			// Transition drawable with a transparent drawable and the final
			// drawable
			final TransitionDrawable td = new TransitionDrawable(
					new Drawable[] {
							new ColorDrawable(android.R.color.transparent),
							drawable });
			// Set background to loading bitmap
//			imageView.setBackgroundDrawable(new BitmapDrawable(mContext
//					.getResources(), mLoadingBitmap));
			imageView.setImageBitmap(mLoadingBitmap);

			imageView.setImageDrawable(td);
			td.startTransition(FADE_IN_TIME);
		} else {
			imageView.setImageDrawable(drawable);
		}
	}

	/**
	 * A custom Drawable that will be attached to the imageView while the work
	 * is in progress. Contains a reference to the actual worker task, so that
	 * it can be stopped if a new binding is required, and makes sure that only
	 * the last started worker process can bind its result, independently of the
	 * finish order.
	 */
	private static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap,
				BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(
					bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	/**
	 * Pause any ongoing background work. This can be used as a temporary
	 * measure to improve performance. For example background work could be
	 * paused when a ListView or GridView is being scrolled using a
	 * {@link android.widget.AbsListView.OnScrollListener} to keep scrolling
	 * smooth.
	 * <p>
	 * If work is paused, be sure setPauseWork(false) is called again before
	 * your fragment or activity is destroyed (for example during
	 * {@link android.app.Activity#onPause()}), or there is a risk the
	 * background thread will never finish.
	 */
	public void setPauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
			mPauseWork = pauseWork;
			if (!mPauseWork) {
				mPauseWorkLock.notifyAll();
			}
		}
	}

	public void clearCache() {
		if (mMemoryCache != null) {
			mMemoryCache.evictAll();
		}
	}

}
