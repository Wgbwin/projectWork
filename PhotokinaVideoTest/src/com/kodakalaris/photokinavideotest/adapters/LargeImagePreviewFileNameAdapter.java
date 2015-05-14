package com.kodakalaris.photokinavideotest.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.kodakalaris.photokinavideotest.R;
import com.kodakalaris.photokinavideotest.activities.BaseActivity;
import com.kodakalaris.photokinavideotest.activities.SelectPhotosActivity;
import com.kodakalaris.photokinavideotest.views.SquareImageView;

public class LargeImagePreviewFileNameAdapter extends PagerAdapter {

	private static final String TAG = LargeImagePreviewFileNameAdapter.class.getSimpleName();
	private SelectPhotosActivity mActivity;

	public LargeImagePreviewFileNameAdapter(SelectPhotosActivity activity) {
		this.mActivity = activity;

	}

	// -----------------------------------------------------------------------------
	// Used by ViewPager. "Object" represents the page; tell the ViewPager where
	// the
	// page should be displayed, from left-to-right. If the page no longer
	// exists,
	// return POSITION_NONE.

	@Override
	public int getCount() {
		return 3;// because we always have 3 images... just waiting for it to
					// changes...
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		// final String filePath = (String) mOtherAdapter.getItem(position);
		// Log.i(TAG, "instantiateItem called:" + filePath);

		LayoutInflater layoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View v = layoutInflater.inflate(R.layout.activity_select_photos_large_preview_element, container,false);
		final SquareImageView view = (SquareImageView) v.findViewById(R.id.select_photos_large_preview);
		final View progressView = v.findViewById(R.id.select_photos_large_preview_loading);
		view.setImagePosition(position);
		ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					// This gets called after the layout finishes laying itself
					// out. That way we correctly know the size of the view
					// (getWidth and getHeight have the correct values)
					// when displaying the image. This allows us to scale the
					// bitmap
					// to the correct size of the view saving memory when
					// rendering with RESOLUTION_FULL
					//
					// Log.i(TAG,"On Global called");
					BaseActivity.removeViewTreeObserverVersionSafe(this, v);
					// For some reason, we can get layed out with a height and
					// width of zero.
					// Normally this wouldn't be a problem, but we cache the 1
					// by 1 bitmap
					// So we need to wait until we are layed out with a width
					// and height.
					if (view.getHeight() != 0 && view.getWidth() != 0) {
						ImageGridAdapter.doSetViewWithFilePath(0, view, progressView, 0, mActivity.getSelectedImagePath(position));
					}
				}
			});
		}
		container.addView(v);
		return v;
	}

	// -----------------------------------------------------------------------------
	// Used by ViewPager.
	@Override
	public boolean isViewFromObject(View view, Object object) {
		// Log.i(TAG, "isViewFromObject called");
		return view == object;
	}

	// -----------------------------------------------------------------------------
	// Returns the "view" at "position".
	// The app should call this to retrieve a view; not used by ViewPager.
	// public View getView(int position) {

	// return views.get(position);
	// }

	// -----------------------------------------------------------------------------
	// Used by ViewPager. "Object" represents the page; tell the ViewPager where
	// the
	// page should be displayed, from left-to-right. If the page no longer
	// exists,
	// return POSITION_NONE.
	@Override
	public int getItemPosition(Object object) {
		Log.i(TAG, "getItemPosition called:" + object);
		return 1;
	}

	// -----------------------------------------------------------------------------
	// Used by ViewPager. Called when ViewPager no longer needs a page to
	// display; it
	// is our job to remove the page from the container, which is normally the
	// ViewPager itself. Since all our pages are persistent, we do nothing to
	// the
	// contents of our "views" ArrayList.
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// Log.i(TAG, "destroyItem called");
		View view = (View) object;
		SquareImageView image = (SquareImageView) view.findViewById(R.id.select_photos_large_preview);
		image.destroyDrawingCache();
		image.setImageBitmap(null);
		container.removeView(view);
	}
}
