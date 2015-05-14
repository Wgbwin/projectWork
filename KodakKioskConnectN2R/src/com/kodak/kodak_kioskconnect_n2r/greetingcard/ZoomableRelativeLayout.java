package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.ROI;

public class ZoomableRelativeLayout extends RelativeLayout {

	private static final int INVALID_POINTER_ID = -1;
	private static final String TAG = "ZoomableRelativeLayout"; 
	private Context mContext;
	private Bitmap mDrawable;
	private DrawableImageView mImageView;
	private float mPosX;
	private float mPosY;
	ZoomableRelativeLayout tempLayout;
	private float mLastTouchX;
	private float mLastTouchY;
	private int mActivePointerId = INVALID_POINTER_ID;
	private int screenWidth  = -1;
	private int screenHeight = -1;
	private int parentWidth = -1;
	private int parentHeight = -1;
	private long downTime = 0;
	private GreetingCardPage mCurrentPage;
	private LinkedList<GreetingCardPage> mCurrentArrayPages = new LinkedList<GreetingCardPage>();
	private Handler zoomHandler;
	private final int SINGLECLICK = 0;
	private final int CLICKTOSEND = 7;
	private final int SLIPTOLEFT = 1;
	private final int SLIPTOTOP = 2;
	private final int SLIPTORIGHT = 3;
	private final int SLIPTOBOTTOM = 4;
	private final int DOUBLESCALE = 5;
	private final int SINGLESCALE = 6;
	private final int RESIZE = 10;
	private final int REVALIDATE = 19;
	private int paramWidth;
	private int paramHeight;
	private LayoutParams mScaleLayoutParams;
	private boolean isScaleOut = true;
	private int pCount = 0;
	private float oldDist = 0;
	private float newDist = 0;
	private int lastX;
	private int lastY;
	private int sysVersion;
	private boolean isPreview = false;
//	private boolean canMove = true;
	
	private boolean isAnimaling = false;
	private RelativeLayout frontLayout;
	private RelativeLayout behindLayout;
	private DrawableImageView frontImg;
	private DrawableImageView behindImg;
	private Rotate3dAnimation mAnimation;
	private GreetingCardManager manager;
	private int accumX = 0;
	private int accumY = 0;
	private int left = 0;
	private int top = 0;
	private int right = 0;
	private int bottom = 0;
	
	private boolean canFling = true;
	private boolean overWithSend = false;
	
	private boolean isForwarding = false;
	private boolean isPageEdit = false;
	public boolean isScaleOut() {
		return isScaleOut;
	}

//	public boolean isCanMove() {
//		return canMove;
//	}
//	public void setCanMove(boolean canMove) {
//		this.canMove = canMove;
//	}
	public void setScaleOut(boolean isScaleOut) {
		this.isScaleOut = isScaleOut;
	}
	public boolean isAnimaling() {
		return isAnimaling;
	}
	public void setAnimaling(boolean isAnimaling) {
		this.isAnimaling = isAnimaling;
	}

	public LayoutParams getmScaleLayoutParams() {
		return mScaleLayoutParams;
	}

	public void setmScaleLayoutParams(LayoutParams mScaleLayoutParams) {
		this.mScaleLayoutParams = mScaleLayoutParams;
	}

	public boolean isPreview() {
		return isPreview;
	}

	public void setPreview(boolean isPreview) {
		this.isPreview = isPreview;
	}
	public boolean isForwarding() {
		return isForwarding;
	}
	public void setForwarding(boolean isForwarding) {
		this.isForwarding = isForwarding;
	}

	public boolean isPageEdit() {
		return isPageEdit;
	}

	public boolean isOverWithSend() {
		return overWithSend;
	}

	public void setOverWithSend(boolean overWithSend) {
		this.overWithSend = overWithSend;
	}

	public boolean isCanFling() {
		return canFling;
	}

	public void setCanFling(boolean canFling) {
		this.canFling = canFling;
	}

	public void setPageEdit(boolean isPageEdit) {
		this.isPageEdit = isPageEdit;
	}

	public int getParentWidth() {
		return parentWidth;
	}

	public void setParentWidth(int parentWidth) {
		this.parentWidth = parentWidth;
	}

	public int getParentHeight() {
		return parentHeight;
	}

	public void setParentHeight(int parentHeight) {
		this.parentHeight = parentHeight;
	}

	public GreetingCardManager getManager() {
		return manager;
	}

	public void setManager(GreetingCardManager manager) {
		this.manager = manager;
	}

	public Rotate3dAnimation getmAnimation() {
		return mAnimation;
	}

	public void setmAnimation(Rotate3dAnimation mAnimation) {
		this.mAnimation = mAnimation;
	}

	public int getParamWidth() {
		return paramWidth;
	}

	public RelativeLayout getFrontLayout() {
		return frontLayout;
	}

	public void setFrontLayout(RelativeLayout firstLayout) {
		this.frontLayout = firstLayout;
	}

	public RelativeLayout getBehindLayout() {
		return behindLayout;
	}

	public void setBehindLayout(RelativeLayout secondLayout) {
		this.behindLayout = secondLayout;
	}

	public DrawableImageView getFrontImg() {
		return frontImg;
	}

	public void setFrontImg(DrawableImageView frontImg) {
		this.frontImg = frontImg;
	}

	public DrawableImageView getBehindImg() {
		return behindImg;
	}

	public void setBehindImg(DrawableImageView behindImg) {
		this.behindImg = behindImg;
	}

	public void setParamWidth(int paramWidth) {
		this.paramWidth = paramWidth;
	}

	public int getParamHeight() {
		return paramHeight;
	}

	public void setParamHeight(int paramHeight) {
		this.paramHeight = paramHeight;
	}

	public Handler getZoomHandler() {
		return zoomHandler;
	}

	public void setZoomHandler(Handler zoomHandler) {
		this.zoomHandler = zoomHandler;
	}

	private boolean clickable;
	
	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	private float mScaleFactor = 1.0f;
	
	public float getmScaleFactor() {
		return mScaleFactor;
	}

	public void setmScaleFactor(float mScaleFactor) {
		this.mScaleFactor = mScaleFactor;
	}

	public float getmPosX() {
		return mPosX;
	}

	public void setmPosX(float mPosX) {
		this.mPosX = mPosX;
	}

	public float getmPosY() {
		return mPosY;
	}

	public void setmPosY(float mPosY) {
		this.mPosY = mPosY;
	}

	public Bitmap getmDrawable() {
		return mDrawable;
	}

	public void setmDrawable(Bitmap mDrawable) {
		this.mDrawable = mDrawable;
	}

	public DrawableImageView getmImageView() {
		return mImageView;
	}

	public void setmImageView(DrawableImageView mImageView) {
		this.mImageView = mImageView;
	}

	public GreetingCardPage getmCurrentPage() {
		return mCurrentPage;
	}

	public void setmCurrentPage(GreetingCardPage mCurrentPage) {
		this.mCurrentPage = mCurrentPage;
	}

	public LinkedList<GreetingCardPage> getmCurrentArrayPages() {
		return mCurrentArrayPages;
	}

	public void setmCurrentArrayPages(LinkedList<GreetingCardPage> mCurrentArrayPages) {
		this.mCurrentArrayPages = mCurrentArrayPages;
	}

	public ZoomableRelativeLayout(Context context) {
		this(context, null, 0);
		tempLayout = this;
	}
	
	public ZoomableRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		tempLayout = this;
	}

	public ZoomableRelativeLayout(Context context, AttributeSet attrs,
			int defStyle ) {
		super(context, attrs, defStyle);
		this.mContext = context;
		setWillNotDraw(false);
		tempLayout = this;
		sysVersion = Integer.parseInt(VERSION.SDK); 
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		 
		if (isClickable()) {
			final int action = event.getAction();
			switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN: {
				PrintHelper.canValidate = false;
				pCount = 1;
				final float x = event.getX();
				final float y = event.getY();
				downTime = new Date().getTime();
				mLastTouchX = x;
				mLastTouchY = y;
				lastX = (int) event.getRawX();
				lastY = (int) event.getRawY();
				mActivePointerId  = event.getPointerId(0);
				
				break;
			}
			
			case MotionEvent.ACTION_POINTER_DOWN:
				if (!isPageEdit) {
					pCount += 1; 
					setCanFling(false);
					oldDist = spacing(event); 
				}
				break;
			
			case MotionEvent.ACTION_MOVE: {
				Log.i(TAG, "####### isAnimaling is " + isAnimaling());
				if (!isPageEdit && !isAnimaling()) {
					if (pCount >= 2) {  
						 newDist = spacing(event);
						}  else if (pCount == 1) {
							if (getmScaleFactor() == 2.0f) {
								int dx = (int)event.getRawX() - lastX;
								int dy = (int)event.getRawY() - lastY;
								
								int rangeWidth = 0;
								if (PrintHelper.model == 2) {
									rangeWidth = getParamWidth()*4;
								} else {
									rangeWidth = getParamWidth()*2;
								}
								if (rangeWidth < getScreenWidth()) {
									left = tempLayout.getLeft();
									right = tempLayout.getRight();
								} else {
									if (tempLayout.getLeft() < 0 && tempLayout.getLeft() > -getScreenWidth()) {
										left = tempLayout.getLeft() + dx;
										right = tempLayout.getRight() + dx;
									} else if (tempLayout.getLeft() >= 0 && dx < 0) {
										left = tempLayout.getLeft() + dx;
										right = tempLayout.getRight() + dx;
									} else if (tempLayout.getLeft() <= -getScreenWidth() && dx > 0) {
										left = tempLayout.getLeft() + dx;
										right = tempLayout.getRight() + dx;
									}
									
								}
								
								if (getParamHeight()*2 < getScreenHeight()) {
									top = tempLayout.getTop();
									 bottom = tempLayout.getBottom();
								} else {
									if (tempLayout.getTop() < 0 && tempLayout.getTop() > - getScreenHeight()) {
										top = tempLayout.getTop() + dy;
										 bottom = tempLayout.getBottom() + dy;
									} else if (tempLayout.getTop() >= 0 && dy < 0) {
										top = tempLayout.getTop() + dy;
										 bottom = tempLayout.getBottom() + dy;
									} else if (tempLayout.getTop() <= - getScreenHeight() && dy > 0) {
										top = tempLayout.getTop() + dy;
										 bottom = tempLayout.getBottom() + dy;
									}
								}
					                
					                tempLayout.layout(left, top, right, bottom);
					                lastX = (int) event.getRawX();
					                lastY = (int) event.getRawY();
					          }
						}
				}
				break; 
				
			}
			
			case MotionEvent.ACTION_UP: {
					if (!isAnimaling()) {
					pCount = 0;
					mActivePointerId = INVALID_POINTER_ID;
					long time = new Date().getTime() - downTime;
					float distanceX = event.getX() - mLastTouchX;
					float distanceY = event.getY() - mLastTouchY;
					if (time < 200 && distanceX > -20 && distanceX < 20 && distanceY > -20 && distanceY < 20) {
						if (isPageEdit()) {
							Message msg = new Message();
							msg.what = CLICKTOSEND;
								if (getZoomHandler() != null) {
							getZoomHandler().sendMessage(msg);
								}
						} else {
							GreetingCardPageLayer layer = getClickedLayer(event);
							Log.i(TAG, "iiiiiiiiiiiiiiiiisForwarding() = " + isForwarding());
							if (layer != null && !isPreview() && !isForwarding()) {
									setForwarding(true);
									getManager().setEditLayer(layer.contentId, layer.holeIndex);
									if (getmScaleFactor() == 2.0f) { // the situation that scale equals 2.0f, the view will zoom in firstly
//										PrintHelper.canValidate = false;
										scaleAfterClick(layer);
									} else {
										Bundle data = new Bundle();
										data.putSerializable("clickedLayer", layer);
										Message msg = new Message();
										msg.what = SINGLECLICK;
										msg.setData(data);
											if (getZoomHandler() != null) {
										getZoomHandler().sendMessage(msg);
									}
							} 
						}
							}
						} else if (time < 400 && isCanFling() && !isPageEdit() && getmScaleFactor() != 2.0f) {
						if (getZoomHandler() != null) {
							if (distanceX < -100) {
								getZoomHandler().sendEmptyMessage(SLIPTOLEFT);
							} else if (distanceX > 100){
								getZoomHandler().sendEmptyMessage(SLIPTORIGHT);
							}
							
							if (distanceY < -100) {
								getZoomHandler().sendEmptyMessage(SLIPTOTOP);
							} else if (distanceY > 100) {
								getZoomHandler().sendEmptyMessage(SLIPTOBOTTOM);
							}
						}
					}
					
					setCanFling(true);
						
						PrintHelper.canValidate = true;
						if (getZoomHandler() != null) {
							getZoomHandler().sendEmptyMessage(REVALIDATE);
						}
					}
				break;
			}
		
			case MotionEvent.ACTION_CANCEL: {
				mActivePointerId = INVALID_POINTER_ID;
				break;
			}
			
			case MotionEvent.ACTION_POINTER_UP: {
				if (!isPageEdit) {
					pCount -= 1; 
					final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
		            >> MotionEvent.ACTION_POINTER_INDEX_SHIFT; 
		            final int pointerId = event.getPointerId(pointerIndex);
		            if (pointerId == mActivePointerId) {
		            	final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
		            	mLastTouchX = event.getX(newPointerIndex);
		            	mLastTouchY = event.getY(newPointerIndex);
		            	mActivePointerId = event.getPointerId(newPointerIndex);
		            }
					 if (newDist > oldDist + 1 && getmScaleFactor() != 2.0f && !isAnimaling()) {  
						 setAnimaling(true);
					   scaleOutAnimation(true);
					 }  
					 if (newDist < oldDist - 1 && getmScaleFactor() == 2.0f && !isAnimaling()) {  
						 setAnimaling(true);
					    scaleOutAnimation(false);
					 }
				}
				break;
			}
			}
		}
		return true;
	}
	
	private float spacing(MotionEvent event) {  
	    float x = event.getX(0) - event.getX(1);  
	    float y = event.getY(0) - event.getY(1);  
	    return FloatMath.sqrt(x * x + y * y);  
	}  
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (getmScaleFactor() == 2.0f) {
			Log.i(TAG, "tempLayout.getTop() = " + " , screenHeight = " + getScreenHeight() + " , getParamHeight() = " + getParamHeight()*2);
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
	}
	
	private void scaleAfterClick(final GreetingCardPageLayer mCardPageLayer) {
		ScaleAnimation centerScaleAnimation;
		setmScaleFactor(0.5f);
//		tempLayout.getmImageView().setmScaleFactor(0.5f);
		centerScaleAnimation = new ScaleAnimation(2, 1f, 2, 1f,  
	           Animation.RELATIVE_TO_SELF, 0.5f,  
	           Animation.RELATIVE_TO_SELF, 0.5f);
		Message msg = new Message();
        msg.what = RESIZE;
        Bundle data = new Bundle();
        Log.i(TAG, "scale on animation end mCardPageLayer is null ? " + (mCardPageLayer==null));
        data.putSerializable("targetLayer", mCardPageLayer);
        msg.setData(data);
        if (getZoomHandler() != null) {
     	   getZoomHandler().sendMessage(msg);
        }
        this.setVisibility(View.INVISIBLE);
		centerScaleAnimation.setDuration(500);  
        centerScaleAnimation.setFillAfter(true);  
        centerScaleAnimation.setAnimationListener(new AnimationListener() {  
        	  
            @Override  
            public void onAnimationStart(Animation animation) {  
                // TODO Auto-generated method stub  
            }  
  
            @Override  
            public void onAnimationRepeat(Animation animation) {  
                // TODO Auto-generated method stub  
  
            }  
   
            @Override  
            public void onAnimationEnd(Animation animation) {
//               if (sysVersion > 10) {
            	   ZoomableRelativeLayout.this.setVisibility(View.VISIBLE);
//            	   for (int i = 0 ; i < tempLayout.getChildCount() ; i ++) {
//                   	RelativeLayout childLayout = (RelativeLayout) tempLayout.getChildAt(i); 
//                   	childLayout.setVisibility(View.VISIBLE);
//                   }
//            	}
               tempLayout.setEnabled(true);
               clearAnimation();
               setAnimaling(false);
//               msg.what = RESIZE;
//               Bundle data = new Bundle();
//               Log.i(TAG, "scale on animation end mCardPageLayer is null ? " + (mCardPageLayer==null));
//               data.putSerializable("targetLayer", mCardPageLayer);
//               msg.setData(data);
//               if (getZoomHandler() != null) {
//            	   getZoomHandler().sendMessage(msg);
//               }
            }  
        });
        
        this.setAnimation(centerScaleAnimation);  
//        if (sysVersion > 10) {
//        	this.setVisibility(View.INVISIBLE);
//        	for (int i = 0 ; i < tempLayout.getChildCount() ; i ++) {
//            	RelativeLayout childLayout = (RelativeLayout) tempLayout.getChildAt(i); 
//            	childLayout.setVisibility(View.INVISIBLE);
//            }
//        }
        
        // start animation
        centerScaleAnimation.startNow();  
       tempLayout.setEnabled(false);
	}
	
	public void scaleOutAnimation(boolean isScaleOut) {
		ScaleAnimation centerScaleAnimation;
		PrintHelper.canValidate = false;
		if (isScaleOut) {
			setmScaleFactor(2.0f);
			centerScaleAnimation = new ScaleAnimation(0.5f, 1f, 0.5f, 1f,  
	                Animation.RELATIVE_TO_SELF, 0.5f,  
	                Animation.RELATIVE_TO_SELF, 0.5f);
		} else {
			setmScaleFactor(0.5f);
			centerScaleAnimation = new ScaleAnimation(2, 1f, 2, 1f,  
	                Animation.RELATIVE_TO_SELF, 0.5f,  
	                Animation.RELATIVE_TO_SELF, 0.5f);
		}
		Message msg = new Message();
        msg.what = RESIZE;
        if (getZoomHandler() != null) {
     	   getZoomHandler().sendEmptyMessage(RESIZE);
        }
        this.setVisibility(View.INVISIBLE);
		centerScaleAnimation.setDuration(500);  
        centerScaleAnimation.setFillAfter(true);  
        centerScaleAnimation.setAnimationListener(new AnimationListener() {  
        	  
            @Override  
            public void onAnimationStart(Animation animation) {  
                // TODO Auto-generated method stub  
            }  
  
            @Override  
            public void onAnimationRepeat(Animation animation) {  
                // TODO Auto-generated method stub  
  
            }  
   
            @Override  
            public void onAnimationEnd(Animation animation) {
            Log.i(TAG, "scale onAnimationEnd is called!");
            	 ZoomableRelativeLayout.this.setVisibility(View.VISIBLE);
            ZoomableRelativeLayout.this.setEnabled(true);
               clearAnimation();
               setAnimaling(false);
            }  
        });
         
        	this.setAnimation(centerScaleAnimation);  
        centerScaleAnimation.start(); 
        this.setEnabled(false);
        
        // start animation
        accumX = 0;
        accumY = 0;
        left = 0;
        top = 0;
        right = 0;
        bottom = 0;
        
	}
	 
	/**
	 * judge whether one layer is clicked
	 */
	private GreetingCardPageLayer getClickedLayer(MotionEvent event) {
		GreetingCardPageLayer result = null;
		float x = event.getX();
		float y = event.getY();
		int pagesCount = getmCurrentArrayPages().size();
		if (pagesCount == 1) {
			result = getLayerDirectly(event, getmCurrentArrayPages().get(0),1,1);
		} else if (pagesCount == 2) {
			float layoutWidth = getmCurrentArrayPages().get(0).getPageWidth()*2;
			float scale  = (getmScaleFactor() == 2.0f) ? 2.0f : 1.0f;
			
			int moveX = 0;
			if (scale == 1.0f) {
				if (tempLayout.getChildAt(0).getLeft() > tempLayout.getChildAt(1).getLeft()) {
					moveX = tempLayout.getChildAt(1).getLeft();
				} else {
					moveX = tempLayout.getChildAt(0).getLeft();
				}
				
			} else if (scale == 2.0f) {
				moveX = getParentWidth() - getmCurrentArrayPages().get(0).getPageWidth()*2;
			}
			
			float distance = x - moveX;
			Log.i(TAG, "scale click distance = " + distance + " , moveX = " + moveX + " , layoutWidth*scale/2 = " + layoutWidth*scale/2);
			if (distance < layoutWidth*scale/2) {
				result = getLayerDirectly(event, getmCurrentArrayPages().get(0),1,2);
				getManager().setEditPage(2);
				getManager().setEditPageIndex(2);
			} else if (distance > layoutWidth*scale/2){
				result = getLayerDirectly(event, getmCurrentArrayPages().get(1),2,2);
				getManager().setEditPage(3);
				getManager().setEditPageIndex(3);
			}
		}
		
		return result;
	}
	
	private GreetingCardPageLayer getLayerDirectly(MotionEvent event,GreetingCardPage mCurrentPage,int ratio,int pageCount) {
		ArrayList<GreetingCardPageLayer> mResultLayers = new ArrayList<GreetingCardPageLayer>();
		GreetingCardPageLayer result = null;
		GreetingCardPageLayer[] mCardPageLayers = null;
		float x = event.getX();
		float y = event.getY();
		mCardPageLayers = mCurrentPage.layers;
		float layoutWidth = mCurrentPage.getPageWidth();
		float layoutHeight = mCurrentPage.getPageHeight();
		float scale  = (getmScaleFactor() == 2.0f) ? 2.0f : 1.0f;
		Log.i(TAG, "page layoutWidth = " + layoutWidth);
		Log.i(TAG, "page layoutHeight = " + layoutHeight);
		Log.i(TAG, "page current scale = " + scale);
		
		int moveX = 0;
		int moveY = 0;
		if (scale == 1.0f) {
			if (pageCount == 1) {
				moveX = tempLayout.getChildAt(1).getLeft();
				moveY = tempLayout.getChildAt(1).getTop();
				if (moveX == 0 && moveY == 0) {
					moveX = tempLayout.getChildAt(0).getLeft();
					moveY = tempLayout.getChildAt(0).getTop();
				}
			} else if (pageCount == 2) {
				if (ratio == 1) {
					if (tempLayout.getChildAt(0).getLeft() > tempLayout.getChildAt(1).getLeft()) {
						moveX = tempLayout.getChildAt(1).getLeft();
						moveY = tempLayout.getChildAt(1).getTop();
					} else {
						moveX = tempLayout.getChildAt(0).getLeft();
						moveY = tempLayout.getChildAt(0).getTop();
					}
				} else if (ratio == 2) {
					if (tempLayout.getChildAt(0).getLeft() > tempLayout.getChildAt(1).getLeft()) {
						moveX = tempLayout.getChildAt(0).getLeft();
						moveY = tempLayout.getChildAt(0).getTop();
					} else {
						moveX = tempLayout.getChildAt(1).getLeft();
						moveY = tempLayout.getChildAt(1).getTop();
					}
				}
			}
		} else if (scale == 2.0f) {
			if (pageCount == 1) {
				moveX = getParentWidth() - mCurrentPage.getPageWidth()*ratio;
			} else if (pageCount == 2) {
				if (ratio == 1) {
					moveX = getParentWidth() - 2*mCurrentPage.getPageWidth();
				} else if (ratio == 2) {
					moveX = getParentWidth();
				}
			}
			
			moveY = getParentHeight() - mCurrentPage.getPageHeight();
		}
		if (pageCount == 2) {
			PrintHelper.editedPageIndex = ratio;
			Log.i(TAG, "zzzzzz zoomable click PrintHelper.editedPageIndex = " + PrintHelper.editedPageIndex);
		}
		
		Log.i(TAG, "page moveX = " + moveX + " , moveY = " + moveY + " , x = " + x + " , y = " + y);
		for (int i = 0 ; i < mCardPageLayers.length ; i ++) {
			GreetingCardPageLayer layer = mCardPageLayers[i];
			ROI mRoi = layer.location;
			float roiX = (float) ((layoutWidth*scale) * mRoi.x / mRoi.ContainerW);
			float roiY = (float) ((layoutHeight*scale) * mRoi.y / mRoi.ContainerH);
			float roiW = (float) ((layoutWidth*scale) * mRoi.w / mRoi.ContainerW);
			float roiH = (float) ((layoutHeight*scale) * mRoi.h / mRoi.ContainerH);
			
			float rangeFormerX = 0;
			float rangeLaterX = 0;
			float rangeFormerY = 0;
			float rangeLaterY = 0;
			
			rangeFormerX = roiX + moveX;
			rangeLaterX = roiX + roiW + moveX;
			rangeFormerY = roiY + moveY;
			rangeLaterY = roiY + roiH + moveY;
			
			if (x > rangeFormerX && x < rangeLaterX && y > rangeFormerY && y < rangeLaterY) {
				result = layer;
				Log.i(TAG, "page current rangeFormerX = " + rangeFormerX + " , rangeLaterX = " + rangeLaterX + " , rangeFormerY = " 
						+ rangeFormerY + " , rangeLaterY = " + rangeLaterY);
				mResultLayers.add(layer);
			}
		}
		  
		if (!mResultLayers.isEmpty()) {
			if (mResultLayers.size() == 1) {
				result = mResultLayers.get(0);
			} else {
				result = mResultLayers.get(0);
				ROI firstRoi = mResultLayers.get(0).location;
				float compareW = (float) ((layoutWidth*scale) * firstRoi.w / firstRoi.ContainerW);
				for (int i = 1 ; i < mResultLayers.size() ; i ++) {
					ROI mRoi = mResultLayers.get(i).location;
					float roiW = (float) ((layoutWidth*scale) * mRoi.w / mRoi.ContainerW);
					if (roiW < compareW) {
						result = mResultLayers.get(i);
						compareW = roiW;
					}
				}
			}
		}
		return result;
	}

}
