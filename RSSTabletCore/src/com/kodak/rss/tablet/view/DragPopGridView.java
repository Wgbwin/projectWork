package com.kodak.rss.tablet.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.aphidmobile.flip.FlipViewController;
import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.content.Theme.BackGround;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.PhotoBooksProductBackgroundsAdapter;
import com.kodak.rss.tablet.adapter.PhotoBooksProductPicturesAdapter;

public class DragPopGridView extends GridView {

	private long dragResponseMS = 500;

	private int mDragPosition;
	private SelectImageView mStartDragItemView = null;		
	private Bitmap mDragBitmap;	
	
	private BackGround dragGroud;
	private List<BackGround> backGrounds;	
	private Handler mHandler;
	private PhotoBookLayout mPhotoBookLayout; 	
		
	private boolean isBackgroundsAdapter;
	private ImageInfo dragImageInfo;
	private ArrayList<ImageInfo> mPagesPics;
	private Layer dragLayer;
	private List<Layer> layerList;
	
	private int moveX;
	private int moveY;
	private float mRawX,mRawY;	
	private Rect viewRect;	
	private DragHelper dragHelper;
		
	public DragPopGridView(Context context) {
		this(context, null);
	}

	public DragPopGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragPopGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);	
		viewRect = new Rect();
		this.dragHelper = new PopDragHelper(context,new SelectImageView(context));
		setFocusable(false);
		setFocusableInTouchMode(false);
	}
	
	public void setHandler(Handler handler){
		isBackgroundsAdapter = false;
		this.mHandler = handler;		
	}

	public void setPhotoBookLayout(PhotoBookLayout layout){
		this.mPhotoBookLayout = layout;		
	}	

	private Runnable mLongClickRunnable = new Runnable() {

		@Override
		public void run() {
			if (mStartDragItemView != null) {
				if (!viewRect.contains((int)mRawX, (int)mRawY))return;
				dragHelper.createDragImage(mDragBitmap);	
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
			if (mDragPosition == AdapterView.INVALID_POSITION || mDragPosition == 0) {				
				return super.dispatchTouchEvent(ev);
			}
			mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
			mStartDragItemView = null;
			if (this.getAdapter() instanceof PhotoBooksProductBackgroundsAdapter) {
				isBackgroundsAdapter = true;	
			}else {
				isBackgroundsAdapter = false;							
			}
			
			BackGround gackGround = null;
			Layer layer = null;
			ImageInfo info = null;
			
			dragGroud = null;
			dragImageInfo = null;
			dragLayer = null;
			if (isBackgroundsAdapter) {				
				final PhotoBooksProductBackgroundsAdapter adapter = (PhotoBooksProductBackgroundsAdapter) this.getAdapter();
				backGrounds = adapter.mBackGrounds;			
				gackGround = backGrounds.get(mDragPosition-1);
			}else {
				final PhotoBooksProductPicturesAdapter adapter = (PhotoBooksProductPicturesAdapter) this.getAdapter();				
				mPagesPics = adapter.mPagesPics;				
				layerList = adapter.layerList;
				if (layerList != null) {
					if (mDragPosition<= layerList.size()) {
						info = null;
						layer = layerList.get(mDragPosition-1);
					}else {
						layer = null;
						int pos = mDragPosition - layerList.size() - 1;
						info = mPagesPics.get(pos);
						if (info != null && info.imageThumbnailResource == null) {
							info = null;
						}
					}					
				}else {
					layer = null;
					info = mPagesPics.get(mDragPosition-1);
					if (info != null && info.imageThumbnailResource == null) {
						info = null;
					}
				}				
			}

			RelativeLayout itemView =  (RelativeLayout) getChildAt(mDragPosition- getFirstVisiblePosition());
			SelectImageView imageView =  (SelectImageView) itemView.getChildAt(0);	
			if (imageView != null && ((isBackgroundsAdapter && gackGround!= null)||
					(!isBackgroundsAdapter && (layer != null || info != null)) )) {
				int[] location = new int[2];
				imageView.getLocationOnScreen(location);						
				if (location[0] < 0 || location[1] < 0) return super.dispatchTouchEvent(ev);;										
				
				dragHelper.setWH(imageView.getWidth(),imageView.getHeight());
				dragHelper.setXY(mRawX, mRawY);
				
				viewRect.set(location[0], location[1], location[0]+dragHelper.width, location[1]+dragHelper.height);	
				mStartDragItemView = imageView;					

				if (isBackgroundsAdapter) {
					dragGroud = gackGround;		
				}else {
					dragImageInfo = info;
					dragLayer = layer;
				}																						
			}				

			if (mStartDragItemView == null) {				
				return super.dispatchTouchEvent(ev);
			}	
			mDragBitmap = imageView.getImageBitmap();
			if (mDragBitmap == null) {
				if (isBackgroundsAdapter) {
					mStartDragItemView.setDrawingCacheEnabled(true);
					mDragBitmap = Bitmap.createBitmap(mStartDragItemView.getDrawingCache());
					mStartDragItemView.destroyDrawingCache();
				}else {
					mDragBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.imagewait60x60);					
				}
			}
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
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {				
		if (dragHelper.mDragImageView != null) {
			moveX = (int) ev.getX();
			moveY = (int) ev.getY();
			mRawX = ev.getRawX();
			mRawY = ev.getRawY();
			switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:								
				dragHelper.onDrag(moveX,moveY,mRawX,mRawY);
				break;
			case MotionEvent.ACTION_UP:
				onStopDrag(mRawX,mRawY);			
				break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}


	private void onStopDrag(float rawX,float rawY) {			
		dragHelper.removeDragImage();
		
		boolean isWant = false;
		boolean isLeft = false;
		if (mPhotoBookLayout != null) {
			FlipViewController controller = (FlipViewController) mPhotoBookLayout.getChildAt(0);
			for (int i = 0; i < controller.getChildCount(); i++) {
				View view =  controller.getChildAt(i);
				if (view != null && view instanceof PhotoBookMainItemView) {
					PhotoBookMainItemView mainItemView = (PhotoBookMainItemView) view;
					
					PhotoBookMainPageView leftPageView = mainItemView.ivLeft;
					if (leftPageView != null) {
						int[] location = new int[2];
						leftPageView.getLocationOnScreen(location);						
						if (location[0] < 0 || location[1] < 0) return ;										
						int width  = leftPageView.getWidth();
						int height = leftPageView.getHeight();			
						if ((location[0] <= rawX && (location[0]+width) >= rawX)&&(location[1] <= rawY && (location[1]+height) >= rawY)) {							
							isWant = true;
							isLeft = true;
						}						
					}
					
					PhotoBookMainPageView rightPageView = mainItemView.ivRight;
					if (rightPageView != null) {
						int[] location = new int[2];
						rightPageView.getLocationOnScreen(location);						
						if (location[0] < 0 || location[1] < 0) return ;										
						int width  = rightPageView.getWidth();
						int height = rightPageView.getHeight();			
						if ((location[0] <= rawX && (location[0]+width) >= rawX)&&(location[1] <= rawY && (location[1]+height) >= rawY)) {							
							isWant = true;
							isLeft = false;
						}						
					}
					break;
				}				
			}
		}
		
		if (isWant) {
			if (isBackgroundsAdapter && dragGroud != null) {
				String mBackgroudId = dragGroud.id;
				int lastIndex = dragGroud.name.lastIndexOf(".");
				String mThemeId = dragGroud.name.substring(0, lastIndex);
							
				Message msg = new Message();			 
		        msg.what = AppConstants.ActionSetBackgroudFlag;  
		        Bundle bundle = new Bundle();    
		        bundle.putBoolean("isLeft", isLeft);
		        bundle.putString("mBackgroudId", mBackgroudId);
		        bundle.putString("mThemeId", mThemeId);
		        msg.setData(bundle);
				mHandler.sendMessage(msg);				
			}else if ((dragImageInfo != null && dragImageInfo.imageThumbnailResource != null) || dragLayer != null) {
				String mImageInfoId = "";
				String mLayerId = "";
				if (dragLayer != null ) {
					mLayerId = dragLayer.contentId;
				}else {
					mImageInfoId = dragImageInfo.id;
				}	
				Message msg = new Message();			 
		        msg.what = AppConstants.ActionAddCopyFlag;  
		        Bundle bundle = new Bundle();    
		        bundle.putBoolean("isLeft", isLeft);		        
		        bundle.putString("mImageInfoId", mImageInfoId);
		        bundle.putString("mLayerId", mLayerId);
		        msg.setData(bundle);		        
		        mHandler.sendMessage(msg);					
			} 		
		}
		dragGroud = null;
		dragImageInfo = null;
		dragLayer = null;
		mStartDragItemView = null;			
	}

	private class PopDragHelper extends DragHelper{

		public PopDragHelper(Context context, View popView) {
			super(context, popView);			
		}

		@Override
		public void setCreateDragView(Bitmap bitmap, String... promptStr) {			
			((SelectImageView)mDragImageView).setImageBitmap(bitmap, false);
			mHandler.removeCallbacks(mLongClickRunnable);
		}

		@Override
		public void setOnDragView(int moveX, int moveY,float rawX,float rawY) {					
		}
		
	}
	
	
	
}
