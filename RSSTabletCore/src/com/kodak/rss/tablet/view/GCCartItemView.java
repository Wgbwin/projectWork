package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.util.GreetingCardUtil;

public class GCCartItemView extends LinearLayout{
	private TextView quantityMinusButton;
	private TextView cartNum;
	private TextView quantityPlusButton;
	public String category;
	public int num;
	private Context mContex;
			
	public GCCartItemView(Context context) {
		super(context);						
		initViewAndAction(context);				
	}	
	
	public GCCartItemView(Context context, AttributeSet attrs) {
		super(context,attrs);
		initViewAndAction(context);
	}
	
	public GCCartItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViewAndAction(context);
	}	
	
	private  void initViewAndAction(final Context context){	
		mContex = context;
		inflate(context,R.layout.cart_item_gc, this);	
		quantityMinusButton = (TextView) findViewById(R.id.quantityMinusButton);		
		cartNum = (TextView) findViewById(R.id.cartNum);
		quantityPlusButton = (TextView) findViewById(R.id.quantityPlusButton);
		
		quantityMinusButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {	
				GreetingCard card = GreetingCardUtil.getCurrentGreetingCard();
				int quantityIncrement = RssTabletApp.getInstance().getQuantityIncrement(card.proDescId);
				int quantity = Integer.parseInt(cartNum.getText().toString());
				if (quantity <= quantityIncrement){					
					quantity = quantityIncrement;										
				}else{
					quantity -= quantityIncrement;					
				}	
				if(quantity == 1){
					quantityMinusButton.setTextColor(getResources().getColor(R.color.blue_normal));
				} else {
					quantityMinusButton.setTextColor(getResources().getColor(R.color.blue_highlight));
				}
				cartNum.setText("" + quantity);							
				GreetingCardUtil.dealWithItem(context,card,quantity);
				
			}});
		
		quantityPlusButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {		
				GreetingCard card = GreetingCardUtil.getCurrentGreetingCard();
				int quantityIncrement = RssTabletApp.getInstance().getQuantityIncrement(card.proDescId);
				int quantity = Integer.parseInt(cartNum.getText().toString());			
				quantity += quantityIncrement;				
				cartNum.setText("" + quantity);			
				if(quantity>1){
					quantityMinusButton.setTextColor(getResources().getColor(R.color.blue_highlight));
				}
				GreetingCardUtil.dealWithItem(context,card,quantity);				
			}});
	}
	
	public void setControlValue(ProductInfo pInfo,int quantityIncrement){	
		GreetingCard card = GreetingCardUtil.getCurrentGreetingCard();		
		if (pInfo == null) {
			cartNum.setText(quantityIncrement+"");
			GreetingCardUtil.dealWithItem(mContex,card,quantityIncrement);			
		}else {
			cartNum.setText(String.valueOf(pInfo.num));	
			GreetingCardUtil.dealWithItem(mContex,card,pInfo.num);
		}		
		int quantity = Integer.parseInt(cartNum.getText().toString());
		if(quantity == 1){
			quantityMinusButton.setTextColor(getResources().getColor(R.color.blue_normal));
		} else {
			quantityMinusButton.setTextColor(getResources().getColor(R.color.blue_highlight));
		}
	}		
	
}
