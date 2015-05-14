package com.kodakalaris.kodakmomentslib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.kodakalaris.kodakmomentslib.adapter.LinearListLayoutBaseAdapter;

/**
 * A simple LinearLayout for list(It is similar with ListView, but this can be put into ScrollView or other scroll widget), it can be horizontal or vertical. 
 * Be careful , this view don't support viewHolder.
 * If you have many items in list, it is better to use another view(ex: ListView)
 * @author Robin QIAN
 *
 */
public class LinearListLayout extends LinearLayout{
	private LinearListLayoutBaseAdapter mAdapter;
	private OnItemClickListener mOnItemClickListener;
	
	public LinearListLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public LinearListLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LinearListLayout(Context context) {
		super(context);
		init(context);
	}
	
	public void init(Context context) {
		//Now nothing to do
	}
	
	public void setAdapter(LinearListLayoutBaseAdapter adapter) {
		this.mAdapter = adapter;
		bindView();
	}
	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.mOnItemClickListener = onItemClickListener;
	}
	
	public void bindView() {
		if (mAdapter == null || mAdapter.isEmpty()) {
			return;
		}
		
		for (int i = 0; i < mAdapter.getCount(); i++) {
			final int position = i;
			final View v = mAdapter.getView(position);
			
			v.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mOnItemClickListener != null) {
						mOnItemClickListener.onItemClick(LinearListLayout.this, v, position, mAdapter.getItemId(position));
					}
				}
			});
			
			addView(v);
		}
		
	}
	
	public interface OnItemClickListener {
		void onItemClick(LinearListLayout parent, View v, int position, long id);
	}
}
