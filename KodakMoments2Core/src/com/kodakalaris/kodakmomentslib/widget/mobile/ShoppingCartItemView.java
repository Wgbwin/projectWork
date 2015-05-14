package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Pricing.LineItem;
import com.kodakalaris.kodakmomentslib.widget.mobile.ShoppingCartItemChildView.Type;

public class ShoppingCartItemView extends FrameLayout {
	private static final String TAG = "ShoppingCartItemView";
	
	private Context mContext;
	
	private View vViewDividerBottom;
	private LinearLayout vLinelyEditBtns;
	private Button vBtnEdit;
	private Button vBtnDelete;
	private ShoppingCartItemChildView vChildViewMain;
	private ShoppingCartItemChildView vChildViewAddiotnalPage;
	private ShoppingCartItemChildView vChildViewDiscount;
	
	private int mInitialLeftMarginForContent;
	
	public ShoppingCartItemView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ShoppingCartItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ShoppingCartItemView(Context context) {
		super(context);
		init(context);
	} 
	
	private void init(Context context) {
		mContext = context;
		inflate(context, R.layout.item_shopping_cart_product, this);
		
		setFocusable(true);
		setFocusableInTouchMode(true);
		
		vViewDividerBottom = findViewById(R.id.view_bottom_divider);
		vLinelyEditBtns = (LinearLayout) findViewById(R.id.linely_edit_btns);
		vBtnDelete = (Button) findViewById(R.id.btn_delete);
		vBtnEdit = (Button) findViewById(R.id.btn_edit);
		vChildViewDiscount = (ShoppingCartItemChildView) findViewById(R.id.view_item_discount);
		vChildViewAddiotnalPage = (ShoppingCartItemChildView) findViewById(R.id.view_item_additionnal_page);
		vChildViewMain = (ShoppingCartItemChildView) findViewById(R.id.view_item_main_info);
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) vChildViewMain.getLayoutParams();
		mInitialLeftMarginForContent = params.leftMargin;
		
	}
	
	public void setInfo(LineItem item) {
		//TODO hard code
		vChildViewAddiotnalPage.setVisibility(View.GONE);
		vChildViewAddiotnalPage.setType(ShoppingCartItemChildView.Type.ADITIONAL_PAGE);
		vChildViewDiscount.setVisibility(View.GONE);
		vChildViewDiscount.setType(Type.DISCOUNT);
		
		vChildViewMain.setType(Type.PRODUCT_INFO);
		vChildViewMain.setQuantity(item.quantity);
		vChildViewMain.setContentFor1stLine(item.name);
		vChildViewMain.setPrice(item.totalPrice.priceStr);
		
		if (isOnlyShowEditBtn()) {
			vBtnDelete.setVisibility(View.GONE);
		} else {
			vBtnDelete.setVisibility(View.VISIBLE);
		}
		
		vLinelyEditBtns.setVisibility(View.INVISIBLE);//set invisible to avoid ontouch edit btn
	}
	
	public void setOnEditBtnClickListener(OnClickListener listener) {
		vBtnEdit.setOnClickListener(listener);
	}
	
	public void setOnDeleteBtnClickListener(OnClickListener listener) {
		vBtnDelete.setOnClickListener(listener);
	}
	
	public void setBottomDividerVisibility(int visibility) {
		vViewDividerBottom.setVisibility(visibility);
	}
	
	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		
		if (gainFocus) {
			System.out.println("gainFocus");
		} else {
			System.out.println("un gainFocus");
		}
	}
	
	
	private float mXDown;
	private float mYDown;
	private boolean mReleased;
	private final int SWIPE_SLOP = 100;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		System.out.println("onTouchEvent:" + event.getAction());
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mXDown = event.getX();
			mYDown = event.getY();
			mReleased = false;
			break;

		case MotionEvent.ACTION_MOVE:
			if (!mReleased) {
				if (mXDown - event.getX() > SWIPE_SLOP) {
					onSwipeLeft();
					mReleased = true;
				} else if (event.getX() - mXDown > SWIPE_SLOP) {
					onSwipeRight();
					mReleased = true;
				}
			}
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mReleased = true;
			break;
			
		}
		return true;
	}
	
	private void onSwipeLeft() {
		if (!isShownEditBtns()) {
			changeEditBtnsVisibility(true);
		}
	}
	
	private void onSwipeRight() {
		if (isShownEditBtns()) {
			changeEditBtnsVisibility(false);
		}
	}
	
	private void changeEditBtnsVisibility(boolean visible) {
		final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) vChildViewMain.getLayoutParams();
		if (params.width == RelativeLayout.LayoutParams.MATCH_PARENT) {
			params.width = vChildViewMain.getWidth();
		}
		
		int offset = getWidth() / 2;
		if (isOnlyShowEditBtn()) {
			offset = offset / 2;
		}
		
		vLinelyEditBtns.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		if (visible) {
			params.leftMargin = - offset;
			vChildViewMain.requestLayout();
			TranslateAnimation animation = new TranslateAnimation(offset, 0 ,0, 0);
			animation.setDuration(300);
			vChildViewMain.startAnimation(animation);
		} else {
			params.leftMargin = mInitialLeftMarginForContent;
			vChildViewMain.requestLayout();
			TranslateAnimation animation = new TranslateAnimation(-offset, 0 ,0, 0);
			animation.setDuration(300);
			vChildViewMain.startAnimation(animation);
			
		}
		
	}
	
	private boolean isShownEditBtns() {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) vChildViewMain.getLayoutParams();
		return params.leftMargin < 0;
	}
	
	private boolean isOnlyShowEditBtn() {
		return true;
	}
}
