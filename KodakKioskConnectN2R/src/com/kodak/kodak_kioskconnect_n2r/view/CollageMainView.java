package com.kodak.kodak_kioskconnect_n2r.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.activity.CollageEditActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageManager;
import com.kodak.kodak_kioskconnect_n2r.view.MainPageView.OnLayerClickListener;
import com.kodak.kodak_kioskconnect_n2r.view.MainPageView.OnLayerDragListener;
import com.kodak.utils.DimensionUtil;

public class CollageMainView extends RelativeLayout implements DragTarget{
	private static final String TAG = "CollageMainView";
	
	private CollagePageView mPageView;
	private Collage mCollage;
	
	private Point mWindowSize;
	private CollageEditActivity mActivity;
	private int mOriginalIndex;
	private int mBringToTopIndex;
	
	//for normal state(not zoom in)
	private int mMaxWidth;
	private int mMaxHeight;
	
	private boolean mIsTouchable = true;
	
	private boolean mInited;
	private boolean mIsZoomIn;
	private boolean mIsEditMode;
	private boolean mIsZoomInDragMode;
	private boolean mIsInLeft;
	
	private Rect mRect4Normal;
	private Rect mRect4NormalPrevoius;
	
	private int mRightPanelWidth;
	

	public CollageMainView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public CollageMainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CollageMainView(Context context) {
		super(context);
		init(context);
	}
	
	public void init(Context context) {
		mActivity = (CollageEditActivity) context;
		
		inflate(context, R.layout.collage_main_view, this);
		mPageView = (CollagePageView) findViewById(R.id.page_view);
		
		
		//it will re-init when onSizeChaned, we init this value here for avoid some null pointer exception
		mWindowSize = new Point();
		mActivity.getWindowManager().getDefaultDisplay().getSize(mWindowSize);
		mWindowSize.y = mWindowSize.y - getResources().getDimensionPixelSize(R.dimen.head_min_height);
	}
	
	private int getIndex4Child(ViewGroup parent, View child) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (parent.getChildAt(i) == child) {
				return i;
			}
		}
		return -1;
	}
	
	public void setCollage(Collage collage) {
		mCollage = collage;
		mPageView.setPage(collage.page);
		
		mRect4Normal = null;
		mMeasured4Collage = false;
		
		requestLayout();
		invalidate();
	}
	
	public boolean isZoomIn() {
		return mIsZoomIn;
	}
	
	public void setTouchable(boolean touchable) {
		mIsTouchable = touchable;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		if (w != 0 && h != 0 && !mInited) {
			mInited = true;
			mMaxWidth = w;
			mMaxHeight = h;
			
			RelativeLayout parent = (RelativeLayout) getParent();
			mOriginalIndex = getIndex4Child(parent, this);
			mBringToTopIndex = getIndex4Child(parent, parent.findViewById(R.id.collage_top_postion));
			
			mWindowSize = new Point();
			mWindowSize.x = parent.getWidth();
			mWindowSize.y = parent.getHeight();
			mRightPanelWidth = getResources().getDimensionPixelSize(R.dimen.collage_theme_container_width);
			
			requestLayout();
		}
		
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mMeasured4Collage && !mIsZoomIn) {
			mRect4Normal = getCurrentLayoutRect();
			mRect4NormalPrevoius = mRect4Normal;
		}
		
	}
	
	private Rect getNormalRect() {
		Rect rect = mRect4Normal == null ? mRect4NormalPrevoius : mRect4Normal;
		
		if (rect == null) {
			//Sometimes it still null, so generate it manunaly, this case is very rarely
			rect = new Rect();
			if (mCollage == null) {
				mCollage = CollageManager.getInstance().getCurrentCollage();
			}
			if (mCollage != null) {
				float pw = mCollage.page.width;
				float ph = mCollage.page.height;
				
				int maxW = mMaxWidth;
				int maxH = mMaxHeight;
				
				if (maxW == 0 || maxH == 0) {
					maxW = mWindowSize.x;
					maxH = mWindowSize.y - getResources().getDimensionPixelSize(R.dimen.head_min_height);
				}
				
				//TODO: it(80, 20) depends on the dp in xml
				int topMargin = DimensionUtil.dip2px(mActivity, 20);
				int leftMargin = DimensionUtil.dip2px(mActivity, 80);
				//bottom , right margin is same with top , left margin
				int w = maxW - leftMargin * 2;
				int h = maxH - topMargin * 2;
				if ((float)w / h > pw / ph) {
					w = (int) (h * pw / ph);
					rect.top = topMargin;
					rect.bottom = rect.top + h;
					rect.left = (maxW - w) / 2;
					rect.right = rect.left + w;
				} else {
					h = (int) (w * ph / pw);
					rect.left = leftMargin;
					rect.right = rect.left + w;
					rect.top = (maxH - h) / 2;
					rect.bottom = rect.top + h;
				}
				
				
			}
		}
		
		return rect;
	}
	
	private Rect getCurrentLayoutRect() {
		return new Rect(getLeft(), getTop(), getRight(), getBottom());
	}
	
	/**
	 * Not really top layer, it will below some special layer for edit
	 */
	private void bringToTop() {
		RelativeLayout parent = (RelativeLayout) getParent();
		
		
		parent.removeView(this);
		parent.addView(this, mBringToTopIndex);
		parent.requestLayout();
		parent.invalidate();
	}
	
	private boolean mMeasured4Collage = false;
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int w = MeasureSpec.getSize(widthMeasureSpec);
		int h = MeasureSpec.getSize(heightMeasureSpec);
		
		//TODO In some old android version(4.1, 4.2.?), the w , h is not equal to params width and heith(don't know why)
		//It will caused that when it enter layer edit mode, the image is not full
		//so we need to check it
		if (mIsEditMode || mIsZoomInDragMode) {
			LayoutParams params = (LayoutParams) getLayoutParams();
			if (w < params.width) {
				w = params.width;
			}
			
			if (h < params.height) {
				h = params.height;
			}
		}
		
		if (mCollage != null) {
			float pw = mCollage.page.width;
			float ph = mCollage.page.height;
			
			if (mIsZoomIn) {
				w = (int) (h * pw / ph);
			} else if (mMaxWidth != 0 && mMaxHeight != 0){
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
				
				mMeasured4Collage = true;
			}
		}
		
		super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
	}
	
	private float mRawXDown;
	private float mRawYDown;
	private float mDistanceDown;
	private Rect mRectDown;
	private static final float PINCH_SLOP = 50;
	private static final float MAX_SPACE_IN_DRAG = 300;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (!mIsTouchable) {
			return true;
		}
		
		switch (MotionEvent.ACTION_MASK & ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mRawXDown = ev.getRawX();
			mRawYDown = ev.getRawY();
			mRectDown = getCurrentLayoutRect();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			mDistanceDown = getPointerDistance(ev);
			break;
		case MotionEvent.ACTION_MOVE:
			if (ev.getPointerCount() > 1) {
				float distance = getPointerDistance(ev);
				if (Math.abs(distance - mDistanceDown) > PINCH_SLOP && !mIsEditMode) {
					if (distance > mDistanceDown && !mIsZoomIn) {
						zoomIn(null);
						return true;
					} else if (distance < mDistanceDown && mIsZoomIn) {
						zoomOut(null);
						return true;
					}
				}
			} else if (mIsZoomInDragMode) {
				float dx = ev.getRawX() - mRawXDown;
				float dy = ev.getRawY() - mRawYDown;
				
				Rect dstRect = new Rect(mRectDown);
				
				//check bounds for new rect
				if (dstRect.left + dx > MAX_SPACE_IN_DRAG) {
					dx = MAX_SPACE_IN_DRAG - dstRect.left;
				}
				if (dstRect.top + dy > MAX_SPACE_IN_DRAG) {
					dy = MAX_SPACE_IN_DRAG - dstRect.top;
				}
				if (mWindowSize.x - dstRect.right - dx > MAX_SPACE_IN_DRAG) {
					dx = mWindowSize.x - dstRect.right - MAX_SPACE_IN_DRAG;
				}
				if (mWindowSize.y - dstRect.bottom - dy > MAX_SPACE_IN_DRAG) {
					dy = mWindowSize.y - dstRect.bottom - MAX_SPACE_IN_DRAG;
				}
				
				if (dx != 0 || dy != 0) {
					dstRect.offset((int)dx, (int)dy);
					moveTo(dstRect, false);
				}
				
			}
		}
		
		return super.onInterceptTouchEvent(ev);
	}
	
	private float getPointerDistance(MotionEvent event){
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x*x + y*y);
	}
	
	/**
	 * @param dstRect
	 */
	private void animateTo(Rect dstRect, boolean center, final AnimationListener listener) {
		Rect currentRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
		
		moveTo(dstRect, center);
		
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
		
		animSet.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				mIsTouchable = false;
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
				mIsTouchable = true;
				if (listener != null) {
					listener.onAnimationEnd(animation);
				}
			}
			
			
		});
		
		
		startAnimation(animSet);
	}
	
	private void moveTo(Rect dstRect, boolean centerHonrizontal) {
		if (dstRect == null) {
			Log.e(TAG, "Error:target rect is null");
			return;
		}
		
		LayoutParams params = (LayoutParams) getLayoutParams();
		params.topMargin = dstRect.top;
		params.bottomMargin = mWindowSize.y - dstRect.bottom;
		params.height = dstRect.height();
		params.width = dstRect.width();
		
		if (centerHonrizontal) {
			params.addRule(CENTER_IN_PARENT);
			params.leftMargin = 0;
			params.rightMargin = 0;
		} else {
			params.addRule(CENTER_IN_PARENT, 0);
			params.leftMargin = dstRect.left;
			params.rightMargin = 0;
		}
		requestLayout();
	}
	
	public void enterLayerEditMode(CollagePageView pageView, Layer layer, AnimationListener listener) {
		mIsZoomIn = true;
		mIsEditMode = true;
		mIsZoomInDragMode = false;
		//get rect for layer
		RectF layerRect = pageView.getLayerRect(layer);
		
		//the max rect for the layer can scale
		RectF maxRect = new RectF();
		maxRect.left = DimensionUtil.dip2px(mActivity, 24);
		maxRect.top =  DimensionUtil.dip2px(mActivity, 48);
		maxRect.right = mWindowSize.x - DimensionUtil.dip2px(mActivity, 120) - mActivity.collageEditLayer.getEditPopViewWidth();
		maxRect.bottom = mWindowSize.y - DimensionUtil.dip2px(mActivity, 48);
		
		//get new Rect for layer
		RectF newRect = new RectF();
		if (layerRect.width() / layerRect.height() > maxRect.width() / maxRect.height()) {
			float w = maxRect.width();
			float h = w * layerRect.height() / layerRect.width();
			newRect.left = maxRect.left;
			newRect.right = newRect.left + w;
			newRect.top = maxRect.top;
			newRect.bottom = newRect.top + h;
		} else {
			float h = maxRect.height();
			float w = h * layerRect.width() / layerRect.height();
			newRect.left = maxRect.left;
			newRect.right = newRect.left + w;
			newRect.top = maxRect.top;
			newRect.bottom = newRect.top + h;
		}
		
		float scale = newRect.width() / layerRect.width();
		
		bringToTop();
		int w = (int) (getWidth() * scale );
		int h = (int) (getHeight() * scale);
		
		Rect dst = new Rect();
		dst.left = (int) (newRect.left - layerRect.left * scale);
		dst.right = dst.left + w;
		dst.top = (int) (newRect.top - layerRect.top * scale);
		dst.bottom = dst.top + h;
		
		animateTo(dst, false, listener);
	}
	
	public void exitEditMode(AnimationListener listener) {
		if (mIsEditMode) {
			mIsEditMode = false;
			animateToNormal(listener);
		}
	} 
	
	
	/**
	 * When right panel open, this view should zoom to left
	 */
	public void animateToLeft(final AnimationListener listener) {
		mIsZoomIn = true;
		mIsInLeft = true;
		mIsZoomInDragMode = false;
		mIsTouchable = false;
		
		int maxWidth = mWindowSize.x - mRightPanelWidth;
		int maxHeight = mWindowSize.y;
		
		//get the max rect for collage
		int margin = DimensionUtil.dip2px(mActivity, 12);
		Rect maxRect = new Rect(margin, margin, maxWidth - margin, maxHeight - margin);
		
		Rect oldRect = getCurrentLayoutRect();
		Rect dstRect = new Rect();
		if ((float)oldRect.width() / oldRect.height() > (float)maxRect.width() / maxRect.height()) {
			int newW = maxRect.width();
			int newH = newW * oldRect.height() / oldRect.width();
			dstRect.left = maxRect.left;
			dstRect.right = maxRect.right;
			dstRect.top = maxRect.top + (maxRect.height() - newH) / 2;
			dstRect.bottom = maxRect.top + newH;
		} else {
			int newH = maxRect.height();
			int newW = newH * oldRect.width() / oldRect.height();
			dstRect.top = maxRect.top;
			dstRect.bottom = maxRect.bottom;
			dstRect.left = maxRect.left + (maxRect.width() - newW) / 2;
			dstRect.right = dstRect.left + newW;
		}
		
		bringToTop();
		
		animateTo(dstRect, false, new AnimationListener() {
			
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
				mIsTouchable = false;
				if (listener != null) {
					listener.onAnimationEnd(animation);
				}
			}
		});
		
	}
	
	public void animateToNormal(final AnimationListener listener) {
		mIsZoomIn = false;
		mIsInLeft = false;
		mIsZoomInDragMode = false;
		mIsEditMode = false;
		mIsTouchable = true;
		
		RelativeLayout parent = (RelativeLayout) getParent();
		parent.removeView(CollageMainView.this);
		parent.addView(CollageMainView.this, mOriginalIndex);
		parent.requestLayout();
		parent.invalidate();
		
		
		Rect dstRect = getNormalRect();
		animateTo(dstRect, true, new AnimationListener() {
			
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
				if (listener != null) {
					listener.onAnimationEnd(animation);
				}
			}
		});
	}
	
	public void zoomIn(AnimationListener listener) {
		if (mIsZoomIn || mIsInLeft) {
			return;
		}
		
		mIsZoomIn = true;
		mIsZoomInDragMode = true;
		
		int maxW = mWindowSize.x * 3;
		int maxH = mWindowSize.y * 3;
		Rect maxRect = new Rect();
		maxRect.left = -mWindowSize.x;
		maxRect.top = -mWindowSize.y;
		maxRect.right = maxRect.left + maxW;
		maxRect.bottom = maxRect.top + maxH;
		
		Rect oldRect = getCurrentLayoutRect();
		Rect dstRect = new Rect();
		if ((float)oldRect.width() / oldRect.height() > (float)maxRect.width() / maxRect.height()) {
			int w = maxRect.width();
			int h = w * oldRect.height() / oldRect.width();
			dstRect.left = maxRect.left;
			dstRect.right = dstRect.left + w;
			dstRect.top = maxRect.centerY() - h / 2;
			dstRect.bottom = dstRect.top + h;
		} else {
			int h = maxRect.height();
			int w = h * oldRect.width() / oldRect.height();
			dstRect.top = maxRect.top;
			dstRect.bottom = dstRect.top + h;
			dstRect.left = maxRect.centerX() - w / 2;
			dstRect.right = dstRect.left + w;
		}
		
		bringToTop();
		animateTo(dstRect, false, listener);
		
	}
	
	public void zoomOut(AnimationListener listener) {
		if (!mIsZoomIn || mIsInLeft) {
			return;
		}
		
		animateToNormal(listener);
	}
	
	public void setOnLayerClickListener(OnLayerClickListener<CollagePageView, CollagePage, Layer> onLayerClickListener) {
		mPageView.setOnLayerClickListener(onLayerClickListener);
	}
	
	public void setOnLayerDragListener(OnLayerDragListener<CollagePageView, CollagePage, Layer> onLayerDragListener) {
		mPageView.setOnLayerDragListener(onLayerDragListener);
	}
	
	public void setImageBitmap (Bitmap bitmap) {
		mPageView.setImageBitmap(bitmap);
	}
	
	public Object[] pointToPosition(float xOnScreen, float yOnScreen) {
		if (mCollage == null || mPageView == null) {
			return null;
		}
		int[] location = new int[2];
		mPageView.getLocationOnScreen(location);
		float relativeX = xOnScreen - location[0];
		float relativeY = yOnScreen - location[1];
		CollagePage p = mPageView.getPage();
		if (p == null) return null;
		for (int m = 0; m < p.layers.length; m++) {
			Layer layer = p.layers[m];
			if (layer == null) continue;
			RectF rect = mPageView.getLayerRect(layer);					
			if ((Layer.TYPE_IMAGE.equals(layer.type) || Layer.TYPE_TEXT_BLOCK.equals(layer.type)) && rect.contains(relativeX, relativeY)) {
				Object[] result = new Object[]{mPageView, p, layer};
				return result;
			}
		}
		return null;
	}

	@Override
	public void hideAllFrames() {
		
	}
	
}
