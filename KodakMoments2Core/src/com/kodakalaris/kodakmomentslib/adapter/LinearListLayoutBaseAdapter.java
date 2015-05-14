package com.kodakalaris.kodakmomentslib.adapter;

import com.kodakalaris.kodakmomentslib.widget.LinearListLayout;

import android.view.View;

/**
 * A simple Adapter for {@link LinearListLayout}
 * @author Robin QIAN
 *
 */
public abstract class LinearListLayoutBaseAdapter {
	public abstract int getCount();
	
	public abstract long getItemId(int position);
	
	public abstract Object getItem(int position);
	
	public abstract View getView(int position);
	
	public boolean isEmpty() {
		return getCount() == 0;
	}
}
