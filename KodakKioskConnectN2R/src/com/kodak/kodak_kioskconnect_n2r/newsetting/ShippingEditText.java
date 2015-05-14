package com.kodak.kodak_kioskconnect_n2r.newsetting;

import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.NewSettingActivity;

public class ShippingEditText extends EditText {

	private long downTime = 0;
	
	public ShippingEditText(Context context) {
		super(context);
		AppContext.getApplication().setEmojiFilter(this);
	}
	
	public ShippingEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		AppContext.getApplication().setEmojiFilter(this);
	}
	
	public ShippingEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		AppContext.getApplication().setEmojiFilter(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			long time = new Date().getTime() - downTime;
			if(time<200){
				refreshPopStatus();
			}
			break;
		case MotionEvent.ACTION_DOWN:
			downTime = new Date().getTime();
			break;
		}
		return true;
	}
	
	private void refreshPopStatus() {
		if (NewSettingActivity.mPopupWindow != null && NewSettingActivity.mPopupWindow.isShowing()) {
			NewSettingActivity.mPopupWindow.dismiss();
		}
	}

}
