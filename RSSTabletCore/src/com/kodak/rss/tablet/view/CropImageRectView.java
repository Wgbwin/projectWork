package com.kodak.rss.tablet.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.FloatMath;
import android.view.MotionEvent;

import com.kodak.rss.tablet.R;

public class CropImageRectView {

	Paint mCropPaint = new Paint();	
	Bitmap corner;
	public RectF contentRect;
	private Context mContext;
	private boolean isPinchZoom = false;
	
	int mGap = 10;

	private float startX;

	double offsetX;
	private double startY,offsetY;
	
	private double scaleFactor = 1.0;	
	
	public double left = 0,right = 0,top = 0,bottom = 0;	
	public double picCanvasWidth,picCanvasHeight,LT,TP;
	double newWidth = 0,newHeight = 0;
	private boolean isEffect = false;		
	double starteddistance = 0.0;
	private float preCos,cos;
	public boolean rotate = false;	
	double defaultScale = 1.0;	
	public double minHeight = 50 ;
	public double minWidth;
	
	public int cornerWidth,cornerHeight;

	public CropImageRectView(Context context) {
		mContext = context;		
		contentRect = new RectF();
		mCropPaint.setStyle(Paint.Style.STROKE);
		mCropPaint.setStrokeWidth(6f);
		mCropPaint.setARGB(200, 118, 207, 224);
		corner = BitmapFactory.decodeResource(context.getResources(),R.drawable.cropboxcorner);		
		minHeight =  (context.getResources().getDisplayMetrics().density * minHeight);
		
		cornerWidth = corner.getWidth();
		cornerHeight = corner.getHeight();
			
		scaleFactor = 1.0;		
	}	

	public void setContentRectValue(){		
		if (rotate) {
			rotate = false;
			newWidth = right - left;
			newHeight = bottom - top;
			double temp = newWidth;
			newWidth = newHeight;
			newHeight = temp; 			
			
			right = newWidth+left;
			bottom = newHeight+top;
						
			rotateCheckBounds();
			checkBounds(LT,TP);
		}
		
		contentRect.left = (float) left;
		contentRect.right = (float) right;
		contentRect.top = (float) top;
		contentRect.bottom = (float) bottom;
	}	

	final public void draw(Canvas canvas) {	
		setContentRectValue();
		canvas.save();
		drawContentRect(canvas);
		drawMidlePoints(canvas);
		canvas.restore();
	}

	private void drawContentRect(Canvas canvas) {				
		canvas.drawRect(contentRect, mCropPaint);
	}

	private void drawMidlePoints(Canvas canvas) {
		if (corner == null) {
			corner = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.cropboxcorner);
		}
		canvas.drawBitmap(corner,(contentRect.left - (corner.getWidth() / 2)),(contentRect.top - (corner.getHeight() / 2)), null);
		canvas.drawBitmap(corner,(contentRect.left - (corner.getWidth() / 2)),(contentRect.bottom - (corner.getHeight() / 2)), null);
		canvas.drawBitmap(corner,(contentRect.right - (corner.getWidth() / 2)),(contentRect.top - (corner.getHeight() / 2)), null);
		canvas.drawBitmap(corner,(contentRect.right - (corner.getWidth() / 2)),(contentRect.bottom - (corner.getHeight() / 2)), null);
	}

	private boolean checkAccepable(double x, double y) {			
		if (x < left+LT - mGap)
			return false;
		else if (x > right+LT + mGap)
			return false;
		else if (y < top+TP - mGap)
			return false;
		else if (y > bottom+TP + mGap)
			return false;
		return true;	
	}
		
	private int md = 0;
	private final static int LEFT_TOP = 1, RIGHT_TOP = 2, LEFT_BOTTOM = 3, RIGHT_BOTTOM = 4;			
	private int getMode(double x, double y) {	
		md = 0;
		if ((Math.abs(left+LT - x) < mGap)&&(Math.abs(top+TP - y) < mGap)){
			md = LEFT_TOP;
		}else if ((Math.abs(right+LT - x) < mGap)&&(Math.abs(top+TP - y) < mGap)){
			md = RIGHT_TOP;
		}else if ((Math.abs(left+LT - x) < mGap)&&(Math.abs(bottom+TP - y) < mGap)){
			md = LEFT_BOTTOM;
		}else if ((Math.abs(right+LT - x) < mGap)&&(Math.abs(bottom+TP - y) < mGap)){
			md = RIGHT_BOTTOM;
		}		
		return md;
	}	

	@SuppressWarnings("deprecation")
	public boolean onTouchEvent(MotionEvent event) {		
		int pointerIndex = ((event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
		int pointerId = event.getPointerId(pointerIndex);		
		int pointCnt = event.getPointerCount();
		if (pointCnt > 1) {
			isPinchZoom = true;
		} else {
			isPinchZoom = false;
		}

		if (!isPinchZoom) {
			isPinchZoom = false;
			pointerId = event.getPointerId(0);
			int action = event.getAction();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				startX = event.getX(pointerId);
				startY = event.getY(pointerId);
				if (checkAccepable(startX, startY)) {					
					getMode(startX, startY);
					isEffect = true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				try {
					if (isEffect) {
						offsetX = event.getX(pointerId) - startX;
						offsetY = event.getY(pointerId) - startY;					
						startX = event.getX(pointerId);
						startY = event.getY(pointerId);
											
						newWidth  = right - left ;
						newHeight = bottom - top;

						switch (md) {
						case LEFT_TOP:	
							if (offsetX < 0 && (left <= 0 || top <= 0)) {
								break;
							}							
							left = left + offsetX;							
							top = top + offsetY;
							if ((left > right)||(top > bottom)) {
								left = left - offsetX;
								top = top - offsetY;
							}
							
							if (bottom - top < minHeight) {
								top = top - offsetY;
							}
							
							if (right - left < minWidth) {
								left = left - offsetX;
							}	
							
							break;
						case RIGHT_TOP:		
							if (offsetX > 0 && ((Math.abs(right - picCanvasWidth) < 0.005) || top <= 0)) {
								break;
							}
							right = right + offsetX;
							top = top + offsetY;
							if ((left > right)||(top > bottom)) {
								right = right - offsetX;
								top = top - offsetY;
							}
							
							if (bottom - top < minHeight) {
								top = top - offsetY;
							}
							
							if (right - left < minWidth) {
								right = right - offsetX;
							}	
							break;
						case LEFT_BOTTOM:	
							if (offsetX < 0 && (left <= 0 || (Math.abs(bottom - picCanvasHeight) < 0.005))) {
								break;
							}
							left = left + offsetX;										
							bottom = bottom + offsetY;
							if ((left > right)||(top > bottom)) {
								left = left - offsetX;
								bottom = bottom - offsetY;
							}
							if (bottom - top < minHeight) {
								bottom = bottom - offsetY;
							}
							
							if (right - left < minWidth) {
								left = left - offsetX;
							}	
							
							break;
						case RIGHT_BOTTOM:
							if (offsetX > 0 && (Math.abs(right - picCanvasWidth) < 0.005  || Math.abs(bottom - picCanvasHeight) < 0.005 )) {
								break;
							}
							right =  right + offsetX;													
							bottom = bottom + offsetY;
							if ((left > right)||(top > bottom)) {
								right = right - offsetX;
								bottom = bottom - offsetY;
							}
							
							if (bottom - top < minHeight) {
								bottom = bottom - offsetY;
							}
							
							if (right - left < minWidth) {
								right = right - offsetX;
							}	
							break;
						default:
							left = left + offsetX;
							top = top + offsetY;
							right = right + offsetX;
							bottom = bottom + offsetY;
							break;
						}
					}
					checkBounds(0,0);				
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				break;
			case MotionEvent.ACTION_UP:
				offsetY = 0;
				offsetX = 0;
				isEffect = false;							
				break;
			default:
				break;
			}
		} else {
			isPinchZoom = true;	
			int action = (event.getAction() & MotionEvent.ACTION_MASK);
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				try {
					float x1 = event.getX(event.getPointerId(0));
					float x2 = event.getX(event.getPointerId(1));
					float y1 = event.getY(event.getPointerId(0));
					float y2 = event.getY(event.getPointerId(1));
					double currentDistance = spacing(x2, x1, y2, y1);
					scaleFactor = currentDistance / starteddistance;
					starteddistance = currentDistance;
					cos = cos(event);
					
					if (Math.abs(cos) > 60 && Math.abs(cos - preCos) > 30) {						
						rotate = !rotate;
					}else {						
						double width = right - left;
						double height = bottom - top;	
						
						if (scaleFactor > 1) {
							newWidth = width * scaleFactor;					
							newHeight = height * scaleFactor;	
													
							if (newWidth > picCanvasWidth) {
								newWidth = picCanvasWidth;
							}
							
							if (newHeight > picCanvasHeight) {
								newHeight = picCanvasHeight;
							}
						}else if (scaleFactor < 1) {
							
							newWidth = width * scaleFactor;					
							newHeight = height * scaleFactor;	
													
							if (newWidth < minWidth) {
								newWidth = minWidth;
							}
							
							if (newHeight < minHeight) {
								newHeight = minHeight;
							}
							scaleFactor = defaultScale;	
						}
						
						left = left + (width - newWidth) / 2;
						top = top + (height - newHeight) / 2;
						right = right - (width - newWidth) / 2;
						bottom = bottom - (height - newHeight)/ 2;
						
					}
					preCos = cos;	
					checkBounds(0,0);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_1_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
				defaultScale = scaleFactor;		
				break;
			
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_1_DOWN:
			case MotionEvent.ACTION_POINTER_2_DOWN:
				float x0 = event.getX(event.getPointerId(0));
				float y0  = event.getY(event.getPointerId(0));
				float x1 = event.getX(event.getPointerId(1));
				float y1 = event.getY(event.getPointerId(1));
				starteddistance = spacing(x1, x0, y1,y0);
				preCos = cos(event);
			default:
				break;
			}
		}
		return true;
	}

	private void rotateCheckBounds(){		
		double perc = 1.0;		
		if (newWidth > picCanvasWidth){			
			perc = picCanvasWidth / newWidth;
			right *= perc;
			bottom *= perc;
			top *= perc;
			left *= perc;		
			newWidth  *= perc;
			newHeight *= perc;
		}		
		if (newHeight > picCanvasHeight){			
			perc = picCanvasHeight / newHeight;
			right *= perc;
			bottom *= perc;
			top *= perc;
			left *= perc;				
			newWidth  *= perc;
			newHeight *= perc;
		} 
	}
	
	private void checkBounds(double leftMGap,double topMGap){		
		if (left - leftMGap < 0){
			left = leftMGap;	
			right = left+newWidth;
		}
		if (right- leftMGap > picCanvasWidth){
			right = picCanvasWidth+leftMGap;
			left =	right - newWidth;
		}
		if (top - topMGap < 0){
			top = topMGap;
			bottom = top+newHeight;	
		}
		if (bottom - topMGap > picCanvasHeight){
			bottom = picCanvasHeight+topMGap;
			top = bottom - newHeight;;
		}		
	}

	private double spacing(float x2, float x1, float y2, float y1) {
		return Math.abs(Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)));
	}
	
	@SuppressLint("FloatMath")
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private float cos(MotionEvent event) {
		if ((event.getX(0) - event.getX(1)) * (event.getY(0) - event.getY(1)) > 0) {
			return (float) ((float) Math.acos(Math.abs(event.getX(0)- event.getX(1))/ spacing(event))/ Math.PI * 180f);
		}
		if ((event.getX(0) - event.getX(1)) * (event.getY(0) - event.getY(1)) < 0) {
			return (float) ((float) Math.acos(-Math.abs(event.getX(0)- event.getX(1))/ spacing(event))/ Math.PI * 180f);
		}
		if (event.getX(0) - event.getX(1) == 0) {
			return (float) 90f;
		}
		if (event.getY(0) - event.getY(1) == 0) {
			return 0f;
		}
		return 45f;
	}
	
}
