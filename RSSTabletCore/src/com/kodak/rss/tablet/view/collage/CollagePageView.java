package com.kodak.rss.tablet.view.collage;

import java.net.URI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;

import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.collage.CollageLayer;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.view.MainPageView;

public class CollagePageView extends MainPageView<CollagePage, CollageLayer>{
		
	private RectF mRect4Frame;
	private Paint mPaint4Frame;	
	private Bitmap mContentBitmap;
	
	private Bitmap mWaitBitmap;
	private RectF mRect4Content;
	
	public CollagePageView(Context context) {
		super(context);
		init(context);
	}
	
	public CollagePageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public CollagePageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		mPaint4Frame = new Paint();
		mPaint4Frame.setStyle(Paint.Style.STROKE);
		mPaint4Frame.setColor(0xFFFBBA06);
		mPaint4Frame.setStrokeWidth(DimensionUtil.dip2px(getContext(), 4));
	}

	@Override
	public void setPage(CollagePage page) {		
		super.setPage(page);
		postInvalidate();
	}
	
	public void setHWRation(float hWRation) {
		if (hWRation <= 0) return;	
		int downloadWidth = (int) (mDownloadHeight/hWRation);
		if (downloadWidth > 0 &&  (downloadWidth - mDownloadWidth != 0)) {
			mDownloadWidth = downloadWidth;
			mContentBitmap = null;	
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (mPage == null) return;
		Bitmap bitmap = getPageBitmap();
		if (bitmap == null) {
			bitmap = getWaitBitmap();
		} 
		
		if (bitmap != null) {		
			canvas.drawBitmap(bitmap, null, mRect4Content, null);
		}
		
		if (mRect4Frame != null) {
			canvas.drawRect(mRect4Frame, mPaint4Frame);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		mRect4Content = new RectF(0, 0, w, h);		
	}
	
	public void setImageBitmap(Bitmap bitmap) {
		this.mContentBitmap = bitmap;
		invalidate();
	}
	
	@Override
	public Bitmap getImageBitmap() {
		return this.mContentBitmap;
	}
	
	private int mDownloadWidth, mDownloadHeight;
	private ImageUseURIDownloader imageDownloader;
	private LruCache<String, Bitmap> mMemoryCache;
	
	public void setDownParameter(int downWidth, int downHeight, LruCache<String, Bitmap> mMemoryCache,ImageUseURIDownloader downloader){
		this.mDownloadWidth = downWidth;
		this.mDownloadHeight = downHeight;
		this.mMemoryCache = mMemoryCache;
		this.imageDownloader = downloader;
	}
	
	private Bitmap getPageBitmap() {
		if (mPage == null) return null;
		Bitmap bitmap = mContentBitmap;
		if (!mPage.isWantMainRefresh() && (bitmap == null || bitmap.isRecycled()) ) {
			bitmap = directUseUrlNative();
		}
		if (mPage.isWantMainRefresh() || bitmap == null || bitmap.isRecycled()) {	
			if (imageDownloader != null && mDownloadWidth != 0 && mDownloadHeight != 0) {
				URI pictureURI = CalendarUtil.getURI(mPage, mDownloadWidth, mDownloadHeight);				
				Collage collage = CollageUtil.getCurrentCollage();
				String pageId = mPage.id;
				CollagePageView.this.setTag(pageId);				
				imageDownloader.downloadProfilePicture(pageId, pictureURI, CollagePageView.this, 0, false, collage.id, mPage.getMainRefreshCount());
			}
		}
		return bitmap;
	}
	
	private Bitmap directUseUrlNative(){
		if (mPage == null) return null;
		String pageId = mPage.id;
		Bitmap bitmap = MemoryCacheUtil.getBitmap(mMemoryCache, pageId);
		if (bitmap != null){
			mContentBitmap = bitmap;
			return bitmap;
		} 
		String dispalyPath =  FilePathConstant.getLoadFilePath(FilePathConstant.collageType, pageId, false, mPage.getMainRefreshCount(),mPage.getMainRefreshSucCount());	
		if (dispalyPath == null) return null;
		bitmap = BitmapFactory.decodeFile(dispalyPath);	
		mContentBitmap = bitmap;
		return bitmap;
	}		
	
	public Bitmap getWaitBitmap() {
		return mWaitBitmap;
	}
	
	public void setWaitBitmap(Bitmap bitmap) {
		mWaitBitmap = bitmap;
	}
	
	public Layer pointTo(float x, float y) {
		if (mPage == null || mPage.layers == null) return null;

		Layer resultLayer = null;		
		for (int i = 0; i < mPage.layers.length; i++) {
			Layer layer = mPage.layers[i];
			if (isPointInLayer(layer, x, y)) {
				resultLayer = layer;
				break;
			}
		}
		return resultLayer;	
	}
	
	public void showFrame() {
		float pw = mPaint4Frame.getStrokeWidth() / 2;
		RectF newFrame = new RectF(pw, pw, getWidth() - pw, getBottom() - pw);
		if (!newFrame.equals(mRect4Frame)) {
			mRect4Frame = newFrame;
			postInvalidate();
		}
	}
	
	public void showFrame(Layer layer) {
		RectF newFrame = getLayerRect(layer);
		if (newFrame != null && !newFrame.equals(mRect4Frame)) {
			mRect4Frame = newFrame;
			postInvalidate();
		}
	}
	
	public void hideAllFrame() {
		if (mRect4Frame != null) {
			mRect4Frame = null;
			postInvalidate();
		}
	}
	
}
