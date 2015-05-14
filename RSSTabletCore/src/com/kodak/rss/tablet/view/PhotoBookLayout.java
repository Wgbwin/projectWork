package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.aphidmobile.flip.FlipViewController;
import com.aphidmobile.flip.FlipViewController.ViewFlipListener;
import com.aphidmobile.flip.FlipViewController.ZoomStatus;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.PhotoBooksProductMainAdapter;

public class PhotoBookLayout extends RelativeLayout{
	private final static String TAG = "PhotoBookLayout";
	
	/**
	 * when photobook zoom in, the main page will be center_horizontal, and there is a margin to left edge of screen. This value is the margin.
	 * This value will be inited when photobook size first inited. 
	 */
	private int pagePartWidth = 0;
	/**
	 * the photobook margin to left and right when it scale to the largest size without zoom in.
	 */
	private int minMarginSide = 70;
	
	public int width;
	public int height;
	private FlipViewController photobook;
	private int type = TYPE_DOUBLE;
	private PhotoBooksProductMainAdapter adapter;
	
	public static final int TYPE_SINGLE = 0;
	public static final int TYPE_DOUBLE = 1;

	public PhotoBookLayout(Context context) {
		super(context);
		init();
	}
	
	public PhotoBookLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public PhotoBookLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public int getType(){
		return type;
	}
	
	public void setAdapter(PhotoBooksProductMainAdapter pbAdapter){
		this.adapter = pbAdapter;
		photobook.setAdapter(pbAdapter);
	}
	
	/**
	 * Note: In PhotoBookMainAdapter.notifyDataSetChanged(), it has done this method. So If you don't need to do this again if you have notifydatasetChanged.
	 */
	public void refreshAllPages(){
		photobook.refreshAllPages();
	}
	
	public void setPhotobookSize(double width,double height){
		photobook.photoBookWidth = width;
		photobook.photoBookHeight = height;
	}
	
	public void setMaxSize(int maxWidth,int maxHeight){
		photobook.maxWidth = maxWidth;
		photobook.maxHeight = maxHeight;
		
	}
	
	public void setPhotoBookSize(double width, double height){
		photobook.photoBookWidth = width;
		photobook.photoBookHeight = height;
	}
	
	public FlipViewController getFlipViewController(){
		return photobook;
	}
	
	public ZoomStatus getZoomStatus(){
		return photobook.zoomStatus;
	}
	
	public void setOnViewFlipListener(ViewFlipListener viewFlipListener){
		photobook.setOnViewFlipListener(viewFlipListener);
	}
	
	/**
	 * see {@link PhotoBooksProductMainAdapter.getViewByPosition(position) }
	 * @param position
	 * @return
	 */
	public PhotoBookMainItemView getViewByPosition(int position){
		return adapter.getViewByPosition(position);
	}
	
	public void goToEditLayer(PhotoBookEditLayer layerEdit){
		
	}
	
	
	/**
	 * Be careful. One position may have two page
	 * @return
	 */
	public int getCurrentPosition(){
		return photobook.getSelectedItemPosition();
	}
	
	public void zoomIn(boolean withAnim){
		photobook.zoomStatus.leftPage = false;
		photobook.zoomStatus.isZoomIn = true;
		
		int oldh = photobook.getHeight();
		int newh = photobook.maxHeight;
		
		setLayoutParamsWhenZoomIn();
		requestLayout();
		
		if(withAnim){
			startPbZoomAnimation(oldh, newh, photobook.zoomStatus.leftPage);
		}else{
			photobook.refreshAllPages();
		}
	}
	
	public void zoomOut(boolean withAnim){
		int newh = photobook.getLayoutHeightByWidth(photobook.maxWidth);
		zoomOut(withAnim, newh);
	}
	
	public void zoomOut(boolean withAnim, int newh){
		photobook.zoomStatus.isZoomIn = false;
		if(newh<0){
			//In this case, the photobook is unvisible
			//And if we add animation, there will be a bug(RSSMOBILE-PDC 1275)
			withAnim = false;
		}
		if(newh < photobook.minHeight){
			newh = photobook.minHeight;
		}else if(newh > photobook.maxHeight) {
			newh = photobook.maxHeight;
		}
		int oldh = photobook.getHeight();
		LayoutParams paramsPb = (LayoutParams) photobook.getLayoutParams();
		paramsPb.setMargins(0, 0, 0, 0);
		
		if(withAnim){
			//if there is a animation, layout size should change after animation,not before
			paramsPb.height = newh;
			paramsPb.width = photobook.getLayoutWidthByHeight(newh);
			paramsPb.leftMargin = (photobook.maxWidth - paramsPb.width)/2;
			photobook.requestLayout();
			
			startPbZoomAnimation(oldh, newh, photobook.zoomStatus.leftPage);
		}else{
			changeSize(newh);
			photobook.refreshAllPages();
			
		}
	}
	
	public void zoomInLeft(boolean withAnim){
		if(photobook.zoomStatus.isZoomIn && !photobook.zoomStatus.leftPage){
			photobook.zoomStatus.leftPage = true;
			setLayoutParamsWhenZoomIn();
			requestLayout();
			
			if(withAnim){
				startPbMoveAnimation(false);
			}
			
		}
	}
	
	public void zoomInRight(boolean withAnim){
		if(photobook.zoomStatus.isZoomIn && photobook.zoomStatus.leftPage){
			photobook.zoomStatus.leftPage = false;
			setLayoutParamsWhenZoomIn();
			requestLayout();
			if(withAnim){
				startPbMoveAnimation(true);
			}
		}
	}
	
	public void pageTo(int index){
		photobook.flipToPage(index);
	}
	
	public void changeSize(int height){
		if(photobook.zoomStatus.isZoomIn){
			zoomOut(false,height);
		}
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = height;
		requestLayout();
	}
	
	boolean inited = false;
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if(!inited){
			inited = true;
			Log.i(TAG,"maxSize:"+w+","+h);
//			photobook.maxWidth = w;
//			photobook.maxHeight = h;
			photobook.marginSide = DimensionUtil.dip2px(getContext(), FlipViewController.MARIN_LEFT_RIGHT_SIDE);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			params.width = LayoutParams.WRAP_CONTENT;
			params.height = LayoutParams.WRAP_CONTENT;
			
			pagePartWidth = getPagePartWidth(w, h);
		}
		width = w;
		height = h;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = MeasureSpec.getSize(heightMeasureSpec);
		if(height<photobook.minHeight){
			height = photobook.minHeight;
			width = photobook.getLayoutWidthByHeight(height);
			super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		}else{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
		
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}
	
	private void init(){
		photobook = (FlipViewController) LayoutInflater.from(getContext()).inflate(R.layout.photobook, null);
		addView(photobook);
		photobook.setPhotoBookLayout(this);
	}
	
	private int getPagePartWidth(int maxWidth,int maxHeight){
		int pageWidth = photobook.getLayoutWidthByHeight(maxHeight)/2;
		return (maxWidth - pageWidth)/2;
	}
	
	private int getPbMarginInScreen(int layoutWidth){
		return photobook.maxWidth - layoutWidth/2-pagePartWidth;
	}
	
	private int getPbMargiOutOfScreen(int layoutWidth){
		return -layoutWidth/2+pagePartWidth;
	}
	
	private void setLayoutParamsWhenZoomIn(){
		RelativeLayout.LayoutParams paramLayout = (RelativeLayout.LayoutParams) this.getLayoutParams();
		int layoutWidth = photobook.getLayoutWidthWhenZoomIn();
		paramLayout.width = photobook.maxWidth;
		paramLayout.height = photobook.maxHeight;
		LayoutParams paramsPb = (LayoutParams) photobook.getLayoutParams();
		paramsPb.width = layoutWidth;
		paramsPb.height = photobook.maxHeight;
		if(photobook.zoomStatus.leftPage){
			paramsPb.setMargins(getPbMarginInScreen(layoutWidth), 0, getPbMargiOutOfScreen(layoutWidth), 0);
		}else{
			paramsPb.setMargins(getPbMargiOutOfScreen(layoutWidth), 0, getPbMarginInScreen(layoutWidth), 0);
		}
	}
	
	private void startPbMoveAnimation(boolean zoomInRight){
		TranslateAnimation ta;
		if(zoomInRight){
			ta = new TranslateAnimation(photobook.maxWidth-pagePartWidth*2, 0, 0, 0);
		}else{
			ta = new TranslateAnimation(-photobook.maxWidth+pagePartWidth*2, 0, 0, 0);
		}
		ta.setDuration(200);
		ta.setInterpolator(new AccelerateDecelerateInterpolator());
		photobook.startAnimation(ta);
	}
	
	private void startPbZoomAnimation(final int oldh,final int newh,boolean leftPage){
		Log.i(TAG,"startPbZoomAnimation oldh:"+ oldh + "  newh:"+ newh + "  leftpage:"+leftPage);
		if(oldh == newh){
			return;
		}
		
		final boolean zoomIn = newh > oldh;
		
		float scaleFrom = (float)oldh/newh;
		int smallMargin = (photobook.maxWidth - photobook.getLayoutWidthByHeight(oldh))/2;
		int largeMargin = getPbMarginInScreen(photobook.getLayoutWidthByHeight(newh));
		int moveX = largeMargin - smallMargin;
		TranslateAnimation ta;
		ScaleAnimation sa;
		if(zoomIn){//zoom in
			sa = new ScaleAnimation(scaleFrom, 1.0f, scaleFrom, 1.0f,Animation.RELATIVE_TO_SELF,leftPage ? 0.0f : 1.0f,Animation.RELATIVE_TO_SELF,0.0f);
			ta = new TranslateAnimation(leftPage ? -moveX : moveX, 0, 0, 0);
		}else{//zoom out
			sa = new ScaleAnimation(scaleFrom, 1.0f, scaleFrom, 1.0f,Animation.RELATIVE_TO_SELF,leftPage ? 1.0f : 0.0f,Animation.RELATIVE_TO_SELF,0.0f);
			ta = new TranslateAnimation(leftPage ? moveX : -moveX, 0, 0, 0);
		}
		AnimationSet as = new AnimationSet(true);
		as.addAnimation(sa);
		as.addAnimation(ta);
		as.setDuration(200);
		as.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				if(!zoomIn){
					LayoutParams paramsPb = (LayoutParams) photobook.getLayoutParams();
					paramsPb.width = LayoutParams.MATCH_PARENT;
					paramsPb.height = LayoutParams.MATCH_PARENT;
					paramsPb.leftMargin = 0;
					changeSize(newh);
				}
				photobook.refreshAllPages();
			}
		});
		photobook.startAnimation(as);
	}
	
}
