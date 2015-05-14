package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug.ExportedProperty;

import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.adapter.PhotoBooksProductMainAdapter;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;

public class PhotoBookMainPageView extends MainPageView<PhotobookPage, Layer>{
	private static final String TAG = "PhotoBookMainPageView";
	private PhotoBooksProductMainAdapter mAdapter;
	private Bitmap mContentBitmap,mLeftPageEdgeBitmap,mRightPageEdgeBitmap,mLeftShadowBitmap,mRightShadowBitmap;
	private boolean mIsLeft;
	private int mPosition;
	private RectF mContentRect;
	private RectF mLeftPageEdgeRect;
	private RectF mRightPageEdgeRect;
	private RectF mLeftShadowRect;
	private RectF mRightShadowRect;
	private RectF mBorderRect;//draw border when selected
	private Paint mBorderPaint;
	private final static float mBorderWidth = 5, mBorderMargin = mBorderWidth/2; 
	private OnPageSelectedListener mOnPageSelectedListener;
	private boolean mSelected = false;
	private boolean mEditable = true;
	private int mW,mH;
	private static final long LONG_PRESS_TIME = 500;
	private PhotoBookEditLayer mLayerEdit;
	

	public PhotoBookMainPageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public PhotoBookMainPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PhotoBookMainPageView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		mLayerEdit = ((PhotoBooksProductActivity) context).layerEdit;
		
		setClickable(true);
		mContentRect = new RectF();
		mLeftPageEdgeRect = new RectF();
		mRightPageEdgeRect = new RectF();
		mLeftShadowRect = new RectF();
		mRightShadowRect = new RectF();
		mBorderRect = new RectF();
		
		mBorderPaint = new Paint();
		mBorderPaint.setColor(0xFFFBBA06);
		mBorderPaint.setStrokeWidth(mBorderWidth);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(mAdapter == null)	return;
		if(mIsLeft && mPosition == 0) return;
		if(!mIsLeft && mPosition == mAdapter.getCount()-1) return;
		
		initBorderRect();
		
		if(mContentBitmap != null){
			canvas.drawBitmap(mContentBitmap, null, mContentRect, null);
		}
		
		if(mLeftPageEdgeBitmap != null){
			canvas.drawBitmap(mLeftPageEdgeBitmap, null, mLeftPageEdgeRect, null);
			mBorderRect.left = mLeftPageEdgeRect.right;
		}
		
		if(mRightPageEdgeBitmap != null){
			canvas.drawBitmap(mRightPageEdgeBitmap, null, mRightPageEdgeRect, null);
			mBorderRect.right -= mRightPageEdgeRect.width();
		}
		
		if(mLeftShadowBitmap != null){
			canvas.drawBitmap(mLeftShadowBitmap, null, mLeftShadowRect, null);
		}
		
		if(mRightShadowBitmap != null){
			canvas.drawBitmap(mRightShadowBitmap, null, mRightShadowRect, null);
		}
		
		if(mSelected){
			//draw border
			//Be carefurl, if we draw line in the border of view, 1/2 line is out of view, so we need to resize it
			canvas.drawLine(mBorderRect.left, mBorderRect.top + mBorderMargin, mBorderRect.right, mBorderRect.top + mBorderMargin, mBorderPaint);
			canvas.drawLine(mBorderRect.left + mBorderMargin, mBorderRect.top, mBorderRect.left + mBorderMargin, mBorderRect.bottom, mBorderPaint);
			canvas.drawLine(mBorderRect.right - mBorderMargin, mBorderRect.bottom, mBorderRect.right - mBorderMargin, mBorderRect.top, mBorderPaint);
			canvas.drawLine(mBorderRect.right, mBorderRect.bottom - mBorderMargin, mBorderRect.left, mBorderRect.bottom - mBorderMargin, mBorderPaint);
		}
		
	}
	
	@Override
	public void setSelected(boolean selected) {
		this.mSelected = selected;
		postInvalidate();
	}
	
	@Override
	@ExportedProperty
	public boolean isSelected() {
		return mSelected;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.mW = w;
		this.mH = h;
		
		float edgeWidth = w/70.0f;
		float shadowWidth = w/15f;
		
		mLeftPageEdgeRect.left = 0;
		mLeftPageEdgeRect.top = 0;
		mLeftPageEdgeRect.right =  edgeWidth;
		mLeftPageEdgeRect.bottom = h;
		
		mRightPageEdgeRect.left = w -  edgeWidth;
		mRightPageEdgeRect.top = 0;
		mRightPageEdgeRect.right = w;
		mRightPageEdgeRect.bottom = h;
		
		mContentRect.left = mLeftPageEdgeBitmap == null ? 0 : edgeWidth;
		mContentRect.top = 0;
		mContentRect.right = mRightPageEdgeBitmap == null ? w : w-edgeWidth;
		mContentRect.bottom = h;
		
		mLeftShadowRect.left = w-shadowWidth;
		mLeftShadowRect.top = 0;
		mLeftShadowRect.right = w;
		mLeftShadowRect.bottom = h;
		
		mRightShadowRect.left = 0;
		mRightShadowRect.top = 0;
		mRightShadowRect.right = shadowWidth;
		mRightShadowRect.bottom = h;
		
	}
	
	private float mXDown,mYDown;
	private Layer mLayerDragged;
	private boolean mIsReleased;
	private MotionEvent mCurrentEvent;
	private final int TOUCH_SLOP = 10;
	private boolean mIsMoved = false;
	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		mCurrentEvent = event;
		PhotobookPage page = (PhotobookPage) mPage;
		switch(event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			mXDown = event.getX();
			mYDown = event.getY();
			mIsMoved = false;
			mIsReleased = false;
			mLayerDragged = null;
			final Layer touchLayerDown = getTouchOnLayer(event);
			if(touchLayerDown != null){
				postDelayed(new Runnable() {
					
					@Override
					public void run() {
						if(!mIsReleased && !mIsMoved){
							mLayerDragged = touchLayerDown;
							mLayerEdit.onDragLayerEvent(mCurrentEvent,PhotoBookMainPageView.this,mLayerDragged);
						}
					}
				}, LONG_PRESS_TIME);
			}
			 
			break;
		case MotionEvent.ACTION_MOVE:
			if(!mIsMoved && (Math.abs(event.getX()-mXDown) > TOUCH_SLOP || Math.abs(event.getY()-mYDown) > TOUCH_SLOP)){
				mIsMoved = true;
			}
			
			if(mLayerDragged != null){
				return mLayerEdit.onDragLayerEvent(event,this,mLayerDragged);
			}
			break;
		case MotionEvent.ACTION_UP:
			mCurrentEvent = null;
			mIsReleased = true;
			if(mLayerDragged != null){
				boolean r = mLayerEdit.onDragLayerEvent(event,this,mLayerDragged);
				mLayerDragged = null;
				return r;
			}
			
			if(mOnLayerClickListener != null
				&& !mIsMoved ){// click on layer
				Layer touchLayer = getTouchOnLayer(event);
				if(touchLayer != null){
					mOnLayerClickListener.onLayerClick(this, mPage, touchLayer,getLayerRect(touchLayer));
					return true;
				}
			}
			
			if(mEditable && !mIsMoved && mOnPageSelectedListener != null && mPage!= null && !PhotobookPage.TYPE_BACK_COVER.equals(page.pageType)){//click on page
				if(!mSelected){
					mOnPageSelectedListener.onPageSelected(this,page);
					setSelected(true);
				}
				return true;
			}
			break;
		}
		return super.onTouchEvent(event);
	}
	
	public void setImageBitmap(Bitmap bitmap){
		this.mContentBitmap = bitmap;
		postInvalidate();
	}
	
	public void setOnPageSelectedListener(OnPageSelectedListener onPageSelectedListener){
		this.mOnPageSelectedListener = onPageSelectedListener; 
	}
	
	public RectF getLayerRect(Layer layer){
		RectF rect = getRectFromROI(layer.location);
		int mr = EditImageView.getRotationsFromLayer(layer)[0];
		
		RectF newRect = new RectF();
		if(mr == 90 || mr==270){
			//rotate rect by center( 90 degree )
			newRect.left = rect.left + (rect.width() - rect.height())/2;
			newRect.right = newRect.left + rect.height();
			newRect.top = rect.top + (rect.height() - rect.width())/2;
			newRect.bottom = newRect.top + rect.width();
		}else{
			newRect = rect;
		}
		return newRect;
	}
	
	/**
	 * @param xOnScreen
	 * @param yOnScreen
	 * @return Layer
	 */
	public Layer pointToLayer(View view ,PhotobookPage p, float xOnScreen, float yOnScreen) {								
		Layer layer = null;
		if (p == null) return layer;
		if (view == null) return layer;	
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		float relativeX = xOnScreen - location[0];
		float relativeY = yOnScreen - location[1];		
		for (int i = 0; i < p.layers.length; i++) {
			Layer mLayer = p.layers[i];
			if (mLayer == null) continue;
			RectF rect = getLayerRect(mLayer);					
			if (Layer.TYPE_IMAGE.equals(mLayer.type) && rect.contains(relativeX, relativeY)) {				
				return mLayer;
			}
		}				
		return layer;
	}		

	public void setBasicInfo(PhotoBooksProductMainAdapter adapter,PhotobookPage page,int position, boolean isLeft){
		this.mAdapter = adapter;
		this.mPosition = position;
		this.mIsLeft = isLeft;
		this.mPage = page;
		mLeftShadowBitmap = null;
		mRightShadowBitmap = null;
		mLeftPageEdgeBitmap = null;
		mRightPageEdgeBitmap = null;
		
		mEditable = PhotoBookProductUtil.getPhotobookPageEditable(page);
		
		if(position != 0 && isLeft){
			mLeftShadowBitmap = adapter.leftShadowBitmap;
		}
		
		if(position != adapter.getCount()-1 && !isLeft){
			mRightShadowBitmap = adapter.rightShadowBitmap;
		}
		
		if(position > 1 && position < adapter.getCount()-1  && isLeft){
			mLeftPageEdgeBitmap = adapter.leftPageEdgeBitmap;
		}
		
		if(position > 0 && position < adapter.getCount()-2 && !isLeft){
			mRightPageEdgeBitmap = adapter.rightPageEdgeBitmap;
		}
		
		postInvalidate();
	}
	
	public boolean isLeft(){
		return mIsLeft;
	}
	
	@Override
	public Bitmap getImageBitmap() {
		return mContentBitmap;
	}
	
	public static interface OnPageSelectedListener{
		void onPageSelected(PhotoBookMainPageView pageView,PhotobookPage page);
	}
	
	private void initBorderRect(){
		mBorderRect.left = 0;
		mBorderRect.top = 0;
		mBorderRect.right = mW;
		mBorderRect.bottom = mH;
	}
	
}
