package com.kodak.rss.tablet.view;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.util.FileDownloader;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.thread.LoadPrintBitmapTask;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

/**
 * Purpose: Interception of a range of images 
 * Author: Bing Wang 
 * Created Time:Sep 2, 2013 9:10:13 AM
 */
public class ImageEditView extends View {

	String TAG = "ImageEditView";
	public int LT, TP;
	private Context mContext;
	Bitmap lowRes;
	CropRectView cropRect;
	private Bitmap contentBitmap, initBitmap;
	ImageInfo imageInfo;

	public double maxCanvasHeight;
	public double maxCanvasWidth;

	public double canvasHeight;
	public double canvasWidth;

	public double picCanvasHeight;
	public double picCanvasWidth;

	public int[] category;
	int mGap = 12;
	private double lowWarningGap = 1.0f;

	public boolean isShowLowRes = false;
	private boolean isDrawCrop = true;
	public ProductInfo productInfo;

	public ProgressBar progressBar;	
	private LoadPrintBitmapTask loadTask;

	public ImageEditView(Context context) {
		super(context);
		init(context);
	}

	public ImageEditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		lowRes = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.alertsmall);
		mGap = (int) (context.getResources().getDisplayMetrics().density * mGap);
		cropRect = new CropRectView(mContext);
		cropRect.mGap = mGap;
		category = new int[2];
		lowResHeight = lowRes.getHeight();
	}

	public void setImageInfo(ImageInfo imageInfo) {
		if (imageInfo == null) return;
		if (imageInfo.id == null) return;		
		if (this.imageInfo == null) {
			this.imageInfo = imageInfo;
			this.initBitmap = null;
			return;
		}
		if (imageInfo.editUrl == null || this.imageInfo.editUrl == null) {
			this.imageInfo = imageInfo;
			this.initBitmap = null;	
			return;
		}		
		if(!(this.imageInfo.id.equals(imageInfo.id) && this.imageInfo.editUrl.equals(imageInfo.editUrl))) {			
			this.imageInfo = imageInfo;
			this.initBitmap = null;			
		}	
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (imageInfo == null  )return;
		if (productInfo == null )return;
		canvasHeight = getHeight();

		drawContent(canvas);

		if (isDrawCrop && initBitmap != null) {
			cropRect.draw(canvas);
		}

		if (isShowLowRes) {
			drawLowRes(canvas);
		}
		replyCropRectContent();
	}

	private void drawContent(Canvas canvas) {
		loadBitmap();
		setLocationInCanvas();
		if (contentBitmap != null && !contentBitmap.isRecycled()) {			
			canvas.drawBitmap(contentBitmap, LT, TP, null);
		}
		recycle();
	}

	private void drawLowRes(Canvas canvas) {
		double initlastLowResHeight = picCanvasHeight + TP + lowResHeight*0.6f;
		double lastLowResHeight = picCanvasHeight + TP + lowResHeight;		
		if (canvasHeight <= lastLowResHeight+lowResHeight*0.5f) {
			canvas.drawBitmap(lowRes, (float) (picCanvasWidth + LT),(float) (lastLowResHeight - lowResHeight), null);
		}else {
			canvas.drawBitmap(lowRes, (float) (picCanvasWidth + LT),(float) (initlastLowResHeight - lowResHeight), null);	
		}
	}
	
	public Handler loadHandler  = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg == null) return;		
			Bundle data = msg.getData();
			if (data == null) return;	
			
			if (loadTask != null) {
				loadTask.interrupt();
				loadTask = null;
			}
			
			String photoId = data.getString(LoadPrintBitmapTask.PHOTOID);	
			if (photoId == null) return;
			if (imageInfo == null  )return;
			if (productInfo == null )return;			
			if (!photoId.equals(imageInfo.id)) return;	
			
			landscape = data.getBoolean(LoadPrintBitmapTask.LANDSCAPE);
			wHratio = data.getDouble(LoadPrintBitmapTask.WHRATIO);
			initBitmap = (Bitmap) data.get(LoadPrintBitmapTask.BITMAP);
			
			if (wHratio <= 0) return;	
			if (initBitmap == null) return;	
			if (initBitmap.isRecycled()) return;	
			
			picCanvasWidth = picCanvasHeight * wHratio;				
			loadEditInfo(picCanvasWidth, picCanvasHeight);				
			imgW = picCanvasWidth;
			imgH = picCanvasHeight;
			postInvalidate();
		}
	};
	
	public boolean isChangeImage = false;
	double imgW;
	double imgH;
	public int width = 0;
	public int height = 0;
	private int lowResHeight;
	private boolean landscape = true;
	private double wHratio = 0;
	
	private void loadBitmap() {
		try {
			canvasWidth = canvasHeight * (maxCanvasWidth / maxCanvasHeight);
			lowWarningGap = 1.0;
			lowWarningGap = 3 * canvasHeight / maxCanvasHeight;
			lowWarningGap = lowWarningGap > 1.5 ? lowWarningGap : 1.5;

			picCanvasHeight = canvasHeight - lowResHeight * lowWarningGap - cropRect.cornerHeight;

			if (wHratio > 0) {			
				picCanvasWidth = picCanvasHeight * wHratio;	
				double maxCWidth = maxCanvasWidth - lowRes.getWidth() - cropRect.cornerWidth;
				if (picCanvasWidth > maxCWidth) {
					picCanvasWidth = maxCWidth;
					picCanvasHeight = picCanvasWidth/wHratio;
				}				
			}
			if (picCanvasHeight == 0) return;

			if ((initBitmap == null || isChangeImage) && imageInfo.editUrl != null ) {
				if (initBitmap != null && !initBitmap.isRecycled()) {
					initBitmap.recycle();
				}
				initBitmap = null;
				isChangeImage = false;	
				
				if (loadTask != null && !loadTask.getPhotoId().equals(imageInfo.id)){
					loadTask.interrupt();
					loadTask = null;
				}
				
				if (loadTask == null) {
					loadTask = new LoadPrintBitmapTask(loadHandler, imageInfo.id, imageInfo.editUrl, picCanvasHeight);
					loadTask.start();
				}
						
			}
			
			if (category[0] != productInfo.pageWidth || category[1] != productInfo.pageHeight) {
				loadEditInfo(picCanvasWidth, picCanvasHeight);
			}					
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void loadEditInfo(double initWidth, double initHeight) {
		if (initBitmap == null) return;
		if (productInfo == null) return;
		parseRate(productInfo);		
		double productWidth = category[0];
		double productHeight = category[1];				
		if (productWidth > productHeight) {				
			ratio = productWidth / productHeight;			
		} else {				
			ratio = productHeight / productWidth;			
		}
		if (initWidth > initHeight) {
			cropRect.categoryRatio = ratio;
			cropRect.minWidth = cropRect.minHeight * ratio;			
		}else {
			cropRect.categoryRatio = 1.0f/ratio;
			cropRect.minWidth = cropRect.minHeight / ratio;
		}
		
		if (productInfo.roi != null) {
			double x = (productInfo.roi.x) * initWidth;
			double y = (productInfo.roi.y) * initHeight;
			double h = (productInfo.roi.h) * initHeight;
			double w = (productInfo.roi.w) * initWidth;

			cropRect.left = x;
			cropRect.top = y;
			cropRect.right = w + x;
			cropRect.bottom = h + y;
			
			if (cropRect.left < 0 || cropRect.top < 0 ||(cropRect.right > initWidth || cropRect.bottom > initHeight)) {
				initCropRectValue(initWidth, initHeight);
			}	
		} else {
			initCropRectValue(initWidth, initHeight);
		}
	}	
		
	private double ratio = 1.0;
	private void initCropRectValue(double initWidth, double initHeight) {		
		ROI tempRoi = ImageUtil.calculateDefaultRoi(initWidth, initHeight,ratio);				
		cropRect.left = tempRoi.x;
		cropRect.top = tempRoi.y;
		cropRect.right = tempRoi.w + tempRoi.x;
		cropRect.bottom = tempRoi.h + tempRoi.y;
		
		tempRoi.x = tempRoi.x / initWidth;
		tempRoi.y = tempRoi.y / initHeight;
		tempRoi.w = tempRoi.w / initWidth;
		tempRoi.h = tempRoi.h / initHeight;

		productInfo.roi = tempRoi;
	}

	private void setLocationInCanvas() {
		if (picCanvasHeight == 0 || picCanvasWidth == 0)return;
		if (initBitmap != null && !initBitmap.isRecycled()) {

			contentBitmap = Bitmap.createScaledBitmap(initBitmap,(int) picCanvasWidth, (int) picCanvasHeight, true);			

			LT = (int) ((maxCanvasWidth - picCanvasWidth - lowRes.getWidth()) / 2);
						
			if (landscape) {			
				TP = (int) (cropRect.cornerHeight + lowResHeight * 0.5);
			}else {
				TP = cropRect.cornerHeight;
			}			

			setCropRectContent();

			imgW = picCanvasWidth;
			imgH = picCanvasHeight;		
			double w = (cropRect.right - cropRect.left) / picCanvasWidth;
			double h =  (cropRect.bottom - cropRect.top) / picCanvasHeight;			
			if (imageInfo.editUrl != null && ImageUtil.isLowResWarning(imageInfo, category,w,h)) {
				isShowLowRes = true;
			} else {
				isShowLowRes = false;
			}
		}
	}
	
	private void setCropRectContent() {
		double wRatio = picCanvasWidth / imgW;
		double hRatio = picCanvasHeight / imgH;
		cropRect.picCanvasWidth = picCanvasWidth;
		cropRect.picCanvasHeight = picCanvasHeight;
		cropRect.LT = LT;
		cropRect.TP = TP;
		cropRect.left = cropRect.left * wRatio + LT;
		cropRect.right = cropRect.right * wRatio + LT;
		cropRect.top = cropRect.top * hRatio + TP;
		cropRect.bottom = cropRect.bottom * hRatio + TP;		
	}

	private void recycle() {
		if (contentBitmap != null && !contentBitmap.isRecycled())
			contentBitmap.recycle();
	}
	
	public void recycleInFinish() {
		if (initBitmap != null && !initBitmap.isRecycled()) initBitmap.recycle();
		if (lowRes != null && !lowRes.isRecycled())lowRes.recycle();
	}	

	private void replyCropRectContent() {
		if (initBitmap == null) return;
		cropRect.left = cropRect.left - LT;
		cropRect.right = cropRect.right - LT;
		cropRect.top = cropRect.top - TP;
		cropRect.bottom = cropRect.bottom - TP;

		saveEditInfo();
	}

	private void saveEditInfo() {		
		if (productInfo != null && picCanvasWidth > 0 && picCanvasHeight >0) {
			if (productInfo.roi == null) {
				productInfo.roi = new ROI();
			}
			
			double rectW = cropRect.right - cropRect.left;	
			double rectH = cropRect.bottom - cropRect.top;

			if (rectW < cropRect.minWidth) {													
				cropRect.right = cropRect.left + cropRect.minWidth;							
				rectW = cropRect.minWidth;															
			}
			if (rectW > picCanvasWidth) {	
				cropRect.left = 0;	
				cropRect.right = picCanvasWidth;							
				rectW = picCanvasWidth;															
			}
			if (rectH < cropRect.minHeight) {																	
				cropRect.bottom = cropRect.top + cropRect.minHeight;								
				rectH = cropRect.minHeight;														
			}
			if (rectH > picCanvasHeight) {	
				cropRect.top = 0;	
				cropRect.bottom = picCanvasHeight;							
				rectH = picCanvasHeight;															
			}
			if (cropRect.left < 0 ) {
				cropRect.left = 0;
				cropRect.right = rectW;				
			}
			if (cropRect.left > picCanvasWidth - cropRect.minWidth) {				
				cropRect.right = picCanvasWidth;
				cropRect.left = picCanvasWidth - rectW;
			}
			if (cropRect.right < 0) {								
				cropRect.left = 0;
				cropRect.right = rectW;				
			}			
			if (cropRect.right > picCanvasWidth) {				
				cropRect.right = picCanvasWidth;
				cropRect.left = picCanvasWidth - rectW;				
			}

			if (cropRect.top < 0) {
				cropRect.top = 0;
				cropRect.bottom = rectH;				
			}
			if (cropRect.top > picCanvasHeight - cropRect.minHeight) {
				cropRect.top = picCanvasHeight - rectH;
				cropRect.bottom = picCanvasHeight;				
			}
			if (cropRect.bottom < 0) {	
				cropRect.top = 0;
				cropRect.bottom = rectH;								
			}
			if (cropRect.bottom > picCanvasHeight) {			
				cropRect.bottom = picCanvasHeight;
				cropRect.top = picCanvasHeight - rectH;
			}

			productInfo.roi.x = (cropRect.left)/picCanvasWidth;
			productInfo.roi.y = (cropRect.top)/picCanvasHeight;
			if (rectW > rectH) {
				productInfo.roi.w = rectH*ratio/picCanvasWidth; 
				productInfo.roi.h = rectH/picCanvasHeight;
				
				if (productInfo.roi.w > 1) {
					productInfo.roi.w = 1;
					cropRect.left = 0;
					rectH = picCanvasWidth/ratio;
					productInfo.roi.h = rectH/picCanvasHeight;;
				}
								
				cropRect.right = rectH*ratio + cropRect.left;
				cropRect.bottom = rectH + cropRect.top;
				
			}else {
				productInfo.roi.w = rectW/picCanvasWidth; 
				productInfo.roi.h = rectW*ratio/picCanvasHeight;
					
				if (productInfo.roi.h > 1) {
					productInfo.roi.h = 1;
					cropRect.top = 0;
					rectW = picCanvasHeight/ratio;
					productInfo.roi.w = rectW/picCanvasWidth;;
				}
				cropRect.right = rectW + cropRect.left;
				cropRect.bottom = rectW*ratio + cropRect.top;
			}												
			Log.w(TAG, "saveROI roi[x:" + productInfo.roi.x + ", y:"+ productInfo.roi.y + ", h:" + productInfo.roi.h + ", w:"+ productInfo.roi.w + "]");
		}
	}

	public void refresh() {
		postInvalidate();
	}	

	private int[] parseRate(ProductInfo productInfo) {		
		category[0] = productInfo.pageWidth;
		category[1] = productInfo.pageHeight;		
		return category;
	}
	
	@SuppressWarnings("deprecation")
	public boolean onTouchEvent(MotionEvent ev) {
		boolean bm = false;
		try {
			if (isShowLowRes) {
				if (ev.getPointerCount() <= 1 && ev.getAction()== MotionEvent.ACTION_DOWN) {					
					int pointerIndexNow = ((ev.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
					int pointerIdNow = ev.getPointerId(pointerIndexNow);
					float onClickX = ev.getX(pointerIdNow);
					float onClickY = ev.getY(pointerIdNow);
									
					double initlastLowResHeight = picCanvasHeight + TP + lowResHeight*0.6f;
					double lastLowResHeight = picCanvasHeight + TP + lowResHeight;							
					if (((picCanvasWidth + LT < onClickX) && (onClickX <= maxCanvasWidth +mGap)) 
							&& ((canvasHeight <= lastLowResHeight+lowResHeight*0.5f) && ((initlastLowResHeight - lowResHeight < onClickY) && (onClickY <= canvasHeight+mGap)) 
									|| (canvasHeight > lastLowResHeight+lowResHeight*0.5f) && ((lastLowResHeight - lowResHeight < onClickY) && (onClickY <= canvasHeight+mGap)))) {
						new InfoDialog.Builder(mContext).setMessage(R.string.low_warning_content)						
						.setNegativeButton(R.string.d_ok, null).create()
						.show();					
						return true;
					}
				}
			}
			bm = cropRect.onTouchEvent(ev);
			if (bm)invalidate();
			return bm;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return bm;
	}

	public void rotate() {
		cropRect.rotate = !cropRect.rotate;
		postInvalidate();
	}

	public void cropDismiss() {
		isDrawCrop = !isDrawCrop;
		postInvalidate();
	}	

	public Handler editHandler;	
	public void editAndDisplayImage(final ImageInfo mImageInfo) {		
		String imageUrl = mImageInfo.imageOriginalResource.fetchPreviewURL();					
		final String saveImagePath = RssTabletApp.getInstance().getTempImageFolderPath() + "/"+mImageInfo.imageOriginalResource.id+ ".jpg";
		FileDownloader.download(true, imageUrl, saveImagePath,new FileDownloader.OnProcessComplete<String>() {
				@Override
				public void onComplete(String result) {															
					if (result != null) {												
						if (saveImagePath.endsWith(".jpg")) {							
							mImageInfo.editUrl = result;														
							List<ProductInfo> products = RssTabletApp.getInstance().products;
							if (products != null) {
								for(ProductInfo pInfo : products) {
									if (pInfo != null && AppConstants.printType.equals(pInfo.productType) && pInfo.chosenImageList!= null) {
										ImageInfo choseIamgeInfo = pInfo.chosenImageList.get(0);
										if (choseIamgeInfo.id.equals(mImageInfo.id)) {												
											pInfo.displayImageUrl = result;																				
										}
									}
								}			
							}
						}
						if (mImageInfo.id.equalsIgnoreCase(imageInfo.id)) {
							Log.e(TAG, "mImageInfo id:"+mImageInfo.id +" imageInfo.id:"+imageInfo.id);
							isChangeImage = true;
							postInvalidate();
						}
						if (editHandler != null) {
							Object[] array = new Object[2];
							array[0] = mImageInfo;						
							editHandler.obtainMessage(2, array).sendToTarget();							
						}
				    }					
			    }
		});
	}

}
