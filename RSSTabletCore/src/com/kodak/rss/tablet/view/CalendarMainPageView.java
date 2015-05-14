package com.kodak.rss.tablet.view;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.Pair;

import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.calendar.CalendarDaysGridInfo;
import com.kodak.rss.core.n2r.bean.calendar.CalendarGridItemPO;
import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;

public class CalendarMainPageView extends MainPageView<CalendarPage, CalendarLayer>{
	private Bitmap mWaitBitmap;
	private Bitmap mContentBitmap;
	private RectF mRect4Content;
	
	private RectF mRect4Frame;
	private Paint mPaint4Frame;
	
	private CalendarDaysGridInfo mCalendarDaysGridInfo;
	private List<RectF> mRects4DaysGridCell;

	public CalendarMainPageView(Context context, AttributeSet attrs,int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public CalendarMainPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CalendarMainPageView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		mPaint4Frame = new Paint();
		mPaint4Frame.setStyle(Paint.Style.STROKE);
		mPaint4Frame.setColor(0xFFFBBA06);
		mPaint4Frame.setStrokeWidth(DimensionUtil.dip2px(getContext(), 4));
	}
	
	@Override
	public void setPage(CalendarPage page) {
		if (mPage != null && mPage.id != null && page != null && page.id != null && !mPage.id.equals(page.id)) {
			mContentBitmap = null;
		}		
		super.setPage(page);
		
		CalendarLayer layer = CalendarUtil.getDaysGridLayer(page);
		if (layer != null) {
			mCalendarDaysGridInfo = CalendarUtil.getDaysGridInfo(layer);
			mRects4DaysGridCell = getDaysGridCellRects();
		}
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (mPage == null) {
			return;
		}
		
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
	
	private ImageUseURIDownloader imageDownloader;
	private LruCache<String, Bitmap> mMemoryCache;
	private int mDownloadWidth, mDownloadHeight;
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
				Calendar calendar = CalendarUtil.getCurrentCalendar();
				String pageId = mPage.id;
				CalendarMainPageView.this.setTag(pageId);				
				imageDownloader.downloadProfilePicture(pageId, pictureURI, CalendarMainPageView.this,0,false,calendar.id,mPage.getMainRefreshCount());
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
		String dispalyPath =  FilePathConstant.getLoadFilePath(FilePathConstant.calendarType, pageId, false, mPage.getMainRefreshCount(),mPage.getMainRefreshSucCount());	
		if (dispalyPath == null) return null;
		bitmap = BitmapFactory.decodeFile(dispalyPath);	
		mContentBitmap = bitmap;
		return bitmap;
	}		
	
	private List<RectF> getDaysGridCellRects() {
		if (mCalendarDaysGridInfo != null) {
			List<RectF> list = new ArrayList<RectF>();
			RectF rect4FirstCell = getRectFromROI(mCalendarDaysGridInfo.firstCellLocation);
			
			for (int m = 0; m < mCalendarDaysGridInfo.cellRows; m++) {
				for (int n = 0 ; n < mCalendarDaysGridInfo.cellColumns; n++) {
					RectF rect = new RectF(rect4FirstCell);
					float dx = n * rect4FirstCell.width();
					float dy = m * rect4FirstCell.height();
					rect.offset(dx, dy);
					list.add(rect);
				}
			}
			return list;
		}
		
		return null;
		
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		mRect4Content = new RectF(0, 0, w, h);
		mRects4DaysGridCell = getDaysGridCellRects();
	}
	
	@Override
	protected CalendarLayer getTouchOnLayer(float x, float y) {
		CalendarLayer layer = super.getTouchOnLayer(x, y);
		CalendarLayer result = layer;
		if (layer != null && layer.sublayers != null) {
			boolean isSubImageLayer = false;
			//in calendar, there is subLayer, so we need check it
			for (int i = 0; i < layer.sublayers.length; i++) {
				if (isPointInLayer(layer.sublayers[i], x, y)) {
					if (Layer.TYPE_IMAGE.equals(layer.sublayers[i].type)) {
						isSubImageLayer = true;
						result = layer.sublayers[i];
					} else if (Layer.TYPE_TEXT_BLOCK.equals(layer.sublayers[i].type) && !isSubImageLayer) {
						result = layer.sublayers[i];
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * @param x
	 * @param y
	 * @return CalendarLayer or CalendarGridItemPO
	 */
	public Pair<CalendarLayer, CalendarGridItemPO> pointTo(float x, float y) {
		if (mPage == null || mPage.layers == null) {
			return null;
		}
		
		
		CalendarLayer resultLayer = null;
		CalendarGridItemPO resultPo = null;
		for (int i = 0; i < mPage.layers.length; i++) {
			CalendarLayer layer = mPage.layers[i];
			if (isPointInLayer(layer, x, y)) {
//				resultLayer = layer;
				if (layer.isCalendarDaysGridLayer()) {
					if (mRects4DaysGridCell != null) {
						for (int m = 0; m < mRects4DaysGridCell.size(); m ++) {
							if (mRects4DaysGridCell.get(m).contains(x, y)) {								
								resultPo = CalendarUtil.getDayInfoByIndex(layer, m);								
								break;
							}
						}
					}
				}				
				resultLayer = CalendarUtil.getSubLayerByItemPo(layer,resultPo);				
				if (resultLayer == null) {
					resultLayer = layer;
				}	
				break;
			}
		}
		
		if (resultLayer == null) {
			return null;
		} else {
			return new Pair<CalendarLayer, CalendarGridItemPO>(resultLayer,resultPo);
		}
	}
	
	public Bitmap getWaitBitmap() {
		return mWaitBitmap;
	}
	
	public void setWaitBitmap(Bitmap bitmap) {
		mWaitBitmap = bitmap;
	}
	
	public void showFrame(CalendarLayer layer) {
		RectF newFrame = getLayerRect(layer);
		if (newFrame != null && !newFrame.equals(mRect4Frame)) {
			mRect4Frame = newFrame;
			postInvalidate();
		}
	}
	
	public void showFrame(CalendarGridItemPO po) {
		RectF newFrame = null;
		if (mRects4DaysGridCell != null && mCalendarDaysGridInfo != null) {
			int index = po.holdIndex;
			if (index < 0) {
				index = po.day + mCalendarDaysGridInfo.firstDayCellIndex;
			}
			newFrame = mRects4DaysGridCell.get(index);
		}
		
		if (newFrame != null && !newFrame.equals(mRect4Frame)) {
			mRect4Frame = newFrame;
			postInvalidate();
		}
	}
	
	/**
	 * show page frame
	 */
	public void showFrame() {
		float pw = mPaint4Frame.getStrokeWidth() / 2;
		RectF newFrame = new RectF(pw, pw, getWidth() - pw, getBottom() - pw);
		if (!newFrame.equals(mRect4Frame)) {
			mRect4Frame = newFrame;
			postInvalidate();
		}
	}
	
	public void hideAllFrames() {
		if (mRect4Frame != null) {
			mRect4Frame = null;
			postInvalidate();
		}
	}
	
	@Override
	public Bitmap getImageBitmap() {
		return mContentBitmap;
	}
	
	public void setImageBitmap(Bitmap bitmap) {
		mContentBitmap = bitmap;
	}
	
}
