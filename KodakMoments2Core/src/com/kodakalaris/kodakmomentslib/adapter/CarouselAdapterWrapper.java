package com.kodakalaris.kodakmomentslib.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class CarouselAdapterWrapper extends PagerAdapter{
	private PagerAdapter mAdapter;
	
	public CarouselAdapterWrapper(PagerAdapter adapter) {
		mAdapter = adapter;
	}
	
	@Override
	public int getCount() {
		return Integer.MAX_VALUE;
	}
	
	public int getRealCount() {
		return mAdapter.getCount();
	}
	
	public PagerAdapter getRealAdapter() {
		return mAdapter;
	}
	
	public int toRealPosition(int position) {
		return position % getRealCount();
	}
	
	public int getFisrtPosition() {
		int mid = getCount() / 2;
		return mid - mid % getRealCount();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return mAdapter.isViewFromObject(view, object);
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		return mAdapter.instantiateItem(container, toRealPosition(position));
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		mAdapter.destroyItem(container, toRealPosition(position), object);
	}
	
	
}
