package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.kodakalaris.kodakmomentslib.adapter.LinearListLayoutBaseAdapter;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Pricing.LineItem;
import com.kodakalaris.kodakmomentslib.widget.mobile.ShoppingCartItemView;

public class ShoppingCartItemAdapter extends LinearListLayoutBaseAdapter {
	private Context mContext;
	private List<LineItem> mList;
	private OnEditBtnsClickListener mOnEditBtnsClickListener;
	
	public ShoppingCartItemAdapter(Context context, List<LineItem> list) {
		mContext = context;
		mList = list;
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
	public LineItem getItem(int position) {
		return mList.get(position);
	}

	@Override
	public View getView(final int position) {
		final ShoppingCartItemView v = new ShoppingCartItemView(mContext);
		final LineItem lineItem = getItem(position);
		v.setInfo(lineItem);
		
		v.setBottomDividerVisibility(position == getCount() - 1 ? View.VISIBLE : View.INVISIBLE);
		
		v.setOnDeleteBtnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mOnEditBtnsClickListener != null) {
					mOnEditBtnsClickListener.onDeleteBtnClick(v, position, lineItem);
				}
			}
		});
		
		v.setOnEditBtnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mOnEditBtnsClickListener != null) {
					mOnEditBtnsClickListener.onEditBtnClick(v, position, lineItem);
				}
			}
		});
		
		return v;
	}
	
	public void setOnEditBtnsClickListener(OnEditBtnsClickListener listener) {
		mOnEditBtnsClickListener = listener;
	}
	
	public interface OnEditBtnsClickListener {
		void onEditBtnClick(View v, int Position, LineItem lineItem);
		void onDeleteBtnClick(View v, int Position, LineItem lineItem);
	}

}
