package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.CalendarPagesAdapter;

/**
 * Purpose: 
 * Author: Bing Wang 
 */
public class CalendarPagesItemView extends LinearLayout{   
	
    private CalendarPageView upImageView;
    private CalendarPageView downImageView;
   
    private CalendarPagesAdapter adapter;
    
    public CalendarPagesItemView(Context context) {
		super(context);		
	}
    
    public CalendarPagesItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
    
	public CalendarPagesItemView(Context context,CalendarPagesAdapter mAdapter) {
		super(context);		
		this.adapter = mAdapter;		
		initView(context);	
	}	

	private  void initView(Context context){
		inflate(context,R.layout.calendar_pages_item, this);	
						
		upImageView = (CalendarPageView) findViewById(R.id.upImage);
		downImageView = (CalendarPageView) findViewById(R.id.downImage);			
		if (adapter != null) {
			upImageView.setLayoutParams(adapter.mLayoutParams);		
			downImageView.setLayoutParams(adapter.mLayoutParams);												
		}		
	}
	
	public void setAdpter(CalendarPagesAdapter mAdapter){
		adapter = mAdapter;		
		upImageView.setLayoutParams(adapter.mLayoutParams);		
		downImageView.setLayoutParams(adapter.mLayoutParams);										
	}		
			
	public void setBasicValue(CalendarPage[] pageItem,int position ){			
		upImageView.setVisibility(View.VISIBLE);		
		downImageView.setVisibility(View.VISIBLE);	
		adapter.setValue(pageItem[0],upImageView, position,false,true);
		adapter.setValue(pageItem[1],downImageView, position,false,false);
	}

}
