package com.kodakalaris.kodakmomentslib.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.AppConstants.ActivityTheme;
import com.kodakalaris.kodakmomentslib.R;

public abstract class BaseGeneralAlertDialogFragment extends DialogFragment {

	protected Context mContext;
	protected ActivityTheme mActivityTheme;
	private LinearLayout vLineLyContentArea;
	private Button vBtnPositive;
	private Button vBtnNegative;
	private TextView vTxtTitle;
	private RelativeLayout realLyRoot;
	
	private CharSequence mTitle;
	private CharSequence mNegativeBtnText;
	private CharSequence mPositiveBtnText;
	private OnClickListener mOnNegativeBtnClickListener;
	private OnClickListener mOnPositiveBtnClickListener;
	
	private boolean isShowing;
	
	protected int mContainerAreaWidth = -1, mContainerAreaHeight = -1;
		
	/**
	 * Default theme : Light
	 */
	public BaseGeneralAlertDialogFragment(Context context) {
		mContext = context;
		mActivityTheme = ActivityTheme.LIGHT;
	};
	
	public BaseGeneralAlertDialogFragment(Context context, ActivityTheme theme) {
		mContext = context;
		mActivityTheme = ActivityTheme.DARK;
	};
	
	public BaseGeneralAlertDialogFragment(Context context, boolean cancelable) {
		this(context);
		setCancelable(cancelable);
	}
	
	public BaseGeneralAlertDialogFragment(Context context, ActivityTheme theme, boolean cancelable) {
		this(context, theme);
		mActivityTheme = ActivityTheme.DARK;
	};
	
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Dialog dialog = new Dialog(getActivity(), R.style.AlertDialogGeneral);
		
		View contentView = null;
		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
		
		if (mActivityTheme == ActivityTheme.LIGHT) {
			contentView = layoutInflater.inflate(R.layout.dialog_base_general_light, null);
		} 
		else if (mActivityTheme == ActivityTheme.DARK) {
			contentView = layoutInflater.inflate(R.layout.dialog_base_general_dark, null);
		} 
		else {
			contentView = layoutInflater.inflate(R.layout.dialog_base_general_light, null);
		}
		LinearLayout messageContainer = (LinearLayout) contentView.findViewById(R.id.vmessage_container);
		messageContainer.addView(initMessageContent());
		dialog.setContentView(contentView);
		
		
		realLyRoot = (RelativeLayout) dialog.findViewById(R.id.root);
		vBtnPositive = (Button) dialog.findViewById(R.id.btn_positive);
		vBtnNegative = (Button) dialog.findViewById(R.id.btn_negative);
		vTxtTitle = (TextView) dialog.findViewById(R.id.txt_title);
		vLineLyContentArea = (LinearLayout) dialog.findViewById(R.id.vcontent_area);
		if(mContainerAreaHeight != -1 || mContainerAreaWidth != -1){
			resizeContentAreaSize();
		}
		
		initForSize();
		
		setTitle(mTitle);
		setNegativeButton(mNegativeBtnText, mOnNegativeBtnClickListener);
		setPositiveButton(mPositiveBtnText, mOnPositiveBtnClickListener);
		
		realLyRoot.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isCancelable()) {
					dismiss();
				}
			}
		});
		
		return dialog;
	}
	
	protected abstract View initMessageContent();
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//Do nothing here.
		//Because onClickListener can't be saved, only save text make no sense
		//It is recommenmed to add flag android:configChanges="keyboardHidden|orientation|screenSize" for the activity.
		//Then the data will not lose because oncreate of activity and onConfigurationChanged will be called
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		initForSize();
	}
	
	/**
	 * default background for dialog is near half-transprent, we need to set it to document of design
	 * but I havn't found the method to set it. Only the exact layoutparams can work
	 */
	private void initForSize() {
		if (realLyRoot == null) {
			return;
		}
		
		Point size = new Point();
		getActivity().getWindowManager().getDefaultDisplay().getSize(size);
		realLyRoot.getLayoutParams().width = size.x;
		realLyRoot.getLayoutParams().height = size.y;
	}
	
	public BaseGeneralAlertDialogFragment setDialogTheme(ActivityTheme theme) {
		mActivityTheme = theme;
		return this;
	}
	
	public BaseGeneralAlertDialogFragment setTitle(CharSequence text) {
		mTitle = text;
		if (vTxtTitle != null) {
			vTxtTitle.setText(mTitle);
			vTxtTitle.setVisibility(mTitle == null ? View.GONE : View.VISIBLE);
		}
		return this;
	}
	
	public BaseGeneralAlertDialogFragment setTitle(int resId) {
		return setTitle(mContext.getString(resId));
	}
	
	public BaseGeneralAlertDialogFragment setPositiveButton(CharSequence buttonText, @Nullable OnClickListener onClickListener) {
		mPositiveBtnText = buttonText;
		mOnPositiveBtnClickListener = onClickListener;
		
		if (vBtnPositive != null) {
			if(TextUtils.isEmpty(mPositiveBtnText)){
				vBtnPositive.setVisibility( View.INVISIBLE );
			}else{
				vBtnPositive.setVisibility( View.VISIBLE );
				vBtnPositive.setText(mPositiveBtnText);
				vBtnPositive.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						dismiss();
						if (mOnPositiveBtnClickListener != null) {
							mOnPositiveBtnClickListener.onClick(BaseGeneralAlertDialogFragment.this, v);
						}
					}
				});
			}
			
			
//			vBtnPositive.setVisibility(mPositiveBtnText == null ? View.INVISIBLE : View.VISIBLE);
		}
		
		return this;
	}
	
	public BaseGeneralAlertDialogFragment setPositiveButton(int buttonTextResId, @Nullable OnClickListener onClickListener) {
		return setPositiveButton(mContext.getString(buttonTextResId), onClickListener);
	}
	
	public BaseGeneralAlertDialogFragment setNegativeButton(CharSequence buttonText, @Nullable OnClickListener onClickListener) {
		mNegativeBtnText = buttonText;
		mOnNegativeBtnClickListener = onClickListener;
		
		if (vBtnNegative != null) {
			if(TextUtils.isEmpty(mNegativeBtnText)){
				vBtnNegative.setVisibility(View.INVISIBLE);
			}else{
				vBtnNegative.setVisibility(View.VISIBLE);
				vBtnNegative.setText(mNegativeBtnText);
				vBtnNegative.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						dismiss();
						if (mOnNegativeBtnClickListener != null ) {
							mOnNegativeBtnClickListener.onClick(BaseGeneralAlertDialogFragment.this, v);
						}
					}
				});
			}
			
			
//			vBtnNegative.setVisibility(mNegativeBtnText == null ? View.INVISIBLE : View.VISIBLE);
		}
		
		return this;
	}
	
	public BaseGeneralAlertDialogFragment setNegativeButton(int buttonTextResId, @Nullable OnClickListener onClickListener) {
		return setNegativeButton(mContext.getString(buttonTextResId), onClickListener);
	}
	
	private void resizeContentAreaSize(){
		RelativeLayout.LayoutParams params = (LayoutParams) vLineLyContentArea.getLayoutParams();
		if(mContainerAreaWidth != -1){
			params.width = mContainerAreaWidth;
		}
		if(mContainerAreaHeight != -1){
			params.height = mContainerAreaHeight;
		}
	}
	
	public BaseGeneralAlertDialogFragment setContentAreaSize(int width, int height){
		this.mContainerAreaWidth = width;
		this.mContainerAreaHeight = height;
		return this;
	}
	
	public BaseGeneralAlertDialogFragment setContentAreaSize(float widthPercent, float heightPercent){
		Point size = new Point();
		((Activity)mContext).getWindowManager().getDefaultDisplay().getSize(size);
		int width = (int)(size.x * widthPercent);
		int height = (int)(size.y * heightPercent);
		setContentAreaSize(width, height);
		
		return this;
	}
	
	@Override
	public void show(FragmentManager manager, String tag) {
		isShowing = true;
		super.show(manager, tag);
	}
	
	@Override
	public int show(FragmentTransaction transaction, String tag) {
		isShowing = true;
		return super.show(transaction, tag);
	}
	
	public boolean isShowing() {
		return isShowing;
	}
	
	@Override
	public void dismiss() {
		//dissmiss() when the app is in background, it will crash by IllegalStateException 
//		isShowing = false;
//		super.dismiss();
		dismissAllowingStateLoss();
	}
	
	@Override
	public void dismissAllowingStateLoss() {
		isShowing = false;
		super.dismissAllowingStateLoss();
	}
	
	public interface OnClickListener {
		void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v);
	}
	
}
