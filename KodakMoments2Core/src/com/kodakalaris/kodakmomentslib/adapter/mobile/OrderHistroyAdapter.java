package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.util.List;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.orderconfirmation.MOrderConfirmationActivity;
import com.kodakalaris.kodakmomentslib.bean.OrderDetail;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class OrderHistroyAdapter extends BaseAdapter {
	
	private List<OrderDetail> mOrders;
	private LayoutInflater mLayoutInflater;
	private Context mContext;
	public OrderHistroyAdapter(Context context, List<OrderDetail> orders) {
		this.mLayoutInflater = LayoutInflater.from(context);
		this.mOrders = orders;
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return mOrders==null ? 0 : mOrders.size();
	}

	@Override
	public Object getItem(int position) {
		return mOrders.get(position);
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
			convertView = mLayoutInflater.inflate(R.layout.item_m_orderhistory, null);
			holder.tvOrderDate = (TextView) convertView.findViewById(R.id.txt_order_date);
			holder.tvOrderId = (TextView) convertView.findViewById(R.id.txt_order_id);
			holder.ibtnPreview = (ImageButton) convertView.findViewById(R.id.ibtn_order_preview);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		final OrderDetail order = mOrders.get(position);
		holder.tvOrderDate.setText(order.getOrderDate());
		holder.tvOrderId.setText(order.getOrderId());
		holder.ibtnPreview.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, MOrderConfirmationActivity.class);
				intent.putExtra(AppConstants.KEY_ORDER, order.getOrderId());
				intent.putExtra(MOrderConfirmationActivity.KEY_PREVIEW, true);
				mContext.startActivity(intent);
			}
		});
		
		return convertView;
	}
	
	class Holder {
		TextView tvOrderDate;
		TextView tvOrderId;
		ImageButton ibtnPreview;
	}

}
