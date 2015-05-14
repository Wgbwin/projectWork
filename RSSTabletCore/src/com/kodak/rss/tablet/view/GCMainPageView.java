package com.kodak.rss.tablet.view;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.kodak.rss.core.n2r.bean.greetingcard.GCLayer;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.GreetingCardUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;

public class GCMainPageView extends MainPageView<GCPage, GCLayer> {
	public static final int INSIDE_SHADOW_LEFT = 1;
	public static final int INSIDE_SHADOW_TOP = 2;
	public static final int INSIDE_SHADOW_RIGHT = 3;
	public static final int INSIDE_SHADOW_BOTTOM = 4;
	
	private static final String TAG = "GCMainPageView";
	private Bitmap mContentBitmap, mWaitBitmap;
	private Bitmap mInsideShadowLeft, mInsideShadowTop, mInsideShadowRight, mInsideShadowBottom;
	private RectF mRect4InsideShadowLeft, mRect4InsideShadowTop, mRect4InsideShadowRight, mRect4InsideShadowBottom;
	private RectF mContentRect;
	private int mWidth, mHeight;
	private int mDownloadWidth, mDownloadHeight;
	
	private boolean mIsShowEdit = true;
	private boolean mIsActive = false;
	private List<GCLayer> mTextBlockLayers;
	private List<RectF> mRects4Text;
	private Paint mPaint4TextFrame;
	
	//In some case , you need to show frame for image layer(ex: drag image to add it to page)
	//these values are used to show frame
	private RectF mRect4ImageLayer;
	private boolean mShowFrame4ImageLayer;
	private Paint mPaint4ImageLayer;		
	private ImageUseURIDownloader imageDownloader;
	
	public GCMainPageView(Context context, AttributeSet attrs,int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public GCMainPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public GCMainPageView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {		
		setClickable(true);
		
		initPaint4TextFrame();
		initPaint4ImageLayerFrame();
	}
	
	@Override
	public void setPage(GCPage page) {
		super.setPage(page);
		
		List<GCLayer> layers = findTextBlockLayer();
		mTextBlockLayers = layers.size() == 0 ? null : layers;
		
		initRects4Text();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		mContentRect = new RectF(0, 0, mWidth, mHeight);
		
		initRects4Text();
		updateRectsForInsideShadow();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		boolean drawContent = false;
		Bitmap bitmap = getBitmap(mPage);
		
		if (bitmap == null) {
			bitmap = mWaitBitmap;
		} else {
			//TODO If server fix this issue, you can remove the code for rotate
			//Fix for RSSMOBILEPDC-1874
			//This is a server issue, sometimes it will return a image with wrong direction(4x6 to 6x4)
			//I don't know when server will fix this bug, so app try to handle this error
			//When we find the direction is not correct, roate it
			if (mPage != null) {
				int bw = bitmap.getWidth();
				int bh = bitmap.getHeight();
				if ((mPage.width > mPage.height && bw < bh) || (mPage.width < mPage.height && bw > bh) ) {
					Log.e(TAG, "oops, server return a wrong direction image :(");
					bitmap = ImageUtil.rotateBitmap(bitmap, 90);
				}
			}
			
			mContentBitmap = bitmap;
			drawContent = true;
		}
		
		canvas.drawBitmap(bitmap, null, mContentRect, null);
		
		if (drawContent) {
			//draw shadow
			if (mInsideShadowLeft != null) {
				canvas.drawBitmap(mInsideShadowLeft, null, mRect4InsideShadowLeft, null);
			}
			if (mInsideShadowRight != null) {
				canvas.drawBitmap(mInsideShadowRight, null, mRect4InsideShadowRight, null);
			}
			if (mInsideShadowTop != null) {
				canvas.drawBitmap(mInsideShadowTop, null, mRect4InsideShadowTop, null);
			}
			if (mInsideShadowBottom != null) {
				canvas.drawBitmap(mInsideShadowBottom, null, mRect4InsideShadowBottom, null);
			}
			
		}
		
		//draw rect for text
		if (mIsShowEdit && drawContent && mRects4Text != null && mRects4Text.size() > 0) {
			//draw frame for textblock layers
			for (int i = 0, len = mRects4Text.size(); i < len; i++) {
				canvas.drawRect(mRects4Text.get(i), mPaint4TextFrame);
			}
		}
		
		//draw rect for image in some case
		if (mShowFrame4ImageLayer && mRect4ImageLayer != null) {
			canvas.drawRect(mRect4ImageLayer, mPaint4ImageLayer);
		}
		
	}
	
	
	private float mXDown;
	private float mYDown;
	private float mDistanceDown;
	private static final int TOUCH_SLOP = 10;
	private boolean mIsMoved;
	private boolean mIsReleased;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mIsActive) {
			return true;
		}
		
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mXDown = event.getX();
			mYDown = event.getY();
			mIsMoved = false;
			mDistanceDown = 0;
			mIsReleased = false;
			break;
			
		case MotionEvent.ACTION_POINTER_DOWN:
			if (mIsReleased) {
				return true;
			}
			
			mDistanceDown = getPointerDistance(event);
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (mIsReleased) {
				return true;
			}
			
			if (!mIsMoved && (Math.abs(event.getX() - mXDown) > TOUCH_SLOP || Math.abs(event.getY() - mYDown) > TOUCH_SLOP)) {
				mIsMoved = true;
			}
			
			break;
		
		case MotionEvent.ACTION_CANCEL:
			mIsReleased = true;
			break;
		case MotionEvent.ACTION_UP:
			if (mIsReleased) {
				return true;
			}
			
			if(!mIsMoved && mDistanceDown == 0 && mOnLayerClickListener != null){// click on layer
				Layer touchLayer = getTouchOnLayer(event);
				if(touchLayer != null){
					mOnLayerClickListener.onLayerClick(this, mPage, touchLayer,getLayerRect(touchLayer));
					return true;
				}
			}
			
			break;
		}
		
		return true;
	}
	
	private float getPointerDistance(MotionEvent event){
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x*x + y*y);
	}
	
	private synchronized void initRects4Text() {
		if (mTextBlockLayers == null) {
			return;
		}
		
		if (mRects4Text == null) {
			mRects4Text = new ArrayList<RectF>();
		} else {
			mRects4Text.clear();
		}
		
		for (GCLayer layer : mTextBlockLayers) {
			mRects4Text.add(getLayerRect(layer));
		}
	}
	
	private void updateRectsForInsideShadow() {
		float w = getWidth();
		float h = getHeight();
		
		mRect4InsideShadowTop = new RectF(0, 0, w, h / 30);
		mRect4InsideShadowBottom = new RectF(0, h - h / 30, w , h);
		mRect4InsideShadowLeft = new RectF(0, 0, w / 30, h);
		mRect4InsideShadowRight = new RectF(w - w / 30, 0, w, h);
	}
	
	private void initPaint4TextFrame() {
		mPaint4TextFrame = new Paint();
		mPaint4TextFrame.setStyle(Paint.Style.STROKE);
		mPaint4TextFrame.setColor(Color.BLUE);
		mPaint4TextFrame.setStrokeWidth(DimensionUtil.dip2px(getContext(), 2));
		PathEffect pe = new DashPathEffect(new float[]{5,5}, 1);
		mPaint4TextFrame.setPathEffect(pe);
	}
	
	private void initPaint4ImageLayerFrame() {
		mPaint4ImageLayer = new Paint();
		mPaint4ImageLayer.setStyle(Paint.Style.STROKE);
		mPaint4ImageLayer.setColor(0xFFFBBA06);
		mPaint4ImageLayer.setStrokeWidth(DimensionUtil.dip2px(getContext(), 4));
	}
	
	private List<GCLayer> findTextBlockLayer() {
		List<GCLayer> list = new ArrayList<GCLayer>();
		
		if (mPage != null) {
			for (int i = 0; i < mPage.layers.size(); i++) {
				GCLayer layer = mPage.layers.get(i);
				
				if (layer != null && GCLayer.TYPE_TEXT_BLOCK.equals(layer.type)) {
					list.add(layer);
				}
			}
		}
		
		return list;
	}
	
	@Override
	protected GCLayer[] getLayers() {
		GCLayer[] layers = new GCLayer[mPage.layers.size()];
		for (int i = 0; i < layers.length; i++) {
			layers[i] = mPage.layers.get(i);
		}
		return layers;
	}
	
	public void showShadow(int where) {
		switch (where) {
		case INSIDE_SHADOW_LEFT:
			mInsideShadowLeft = BitmapFactory.decodeResource(getResources(), R.drawable.book_insideshadow_right_xxhdpi);
			break;
		case INSIDE_SHADOW_RIGHT:
			mInsideShadowRight = BitmapFactory.decodeResource(getResources(), R.drawable.book_insideshadow_left_xxhdpi);
			break;
		case INSIDE_SHADOW_TOP:
			Bitmap tempTop = BitmapFactory.decodeResource(getResources(), R.drawable.book_insideshadow_left_xxhdpi);
			mInsideShadowTop = ImageUtil.rotateBitmap(tempTop, -90);
			tempTop.recycle();
			break;
		case INSIDE_SHADOW_BOTTOM:
			Bitmap tempBottom = BitmapFactory.decodeResource(getResources(), R.drawable.book_insideshadow_right_xxhdpi);
			mInsideShadowBottom = ImageUtil.rotateBitmap(tempBottom, -90);
			tempBottom.recycle();
			break;
		}
	}
	
	public void hideShadow(int where) {
		switch (where) {
		case INSIDE_SHADOW_LEFT:
			if (mInsideShadowLeft != null && !mInsideShadowLeft.isRecycled()){
				mInsideShadowLeft.recycle();
				mInsideShadowLeft = null;
			}
			break;
		case INSIDE_SHADOW_RIGHT:
			if (mInsideShadowRight != null && !mInsideShadowRight.isRecycled()){
				mInsideShadowRight.recycle();
				mInsideShadowRight = null;
			}
			break;
		case INSIDE_SHADOW_TOP:
			if (mInsideShadowTop != null && !mInsideShadowTop.isRecycled()){
				mInsideShadowTop.recycle();
				mInsideShadowTop = null;
			}
			break;
		case INSIDE_SHADOW_BOTTOM:
			if (mInsideShadowBottom != null && !mInsideShadowBottom.isRecycled()){
				mInsideShadowBottom.recycle();
				mInsideShadowBottom = null;
			}
			break;
		}
	}
	
	public synchronized void showFrameForLayer(Layer layer) {
		if (!mShowFrame4ImageLayer) {
			mShowFrame4ImageLayer = true;
			mRect4ImageLayer = getLayerRect(layer);
			postInvalidate();
		}
	}
	
	public synchronized void hideFrameForLayer() {
		if (mShowFrame4ImageLayer) {
			mShowFrame4ImageLayer = false;
			mRect4ImageLayer = null;
			postInvalidate();
		}
	}
	
	public Bitmap getImageBitmap() {
		return mContentBitmap;
	}
	
	public void setImageBitmap(Bitmap bitmap) {
		mContentBitmap = bitmap;
	}
	
	public void setWaitBitmap(Bitmap bitmap) {
		mWaitBitmap = bitmap;
	}
	
	public void setImageDownloader(ImageUseURIDownloader downloader) {
		imageDownloader = downloader;
	}
	
	public void setActive(boolean active) {
		mIsActive = active;
		postInvalidate();
	}
	
	public void notifyReDownloadImage() {
		postInvalidate();
	}
	
	private int mIndex = -1;
	public void showPreview() {
		mIsShowEdit = false;
		if (mPage != null && GreetingCardUtil.isHaveNullTextLayer(mPage)) {
			GreetingCard currentCard = GreetingCardUtil.getCurrentGreetingCard();
			if (mIndex == -1) {			
				mIndex = GreetingCardUtil.getIndexByPageId(currentCard, mPage.id);
			}		
			mPage.setPageRefresh();
			currentCard.pages[mIndex] = mPage;
		}		
		postInvalidate();
	}
	
	public void showEdit() {
		mIsShowEdit = true;
		if (mPage != null && GreetingCardUtil.isHaveNullTextLayer(mPage)) {
			GreetingCard currentCard = GreetingCardUtil.getCurrentGreetingCard();
			if (mIndex == -1) {			
				mIndex = GreetingCardUtil.getIndexByPageId(currentCard, mPage.id);
			}		
			mPage.setPageRefresh();
			currentCard.pages[mIndex] = mPage;
		}		
		postInvalidate();
	}
	
	public void setDownloadImageSize(int width, int height) {
		mDownloadWidth = width;
		mDownloadHeight = height;
		postInvalidate();
	}
	
	private Bitmap getBitmap(GCPage page){	
		if (page == null) return null;
		Bitmap bitmap =  mContentBitmap;
		if (!page.isWantMainRefresh() && (bitmap == null || bitmap.isRecycled()) ) {
			bitmap = directUseUrlNative(page);
		}							
		if (page.isWantMainRefresh() || bitmap == null || bitmap.isRecycled()) {	
			if (imageDownloader != null && mDownloadWidth != 0 && mDownloadHeight != 0) {
				URI pictureURI = null;
				if (mIsShowEdit && GreetingCardUtil.isHaveNullTextLayer(mPage)) {			
					pictureURI = GreetingCardUtil.getSampleTextURI(mPage, mDownloadWidth, mDownloadHeight);
				}else {
					pictureURI = GreetingCardUtil.getURI(mPage, mDownloadWidth, mDownloadHeight);
				}
				GreetingCard card = GreetingCardUtil.getCurrentGreetingCard();
				String pageId = page.id;
				GCMainPageView.this.setTag(pageId);
				Log.d(TAG, "DOWNLOAD:" + pictureURI.toString());
				imageDownloader.downloadProfilePicture(pageId, pictureURI, GCMainPageView.this,0,false,card.id,page.getMainRefreshCount());
			}
														
		}
		return bitmap;
	}
	
	private Bitmap directUseUrlNative(GCPage page){
		if (page == null) return null;
		String pageId = page.id;
		String dispalyPath =  FilePathConstant.getLoadFilePath(FilePathConstant.cardType, pageId, true,page.getMainRefreshCount(),page.getMainRefreshSucCount());	
		if (dispalyPath == null) return null;
		Bitmap bitmap = BitmapFactory.decodeFile(dispalyPath);					
		return bitmap;
	}		
	
}
