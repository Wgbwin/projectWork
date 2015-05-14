package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import java.io.IOException;
import java.util.Date;

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
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.kodak.flip.ScaleGestureDetector;
import com.kodak.flip.ScaleGestureDetector.OnScaleGestureListener;
import com.kodak.kodak_kioskconnect_n2r.ROI;

public class EditImage extends View implements OnScaleGestureListener{
	private final String TAG = EditImage.class.getSimpleName();

	private Bitmap mImage = null;
	private int temp = 0;
	private ScaleGestureDetector mScaleGestureDetector;
	private float mPreviousX, mPreviousY, mCurrentX, mCurrentY, mOffsetX,
			mOffsetY;
	private long downTime = 0;
	private float downPostionX = 0f;
	private float downPostionY = 0f;
	private float mLastValidOffsetX = 0.0f, mLastValidOffsetY = 0.0f;
	private float mLastValidScale = 1.0f;
	private Matrix mLastValidRotate = new Matrix();
	private float mScale = 1.0f;
	private Matrix mMatrix = new Matrix();
	private double ratio = 0.0;
	private int mViewWidth = 0;
	private int mViewHeight = 0;
	private float mImageWidth = 0.0f;
	private float mImageHeight = 0.0f;
	public boolean mIsInEditMode = false;
	private float mAnoterX = 0.0f, mAnoterY = 0.0f;
	private Context context;
	private RectF mTempDst = new RectF();
	public boolean mIsEditingMode = false;
	private GreetingCardPageLayer copyLayer;
	private int paramWidth;
	private int paramHeight;
	private Rect mTempSrc = new Rect();
	private int mRotateDegree = -1;
	private Matrix mRotateMatrix = null;
	private final int INITWAIT = 1;
	private final int INITIMAGE = 2;
	private float imageEditPanFactorX = 0.0f;
	private float imageEditPanFactorY = 0.0f;
	private ROI roi;
	private boolean needRotated = false;
	private ZoomableRelativeLayout mZoomableLayout;
	public boolean getEditMode() {
		return mIsInEditMode;
	}
	
	public boolean ismIsEditingMode() {
		return mIsEditingMode;
	}

	public GreetingCardPageLayer getCopyLayer() {
		return copyLayer;
	}

	public void setCopyLayer(GreetingCardPageLayer copyLayer) {
		this.copyLayer = copyLayer;
	}

	public void setmIsEditingMode(boolean mIsEditingMode) {
		this.mIsEditingMode = mIsEditingMode;
	}
	
	public ZoomableRelativeLayout getmZoomableLayout() {
		return mZoomableLayout;
	}

	public void setmZoomableLayout(ZoomableRelativeLayout mZoomableLayout) {
		this.mZoomableLayout = mZoomableLayout;
	}

	public ROI getRoi() {
		return roi;
	}

	public void setRoi(ROI roi) {
		this.roi = roi;
	}

	public int getmRotateDegree() {
		return mRotateDegree;
	}

	public void setmRotateDegree(int mRotateDegree) {
		this.mRotateDegree = mRotateDegree;
	}

	public EditImage(Context context, Bitmap mBitmap, GreetingCardPageLayer copyLayer, int paramWidth,
			int paramHeight) {
		super(context);
		initCutoutView(context);
		ExifInterface exif = null;
		int exifDegree = 0;
		if (copyLayer.getPhotoInfo().getPhotoSource().isFromPhone()){
			try {
				exif = new ExifInterface(copyLayer.getPhotoInfo().getPhotoPath());
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
		}
		
		Matrix matrix = new Matrix();
		matrix.postRotate(-copyLayer.degree+exifDegree);
		this.mImage = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
		mImageWidth = mImage.getWidth();
		mImageHeight = mImage.getHeight();
		mTempSrc.left = 0;
		mTempSrc.top = 0;
		mTempSrc.bottom = (int) mImageHeight;
		mTempSrc.right = (int) mImageWidth;
		this.copyLayer = copyLayer;
		this.mOffsetX = copyLayer.offsetX;
		this.mOffsetY = copyLayer.offsetY;
		this.mScale = copyLayer.scale;
		this.mViewWidth = paramWidth;
		this.mViewHeight = paramHeight;
		updateImageBound(0);
		saveRoi();
		invalidate();
	}
	 
//	public EditImage(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		initCutoutView(context);
//	}
//	
//	public EditImage(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		initCutoutView(context);
//	}

	@Override
	protected void onDraw(Canvas canvas) { //TODO
		super.onDraw(canvas);
		// add this mapping canvas for kindle fire mapping issue
		Canvas canvas4Mapping = new Canvas();
		canvas4Mapping.setMatrix(null);

		canvas.save();
		canvas.translate(mAnoterX, mAnoterY);
		canvas4Mapping.translate(mAnoterX, mAnoterY);
		canvas.translate(mOffsetX, mOffsetY);
		canvas4Mapping.translate(mOffsetX, mOffsetY);
		canvas.scale(mScale, mScale,paramWidth / 2, paramHeight / 2);
		canvas4Mapping.scale(mScale, mScale,paramWidth / 2, paramHeight / 2);
		canvas.concat(mMatrix);
		canvas4Mapping.concat(mMatrix);
		mLastValidOffsetX = mOffsetX;
		mLastValidOffsetY = mOffsetY;
		mLastValidScale = mScale;
		mLastValidRotate.set(mMatrix);
		
		float[] pots = { mTempDst.left, mTempDst.top, mTempDst.right,
				mTempDst.top, mTempDst.right, mTempDst.bottom,
				mTempDst.left, mTempDst.bottom };
		canvas4Mapping.getMatrix().mapPoints(pots);
		
		canvas.drawBitmap(mImage, mTempSrc, mTempDst, null);
		drawFrame(canvas);
		canvas.save();
		canvas.restore();
	}
	
	private void saveRoi() {
		if(roi == null){
			roi = new ROI();
		}
		double leftDes, topDes;
		BitmapFactory.Options options = getFileOptions(copyLayer.getPhotoInfo().getPhotoPath());
		int degree = getmRotateDegree()==-1?0:getmRotateDegree();
		if((degree-copyLayer.degree)%180==-90){		
			roi.ContainerH = options.outWidth;
			roi.ContainerW = options.outHeight;
		} else {	
			roi.ContainerH = options.outHeight;
			roi.ContainerW = options.outWidth;
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
		
		
		if(roi.w + roi.x > roi.ContainerW && roi.w + roi.x < roi.ContainerW+1){
			roi.w -= 1.000000;
			Log.e(TAG, "x+w>containerW, w-1");
		}
		if(roi.h + roi.y > roi.ContainerH && roi.h + roi.y < roi.ContainerH+1){
			roi.h -= 1.000000;
			Log.e(TAG, "y+h>containerH, h-1");
		}
		
		// store the edit information into layer
		copyLayer.offsetX = mOffsetX;
		copyLayer.offsetY = mOffsetY;
		copyLayer.scale = mScale;
		
		Log.e(TAG, "saveRoi x: " + roi.x + ",y: " + roi.y + ",w: " + roi.w + ",h: " + roi.h + ",cw: " + roi.ContainerW + ",ch: " + roi.ContainerH + ", ratio:" + ratio);
	}
	
	private BitmapFactory.Options getFileOptions(String filePath){
		BitmapFactory.Options options = new BitmapFactory.Options();
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
	
	private double formatDouble(double d){
		if(d<0.000001){
			d = 0.000000;
		}
		String strd = d+"000000";
		return new Double(strd.substring(0, strd.lastIndexOf(".")) + "." + strd.substring(strd.lastIndexOf(".")+1,strd.lastIndexOf(".")+7));
	}
	
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
			mOffsetX = 0;
			mOffsetY = 0;
			updateImageBound(INITWAIT);
			saveRoi();
			invalidate();
			
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) { //TODO
		mScaleGestureDetector.onTouchEvent(event);

		mCurrentX = (float) (event.getX());
		mCurrentY = (float) (event.getY());
		switch (event.getAction()) {
		case MotionEvent.ACTION_POINTER_1_UP:
		case MotionEvent.ACTION_POINTER_2_UP:
			int id = (((event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT) == 0) ? 1
					: 0;
			mPreviousX = event.getX(id);
			mPreviousY = event.getY(id);
			break;
		case MotionEvent.ACTION_UP:
			// Log.i(TAG, "onTouchEvent up");
			long time = new Date().getTime() - downTime;
			float disX = event.getX() - downPostionX;
			float disY = event.getY() - downPostionY;
			if (time < 200) {
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
		return true;
	}
	
	public void touchBegin(float posX, float posY) {
		getmZoomableLayout().setOverWithSend(true);
		mPreviousX = posX;
		mPreviousY = posY;
		invalidate();
	}
 
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mViewWidth = w;
		mViewHeight = h;
		updateImageBound(INITWAIT);
	}
	
	public void touchEnd(float posX, float posY) {
		// Log.d(TAG, "touchEnd");
		mPreviousX = posX;
		mPreviousY = posY;
		mOffsetX = mLastValidOffsetX;
		mOffsetY = mLastValidOffsetY;
		mScale = mLastValidScale;
		mMatrix.set(mLastValidRotate);
		saveRoi();
//		ratio = (double) mImageHeight / (double) mViewHeight;
		invalidate();
	}

	public void touchMoved(float posX, float posY) {//TODO
		// Log.d(TAG, "touchMoved");
		
		mOffsetX += (posX - mPreviousX);
		mOffsetY += (posY - mPreviousY);
		mPreviousX = posX;
		mPreviousY = posY;
		
		mImageWidth = mImage.getWidth();
		mImageHeight = mImage.getHeight();
	
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

		Log.i("d", "mOffsetX = " + mOffsetX + " , mOffsetY = " + mOffsetY );
	}
	
	private void initCutoutView(Context context) {
		this.context = context;
		mMatrix = new Matrix();
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
	}
	
	private void drawFrame(Canvas canvas) {
		canvas.restore();
		canvas.save();
		Paint paint = new Paint();
		paint.setColor(Color.parseColor("#FBBA06"));
		paint.setStrokeWidth(8);
		canvas.drawLine(0, 0, this.getWidth() - 1, 0, paint);
		canvas.drawLine(0, 0, 0, this.getHeight() - 1, paint);
		canvas.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight() - 1, paint);
		canvas.drawLine(0, this.getHeight() - 1, this.getWidth() - 1, this.getHeight() - 1, paint);
		canvas.restore();
		canvas.save();
	}
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) {//TODO
		float scale = detector.getScaleFactor();
		float scaleTemp = mScale;
		scaleTemp *= scale;
		if (scaleTemp > 5.0f || scaleTemp < 1.0f) {
			return true; // Ignore the request while scale factor is >2.0 or <1.0
		}
		mScale = scaleTemp;
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mImage != null) {
			mImage.recycle();
			mImage = null;
		}

		System.gc();
	}
}
