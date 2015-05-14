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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.core.util.FileDownloader;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfos;
import com.kodak.rss.tablet.thread.PhotoBookEditTask;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.EditImageView.OnEditlistener;
import com.kodak.rss.tablet.view.EditImageView.OnRotateListener;
import com.kodak.rss.tablet.view.ProductEditPopView.OnEditItemClickListener;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class PhotoBookEditLayer extends RelativeLayout{
	private static final String TAG = "PhotoBookEditLayer";
	private EditImageView ivEdit;
	private PhotoBookEditPopView pop;
	private ProgressBar pbEditLayer;
	private ProgressBar pbEditPage;
	private ProgressBar pbDownloadLayer;
	private WeakReference<PhotoBookMainPageView> pageViewRef;
	private Point screenSize;
	private OnEditItemClickListener onEditItemClickListener;
	private OnDoneClickListener onDoneClickListener;
	private boolean editPage;//edit page or layer(image)
	private PhotoBooksProductActivity activity;
	
	private Point pageViewLocation;
	private int pageViewWidth;
	private int pageViewHeight;
	private RectF layerRect;
	
	public PhotoBookEditLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public PhotoBookEditLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PhotoBookEditLayer(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		inflate(context, R.layout.photobook_edit_layer, this);
		ivEdit = (EditImageView) findViewById(R.id.iv_edit);
		pop = (PhotoBookEditPopView) findViewById(R.id.edit_pop);
		pbEditLayer = (ProgressBar) findViewById(R.id.edit_progress_for_layer);
		pbEditPage = (ProgressBar) findViewById(R.id.edit_progress_for_page);
		pbDownloadLayer = (ProgressBar) findViewById(R.id.download_progress_for_layer);
		
		screenSize = new Point();
		pageViewRef = new WeakReference<PhotoBookMainPageView>(null);
		activity = (PhotoBooksProductActivity) context;
		activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
		
	}
	
	public void setOnPopEditItemClickListener(OnEditItemClickListener<PhotoBookEditPopView, PhotobookPage, Layer> onEditItemClickListener){
		this.onEditItemClickListener = onEditItemClickListener;
	}
	
	public void setOnDoneClickListener(OnDoneClickListener onDoneClickListener){
		this.onDoneClickListener = onDoneClickListener;
	}
	
	public void selectCurrentPage(){
		ivEdit.setVisibility(View.INVISIBLE);
		PhotoBookMainPageView pageView = pageViewRef.get();
		if(pageView!=null){
			pageView.setSelected(true);
			showPageEditPop(pageView, pageView.getPage());
		}
	}
	
	public void showPageEditPop(final PhotoBookMainPageView pageView, PhotobookPage page){
		dismissEditProgress();
		this.pageViewRef = new WeakReference<PhotoBookMainPageView>(pageView);
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
					onDoneClickListener.OnDoneClick(PhotoBookEditLayer.this,pageView,ivEdit);
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
					onDoneClickListener.OnDoneClick(PhotoBookEditLayer.this,pageView,ivEdit);
				}else{
					dismiss();
				}
			}
		});
		
		pop.setVisibility(View.VISIBLE);
		
	}
	
	public void showEditImageAndPop(final PhotoBookMainPageView pageView, Layer layer, RectF layerRect){
		dismissEditProgress();
		setVisibility(VISIBLE);
		this.pageViewRef = new WeakReference<PhotoBookMainPageView>(pageView);
		this.layerRect = layerRect;
		
		editPage = false;
		showEditImage(pageView, layer, layerRect);
		showEditImagePop(pageView, layer, layerRect);
		
		pop.setOnDoneClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onDoneClickListener != null){
					onDoneClickListener.OnDoneClick(PhotoBookEditLayer.this,pageView,ivEdit);
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
					onDoneClickListener.OnDoneClick(PhotoBookEditLayer.this,pageView,ivEdit);
				}else{
					dismiss();
				}
			}
		});
	}
	
	public void showEditImageAndPop(final PhotoBookMainPageView pageView,final Layer layer){
		//maybe pageview havn't get size, so run delayed
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				showEditImageAndPop(pageView, layer, pageView.getLayerRect(layer));
			}
		}, 500);
	}
	
	public void showPageEditProgress(final PhotobookPage page){
			RelativeLayout.LayoutParams pbParams = (LayoutParams) pbEditPage.getLayoutParams();
			int[] rootLocation = new int[2];
			getLocationOnScreen(rootLocation);
			//If you try to use pageViewRef.get() to get the pageView, then to get its location and size, it will cause some problem
			//So it's better to init these values at first
			pbParams.leftMargin = (int) (pageViewLocation.x - rootLocation[0] + pageViewWidth/2 - pbEditPage.getWidth()/2);
			pbParams.topMargin = (int) (pageViewLocation.y - rootLocation[1] + pageViewHeight/2 - pbEditPage.getHeight()/2);
			
			pbEditPage.setVisibility(View.VISIBLE);
			pbEditPage.requestLayout();
	}
	
	public void dismissEditProgress(){
		pbEditLayer.setVisibility(View.INVISIBLE);
		pbEditPage.setVisibility(View.INVISIBLE);
		pbDownloadLayer.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * @return true: edit page.  false: edit image
	 */
	public boolean isEditPage(){
		return editPage;
	}
	
	public void showLayerEditProgress(final PhotobookPage page,final Layer layer){
		RelativeLayout.LayoutParams pbParams = (LayoutParams) pbEditLayer.getLayoutParams();
		
		if(layerRect != null){
			pbParams.leftMargin = (int) (layerRect.width()/2 - pbEditLayer.getWidth()/2);
			pbParams.topMargin = (int) (layerRect.height()/2 - pbEditLayer.getHeight()/2.0+ivEdit.getRotateIconHeight()/2.0);
			pbEditLayer.requestLayout();
			pbEditLayer.setVisibility(View.VISIBLE);
		}
	}
	
	private void showLayerDownloadProgress(final PhotobookPage page, final Layer layer){
		RelativeLayout.LayoutParams pbParams = (LayoutParams) pbDownloadLayer.getLayoutParams();
		
		if(layerRect != null){
			pbParams.leftMargin = (int) (layerRect.width()/2 - pbDownloadLayer.getWidth()/2);
			pbParams.topMargin = (int) (layerRect.height()/2 - pbDownloadLayer.getHeight()/2.0+ivEdit.getRotateIconHeight()/2.0);
			pbDownloadLayer.requestLayout();
			pbDownloadLayer.setVisibility(View.VISIBLE);
		}
	}
	
	public PhotoBookMainPageView getPageView(){
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
		ivEdit.setVisibility(View.INVISIBLE);
		
		PhotoBookMainPageView pageView = pageViewRef.get();
		if(pageView != null){
			pageViewRef.get().setSelected(false);
		}
	}
	
	private RectF photobookRect=null;
	private int bottomPanelY;
	private boolean scaledDragLayer = false;
	private PhotobookPage layerPageDragged = null;
	private boolean isDragStarted = false;;
	public boolean onDragLayerEvent(MotionEvent event,PhotoBookMainPageView pageView, Layer layer){
		if(layerPageDragged == null){
			//Because in adapter.getView(), it always reuse and reassign photobookpage, so we need to record page at first
			layerPageDragged = pageView.getPage();
		}
		
		if(PhotobookPage.TYPE_COVER.equals(layerPageDragged.pageType)){
			return true;
		}
		
		float x = event.getRawX();
		float y = event.getRawY();
		switch(event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			if(!isDragStarted){
				isDragStarted = true;
				activity.pbLayout.getFlipViewController().setFlipByTouchEnabled(false);
				dismiss();
				setVisibility(VISIBLE);
				showEditImageForDrag(pageView, layer, pageView.getLayerRect(layer));
				ivEdit.setEditable(false);
				
				int[] location = new int[2];
				activity.pbLayout.getLocationOnScreen(location);
				photobookRect = new RectF(location[0], location[1], location[0]+activity.pbLayout.getWidth(), location[1]+activity.pbLayout.getHeight());
				activity.dragPhotoGrid.getLocationOnScreen(location);
				bottomPanelY = location[1];
				scaledDragLayer = false;
			}
			
			int w = ivEdit.getWidth(), h = ivEdit.getHeight();
			//when move to left/right side of photobook, auto flip
			if(!activity.pbLayout.getFlipViewController().isInFlipAnimation() && y > photobookRect.top && y < photobookRect.bottom){
				if(x - w/3 < photobookRect.left && activity.pbLayout.getCurrentPosition()>1){
					activity.pbLayout.getFlipViewController().flipToPrevious();
				}else if(x + w/3 > photobookRect.right && activity.pbLayout.getCurrentPosition() < activity.mainAdapter.getCount() - 2){
					activity.pbLayout.getFlipViewController().flipToNext();
				}
			}
			
			//when move in the panel, scale out
			if(!scaledDragLayer && y > bottomPanelY){
				float scaleX = activity.pagesAdapter.pageWidth/1.2f/w;
				float scaleY = activity.pagesAdapter.pageHeight/0.7f/h;
				float scale = scaleX > scaleY ? scaleY : scaleX;
				Animation anim = new ScaleAnimation(1, scale, 1, scale,w/2,h/2);
				anim.setDuration(100);
				anim.setFillAfter(true);
				ivEdit.startAnimation(anim);
				scaledDragLayer = true;
			}
			
			//move imageView, let it follow finger
			ivEdit.layout((int)x-w/2, (int)y-h/2, (int)x+w/2, (int)y+h/2);
			break;
		case MotionEvent.ACTION_UP:
			activity.pbLayout.getFlipViewController().setFlipByTouchEnabled(true);
			dismiss();
			ivEdit.clearAnimation();
			//if drag layer in another page, move this content to this page
			PhotobookPage desPage = null;
			if(!isDragStarted){
				//make sure drag has been started, or do nothing here
			}else if(photobookRect.contains(x,y)){
				PhotobookPage[] pages = PhotoBookProductUtil.getCurrentPages(activity.pbLayout.getCurrentPosition());
				if(x < photobookRect.left + photobookRect.width()/2 ){ // left page
					desPage = pages[0];
				}else{//right page
					desPage = pages[1];
				}
			}else if(y > bottomPanelY){//in bottom panel
				//get the adapter potision
				int[] location = new int[2];
				activity.dragPhotoGrid.getLocationOnScreen(location);
				int position = activity.dragPhotoGrid.pointToPosition((int)(x-location[0]), (int)(y-location[1]));
				
				if(position < activity.pagesAdapter.itemPages.size() && position >= 0){
					desPage = activity.pagesAdapter.itemPages.get(position);
				}
			}
			
			if (desPage != null) {
				if (Layer.TYPE_TEXT_BLOCK.equals(layer.type) && PhotoBookProductUtil.isTitlePage(desPage)) {
					//add check for RSSMOBILEPDC-1577 [Tablet] - PB: It should not allow to move Page Text to Title Page
					new InfoDialog.Builder(getContext())
					.setMessage(R.string.cannot_add_to_title_prompt)
					.setPositiveButton(R.string.d_ok, null)
					.create()
					.show();
				} else if (PhotoBookProductUtil.getPhotobookPageEditable(desPage) && !desPage.id.equals(layerPageDragged.id) && !PhotobookPage.TYPE_COVER.equals(desPage.pageType)) {
				    // add check for the desPage is have more images
					if (layer != null && Layer.TYPE_IMAGE.equals(layer.type) && PhotoBookProductUtil.getIsNotNullNum(desPage.layers) >= Integer.valueOf(desPage.maxNumberOfImages)){								
						new InfoDialog.Builder(getContext()).setMessage(R.string.page_enough_prompt)			
						.setNegativeButton(R.string.d_ok, null)
						.create()
						.show();			
					}else {
						new PhotoBookEditTask(getContext(), PhotoBookEditTask.MOVE_CONTENT, activity.editTaskHandler, layerPageDragged, layer, desPage.id).start();
					}
				}else if(PhotoBookProductUtil.getPhotobookPageEditable(desPage) && desPage.id.equals(layerPageDragged.id) ){					
					Layer changeLayer = pageView.pointToLayer(pageView,desPage,x,y);
					if (changeLayer != null && changeLayer.contentId != null && !"".equals(changeLayer.contentId) && !changeLayer.contentId.equals(layer.contentId)) {
						new PhotoBookEditTask(getContext(), PhotoBookEditTask.SWAP_CONTENT, activity.editTaskHandler, layerPageDragged, layer, changeLayer.contentId).start();
					}
				}
			}
			
			scaledDragLayer = false;
			photobookRect = null;
			bottomPanelY = 0;
			layerPageDragged = null;
			isDragStarted = false;
			break;
		}
		return true;
	}
	
	public static interface OnDoneClickListener{
		/**
		 * pageView or ivEdit maybe null, it depends on whether the pop shown is for page or layer
		 * @param editLayer
		 * @param pageView
		 * @param ivEdit
		 */
		void OnDoneClick(PhotoBookEditLayer editLayer, PhotoBookMainPageView pageView, EditImageView ivEdit);
	}
	
	private void showEditImage(final PhotoBookMainPageView pageView, final Layer layer,
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
		
		if(!info.isUseServerImage){//local image
			String imagePath = PhotoBookProductUtil.getLocalImagePathByContentId(layer.contentId);
			Log.i(TAG, "image path:"+imagePath);
			if(imagePath == null || !new File(imagePath).exists()){
				Log.i(TAG, "file not exist");
				info.isUseServerImage = true;
			}else{
				showEditImage(pageView, layer, layerRect, decodeScaleBitmap(imagePath, (int)layerRect.width()*2, (int)layerRect.height()*2));
				shown = true;
			}
		}
		
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
		PhotoBookMainPageView mPageView;
		RectF mLayerRect;
		
		/**
		 * @param pageView
		 * @param layer
		 * @param layerRect
		 * @param layerInfo
		 * @param lowRes Set it true, it will download super high res image after download low res image 
		 */
		GetLayerImageFromServerTask(PhotoBookMainPageView pageView, Layer layer,
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
	
	
	private void showEditImageForDrag(final PhotoBookMainPageView pageView, final Layer layer,
			final RectF layerRect){
		showEditImage(pageView, layer, layerRect);
	}
	
	private void updateEditImageBitmap(PhotobookPage page, Layer layer, Bitmap bitmap){
		ivEdit.setEditBitmap(bitmap);
		RelativeLayout.LayoutParams params = (LayoutParams) ivEdit.getLayoutParams();
		ivEdit.setBasicParams(page, layer, params.width, params.height);
	}
	
	private void showEditImage(PhotoBookMainPageView pageView, Layer layer,
			RectF layerRect,Bitmap bitmap){
		ivEdit.setEditBitmap(bitmap);
		
		setImageEditLayout(pageView, layer, layerRect);
		RelativeLayout.LayoutParams params = (LayoutParams) ivEdit.getLayoutParams();
		ivEdit.setBasicParams(pageView.getPage(), layer, params.width, params.height);
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
	
	private void showEditImagePop(PhotoBookMainPageView pageView, Layer layer,
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
	
	private void setImageEditLayout(PhotoBookMainPageView pageView, Layer layer,
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
	
	private void setImageEditPopLayout(PhotoBookMainPageView pageView, Layer layer,
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
	
	private void setPageEditLayout(PhotoBookMainPageView pageView){
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
	
	private Point getPageViewLocationInWindow(PhotoBookMainPageView pageView){
//		int[] location = new int[2];
//		pageView.getLocationInWindow(location);
//		
//		int x = location[0];
//		int y = location[1];
		
		//Sometimes can not get correct location by pageView.getLocationInWindow.
		//Maybe because the pageview is refresh when photobook adapter notifyDataSetChaned.
		//So here we use photobook to calculate the location
		
		int[] locPhotobook = new int[2];
		View v = activity.pbLayout.getFlipViewController();
		v.getLocationInWindow(locPhotobook);
		int x = pageView.isLeft() ? locPhotobook[0] : locPhotobook[0] + v.getWidth()/2;
		int y = locPhotobook[1];
		
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
