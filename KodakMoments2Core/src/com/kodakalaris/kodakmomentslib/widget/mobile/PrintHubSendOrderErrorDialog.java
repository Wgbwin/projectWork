package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.home.MHomeActivity;
import com.kodakalaris.kodakmomentslib.service.PrintHubUploadService;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;

public class PrintHubSendOrderErrorDialog extends BaseGeneralAlertDialogFragment {

	private Context mContext;
	private String mMessage;

	public PrintHubSendOrderErrorDialog(Context context,String message, boolean cancelable) {
		super(context, cancelable);
		this.mContext = context;
		this.mMessage = message;
	}

	public PrintHubSendOrderErrorDialog initDialog(Context context) {
		setTitle(mContext.getString(R.string.PrintHubSendOrder_dialog_title));
		setNegativeButton(context.getString(R.string.Common_Cancel), new OnClickListener() {

			@Override
			public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
				Intent i = new Intent();
				i.setClass(mContext, MHomeActivity.class);
				startActivity(i);
				((Activity) mContext).finish();
				dismiss();
			}
		});
		setPositiveButton(context.getString(R.string.Common_OK), new OnClickListener() {

			@Override
			public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
				dismiss();
				Intent serviceIntent = new Intent(mContext, PrintHubUploadService.class);
				try {
					ComponentName serviceComponentName = mContext.startService(serviceIntent);
					if (serviceComponentName != null) {
						Log.i("startPrintHubUploadService", "onCreate() startService called CompnentName=" + serviceComponentName.toString());
					}
				} catch (SecurityException se) {
					se.printStackTrace();
				}
			}
		});
		
		setContentAreaSize(0.78f,-1);
		return this;
	}

	@Override
	protected View initMessageContent() {
		TextView vTxtErrorMessage = new TextView(mContext);
		vTxtErrorMessage.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		vTxtErrorMessage.setText(mMessage);
		return vTxtErrorMessage;
	}

}
