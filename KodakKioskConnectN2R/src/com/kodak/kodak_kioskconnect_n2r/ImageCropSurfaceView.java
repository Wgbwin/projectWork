package com.kodak.kodak_kioskconnect_n2r;

import com.example.android.bitmapfun.util.Utils;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.utils.ImageUtil;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ImageCropSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
	protected String TAG = ImageCropSurfaceView.VIEW_LOG_TAG;
	private Context mContext;
	private ImageSelectionDatabase mImageSelectionDatabase = null;
	private TutorialThread _thread;
	private float startX;
	private float startY;
	private float offsetX;
	private float offsetY;
	private double scaleFactor = 1.0;
	private double lastScaleFactor = 1.0;
	private float[] startXs;
	private float[] startYs;
	public int imageX;
	public int imageY;
	int newWidth = 0;
	int newHeight = 0;
	int newX = 0;
	int newY = 0;
	double starteddistance = 0.0;
	private boolean isPinchZoom = false;
	Paint mCropPaint = new Paint();
	Paint mCirclePaint = new Paint();
	int x = 0;
	int y = 0;
	public int width = 0;
	public int height = 0;
	float canvasWidth;
	float canvasHeight;
	public RectF rect = null;
	public RectF rect2 = null;
	public Bitmap img = null;
	BitmapFactory.Options options = new Options();
	Bitmap bit = null;
	Bitmap scaledBitmap = null;
	Bitmap rotated = null;
	Bitmap temp = null;
	ROI roi = new ROI();
	ROI origRoi = new ROI();
	//ROI newRoi = new ROI();
	boolean showCropBox;
	int effect;
	Bitmap corner;
	Bitmap lowRes;
	boolean rotate = false;
	double defaultScale = 1.0;
	boolean showLowRes = false;
	public boolean needRefresh = false;
	
	public ImageCropSurfaceView(Context context)
	{
		super(context);
		mContext = context;
		getHolder().addCallback(this);
		_thread = new TutorialThread(getHolder(), this);
		mImageSelectionDatabase = new ImageSelectionDatabase(mContext);
		mImageSelectionDatabase.open();
		mCropPaint.setStyle(Paint.Style.STROKE);
		mCropPaint.setStrokeWidth(6f);
		mCropPaint.setARGB(200, 118, 207, 224);
		mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mCirclePaint.setStrokeWidth(12f);
		mCirclePaint.setARGB(200, 118, 207, 224);
		startXs = new float[2];
		startYs = new float[2];
		scaleFactor = 1.0;
		corner = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.cropboxcorner);
		lowRes = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.alertsmall);
	}

	public ImageCropSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
		getHolder().addCallback(this);
		_thread = new TutorialThread(getHolder(), this);
		mImageSelectionDatabase = new ImageSelectionDatabase(mContext);
		mImageSelectionDatabase.open();
		mCropPaint.setStyle(Paint.Style.STROKE);
		mCropPaint.setStrokeWidth(6f);
		mCropPaint.setARGB(200, 118, 207, 224);
		mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mCirclePaint.setStrokeWidth(12f);
		mCirclePaint.setARGB(200, 118, 207, 224);
		startXs = new float[2];
		startYs = new float[2];
		scaleFactor = 1.0;
		corner = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.cropboxcorner);
		lowRes = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.alertsmall);
	}

	public void InterruptThread()
	{
		_thread.interrupt();
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		double scaleHeight = 0;
		double scaleWidth = 0;
		boolean landscape = false;
		int origW = 0;
		int origH = 0;
		int downsample = 0;
		// get the height and width of the canvas so we know how large to make the image
		try
		{
			canvasWidth = canvas.getWidth();
			canvasHeight = canvas.getHeight();
			canvas.drawColor(Color.BLACK);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		try
		{

			// if the image is null, we need to render it
			if (img == null)
			{
				Log.w(TAG, "img is null.");
				options.inJustDecodeBounds = true;
				//BitmapFactory.decodeFile(PrintHelper.selectedImage.filename, options);
				PhotoInfo photo = PrintHelper.selectedImage.photoInfo;
				if(photo.getPhotoSource().isFromPhone()){
					bit = PrintHelper.loadThumbnailImage(PrintHelper.selectedImage.photoInfo.getLocalUri(), MediaStore.Images.Thumbnails.MINI_KIND, options, mContext);
					origW = options.outWidth;
					origH = options.outHeight;
				} else {
					bit = ImageUtil.getBitmapOfPhotoInfo(photo, mContext);
					origW = photo.getWidth();
					origH = photo.getHeight();
				}
				
				int temp = 0;
				loadEditInfo(PrintHelper.selectedImage);
				
				ExifInterface exif = null;
				if(photo.getPhotoSource().isFromPhone()){
					exif = new ExifInterface(PrintHelper.selectedImage.photoInfo.getPhotoPath());
				
					if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90)
					{
						temp = origW;
						origW = origH;
						origH = temp;
					
					}
					else if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270)
					{
						temp = origW;
						origW = origH;
						origH = temp;
					}
				}
				// double ratio = (float) options.outWidth / (float) options.outHeight;
				if (origW > origH)
				{
					landscape = true;
				}
				else
				{
					landscape = false;
				}
				options.inJustDecodeBounds = false;
				// figure out how much we need to downsample the original image to fit in our canvas
				if (landscape)
				{
					downsample = (int) Math.ceil((origH * 1.0) / (canvasHeight * 1.0));
				}
				else
				{
					downsample = (int) Math.ceil((origW * 1.0) / (canvasWidth * 1.0));
				}
				options.inSampleSize = downsample;
				/*//add by song for PNG file
				String uri = PrintHelper.selectedImage.uri;
				String loadFileName = Utils.getFilePath(PrintHelper.selectedImage.uri, mContext); 
				if (loadFileName.toUpperCase().endsWith(".PNG")){
					bit = PrintHelper.getThumbOfPNG(uri);	
					
				}else{*/
				if(photo.getPhotoSource().isFromPhone()){
					bit = PrintHelper.loadThumbnailImage(PrintHelper.selectedImage.photoInfo.getLocalUri(), MediaStore.Images.Thumbnails.MINI_KIND, options, mContext);
				}
				/*}*/

				if(exif != null){
					if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90)
					{
						Matrix matrix = new Matrix();
						matrix.postRotate(90);
						rotated = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
					}
					else if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270)
					{
						Matrix matrix = new Matrix();
						matrix.postRotate(270);
						rotated = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
					}else if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_180){
						Matrix matrix = new Matrix();
						matrix.postRotate(180);
						rotated = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
					}
				}
				if(rotated != null)
				{
					bit = null;
					bit = rotated;
				}
				
				scaleHeight = (canvasHeight * 1.0) / (bit.getHeight() * 1.0);
				scaleWidth = (canvasWidth * 1.0) / (bit.getWidth() * 1.0);
				// scale the downsampled image in order to fill the entire width for landscape or height for a portrait image
				if (scaleHeight < scaleWidth)
				{
					img = Bitmap.createScaledBitmap(bit, (int) (bit.getWidth() * scaleHeight), (int) (bit.getHeight() * scaleHeight), true);
				}
				else
				{
					img = Bitmap.createScaledBitmap(bit, (int) (bit.getWidth() * scaleWidth), (int) (bit.getHeight() * scaleWidth), true);
				}
				bit.recycle();
				
				// Figure out our offset for the rendered image
				imageX = (int) ((canvasWidth - img.getWidth()) / 2);
				imageY = (int) ((canvasHeight - img.getHeight()) / 2);
				roi = PrintHelper.selectedImage.roi;
				
				/*if(roi == null)
				{
					Log.e(TAG, "Error, roi is null!");
				}
				else
				{
					width = (int) (img.getWidth() * roi.w);
					height = (int) (img.getHeight() * roi.h);
				}*/
				if(width ==0 || height == 0)
				{
					Log.w(TAG, "width or height is 0");
					double productWidth = Double.parseDouble(PrintHelper.selectedImage.width);
					double productHeight = Double.parseDouble(PrintHelper.selectedImage.height);
					double ratio = 1.0;
					if(productWidth > productHeight)
					{
						ratio = productWidth/productHeight;
					}
					else
					{
						ratio = productHeight/productWidth;
					}
					
					ROI tempRoi = PrintHelper.CalculateDefaultRoi(img.getWidth(), img.getHeight(), ratio);
					tempRoi.x = tempRoi.x/img.getWidth();
					tempRoi.y = tempRoi.y/img.getHeight();
					tempRoi.w = tempRoi.w/img.getWidth();
					tempRoi.h = tempRoi.h/img.getHeight();
					
					roi = tempRoi;
					PrintHelper.selectedImage.roi = roi;
					width = (int) (img.getWidth() * roi.w);
					height = (int) (img.getHeight() * roi.h);
				}
				
				int x = (int) (( roi.x) * img.getWidth());
				int y = (int) (( roi.y) * img.getHeight());
				int h = (int) (( roi.h) * img.getHeight());
				int w = (int) (( roi.w) * img.getWidth());
				
				rect = new RectF(x + imageX, y + imageY, w + x + imageX, (int) h + y + imageY);

				Log.d(TAG,"img.width:"+img.getWidth()+" img.height:"+img.getHeight());
				Log.d(TAG,"height: "+height+" width:"+width);
				Log.d(TAG,"newH: "+newHeight+" newW:"+newWidth);
				Log.d(TAG,"imageX:"+imageX+" imageY:"+imageY);
				Log.d(TAG, "x: "+x+" y: "+y+" h: "+h+" w: "+w);
				Log.d(TAG,"rectLeft: "+rect.left+" rectTop: "+rect.top+" rectHeight: "+rect.height()+" rectWidth: "+rect.width());
			}
			
			if(!showCropBox)
			{				
				Log.w(TAG, "not showCropBox");
				int x = (int) (( roi.x) * img.getWidth());
				int y = (int) (( roi.y) * img.getHeight());
				int h = (int) (( roi.h) * img.getHeight());
				int w = (int) (( roi.w) * img.getWidth());
				//Log.d(TAG, "x: "+x+" y: "+y+" h: "+h+" w: "+w);
				bit = Bitmap.createBitmap(img, x, y, w, h);
				
				scaleHeight = (canvasHeight * 1.0) / (bit.getHeight() * 1.0);
				scaleWidth = (canvasWidth * 1.0) / (bit.getWidth() * 1.0);
				// scale the downsampled image in order to fill the entire width for landscape or height for a portrait image
				if (scaleHeight < scaleWidth)
				{
					scaledBitmap = Bitmap.createScaledBitmap(bit, (int) (bit.getWidth() * scaleHeight), (int) (bit.getHeight() * scaleHeight), true);
				}
				else
				{
					scaledBitmap = Bitmap.createScaledBitmap(bit, (int) (bit.getWidth() * scaleWidth), (int) (bit.getHeight() * scaleWidth), true);
				}
				bit.recycle();
				
				imageX = (int) ((canvasWidth - scaledBitmap.getWidth()) / 2);
				imageY = (int) ((canvasHeight - scaledBitmap.getHeight()) / 2);
				/*try
				{
				switch(effect)
				{
					case 0:
						break;
					case 1:
						bit = PrintHelper.doGreyscale(bit);
						break;
					case 2:
						bit = PrintHelper.createSepiaToningEffect(bit, 50, 200, 200, 200);
						break;
					}
				}
				catch(Exception ex)
				{
					
				}*/
				canvas.drawBitmap(scaledBitmap, imageX,imageY, null);
				
			}
			else
			{
				try
				{
					imageX = (int) ((canvasWidth - img.getWidth()) / 2);
					imageY = (int) ((canvasHeight - img.getHeight()) / 2);
					/*try
					{
					switch(effect)
					{
						case 0:
							break;
						case 1:
							img = PrintHelper.createSepiaToningEffect(img, 0, 200, 200, 200);
							break;
						case 2:
							img = PrintHelper.doGreyscale(img);
							break;
						}
					}
					catch(Exception ex)
					{
						
					}*/
					/*
					int x = (int) (( roi.x) * img.getWidth());
					int y = (int) (( roi.y) * img.getHeight());
					int h = (int) (( roi.h) * img.getHeight());
					int w = (int) (( roi.w) * img.getWidth());
					
					Log.d(TAG, "x: "+x+" y: "+y+" h: "+h+" w: "+w);
					Log.d(TAG,"rectLeft: "+rect.left+" rectTop: "+rect.top+" rectHeight: "+rect.height()+" rectWidth: "+rect.width());
					*/
					canvas.drawBitmap(img, imageX,imageY, null);
					
					if(rotate)
					{
						Log.w(TAG, "showCropBox rotate");
						if(newWidth == 0 || newHeight == 0)
						{
							newWidth = width;
							newHeight = height;
						}
						
						int temp = newWidth;
						newWidth = newHeight;
						newHeight = temp;
						
						temp = width;
						width = height;
						height = temp; 
						rotate = false;
						
						rect.right = newWidth+rect.left;
						rect.bottom = newHeight+rect.top;
						
						
						//lastScaleFactor = 1.0;
						rotateCheckBounds();
						checkBounds();
					}
					
					canvas.drawRect(rect, mCropPaint);
					
					if(corner == null)
					{
						corner = BitmapFactory.decodeResource(mContext.getResources(),
				                R.drawable.cropboxcorner);
					}
					
					canvas.drawBitmap(corner,(int)(rect.left-(corner.getWidth()/2)),(int)(rect.top-(corner.getHeight()/2)),null);
					canvas.drawBitmap(corner,(int)(rect.left-(corner.getWidth()/2)),(int)(rect.bottom-(corner.getHeight()/2)),null);
					canvas.drawBitmap(corner,(int)(rect.right-(corner.getWidth()/2)),(int)(rect.top-(corner.getHeight()/2)),null);
					canvas.drawBitmap(corner,(int)(rect.right-(corner.getWidth()/2)),(int)(rect.bottom-(corner.getHeight()/2)),null);
					
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			
			// refresh the low res warning icon after change print size
			if(needRefresh){
				double width = img.getWidth();
				double height = img.getHeight();
				double w = (rect.width() / width);
				double h =  (rect.height() / height);
				showLowRes = PrintHelper.isLowResWarning(PrintHelper.selectedImage, w, h);
				needRefresh = false;
			}
			
			if(showLowRes)
			{
				canvas.drawBitmap(lowRes, 0,canvas.getHeight()-lowRes.getHeight(), null);
			}
			

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		try{
		if (showLowRes) {
			if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP
					&& event.getPointerCount() <= 1) {
				int pointerIndexNow = ((event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
				int pointerIdNow = event.getPointerId(pointerIndexNow);
				startX = event.getX(pointerIdNow);
				startY = event.getY(pointerIdNow);

				if (startX >= 0 && startX < lowRes.getWidth() * 3
						&& startY >= canvasHeight - lowRes.getHeight() * 2
						&& startY < canvasHeight) {
					Log.d(TAG, " *Low Res Icon was clicked!* ");
					
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(mContext);
					builder.setTitle(mContext.getString(R.string.lowResWarning));
					builder.setMessage("");
					builder.setPositiveButton(mContext.getString(R.string.OK), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.setNegativeButton("", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.create().show();
					
					return true;
				}
				else {
					// check other cases
				}

			}
		}
		if (!showCropBox)
		{
			return false;
		}
		int pointerIndex = ((event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
		int pointerId = event.getPointerId(pointerIndex);
		int action = (event.getAction() & MotionEvent.ACTION_MASK);
		int pointCnt = event.getPointerCount();
		if (pointCnt > 1)
			isPinchZoom = true;
		else
			isPinchZoom = false;
		if (!isPinchZoom)
		{
			isPinchZoom = false;
			pointerId = event.getPointerId(0);
			switch (action)
			{
			case MotionEvent.ACTION_DOWN:
				startX = event.getX(pointerId);
				startY = event.getY(pointerId);
				break;
			case MotionEvent.ACTION_MOVE:
				try
				{
					offsetX = event.getX(pointerId) - startX;
					offsetY = event.getY(pointerId) - startY;
					startX = event.getX(pointerId);
					startY = event.getY(pointerId);
					newWidth = (int) (width * scaleFactor);
					newHeight = (int) (height * scaleFactor);
					rect.left = rect.left + offsetX;
					rect.top = rect.top + offsetY;
					rect.right = rect.right + offsetX;
					rect.bottom = rect.bottom + offsetY;
					checkBounds();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				break;
			case MotionEvent.ACTION_UP:
				offsetY = 0;
				offsetX = 0;
				break;
			default:
				break;
			}
		}
		else
		{
			isPinchZoom = true;
			switch (action)
			{
			case MotionEvent.ACTION_MOVE:
				try
				{
					float x1 = event.getX(event.getPointerId(0));
					float x2 = event.getX(event.getPointerId(1));
					float y1 = event.getY(event.getPointerId(0));
					float y2 = event.getY(event.getPointerId(1));
					double currentDistance = Math.abs(Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)));
					scaleFactor = currentDistance / starteddistance;
					scaleFactor = lastScaleFactor * scaleFactor;
					//Log.d("scaleFactor", scaleFactor + "");
					//Log.d("currentDistance", currentDistance + "");
					//Log.d("startDistance", starteddistance + "");
					if (((int) (width * Math.abs(scaleFactor)) < img.getWidth()) && (((int) (height * Math.abs(scaleFactor))) < img.getHeight()))
					{
						newWidth = (int) (width * Math.abs(scaleFactor));
						newHeight = (int) (height * Math.abs(scaleFactor));
						defaultScale = scaleFactor;
					}
					else
					{						
						scaleFactor = defaultScale;
						lastScaleFactor = defaultScale;
					}
					rect.left = rect.left + (rect.width() - newWidth) / 2;
					rect.top = rect.top + (rect.height() - newHeight) / 2;
					rect.right = rect.right - (rect.width() - newWidth) / 2;
					rect.bottom = rect.bottom - (rect.height() - newHeight) / 2;
					rotateCheckBounds();
					checkBounds();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_1_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
			{
				lastScaleFactor = scaleFactor;
				defaultScale = scaleFactor;
				double width = img.getWidth();
				double height = img.getHeight();
				double w = (rect.width() / width);
				double h =  (rect.height() / height);
				if(PrintHelper.isLowResWarning(PrintHelper.selectedImage,w,h))
				{
					showLowRes = true;
				}
				else
				{
					showLowRes = false;
				}
				break;
			}
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_1_DOWN:
			case MotionEvent.ACTION_POINTER_2_DOWN:
				startXs[0] = event.getX(event.getPointerId(0));
				startYs[0] = event.getY(event.getPointerId(0));
				startXs[1] = event.getX(event.getPointerId(1));
				startYs[1] = event.getY(event.getPointerId(1));
				starteddistance = Math.abs(Math.sqrt((startXs[1] - startXs[0]) * (startXs[1] - startXs[0]) + (startYs[1] - startYs[0]) * (startYs[1] - startYs[0])));
				Log.d("Tag", starteddistance + "");
			default:
				break;
			}
		}
		} catch(IllegalArgumentException e){
			Log.e(TAG, "IllegalArgumentException error...." );
		}
		return true; // processed
	}

	public void rotateCheckBounds()
	{
		double diff = 0.0;
		double perc = 1.0;
		
		if (newWidth > img.getWidth())
		{
			diff = newWidth - img.getWidth();
			perc = 1.0-(diff / newWidth);
			rect.right *= perc;
			rect.bottom *= perc;
			rect.top *= perc;
			rect.left *= perc;
			newWidth *= perc;
			newHeight *= perc;
			scaleFactor *= perc;
			defaultScale *= perc;
			//Log.e(TAG, "newWidth[rect{left:" + rect.left + ",top:" + rect.top + ",right:" + rect.right + ",bottom:" + rect.bottom
			//		+ "}, newWidth:" + newWidth + ", newHeight:" + newHeight + ", scaleFactor:" + scaleFactor + "]");
		}
		
		if (newHeight > img.getHeight())
		{
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
			//Log.e(TAG, "newHeight[rect{left:" + rect.left + ",top:" + rect.top + ",right:" + rect.right + ",bottom:" + rect.bottom
			//		+ "}, newWidth:" + newWidth + ", newHeight:" + newHeight + ", scaleFactor:" + scaleFactor + "]");
		} 
		//Log.w(TAG, "rotateCheckBounds[rect{left:" + rect.left + ",top:" + rect.top + ",right:" + rect.right + ",bottom:" + rect.bottom
		//		+ "}, newWidth:" + newWidth + ", newHeight:" + newHeight + ", scaleFactor:" + scaleFactor + "]");
		
	}

	public void checkBounds()
	{
		if (rect.left < imageX)
		{
			rect.left = imageX;
			rect.right = rect.left + newWidth;
			//Log.e(TAG, "left....[rect{left:" + rect.left + ",top:" + rect.top + ",right:" + rect.right + ",bottom:" + rect.bottom + "}]");
		}
		if (rect.right > (imageX + img.getWidth()))
		{
			rect.right = (imageX + img.getWidth());
			rect.left = (imageX + img.getWidth()) - newWidth;
			//Log.e(TAG, "right....[rect{left:" + rect.left + ",top:" + rect.top + ",right:" + rect.right + ",bottom:" + rect.bottom + "}]");
		}
		if (rect.top < imageY)
		{
			rect.top = imageY;
			rect.bottom = newHeight + imageY;
			//Log.e(TAG, "top....[rect{left:" + rect.left + ",top:" + rect.top + ",right:" + rect.right + ",bottom:" + rect.bottom + "}]");
		}
		if (rect.bottom > (img.getHeight() + imageY))
		{
			rect.bottom = img.getHeight()+ imageY;
			rect.top = rect.bottom - newHeight;// + imageY;
			//Log.e(TAG, "bottom....[rect{left:" + rect.left + ",top:" + rect.top + ",right:" + rect.right + ",bottom:" + rect.bottom + "}]");
		}
		//Log.w(TAG, "checkBounds[rect{left:" + rect.left + ",top:" + rect.top + ",right:" + rect.right + ",bottom:" + rect.bottom 
		//		+ "}, imageX:" + imageX + ", imageY:" + imageY + "]");
//
//		double diff = 0.0;
//		double perc = 1.0;
//		
//		if (newWidth > img.getWidth())
//		{
//			diff = rect.right - (rect.left + newWidth);
//			perc = diff / rect.right;
//			rect.right *= perc;
//			rect.left *= perc;
//			newWidth *= perc;
//			newHeight *= perc;
//		}
//		
//		if (newHeight > img.getHeight())
//		{
//			diff = rect.bottom - (rect.top + newHeight);
//			perc = diff / rect.right;
//			rect.bottom *= perc;
//			rect.top *= perc;
//			newWidth *= perc;
//			newHeight *= perc;
//		} 
//		
//		if (newWidth > 0 && newHeight > 0)
//		{
//			newRoi.x = (rect.left - imageX) / newWidth;
//			newRoi.y = (rect.top) / newHeight;
//			newRoi.w = (rect.right - rect.left) / newWidth;
//			newRoi.h = (rect.bottom - rect.top) / newHeight;
//		}
//		else
//		{
//			newRoi.x = (rect.left - imageX) / newWidth;
//			newRoi.y = (rect.top) / newHeight;
//			newRoi.w = (rect.right - rect.left) / newWidth;
//			newRoi.h = (rect.bottom - rect.top) / newHeight;
//		}
		
	}
	
	private void loadEditInfo(ProductInfo item){
		width = item.imgWidth;
		height = item.imgHeight;
		newWidth = item.newWidth;
		newHeight = item.newHeight;
		scaleFactor = item.scaleFactor;
		lastScaleFactor = item.lastScaleFactor;
		defaultScale = item.defaultScaleFactor;
		Log.w(TAG, "loadEditInfo[newWidth:" + newWidth 
				+ ", newHeight:" + newHeight
				+ ", scaleFactor:" + scaleFactor
				+ ", lastScaleFactor:" + lastScaleFactor
				+ ", defaultScaleFactor:" + defaultScale
				+ ", width:" + width 
				+ ", height:" + height + "]");
	}
	
	private void storeEditInfo(ProductInfo item){
		item.imgWidth = width;
		item.imgHeight = height;
		item.newWidth = newWidth;
		item.newHeight = newHeight;
		item.scaleFactor = scaleFactor;
		item.lastScaleFactor = lastScaleFactor;
		item.defaultScaleFactor = defaultScale;
		Log.w(TAG, "storeEditInfo[newWidth:" + item.newWidth 
				+ ", newHeight:" + item.newHeight
				+ ", scaleFactor:" + item.scaleFactor
				+ ", lastScaleFactor:" + item.lastScaleFactor
				+ ", defaultScaleFactor:" + item.defaultScaleFactor
				+ ", width:" + width 
				+ ", height:" + height + "]");
	}

	public ROI getROI()
	{
		return roi;
	}

	public void saveROI()
	{
		if (!showCropBox)
			return;
		double width = img.getWidth();
		double height = img.getHeight();
		roi.x = (rect.left - imageX) / width;
		roi.y = (rect.top - imageY) / height;
		roi.w = rect.width() / width;
		roi.h = rect.height() / height;
		Log.w(TAG, "saveROI roi[x:" + roi.x + ", y:" + roi.y + ", h:" + roi.h + ", w:" + roi.w + "]");
	/*	roi.x = newRoi.x;
		roi.y = newRoi.y;
		roi.w = newRoi.w;
		roi.h = newRoi.h;*/
		//mImageSelectionDatabase.setProductCrop(PrintHelper.selectedImage.uri, roi.x, roi.y, roi.w, roi.h);
		PrintHelper.selectedImage.roi.x = roi.x;
		PrintHelper.selectedImage.roi.y = roi.y;
		PrintHelper.selectedImage.roi.w = roi.w;
		PrintHelper.selectedImage.roi.h = roi.h;
		
		storeEditInfo(PrintHelper.selectedImage);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if (!_thread.isAlive())
		{
			_thread = new TutorialThread(getHolder(), ImageCropSurfaceView.this);
		}
		_thread.setRunning(true);
		_thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		boolean retry = true;
		_thread.setRunning(false);
		while (retry)
		{
			try
			{
				_thread.join();
				retry = false;
			}
			catch (InterruptedException e)
			{
				// we will try it again and again...
			}
		}
		mImageSelectionDatabase.close();
	}
}

class TutorialThread extends Thread
{
	private SurfaceHolder _surfaceHolder;
	private ImageCropSurfaceView _panel;
	private boolean _run = false;

	public TutorialThread(SurfaceHolder surfaceHolder, ImageCropSurfaceView panel)
	{
		_surfaceHolder = surfaceHolder;
		_panel = panel;
	}

	public void setRunning(boolean run)
	{
		_run = run;
	}

	@Override
	public void run()
	{
		Canvas c;
		while (_run)
		{
			c = null;
			try
			{
				c = _surfaceHolder.lockCanvas(null);
				synchronized (_surfaceHolder)
				{
					_panel.onDraw(c);
				}
			}
			finally
			{
				// do this in a finally so that if an exception is thrown
				// during the above, we don't leave the Surface in an
				// inconsistent state
				if (c != null)
				{
					_surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}
}