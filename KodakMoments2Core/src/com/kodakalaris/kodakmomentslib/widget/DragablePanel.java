package com.kodakalaris.kodakmomentslib.widget;


import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.util.Log;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 
 * @author Kane
 *
 */
public abstract class DragablePanel extends ViewGroup {
	private static final String TAG = DragablePanel.class.getSimpleName();
	
	// draw's orientation
	public static final int VERTICAL = 0;
	public static final int HORINZONTAL = 1;
	
	// the position of handle
	public static final int BOTTOM = 0;
	public static final int TOP = 1;
	public static final int CENTER_VERTICAL = 2;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;
	public static final int CENTER_HORINZONTAL = 5;
	
	private static final int TAP_THRESHOLD = 6;
    private static final float MAXIMUM_TAP_VELOCITY = 100.0f;
    private static final float MAXIMUM_MINOR_VELOCITY = 150.0f;
    private static final float MAXIMUM_MAJOR_VELOCITY = 200.0f;
    private static final float MAXIMUM_ACCELERATION = 2000.0f;
    private static final int VELOCITY_UNITS = 1000;
    private static final int MSG_ANIMATE = 1000;
    private static final int ANIMATION_FRAME_DURATION = 1000 / 60;

    private static final int EXPANDED_FULL_OPEN = -10001;
    private static final int COLLAPSED_FULL_CLOSED = -10002;
	
	private int mHandleId, mContentId;
	private View mHandle, mContent;
	
	private final Rect mFrame = new Rect();
	private final Rect mInvalidate = new Rect();
	private boolean mTracking;
	private boolean mLocked;
	
	private VelocityTracker mVelocityTracker;
	
	private boolean mVertical;
	private boolean mExpanded;
	private int mBottomOffset;
    private int mTopOffset;
    private int mHandleHeight;
    private int mHandleWidth;
    
    private OnDrawerOpenListener mOnDrawerOpenListener;
    private OnDrawerCloseListener mOnDrawerCloseListener;
    private OnDrawerScrollListener mOnDrawerScrollListener;
    
    private final Handler mHandler = new SlidingHandler();
    private float mAnimatedAcceleration;
    private float mAnimatedVelocity;
    private float mAnimationPosition;
    private long mAnimationLastTime;
    private long mCurrentAnimationTime;
    private int mTouchDelta;
    private boolean mAnimating;
    private boolean mAllowSingleTap;
    private boolean mAnimateOnClick;
    // Added by Kane
    /**
     * TODO add block
     */
    private boolean mAllowAutoAnimation;
    /**
     * TODO add block
     */
    private boolean mAllowHandleOutsideTracking;

    private final int mTapThreshold;
    private final int mMaximumTapVelocity;
    private final int mMaximumMinorVelocity;
    private final int mMaximumMajorVelocity;
    private final int mMaximumAcceleration;
    private final int mVelocityUnits;
    
    private int mBottom;
    private int mTop;
    private int mRight;
    private int mLeft;
    
    private int mPosition = BOTTOM;
    private boolean mScrolling = false;
    
    private final int mMinimumHeight;
    private final int mMaximumHeight;
	
	public DragablePanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public DragablePanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Panel);
		int orientation = a.getInt(R.styleable.Panel_orientation, VERTICAL);
		mVertical = orientation == VERTICAL;
		
		mAllowSingleTap = a.getBoolean(R.styleable.Panel_allowSingleTap, true);
        mAnimateOnClick = a.getBoolean(R.styleable.Panel_animateOnClick, true);
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        
        if(mVertical){
        	int height = wm.getDefaultDisplay().getHeight();
        	float maxPercent = a.getFloat(R.styleable.Panel_offsetTop, 0);
        	mMaximumHeight = (int) ((maxPercent == 0 ? 0 : 1-maxPercent) * height);
        	mMinimumHeight = (int) ((a.getFloat(R.styleable.Panel_offsetBottom, 0)) * height);
        } else {
        	mMaximumHeight = 0;
        	mMinimumHeight = 0;
        }
        
        mTopOffset = mMaximumHeight;
        mBottomOffset = -mMinimumHeight;
		
		int handleId = a.getResourceId(R.styleable.Panel_handle, 0);
	    if (handleId == 0) {
	    	throw new IllegalArgumentException("The handle attribute is required and must refer to a valid child.");
	    }

	    int contentId = a.getResourceId(R.styleable.Panel_content, 0);
	    if (contentId == 0) {
	    	throw new IllegalArgumentException("The content attribute is required and must refer to a valid child.");
	    }

	    if (handleId == contentId) {
	    	throw new IllegalArgumentException("The content and handle attributes must refer to different children.");
	    }

	    mHandleId = handleId;
	    mContentId = contentId;
	    
	    final float density = getResources().getDisplayMetrics().density;
        mTapThreshold = (int) (TAP_THRESHOLD * density + 0.5f);
        mMaximumTapVelocity = (int) (MAXIMUM_TAP_VELOCITY * density + 0.5f);
        mMaximumMinorVelocity = (int) (MAXIMUM_MINOR_VELOCITY * density + 0.5f);
        mMaximumMajorVelocity = (int) (MAXIMUM_MAJOR_VELOCITY * density + 0.5f);
        mMaximumAcceleration = (int) (MAXIMUM_ACCELERATION * density + 0.5f);
        mVelocityUnits = (int) (VELOCITY_UNITS * density + 0.5f);
	    
	    a.recycle();
        setAlwaysDrawnWithCacheEnabled(false);
	}
	
	@Override
	protected void onFinishInflate() {
		mHandle = findViewById(mHandleId);
        if (mHandle == null) {
            throw new IllegalArgumentException("The handle attribute is must refer to an" + " existing child.");
        }
        mHandle.setOnClickListener(new DrawerToggler());
        
        mContent = findViewById(mContentId);
        if (mContent == null) {
            throw new IllegalArgumentException("The content attribute is must refer to an" + " existing child.");
        }
        
        // initial the position for the first time 
        mAnimationPosition = mVertical ? mHandle.getTop() : mHandle.getLeft();
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
        }

        final View handle = mHandle;
        measureChild(handle, widthMeasureSpec, heightMeasureSpec);

        if (mVertical) {
            int height = heightSpecSize - handle.getMeasuredHeight() - mTopOffset;
            mContent.measure(MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else {
            int width = widthSpecSize - handle.getMeasuredWidth() - mTopOffset;
            mContent.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.EXACTLY));
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		final long drawingTime = getDrawingTime();
	    final View handle = mHandle;
	    final boolean isVertical = mVertical;

	    drawChild(canvas, handle, drawingTime);

	   if (mTracking || mAnimating) {
	    	final Bitmap cache = mContent.getDrawingCache();
	    	if (cache != null) {
	    		if (isVertical) {
	    			canvas.drawBitmap(cache, 0, handle.getBottom(), null);
	    		} else {
	    			canvas.drawBitmap(cache, handle.getRight(), 0, null);                    
	    		}
	    	} else {
	    		canvas.save();
	    		canvas.translate(isVertical ? 0 : handle.getLeft() - mTopOffset, 0);
	    		drawChild(canvas, mContent, drawingTime);
	    		canvas.restore();
	    	}
	    } else if (mExpanded || mBottomOffset != 0) {
	    	drawChild(canvas, mContent, drawingTime);
	    	if(mBottomOffset != 0){
	    		 prepareContent();
	    	}
	    }
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mTracking || !changed) {
            return;
        }
		
		mTop = t; mLeft = l; mBottom = b; mRight = r;

        final View handle = mHandle;
        // fix bug about while changed is true, this panel will fullfill the screen. Kane
        int handleLeft = 0;
        int handleTop = mVertical ? getHeight()-handle.getMeasuredHeight()+ mBottomOffset : 0 ;
        int handleRight = handle.getMeasuredWidth();
        int handleBottom = getHeight() + mBottomOffset;
        handle.layout(handleLeft, handleTop, handleRight, handleBottom);
        mHandleHeight = handle.getHeight();
        mHandleWidth = handle.getWidth();

        int childWidth = handle.getMeasuredWidth();
        int childHeight = handle.getMeasuredHeight();
        
        final View content = mContent;

        if (mVertical) {
            content.layout(0, mHandle.getBottom(), content.getMeasuredWidth(), content.getMeasuredHeight());
        } else {
            content.layout(mTopOffset + childWidth, 0, mTopOffset + childWidth + content.getMeasuredWidth(), content.getMeasuredHeight());            
        }
        
	}
	
	private int handleDefaultTop(){
		final View handle = mHandle;
		int handleTop = mVertical ? getHeight()-handle.getMeasuredHeight() : 0;
		return handleTop + mMinimumHeight;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (mLocked) {
            return false;
        }

        final int action = event.getAction();

        float x = event.getX();
        float y = event.getY();

        final View handle = mHandle;
        
        if (!mTracking && !isTouchInView(mHandle, event)) {
            return false;
        } 
        
        boolean slidable = true;
        if(!mAllowHandleOutsideTracking){
        	if(!isTouchInView(mHandle, event)){
        		mTracking = false;
        		slidable = false;
        	}
        }
        
        if(slidable){
        	if (action == MotionEvent.ACTION_DOWN) {
        		mTouchFalgs = MotionEvent.ACTION_DOWN;
        		mTracking = true;

        		handle.setPressed(true);
        		// Must be called before prepareTracking()
        		prepareContent();

        		// Must be called after prepareContent()
        		/*if (mOnDrawerScrollListener != null) {
        			mOnDrawerScrollListener.onScrollStarted();
        		}*/

        		if (mVertical) {
        			final int top = mHandle.getTop();
        			mTouchDelta = (int) y - top;
        			prepareTracking(top);
        		} else {
        			final int left = mHandle.getLeft();
        			mTouchDelta = (int) x - left;
        			prepareTracking(left);
        		}
        		mVelocityTracker.addMovement(event);
        		checkClick();
        		return false;
        	}
        	
        	if(action == MotionEvent.ACTION_MOVE){
        		if(checking){
        			return false;
        		}
        		mTouchFalgs |= MotionEvent.ACTION_MOVE;
        		return needInterceptTouchEvent(event);
        	}
        	if(action == MotionEvent.ACTION_UP && mTouchFalgs==MotionEvent.ACTION_DOWN){
        		if(!needInterceptTouchEvent(event)){
        			saveStatus();
        		}
        		return false;
        	} else {
        		return true;
        	}
        }
	
        return false;
	}
	
	protected abstract boolean needInterceptTouchEvent(MotionEvent event);
	
	private boolean checking = false;
	private int mTouchFalgs = 0;
	private void checkClick(){
		checking = true;
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				checking = false;
			}
			
		}, ViewConfiguration.getTapTimeout()/2);
		
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLocked) {
            return true;
        }

        final View handle = mHandle;
    	final View content = mContent;
        if (mTracking) {
            mVelocityTracker.addMovement(event);
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                	if(!mExpanded){
                		mExpanded = true;
                	}
                	if (mOnDrawerScrollListener != null && !mScrolling) {
                		mScrolling = true;
            			mOnDrawerScrollListener.onScrollStarted();
            		}
                    moveHandle((int) (mVertical ? event.getY() : event.getX()) - mTouchDelta);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(mVelocityUnits);

                    float yVelocity = velocityTracker.getYVelocity();
                    float xVelocity = velocityTracker.getXVelocity();
                    boolean negative;

                    final boolean vertical = mVertical;
                    if (vertical) {
                        negative = yVelocity < 0;
                        if (xVelocity < 0) {
                            xVelocity = -xVelocity;
                        }
                        if (xVelocity > mMaximumMinorVelocity) {
                            xVelocity = mMaximumMinorVelocity;
                        }
                    } else {
                        negative = xVelocity < 0;
                        if (yVelocity < 0) {
                            yVelocity = -yVelocity;
                        }
                        if (yVelocity > mMaximumMinorVelocity) {
                            yVelocity = mMaximumMinorVelocity;
                        }
                    }

                    float velocity = (float) Math.hypot(xVelocity, yVelocity);
                    if (negative) {
                        velocity = -velocity;
                    }

                    final int top = handle.getTop();
                    final int left = handle.getLeft();
                    
                    if(!mAllowAutoAnimation){
                    	saveStatus();
                    	break;
                    }
                    
                    if (Math.abs(velocity) < mMaximumTapVelocity) {
                        if (vertical ? (mExpanded && top < mTapThreshold + mTopOffset) || (!mExpanded && top > mBottomOffset + mBottom - mTop - mHandleHeight - mTapThreshold) :
                                (mExpanded && left < mTapThreshold + mTopOffset) || (!mExpanded && left > mBottomOffset + mRight - mLeft - mHandleWidth - mTapThreshold)) {

                            if (mAllowSingleTap) {
                                playSoundEffect(SoundEffectConstants.CLICK);
                                
                                if (mExpanded) {
                                    animateClose(vertical ? top : left);
                                } else {
                                    animateOpen(vertical ? top : left);
                                }
                            } else {
                                performFling(vertical ? top : left, velocity, false);
                            }

                        } else {
                            performFling(vertical ? top : left, velocity, false);
                        }
                    } else {
                        performFling(vertical ? top : left, velocity, false);
                    }
                }
                break;
            }
        }
        if(isTouchInView(handle, event) || isTouchInView(content, event)){
        	return true;
        }
        return mTracking || mAnimating || super.onTouchEvent(event);
    }
	
	private boolean isTouchInView(final View view, MotionEvent event){
		float x = event.getX();
	    float y = event.getY();

	    final Rect frame = mFrame;
	    view.getHitRect(frame);
		
		return frame.contains((int)x, (int)y);
	}

    private void animateClose(int position) {
        prepareTracking(position);
        performFling(position, mMaximumAcceleration, true);
    }

    private void animateOpen(int position) {
        prepareTracking(position);
        performFling(position, -mMaximumAcceleration, true);
    }
    
    private void saveStatus(){
    	long now = SystemClock.uptimeMillis();
        mAnimationLastTime = now;
        mAnimating = false;
        mHandler.removeMessages(MSG_ANIMATE);
        stopTracking();
    }
    
    private Rect getContentRect(){
    	Rect rect = new Rect();
    	/*int contentLeft = 0;
    	int contentTop = mVertical ? mHandle.getBottom() : 0;
    	int contentRight = mVertical ? getWidth() : mHandle.getLeft();
    	int contentBottom = getHeight();*/
    	
    	rect.left = 0;
    	rect.top = 0;
    	rect.right = getWidth();
    	rect.bottom = getHeight() - mHandle.getBottom();
    	
    	return rect;
    }

    private void performFling(int position, float velocity, boolean always) {
        mAnimationPosition = position;
        mAnimatedVelocity = velocity;

        if (mExpanded) {
            if (always || (velocity > mMaximumMajorVelocity ||
                    (position > mTopOffset + (mVertical ? mHandleHeight : mHandleWidth) &&
                            velocity > -mMaximumMajorVelocity))) {
                // We are expanded, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the expanded position.
                mAnimatedAcceleration = mMaximumAcceleration;
                if (velocity < 0) {
                    mAnimatedVelocity = 0;
                }
            } else {
                // We are expanded and are now going to animate away.
                mAnimatedAcceleration = -mMaximumAcceleration;
                if (velocity > 0) {
                    mAnimatedVelocity = 0;
                }
            }
        } else {
            if (!always && (velocity > mMaximumMajorVelocity ||
                    (position > (mVertical ? getHeight() : getWidth()) / 2 &&
                            velocity > -mMaximumMajorVelocity))) {
                // We are collapsed, and they moved enough to allow us to expand.
                mAnimatedAcceleration = mMaximumAcceleration;
                if (velocity < 0) {
                    mAnimatedVelocity = 0;
                }
            } else {
                // We are collapsed, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the collapsed position.
                mAnimatedAcceleration = -mMaximumAcceleration;
                if (velocity > 0) {
                    mAnimatedVelocity = 0;
                }
            }
        }

        long now = SystemClock.uptimeMillis();
        mAnimationLastTime = now;
        mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
        mAnimating = true;
        mHandler.removeMessages(MSG_ANIMATE);
        mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
        stopTracking();
    }

    private void prepareTracking(int position) {
    	mTracking = true;
        mVelocityTracker = VelocityTracker.obtain();
        boolean opening = !mExpanded;
        if (opening) {
            mAnimatedAcceleration = mMaximumAcceleration;
            mAnimatedVelocity = mMaximumMajorVelocity;
            mAnimationPosition = mBottomOffset + (mVertical ? getHeight() - mHandleHeight : getWidth() - mHandleWidth);
            mHandler.removeMessages(MSG_ANIMATE);
            long now = SystemClock.uptimeMillis();
            mAnimationLastTime = now;
            mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
            mAnimating = true;
        } else {
            if (mAnimating) {
                mAnimating = false;
                mHandler.removeMessages(MSG_ANIMATE);
            }
            moveHandle(position);
        }
    }

    private void moveHandle(int position) {
        final View handle = mHandle;
        final View content = mContent;
        if (mVertical) {
            if (position == EXPANDED_FULL_OPEN) {
                handle.offsetTopAndBottom(mTopOffset - handle.getTop());
                content.layout(0, handle.getBottom(), getWidth(), getHeight());
                invalidate();
            } else if (position == COLLAPSED_FULL_CLOSED) {
                handle.offsetTopAndBottom(mBottomOffset + mBottom - mTop - mHandleHeight - handle.getTop());
                content.layout(0, handle.getBottom(), getWidth(), getHeight());
                invalidate();
            } else {
                final int top = handle.getTop();
                int deltaY = position - top;
                if (position < mTopOffset) {
                    deltaY = mTopOffset - top;
                } else if (deltaY > mBottomOffset + mBottom - mTop - mHandleHeight - top) {
                    deltaY = mBottomOffset + mBottom - mTop - mHandleHeight - top;
                }
                handle.offsetTopAndBottom(deltaY);
                content.layout(0, handle.getBottom(), getWidth(), getHeight());
                
                final Rect frame = mFrame;
                final Rect region = mInvalidate;

                handle.getHitRect(frame);
                region.set(frame);

                region.union(frame.left, frame.top - deltaY, frame.right, frame.bottom - deltaY);
                region.union(0, frame.bottom - deltaY, getWidth(), frame.bottom - deltaY + content.getHeight());

                invalidate(region);
            }
        } else {
            if (position == EXPANDED_FULL_OPEN) {
                handle.offsetLeftAndRight(mTopOffset - handle.getLeft());
                invalidate();
            } else if (position == COLLAPSED_FULL_CLOSED) {
                handle.offsetLeftAndRight(mBottomOffset + mRight - mLeft - mHandleWidth - handle.getLeft());
                invalidate();
            } else {
                final int left = handle.getLeft();
                int deltaX = position - left;
                if (position < mTopOffset) {
                    deltaX = mTopOffset - left;
                } else if (deltaX > mBottomOffset + mRight - mLeft - mHandleWidth - left) {
                    deltaX = mBottomOffset + mRight - mLeft - mHandleWidth - left;
                }
                handle.offsetLeftAndRight(deltaX);

                final Rect frame = mFrame;
                final Rect region = mInvalidate;

                handle.getHitRect(frame);
                region.set(frame);

                region.union(frame.left - deltaX, frame.top, frame.right - deltaX, frame.bottom);
                region.union(frame.right - deltaX, 0, frame.right - deltaX + mContent.getWidth(), getHeight());

                invalidate(region);
            }
        }
    }
    
    protected void relayoutContent(){
    	final View content = mContent;
         if (content.isLayoutRequested()) {
             if (mVertical) {
                 final int childHeight = mHandleHeight;
                 int height = mBottom - mTop - childHeight - mTopOffset;
                 content.measure(MeasureSpec.makeMeasureSpec(mRight - mLeft, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                 content.layout(0, mHandle.getBottom(), getWidth(), getHeight());
             } else {
                 final int childWidth = mHandle.getWidth();
                 int width = mRight - mLeft - childWidth - mTopOffset;
                 content.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mBottom - mTop, MeasureSpec.EXACTLY));
                 content.layout(childWidth + mTopOffset, 0, mTopOffset + childWidth + content.getMeasuredWidth(), content.getMeasuredHeight());
             }
         }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void prepareContent() {
        if (mAnimating) {
            return;
        }
        
        relayoutContent();
        // Something changed in the content, we need to honor the layout request
        // before creating the cached bitmap
       
        // Try only once... we should really loop but it's not a big deal
        // if the draw was cancelled, it will only be temporary anyway
        final View content = mContent;
        content.getViewTreeObserver().dispatchOnPreDraw();
        if (!content.isHardwareAccelerated()) {
        	content.buildDrawingCache();
        }

    }

    private void stopTracking() {
        mHandle.setPressed(false);
        //mTracking = false;

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        // delay change mTracking to false in order to make sure the latest children be drawn
        postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mTracking = false;
				if (mOnDrawerScrollListener != null) {
		        	boolean closed = isClosed();
		        	mScrolling = false;
		        	Rect rect = getContentRect();
		        	mOnDrawerScrollListener.onScrollEnded(closed, rect);
		        }
			}
		}, 20);
        
    }

    // stopAtCenter: it's exist in original file, it's used for stop the animation at center position
    private void doAnimation(boolean stopAtCenter) {
        if (mAnimating) {
            incrementAnimation(stopAtCenter);
            if(stopAtCenter && mAnimationPosition == getCenterPosition()){
            	mAnimating = false;
            	mExpanded = true;
            	requestLayout();
            	invalidate();
            	relayoutContent();
            	if (mOnDrawerScrollListener != null) {
		        	boolean closed = isClosed();
		        	mScrolling = false;
		        	Rect rect = getContentRect();
		        	mOnDrawerScrollListener.onScrollEnded(closed, rect);
		        }
            } else if (mAnimationPosition >= mBottomOffset + (mVertical ? getHeight() : getWidth()) - 1) {
                mAnimating = false;
                closeDrawer();
            } else if (mAnimationPosition < mTopOffset) {
                mAnimating = false;
                openDrawer();
            } else {
                moveHandle((int) mAnimationPosition);
                mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
                mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
            }
        }
    }
    
    private int getCenterPosition(){
    	return mVertical ? (getHeight() - mHandleHeight)/2 : (getWidth() - mHandleWidth)/2;
    }
    
    private void incrementAnimation(boolean stopAtCenter) {
        long now = SystemClock.uptimeMillis();
        float t = (now - mAnimationLastTime) / 1000.0f;                   // ms -> s
        final float position = mAnimationPosition;
        final float v = mAnimatedVelocity;                                // px/s
        final float a = mAnimatedAcceleration;                            // px/s/s
        mAnimationPosition = position + (v * t) + (0.5f * a * t * t);     // px
        mAnimatedVelocity = v + (a * t);                                  // px/s
        mAnimationLastTime = now;                                         // ms
        if(stopAtCenter && mAnimationPosition <= getCenterPosition()){
        	mAnimationPosition = getCenterPosition();
        }
    }

    /**
     * Toggles the drawer open and close. Takes effect immediately.
     *
     * @see #open()
     * @see #close()
     * @see #animateClose()
     * @see #animateOpen()
     * @see #animateToggle()
     */
    public void toggle() {
        if (!mExpanded) {
            openDrawer();
        } else {
            closeDrawer();
        }
        invalidate();
        requestLayout();
    }

    /**
     * Toggles the drawer open and close with an animation.
     *
     * @see #open()
     * @see #close()
     * @see #animateClose()
     * @see #animateOpen()
     * @see #toggle()
     */
    public void animateToggle() {
        if (!mExpanded) {
            animateOpen();
        } else {
            animateClose();
        }
    }

    /**
     * Opens the drawer immediately.
     *
     * @see #toggle()
     * @see #close()
     * @see #animateOpen()
     */
    @TargetApi(Build.VERSION_CODES.DONUT)
	public void open() {
        openDrawer();
        invalidate();
        requestLayout();

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    /**
     * Closes the drawer immediately.
     *
     * @see #toggle()
     * @see #open()
     * @see #animateClose()
     */
    public void close() {
        closeDrawer();
        invalidate();
        requestLayout();
    }

    /**
     * Closes the drawer with an animation.
     *
     * @see #close()
     * @see #open()
     * @see #animateOpen()
     * @see #animateToggle()
     * @see #toggle()
     */
    public void animateClose() {
        prepareContent();
        final OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateClose(mVertical ? mHandle.getTop() : mHandle.getLeft());

        if (scrollListener != null) {
        	boolean closed = isClosed();
        	mScrolling = false;
        	Rect rect = getContentRect();
        	scrollListener.onScrollEnded(closed, rect);
        }
    }
    
    /**
     * Opens the drawer with an animation.
     *
     * @see #close()
     * @see #open()
     * @see #animateClose()
     * @see #animateToggle()
     * @see #toggle()
     */
	public void animateOpen() {
        prepareContent();
        final OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateOpen(mVertical ? mHandle.getTop() : mHandle.getLeft());

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        if (scrollListener != null) {
        	boolean closed = isClosed();
        	mScrolling = false;
        	Rect rect = getContentRect();
            scrollListener.onScrollEnded(closed, rect);
        }
    }
	
	public void refreshContent(){
		if (mOnDrawerScrollListener != null) {
        	boolean closed = isClosed();
        	mScrolling = false;
        	Rect rect = getContentRect();
        	mOnDrawerScrollListener.onScrollEnded(closed, rect);
        }
	}
	
	private boolean isClosed(){
		return mVertical ? mHandle.getBottom() == getHeight() : mHandle.getLeft() == 0;
	}

	@Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(DragablePanel.class.getName());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(DragablePanel.class.getName());
    }

    private void closeDrawer() {
        moveHandle(COLLAPSED_FULL_CLOSED);
        //mContent.setVisibility(View.GONE);
        //mContent.destroyDrawingCache();

        if (!mExpanded) {
            return;
        }

        mExpanded = false;
        if (mOnDrawerCloseListener != null) {
            mOnDrawerCloseListener.onDrawerClosed();
        }
    }

    private void openDrawer() {
        moveHandle(EXPANDED_FULL_OPEN);
        //mContent.setVisibility(View.VISIBLE);

        if (mExpanded) {
            return;
        }

        mExpanded = true;

        if (mOnDrawerOpenListener != null) {
        	Rect rect = getContentRect();
            mOnDrawerOpenListener.onDrawerOpened(rect);
        }
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes open.
     *
     * @param onDrawerOpenListener The listener to be notified when the drawer is opened.
     */
    public void setOnDrawerOpenListener(OnDrawerOpenListener onDrawerOpenListener) {
        mOnDrawerOpenListener = onDrawerOpenListener;
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes close.
     *
     * @param onDrawerCloseListener The listener to be notified when the drawer is closed.
     */
    public void setOnDrawerCloseListener(OnDrawerCloseListener onDrawerCloseListener) {
        mOnDrawerCloseListener = onDrawerCloseListener;
    }

    /**
     * Sets the listener that receives a notification when the drawer starts or ends
     * a scroll. A fling is considered as a scroll. A fling will also trigger a
     * drawer opened or drawer closed event.
     *
     * @param onDrawerScrollListener The listener to be notified when scrolling
     *        starts or stops.
     */
    public void setOnDrawerScrollListener(OnDrawerScrollListener onDrawerScrollListener) {
        mOnDrawerScrollListener = onDrawerScrollListener;
    }

    /**
     * Returns the handle of the drawer.
     *
     * @return The View reprenseting the handle of the drawer, identified by
     *         the "handle" id in XML.
     */
    public View getHandle() {
        return mHandle;
    }

    /**
     * Returns the content of the drawer.
     *
     * @return The View reprenseting the content of the drawer, identified by
     *         the "content" id in XML.
     */
    public View getContent() {
        return mContent;
    }

    /**
     * Unlocks the SlidingDrawer so that touch events are processed.
     *
     * @see #lock() 
     */
    public void unlock() {
        mLocked = false;
    }

    /**
     * Locks the SlidingDrawer so that touch events are ignores.
     *
     * @see #unlock()
     */
    public void lock() {
        mLocked = true;
    }

    /**
     * Indicates whether the drawer is currently fully opened.
     *
     * @return True if the drawer is opened, false otherwise.
     */
    public boolean isOpened() {
        return mExpanded;
    }

    /**
     * Indicates whether the drawer is scrolling or flinging.
     *
     * @return True if the drawer is scroller or flinging, false otherwise.
     */
    public boolean isMoving() {
        return mTracking || mAnimating;
    }

    private class DrawerToggler implements OnClickListener {
        public void onClick(View v) {
            if (mLocked) {
                return;
            }
            // mAllowSingleTap isn't relevant here; you're *always*
            // allowed to open/close the drawer by clicking with the
            // trackball.

            /*if (mAnimateOnClick) {
                animateToggle();
            } else {
                toggle();
            }*/
            if(mVertical){
            	mPosition = calculateCurrentPosition();
            	switch (mPosition) {
				case BOTTOM:
					mPosition = CENTER_VERTICAL;
					mExpanded = false;
					animateOpen();
					break;
				case CENTER_VERTICAL:
					mPosition = TOP;
					mExpanded = false;
					animateOpen();
					break;
				case TOP:
					mPosition = BOTTOM;
					mExpanded = true;
					animateClose();
					break;
				}
            	
            }
        }
    }
    
    // because user can move handle manually, so need to calculate the current position for animation - Kane
    private int calculateCurrentPosition(){
    	final int baseLine = mHandle.getTop() - mHandleHeight/2 + mBottomOffset;
    	int position = -1;
    	if(baseLine > getCenterPosition()){
    		position = BOTTOM;
    	} 
    	else if(baseLine <= getCenterPosition() && baseLine > mTopOffset){
    		position = CENTER_VERTICAL;
    	} 
    	else {
    		position = TOP;
    	}
    	return position;
    }
	
	private class SlidingHandler extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
                case MSG_ANIMATE:
                    doAnimation(mPosition == CENTER_VERTICAL);
                    break;
            }
        }
    }
	
	 /**
     * Callback invoked when the drawer is opened.
     */
    public static interface OnDrawerOpenListener {
        /**
         * Invoked when the drawer becomes fully open.
         */
        public void onDrawerOpened(Rect contentRect);
    }

    /**
     * Callback invoked when the drawer is closed.
     */
    public static interface OnDrawerCloseListener {
        /**
         * Invoked when the drawer becomes fully closed.
         */
    	public void onDrawerClosed();
    }

    /**
     * Callback invoked when the drawer is scrolled.
     */
    public static interface OnDrawerScrollListener {
        /**
         * Invoked when the user starts dragging/flinging the drawer's handle.
         */
        public void onScrollStarted();

        /**
         * Invoked when the user stops dragging/flinging the drawer's handle.
         */
        public void onScrollEnded(boolean closed, Rect contentRect);
    }

}
