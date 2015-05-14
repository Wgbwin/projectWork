package com.kodak.rss.tablet.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.view.ProductItemView;

public class ProductAdapter extends BaseAdapter{

	public volatile SortableHashMap<String, int[]> collection = new SortableHashMap<String, int[]>();
	private Context context;	
		
	public ProductAdapter(Context context) {					
		this.context = context;				
		init();				
	}
	
	private void init(){	
		collection.clear();		
		collection.put(AppConstants.projectType, new int[]{R.string.l_project,R.drawable.projects});
		collection.put(AppConstants.printType, new int[]{R.string.l_print,R.drawable.prints});
		collection.put(AppConstants.bookType, new int[]{R.string.l_book,R.drawable.photobook});		
		collection.put(AppConstants.cardType, new int[]{R.string.l_card,R.drawable.cards});		
		collection.put(AppConstants.kioskConnectType, new int[]{R.string.l_kiosk_connect,R.drawable.button_kioskconnect});
	}	

	@Override
	public int getCount() {
		return collection.size();
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
		ProductItemView itemView;		
		if (convertView == null) {						
			itemView = new ProductItemView(context, ProductAdapter.this);	
			convertView = itemView;
			convertView.setTag(itemView);
		}else {
			itemView = (ProductItemView) convertView.getTag();
		}		
		itemView.initViewDispaly(position);	
		return convertView;
      } 
			
 }

