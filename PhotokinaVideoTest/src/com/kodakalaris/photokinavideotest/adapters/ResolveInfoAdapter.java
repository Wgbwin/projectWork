package com.kodakalaris.photokinavideotest.adapters;

import java.util.List;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodakalaris.photokinavideotest.R;

public class ResolveInfoAdapter extends BaseAdapter {
	/**
	 * 
	 */
	private final Context mContext;
	private final List<ResolveInfo> mResolveInfo;
	public ResolveInfoAdapter(Context context, List<ResolveInfo> resolveInfo) {
		this.mContext = context;
		this.mResolveInfo = resolveInfo;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.activity_preview_share_dialog_element, parent, false);
		}

		ImageView image = (ImageView) convertView.findViewById(R.id.preview_dialog_element_image);
		TextView text = (TextView) convertView.findViewById(R.id.preview_dialog_element_text);
		ResolveInfo info = mResolveInfo.get(position);
		if (info != null) {
			image.setImageDrawable(info.activityInfo.loadIcon(mContext.getPackageManager()));
			text.setText(info.activityInfo.loadLabel(mContext.getPackageManager()));
		} else {
			image.setImageResource(R.drawable.ic_launcher);
			text.setText(R.string.activity_preview_share_option_others);
		}

		return convertView;
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public Object getItem(int position) {
		return position;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mResolveInfo.size();
	}
}