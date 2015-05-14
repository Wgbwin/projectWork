package com.kodak.rss.tablet.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;
import com.kodak.rss.core.util.ImageResources;
import com.kodak.rss.tablet.view.ColorEffectItemView;

public class PopEditAdapter extends BaseAdapter {
	public List<ColorEffect> editItems;
	Context mContext;	
	public ImageResources mRes;
	
	public PopEditAdapter(Context context,List<ColorEffect> editItems,ImageResources mRes) {
		this.mContext = context;
		this.editItems = editItems;	
		this.mRes = mRes;
	}

	@Override
	public int getCount() {
		return editItems.size();		
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
	public View getView(final int position, View convertView, ViewGroup parent) {		
		ColorEffectItemView ceItemView;
		if (convertView == null) {
			ceItemView = new ColorEffectItemView(mContext, PopEditAdapter.this,position);
			convertView = ceItemView;
			convertView.setTag(ceItemView);			
		}else {
			ceItemView = (ColorEffectItemView) convertView.getTag();
		}
						
		ColorEffect ce = editItems.get(position);
		ceItemView.setControlValue(ce);	 

		return convertView;
	}
}
