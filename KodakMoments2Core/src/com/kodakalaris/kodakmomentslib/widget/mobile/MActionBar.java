package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.AppConstants.ActivityTheme;
import com.kodakalaris.kodakmomentslib.R;

public class MActionBar extends RelativeLayout{
	public static final int THEME_LIGHT = 0;
	public static final int THEME_DARK = 1;
	public static final int TITLE_GRAVITY_CENTER = 0;
	public static final int TITLE_GRAVITY_LEFT = 1;
	public static final int TITLE_GRAVITY_RIGHT = 2;
	
	private ActivityTheme mTheme = ActivityTheme.LIGHT;
	private String mTitle;
	private int mTitleLogo;
	private int mLeftButtonRes;
	private int mRightButtonRes;
	private int mRightTextRes;
	private int mTitleGravity = TITLE_GRAVITY_CENTER;
	
	private TextView vTxtTitle;
	private ImageView vIvTitleLogo;
	private ImageButton vIbtnLeft;
	private ImageButton vIbtnRight;
	private TextView vTxtRight;

	public MActionBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MActionBar);  
		mTitle = a.getString(R.styleable.MActionBar_title_text);
		mTitleLogo = a.getResourceId(R.styleable.MActionBar_title_logo, 0);
		mTheme = a.getInteger(R.styleable.MActionBar_theme, THEME_LIGHT) == THEME_LIGHT ? ActivityTheme.LIGHT : ActivityTheme.DARK;
		mTitleGravity = a.getInteger(R.styleable.MActionBar_title_gravity, TITLE_GRAVITY_CENTER);
		mLeftButtonRes = a.getResourceId(R.styleable.MActionBar_left_button_image_src, 0);
		mRightButtonRes = a.getResourceId(R.styleable.MActionBar_right_button_image_src, 0);
		mRightTextRes = a.getResourceId(R.styleable.MActionBar_right_button_text_src, 0);
		
		a.recycle();
		
		init(context);
	}

	public MActionBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MActionBar(Context context) {
		super(context);
		
		init(context);
	}
	
	private void findViews() {
		vTxtTitle = (TextView) findViewById(R.id.txt_title);
		vIvTitleLogo = (ImageView) findViewById(R.id.iv_title_logo);
		vIbtnLeft = (ImageButton) findViewById(R.id.ibtn_left);
		vIbtnRight = (ImageButton) findViewById(R.id.ibtn_right);
		vTxtRight = (TextView) findViewById(R.id.tv_right);
	}
	
	public void init(Context context) {
		if (mTheme == ActivityTheme.LIGHT) {
			inflate(context, R.layout.view_m_action_bar_light, this);
		} else if (mTheme == ActivityTheme.DARK) {
			inflate(context, R.layout.view_m_action_bar_dark, this);
		} else {
			inflate(context, R.layout.view_m_action_bar_light, this);
		}
		
		findViews();
		
		setTitle(mTitle);
		setTitleLogo(mTitleLogo);
		setLeftButtonImage(mLeftButtonRes);
		if(mRightButtonRes != 0){
			setRightButtonImage(mRightButtonRes);
		} else if(mRightTextRes != 0) {
			setRightText(mRightTextRes);
		}
		
		
		switch (mTitleGravity) {
		case TITLE_GRAVITY_CENTER:
			//Because when set it in center, maybe the right button or left button is null(0 width)
			//So we need to put it in middle right
			LayoutParams params = (LayoutParams) vTxtTitle.getLayoutParams();
			params.addRule(RIGHT_OF, 0);
			params.addRule(LEFT_OF, 0);
			vTxtTitle.setGravity(Gravity.CENTER);
			break;
		case TITLE_GRAVITY_LEFT:
			vTxtTitle.setGravity(Gravity.LEFT);
			break;
		case TITLE_GRAVITY_RIGHT:
			vTxtTitle.setGravity(Gravity.RIGHT);
			break;
		default:
			break;
		}
	}
	
	public void setRightButtonVisiable(boolean isVisible){
		if(!isVisible){
			vIbtnRight.setVisibility(View.INVISIBLE);
		}
	}
	
	
	public void setOnLeftButtonClickListener(OnClickListener onClickListener) {
		vIbtnLeft.setOnClickListener(onClickListener);
	}
	
	public void setOnRightButtonClickListener(OnClickListener onClickListener) {
		vIbtnRight.setOnClickListener(onClickListener);
		vTxtRight.setOnClickListener(onClickListener);
	}
	
	public void setTitle(String text) {
		vTxtTitle.setText(text == null ? "" : text);
	}
	
	public void setLeftButtonImage(int resId) {
		if (resId != 0) {
			vIbtnLeft.setImageResource(resId);
		}
	}
	
	public void setRightButtonImage(int resId) {
		if (resId != 0) {
			vIbtnRight.setImageResource(resId);
			vTxtRight.setVisibility(View.GONE);
		}
	}
	
	public void setRightBtnEnabled(boolean enabled) {
		if (vIbtnRight != null) {
			vIbtnRight.setEnabled(enabled);
		}
		if (vTxtRight != null) {
			vTxtRight.setEnabled(enabled);
		}
	}
	
	public void setRightText(int resId) {
		if (resId != 0) {
			vTxtRight.setText(resId);
			vIbtnRight.setVisibility(View.GONE);
		}
	}
	
	public void setTitleLogo(int resId){
		if(resId != 0){
			vIvTitleLogo.setImageResource(resId);
		}
	}
	
}
