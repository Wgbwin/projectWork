package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.AppConstants.ActivityTheme;

public class TipBar extends RelativeLayout {
	private static final long DEFAULT_CLOSE_DELAY  = 10000;
	private static final boolean DEFAULT_AUTO_CLOSE = true;
	
	private String mText;
	private boolean mIsAutoClose = DEFAULT_AUTO_CLOSE;
	private long mCloseDelay = DEFAULT_CLOSE_DELAY; //unit: ms
	
	private TextView vTxtContent;
	private View vBtnClose;
	
	public TipBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TipBar);
		mText = a.getString(R.styleable.TipBar_text);
		mIsAutoClose = a.getBoolean(R.styleable.TipBar_autoClose, DEFAULT_AUTO_CLOSE);
		mCloseDelay = (long) (a.getFloat(R.styleable.TipBar_closeDelay, DEFAULT_CLOSE_DELAY / 1000) * 1000);
		
		a.recycle();
		
		init(context);
	}

	public TipBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TipBar(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		inflate(context, R.layout.include_tip_bar, this);
		
		vBtnClose = findViewById(R.id.btn_close);
		vTxtContent = (TextView) findViewById(R.id.txt_content);
		
		setContent(mText);
		
		vBtnClose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		if (mIsAutoClose) {
			postDelayed(new Runnable() {
				
				@Override
				public void run() {
					dismiss();
				}
			}, mCloseDelay);
		}
	}
	
	public void setContent(String text) {
		mText = text;
		
		if (vTxtContent != null) {
			vTxtContent.setText(text);
		}
	}
	
	public void setContent(int textResId) {
		setContent(getContext().getString(textResId));
	}
	
	public void dismiss() {
		setVisibility(View.GONE);
	}
	
	public void setCloseDetayTime(long timeMilles) {
		mCloseDelay = timeMilles;
	}
	
	public void closeDelay() {
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				dismiss();
			}
		}, mCloseDelay);
	}
	
}
