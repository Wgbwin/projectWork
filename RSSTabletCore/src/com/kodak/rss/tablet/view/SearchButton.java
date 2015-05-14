package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class SearchButton extends Button{
	
	private int moveCount = 0;
	public SourcePanel panel;
	
	public SearchButton(Context context) {
		super(context);	
		init(context);
	}
	
	public SearchButton(Context context, AttributeSet attrs) {
		super(context, attrs);		
	}

	public SearchButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		
	}
	
	public void setSourcePanel(SourcePanel sPanel){
		this.panel = sPanel;
		SearchButton.this.setOnTouchListener(touchListener);
	}

	private OnTouchListener touchListener = new OnTouchListener() {			
		float lastY=0; boolean flag,moved;

		@Override
		public boolean onTouch(View v, MotionEvent event) {			
			if (panel == null) return false;
			int action=event.getAction();
			switch (action) {			
			case MotionEvent.ACTION_DOWN:
				moveCount = 0;
				flag = false;
				lastY=event.getRawY();
				moved = false;
				break;			
			case MotionEvent.ACTION_MOVE:
				moveCount++;
				if (moveCount < 2 ) {
					lastY = event.getRawY();
					flag =  false;	
					break;
				}
				if(!moved){
					if(panel.mOnSizeChangeListener != null){
						panel.mOnSizeChangeListener.onSizeChangeStart(panel,panel.mContentHeight);
					}					
					moved = true;
				}
				float distanceY=event.getRawY()-lastY;
				panel.mContentHeight = (int) (panel.mContentHeight - distanceY) >= 0 ? (int) (panel.mContentHeight - distanceY):0;	
				panel.panelEditContentHeight = (int) (panel.panelEditContentHeight +  distanceY) >= 0 ? (int)(panel.panelEditContentHeight +  distanceY):0;			
				if(panel.panelEditContent.getVisibility() !=View.VISIBLE){					
					if(distanceY < 0){
						if(panel.mContentHeight > panel.maxContentHeight){	
							panel.panelEditContentHeight = 0;
							panel.mContentHeight = panel.maxContentHeight;
						}						
						if(panel.panelEditContentHeight + panel.mContentHeight > panel.maxContentHeight){
							panel.panelEditContentHeight = panel.maxContentHeight - panel.mContentHeight >= 0 ?(panel.maxContentHeight - panel.mContentHeight):0;						
						}else{
							panel.panelEditContentHeight = panel.maxContentHeight - panel.mContentHeight;
						}
					}else{
						panel.panelEditContentHeight = panel.maxContentHeight - panel.mContentHeight  >= 0 ?(panel.maxContentHeight -panel.mContentHeight):0;
					}								
				}else{						
					if(distanceY < 0){
						if(panel.mContentHeight > panel.maxContentHeight){							
							panel.mContentHeight = panel.maxContentHeight;
						}						
						if(panel.panelEditContentHeight + panel.mContentHeight > panel.maxpanelEditContentHeight){
							panel.panelEditContentHeight = panel.maxpanelEditContentHeight - panel.mContentHeight >= 0 ?(panel.maxpanelEditContentHeight - panel.mContentHeight):0;
						}else{
							panel.panelEditContentHeight = panel.maxpanelEditContentHeight - panel.mContentHeight;
						}
					}else{
						panel.panelEditContentHeight = panel.maxpanelEditContentHeight - panel.mContentHeight  >= 0 ?(panel.maxpanelEditContentHeight - panel.mContentHeight):0;						
					}
				}
				
				panel.panelEditContentParams.height = panel.panelEditContentHeight; 
				panel.panelEditContent.setLayoutParams(panel.panelEditContentParams);
				
				if(panel.mOnSizeChangeListener != null){
					panel.mOnSizeChangeListener.onSizeChanged(panel, panel.mContentHeight);
				}
				
				panel.oldHeight = panel.mContentHeight;
				panel.contentParams.height = panel.mContentHeight;	
				panel.mContent.setLayoutParams(panel.contentParams);
				invalidate();				
				lastY=event.getRawY();
				flag =  true;	
				break;
			case MotionEvent.ACTION_UP:	
				if (moveCount < 2 ) {
					panel.setOpenAndClose();
					flag =  true;	
					break;
				}
				
				if(moved){
					if(panel.mOnSizeChangeListener != null){
						panel.mOnSizeChangeListener.onSizeChangeEnd(panel, panel.mContentHeight);
					}
				}
				break;
			}
			return flag;			
		}
	};
	
	

}
