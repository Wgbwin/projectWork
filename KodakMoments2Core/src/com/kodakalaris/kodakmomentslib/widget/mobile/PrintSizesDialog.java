package com.kodakalaris.kodakmomentslib.widget.mobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.CountryInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.interfaces.IKM2Manager;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;

public class PrintSizesDialog extends BaseGeneralAlertDialogFragment {
	
	private PrintAdapter mAdapter;
	private PhotoInfo mImage;
	private IKM2Manager mManager;
	private PrintItemsChangedListener mListener;
	private CountryInfo mCountryInfo;
	
	public PrintSizesDialog(Context context, List<PrintItem> printItems, PrintItemsChangedListener listener, boolean cancelable){
		super(context, cancelable);
		// as the PhotoInfo in the items are all the same, so we can use the PhotoInfo which in the first item.
		this.mImage = printItems.get(0).getImage();
		KM2Application app = KM2Application.getInstance();
		if(app.getFlowType().isPrintWorkFlow()){
			mManager = PrintManager.getInstance(context);
		}else if (app.getFlowType().isPrintHubWorkFlow()){
			mManager = PrintHubManager.getInstance();
		}	
		mAdapter = new PrintAdapter(context, printItems);
		mListener = listener;
		mCountryInfo = app.getCountryInfo();
	}
	
	public PrintSizesDialog initDialog(Context context){
		setTitle(context.getString(R.string.KMReviewPrints_SelectSize));
		setNegativeButton(context.getString(R.string.Common_Cancel), new OnClickListener() {
			
			@Override
			public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
				dismiss();
			}
		});
		setPositiveButton(context.getString(R.string.save), new OnClickListener() {
			
			@Override
			public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
				Map<RssEntry, Boolean> selectedItems = mAdapter.getItemSelectedStatus();
				Set<RssEntry> entrySet = selectedItems.keySet();
				Iterator<RssEntry> iter = entrySet.iterator();
				while(iter.hasNext()) {
					RssEntry entry = iter.next();
					boolean selected = selectedItems.get(entry);
					if(selected){
						if(KM2Application.getInstance().getFlowType().isPrintWorkFlow()){
							((PrintManager) mManager).createPrintItem(mImage, entry);
						}else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()){							
							((PrintHubManager) mManager).createPrintItem(mImage, entry);
						}	
						
					} else {
						if(KM2Application.getInstance().getFlowType().isPrintWorkFlow()){
							((PrintManager) mManager).deletePrintItem(mImage, entry);
						}else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()){							
							((PrintHubManager) mManager).deletePrintItem(mImage, entry);
						}	
					}
				}
				mListener.onPrintItemsChanged();
				dismiss();
			}
		});
		setContentAreaSize(0.78f, 0.69f);
		return this;
	}

	@Override
	protected View initMessageContent() {
		ListView lvPrints = new ListView(getActivity());
		lvPrints.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		lvPrints.setAdapter(mAdapter);
		lvPrints.setDivider(null);
		lvPrints.setSelector(R.color.transparent);
		mAdapter.notifyDataSetChanged();
		return lvPrints;
	}
	
	private class PrintAdapter extends BaseAdapter {
		
		private LayoutInflater layoutInflater;
		private List<RssEntry> entries;
		private Map<RssEntry, Boolean> itemSelectedStatus;
		
		private List<RssEntry> mEnableProducts;
		private List<RssEntry> mSelectedProducts;
		
		public PrintAdapter(Context context, List<PrintItem> items){
			layoutInflater = LayoutInflater.from(context);
			if(KM2Application.getInstance().getFlowType().isPrintWorkFlow()){
				entries = ((PrintManager) mManager).getPrintProducts();
				
				mSelectedProducts = new ArrayList<RssEntry>();
				if(items != null){
					for(PrintItem item : items){
						if(!mSelectedProducts.contains(item.getEntry())){
							mSelectedProducts.add(item.getEntry());
						}
					}
				}
				mEnableProducts = ((PrintManager) mManager).getEnableProducts(mSelectedProducts);
			}else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()){
				entries = ((PrintHubManager) mManager).getPrintProducts();
			}	
			
			itemSelectedStatus = new HashMap<RssEntry, Boolean>();
			for(RssEntry entry : entries){
				boolean selected = false;
				for(PrintItem item : items){
					if(item.getEntry().equals(entry)){
						selected = true;
						break;
					}
				}
				itemSelectedStatus.put(entry, selected);
			}
		}

		@Override
		public int getCount() {
			return entries == null ? 0 : entries.size();
		}

		@Override
		public Object getItem(int position) {
			return entries == null ? null : entries.get(position);
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
				convertView = layoutInflater.inflate(R.layout.item_print_size_dialog, null);
				holder.cbSelected = (CheckBox) convertView.findViewById(R.id.cb_selected);
				holder.tvPrintSize = (TextView) convertView.findViewById(R.id.tv_print_size);
				holder.tvPrintPrice = (TextView) convertView.findViewById(R.id.tv_print_price);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			
			final RssEntry entry = entries.get(position);
			holder.tvPrintSize.setText(entry.proDescription.shortName);
			holder.tvPrintSize.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onPrintSizeChecked(entry);
				}
			});
			if (entry.maxUnitPrice !=null){
				holder.tvPrintPrice.setText(entry.maxUnitPrice.priceStr);
			}	
			
			// disable the products that the current selected retailers do not support
			if(mEnableProducts != null && mEnableProducts.size()>0){
				holder.cbSelected.setEnabled(mEnableProducts.contains(entry));
			}
			// update the color of the products which are not supported by the selected retailers
			int textColorId = 0;
			if(holder.cbSelected.isEnabled()){
				textColorId = R.color.near_black;
			} else {
				textColorId = R.color.near_black_50alpha;
			}
			holder.tvPrintSize.setTextColor(getResources().getColor(textColorId));
			holder.tvPrintPrice.setTextColor(getResources().getColor(textColorId));
			if(mCountryInfo != null && !mCountryInfo.showsMSRPPricing()){
				holder.tvPrintPrice.setVisibility(View.INVISIBLE);
			}
			holder.cbSelected.setChecked(itemSelectedStatus.get(entry));
			holder.cbSelected.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onPrintSizeChecked(entry);
				}
			});
			
			return convertView;
		}
		
		private void onPrintSizeChecked(RssEntry entry){
			if(mEnableProducts != null){
				if(!mEnableProducts.contains(entry)){
					return;
				}
			}
			
			itemSelectedStatus.put(entry, !itemSelectedStatus.get(entry));
			if(KM2Application.getInstance().getFlowType().isPrintWorkFlow()){
				if(itemSelectedStatus.get(entry)){
					mSelectedProducts.remove(entry);
				} else {
					mSelectedProducts.add(entry);
				}
				mEnableProducts = ((PrintManager) mManager).getEnableProducts(mSelectedProducts);
			}
			notifyDataSetChanged();
		}
		
		public Map<RssEntry, Boolean> getItemSelectedStatus() {
			return itemSelectedStatus;
		}

		private class Holder {
			CheckBox cbSelected;
			TextView tvPrintSize;
			TextView tvPrintPrice;
		}
		
	}
	
	public interface PrintItemsChangedListener {
		public void onPrintItemsChanged();
	}

}
