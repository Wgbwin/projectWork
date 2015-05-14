package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.AppConstants.ActivityTheme;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.R;

/**
 * 
 * @author Robin QIAN
 *
 */
public class GeneralAlertDialogFragment extends BaseGeneralAlertDialogFragment {
	private TextView vTxtMessage;
	private CharSequence mMessage;
	
	public GeneralAlertDialogFragment(Context context, ActivityTheme theme,
			boolean cancelable) {
		super(context, theme, cancelable);
	}

	public GeneralAlertDialogFragment(Context context, ActivityTheme theme) {
		super(context, theme);
	}

	public GeneralAlertDialogFragment(Context context, boolean cancelable) {
		super(context, cancelable);
	}

	public GeneralAlertDialogFragment(Context context) {
		super(context);
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		setMessage(mMessage);
		
		return dialog;
	}
	
	public GeneralAlertDialogFragment setMessage(CharSequence text) {
		mMessage = text;
		if (vTxtMessage != null) {
			vTxtMessage.setText(mMessage);
		}
		return this;
	}
	
	public GeneralAlertDialogFragment setMessage(int resId) {
		return setMessage(mContext.getString(resId));
	}
	
	@Override
	protected View initMessageContent() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View v;
		if (mActivityTheme == ActivityTheme.LIGHT) {
			v = inflater.inflate(R.layout.dialog_general_light, null);
		} else if (mActivityTheme == ActivityTheme.DARK) {
			v = inflater.inflate(R.layout.dialog_general_dark, null);
		} else {
			v = inflater.inflate(R.layout.dialog_general_light, null);
		}
		
		vTxtMessage = (TextView) v.findViewById(R.id.txt_message);
		return v;
	}

	@Override
	public GeneralAlertDialogFragment setDialogTheme(ActivityTheme theme) {
		return (GeneralAlertDialogFragment) super.setDialogTheme(theme);
	}

	@Override
	public GeneralAlertDialogFragment setTitle(CharSequence text) {
		return (GeneralAlertDialogFragment) super.setTitle(text);
	}

	@Override
	public GeneralAlertDialogFragment setTitle(int resId) {
		return (GeneralAlertDialogFragment) super.setTitle(resId);
	}

	@Override
	public GeneralAlertDialogFragment setPositiveButton(
			CharSequence buttonText, @Nullable OnClickListener onClickListener) {
		return (GeneralAlertDialogFragment) super.setPositiveButton(buttonText, onClickListener);
	}

	@Override
	public GeneralAlertDialogFragment setPositiveButton(
			int buttonTextResId, @Nullable OnClickListener onClickListener) {
		return (GeneralAlertDialogFragment) super.setPositiveButton(buttonTextResId, onClickListener);
	}

	@Override
	public GeneralAlertDialogFragment setNegativeButton(
			CharSequence buttonText, @Nullable OnClickListener onClickListener) {
		return (GeneralAlertDialogFragment) super.setNegativeButton(buttonText, onClickListener);
	}

	@Override
	public GeneralAlertDialogFragment setNegativeButton(
			int buttonTextResId, @Nullable OnClickListener onClickListener) {
		return (GeneralAlertDialogFragment) super.setNegativeButton(buttonTextResId, onClickListener);
	}

	@Override
	public GeneralAlertDialogFragment setContentAreaSize(int width,
			int height) {
		return (GeneralAlertDialogFragment) super.setContentAreaSize(width, height);
	}

	@Override
	public GeneralAlertDialogFragment setContentAreaSize(
			float widthPercent, float heightPercent) {
		return (GeneralAlertDialogFragment) super.setContentAreaSize(widthPercent, heightPercent);
	}
	
	

}
