package com.kodak.flip;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.AppContext;
import com.aphidmobile.flip.FlipViewController;
import com.kodak.flip.ScaleGestureDetector.OnScaleGestureListener;
import com.kodak.kodak_kioskconnect_n2r.ArrangeActivity;
import com.kodak.kodak_kioskconnect_n2r.CartItem;
import com.kodak.kodak_kioskconnect_n2r.ImageInfo;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintMakerWebService;
import com.kodak.kodak_kioskconnect_n2r.QuickBookFlipperActivity;
import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.quickbook.database.ThumbnailProvider;
import com.kodak.utils.ImageUtil;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

public class CropView extends View implements OnScaleGestureListener {
	private static final String TAG = CropView.class.getSimpleName();
	
	private Matrix mMatrix = new Matrix();
	private Matrix mRotateMatrix = null;
	private float mScale = 1.0f;
	
	private ScaleGestureDetector mScaleGestureDetector;
	// Avoid allocations...
	private Rect mTempSrc = new Rect();
	private RectF mTempDst = new RectF();
	private float mRotateDegree = 0;
	private ProductInfo item;
	
	public int showWarning;
	public int fadeWarning;
	private Context context;
	public static final int REFRESH_POP_STATUS = 5;
	ThumbnailProvider mProvider;
	
	public Handler warningHandler;
	
	private boolean validEdit = true;
	
	private int radioX = 1, radioY = 1;
	private PhotoBookPage page;
	private ROI roi;
	private boolean needRotated = false;
	private int exifDegree = 0;
	private boolean isEditing = false;
	
	private ROI uploadRoi;
	private int uploadDegree;
	
	private boolean needDrawShadow = false;
	private SoftReference<Bitmap> shadow;
	private int shadowMode = -1;
	public static int LEFTPAGE_SHADOW = 0;
	public static int RIGHTPAGE_SHADOW = 1;
	public void setNeedDrawShadow(boolean need, int mode){
		this.needDrawShadow = need;
		this.shadowMode = mode;
	}
	
	public PhotoBookPage getPage() {
		return page;
	}

	public void setPage(PhotoBookPage page) {
		this.page = page;
	}
	public boolean isEditing() {
		return isEditing;
	}
	public void setEditing(boolean isEditing) {
		this.isEditing = isEditing;
	}

	FlipViewController controller;
	public FlipViewController getController() {
		return controller;
	}

	public void setController(FlipViewController controller) {
		this.controller = controller;
	}

	private ProgressBar mProgressBar;
	
	public ProgressBar getmProgressBar() {
		return mProgressBar;
	}

	public void setmProgressBar(ProgressBar mProgressBar) {
		this.mProgressBar = mProgressBar;
	}

	private void initCutoutView(Context context) {
		// Need to close the hardware accelerate, as Opengl only supports to
		// 2048*2048 texture display
		/*
		 * if (Build.VERSION.SDK_INT > 10) {
		 * this.setLayerType(LAYER_TYPE_SOFTWARE, null); // This API is // above
		 * v11 }
		 */
		this.context = context;
		mMatrix = new Matrix();
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
	}

	/** Width of view. */
	private int mViewWidth = 0;

	/** Height of view. */
	private int mViewHeight = 0;

	/** Width of image. */
	private float mImageWidth = 0.0f;

	/** Height of image. */
	private float mImageHeight = 0.0f;

	private float mAnoterX = 0.0f, mAnoterY = 0.0f;

	/** Indicates whether cutout is in edit. */
	public boolean mIsInEditMode = false;
	public boolean getEditMode() {
		return mIsInEditMode;
	}
	public boolean mIsEditingMode = false;

	public boolean ismIsEditingMode() {
		return mIsEditingMode;
	}

	public OnCropViewOnlyClickListener mClickListener;
	
	
	
	public OnCropViewOnlyClickListener getmClickListener() {
		return mClickListener;
	}

	public void setmClickListener(OnCropViewOnlyClickListener mClickListener) {
		this.mClickListener = mClickListener;
	}

	public void setmIsEditingMode(boolean mIsEditingMode) {
		this.mIsEditingMode = mIsEditingMode;
	}
	
	private final int INITWAIT = 1;
	private final int INITIMAGE = 2;
	public boolean setEditMode(boolean flag) {
		this.mIsInEditMode = flag;
		this.mIsEditingMode = flag;
		if(mIsInEditMode == true) {
			//MINI_KIND 
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			options.inSampleSize = 1;
			Bitmap mini = null;
			if(item.photoInfo.getPhotoSource().isFromPhone()){
				mini = PrintHelper.loadThumbnailImage(page.PhotoBookPageImages.get(0).photoLocalURI, MediaStore.Images.Thumbnails.MINI_KIND, options, context);
				ExifInterface exif = null;
				exifDegree = 0;
				try {
					exif = new ExifInterface(getFilePath(page.PhotoBookPageImages.get(0).photoLocalURI));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(exif!=null){
					if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90){
						exifDegree = 90;
						needRotated = true;
					} else if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_180) {
						exifDegree = 180;
						needRotated = false;
					} else if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270) {
						exifDegree = 270;
						needRotated = true;
					}
					Log.e(TAG, "exifDegree: " + exifDegree);
				}
			} else {
				mini = ImageUtil.getBitmapOfPhotoInfo(item.photoInfo, context);
				
				if(mini == null){
					return false;
				}
			}
			Matrix matrix = new Matrix();
			matrix.postRotate(-page.PhotoBookPageImages.get(0).angle + exifDegree);
			//Log.w(TAG, "angle: " + -page.PhotoBookPageImages.get(0).angle + ", exifDegree:" + exifDegree);
			if (mini != null) {
				mini = Bitmap.createBitmap(mini, 0, 0, mini.getWidth(), mini.getHeight(), matrix, true);
			}
			initEditParam(INITIMAGE);
			setImageBitmap(mini , INITIMAGE);
		} else {
			setEditing(false);
			warningHandler.sendEmptyMessage(REFRESH_POP_STATUS);
			finishEdit();
		}
		invalidate();
		return true;
	}
	
	private void finishEdit(){
		((QuickBookFlipperActivity)context).waitingHandler.sendEmptyMessage(QuickBookFlipperActivity.START_WAITING);
		mProvider = ThumbnailProvider.obtainInstance(context);
		mProvider.deleteMini(page.sPhotoBookPageID);
		saveRoi();
		uploadRoi = roi;
		netHandler.sendEmptyMessage(START_UPLOAD);
		
		this.page.setmOffsetX(mOffsetX);
		this.page.setmOffsetY(mOffsetY);
		this.page.setmRotateDegree(mRotateDegree);
		Log.i(TAG, "when store the information mLastValidOffsetX =  " + mLastValidOffsetX + " , mLastValidOffsetY = " + mLastValidOffsetY + " , mRotateDegree = " + mRotateDegree + "mAnoterX , mAnoterY = " 
				 + mAnoterX + " , " + mAnoterY);
		initEditParam(INITWAIT);
		mIsEditingMode = false;
		setImageBitmap(((QuickBookFlipperActivity)context).wait_image , INITWAIT);
	}
	
	private void initEditParam(int index) {
			mLastValidRotate = new Matrix();
			if (index == INITWAIT) {
				mLastValidOffsetX = 0;
				mLastValidOffsetY = 0;
				mLastValidScale = 1.0f;
				mOffsetX = 0f;
				mOffsetY = 0f;
			} else {
				mLastValidOffsetX = this.page.getmOffsetX();
				mLastValidOffsetY = this.page.getmOffsetY();
				mOffsetX = mLastValidOffsetX;
				mOffsetY = mLastValidOffsetY;
				mLastValidScale = this.page.getmScale();
				//Log.i(TAG, "initinitinitinitinitinitinitinitinit////  mLastValidScale,mLastValidOffsetX,mLastValidOffsetY = " + mLastValidScale + " , " + mLastValidOffsetX + " , " + mLastValidOffsetY
				//		+ "mAnoterX , mAnoterY = " + mAnoterX + " , " + mAnoterY);
			}
			
			if(page == null || page.PhotoBookPageImages == null || page.PhotoBookPageImages.get(0) == null){
				mRotateDegree = 0;
			} else {
				if (index == INITWAIT) {
					mRotateDegree = 0;//page.PhotoBookPageImages.get(0).angle;
				} 
				
				mRotateMatrix = new Matrix();
				mRotateMatrix.postRotate(mRotateDegree);
				Log.i(TAG, "mRotateDegree setted by mRotateMatrix is " + mRotateDegree);
				mMatrix.set(mRotateMatrix);
			}
			if (index == INITWAIT) {
				mScale = 1.0f;
			} else {
				mScale = mLastValidScale;
			}
	}

	/** Currently use Bitmap, if Drawable is better then consider Drawable. */
	private Bitmap mImage;

	private float mPreviousX, mPreviousY, mCurrentX, mCurrentY, mOffsetX, mOffsetY;

	private static final int DRAW_SOMETHING = 0X0001;

	private Handler mHanderDrawing = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DRAW_SOMETHING:
				Log.d(TAG, "yes, i must really draw something");
				invalidate();
				break;
			default:
				break;
			}
		}

	};

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mImage != null) {
			// add this mapping canvas for kindle fire mapping issue
			Canvas canvas4Mapping = new Canvas(); 
			canvas4Mapping.setMatrix(null);

			canvas.save();
			canvas.translate(mAnoterX, mAnoterY);
			canvas4Mapping.translate(mAnoterX, mAnoterY);
			canvas.translate(mOffsetX, mOffsetY);
			canvas4Mapping.translate(mOffsetX, mOffsetY);
			canvas.scale(mScale, mScale);
			canvas4Mapping.scale(mScale, mScale);
			canvas.concat(mMatrix);
			canvas4Mapping.concat(mMatrix);
			canvas.drawBitmap(mImage, mTempSrc, mTempDst, null);
			float[] pots = { mTempDst.left, mTempDst.top, mTempDst.right,
					mTempDst.top, mTempDst.right, mTempDst.bottom,
					mTempDst.left, mTempDst.bottom };
			canvas4Mapping.getMatrix().mapPoints(pots);
			
			if (!CutoutCoverageChecker.isCutoutFullyCovered(mViewWidth,
					mViewHeight, pots[0], pots[1], pots[2], pots[3], pots[4],
					pots[5], pots[6], pots[7])) {
				validEdit = false;
			} else {
				mLastValidOffsetX = mOffsetX;
				mLastValidOffsetY = mOffsetY;
				mLastValidScale = mScale;
				mLastValidRotate.set(mMatrix);
				//Log.i(TAG , "@@@@@@@@@@@@@@@@@@onDraw@@@@@@@@@@@@@@@@@  mOffsetX = " + mOffsetX + " , mOffsetY = " + mOffsetY 
				//		+ " mAnoterX = " + mAnoterX + " , mAnoterY = " + mAnoterY + " , mScale = " + mScale);
				validEdit = true;
			}
			if(mIsInEditMode && canEdit){
				drawFrame(canvas);
			}
			drawShadow(canvas);
			
			canvas.save();
			// this part to check whether show low res waring
			// TODO need to calculate exactly
			if(item != null){	
				double scaleX = item.roi.w / item.roi.ContainerW;
				double scaleY = item.roi.h / item.roi.ContainerH;
				if(PrintHelper.isLowResWarning(item, item.photoInfo, scaleX, scaleY) && warningHandler != null){
					warningHandler.sendEmptyMessage(showWarning);
				} else if(warningHandler != null){
					warningHandler.sendEmptyMessage(fadeWarning);
				}
				
			}
			canvas.restore();
		}
	}

	private float mLastValidOffsetX = 0.0f, mLastValidOffsetY = 0.0f;
	private float mLastValidScale = 1.0f;
	private Matrix mLastValidRotate = new Matrix();

	void onRecycle() // package level
	{
		// if (mImage != null && !mImage.isRecycled()) {
		// mImage.recycle();
		// System.gc();
		// Log.i("recycle", "onRecycleCropView");
		// }
		// System.gc();
		// mImage = null;
	}

	public void setImageBitmap(Bitmap bitmap , int index) {
		onRecycle();
		mImage = bitmap;
		
		if(mImage == null){
			return;
		}
		mImageWidth = mImage.getWidth();
		mImageHeight = mImage.getHeight();
		mTempSrc.left = 0;
		mTempSrc.top = 0;
		mTempSrc.bottom = (int) mImageHeight;
		mTempSrc.right = (int) mImageWidth;
		
		updateImageBound(index);
		invalidate();
		setEditing(true);
	}
	
	private Bitmap mirrorBitmap(Bitmap bitmap){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(temp);
		Matrix matrix = new Matrix();
		matrix.postScale(-1, 1);
		canvas.concat(matrix);
		Bitmap mirror = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		Rect src = new Rect(0,0,mirror.getWidth(), mirror.getHeight());
		Rect dst = new Rect(0, 0, width, height);
		canvas.drawBitmap(mirror, src, dst, null);
		return mirror;
	}
	
	public void recycleBitmap(){
		onDetachedFromWindow();
	}
	
	private void drawFrame(Canvas canvas){
		canvas.restore();
		canvas.save();
		Paint paint = new Paint();
		paint.setColor(Color.parseColor("#FBBA06"));
		paint.setStrokeWidth(8);
		canvas.drawLine(0, 0, this.getWidth() - 1, 0, paint);
		canvas.drawLine(0, 0, 0, this.getHeight() -1, paint);
		canvas.drawLine(this.getWidth() -1, 0, this.getWidth() -1, this.getHeight() -1, paint);
		canvas.drawLine(0, this.getHeight() -1, this.getWidth() - 1, this.getHeight() -1 , paint);
		canvas.restore();
		canvas.save();
	}
	
	private void drawShadow(Canvas canvas){
		canvas.restore();
		canvas.save();
		if(needDrawShadow && !mIsEditingMode){
			if(shadowMode == RIGHTPAGE_SHADOW){
				if(shadow == null || shadow.get()==null){
					shadow = new SoftReference<Bitmap>(PrintHelper.readBitMap(context, R.drawable.pageright_simplex));
				}
			} else if(shadowMode == LEFTPAGE_SHADOW){
				if(shadow == null || shadow.get()==null){
					shadow = new SoftReference<Bitmap>(mirrorBitmap(PrintHelper.readBitMap(context, R.drawable.pageright_simplex)));
				}
			}
			if(shadow!=null && shadow.get()!=null){
				Rect src = new Rect(0, 0, shadow.get().getWidth(), shadow.get().getHeight());
				RectF dst = new RectF(0, 0, this.getWidth(), this.getHeight());
				canvas.drawBitmap(shadow.get(), src, dst, null);
				canvas.save();
			}
		}
		canvas.restore();
		canvas.save();
	}
	
	public void setPrintSize(int w, int h){
		radioX = w;
		radioY = h;
	}

	private boolean canEdit = false;
	public boolean isCanEdit() {
		return canEdit;
	}

	
	public void setPhotoBookPage(PhotoBookPage photoBookPage){
		this.page = photoBookPage;
		if(page == null){
			canEdit = false;    
			setImageBitmap(((QuickBookFlipperActivity)context).btp , INITWAIT);
			return;
		}
		boolean isDuplexFiller = page.sPhotoBookPageName.equals(PhotoBookPage.DUPLEX_FILLER);
		
		if(!isDuplexFiller&&page!=null&&page.PhotoBookPageImages!=null&&page.PhotoBookPageImages.size()>0&&page.PhotoBookPageImages.get(0)!=null&&page.PhotoBookPageImages.get(0).photoLocalURI!=null){
			item = new ProductInfo(context);
			item.width = radioX + "";
			item.height = radioY + "";
			PhotoInfo photo = null;
			List<PhotoInfo> photos = AppContext.getApplication().getPhotobook().selectedImages;
			if(!TextUtils.isEmpty(page.PhotoBookPageImages.get(0).photoLocalURI)){
				for(PhotoInfo pi : photos){
					if(page.PhotoBookPageImages.get(0).photoLocalURI.equals(pi.getLocalUri())){
						photo = pi;
						break;
					}
				}
			} else {
				for(PhotoInfo pi : photos){
					if(page.PhotoBookPageImages.get(0).photoPath.equals(pi.getPhotoPath())){
						photo = pi;
						break;
					}
				}
			}
			item.photoInfo = photo;
			item.roi = page.PhotoBookPageImages.get(0).croproi;
		}
		
		if(page.bPhotoBookPageEditable) {
			canEdit = true;
		}
		mProvider = ThumbnailProvider.obtainInstance(context);
		Bitmap bitmap = null;
		boolean oome = false;
		try{
			bitmap = mProvider.getMini(page.sPhotoBookPageID);
			setImageBitmap(bitmap,INITIMAGE);
		} catch (OutOfMemoryError oom) {
			bitmap = null;
			oome = true;
			System.gc();
			oom.printStackTrace();
		}
		if(bitmap == null){
			bitmap = ((QuickBookFlipperActivity)context).wait_image;
			if(page.sPhotoBookPageURL != null && !oome && !page.isDownloading){
				page.isDownloading = true;
				((QuickBookFlipperActivity)context).qbPageDownloader.appendDownloadAtPlace(2,page.sPhotoBookPageURL, page.sPhotoBookPageID);
			}
			setImageBitmap(bitmap,INITWAIT);
		} else {
			//Log.e(TAG, "setPhotoBookPage.... can get the bitmap from cach. then load edit param....");
		}
//		setImageBitmap(bitmap);
	}
	
	public void rotateBitmap(){
		if(mImage != null){
			if(mRotateDegree == -1){
				mRotateDegree = -90;
			} else {
				mRotateDegree -= 90;
			}
			mRotateMatrix = new Matrix();
			mRotateMatrix.postRotate(-90);
			
			mImage = Bitmap.createBitmap(mImage, 0, 0, mImage.getWidth(), mImage.getHeight(), mRotateMatrix, true);
			
			mImageWidth = mImage.getWidth();
			mImageHeight = mImage.getHeight();
			
			mTempSrc = new Rect(0, 0, mImage.getWidth(), mImage.getHeight());				
			mTempDst = new RectF();
			
			updateImageBound(INITWAIT);
			saveRoi();
			invalidate();
			
		}
	}
	
	private void saveRoi() {
		if(roi == null){
			roi = new ROI();
		}
		uploadDegree = ((int)mRotateDegree)%360;
		double leftDes, topDes;
		if(item.photoInfo.getPhotoSource().isFromPhone()){
			BitmapFactory.Options options = getFileOptions(page.PhotoBookPageImages.get(0).photoLocalURI);
			if(Math.abs((uploadDegree-page.PhotoBookPageImages.get(0).angle)%180)==90){		
				roi.ContainerH = options.outWidth;
				roi.ContainerW = options.outHeight;
			} else {	
				roi.ContainerH = options.outHeight;
				roi.ContainerW = options.outWidth;
			}
		} else {
			if(Math.abs((uploadDegree-page.PhotoBookPageImages.get(0).angle)%180)==90){		
				roi.ContainerH = item.photoInfo.getWidth();
				roi.ContainerW = item.photoInfo.getHeight();
			} else {	
				roi.ContainerH = item.photoInfo.getHeight();
				roi.ContainerW = item.photoInfo.getWidth();
			}
			
		}
		if(needRotated){
			double tempInt = roi.ContainerH;
			roi.ContainerH = roi.ContainerW;
			roi.ContainerW = tempInt;
		}
		leftDes = Math.abs((((double)(mImageWidth*mScale)-(mViewWidth*ratio))/(double)mScale)/2-((double)mOffsetX/(double)mScale*ratio));
		topDes = Math.abs((((double)(mImageHeight*mScale)-(mViewHeight*ratio))/(double)mScale)/2-((double)mOffsetY/(double)mScale*ratio));
		
		roi.x = (double)leftDes/(double)mImageWidth * roi.ContainerW;
		roi.y = (double)topDes/(double)mImageHeight * roi.ContainerH;
		if(ratio == (double) mImageWidth / (double) mViewWidth){
			roi.w = (double)roi.ContainerW/(double)mScale;
			roi.h = roi.w * ((double)mViewHeight/(double)mViewWidth);
		} else {
			roi.h = (double)roi.ContainerH/(double)mScale;
			roi.w = roi.h * ((double)mViewWidth/(double)mViewHeight);
		}
		
		roi.x = formatDouble(roi.x);
		roi.y = formatDouble(roi.y);
		roi.w = formatDouble(roi.w);
		roi.h = formatDouble(roi.h);
		roi.ContainerH = formatDouble(roi.ContainerH);
		roi.ContainerW = formatDouble(roi.ContainerW);
		
		item.roi = roi;
		uploadRoi = roi;
		if(roi.w + roi.x > roi.ContainerW && roi.w + roi.x < roi.ContainerW+1){
			roi.w -= 1.000000;
			Log.e(TAG, "x+w>containerW, w-1");
		}
		if(roi.h + roi.y > roi.ContainerH && roi.h + roi.y < roi.ContainerH+1){
			roi.h -= 1.000000;
			Log.e(TAG, "y+h>containerH, h-1");
		}
		Log.e(TAG, "saveRoi x: " + roi.x + ",y: " + roi.y + ",w: " + roi.w + ",h: " + roi.h + ",cw: " + roi.ContainerW + ",ch: " + roi.ContainerH + ", ratio:" + ratio);
	}
	
	private double formatDouble(double d){
		if(d<0.000001){
			d = 0.000000;
		}
		String strd = d+"000000";
		return new Double(strd.substring(0, strd.lastIndexOf(".")) + "." + strd.substring(strd.lastIndexOf(".")+1,strd.lastIndexOf(".")+7));
	}
	
	public static final int START_DOWNLOAD = 0;
	public static final int FINISH_DOWNLOAD = 1;
	private static final int START_UPLOAD = 2;
	private static final int FINISH_UPLOAD = 3;
	public Handler netHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case START_DOWNLOAD:
				Log.w(TAG, "start download....");
				if (getmProgressBar() != null && !getmProgressBar().isShown()) {
					getmProgressBar().setVisibility(View.VISIBLE);
				}
				PrintHelper.lastFailedDegree = -1;
				PrintHelper.lastFailedROI = null;
				page.isDownloading = true;
				((QuickBookFlipperActivity)context).qbPageDownloader.requestDownload(page.sPhotoBookPageURL, page.sPhotoBookPageID);
				break;
			case FINISH_DOWNLOAD:
				break;
			case START_UPLOAD:
				new Uploader().execute("");
				break;
			case FINISH_UPLOAD:
				break;
			default:
				break;
			}
		}
		
	};
	
	public void reUpload(){
		finishEdit();
	}

	private class Uploader extends AsyncTask<String, Void, Integer> {
		private ROI needToUpload;
		private int degree = -1;
		@Override
		protected Integer doInBackground(String... params) {
			QuickBookFlipperActivity.pageUploading = true;
			needToUpload = uploadRoi;
			PrintMakerWebService service = new PrintMakerWebService(context, "");
			if(PrintHelper.lastFailedDegree!=-1){
				degree = PrintHelper.lastFailedDegree;
			} else {
				degree = uploadDegree;
			}
			
			String strImageInfo = service.getImageInfo(page.PhotoBookPageImages.get(0).photoID);
			Log.w(TAG, "edit PhotoId:" + page.PhotoBookPageImages.get(0).photoID);
			ImageInfo imageInfo = service.parseImageInfo(strImageInfo);
			if(imageInfo == null){
				return 0;
			}
			checkEditContentList();
			Log.w(TAG, "1.Degree[degree:" + degree + ", local:" + page.PhotoBookPageImages.get(0).angle + ", server:" + imageInfo.angle + "]");
			if(imageInfo.angle != Math.abs(page.PhotoBookPageImages.get(0).angle)){
				int diffDegree = imageInfo.angle - page.PhotoBookPageImages.get(0).angle;
				String result = service.pbRotateImageDegree(context, page.PhotoBookPageImages.get(0).photoID, (int)-diffDegree);
				if(result.equals("")){
					Log.w(TAG, "diffDegree:" + diffDegree + " -> failed!");
					return 0;
				}
				Log.w(TAG, "diffDegree:" + diffDegree + " -> succeed!");
			}
			if(degree != -1){
				String degreeResult = service.pbRotateImageDegree(context, page.PhotoBookPageImages.get(0).photoID, (int)-degree);
				if(degreeResult.equals("")){
					return 0;
				} else {
					page.PhotoBookPageImages.get(0).angle = Math.abs((page.PhotoBookPageImages.get(0).angle-degree))%360;
				}
			}
			
			if(PrintHelper.lastFailedROI != null){
				needToUpload = PrintHelper.lastFailedROI;
			}
			
			String result = service.pbSetImageCrop(context, page.PhotoBookPageImages.get(0).photoID, needToUpload);
			if(!result.equals("")){
				page.PhotoBookPageImages.get(0).croproi = needToUpload;
			} else {
				return 0;
			}
			
			service.layoutPageTask(page.sPhotoBookPageID);
			
			mProvider.deleteThumbnail(page.sPhotoBookPageID + ArrangeActivity.ARRANGE_THUMBNAIL_SUFFIX);
			Log.i(TAG, "upload url:" + page.sPhotoBookPageURL);
			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			((QuickBookFlipperActivity)context).waitingHandler.sendEmptyMessage(QuickBookFlipperActivity.STOP_WAITING);
			controller.postFlippedToView(controller.getCards().getFrontCards().getIndex());
			if (result == 0){
				PrintHelper.lastFailedROI = new ROI();
				PrintHelper.lastFailedROI.ContainerH = needToUpload.ContainerH;
				PrintHelper.lastFailedROI.ContainerW = needToUpload.ContainerW;
				PrintHelper.lastFailedROI.w = needToUpload.w;
				PrintHelper.lastFailedROI.h = needToUpload.h;
				PrintHelper.lastFailedROI.x = needToUpload.x;
				PrintHelper.lastFailedROI.y = needToUpload.y;
				
				PrintHelper.lastFailedDegree = new Integer(degree);
				((QuickBookFlipperActivity)context).waitingHandler.sendEmptyMessage(QuickBookFlipperActivity.UPLOAD_ROI_ERROR);
			} else {
				netHandler.sendEmptyMessage(START_DOWNLOAD);
			}
			QuickBookFlipperActivity.pageUploading = false;
			
		}
		
	}
	
	private void checkEditContentList(){
		if(PrintHelper.contentIdOfEditedImages == null){
			PrintHelper.contentIdOfEditedImages = new ArrayList<String>();
		}
		if(!PrintHelper.contentIdOfEditedImages.contains(page.PhotoBookPageImages.get(0).photoID)){
			PrintHelper.contentIdOfEditedImages.add(page.PhotoBookPageImages.get(0).photoID);
		}
	}
	
	public void setViewRatio() {
		
	}
	
	private double ratio = 0.0;
	private void updateImageBound(int index) {
		//Log.d(TAG, "updateImageBound....");
		
		if (mImage == null) {
			return;
		}
		if (mViewWidth == 0 || mViewHeight == 0) {
			return;
		}
	
		mAnoterX = (float) mViewWidth / 2;
		mAnoterY = (float) mViewHeight / 2;

		if (index == INITWAIT) {
			mOffsetX = (int) (imageEditPanFactorX * mAnoterX);
			mOffsetY = (int) (imageEditPanFactorY * mAnoterY);
		} else {
			mOffsetX = this.page.getmOffsetX();
			mOffsetY = this.page.getmOffsetY();
		}
		

		float ratioImage = (float) mImageWidth / (float) mImageHeight;
		float ratioView = (float) mViewWidth / (float) mViewHeight;

		float w, h;
		if (ratioImage > ratioView) { // fill the height to the view
			//Log.e(TAG, "fill the height to the view");
			h = (float) mViewHeight;
			w = (float) mViewHeight * ratioImage;

			mTempDst.top = (float) (0 - mAnoterY);
			mTempDst.bottom = (float) (h - mAnoterY);
			mTempDst.left = (float) (0 - (w - mViewWidth) / 2 - mAnoterX);
			mTempDst.right = (float) (mViewWidth + (w - mViewWidth) / 2 - mAnoterX);
			
			ratio = (double)mImageHeight / (double)mViewHeight;
		} else { // fill the width
			//Log.e(TAG, "fill the width to the view");
			w = (float) mViewWidth;
			h = (float) mViewWidth / ratioImage;

			mTempDst.left = (float) (0 - mAnoterX);
			mTempDst.right = (float) (w - mAnoterX);
			mTempDst.top = (float) (0 - (h - mViewHeight) / 2 - mAnoterY);
			mTempDst.bottom = (float) (mViewHeight + (h - mViewHeight) / 2 - mAnoterY);
			
			ratio = (double) mImageWidth / (double) mViewWidth;
		}
	}

	public boolean isImageNull() {
		if (mImage == null)
			return true;
		else
			return false;
	}

	public void touchBegin(float posX, float posY) {
		Log.d(TAG, "touchBegin");
		mPreviousX = posX;
		mPreviousY = posY;
		invalidate();
	}

	public void touchEnd(float posX, float posY) {
		//Log.d(TAG, "touchEnd");
		mPreviousX = posX;
		mPreviousY = posY;
		if(!validEdit){
			mOffsetX = mLastValidOffsetX;
			mOffsetY = mLastValidOffsetY;
			mScale = mLastValidScale;
			mMatrix.set(mLastValidRotate);
			mHanderDrawing.sendEmptyMessageDelayed(DRAW_SOMETHING, 100);
		}
		saveRoi();
		if(item!=null){
			item.roi = roi;
		}
		//Log.i(TAG, "touchEnd eeeeeeeeeeeeeeeeeeeeeeeee mOffsetX , mOffsetY = " + mOffsetX + " , " + mOffsetY);
		invalidate();
	}

	public void touchMoved(float posX, float posY) {
		//Log.d(TAG, "touchMoved");
		if (true && mIsInEditMode) {
			mOffsetX += (posX - mPreviousX);
			mOffsetY += (posY - mPreviousY);
			mPreviousX = posX;
			mPreviousY = posY;
			
			// if leftDes equal 0, do not move
			if((double)mOffsetX > (((double)(mImageWidth*mScale)-(mViewWidth*ratio))/(double)mScale)/2*(double)mScale/ratio){
				mOffsetX = (float) ((((double)(mImageWidth*mScale)-(mViewWidth*ratio))/(double)mScale)/2*(double)mScale/ratio);
			}
			// if topDes equal 0, do not move
			if(((double)mOffsetY > (((double)(mImageHeight*mScale)-(mViewHeight*ratio))/(double)mScale)/2*(double)mScale/ratio)){
				mOffsetY = (float) ((((double)(mImageHeight*mScale)-(mViewHeight*ratio))/(double)mScale)/2*(double)mScale/ratio);
			}
			if((((double)(mImageWidth*mScale)-(mViewWidth*ratio))/(double)mScale)/2-((double)mOffsetX/(double)mScale*ratio)>mImageWidth-mViewWidth*ratio/mScale){
				mOffsetX=(float) ((((((double)(mImageWidth*mScale)-(mViewWidth*ratio))/(double)mScale)/2)-(mImageWidth-mViewWidth*ratio/mScale))/ratio*(double)mScale);
			}
			if((((double)(mImageHeight*mScale)-(mViewHeight*ratio))/(double)mScale)/2-((double)mOffsetY/(double)mScale*ratio)>mImageHeight-mViewHeight*ratio/mScale){
				mOffsetY=(float) ((((((double)(mImageHeight*mScale)-(mViewHeight*ratio))/(double)mScale)/2)-(mImageHeight-mViewHeight*ratio/mScale))/ratio*(double)mScale);
			}
		}
	}
	
	private BitmapFactory.Options getFileOptions(String strUri){
		BitmapFactory.Options options = new BitmapFactory.Options();
		String filePath = getFilePath(strUri);
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		return options;
	}
	
	private String getFilePath(String strUri){
		Uri uri = Uri.parse(strUri);
		ContentResolver cr = context.getContentResolver();
		String[] poj = {MediaStore.Images.Media.DATA};
		Cursor cursor = cr.query(uri, poj, null, null, null);
		String filePath = "";
		try{
			cursor.moveToFirst();
			filePath = cursor.getString(0);
		} catch(Exception e){
			e.printStackTrace();
			if(cursor != null){
				cursor.close();
			}
		}
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
		return filePath;
	}
	
	@Override
	public boolean performClick() {
		return super.performClick();
	}
	
	private long downTime = 0;
	private float downPostionX = 0f;
	private float downPostionY = 0f;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mIsInEditMode && !ismIsEditingMode()){
			getParent().requestDisallowInterceptTouchEvent(true);
		} else {
			controller.setCurrentView(this);
			if (ismIsEditingMode()) {
				getParent().requestDisallowInterceptTouchEvent(true);
			} else {
				return false;
			}
		}

		mCurrentX = (float) (event.getX());
		mCurrentY = (float) (event.getY());
		
		if (mImage == null) {
			return true;
		}
		if (mIsInEditMode) {
			mScaleGestureDetector.onTouchEvent(event);
			switch (event.getAction()) {
			case MotionEvent.ACTION_POINTER_1_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
				int id = (((event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT) == 0) ? 1
						: 0;
				mPreviousX = event.getX(id);
				mPreviousY = event.getY(id);
				break;
			case MotionEvent.ACTION_UP:
				//Log.i(TAG, "onTouchEvent up");
				long time = new Date().getTime() - downTime;
				float disX = event.getX() - downPostionX;
				float disY = event.getY() - downPostionY;
				//Log.i(TAG, "time/////// = " + time + ", ////disx//// = " + disX + "////y//// = " + disY );
				if(time<200){
//					performClick();
					Log.i(TAG, "performClick");
					getmClickListener().onOnlyClick();
				}
				touchEnd(mCurrentX, mCurrentY);
				break;
			case MotionEvent.ACTION_DOWN:
				downTime = new Date().getTime();
				downPostionX = event.getX();
				touchBegin(mCurrentX, mCurrentY);
				break;
			case MotionEvent.ACTION_CANCEL:
				touchEnd(mCurrentX, mCurrentY);
				break;
			case MotionEvent.ACTION_MOVE:
				touchMoved(mCurrentX, mCurrentY);
				break;
			default:
				break;
			}
			postInvalidate();
		}
		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mViewWidth = w;
		mViewHeight = h;
		updateImageBound(INITWAIT);
	}

	public CropView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initCutoutView(context);
	}

	public CropView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCutoutView(context);
	}

	public CropView(Context context) {
		super(context);
		initCutoutView(context);
	}

	private float imageEditPanFactorX = 0.0f;
	private float imageEditPanFactorY = 0.0f;

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		if (!mIsInEditMode)
			return true;

		float scale = detector.getScaleFactor();
		float scaleTemp = mScale;
		scaleTemp *= scale;
		if (scaleTemp > 5.0f || scaleTemp < 1.0f) {
			return true; // Ignore the request while scale factor is >5.0 or
							// <1.0
		}
		mScale = scaleTemp;
		this.page.setmScale(scaleTemp);
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		//Log.d(TAG, "onScaleBegin");
		if (!mIsInEditMode)
			return true;
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		//Log.d(TAG, "onScaleEnd");
		if (!mIsInEditMode)
			return;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if(mImage!=null){
			mImage.recycle();
			mImage = null;
		}
		if(shadow!=null && shadow.get()!=null){
			shadow.get().recycle();
			shadow.clear();
		}
		if(controller!=null){
			setController(null);
		}
		System.gc();
	}

	public ProductInfo getItem() {
		return item;
	}
	
	
	
}
