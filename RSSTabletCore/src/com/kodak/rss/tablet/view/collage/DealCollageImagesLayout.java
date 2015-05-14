package com.kodak.rss.tablet.view.collage;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.CollageEditActivity;
import com.kodak.rss.tablet.thread.collage.DealImagesToCollagePageTasks;
import com.kodak.rss.tablet.view.SelectImageView;
import com.kodak.rss.tablet.view.dialog.DialogUploadImageError;

public class DealCollageImagesLayout extends RelativeLayout{

	private Context context;
	private LayoutParams addParams;		
	
	public static final int HANDLER_ADD_START = 0;
	public static final int HANDLER_ADD = 1;
	public static final int HANDLER_ADD_END = 2;	
	public static final int HANDLER_ADD_ERROR = 3;	
	public static final int HANDLER_ADD_ERROR_HIDE = 4;	
	private Handler addHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (context == null) return;
			if (context instanceof CollageEditActivity && ((CollageEditActivity)context).isFinishing()) return;										
			Object obj = msg.obj;			
			switch (msg.what) {
			case HANDLER_ADD_START:					
				((CollageEditActivity)context).showEditPBar();				
				break;
			case HANDLER_ADD:
				if (context instanceof CollageEditActivity) {
					((CollageEditActivity)context).notifyCollagePageChanged();
				}
				if (obj != null) {
					@SuppressWarnings("unchecked")
					List<ImageInfo> imageInfoList = (List<ImageInfo>) obj;
					for (ImageInfo imageInfo : imageInfoList) {
						DealCollageImagesLayout.this.hideImageView(imageInfo);
					}
				}
				break;
			case HANDLER_ADD_END:				
				setTasksNull();	
				((CollageEditActivity)context).editBar.setVisibility(View.GONE);
				break;		
			case HANDLER_ADD_ERROR:
				if (obj != null) {
					if(obj instanceof RssWebServiceException){
						((CollageEditActivity)context).showErrorWarning((RssWebServiceException) obj);
						
					}else if(obj instanceof ImageInfo){
						ImageInfo failInfo = (ImageInfo) obj;
						if (failInfo != null && failInfo.imageOriginalResource == null && !((CollageEditActivity)context).isHaveUploadErrorDialog) {									
							new DialogUploadImageError().initDialogUploadImageError(context,failInfo);
						}	
					}
				}
				break;	
				
			case HANDLER_ADD_ERROR_HIDE:				
				if (obj != null) {
					@SuppressWarnings("unchecked")
					List<ImageInfo> imageInfoList = (List<ImageInfo>) obj;
					for (ImageInfo imageInfo : imageInfoList) {
						DealCollageImagesLayout.this.hideImageView(imageInfo);
					}
				}
				break;				
			}
		}	
	};
	
	private DealImagesToCollagePageTasks dealImagesToCollagePageTasks;
	
	public DealCollageImagesLayout(Context context) {
		super(context);	
		init(context);
	}

	public DealCollageImagesLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public DealCollageImagesLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		this.context = context;
		this.addParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		dealImagesToCollagePageTasks = new DealImagesToCollagePageTasks(context, addHandler);
	}	
	
	public void dealImage(final RelativeLayout animLayer,View view, final String pageId,final ImageInfo imageInfo,boolean isAdd){
		if (isAdd) {
			addImageToPageUseAnimation(animLayer,view, pageId,imageInfo);
		}else {
			deleteImageInPage(pageId, imageInfo);
		}
	}

	private int displayImageX,displayImageY,displayImageW,displayImageH;	
	public void addImageToPageUseAnimation(final RelativeLayout animLayer,View view, final String pageId,final ImageInfo imageInfo){
		if(view == null) return;
		if(imageInfo == null) return;
		Bitmap bitmap = null;
		View selView = view.findViewById(R.id.photoContent);
		if(selView != null && selView instanceof SelectImageView){
			bitmap = ((SelectImageView)selView).getImageBitmap();
		}
		final ImageView iv = new ImageView(context);		
		final ImageView child = new ImageView(context);
		if (bitmap != null) {			
			iv.setImageBitmap(bitmap);
			child.setImageBitmap(bitmap);
		}else{
			iv.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.imagewait60x60));
			child.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.imagewait60x60));
		}

		int[] location = new int[2];
		view.getLocationInWindow(location);
		int x = location[0];
		int y = location[1];
		int w = view.getWidth();
		int h = view.getHeight();
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
		params.leftMargin = x;
		params.topMargin = y;
		
		if (! (displayImageW > 0)) {
	    	int[] locationDisplayImage = new int[2];
	    	DealCollageImagesLayout.this.getLocationOnScreen(locationDisplayImage);	
			displayImageX = locationDisplayImage[0];
			displayImageY = locationDisplayImage[1];
			displayImageW = DealCollageImagesLayout.this.getWidth();
			displayImageH = DealCollageImagesLayout.this.getHeight(); 	
		}
				
		TranslateAnimation ta = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.ABSOLUTE, displayImageX-x,
					TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.ABSOLUTE, displayImageY-y);
		ScaleAnimation	sa = new ScaleAnimation(1, (float)displayImageW/w, 1, (float)displayImageH/h,Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);					
			
		final AnimationSet animSet = new AnimationSet(true);				
		animSet.addAnimation(sa);
		animSet.addAnimation(ta);
		animSet.setInterpolator(new AccelerateDecelerateInterpolator());
		animSet.setDuration(500);
		
		final String LastAnimationID = animSet.toString();
		animLayer.removeAllViews();
		
		animLayer.addView(iv,params);
		iv.startAnimation(animSet);
		
		animSet.setAnimationListener(new AnimationListener() {		
			@Override
			public void onAnimationStart(Animation animation) {}			
			@Override
			public void onAnimationRepeat(Animation animation) {}			
			@SuppressWarnings("unused")
			@Override
			public void onAnimationEnd(Animation animation) {
				if (iv != null) iv.setVisibility(View.GONE);
				String animaionID = animation.toString();
				if (LastAnimationID != null && animaionID.equalsIgnoreCase(LastAnimationID)) {
					if (animLayer == null) return;
					animLayer.removeAllViews();	
					addImageToPage(child,pageId,imageInfo);					
				}
			}
		});					
	}
	
	private void addImageToPage(ImageView child, String pageId,ImageInfo imageInfo){	
		if (child == null) return;
		synchronized (DealCollageImagesLayout.this) {
			child.setTag(imageInfo);
			child.setScaleType(ImageView.ScaleType.FIT_CENTER);		
			DealCollageImagesLayout.this.addView(child, addParams);	
		}

		if (dealImagesToCollagePageTasks == null) {
			dealImagesToCollagePageTasks = new DealImagesToCollagePageTasks(context, addHandler);
		}
		dealImagesToCollagePageTasks.dealTask(pageId, imageInfo,true);		
	}
	
	public void deleteImageInPage(String pageId, ImageInfo imageInfo){	
		if(pageId == null) return;
		if(imageInfo == null) return;	
		DealCollageImagesLayout.this.hideImageView(imageInfo);
		if (dealImagesToCollagePageTasks == null) {
			dealImagesToCollagePageTasks = new DealImagesToCollagePageTasks(context, addHandler);
		}
		dealImagesToCollagePageTasks.dealTask(pageId, imageInfo,false);				
	}	
	
	public boolean isWaitAddImageDone(String pageId){		
		if (dealImagesToCollagePageTasks == null) return false;		
		return dealImagesToCollagePageTasks.getTaskPo(pageId, false) != null;		
	}
	
	public void setTasksNull() {
		if (dealImagesToCollagePageTasks != null) {			
			dealImagesToCollagePageTasks.interrupt();			
			dealImagesToCollagePageTasks = null;
		}
	}	
	
	public boolean endTasks(){
		boolean isEndTask = false;
		if (dealImagesToCollagePageTasks == null) return true;
		if (dealImagesToCollagePageTasks.isAlive()) {			
			if (dealImagesToCollagePageTasks.dealImageToPageTaskList.size() == 0) {
				isEndTask = true;
				setTasksNull();				
			}else {
				isEndTask = false;
				dealImagesToCollagePageTasks.setSkipFlag();
			}			
		}else {
			isEndTask = true;
			setTasksNull();
		}	
		return isEndTask;
	}

	private void hideImageView(ImageInfo imageInfo){
		if (imageInfo == null) return;
		if (imageInfo.id == null) return;	
		synchronized (DealCollageImagesLayout.this) {
			int childSize = DealCollageImagesLayout.this.getChildCount();				
			for (int i = 0; i < childSize; i++) {
				View view = DealCollageImagesLayout.this.getChildAt(i);
				if (view != null && view instanceof ImageView) {
					Object obj = view.getTag();
					if(obj != null && obj instanceof ImageInfo ){
						ImageInfo viewTag = (ImageInfo) obj;
						if (viewTag.id == null) continue;
						if (!viewTag.id.equals(imageInfo.id)) continue;												
						DealCollageImagesLayout.this.removeView(view);
						break;						
					}
				}
			}
		}	
	}	
	
	
}
