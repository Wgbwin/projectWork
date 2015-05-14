package com.kodak.rss.tablet.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.FBKImageAdapter;
import com.kodak.rss.tablet.adapter.ImageAdapter;
import com.kodak.rss.tablet.facebook.FbkObject;
import com.kodak.rss.tablet.facebook.FbkPhoto;

public class GCDragPopGridView extends GridView {

	private long dragResponseMS = 500;

	private int mDragPosition;
	private SelectImageView mStartDragItemView = null;
	
	private Bitmap mDragBitmap;	

	private float mRawX,mRawY;	

	private Handler mHandler;

	private ImageInfo dragImageInfo;
	private DisplayMetrics dm;
	private Rect viewRect;
	
	private AniamtionDragHelper dragHelper;
		
	public GCDragPopGridView(Context context) {
		this(context, null);
	}

	public GCDragPopGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GCDragPopGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		
		viewRect = new Rect(); 
		setFocusable(false);
		setFocusableInTouchMode(false);	
		dm = getResources().getDisplayMetrics();
		this.dragHelper = new AniamtionDragHelper(context);	
	}

	public void setAnimLayout(RelativeLayout animLayer,DragTarget dragTarget){						
		dragHelper.setAnimParentView(animLayer, dragTarget);				
	}
	
	public void setAnimationScaleHalf(boolean isScaleHalf){		
		dragHelper.setAnimationScaleHalf(isScaleHalf);				
	}
	
	private synchronized Handler getMHandler() {
		if (mHandler == null) {
			mHandler = new Handler(Looper.getMainLooper());
		}
		return mHandler;
	}
	
	public void setOnDragListener(AniamtionDragHelper.OnGridDragListener onDragListener){		
		dragHelper.setOnDragListener(onDragListener);
	}	
		
	private Runnable mLongClickRunnable = new Runnable() {
		@Override
		public void run() {
			if (mStartDragItemView != null) {											
				createDragImage(mDragBitmap, (int)mRawX, (int)mRawY);
				getMHandler().removeCallbacks(mLongClickRunnable);
			}
		}
	};

	private float rawX, rawY;	
	private SortableHashMap<Integer, String[]> imageBuckets;
	private ArrayList<FbkObject> fbkImageList;
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {		
		int mDownX = (int) ev.getX();
		int mDownY = (int) ev.getY();
		mRawX = ev.getRawX();
		mRawY = ev.getRawY();	
		int pointCnt = ev.getPointerCount();
		if (pointCnt > 1) {			
			if (mStartDragItemView != null && dragImageInfo != null) {
				return true;
			}else {
				dragHelper.removeDragImage();
				viewRect.set(0, 0, 0, 0);
				mStartDragItemView = null;				
				dragImageInfo = null;	
				getMHandler().removeCallbacks(mLongClickRunnable);
				return super.dispatchTouchEvent(ev);
			}	
		}
		
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:	
			rawX = mRawX;
			rawY = mRawY;	
			mDragPosition = pointToPosition(mDownX, mDownY);
			if (mDragPosition == AdapterView.INVALID_POSITION ) {				
				return super.dispatchTouchEvent(ev);
			}
			getMHandler().postDelayed(mLongClickRunnable, dragResponseMS);
			mStartDragItemView = null;
			viewRect.set(0, 0, 0, 0);
			if (!(this.getAdapter() instanceof ImageAdapter || this.getAdapter() instanceof FBKImageAdapter)) {
				return super.dispatchTouchEvent(ev);				
			}					
			dragImageInfo = null;
			
			ImageInfo initImageInfo = null;		
			if (this.getAdapter() instanceof ImageAdapter ) {
				final ImageAdapter adapter = (ImageAdapter) this.getAdapter();					
				imageBuckets = adapter.imageBuckets;
				if (imageBuckets == null) return super.dispatchTouchEvent(ev);	
				int keyId  = imageBuckets.keyAt(mDragPosition);
				String key = String.valueOf(keyId);		
				if (!(adapter.dirtyList != null && adapter.dirtyList.contains(keyId))) {
					String value = imageBuckets.valueAt(mDragPosition)[0];
					String bucketDisplayName = imageBuckets.valueAt(mDragPosition)[1];
					Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, key);
					initImageInfo = new ImageInfo(key, value, uri.toString(),uri.toString());
					initImageInfo.editUrl = value;
					initImageInfo.bucketDisplayName = bucketDisplayName;
					initImageInfo.fromSource = "Photos";
				}								
			}
			
			if (this.getAdapter() instanceof FBKImageAdapter ) {
				final FBKImageAdapter adapter = (FBKImageAdapter) this.getAdapter();				
				fbkImageList = adapter.fbkImageList;
				if (fbkImageList == null) return super.dispatchTouchEvent(ev);	
				FbkPhoto fbkPhoto = (FbkPhoto) fbkImageList.get(mDragPosition);
				if (fbkPhoto == null) return super.dispatchTouchEvent(ev);
				String key = fbkPhoto.ID;
				initImageInfo = new ImageInfo();
				initImageInfo.isfromNative = false;		
				initImageInfo.id = key;	
				initImageInfo.fromSource = "Facebook";
				initImageInfo.bucketDisplayName = fbkPhoto.bucketName;
				initImageInfo.downloadOriginalUrl = fbkPhoto.getOriginalLink();
				initImageInfo.origHeight = fbkPhoto.origHeight;
				initImageInfo.origWidth = fbkPhoto.origWidth;
				if (initImageInfo.origHeight > 0 && initImageInfo.origWidth > 0) {
					initImageInfo.uploadOriginalUrl = initImageInfo.downloadOriginalUrl;	
				}
				initImageInfo.downloadThumbnailUrl = fbkPhoto.getThumbnailLink();
			}
						
			RelativeLayout itemView =  (RelativeLayout) getChildAt(mDragPosition- getFirstVisiblePosition());
			SelectImageView imageView =  (SelectImageView) itemView.getChildAt(0);	
			if (imageView != null &&  initImageInfo != null ) {
				int[] location = new int[2];
				imageView.getLocationOnScreen(location);						
				if (location[0] < 0 || location[1] < 0) return super.dispatchTouchEvent(ev);
				int width  = imageView.getWidth();
				int height = imageView.getHeight();					
				dragHelper.setWH(width, height);								
			    int left = location[0];
			    int right = left + width;
			    int top = location[1];
			    int bottom = top + height;
			    viewRect.set(left-5, top-5, right+5, bottom+5);				
				mStartDragItemView = imageView;													
				dragImageInfo = initImageInfo;																							
			}				

			if (mStartDragItemView == null) {				
				return super.dispatchTouchEvent(ev);
			}	
			mDragBitmap = imageView.getImageBitmap();
			if (mDragBitmap == null) {				
				mDragBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.imagewait60x60);									
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float space = dragHelper.spacing(mRawX,rawX,mRawY,rawY);
			rawX = mRawX;
			rawY = mRawY;						
			if (space > dm.density*5 ||!dragHelper.isTouchInItem(mDownX, mDownY)) {
				getMHandler().removeCallbacks(mLongClickRunnable);
			}
			break;
		case MotionEvent.ACTION_UP:
			getMHandler().removeCallbacks(mLongClickRunnable);			
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {				
		if (dragHelper.mDragImageView != null) {		
			mRawX = ev.getRawX();
			mRawY = ev.getRawY();
			switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:			
				onDrag(mRawX,mRawY);					
				break;
			case MotionEvent.ACTION_UP:				
				onStopDrag(mRawX,mRawY);				
				break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}

	private void createDragImage(final Bitmap bitmap, final int downX, final int downY) {		
		if (!viewRect.contains(downX, downY)) return;
		dragHelper.createDragImage(bitmap, downX, downY);
	}
		
	private void onDrag(final float rawX,final float rawY) {				
		dragHelper.onDrag(rawX, rawY);	
	}

	private void onStopDrag(float rawX,float rawY) {
		viewRect.set(0, 0, 0, 0);
		mStartDragItemView = null;		
		dragHelper.onStopDrag(rawX, rawY, dragImageInfo, mDragBitmap);
	}

}
