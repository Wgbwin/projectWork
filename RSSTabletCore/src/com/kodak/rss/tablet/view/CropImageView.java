package com.kodak.rss.tablet.view;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.util.ProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;

public class CropImageView extends View {

	String TAG = "CropImageView";
	public float LT, TP;
	private Context mContext;

	CropImageRectView cropRect;
	private Bitmap contentBitmap, initBitmap;
	private Layer layer;
	private ImageInfo imageInfo;
	private ProductLayerLocalInfo layerLocalInfo;
	private Button button;
	
	public double canvasHeight;
	public double canvasWidth;

	public double picCanvasHeight;
	public double picCanvasWidth;
	public DisplayMetrics dm;
	int mGap = 15;	
	public ROI roi;
	
	private Bitmap waitBitmap;
	private boolean isChangeImage = false;
	private String downLoadPhotoPath;
	private boolean isDrawCrop = false;
	
	private ImageUseURIDownloader imageDownloader;
	private final Map<String, Request> pendingRequests = new HashMap<String, Request>();
	private onProcessImageResponseListener onResponseListener = new onProcessImageResponseListener() {		
		@Override
		public void onProcess(Response response, String profileId, View view,int position,String flowTpye,String productId) {			
			if (response == null || pendingRequests == null) return;										
			if (imageInfo == null) {
				downLoadPhotoPath = FilePathConstant.getLoadFilePath(FilePathConstant.bookType, profileId, false);
			}else {
				imageInfo.editUrl = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, profileId, false);
				imageInfo.originalUrl= imageInfo.editUrl;				
				imageInfo.uploadOriginalUrl = imageInfo.editUrl;
			}
			
			if(response.getRequest().getCreateTime() >= layerLocalInfo.getLatestTimeForNeedRefresh()){
				layerLocalInfo.isNeedRefreshForCropImage = false;
				isDrawCrop = true;
				isChangeImage = true;			
			}
			if (view != null) {
				view.postInvalidate();
			}			
		}
	};
	
	public CropImageView(Context context) {
		super(context);
		init(context);
	}

	public CropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		dm = getResources().getDisplayMetrics();
		mGap = (int) (dm.density * mGap);
		cropRect = new CropImageRectView(mContext);
		cropRect.mGap = mGap;
	}
	
	public void setInfo(Layer layer, ROI oldRoi, ProductLayerLocalInfo layerLocalInfo, ImageInfo imageInfo, Button button) {
		this.layer = layer;
		this.roi = oldRoi;		
		this.imageInfo = imageInfo;
		this.button = button;
		this.layerLocalInfo = layerLocalInfo;					
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (imageInfo == null && layer == null)return;
		canvasHeight = getHeight();
		canvasWidth = getWidth();
		
		float buttonWidth = dm.density*40;
		if (button != null ) {
			buttonWidth = buttonWidth + 2*button.getWidth();
		}		
		drawContent(canvas, buttonWidth);
		if (isDrawCrop) {
			cropRect.draw(canvas);
			replyCropRectContent();
		}		
	}

	private void drawContent(Canvas canvas,float buttonWidth) {
		loadBitmap(buttonWidth);
		setLocationInCanvas();
		if (contentBitmap != null && !contentBitmap.isRecycled()) {			
			canvas.drawBitmap(contentBitmap, LT, TP, null);
		}
		recycle();
	}
	
	private void loadBitmap(float buttonWidth) {
		try {
			if (canvasHeight == 0 || canvasWidth == 0) return;			
			if (initBitmap == null || isChangeImage) {
				if (initBitmap != null && !initBitmap.isRecycled()) {
					initBitmap.recycle();
				}
				isChangeImage = false;	
				initBitmap = null;
				Bitmap mBitmap = null;
																
				int downsample = 1;
				BitmapFactory.Options options = new Options();
				options.inJustDecodeBounds = true;	
				
				String photoPath = null;
				if (imageInfo != null) {
					if (imageInfo.isfromNative) {
						if(layerLocalInfo.isUseServerImage && layerLocalInfo.isNeedRefreshForCropImage){
							URI pictureURI = ProductUtil.getURI(layer, (int)canvasWidth,(int)canvasHeight);
							if(pictureURI != null){
								imageDownloader = new ImageUseURIDownloader(mContext,pendingRequests);	
								imageDownloader.setSaveType(FilePathConstant.externalType);		
								imageDownloader.setOnProcessImageResponseListener(onResponseListener);								
								imageDownloader.downloadProfilePicture(imageInfo.id, pictureURI, CropImageView.this, 0, !layerLocalInfo.isNeedRefreshForCropImage,false);
							}
						}else{
							photoPath =imageInfo.editUrl;
						}
					}else {
						if (imageInfo.editUrl == null) {
							imageInfo.editUrl = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, imageInfo.id, false);
							imageInfo.originalUrl= imageInfo.editUrl;				
							imageInfo.uploadOriginalUrl = imageInfo.editUrl;
						}
						photoPath = imageInfo.editUrl;
						if (photoPath == null || (layerLocalInfo.isUseServerImage && layerLocalInfo.isNeedRefreshForCropImage)) {							
							URI pictureURI = ProductUtil.getURI(layer, (int)canvasWidth,(int)canvasHeight);
							if (pictureURI != null){
								imageDownloader = new ImageUseURIDownloader(mContext,pendingRequests);	
								imageDownloader.setSaveType(FilePathConstant.externalType);		
								imageDownloader.setOnProcessImageResponseListener(onResponseListener);								
								imageDownloader.downloadProfilePicture(imageInfo.id, pictureURI, CropImageView.this, 0, !layerLocalInfo.isNeedRefreshForCropImage,false);	
							}												
						}
						
					}
				}else {
					if (downLoadPhotoPath == null) {
						downLoadPhotoPath = FilePathConstant.getLoadFilePath(FilePathConstant.bookType, layer.contentId, false);					
					}					
					photoPath = downLoadPhotoPath;
					if (photoPath == null || (layerLocalInfo.isUseServerImage && layerLocalInfo.isNeedRefreshForCropImage)) {
						URI pictureURI = ProductUtil.getURI(layer, (int)canvasWidth,(int)canvasHeight);
						if (pictureURI != null){
							imageDownloader = new ImageUseURIDownloader(mContext,pendingRequests);	
							imageDownloader.setSaveType(FilePathConstant.bookType);	
							imageDownloader.setOnProcessImageResponseListener(onResponseListener);					
							imageDownloader.downloadProfilePicture(layer.contentId, pictureURI, CropImageView.this, 0, !layerLocalInfo.isNeedRefreshForCropImage, false);
						}							
					}	
				}
				
				if (photoPath == null || (layerLocalInfo.isUseServerImage && layerLocalInfo.isNeedRefreshForCropImage)) {
					isDrawCrop = false;
					if (waitBitmap == null || initBitmap == null || initBitmap.isRecycled()) {
						waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.image_wait234x156);
					}					
					initBitmap = waitBitmap;							
					picCanvasWidth = canvasWidth - buttonWidth; 
					picCanvasHeight = canvasHeight - cropRect.cornerHeight;				
					return;
				}
				isDrawCrop = true;
				BitmapFactory.decodeFile(photoPath , options);
				int origW = options.outWidth;
				int origH = options.outHeight;
				
				int downsampleW = 1;	
				double maxHeight = canvasHeight - cropRect.cornerHeight;
				double maxWidth = canvasWidth - buttonWidth;
				if (origW > origH) {		
					picCanvasWidth = canvasWidth - buttonWidth; 
					picCanvasHeight = picCanvasWidth * origH / origW;
					if (picCanvasHeight > maxHeight) {
						picCanvasHeight = maxHeight;
						picCanvasWidth = maxHeight* origW / origH;
						if (picCanvasWidth > maxWidth) {
							picCanvasWidth = maxWidth;
						}
					}
					downsample = (int) Math.ceil((origH * 1.0)/ (picCanvasHeight * 1.0));
				} else {					
					picCanvasHeight = canvasHeight - cropRect.cornerHeight;
					picCanvasWidth = picCanvasHeight * origW / origH;
					if (picCanvasWidth > maxWidth) {
						picCanvasWidth = maxWidth;
						picCanvasHeight = maxWidth* origH / origW;
						if (picCanvasHeight > maxHeight) {
							picCanvasHeight = maxHeight;
						}
					}					
					downsampleW = (int) Math.ceil((origW * 1.0)/ (picCanvasWidth * 1.0));
					downsample = downsample > downsampleW ? downsample : downsampleW;
				}
			
				options.inJustDecodeBounds = false;
				options.inSampleSize = downsample;
				mBitmap = BitmapFactory.decodeFile(photoPath, options);
				
				if (imageInfo != null && imageInfo.isfromNative) {									
					int rotate = ImageUtil.getDegreesExifOrientation(photoPath);
		    		if(rotate > 0 && mBitmap != null) {   
		    			Bitmap rotateBitmap = ImageUtil.rotateBitmap(mBitmap,rotate);
		                if(rotateBitmap != null) {   
		                	mBitmap.recycle();   
		                	mBitmap = rotateBitmap;   
		                }             
		    		}
				}
				if (mBitmap == null) return;
				int height = mBitmap.getHeight();
				int width = mBitmap.getWidth();
				if (width > height) {		
					picCanvasWidth = canvasWidth - buttonWidth; 
					picCanvasHeight = picCanvasWidth * height / width;
					if (picCanvasHeight > maxHeight) {
						picCanvasHeight = maxHeight;
						picCanvasWidth = maxHeight* width / height;
						if (picCanvasWidth > maxWidth) {
							picCanvasWidth = maxWidth;
						}
					}					
				} else {					
					picCanvasHeight = canvasHeight - cropRect.cornerHeight;
					picCanvasWidth = picCanvasHeight * width / height;
					if (picCanvasWidth > maxWidth) {
						picCanvasWidth = maxWidth;
						picCanvasHeight = maxWidth* height / width;
						if (picCanvasHeight > maxHeight) {
							picCanvasHeight = maxHeight;
						}
					}									
				}
				
				double scaleHeight = (picCanvasHeight * 1.0)/ (height* 1.0);
				double scaleWidth = (picCanvasWidth * 1.0)/ (width* 1.0);
				if (scaleHeight < scaleWidth) {
					initBitmap = Bitmap.createScaledBitmap(mBitmap,(int) (width*scaleHeight),(int) (height*scaleHeight), true);
				} else {
					initBitmap = Bitmap.createScaledBitmap(mBitmap,(int) (width*scaleWidth),(int) (height*scaleWidth), true);
				}
				mBitmap.recycle();

				loadEditInfo(picCanvasWidth, picCanvasHeight);				
			}					
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void setLocationInCanvas() {
		if (picCanvasHeight == 0 || picCanvasWidth == 0) return;
		if (initBitmap != null && !initBitmap.isRecycled()) {
			contentBitmap = Bitmap.createScaledBitmap(initBitmap,(int) picCanvasWidth, (int) picCanvasHeight, true);			
			LT = (float) ((canvasWidth - picCanvasWidth)/2);
			TP = (float) ((canvasHeight - picCanvasHeight)/2);;

			if (!isDrawCrop) return;
			setCropRectContent();
		}
	}

	private void loadEditInfo(double initWidth, double initHeight) {		
		if (roi != null) {
			double x = roi.x * initWidth;
			double y = roi.y * initHeight;
			double h = roi.h * initHeight;
			double w = roi.w * initWidth;
				
			cropRect.left = x;
			cropRect.top = y;
			cropRect.right = w + x;
			cropRect.bottom = h + y;
						
			cropRect.minWidth = cropRect.minHeight;
			
			if ((cropRect.left < 0 || cropRect.top < 0)||( w<=0 && h<= 0)) {
				initCropRectValue(initWidth, initHeight);
			}	
		} else {
			initCropRectValue(initWidth, initHeight);
		}
	}

	private void initCropRectValue(double initWidth, double initHeight) {		
		double ratio = 1.0;		
		ratio = initWidth / initHeight;
		cropRect.minWidth = cropRect.minHeight;
		
		roi = ImageUtil.calculateDefaultRoi(initWidth, initHeight,ratio);		
		
		cropRect.left = roi.x;
		cropRect.top = roi.y;
		cropRect.right = roi.w + roi.x;
		cropRect.bottom = roi.h + roi.y;
		
		roi.x = roi.x / initWidth;
		roi.y = roi.y / initHeight;
		roi.h = roi.h / initHeight;
		roi.w = roi.w / initWidth;	
	}

	private void setCropRectContent() {		
		cropRect.picCanvasWidth = picCanvasWidth;
		cropRect.picCanvasHeight = picCanvasHeight;
		cropRect.LT = LT;
		cropRect.TP = TP;
		cropRect.left = cropRect.left + LT;
		cropRect.right = cropRect.right + LT;
		cropRect.top = cropRect.top + TP;
		cropRect.bottom = cropRect.bottom  + TP;	
	}

	private void recycle() {
		if (contentBitmap != null && !contentBitmap.isRecycled())
			contentBitmap.recycle();
	}
	
	public void recycleInFinish() {
		if (initBitmap != null && !initBitmap.isRecycled()) initBitmap.recycle();		
	}	

	private void replyCropRectContent() {		
		cropRect.left = cropRect.left - LT;
		cropRect.right = cropRect.right - LT;
		cropRect.top = cropRect.top - TP;
		cropRect.bottom = cropRect.bottom - TP;
		saveEditInfo();
	}

	private void saveEditInfo() {
		if (picCanvasHeight == 0 || picCanvasWidth == 0) return;		
		if (roi == null ) {
			roi = new ROI();
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

		roi.x = cropRect.left/picCanvasWidth;
		roi.y = cropRect.top/picCanvasHeight; 
		roi.w = rectW/picCanvasWidth; 
		roi.h = rectH/picCanvasHeight;
		
		Log.w(TAG, "saveROI roi[x:" + roi.x + ", y:"+ roi.y + ", h:" + roi.h + ", w:"+ roi.w + "]");		
	}	
	
	public boolean onTouchEvent(MotionEvent ev) {
		boolean bm = false;
		if (!isDrawCrop) return bm;
		try {			
			bm = cropRect.onTouchEvent(ev);
			if (bm)invalidate();
			return bm;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return bm;
	}

}
