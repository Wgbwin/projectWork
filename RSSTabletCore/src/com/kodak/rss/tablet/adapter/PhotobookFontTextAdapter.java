package com.kodak.rss.tablet.adapter;

import com.kodak.rss.tablet.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PhotobookFontTextAdapter extends BaseAdapter {

	private String[] source;
	private LayoutInflater mInflater;
	
	public PhotobookFontTextAdapter(Context context, String[] source){
		this.source = source;
		this.mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return source==null ? 0 : source.length;
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
		Holder holder = null;
		if(convertView == null){
			holder = new Holder();
			convertView = mInflater.inflate(R.layout.font_textview_item, null);
			holder.textView = (TextView) convertView.findViewById(R.id.tv_text);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.textView.setText(source[position]);
		return convertView;
	}
	
	class Holder {
		TextView textView;
	}

}
