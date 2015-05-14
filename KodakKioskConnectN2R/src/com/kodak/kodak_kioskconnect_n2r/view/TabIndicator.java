package com.kodak.kodak_kioskconnect_n2r.view;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import java.util.List;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;

public class TabIndicator extends HorizontalScrollView {
	private int[] mTabStringResIds;
	private String[] mTabStrings ;
	private List<TabView> tabViews;
	private LinearLayout vTabLayout;
	private int mMaxTabWidth;
	private int mSelectedTabIndex;
//	private OnTabReselectedListener mTabReselectedListener;
	private ITabSelectListener mTabSelectListener ;
	public interface ITabSelectListener {
		 public void onTabSelected(TabView tab);
//		 public void onTabUnselected(TabView tab);
		 public void onTabReselected(TabView tab );
	}

	

	/**
	 * Interface for a callback when the selected tab has been reselected.
	 */
	public interface OnTabReselectedListener {
		/**
		 * Callback when the selected tab has been reselected.
		 * 
		 * @param position
		 *            Position of the current center item.
		 */
		void onTabReselected(int position);
	}

	private Runnable mTabSelector;
	private final OnClickListener mTabClickListener = new OnClickListener() {
		public void onClick(View view) {
			TabView tabView = (TabView) view;
			
			// TODO
			final int oldSelected = mSelectedTabIndex;
			final int newSelected = tabView.getIndex();
			setCurrentItem(newSelected);
			
			if (oldSelected == newSelected && mTabSelectListener != null) {
				mTabSelectListener.onTabReselected(tabView);
			}else {
				mTabSelectListener.onTabSelected(tabView) ;
				
			}
		}
	};

	public TabIndicator(Context context) {
		this(context, null);

	}

	public TabIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		setHorizontalScrollBarEnabled(false);
		vTabLayout = new LinearLayout(getContext());
		addView(vTabLayout, new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
	}
	
	
	public void setOnTabSelectListener(ITabSelectListener listener){
		mTabSelectListener = listener ;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final boolean lockedExpanded = widthMode == MeasureSpec.EXACTLY;
		setFillViewport(lockedExpanded);

		final int childCount = vTabLayout.getChildCount();
		if (childCount > 1 && (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST)) {
			if (childCount > 2) {
				mMaxTabWidth = (int) (MeasureSpec.getSize(widthMeasureSpec) * 0.4f);
			} else {
				mMaxTabWidth = MeasureSpec.getSize(widthMeasureSpec) / 2;
			}
		} else {
			mMaxTabWidth = -1;
		}

		final int oldWidth = getMeasuredWidth();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int newWidth = getMeasuredWidth();

		if (lockedExpanded && oldWidth != newWidth) {
			// Recenter the tab display if we're at a new (scrollable) size.
			setCurrentItem(mSelectedTabIndex);
		}
	}

	public void setCurrentItem(int item) {

		mSelectedTabIndex = item;

		final int tabCount = vTabLayout.getChildCount();
		for (int i = 0; i < tabCount; i++) {
			final View child = vTabLayout.getChildAt(i);
			final boolean isSelected = (i == item);
			child.setSelected(isSelected);
			if (isSelected) {
				animateToTab(item);
			}
		}
	}
	
	public int getCurrentItem(){
		return mSelectedTabIndex ;
	}

	private void animateToTab(final int position) {
		final View tabView = vTabLayout.getChildAt(position);
		if (mTabSelector != null) {
			removeCallbacks(mTabSelector);
		}
		mTabSelector = new Runnable() {
			public void run() {
				final int scrollPos = tabView.getLeft() - (getWidth() - tabView.getWidth()) / 2;
				smoothScrollTo(scrollPos, 0);
				mTabSelector = null;
			}
		};
		post(mTabSelector);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mTabSelector != null) {
			// Re-post the selector we saved
			post(mTabSelector);
		}
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mTabSelector != null) {
			removeCallbacks(mTabSelector);
		}
	}

	public void setTabStringResIds(int[] mTabStringResIds) {
		this.mTabStringResIds = mTabStringResIds;
		notifyDataSetChanged();
	}
	
	public void setTabStringResStrings(String[] mTabStrings ){
		this.mTabStrings = mTabStrings ;
		notifyDataSetChanged() ;
	}

	public void notifyDataSetChanged() {
		vTabLayout.removeAllViews();
		if (mTabStringResIds != null && mTabStringResIds.length > 0) {
			int count = mTabStringResIds.length;
			for (int i = 0; i < count; i++) {
				addTab(i, mTabStringResIds[i]);
			}

		}else if(mTabStrings!=null && mTabStrings.length >0 ){
			int count = mTabStrings.length;
			for (int i = 0; i < count; i++) {
				addTab(i, mTabStrings[i]);
			}
		}
		requestLayout();

	}

//	public void addTab(TabView tabView){
//		
//		
//		vTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, MATCH_PARENT, 1));
//	}
	
	private void addTab(int index, int stringResId) {
		final TabView tabView = new TabView(getContext());
		tabView.mIndex = index;
		
		tabView.setFocusable(true);
		tabView.setOnClickListener(mTabClickListener);
		tabView.setText(stringResId);

		vTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, MATCH_PARENT, 1));
	}
	
	private void addTab(int index, String tabString) {
		final TabView tabView = new TabView(getContext());
		tabView.mIndex = index;
		
		tabView.setFocusable(true);
		tabView.setOnClickListener(mTabClickListener);
		tabView.setText(tabString);

		vTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, MATCH_PARENT, 1));
	}

	public class TabView extends TextView {
		private int mIndex;
		public TabView(Context context) {
			super(context, null, R.attr.tabIndicatorStyle);
		}

		@Override
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			// Re-measure if we went beyond our maximum size.
			if (mMaxTabWidth > 0 && getMeasuredWidth() > mMaxTabWidth) {
				super.onMeasure(MeasureSpec.makeMeasureSpec(mMaxTabWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
			}
		}

		public int getIndex() {
			return mIndex;
		}

	}

}
