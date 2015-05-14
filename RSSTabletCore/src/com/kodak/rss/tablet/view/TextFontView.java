package com.kodak.rss.tablet.view;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.greetingcard.GCLayer;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Data;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.prints.Page;
import com.kodak.rss.core.n2r.bean.text.Font;
import com.kodak.rss.core.n2r.bean.text.TextBlock;
import com.kodak.rss.core.util.AsyncImageLoader;
import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.activities.CollageEditActivity;
import com.kodak.rss.tablet.activities.GCEditActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.adapter.PhotobookFontAdapter;
import com.kodak.rss.tablet.adapter.PhotobookFontColorAdapter;
import com.kodak.rss.tablet.adapter.PhotobookFontImageAdapter;
import com.kodak.rss.tablet.adapter.PhotobookFontTextAdapter;
import com.kodak.rss.tablet.thread.EditFontTask;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.GreetingCardUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;

public class TextFontView<P extends Page,L extends Layer> extends FrameLayout {
	private final String TAG = TextFontView.class.getSimpleName();
	
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
	private EditText etTexts;
	private Button btDone;
	
	private PopupWindow popWindow;
	private ListView lvContent;
	private GridView gvColors;
	
	private boolean isCaption = true;
	
	private String[] fontSizes;
	private String[] fontAlignments;
	private int[] fontAlignmentsSource;
	private String[] fontJustifications;
	private int[] fontJustificationSource;
	private String[] fontColors;
	private List<Font> fonts;
	
	private P page;
	private L layer;
	private TextBlock textBlock;
	
	private AsyncImageLoader asyncImageLoader;
	
	private EditFontTask<P,L> editTask;
	
	private DisplayMetrics dm;
	private int curSoftKeyboardHeight = 0;
	private int preSoftKeyboardHeight = 0;
	private int topMSpace = 0;
	private int initTopSpace = 0;	
	private InputMethodManager imm;

	public TextFontView(Context context) {
		super(context);
		init(context);
	}
	
	public TextFontView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public TextFontView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		mContext = context;
		imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		dm = getResources().getDisplayMetrics();
		inflate(context, R.layout.photobook_text_font, this);
		
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
		
		etTexts = (EditText) findViewById(R.id.et_texts);
		TextUtil.addEmojiFilter(etTexts);
		btDone = (Button) findViewById(R.id.bt_Done);
		
		etTexts.addTextChangedListener(textChangedListener);
		
		//Fix for RSSMOBILEPDC-1635	[Tablet] Card: On Nexus 7(V4.3), the text input dialog shows strange after tapping Enter key for 1 seconds.
		//If long click enter, the focus will go to next, and it will cause the issue.
		//So let it next focus on itself
		etTexts.setNextFocusDownId(R.id.et_texts);
		
		
		btDone.setOnClickListener(onClickListener);
		editTask = new EditFontTask<P,L>(mContext);
		initData();
		setIsCaption(true);
	}
	
	private void initData(){
		fonts = RssTabletApp.getInstance().fonts;
		fontSizes = TextBlock.fontSizes;
		fontAlignments = TextBlock.fontAlignments;
		fontAlignmentsSource = new int[]{R.drawable.textprop_align_topleft, R.drawable.textprop_align_topcenter, R.drawable.textprop_align_topright,
				R.drawable.textprop_align_centerleft, R.drawable.textprop_align_centered, R.drawable.textprop_align_centerright,
				R.drawable.textprop_align_bottomleft, R.drawable.textprop_align_bottomcenter, R.drawable.textprop_align_bottomright};
		fontJustifications = TextBlock.fontJustifications;
		fontJustificationSource = new int[]{R.drawable.textprop_justification_left, R.drawable.textprop_justification_center, R.drawable.textprop_justification_right};
		fontColors = TextBlock.fontColors;
	}
	
	public TextFontView<P,L> setViewSize(Point point){
		int width = dm.widthPixels * 11 / 24;
		int height = (int) (width/2 - dm.density*5);
		ViewGroup.LayoutParams params = getLayoutParams();
		params.height = height;
		params.width = width;	
		topMSpace = dm.heightPixels - height;
		setLayoutParams(params);					
		return this;
	}
	
	public TextFontView<P,L> setIsCaption(boolean isCaption){
		this.isCaption = isCaption;
		updateButtonsStatus(!isCaption);
		if(isCaption){
			initButtonValuesOnCaption() ;
		}
		return this;
	}
	
	public TextFontView<P,L> setEditPageInfo(P page, L layer){
		this.page = page;
		this.layer = layer;
		return this;
	}
	
	public TextFontView<P, L> setFontComponentsVisible(boolean isVisible) {
		View v = findViewById(R.id.bt_panel);
		int visibleState = isVisible ? View.VISIBLE : View.GONE;
		v.setVisibility(visibleState);
		return this;
	}
	
	public TextFontView<P, L> setTitleVisible(boolean isVisible){
		View v = findViewById(R.id.tv_title);
		int visibleState = isVisible ? View.VISIBLE : View.GONE;
		v.setVisibility(visibleState);
		return this;
	}

	private void updateButtonsStatus(boolean enable){
		fontContainer.setEnabled(enable);
		sizeContainer.setEnabled(enable);
		alignmentContainer.setEnabled(enable);
		justificationContainer.setEnabled(enable);
		colorContainer.setEnabled(enable);
		if(enable){
			fontContainer.setOnClickListener(onClickListener);
			sizeContainer.setOnClickListener(onClickListener);
			alignmentContainer.setOnClickListener(onClickListener);
			justificationContainer.setOnClickListener(onClickListener);
			colorContainer.setOnClickListener(onClickListener);
		}
	}
	
	private void initButtonValuesOnCaption(){
		tvFont.setText("");
		ivFont.setImageBitmap(null) ;
		tvSize.setText("");
		tvAlignment.setBackgroundDrawable(null) ;
		tvJustification.setBackgroundDrawable(null) ;
		
		((GradientDrawable)colorContainer.getBackground()).setColor(getResources().getColor(android.R.color.darker_gray));
		
	}
	
	private void initButtonValues(){
		textBlock = layer.getTextBlock();	
		
		//fixed RSSMOBILEPDC-1715 
		boolean isHaveSampleText = false;
		if (layer.data != null) {
			for(Data d : layer.data){
				String name = d.name==null?"":d.name;						
				if(Data.TYPE_SAMPLETEXT.equals(name)){
					isHaveSampleText = true;
					break;
				}		
			}		
		}
		String text = null;	
		if (!isHaveSampleText && textBlock != null && " ".equals(textBlock.text)) {
			textBlock.text = "";							
		}
		
		if (textBlock != null && "".equals(textBlock.text)) {
			if (isHaveSampleText) {
				if (!"".equals(textBlock.sampleText)) {
					textBlock.text = textBlock.sampleText;
				}else {
					textBlock.text = textBlock.defaultText;
				}
			}else {
				textBlock.text = textBlock.defaultText;
			}
		}
		text = textBlock.text;
		if(fonts == null){
			fonts = RssTabletApp.getInstance().fonts;
		}
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
		
		if (layer instanceof GCLayer && ((GCLayer)layer).fontSize > 0) {
			textBlock.fontSize = String.valueOf(((GCLayer)layer).fontSize);
		}
		if("".equals(textBlock.fontSize)){
			textBlock.fontSize = "Auto";
		} 
				
		if("Auto".equalsIgnoreCase(textBlock.fontSize) || "0".equals(textBlock.fontSize)){
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
		
		String sampleText = getResources().getString(R.string.Common_SampleText) ;
		if(
//		  sampleText.equals(textBlock.text) ||
//				( (!textBlock.isAppendable)  &&  ( textBlock.defaultText.equals(textBlock.text)|| textBlock.sampleText.equals(textBlock.text)) ) ){
		 sampleText.equals(textBlock.text) ||
				( (!textBlock.isAppendable)  && (text != null && text.equals(textBlock.text)) ) ){			
			etTexts.setSelectAllOnFocus(true) ;
		}else {
			etTexts.setSelectAllOnFocus(false) ;
		}
		
		etTexts.setText(textBlock.text);
		
		((GradientDrawable)colorContainer.getBackground()).setColor(Color.parseColor(textBlock.color));
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
		if(!isCaption){
			initButtonValues();
		} else {
			textBlock = new TextBlock();
			etTexts.setText(layer.getCaptionText());
		}
		((View)getParent()).setVisibility(View.VISIBLE);
		TextFontView.this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
	        @Override
	        public void onGlobalLayout() {	           
	            Rect r = new Rect();
	            TextFontView.this.getWindowVisibleDisplayFrame(r);
	            preSoftKeyboardHeight = curSoftKeyboardHeight;
	            curSoftKeyboardHeight = dm.heightPixels - (r.bottom - r.top); 	
	            if (curSoftKeyboardHeight - preSoftKeyboardHeight != 0 ) {	            	
	            	if (curSoftKeyboardHeight > 0 ) {
	            		final int space = curSoftKeyboardHeight - topMSpace;	            		
	            		if (space <= 0) return;	  	            		
	            		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) TextFontView.this.getLayoutParams();	
						int topM = params.topMargin;
						if (topM < initTopSpace) return;
						TranslateAnimation ta = new TranslateAnimation(0, 0, space, 0);						
						ta.setDuration(150);							
						params.topMargin = topM - space;
						TextFontView.this.setLayoutParams(params);
		            	TextFontView.this.requestLayout();
		            	TextFontView.this.startAnimation(ta);	
					}else {	
						final int space = preSoftKeyboardHeight - topMSpace;						
	            		if (space <= 0) return;	 
	            		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) TextFontView.this.getLayoutParams();	
    					int topM = params.topMargin;
    					if (topM == initTopSpace) return;		            		
						final TranslateAnimation ta = new TranslateAnimation(0, 0, 0, space);						
						ta.setDuration(150);	
						ta.setFillAfter(true);
		            	ta.setAnimationListener(new AnimationListener() {			
		        			@Override
		        			public void onAnimationStart(Animation animation) {
		        				RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) TextFontView.this.getLayoutParams();	
		    					int topM = params.topMargin;
		    					if (topM == initTopSpace) {
		    						ta.cancel();
		    						TextFontView.this.clearAnimation();
		    					}
		        			}			
		        			@Override
		        			public void onAnimationRepeat(Animation animation) {}			
		        			@Override
		        			public void onAnimationEnd(Animation animation) {
		        				TextFontView.this.clearAnimation();
		        				RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) TextFontView.this.getLayoutParams();	
		    					int topM = params.topMargin;
		    					if (topM == initTopSpace) return;	
		        				params.topMargin = topM + space;
								TextFontView.this.setLayoutParams(params);
				            	TextFontView.this.requestLayout();
		        			}
		        		});			            	
		            	TextFontView.this.startAnimation(ta);												
					}	            
				}
	            
	        }
	    });
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
				if(isCaption){
					editTask.startSetCaptionTask(page, layer, textBlock, handler, true);
				} else {
					// TODO edit or add text layers
					editTask.startSetFontTask(page, layer, textBlock, handler, true);
				}
				
				if (mContext instanceof GCEditActivity) {
					((GCEditActivity) mContext).gCMainView.moveToCenter();
				}
				
				if (mContext instanceof CalendarEditActivity) {
					((CalendarEditActivity) mContext).calendarMainView.moveToCenter();
				}
				
				if (mContext instanceof CollageEditActivity) {
					((CollageEditActivity) mContext).collageMainView.moveToCenter();
				}
				hideSoftKeyboard();
			}
		}
	};

	private void hideSoftKeyboard(){
		if(mContext instanceof Activity){
			imm.hideSoftInputFromWindow( ((Activity)mContext).getCurrentFocus().getWindowToken(), 0);
		}
	}	
	
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
			int columesWidth = etTexts.getWidth()/9;
			adapter = new PhotobookFontColorAdapter(mContext, fontColors, columesWidth);
			break;
		}
		View v = null;
		if(type == Type_Color){
			v = inflate(mContext, R.layout.photobook_color_gridview, null);
			gvColors = (GridView) v.findViewById(R.id.gv_color);
			gvColors.setAdapter(adapter);
			gvColors.setOnItemClickListener(listener);
			width = etTexts.getWidth();
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
	
	class PopWindowOnItemClickListener implements OnItemClickListener {
		private int type;
		
		public PopWindowOnItemClickListener(int type){
			this.type = type;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
			editTask.startSetFontTask(page, layer, textBlock, handler, false);
			if(popWindow != null){
				popWindow.dismiss();
			}
		}
		
	};
	
	TextWatcher textChangedListener = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(textBlock == null){
				return;
			}
			if(s.equals(textBlock.text)){
				return;
			}
			textBlock.text = s==null? "" : s.toString();
			if(isCaption){
				editTask.startSetCaptionTask(page, layer, textBlock, handler, false);
			} else {
				
				//fixed RSSMOBILEPDC-1715 
				boolean isHaveSampleText = false;
				if (layer.data != null) {
					for(Data d : layer.data){
						String name = d.name==null?"":d.name;						
						if(Data.TYPE_SAMPLETEXT.equals(name)){
							isHaveSampleText = true;
							break;
						}		
					}		
				}
				if (!isHaveSampleText) {
					String text = textBlock.text == null ? "" : textBlock.text;
					textBlock.text = "".equals(text) ? " ": text;
				}
				editTask.startSetFontTask(page, layer, textBlock, handler, false);
			}
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			
		}
	};
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			BaseNetActivity activity = (BaseNetActivity) mContext;
			if(activity.isFinishing())return;
			if(msg.obj instanceof RssWebServiceException){
				activity.showErrorWarning((RssWebServiceException)msg.obj);
				return;
			}		
			P p = (P) msg.obj;
			int arg = msg.arg1;
			page = p;			
			if(arg == 1){
				if (activity instanceof GCEditActivity) {
					if(page.layers!= null && layer!= null){
						for(GCLayer lay : ((GCPage)page).layers){
							if(lay != null && layer.contentId == lay.contentId){
								layer = (L)lay;
								break;
							}
						}
					}				
				}
				if (activity instanceof PhotoBooksProductActivity || activity instanceof CalendarEditActivity || activity instanceof CollageEditActivity) {
					if(page.layers!=null && layer!=null){
						for(Layer lay : page.layers){
							if(lay != null && layer.contentId == lay.contentId){
								layer = (L)lay;
								break;
							}
						}
					}
				}

			}
			if (activity instanceof GCEditActivity) {
				GreetingCardUtil.updatePageInCard((GCPage)p,false);
				((GCEditActivity)mContext).notifyGCPagesChanged(page.id);
			}
			if (activity instanceof PhotoBooksProductActivity) {
				PhotoBookProductUtil.updatePageInPhotobook((PhotobookPage)p,false);
				((PhotoBooksProductActivity)mContext).notifyPhotoBookPagesChanged();
			}
			if (activity instanceof CalendarEditActivity) {
				CalendarUtil.updatePageInCalendar((CalendarPage) p, true);
				((CalendarEditActivity)mContext).notifyCalendarPagesChanged();
			}
			if (activity instanceof CollageEditActivity) {
				CollageUtil.updatePageInCollage((CollagePage) p, true, false);
				((CollageEditActivity)mContext).notifyCollagePageChanged();
			}
			
		}		
	};

}
