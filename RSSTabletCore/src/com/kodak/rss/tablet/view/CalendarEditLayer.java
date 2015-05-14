package com.kodak.rss.tablet.view;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.core.util.FileDownloader;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfos;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.view.EditImageView.OnEditlistener;
import com.kodak.rss.tablet.view.EditImageView.OnRotateListener;
import com.kodak.rss.tablet.view.ProductEditPopView.OnEditItemClickListener;

public class CalendarEditLayer extends RelativeLayout{
	private static final String TAG = "CalendarEditLayer";
	
	private static final float MAX_ZOOM_IN_SCALE = 5f;
	
	private EditImageView ivEdit;
	private CalendarEditPopView pop;
	private ProgressBar pbEditLayer;
	private ProgressBar pbEditPage;
	private ProgressBar pbDownloadLayer;
	private View viewLayer4EditProgress;
	private WeakReference<CalendarMainPageView> pageViewRef;
	private Point screenSize;
	private OnEditItemClickListener onEditItemClickListener;
	private OnDoneClickListener onDoneClickListener;
	private boolean editPage;//edit page or layer(image)
	private CalendarEditActivity activity;
	
	private Point pageViewLocation;
	private int pageViewWidth;
	private int pageViewHeight;
	private RectF layerRect;
	
	public CalendarEditLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CalendarEditLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CalendarEditLayer(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		inflate(context, R.layout.calendar_edit_layer, this);
		ivEdit = (EditImageView) findViewById(R.id.iv_edit);
		pop = (CalendarEditPopView) findViewById(R.id.edit_pop);
		pbEditLayer = (ProgressBar) findViewById(R.id.edit_progress_for_layer);
		pbEditPage = (ProgressBar) findViewById(R.id.edit_progress_for_page);
		pbDownloadLayer = (ProgressBar) findViewById(R.id.download_progress_for_layer);
		viewLayer4EditProgress = findViewById(R.id.layer_for_edit_progress);
		
		screenSize = new Point();
		pageViewRef = new WeakReference<CalendarMainPageView>(null);
		activity = (CalendarEditActivity) context;
		activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
		
	}
	
	public void setOnPopEditItemClickListener(OnEditItemClickListener<CalendarEditPopView, CalendarPage, CalendarLayer> onEditItemClickListener){
		this.onEditItemClickListener = onEditItemClickListener;
	}
	
	public void setOnDoneClickListener(OnDoneClickListener onDoneClickListener){
		this.onDoneClickListener = onDoneClickListener;
	}
	
	public void selectCurrentPage(){
		ivEdit.setVisibility(View.INVISIBLE);
		CalendarMainPageView pageView = pageViewRef.get();
		if(pageView!=null){
			pageView.setSelected(true);
			showPageEditPop(pageView, pageView.getPage());
		}
	}
	
	public void showPageEditPop(final CalendarMainPageView pageView, CalendarPage page){
		dismissEditProgress();
		this.pageViewRef = new WeakReference<CalendarMainPageView>(pageView);
		pageViewLocation = getPageViewLocationInWindow(pageView);
		pageViewWidth = pageView.getWidth();
		pageViewHeight = pageView.getHeight();
		
		editPage = true;
		setVisibility(View.VISIBLE);
		pop.setInfo(page, null);
		
		setPageEditLayout(pageView);
		
		pop.setOnDoneClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onDoneClickListener != null){
					onDoneClickListener.OnDoneClick(CalendarEditLayer.this,pageView,ivEdit);
				}else{
					dismiss();
				}
			}
		});
		
		if(onEditItemClickListener != null){
			pop.setOnEditItemClickListener(onEditItemClickListener);
		}
		
		setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onDoneClickListener != null){
					onDoneClickListener.OnDoneClick(CalendarEditLayer.this,pageView,ivEdit);
				}else{
					dismiss();
				}
			}
		});
		
		pop.setVisibility(View.VISIBLE);
		
	}
	
	public void showEditImageAndPop(final CalendarMainPageView pageView, CalendarLayer layer, RectF layerRect){
		dismissEditProgress();
		setVisibility(VISIBLE);
		this.pageViewRef = new WeakReference<CalendarMainPageView>(pageView);
		this.layerRect = layerRect;
		
		editPage = false;
		showEditImage(pageView, layer, layerRect);
		showEditImagePop(pageView, layer, layerRect);
		
		pop.setOnDoneClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onDoneClickListener != null){
					onDoneClickListener.OnDoneClick(CalendarEditLayer.this,pageView,ivEdit);
				}else{
					dismiss();
				}
			}
		});
		
		//when click on empty area, dismiss edit pop
		setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(onDoneClickListener != null){
					onDoneClickListener.OnDoneClick(CalendarEditLayer.this,pageView,ivEdit);
				}else{
					dismiss();
				}
			}
		});
	}
	
	public void showEditImageAndPop(final CalendarMainPageView pageView,final CalendarLayer layer){
		//maybe pageview havn't get size, so run delayed
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				showEditImageAndPop(pageView, layer, pageView.getLayerRect(layer));
			}
		}, 50);
	}
	
	public void showPageEditProgress(final CalendarPage page){
			RelativeLayout.LayoutParams pbParams = (LayoutParams) pbEditPage.getLayoutParams();
			int[] rootLocation = new int[2];
			getLocationOnScreen(rootLocation);
			//If you try to use pageViewRef.get() to get the pageView, then to get its location and size, it will cause some problem
			//So it's better to init these values at first
			pbParams.leftMargin = (int) (pageViewLocation.x - rootLocation[0] + pageViewWidth/2 - pbEditPage.getWidth()/2);
			pbParams.topMargin = (int) (pageViewLocation.y - rootLocation[1] + pageViewHeight/2 - pbEditPage.getHeight()/2);
			
			pbEditPage.setVisibility(View.VISIBLE);
			pbEditPage.requestLayout();
			
			viewLayer4EditProgress.setVisibility(View.VISIBLE);
	}
	
	public void dismissEditProgress(){
		pbEditLayer.setVisibility(View.INVISIBLE);
		pbEditPage.setVisibility(View.INVISIBLE);
		pbDownloadLayer.setVisibility(View.INVISIBLE);
		viewLayer4EditProgress.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * @return true: edit page.  false: edit image
	 */
	public boolean isEditPage(){
		return editPage;
	}
	
	public boolean isInEditProgress() {
		return pbEditLayer.getVisibility() == View.VISIBLE || pbEditPage.getVisibility() == View.VISIBLE;
	}
	
	public void showLayerEditProgress(final CalendarPage page,final Layer layer){
		RelativeLayout.LayoutParams pbParams = (LayoutParams) pbEditLayer.getLayoutParams();
		
		if(layerRect != null){
			pbParams.leftMargin = (int) (layerRect.width()/2 - pbEditLayer.getWidth()/2);
			pbParams.topMargin = (int) (layerRect.height()/2 - pbEditLayer.getHeight()/2.0+ivEdit.getRotateIconHeight()/2.0);
			pbEditLayer.requestLayout();
			pbEditLayer.setVisibility(View.VISIBLE);
			
			viewLayer4EditProgress.setVisibility(View.VISIBLE);
		}
	}
	
	private void showLayerDownloadProgress(final CalendarPage page, final Layer layer){
		RelativeLayout.LayoutParams pbParams = (LayoutParams) pbDownloadLayer.getLayoutParams();
		
		if(layerRect != null){
			pbParams.leftMargin = (int) (layerRect.width()/2 - pbDownloadLayer.getWidth()/2);
			pbParams.topMargin = (int) (layerRect.height()/2 - pbDownloadLayer.getHeight()/2.0+ivEdit.getRotateIconHeight()/2.0);
			pbDownloadLayer.requestLayout();
			pbDownloadLayer.setVisibility(View.VISIBLE);
		}
	}
	
	public CalendarMainPageView getPageView(){
		if(pageViewRef != null){
			return pageViewRef.get();
		}
		
		return null;
	}
	
	public EditImageView getEditImageView(){
		return ivEdit;
	}
	
	public int getEditPopViewWidth() {
		return pop.getWidth();
	}
	
	public int getEditPopViewHeight() {
		return pop.getHeight();
	}
	
	public void dismiss(){
		setVisibility(View.INVISIBLE);
		pop.setVisibility(View.INVISIBLE);
		ivEdit.setVisibility(View.INVISIBLE);
		
		CalendarMainPageView pageView = pageViewRef.get();
		if(pageView != null){
			pageViewRef.get().setSelected(false);
		}
	}
	
	public static interface OnDoneClickListener{
		/**
		 * pageView or ivEdit maybe null, it depends on whether the pop shown is for page or layer
		 * @param editLayer
		 * @param pageView
		 * @param ivEdit
		 */
		void OnDoneClick(CalendarEditLayer editLayer, CalendarMainPageView pageView, EditImageView ivEdit);
	}
	
	private void showEditImage(final CalendarMainPageView pageView, final Layer layer,
			final RectF layerRect){
		if(Layer.TYPE_TEXT_BLOCK.equals(layer.type)){
			showEditImage(pageView, layer, layerRect,null);
			return;
		}
		ProductLayerLocalInfos infos = RssTabletApp.getInstance().getProductLayerLocalInfos();
		ProductLayerLocalInfo info = infos.get(layer.contentId);
		boolean shown = false;
		if(info == null){
			activity.addLayerLocalInfo(layer);
			info = infos.get(layer.contentId);
		}
		
//		if(!info.isUseServerImage){//local image
//			String imagePath = CalendarUtil.getLocalImagePathByContentId(layer.contentId);
//			Log.i(TAG, "image path:"+imagePath);
//			if(imagePath == null || !new File(imagePath).exists()){
//				Log.i(TAG, "file not exist");
//				info.isUseServerImage = true;
//			}else{
//				showEditImage(pageView, layer, layerRect, decodeScaleBitmap(imagePath, (int)layerRect.width()*2, (int)layerRect.height()*2));
//				shown = true;
//			}
//		}
		
		if(!shown){//need download from server or use cached image
			new GetLayerImageFromServerTask(pageView, layer, layerRect, info, true).execute();
		}
	}
	
	private class GetLayerImageFromServerTask extends AsyncTask<Void, Void, Boolean> {
		long mStartTime;
		String mFilePath;
		String mUrl;
		boolean mNeedRefresh;
		ProductLayerLocalInfo mLayerInfo;
		boolean mLowRes;
		Layer mLayer;
		CalendarMainPageView mPageView;
		RectF mLayerRect;
		
		/**
		 * @param pageView
		 * @param layer
		 * @param layerRect
		 * @param layerInfo
		 * @param lowRes Set it true, it will download super high res image after download low res image 
		 */
		GetLayerImageFromServerTask(CalendarMainPageView pageView, Layer layer,
				RectF layerRect, ProductLayerLocalInfo layerInfo,  boolean lowRes) {
			mLayerInfo = layerInfo;
			mLowRes = lowRes;
			mLayer = layer;
			mPageView = pageView;
			mLayerRect = layerRect;
			
			if (lowRes) {
				showEditImage(pageView, layer, layerRect, null);//show image frame without image
				showLayerDownloadProgress(pageView.getPage(),layer);
				
				mFilePath = CalendarUtil.getLayerImageLowResCacheFilePath(layer);
				mUrl = CalendarUtil.getLayerLowResUrl(layer);
				mNeedRefresh = layerInfo.isNeedRefreshForLowRes;
			} else {
				//when downloading super high res image, the low res image has been shown, so we don't need to show image frame and progress
				mFilePath = CalendarUtil.getLayerImageSuperHighResCacheFilePath(layer);
				mUrl = CalendarUtil.getLayerSuperHighResUrl(layer);
				mNeedRefresh = layerInfo.isNeedRefreshForSuperHighRes;
			}
			
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			mStartTime = System.currentTimeMillis();
			
			if (!mNeedRefresh && new File(mFilePath).exists()) {
				return true;
			} else {
				return FileDownloader.download(mUrl, mFilePath);
			}
			
		}
		
		@Override
		protected void onPostExecute(Boolean succeed) {
			boolean isImageLatest = mStartTime >= mLayerInfo.getLatestTimeForNeedRefresh();
			if (succeed && isImageLatest) {
				if (mLowRes) {
					mLayerInfo.isNeedRefreshForLowRes = false;
				} else {
					mLayerInfo.isNeedRefreshForSuperHighRes = false;
				}
			}
			
			//check if current image is need update, maybe user have change to edit another image
			if(View.VISIBLE == ivEdit.getVisibility() && ivEdit.getLayer()!=null && ivEdit.getLayer().contentId.equals(mLayer.contentId)){
				if (mLowRes) {
					dismissEditProgress();
				}
				
				if(succeed){
					if(isImageLatest){
						if (mLowRes) {
							updateEditImageBitmap(mPageView.getPage(), mLayer, decodeScaleBitmap(mFilePath, (int)(ivEdit.getWidth()*1.5), (int)(ivEdit.getHeight()*1.5)));
							new GetLayerImageFromServerTask(mPageView, mLayer, mLayerRect, mLayerInfo, false).execute();
						} else {
							ivEdit.updateImage(decodeScaleBitmap(mFilePath, (int)(ivEdit.getWidth()*2), (int)(ivEdit.getHeight()*2)));
						}
					}else{
						new GetLayerImageFromServerTask(mPageView, mLayer, mLayerRect, mLayerInfo, mLowRes).execute();
					}
				}
			}
		}
		
	}
	
	private void updateEditImageBitmap(CalendarPage page, Layer layer, Bitmap bitmap){
		ivEdit.setEditBitmap(bitmap);
		RelativeLayout.LayoutParams params = (LayoutParams) ivEdit.getLayoutParams();
		ivEdit.setBasicParams(page, layer, params.width, params.height);
		ivEdit.setMaxZoomInScale(MAX_ZOOM_IN_SCALE);
	}
	
	private void showEditImage(CalendarMainPageView pageView, Layer layer,
			RectF layerRect,Bitmap bitmap){
		ivEdit.setEditBitmap(bitmap);
		
		setImageEditLayout(pageView, layer, layerRect);
		RelativeLayout.LayoutParams params = (LayoutParams) ivEdit.getLayoutParams();
		ivEdit.setBasicParams(pageView.getPage(), layer, params.width, params.height);
		ivEdit.setMaxZoomInScale(MAX_ZOOM_IN_SCALE);
		ivEdit.setOnRotateListener(new OnRotateListener() {
			
			@Override
			public void onRotateStart() {
				pop.setAlpha(0.6f);
				
			}
			
			@Override
			public void onRotateEnd() {
				pop.setAlpha(1);
				
			}
		});
		
		ivEdit.setOnEditListener(new OnEditlistener() {
			
			@Override
			public void onEditStart(int mode) {
				pop.setPopTouchable(false);
			}
			
			@Override
			public void onEditEnd(int mode) {
				pop.setPopTouchable(true);
			}
		});
		
		ivEdit.setVisibility(View.VISIBLE);
	}
	
	private void showEditImagePop(CalendarMainPageView pageView, CalendarLayer layer,
			RectF layerRect){
		pop.setInfo(pageView.getPage(), layer);
		
		setImageEditPopLayout(pageView, layer, layerRect);
		
		pop.setOnDoneClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setVisibility(View.INVISIBLE);
				pop.setVisibility(View.INVISIBLE);
				ivEdit.setVisibility(View.INVISIBLE);
			}
		});
		
		if(onEditItemClickListener != null){
			pop.setOnEditItemClickListener(onEditItemClickListener);
		}
		
		pop.setVisibility(View.VISIBLE);
	}
	
	private void setImageEditLayout(CalendarMainPageView pageView, Layer layer,
			RectF layerRect){
		int[] pageViewLocation = new int[2];
		pageView.getLocationInWindow(pageViewLocation);
		
		int[] layerEditLoction = new int[2];
		getLocationInWindow(layerEditLoction);
		
		int x = pageViewLocation[0] + (int)layerRect.left - layerEditLoction[0];
		int y = pageViewLocation[1] + (int)layerRect.top - layerEditLoction[1];
		
		RelativeLayout.LayoutParams params = (LayoutParams) ivEdit.getLayoutParams();
		params.leftMargin = x;
		params.topMargin = y-ivEdit.getPaddingTop() + ivEdit.getPaddingBottom();
		params.width = (int)layerRect.width();
		params.height = (int)layerRect.height() + ivEdit.getPaddingTop() - ivEdit.getPaddingBottom();
		ivEdit.requestLayout();
	}
	
	private void setImageEditPopLayout(CalendarMainPageView pageView, Layer layer,
			RectF layerRect){
		int[] pageViewLocation = new int[2];
		pageView.getLocationOnScreen(pageViewLocation);
		
		int[] layerEditLocation = new int[2];
		getLocationOnScreen(layerEditLocation);
		
		Rect archorRectOnScreen = new Rect();
		archorRectOnScreen.left = (int) (pageViewLocation[0] + layerRect.left - ivEdit.getPaddingLeft());
		archorRectOnScreen.right = (int) (pageViewLocation[0] + layerRect.right + ivEdit.getPaddingRight());
		archorRectOnScreen.top = (int) (pageViewLocation[1] + layerRect.top);
		archorRectOnScreen.bottom = (int) (pageViewLocation[1] + layerRect.bottom);
		
		setEditPopLayout(archorRectOnScreen, layerEditLocation);
	}
	
	private void setEditPopLayout(Rect archorRectOnScreen,int[] layerEditLocation){
		RelativeLayout.LayoutParams params = (LayoutParams) pop.getLayoutParams();
		int w = screenSize.x/4 + DimensionUtil.dip2px(getContext(), 20) ;
		int h = (int) (screenSize.y/1.5);
		
		params.width = w;
		params.height = h;
		boolean showInLeft = archorRectOnScreen.left - w > 0 || archorRectOnScreen.left > screenSize.x - archorRectOnScreen.right;
		pop.setShowInLeft(showInLeft);
		
		////set left margin
		int left;
		if(showInLeft){
			left = archorRectOnScreen.left - w;
		}else{
			left = archorRectOnScreen.right;
		}
		left = left - layerEditLocation[0];
		if(left<0){
			left = 0;
		}
		if(left>screenSize.x -w){
			left = screenSize.x - w;
		}
		params.leftMargin = left;;
		
		/////set top margin
		int top = (archorRectOnScreen.top + archorRectOnScreen.bottom-h)/2;
		if(top < archorRectOnScreen.top){
			top = archorRectOnScreen.top;
		}
		if(top + h > layerEditLocation[1] + getHeight()){
			top = layerEditLocation[1] + getHeight() - h;
		}
		params.topMargin = top - layerEditLocation[1];
		
		pop.setPointY((archorRectOnScreen.top + archorRectOnScreen.bottom)/2 - top);
		
		pop.requestLayout();
	}
	
	private void setPageEditLayout(CalendarMainPageView pageView){
		Point p = getPageViewLocationInWindow(pageView);
		
		int[] layerEditLocation = new int[2];
		getLocationInWindow(layerEditLocation);
		
		Rect archorRectOnScreen = new Rect();
		archorRectOnScreen.left = p.x;
		archorRectOnScreen.right = p.x + pageView.getWidth();
		archorRectOnScreen.top = p.y;
		archorRectOnScreen.bottom = p.y + pageView.getHeight();
		
		setEditPopLayout(archorRectOnScreen, layerEditLocation);
	}
	
	private Point getPageViewLocationInWindow(CalendarMainPageView pageView){
		int[] location = new int[2];
		pageView.getLocationInWindow(location);
		
		int x = location[0];
		int y = location[1];
		
		Point p = new Point(x, y);
		
		return p;
	}
	
	private Bitmap decodeScaleBitmap(String path, int w, int h){
		if(path == null){
			return null;
		}
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		
		if(w == 0) w = screenSize.x/2;
		if(h == 0) h = screenSize.y/2;
		int sw = opts.outWidth/w + 1;
		int sh = opts.outHeight/h + 1;
		opts.inSampleSize = sw>sh ? sw : sh;
		opts.inJustDecodeBounds = false;
		Bitmap bm = null;
		try{
			bm = BitmapFactory.decodeFile(path, opts);
			//check exif info, maybe need rotate
			int exifOri = ImageUtil.getExifOrientation(path);
			int degrees = 0;
			if(exifOri == ExifInterface.ORIENTATION_ROTATE_90){
				degrees = 90;
			}else if(exifOri == ExifInterface.ORIENTATION_ROTATE_180){
				degrees = 180;
			}else if(exifOri == ExifInterface.ORIENTATION_ROTATE_270){
				degrees = 270;
			}
			
			if(degrees != 0){
				Bitmap newBm = ImageUtil.rotateBitmap(bm, degrees);
				bm.recycle();
				return newBm;
			}
		}catch(OutOfMemoryError e){
			e.printStackTrace();
			//check exif info, maybe need rotate
			int exifOri = ImageUtil.getExifOrientation(path);
			int degrees = 0;
			if(exifOri == ExifInterface.ORIENTATION_ROTATE_90){
				degrees = 90;
			}else if(exifOri == ExifInterface.ORIENTATION_ROTATE_180){
				degrees = 180;
			}else if(exifOri == ExifInterface.ORIENTATION_ROTATE_270){
				degrees = 270;
			}
			
			if(degrees != 0){
				Bitmap newBm = ImageUtil.rotateBitmap(bm, degrees);
				bm.recycle();
				return newBm;
			}
		}
		
		
		return bm;
	}
}
