package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.adapter.LinearListLayoutBaseAdapter;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.CountryInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;

public class PrintSizeAndPriceAdapter extends LinearListLayoutBaseAdapter{
	private Context mContext;
	private List<RssEntry> mEntries;
	private CountryInfo mCountryInfo;
	private PrintSizeClickedListener mListener;
	
	public PrintSizeAndPriceAdapter(Context context, CountryInfo countryInfo, PrintSizeClickedListener listener) {
		mContext = context;
		mEntries = PrintManager.getInstance(context).getPrintProducts();
		this.mCountryInfo = countryInfo;
		mListener = listener;
	}
	
	@Override
	public int getCount() {
		return mEntries == null ? 0 : mEntries.size();
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
	public View getView(int position) {
		View v = LayoutInflater.from(mContext).inflate(R.layout.item_print_size_selection, null);
		TextView tvName = (TextView) v.findViewById(R.id.txt_item_prints_size);
		TextView tvPrice = (TextView) v.findViewById(R.id.txt_item_print_money);
		
		final RssEntry entry = mEntries.get(position); 
		String name = entry.proDescription.shortName;
		String price = entry.maxUnitPrice.priceStr;
		tvName.setText(name);
		tvPrice.setText(price);
		if(mCountryInfo != null && !mCountryInfo.showsMSRPPricing()){
			tvPrice.setVisibility(View.INVISIBLE);
			v.findViewById(R.id.v_item__rule).setVisibility(View.INVISIBLE);
		}
		
		tvName.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onPrintSizeClicked(entry);
			}
		});
		
		return v;
	}
	
	public interface PrintSizeClickedListener {
		public void onPrintSizeClicked(RssEntry entry);
	}

}
