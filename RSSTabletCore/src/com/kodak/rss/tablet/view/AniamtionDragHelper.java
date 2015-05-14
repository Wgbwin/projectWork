package com.kodak.rss.tablet.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.animation.AnimationStatus;
import com.kodak.rss.tablet.animation.DoubleSCaleAniamtion;

/**use this helper must call setAnimParentView to set animLayer is not Null*/
public class AniamtionDragHelper {
	
	private RelativeLayout animLayer;
	private RelativeLayout.LayoutParams params;
	public SelectImageView mDragImageView;
	private SelectImageView popUpImageView;	
	public int width;
	public int height;
	public int orgWidth,orgHeight;	
	public int ox,oy;
	
	public boolean isFrisitDragInCard = false;
	private DragTarget dragTarget;
	private boolean  isAnimationScaleHalf = false;
	
	public OnGridDragListener onGridDragListener;
	
	private Object[] objs;
	private ImageView iv;
	private RelativeLayout.LayoutParams ivParams;
	private Context context;
	private DisplayMetrics dm;
	
	public AniamtionDragHelper(Context context){
		this.context = context;
		this.dm = context.getResources().getDisplayMetrics();
		this.popUpImageView = new SelectImageView(context);
		this.params = new RelativeLayout.LayoutParams(0, 0);
		this.ivParams = new RelativeLayout.LayoutParams(0, 0);
		
	}
		
	public void setAnimParentView(RelativeLayout animLayer,DragTarget dragTarget) {		
		this.animLayer = animLayer;
		this.dragTarget = dragTarget;		
	}
	
	public void setAnimationScaleHalf(boolean isScaleHalf){
		this.isAnimationScaleHalf = isScaleHalf;
	}
	
	public void setOnDragListener(OnGridDragListener onDragListener){		
		this.onGridDragListener = onDragListener;		
	}

	public Object[] getObject(){
		return objs;
	}
	
	public void setWH(int viewWidth,int viewHeight){
		this.orgWidth =this.width = viewWidth;
		this.orgHeight =this.height = viewHeight;
	}
	
	private void setWHLT(float rawX,float rawY){
		this.ox = (int) (rawX - this.width*1f/2);
		this.ox =  this.ox > 0 ? this.ox : 0;		
		this.oy = (int) (rawY - this.height*1f/2);
		this.oy =  this.oy > 0 ? this.oy : 0;	
		if (params == null) return;
		params.leftMargin = ox;
		params.topMargin = oy;
		if (width > orgWidth) {
			width = orgWidth;
		}
		if (width < orgWidth*1f/4) {
			width = (int) (orgWidth*1f/4);
		}
		if (height > orgHeight) {
			height = orgHeight;
		}
		if (height < orgHeight*1f/4) {
			height = (int) (orgHeight*1f/4);
		}
		
		if (width < dm.density*14) {
			width = (int) dm.density*14;
		}
		
		if (height < dm.density*14) {
			height = (int) dm.density*14;
		}
		
		params.width = width;
		params.height = height;		
	}
	
	private void setLT(float rawX,float rawY){
		this.ox = (int) (rawX - this.width*1f/2);
		this.ox =  this.ox > 0 ? this.ox : 0;		
		this.oy = (int) (rawY - this.height*1f/2);
		this.oy =  this.oy > 0 ? this.oy : 0;	
		if (params == null) return;
		params.leftMargin = ox;
		params.topMargin = oy;		
	}
	
	public void addTempImageView(int l,int t,int w,int h,Bitmap bitmap){
		if (iv != null) return;
		iv = new ImageView(context);
		iv.setImageBitmap(bitmap);
		ivParams.leftMargin = l;
		ivParams.topMargin = t;
		ivParams.width = w;
		ivParams.height = h;		
		animLayer.addView(iv, ivParams);
	}
	
	public void addTempImageView(final int l,final int t,final int w,final int h,int resourceId){
		if (iv != null) return;
		iv = new ImageView(context);
		iv.setImageResource(resourceId);
		ivParams.leftMargin = l;
		ivParams.topMargin = t;
		ivParams.width = w;
		ivParams.height = h;		
		animLayer.addView(iv, ivParams);		
		ScaleAnimation sa = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		sa.setDuration(300);		
		sa.setFillAfter(true);
		sa.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}			
			@Override
			public void onAnimationRepeat(Animation animation) {}			
			@Override
			public void onAnimationEnd(Animation animation) {
				if (iv == null) return;	
				iv.clearAnimation();
				ivParams.leftMargin = (int) (l - w*1f/4);
				ivParams.topMargin = (int) (t - h*1f/4);
				ivParams.width = (int) (1.5f*w);
				ivParams.height = (int) (1.5f*h);
				iv.setLayoutParams(ivParams); 							
			}
		});	
		iv.startAnimation(sa);
	}
	
	public void deleteTempImageView(){
		if (iv == null) return;
		animLayer.removeView(iv);
		iv = null;
	}

	public void removeDragImage() {
		if (mDragImageView != null) {
			animLayer.removeView(mDragImageView);
			mDragImageView = null;
		}
	}	
	
	public void createDragImage(Bitmap bitmap,final float rawX, final float rawY, Object... objs) {			
		if (onGridDragListener != null) {
			onGridDragListener.onStartDrag(rawX,rawY);
		}		
		removeDragImage();	
		setWHLT(rawX, rawY);	
		this.objs = objs;		
		mDragImageView = popUpImageView;	
		mDragImageView.setImageBitmap(bitmap, true,true);		
		animLayer.addView(mDragImageView, params);		
		ArrayList<AnimationStatus> statusList = new ArrayList<AnimationStatus>(3);			
		if (isAnimationScaleHalf) {
			statusList.add(new AnimationStatus(1.0f,1.0f,0f));	
			statusList.add(new AnimationStatus(1.5f,1.5f,0.3f));//1.5
			statusList.add(new AnimationStatus(1.25f,1.25f,0.6f));//1
			statusList.add(new AnimationStatus(1.1f,1.1f,0.9f));//0.65
			statusList.add(new AnimationStatus(1.04f,1.04f,1f));//0.5
		}else {
			statusList.add(new AnimationStatus(1.0f,1.0f,0f));	
			statusList.add(new AnimationStatus(1.5f,1.5f,0.5f));//1.5
			statusList.add(new AnimationStatus(1.25f,1.25f,1f));//1
		}
		
		DoubleSCaleAniamtion da = new DoubleSCaleAniamtion(statusList);		
		da.setDuration(300);
		da.setFillAfter(true);
		mDragImageView.startAnimation(da);	
		da.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}			
			@Override
			public void onAnimationRepeat(Animation animation) {}			
			@Override
			public void onAnimationEnd(Animation animation) {
				if (mDragImageView == null) return;	
				mDragImageView.clearAnimation();
				if (isAnimationScaleHalf) {	
					width = (int) (width*0.5f);
					height = (int) (height*0.5f);
					setWHLT(rawX, rawY);					
					mDragImageView.setLayoutParams(params); 
				}				
				((View)mDragImageView.getParent()).setVisibility(View.VISIBLE);				
			}
		});		
	}

	private boolean isDragStarted = false;
	public void onDrag(final float rawX,final float rawY,Object... onDragObjs) {		
		if (mDragImageView == null) return;
					
		if (onGridDragListener != null) {
			onGridDragListener.onDragging(rawX,rawY);
		}	
		if (!isDragStarted) {				
			if (onGridDragListener != null) {
				onGridDragListener.showEdit();
			}			
			isDragStarted = true;
		}		
		
		setLT(rawX, rawY);		
		if (dragTarget == null && objs == null) {
			mDragImageView.setLayoutParams(params); 
			return;
		}
		Object[] result = null;
		if (dragTarget != null) {
			result = dragTarget.pointToPosition(rawX,rawY);		
		}
		if (result == null && onDragObjs != null && onDragObjs.length >= 1 ) {				
			result = onDragObjs;
		}				
		if (result != null ) {		
			if (onGridDragListener != null) {
				onGridDragListener.showFrameForLayer(result);
			}				
			
			if (isFrisitDragInCard) {
				mDragImageView.setLayoutParams(params); 
			}else {				
				isFrisitDragInCard = true;	
				mDragImageView.clearAnimation();
				ArrayList<AnimationStatus> statusList = new ArrayList<AnimationStatus>(2);
				statusList.add(new AnimationStatus(1.0f,1.0f,0f));	
				statusList.add(new AnimationStatus(0.5f,0.5f,0.7f));
				statusList.add(new AnimationStatus(0.5f,0.5f,1f));				
				DoubleSCaleAniamtion da = new DoubleSCaleAniamtion(statusList);	
				da.setDuration(300);
				da.setFillAfter(true);
				mDragImageView.startAnimation(da);
				da.setAnimationListener(new AnimationListener() {			
					@Override
					public void onAnimationStart(Animation animation) {}			
					@Override
					public void onAnimationRepeat(Animation animation) {}			
					@Override
					public void onAnimationEnd(Animation animation) {
						if (mDragImageView == null) return;	
						mDragImageView.clearAnimation();
						width = (int) (width*0.5f);
						height = (int) (height*0.5f);							
						setWHLT(rawX, rawY);							
						mDragImageView.setLayoutParams(params); 		
					}
				});
			}
		}else {	
			if (dragTarget != null) {
				dragTarget.hideAllFrames();
			}
			if (isFrisitDragInCard) {
				isFrisitDragInCard = false;
				mDragImageView.clearAnimation();
				ArrayList<AnimationStatus> statusList = new ArrayList<AnimationStatus>(2);
				statusList.add(new AnimationStatus(1.0f,1.0f,0f));	
				statusList.add(new AnimationStatus(2.0f,2.0f,0.7f));
				statusList.add(new AnimationStatus(2.0f,2.0f,1f));				
				DoubleSCaleAniamtion da = new DoubleSCaleAniamtion(statusList);	
				da.setDuration(300);
				da.setFillAfter(true);
				mDragImageView.startAnimation(da);
				da.setAnimationListener(new AnimationListener() {			
					@Override
					public void onAnimationStart(Animation animation) {}			
					@Override
					public void onAnimationRepeat(Animation animation) {}			
					@Override
					public void onAnimationEnd(Animation animation) {
						if (mDragImageView == null) return;
						mDragImageView.clearAnimation();
						width = width*2;
						height = height*2;							
						setWHLT(rawX, rawY);				
						mDragImageView.setLayoutParams(params); 		
					}
				});
			}else {										
				mDragImageView.setLayoutParams(params);    				
			}	       					
		}
	}
	
	public void onStopDrag(float rawX,float rawY,ImageInfo dragImageInfo,Bitmap bitmap) {
		isDragStarted = false;		
		isFrisitDragInCard = false;	
		if (dragTarget == null) {
			removeDragImage();					
			dragImageInfo = null;	
			return;
		}		
		final Object[] result = dragTarget.pointToPosition(rawX,rawY);
		removeDragImage();		
		if (result == null) {									
			dragImageInfo = null;							       	       			
		}
		if (onGridDragListener != null) {
			onGridDragListener.onStopDrag(rawX,rawY,result,dragImageInfo,bitmap);
		}	
		dragTarget.hideAllFrames();
	}
	
	public void onStopDrag(float rawX,float rawY) {
		isDragStarted = false;		
		isFrisitDragInCard = false;			
		removeDragImage();			
		if (dragTarget != null) {			
			dragTarget.hideAllFrames();
		}								  							
	}
	
	public boolean isTouchInItem(int x, int y) {
		if (mDragImageView == null) return true;
		int leftOffset = mDragImageView.getLeft();
		int topOffset = mDragImageView.getTop();
		if (leftOffset < 0 || topOffset < 0) return true;
		if (x < leftOffset || x > leftOffset + width) return false;
		if (y < topOffset || y > topOffset + height) return false;
		return true;
	}
	
	public float spacing(float x1,float x2, float y1,float y2) {
		float space = (x2 - x1)*(x2 - x1) + (y2-y1)*(y2-y1);
		return FloatMath.sqrt(space);
	}
	
	public Bitmap getBitmap(String imagePath){
		Bitmap bitmap = null;
		if (imagePath != null && orgWidth > 0 && orgHeight > 0) {
			int downsample = 1;	 
		 	try {	    	    	
			   BitmapFactory.Options options = new Options();	    	    	
			   options.inJustDecodeBounds = true;				
			   BitmapFactory.decodeFile(imagePath, options);
			   int origW = options.outWidth;
			   int origH = options.outHeight;
			   int downsampleW = 1;	
			   if(origH > orgHeight && orgHeight > 0){		    				
				  downsample = (int) Math.ceil((origH * 1.0)/ orgHeight);		    						
				}					    	    
			   if(origW > orgWidth && orgWidth > 0){								
				 downsampleW = (int) Math.ceil((origW * 1.0)/ orgWidth);
		         downsample = downsample > downsampleW ? downsample : downsampleW;
		       }
			   options.inJustDecodeBounds = false;
			   options.inSampleSize = downsample;
			   options.inPreferredConfig = Bitmap.Config.RGB_565;   
			   bitmap = BitmapFactory.decodeFile(imagePath, options);
				    		
			   int rotate = ImageUtil.getDegreesExifOrientation(imagePath);  
			   if(rotate > 0 && bitmap != null) {   
				  Bitmap rotateBitmap = ImageUtil.rotateBitmap(bitmap,rotate);     
				  if(rotateBitmap != null) {   
				      bitmap.recycle();   
				       bitmap = rotateBitmap;   
				  }             
				}	    		
		 	}catch (OutOfMemoryError oom) {	    	
		 	    bitmap = null;
		 	    System.gc();
			}			
		}  	
 	    if (bitmap == null) {
 		    bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.imagewait60x60);
 	    }	
		return bitmap;
	}

	public interface OnGridDragListener{
		public void showEdit();
		public void onStartDrag(float rawX,float rawY);
		public void onDragging(float rawX,float rawY);
		public void showFrameForLayer(Object[] result);		
		public void onStopDrag(float rawX,float rawY,Object[] result,ImageInfo dragImageInfo,Bitmap bitmap);		
	}

}
