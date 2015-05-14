package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.bean.content.Theme.BackGround;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.PhotoBooksThemeSelectActivity;
import com.kodak.rss.tablet.adapter.PhotobookThemeSelectionAdapter;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.DialogShowPic;
import com.kodak.rss.tablet.view.dialog.DialogShowPic.onDialogListener;

/**
 * Purpose: 
 * Author: Bing Wang 
 */
public class ThemeItemView extends LinearLayout{   
    private Context mContext;
	private TextView themeName;
	private TextView themeNum;
	private ImageButton searchButton;
	private ThemeItemContentView  themeContentView;	
	
	private PhotoBooksThemeSelectActivity themeActivity;
	private PhotobookThemeSelectionAdapter adapter;
	private Theme theme;
	private View layoutView;
	private int position;	
	private Photobook currentPhotoBook = null;

	public ThemeItemView(Context context) {
		super(context);
		this.mContext = context;
		initViewAndAction();	
	}	
	
	public ThemeItemView(Context context, AttributeSet attrs) {
		super(context, attrs);	
		this.mContext = context;	
		initViewAndAction();
		
	}  		

	private  void initViewAndAction(){			
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutView = inflater.inflate(R.layout.photobook_theme_content, this);	
		
		themeName = (TextView) findViewById(R.id.theme_name);
		themeNum = (TextView) findViewById(R.id.theme_num);			
		searchButton = (ImageButton) findViewById(R.id.search_button);			
		themeContentView = (ThemeItemContentView) findViewById(R.id.content);
		
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();

		searchButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (themeActivity.curSelectedTheme == null ||((themeActivity.curSelectedTheme != null && theme !=null) && !theme.id.equals(themeActivity.curSelectedTheme.id))) {
					themeActivity.curSelectedTheme  = theme;
					themeActivity.curSelectedPosition = position;
					if (themeActivity.preItemView != null) {
						themeActivity.preItemView.setBackgroundResource(R.drawable.theme_backgroud_dis);
					}	
					setBackground();	
					themeActivity.preItemView = ThemeItemView.this; 
				}else if (theme == null) {
					themeActivity.curSelectedTheme  = theme;
					themeActivity.curSelectedPosition = position;
					if (themeActivity.preItemView != null) {
						themeActivity.preItemView.setBackgroundResource(R.drawable.theme_backgroud_dis);
					}
					setBackground();
					themeActivity.preItemView = ThemeItemView.this; 	
				}		
				
				if (!themeActivity.isExistShowPicDialog) {					
					if (theme != null) {
						BackGround[] backGrounds = theme.backGrounds;			
		    			if (backGrounds == null || theme.backGrounds.length < 1) return; 		    			
		    			DialogShowPic dialogShowPic = new DialogShowPic();
		    			dialogShowPic.setBackGrounds(mContext,backGrounds,adapter.mMemoryCache);
		    			dialogShowPic.createDialog(mContext, theme.name, new onDialogListener() {						
								@Override
								public void onDone() {
									themeActivity.isExistShowPicDialog = false;
								}
							});	
						themeActivity.isExistShowPicDialog = true;		    						    			
					}else {						
						if (currentPhotoBook.chosenpics != null && currentPhotoBook.chosenpics.size() > 0 ) {
							DialogShowPic dialogShowPic = new DialogShowPic();
							dialogShowPic.setObjectList(mContext,currentPhotoBook.chosenpics,adapter.mMemoryCache);
							String yourPhotos = mContext.getString(R.string.your_photos);
							dialogShowPic.createDialog(mContext,yourPhotos, new onDialogListener() {						
									@Override
									public void onDone() {
										themeActivity.isExistShowPicDialog = false;
									}
								});	
							 themeActivity.isExistShowPicDialog = true;	
						}				
					}		
				}
			}
		});	
		
		
		layoutView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {			
				if (themeActivity.curSelectedTheme == null ||((themeActivity.curSelectedTheme != null && theme !=null) && !theme.id.equals(themeActivity.curSelectedTheme.id))) {
					themeActivity.curSelectedTheme  = theme;
					themeActivity.curSelectedPosition = position;
					if (themeActivity.preItemView != null) {
						themeActivity.preItemView.setBackgroundResource(R.drawable.theme_backgroud_dis);
					}
					setBackground();
					themeActivity.preItemView = ThemeItemView.this; 					
				}else if (theme == null) {
					themeActivity.curSelectedTheme  = theme;	
					themeActivity.curSelectedPosition = position;
					if (themeActivity.preItemView != null) {
						themeActivity.preItemView.setBackgroundResource(R.drawable.theme_backgroud_dis);
					}
					setBackground();
					themeActivity.preItemView = ThemeItemView.this; 	
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
	
	public void setValue(PhotoBooksThemeSelectActivity activity,Theme theme,PhotobookThemeSelectionAdapter mAdapter,int position){
		this.themeActivity = activity;
		this.adapter = mAdapter;	
		this.theme = theme;	
		this.position = position;
		
		if ((themeActivity.curSelectedTheme != null && theme != null && theme.id.equals(themeActivity.curSelectedTheme.id))) {
			setBackground();
			themeActivity.preItemView = ThemeItemView.this; 	
		}else {
			replyBackgroud();
		}
		
		String designs = "Designs";
		designs = activity.getString(R.string.Designs);		
		if (theme == null) {
			int size = currentPhotoBook.chosenpics.size();			
			themeName.setText(R.string.your_photos);
			themeNum.setText(String.valueOf(size)+" "+designs);			
		}else {
			themeName.setText(theme.name);
			themeNum.setText(String.valueOf(theme.backGrounds.length)+" "+designs);
		}
		themeContentView.setPagesPhotos(theme, mAdapter,position);		
	}

}

