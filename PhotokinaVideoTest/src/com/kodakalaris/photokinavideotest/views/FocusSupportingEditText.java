package com.kodakalaris.photokinavideotest.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.kodakalaris.photokinavideotest.activities.PreviewActivity;

public class FocusSupportingEditText extends EditText {
	private static final String TAG = FocusSupportingEditText.class.getSimpleName();
	private PreviewActivity mActivity;

	public FocusSupportingEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (context instanceof PreviewActivity) {
			mActivity = (PreviewActivity) context;
			setOnEditorActionListener(new OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
						// Log.i(TAG, "Done pressed");
						mActivity.focusOnSomethingElse(v);
						InputMethodManager inputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.toggleSoftInput(0, 0);
					}
					return false;
				}
			});
		}
	}
	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (mActivity != null) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				// Log.e(TAG, "Hit the back button");
				mActivity.focusOnSomethingElse(this);
				return false; // So it is propagated.
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
