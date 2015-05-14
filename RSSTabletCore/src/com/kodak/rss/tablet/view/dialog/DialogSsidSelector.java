package com.kodak.rss.tablet.view.dialog;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kodak.rss.tablet.R;

public class DialogSsidSelector extends Dialog{
	
	public DialogSsidSelector(Context context,SsidAdapter adapter,OnItemClickListener onItemClickListener) {
		super(context, R.style.SsidSelectorDialog);
		
		View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_ssid_selector, null);
		ListView lv = (ListView) contentView.findViewById(R.id.lv_ssid);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(onItemClickListener);
		setContentView(contentView);
	}
	
	public static class SsidAdapter extends BaseAdapter{
		private List<String> ssidList;
		private Context context;
		
		public SsidAdapter(Context context,List<String> ssidList) {
			super();
			this.ssidList = ssidList;
			this.context = context;
		}

		@Override
		public int getCount() {
			return ssidList.size();
		}

		@Override
		public String getItem(int position) {
			return ssidList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View cv, ViewGroup parent) {
			ViewHolder holder;
			if(cv==null){
				cv = LayoutInflater.from(context).inflate(R.layout.ssid_item, null);
				holder = new ViewHolder();
				holder.tv = (TextView) cv.findViewById(R.id.tv_ssid);
				cv.setTag(holder);
			}else{
				holder = (ViewHolder) cv.getTag();
			}
			
			holder.tv.setText(getItem(position));
			return cv;
		}

		public void setList(List<String> list) {
			this.ssidList = list;
		}
		
	}
	
	private static class ViewHolder{
		TextView tv;
	}
}
