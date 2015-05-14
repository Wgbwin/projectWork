package com.kodak.rss.tablet.view;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.PhotoBooksProductPagesAdapter;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;

public class DragGridView extends GridView {

	private long dragResponseMS = 500;

	private int mDragPosition;
	private PhotoBookPageView mStartDragItemView = null;	
	private View popUpImageView;
	private Bitmap mDragBitmap;
	private String posStr = "";

	private int moveX;
	private int moveY;
	private float mRawX,mRawY;	
	
	private PhotobookPage dragPage;	
	private PhotobookPage toPage;
	
	private List<PhotobookPage> pageItems;
	private LinearLayout dragCurrentPositionView;
	private Handler mHandler;
	private DisplayMetrics dm;
	
	private Context context;
	
	private int speed = 120;
	private int mDownScrollBorder;
	private int mUpScrollBorder;
	private Rect viewRect;
	private DragHelper dragHelper;
	
	public DragGridView(Context context) {
		this(context, null);
	}

	public DragGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;	
		viewRect = new Rect();
		dm = getResources().getDisplayMetrics();
		popUpImageView = LayoutInflater.from(context).inflate(R.layout.popup_single_page, null);	
		this.dragHelper = new AnimationDragHelper(context,popUpImageView);			
		setFocusable(false);
		setFocusableInTouchMode(false);
	}
	
	public void setHandler(Handler handler){
		this.mHandler = handler;		
	}	

	private Runnable mLongClickRunnable = new Runnable() {

		@Override
		public void run() {
			if (dragCurrentPositionView != null) {
				if (!viewRect.contains((int)mRawX, (int)mRawY))return;
				dragHelper.createDragImage(mDragBitmap,posStr);	
				hideDropItem();
				mHandler.removeCallbacks(mLongClickRunnable);
			}
		}
	};

	private float rawX, rawY;	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		moveX = (int) ev.getX();
		moveY = (int) ev.getY();
		mRawX = ev.getRawX();
		mRawY = ev.getRawY();	
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			rawX = mRawX;
			rawY = mRawY;	
			viewRect.set(0, 0, 0, 0);
			mDragPosition = pointToPosition(moveX, moveY);
			if (mDragPosition == AdapterView.INVALID_POSITION) {				
				return super.dispatchTouchEvent(ev);
			}
			mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
			dragPage = null;
			mStartDragItemView = null;			
			final PhotoBooksProductPagesAdapter adapter = (PhotoBooksProductPagesAdapter) this.getAdapter();
			pageItems = adapter.itemPages;			
			PhotobookPage photobookPageItem = pageItems.get(mDragPosition);
			PhotoBookPagesItemView pageItemView = (PhotoBookPagesItemView) getChildAt(mDragPosition- getFirstVisiblePosition());
			LinearLayout LView = (LinearLayout) pageItemView.getChildAt(0);	
			LinearLayout LPhotoView = (LinearLayout) LView.getChildAt(1);//PhotoBookPageBackView  TextView		
			PhotoBookPageView photobookPageView = (PhotoBookPageView) LPhotoView.getChildAt(0);
			if (photobookPageView != null && photobookPageItem!= null && PhotobookPage.TYPE_STANDARD.equals(photobookPageItem.pageType)) {
				int[] location = new int[2];
				LView.getLocationOnScreen(location);						
				if (location[0] < 0 || location[1] < 0) return super.dispatchTouchEvent(ev);;
														
				dragHelper.setWH(LPhotoView.getWidth(),LPhotoView.getHeight());
						
				if ((location[0] <= mRawX && (location[0]+dragHelper.width >= mRawX))) {
					photobookPageView.setHideHight(true,true);
					mStartDragItemView = photobookPageView;
					dragCurrentPositionView = LPhotoView;
					viewRect.set(location[0], location[1], location[0]+dragHelper.width, location[1]+dragHelper.height);		
					dragHelper.setXY(mRawX, mRawY);
					
					dragPage = photobookPageItem;					
					posStr =  PhotoBookProductUtil.getPageIndexText(context, PhotoBookProductUtil.getCurrentPhotoBook(), dragPage);										
				}
			}				

			if (mStartDragItemView == null) {				
				return super.dispatchTouchEvent(ev);
			}	
			mDownScrollBorder = getHeight()/5;
			mUpScrollBorder =  getHeight()*4/5;	
			
			mDragBitmap = null;
			mStartDragItemView.setDrawingCacheEnabled(true);
			mDragBitmap = Bitmap.createBitmap(mStartDragItemView.getDrawingCache());
			mStartDragItemView.destroyDrawingCache();
			break;
		case MotionEvent.ACTION_MOVE:
			dragHelper.setXY(mRawX, mRawY);
			float space = dragHelper.spacing(mRawX,rawX,mRawY,rawY);
			rawX = mRawX;
			rawY = mRawY;						
			if (space > 5 || !dragHelper.isTouchInItem(moveX, moveY)) {
				mHandler.removeCallbacks(mLongClickRunnable);
			}
			break;
		case MotionEvent.ACTION_UP:
			mHandler.removeCallbacks(mLongClickRunnable);
			mHandler.removeCallbacks(mScrollRunnable);
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private Runnable mScrollRunnable = new Runnable() {
		
		@Override
		public void run() {
			if (mHandler == null) return;
			int scrollY;
			if(moveY > mUpScrollBorder){
				 scrollY = -speed;
				 mHandler.postDelayed(mScrollRunnable, 25);
			}else if(moveY < mDownScrollBorder){
				scrollY = speed;
				 mHandler.postDelayed(mScrollRunnable, 25);
			}else{
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
			}			
			onSwapItem(moveX, moveY,mRawX,mRawY);	
			int mPos = pointToPosition(moveX, moveY);
			if (mPos == AdapterView.INVALID_POSITION) return;
			View view = getChildAt(mPos - getFirstVisiblePosition());
			if (view == null) return;
			smoothScrollToPositionFromTop(mPos, view.getTop() + scrollY);
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent ev) {				
		if (dragHelper.mDragImageView != null) {			
			switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				moveX = (int) ev.getX();
				moveY = (int) ev.getY();
				mRawX = ev.getRawX();
				mRawY = ev.getRawY();
				dragHelper.onDrag(moveX, moveY,mRawX,mRawY);
				break;
			case MotionEvent.ACTION_UP:
				onStopDrag();				
				break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}

	
	private void hideDropItem(){
		final PhotoBooksProductPagesAdapter adapter = (PhotoBooksProductPagesAdapter)this.getAdapter();
		adapter.hideDropItem(true, dragPage);		
	}

	private void onSwapItem(int moveX, int moveY,float rawX,float rawY) {
		boolean isChange = false;		
		int tempPosition = pointToPosition(moveX, moveY);
		if (tempPosition != AdapterView.INVALID_POSITION) {  			
			final PhotoBooksProductPagesAdapter adapter = (PhotoBooksProductPagesAdapter) this.getAdapter();
			pageItems = adapter.itemPages;
			PhotobookPage photobookPageItem = pageItems.get(tempPosition);			
			PhotoBookPagesItemView pageItemView = (PhotoBookPagesItemView) getChildAt(tempPosition - getFirstVisiblePosition());	
			LinearLayout LView = (LinearLayout) pageItemView.getChildAt(0);	
			LinearLayout LPhotoView = (LinearLayout) LView.getChildAt(1);//PhotoBookPageBackView  TextView										
			if (photobookPageItem!= null && PhotobookPage.TYPE_STANDARD.equals(photobookPageItem.pageType)) {
				if (dragCurrentPositionView != LPhotoView){
					int[] location = new int[2];
					LPhotoView.getLocationOnScreen(location);						
					if (location[0] < 0 || location[1] < 0) return;						
									
					if ((location[0] <= rawX && (location[0]+dragHelper.width >= rawX))) {							
						isChange = true;						
						dragCurrentPositionView = LPhotoView;
						
						toPage = photobookPageItem;						
						posStr =  PhotoBookProductUtil.getPageIndexText(context, PhotoBookProductUtil.getCurrentPhotoBook(), toPage);	
						((TextView)dragHelper.mDragImageView.findViewById(R.id.dispalyName)).setText(posStr);													
					}
				}
			}
			if (isChange) {
				onMove(mDragPosition, tempPosition);							
			}			
		}
	}
	
	private void onStopDrag() {			
		dragHelper.removeDragImage();		
		final PhotoBooksProductPagesAdapter adapter = (PhotoBooksProductPagesAdapter) this.getAdapter();
		adapter.hideDropItem(false,null);
		
		if (dragPage != null) {
			PhotobookPage gotoPage = null;			
			Photobook mPhotobook = PhotoBookProductUtil.getCurrentPhotoBook();
			int size = adapter.pages.size();
			int i = 0;
			for (; i < size; i++) {
				PhotobookPage newPage = adapter.pages.get(i);
				if (dragPage.id.equals(newPage.id) ) {					
					gotoPage = mPhotobook.pages.get(i);;				
					break;	
				}				
			}

			if ( gotoPage != null && !gotoPage.id.equals(dragPage.id)) {
				String[] strArray = new String[2];
				strArray[0] = dragPage.id;
				strArray[1] = gotoPage.id;	
				mHandler.obtainMessage(AppConstants.ActionMovePageFlag,strArray).sendToTarget();				
			}
		}
		dragPage = null;		
		mStartDragItemView = null;	
		dragCurrentPositionView = null;
	}
			
	private String LastAnimationID;
	private int tempTo ;	
	private void onMove(int from, int to) {		
		if (toPage == null || dragPage == null || toPage.id.equals(dragPage.id)) return;		
		if (from == to ) return;	
		tempTo = to;	
		final PhotoBooksProductPagesAdapter adapter = (PhotoBooksProductPagesAdapter) this.getAdapter();			
		LastAnimationID = "";
		int fromPos = -1; int toPos = -1; 
		for (int i = 0; i < adapter.pages.size(); i++) {
			PhotobookPage page = adapter.pages.get(i);				
			if (dragPage.id.equals(page.id)) {
				fromPos = i;
				break;
			}			
		}							
		for (int i = 0; i < adapter.pages.size(); i++) {
			PhotobookPage page = adapter.pages.get(i);							
			if (toPage.id.equals(page.id)) {
				toPos = i;	
				break;
			}			
		}
		
		Photobook mPhotobook = PhotoBookProductUtil.getCurrentPhotoBook();
		boolean isDuplex = mPhotobook.isDuplex;		
		if (fromPos < toPos) {			
			for (int pos = from; pos <= to; pos++) {				
				PhotoBookPagesItemView pageItemView = (PhotoBookPagesItemView) getChildAt(pos - getFirstVisiblePosition());					
				if (isDuplex) {					
					if (pos == from ) {
						continue;
					}	
					final View moveView = pageItemView;
					if (moveView != null) {	
						float fromX = -1;						
						float toX = 1-(dragHelper.width+dm.density*5);		
						float fromY = 0;						
						float toY = 0 ;											
						reSetAnimationListener(fromX, toX, fromY, toY, pos, to, adapter, moveView);	
					}																				
				}else {					
					if (pos == from ) {
						continue;
					}
					pageItems = adapter.itemPages;
					PhotobookPage photobookPageItem = pageItems.get(pos);	
					if (photobookPageItem != null) {
						final View moveView = pageItemView;
						if (moveView != null) {						
							float fromX = -1;						
							float toX = 1-(2*dragHelper.width+dm.density*10);
							float fromY = 0;						
							float toY = 0 ;																															
							reSetAnimationListener(fromX, toX, fromY, toY, pos, to, adapter, moveView);	
						}							
					}
				}																
			}					
		}else {		
			for (int pos = from; pos >= to; pos--) {
				PhotoBookPagesItemView pageItemView = (PhotoBookPagesItemView) getChildAt(pos - getFirstVisiblePosition());	
				if (isDuplex) {					
					if (pos == from ) {
						continue;
					}	
					final View moveView = pageItemView;
					if (moveView != null) {						
						float fromX = 1;						
						float toX = (dragHelper.width+dm.density*5)-1;	
						float fromY = 0;						
						float toY = 0 ;
						reSetAnimationListener(fromX, toX, fromY, toY, pos, to, adapter, moveView);
					}								
				}else{										
					if (pos == from ) {
						continue;
					}
					pageItems = adapter.itemPages;
					PhotobookPage photobookPageItem = pageItems.get(pos);	
					if (photobookPageItem != null) {
						final View moveView = pageItemView;
						if (moveView != null) {						
							float fromX = 1;						
							float toX = (2*dragHelper.width+dm.density*10)-1;	
							float fromY = 0;						
							float toY = 0 ;	
							reSetAnimationListener(fromX, toX, fromY, toY, pos, to, adapter, moveView);
						}															
					}													
				}
			}									
		}						
	}	
	
	private void reSetAnimationListener( float fromX,float toX ,float fromY ,float toY, int position,int end,final PhotoBooksProductPagesAdapter adapter,final View moveView){	
		Animation animation = getMoveAnimation(fromX, toX, fromY, toY);
		moveView.startAnimation(animation);						
		if (position == end )LastAnimationID = animation.toString();
		
		animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {							
				String animaionID = animation.toString();
				if (animaionID.equalsIgnoreCase(LastAnimationID)) {
					adapter.onChange(toPage, dragPage);
					mDragPosition = tempTo;													
				}
				moveView.clearAnimation();
			}
		});			
	}

	private Animation getMoveAnimation(float fromX, float toX, float fromY,float toY){		
		TranslateAnimation translateAnimation = new TranslateAnimation(fromX, toX, fromY, toY);		
		translateAnimation.setFillAfter(true);		
		translateAnimation.setDuration(250);	//300
		return translateAnimation;
	}	
	
	private class AnimationDragHelper extends DragHelper{

		public AnimationDragHelper(Context context, View popView) {
			super(context, popView);			
		}

		@Override
		public void setCreateDragView(Bitmap bitmap, String... promptStr) {				
			((ImageView)mDragImageView.findViewById(R.id.dispalyImage)).setImageBitmap(bitmap);		
			((TextView)mDragImageView.findViewById(R.id.dispalyName)).setText(promptStr[0]);			
			mHandler.removeCallbacks(mLongClickRunnable);
		}

		@Override
		public void setOnDragView(int moveX, int moveY,float rawX,float rawY) {
			onSwapItem(moveX, moveY,rawX,rawY);				
			if (mHandler != null) {
				mHandler.post(mScrollRunnable);
			}		
		}
		
	}
	

}
