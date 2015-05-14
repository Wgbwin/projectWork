package com.kodakalaris.photokinavideotest;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.kodakalaris.photokinavideotest.activities.BaseActivity;
import com.kodakalaris.photokinavideotest.views.SquareImageView;

public class ThumbnailRenderer extends AsyncTask<Pair<String, Integer>, Void, Bitmap> {

	private static final String TAG = ThumbnailRenderer.class.getSimpleName();
	private WeakReference<SquareImageView> mWeakRefImage = null;
	private String mFilePath;
	private WeakReference<View> mWeakRefProgress;

	public ThumbnailRenderer(SquareImageView view, View progressView) {
		mWeakRefImage = new WeakReference<SquareImageView>(view);
		mWeakRefProgress = new WeakReference<View>(progressView);
	}

	@Override
	protected Bitmap doInBackground(Pair<String, Integer>... params) {
		//Warning: This is called on multiple threads at the same time.
		//Each thread uses a different instance of this class.
		mFilePath = params[0].first;
		int id = params[0].second.intValue();

		if (mFilePath != null && mFilePath != "") {
			SquareImageView holder = mWeakRefImage.get();
			if (holder == null) {
				return null;
			}
			Bitmap thumbnail;
			if (holder.getImageSize() == SquareImageView.RESOLUTION_THUMBNAIL) {
				thumbnail = MediaStoreUtils.getGridThumbnail(id, holder, mFilePath);
			} else {
				Log.i(TAG, "Rendering high res... " + holder.getWidth() + ":" + holder.getHeight() + ":" + mFilePath);
				if (holder.getWidth() == 0 || holder.getHeight() == 0) {
					Log.w(TAG, "Rendering highres bitmap 0 by 0. Use a view tree observer and render the image once it size is known");
				}
				thumbnail = MediaStoreUtils.getFullRes(holder.getContext(), mFilePath, holder.getWidth(), holder.getHeight(), holder.getImageSize());
			}
			return thumbnail;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		super.onPostExecute(bitmap);
		// Log.i(TAG, "Asyc completed");
		SquareImageView holder = mWeakRefImage.get();
		View progressView = mWeakRefProgress.get();
		if (progressView != null) {
			// Log.w(TAG,"Setting gone 2");
			progressView.setVisibility(View.GONE);
		}
		if (holder == null || bitmap == null) {
			return;
		}

		// Log.w(TAG, "Asyc 1:" + mFilePath);
		// SLog.w(TAG, "Asyc 2:" + holder.getFilePath());
		if (mFilePath.equals(holder.getFilePath())) {
			holder.setImageBitmap(bitmap);
		} else {
			// Log.w(TAG,
			// "Asyc would have used the wrong image. Not setting bitmap");
		}
		if (holder.getContext() instanceof BaseActivity) {
			BaseActivity activity = (BaseActivity) holder.getContext();
			if (holder.getImageSize() == SquareImageView.RESOLUTION_HIGH) {
				activity.addBitmapToCache(mFilePath, bitmap);
			} else if (holder.getImageSize() == SquareImageView.RESOLUTION_THUMBNAIL) {
				activity.addThumbToCache(mFilePath, bitmap);
			}
		}
	}
}
