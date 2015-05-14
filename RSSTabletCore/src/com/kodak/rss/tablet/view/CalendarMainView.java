package com.kodak.rss.tablet.view;

import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.calendar.CalendarGridItemPO;
import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.MainPageView.OnLayerClickListener;
import com.kodak.rss.tablet.view.MainPageView.OnLayerDragListener;

public class CalendarMainView extends RelativeLayout implements DragTarget{
	private static final String TAG = "CalendarMainView";
	
	public static final int PAGE_POSITION_TOP = 0;
	public static final int PAGE_POSITION_BOTTOM = 1;
	
	private Context mContext;
	private CalendarEditActivity mActivity;
	private Calendar mCalendar;
	private CalendarMainPageView[] mPageViews = new CalendarMainPageView[2];
	private int mPosition = 0;
	private int mMaxWidth, mMaxHeight;
	private Point mWindowSize;
	
	
	private int mPagePosition = PAGE_POSITION_TOP;
	
	/** the rect for this view when not zoom in */
	private Rect mRect4LastNormalLayout;
	private Rect mRect4ZoomInLayoutOriginal;
	private Rect mRect4NormalLayoutOriginal;
	
	private int mOriginalOrderIndex;
	private boolean mIsZoomIn;
	private boolean mIsEditMode;
	
	public LruCache<String, Bitmap> mMemoryCache;
	private Bitmap mWaitBitmap;	
	private ImageUseURIDownloader imageDownloader;
	private Map<String, Request> pendingRequests;
	private onProcessImageResponseListener onResponseListener = new onProcessImageResponseListener() {		
		@Override
		public void onProcess(Response response, String profileId, View view,int position,String flowTpye,String productId) {			
			if (response == null || pendingRequests == null) return;
			int refreshCount = response.getRequest().getRefreshCount();			
			MemoryCacheUtil.removeBitmap(mMemoryCache, profileId);	
			if (response.getError() == null) {
				Bitmap bitmap = response.getBitmap();
				MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);	
				if (bitmap != null) {
					CalendarUtil.refreshSucPageInCalendar(profileId, refreshCount);	
				}
				Bitmap mContentBitmap = bitmap == null ? mWaitBitmap : bitmap;
				if (mActivity != null && !mActivity.isFinishing()) {
					if (view != null && view instanceof CalendarMainPageView) {						
						if (isCurrentViewBitmap(profileId)) {
							((CalendarMainPageView)view).setImageBitmap(mContentBitmap);
							view.postInvalidate();	
						}
						mActivity.synAdapterNotifyDataSet();
					}				
				}
			}							
		}
	};
	
	public CalendarMainView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CalendarMainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CalendarMainView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		mContext = context;
		mActivity = (CalendarEditActivity) context;
		inflate(context, R.layout.calendar_main_view, this);
		mPageViews[0] = (CalendarMainPageView) findViewById(R.id.pv_1);
		mPageViews[1] = (CalendarMainPageView) findViewById(R.id.pv_2);
		
		Bitmap waitBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_wait232x174);
		mPageViews[0].setWaitBitmap(waitBitmap);
		mPageViews[1].setWaitBitmap(waitBitmap);
		
		setVisibility(View.INVISIBLE);
		
		//init temp for avoid null pointerException when setCalendar, it will reset in onSizechanged
		mWindowSize = new Point();
		mActivity.getWindowManager().getDefaultDisplay().getSize(mWindowSize);
	}
	
	public void setCalendar(Calendar calendar,int downW,int downH,LruCache<String, Bitmap> mMemoryCache,Map<String, Request> pendingRequests) {
		mCalendar = calendar;
		this.mMemoryCache = mMemoryCache;
		this.pendingRequests = pendingRequests;
		
		imageDownloader = new ImageUseURIDownloader(mContext,pendingRequests);	
		imageDownloader.setSaveType(FilePathConstant.calendarType);	
		imageDownloader.setIsThumbnail(false);
		imageDownloader.setOnProcessImageResponseListener(onResponseListener);
			
		mPageViews[0].setDownParameter(downW, downH, mMemoryCache,imageDownloader);
		mPageViews[1].setDownParameter(downW, downH, mMemoryCache,imageDownloader);
		
		if (isSinglePage()) {
			mPageViews[1].setVisibility(View.GONE);
			LayoutParams params = (LayoutParams) getLayoutParams();
			params.bottomMargin = getBottomPanelDefaultHeight() + DimensionUtil.dip2px(mContext, 12);
		}
		
		requestLayout();
		notifyDataSetChanged();
	}
	
	boolean mMeasured4Calendar;
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int w = MeasureSpec.getSize(widthMeasureSpec);
		int h = MeasureSpec.getSize(heightMeasureSpec);
		
		//TODO In some old android version(4.1, 4.2.?), the w , h is not equal to params width and heith(don't know why)
		//It will caused that when it enter layer edit mode, the image is not full
		//so we need to check it
		if (mIsEditMode) {
			LayoutParams params = (LayoutParams) getLayoutParams();
			if (w < params.width) {
				w = params.width;
			}
			
			if (h < params.height) {
				h = params.height;
			}
		}
		
		if (mCalendar != null) {
			CalendarPage[] pages = getCurrentPages();
			float pw = pages[0].width;
			float ph = isSinglePage() ? pages[0].height : pages[0].height * 2;
			
			if (pw == 0 || ph == 0) {
				return;
			}
			
			
			if (mIsZoomIn) {
				w = (int) (h * pw / ph);
			} else if (mMaxWidth != 0 && mMaxHeight != 0) {
				if ((float)mMaxWidth / mMaxHeight > pw / ph) {
					if (h > mMaxHeight) {
						h = mMaxHeight;
					}
					
					w = (int) (h * pw / ph);
				} else {
					if (w > mMaxWidth) {
						w = mMaxWidth;
					}
					
					h = (int) (w * ph / pw);
				}
				
				mMeasured4Calendar = true;
				if (getVisibility() != View.VISIBLE ) {
					setVisibility(View.VISIBLE);
				}
			}
		}
		
		super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
	}
	
	private boolean mInited;
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		if (w != 0 && h != 0 && !mInited) {
			mInited = true;
			mMaxWidth = w;
			mMaxHeight = h;
			
			mOriginalOrderIndex = getOrderIndex();
			
			View parent = (View) getParent();
			mWindowSize = new Point(parent.getWidth(), parent.getHeight());
			
			requestLayout();
		}
		
		if (mMeasured4Calendar) {
			if (mRect4LastNormalLayout == null) {
				mRect4LastNormalLayout = new Rect(getLeft(), getTop(), getRight(), getBottom());
			}
			
			if (mRect4NormalLayoutOriginal == null) {
				mRect4NormalLayoutOriginal = new Rect(mRect4LastNormalLayout);
			}
			
			if (mRect4ZoomInLayoutOriginal == null) {
				int rw = getWidthByHeight(mWindowSize.y);
				int rh = mWindowSize.y;
				int left = (mWindowSize.x - rw) / 2;
				mRect4ZoomInLayoutOriginal = new Rect(left, 0, left + rw, rh);
			}
			
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mRect4LastNormalLayout!= null && !mIsZoomIn) {
			mRect4LastNormalLayout.set(l, t, r, b);
		}
		
	}
	
	private LayoutParams generateOriginalParams() {
		//TODO: not very good, if you change properties in xml, please change code here
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.setMargins(0, mRect4LastNormalLayout.top, 0, mWindowSize.y - mRect4LastNormalLayout.bottom);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		
		return params;
	}
	
	private int getWidthByHeight(int h) {
		CalendarPage[] pages = getCurrentPages();
		float pw = pages[0].width;
		float ph = isSinglePage() ? pages[0].height : pages[0].height * 2;
		
		return (int) (h * pw / ph);
	}
	
	private int getHeightByWidth(int w) {
		CalendarPage[] pages = getCurrentPages();
		float pw = pages[0].width;
		float ph = isSinglePage() ? pages[0].height : pages[0].height * 2;
		
		return (int) (w * ph / pw);
	}
	
	private int getOrderIndex() {
		RelativeLayout parent = (RelativeLayout) getParent();
		
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (parent.getChildAt(i) == this) {
				return i;
			}
		}
		
		return 0;
	}
	
	public void setOnLayerDragListener(OnLayerDragListener<CalendarMainPageView, CalendarPage, CalendarLayer> onLayerDragListener) {
		mPageViews[0].setOnLayerDragListener(onLayerDragListener);
		mPageViews[1].setOnLayerDragListener(onLayerDragListener);
	}
	
	public void setOnLayerClickListener(OnLayerClickListener<CalendarMainPageView, CalendarPage, CalendarLayer> onLayerClickListener) {
		mPageViews[0].setOnLayerClickListener(onLayerClickListener);
		mPageViews[1].setOnLayerClickListener(onLayerClickListener);
	}
	
	public int getPagePosition() {
		return mPagePosition;
	}
	
	/**
	 * @param dstRect
	 */
	private void animateTo(Rect dstRect, boolean centerHonrizontal, AnimationListener listener) {
		Rect currentRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
		
		moveTo(dstRect, centerHonrizontal);
		
		AnimationSet animSet = new AnimationSet(true);
		
		if (currentRect.height() != dstRect.height()) {
			float scale = (float)currentRect.height() / dstRect.height();
			ScaleAnimation sa = new ScaleAnimation(scale, 1, scale, 1);
			animSet.addAnimation(sa);
		}
		
		int dy = currentRect.top - dstRect.top;
		int dx = currentRect.left - dstRect.left;
		if (dx != 0 || dy != 0 ) {
			TranslateAnimation ta = new TranslateAnimation(dx, 0, dy, 0);
			animSet.addAnimation(ta);
		}
		
		animSet.setDuration(500);
		
		if (listener != null) {
			animSet.setAnimationListener(listener);
		}
		
		startAnimation(animSet);
	}
	
	private void moveTo(Rect dstRect, boolean centerHonrizontal) {
		LayoutParams params = (LayoutParams) getLayoutParams();
		params.topMargin = dstRect.top;
		params.bottomMargin = mWindowSize.y - dstRect.bottom;
		params.height = dstRect.height();
		params.width = dstRect.width();
		
		if (centerHonrizontal) {
			params.addRule(CENTER_HORIZONTAL);
			params.leftMargin = 0;
			params.rightMargin = 0;
		} else {
			params.addRule(CENTER_HORIZONTAL, 0);
			params.leftMargin = dstRect.left;
			params.rightMargin = 0;
		}
		requestLayout();
	}
	
	public void moveToTop() {
		if (mIsZoomIn) {
			return;
		}
		
		if (mPagePosition == PAGE_POSITION_TOP || isSinglePage()) {
			return;
		}
		
		mPagePosition = PAGE_POSITION_TOP;
		
		animateTo(mRect4NormalLayoutOriginal, true, null);
		
	}
	
	public void moveToBottom() {
		if (mIsZoomIn) {
			return;
		}
		
		if (mPagePosition == PAGE_POSITION_BOTTOM || isSinglePage()) {
			return;
		}
		
		mPagePosition = PAGE_POSITION_BOTTOM;
		
		animateTo(getRectWhenMoveToBottom(), true, null);
	}
	
	public void zoomIn() {
		zoomIn(null);
	}
	
	public void zoomIn(AnimationListener listener) {
		mIsZoomIn = true;
		
		mActivity.calendarZoomInLayer.setVisibility(View.VISIBLE);
		mActivity.exitZoomInButton.setVisibility(View.VISIBLE);
		bringToTop();
		((CalendarEditActivity) mContext).zoomButton.setVisibility(View.INVISIBLE);
		
		animateTo(mRect4ZoomInLayoutOriginal, true, listener);
	}
	
	public void zoomOut() {
		zoomOut(null);
	}
	
	public void zoomOut(final AnimationListener listener) {
		animateToNormal(listener);
	}
	
	private void animateToNormal(final AnimationListener listener) {
		mIsZoomIn = false;
		
		RelativeLayout parent = (RelativeLayout) getParent();
		parent.removeView(CalendarMainView.this);
		parent.addView(CalendarMainView.this, mOriginalOrderIndex);
		parent.requestLayout();
		parent.invalidate();
		
		mActivity.zoomButton.setVisibility(View.VISIBLE);
		mActivity.calendarZoomInLayer.setVisibility(View.INVISIBLE);
		mActivity.exitZoomInButton.setVisibility(View.INVISIBLE);
		
		animateTo(mRect4LastNormalLayout, true, new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				if (listener != null) {
					listener.onAnimationStart(animation);
				}
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				if (listener != null) {
					listener.onAnimationRepeat(animation);
				}
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mActivity.zoomButton.setVisibility(View.VISIBLE);
				mActivity.vgEditTools.setVisibility(mCalendar.getCalendarType() == Calendar.Annual_Calendars ? View.INVISIBLE : View.VISIBLE);
				if (listener != null) {
					listener.onAnimationEnd(animation);
				}
			}
		});
	}
	
	/**
	 * Move to top left for text edit
	 */
	public void moveToTopLeft(CalendarPage page, AnimationListener listener) {
		mIsZoomIn = true;
		Rect dstRect = getRectWhenMoveToTopLeft(page);
		
		mActivity.zoomButton.setVisibility(View.INVISIBLE);
		mActivity.vgEditTools.setVisibility(View.INVISIBLE);
		mActivity.exitZoomInButton.setVisibility(View.INVISIBLE);
		mActivity.calendarZoomInLayer.setVisibility(View.VISIBLE);
		
		bringToTop();
		
		animateTo(dstRect, false, listener);
	}
	
	/**
	 * Move to top left for text edit
	 */
	public void moveToTopLeft(CalendarPage page) {
		moveToTopLeft(page, null);
	}
	
	public void moveToCenter(AnimationListener listener) {
		animateToNormal(listener);
	}
	
	public void moveToCenter() {
		moveToCenter(null);
	}
	
	private int getBottomPanelDefaultHeight() {
		return mWindowSize.y / 3 + DimensionUtil.dip2px(mContext, 60);
	}
	
	/**
	 * For normal state, not zoom in state
	 * @return Rect
	 */
	private Rect getRectWhenMoveToBottom() {
		int bottom = mWindowSize.y - getBottomPanelDefaultHeight();
		int dy = bottom - mRect4NormalLayoutOriginal.bottom;
		Rect dstRect = new Rect(mRect4NormalLayoutOriginal);
		
		dstRect.offset(0, dy);
		
		return dstRect;
	}
	
	private Rect getRectWhenMoveToTopLeft(CalendarPage page) {
		boolean isTop = isTopPage(page);
		//max rect for scale the pageview
		Rect maxRect = new Rect();
		maxRect.left = 0;
		maxRect.top = 0;
		maxRect.right = mWindowSize.x / 2;
		maxRect.bottom = mWindowSize.y / 2  - DimensionUtil.dip2px(mContext, 40);
		
		Rect oldPageRect = isTop ? getLayoutRect(mPageViews[0]) : getLayoutRect(mPageViews[1]);
		Rect newPageRect = new Rect();
		if ((float)oldPageRect.width() / oldPageRect.height() > (float)maxRect.width() / maxRect.height()) {
			int newW = maxRect.width();
			int newH = newW * oldPageRect.height() / oldPageRect.width();
			newPageRect.left = maxRect.left;
			newPageRect.right = newPageRect.left + newW;
			newPageRect.top = maxRect.centerY() - newH / 2;
			newPageRect.bottom = newPageRect.top + newH;
		} else {
			int newH = maxRect.height();
			int newW = newH * oldPageRect.width() / oldPageRect.height();
			newPageRect.left = maxRect.centerX() - newW / 2;
			newPageRect.right = newPageRect.left + newW;
			newPageRect.top = maxRect.top;
			newPageRect.bottom = newPageRect.top + newH;
		}
		
		Rect result = new Rect(newPageRect);
		if (isSinglePage()) {
			//do nothing
		} else if (isTop) {
			result.bottom = result.bottom + result.height();
		} else {
			result.top = result.top - result.height();
		}
		return result;
	}
	
	private Rect getLayoutRect(View v) {
		Rect rect = new Rect();
		View parent = (View) getParent();
		
		int[] locParent = new int[2];
		parent.getLocationInWindow(locParent);
		int[] locView = new int[2];
		v.getLocationInWindow(locView);
		
		rect.left = locView[0] - locParent[0];
		rect.top = locView[1] - locView[1];
		rect.right = rect.left + v.getWidth();
		rect.bottom = rect.top + v.getHeight();
		
		return rect;
	}
	
	private boolean isTopPage(CalendarPage page) {
		if (isSinglePage()) {
			return true;
		} else {
			if (page.id.equals(mPageViews[0].getPage().id)) {
				return true;
			} else if (mPageViews[1] != null && mPageViews[1].getPage().id.equals(page.id)){
				return false;
			}
		}
		
		return mPagePosition == PAGE_POSITION_TOP;
	}
	
	public void enterLayerEditMode(CalendarMainPageView pageView, CalendarLayer layer) {
		mIsZoomIn = true;
		mIsEditMode = true;
		//get rect for layer
		RectF layerRect = pageView.getLayerRect(layer);
		
		//the max rect for the layer can scale
		int popWidth = mActivity.calendarEditLayer.getEditPopViewWidth();
		RectF maxRect = new RectF(popWidth + 10, mWindowSize.y / 6, mWindowSize.x - popWidth - 10, mWindowSize.y - mWindowSize.y / 6);
		
		//get new Rect for layer
		RectF newRect = new RectF();
		if (layerRect.width() / layerRect.height() > maxRect.width() / maxRect.height()) {
			float w = maxRect.width();
			float h = w * layerRect.height() / layerRect.width();
			newRect.left = maxRect.left;
			newRect.right = newRect.left + w;
			newRect.top = maxRect.centerY() - h / 2;
			newRect.bottom = newRect.top + h;
		} else {
			float h = maxRect.height();
			float w = h * layerRect.width() / layerRect.height();
			newRect.left = maxRect.centerX() - w / 2;
			newRect.right = newRect.left + w;
			newRect.top = maxRect.top;
			newRect.bottom = newRect.top + h;
		}
		
		float scale = newRect.width() / layerRect.width();
		
		bringToTop();
//		new params for calendar view
		LayoutParams params = (LayoutParams) getLayoutParams();
		int w = (int) (getWidth() * scale );
		int h = (int) (getHeight() * scale);
		params.width = w;
		params.height = h;
		params.leftMargin = (int) (newRect.left - layerRect.left * scale);
		params.rightMargin = mWindowSize.x - params.leftMargin -w;
		
		if (pageView == mPageViews[0]) {
			params.topMargin = (int) (newRect.top - layerRect.top * scale);
		} else {
			params.topMargin = (int) (newRect.top - layerRect.top * scale - h / 2);
		}
		params.bottomMargin = mWindowSize.y - params.topMargin - h;
		params.addRule(CENTER_HORIZONTAL, 0);
		requestLayout();
		invalidate();
		
		mActivity.calendarZoomInLayer.setVisibility(View.VISIBLE);
		mActivity.exitZoomInButton.setVisibility(View.INVISIBLE);
	}
	
	public void exitEditMode(AnimationListener listener) {
		if (mIsEditMode) {
			mIsEditMode = false;
			zoomOut(listener);
		}
	}
	
	public void exitEditMode() {
		if (mIsEditMode) {
			mIsEditMode = false;
			zoomOut(null);
		}
	}
	
	public void enterAddResouceMode() {
		zoomIn();
		mActivity.exitZoomInButton.setVisibility(View.INVISIBLE);
	}
	
	public void exitAddResouceMode() {
		zoomOut();
	}
	
	/**
	 * Not really top layer, it will below some special layer for edit
	 */
	private void bringToTop() {
		RelativeLayout parent = (RelativeLayout) getParent();
		
		int index = 0;
		
		View v = mActivity.findViewById(R.id.calendar_top_layer);
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (parent.getChildAt(i) == v) {
				index = i;
				break;
			}
		}
		
		parent.removeView(this);
		parent.addView(this, index);
		parent.requestLayout();
		parent.invalidate();
	}
	
	@Override
	public void hideAllFrames() {
		if (mPageViews[0] != null) {
			mPageViews[0].hideAllFrames();
		}
		
		if (mPageViews[1] != null) {
			mPageViews[1].hideAllFrames();
		}
	}
	
	/**
	 * @param xOnScreen
	 * @param yOnScreen
	 * @return Object[]{new Object[]{CalendarMainPageView, CalendarPage, CalendarLayer, CalendarGridItemPO};}
	 */
	public Object[] pointToPosition(float xOnScreen, float yOnScreen) {
		if (mCalendar == null || mPageViews == null) {
			return null;
		}
		
		for (int i = 0 ; i < mPageViews.length; i++) {
			CalendarMainPageView pageView = mPageViews[i];
			if (pageView != null && pageView.getVisibility() ==View.VISIBLE) {
				int[] location = new int[2];
				pageView.getLocationOnScreen(location);
				float relativeX = xOnScreen - location[0];
				float relativeY = yOnScreen - location[1];
				
				//if the point is in this pageView
				if (relativeX >= 0 && relativeY >= 0 && relativeX <= pageView.getWidth() && relativeY <= pageView.getHeight()) {
					Pair<CalendarLayer, CalendarGridItemPO> pair = pageView.pointTo(relativeX, relativeY);
					if (pair == null) {
						return new Object[]{pageView, pageView.getPage(), null, null};
					} else {
						return new Object[]{pageView, pageView.getPage(), pair.first, pair.second}; 
					}
				}
				
			}
		}
		
		return null;
	}
	
	public void setPosition(int position) {
		switch (mCalendar.getCalendarType()) {
		case Calendar.Annual_Calendars:
			//only one page, so do nothing
			break;
		case Calendar.Monthly_Simplex:
			if (position >= 0 && position < mCalendar.pages.size()) {
				mPosition = position;
				notifyDataSetChanged();
				mActivity.setCPAdapterCurPosition(position);
			}
			
			break;
		case Calendar.Monthly_Duplex:
			if (position < 0 || position * 2 > mCalendar.pages.size()) {
				return;
			}
			
			mPosition = position;
			notifyDataSetChanged();
			mActivity.setCPAdapterCurPosition(position);
			
			if ((mPosition == 0 || mPosition * 2 == mCalendar.pages.size())&& mPagePosition == PAGE_POSITION_BOTTOM) {
				moveToTop();
			}
			break;
		}
	}
	
	public int getPosition() {
		return mPosition;
	}	
	
	public void refresh(Calendar calendar) {
		mCalendar = calendar;				
		if (isSinglePage()) {
			mPageViews[1].setVisibility(View.GONE);
		}		
		notifyDataSetChanged();
	}
	
	public CalendarPage[] getCurrentPages() {
		CalendarPage[] pages = null;
		switch (mCalendar.getCalendarType()) {
		case Calendar.Annual_Calendars:
			pages = new CalendarPage[1];
			pages[0] = mCalendar.pages.get(0);
			break;
		case Calendar.Monthly_Simplex:
			pages = new CalendarPage[1];
			pages[0] = mCalendar.pages.get(mPosition);
			break;
		case Calendar.Monthly_Duplex:
			pages = new CalendarPage[2];
			if (mPosition == 0) {
				pages[0] = mCalendar.pages.get(0);
				pages[1] = null;
			} else if (mPosition * 2 == mCalendar.pages.size()) {
				pages[0] = mCalendar.pages.get(mPosition * 2 -1);
				pages[1] = null;
			} else {
				pages[0] = mCalendar.pages.get(mPosition * 2 -1);
				pages[1] = mCalendar.pages.get(mPosition * 2);
			}
			break;
		default:
			Log.e(TAG, "Error: Unknown calendar type!");
			break;
		}
		
		return pages;
	}
	
	
	public CalendarPage getCurrentFocusedPage() {
		if (isSinglePage()) {
			return mPageViews[0].getPage();
		} else {
			return mPageViews[mPagePosition == PAGE_POSITION_TOP ? 0 : 1].getPage();
		}
		
		
	}
	
	public void showFrame(CalendarPage page, CalendarLayer layer) {
		CalendarMainPageView pageView = findPageView(page);
		
		if (pageView != null) {
			if (layer == null) {
				pageView.showFrame();
			} else {
				pageView.showFrame(layer);
			}
		}
	}
	
	public CalendarMainPageView findPageView(CalendarPage page) {
		for (int i = 0; i < mPageViews.length; i++) {
			if (mPageViews[i] != null && mPageViews[i].getPage() != null && mPageViews[i].getPage().id.equals(page.id)) {
				return mPageViews[i];
			}
		}
		return null;
	}
	
	public void notifyDataSetChanged() {
		if (isSinglePage()) {
			mPageViews[0].setPage(getCurrentPages()[0]);
			mPageViews[0].postInvalidate();
		} else {
			CalendarPage[] pages = getCurrentPages();
			mPageViews[0].setPage(pages[0]);
			mPageViews[1].setPage(pages[1]);
			
			mPageViews[0].postInvalidate();
			mPageViews[1].postInvalidate();
		}
		
		postInvalidate();
	}
	
	public void synNotifyDataSet(int index){		
		mPageViews[index].setImageBitmap(null);
		mPageViews[index].postInvalidate();
	}
	
	private float mXDown;
	private float mYDown;
	private final int PAN_SLOP = 50;
	private boolean mIsReleased = false;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mXDown = ev.getX();
			mYDown = ev.getY();
			mIsReleased = false;
			break;
		case MotionEvent.ACTION_MOVE:
			//check if is dragging
			if (mPageViews[0].isDraggingLayer() || mPageViews[1].isDraggingLayer()) {
				return false;
			}
			
			if (mIsReleased) {
				return true;
			} 
			
			//check pan gesture
			float panX = ev.getX() - mXDown;
			float panY = ev.getY() - mYDown;
			float absPanX = Math.abs(panX);
			float absPanY = Math.abs(panY);
			if (absPanX > PAN_SLOP || absPanY > PAN_SLOP) {
				if (absPanX > absPanY) {
					pan(panX > 0 ? PAN_RIGHT : PAN_LEFT);
				} else {
					pan(panY > 0 ? PAN_BOTTOM : PAN_TOP);
				}
				
				mIsReleased = true;
				return true;
			}
			break;
		
		}
		return false;
	}
	
	private boolean isSinglePage() {
		return mCalendar.getCalendarType() == Calendar.Annual_Calendars || mCalendar.getCalendarType() == Calendar.Monthly_Simplex;
	}
	
	private static final int PAN_LEFT = 1;
	private static final int PAN_RIGHT = 2;
	private static final int PAN_TOP = 3;
	private static final int PAN_BOTTOM = 4;
	private void pan(int direction) {
		switch (direction) {
		case PAN_LEFT:
			setPosition(mPosition + 1);
			break;
		case PAN_RIGHT:
			setPosition(mPosition - 1);
			break;
		case PAN_TOP:
			switch (mCalendar.getCalendarType()) {
			case Calendar.Annual_Calendars:
				break;
			case Calendar.Monthly_Simplex:
				setPosition(mPosition + 1); 
				break;
			case Calendar.Monthly_Duplex:
				if (mPosition == 0) {
					setPosition(mPosition + 1);
				} else if (mCalendar != null && mPosition * 2 == mCalendar.pages.size()) {
					//do nothing
				} else {
					moveToBottom();
				}
				break;
			}
			
			break;
		case PAN_BOTTOM:
			switch (mCalendar.getCalendarType()) {
			case Calendar.Annual_Calendars:
				break;
			case Calendar.Monthly_Simplex:
				setPosition(mPosition - 1);
				break;
			case Calendar.Monthly_Duplex:
				if (mPosition == 0) {
					//do nothing
				} else if (mCalendar != null && mPosition * 2 == mCalendar.pages.size()) {
					setPosition(mPosition -1); 
				} else {
					moveToTop();
				}
				break;
			}
			
			break;
			
		}
	}
	
	private boolean isCurrentViewBitmap(String pageId){
		boolean isCur = false;
		if (pageId == null) return isCur;
		CalendarPage[] pages = getCurrentPages();
		if (pages != null) {
			for (CalendarPage page : pages) {
				if (page == null) continue;
				if (page.id == null) continue;
				if (page.id.equals(pageId)) {
					isCur = true;
					break;
				}				
			}		
		}		
		return isCur;
	}
	
}
