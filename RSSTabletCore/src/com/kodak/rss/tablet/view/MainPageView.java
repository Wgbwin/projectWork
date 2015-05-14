package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.prints.Page;

public abstract class MainPageView<P extends Page, L extends Layer> extends View {
	protected P mPage;
	protected OnLayerClickListener mOnLayerClickListener;
	protected OnLayerDragListener mOnLayerDragListener;

	public MainPageView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public MainPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MainPageView(Context context) {
		super(context);
	}
	
	
	public void setPage(P page) {
		mPage = page;
	}
	
	public P getPage() {
		return mPage;
	}
	
	public void setOnLayerClickListener(OnLayerClickListener onLayerClickListener) {
		mOnLayerClickListener = onLayerClickListener;
	}
	
	public void setOnLayerDragListener(OnLayerDragListener onLayerDragListener) {
		mOnLayerDragListener = onLayerDragListener;
	}
	
	
	
	private float mXDown,mYDown;
	private final static int TOUCH_SLOP = 10;
	private static final long LONG_PRESS_TIME = 500;
	private boolean mIsMoved = false;
	private MotionEvent mCurrentEvent;
	private L mLayerDragged;
	private boolean mIsReleased;
	private Runnable mLongPressRunnable;
	private Bitmap mBitmap4LayerDrag;
	@SuppressWarnings("unchecked")
	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		mCurrentEvent = event;
		switch(event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			mXDown = event.getX();
			mYDown = event.getY();
			mIsMoved = false;
			mIsReleased = false;
			mLayerDragged = null;
			if (mBitmap4LayerDrag != null) {
				if (!mBitmap4LayerDrag.isRecycled()) {
					mBitmap4LayerDrag.recycle();
				}
				mBitmap4LayerDrag = null;
			}
			if (mLongPressRunnable != null) {
				removeCallbacks(mLongPressRunnable);
				mLongPressRunnable = null;
			}
			
			if (mOnLayerDragListener != null) {
				final L touchLayerDown = getTouchOnLayer(event);
				if(touchLayerDown != null){
					mLongPressRunnable = new Runnable() {
						
						@Override
						public void run() {
							if(!mIsReleased && !mIsMoved){
								mLayerDragged = touchLayerDown;
								if (mOnLayerDragListener != null) {
									mBitmap4LayerDrag = getLayerBitmap(mLayerDragged);
									mOnLayerDragListener.onStartDrag(mCurrentEvent, MainPageView.this, mPage, mLayerDragged, mBitmap4LayerDrag);
								}
							}
						}
					};
					postDelayed(mLongPressRunnable, LONG_PRESS_TIME);
				}
				
			}
			
			break;
		case MotionEvent.ACTION_MOVE:
			if(!mIsMoved && (Math.abs(event.getX()-mXDown) > TOUCH_SLOP || Math.abs(event.getY()-mYDown) > TOUCH_SLOP)){
				mIsMoved = true;
			}
			
			if (isDraggingLayer()) {
				mOnLayerDragListener.onDragging(event, this, mPage, mLayerDragged, mBitmap4LayerDrag);
			}
			break;
		case MotionEvent.ACTION_UP:
			mCurrentEvent = null;
			mIsReleased = true;
			if (mLongPressRunnable != null) {
				removeCallbacks(mLongPressRunnable);
				mLongPressRunnable = null;
			}
			
			if(mLayerDragged != null && mOnLayerDragListener != null){
				mOnLayerDragListener.OnDrop(event, this, mPage, mLayerDragged, mBitmap4LayerDrag);
				mLayerDragged = null;
				if (mBitmap4LayerDrag != null) {
					if (!mBitmap4LayerDrag.isRecycled()) {
						mBitmap4LayerDrag.recycle();
					}
					mBitmap4LayerDrag = null;
				}
				return true;
			}
			
			if(mOnLayerClickListener != null
				&& !mIsMoved ){// click on layer
				Layer touchLayer = getTouchOnLayer(event);
				if(touchLayer != null){
					mOnLayerClickListener.onLayerClick(this, mPage, touchLayer,getLayerRect(touchLayer));
					return true;
				}
			}
			
			break;
		case MotionEvent.ACTION_CANCEL:
			mCurrentEvent = null;
			if (mBitmap4LayerDrag != null) {
				if (!mBitmap4LayerDrag.isRecycled()) {
					mBitmap4LayerDrag.recycle();
				}
				mBitmap4LayerDrag = null;
			}
			mIsReleased = true;
			if (mLongPressRunnable != null) {
				removeCallbacks(mLongPressRunnable);
				mLongPressRunnable = null;
			}
			break;
		}
		
		//TODO:this return value will infect parent view's onInterceptTouchEvent
		//Maybe there is a better method to solve this problem
		return true;
	}
	
	public boolean isDraggingLayer() {
		return mOnLayerDragListener != null && mLayerDragged != null && !mIsReleased;
	}
	
	/**
	 * get the layer you touched
	 * @param event
	 * @return the layer you touched. If result is null, it means no layer is touched
	 */
	protected L getTouchOnLayer(MotionEvent event){
		return getTouchOnLayer(event.getX(), event.getY());
	}
	
	protected Bitmap getLayerBitmap(L layer) {
		Bitmap bitmap = getImageBitmap();
		
		if (bitmap != null) {
			RectF rect = getLayerRect(layer);
			float scale = (float)bitmap.getWidth() / getWidth();
			return Bitmap.createBitmap(bitmap, (int) (rect.left * scale), (int) (rect.top * scale), (int) (rect.width() * scale), (int) (rect.height() * scale ));
		} else {
			return null;
		}
		
	}
	
	/**
	 * get the layer you touched
	 * @param x
	 * @param y
	 * @return the layer you touched. If result is null, it means no layer is touched
	 */
	protected L getTouchOnLayer(float x, float y){
		L[] layers = getLayers();
		if(mPage != null && layers != null){
			
			for(int i = layers.length -1; i>=0; i--){
				if(layers[i] == null){
					continue;
				}
				
				if (isPointInLayer(layers[i], x, y)) {
					return layers[i];
				}
			}
		}
		
		return null;
	}
	
	protected boolean isPointInLayer(Layer layer, float x, float y) {
		if (layer != null) {
			PointF touch = new PointF(x,y);
			//inverse rotation for touch point
			RectF rect = getLayerRect(layer);
			PointF center = new PointF((rect.left+rect.right)/2, (rect.top + rect.bottom)/2);
			PointF dst = rotatePoint(touch, center, -layer.angle);
			
			if(rect.contains(dst.x,dst.y)){
				return true;
			}
		}
		
		return false;
	}
	
	public RectF getLayerRect(Layer layer){
		RectF rect = getRectFromROI(layer.location);
		
		RectF newRect = new RectF();
		if(layer.angle == 90 || layer.angle == 270){
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
	
	protected L[] getLayers() {
		return (L[]) (mPage == null ? null : mPage.layers);
	}
	
	/**
	 * rotate point p by point center as circle center
	 * @param p
	 * @param center
	 * @param angel
	 */
	protected PointF rotatePoint(PointF p, PointF center, float angel){
		if(angel == 0){
			return new PointF(p.x,p.y);
		}
		double k = Math.toRadians(angel);
		float x =(float) ((p.x-center.x)*Math.cos(k) +(p.y-center.y)*Math.sin(k)+center.x);
		float y=(float) (-(p.x-center.x)*Math.sin(k) + (p.y-center.y)*Math.cos(k)+center.y); 
		return new PointF(x,y);
	}
	
	protected RectF getRectFromROI(ROI r){
		return getRectFromROI(r, getWidth(), getHeight());
	}
	
	protected RectF getRectFromROI(ROI r, float parentW, float parentH){
		float left = (float) (parentW * r.x / r.ContainerW);
		float top = (float) (parentH * r.y / r.ContainerH);
		float right = left + (float)(parentW * r.w / r.ContainerW);
		float bottom = top + (float) (parentH * r.h / r.ContainerH);
		return new RectF(left, top, right, bottom);
	}
	
	public abstract Bitmap getImageBitmap();
	
	public static interface OnLayerClickListener<V extends MainPageView, P extends Page, L extends Layer> {
		void onLayerClick(V pageView, P page, L layer, RectF layerRect);
	}
	
	public static interface OnLayerDragListener<V extends MainPageView, P extends Page, L extends Layer>{
		void onStartDrag(MotionEvent event, V pageView, P page, L layer, Bitmap bitmap);
		
		void onDragging(MotionEvent event, V pageView, P page, L layer, Bitmap bitmap);
		
		void OnDrop(MotionEvent event, V pageView, P page, L layer, Bitmap bitmap);
	}
	
}
