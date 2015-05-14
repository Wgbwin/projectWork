package com.kodakalaris.photokinavideotest.activities;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.kodakalaris.photokinavideotest.R;
import com.kodakalaris.photokinavideotest.adapters.ProjectListAdapter;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams;

public class PreviousProjectsActivity extends Activity {
	private static final String TAG = PreviousProjectsActivity.class.getSimpleName();
	private ListView mProjectList;
	private ProjectListAdapter mProjectListAdapter;
	private int mCurentIndex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle(R.string.activity_previous_projects_title);
		// Log.e(TAG, "Recreating activity");
		setContentView(R.layout.activity_previous_projects);
		mProjectList = (ListView) findViewById(R.id.previous_projects_project_list);
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
		String projectName = getResources().getString(R.string.activity_previous_projects_default_project_title_prefix)+" " + mCurentIndex;
		VideoGenParams params = new VideoGenParams(this,projectName);
		mCurentIndex++;
		params.persistToFileSystem(this);
		String guid = params.mUUID.toString();
		editProject(guid);
		//refreshStoryList();
	}

	private void refreshStoryList() {
		mProjectListAdapter = new ProjectListAdapter(this);
		mProjectList.setAdapter(mProjectListAdapter);
	}
	private void editProject(String guid) {
		Intent intent = new Intent(PreviousProjectsActivity.this, SelectPhotosActivity.class);
		Bundle options = new Bundle();
		options.putString(BaseActivity.INSTANCE_STATE_KEY_VIDEO_PARAMATERS, guid);
		intent.putExtras(options);
		startActivity(intent);
	}
	@Override
	protected void onResume() {
		super.onResume();
		refreshStoryList();
		mProjectList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
				String guid = ((VideoGenParams) mProjectListAdapter.getItem(index)).mUUID.toString();
				editProject(guid);
			}
		});
		mProjectList.setOnItemLongClickListener(new OnItemLongClickListener() {
			private static final int OPTION_DELETE = 0;
			private static final int OPTION_RENAME = 1;
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int index, long arg3) {
				final VideoGenParams project = ((VideoGenParams) mProjectListAdapter.getItem(index));
				String[] options = getResources().getStringArray(R.array.activity_previous_projects_project_long_press_options);
				AlertDialog.Builder builder = new AlertDialog.Builder(PreviousProjectsActivity.this);
				builder.setTitle(R.string.activity_previous_projects_long_press_title);
				if (project == null || project.mUUID == null) {
					options = new String[]{options[0]};// Only show delete
				}
				builder.setItems(options, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case OPTION_DELETE :
								// Don't use the projects's uuid for deleting,
								// it might be null and we always want them
								// to be able to delete a project.
								String uuid;
								if (project == null || project.mUUID == null) {
									File f = new File(VideoGenParams.getProjectBasePath(PreviousProjectsActivity.this));
									f.mkdirs();
									File[] projects = f.listFiles();
									//Sort the files in the same order as the displaying list.
									//This ensure we delete the right project.
									VideoGenParams.sortProjects(projects);
									File project = projects[index];
									uuid = project.getName();
								} else {
									uuid = project.mUUID.toString();
								}
								Log.e(TAG, "Deleting:" + uuid);
								VideoGenParams.deleteProject(PreviousProjectsActivity.this, uuid);

								refreshStoryList();
								break;
							case OPTION_RENAME :
								AlertDialog.Builder alert = new AlertDialog.Builder(PreviousProjectsActivity.this);
								alert.setTitle("Change Project Title");
								alert.setMessage("");

								// Set an EditText view to get user input
								final EditText input = (EditText) getLayoutInflater().inflate(R.layout.previous_projects_project_rename_dialog_edit_text, null);
								alert.setView(input);
								input.setText(project.mProjectTitle);
								input.requestFocus();
								alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										String value = input.getText().toString();
										Log.e(TAG, "Setting title:" + value);
										project.mProjectTitle = value;
										project.persistToFileSystem(PreviousProjectsActivity.this);
										refreshStoryList();
									}
								});

								alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
									}
								});
								AlertDialog renameDialog = alert.show();
								int textViewId = renameDialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
								TextView tv = (TextView) renameDialog.findViewById(textViewId);
								tv.setTextColor(Color.BLACK);
								renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
								break;
						}

					}
				});
				Dialog d = builder.show();
				int textViewId = d.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
				TextView tv = (TextView) d.findViewById(textViewId);
				tv.setTextColor(Color.BLACK);
				return true;
			}
		});
	}
}
