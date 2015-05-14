package com.kodakalaris.photokinavideotest.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kodakalaris.photokinavideotest.R;
import com.kodakalaris.photokinavideotest.activities.BaseActivity;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams;
import com.kodakalaris.photokinavideotest.views.SquareImageView;

public class ProjectListAdapter extends BaseAdapter {
	private static final String TAG = ProjectListAdapter.class.getSimpleName();
	ArrayList<VideoGenParams> mProjects;
	private Context mContext;
	public ProjectListAdapter(Context context) {
		mContext = context;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		// Log.i(TAG, "Getting view:" + position);
		final VideoGenParams project = mProjects.get(position);
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.previous_projects_project_list_element, parent, false);
			final SquareImageView image = (SquareImageView) convertView.findViewById(R.id.previous_projects_project_list_element_image);
			final TextView text = (TextView) convertView.findViewById(R.id.previous_projects_project_list_element_text);
			image.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					BaseActivity.removeViewTreeObserverVersionSafe(this, image);
					if (image.getHeight() != 0 && image.getWidth() != 0) {
						// Log.e(TAG, "Inflating view after layout:" +
						// image.getHeight() + " P:" + position);
						doSetViewBasedOnProject(project, image, text);
						// } else {
						// Log.e(TAG, "Image height 0 after global layout");
					}
				}
			});
			notifyDataSetChanged();
			// This is required, otherwise onGlobalLayout
			// will not get called for the first element
			// which is off the screen. Why this view isn't
			// recycled (convertView != null)
			// doesn't make any since. You would think that inflating the view
			// would mean that the view needs to get layed out.
			// Shouldn't convert view either be non null or onGlobalLayout get
			// called? Guess not.
		} else {
			SquareImageView image = (SquareImageView) convertView.findViewById(R.id.previous_projects_project_list_element_image);
			TextView text = (TextView) convertView.findViewById(R.id.previous_projects_project_list_element_text);
			// Log.e(TAG, "Inflating view RECYCLE layout:" + image.getHeight() +
			// " P:" + position);
			doSetViewBasedOnProject(project, image, text);
		}
		return convertView;
	}

	private void doSetViewBasedOnProject(final VideoGenParams project, final SquareImageView image, final TextView text) {
		if (project != null && project.mVignettes != null && project.mVignettes.size() > 0 && project.mVignettes.get(0).mImagePath != null) {
			image.setImageBitmapAndFilePath(project.mVignettes.get(0).mImagePath);
		} else {
			// Log.e(TAG, "Project with Null");
			image.setImageBitmapAndFilePath(null);
		}
		if (project != null && project.mUUID != null) {
			text.setText(project.mProjectTitle + "\n" + project.mUUID.toString());
		} else {
			text.setText(R.string.activity_previous_projects_error_loading_project);
		}
	}
}
