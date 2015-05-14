package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class MEditText extends EditText {
	private HideKeyboard mHideKeyboard;

	public MEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	public MEditText(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public MEditText(Context context) {
		super(context);

	}

	public void setHideListener(HideKeyboard hideKeyboard) {
		mHideKeyboard = hideKeyboard;
	}

	public interface HideKeyboard {
		public abstract void hideKeyboard();
	}

	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			mHideKeyboard.hideKeyboard();
		}
		return super.dispatchKeyEventPreIme(event);
	}

}
