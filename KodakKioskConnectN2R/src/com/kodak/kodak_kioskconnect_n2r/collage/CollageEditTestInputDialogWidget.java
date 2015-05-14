package com.kodak.kodak_kioskconnect_n2r.collage;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.PaintDrawable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants.LoadImageType;
import com.AppContext;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.adapter.CollageFontAdapter;
import com.kodak.kodak_kioskconnect_n2r.adapter.CollageFontColorAdapter;
import com.kodak.kodak_kioskconnect_n2r.adapter.CollageFontImageAdapter;
import com.kodak.kodak_kioskconnect_n2r.adapter.CollageFontSizeAdapter;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Data;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Page;
import com.kodak.kodak_kioskconnect_n2r.bean.text.Font;
import com.kodak.kodak_kioskconnect_n2r.bean.text.TextBlock;
import com.kodak.utils.SoftKeyboardUtil;
import com.kodak.utils.SoftKeyboardUtil.OnSoftKeyboardChangeListener;

public class CollageEditTestInputDialogWidget extends RelativeLayout {

	public EditText editTextInput;
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
	private LinearLayout bt_panel;
	private Button cancelButton;
	private Button doneButton;
	private GridView gvColors;
	private RelativeLayout collageEditInputBar;

	private PopupWindow popWindow;
	private ListView lvContent;
	private List<Font> fonts;
	private static final int Type_Font = 1;
	private static final int Type_Size = 2;
	private static final int Type_Alignment = 3;
	private static final int Type_Justinfication = 4;
	private static final int Type_Color = 5;

	private String[] fontSizes;
	private String[] fontAlignments;
	private String[] fontJustifications;
	private String[] fontColors;
	private TextBlock textBlock;
	private int[] fontAlignmentsSource;
	private int[] fontJustificationSource;
	private int previousKeyboradHeight = -1;
	private int mSoftkeybardHeight = 0;
	private boolean alreadyGetData = false;
	private ImageFetcher mImageFetcher;
	private static final String IMAGE_CACHE_DIR = "collageFontImages";
	private CollagePage CollagePage;
	private Layer CollageLayer;

	private Context mContext;
	private CollageEditTestInputListener mListener;

	public interface CollageEditTestInputListener {
		public void updateUI();

		public void updateTextCollage(Page page, Layer layer, TextBlock textBlock);
	}

	public CollageEditTestInputDialogWidget(Context context) {
		super(context);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.collage_edittext_input, this);
		init();
	}

	public CollageEditTestInputDialogWidget(Context context, AttributeSet a) {
		super(context, a);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.collage_edittext_input, this);
		init();
	}

	public void showTextFontView(CollagePage page, Layer layer) {
		this.CollagePage = page;
		this.CollageLayer = layer;
		fonts = AppContext.getApplication().getFonts();
		initButtonValues();

	}

	private void init() {
		int mImageThumbSize = mContext.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(mContext, IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory
		mImageFetcher = new ImageFetcher(mContext, mImageThumbSize);
		mImageFetcher.addImageCache(((FragmentActivity) mContext).getSupportFragmentManager(), cacheParams);
		mListener = (CollageEditTestInputListener) mContext;
		fonts = AppContext.getApplication().getFonts();
		fontSizes = TextBlock.fontSizes;
		fontAlignments = TextBlock.fontAlignments;
		fontJustifications = TextBlock.fontJustifications;
		fontAlignmentsSource = new int[] { R.drawable.textprop_align_topleft, R.drawable.textprop_align_topcenter,
				R.drawable.textprop_align_topright, R.drawable.textprop_align_centerleft, R.drawable.textprop_align_centered,
				R.drawable.textprop_align_centerright, R.drawable.textprop_align_bottomleft, R.drawable.textprop_align_bottomcenter,
				R.drawable.textprop_align_bottomright };
		fontJustificationSource = new int[] { R.drawable.textprop_justification_left, R.drawable.textprop_justification_center,
				R.drawable.textprop_justification_right };
		fontColors = TextBlock.fontColors;
		getViews();
		setEvents();
	}

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int viewId = v.getId();
			if (viewId == R.id.rl_font) {
				showContentWindow(v, Type_Font);
			} else if (viewId == R.id.rl_size) {
				showContentWindow(v, Type_Size);
			} else if (viewId == R.id.rl_alignment) {
				showContentWindow(v, Type_Alignment);
			} else if (viewId == R.id.rl_justinfication) {
				showContentWindow(v, Type_Justinfication);
			} else if (viewId == R.id.rl_color) {
				showContentWindow(v, Type_Color);
			} else if (viewId == R.id.cancelButton) {
				hiddenSoftInput();
			} else if (viewId == R.id.doneButton) {
				textBlock.text = editTextInput.getText().toString();
				mListener.updateTextCollage(CollagePage, CollageLayer, textBlock);
				hiddenSoftInput();
			}
		}

	};

	private void showContentWindow(View view, int type) {
		if (popWindow != null) {
			popWindow.dismiss();
		}
		int width = 0;
		BaseAdapter adapter = null;
		PopWindowOnItemClickListener listener = new PopWindowOnItemClickListener(type);
		int xoffset = 0;
		width = view.getWidth();
		switch (type) {
		case Type_Font:
			fonts = AppContext.getApplication().getFonts();
			adapter = new CollageFontAdapter(mContext, fonts);
			break;
		case Type_Size:
			adapter = new CollageFontSizeAdapter(mContext, fontSizes);
			break;
		case Type_Alignment:
			adapter = new CollageFontImageAdapter(mContext, fontAlignmentsSource);
			break;
		case Type_Justinfication:
			adapter = new CollageFontImageAdapter(mContext, fontJustificationSource);
			break;
		case Type_Color:
			int columesWidth = bt_panel.getWidth() * 8 / 10 / 9;
			adapter = new CollageFontColorAdapter(mContext, fontColors, columesWidth);
			break;
		}
		View v = null;
		if (type == Type_Color) {
			v = inflate(mContext, R.layout.collage_color_gridview, null);
			gvColors = (GridView) v.findViewById(R.id.gv_color);
			gvColors.setAdapter(adapter);
			gvColors.setOnItemClickListener(listener);
			width = bt_panel.getWidth() * 8 / 10;
		} else {
			v = inflate(mContext, R.layout.collage_font_listview, null);
			lvContent = (ListView) v.findViewById(R.id.lv_content);
			lvContent.setDivider(null);
			lvContent.setAdapter(adapter);
			lvContent.setOnItemClickListener(listener);

		}
		popWindow = new PopupWindow(v, width, getHeight()-mSoftkeybardHeight);
		popWindow.setFocusable(true);
		popWindow.setBackgroundDrawable(new PaintDrawable(Color.TRANSPARENT));
		popWindow.setOutsideTouchable(true);
		int[] location = new int[2];  
		view.getLocationOnScreen(location);  
		popWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1]-popWindow.getHeight());  
		adapter.notifyDataSetChanged();
	}

	class PopWindowOnItemClickListener implements OnItemClickListener {
		private int type;

		public PopWindowOnItemClickListener(int type) {
			this.type = type;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			switch (type) {
			case Type_Font:
				tvFont.setText(fonts.get(position).displayName);
				mImageFetcher.setFontText(tvFont);
				mImageFetcher.loadImage(fonts.get(position).sampleURL, fonts.get(position).sampleURL, ivFont, LoadImageType.WEB_FONT_IMAGE);
				textBlock.font = fonts.get(position);
				textBlock.fontName = fonts.get(position).name;
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
				((GradientDrawable) colorContainer.getBackground()).setColor(Color.parseColor(fontColors[position]));
				textBlock.color = fontColors[position];
				break;
			}

			if (popWindow != null) {
				popWindow.dismiss();
			}
		}

	};

	private float getFontHeight(float fontSize) {
		Paint paint = new Paint();
		paint.setTextSize(fontSize);
		FontMetrics fm = paint.getFontMetrics();
		float fFontHeight = (float) Math.ceil(fm.descent - fm.ascent);
		return fFontHeight;

	}

	private void getViews() {
		cancelButton = ((Button) findViewById(R.id.cancelButton));
		doneButton = ((Button) findViewById(R.id.doneButton));
		bt_panel = (LinearLayout) findViewById(R.id.bt_panel);
		fontContainer = findViewById(R.id.rl_font);
		sizeContainer = findViewById(R.id.rl_size);
		alignmentContainer = findViewById(R.id.rl_alignment);
		justificationContainer = findViewById(R.id.rl_justinfication);
		colorContainer = findViewById(R.id.rl_color);

		tvFont = (TextView) fontContainer.findViewById(R.id.v_content);
		ivFont = (ImageView) fontContainer.findViewById(R.id.v_image_content);
		tvSize = (TextView) sizeContainer.findViewById(R.id.v_content);
		tvAlignment = (TextView) alignmentContainer.findViewById(R.id.v_content);
		tvJustification = (TextView) justificationContainer.findViewById(R.id.v_content);
		editTextInput = ((EditText) findViewById(R.id.collageEditTextInput));
		AppContext.getApplication().setEmojiFilter(editTextInput) ;
		editTextInput.requestFocus();
		initCollageEditTextInputParment();
	}

	private void setEvents() {
		fontContainer.setOnClickListener(onClickListener);
		sizeContainer.setOnClickListener(onClickListener);
		alignmentContainer.setOnClickListener(onClickListener);
		justificationContainer.setOnClickListener(onClickListener);
		colorContainer.setOnClickListener(onClickListener);
		cancelButton.setOnClickListener(onClickListener);
		doneButton.setOnClickListener(onClickListener);
	}

	private void initButtonValues() {
		textBlock = CollageLayer.getTextBlock();
		boolean isHaveSampleText = false;
		if (CollageLayer.data != null) {
			for (Data d : CollageLayer.data) {
				String name = d.name == null ? "" : d.name;
				if (Data.TYPE_SAMPLETEXT.equals(name)) {
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
				} else {
					textBlock.text = textBlock.defaultText;
				}
			} else {
				textBlock.text = textBlock.defaultText;
			}
		}
		text = textBlock.text;

		for (int i = 0; i < fonts.size(); i++) {
			if (fonts.get(i).name.equalsIgnoreCase(textBlock.fontName)) {
				textBlock.font = fonts.get(i);
				mImageFetcher.loadImage(textBlock.font.sampleURL, textBlock.font.sampleURL, ivFont, LoadImageType.WEB_FONT_IMAGE);
				break;
			}
		}

		String size = "";

		if (CollageLayer instanceof Layer && (CollageLayer).fontSize > 0) {
			textBlock.fontSize = String.valueOf(CollageLayer.fontSize);
		}
		if ("".equals(textBlock.fontSize)) {
			textBlock.fontSize = "Auto";
		}

		if ("Auto".equalsIgnoreCase(textBlock.fontSize)) {
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

		for (int i = 0; i < fontAlignments.length; i++) {
			if (fontAlignments[i].equalsIgnoreCase(textBlock.alignment)) {
				tvAlignment.setBackgroundResource(fontAlignmentsSource[i]);
				break;
			}
		}

		for (int i = 0; i < fontJustifications.length; i++) {
			if (fontJustifications[i].equalsIgnoreCase(textBlock.justification)) {
				tvJustification.setBackgroundResource(fontJustificationSource[i]);
			}
		}

		String sampleText = getResources().getString(R.string.Common_SampleText);
		if (sampleText.equals(textBlock.text) || ((!textBlock.isAppendable) && (text != null && text.equals(textBlock.text)))) {
			editTextInput.setSelectAllOnFocus(true);
		} else {
			editTextInput.setSelectAllOnFocus(false);
		}

		editTextInput.setText(textBlock.text);

		((GradientDrawable) colorContainer.getBackground()).setColor(Color.parseColor(textBlock.color));
	}

	private void hiddenSoftInput() {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(((Activity) mContext).getCurrentFocus().getWindowToken(), 0);
		mListener.updateUI();
		editTextInput.requestFocus();
	}

	private void initCollageEditTextInputParment() {
		
		SoftKeyboardUtil.observeSoftKeyboard((Activity) mContext, new OnSoftKeyboardChangeListener() {

			@Override
			public void onSoftKeyBoardChange(int softkeybardHeight, boolean visible) {
				if (previousKeyboradHeight == softkeybardHeight) {
					return;
				}

				previousKeyboradHeight = softkeybardHeight;
				RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) bt_panel.getLayoutParams();
				if (!alreadyGetData){
					if (visible) {
						params.bottomMargin = softkeybardHeight;
						mSoftkeybardHeight = softkeybardHeight;
						alreadyGetData = true;
					} else {
						params.bottomMargin = 0;
					}
					bt_panel.setLayoutParams(params);					
				}
				
			}
		});

	}
}