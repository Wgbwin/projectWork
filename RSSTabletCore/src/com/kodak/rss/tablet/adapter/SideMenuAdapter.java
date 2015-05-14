package com.kodak.rss.tablet.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.bean.SideMenuItem;

public class SideMenuAdapter extends BaseAdapter{
	private Context context;
	private List<SideMenuItem> list;

	public SideMenuAdapter(Context context, List<SideMenuItem> list){
		this.context = context;
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public SideMenuItem getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return list.get(position).getId();
	}

	@Override
	public View getView(int position, View cv, ViewGroup parent) {
		ViewHolder holder;
		if(cv == null){
			cv = LayoutInflater.from(context).inflate(R.layout.side_menu_item, null);
			holder = new ViewHolder();
			holder.tvText = (TextView) cv.findViewById(R.id.tv_text);
			holder.ivIcon = (ImageView) cv.findViewById(R.id.iv_icon);
			cv.setTag(holder);
		}else{
			holder = (ViewHolder) cv.getTag();
		}
		
		SideMenuItem item = getItem(position);
		holder.tvText.setText(item.getText());
		holder.ivIcon.setImageResource(item.getImageResId());
		return cv;
	}
	
	public class ViewHolder{
		public TextView tvText;
		public ImageView ivIcon;
	}
	
	public List<SideMenuItem> getList(){
		return list;
	}
	
	public SideMenuItem getItemById(int id){
		for(SideMenuItem item : list){
			if(item.getId() == id){
				return item;
			}
		}
		return null;
	}
	
	public void removeItem(int itemId){
		for(int i=0;i<list.size();i++){
			if(list.get(i).getId() == itemId){
				list.remove(i);
				return;
			}
		}
	}
	

}
