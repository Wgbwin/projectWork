package com.kodak.rss.tablet.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.activities.PrintsActivity;
import com.kodak.rss.tablet.bean.StorePriceInfo;
import com.kodak.rss.tablet.view.CartItemView;
import com.kodak.rss.tablet.view.ImageEditView;

/**
 * Purpose: prints cart List
 * Author: Bing Wang
 * Created Time: Aug 20, 2013 9:20:43 AM 
 */
public class CartListAdapter  extends BaseAdapter{
	private String TAG = "CartListAdapter:";
	Context context;	
	public List <ProductInfo>  productBuckets ;
	public ImageEditView editImageView;
	private int size;
	public List<StorePriceInfo> StorePriceInfoList;
	
	public CartListAdapter(Context context,List <ProductInfo> productInfoList ,ImageEditView imageEditView) {
		this.context = context;
		if (context instanceof PrintsActivity) {
			this.StorePriceInfoList =((PrintsActivity)context).StorePriceInfoList;
		}		
		this.productBuckets = productInfoList;	
		this.editImageView = imageEditView;
		this.size = productBuckets.size();
	}

	@Override
	public int getCount() {		
		return size;
	}

	@Override
	public Object getItem(int position) {		
		return position;
	}

	@Override
	public long getItemId(int position) {		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		CartItemView civ = null;
		if (convertView == null){
			civ = new CartItemView(context,CartListAdapter.this);
			convertView = civ;
			convertView.setTag(civ);
		}else{
			civ = (CartItemView) convertView.getTag();
		}	
		
		ProductInfo pInfo = productBuckets.get(position);
		civ.setPos(position);
		civ.setControlValue(pInfo);
		
		if (pInfo.isCurrentChecked) {
			Log.d(TAG, "getView isCurrentChecked"+position);
			editImageView.productInfo = pInfo;
			editImageView.refresh();
		}		
		
		return convertView;
	}
	
}


