package com.kodak.rss.tablet.view;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.kodak.rss.core.n2r.bean.calendar.CalendarGridItemPO;
import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.bean.text.Font;
import com.kodak.rss.core.n2r.bean.text.TextBlock;
import com.kodak.rss.core.util.AsyncImageLoader;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.adapter.PhotobookFontAdapter;
import com.kodak.rss.tablet.adapter.PhotobookFontColorAdapter;
import com.kodak.rss.tablet.adapter.PhotobookFontImageAdapter;
import com.kodak.rss.tablet.adapter.PhotobookFontTextAdapter;
import com.kodak.rss.tablet.thread.calendar.CalendarSetDateTaskGroup;
import com.kodak.rss.tablet.thread.calendar.CheckTextUpdateStatusTask;
import com.kodak.rss.tablet.util.CalendarUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.PaintDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class CalendarDateTextView extends FrameLayout {
	
	private static final String TAG = "CalendarDateTextView";
	
	private Context mContext;
	
	private View fontContainer;
	private View sizeContainer;
	private View alignmentContainer;
	private View justificationContainer;
	private View colorContainer;
	
	private TextView tvFont;
	private ImageView ivFont;
	private TextView tvSize;
	private TextView tvAlignment;
	private TextView tvJustification;
	private CalendarDateViewGroup datesView;
	private Button btDone;
	
	private CalendarSetDateTaskGroup dateTaskGroup;
	
	private PopupWindow popWindow;
	private ListView lvContent;
	private GridView gvColors;
	
	private String[] fontSizes;
	private String[] fontAlignments;
	private int[] fontAlignmentsSource;
	private String[] fontJustifications;
	private int[] fontJustificationSource;
	private String[] fontColors;
	private List<Font> fonts;
	private AsyncImageLoader asyncImageLoader;
	
	private List<DateFont> dateFonts;
	
	private CalendarPage mPage;
	private CalendarLayer mLayer;
	
	private DisplayMetrics dm;
	private int curSoftKeyboardHeight = 0;
	private int preSoftKeyboardHeight = 0;
	private int topMSpace = 0;
	private int initTopSpace = 0;	
	private InputMethodManager imm;
	
	private DateFont currentDateFont;
	
	public CalendarDateTextView(Context context) {
		super(context);
		init(context);
	}
	
	public CalendarDateTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CalendarDateTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	
	protected void init(Context context) {
		this.mContext = context;
		imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		dm = getResources().getDisplayMetrics();
		inflate(mContext, R.layout.calendar_date_text_window, this);
		initViews();
	}
	
	private void initViews(){
		fontContainer = findViewById(R.id.rl_font);
		sizeContainer = findViewById(R.id.rl_size);
		alignmentContainer = findViewById(R.id.rl_alignment);
		justificationContainer = findViewById(R.id.rl_justinfication);
		colorContainer = findViewById(R.id.rl_color);
		
		fontContainer.setBackgroundResource(R.drawable.font_button_background_default) ;
		sizeContainer.setBackgroundResource(R.drawable.font_button_background_default) ;
		alignmentContainer.setBackgroundResource(R.drawable.font_button_background_default) ;
		justificationContainer.setBackgroundResource(R.drawable.font_button_background_default) ;
		
		tvFont = (TextView) fontContainer.findViewById(R.id.v_content);
		ivFont = (ImageView) fontContainer.findViewById(R.id.v_image_content);
		tvSize = (TextView) sizeContainer.findViewById(R.id.v_content);
		tvAlignment = (TextView) alignmentContainer.findViewById(R.id.v_content);
		tvJustification = (TextView) justificationContainer.findViewById(R.id.v_content);
		
		datesView = (CalendarDateViewGroup) findViewById(R.id.datesView);
		btDone = (Button) findViewById(R.id.bt_Done);
		
		btDone.setOnClickListener(onClickListener);
		fontContainer.setOnClickListener(onClickListener);
		sizeContainer.setOnClickListener(onClickListener);
		alignmentContainer.setOnClickListener(onClickListener);
		justificationContainer.setOnClickListener(onClickListener);
		colorContainer.setOnClickListener(onClickListener);
	}
	
	private void initData(CalendarLayer layer){
		fonts = RssTabletApp.getInstance().fonts;
		fontSizes = TextBlock.fontSizes;
		fontAlignments = TextBlock.fontAlignments;
		fontAlignmentsSource = new int[]{R.drawable.textprop_align_topleft, R.drawable.textprop_align_topcenter, R.drawable.textprop_align_topright,
				R.drawable.textprop_align_centerleft, R.drawable.textprop_align_centered, R.drawable.textprop_align_centerright,
				R.drawable.textprop_align_bottomleft, R.drawable.textprop_align_bottomcenter, R.drawable.textprop_align_bottomright};
		fontJustifications = TextBlock.fontJustifications;
		fontJustificationSource = new int[]{R.drawable.textprop_justification_left, R.drawable.textprop_justification_center, R.drawable.textprop_justification_right};
		fontColors = TextBlock.fontColors;
		
		dateFonts = new ArrayList<CalendarDateTextView.DateFont>();
		
		List<CalendarGridItemPO> dateItems = CalendarUtil.getDatesInLayer(layer);
		if(dateItems!=null){
			for(int i=0; i<dateItems.size(); i++){
				CalendarLayer subLayer = null;
				for(CalendarLayer tempLayer : layer.sublayers){
					if(tempLayer.type.equals(CalendarLayer.TYPE_TEXT_BLOCK) && tempLayer.getDataValueWithoutType(CalendarLayer.FLAG_DATA_DAY)==dateItems.get(i).day){
						subLayer = tempLayer;
						break;
					}
				}
				DateFont dateFont = new DateFont();
				dateFont.pageId = mPage.id;
				dateFont.date = dateItems.get(i);
				dateFont.textBlock = subLayer.getTextBlock();
				dateFonts.add(dateFont);
			}
			datesView.refreshDates(this, dateTaskGroup, dateFonts);
		} else {
			Log.e(TAG, "initial date data error.");
		}
	}
	
	public CalendarDateTextView setViewSize(Point point){
		int width = dm.widthPixels * 11 / 24;
		int height = (int) (width/2 - dm.density*3);
		ViewGroup.LayoutParams params = getLayoutParams();
		params.height = height;
		params.width = width;	
		topMSpace = dm.heightPixels - height;
		setLayoutParams(params);					
		return this;
	}
	
	public void showTextFontView(boolean showAtLeft, int topMargin, int sideMargin){
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
		if(showAtLeft){
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			params.leftMargin = sideMargin;
		} else {
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			params.rightMargin = sideMargin;
		}
		initTopSpace = topMargin;
		topMSpace =  topMSpace - topMargin;
		params.topMargin = topMargin;		
		setLayoutParams(params);
		((View)getParent()).setVisibility(View.VISIBLE);
		CalendarDateTextView.this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
	        @Override
	        public void onGlobalLayout() {	           
	            Rect r = new Rect();
	            CalendarDateTextView.this.getWindowVisibleDisplayFrame(r);
	            preSoftKeyboardHeight = curSoftKeyboardHeight;
	            curSoftKeyboardHeight = dm.heightPixels - (r.bottom - r.top); 	
	            if (curSoftKeyboardHeight - preSoftKeyboardHeight != 0 ) {	            	
	            	if (curSoftKeyboardHeight > 0 ) {
	            		final int space = curSoftKeyboardHeight - topMSpace;	            		
	            		if (space <= 0) return;	  	            		
	            		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) CalendarDateTextView.this.getLayoutParams();	
						int topM = params.topMargin;
						if (topM < initTopSpace) return;
						TranslateAnimation ta = new TranslateAnimation(0, 0, space, 0);						
						ta.setDuration(150);							
						params.topMargin = topM - space;
						CalendarDateTextView.this.setLayoutParams(params);
						CalendarDateTextView.this.requestLayout();
						CalendarDateTextView.this.startAnimation(ta);	
					}else {	
						final int space = preSoftKeyboardHeight - topMSpace;						
	            		if (space <= 0) return;	 
	            		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) CalendarDateTextView.this.getLayoutParams();	
    					int topM = params.topMargin;
    					if (topM == initTopSpace) return;		            		
						final TranslateAnimation ta = new TranslateAnimation(0, 0, 0, space);						
						ta.setDuration(150);	
						ta.setFillAfter(true);
		            	ta.setAnimationListener(new AnimationListener() {			
		        			@Override
		        			public void onAnimationStart(Animation animation) {
		        				RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) CalendarDateTextView.this.getLayoutParams();	
		    					int topM = params.topMargin;
		    					if (topM == initTopSpace) {
		    						ta.cancel();
		    						CalendarDateTextView.this.clearAnimation();
		    					}
		        			}			
		        			@Override
		        			public void onAnimationRepeat(Animation animation) {}			
		        			@Override
		        			public void onAnimationEnd(Animation animation) {
		        				CalendarDateTextView.this.clearAnimation();
		        				RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) CalendarDateTextView.this.getLayoutParams();	
		    					int topM = params.topMargin;
		    					if (topM == initTopSpace) return;	
		        				params.topMargin = topM + space;
		        				CalendarDateTextView.this.setLayoutParams(params);
		        				CalendarDateTextView.this.requestLayout();
		        			}
		        		});			            	
		            	CalendarDateTextView.this.startAnimation(ta);												
					}	            
				}
	            
	        }
	    });
	}
	
	public void updateFontValues(TextBlock textBlock){
		for(int i=0; i<fonts.size(); i++){
			if(fonts.get(i).name.equalsIgnoreCase(textBlock.fontName)){
				textBlock.font = fonts.get(i);
				if(asyncImageLoader==null){
					AsyncImageLoader.Config config = new AsyncImageLoader.Config();
					config.setMaxThreadSize(5)
					.setCacheFolderPath(RssTabletApp.getInstance().getTempFolderPath());
					asyncImageLoader = new AsyncImageLoader(getContext(),config);
				}
				asyncImageLoader.loadImageAsync(ivFont, textBlock.font.sampleURL, new AsyncImageLoader.ImageDownloaderCallBack() {
					
					@Override
					public void OnImageDownloaded(View view, Bitmap bitmap) {
						ivFont.setVisibility(View.VISIBLE);
						tvFont.setVisibility(View.INVISIBLE);
						ivFont.setImageBitmap(bitmap);
					}
				});
				break;
			}
		}
		
		String size = "";
		if("".equals(textBlock.fontSize)){
			textBlock.fontSize = "Auto";
		} 
		if("Auto".equalsIgnoreCase(textBlock.fontSize)){
			size = fontSizes[0];
		} else {
			DecimalFormat format = new DecimalFormat("###");
			try {
				size = format.parse(format.format(Double.valueOf(textBlock.fontSize))).toString();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		tvSize.setText(size);
		
		for(int i=0; i<fontAlignments.length; i++){
			if(fontAlignments[i].equalsIgnoreCase(textBlock.alignment)){
				tvAlignment.setBackgroundResource(fontAlignmentsSource[i]);
				break;
			}
		}
		
		for(int i=0; i<fontJustifications.length; i++){
			if(fontJustifications[i].equalsIgnoreCase(textBlock.justification)){
				tvJustification.setBackgroundResource(fontJustificationSource[i]);
			}
		}
		
		((GradientDrawable)colorContainer.getBackground()).setColor(Color.parseColor(textBlock.color));
	}
	
	public static final int Type_Font = 1;
	public static final int Type_Size = 2;
	public static final int Type_Alignment = 3;
	public static final int Type_Justinfication = 4;
	public static final int Type_Color = 5;
	OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int viewId = v.getId();
			if(viewId == R.id.rl_font){
				showContentWindow(v, Type_Font);
			} 
			else if(viewId == R.id.rl_size) {
				showContentWindow(v, Type_Size);
			} 
			else if(viewId == R.id.rl_alignment) {
				showContentWindow(v, Type_Alignment);
			} 
			else if(viewId == R.id.rl_justinfication) {
				showContentWindow(v, Type_Justinfication);
			} 
			else if(viewId == R.id.rl_color) {
				showContentWindow(v, Type_Color);
			} 
			else if(viewId == R.id.bt_Done) {				
				((View)getParent()).setVisibility(View.GONE);
				hideSoftKeyboard();
				
				if (mContext instanceof CalendarEditActivity) {
					((CalendarEditActivity) mContext).calendarMainView.moveToCenter();
					CheckTextUpdateStatusTask task = new CheckTextUpdateStatusTask((CalendarEditActivity) mContext);
					task.execute();
				}
			}
		}
	};
	
	private void showContentWindow(View view, int type){
		if(popWindow != null){
			popWindow.dismiss();
		}
		int width = 0;
		BaseAdapter adapter = null;
		PopWindowOnItemClickListener listener = new PopWindowOnItemClickListener(type);
		switch (type) {
		case Type_Font:
			if(asyncImageLoader == null){
				AsyncImageLoader.Config config = new AsyncImageLoader.Config();
				config.setMaxThreadSize(5)
				.setCacheFolderPath(RssTabletApp.getInstance().getTempFolderPath());
				asyncImageLoader = new AsyncImageLoader(getContext(),config);
			}
			adapter = new PhotobookFontAdapter(mContext, fonts,asyncImageLoader);
			break;
		case Type_Size:
			adapter = new PhotobookFontTextAdapter(mContext, fontSizes);
			break;
		case Type_Alignment:
			adapter = new PhotobookFontImageAdapter(mContext, fontAlignmentsSource);
			break;
		case Type_Justinfication:
			adapter = new PhotobookFontImageAdapter(mContext, fontJustificationSource);
			break;
		case Type_Color:
			int columesWidth = datesView.getWidth()/9;
			adapter = new PhotobookFontColorAdapter(mContext, fontColors, columesWidth);
			break;
		}
		View v = null;
		if(type == Type_Color){
			v = inflate(mContext, R.layout.photobook_color_gridview, null);
			gvColors = (GridView) v.findViewById(R.id.gv_color);
			gvColors.setAdapter(adapter);
			gvColors.setOnItemClickListener(listener);
			width = datesView.getWidth();
		} else {
			v = inflate(mContext, R.layout.photobook_font_listview, null);
			lvContent = (ListView) v.findViewById(R.id.lv_content);
			lvContent.setDivider(null);
			lvContent.setAdapter(adapter);
			lvContent.setOnItemClickListener(listener);
			width = view.getWidth();
		}
		popWindow = new PopupWindow(v, width, getHeight());
		adapter.notifyDataSetChanged();		
		popWindow.setFocusable(true);
		popWindow.setBackgroundDrawable(new PaintDrawable(Color.TRANSPARENT));
		popWindow.setOutsideTouchable(true);
		if(type == Type_Color){
			int xoffset = (view.getWidth())- width;
			popWindow.showAsDropDown(view, xoffset, 0);
		} else {
			popWindow.showAsDropDown(view);
		}
	}
	
	private void hideSoftKeyboard(){
		if(mContext instanceof Activity){
			imm.hideSoftInputFromWindow( ((Activity)mContext).getCurrentFocus().getWindowToken(), 0);
		}
	}
	
	public CalendarDateTextView setCalendarInfo(CalendarSetDateTaskGroup dateTaskGroup, CalendarPage page, CalendarLayer layer){
		this.dateTaskGroup = dateTaskGroup;
		this.mPage = page;
		this.mLayer = layer;
		initData(layer);
		return this;
	}
	
	class PopWindowOnItemClickListener implements OnItemClickListener {
		private int type;
		
		public PopWindowOnItemClickListener(int type){
			this.type = type;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			TextBlock textBlock = currentDateFont.textBlock;
			switch (type) {
			case Type_Font:
				tvFont.setText(fonts.get(position).displayName);
				textBlock.font = fonts.get(position);
				textBlock.fontName = fonts.get(position).name;
				
				asyncImageLoader.loadImageAsync(ivFont, textBlock.font.sampleURL, new AsyncImageLoader.ImageDownloaderCallBack() {
					
					@Override
					public void OnImageDownloaded(View view, Bitmap bitmap) {
						ivFont.setVisibility(View.VISIBLE);
						tvFont.setVisibility(View.INVISIBLE);
						ivFont.setImageBitmap(bitmap);
					}
				});
				break;
			case Type_Size:
				tvSize.setText(fontSizes[position]);
				textBlock.fontSize = fontSizes[position];
				break;
			case Type_Alignment:
				tvAlignment.setBackgroundResource(fontAlignmentsSource[position]);
				textBlock.alignment = fontAlignments[position];
				break;
			case Type_Justinfication:
				tvJustification.setBackgroundResource(fontJustificationSource[position]);
				textBlock.justification = fontJustifications[position];
				break;
			case Type_Color:
				((GradientDrawable)colorContainer.getBackground()).setColor(Color.parseColor(fontColors[position]));
				textBlock.color = fontColors[position];
				break;
			}
			if(popWindow != null){
				popWindow.dismiss();
			}
			dateTaskGroup.addTask(currentDateFont);
		}
		
	};
	
	public DateFont getCurrentDateFont() {
		return currentDateFont;
	}

	public void setCurrentDateFont(DateFont dateFont) {
		this.currentDateFont = dateFont;
	}

	public static class DateFont {
		
		public String pageId;
		public CalendarGridItemPO date;
		public TextBlock textBlock;
		
		/**
		 * 
		 * @return combination of month and day with mmdd format
		 */
		public String getMonthAndDayString(){
			String month = "";
			if(date.month<10){
				month = "0" + date.month;
			} else {
				month = "" + date.month;
			}
			String day = "";
			if(date.day < 10){
				day = "0" + date.day;
			} else {
				day = "" + date.day;
			}
			return month+day;
		}
	}

}
