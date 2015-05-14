package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.AppConstants.ActivityTheme;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.bean.LocalCustomerInfo;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;

public class ConfirmOrderDialog extends BaseGeneralAlertDialogFragment {
	private CheckBox vChkbSendEmail;
	private TextView vTxtAddress;
	private String mAddress;

	public ConfirmOrderDialog(Context context, ActivityTheme theme,
			boolean cancelable) {
		super(context, theme, cancelable);
	}

	public ConfirmOrderDialog(Context context, ActivityTheme theme) {
		super(context, theme);
	}

	public ConfirmOrderDialog(Context context, boolean cancelable) {
		super(context, cancelable);
	}

	public ConfirmOrderDialog(Context context) {
		super(context);
	}
	
	public ConfirmOrderDialog setAddress(String address) {
		mAddress = address;
		return this;
	}

	@Override
	protected View initMessageContent() {
		View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm_order_content, null);
		
		vChkbSendEmail = (CheckBox) v.findViewById(R.id.chkb_send_email);
		vTxtAddress = (TextView) v.findViewById(R.id.txt_address);
		
		vTxtAddress.setText(mAddress);
		
		return v;
	}
	
	
	public boolean isCheckedSendEmail() {
		return vChkbSendEmail.isChecked();
	}
	
	
}
