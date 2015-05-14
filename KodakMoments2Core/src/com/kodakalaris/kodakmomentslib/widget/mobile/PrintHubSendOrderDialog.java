package com.kodakalaris.kodakmomentslib.widget.mobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.sendingorder.MPrintHubSendingOrderActivity;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.interfaces.IKM2Manager;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.PrintSizesDialog.PrintItemsChangedListener;

public class PrintHubSendOrderDialog extends BaseGeneralAlertDialogFragment {

	private PrintAdapter mAdapter;
	private IKM2Manager mManager;
	private List<PhotoInfo> images = new ArrayList<PhotoInfo>();
	private Context mContext;

	public PrintHubSendOrderDialog(Context context, List<PrintItem> printItems, PrintItemsChangedListener listener, boolean cancelable) {
		super(context, cancelable);
		this.mContext = context;
		mManager = PrintHubManager.getInstance();
		mAdapter = new PrintAdapter(context, printItems);
	}

	public PrintHubSendOrderDialog initDialog(Context context) {
		setTitle(mContext.getString(R.string.PrintHubSendOrder_dialog_title));
		setNegativeButton(context.getString(R.string.Common_Cancel), new OnClickListener() {

			@Override
			public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
				dismiss();
			}
		});
		setPositiveButton(context.getString(R.string.Common_OK), new OnClickListener() {

			@Override
			public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
				dismiss();
				Intent i = new Intent();
				i.setClass(mContext, MPrintHubSendingOrderActivity.class);
				startActivity(i);
				((Activity) mContext).finish();
			}
		});

		setContentAreaSize(0.62f, 0.47f);
		return this;
	}

	@Override
	protected View initMessageContent() {
		ListView lvPrints = new ListView(getActivity());
		lvPrints.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		lvPrints.setAdapter(mAdapter);
		lvPrints.setDivider(null);
		lvPrints.setDividerHeight(25);
		lvPrints.setSelector(R.color.transparent);
		mAdapter.notifyDataSetChanged();
		return lvPrints;
	}

	private class PrintAdapter extends BaseAdapter {

		private LayoutInflater layoutInflater;
		private List<RssEntry> entries;
		private List<RssEntry> selectedEntries;
		private Map<RssEntry, Integer> itemSelectedCounts;

		public PrintAdapter(Context context, List<PrintItem> items) {
			layoutInflater = LayoutInflater.from(context);
			selectedEntries = new ArrayList<RssEntry>();
			entries = ((PrintHubManager) mManager).getPrintProducts();

			itemSelectedCounts = new HashMap<RssEntry, Integer>();
			PhotoInfo image = null;
			for (PrintItem item : items) {
				image = item.getImage();
				if (!images.contains(image)) {
					images.add(image);
				}
			}
			int tempCount = 0;
			for (RssEntry entry : entries) {
				int count = 0;
				for (PrintItem item : items) {
					if (item.getEntry().equals(entry)) {
						tempCount = item.getCount();
						count = count + tempCount;
					}
				}
				if (count > 0) {
					selectedEntries.add(entry);
					itemSelectedCounts.put(entry, count);
				}
			}
		}

		@Override
		public int getCount() {
			return selectedEntries.size();
		}

		@Override
		public Object getItem(int position) {
			return selectedEntries == null ? null : selectedEntries.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				holder = new Holder();
				convertView = layoutInflater.inflate(R.layout.item_print_hub_send_order_dialog, null);
				holder.vTxtPrintSize = (TextView) convertView.findViewById(R.id.txt_printHub_sendOrder_size);
				holder.vTxtPrintNumber = (TextView) convertView.findViewById(R.id.txt_printHub_sendOrder_number);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			final RssEntry entry = selectedEntries.get(position);
			holder.vTxtPrintSize.setText(entry.proDescription.shortName);
			holder.vTxtPrintNumber.setText(itemSelectedCounts.get(entry).toString());
			return convertView;
		}

		private class Holder {
			TextView vTxtPrintSize;
			TextView vTxtPrintNumber;
		}

	}

	public interface AllPrintItemsChangedListener {
		public void onAllPrintItemsChanged();
	}
}
