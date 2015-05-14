package com.kodak.kodak_kioskconnect_n2r.view;

import java.util.ArrayList;

import com.kodak.kodak_kioskconnect_n2r.animation.AnimationStatus;
import com.kodak.kodak_kioskconnect_n2r.animation.DoubleSCaleAniamtion;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class AnimationDragHelper {
	
	private Context mContext;
	private RelativeLayout.LayoutParams mParams;
	private RelativeLayout.LayoutParams ivParams;
	private FrameImageView mDragImageView;
	private FrameImageView popUpImageView;
	private ImageView iv;
	
	private RelativeLayout mAnimLayer;
	private DragTarget mDragTarget;
	
	private boolean isAnimationScaleHalf = false;
	private int width, height;
	private int orgWidth, orgHeight;
	private int ox,oy;
	private Object[] objs;
	
	private boolean isFirstDragInCard = false;
	
	public AnimationDragHelper(Context context){
		this.mContext = context;
		this.popUpImageView = new FrameImageView(mContext);
		this.mParams = new RelativeLayout.LayoutParams(0, 0);
		this.ivParams = new RelativeLayout.LayoutParams(0, 0);
	}
	
	public void setAnimParentView(RelativeLayout animLayer, DragTarget dragTarget){
		this.mAnimLayer = animLayer;
		this.mDragTarget = dragTarget;
	}

	public void setAnimationScaleHalf(boolean isAnimationScaleHalf) {
		this.isAnimationScaleHalf = isAnimationScaleHalf;
	}
	
	public void setSize(int viewWidth, int viewHeight){
		this.orgWidth = this.width = viewWidth;
		this.orgHeight = this.height = viewHeight;
	}
	
	public void createDragImage(Bitmap bitmap, final float rawX, final float rawY, Object... objs){
		removeDragImage();
		setPosition(rawX, rawY);
		this.objs = objs;
		mDragImageView = popUpImageView;
		mDragImageView.setImageBitmap(bitmap, true);
		mAnimLayer.addView(mDragImageView, mParams);
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
					setPosition(rawX, rawY);					
					mDragImageView.setLayoutParams(mParams); 
				}				
				((View)mDragImageView.getParent()).setVisibility(View.VISIBLE);				
			}
		});	
	}
	
	private void setPosition(float rawX,float rawY){
		this.ox = (int) (rawX - this.width*1f/2);
		this.ox =  this.ox > 0 ? this.ox : 0;		
		this.oy = (int) (rawY - this.height*1f/2);
		this.oy =  this.oy > 0 ? this.oy : 0;	
		if (mParams == null) return;
		mParams.leftMargin = ox;
		mParams.topMargin = oy;
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
		mParams.width = width;
		mParams.height = height;		
	}
	
	private void setLeftTopPoint(float rawX, float rawY){
		this.ox = (int) (rawX - this.width*1f/2);
		this.ox =  this.ox > 0 ? this.ox : 0;		
		this.oy = (int) (rawY - this.height*1f/2);
		this.oy =  this.oy > 0 ? this.oy : 0;	
		if (mParams == null) return;
		mParams.leftMargin = ox;
		mParams.topMargin = oy;
	}
	
	public void removeDragImage() {
		if (mDragImageView != null) {
			mAnimLayer.removeView(mDragImageView);
			mDragImageView = null;
		}
	}
	
	public void addTempImageView(int l,int t,int w,int h,Bitmap bitmap){
		if (iv != null) return;
		iv = new ImageView(mContext);
		iv.setImageBitmap(bitmap);
		ivParams.leftMargin = l;
		ivParams.topMargin = t;
		ivParams.width = w;
		ivParams.height = h;		
		mAnimLayer.addView(iv, ivParams);
	}
	
	public void addTempImageView(final int l,final int t,final int w,final int h,int resourceId){
		if (iv != null) return;
		iv = new ImageView(mContext);
		iv.setImageResource(resourceId);
		ivParams.leftMargin = l;
		ivParams.topMargin = t;
		ivParams.width = w;
		ivParams.height = h;		
		mAnimLayer.addView(iv, ivParams);		
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
		mAnimLayer.removeView(iv);
		iv = null;
	}
	
	private boolean isDragStarted = false;
	public void onDrag(final float rawX, final float rawY, Object... onDragObjs){
		if (!isDragStarted) {
			isDragStarted = true;
		}
		setLeftTopPoint(rawX, rawY);
		if (mDragTarget == null && objs == null) {
			mDragImageView.setLayoutParams(mParams); 
			return;
		}
		Object[] result = null;
		if (mDragTarget != null) {
			result = mDragTarget.pointToPosition(rawX,rawY);		
		}
		if (result == null && onDragObjs != null && onDragObjs.length >= 1 ) {				
			result = onDragObjs;
		}
		
		if(result != null){
			if(isFirstDragInCard){
				mDragImageView.setLayoutParams(mParams);
			} else {
				isFirstDragInCard = true;
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
						setPosition(rawX, rawY);							
						mDragImageView.setLayoutParams(mParams); 		
					}
				});
			}
		} else {	
			if (mDragTarget != null) {
				mDragTarget.hideAllFrames();
			}
			if (isFirstDragInCard) {
				isFirstDragInCard = false;
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
						setPosition(rawX, rawY);				
						mDragImageView.setLayoutParams(mParams); 		
					}
				});
			}else {										
				mDragImageView.setLayoutParams(mParams);    				
			}	       					
		}
	}
	
	public void onStopDrag(float rawX, float rawY, Bitmap bitmap){
		isDragStarted = false;
		isFirstDragInCard = false;
		if(mDragTarget == null){
			removeDragImage();
			return;
		}
		removeDragImage();
		mDragTarget.hideAllFrames();
	}
}
