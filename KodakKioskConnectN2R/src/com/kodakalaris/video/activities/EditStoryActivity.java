package com.kodakalaris.video.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.utils.RSSLocalytics;
import com.kodakalaris.video.storydoc_format.VideoGenParams;

public class EditStoryActivity extends PreviewActivity {
	
	private static final String TAG = EditStoryActivity.class.getSimpleName();
	public static final String TMS_EDIT="TMS Edit";
	public static final String TMS_DELETE="TMS Delete";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RSSLocalytics.recordLocalyticsPageView(this, TMS_EDIT);
		findViewById(R.id.preview_edit_holder).setVisibility(View.VISIBLE);
		findViewById(R.id.preview_share_holder).setVisibility(View.VISIBLE);
		findViewById(R.id.preview_delete_holder).setVisibility(View.VISIBLE);
		headerBar_tex.setText(mVideoGenParams.mProjectTitle);
	}
	
	public void onEditButton(View v){
		RSSLocalytics.recordLocalyticsEvents(EditStoryActivity.this, TMS_EDIT);
		Intent intent = new Intent(this, TMSSelectPhotosActivity.class);
		boolean IsEditActivity=true;
		Bundle options = new Bundle();
		options.putString(INSTANCE_STATE_KEY_VIDEO_PARAMATERS, mVideoGenParams.mUUID.toString());
		intent.putExtras(options);
		intent.putExtra("TMS_EDIT", IsEditActivity);
		startActivity(intent);
		finish();
	}
	
	public void onDeleteButton(View v){
		RSSLocalytics.recordLocalyticsEvents(EditStoryActivity.this, TMS_DELETE);
		findViewById(R.id.flDelete).setVisibility(View.VISIBLE);
		TextView tvTitleName = (TextView) findViewById(R.id.tvTitleName);
		//tvTitleName.setText(String.format(getString(R.string.TMS_are_you_sure_delete2), mVideoGenParams.mProjectTitle));
		tvTitleName.setText(mVideoGenParams.mProjectTitle);
	}
	
	public void onDeleteYes(View v){
		String uuid = mVideoGenParams.mUUID.toString();
		VideoGenParams.deleteProject(this, uuid);
		mVideoGenParams = null;
		Intent mIntent = new Intent(this, PreviousProjectsActivity.class);
		startActivity(mIntent);
		finish();
	}
	
	public void onDeleteNo(View v){
		findViewById(R.id.flDelete).setVisibility(View.GONE);
	}

}
