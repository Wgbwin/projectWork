package com.kodak.rss.tablet.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.util.PicMediaUtil;
import com.kodak.rss.tablet.util.PicMediaUtil.BitmapBuddle;
/**
 * 
 * Purpose: 
 * Author: Slider Xiao
 * Created Time: Aug 13, 2013 9:50:43 AM 
 * Update By: Slider Xiao, Aug 13, 2013 9:50:43 AM
 */
public class RainPicView extends SurfaceView  implements SurfaceHolder.Callback {
	private String TAG = "RainPicView";
	private Vector<RainImage> mImages;
	private List<RainImage> mSwapImages = new ArrayList<RainPicView.RainImage>(); // image which will be delete by images when rain-image reach the bottom
	private SurfaceHolder mRainHolder;
	private int mWidth,mHeight;
	private Context mContext;
	private DrawImage mDrawImage;
	private LoadImage mLoadImage;
	private LoadBackground mLoadBackground;
	private Bitmap mBackground;
	private Rect mBackGroundSrcRect;
	private Rect mFullRect;
		
	private PicMediaUtil mPicMediaUtil;
	private DisplayMetrics mDm;
	private int mScalWidth;
	
	private OnItemClickListener mOnImageClickListener;
	

	public RainPicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(TAG, "RainPicView");
		init(context);
	}

	private void init(Context context){
		this.mContext = context;
		mDm = getResources().getDisplayMetrics();
		mScalWidth = mDm.widthPixels/7;		
		
		mRainHolder = getHolder();
		mRainHolder.addCallback(this);
		mPicMediaUtil = new PicMediaUtil(context);
		Log.d(TAG, "init picMediaUtil.getCount "+ mPicMediaUtil.getCount());
	}
	
	public void start(){
		mDrawImage = new DrawImage();
		mLoadImage = new LoadImage();
		mLoadBackground = new LoadBackground();
		mDrawImage.flag = true;
		mLoadImage.flag = true;
		mLoadBackground.flag = true;
		mLoadImage.start();
		mDrawImage.start();
		mLoadBackground.start();
	}
	
	public void stop(){
		if(mDrawImage!=null){
			mDrawImage.flag = false;
			mDrawImage = null;
		}
		if(mLoadImage!=null){
			mLoadImage.flag = false;
			mLoadImage = null;
		}
		if(mLoadBackground!=null){
			mLoadBackground.flag = false;
			mLoadBackground = null;
		}
		if(mImages!=null){
			synchronized (mImages) {
				for(RainImage image:mImages){
					image.recyle();
				}
				mImages.clear();
			}
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mFullRect = new Rect(0, 0, w, h);
		mBackGroundSrcRect = new Rect();
	}
	
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
		Log.d(TAG, "surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated ");
		mHeight = getHeight();
		mWidth = getWidth();
		Log.d(TAG, "surfaceCreated  width "+mWidth+" height "+mHeight);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		stop();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev){		
		float mRawX = ev.getRawX();
		float mRawY = ev.getRawY();	
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(mOnImageClickListener == null) return super.onTouchEvent(ev);		
			if(mImages==null) return super.onTouchEvent(ev);		
			synchronized (mImages) {		
				for (int i = mImages.size() - 1; i >= 0; i--) {
					RainImage image = mImages.get(i);
					if ((mRawX > image.x && mRawX < image.x+image.imageWidth) && 
							(mRawY > image.imageY && mRawY < image.imageY+image.imageHeight)) {
						mOnImageClickListener.onItemClick(this, image);
						break;
					}
				}
			}			
			break;
		}
		return super.onTouchEvent(ev);		
	}
	
	public void setOnItemClickListener(OnItemClickListener onImageClickListener) {
		mOnImageClickListener = onImageClickListener;
	}
	
	private class LoadBackground extends Thread {
		final int SPACE_TIME = 5000;
		boolean flag = true;
		int position = 0;
		@Override
		public void run() {
			long spaceTime = SPACE_TIME;
			while (flag) {
				if (mImages != null && !mImages.isEmpty() && mBackGroundSrcRect != null) {
//					//recycle old background
//					if (mBackground != null && !mBackground.isRecycled()) {
//						mBackground.recycle();
//					}
					
					if (position > mImages.size() - 1) {
						position = mImages.size() - 1 ;
					}
					
					String path = mImages.get(position).imagePath;
					int size = mDm.widthPixels / 5;
					mBackground = ImageUtil.getImageLocal(path, size, size);
					
					//set the source rect to display (center)
					int w = mBackground.getWidth();
					int h = mBackground.getHeight();
					if ((float)mDm.widthPixels / mDm.heightPixels > (float)w / h) {
						mBackGroundSrcRect.left = 0;
						mBackGroundSrcRect.right = w;
						int rectH = w * mDm.heightPixels / mDm.widthPixels;
						mBackGroundSrcRect.top = (h - rectH) / 2;
						mBackGroundSrcRect.bottom = mBackGroundSrcRect.top + rectH;
					} else {
						mBackGroundSrcRect.top = 0;
						mBackGroundSrcRect.bottom = h;
						int rectW = h * mDm.widthPixels / mDm.heightPixels;
						mBackGroundSrcRect.left = (w - rectW) / 2;
						mBackGroundSrcRect.right = mBackGroundSrcRect.left + rectW;
					}
					
					position ++;
					spaceTime = SPACE_TIME;
				} else {
					spaceTime = 200;
				}
				
				try {
					Thread.sleep(spaceTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private class LoadImage extends Thread {
		final int SPACE_TIME_MIN = 100; // when images is not many, improve the load speed
		final int SPACE_TIME = 500;
		final int SCREEN_PIC_MAX = 10;//18 is the old number
		boolean flag = true;
		@Override
		public void run() {
			if(mImages==null){
				mImages = new Vector<RainPicView.RainImage>();
			}			
			int size = mPicMediaUtil.getCount();
			int index = 0;
			while (flag) {
				if(mImages.size()<SCREEN_PIC_MAX){	
					BitmapBuddle bitmapBuddle = mPicMediaUtil.getBitmap(index,mScalWidth,mScalWidth);
					Bitmap bitmap = bitmapBuddle.getBitmap();
					index = bitmapBuddle.getPosition();
					if(bitmap!=null){
						bitmap = ImageUtil.combinateFrame(bitmap);
						if(bitmap!=null){
							synchronized (mImages) {
								mImages.add(new RainImage(bitmap, bitmapBuddle.getPath()));
							}
						}
					}
					if(index>=size){
						index = 0;
					}
				}
				try{
					if(size<4){
						Thread.sleep(SPACE_TIME_MIN);	
					}else{
						Thread.sleep(SPACE_TIME);	
					}
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
		
	private class DrawImage extends Thread{
		boolean flag = true;
		final int SPACE_TIME = 33;
		
		@Override
		public void run() {
			if(mImages==null) return;
			while (flag) {	
				Canvas canvas = null;
				try {
					canvas = mRainHolder.lockCanvas();
				} catch (Exception e) {
					e.printStackTrace();
				}	
				// when you start the this thread quickly, canvas will be null
				if(canvas!=null){
					if (mBackground != null && !mBackground.isRecycled()) {
						canvas.drawBitmap(mBackground, mBackGroundSrcRect, mFullRect, null);
						canvas.drawColor(0xaf000000);
					} else {
						canvas.drawColor(Color.BLACK);
					}
					synchronized (mImages) {						
						for(RainImage image: mImages){
							float mY = image.getY();
							if(mY >mHeight+100){									
								mSwapImages.add(image);									
							}else {
								canvas.drawBitmap(image.bitmap, image.getMatrix(), null);
							}
						}
							
						for(RainImage image: mSwapImages){
							mImages.remove(image);
							image.recyle();
						}
						mSwapImages.clear();
					}					
					try {
						if (mRainHolder != null) {
							mRainHolder.unlockCanvasAndPost(canvas);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				try{
					Thread.sleep(SPACE_TIME);	
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}
	}
	
	public class RainImage{
		private final float SPEED_MAX = 1.5f;	// 1-2 range
		private final int ROTATE_MAX = 90;	// 0-90 RANGE
		private final float OFFSET_X = 10;
		private final float OFFSET_Y = -10;
		Bitmap bitmap;
		Matrix matrix;
		private float rotate;
		public float x,y;	// coordinate
		private float speed;	// speed in y coordinate
		public String imagePath;
		public float imageY;
		public float imageHeight;
		public float imageWidth;		
		
		public RainImage(Bitmap bitmap,  String imagePath){
			this.bitmap = bitmap;
			this.imagePath = imagePath;			
//			rotate = ((int)(Math.random()*ROTATE_MAX))%360;
			rotate = 0 ;
			speed = (float)(Math.random()*SPEED_MAX)+0.5f;	
			
			matrix = new Matrix();
			imageHeight = bitmap.getHeight();
			imageWidth = bitmap.getWidth();
			
			matrix.postRotate(rotate,imageWidth/2,imageHeight/2);
			RectF rectF = getBounds();
			y = rectF.bottom>0? -rectF.bottom:rectF.bottom;
			x = rectF.left>0? rectF.left:-rectF.left;
			x = (int)(Math.random()*(mWidth-(rectF.right-rectF.left)))+x;
			matrix.setTranslate(x, y);
		}
		public float getY(){
			float result = y;
			y = y+speed;
			return result;
		}
		public Matrix getMatrix(){
			imageY = y;
			matrix.setTranslate(x, y);
			matrix.postRotate(rotate,x+imageWidth/2,y+imageHeight/2);
			y = y+speed;
			return matrix;
		}
		public void restore(){
			rotate = ((int)(Math.random()*ROTATE_MAX))%360;
			rotate = 0 ;
			speed = (float)(Math.random()*SPEED_MAX)+0.5f;	
			matrix = new Matrix();
			matrix.postRotate(rotate,bitmap.getWidth()/2,bitmap.getHeight()/2);
			RectF rectF = getBounds();
			y = rectF.bottom>0? -rectF.bottom:rectF.bottom;
			x = rectF.left>0? rectF.left:-rectF.left;
			x = (int)(Math.random()*(mWidth-(rectF.right-rectF.left)))+x;
			matrix.setTranslate(x, y);
		}
		
		public void recyle(){
			if(bitmap != null && !bitmap.isRecycled()){
				bitmap.recycle();
				bitmap = null;
			}				
			matrix = null;
		}
		
		public RectF getBounds(){
			float[] srcPoint = {0,0,bitmap.getWidth(),0,0,bitmap.getHeight(),bitmap.getWidth(),bitmap.getHeight()};
			float[] dstPoint = new float[8];
			matrix.mapPoints(dstPoint, srcPoint);
			
			//get External Matrix by float[]
			float mLeft = dstPoint[0];
			for(int i=0;i<dstPoint.length;i=i+2){
				if(mLeft>dstPoint[i]){
					mLeft = dstPoint[i];
				}
			}
			float mTop = dstPoint[1];
			for(int i=1;i<dstPoint.length;i=i+2){
				if(mTop>dstPoint[i]){
					mTop = dstPoint[i];
				}
			}
			float mRight = dstPoint[0];
			for(int i=0;i<dstPoint.length;i=i+2){
				if(mRight<dstPoint[i]){
					mRight = dstPoint[i];
				}
			}
			float mBottom = dstPoint[1];
			for(int i=1;i<dstPoint.length;i=i+2){
				if(mBottom<dstPoint[i]){
					mBottom = dstPoint[i];
				}
			}
			return new RectF(mLeft, mTop, mRight, mBottom);
		}
	}
	
	public static interface OnItemClickListener {
		public void onItemClick(RainPicView view, RainImage image);
	}
	
}
