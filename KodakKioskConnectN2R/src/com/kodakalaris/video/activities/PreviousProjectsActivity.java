package com.kodakalaris.video.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.utils.RSSLocalytics;
import com.kodakalaris.video.adapters.ProjectListAdapter;
import com.kodakalaris.video.storydoc_format.VideoGenParams;

public class PreviousProjectsActivity extends BaseActivity {
	private static final String TAG = PreviousProjectsActivity.class.getSimpleName();
	private ListView mProjectList;
	private ProjectListAdapter mProjectListAdapter;
	private DrawerLayout tmd_drawer_layout;
	private LinearLayout tms_left_drawer;
	public static final String TMS_MY_PROJECT="TMS My Projects";
	public static final String TMS_SAVED_STORY_SELECTED="TMS Saved Story Selected";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getActionBar().setTitle(R.string.activity_previous_projects_title);
		// Log.e(TAG, "Recreating activity");
		//setContentView(R.layout.activity_previous_projects);
		setContentLayout(R.layout.activity_previous_projects);
		RSSLocalytics.recordLocalyticsPageView(this, TMS_MY_PROJECT);
		initData();
		getViews();
		setEvents();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void onCreateNewStoryClick(View v) {
		VideoGenParams params = new VideoGenParams(this);
		params.persistToFileSystem(this);
		String guid = params.mUUID.toString();
		editProject(guid);
		// refreshStoryList();
	}

	private void refreshStoryList() {
		mProjectListAdapter = new ProjectListAdapter(this, R.layout.previous_projects_project_list_element, R.id.previous_projects_project_list_element_image);
		mProjectList.setAdapter(mProjectListAdapter);
	}

	private void editProject(String guid) {
		VideoGenParams params = VideoGenParams.readFromFileSystem(this, guid);
		int validateResult = params.validate();
		Intent intent = null;
		if(validateResult==VideoGenParams.INVALID_IMAGE_SELECTION || validateResult==VideoGenParams.INVALID_NO_IMAGE_SELECTED){
			intent = new Intent(PreviousProjectsActivity.this, TMSSelectPhotosActivity.class);
		} else if(validateResult == VideoGenParams.INVALID_AUDIO_RECORD){
			intent = new Intent(PreviousProjectsActivity.this, AddAudioActivity.class);
		} else {
			intent = new Intent(PreviousProjectsActivity.this, EditStoryActivity.class);
		}
		Bundle options = new Bundle();
		options.putString(BaseActivity.INSTANCE_STATE_KEY_VIDEO_PARAMATERS, guid);
		intent.putExtras(options);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshStoryList();
	}
	
	private void getViews() {
		mProjectList = (ListView) findViewById(R.id.previous_projects_project_list);	
		tmd_drawer_layout = (DrawerLayout) findViewById(R.id.tmd_drawer_layout);
		tms_left_drawer = (LinearLayout) findViewById(R.id.tms_left_drawer);
		tmd_drawer_layout.removeView(tms_left_drawer);
	}

	private void initData() {
	}

	private void setEvents() {
		mProjectList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
				RSSLocalytics.recordLocalyticsEvents(PreviousProjectsActivity.this, TMS_SAVED_STORY_SELECTED);
				String guid = ((VideoGenParams) mProjectListAdapter.getItem(index)).mUUID.toString();
				editProject(guid);
			}
		});
	}
}
