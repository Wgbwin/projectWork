package com.kodak.rss.tablet.adapter;

import java.util.ArrayList;
import java.util.List;

import com.kodak.rss.core.bean.OrderDetail;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.OrderSummaryActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
/**
 * 
 * @author Kane Jin
 *
 */
public class OrderHistoryAdapter extends BaseAdapter {
	private final String TAG = OrderHistoryAdapter.class.getSimpleName();
	
	private Context mContext;
	private String[] orderIds = null;
	private List<String> lOrderIds = null;
	private LayoutInflater mLayoutInflater;
	private RssTabletApp app;
	
	private boolean orderHistoryViewed = false;

	public boolean isOrderHistoryViewed() {
		return orderHistoryViewed;
	}

	public OrderHistoryAdapter(Context context){
		app = RssTabletApp.getInstance();
		mContext = context;
		String orders = SharedPreferrenceUtil.getString(context, AppConstants.OrderHistory);
		mLayoutInflater = LayoutInflater.from(context);
		try{
			orderIds = orders.split(",");
		} catch (Exception e) {
			// TODO: nothing need to do
		}
		if(orderIds != null){
			lOrderIds = new ArrayList<String>();
			// order the orders by date
			for(int i=orderIds.length-1; i>=0; i--){
				if(!"".equals(orderIds[i])){
					lOrderIds.add(orderIds[i]);
				}
			}
		}
	}

	@Override
	public int getCount() {
		if(lOrderIds != null){
			return lOrderIds.size();
		}
		return 0;
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
			convertView = mLayoutInflater.inflate(R.layout.order_histroy_item, null);
			holder = new Holder();
			holder.tvOrderDate = (TextView) convertView.findViewById(R.id.tv_order_date);
			holder.tvOderId = (TextView) convertView.findViewById(R.id.tv_orderId);
			holder.btOrderDetail = (Button) convertView.findViewById(R.id.bt_orderDetail);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		final OrderDetail orderDetail = OrderDetail.load(mContext, lOrderIds.get(position));
		if(orderDetail != null){
			holder.tvOrderDate.setText(orderDetail.getOrderTime());
			holder.tvOderId.setText(orderDetail.getOrderId());
			holder.btOrderDetail.setOnClickListener(new OnClickListener() {
			
				@Override
				public void onClick(View v) {
					orderHistoryViewed = true;
					Intent mIntent = new Intent(mContext, OrderSummaryActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("orderId", orderDetail.getOrderId());
					bundle.putBoolean("justPreview", true);
					mIntent.putExtras(bundle);
					mContext.startActivity(mIntent);
				}
			});
			return convertView;
		} 
		return null;
		
	}	
	
	class Holder {
		TextView tvOrderDate;
		TextView tvOderId;
		Button btOrderDetail;
	}

}
