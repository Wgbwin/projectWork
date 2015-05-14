package com.kodakalaris.video.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLayoutChangeListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodakalaris.video.VideoAnimationProperty;
import com.kodakalaris.video.activities.AddTitleActivity;
import com.kodakalaris.video.activities.BaseActivity;
import com.kodakalaris.video.storydoc_format.VideoGenParams;
import com.kodakalaris.video.views.AnimatedVideoImage;
import com.kodakalaris.video.views.SquareImageView;

public class ProjectGridAdapter extends BaseAdapter {

	private static final String TAG = ProjectGridAdapter.class.getSimpleName();
	ArrayList<VideoGenParams> mProjects;
	private Context mContext;
	private int mChildLayout;
	private int mImageID;
	protected boolean mZeroHasBeenLayedOut = false;

	public ProjectGridAdapter(Context context, int childLayout, int imageID) {
		mContext = context;
		mChildLayout = childLayout;
		mImageID = imageID;
		
		ArrayList<VideoGenParams> allProjects = VideoGenParams.readAllFromFileSystem(context);
		mProjects = new ArrayList<VideoGenParams>(allProjects.size());
		for (VideoGenParams param : allProjects) {
			try{
				if (param.validate() == VideoGenParams.VALID) {
					mProjects.add(param);
				}
			} catch (NullPointerException e){
				VideoGenParams.deleteProject(context, param.mUUID.toString());
			}
		}
	}

	@Override
	public int getCount() {
		return mProjects.size();
	}

	@Override
	public Object getItem(int position) {
		return mProjects.get(position);

	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		Log.i(TAG, "Getting view:" + position);
		final VideoGenParams project = mProjects.get(position);
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(mChildLayout, parent, false);
			final View viewParent = convertView;
			final SquareImageView image = (SquareImageView) convertView.findViewById(mImageID);
			image.addOnLayoutChangeListener(new OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					image.removeOnLayoutChangeListener(this);
					if (image.getHeight() != 0 && image.getWidth() != 0) {
						doSetViewBasedOnProject(project, viewParent, position);
					}
					Log.e(TAG, "LayoutChanged:" + position);

				}
			});
			if (image.getWidth() != 0) {
				Log.e(TAG, "How can this be, it already knows its size");
				doSetViewBasedOnProject(project, viewParent, position);
			}
			/*
			 * viewParent.getViewTreeObserver().addOnGlobalLayoutListener(new
			 * OnGlobalLayoutListener() {
			 * 
			 * @Override public void onGlobalLayout() { if (position == 0) {
			 * mZeroHasBeenLayedOut = true; }
			 * BaseActivity.removeViewTreeObserverVersionSafe(this, viewParent);
			 * if (image.getHeight() != 0 && image.getWidth() != 0) { //
			 * Log.e(TAG, "Inflating view after layout:" + // image.getHeight()
			 * + " P:" + position); doSetViewBasedOnProject(project, viewParent,
			 * position); // } else { // Log.e(TAG,
			 * "Image height 0 after global layout"); } } });
			 */
			// if (mNotifyDataSetChanged) {
			notifyDataSetChanged();
			// }
			// This is required, otherwise onGlobalLayout
			// will not get called for the first element
			// which is off the screen. Why this view isn't
			// recycled (convertView != null)
			// doesn't make any since. You would think that inflating the view
			// would mean that the view needs to get layed out.
			// Shouldn't convert view either be non null or onGlobalLayout get
			// called? Guess not.
		} else {
			// Log.e(TAG, "Rescoring from convert");
			SquareImageView image = (SquareImageView) convertView.findViewById(mImageID);
			// if (position != 0 || mZeroHasBeenLayedOut) {
			if (image.getHeight() != 0 && image.getWidth() != 0) {
				doSetViewBasedOnProject(project, convertView, position);
			}
			// }
		}
		return convertView;
	}

	protected void doSetViewBasedOnProject(VideoGenParams project, View parent, int position) {
		final AnimatedVideoImage image = (AnimatedVideoImage) parent.findViewById(R.id.my_stories_project_grid_element_image);
		final TextView text = (TextView) parent.findViewById(R.id.my_stories_project_grid_element_title);
		final TextView subText = (TextView) parent.findViewById(R.id.my_stories_project_grid_element_date);
		final ImageButton playButton = (ImageButton) parent.findViewById(R.id.my_stories_project_grid_element_play_circle);
		playButton.setTag(project);
		if (project != null && project.mVignettes != null && project.mVignettes.size() > 0 && project.mVignettes.get(0).mImagePath != null) {
			Log.e(TAG, "Gettting progect grid view");
			ImageGridAdapter.doSetViewWithFilePath(position, image, null, 0, project.mVignettes.get(0).mImagePath);
			if (project.mVignettes.get(0).mStartBounds == null) {
				project.mVignettes.get(0).mStartBounds = BaseActivity.calculateStartBounds(project.mVignettes.get(0).mImagePath);
			}
			image.initConstantMatrix(project.mVignettes.get(0).mStartBounds);
			image.setMatrixProperty(new VideoAnimationProperty(project.mVignettes.get(0).mStartBounds));
		} else {
			// Log.e(TAG, "Project with Null");
			image.setImageBitmapAndFilePath(null);
		}
		if (project != null) {
			String title;
			if (project.mProjectTitle == null) {
				title = "";
			} else {
				title = project.mProjectTitle;
			}
			text.setText(title);
			String subTitle;
			if (project.mProjectSubTitleTimeDate == null) {
				subTitle = "";
			} else {
				subTitle = project.mProjectSubTitleTimeDate;
			}
			subText.setText(subTitle);
		} else {
			text.setText(R.string.activity_previous_projects_error_loading_project);
		}
		String subTitle;

	}
}
