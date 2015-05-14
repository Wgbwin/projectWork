package com.kodak.rss.tablet.view.collage;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.CollageEditActivity;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.DragTarget;
import com.kodak.rss.tablet.view.MainPageView.OnLayerClickListener;
import com.kodak.rss.tablet.view.MainPageView.OnLayerDragListener;

public class CollageMainView extends RelativeLayout implements DragTarget{
	
	private CollagePageView mPageView;
	private Collage mCollage;
	
	private CollageEditActivity mActivity;
			
	private int mMaxHeight;
	private int mMinHeight = 50;
	private DisplayMetrics dm;
		
	public LruCache<String, Bitmap> mMemoryCache;
	private Bitmap mWaitBitmap;	
	public ImageUseURIDownloader imageDownloader;
	private final Map<String, Request> pendingRequests = new HashMap<String, Request>();
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
					CollageUtil.refreshSucPageInCollage(profileId, refreshCount);	
				}
				Bitmap mContentBitmap = bitmap == null ? mWaitBitmap : bitmap;
				if (mActivity != null && !mActivity.isFinishing()) {
					if (view != null && view instanceof CollagePageView && view.getTag().toString().equals(profileId)) {												
						((CollagePageView)view).setImageBitmap(mContentBitmap);						
					}				
				}				
			}							
		}
	};	
	
	public CollageMainView(Context context) {
		super(context);
		init(context);
	}
	
	public CollageMainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public CollageMainView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public void init(Context context) {
		mActivity = (CollageEditActivity) context;	
		inflate(context, R.layout.collage_main_view, this);
		mPageView = (CollagePageView) findViewById(R.id.page_view);
		dm = context.getResources().getDisplayMetrics();
		mWaitBitmap = BitmapFactory.decodeResource(mActivity.getResources(),R.drawable.imagewait60x60);
	}

	public void setCollage(Collage collage, int downW,int downH,LruCache<String, Bitmap> mMemoryCache) {
		mCollage = collage;
				
		this.mMemoryCache = mMemoryCache;	
		imageDownloader = new ImageUseURIDownloader(mActivity,pendingRequests);	
		imageDownloader.setSaveType(FilePathConstant.collageType);	
		imageDownloader.setIsThumbnail(false);
		imageDownloader.setOnProcessImageResponseListener(onResponseListener);
			
		mPageView.setPage(collage.page);
		mPageView.setDownParameter(downW, downH, mMemoryCache, imageDownloader);
		mPageView.setWaitBitmap(mWaitBitmap);
				
		mMaxHeight = downH;		

		requestLayout();
		invalidate();
	}
	
	public void refresh(Collage collage) {
		mCollage = collage;
		mPageView.setPage(collage.page);
		postInvalidate();
	}
	
	public void refresh(Collage collage,float hWRation) {
		mCollage = collage;		
		mPageView.setHWRation(hWRation);
		mPageView.setPage(collage.page);
		requestLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int w = MeasureSpec.getSize(widthMeasureSpec);
		int h = MeasureSpec.getSize(heightMeasureSpec);

		if (mCollage != null) {
			float pw = mCollage.page.width;
			float ph = mCollage.page.height;
			if (pw == 0 || ph == 0) return;
			
			if (mMaxHeight != 0){				
				if (h > mMaxHeight) {
					h = mMaxHeight;
				}else if (h < mMinHeight){
					h = mMinHeight;
				}					
				w = (int) (h * pw / ph);									
			}
		}		
		super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
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

	@Override
	public void hideAllFrames() {
		if (mPageView != null) {
			mPageView.hideAllFrame();
		}		
	}

	@Override
	public Object[] pointToPosition(float xOnScreen, float yOnScreen) {		
		if (mCollage == null || mPageView == null) return null;							
		if (mPageView != null && mPageView.getVisibility() ==View.VISIBLE) {
			int[] location = new int[2];
			mPageView.getLocationOnScreen(location);
			float relativeX = xOnScreen - location[0];
			float relativeY = yOnScreen - location[1];

			if (relativeX >= 0 && relativeY >= 0 && relativeX <= mPageView.getWidth() && relativeY <= mPageView.getHeight()) {
				Layer layer = mPageView.pointTo(relativeX, relativeY);				
				return new Object[]{mPageView, mPageView.getPage(), layer}; 				
			}				
		}
		return null;
	}	
		
	public void moveToTopLeft() {
		Rect currentRect = new Rect(getLeft(), getTop(), getRight(), getBottom());	
		Rect dstRect = getRectWhenMoveToTopLeft();		
		bringToTop(dstRect);
		
		animateTo(currentRect,dstRect);
	}	
	
	private Rect getRectWhenMoveToTopLeft() {	
		Rect maxRect = new Rect();
		maxRect.left = 0;
		maxRect.top = 0;
		maxRect.right = dm.widthPixels* 11 / 24;
		maxRect.bottom = dm.heightPixels / 2  - DimensionUtil.dip2px(mActivity, 20);

		Rect newPageRect = new Rect();
		
		if (mActivity.wHRatio > 0) {
			int newH = maxRect.height();
			int newW = (int) (newH / mActivity.wHRatio);
			newPageRect.left =  DimensionUtil.dip2px(mActivity, 5);
			newPageRect.right = newPageRect.left + newW;
			newPageRect.top = DimensionUtil.dip2px(mActivity, 47);
			newPageRect.bottom = newPageRect.top + newH;
		}else {
			int newW = maxRect.width();
			int newH = (int) (newW *  mActivity.wHRatio);
			newPageRect.left = DimensionUtil.dip2px(mActivity, 5);
			newPageRect.right = newPageRect.left + newW;
			newPageRect.top = DimensionUtil.dip2px(mActivity, 47);
			newPageRect.bottom = newPageRect.top + newH;						
		}	
		return newPageRect;
	}

	private void bringToTop(Rect dstRect) {
		RelativeLayout parent = (RelativeLayout) getParent();
		RelativeLayout parentView = (RelativeLayout) mActivity.findViewById(R.id.parentView);
		
		int index = 0;		
		View v = mActivity.findViewById(R.id.collage_top_layer);
		for (int i = 0; i < parentView.getChildCount(); i++) {
			if (parentView.getChildAt(i) == v) {
				index = i;
				break;
			}
		}			
		parent.removeView(this);		
		parentView.addView(this, index);
		parentView.requestLayout();
		parentView.invalidate();
	}	
	
	private void animateTo(Rect currentRect, Rect dstRect) {			
		setParams(dstRect,true);

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
		startAnimation(animSet);
	}
	
	private void setParams(Rect dstRect,boolean isSetPanelHeight) {			
		LayoutParams params = (LayoutParams) getLayoutParams();	
		params.topMargin = dstRect.top;		
		params.bottomMargin = dm.heightPixels - dstRect.bottom;
		params.leftMargin = dstRect.left;
		params.height = dstRect.height();
		params.width = dstRect.width();	
		params.addRule(CENTER_HORIZONTAL, 0);		
		requestLayout();
		
		if (isSetPanelHeight) {
			int panelOpenHeight = (int) (dm.heightPixels - dstRect.bottom - dm.density*60);
			mActivity.panel.setOpenContentHeight(panelOpenHeight);
		}		
	}
	
	private LayoutParams getParams() {			
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);		
		params.addRule(CENTER_HORIZONTAL);		
		return params;
	}

	public void moveToCenter(){
		RelativeLayout parent = (RelativeLayout) getParent();
		parent.removeView(CollageMainView.this);
		
		RelativeLayout parentView = (RelativeLayout) mActivity.findViewById(R.id.collage_content);

		parentView.addView(CollageMainView.this, 1, getParams());
		parent.requestLayout();
		parent.invalidate();
		
	}
	
}
