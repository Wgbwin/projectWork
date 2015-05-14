package com.kodak.rss.tablet.view;

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
import com.kodak.rss.core.n2r.bean.calendar.CalendarGridItemPO;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.thread.calendar.AddImagesToCalendarPageTasks;
import com.kodak.rss.tablet.view.dialog.DialogUploadImageError;

public class AddImagesLayout extends RelativeLayout{

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
			if (context instanceof CalendarEditActivity && ((CalendarEditActivity)context).isFinishing()) return;										
			Object obj = msg.obj;			
			switch (msg.what) {
			case HANDLER_ADD_START:				
				break;
			case HANDLER_ADD:
				if (context instanceof CalendarEditActivity) {
					((CalendarEditActivity)context).notifyCalendarPagesChanged();
				}
				if (obj != null) {
					@SuppressWarnings("unchecked")
					List<ImageInfo> imageInfoList = (List<ImageInfo>) obj;
					for (ImageInfo imageInfo : imageInfoList) {
						AddImagesLayout.this.hideImageView(imageInfo);
					}
				}
				break;
			case HANDLER_ADD_END:				
				setTasksNull();				
				break;		
			case HANDLER_ADD_ERROR:
				if (obj != null) {
					if(obj instanceof RssWebServiceException){
						((CalendarEditActivity)context).showErrorWarning((RssWebServiceException) obj);
						
					}else if(obj instanceof ImageInfo){
						ImageInfo failInfo = (ImageInfo) obj;
						if (failInfo != null && failInfo.imageOriginalResource == null && !((CalendarEditActivity)context).isHaveUploadErrorDialog) {									
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
						AddImagesLayout.this.hideImageView(imageInfo);
					}
				}
				break;				
			}
		}	
	};
	
	private AddImagesToCalendarPageTasks addImagesToCalendarPageTasks;
	
	public AddImagesLayout(Context context) {
		super(context);	
		init(context);
	}

	public AddImagesLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public AddImagesLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		this.context = context;
		this.addParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addImagesToCalendarPageTasks = new AddImagesToCalendarPageTasks(context, addHandler);
	}	
	
	private int displayImageX,displayImageY,displayImageW,displayImageH;	
	public void addImageToPageUseAnimation(final RelativeLayout animLayer,View view, final String pageId,final ImageInfo imageInfo,final int holeIndex){
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
	    	AddImagesLayout.this.getLocationOnScreen(locationDisplayImage);	
			displayImageX = locationDisplayImage[0];
			displayImageY = locationDisplayImage[1];
			displayImageW = AddImagesLayout.this.getWidth();
			displayImageH = AddImagesLayout.this.getHeight(); 	
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
					addImageToPage(child,pageId,imageInfo,null,holeIndex);					
				}
			}
		});					
	}
	
	private void addImageToPage(ImageView child, String pageId,ImageInfo imageInfo,CalendarGridItemPO gridItemPo,int holeIndex){	
		if (child == null) return;
		synchronized (AddImagesLayout.this) {
			child.setTag(imageInfo);
			child.setScaleType(ImageView.ScaleType.FIT_CENTER);		
			AddImagesLayout.this.addView(child, addParams);	
		}

		if (addImagesToCalendarPageTasks == null) {
			addImagesToCalendarPageTasks = new AddImagesToCalendarPageTasks(context, addHandler);
		}
		addImagesToCalendarPageTasks.addTask(pageId, imageInfo,gridItemPo,holeIndex);		
	}
	
	public void addImageToPage(Bitmap bitmap, String pageId,ImageInfo imageInfo,int holeIndex){	
		if(pageId == null) return;
		if(imageInfo == null) return;
		final ImageView child = new ImageView(context);
		if (bitmap != null) {						
			child.setImageBitmap(bitmap);
		}else{			
			child.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.imagewait60x60));
		}
		addImageToPage(child, pageId, imageInfo,null,holeIndex);		
	}		
	
	public void addImageToPage(Bitmap bitmap, String pageId,ImageInfo imageInfo,CalendarGridItemPO gridItemPo,int holeIndex){	
		if(pageId == null) return;
		if(imageInfo == null) return;
		if(gridItemPo == null) return;
		final ImageView child = new ImageView(context);
		if (bitmap != null) {						
			child.setImageBitmap(bitmap);
		}else{			
			child.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.imagewait60x60));
		}
		addImageToPage(child, pageId, imageInfo, gridItemPo,holeIndex);		
	}		
	
	public boolean isWaitAddImageDone(String pageId){		
		if (addImagesToCalendarPageTasks == null) return false;		
		return addImagesToCalendarPageTasks.getTaskPo(pageId, false) != null;		
	}
	
	public void setTasksNull() {
		if (addImagesToCalendarPageTasks != null) {			
			addImagesToCalendarPageTasks.interrupt();			
			addImagesToCalendarPageTasks = null;
		}
	}	
	
	public boolean endTasks(){
		boolean isEndTask = false;
		if (addImagesToCalendarPageTasks == null) return true;
		if (addImagesToCalendarPageTasks.isAlive()) {			
			if (addImagesToCalendarPageTasks.addImageToPageTaskList.size() == 0) {
				isEndTask = true;
				setTasksNull();				
			}else {
				isEndTask = false;
				addImagesToCalendarPageTasks.setSkipFlag();
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
		if (imageInfo.imageOriginalResource == null) return;
		if (imageInfo.imageOriginalResource.id == null) return;
		synchronized (AddImagesLayout.this) {
			int childSize = AddImagesLayout.this.getChildCount();				
			for (int i = 0; i < childSize; i++) {
				View view = AddImagesLayout.this.getChildAt(i);
				if (view != null && view instanceof ImageView) {
					Object obj = view.getTag();
					if(obj != null && obj instanceof ImageInfo ){
						ImageInfo viewTag = (ImageInfo) obj;
						if (viewTag.id == null) continue;
						if (!viewTag.id.equals(imageInfo.id)) continue;						
						if (viewTag.imageOriginalResource != null && viewTag.imageOriginalResource.id != null 
								&& viewTag.imageOriginalResource.id.equals(imageInfo.imageOriginalResource.id)) {
							AddImagesLayout.this.removeView(view);
							break;
						}
					}
				}
			}
		}	
	}	
	
}
