package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.adapter.LinearListLayoutBaseAdapter;
import com.kodakalaris.kodakmomentslib.bean.items.OrderConfirmationProductItem;

public class OrderConfirmationProductsAdapter extends LinearListLayoutBaseAdapter {
	private Context mContext;
	private List<OrderConfirmationProductItem> mList;;
	
	public OrderConfirmationProductsAdapter(Context context, List<OrderConfirmationProductItem> list) {
		mList = list;
		mContext = context;
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public OrderConfirmationProductItem getItem(int position) {
		return mList.get(position);
	}

	@Override
	public View getView(int position) {
		View v = LayoutInflater.from(mContext).inflate(R.layout.item_order_confirmation_products, null);
		
		TextView txtName = (TextView) v.findViewById(R.id.txt_name);
		TextView txtAmount = (TextView) v.findViewById(R.id.txt_amount);
		
		OrderConfirmationProductItem item = mList.get(position);
		
		txtName.setText(item.name);
		txtAmount.setText(String.valueOf(item.amount));
		
		return v;
	}

}
