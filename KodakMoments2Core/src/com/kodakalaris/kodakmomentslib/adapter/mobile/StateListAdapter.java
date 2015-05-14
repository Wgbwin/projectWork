package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.util.ArrayList;
import java.util.List;

import net.simonvt.numberpicker.NumberPicker;
import net.simonvt.numberpicker.NumberPicker.OnScrollListener;
import net.simonvt.numberpicker.NumberPicker.OnValueChangeListener;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.findstore.BaseDestinationStoreSelectionActivity;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.mobile.CustomNumberPicker;

public class StateListAdapter extends BaseAdapter {
	private Context mContext;
	private CustomNumberPicker numberPicker = null;
	private PopupWindow popupWindow = null;
	private List<String> mState = new ArrayList<String>();
	private ShoppingCartManager manager;
	private final String TAG = getClass().getSimpleName();

	public StateListAdapter(Context context) {
		this.mContext = context;
	}

	@Override
	public int getCount() {

		return 0;
	}

	@Override
	public Object getItem(int position) {

		return null;
	}

	@Override
	public long getItemId(int position) {

		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		return null;
	}

	public void showPopWindow(final View btn) {
		manager = ShoppingCartManager.getInstance();
		mState = manager.getmStateValueList();
		View popContentView = LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.number_picker, null);
		numberPicker = (CustomNumberPicker) popContentView.findViewById(R.id.numberPicker);
		int stateSize = mState.size();
		if (stateSize <= 0) {
			stateSize = 0;
		} else {
			numberPicker.setMaxValue(stateSize - 1);
			numberPicker.setMinValue(0);
			BaseDestinationStoreSelectionActivity.txtState.setText(manager.getmStateValueList().get(0));
		}
		String[] displayValue = mState.toArray(new String[stateSize]);
		Log.e(TAG, "displayValueLength==" + displayValue.length);
		numberPicker.setDisplayedValues(displayValue);	
		numberPicker.setOnValueChangedListener(new OnValueChangeListener() {
			
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				manager.setmStateNumberPicker(newVal);
				Log.e(TAG, "stateNumberPicker==" + numberPicker.getValue());
				BaseDestinationStoreSelectionActivity.txtState.setText(manager.getmStateValueList().get(newVal));
				
			}
		});		
		
		if (null != popupWindow && popupWindow.isShowing()) {
			popupWindow.dismiss();
		} else {
			int w = KM2Application.getInstance().getScreenW();
			int h = KM2Application.getInstance().getScreenH() / 3;
			popupWindow = new PopupWindow(popContentView, w, h, true);
			popupWindow.setFocusable(true);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setBackgroundDrawable(new PaintDrawable(Color.WHITE));
			popupWindow.showAsDropDown(btn);
		}
		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {

			}
		});

	}


}
