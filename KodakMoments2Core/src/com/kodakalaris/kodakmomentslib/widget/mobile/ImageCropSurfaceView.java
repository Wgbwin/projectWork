package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.kodakalaris.kodakmomentslib.AppConstants.ActivityTheme;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.photoedit.MPhotoEditActivity;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;
import com.kodakalaris.kodakmomentslib.util.ImageUtil;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;

public class ImageCropSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {
	protected String TAG = "ImageCropSurfaceView";
	private Context mContext;
	public TutorialThread mThread;

	/**
	 * cropBox startX position
	 */
	private float startX;
	/**
	 * cropBox startY position
	 */
	private float startY;
	/**
	 * for cropBox offsetX
	 */
	private float offsetX;
	/**
	 * for cropBox offsetY
	 */
	private float offsetY;

	/**
	 * scaleFactor
	 */
	private double scaleFactor = 1.0;
	/**
	 * lastScaleFactor
	 */
	private double lastScaleFactor = 1.0;

	/**
	 * for background start X position
	 */
	public int imageX = 0;
	/**
	 * for background start Y position
	 */
	public int imageY = 0;

	/**
	 * cropBox's newWidth
	 */
	float newWidth = 0;
	/**
	 * cropBox's newHeight
	 */
	float newHeight = 0;

	/**
	 * started distance
	 */
	double starteddistance = 1.0;
	/**
	 * can be zoom or not
	 */
	private boolean isPinchZoom = false;

	/**
	 * cropBox's paint
	 */
	Paint mCropPaint = new Paint();
	public PrintItem printItem;
	/**
	 * represent as ROI's width
	 */
	public float width = 0;
	/**
	 * represent as ROI's height
	 */
	public float height = 0;

	float canvasWidth;
	float canvasHeight;

	/**
	 * cropBox's rectangle
	 */
	public RectF rect = null;

	/**
	 * sourceBitmap as background
	 */
	public Bitmap img = null;
	/**
	 * bitmap comes from printItem
	 */
	public ROI roi = new ROI();

	Bitmap lowRes;
	boolean rotate = false;
	double defaultScale = 1.0;
	boolean showLowRes = false;
	public int rotateDegree = 0;
	private long preTouchTime;
	private float x1;
	private float x2;
	private float y1;
	private float y2;
	private boolean isTouchForMove = true;
	private boolean isNeedSwapROI = false;
	private boolean isNeedStop = false;;

	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			if (isTouchLowResIcon(event))
				return true;
			int pointerIndex = ((event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			int pointerId = event.getPointerId(pointerIndex);
			int action = (event.getAction() & MotionEvent.ACTION_MASK);
			int pointCnt = event.getPointerCount();
			if (pointCnt > 1)
				isPinchZoom = true;
			else
				isPinchZoom = false;
			if (!isPinchZoom) {
				pointerId = event.getPointerId(0);
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					startX = event.getX(pointerId);
					startY = event.getY(pointerId);
					if (!checkFingerDownAreaOut(event)) {
						// when finger touches in the image
						preTouchTime = System.currentTimeMillis();
						x1 = event.getX();
						y1 = event.getY();
					}
					break;
				case MotionEvent.ACTION_MOVE:
					onePointToMoveCropBox(event, pointerId);
					if (!checkFingerDownAreaOut(event)) {
						x2 = event.getX();
						y2 = event.getY();
						double distance = distanceBetweenFingers(x1, x2, y1, y2);
						if (distance > 10) {
							isTouchForMove = false;
						}
					}

					break;
				case MotionEvent.ACTION_UP:
					offsetY = 0;
					offsetX = 0;
					if (System.currentTimeMillis() - preTouchTime < 2500
							&& isTouchForMove) {
						startX = rect.centerX();
						startY = rect.centerY();
						onePointToMoveCropBox(event, pointerId);
					}
					if (!isTouchForMove) {
						isTouchForMove = true;
					}
					break;
				}
			} else {
				switch (action) {
				case MotionEvent.ACTION_MOVE:
					zoomCropBox(event);
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_1_UP:
				case MotionEvent.ACTION_POINTER_2_UP: {
					lastScaleFactor = scaleFactor;
					defaultScale = scaleFactor;
					checkIfLowResource();
					break;
				}
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_1_DOWN:
				case MotionEvent.ACTION_POINTER_2_DOWN:
					starteddistance = getDistanceBetweenToPoint(event);
					break;
				}
			}

		} catch (IllegalArgumentException e) {
			Log.e(TAG, "IllegalArgumentException error...." + e.getMessage());
		}
		return true; // processed
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (canvas == null) {
			return;
		}
		if (isNeedStop) {
			canvas.drawColor(Color.BLACK);
			return;
		}
		// get the height and width of the canvas so we know how large to make
		// the image
		try {
			canvasWidth = canvas.getWidth();
			canvasHeight = canvas.getHeight();
			canvas.drawColor(Color.BLACK);
			// if the image is null, we need to render it
			if (img == null) {
				PhotoInfo photo = printItem.getImage();
				if (TextUtils.isEmpty(photo.getPhotoEditPath())) {
					photo.setPhotoEditPath(ImageUtil.getFilePath(mContext,
							photo.getLocalUri()));
				}
				ExifInterface exif = null;

				if (photo.getPhotoSource().isFromPhone()) {
					exif = new ExifInterface(photo.getPhotoEditPath());

					img = (Bitmap) ImageUtil.getImageLocal(
							photo.getPhotoEditPath(),
							(int) (canvasWidth * 78 / 100),
							(int) (canvasHeight * 78 / 100));

					// Log.i(TAG,
					// "img.getHeight(),img.getWidth():" + img.getHeight()
					// + "," + img.getWidth());
				} else {
					// TODO load image from network by Kaly
				}

				if (exif != null) {
					int orientation = exif.getAttributeInt("Orientation", 0);
					if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
						img = ImageUtil.rotateBitmap(img, 90);
						isNeedSwapROI = true;
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
						img = ImageUtil.rotateBitmap(img, 180);
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
						img = ImageUtil.rotateBitmap(img, 270);
						isNeedSwapROI = true;
					}
				}
				img = ImageUtil.rotateBitmap(img, rotateDegree);
				double scaleHeight = (canvasHeight * 1.0 * 0.8)
						/ (img.getHeight() * 1.0);
				double scaleWidth = (canvasWidth * 1.0 * 0.8)
						/ (img.getWidth() * 1.0);
				// scale the downsampled image in order to fill the entire width
				// for landscape or height for a portrait image
				if (scaleHeight < scaleWidth) {
					img = Bitmap.createScaledBitmap(img,
							(int) (img.getWidth() * scaleHeight),
							(int) (img.getHeight() * scaleHeight), true);
				} else {
					img = Bitmap.createScaledBitmap(img,
							(int) (img.getWidth() * scaleWidth),
							(int) (img.getHeight() * scaleWidth), true);
				}
				// Figure out our offset for the rendered image

				imageX = (int) ((canvasWidth - img.getWidth()) / 2);
				imageY = (int) ((canvasHeight - img.getHeight()) / 2);
				loadRoi();
			}
			if (rect == null) {
				loadRoi();
			}

			try {
				imageX = (int) ((canvasWidth - img.getWidth()) / 2);
				imageY = (int) ((canvasHeight - img.getHeight()) / 2);
				canvas.drawBitmap(img, imageX, imageY, null);
				if (rotate) {
					if (newWidth == 0 || newHeight == 0) {
						newWidth = width;
						newHeight = height;
					}
					float temp = newWidth;
					newWidth = newHeight;
					newHeight = temp;
					temp = width;
					width = height;
					height = temp;
					rotate = false;
					float oldCenterX = rect.centerX();
					float oldCenterY = rect.centerY();
					rect.right = newWidth + rect.left;
					rect.bottom = newHeight + rect.top;
					rotateCheckBounds();
					rect.offset(oldCenterX - rect.centerX(),
							oldCenterY - rect.centerY());
				}
				checkCropBoxBounds();
				canvas.drawRect(rect, mCropPaint);
			} catch (Exception ex) {
				Log.e(TAG, "****************mm***" + ex.getLocalizedMessage());
			}

			// refresh the low res warning icon after change print size

			checkIfLowResource();

			if (showLowRes) {
				canvas.drawBitmap(lowRes, imageX,
						canvas.getHeight() - lowRes.getHeight() - imageY, null);
			}

		} catch (Exception ex) {
			// ex.printStackTrace();
			Log.i(TAG, "~~~~~~~~~" + ex.getMessage());
		}

	}

	public void updateImage() {
		PhotoInfo photo = printItem.getImage();
		if (TextUtils.isEmpty(photo.getPhotoEditPath())) {
			photo.setPhotoEditPath(ImageUtil.getFilePath(mContext,
					photo.getLocalUri()));
		}
		if (img != null) {
			img.recycle();
			img = null;
		}
		if (photo.getPhotoSource().isFromPhone()) {
			img = (Bitmap) ImageUtil.getImageLocal(photo.getPhotoEditPath(),
					(int) (canvasWidth * 78 / 100),
					(int) (canvasHeight * 78 / 100));
			
			if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()) {
				int orientation = ImageUtil.getDegreesExifOrientation(photo.getPhotoEditPath());							
				if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
					img = ImageUtil.rotateBitmap(img, 90);				
				} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
					img = ImageUtil.rotateBitmap(img, 180);
				} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
					img = ImageUtil.rotateBitmap(img, 270);				
				}
			}

		} else {
			// TODO load image from network by Kaly
		}
		img = ImageUtil.rotateBitmap(img, rotateDegree);
		double scaleHeight = (canvasHeight * 1.0 * 0.8)
				/ (img.getHeight() * 1.0);
		double scaleWidth = (canvasWidth * 1.0 * 0.8) / (img.getWidth() * 1.0);
		// scale the downsampled image in order to fill the entire width
		// for landscape or height for a portrait image
		if (scaleHeight < scaleWidth) {
			img = Bitmap.createScaledBitmap(img,
					(int) (img.getWidth() * scaleHeight),
					(int) (img.getHeight() * scaleHeight), true);
		} else {
			img = Bitmap.createScaledBitmap(img,
					(int) (img.getWidth() * scaleWidth),
					(int) (img.getHeight() * scaleWidth), true);
		}
		imageX = (int) ((canvasWidth - img.getWidth()) / 2);
		imageY = (int) ((canvasHeight - img.getHeight()) / 2);
	}

	// public boolean isNotneedSwapROI = false;
	public void setRotateDegree(int wantRotateDegree) {
		rotateDegree = wantRotateDegree;
		// img = null;
		// isNotneedSwapROI = true;
	}
	
	public int[] getPageWHInfo(){
		int pageWidth = printItem.getEntry().proDescription.pageWidth;
		int pageHeight = printItem.getEntry().proDescription.pageHeight;
		return new int[]{pageWidth,pageHeight};
	}
		
	@SuppressWarnings("deprecation")
	private boolean isTouchLowResIcon(MotionEvent event) {
		if (showLowRes) {
			if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP
					&& event.getPointerCount() <= 1) {
				int pointerIndexNow = ((event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
				int pointerIdNow = event.getPointerId(pointerIndexNow);
				startX = event.getX(pointerIdNow);
				startY = event.getY(pointerIdNow);
				if (startX >= imageX
						&& startX < lowRes.getWidth() * 1.5 + imageX
						&& startY >= canvasHeight - lowRes.getHeight() * 1.5
								- imageY && startY < canvasHeight - imageY) {
					// Log.d(TAG, " *Low Res Icon was clicked!* ");
					showAlertDialog();
					return true;
				}
			}
		}
		return false;
	}

	private void zoomCropBox(MotionEvent event) {
		try {
			double currentDistance = getDistanceBetweenToPoint(event);
			scaleFactor = lastScaleFactor * (currentDistance / starteddistance);
			if (((int) (width * scaleFactor) < img.getWidth())
					&& (((int) (height * scaleFactor)) < img.getHeight())) {
				newWidth = (int) (width * scaleFactor);
				newHeight = (int) (height * scaleFactor);
				defaultScale = scaleFactor;
			} else {
				scaleFactor = defaultScale;
				lastScaleFactor = defaultScale;
			}
			rect.left = rect.left + (rect.width() - newWidth) / 2;
			rect.top = rect.top + (rect.height() - newHeight) / 2;
			rect.right = rect.right - (rect.width() - newWidth) / 2;
			rect.bottom = rect.bottom - (rect.height() - newHeight) / 2;

			checkCropBoxBounds();
		} catch (Exception ex) {
			// ex.printStackTrace();
			Log.e(TAG, "!!!!!!!!" + ex.getMessage());
		}
	}

	private void checkIfLowResource() {
		double width = img.getWidth();
		double height = img.getHeight();
		double w = (rect.width() / width);
		double h = (rect.height() / height);
		if (TextUtils.isEmpty(printItem.getImage().getPhotoEditPath())) {
			String mPhotoEditPath = ImageUtil.getFilePath(mContext, printItem
					.getImage().getLocalUri());
			printItem.getImage().setPhotoEditPath((mPhotoEditPath));
		}
		int pageWidth = printItem.getEntry().proDescription.pageWidth;
		int pageHeight = printItem.getEntry().proDescription.pageHeight;
		int[] category = new int[2];
		if (w > h && width > height) {
			category[0] = pageWidth;
			category[1] = pageHeight;
			showLowRes = ImageUtil.isLowResWarning(printItem.getImage(),
					category, w, h);
		} else {
			category[0] = pageHeight;
			category[1] = pageWidth;
			showLowRes = ImageUtil.isLowResWarning(printItem.getImage(),
					category, w, h);
		}
	}

	private void showAlertDialog() {
		GeneralAlertDialogFragment lowResDialog = new GeneralAlertDialogFragment(
				mContext, ActivityTheme.DARK, true);
		lowResDialog.setMessage(mContext
				.getString(R.string.image_lowResWarning));
		lowResDialog.setPositiveButton(mContext.getString(R.string.image_ok),
				new BaseGeneralAlertDialogFragment.OnClickListener() {

					@Override
					public void onClick(
							BaseGeneralAlertDialogFragment dialogFragment,
							View v) {
						dialogFragment.dismiss();
					}
				});
		lowResDialog.show(
				((MPhotoEditActivity) mContext).getSupportFragmentManager(),
				TAG);
	}

	private double getDistanceBetweenToPoint(MotionEvent event) {
		double x = event.getX(0) - event.getX(1);
		double y = event.getY(0) - event.getY(1);
		return Math.sqrt(x * x + y * y);
	}

	private void onePointToMoveCropBox(MotionEvent event, int pointerId) {
		if (pointerId != 0) {
			return;
		}
		try {
			offsetX = event.getX(pointerId) - startX;
			offsetY = event.getY(pointerId) - startY;
			startX = event.getX(pointerId);
			startY = event.getY(pointerId);
			rect.left = rect.left + offsetX;
			rect.top = rect.top + offsetY;
			rect.right = rect.right + offsetX;
			rect.bottom = rect.bottom + offsetY;
			checkCropBoxBounds();
		} catch (Exception ex) {
			Log.i(TAG, "onePointToMoveCropBox:" + ex.getMessage());
		}
	}

	/**
	 * Description: invoke to scale the size if out of the bounds to fix frame
	 * Add by: Kaly At Jan 21, 2015 10:24:56 AM
	 */
	public void rotateCheckBounds() {
		double diff = 0.0;
		double perc = 1.0;

		if (newWidth > img.getWidth()) {
			diff = newWidth - img.getWidth();
			perc = 1.0 - (diff / newWidth);
			rect.right *= perc;
			rect.bottom *= perc;
			rect.top *= perc;
			rect.left *= perc;
			newWidth *= perc;
			newHeight *= perc;
			scaleFactor *= perc;
			defaultScale *= perc;
		}

		if (newHeight > img.getHeight()) {
			diff = newHeight - img.getHeight();
			perc = 1.0 - (diff / newHeight);
			rect.right *= perc;
			rect.bottom *= perc;
			rect.top *= perc;
			rect.left *= perc;
			newWidth *= perc;
			newHeight *= perc;
			scaleFactor *= perc;
			defaultScale *= perc;
		}
	}

	/**
	 * Description: invoke to stop moving cropBox out of the bounds to fix frame
	 * Add by: Kaly At Jan 21, 2015 10:24:56 AM
	 */
	public void checkCropBoxBounds() {
		if (rect.left < imageX) {
			rect.left = imageX;
			rect.right = rect.left + newWidth;
		}
		if (rect.right > (imageX + img.getWidth())) {
			rect.right = (imageX + img.getWidth());
			rect.left = rect.right - newWidth;
		}
		if (rect.top < imageY) {
			rect.top = imageY;
			rect.bottom = newHeight + rect.top;
		}
		if (rect.bottom > (img.getHeight() + imageY)) {
			rect.bottom = img.getHeight() + imageY;
			rect.top = rect.bottom - newHeight;
		}
	}

	private boolean checkFingerDownAreaOut(MotionEvent event) {
		if (event != null && img != null) {
			int positionX = (int) event.getX(0);
			int positionY = (int) event.getY(0);
			if (positionX >= imageX && positionX <= imageX + img.getWidth()
					&& positionY >= imageY
					&& positionY <= imageY + img.getHeight()) {
				return false;
			}
		}
		return true;
	}

	public void loadRoi() {
		float x = 0;
		float y = 0;
		Log.i(TAG, "pre--Crop-L-roi:>" + roi.toString());
		Log.i(TAG, "===========pre--Crop-L- printItem.getRoi():>"
				+ printItem.getRoi().toString());
		if (isNeedSwapROI) {
			Log.i(TAG, "==============================");
			roi.x = printItem.getRoi().y;
			roi.y = printItem.getRoi().x;
			roi.w = printItem.getRoi().h;
			roi.h = printItem.getRoi().w;
		} else {
			Log.i(TAG, "---------------------------");
			roi.x = printItem.getRoi().x;
			roi.y = printItem.getRoi().y;
			roi.w = printItem.getRoi().w;
			roi.h = printItem.getRoi().h;
		}

		x = (float) ((roi.x) * img.getWidth());
		y = (float) ((roi.y) * img.getHeight());
		height = (float) ((roi.h) * img.getHeight());
		width = (float) ((roi.w) * img.getWidth());
			
		int[] printPageInfo = getPageWHInfo();	
		if (printPageInfo != null) {
			double gap = 15;			
			if ((printPageInfo[0] >= printPageInfo[1] ) && (width >= height) || (printPageInfo[0] <= printPageInfo[1] ) && (width <= height)) {
				double pWInfo = printPageInfo[1]*width;
				double pHInfo = printPageInfo[0]*height;	
				if (Math.abs(pWInfo - pHInfo) > gap) {										
					width = (float) (pHInfo/printPageInfo[1]);					
				}				
			} else if ((printPageInfo[0] >= printPageInfo[1] ) && (width <= height) || (printPageInfo[0] <= printPageInfo[1] ) && (width >= height)){
				double pWInfo = printPageInfo[0]*width;
				double pHInfo = printPageInfo[1]*height;	
				if (Math.abs(pWInfo - pHInfo) > gap ) {								
					width = (float) (pHInfo/printPageInfo[0]);					
				}		
			} 			
		}
		newWidth = width;
		newHeight = height;
		rect = new RectF(x + imageX, y + imageY, width + x + imageX,
				(int) height + y + imageY);
		Log.i(TAG, "============late--Crop-L->" + roi.toString());
	}

	public ROI getROI() {
		return roi;
	}

	public ROI saveROI() {
		if (img != null) {
			double width = img.getWidth();
			double height = img.getHeight();
			if (isNeedSwapROI) {
				roi.y = (rect.left - imageX) / width;
				roi.x = (rect.top - imageY) / height;
				roi.h = rect.width() / width;
				roi.w = rect.height() / height;
			} else {
				roi.x = (rect.left - imageX) / width;
				roi.y = (rect.top - imageY) / height;
				roi.w = rect.width() / width;
				roi.h = rect.height() / height;
			}
			roi.ContainerH = printItem.getRoi().ContainerH;
			roi.ContainerW = printItem.getRoi().ContainerW;
			Log.i(TAG, "Crop-S->" + roi.toString());
			roi.x = roi.x <= 0 ? 0 : roi.x;
			roi.y = roi.y <= 0 ? 0 : roi.y;
			roi.w = roi.w > 1 ? 1 : roi.w;
			roi.h = roi.h > 1 ? 1 : roi.h;
			return roi;
		}
		return printItem.getRoi();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(TAG, "******" + "surfaceDestroyed");
		holder.removeCallback(this);
		boolean retry = true;
		if (mThread != null) {
			mThread.setRunning(false);
			while (retry) {
				try {
					mThread.join();
					retry = false;
				} catch (InterruptedException e) {
					// we will try it again and again...
					Log.e(TAG, "******" + e.getMessage());
				}
			}
		}
	}

	private double distanceBetweenFingers(float x1, float x2, float y1, float y2) {
		float disX = Math.abs(x1 - x2);
		float disY = Math.abs(y1 - y2);
		return Math.sqrt(disX * disX + disY * disY);
	}

	public void setViewVisible() {
		isNeedStop = false;
		if (mThread == null || !mThread.isAlive()) {
			mThread = null;
			mThread = new TutorialThread(getHolder(), ImageCropSurfaceView.this);
			mThread.setRunning(true);
			mThread.start();
		}
	}

	public void setViewGone() {
		isNeedStop = true;
		if (mThread != null) {
			mThread.setRunning(false);
			mThread.interrupt();
			mThread = null;
		}
	}

	private void initData(Context context) {
		mContext = context;
		getHolder().addCallback(this);
		mCropPaint.setStyle(Paint.Style.STROKE);
		mCropPaint.setStrokeWidth(4f);
		mCropPaint.setColor(Color.parseColor("#FBBA06"));
		Bitmap src = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.icon_lowresolution);
		lowRes = Bitmap.createScaledBitmap(src, src.getWidth() * 2,
				src.getHeight() * 2, true);
	}

	public ImageCropSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
	}

	public boolean isRotate() {
		return rotate;
	}

	public void setRotate(boolean rotate) {
		this.rotate = rotate;
	}

	public ImageCropSurfaceView(Context context) {
		super(context);
		initData(context);
	}

	public void InterruptThread() {
		mThread.interrupt();
	}

	public void recycleInFinish() {
		if (img != null && !img.isRecycled())
			img.recycle();
		if (lowRes != null && !lowRes.isRecycled())
			lowRes.recycle();
	}

}

class TutorialThread extends Thread {
	private SurfaceHolder mSurfaceHolder;
	private ImageCropSurfaceView mImageView;
	private boolean mRun = false;

	public TutorialThread(SurfaceHolder surfaceHolder,
			ImageCropSurfaceView panel) {
		mSurfaceHolder = surfaceHolder;
		mImageView = panel;
	}

	public void setRunning(boolean run) {
		mRun = run;
	}

	@Override
	public void run() {
		Canvas canvas = null;
		while (mRun) {
			canvas = null;
			try {
				if (mSurfaceHolder.getSurface() != null) {
					canvas = mSurfaceHolder.lockCanvas();
					synchronized (mSurfaceHolder) {
						mImageView.onDraw(canvas);
					}
					try {
						Thread.sleep(20);
					} catch (Exception e) {
					}
				}
			} finally {
				if (canvas != null) {
					mSurfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
}