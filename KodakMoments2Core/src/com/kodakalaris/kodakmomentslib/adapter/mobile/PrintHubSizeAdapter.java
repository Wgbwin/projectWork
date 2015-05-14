package com.kodakalaris.kodakmomentslib.adapter.mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.adapter.LinearListLayoutBaseAdapter;
import com.kodakalaris.kpp.PrintSize;
import com.kodakalaris.kpp.PrinterInfo;

public class PrintHubSizeAdapter extends LinearListLayoutBaseAdapter {
	private PrinterInfo mInfo;
	private Context mContext;
	private OnSizeClickListener mOnSizeClickListener;
	
	public PrintHubSizeAdapter(Context context, PrinterInfo info) {
		mContext = context;
		mInfo = info;
	}
	
	@Override
	public int getCount() {
		if (mInfo == null) {
			return 0;
		}
		
		return mInfo.getSupportedPrintSizes().length;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public PrintSize getItem(int position) {
		return mInfo.getSupportedPrintSizes()[position];
	}

	@Override
	public View getView(final int position) {
		final PrintSize size = getItem(position);
		final View v = LayoutInflater.from(mContext).inflate(R.layout.item_printhub_size, null);
		Button btn = (Button) v.findViewById(R.id.btn_size);
		btn.setText(size.getSize().name());
		
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mOnSizeClickListener != null) {
					mOnSizeClickListener.onClick(v, position, size);
				}
			}
		});
		return v;
	}
	
	public void setOnSizeClickListener(OnSizeClickListener onSizeClickListener) {
		mOnSizeClickListener = onSizeClickListener;
	}
	
	public static interface OnSizeClickListener {
		void onClick(View v, int position, PrintSize size);
	}

}
