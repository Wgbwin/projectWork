package com.kodak.kodak_kioskconnect_n2r;

import java.util.List;

import com.kodak.kodak_kioskconnect_n2r.ShareUtils.ShareItem;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ShareListView extends ListView {
	private String TAG = ShareListView.class.getSimpleName();
	
	private Context context;
	private LayoutInflater inflater;
	private List<ShareItem> shareItems;
	private ShareAdapter adapter;
	private PopupWindow popupWindow;

	public ShareListView(Context context) {
		super(context);
		init(context);
	}
	
	public ShareListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public ShareListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ShareUtils shareUtils = new ShareUtils(context);
		shareItems = shareUtils.getShareItems();
		adapter = new ShareAdapter();
		setAdapter(adapter);
		adapter.notifyDataSetChanged();
		setSelection(shareItems.size()-1);
	}
	
	public void setPopupWindow(PopupWindow pw){
		popupWindow = pw;
	}

	private class ShareAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			return shareItems.size();
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
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.shareitem, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.shareIcon);
			TextView name = (TextView) convertView.findViewById(R.id.shareName);
			
			ShareItem item = shareItems.get(position);
			icon.setImageDrawable(item.getIcon());
			name.setText(item.getName());
			
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ShareItem shareItem = shareItems.get(position);
					String packName = shareItem.getPackName();
					String actName = shareItem.getActName();
					Intent intent = new Intent(Intent.ACTION_SEND);					
					if(packName.contains("wmc")){
						if(PrintHelper.getAccessTokenResponse(getContext()).access_token.equals("")){
							intent = new Intent(context, ShareLoginActivity.class);
						} else {
							intent = new Intent(context, ShareVerifyActivity.class);
						}
					} else {
						ComponentName comp = new ComponentName(packName, actName);
						intent.setComponent(comp);
						Uri uri = Uri.parse(PrintHelper.selectedImageUrls.get(PrintHelper.selectedImageUrls.size()-1));
						intent.setType(context.getContentResolver().getType(uri));
						intent.putExtra(Intent.EXTRA_STREAM, uri);
					}
					context.startActivity(intent);
					if(popupWindow.isShowing()){
						popupWindow.dismiss();
					}
					
				}
			});
			
			return convertView;
		}
		
	}
}
