package com.kodak.rss.tablet.view;

import java.net.URI;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.content.SearchStarter;
import com.kodak.rss.core.n2r.bean.greetingcard.GCCategory;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.GCBaseAdapter;

/**
 * Purpose: 
 * Author: Bing Wang
 */
public class GCItemView extends LinearLayout{  
		
	private Context context;
	private TextView itemName;	
	private SelectImageView itemIcon;		
	private View layoutView;	

	public GCItemView(Context context,int itemHeight,int totle ,double pageNum) {
		super(context);			
		this.context = context;				
		initView();	
		double itemNumInPage = totle;
		if (totle > (int)pageNum) {
			itemNumInPage = pageNum;
		}
		DisplayMetrics dm = context.getResources().getDisplayMetrics();		
		int mItemWidth = (int) (dm.widthPixels*1.0/itemNumInPage);			
		setParams(mItemWidth, itemHeight);
	}  	
	
	public GCItemView(Context context,int itemWidth,int itemHeight) {
		super(context);			
		this.context = context;				
		initView();	
		setParams(itemWidth, itemHeight);
	}  		
	
	public GCItemView(Context context, AttributeSet attrs) {
		super(context, attrs);	
		this.context = context;	
		initView();		
	}  
	
	public void setParams(int mItemWidth ,int itemHeight){				
		ViewGroup.LayoutParams lParams = (LayoutParams) layoutView.getLayoutParams(); 
		lParams.width = mItemWidth;
		lParams.height = itemHeight;
		layoutView.setLayoutParams(lParams);
	}
	
	private void initView(){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.gc_item, this);
		layoutView = findViewById(R.id.item);	
		itemIcon = (SelectImageView) findViewById(R.id.item_icon);
		itemName = (TextView) findViewById(R.id.item_name);		
	}

	public void setValue(GCBaseAdapter adapter,SearchStarter ss,int pos,boolean isMain){			
		if (ss == null) return;						
		String name = ss.name;		
		String id = adapter.getId(name);
		URI pictureURI = adapter.getUri(ss.glyphUrl);
		Bitmap mBitmap = adapter.getBitmap(id, pictureURI, itemIcon, pos);  				
		itemName.setText(name);	
		if (!isMain) {
			itemName.setTextColor(Color.WHITE);
		}
		itemIcon.setImageBitmap(mBitmap);					
	}	
	
	public void setValue(GCBaseAdapter adapter,GCCategory gcc,int pos){			
		if (gcc == null) return;
		String name = gcc.localizedName;			
		String id = gcc.id;
		URI pictureURI = adapter.getUri(gcc.glyphURL);
		Bitmap mBitmap = adapter.getBitmap(id, pictureURI, itemIcon, pos);    	
		itemName.setText(name);					
		itemIcon.setImageBitmap(mBitmap);					
	}

	/**
	 * For GCDesignCategorySelectionAdapter
	 * @param adapter
	 * @param gcc
	 * @param pos
	 * @param isSelected
	 */
	public void setValue(GCBaseAdapter adapter,GCCategory gcc,int pos, boolean isSelected){			
		if (gcc == null) return;		
		String name = getCardShotName(RssTabletApp.getInstance().getCatalogList(), gcc);
		if(isSelected){
			itemIcon.setIsChice(true,true);
		} else {
			itemIcon.setIsChice(false,true);
		}
		String id = gcc.id;
		URI pictureURI = adapter.getUri(gcc.glyphURL);
		Bitmap mBitmap = adapter.getBitmap(id, pictureURI, itemIcon, pos);    	
		itemName.setText(name);
		itemName.setMaxLines(2);
		itemName.setEllipsize(TruncateAt.END);
		LinearLayout.LayoutParams params = (LayoutParams) itemName.getLayoutParams();
		params.topMargin = -5;
		itemIcon.setImageBitmap(mBitmap);
	}
	
	private String getCardShotName(List<Catalog> catalogs, GCCategory gcc){
		String name = "";
		for(String desId : gcc.productIdentifiers){
			for(Catalog catalog : catalogs){
				RssEntry entry = catalog.getProductEntry(desId);
				if(entry != null){
					name = entry.proDescription.shortName;
					break;
				}
			}
			if(!"".equals(name)){
				break;
			}
		}
		return name;
	}
	 
}
