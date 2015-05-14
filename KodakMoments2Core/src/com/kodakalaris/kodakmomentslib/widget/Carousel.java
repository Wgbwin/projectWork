package com.kodakalaris.kodakmomentslib.widget;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.kodakalaris.kodakmomentslib.AppConstants.ActivityState;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.adapter.CarouselAdapterWrapper;
import com.kodakalaris.kodakmomentslib.adapter.mobile.SimpleCarouselAdapter;
import com.kodakalaris.kodakmomentslib.interfaces.ActivityStateWatcher;

/**
 * Please do startAutoFlip() when activity.onStart() and stopAutoFlip when activity.onStop()
 * @author Robin QIAN
 *
 */
public class Carousel extends ViewPager{
	private static final long DEFAULT_FLIP_INTERVAL = 5000;
	private static final int PAGE_SCROLL_DURAIOTN = 1500;
	
	private CarouselAdapterWrapper mAdapterWrapper;
	private OnPageChangeListener mOnPageChangeListener;
	private long mFlipInterval = DEFAULT_FLIP_INTERVAL; // unit: millisecond
	private Context mContext;
	private boolean mVisible, mStartAutoFlip;
	private boolean mAutoFlipEnabled = true;
	private CarouselPageIndicator mPageIndicator;
	private int mPageIndicatorId;
	private CarouselScroller mCarouselScroller;
	
	private Runnable mAutoFlip = new Runnable() {
		
		@Override
		public void run() {
			toNextPage();
		}
	};

	public Carousel(final Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Carousel);
		mAutoFlipEnabled = a.getBoolean(R.styleable.Carousel_autoFlip, mAutoFlipEnabled);
		mFlipInterval = (long) (a.getFloat(R.styleable.Carousel_flipInterval, mFlipInterval/1000) * 1000);
		mPageIndicatorId = a.getResourceId(R.styleable.Carousel_pageIndicatorId, 0);
		a.recycle();
		
		init(context);
	}

	public Carousel(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		mContext = context;
		setPageScrollDuration(PAGE_SCROLL_DURAIOTN);
	}
	
	private void setPageScrollDuration(int duration) {
		try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = viewpager.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);
            mCarouselScroller = new CarouselScroller(getContext(),
                    (Interpolator) interpolator.get(null));
            scroller.set(this, mCarouselScroller);
            mCarouselScroller.setScrollDuration(duration);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	@Override
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mOnPageChangeListener = listener;
		super.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				if (getRealAdapter() != null && getRealAdapter().getCount() > 1) {
					startAutoFlip();
				}
				
				if (mPageIndicator != null) {
					mPageIndicator.setPosition(getRealPosition(position));
				}
				
				if (mOnPageChangeListener != null) {
					mOnPageChangeListener.onPageSelected(getRealPosition(position));
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if (mOnPageChangeListener != null) {
					mOnPageChangeListener.onPageScrolled(getRealPosition(position), positionOffset, positionOffsetPixels);
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				if (mOnPageChangeListener != null) {
					mOnPageChangeListener.onPageScrollStateChanged(state);
				}
			}
		});
		
		//for init, update the txt
		mOnPageChangeListener.onPageSelected(0);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			stopAutoFlip();
			break;
		case MotionEvent.ACTION_MOVE:
			if (mStartAutoFlip) {
				stopAutoFlip();
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			startAutoFlip();
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	@Override
	public int getCurrentItem() {
		if (mAdapterWrapper == null) {
			return super.getCurrentItem();
		} else {
			return mAdapterWrapper.toRealPosition(super.getCurrentItem());
		}
	}
	
	/**
	 * 
	 */
	public void toNextPage() {
		super.setCurrentItem(super.getCurrentItem() + 1);
	}
	
	public void toPreviousPage() {
		super.setCurrentItem(super.getCurrentItem() - 1);
	}
	
	/** 
	 * TODO have bugs in this method, it may cause main thread block
	 */
	@Override
	public void setCurrentItem(int item) {
		int offset = getFlipItemsOffset(item);
		if (offset == 0) {
			return;
		} else {
			super.setCurrentItem(super.getCurrentItem() + offset, true);
		}
		
	}
	
	/** 
	 * TODO have bugs in this method, it may cause main thread block
	 */
	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		if (mAdapterWrapper == null) {
			super.setCurrentItem(item, smoothScroll);
		} else {
			int offset = getFlipItemsOffset(item);
			if (offset == 0) {
				return;
			} else {
				super.setCurrentItem(super.getCurrentItem() + offset, smoothScroll);
			}
		}
	}
	
	private int getFlipItemsOffset(int item) {
		int outItem = super.getCurrentItem();
		
		int outOffset = outItem % mAdapterWrapper.getRealCount();
		int offset = item % mAdapterWrapper.getRealCount();
		
		return offset - outOffset;
	}
	
	private void updateFlipping() {
		removeCallbacks(mAutoFlip);
		if (mVisible && mStartAutoFlip && mAutoFlipEnabled && getRealAdapter() != null && getRealAdapter().getCount() > 1) {
			if (mContext instanceof ActivityStateWatcher) {
				ActivityState state = ((ActivityStateWatcher) mContext).getActivityState();
				if (state == ActivityState.STOPPED || state == ActivityState.DESTROYED) {
					mStartAutoFlip = false;
					return;
				}
			}
			postDelayed(mAutoFlip, mFlipInterval);
		}
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mVisible = false;
		
		updateFlipping();
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		mVisible = visibility == VISIBLE;
		
		updateFlipping();
	}
	
	public void startAutoFlip() {
		mStartAutoFlip = true;
		
		updateFlipping();
	}
	
	public void stopAutoFlip() {
		mStartAutoFlip = false;
		updateFlipping();
	}
	
	public void setAutoFlipEnabled(boolean enabled) {
		mAutoFlipEnabled = enabled;
	}
	
	/**
	 * Be careful, this method invode super.setCurrentItem(mAdapterWrapper.getFisrtPosition(), false);
	 * This may cause thread block if this view it is visible after activity resume(), I don't know the exact reason.
	 * */
	@Override
	public void setAdapter(PagerAdapter adapter) {
		if (adapter == null) {
			super.setAdapter(null);
			
			if (mAdapterWrapper != null) {
				PagerAdapter ad = mAdapterWrapper.getRealAdapter();
				if (ad instanceof SimpleCarouselAdapter) {
					((SimpleCarouselAdapter) ad).clearMemoryCache();
				}
			}
			
		} else {
			if (adapter.getCount() <= 1) {
				super.setAdapter(adapter);
			} else {
				mAdapterWrapper = new CarouselAdapterWrapper(adapter);
				super.setAdapter(mAdapterWrapper);
				super.setCurrentItem(mAdapterWrapper.getFisrtPosition(), false);
			}
			
			if (mPageIndicator == null && getContext() instanceof Activity) {
				//Becarefull, if do this Constuctor method, findViewById may return null
				//The reason is that CarouselPageIndicator view havn't been parse by app. 
				mPageIndicator = (CarouselPageIndicator) ((Activity) getContext()).findViewById(mPageIndicatorId);
				if (mPageIndicator != null) {
					setPageIndicator(mPageIndicator);
				}
			}
			
			if (mPageIndicator !=null) {
				mPageIndicator.setSize(getRealAdapter().getCount());
				mPageIndicator.setPosition(0);
			}
			
			startAutoFlip();
		}
	}
	
	
	
	public void setFlipInterval(long milliseconds) {
		mFlipInterval = milliseconds;
	}
	
	public PagerAdapter getRealAdapter() {
		if (mAdapterWrapper != null) {
			return mAdapterWrapper.getRealAdapter();
		} else {
			return getAdapter();
		}
	}
	
	private int getRealPosition(int position) {
		if (mAdapterWrapper == null) {
			return position;
		} else {
			return mAdapterWrapper.toRealPosition(position);
		}
	}
	
	public void setPageIndicator(CarouselPageIndicator pageIndicator) {
		mPageIndicator = pageIndicator;
	}
	
	/**
	 * The default scroller for viewpager is too fast , use this class to make carousel page switch speed slower 
	 * @author Robin QIAN
	 *
	 */
	private class CarouselScroller extends Scroller {
		private int mDuration = 1;

	    public CarouselScroller(Context context) {
	        super(context);
	    }

	    public CarouselScroller(Context context, Interpolator interpolator) {
	        super(context, interpolator);
	    }

	    @SuppressLint("NewApi")
	    public CarouselScroller(Context context, Interpolator interpolator, boolean flywheel) {
	        super(context, interpolator, flywheel);
	    }

	    /**
	     * Set the factor by which the duration will change
	     */
	    public void setScrollDuration(int duration) {
	    	mDuration = duration;
	    }

	    @Override
	    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
	        super.startScroll(startX, startY, dx, dy, mDuration);
	    }
	}
	
}
