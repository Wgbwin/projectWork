package com.kodak.rss.tablet.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kodak.rss.tablet.bean.PrintEditInfo;
import com.kodak.rss.tablet.view.collage.CollageEditItemView;

public class CollageEditListAdapter  extends BaseAdapter {
	
	private Context context;	
	public List<PrintEditInfo> editList;		
	OnSelectListener onSelectListener;
	
	public CollageEditListAdapter(Context context,List<PrintEditInfo> editList,OnSelectListener onSelectListener) {
		this.context = context;	
		this.editList = editList;
		this.onSelectListener = onSelectListener;	
	}

	@Override
	public int getCount() {		
		return editList.size();
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
		CollageEditItemView itemView;
		if (convertView == null) {						
			itemView = new CollageEditItemView(context, CollageEditListAdapter.this,onSelectListener);
			convertView = itemView;
			convertView.setTag(itemView);
		}else {
			itemView = (CollageEditItemView) convertView.getTag();
		}		
		itemView.initViewAndAction(position);									
		return convertView;
	}

	public interface OnSelectListener {
		public void onSelect(PrintEditInfo editInfo);		
	}
	
}
