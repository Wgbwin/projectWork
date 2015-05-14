package com.kodakalaris.video.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodakalaris.video.activities.BaseActivity;
import com.kodakalaris.video.storydoc_format.VideoGenParams;
import com.kodakalaris.video.views.SquareImageView;

public class ProjectListAdapter extends BaseAdapter {
	private static final String TAG = ProjectListAdapter.class.getSimpleName();
	ArrayList<VideoGenParams> mProjects;
	private Context mContext;
	private int mChildLayout;
	private int mImageID;
	protected boolean mZeroHasBeenLayedOut = false;

	public ProjectListAdapter(Context context, int childLayout, int imageID) {
		mContext = context;
		mChildLayout = childLayout;
		mImageID = imageID;
		mProjects = VideoGenParams.readAllFromFileSystem(context);
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

	protected void doSetViewBasedOnProject(final VideoGenParams project, View parent, int position) {
		final SquareImageView image = (SquareImageView) parent.findViewById(R.id.previous_projects_project_list_element_image);
		final TextView text = (TextView) parent.findViewById(R.id.previous_projects_project_list_element_text);
		if (project != null && project.mVignettes != null && project.mVignettes.size() > 0 && project.mVignettes.get(0).mImagePath != null) {
			ImageGridAdapter.doSetViewWithFilePath(position, image, null, 0, project.mVignettes.get(0).mImagePath);
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
			String date;
			if (project.mProjectSubTitleTimeDate == null) {
				date = "";
			} else {
				date = project.mProjectSubTitleTimeDate;
			}
			text.setText(title + "\n" + date);
		} else {
			text.setText(R.string.activity_previous_projects_error_loading_project);
		}
	}
}
