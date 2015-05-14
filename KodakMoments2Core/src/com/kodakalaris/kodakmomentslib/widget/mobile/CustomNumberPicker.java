package com.kodakalaris.kodakmomentslib.widget.mobile;

import net.simonvt.numberpicker.NumberPicker;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class CustomNumberPicker extends NumberPicker {
	public CustomNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void addView(View child) {
		super.addView(child);
		updateView(child);
	}

	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		updateView(child);
	}

	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, params);
		updateView(child);
	}

	public void updateView(View view) {
		if (view instanceof EditText) {
			((EditText) view).setFocusable(false);
			((EditText) view).setTextSize(20);
			((EditText) view).setTextColor(Color.BLACK);
		}
	}
}
