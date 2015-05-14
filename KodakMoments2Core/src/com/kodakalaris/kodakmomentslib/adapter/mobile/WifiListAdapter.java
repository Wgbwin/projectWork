package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.util.List;

import com.kodakalaris.kodakmomentslib.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class WifiListAdapter extends BaseAdapter {
	private Context mContext;
	private List<String> mList;
	
	public WifiListAdapter(Context context, List<String> list) {
		mContext = context;
		mList = list;
	}
	
	public void setList(List<String> list) {
		mList = list;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public String getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View cv, ViewGroup parent) {
		if (cv == null) {
			cv = LayoutInflater.from(mContext).inflate(R.layout.item_wifi_ssid, null);
		}
		
		((TextView) cv).setText(mList.get(position));
		
		return cv;
	}
	
}
