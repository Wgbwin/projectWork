package com.kodak.kodak_kioskconnect_n2r.view;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintMakerWebService;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.activity.ShoppingCartActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.Discount;
import com.kodak.kodak_kioskconnect_n2r.bean.shoppingcart.Cart;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CouponInputDialog extends Dialog {
	private static final String TAG = CouponInputDialog.class.getSimpleName();
	
	private static final float widthRatio = 0.7f;
	private static final float heightRatio = 0.8f;
	
	private EditText etCouponInput;
	private TextView tvCouponStatus;
	private Button btApply;
	private Button btDone;
	private View progressContainer;
	
	private PrintMakerWebService mService;
	private String mProducts;
	private String mRetailerId;
	private Cart mCart;
	
	private Context mContext;
	
	public CouponInputDialog(Context context, int theme, String retailerId, String products, Cart cart) {
		super(context, theme);
		this.mContext = context;
		setContentView(R.layout.dialog_coupon_input);
		mService = new PrintMakerWebService(mContext, "");
		this.mProducts = products;
		this.mRetailerId = retailerId;
		this.mCart = cart;
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((ShoppingCartActivity)mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int width = (int) (displayMetrics.widthPixels * widthRatio);
		final int height = (int) (displayMetrics.heightPixels * heightRatio);
		getWindow().setLayout(width, height);
		getViews();
		setEvents();
	}	
	
	private void getViews(){
		etCouponInput = (EditText) findViewById(R.id.etCouponInput);
		AppContext.getApplication().setEmojiFilter(etCouponInput);
		tvCouponStatus = (TextView) findViewById(R.id.tvCouponStatus);
		btApply = (Button) findViewById(R.id.btApply);
		btDone = (Button) findViewById(R.id.btDone);
		btApply.setEnabled(true);
		progressContainer = findViewById(R.id.waiting_container);
	}
	
	private void setEvents(){
		btApply.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PrintHelper.couponCode = etCouponInput.getText().toString().trim();
				new CheckCouponTask().execute();
			}
		});
		
		btDone.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PrintHelper.couponCode = etCouponInput.getText().toString().trim();
				((ShoppingCartActivity)mContext).refreshPrice();
				dismiss();
			}
		});
	}
	
	@Override
	public void show() {
		if(PrintHelper.couponCode!=null && !PrintHelper.couponCode.equals("")){
			etCouponInput.setText(PrintHelper.couponCode);
		}
		if(mCart != null && mCart.discounts!=null && mCart.discounts.length>0){
			tvCouponStatus.setText(mCart.discounts[0].localizedStatusDescription);
		}
		super.show();
	}

	private class CheckCouponTask extends AsyncTask<Void, Void, Discount>{

		@Override
		protected void onPreExecute() {
			tvCouponStatus.setText("");
			btApply.setEnabled(false);
			progressContainer.setVisibility(View.VISIBLE);
		}

		@Override
		protected Discount doInBackground(Void... params) {
			Cart cart = mService.PriceProducts2(mContext, mProducts, mRetailerId);
			if(cart != null){
				if(cart.discounts!=null && cart.discounts.length>0){
					return cart.discounts[0];
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Discount discount) {
			progressContainer.setVisibility(View.GONE);
			btApply.setEnabled(true);
			if(discount != null){
				tvCouponStatus.setText(discount.localizedStatusDescription);
			} else {
				tvCouponStatus.setText(R.string.problemSendingOrder);
			}
		}

		
		
	}

}
