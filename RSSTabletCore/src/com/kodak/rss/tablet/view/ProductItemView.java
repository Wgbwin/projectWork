package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.ProductAdapter;

/**
 * Purpose: 
 * Author: Bing Wang
 */
public class ProductItemView extends LinearLayout{  
	
	int position;	
	private Context context;
	private TextView itemName;	
	private ImageView itemIcon;
		
	private View layoutView;
	
	private ProductAdapter adapter;
		
	public ProductItemView(Context context,ProductAdapter adapter) {
		super(context);
		this.context = context;	
		this.adapter = adapter;		
		inflate(context,R.layout.hl_item, this);	
		initView();				
	}	

	private void initView(){	
		layoutView =  findViewById(R.id.item);		
		itemIcon = (ImageView) findViewById(R.id.item_icon);
		itemName = (TextView) findViewById(R.id.item_name);
		if (adapter == null) return;
		if (adapter.collection == null) return;
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		
		double itemNumInPage = adapter.collection.size();
		if (adapter.collection.size() > 5) {
			itemNumInPage = 5.2;
		}
		int mItemWidth = (int) (dm.widthPixels*1.0/itemNumInPage);				
		
		ViewGroup.LayoutParams lParams = (LayoutParams) layoutView.getLayoutParams(); 
		lParams.width = mItemWidth;
		layoutView.setLayoutParams(lParams);
		
	}
	
	public void initViewDispaly(int position){	
		this.position = position;
		if (adapter == null) return;
		itemName.setText(adapter.collection.valueAt(position)[0]);
		itemIcon.setImageResource(adapter.collection.valueAt(position)[1]);		
	}

}
