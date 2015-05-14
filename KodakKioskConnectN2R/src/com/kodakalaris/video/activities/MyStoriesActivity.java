package com.kodakalaris.video.activities;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.AppManager;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.utils.RSSLocalytics;
import com.kodakalaris.video.adapters.ProjectGridAdapter;
import com.kodakalaris.video.storydoc_format.VideoGenParams;
import com.kodakalaris.video.two_way_grid_view.TwoWayAdapterView;
import com.kodakalaris.video.two_way_grid_view.TwoWayAdapterView.OnItemClickListener;
import com.kodakalaris.video.two_way_grid_view.TwoWayAdapterView.OnItemLongClickListener;
import com.kodakalaris.video.two_way_grid_view.TwoWayGridView;
import com.kodakalaris.video.views.AnimatedVideoDialog;
import com.kodakalaris.video.views.AnimatedVideoView.AnimatedVideoViewHoldingActivity;

public class MyStoriesActivity extends BaseActivity implements AnimatedVideoViewHoldingActivity {
	private static final String TAG = MyStoriesActivity.class.getSimpleName();
	private TwoWayGridView mProjectGrid;
	private ProjectGridAdapter mProjectGridAdapter;
	private View mFirstTimeImage;
	private Dialog mVideoDialog;
	private VideoGenParams mCurrentlyPlayingParams;
	public static final String TMS_MY_STORYS="TMS My Stories";
	public static final String TMS_CREATE_START="TMS Create Start";
	public static final String TMS_PLAY="TMS Play";
	public static int CreateTime=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_my_stories);
		Log.e(TAG,this.getPackageName());
		Log.e(TAG,PreviewActivity.getKeyHash(this));
		Log.e(TAG,getResources().getString(R.string.app_id));
		setContentLayout(R.layout.activity_my_stories);
		RSSLocalytics.recordLocalyticsPageView(this, TMS_MY_STORYS);
		getViews();
		initData();
		setEvents();
	}

	@Override
	protected void onPause() {
		if (mVideoDialog != null){
			mVideoDialog.dismiss();
		}
		super.onPause();
	}

	public void onCreateNewStoryClick(View v) {
		VideoGenParams params = new VideoGenParams(this);
		params.persistToFileSystem(this);
		String guid = params.mUUID.toString();
		editProject(guid);
		RSSLocalytics.recordLocalyticsEvents(MyStoriesActivity.this, TMS_CREATE_START);
		// refreshStoryList();
		Time time=new Time();
		time.setToNow();
		int CreateHour=time.hour;
		int CreateMinute=time.minute;
		int CreateSecond=time.second;
		CreateTime=CreateHour*60*60+CreateMinute*60+CreateSecond;
	}

	private void refreshStoryList() {
		mProjectGridAdapter = new ProjectGridAdapter(this, R.layout.activity_my_stories_grid_element, R.id.my_stories_project_grid_element_image);
		if (mProjectGridAdapter.getCount() == 0){
			mProjectGrid.setVisibility(View.GONE);
			mFirstTimeImage.setVisibility(View.VISIBLE);
			headerBar_tex.setVisibility(View.GONE);
		} else{
			mProjectGrid.setVisibility(View.VISIBLE);
			headerBar_tex.setVisibility(View.VISIBLE);
			mFirstTimeImage.setVisibility(View.GONE);
		}
		mProjectGrid.setAdapter(mProjectGridAdapter);
	}

	private void editProject(String guid) {
//		Intent intent = new Intent(MyStoriesActivity.this, SelectPhotosActivity.class);
		Intent intent = new Intent(MyStoriesActivity.this, TMSSelectPhotosActivity.class);
		Bundle options = new Bundle();
		options.putString(BaseActivity.INSTANCE_STATE_KEY_VIDEO_PARAMATERS, guid);
		intent.putExtras(options);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// fix defect RSSMOBILEPDC-1833 Kane
		// text count number cannot be got when onResume, so sleep 100ms waiting for the layout built
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				final TextView storyText = (TextView) findViewById(R.id.my_stories_text);
				storyText.setText(R.string.TMS_mainmenu_tellmystory_text);
				if(storyText.getLineCount()>2){
					storyText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
					RelativeLayout.LayoutParams params = (LayoutParams) storyText.getLayoutParams();
					float m = getResources().getDisplayMetrics().density ;
					params.bottomMargin = (int)(-40 * m + 0.5f);
					storyText.setLayoutParams(params);
				}
				super.onPostExecute(result);
			}
		}.execute();
		
		refreshStoryList();
	}

	private void getViews() {
		mProjectGrid = (TwoWayGridView) findViewById(R.id.my_stories_project_grid);
		mProjectGrid.setScrollDirectionPortrait(TwoWayGridView.SCROLL_HORIZONTAL);
		mFirstTimeImage = findViewById(R.id.my_stories_first_time);
	}

	private void initData() {

	}

	private void setEvents() {
		mProjectGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(TwoWayAdapterView<?> parent, View view, int index, long id) {
				String guid = ((VideoGenParams) mProjectGridAdapter.getItem(index)).mUUID.toString();
				editProject(guid);
			}
		});
		mProjectGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
			private static final int OPTION_DELETE = 0;
			private static final int OPTION_RENAME = 1;

			@Override
			public boolean onItemLongClick(TwoWayAdapterView<?> parent, View view, final int index, long id) {
				final VideoGenParams project = ((VideoGenParams) mProjectGridAdapter.getItem(index));
				String[] options = getResources().getStringArray(R.array.activity_previous_projects_project_long_press_options);
				AlertDialog.Builder builder = new AlertDialog.Builder(MyStoriesActivity.this);
				builder.setTitle(R.string.activity_previous_projects_long_press_title);
				if (project == null || project.mUUID == null) {
					options = new String[] { options[0] };// Only show delete
				}
				builder.setItems(options, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case OPTION_DELETE:
							// Don't use the projects's uuid for deleting,
							// it might be null and we always want them
							// to be able to delete a project.
							String uuid;
							if (project == null || project.mUUID == null) {
								File f = new File(VideoGenParams.getProjectBasePath(MyStoriesActivity.this));
								f.mkdirs();
								File[] projects = f.listFiles();
								// Sort the files in the same order as the
								// displaying list.
								// This ensure we delete the right project.
								VideoGenParams.sortProjects(projects);
								File project = projects[index];
								uuid = project.getName();
							} else {
								uuid = project.mUUID.toString();
							}
							Log.e(TAG, "Deleting:" + uuid);
							VideoGenParams.deleteProject(MyStoriesActivity.this, uuid);

							refreshStoryList();
							break;
						case OPTION_RENAME:
							AlertDialog.Builder alert = new AlertDialog.Builder(MyStoriesActivity.this);
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
									project.persistToFileSystem(MyStoriesActivity.this);
									refreshStoryList();
								}
							});

							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
								}
							});
							AlertDialog renameDialog = alert.show();
							// int textViewId =
							// renameDialog.getContext().getResources().getIdentifier("android:id/alertTitle",
							// null, null);
							// TextView tv = (TextView)
							// renameDialog.findViewById(textViewId);
							// tv.setTextColor(Color.BLACK);
							renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
							break;
						}

					}
				});
				Dialog d = builder.show();
				// int textViewId =
				// d.getContext().getResources().getIdentifier("android:id/alertTitle",
				// null, null);
				// TextView tv = (TextView) d.findViewById(textViewId);
				// tv.setTextColor(Color.BLACK);
				return true;
			}
		});
	}

	public void onStartButtonClicked(View view) {
		if (mVideoDialog == null) {
			RSSLocalytics.recordLocalyticsEvents(MyStoriesActivity.this, TMS_PLAY);
			mCurrentlyPlayingParams = (VideoGenParams) view.getTag();
			mVideoDialog = new AnimatedVideoDialog(this, mCurrentlyPlayingParams,MyStoriesActivity.this,R.style.DropDownDialog);
			mVideoDialog.show();
		}
	}

	@Override
	public void onShouldStartAudio(int index) {
		playAudioFile(mCurrentlyPlayingParams.mVignettes.get(index).mAudioPath);
	}

	@Override
	public void onVideoAnimationEnded() {
		mCurrentlyPlayingParams = null;
		//The dialog dismisses it self right before calling onVideoAnimationEnded
		//mVideoDialog.dismiss();
		mVideoDialog = null;
	}

	@Override
	public void onReadyToPlay() {
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainMenu.class);
		AppManager.getAppManager().finishAllActivity();
		startActivity(intent);
		finish();
	}
}
