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
import com.kodakalaris.photokinavideotest.views.SquareImageView;

public class LargeImagePreviewPagerAdapter extends PagerAdapter {

	private static final String TAG = LargeImagePreviewPagerAdapter.class.getSimpleName();
	private ImageGridAdapter mOtherAdapter;
	private Context mContext;

	public LargeImagePreviewPagerAdapter(ImageGridAdapter otherAdapter, Context context) {
		this.mOtherAdapter = otherAdapter;
		this.mContext = context;
	}

	// -----------------------------------------------------------------------------
	// Used by ViewPager. "Object" represents the page; tell the ViewPager where
	// the
	// page should be displayed, from left-to-right. If the page no longer
	// exists,
	// return POSITION_NONE.

	@Override
	public int getCount() {
		// Log.i(TAG,"GetCounts says "+ mOtherAdapter.getCount());
		return mOtherAdapter.getCount();
		// return views.size();
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		// final String filePath = (String) mOtherAdapter.getItem(position);
		// Log.i(TAG, "instantiateItem called:" + filePath);

		LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View v = layoutInflater.inflate(R.layout.activity_select_photos_large_preview_element, container,false);
		final SquareImageView view = (SquareImageView) v.findViewById(R.id.select_photos_large_preview);
		final View progressView = v.findViewById(R.id.select_photos_large_preview_loading);
		view.setImagePosition(position);
		ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					BaseActivity.removeViewTreeObserverVersionSafe(this, v);
					mOtherAdapter.doSetView(position, view, progressView);
				}
			});
		}
		// v.invalidate();
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