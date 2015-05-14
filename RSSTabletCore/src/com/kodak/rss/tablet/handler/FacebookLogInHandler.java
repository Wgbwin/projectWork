package com.kodak.rss.tablet.handler;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseActivity;
import com.kodak.rss.tablet.facebook.FbkObject;
import com.kodak.rss.tablet.facebook.FbkUser;
import com.kodak.rss.tablet.facebook.HandlerConstant;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class FacebookLogInHandler extends Handler{
					
	private BaseActivity activity;
	private boolean isHaveDialog;
	private ProgressDialog mSpinner;
	
	public FacebookLogInHandler(Context context) {			
		this.activity = (BaseActivity) context;	
		mSpinner = new ProgressDialog(activity);
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("loading...");		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(Message msg) {
		if (activity == null) return;
		if (activity.isFinishing()) return;			
		final int action = msg.what;
		Object msgObject = msg.obj;			
		switch (action) {
		case HandlerConstant.GET_FBKINFO_ERROR:	
			if (mSpinner != null && mSpinner.isShowing()) {
				mSpinner.dismiss();
			}		
			if (msgObject != null && !isHaveDialog) {							
				android.content.DialogInterface.OnClickListener okOnClickListener  = new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						isHaveDialog = false;				
					}		
				};					
				new InfoDialog.Builder(activity).setMessage(R.string.fail_connect_facebook)
				.setPositiveButton(activity.getText(R.string.d_ok), okOnClickListener).create()
				.show();	
				isHaveDialog = true;				
			}
			break;

		case HandlerConstant.GET_FBK_TOKEN_INFO_START:								
			activity.fbkAuth.getInfoList(mSpinner,FacebookLogInHandler.this,AppConstants.getMainUser,true,false);
			break;
			
		case HandlerConstant.GET_FBK_TOKEN_INFO_END:
			if (mSpinner != null && mSpinner.isShowing()) {
				mSpinner.dismiss();
			}
			ArrayList<FbkObject> mainUsers = (ArrayList<FbkObject>) msgObject;
			FbkUser fbkMainUser = (FbkUser) mainUsers.get(0);
			if (fbkMainUser != null) {
				SharedPreferrenceUtil.saveFacebookUserId(activity,fbkMainUser.ID);
				SharedPreferrenceUtil.saveFacebookFristName(activity, fbkMainUser.frist_name);
				SharedPreferrenceUtil.saveFacebookLastName(activity, fbkMainUser.last_name);
			}
			if (activity.sideMenu != null) {
				activity.sideMenu.notifyLoginStatusChanged();
			}				
			break;	
		}		
	}
	
	public void logIn(){		
		FacebookLogInHandler.this.sendEmptyMessage(HandlerConstant.GET_FBK_TOKEN_INFO_START);				
	}
		
}
