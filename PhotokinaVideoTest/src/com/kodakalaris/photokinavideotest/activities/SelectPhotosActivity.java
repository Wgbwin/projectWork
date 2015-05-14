package com.kodakalaris.photokinavideotest.activities;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.kodakalaris.photokinavideotest.MediaStoreUtils;
import com.kodakalaris.photokinavideotest.R;
import com.kodakalaris.photokinavideotest.SquareImageViewClickListener;
import com.kodakalaris.photokinavideotest.adapters.ImageGridAdapter;
import com.kodakalaris.photokinavideotest.adapters.LargeImagePreviewFileNameAdapter;
import com.kodakalaris.photokinavideotest.adapters.LargeImagePreviewPagerAdapter;
import com.kodakalaris.photokinavideotest.roifacedetect.FaceDetectorTask;
import com.kodakalaris.photokinavideotest.roifacedetect.FinishedFindingFacesEvent;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams.Vignette;
import com.kodakalaris.photokinavideotest.two_way_grid_view.TwoWayAbsListView;
import com.kodakalaris.photokinavideotest.two_way_grid_view.TwoWayGridView;
import com.kodakalaris.photokinavideotest.views.SquareImageView;

public class SelectPhotosActivity extends BaseActivity implements SquareImageViewClickListener {

	TwoWayGridView mBottomGrid;
	View mDoneButton;
	private boolean mIsLargePreviewVisable;
	ViewGroup mRootView;
	ViewPager mLargePreviewPager;
	LargeImagePreviewPagerAdapter mLargePreviewPagerAdapter;
	private LargeImagePreviewFileNameAdapter mLargePreviewFileNameAdapter;
	private View mHoldAndDragLabel;
	ArrayList<SquareImageView> mSelectedImagesViews;
	private AsyncTask<Void, Void, Void> mImageFilePersistor;
	protected boolean mAreImagesDragable = true;
	private FaceDetectorTask mFaceDetectorTask;
	protected static final String TAG = SelectPhotosActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_photos);
		mSelectedImagesViews = new ArrayList<SquareImageView>();
		mSelectedImagesViews.add((SquareImageView) findViewById(R.id.three_across_top_1));
		mSelectedImagesViews.add((SquareImageView) findViewById(R.id.three_across_top_2));
		mSelectedImagesViews.add((SquareImageView) findViewById(R.id.three_across_top_3));
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			// Log.e(TAG,"Index: "+i);
			SquareImageView v = mSelectedImagesViews.get(i);
			v.setImagePosition(i);
		}

		mRootView = (ViewGroup) findViewById(R.id.select_photos_root_container);
		mDoneButton = (View) findViewById(R.id.select_photos_done_button);;
		mHoldAndDragLabel = (View) findViewById(R.id.select_photos_hold_and_drag_label);
		mBottomGrid = (TwoWayGridView) findViewById(R.id.select_photos_bottom_grid);
		if (USE_HORIZONTAL) {
			mBottomGrid.setScrollDirectionPortrait(TwoWayGridView.SCROLL_HORIZONTAL);
		} else {
			mBottomGrid.setScrollDirectionPortrait(TwoWayGridView.SCROLL_VERTICAL);
		}
		ImageGridAdapter adapter = new ImageGridAdapter(this);
		mBottomGrid.setAdapter(adapter);
		mLargePreviewPager = (ViewPager) findViewById(R.id.select_photos_large_preview_view_pager);

		mLargePreviewPager.setPageMargin((int) (-2 * getResources().getDimension(R.dimen.pager_padding)));

		mLargePreviewFileNameAdapter = new LargeImagePreviewFileNameAdapter(this);
		mLargePreviewPagerAdapter = new LargeImagePreviewPagerAdapter(adapter, this);

		// mImageHolderLargePreview = (SquareImageView)
		// findViewById(R.id.select_photos_large_preview);

		// MediaScannerConnection.scanFile(this, new
		// String[]{Environment.getExternalStorageDirectory().getAbsolutePath()},
		// null, null);
		setProperNumberOfRowsOrColsBasedOnOrientationAndGridScrollDirection();
	}
	@Override
	protected void onResume() {
		super.onResume();
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			String imageFilePath = mVideoGenParams.mVignettes.get(i).mImagePath;
			// Log.e(TAG, "Restoreing image:" + imageFilePath);
			if (!imageFilePath.equals("")) {
				mHoldAndDragLabel.setVisibility(View.GONE);
			}
			mSelectedImagesViews.get(i).setImageBitmapAndFilePath(imageFilePath);
		}
		// onImageDrop(null, null, true, false);
		refreshViewPager();
		updateDoneButton();
	}

	void setProperNumberOfRowsOrColsBasedOnOrientationAndGridScrollDirection() {
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (mBottomGrid.getScrollDirectionLandscape() == TwoWayAbsListView.SCROLL_HORIZONTAL) {
				mBottomGrid.setNumRows(getResources().getInteger(R.integer.activity_select_photos_bottom_grid_number_rows));
			} else if (mBottomGrid.getScrollDirectionLandscape() == TwoWayAbsListView.SCROLL_VERTICAL) {
				mBottomGrid.setNumColumns(getResources().getInteger(R.integer.activity_select_photos_bottom_grid_number_columns));
			}
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			if (mBottomGrid.getScrollDirectionPortrait() == TwoWayAbsListView.SCROLL_HORIZONTAL) {
				mBottomGrid.setNumRows(getResources().getInteger(R.integer.activity_select_photos_bottom_grid_number_rows));
			} else if (mBottomGrid.getScrollDirectionPortrait() == TwoWayAbsListView.SCROLL_VERTICAL) {
				mBottomGrid.setNumColumns(getResources().getInteger(R.integer.activity_select_photos_bottom_grid_number_columns));
			}
		}
	}

	public void onDoneButton(View v) {
		Intent intent = new Intent(this, AddAudioActivity.class);
		persistImages(false);
		Bundle options = new Bundle();
		// options.putStringArrayList(AddAudioActivity.PARAM_SELECTED_IMAGES,
		// selectedImagesPath);
		options.putString(INSTANCE_STATE_KEY_VIDEO_PARAMATERS, mVideoGenParams.mUUID.toString());
		intent.putExtras(options);
		startActivity(intent);
	}
	/**
	 * Since we can swap image, this logic is more confusing. We can't just
	 * overwrite the old image 1 with the new image 1 because the new image 2
	 * might have old image 1 as its source.... This also means we can't delete
	 * any images until all of them have been saved to a temporary file. This
	 * logic must work with any number of images and NEVER delete a users image.
	 **/
	public interface FilePersisterHelper {
		public String getCurrentPath(int i);
		public String getOldPath(Vignette vig);
		public void setNewPath(String path, Vignette vig);
		public String getFilePrefix();
		public boolean compressFilesAsBitmaps();
		public boolean areFilesinBitmapCache();

	}
	@Override
	protected void onPause() {
		// for (int i = 0; i < mSelectedImagesViews.size(); i++) {
		// String imageFilePath = mSelectedImagesViews.get(i).getFilePath();
		// Log.e(TAG, "Persisting image:" + imageFilePath);
		// mVideoGenParams.mVignettes.get(i).mImagePath = imageFilePath;
		// }
		if (mFaceDetectorTask != null) {
			try {
				mFaceDetectorTask.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		mFaceDetectorTask = null;
		persistImages(false);
		try {
			mImageFilePersistor.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		mImageFilePersistor = null;

		super.onPause();

	}
	private void persistImages(final boolean wasSwap) {

		if (mImageFilePersistor != null) {
			try {
				mImageFilePersistor.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mImageFilePersistor = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				mAreImagesDragable = false;
				super.onPreExecute();
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				mAreImagesDragable = true;
			}

			@Override
			protected Void doInBackground(Void... params) {
				persistFiles(new FilePersisterHelper() {
					@Override
					public String getCurrentPath(int i) {
						// Log.e(TAG, "Current:" +
						// mSelectedImagesViews.get(i).getFilePath());
						return mSelectedImagesViews.get(i).getFilePath();
					}
					@Override
					public String getOldPath(Vignette vig) {
						// Log.e(TAG, "Old:" + vig.mImagePath);
						return vig.mImagePath;
					}
					@Override
					public void setNewPath(String path, Vignette vig) {
						vig.mImagePath = path;
						mSelectedImagesViews.get(vig.mIndex).setFilePath(path);
					}
					@Override
					public String getFilePrefix() {
						return "image";
					}
					@Override
					public boolean compressFilesAsBitmaps() {
						return !wasSwap;
					}
					@Override
					public boolean areFilesinBitmapCache() {
						return true;
					}
				});
				return null;
			}
		};
		mImageFilePersistor.execute();
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState == null) {
		} else {
			for (int i = 0; i < mSelectedImagesViews.size() && i < mVideoGenParams.mVignettes.size(); i++) {
				VideoGenParams.Vignette vig = mVideoGenParams.mVignettes.get(i);
				mSelectedImagesViews.get(i).setImageBitmapAndFilePath(vig.mImagePath);
			}
			updateDoneButton();
		}

	}
	public String getSelectedImagePath(int i) {
		return mSelectedImagesViews.get(i).getFilePath();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ArrayList<String> selectedImagesPath = new ArrayList<String>();
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			selectedImagesPath.add(mSelectedImagesViews.get(i).getFilePath());
		}
		if (mSelectedImagesViews.size() == 0) {
			Log.e(TAG, "Strange Error2");

		}
		if (selectedImagesPath.size() == 0) {
			Log.e(TAG, "Strange Error3");

		}
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onImageClick(SquareImageView view, String filePath) {
		if (view.getImageType() == SquareImageView.IMAGE_TYPE_GRID) {
			mLargePreviewPager.setAdapter(mLargePreviewPagerAdapter);
			mIsLargePreviewVisable = true;
			mLargePreviewPager.setVisibility(View.VISIBLE);
			changePreviewImage(view.getImagePosition());
		} else if (view.getImageType() == SquareImageView.IMAGE_TYPE_TOP_THREE) {
			if (view.getFilePath() != null && view.getFilePath() != "") {
				if (mIsLargePreviewVisable && mLargePreviewPager.getCurrentItem() == view.getImagePosition()) {
					mIsLargePreviewVisable = false;
					mLargePreviewPager.setVisibility(View.GONE);
				} else {
					mLargePreviewPager.setAdapter(mLargePreviewFileNameAdapter);
					mIsLargePreviewVisable = true;
					mLargePreviewPager.setVisibility(View.VISIBLE);
				}
				changePreviewImage(view.getImagePosition());
			}
		} else if (view.getImageType() == SquareImageView.IMAGE_TYPE_LARGE_RENDER) {
			mIsLargePreviewVisable = false;
			mLargePreviewPager.setVisibility(View.GONE);
		} else {
			Log.e(TAG, "Clicked on an image which didn't have a tag position set");
		}
	}

	@Override
	public void onBackPressed() {
		if (mIsLargePreviewVisable) {
			mIsLargePreviewVisable = false;
			mLargePreviewPager.setVisibility(View.GONE);
			return;
		}
		// persistImages();//on pause does it too
		super.onBackPressed();
	}

	private void changePreviewImage(int i) {
		// Log.w(TAG,"Changing preview Image");
		mLargePreviewPager.setCurrentItem(i, true);
		// mImageHolderLargePreview.setImageBitmapAndFilePath(filePath,
		// SquareImageView.RESOLUTION_FULL);
	}

	@Override
	public ViewGroup getRootView() {
		return mRootView;
	}

	@Override
	public int getShadowWidth() {
		return mSelectedImagesViews.get(1).getWidth();
	}

	@Override
	public int getShadowHeight() {
		return mSelectedImagesViews.get(1).getHeight();
	}

	@Override
	public void onImageDrop(SquareImageView dropSource, SquareImageView dropedOn, boolean isTargetDropable, boolean wasSwap) {
		if (mFaceDetectorTask != null) {
			try {
				mFaceDetectorTask.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		if (mImageFilePersistor != null) {
			try {
				mImageFilePersistor.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		mFaceDetectorTask = null;

		if (isTargetDropable) {
			// dropSource or dropedOn could be null
			mHoldAndDragLabel.setVisibility(View.GONE);
			if (mLargePreviewFileNameAdapter == mLargePreviewPager.getAdapter()) {
				// Log.e(TAG, "Using top three images adapter");
				refreshViewPager();
			}
			if (wasSwap) {
				int dropOnIndex = -1;
				int dropSourceIndex = -1;
				for (int i = 0; i < mSelectedImagesViews.size(); i++) {
					SquareImageView view = mSelectedImagesViews.get(i);
					if (view == dropedOn) {
						dropOnIndex = i;
						// Log.e(TAG, "Match dropOn" + i);
					}
					if (view == dropSource) {
						dropSourceIndex = i;
						// Log.e(TAG, "Match dropSource" + i);
					}
				}
				VideoGenParams.Vignette vigSource = mVideoGenParams.mVignettes.get(dropSourceIndex);
				VideoGenParams.Vignette vigOn = mVideoGenParams.mVignettes.get(dropOnIndex);
				mVideoGenParams.mVignettes.set(dropSourceIndex, vigOn);
				mVideoGenParams.mVignettes.set(dropOnIndex, vigSource);
				vigOn.mIndex = dropSourceIndex;
				vigSource.mIndex = dropOnIndex;
				swapFiles(vigSource, vigOn);
			} else {
				// Log.w(TAG, "Image added");
				for (int i = 0; i < mSelectedImagesViews.size(); i++) {
					SquareImageView view = mSelectedImagesViews.get(i);
					if (dropedOn == view) {
						// Log.w(TAG, "Clearing Bounds:" + i);
						final Vignette v = mVideoGenParams.mVignettes.get(i);
						v.mEndBounds = null;
						v.mStartBounds = null;
						mFaceDetectorTask = new FaceDetectorTask(null);
						mFaceDetectorTask.setFinishEvent(new FinishedFindingFacesEvent() {
							@Override
							public void onFinish(RectF result) {
								v.mEndBounds = result;
								Log.e(TAG, "Setting Face ROI:" + (result == null ? "null" : result.toShortString()));
							}
						});
						Bitmap b = MediaStoreUtils.getFullRes(SelectPhotosActivity.this, dropedOn.getFilePath(), 1280, 1280, SquareImageView.RESOLUTION_HIGH);
						mFaceDetectorTask.execute(b);
						/*
						 * if (mLocationGetterTask != null) { try {
						 * mLocationGetterTask.get(); } catch
						 * (InterruptedException e) { e.printStackTrace(); }
						 * catch (ExecutionException e) { e.printStackTrace(); }
						 * } mFaceDetectorTask = new FaceDetectorTask(null);
						 * mFaceDetectorTask.setFinishEvent(new
						 * FinishedFindingFacesEvent() {
						 * 
						 * @Override public void onFinish(RectF result) {
						 * v.mEndBounds = result; Log.e(TAG, "Setting Face ROI:"
						 * + (result == null ? "null" :
						 * result.toShortString())); } }); Bitmap b =
						 * MediaStoreUtils.getFullRes(SelectPhotosActivity.this,
						 * dropedOn.getFilePath(), 1280, 1280,
						 * SquareImageView.RESOLUTION_HIGH);
						 * mFaceDetectorTask.execute(b);
						 */
					}
				}
				persistImages(wasSwap);
			}
		} else {
			// Log.e(TAG, "Image: " + dropSource.getFilePath());
			// Log.w(TAG, "Image removed");
			for (int i = 0; i < mSelectedImagesViews.size(); i++) {
				SquareImageView view = mSelectedImagesViews.get(i);
				if (dropSource == view) {
					Log.e(TAG, "Equal to to three view");
					view.setImageBitmapAndFilePath("");
					refreshViewPager();
					// } else if (dropedOn == view) {
					// Log.w(TAG, "Clearing Bounds:" + i);
					mVideoGenParams.mVignettes.get(i).mEndBounds = null;
					mVideoGenParams.mVignettes.get(i).mStartBounds = null;
				}
			}
		}
		// Log.e(TAG, "Image Changes Swap:" + wasSwap);
		// persistImages(wasSwap);
		updateDoneButton();
	}
	private void updateDoneButton() {
		boolean allImagesComplete = true;
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			SquareImageView view = mSelectedImagesViews.get(i);
			String filePath = view.getFilePath();
			if (filePath == null || filePath.equals("")) {
				allImagesComplete = false;
				break;
			}
		}
		if (allImagesComplete) {
			mDoneButton.setEnabled(true);
			mDoneButton.setVisibility(View.VISIBLE);
		} else {
			mDoneButton.setEnabled(false);
			mDoneButton.setVisibility(View.INVISIBLE);
		}
	}

	private void refreshViewPager() {
		// Log.w(TAG,"Refreshing");
		int item = mLargePreviewPager.getCurrentItem();
		mLargePreviewPager.setAdapter(mLargePreviewPager.getAdapter());
		mLargePreviewPager.setCurrentItem(item, false);
	}

	@Override
	public boolean areViewsDragable() {
		// return mAreImagesDragable;
		return true;
	}

}
