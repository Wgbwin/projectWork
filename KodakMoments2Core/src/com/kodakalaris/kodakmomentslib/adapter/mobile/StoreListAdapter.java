package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo;

public class StoreListAdapter extends BaseAdapter {
	LayoutInflater mLayoutInflater;
	private ArrayList<StoreInfo> stores;
	private int selectedPosition = -1;
	private String timeFormat = "";
	private Context mContext;
	private boolean isUseMiles = true;

	public StoreListAdapter(Context context) {
		this.mContext = context;
	}

	public StoreListAdapter(Context context, List<StoreInfo> stores) {
		this.mContext = context;
		this.mLayoutInflater = LayoutInflater.from(mContext);
		this.stores = (ArrayList<StoreInfo>) stores;
		ContentResolver cr = context.getContentResolver();
		timeFormat = android.provider.Settings.System.getString(cr, android.provider.Settings.System.TIME_12_24);
		String country = KM2Application.getInstance().getCountryCodeUsed();
		if (country == null) {
			country = "";
		}
		if ("US".equalsIgnoreCase(country) || "GB".equalsIgnoreCase(country) || "".equals(country)) {
			isUseMiles = true;

		} else {
			isUseMiles = false;
		}

	}

	public void updateStores(List<StoreInfo> stores) {
		this.stores = (ArrayList<StoreInfo>) stores;
		notifyDataSetChanged();
	}

	public void refreshSelectedPosition(int position) {
		this.selectedPosition = position;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return stores.size();
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
		StoreHolder holder = null;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.store_item, null);
			holder = new StoreHolder();
			holder.container = (RelativeLayout) convertView.findViewById(R.id.store_item_container);
			holder.tvName = (TextView) convertView.findViewById(R.id.tv_store_name);
			holder.tvAddress = (TextView) convertView.findViewById(R.id.tv_store_address);
			holder.tvCity = (TextView) convertView.findViewById(R.id.tv_store_city);
			holder.tvTel = (TextView) convertView.findViewById(R.id.tv_store_tel);
			holder.tvMiles = (TextView) convertView.findViewById(R.id.tv_store_miles);
			holder.tvHours = (TextView) convertView.findViewById(R.id.tv_store_hours);

			convertView.setTag(holder);
		} else {
			holder = (StoreHolder) convertView.getTag();
		}
		if (stores == null || stores.size() == 0) {
			return null;
		}
		StoreInfo store = stores.get(position);
		String name = store.name;
		String address = store.address.address1;
		String city = store.address.city + ", " + store.address.stateProvince;
		String tel = store.phone;
		String distance = "";
		String unit = "";
		if (!isUseMiles) {// KM
			distance = store.convertKiloMilesToString();
			unit = mContext.getString(R.string.FindStore_KM);
		} else {// Mile
			distance = store.convertMilesToString();
			unit = mContext.getString(R.string.FindStore_Miles);
		}
		String hours = store.convertHoursToString(timeFormat);
		holder.tvName.setText(name);
		if (store.isATestStore) {
			holder.tvName.setTextColor(mContext.getResources().getColor(R.color.kodak_red));
		} else {
			holder.tvName.setTextColor(mContext.getResources().getColor(R.color.near_black));
		}
		holder.tvAddress.setText(address);
		holder.tvCity.setText(city);
		holder.tvTel.setText(tel);
		holder.tvMiles.setText(distance + " " + unit);
		holder.tvHours.setText(hours);

		return convertView;
	}

}

class StoreHolder {
	RelativeLayout container;
	TextView tvName;
	TextView tvAddress;
	TextView tvCity;
	TextView tvTel;
	TextView tvMiles;
	TextView tvHours;
}
