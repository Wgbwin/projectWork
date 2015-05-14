package com.kodakalaris.kodakmomentslib.adapter;

import java.util.ArrayList;
import java.util.List;

import com.kodakalaris.kodakmomentslib.fragment.mobile.AlbumSelectFragment;
import com.kodakalaris.kodakmomentslib.fragment.mobile.PhotoSelectFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;
/**
 * the adapter of ViewPager in image selection screen
 * @author sunny
 *
 */
public class FragmentTabAdapter extends FragmentPagerAdapter {
	private final Context mContext;
	private List<FragmentInfo> mFragmentInfoes;
	public FragmentTabAdapter(FragmentActivity activity) {
		super(activity.getSupportFragmentManager());
		mContext = activity;
	}

	public FragmentTabAdapter(FragmentActivity activity,List<FragmentInfo> fragments){
		this(activity);
		mFragmentInfoes = fragments;
	}

	@Override
	public Fragment getItem(int position) {
		FragmentInfo info = mFragmentInfoes.get(position);
		Fragment fragment = Fragment.instantiate(mContext, info.getmFragmentName(), info.getBundle());

		return fragment;
	}

	@Override
	public int getCount() {
		if (mFragmentInfoes != null) {
			return mFragmentInfoes.size();
		} else {
			return 0;
		}
	}
	
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		FragmentInfo info = mFragmentInfoes.get(position);
		Fragment fragment  = (Fragment) super.instantiateItem(container, position);
		if(position==0){
			PhotoSelectFragment photoSelectFragment = (PhotoSelectFragment) fragment;
			photoSelectFragment.setContext(mContext);
			photoSelectFragment.setBundle(info.getBundle());
			
		} else if(position==1){
			AlbumSelectFragment albumFragment = (AlbumSelectFragment) fragment;
			albumFragment.setBundle(info.getBundle());
		}
		return fragment;
	}
	
	
	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return PagerAdapter.POSITION_NONE;
	}
	@Override
	public CharSequence getPageTitle(int position) {
		
		return mFragmentInfoes.get(position).getmPageTitle();
	}
	
	public void addFragmentTabs(Class<?> clss, String pageTitle,Bundle args) {
		FragmentInfo fragmentInfo = new FragmentInfo(clss.getName(), pageTitle,args);
		if (mFragmentInfoes == null) {
			mFragmentInfoes = new ArrayList<FragmentInfo>();
		}
		mFragmentInfoes.add(fragmentInfo);

//		notifyDataSetChanged();
	}
	
	public List<FragmentInfo> getmFragmentInfoes() {
		return mFragmentInfoes;
	}

	public void setmFragmentInfoes(List<FragmentInfo> mFragmentInfoes) {
		this.mFragmentInfoes = mFragmentInfoes;
	}

	
	
	public static class FragmentInfo {
		private String mFragmentName;
		private Bundle bundle;
		private String mPageTitle;

		

		public FragmentInfo(String mFragmentName,String mPageTitle, Bundle bundle) {
			super();
			this.mFragmentName = mFragmentName;
			this.mPageTitle = mPageTitle;
			this.bundle = bundle;
		}

		public String getmFragmentName() {
			return mFragmentName;
		}

		public void setmFragmentName(String mFragmentName) {
			this.mFragmentName = mFragmentName;
		}

		public Bundle getBundle() {
			return bundle;
		}

		public void setBundle(Bundle bundle) {
			this.bundle = bundle;
		}
		
		public String getmPageTitle() {
			return mPageTitle;
		}

		public void setmPageTitle(String mPageTitle) {
			this.mPageTitle = mPageTitle;
		}

		@Override
		public String toString() {
			return mFragmentName;
		}

	}
	
	
}
