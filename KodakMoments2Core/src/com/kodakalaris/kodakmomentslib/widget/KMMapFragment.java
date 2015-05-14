package com.kodakalaris.kodakmomentslib.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.MapFragment;

public class KMMapFragment extends MapFragment {

	private OnTouchListener mOnTouchListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = super.onCreateView(inflater, container, savedInstanceState);
		TouchableWrapper frameLayout = new TouchableWrapper(getActivity());
		frameLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		((ViewGroup) layout).addView(frameLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		return layout;
	}

	public void setListener(OnTouchListener listener) {
		mOnTouchListener = listener;
	}

	public interface OnTouchListener {
		public abstract void onTouch();
	}

	public class TouchableWrapper extends FrameLayout {

		public TouchableWrapper(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mOnTouchListener.onTouch();
				break;
			case MotionEvent.ACTION_UP:
				mOnTouchListener.onTouch();
				break;
			}
			return super.dispatchTouchEvent(event);
		}
	}
}
