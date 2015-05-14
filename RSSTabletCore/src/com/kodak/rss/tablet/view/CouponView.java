package com.kodak.rss.tablet.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.shoppingcart.Cart;
import com.kodak.rss.core.n2r.bean.shoppingcart.Discount;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.ShoppingCartActivity;
import com.kodak.rss.tablet.thread.SendingOrderTask;

public class CouponView extends FrameLayout implements OnClickListener{
		
	private ShoppingCartActivity mContext;
	private DisplayMetrics dm;
	private InputMethodManager imm;
	private EditText couponIdEdit;
	private Button applyButton;
	private Button doneButton;
	
	private TextView validInfoText;
	private TextView termConditionText;
	
	public int topMSpace = 0;
	private int initTopSpace = 0;	
	
	private String cartId, retailerId, products;
	private String couponTermsUrl = "";
		
	public CouponView(Context context) {
		super(context);
		init(context);
	}
	
	public CouponView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public CouponView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	protected void init(Context context) {
		mContext = (ShoppingCartActivity) context;
		dm = getResources().getDisplayMetrics();
		imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		inflate(mContext, R.layout.dialog_coupon, this);				
		couponIdEdit = (EditText) findViewById(R.id.coupon_id);
		TextUtil.addEmojiFilter(couponIdEdit);
		applyButton = (Button) findViewById(R.id.apply_button);
		doneButton = (Button) findViewById(R.id.done_button);
		
		validInfoText = (TextView) findViewById(R.id.valid_info);
		termConditionText = (TextView) findViewById(R.id.term_condition);
		
		applyButton.setOnClickListener(this);
		doneButton.setOnClickListener(this);
		termConditionText.setOnClickListener(this);
		couponIdEdit.setOnEditorActionListener(editorActionListener);
	}
	
	public CouponView setViewSize(){
		int width = dm.widthPixels *5/12;
		int height = (int) (width/2 - dm.density*4);
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
		params.height = height;
		params.width = width;
		int topSpace = (int) (dm.heightPixels/2 - height + dm.density*15);
		int topMargin = (int) (topSpace > 0 ? topSpace : dm.density*150);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.topMargin = topMargin;
		initTopSpace = topMargin;
		topMSpace = dm.heightPixels - height - topSpace;		
		setLayoutParams(params);
		return this;
	}
	
	public CouponView setNeededInfo(String cartId, String retailerId, String productsWithCount){
		this.cartId = cartId;
		this.retailerId = retailerId;
		this.products = productsWithCount;
		return this;
	}
	
	private void hideSoftKeyboard(){
		if(mContext instanceof Activity){
			imm.hideSoftInputFromWindow(((Activity)mContext).getCurrentFocus().getWindowToken(), 0);
		}
	}
	
	public void dismiss(){		
		((View)getParent()).setVisibility(View.GONE);		
		hideSoftKeyboard();
	}
	
	private int curSoftKeyboardHeight = 0;
	private int preSoftKeyboardHeight = 0;
	public void setViewShow(){
		((View)getParent()).setVisibility(View.VISIBLE);
		CouponView.this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
	        @Override
	        public void onGlobalLayout() {	           
	            Rect r = new Rect();
	            CouponView.this.getWindowVisibleDisplayFrame(r);	            
	            preSoftKeyboardHeight = curSoftKeyboardHeight;
	            curSoftKeyboardHeight = dm.heightPixels - (r.bottom - r.top); 	
	            if (curSoftKeyboardHeight - preSoftKeyboardHeight != 0 ) {	            	
	            	if (curSoftKeyboardHeight > 0 ) {	
	            		final int space = curSoftKeyboardHeight - topMSpace;	            		
	            		if (space <= 0) return;	  	      
	            		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) CouponView.this.getLayoutParams();																	
	            		int topM = params.topMargin;
						if (topM < initTopSpace) return;
						TranslateAnimation ta = new TranslateAnimation(0, 0, space, 0);						
						ta.setDuration(150);							
						params.topMargin = topM - space;
						CouponView.this.setLayoutParams(params);
						CouponView.this.requestLayout();
						CouponView.this.startAnimation(ta);							
					}else {	
						final int space = preSoftKeyboardHeight - topMSpace;						
	            		if (space <= 0) return;	 
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) CouponView.this.getLayoutParams();	
						int topM = params.topMargin;
    					if (topM == initTopSpace) return;		            		
						final TranslateAnimation ta = new TranslateAnimation(0, 0, 0, space);						
						ta.setDuration(150);	
						ta.setFillAfter(true);	
						ta.setAnimationListener(new AnimationListener() {			
		        			@Override
		        			public void onAnimationStart(Animation animation) {
		        				RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) CouponView.this.getLayoutParams();	
		    					int topM = params.topMargin;
		    					if (topM == initTopSpace) {
		    						ta.cancel();
		    						CouponView.this.clearAnimation();
		    					}
		        			}			
		        			@Override
		        			public void onAnimationRepeat(Animation animation) {}			
		        			@Override
		        			public void onAnimationEnd(Animation animation) {
		        				CouponView.this.clearAnimation();
		        				RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) CouponView.this.getLayoutParams();	
		    					int topM = params.topMargin;
		    					if (topM == initTopSpace) return;	
		        				params.topMargin = topM + space;
		        				CouponView.this.setLayoutParams(params);
		        				CouponView.this.requestLayout();
		        			}
		        		});			            	
						CouponView.this.startAnimation(ta);					
					}	            
				}	            
	        }
	    });			
	}
	
	public boolean isShowing(){
		return ((View)getParent()).getVisibility()==View.VISIBLE;
	}

	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		if(viewId == R.id.apply_button){
			String couponText = couponIdEdit.getText().toString().trim();
			if (couponText == null || (couponText != null && "".equals(couponText))) {
				Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);  
				couponIdEdit.startAnimation(shake);  
				return;
			}
			RssTabletApp.getInstance().setCouponCode(couponText);
			validInfoText.setText("");
			termConditionText.setText("");
			applyButton.setEnabled(false);
			doneButton.setEnabled(false);
			new Thread(apply).start();	
		}else if(viewId == R.id.done_button){
			RssTabletApp.getInstance().setCouponCode(couponIdEdit.getText().toString().trim());
			mContext.refreshPrice();
			dismiss();
		}else if(viewId == R.id.term_condition){
			mContext.showCouponTerms(couponTermsUrl);
		}		
	}
	
	OnEditorActionListener editorActionListener = new OnEditorActionListener() {
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(v.getId() == R.id.coupon_id){
				if(actionId == EditorInfo.IME_ACTION_DONE){
					String couponText = couponIdEdit.getText().toString().trim();
					if (couponText == null || (couponText != null && "".equals(couponText))) {
						Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);  
						couponIdEdit.startAnimation(shake);  
						return false;
					}
					RssTabletApp.getInstance().setCouponCode(couponText);
					validInfoText.setText("");
					termConditionText.setText("");
					applyButton.setEnabled(false);
					doneButton.setEnabled(false);
					new Thread(apply).start();	
				}
			}
			return false;
		}
	};
		
	private final int APPLAY_FINISHED = 1;		
	Handler applayHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {			
			case APPLAY_FINISHED:
				Cart cart = null;		
				if (msg.obj != null) {
					cart = (Cart) msg.obj;		
				}
				String couponText = couponIdEdit.getText().toString();
				if (cart != null && cart.discounts != null) {
					for (Discount discount : cart.discounts) {
						if (discount == null) continue;
						if (discount.code == null) continue;	
						if (discount.code.equals(couponText)) {
							if (discount.isError()) {
								validInfoText.setText(discount.localizedStatusDescription+"("+couponText +")");
								RssTabletApp.getInstance().setCouponCode("");
							}else {
								if (discount.termsAndConditionsURL != null && !"".equals(discount.termsAndConditionsURL)) {
									termConditionText.setVisibility(View.VISIBLE);
									termConditionText.setText(R.string.show_details);
									couponTermsUrl = discount.termsAndConditionsURL;
								}else {
									termConditionText.setVisibility(View.INVISIBLE);
									termConditionText.setText("");
									couponTermsUrl = "";
								}							
								validInfoText.setText(couponText+" - "+discount.localizedStatusDescription);
								RssTabletApp.getInstance().setCouponCode(discount.code);
							}
							break;
						}					
					}			
				}	
				applyButton.setEnabled(true);
				doneButton.setEnabled(true);
				break;
			}	
		}		
	};	
	
	Runnable apply = new Runnable(){
		@Override
		public void run() {			
			RssTabletApp app = RssTabletApp.getInstance();
			WebService service = new WebService(mContext);						
			if(mContext.cartId.equals("")){	
				Cart cart = null;
				try {
					cart = service.createCartTask();
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
				if(cart != null){
					mContext.cartId = cart.cartId;
				}
			}
			String couponText = couponIdEdit.getText().toString();
			if (couponText == null) {
				applayHandler.obtainMessage(APPLAY_FINISHED).sendToTarget();
				return;
			}
			if ("".equals(couponText)) {
				applayHandler.obtainMessage(APPLAY_FINISHED).sendToTarget();
				return;
			}
			
			Cart cart = null;
			try {
				//cart = service.checkCouponCodeTask(mContext.cartId, couponText);
				cart = service.priceProduct3Task(cartId, retailerId, products, app.getCouponCode());
			} catch (RssWebServiceException e) {				
				e.printStackTrace();
			}		
			applayHandler.obtainMessage(APPLAY_FINISHED,cart).sendToTarget();
		}
	};
	

}
