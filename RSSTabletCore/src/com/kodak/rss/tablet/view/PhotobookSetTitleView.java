package com.kodak.rss.tablet.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.util.StringUtils;
import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.thread.SetPhotobookTitleTask;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;

public class PhotobookSetTitleView extends PhotobookTextEditView {
	
	private EditText etTitle;
	private EditText etAuthor;
	private EditText etSubTitle;
	private EditText etProjectName;
	private Button btDone;
	private Photobook photobook;
	private InputMethodManager imm;
	private int curSoftKeyboardHeight = 0;
	private int preSoftKeyboardHeight = 0;	
	private int initTopSpace = 0;
	// if app is creating a new photobook, the edit text for project name has a default value
	private boolean isCreatePhotoBook = false;

	public PhotobookSetTitleView(Context context) {
		super(context);
	}
	
	public PhotobookSetTitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public PhotobookSetTitleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init(Context context) {
		mContext = context;
		imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		inflate(mContext, R.layout.photobook_title_author_subtitle, this);		
		photobook = PhotoBookProductUtil.getCurrentPhotoBook();
		initViews();
	}
	
	private void initViews(){
		etTitle = (EditText) findViewById(R.id.et_title);
		TextUtil.addEmojiFilter(etTitle);
		etAuthor = (EditText) findViewById(R.id.et_author);
		TextUtil.addEmojiFilter(etAuthor);
		etSubTitle = (EditText) findViewById(R.id.et_subtitle);
		TextUtil.addEmojiFilter(etSubTitle);
		etProjectName = (EditText) findViewById(R.id.et_projectname);
		TextUtil.addEmojiFilter(etProjectName);
		btDone = (Button) findViewById(R.id.bt_tas_done);

		btDone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doOnDoneClick();
			}
		});
		
		etProjectName.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE){
					doOnDoneClick();
					return true;
				}
				return false;
			}
		});
		
		if(photobook.canSetAuthor){
			etAuthor.setVisibility(View.VISIBLE);
		} else {
			etAuthor.setVisibility(View.GONE);
		}
		if(photobook.canSetTitle){
			etTitle.setVisibility(View.VISIBLE);
		} else {
			etTitle.setVisibility(View.GONE);
		}
		if(photobook.canSetSubtitle){
			etSubTitle.setVisibility(View.VISIBLE);
		} else {
			etSubTitle.setVisibility(View.GONE);
		}
		initData();
	}
	
	private void initData(){
		refreshData(true);
	}
	
	public void setCreatePhotobook(boolean isCreatePhotobook) {
		this.isCreatePhotoBook = isCreatePhotobook;
	}
	
	private void refreshData(boolean init){
		String author = photobook.author;
		String title = photobook.title;
		String subTitle = photobook.subTitle;
		
		if(!init){
			if(StringUtils.isEmpty(author))
				author = etAuthor.getText().toString();
			if(StringUtils.isEmpty(title))
				title = etTitle.getText().toString();
			if(StringUtils.isEmpty(subTitle))
				subTitle = etSubTitle.getText().toString();
		} else {
			if(StringUtils.isEmpty(title)){
				title = PhotoBookProductUtil.getDefaultPhotobookTitle();
				
				if(StringUtils.isEmpty(title))
					title = "Saved Photos";
			}
			
			if(StringUtils.isEmpty(author)){
				author = PhotoBookProductUtil.getDefaultPhotobookAuthor(mContext);
			}
			
			if(StringUtils.isEmpty(subTitle)){
				SimpleDateFormat f = new SimpleDateFormat("MMMM-yyyy");
				Date date = new Date() ;
				subTitle = f.format(date) ;
			}
		}
		
		
		
		String projectName = "";
		if(isCreatePhotoBook){
			projectName = title;
		}else{
			PhotoBooksProductActivity activity = (PhotoBooksProductActivity) mContext;
			projectName = activity.projectName;
			if (StringUtils.isEmpty(projectName)) projectName = etProjectName.getText().toString();
			if (StringUtils.isEmpty(projectName)) projectName = "";
			
			if ("".equals(projectName)) {
				if (!StringUtils.isEmpty(title) && !" ".equals(title)) {
					projectName = title;
				} else {
					projectName = AppConstants.PROJECT_DEFAULT_NAME;
				}
			}
			
		}
		
		//For RSSMOBILEPDC-1559
		//In IOS , if the string is " ", it will show hint
		etAuthor.setText(" ".equals(author) ? "" : author);
		etTitle.setText(" ".equals(title) ? "" : title);
		etSubTitle.setText(" ".equals(subTitle) ? "" : subTitle);
		etProjectName.setText(" ".equals(projectName) ? "" : projectName);
	}
	
	private void hideSoftKeyboard(){
		if(mContext instanceof Activity){
			imm.hideSoftInputFromWindow( ((Activity)mContext).getCurrentFocus().getWindowToken(), 0);
		}
	}
	
	private void doOnDoneClick(){		
		((View)getParent()).setVisibility(View.GONE);
		String author = etAuthor.getText().toString();
		String title = etTitle.getText().toString();
		String subtitle = etSubTitle.getText().toString();
		String projectName = etProjectName.getText().toString();
		author = author.equals("") ? " ": author;
		title = title.equals("") ? " " : title;
		subtitle = subtitle.equals("") ? " " : subtitle;
		SetPhotobookTitleTask task = new SetPhotobookTitleTask(mContext, author, title, subtitle,projectName);
		hideSoftKeyboard();
		task.execute(photobook);
	}
	
	@Override
	public void initFocusView() {
		View view = null;
		if(etTitle.getVisibility() == View.VISIBLE){
			etTitle.requestFocus();
			view = etTitle;
		} else if(etAuthor.getVisibility() == View.VISIBLE) {
			etAuthor.requestFocus();
			view = etAuthor;
		} else if(etSubTitle.getVisibility() == View.VISIBLE) {
			etSubTitle.requestFocus();
			view = etSubTitle;
		}
		if(view != null){
			imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
		}
	}
	
	@Override
	public void showAtLeft(boolean showAtLeft, int topMargin, int sideMargin) {
		super.showAtLeft(showAtLeft, topMargin, sideMargin);		
		refreshData(false);				
		topMSpace = topMSpace - topMargin;
		initTopSpace = topMargin;
		PhotobookSetTitleView.this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
	        @Override
	        public void onGlobalLayout() {	           
	            Rect r = new Rect();
	            PhotobookSetTitleView.this.getWindowVisibleDisplayFrame(r);            
	            preSoftKeyboardHeight = curSoftKeyboardHeight;
	            curSoftKeyboardHeight = dm.heightPixels - (r.bottom - r.top); 	
	            if (curSoftKeyboardHeight - preSoftKeyboardHeight != 0 ) {
	            	if (curSoftKeyboardHeight > 0 ) {
	            		int space = curSoftKeyboardHeight - topMSpace;	            		
	            		if (space <= 0) return;	            		
	            		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) PhotobookSetTitleView.this.getLayoutParams();	
						int topM = params.topMargin;
						if (topM < initTopSpace) return;
						TranslateAnimation ta = new TranslateAnimation(0, 0, space, 0);						
						ta.setDuration(150);					
						params.topMargin = topM - space;
						PhotobookSetTitleView.this.setLayoutParams(params);
						PhotobookSetTitleView.this.requestLayout();
						PhotobookSetTitleView.this.startAnimation(ta);	
					}else {	
						final int space = preSoftKeyboardHeight - topMSpace;						
	            		if (space <= 0) return;	  
	            		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) PhotobookSetTitleView.this.getLayoutParams();	
    					int topM = params.topMargin;
    					if (topM == initTopSpace) return;
						final TranslateAnimation ta = new TranslateAnimation(0, 0, 0, space);						
						ta.setDuration(150);	
						ta.setFillAfter(true);
		            	ta.setAnimationListener(new AnimationListener() {			
		        			@Override
		        			public void onAnimationStart(Animation animation) {
		        				RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) PhotobookSetTitleView.this.getLayoutParams();	
		    					int topM = params.topMargin;
		    					if (topM == initTopSpace) {
		    						ta.cancel();
		    						PhotobookSetTitleView.this.clearAnimation();
		    					}
		        			}			
		        			@Override
		        			public void onAnimationRepeat(Animation animation) {}			
		        			@Override
		        			public void onAnimationEnd(Animation animation) {
		        				PhotobookSetTitleView.this.clearAnimation();
		        				RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) PhotobookSetTitleView.this.getLayoutParams();	
		    					int topM = params.topMargin;
		    					if (topM == initTopSpace) return;	
		        				params.topMargin = topM + space;		        				
		        				PhotobookSetTitleView.this.setLayoutParams(params);
		        				PhotobookSetTitleView.this.requestLayout();
		        			}
		        		});			            	
		            	PhotobookSetTitleView.this.startAnimation(ta);												
					}	            
				}	            
	        }
	    });
		
	}
	
}
