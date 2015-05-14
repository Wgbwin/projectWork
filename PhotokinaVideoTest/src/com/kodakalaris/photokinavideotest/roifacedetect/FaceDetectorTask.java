package com.kodakalaris.photokinavideotest.roifacedetect;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class FaceDetectorTask extends AsyncTask<Bitmap, Void, RectF> {

	private static final String TAG = FaceDetectorTask.class.getSimpleName();
	final int MAX_FACES = 6;
	final int IMAGE_TARGET_DIMENTION = 1200;
	final float MIN_GEN_ROI_PERCENT_IMAGE = 0.56f;
	final float NO_FACE_ZOOM_PERCENT = 0.05f;

	private Bitmap outputBitmap;
	private RectF roi;
	private float outputAspectRatio = 1;

	private WeakReference<ImageView> outputView;

	private FinishedFindingFacesEvent finishedEvent;

	public FaceDetectorTask() {
		this(1);
	}

	public FaceDetectorTask(float aspectRatio) {
		this.outputAspectRatio = aspectRatio;
	}

	public FaceDetectorTask(ImageView view) {
		this();
		outputView = new WeakReference<ImageView>(view);
	}

	public FaceDetectorTask(RectF outputROI, ImageView view, float aspectRatio) {
		this(aspectRatio);
		outputView = new WeakReference<ImageView>(view);
	}

	protected void onPostExecute(RectF roi) {
		if (outputView != null && outputBitmap != null) {
			ImageView view = outputView.get();
			if (view != null) {
				view.setImageBitmap(outputBitmap);
			}
			outputBitmap = null;
		}
		if (finishedEvent != null) {
			finishedEvent.onFinish(roi);
		}
	}

	@Override
	protected RectF doInBackground(Bitmap... img) {

		roi = new RectF(img[0].getWidth() / 2, img[0].getHeight() / 2, 0.0f, 0.0f);
		Bitmap b = getScaledBitmap(img[0]);
		if (!(b.getWidth() % 2 == 0)) {
			Log.e(TAG, "You are about to get:\n" + "ERROR: Return 0 faces because error exists in btk_FaceFinder_putDCR.\n");
		}
		outputBitmap = detectFace(b, (outputView != null && outputView.get() != null));
		return roi;
	}
	/**
	 * Bitmaps are already scaled. Dont bother wasting ram by holding another
	 * copy.
	 * */
	private Bitmap getScaledBitmap(Bitmap img) {
		float aspect = img.getWidth() / (float) img.getHeight();
		if (img.getWidth() > img.getHeight()) {
			// if (img.getWidth() < IMAGE_TARGET_DIMENTION) {
			// return img;
			// }
			return Bitmap.createScaledBitmap(img, IMAGE_TARGET_DIMENTION, (int) (IMAGE_TARGET_DIMENTION * (1.0 / aspect)), true);
		}
		// if (img.getHeight() < IMAGE_TARGET_DIMENTION) {
		// return img;
		// }
		int beforeVal = (int) (IMAGE_TARGET_DIMENTION * aspect);

		int value = (((int) (IMAGE_TARGET_DIMENTION * aspect)) / 2) * 2;
		Log.e(TAG, "Before:" + beforeVal + " After:" + value);
		return Bitmap.createScaledBitmap(img, value, IMAGE_TARGET_DIMENTION, true);
	}

	private Bitmap detectFace(Bitmap b, boolean draw) {
		Face[] faces = new Face[MAX_FACES];
		FaceDetector detector = new FaceDetector(b.getWidth(), b.getHeight(), MAX_FACES);
		Bitmap img = b;
		Canvas c = null;
		Paint p = null;
		img = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.RGB_565);
		c = new Canvas(img);
		p = new Paint();
		c.drawBitmap(b, 0, 0, null);
		if (draw) {
			p.setAlpha(100);
			p.setStyle(Paint.Style.STROKE);
			p.setTextSize(24.0f);
			p.setAntiAlias(true);
		}
		detector.findFaces(img, faces);
		PointF point = new PointF();
		int numFaces = 0;
		int largestFaceIndex = 0;
		for (int f = 0; f < faces.length; f++) {
			if (faces[f] != null) {
				numFaces++;
				faces[f].getMidPoint(point);
				if (draw) {
					p.setColor(Color.RED);
					p.setStyle(Paint.Style.STROKE);
					p.setStrokeWidth(3.0f);
					p.setAlpha(140);
					c.drawCircle(point.x, point.y, faces[f].eyesDistance(), p);
					p.setStyle(Paint.Style.FILL);
					p.setColor(Color.BLACK);
					p.setAlpha(255);
					c.drawText(faces[f].confidence() + "", point.x + 2, 2 + point.y + faces[f].eyesDistance() * 2, p);
					p.setColor(Color.RED);
					c.drawText(faces[f].confidence() + "", point.x, point.y + faces[f].eyesDistance() * 2, p);
				}
				if (faces[f].eyesDistance() > faces[largestFaceIndex].eyesDistance()) {
					largestFaceIndex = f;
				}
			}
		}
		for (int f = 0; f < faces.length; f++) {
			if (faces[f] != null) {
				faces[f].getMidPoint(point);
				float faceSize = faces[f].eyesDistance() / Math.min(img.getHeight(), img.getWidth());
				float facePadLeft = 0;
				float facePadTop = 0;
				float facePadRight = 0;
				float facePadBottom = 0;
				float facePad = faces[f].eyesDistance();

				p.setColor(Color.GREEN);
				p.setAlpha(255);
				c.drawText((int) (faceSize * 100) + "%", point.x, point.y + faces[f].eyesDistance() * 2 - 22, p);

				// Start process
				if (faceSize <= 0.05) {
					facePad *= 3.4;
				} else if (faceSize <= 0.2) {
					facePad *= 2;
				} else if (faceSize <= 0.4) {
					facePad *= 1.7f;
				} else if (faceSize <= 0.8) {
					facePad *= 1.2;
				}
				facePadBottom = (float) (Math.pow(1.0f - faceSize, 10) * facePad * 1.5f);
				// end Process
				facePadLeft += facePad;
				facePadTop += facePad;
				facePadRight += facePad;
				facePadBottom += facePad;
				if (faces[f].eyesDistance() >= 0.33f * faces[largestFaceIndex].eyesDistance()) {
					p.setColor(Color.YELLOW);
					p.setStyle(Paint.Style.STROKE);
					p.setAlpha(100);
					c.drawRect(point.x - facePadLeft, point.y - facePadTop, point.x + facePadRight, point.y + facePadBottom, p);
					roi.union(point.x - facePadLeft, point.y - facePadTop, point.x + facePadRight, point.y + facePadBottom);
				}
			}
		}
		roi.left = Math.max(0, roi.left);
		roi.top = Math.max(0, roi.top);
		roi.right = Math.min(b.getWidth(), roi.right);
		roi.bottom = Math.min(b.getHeight(), roi.bottom);

		if (Math.min(roi.width(), roi.height()) / Math.min(b.getWidth(), b.getHeight()) < MIN_GEN_ROI_PERCENT_IMAGE) {
			float widthInc = MIN_GEN_ROI_PERCENT_IMAGE - roi.width() / b.getWidth();
			float heightInc = MIN_GEN_ROI_PERCENT_IMAGE - roi.height() / b.getHeight();
			if (widthInc > 0) {
				roi.left -= widthInc / 2.0f * b.getWidth();
				roi.right += widthInc / 2.0f * b.getWidth();
			}
			if (heightInc > 0) {
				roi.top -= heightInc / 2.0f * b.getHeight();
				roi.bottom += heightInc / 2.0f * b.getHeight();
			}
		}

		RectF newROI = getAspectRatioRectangle(roi, 1);
		float widthDiff = (newROI.width() - roi.width()) / 2;
		float heightDiff = (newROI.height() - roi.height()) / 2;
		newROI.left -= widthDiff;
		newROI.top -= heightDiff;
		newROI.right -= widthDiff;
		newROI.bottom -= heightDiff;
		if (widthDiff * 2 < b.getWidth()) {
			if (newROI.left < 0) {
				newROI.right -= newROI.left;
				newROI.left -= newROI.left;
			} else if (newROI.right > b.getWidth()) {
				newROI.left -= newROI.right - b.getWidth();
				newROI.right -= newROI.right - b.getWidth();
			}
		} else {
			newROI.left = (widthDiff * 2 - b.getWidth()) / -2.0f;
			newROI.right = newROI.left + (widthDiff * 2);
		}
		if (heightDiff * 2 < b.getHeight()) {
			if (newROI.top < 0) {
				newROI.bottom -= newROI.top;
				newROI.top -= newROI.top;
			} else if (newROI.bottom > b.getHeight()) {
				newROI.top -= newROI.bottom - b.getHeight();
				newROI.bottom -= newROI.bottom - b.getHeight();
			}
		} else {
			newROI.top = (heightDiff * 2 - b.getHeight()) / -2.0f;
			newROI.bottom = newROI.top + (heightDiff * 2);
		}
		Log.e(TAG, "Found:" + numFaces);
		if (numFaces == 0) {
			// Warning, this 0 face logic is not used. That logic is done by
			// ROIImageView
			if (b.getWidth() > b.getHeight())// landscape
			{
				// 50 / 50
				roi.top = 0 + NO_FACE_ZOOM_PERCENT;
				roi.bottom = 1 - NO_FACE_ZOOM_PERCENT;
				float w = (b.getHeight() * (1 / outputAspectRatio)) / b.getWidth();
				roi.left = 0.5f - w / 2 + NO_FACE_ZOOM_PERCENT;
				roi.right = 0.5f + w / 2 - NO_FACE_ZOOM_PERCENT;
			} else {
				roi.left = 0 + NO_FACE_ZOOM_PERCENT;
				roi.right = 1 - NO_FACE_ZOOM_PERCENT;
				float h = (b.getWidth() * outputAspectRatio) / b.getHeight();
				roi.top = 0.1f + NO_FACE_ZOOM_PERCENT;
				roi.bottom = roi.top + h - NO_FACE_ZOOM_PERCENT;
			}
		} else {
			roi = newROI;
			roi.left /= (float) b.getWidth();
			roi.top /= (float) b.getHeight();
			roi.right /= (float) b.getWidth();
			roi.bottom /= (float) b.getHeight();
		}
		if (draw) {
			newROI.left = roi.left * b.getWidth();
			newROI.top = roi.top * b.getHeight();
			newROI.right = roi.right * b.getWidth();
			newROI.bottom = roi.bottom * b.getHeight();
			p.setColor(Color.BLACK);
			p.setStyle(Paint.Style.FILL);
			p.setAlpha(150);
			c.drawRect(0, 0, newROI.left, b.getHeight(), p);
			c.drawRect(newROI.left, 0, newROI.right, newROI.top, p);
			c.drawRect(newROI.right, 0, b.getWidth(), b.getHeight(), p);
			c.drawRect(newROI.left, newROI.bottom, newROI.right, b.getHeight(), p);
			if (numFaces == 0) {
				p.setStyle(Paint.Style.STROKE);
				p.setAlpha(255);
				point = new PointF(50, 50);
				p.setColor(Color.RED);
				c.drawText("NO FACES", point.x, point.y, p);
			}
			p.setAlpha(255);
			p.setColor(Color.MAGENTA);
			p.setStyle(Paint.Style.STROKE);
			c.drawRect(newROI, p);
		}

		detector = null;
		// TODO scale ROI to 0 1
		// roi.left /= (float) b.getWidth();
		// roi.right /= (float) b.getWidth();
		// roi.top /= (float) b.getHeight();
		// roi.bottom /= (float) b.getHeight();
		b = null;
		if (!new RectF(0, 0, 1, 1).contains(roi)) {
			Log.e(TAG, "ROI out of boiunds");
			roi = null;
		}
		if (numFaces == 0) {
			roi = null;
		}
		return img;

	}
	public RectF getAspectRatioRectangle(RectF rect, float ratio) {
		RectF output = new RectF(rect);
		if (rect.width() < rect.height() * ratio) {
			output.right = output.left + rect.height() * ratio;
			output.bottom = output.top + rect.height();
		} else {
			output.right = output.left + rect.width();
			output.bottom = output.top + rect.width() * (1 / ratio);
		}
		return output;
	}

	public void cancel() {
		super.cancel(true);
		if (outputBitmap != null) {
			outputBitmap.recycle();
		}
	}

	public void setFinishEvent(FinishedFindingFacesEvent e) {
		finishedEvent = e;
	}
}
