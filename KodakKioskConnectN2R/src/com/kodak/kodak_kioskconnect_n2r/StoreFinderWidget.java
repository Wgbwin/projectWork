package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.MapView;

public class StoreFinderWidget extends LinearLayout
{
	MapView mapView;
	TextView storeName;
	TextView storeNumber;
	TextView storeAddress;
	TextView storeHours;
	Button findStore;
	Button findOutMore;
	Context mContext;
	public StoreFinderWidget(Context context)
	{
		super(context);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.setupstorewidget, this);

	}
}
