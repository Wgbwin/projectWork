package com.kodak.kodak_kioskconnect_n2r.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Page;
import com.kodak.utils.ProductUtil;


public class EditImageView extends ImageView{
	private static final String TAG = "EditImageView";
	
	private final static float MAX_ROTATION = 45.0f;
	
	private float xDown;
	private float yDown;
	private float rawXDown;
	private float rawYDown;
	private float distDown;
	private float degreesDown;
	private float rotationDown;
	private PointF zoomCenter;
	private boolean edited;// is this image has been edited
	
	public static final int MODE_NONE = 0;
	public static final int MODE_DRAG = 1;
	public static final int MODE_ZOOM = 2;
	public static final int MODE_ROTATE = 3;
	private int mode;
	
	private Matrix matrixDraw;
	private Matrix matrixDown;
	private Matrix matrixMove;
	
	private Bitmap editBitmap;
	private int imgWidth;//editBitmap width
	private int imgHeight;//edit Bitmap height
	
	private float maxZoomInScale = 0;
	
	private Bitmap iconRotate;
	private Rect iconRotateRect;
	private boolean rotatable = true;
	private boolean editable = true;
	private Layer layer;
	private Page page;
	
	private int imageDegrees = 0;
	
	private Paint borderPaint;
	
	private OnRotateListener onRotateListener;
	private OnEditlistener onEditlistener;
	
	private Bitmap mask;
	private boolean needDrawMask = false;
	
	public EditImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public EditImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public EditImageView(Context context) {
		super(context);
		init(context);
	}
	
	public void setEditBitmap(Bitmap editBitmap){
		if(this.editBitmap != null && this.editBitmap != editBitmap && !this.editBitmap.isRecycled()){
			this.editBitmap.recycle();
		}
		this.editBitmap = editBitmap;
		if(editBitmap != null){
			imgWidth = editBitmap.getWidth();
			imgHeight = editBitmap.getHeight();
		}
	}
	
	public int getRotateIconHeight(){
		//explain : border+icon = paddingtop and border = padding(left,bottom,right)
		return getPaddingTop()-getPaddingBottom();
	}
	
	public void setRotatable(boolean rotatable){
		this.rotatable = rotatable;
	}
	
	public void setEditable(boolean editable){
		this.editable = editable;
	}
	
	public boolean isEditable(){
		return editable;
	}
	
	public void setOnRotateListener(OnRotateListener onRotateListener){
		this.onRotateListener = onRotateListener;
	}
	
	public boolean isEdited(){
		return edited;
	}
	
	public void setOnEditListener(OnEditlistener onEditlistener){
		this.onEditlistener = onEditlistener;
	}
	
	public void setBasicParams(Page page, Layer layer,int width,int height){
		if (page instanceof CollagePage) {
			rotatable = ((CollagePage) page).pageType.equals("Standard");
		} else {
			rotatable = false;
		}
		this.page = page;
		this.layer = layer;
		imageDegrees = 0;
		edited = false;
		editable = true;
		setRotation(getRotationsFromLayer(layer)[1]);
		if(editBitmap != null){
			matrixDraw.set(getMatrixByParams(layer, width, height));
		}
		invalidate();
	}
	
	public void rotateImage() {
		imageDegrees += 90;
		imageDegrees %= 360;
		
		int oldDegrees = getRotateDegrees(matrixDraw);
		
		if (oldDegrees == imageDegrees) {
			Log.i(TAG, "image don't need to rotate");
			return;
		}
		
		RectF rect = getImageRect(matrixDraw);
		
		matrixDraw.postRotate(imageDegrees - oldDegrees, rect.centerX(), rect.centerY());
		formatMatrix(matrixDraw, true);
		invalidate();
		
	}
	
	public float getImageDegrees() {
		return imageDegrees;
	}
	
	
	public void updateImage(Bitmap bitmap) {
		if (bitmap != null) {
			
			int oldImgW = imgWidth;
			int oldImgH = imgHeight;
			RectF rect = getImageRect(matrixDraw);
			
			if (editBitmap != null && !editBitmap.isRecycled()) {
				editBitmap.recycle();
			}
			
			editBitmap = bitmap;
			imgWidth = bitmap.getWidth();
			imgHeight = bitmap.getHeight();
			
			float scale = (float)oldImgW / imgWidth;
			matrixDraw.postScale(scale, scale, rect.left, rect.top);
			
			//if image is in move,drag,etc, the other matrix should also update 
			if (mode != MODE_NONE) {
				RectF rectTemp = getImageRect(matrixMove);
				matrixMove.postScale(scale, scale, rectTemp.left, rectTemp.top);
				
				rectTemp = getImageRect(matrixDown);
				matrixDown.postScale(scale, scale, rectTemp.left, rectTemp.top);
			}
			
			invalidate();
		}
		
	}
	
	/**
	 * If set to 0, it means that there is no limit for zoom in scale
	 * @param scale
	 */
	public void setMaxZoomInScale(float scale) {
		maxZoomInScale = scale;
	}
	
	public Page getPage(){
		return page;
	}
	
	public Layer getLayer(){
		return layer;
	}
	
	/**
	 * layer roi
	 * @return
	 */
	public ROI getImageROI(){
		return getImageROI(matrixDraw);
	}
	
	private ROI getImageROI(Matrix imageMatrix) {
		if(layer == null){
			return null;
		}
		
		ROI roi = new ROI();
		ROI oldRoi = ProductUtil.getImageCropROI(layer);
		
		//normally, it won't happen
		if(oldRoi == null){
			return null;
		}
		
		
		roi.ContainerW = oldRoi.ContainerW;
		roi.ContainerH = oldRoi.ContainerH;
		
		//make a matrix without rotate
		Matrix matrix = new Matrix(imageMatrix);
		int r = getRotationsFromLayer(layer)[0];
		int w = getWidth()-getPaddingLeft() - getPaddingRight();
		int h = getHeight() - getPaddingTop() - getPaddingBottom();
		if( r == 180 ){
			matrix.postRotate(-r, w/2f, h/2f);
		}else if( r ==90 ){
			matrix.postRotate(-r, w/2f, w/2f);
		}else if( r == 270){
			matrix.postRotate(-r, h/2f, h/2f);
		}
		
		RectF rect = getImageRect(matrix);
		float scale = Math.abs(getScaleX(matrix));
		roi.x = -roi.ContainerW * rect.left / imgWidth / scale;
		roi.y = -roi.ContainerH * rect.top / imgHeight / scale;
		roi.w = r ==0 || r== 180 ? w / scale * roi.ContainerW/imgWidth: h / scale * roi.ContainerH/imgHeight ;
		roi.h = roi.w * oldRoi.h / oldRoi.w;
		
		//format the values
		if(roi.w > roi.ContainerW) roi.w = roi.ContainerW;
		if(roi.h > roi.ContainerH) roi.h = roi.ContainerH;
		if(roi.x + roi.w > roi.ContainerW) roi.x = roi.ContainerW-roi.w;
		if(roi.x < 0) roi.x = 0;
		if(roi.y + roi.h > roi.ContainerH) roi.y = roi.ContainerH-roi.h;
		if(roi.y < 0) roi.y = 0;
		
		return roi;
	}
	
	/**
	 * layer angle for photobook(0-360)
	 * @return
	 */
	public float getLayerAngel(){
		int[] rs = getRotationsFromLayer(layer);
		float angle = -rs[0] - getRotation();
		//format angle (0-360)
		angle = angle % 360;
		if(angle<0){
			angle = angle+360;
		}
		return angle;
	}
	
	/**
	 * @param layer
	 * @return {matrix rotation, view rotation}
	 * 
	 */
	public static int[] getRotationsFromLayer(Layer layer){
		//format angle (0-360)
		int angle = -layer.angle % 360;
		if(angle<0){
			angle = angle+360;
		}
		
		int mr = 0;//matrix rotation
		int vr = 0;//view roatation
		if(angle<=45){
			mr = 0;
			vr = angle;
		}else if(angle>45 && angle<=90){
			mr = 90;
			vr = angle - mr;
		}else if(angle>90 && angle<135){
			mr = 90;
			vr = angle-mr;
		}else if(135<=angle && angle<=180){
			mr = 180;
			vr = angle - mr;
		}else if(180<angle && angle<=225){
			mr = 180;
			vr = angle - mr;
		}else if(225<angle && angle<=270){
			mr = 270;
			vr = angle - mr;
		}else if(270<angle && angle<315){
			mr = 270;
			vr = angle-mr;
		}else if(315<=angle && angle<=360){
			mr = 0;
			vr = angle-360;
		}
		
		return new int[]{mr,vr};
	}
	
	private void init(Context context){
		setLayerType(LAYER_TYPE_HARDWARE, null);
		
		matrixDown = new Matrix();
		matrixMove = new Matrix();
		zoomCenter = new PointF(0,0);
		mode = MODE_NONE;
		matrixDraw = new Matrix();
		
		iconRotate = BitmapFactory.decodeResource(getResources(),R.drawable.tilt);
		iconRotateRect = new Rect();
		
		borderPaint = new Paint();
		borderPaint.setColor(0xFFFBBA06);
		borderPaint.setStrokeWidth(getPaddingLeft()*2);
		borderPaint.setAntiAlias(true);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		//draw border
		canvas.save();
		canvas.clipRect(0,getPaddingTop()-getPaddingLeft(),getWidth(),getHeight());
		canvas.drawLine(0, getPaddingTop()-getPaddingLeft(), getWidth(), getPaddingTop()-getPaddingLeft(), borderPaint);
		canvas.drawLine(0, getPaddingTop(), 0, getHeight(), borderPaint);
		canvas.drawLine(getWidth(), getHeight(), getWidth(), getPaddingTop()-getPaddingLeft(), borderPaint);
		canvas.drawLine(getWidth(), getHeight(), 0, getHeight(), borderPaint);
		canvas.restore();
		
		if(editBitmap == null){
			super.onDraw(canvas);
		}else{
			//draw image
			canvas.save();
			canvas.clipRect(getPaddingLeft(), getPaddingTop(), getWidth()-getPaddingRight(), getHeight()-getPaddingBottom());
			canvas.translate(getPaddingLeft(), getPaddingTop());
			canvas.drawColor(Color.WHITE);//png has transparent area, set it to white
			canvas.drawBitmap(editBitmap, matrixDraw, null);
			canvas.restore();
			
		}
		//draw rotate icon
		if(rotatable && editable){
			canvas.drawBitmap(iconRotate, null, iconRotateRect, null);
		}
		
		if(mask != null && needDrawMask){
			canvas.save();
			canvas.drawBitmap(mask, maskSrcRect, maskDstRect, null);
			canvas.restore();
		}
	}
	
	private Rect maskSrcRect;
	private RectF maskDstRect;
	public void setMask(Bitmap mask) {
		this.mask = mask;
		this.needDrawMask = true;
		if(this.mask != null){
			maskSrcRect = getMaskSrcRect(this.mask.getWidth(), this.mask.getHeight());
			maskDstRect = new RectF(getPaddingLeft(), getPaddingTop(), getWidth()-getPaddingRight(), getHeight()-getPaddingBottom());
			setAlpha(1.0f);
			invalidate();
		} else {
			setAlpha(0.5f);
		}
	}
	
	private Rect getMaskSrcRect(int maskWidth, int maskHeight){
		Rect dstSrc = new Rect();
		dstSrc.left = (int) (maskWidth * (layer.location.x / layer.location.ContainerW));
		dstSrc.top = (int) (maskHeight * (layer.location.y / layer.location.ContainerH));
		dstSrc.right = (int) (maskWidth * ((layer.location.w + layer.location.x) / layer.location.ContainerW));
		dstSrc.bottom = (int) (maskHeight * ((layer.location.y + layer.location.h) / layer.location.ContainerH));
		return dstSrc;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.i(TAG,"onSizeChanged:"+oldw+","+oldh+" -> " +w+","+h);
		
		int size = getPaddingTop() - getPaddingBottom();
		iconRotateRect.left = (w - size)/2;
		iconRotateRect.right = (w + size)/2;
		iconRotateRect.top = size - size * iconRotate.getHeight()/iconRotate.getWidth();
		iconRotateRect.bottom = size;
	}
	
	private boolean released = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!editable){
			return true;
		}
		
		switch (event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			released = false;
			if(rotatable && isTouch4Rotate(event)){
				mode = MODE_ROTATE;
				rotationDown = getRotation();
				if(onRotateListener != null){
					onRotateListener.onRotateStart();
				}
			}else if (editBitmap == null) {
				released = true;
				break;
			}else{
				mode = MODE_DRAG;
			}
			if(onEditlistener != null){
				onEditlistener.onEditStart(mode);
			}
			xDown = event.getX();
			yDown = event.getY();
			rawXDown = event.getRawX();
			rawYDown = event.getRawY();
			matrixDown.set(matrixDraw);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			if (released) {
				break;
			}
			
			if(mode != MODE_ROTATE){
				mode = MODE_ZOOM;
				distDown = getPointerDistance(event);
				degreesDown = getDegrees(event);
				rotationDown = getRotation();
				saveZoomCenterPoint(zoomCenter, event);
				matrixDown.set(matrixDraw);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (released) {
				break;
			}
			
			matrixMove.set(matrixDown);
			edited = true;
			if(mode == MODE_ZOOM){
				double dist = getPointerDistance(event);
				float scale = (float) (dist/distDown);
				matrixMove.postScale(scale, scale, zoomCenter.x, zoomCenter.y);
				
				if (scale > 0 && !isOutOfZoomInScale(matrixMove)) {
					formatMatrix(matrixMove, MODE_ZOOM);
					matrixDraw.set(matrixMove);
					invalidate();
				}
			}else if(mode == MODE_DRAG){
				matrixMove.postTranslate(event.getX() - xDown, event.getY() - yDown);
				formatMatrix(matrixMove, MODE_DRAG);
				matrixDraw.set(matrixMove);
				invalidate();
			}else if(mode == MODE_ROTATE){
				setRotation(getImageRotationDegrees(event));
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (released) {
				break;
			}
			
			if(mode == MODE_ROTATE && onRotateListener != null){
				onRotateListener.onRotateEnd();
			}
			mode = MODE_NONE;
			break;
		case MotionEvent.ACTION_UP:
			if (released) {
				break;
			}
			
			if(mode == MODE_ROTATE && onRotateListener != null){
				onRotateListener.onRotateEnd();
			}
			if(onEditlistener != null){
				onEditlistener.onEditEnd(mode);
			}
			mode = MODE_NONE;
			break;
		}
		return true;
	}
	
	public static interface OnRotateListener{
		void onRotateStart();
		void onRotateEnd();
	}
	
	public static interface OnEditlistener{
		void onEditStart(int mode);
		void onEditEnd(int mode);
	}
	
	private float getPointerDistance(MotionEvent event){
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x*x + y*y);
	}
	
	private float getDegrees(MotionEvent event){
		double deltaX = event.getX(0) - event.getX(1);
		double deltaY = event.getY(0) - event.getY(1);
		double radians = Math.atan2(deltaY, deltaX);
		return (float) Math.toDegrees(radians);
	}
	
	private float getImageRotationDegrees(MotionEvent event){
		//use rawX/Y , because I use setRotation for rotate, and it will change relative x,y 
		float x = event.getRawX() - rawXDown;
		
		float rotation = x/getWidth()*2 * MAX_ROTATION + rotationDown; 
		
		boolean clockwise = (x >= 0); 
		if(Math.abs(rotation) > MAX_ROTATION){
			rotation = clockwise ? MAX_ROTATION : -MAX_ROTATION;
		}
		
		return rotation;
	}
	
	private void saveZoomCenterPoint(PointF point,MotionEvent event){
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		zoomCenter.set(x/2, y/2);
	}
	
	private void formatMatrix(Matrix matrix, int mode){
		formatMatrix(matrix, mode == MODE_ZOOM);
	}
	
	private void formatMatrix(Matrix matrix, boolean checkScale){
		formatMatrix(matrix, checkScale, getWidth(), getHeight());
	}
	
	private void formatMatrix(Matrix matrix, boolean checkScale,int width,int height){
		int w = width-getPaddingLeft()-getPaddingRight(),h=height-getPaddingTop()-getPaddingBottom();
		RectF rect = getImageRect(matrix);
		if(checkScale){
			if(rect.right - rect.left < w || rect.bottom - rect.top < h){
				//because photobook layout rect is not exact, so we need to check if imgWidth>imgHeight
				float scale = ((double)imgHeight/h>(double)imgWidth/w) ? (float)w/rect.width() : (float)h/rect.height();
				matrix.postScale(scale, scale,0,0);
			}
		}
		
		rect = getImageRect(matrix);
		
		float dx=0,dy=0;
		if(rect.left > 0){
			dx = -rect.left;
		}
		if(rect.top > 0){
			dy = -rect.top;
		}
		if(rect.right < w){
			dx = w-rect.right;
		}
		if(rect.bottom < h){
			dy = h - rect.bottom;
		}
		
		if(dx !=0 || dy != 0){
			matrix.postTranslate(dx, dy);
		}
	}
	
	private Matrix getMatrixByParams(Layer layer,int width,int height){
		Matrix matrix = new Matrix();
		int[] rotations = getRotationsFromLayer(layer);
		int mr = rotations[0];
		int w = width-getPaddingLeft()-getPaddingRight(),h=height-getPaddingTop()-getPaddingBottom();
		ROI roi = ProductUtil.getImageCropROI(layer);
		
		float scale=0;
		//because photobook layout rect is not exact, so we need check if imgWidth<imgHeight
		if((double)imgHeight/h>(double)imgWidth/w){
			scale = (float) (mr == 90 || mr == 270  ? (float)h/roi.w * roi.ContainerW/imgWidth : (float)h/roi.h * roi.ContainerH/imgHeight);
		}else{
			scale = (float) (mr == 90 || mr == 270  ? (float)w/roi.h * roi.ContainerH/imgHeight : (float)w/roi.w * roi.ContainerW/imgWidth);
		}
		matrix.postScale(scale, scale,0,0);
		matrix.postTranslate(-(float)(roi.x*scale*imgWidth/roi.ContainerW),-(float)(roi.y*scale * imgHeight/roi.ContainerH));
		if( mr == 180 ){
			matrix.postRotate(mr,w/2f,h/2f);
		}else if( mr == 90 ){
			matrix.postRotate(mr,w/2f,w/2f);
		}else if( mr == 270 ){
			matrix.postRotate(mr, h/2f, h/2f);
		}
		
		formatMatrix(matrix, true,width,height);
		
		return matrix;
	}
	
	/**
	 * ignore rotate
	 * @param matrix
	 * @return
	 */
	private RectF getImageRect(Matrix matrix){
		PointF[] pts = getImageCorners(matrix);
		float left=pts[0].x,top=pts[0].y,right=pts[3].x,bottom=pts[3].y;
		for(int i=0,size=pts.length;i<size;i++){
			float x = pts[i].x;
			float y = pts[i].y;
			if(x<left)
				left = x;
			if(y<top)
				top = y;
			if(x>right)
				right = x;
			if(y>bottom)
				bottom = y;
			
		}
		return new RectF(left, top, right, bottom);
	}
	
	/**
	 * get 4 corners of the image
	 * ignore rotate
	 * @param matrix
	 * @return
	 */
	private PointF[] getImageCorners(Matrix matrix){
		float[] f = new float[9];  
        matrix.getValues(f);
		float x1 = f[0] * 0 + f[1] * 0 + f[2];  
        float y1 = f[3] * 0 + f[4] * 0 + f[5];  
        float x2 = f[0] * imgWidth + f[1] * 0 + f[2];  
        float y2 = f[3] * imgWidth + f[4] * 0 + f[5];  
        float x3 = f[0] * 0 + f[1] * imgHeight + f[2];  
        float y3 = f[3] * 0 + f[4] * imgHeight + f[5];  
        float x4 = f[0] * imgWidth + f[1] * imgHeight + f[2];  
        float y4 = f[3] * imgWidth + f[4] * imgHeight + f[5];  
        
        PointF[] p = new PointF[]{new PointF(x1,y1),new PointF(x2,y2),new PointF(x3,y3),new PointF(x4,y4)};
        return p;
	}
	
	private boolean isTouch4Rotate(MotionEvent event){
		//make a rect for rotate touch
		RectF rect = new RectF(iconRotateRect.left,iconRotateRect.top,iconRotateRect.right,iconRotateRect.bottom);
		
		return rect.contains(event.getX(), event.getY());
	}
	
	private float getScaleX(Matrix matrix){
		float[] f = new float[9];
		matrix.getValues(f);
		return f[Matrix.MSCALE_X];
	}
	
	private int getRotateDegrees(Matrix matrix) {
		float[] arr = new float[9];
		matrix.getValues(arr);
		
//		double x = Math.atan2(arr[7], arr[8]);
//		double y = Math.atan2(-arr[6],Math.sqrt((Math.pow(arr[7],2)+Math.pow(arr[8],2))));
		double z = Math.atan2(arr[0], arr[3]);
		
		int degrees = (int) (z * 180 / Math.PI);
		
		degrees %= 360;
		if (degrees < 0) {
			degrees += 360;
		}
		
		//TODO Maybe the calculate is wrong, I run this method and get the wrong value, so reset it by the result
		//Please fix this problem if you know the right method to get rotate degrees
		if (degrees == 0) {
			return 90;
		} else if (degrees == 270) {
			return 180;
		} else if (degrees == 180) {
			return 270;
		} else if (degrees == 90) {
			return 0;
		}
		
		return degrees;
	}
	
	private boolean isOutOfZoomInScale(Matrix matrix) {
		if (maxZoomInScale == 0) {
			//0 means there is no limit for zoom in scale
			return false;
		}
		
		ROI roi = getImageROI(matrix);
		if (roi != null && roi.ContainerW / roi.w > maxZoomInScale && roi.ContainerH / roi.h > maxZoomInScale) {
			return true;
		}
		
		return false;
	}
	
	public void dismiss() {
		setVisibility(View.INVISIBLE);
		needDrawMask = false;
		if(mask!=null){
			mask.recycle();
			mask = null;
		}
	}
}


