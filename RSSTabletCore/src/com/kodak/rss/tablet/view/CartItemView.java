package com.kodak.rss.tablet.view;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.CartListAdapter;
import com.kodak.rss.tablet.bean.StorePriceInfo;
import com.kodak.rss.tablet.util.ShoppingCartUtil;

/**
 * Purpose: 
 * Author: Bing Wang
 * Created Time: Sep 2, 2013 13:20:43 PM 
 */
public class CartItemView extends LinearLayout{
    private String TAG = "CartItemView:";
	private TextView productPrice;
	private TextView productCategory;
	private Button quantityMinusButton;
	private TextView cartNum;
	private Button quantityPlusButton;
	public String category;
	public double price;
	public int num;	
	float scale = 1.0f;
	BitmapDrawable bd;
	CartListAdapter cartListAdapter;
	int position;	
	private Context context;
	
	public CartItemView(Context context,CartListAdapter cartListAdapter) {
		super(context);
		this.context = context;
		inflate(context,R.layout.cart_item, this);	
		this.cartListAdapter = cartListAdapter;			
		scale = getContext().getResources().getDisplayMetrics().density;
		Bitmap highlight = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallbackpanel_sel_xxhdpi); 
		Bitmap cart_highlight = Bitmap.createScaledBitmap(highlight, (int) (255*scale),(int) (55*scale), true);
		bd=new BitmapDrawable(cart_highlight);
		highlight.recycle();
		initViewAndAction();				
	}		

	public void setPos(int position){
		this.position = position;	
	}
	
	private  void initViewAndAction(){		
		productPrice = (TextView) findViewById(R.id.productPrice);
		productCategory = (TextView) findViewById(R.id.productCategory);
		quantityMinusButton = (Button) findViewById(R.id.quantityMinusButton);		
		cartNum = (TextView) findViewById(R.id.cartNum);
		quantityPlusButton = (Button) findViewById(R.id.quantityPlusButton);	
		
		quantityMinusButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				setBackground();
				int quantityIncrement = cartListAdapter.productBuckets.get(position).quantityIncrement;
				int quantity = Integer.parseInt(cartNum.getText().toString());
				if (quantity <= quantityIncrement){
					if (judgeHaveItem(quantity,position)) {
						quantity = 0;
					}else {
						quantity = quantityIncrement;
					}					
				}else{
					quantity-=quantityIncrement;					
				}
				cartListAdapter.productBuckets.get(position).num = quantity;
				cartNum.setText("" + quantity);				
				
				for (int i = 0; i < cartListAdapter.productBuckets.size(); i++) {
					if (i == position) {
						cartListAdapter.productBuckets.get(position).isCurrentChecked =true;
					}else {
						cartListAdapter.productBuckets.get(i).isCurrentChecked =false;
					}					
				}	
				ShoppingCartUtil.dealWithItem(cartListAdapter.editImageView.imageInfo, cartListAdapter.productBuckets.get(position));
				cartListAdapter.notifyDataSetChanged();
			}});
		
		quantityPlusButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				setBackground();
				int quantityIncrement = cartListAdapter.productBuckets.get(position).quantityIncrement;
				int quantity = Integer.parseInt(cartNum.getText().toString());			
				quantity+=quantityIncrement;
				cartListAdapter.productBuckets.get(position).num = quantity;
				cartNum.setText("" + quantity);	
				
				for (int i = 0; i < cartListAdapter.productBuckets.size(); i++) {
					if (i == position) {
						cartListAdapter.productBuckets.get(position).isCurrentChecked =true;
					}else {
						cartListAdapter.productBuckets.get(i).isCurrentChecked =false;
					}					
				}
				ShoppingCartUtil.dealWithItem(cartListAdapter.editImageView.imageInfo, cartListAdapter.productBuckets.get(position));
				cartListAdapter.notifyDataSetChanged();
			}});
	}
	
	private boolean judgeHaveItem(int quantity,int position){
		boolean isCanMinus = false;			
		List <ProductInfo>  productBuckets = cartListAdapter.productBuckets;			
		for (int i=0; i < productBuckets.size(); i++) {
			if (i != position) {
				ProductInfo productInfo = productBuckets.get(i);
				if (productInfo != null && productInfo.num > 0) {
					isCanMinus = true;
					break;
				}
			}
		}					
		return isCanMinus;
	}
	
	public void setControlValue(ProductInfo pInfo){					
		if(pInfo.isCurrentChecked){
			setBackground();				
		}else {
			replyBackgroud();				
		}
		cartNum.setText(String.valueOf(pInfo.num));	
		productCategory.setText(pInfo.category);
		boolean ifShowPrice = ifShowPriceForPrints ();
		if (ifShowPrice){
//			productPrice.setText(pInfo.price);
			productPrice.setText(getStorePrice(pInfo));
		}			
	}		
	
	public void setBackground(){
		this.setBackgroundDrawable(bd);
	}		
	
	public void replyBackgroud(){
		this.setBackgroundDrawable(null);
	}
	
	private boolean ifShowPriceForPrints () {
		if (ShoppingCartUtil.isShowsMSRPPricing()){
			return true;
		}
		String currentRetailerID = SharedPreferrenceUtil.getString(context,SharedPreferrenceUtil.SELECTED_RETAILER_ID);
		if (currentRetailerID !=""){
			return true;
		}
		return false;
	}
	
	private String getStorePrice(ProductInfo pInfo){
		if (pInfo == null) return "";
		if (pInfo.descriptionId == null) return pInfo.price;
		if (cartListAdapter.StorePriceInfoList == null) return pInfo.price;
		for (StorePriceInfo lItem : cartListAdapter.StorePriceInfoList) {
			if (lItem == null)continue;
			if (lItem.descriptionId == null)continue;
			if (lItem.unitPrice == null)continue;
			if (pInfo.descriptionId.equals(lItem.descriptionId)) {
				return lItem.unitPrice;
			}			
		}
		return pInfo.price;
	}
	
}
