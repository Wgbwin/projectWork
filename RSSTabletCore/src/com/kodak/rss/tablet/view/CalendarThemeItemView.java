package com.kodak.rss.tablet.view;

import java.net.URI;
import java.net.URISyntaxException;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kodak.rss.core.n2r.bean.calendar.CalendarTheme;
import com.kodak.rss.core.n2r.bean.calendar.CalendarTheme.BackGround;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.CalendarThemeSelectionActivity;
import com.kodak.rss.tablet.adapter.CalendarThemeSelectionAdapter;
import com.kodak.rss.tablet.view.dialog.DialogShowPic;
import com.kodak.rss.tablet.view.dialog.DialogShowPic.onDialogListener;

/**
 * Purpose: 
 * Author: Bing Wang 
 */
public class CalendarThemeItemView extends LinearLayout{   
    private Context mContext;
	
	private ImageButton searchButton;
	private ImageView  themeContentView;	
	
	private CalendarThemeSelectionActivity themeActivity;	
	private CalendarTheme theme;
	private CalendarThemeSelectionAdapter mAdapter;
	private View layoutView;	
	private AbsListView.LayoutParams mLayoutParams;	

	public CalendarThemeItemView(Context context,int height) {
		super(context);
		this.mContext = context;
		initViewAndAction(height);	
	}	
	
	public CalendarThemeItemView(Context context, AttributeSet attrs) {
		super(context, attrs);	
		this.mContext = context;	
		initViewAndAction(0);
		
	}  		

	private  void initViewAndAction(int height){			
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutView = inflater.inflate(R.layout.calendar_theme_content, this);	
			
		searchButton = (ImageButton) findViewById(R.id.search_button);			
		themeContentView = (ImageView) findViewById(R.id.content);

		mLayoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, height);
				
		searchButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (themeActivity.curSelectedTheme == null ||((themeActivity.curSelectedTheme != null && theme !=null) && !theme.id.equals(themeActivity.curSelectedTheme.id))) {
					themeActivity.curSelectedTheme  = theme;					
					if (themeActivity.preItemView != null) {
						themeActivity.preItemView.setBackgroundResource(R.drawable.theme_backgroud_dis);
					}	
					setBackground();	
					themeActivity.preItemView = CalendarThemeItemView.this; 
				}else if (theme == null) {
					themeActivity.curSelectedTheme  = theme;					
					if (themeActivity.preItemView != null) {
						themeActivity.preItemView.setBackgroundResource(R.drawable.theme_backgroud_dis);
					}
					setBackground();
					themeActivity.preItemView = CalendarThemeItemView.this; 	
				}		
				
				if (!themeActivity.isExistShowPicDialog) {					
					if (theme != null) {
						BackGround[] backGrounds = theme.backGrounds;			
		    			if (backGrounds == null || theme.backGrounds.length < 1) return; 		    			
		    			DialogShowPic dialogShowPic = new DialogShowPic();
		    			dialogShowPic.setBackGrounds(mContext,backGrounds,mAdapter.mMemoryCache);
		    			dialogShowPic.createDialog(mContext, theme.name, new onDialogListener() {						
								@Override
								public void onDone() {
									themeActivity.isExistShowPicDialog = false;
								}
							});	
						themeActivity.isExistShowPicDialog = true;		    						    			
					}
				}
			}
		});	
				
		layoutView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {			
				if (themeActivity.curSelectedTheme == null ||((themeActivity.curSelectedTheme != null && theme !=null) && !theme.id.equals(themeActivity.curSelectedTheme.id))) {
					themeActivity.curSelectedTheme  = theme;					
					if (themeActivity.preItemView != null) {
						themeActivity.preItemView.setBackgroundResource(R.drawable.theme_backgroud_dis);
					}
					setBackground();
					themeActivity.preItemView = CalendarThemeItemView.this; 					
				}else if (theme == null) {
					themeActivity.curSelectedTheme  = theme;	
					if (themeActivity.preItemView != null) {
						themeActivity.preItemView.setBackgroundResource(R.drawable.theme_backgroud_dis);
					}
					setBackground();
					themeActivity.preItemView = CalendarThemeItemView.this; 	
				}		
			}
		});

	}
	
	private void setBackground(){
		this.setBackgroundResource(R.drawable.theme_backgroud_up);		
	}	
	
	private void replyBackgroud(){
		this.setBackgroundResource(R.drawable.theme_backgroud_dis);	
	}

	private URI getPictureURI(String glyphURL){
		URI pictureURI = null ;		    		   	    		
	    try {
	    	pictureURI = new URI(glyphURL);
	    } catch (URISyntaxException e) {
	    	pictureURI = null;
	    }
	    return pictureURI;
	} 
	
	public void setValue(CalendarThemeSelectionActivity activity,CalendarTheme theme,CalendarThemeSelectionAdapter mAdapter,int position){
		this.themeActivity = activity;		
		this.theme = theme;	
		this.mAdapter = mAdapter;	

		if ((themeActivity.curSelectedTheme != null && theme != null && theme.id.equals(themeActivity.curSelectedTheme.id))) {				
			setBackground();
			themeActivity.preItemView = CalendarThemeItemView.this; 	
		}else {
			replyBackgroud();
		}
				
		CalendarThemeItemView.this.setLayoutParams(mLayoutParams);		
		
		if (theme != null && theme.id != null && theme.glyphUrl != null && !"".equals(theme.glyphUrl)) {
			URI pictureURI = getPictureURI(theme.glyphUrl);
			themeContentView.setImageBitmap(mAdapter.getBitmap(theme.id, pictureURI, themeContentView, position));
		}else {
			themeContentView.setImageBitmap(null);
		}		
	}

}

