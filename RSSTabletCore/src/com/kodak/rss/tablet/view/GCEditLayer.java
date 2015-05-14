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

import com.kodak.rss.core.n2r.bean.greetingcard.GCLayer;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.core.util.FileDownloader;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.GCEditActivity;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfos;
import com.kodak.rss.tablet.thread.GCGetPreviewWithHoleTask;
import com.kodak.rss.tablet.util.GreetingCardUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.EditImageView.OnEditlistener;
import com.kodak.rss.tablet.view.EditImageView.OnRotateListener;
import com.kodak.rss.tablet.view.ProductEditPopView.OnEditItemClickListener;

public class GCEditLayer extends EditLayer{
	private final String TAG = "GCEditLayer";
	
	private static final float MAX_ZOOM_IN_SCALE = 5f;
	
	private EditImageView ivEdit;
	private GCEditPopView pop;
	private OnEditItemClickListener onEditItemClickListener;
	private OnDoneClickListener onDoneClickListener;
	private ProgressBar editLayer;
	private ProgressBar downloadLayer;
	private WeakReference<GCMainPageView> pageViewRef;
	private Point screenSize;
	private GCEditActivity activity;
	private RectF layerRect;
	private GCPage editingPage;
	private Context context;
	
	public GCEditLayer(Context context) {
		super(context);
		init(context);
	}
	
	public GCEditLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public GCEditLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public void setOnPopEditItemClickListener(OnEditItemClickListener<GCEditPopView, GCPage, GCLayer> onEditItemClickListener){
		this.onEditItemClickListener = onEditItemClickListener;
	}
	
	public void setOnDoneClickListener(OnDoneClickListener onDoneClickListener){
		this.onDoneClickListener = onDoneClickListener;
	}
	
	private void init(Context context){
		this.context = context;
		inflate(context, R.layout.greetingcard_edit_layer, this);
		ivEdit = (EditImageView) findViewById(R.id.iv_edit);
		pop = (GCEditPopView) findViewById(R.id.edit_pop);
		editLayer = (ProgressBar) findViewById(R.id.edit_progress_for_layer);
		downloadLayer = (ProgressBar) findViewById(R.id.download_progress_for_layer);
		
		screenSize = new Point();
		pageViewRef = new WeakReference<GCMainPageView>(null);
		activity = (GCEditActivity) context;
		activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
	}
	
	public void showEditImageAndPop(final GCMainPageView pageView, GCPage page, GCLayer layer, RectF layerRect){
		dismissEditProgress();
		setVisibility(VISIBLE);
		this.pageViewRef = new WeakReference<GCMainPageView>(pageView);
		this.layerRect = layerRect;
		editingPage = page;
		
		showEditImage(pageView, page, layer, layerRect);
		showEditImagePop(pageView, layer, layerRect);
		
		pop.setInfo(page, layer);	
		pop.setOnDoneClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onDoneClickListener != null){
					onDoneClickListener.OnDoneClick(GCEditLayer.this,pageView,ivEdit);
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
					onDoneClickListener.OnDoneClick(GCEditLayer.this,pageView,ivEdit);
				}else{
					dismiss();
				}
			}
		});
	}
	
	public void showEditImageAndPop(final GCMainPageView pageView, final GCPage page, final GCLayer layer){
		//maybe pageview havn't get size, so run delayed
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				showEditImageAndPop(pageView, page, layer, pageView.getLayerRect(layer));
			}
		}, 500);
	}

	public void dismissEditProgress(){
		editLayer.setVisibility(View.INVISIBLE);
		downloadLayer.setVisibility(View.INVISIBLE);
	}
	
	public void showLayerEditProgress(final GCPage page,final Layer layer){
		RelativeLayout.LayoutParams pbParams = (LayoutParams) editLayer.getLayoutParams();
		
		if(layerRect != null){
			pbParams.leftMargin = (int) (layerRect.width()/2 - editLayer.getWidth()/2);
			pbParams.topMargin = (int) (layerRect.height()/2 - editLayer.getHeight()/2.0+ivEdit.getRotateIconHeight()/2.0);
			editLayer.requestLayout();
			editLayer.setVisibility(View.VISIBLE);
		}
	}
	
	private void showLayerDownloadProgress(final GCPage page, final Layer layer){
		RelativeLayout.LayoutParams pbParams = (LayoutParams) downloadLayer.getLayoutParams();
		
		if(layerRect != null){
			pbParams.leftMargin = (int) (layerRect.width()/2 - downloadLayer.getWidth()/2);
			pbParams.topMargin = (int) (layerRect.height()/2 - downloadLayer.getHeight()/2.0+ivEdit.getRotateIconHeight()/2.0);
			downloadLayer.requestLayout();
			downloadLayer.setVisibility(View.VISIBLE);
		}
	}
	
	public GCMainPageView getPageView(){
		if(pageViewRef != null){
			return pageViewRef.get();
		}
		
		return null;
	}
	
	public EditImageView getEditImageView(){
		return ivEdit;
	}
	
	public void dismiss(){
		setVisibility(View.INVISIBLE);
		pop.setVisibility(View.INVISIBLE);
		//ivEdit.setVisibility(View.INVISIBLE);
		ivEdit.dismiss();
		
		GCMainPageView pageView = pageViewRef.get();
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
		void OnDoneClick(GCEditLayer editLayer, GCMainPageView pageView, EditImageView ivEdit);
	}
	
	private void showEditImage(final GCMainPageView pageView, final GCPage page, final Layer layer, final RectF layerRect){
		ProductLayerLocalInfos infos = RssTabletApp.getInstance().getProductLayerLocalInfos();
		ProductLayerLocalInfo info = infos.get(layer.contentId);
		boolean shown = false;
		if(info == null){
			activity.addLayerLocalInfo(layer);
			info = infos.get(layer.contentId);
		}
		if(info == null || (!info.isUseServerImage && !info.isNeedRefreshForLowRes)){//normally info is not null
			String imagePath = GreetingCardUtil.getLocalImagePathByContentId(layer.contentId);
			Log.i(TAG, "image path:"+imagePath);
			if(imagePath == null || !new File(imagePath).exists()){
				Log.i(TAG, "file not exist");
				if(info!=null){
					info.isUseServerImage = true;
				}
			}else{
				showEditImage(pageView, layer, layerRect, decodeScaleBitmap(imagePath, (int)layerRect.width()*2, (int)layerRect.height()*2));
				shown = true;
			}
		}
		
		// Get the mask
		new GCGetPreviewWithHoleTask(context, ivEdit, page, layer).execute();
		
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
		GCMainPageView mPageView;
		RectF mLayerRect;
		
		/**
		 * @param pageView
		 * @param layer
		 * @param layerRect
		 * @param layerInfo
		 * @param lowRes Set it true, it will download super high res image after download low res image 
		 */
		GetLayerImageFromServerTask(GCMainPageView pageView, Layer layer,
				RectF layerRect, ProductLayerLocalInfo layerInfo,  boolean lowRes) {
			mLayerInfo = layerInfo;
			mLowRes = lowRes;
			mLayer = layer;
			mPageView = pageView;
			mLayerRect = layerRect;
			
			if (lowRes) {
				showEditImage(pageView, layer, layerRect, null);//show image frame without image
				showLayerDownloadProgress(pageView.getPage(),layer);
				
				mFilePath = PhotoBookProductUtil.getLayerImageLowResCacheFilePath(layer);
				mUrl = PhotoBookProductUtil.getLayerLowResUrl(layer);
				mNeedRefresh = layerInfo.isNeedRefreshForLowRes;
			} else {
				//when downloading suer high res image, the low res image has been shown, so we don't need to show image frame and progress
				mFilePath = PhotoBookProductUtil.getLayerImageSuperHighResCacheFilePath(layer);
				mUrl = PhotoBookProductUtil.getLayerSuperHighResUrl(layer);
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
	
	private void updateEditImageBitmap(GCPage page, Layer layer, Bitmap bitmap){
		ivEdit.setEditBitmap(bitmap);
		RelativeLayout.LayoutParams params = (LayoutParams) ivEdit.getLayoutParams();
		ivEdit.setBasicParams(page, layer, params.width, params.height);
		ivEdit.setMaxZoomInScale(MAX_ZOOM_IN_SCALE);
	}
	
	private void showEditImage(GCMainPageView pageView, Layer layer, RectF layerRect,Bitmap bitmap){
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
		ivEdit.setMask(null);
	}
	
	private void showEditImagePop(GCMainPageView pageView, GCLayer layer, RectF layerRect){
		pop.setInfo(pageView.getPage(), layer);
		
		setImageEditPopLayout(pageView, layer, layerRect);
		
		pop.setOnDoneClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setVisibility(View.INVISIBLE);
				pop.setVisibility(View.INVISIBLE);
				//ivEdit.setVisibility(View.INVISIBLE);
				ivEdit.dismiss();
			}
		});
		
		if(onEditItemClickListener != null){
			pop.setOnEditItemClickListener(onEditItemClickListener);
		}
		
		pop.setVisibility(View.VISIBLE);
	}
	
	private void setImageEditLayout(GCMainPageView pageView, Layer layer,
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
	
	private void setImageEditPopLayout(GCMainPageView pageView, Layer layer,
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
		int w = screenSize.x/4 + DimensionUtil.dip2px(context, 20);
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

	public GCPage getEditingPage() {
		return editingPage;
	}
	
	

}
