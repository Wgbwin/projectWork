package com.kodak.kodak_kioskconnect_n2r.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.kodak.kodak_kioskconnect_n2r.R;

public class CollageFontColorAdapter extends BaseAdapter {
	
	private String[] source;
	private LayoutInflater mInflater;
	private int width;
	
	public CollageFontColorAdapter(Context context, String[] source, int width){
		mInflater = LayoutInflater.from(context);
		this.source = source;
		this.width = width;
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
			convertView = mInflater.inflate(R.layout.font_imageview_item, null);
			holder.imageView = (ImageView) convertView.findViewById(R.id.iv_icon);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.imageView.setBackgroundColor(Color.parseColor(source[position]));
		LinearLayout.LayoutParams params = (LayoutParams) holder.imageView.getLayoutParams();
		params.width = params.height = LinearLayout.LayoutParams.MATCH_PARENT;
		holder.imageView.setLayoutParams(params);
		convertView.setLayoutParams(new GridView.LayoutParams(width, width));
		return convertView;
	}
	
	class Holder {
		ImageView imageView;
	}

}
