package com.kodak.rss.tablet.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.kodak.rss.tablet.R;

/**
 * Purpose: Up and down slider controls  only supports the landscape now
 * Author: Bing Wang
 * Created Time: Aug 19, 2013 9:20:43 AM 
 */
public class SourcePanel extends LinearLayout {

	private int mPosition;	
	private int panelEditContentId;		
	private int mHandleId;
	private int mContentId;
	public View panelEditContent;	
	private View mHandle;
	public View mContent;	

	public static final int TOP = 0;
	public static final int BOTTOM = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;

	public int mContentHeight;	
	public int panelEditContentHeight;	
	private int mOrientation;	
	private boolean mBringToFront;
	public LayoutParams contentParams;
	public LayoutParams  panelEditContentParams;	
	public int maxContentHeight;
	public int maxpanelEditContentHeight;   	
	public int maxOpenContentHeight;
	
	public int orgHeight;
	public OnOpenAndCloseListener mOnOpenAndCloseListener;
	public OnSizeChangeListener mOnSizeChangeListener;

	public SourcePanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Panel);	
		mPosition = a.getInteger(R.styleable.Panel_position, BOTTOM); // position defaults to BOTTOM	

		RuntimeException e = null;
		
		panelEditContentId = a.getResourceId(R.styleable.Panel_panelEditContent, 0);
		if (panelEditContentId == 0) {
			e = new IllegalArgumentException(a.getPositionDescription()
				+ ": The handle attribute is required and must refer to a valid child.");
		}			
		mHandleId = a.getResourceId(R.styleable.Panel_handle, 0);
		if (mHandleId == 0) {
			e = new IllegalArgumentException(a.getPositionDescription()
				+ ": The handle attribute is required and must refer to a valid child.");
		}
		mContentId = a.getResourceId(R.styleable.Panel_content, 0);
		if (mContentId == 0) {
			e = new IllegalArgumentException(a.getPositionDescription()
				+ ": The content attribute is required and must refer to a valid child.");
		}
		a.recycle();

		if (e != null) {
			throw e;
		}
		mOrientation = (mPosition == TOP || mPosition == BOTTOM) ? VERTICAL : HORIZONTAL;
		setOrientation(mOrientation);			
		setBaselineAligned(false);				
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();		
		panelEditContent = findViewById(panelEditContentId);
		if (panelEditContent == null) {
			String name = getResources().getResourceEntryName(panelEditContentId);
			throw new RuntimeException("Your Panel must have a child View whose id attribute is 'R.id." + name + "'");
		}
		
		mHandle = findViewById(mHandleId);
		if (mHandle == null) {
			String name = getResources().getResourceEntryName(mHandleId);
			throw new RuntimeException("Your Panel must have a child View whose id attribute is 'R.id." + name + "'");
		}
		mHandle.setOnTouchListener(touchListener);
		mHandle.setOnClickListener(clickListener);

		mContent = findViewById(mContentId);
		if (mContent == null) {
			String name = getResources().getResourceEntryName(mHandleId);
			throw new RuntimeException("Your Panel must have a child View whose id attribute is 'R.id." + name + "'");
		}
		removeView(panelEditContent);		
		removeView(mHandle);
		removeView(mContent);
		if (mPosition == TOP || mPosition == LEFT) {
			addView(mContent);
			addView(mHandle);			
			addView(panelEditContent);			
		} else {
			addView(panelEditContent);						
			addView(mHandle);
			addView(mContent);
		}
		
		mHandle.setClickable(true);	
		mContent.setClickable(true);		
		contentParams = (LayoutParams) mContent.getLayoutParams();
		panelEditContentParams = (LayoutParams) panelEditContent.getLayoutParams();	
		
	}
	
	public boolean setHandleEnable(boolean isEnable){
		boolean isReaToOther = false;
		boolean enable = mHandle.isEnabled();
		if (!enable && isEnable) {
			isReaToOther = true;
		}				
		mHandle.setEnabled(isEnable);
		View searchButton = mHandle.findViewById(R.id.search_button);
		if (searchButton != null) {
			searchButton.setEnabled(isEnable);
		}		
		return isReaToOther;
	}
	
	public void setOnOpenAndCloseListener(OnOpenAndCloseListener listener){
		this.mOnOpenAndCloseListener = listener;
	}
	
	public void setOnSizeChangeListener(OnSizeChangeListener onSizeChangeListener) {
		this.mOnSizeChangeListener = onSizeChangeListener;
	}

	public View getContent() {
		return mContent;
	}
	
	public void setpanelEditContentHeight(int otherWidgetHeight) {
		DisplayMetrics dm = getResources().getDisplayMetrics(); 		 		
		int value = (int) (dm.density * otherWidgetHeight + 0.5f);
        maxContentHeight = (dm.heightPixels-otherWidgetHeight)/2;                      	
        maxpanelEditContentHeight = dm.heightPixels - value;       
	}
	
	public void setMaxPanelEditContentHeight(int otherWidgetHeight) {
		DisplayMetrics dm = getResources().getDisplayMetrics(); 		 		
		int value = (int) (dm.density * otherWidgetHeight + 0.5f);                           	
        maxpanelEditContentHeight = dm.heightPixels - value;           
	}
	public void setMaxContentHeight(int otherWidgetHeight) {
		DisplayMetrics dm = getResources().getDisplayMetrics(); 		 				                         	
		maxContentHeight = (dm.heightPixels-otherWidgetHeight)/2;             
	}
	
	public void setpanelContentHeight(int otherWidgetHeight) {
		DisplayMetrics dm = getResources().getDisplayMetrics(); 		 		
		int value = (int) (dm.density * otherWidgetHeight + 0.5f);                           
		maxpanelEditContentHeight = maxContentHeight = dm.heightPixels - value;              
	}	

	public void initPanelContentHeight(int otherWidgetHeight) {
		setpanelContentHeight(otherWidgetHeight);       
        panelEditContentParams.height = maxpanelEditContentHeight;	
		panelEditContent.setLayoutParams(panelEditContentParams);	
	}	

	public void setpanelEditContentVisible(boolean  visible) {
		if(visible){
			panelEditContent.setVisibility(View.VISIBLE);				
			if(mContentHeight > maxContentHeight){							
				mContentHeight = maxContentHeight;
			}									
			panelEditContentHeight = maxpanelEditContentHeight - mContentHeight;			
			panelEditContentParams.height = panelEditContentHeight;
            panelEditContent.setLayoutParams(panelEditContentParams);				
		}else {
			panelEditContent.setVisibility(View.INVISIBLE);
		}		
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		ViewParent parent = getParent();
		if (parent != null && parent instanceof FrameLayout) {
			mBringToFront = true;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);		
		mContentHeight = mContent.getHeight();			
		panelEditContentHeight = panelEditContent.getHeight();
	}

	OnTouchListener touchListener = new OnTouchListener() {			
		float lastY=0; boolean flag,moved;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action=event.getAction();
			switch (action) {			
			case MotionEvent.ACTION_DOWN:				
				flag = false;
				lastY=event.getRawY();
				moved = false;
				break;			
			case MotionEvent.ACTION_MOVE:
				if(!moved){
					if(mOnSizeChangeListener != null){
						mOnSizeChangeListener.onSizeChangeStart(SourcePanel.this, mContentHeight);
					}
					
					moved = true;
				}
				float distanceY=event.getRawY()-lastY;
				mContentHeight = (int) (mContentHeight - distanceY) >= 0 ? (int) (mContentHeight - distanceY):0;	
				panelEditContentHeight = (int) (panelEditContentHeight +  distanceY) >= 0 ? (int)(panelEditContentHeight +  distanceY):0;			
				if(panelEditContent.getVisibility() !=View.VISIBLE){					
					if(distanceY < 0){
						if(mContentHeight > maxContentHeight){	
							panelEditContentHeight = 0;
							mContentHeight = maxContentHeight;
						}						
						if(panelEditContentHeight + mContentHeight > maxContentHeight){
							panelEditContentHeight = maxContentHeight - mContentHeight >= 0 ?(maxContentHeight - mContentHeight):0;						
						}else{
							panelEditContentHeight = maxContentHeight - mContentHeight;
						}
					}else{
						panelEditContentHeight = maxContentHeight - mContentHeight  >= 0 ?(maxContentHeight - mContentHeight):0;
					}								
				}else{						
					if(distanceY < 0){
						if(mContentHeight > maxContentHeight){							
							mContentHeight = maxContentHeight;
						}						
						if(panelEditContentHeight + mContentHeight > maxpanelEditContentHeight){
							panelEditContentHeight = maxpanelEditContentHeight - mContentHeight >= 0 ?(maxpanelEditContentHeight - mContentHeight):0;
						}else{
							panelEditContentHeight = maxpanelEditContentHeight - mContentHeight;
							
							
						}
					}else{
						panelEditContentHeight = maxpanelEditContentHeight - mContentHeight  >= 0 ?(maxpanelEditContentHeight - mContentHeight):0;
						
					}
				}
				
				panelEditContentParams.height = panelEditContentHeight; 
				panelEditContent.setLayoutParams(panelEditContentParams);
				
				if(mOnSizeChangeListener != null){
					mOnSizeChangeListener.onSizeChanged(SourcePanel.this, mContentHeight);
				}
				
				oldHeight =	mContentHeight;
				contentParams.height = mContentHeight;	
				mContent.setLayoutParams(contentParams);
				invalidate();				
				lastY=event.getRawY();
				flag =  true;	
				break;
			case MotionEvent.ACTION_UP:
				if(moved){
					if(mOnSizeChangeListener != null){
						mOnSizeChangeListener.onSizeChangeEnd(SourcePanel.this, mContentHeight);
					}
				}
				break;
			}
			return flag;			
		}
	};
	
	OnClickListener clickListener = new OnClickListener() {
		public void onClick(View v) {
			setOpenAndClose();		
    	}
	};
	
	public int oldHeight;
	public void setOpenAndClose(){
		int oldh = mContentHeight;
		int mMaxContentHeight = maxContentHeight;
		if (maxOpenContentHeight > 0) {
			mMaxContentHeight = maxOpenContentHeight;
		}		
		if (mBringToFront) bringToFront();
		
		if (orgHeight > 0) {
			if(mContentHeight >= 0 && mContentHeight <= 5){				
				mContentHeight = orgHeight - 1;				
			}else if(mContentHeight > mMaxContentHeight - 5){				
				mContentHeight = orgHeight + 1;				
			}else if(mContentHeight > 5 && mContentHeight < orgHeight - 5){
				mContentHeight = 0;
			}else if(mContentHeight > orgHeight + 5 && mContentHeight <= mMaxContentHeight - 5){
				mContentHeight = mMaxContentHeight;
			}else if(mContentHeight >= orgHeight - 5 && mContentHeight <= orgHeight + 5){
				if (oldHeight < orgHeight) {
					mContentHeight = mMaxContentHeight;
				}else {
					mContentHeight = 0;
				}
			}			
		}else {
			if(mContentHeight >= 0 && mContentHeight <= 5 ){
				mContentHeight = mMaxContentHeight;
			}else if(mContentHeight > 5 && mContentHeight <= mMaxContentHeight/2){
				mContentHeight = 0;
			}else if(mContentHeight > mMaxContentHeight/2 && mContentHeight <= mMaxContentHeight - 5){
				mContentHeight = mMaxContentHeight;
			}else if(mContentHeight > mMaxContentHeight - 5){
				mContentHeight = 0;
			}
		}
		oldHeight =	mContentHeight;
		
		if(panelEditContent.getVisibility() !=View.VISIBLE){		
			panelEditContentHeight = mContentHeight == mMaxContentHeight ? 0 : mMaxContentHeight;																			
		}else{	
			panelEditContentHeight = mContentHeight == 0 ? maxpanelEditContentHeight :maxpanelEditContentHeight - mContentHeight;
		}
		
		if (mOnOpenAndCloseListener != null) {
			mOnOpenAndCloseListener.onOpenAndCloseStart(SourcePanel.this, oldh, mContentHeight);
		}

		panelEditContentParams.height = panelEditContentHeight;	
		panelEditContent.setLayoutParams(panelEditContentParams);	
		
		contentParams.height = mContentHeight;	
		mContent.setLayoutParams(contentParams);

		invalidate();	
		
		if (mOnOpenAndCloseListener != null) {
			mOnOpenAndCloseListener.onOpenAndCloseEnd(SourcePanel.this, oldh, mContentHeight);
		}
	}
	
	public void setMaxOpenContentHeight(int maxOpenHeihgt) {
		int oldh = mContentHeight;
		DisplayMetrics dm = getResources().getDisplayMetrics(); 		 		
		int value = (int) (dm.density * maxOpenHeihgt + 0.5f);                           
		maxOpenContentHeight = dm.heightPixels - value; 
				
		mContentHeight = maxOpenContentHeight;
		panelEditContentHeight = maxpanelEditContentHeight - maxOpenContentHeight;	
		if(mOnOpenAndCloseListener != null){
			mOnOpenAndCloseListener.onOpenAndCloseStart(SourcePanel.this, oldh, mContentHeight);
		}
		
		panelEditContentParams.height = panelEditContentHeight;	
		panelEditContent.setLayoutParams(panelEditContentParams);
		
		oldHeight =	mContentHeight;
		contentParams.height = mContentHeight;	
		mContent.setLayoutParams(contentParams);
		invalidate();	
		
		if(mOnOpenAndCloseListener != null){
			mOnOpenAndCloseListener.onOpenAndCloseEnd(SourcePanel.this, oldh, mContentHeight);
		}
	}
	
	public void setOpenContentHeight(int maxOpenHeight) {
		orgHeight = maxOpenHeight;
		
		int oldh = mContentHeight;	
		mContentHeight = maxOpenHeight;
		panelEditContentHeight = maxpanelEditContentHeight - mContentHeight;	
		if(mOnOpenAndCloseListener != null){
			mOnOpenAndCloseListener.onOpenAndCloseStart(SourcePanel.this, oldh, mContentHeight);
		}
		
		panelEditContentParams.height = panelEditContentHeight;	
		panelEditContent.setLayoutParams(panelEditContentParams);
		
		oldHeight =	mContentHeight;
		contentParams.height = mContentHeight;	
		mContent.setLayoutParams(contentParams);
		invalidate();	
		
		if(mOnOpenAndCloseListener != null){
			mOnOpenAndCloseListener.onOpenAndCloseEnd(SourcePanel.this, oldh, mContentHeight);
		}
	}
	
	
	public int getPanelEditContentHeight(){
		return panelEditContentHeight;
	}
	
	public interface OnOpenAndCloseListener {
		public void onOpenAndCloseStart(SourcePanel sourcePanel, int oldh, int newh);
		public void onOpenAndCloseEnd(SourcePanel sourcePanel, int oldh, int newh);
	}
	
	public interface OnSizeChangeListener {
		public void onSizeChangeStart(SourcePanel sourcePanel, int h);
		public void onSizeChanged(SourcePanel sourcePanel, int h);
		public void onSizeChangeEnd(SourcePanel sourcePanel, int h);
	}
	
}
