package com.kodakalaris.kodakmomentslib.widget.mobile;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.kodakalaris.kodakmomentslib.AppConstants.ActivityTheme;
import com.kodakalaris.kodakmomentslib.AppConstants.PhotoSource;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.photoedit.MPhotoEditActivity;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;
import com.kodakalaris.kodakmomentslib.util.FileDownloader;
import com.kodakalaris.kodakmomentslib.util.ImageUtil;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;

public class ZoomImageView extends ImageView {
	private String TAG = ZoomImageView.class.getSimpleName();
	private Context mContext;
	public static final int STATUS_NONE = 0;
	/**
	 * init image zoom status
	 */
	public static final int STATUS_ZOOM_OUT = 1;

	/**
	 * init image scale status
	 */
	public static final int STATUS_ZOOM_IN = 2;

	/**
	 * init image drag status
	 */
	public static final int STATUS_MOVE = 3;

	/**
	 * as a tool to move or scale image
	 */
	public Matrix matrix = new Matrix();

	/**
	 * show the image
	 */
	public Bitmap sourceBitmap;

	/**
	 * record the current status
	 * STATUS_INIT、STATUS_ZOOM_OUT、STATUS_ZOOM_IN和STATUS_MOVE
	 */
	public int currentStatus;

	/**
	 * ZoomImageView's width
	 */
	public int width;

	/**
	 * ZoomImageView's height
	 */
	public int height;

	/**
	 * record the center of two point on the X coordinate
	 */
	public float centerPointX;

	/**
	 * record the center of two point on the Y coordinate
	 */
	public float centerPointY;

	/**
	 * record the image's width which will change when it's scaling
	 */
	public float currentBitmapWidth;

	/**
	 * record the image's height which will change when it's scaling
	 */
	public float currentBitmapHeight;

	/**
	 * Record the abscissa(x-coordinate) of the last time finger movement
	 */
	public float lastXMove = -1;

	/**
	 * Record the ordinate(y-coordinate) of the last time finger movement
	 */
	public float lastYMove = -1;

	/**
	 * record the moving distance in the horizontal direction
	 */
	public float movedDistanceX;

	/**
	 * record the moving distance in the vertical direction
	 */
	public float movedDistanceY;

	/**
	 * record the offset in the horizontal direction
	 */
	public float totalTranslateX = 0.0f;

	/**
	 * record the offset in the vertical direction
	 */
	public float totalTranslateY = 0.0f;

	/**
	 * record the image's total scale in the matrix
	 */
	public float totalRatio;
	/**
	 * record the image's max scale in the matrix
	 */
	// public float maxRatio = 6;
	/**
	 * record the fingers moving distance caused by scaling
	 */
	public float scaledRatio;
	/**
	 * record the distance between the last two fingers
	 */
	public double lastFingerDis;

	/**
	 * record the user's interest of the image's area
	 */
	private ROI roi = new ROI();
	public PrintItem printItem;
	private PhotoInfo photoInfo;
	private Bitmap initBitmap;
	public Bitmap lowRes;

	public int rotateDegree = 0;
	private long preTouchTime;
	private float x1;
	private float x2;
	private float y1;
	private float y2;
	private boolean isTouchForMove = true;
	public boolean showLowRes = false;
	private boolean isNeedSwapROI = false;

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		width = getWidth();
		height = getHeight();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (showLowRes) {// if low resolution icon is shown
			if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP
					&& event.getPointerCount() <= 1) {
				int pointerIndexNow = ((event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
				int pointerIdNow = event.getPointerId(pointerIndexNow);
				if (pointerIdNow == 0) {
					float startX = event.getX(pointerIdNow);
					float startY = event.getY(pointerIdNow);
					if (startX >= 0 && startX < lowRes.getWidth() * 1.5
							&& startY >= height - lowRes.getHeight() * 1.5
							&& startY < height) {
						showAlertDialog();
						return true;
					}
				}
			}
		}

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			if (event.getPointerCount() == 1) {
				preTouchTime = System.currentTimeMillis();
				x1 = event.getX();
				y1 = event.getY();
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			if (event.getPointerCount() == 2) {
				// 2 fingers on the screen, calculate distance between fingers
				lastFingerDis = distanceBetweenFingers(event);
				centerPointBetweenFingers(event);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() == 1) {
				float xMove = event.getX();
				float yMove = event.getY();
				oneFingerMoveView(xMove, yMove);
				x2 = event.getX();
				y2 = event.getY();
				double distance = distanceBetweenFingers(x1, x2, y1, y2);
				Log.e(TAG, "distance:"+distance);
				if (distance > 12.0) {
					isTouchForMove = false;
				}
			} else if (event.getPointerCount() == 2) {
				double fingerDis = distanceBetweenFingers(event);
				twoFingerZoomView(fingerDis);
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (event.getPointerCount() == 2) {
				// Finger leave the screen, temporarily value restored
				lastXMove = -1;
				lastYMove = -1;
			}
		case MotionEvent.ACTION_UP:
			// Finger leave the screen, temporarily value restored
			lastXMove = -1;
			lastYMove = -1;
			if (event.getPointerCount() == 1) {
				if (System.currentTimeMillis() - preTouchTime < 2500
						&& isTouchForMove) {
					movePointToCenter();
				}
				if (!isTouchForMove) {
					isTouchForMove = true;
				}
			}
			break;
		}

		return true;
	}

	private void movePointToCenter() {
		float centerX = getWidth() / 2;
		float centerY = getHeight() / 2;
		movedDistanceX = centerX - x1;
		movedDistanceY = centerY - y1;
		checkMoveDistanceBounds();
		currentStatus = STATUS_MOVE;
		invalidate();
	}

	public void oneFingerMoveView(float xMove, float yMove) {
		if (lastXMove == -1 && lastYMove == -1) {
			lastXMove = xMove;
			lastYMove = yMove;
		}
		currentStatus = STATUS_MOVE;
		movedDistanceX = xMove - lastXMove;
		movedDistanceY = yMove - lastYMove;
		checkMoveDistanceBounds();
		// invoke the method onDraw()
		invalidate();
		lastXMove = xMove;
		lastYMove = yMove;
	}

	/**
	 * Description: image is not allowed to be dragged out of the view border
	 * 2015 1:18:58 PM
	 */
	private void checkMoveDistanceBounds() {
		if (totalTranslateX + movedDistanceX > 0) {
			movedDistanceX = 0 - totalTranslateX;
		} else if (width - (totalTranslateX + movedDistanceX) > currentBitmapWidth) {
			movedDistanceX = width - totalTranslateX - currentBitmapWidth;
		}
		if (totalTranslateY + movedDistanceY > 0) {
			movedDistanceY = 0 - totalTranslateY;
		} else if (height - (totalTranslateY + movedDistanceY) > currentBitmapHeight) {
			movedDistanceY = height - totalTranslateY - currentBitmapHeight;
		}
	}

	public void twoFingerZoomView(double fingerDis) {
		// Where two finger moving on the screen is for scaling
		if (fingerDis > lastFingerDis) {
			currentStatus = STATUS_ZOOM_OUT;
		} else if (fingerDis < lastFingerDis) {
			currentStatus = STATUS_ZOOM_IN;
		}

		scaledRatio = (float) (fingerDis / lastFingerDis);
		totalRatio = totalRatio * scaledRatio;
		invalidate();
		lastFingerDis = fingerDis;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (initBitmap == null) {
			return;
		}
		if (sourceBitmap == null) {
			sourceBitmap = initBitmap;
		}
		switch (currentStatus) {
		case STATUS_ZOOM_OUT:
		case STATUS_ZOOM_IN:
			zoom(canvas);
			break;
		case STATUS_MOVE:
			move(canvas);
			break;
		case STATUS_NONE:
			handleImageWithMatrix(canvas);
			break;
		}
		instantStorePrintItem();
		checkIfLowResource();
		if (showLowRes) {
			canvas.drawBitmap(lowRes, 0, height - lowRes.getHeight(), null);
		}

	}

	public void instantStorePrintItem() {
		if (isNeedSwapROI) {
			printItem.getRoi().y = Math.abs((totalTranslateX / totalRatio)
					/ roi.ContainerW);
			printItem.getRoi().x = Math.abs((totalTranslateY / totalRatio)
					/ roi.ContainerH);
			printItem.getRoi().h = (width / totalRatio / roi.ContainerW);
			printItem.getRoi().w = (height / totalRatio / roi.ContainerH);
		} else {
			printItem.getRoi().x = Math.abs((totalTranslateX / totalRatio)
					/ roi.ContainerW);
			printItem.getRoi().y = Math.abs((totalTranslateY / totalRatio)
					/ roi.ContainerH);
			printItem.getRoi().w = (width / totalRatio / roi.ContainerW);
			printItem.getRoi().h = (height / totalRatio / roi.ContainerH);
		}
		Log.i(TAG, "zoomStore:" + printItem.getRoi().toString());
	}

	public ROI getROI() {
		return printItem.getRoi();
	}

	/**
	 * To zoom the image
	 */
	public void zoom(Canvas canvas) {
		currentBitmapWidth = sourceBitmap.getWidth() * totalRatio;
		currentBitmapHeight = sourceBitmap.getHeight() * totalRatio;
		float translateX = 0f;
		float translateY = 0f;
		// if image width is smaller than the screen width
		if (currentBitmapWidth < width) {
			currentBitmapWidth = width;
			totalRatio = currentBitmapWidth / sourceBitmap.getWidth();
			currentBitmapHeight = sourceBitmap.getHeight() * totalRatio;
		}
		// if image height is smaller than the height of the screen

		if (currentBitmapHeight < height) {
			currentBitmapHeight = height;
			totalRatio = currentBitmapHeight / sourceBitmap.getHeight();
			currentBitmapWidth = sourceBitmap.getWidth() * totalRatio;
		}
		if (currentBitmapWidth > width) {
			translateX = totalTranslateX * scaledRatio + centerPointX
					* (1 - scaledRatio);
			// ensure that horizontal direction will not offset the screen
			if (translateX > 0) {
				translateX = 0;
			} else if (width - translateX > currentBitmapWidth) {
				translateX = width - currentBitmapWidth;
			}
		}

		if (currentBitmapHeight > height) {
			translateY = totalTranslateY * scaledRatio + centerPointY
					* (1 - scaledRatio);
			// ensure that vertical direction will not offset the screen
			if (translateY > 0) {
				translateY = 0;
			} else if (height - translateY > currentBitmapHeight) {
				translateY = height - currentBitmapHeight;
			}
		}

		totalTranslateX = translateX;
		totalTranslateY = translateY;
		handleImageWithMatrix(canvas);
	}

	/**
	 * To handle with the translate
	 * 
	 */
	public void move(Canvas canvas) {
		// According to the fingers moving distance to calculate the total
		// offset value
		float translateX = totalTranslateX + movedDistanceX;
		float translateY = totalTranslateY + movedDistanceY;
		totalTranslateX = translateX;
		totalTranslateY = translateY;
		checkBounds();
		matrix.postTranslate(movedDistanceX, movedDistanceY);
		canvas.drawBitmap(sourceBitmap, matrix, null);
	}

	/**
	 * Description: reset matrix to the wanted effect
	 */
	private void handleImageWithMatrix(Canvas canvas) {
		if (sourceBitmap == null) {
			return;
		}
		matrix.reset();
		// first,zoom the image with the existing radio
		matrix.postScale(totalRatio, totalRatio);
		checkBounds();
		// then translate the moving distance
		matrix.postTranslate(totalTranslateX, totalTranslateY);
		canvas.drawBitmap(sourceBitmap, matrix, null);
	}

	/**
	 * Description: avoid the print area to have the image
	 */
	private void checkBounds() {
		if (totalTranslateX > 0) {
			totalTranslateX = 0;
		}
		if (totalTranslateY > 0) {
			totalTranslateY = 0;
		}
		if (isNeedSwapROI) {
			if (width - totalTranslateX > currentBitmapWidth) {
				totalTranslateX = width - currentBitmapWidth;
			}
			if (height - totalTranslateY > currentBitmapHeight) {
				totalTranslateY = height - currentBitmapHeight;
			}
		} else {
			if (width - totalTranslateX > currentBitmapWidth) {
				totalTranslateX = width - currentBitmapWidth;
			}
			if (height - totalTranslateY > currentBitmapHeight) {
				totalTranslateY = height - currentBitmapHeight;
			}
		}
	}

	public void updateData(ROI mRoi, int mWidth, int mHeight) {
		if (mRoi == null) {
			return;
		}
		width = mWidth;
		height = mHeight;
		if (isNeedSwapROI) {
			roi.x = mRoi.y;
			roi.y = mRoi.x;
			roi.w = mRoi.h;
			roi.h = mRoi.w;
		} else {
			roi.x = mRoi.x;
			roi.y = mRoi.y;
			roi.w = mRoi.w;
			roi.h = mRoi.h;
		}
		roi.ContainerH = initBitmap.getHeight();
		roi.ContainerW = initBitmap.getWidth();
		Log.i("ZoomImageView", "updateData:>" + roi.toString());
		updateData();
	}

	public void setImageBitmap(PrintItem printItem, int mWidth, int mHeight,
			int rotateDegree) {
		this.rotateDegree = rotateDegree;
		this.printItem = printItem;
		Log.i("ZoomImageView", "pre:>" + printItem.getRoi().toString());
		photoInfo = printItem.getImage();
		if (TextUtils.isEmpty(photoInfo.getPhotoEditPath())) {
			photoInfo.setPhotoEditPath(ImageUtil.getFilePath(mContext,
					photoInfo.getLocalUri()));

		}
		width = mWidth;
		height = mHeight;
		ExifInterface exif = null;
		isNeedSwapROI = false;
		if (photoInfo.getPhotoSource() == PhotoSource.PHONE) {
			try {
				exif = new ExifInterface(photoInfo.getPhotoEditPath());
			} catch (IOException e) {
				Log.i("ZoomImageView", e.getMessage());
			}
			initBitmap = ImageUtil.getImageLocal(photoInfo.getPhotoEditPath(),
					width, height);
		} else {
			// TODO load image from network by Kaly
			initBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.image_15);
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt("Orientation", 0);
			if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
				initBitmap = ImageUtil.rotateBitmap(initBitmap, 90);
				isNeedSwapROI = true;
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
				initBitmap = ImageUtil.rotateBitmap(initBitmap, 180);
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
				initBitmap = ImageUtil.rotateBitmap(initBitmap, 270);
				isNeedSwapROI = true;
			}
			if (isNeedSwapROI) {
				roi.x = printItem.getRoi().y;
				roi.y = printItem.getRoi().x;
				roi.w = printItem.getRoi().h;
				roi.h = printItem.getRoi().w;
			} else {
				roi.x = printItem.getRoi().x;
				roi.y = printItem.getRoi().y;
				roi.w = printItem.getRoi().w;
				roi.h = printItem.getRoi().h;
			}

		}
		initBitmap = ImageUtil.rotateBitmap(initBitmap, rotateDegree);
		roi.ContainerH = initBitmap.getHeight();
		roi.ContainerW = initBitmap.getWidth();
		Log.i("ZoomImageView", "late:>" + roi.toString());
		sourceBitmap = initBitmap;
		updateData();

	}

	public int totelRotateDegree = 0;

	// add rotate the image and the roi
	public void rotateImageBitmap() {
		if (initBitmap == null)
			return;

		initBitmap = ImageUtil.rotateBitmap(initBitmap, 90);
		totelRotateDegree += 90;
		// if (totelRotateDegree >= 360) {
		// totelRotateDegree = totelRotateDegree - 360;
		// }

		roi.x = printItem.getRoi().y;
		roi.y = printItem.getRoi().x;
		roi.w = printItem.getRoi().h;
		roi.h = printItem.getRoi().w;

		roi.ContainerH = initBitmap.getHeight();
		roi.ContainerW = initBitmap.getWidth();
		Log.i("ZoomImageView", "late:>" + roi.toString());
		sourceBitmap = initBitmap;

	}

	public ROI getRoi() {
		return roi;
	}

	public int getTotelRotateDegree() {
		return totelRotateDegree;
	}

	public void updateData(int mWidth, int mHeight) {
		width = mWidth;
		height = mHeight;
		updateData();
	}

	public void updateData() {
		int startBitmapPositionX = (int) (roi.x * roi.ContainerW);
		int startBitmapPositionY = (int) (roi.y * roi.ContainerH);
		totalRatio = (float) (width / (roi.w * roi.ContainerW));
		totalTranslateX = -(startBitmapPositionX * totalRatio);
		totalTranslateY = -(startBitmapPositionY * totalRatio);
		currentBitmapWidth = sourceBitmap.getWidth() * totalRatio;
		currentBitmapHeight = sourceBitmap.getHeight() * totalRatio;
		currentStatus = STATUS_NONE;
		invalidate();
	}

	/**
	 * Calculate the distance between two fingers
	 */
	public double distanceBetweenFingers(MotionEvent event) {
		return distanceBetweenFingers(event.getX(0), event.getX(1),
				event.getY(0), event.getY(1));
	}

	private double distanceBetweenFingers(float x1, float x2, float y1, float y2) {
		float disX = Math.abs(x1 - x2);
		float disY = Math.abs(y1 - y2);
		return Math.sqrt(disX * disX + disY * disY);
	}

	/**
	 * Calculation of the center point of the coordinates between two fingers
	 */
	public void centerPointBetweenFingers(MotionEvent event) {
		float xPoint0 = event.getX(0);
		float yPoint0 = event.getY(0);
		float xPoint1 = event.getX(1);
		float yPoint1 = event.getY(1);
		centerPointX = (xPoint0 + xPoint1) / 2;
		centerPointY = (yPoint0 + yPoint1) / 2;
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

	public void checkIfLowResource() {
		double w = printItem.getRoi().w;
		double h = printItem.getRoi().h;
		if (TextUtils.isEmpty(printItem.getImage().getPhotoEditPath())) {
			String mPhotoEditPath = ImageUtil.getFilePath(mContext, printItem
					.getImage().getLocalUri());
			printItem.getImage().setPhotoEditPath((mPhotoEditPath));
		}
		int width = printItem.getEntry().proDescription.pageWidth;
		int height = printItem.getEntry().proDescription.pageHeight;
		int[] category = new int[2];
		if (w > h && width > height) {
			category[0] = width;
			category[1] = height;
			showLowRes = ImageUtil.isLowResWarning(printItem.getImage(),
					category, w, h);
		} else {
			category[0] = height;
			category[1] = width;
			showLowRes = ImageUtil.isLowResWarning(printItem.getImage(),
					category, w, h);
		}

	}

	public void downloadImage(final PhotoInfo photoInfo, final Handler handler) {
		if (photoInfo.getImageResource().fetchPreviewURL() == null) {
			return;
		}
		if (photoInfo.getImageResource().id == null) {
			return;
		}
		final String imageUrl = photoInfo.getImageResource().fetchPreviewURL();
		final String saveImagePath = KM2Application.getInstance()
				.getTempImageFolderPath()
				+ "/"
				+ photoInfo.getImageResource().id + ".jpg";
		Log.i(TAG, "imageUrl--->" + imageUrl);
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean result = FileDownloader.download(imageUrl,
						saveImagePath);
				while (!result) {
					result = FileDownloader.download(imageUrl, saveImagePath);
				}
				handler.post(new Runnable() {

					@Override
					public void run() {
						initBitmap = ImageUtil.getImageLocal(saveImagePath,
								width, height);
						sourceBitmap = initBitmap;
						rotateDegree = 0;
						photoInfo.setPhotoEditPath(saveImagePath);
						Object[] array = new Object[2];
						if (handler != null) {
							if (initBitmap == null) {
								array[0] = null;
							} else {
								Log.i(TAG, "initBitmap loads succeed ");
								array[0] = photoInfo;

							}
							handler.obtainMessage(MPhotoEditActivity.END, array)
									.sendToTarget();
						}
					}
				});
			}
		}).start();

	}

	public void initData(PrintItem printItem) {
		this.printItem = printItem;
	}

	public PrintItem savePrintItem() {
		return printItem;
	}

	/**
	 * ZoomImageView Constructor，The current operating status is set to
	 * STATUS_INIT。
	 */
	public ZoomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		currentStatus = STATUS_NONE;
		Bitmap src = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.icon_lowresolution);
		lowRes = Bitmap.createScaledBitmap(src, src.getWidth() * 2,
				src.getHeight() * 2, true);
	}

	public void recycleInFinish() {
		if (initBitmap != null && !initBitmap.isRecycled())
			initBitmap.recycle();
		if (lowRes != null && !lowRes.isRecycled())
			lowRes.recycle();
	}

}