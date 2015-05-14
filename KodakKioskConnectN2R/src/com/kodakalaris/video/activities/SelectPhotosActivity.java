package com.kodakalaris.video.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodakalaris.video.MediaStoreUtils;
import com.kodakalaris.video.SquareImageViewClickListener;
import com.kodakalaris.video.activities.TMSSelectPhotosActivity.FilePersisterHelper;
import com.kodakalaris.video.adapters.ImageGridAdapter;
import com.kodakalaris.video.adapters.LargeImagePreviewFileNameAdapter;
import com.kodakalaris.video.adapters.LargeImagePreviewPagerAdapter;
import com.kodakalaris.video.roifacedetect.FaceDetectorTask;
import com.kodakalaris.video.roifacedetect.FinishedFindingFacesEvent;
import com.kodakalaris.video.storydoc_format.VideoGenParams;
import com.kodakalaris.video.storydoc_format.VideoGenParams.Vignette;
import com.kodakalaris.video.two_way_grid_view.TwoWayAbsListView;
import com.kodakalaris.video.two_way_grid_view.TwoWayGridView;
import com.kodakalaris.video.views.SquareImageView;

public class SelectPhotosActivity extends BaseActivity implements SquareImageViewClickListener {

	TwoWayGridView mBottomGrid;
	View mDoneButton;
	private boolean mIsLargePreviewVisable;
	ViewGroup mRootView;
	ViewPager mLargePreviewPager;
	LargeImagePreviewPagerAdapter mLargePreviewPagerAdapter;
	private LargeImagePreviewFileNameAdapter mLargePreviewFileNameAdapter;
	ArrayList<SquareImageView> mSelectedImagesViews;
	private AsyncTask<Void, Void, Void> mImageFilePersistor;
	protected boolean mAreImagesDragable = true;
	private FaceDetectorTask mFaceDetectorTask;
	protected static final String TAG = SelectPhotosActivity.class.getSimpleName();
	private static final String SHOW_SELECT_PHOTO_HELP = "SHOW_SELECT_PHOTO_HELP";
	private static final String SHOW_REMOVE_PHOTO_HELP = "SHOW_REMOVE_PHOTO_HELP";
	private LinearLayout helpLayout;
	private CheckBox helpCheckBox;
	private Button helpSetButton;
	private TextView text1, text2;
	private ImageButton delImgButton;
	private RelativeLayout mAnimLayer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_select_photos);
		setContentLayout(R.layout.activity_select_photos);
		getViews();
		setEvents();
		setProperNumberOfRowsOrColsBasedOnOrientationAndGridScrollDirection();
	}

	@Override
	protected void onResume() {
		super.onResume();
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			String imageFilePath = mVideoGenParams.mVignettes.get(i).mImagePath;
			// Log.e(TAG, "Restoreing image:" + imageFilePath);
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
//	public interface FilePersisterHelper {
//		public String getCurrentPath(int i);
//
//		public String getOldPath(Vignette vig);
//
//		public void setNewPath(String path, Vignette vig);
//
//		public String getFilePrefix();
//
//		public boolean compressFilesAsBitmaps();
//
//		public boolean areFilesinBitmapCache();
//
//	}

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
//			mLargePreviewPager.setAdapter(mLargePreviewPagerAdapter);
//			mIsLargePreviewVisable = true;
//			mLargePreviewPager.setVisibility(View.VISIBLE);
//			changePreviewImage(view.getImagePosition());
			
			int position = getFirstEmptyPosition();
			if (position != -1) {
				selectAndAnimateImage(view,position);
			}
			
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
	public void onImageDoubleClick(SquareImageView squareImageView) {
		// TODO Auto-generated method stub
		
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
	
	private int getFirstEmptyPosition() {
		int position = -1;
		for (int i = 0; i < mSelectedImagesViews.size(); i++) {
			String filePath = mSelectedImagesViews.get(i).getFilePath();
			
			if (filePath == null || filePath .equals("")) {
				position = i;
				break;
			}
		}
		
		return position;
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
						setVignetteParams(i, dropedOn.getFilePath());
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
	
	private String mLastAnimationID;
	private HashMap<Integer, String> mAddImageMap = new HashMap<Integer, String>();
	private void selectAndAnimateImage(final SquareImageView view, int position) {
		//Generate a view for animation
		final ImageView iv = new ImageView(this);
		iv.setImageBitmap(getBitmapFromView(view));
		
		int[] locationSrc = new int[2];
		view.getLocationInWindow(locationSrc);
		int[] locationAnimLayer = new int[2];
		mAnimLayer.getLocationInWindow(locationAnimLayer);
		int[] locationDest = new int[2];
		final SquareImageView destView = mSelectedImagesViews.get(position);
		destView.getLocationInWindow(locationDest);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(view.getWidth(), view.getHeight());
		
		//Be careful, if we add imageView in the edge of the animLayer, the size may be incorrect
		//Here we add this imageView in left-top avoid this issue
		mAnimLayer.addView(iv,params);
		TranslateAnimation anim = new TranslateAnimation(TranslateAnimation.ABSOLUTE, locationSrc[0] - locationAnimLayer[0], TranslateAnimation.ABSOLUTE, locationDest[0] - locationAnimLayer[0],
				TranslateAnimation.ABSOLUTE, locationSrc[1] - locationAnimLayer[1], TranslateAnimation.ABSOLUTE, locationDest[1] - locationAnimLayer[1]);
		anim.setDuration(500);
		anim.setFillAfter(true);
		
		destView.setFilePath(view.getFilePath());//avoid repeat animation to the same select image
		mAddImageMap.put(position, view.getFilePath());
		
		final String animationId =  anim.toString();
		mLastAnimationID = animationId;
		anim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				destView.setImageBitmapAndFilePath(view.getFilePath());
				
				if (mLastAnimationID != null && mLastAnimationID.equals(animationId)) {
					for(Map.Entry<Integer, String> entry : mAddImageMap.entrySet()) {
						int position = entry.getKey();
						String filePath = entry.getValue();
						
						setVignetteParams(position, filePath);
					}
					
					mAnimLayer.removeAllViews();
					mAddImageMap.clear();
					persistImages(false);
					updateDoneButton();
				}
			}
		});
		
		iv.startAnimation(anim);
	}
	
	private void setVignetteParams(final int position, final String filePath) {
		final Vignette v = mVideoGenParams.mVignettes.get(position);
		v.mEndBounds = null;
		v.mStartBounds = calculateStartBounds(filePath);
		mFaceDetectorTask = new FaceDetectorTask(null);
		mFaceDetectorTask.setFinishEvent(new FinishedFindingFacesEvent() {
			@Override
			public void onFinish(RectF result) {
				v.mEndBounds = result;
				Log.e(TAG, "Setting Face ROI:" + (result == null ? "null" : result.toShortString()));
			}
		});
		Bitmap b = MediaStoreUtils.getFullRes(SelectPhotosActivity.this, filePath, AppConstants.TMS_IMAGE_MAX_DIMENSION, AppConstants.TMS_IMAGE_MAX_DIMENSION,
				SquareImageView.RESOLUTION_HIGHER);
		// b = Bitmap.createScaledBitmap(b, AppConstants.TMS_IMAGE_MAX_DIMENSION, AppConstants.TMS_IMAGE_MAX_DIMENSION, true);
		mFaceDetectorTask.execute(b);
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
			if (isNeedShowRemoveTips()) {
				helpCheckBox.setVisibility(View.GONE);
				helpSetButton.setText(getString(R.string.TMS_help_all_set));
				helpLayout.setVisibility(View.VISIBLE);
				delImgButton.setVisibility(View.VISIBLE);
				mDoneButton.setVisibility(View.VISIBLE);
				text1.setVisibility(View.GONE);
				text2.setText(getString(R.string.TMS_select_photos_help_continue_tapping));
			}
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

	public void onsSelectPhotoHelpSetButton(View v) {
		String buttonString = (String) helpSetButton.getText();
		if (buttonString.equals(getString(R.string.TMS_select_photos_help_dialog_got_it))) {
			boolean isNeedHelp = !helpCheckBox.isChecked();
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(SHOW_SELECT_PHOTO_HELP, isNeedHelp).commit();
			helpLayout.setVisibility(View.GONE);
		} else if (buttonString.equals(getString(R.string.TMS_help_all_set))) {
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(SHOW_REMOVE_PHOTO_HELP, false).commit();
			helpLayout.setVisibility(View.GONE);
		}

	}

	private void getViews() {
		helpLayout = (LinearLayout) findViewById(R.id.select_photo_help_tips);
		helpLayout.setVisibility(isNeedShowSelectTips() ? View.VISIBLE : View.GONE);
		helpCheckBox = (CheckBox) findViewById(R.id.select_photo_dont_show_help_again);
		helpSetButton = (Button) findViewById(R.id.select_photo_help_set_button);
		delImgButton = (ImageButton) findViewById(R.id.TMS_select_photos_help_dialog_imgBtn);
		text1 = (TextView) findViewById(R.id.TMS_select_photos_help_dialog_tex1);
		text2 = (TextView) findViewById(R.id.TMS_select_photos_help_dialog_tex2);
		headerBar_tex.setText(getString(R.string.TMS_select_photos_title));
		mAnimLayer = (RelativeLayout) findViewById(R.id.select_photo_anim_layer);
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
		mDoneButton = (View) findViewById(R.id.select_photos_done_button);
		;
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
	}
	
	private boolean isNeedShowSelectTips() {
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SHOW_SELECT_PHOTO_HELP, true);
	}
	
	private boolean isNeedShowRemoveTips() {
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SHOW_REMOVE_PHOTO_HELP, true);
	}

	private void setEvents() {
		helpCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				String buttonString = (String) helpSetButton.getText();
				if (buttonString.equals(getString(R.string.TMS_select_photos_help_dialog_got_it))) {
					boolean isNeedHelp = !helpCheckBox.isChecked();
					PreferenceManager.getDefaultSharedPreferences(SelectPhotosActivity.this).edit().putBoolean(SHOW_SELECT_PHOTO_HELP, isNeedHelp).commit();
					helpLayout.setVisibility(View.GONE);
				}

			}
		});

	}
	
	private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) 
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
//        else 
            //does not have background drawable, then draw white background on the canvas
//            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

}
