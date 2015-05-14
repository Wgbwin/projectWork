package com.kodak.rss.tablet.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.kodak.rss.core.n2r.bean.content.SearchStarterCategory;
import com.kodak.rss.tablet.R;

public class GCSubButtonAdapter extends BaseAdapter {
	
	public int size;
	private Context mContext;		
	private List<SearchStarterCategory> subCategorys;
	private int itemWidth;
	private int selectedItem = 0;
	
	public GCSubButtonAdapter(Context context,List<SearchStarterCategory> subCategorys) {
		this.mContext = context;
		this.subCategorys = subCategorys;
		this.size = subCategorys.size();	
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();		
		itemWidth = dm.widthPixels/5;							
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

	public int getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(int selectedItem) {
		this.selectedItem = selectedItem;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		Button button;
		if (convertView == null) {
			button = new Button(mContext);
			convertView = button;
			convertView.setTag(button);
		}else {
			button = (Button) convertView.getTag();
		}	
		button.setWidth(itemWidth);
		if(selectedItem == position){
			button.setBackgroundResource(R.drawable.tab_sel);
			button.setTextColor(Color.WHITE);
		} else {
			button.setBackgroundResource(R.drawable.tab_up);
			button.setTextColor(Color.LTGRAY);
		}
		SearchStarterCategory ssc = subCategorys.get(position);	
		button.setText(ssc.name);				
		return convertView;
	}

	
}
