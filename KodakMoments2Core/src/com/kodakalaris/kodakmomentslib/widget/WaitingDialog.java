package com.kodakalaris.kodakmomentslib.widget;

import com.kodakalaris.kodakmomentslib.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class WaitingDialog extends BaseGeneralAlertDialogFragment {

	private Context mContext;
	private TextView tvMessage;
	private int messageId;
	
	private boolean isShowing;
	
	public WaitingDialog(Context context, boolean cancelable) {
		super(context, cancelable);
		mContext = context;
	}
	
	public WaitingDialog(Context context, boolean cancelable, int messageId) {
		this(context, cancelable);
		initDialog(messageId);
	}
	
	@Override
	protected View initMessageContent() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_waiting, null);
		tvMessage = (TextView) view.findViewById(R.id.tv_message);
		tvMessage.setText(mContext.getString(messageId));
		return view;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		isShowing = true;
		return super.onCreateDialog(savedInstanceState);
	}

	public WaitingDialog initDialog(int messageId){
		this.messageId = messageId;
		setContentAreaSize(0.78f, -1f);
		return this;
	}

}
