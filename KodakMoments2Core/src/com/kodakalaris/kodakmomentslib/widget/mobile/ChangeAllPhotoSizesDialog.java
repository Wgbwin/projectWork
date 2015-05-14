package com.kodakalaris.kodakmomentslib.widget.mobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.simonvt.numberpicker.NumberPicker;
import net.simonvt.numberpicker.NumberPicker.OnValueChangeListener;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.interfaces.IKM2Manager;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.util.DimensionUtil;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.PrintSizesDialog.PrintItemsChangedListener;

public class ChangeAllPhotoSizesDialog extends BaseGeneralAlertDialogFragment {

	private PrintAdapter mAdapter;
	private IKM2Manager mManager;
	private PrintItemsChangedListener mListener;
	private List<PhotoInfo> images = new ArrayList<PhotoInfo>();
	private Context mContext;
	private PopupWindow popupWindow = null;
	private CustomNumberPicker numberPicker = null;

	public ChangeAllPhotoSizesDialog(Context context, List<PrintItem> printItems, PrintItemsChangedListener listener, boolean cancelable) {
		super(context, cancelable);
		this.mContext = context;
		if (KM2Application.getInstance().getFlowType().isPrintWorkFlow()) {
			mManager = PrintManager.getInstance(mContext);
		} else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()) {
			mManager = PrintHubManager.getInstance();
		}
		mAdapter = new PrintAdapter(context, printItems);
		mListener = listener;
	}

	public ChangeAllPhotoSizesDialog initDialog(Context context) {
		setTitle(getTitle());
		setNegativeButton(context.getString(R.string.Common_Cancel), new OnClickListener() {

			@Override
			public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
				dismiss();
			}
		});
		setPositiveButton(context.getString(R.string.save), new OnClickListener() {

			@Override
			public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
				Map<RssEntry, Integer> itemSelectedCounts = mAdapter.getItemSelectedCounts();
				Set<RssEntry> entrySet = itemSelectedCounts.keySet();
				Iterator<RssEntry> iter = entrySet.iterator();
				while (iter.hasNext()) {
					RssEntry entry = iter.next();
					int counts = itemSelectedCounts.get(entry);
					if (KM2Application.getInstance().getFlowType().isPrintWorkFlow()) {
						((PrintManager) mManager).changeAllPhotoSize(images,counts, entry);
					} else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()) {
						((PrintHubManager) mManager).changeAllPhotoSize(images,counts, entry);
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
		lvPrints.setDividerHeight(25);
		lvPrints.setSelector(R.color.transparent);
		mAdapter.notifyDataSetChanged();
		return lvPrints;
	}

	private class PrintAdapter extends BaseAdapter {

		private LayoutInflater layoutInflater;
		private List<RssEntry> entries;
		private Map<RssEntry, Integer> itemSelectedCounts;
		private Button btn;
		private View cView;
		private List<RssEntry> enableProducts;

		public PrintAdapter(Context context, List<PrintItem> items) {
			layoutInflater = LayoutInflater.from(context);
			if (KM2Application.getInstance().getFlowType().isPrintWorkFlow()) {
				entries = ((PrintManager) mManager).getPrintProducts();
				enableProducts = ((PrintManager) mManager).getEnableProducts(selectedProducts());
			} else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()) {
				entries = ((PrintHubManager) mManager).getPrintProducts();
			}
			itemSelectedCounts = new HashMap<RssEntry, Integer>();
			PhotoInfo image = null;
			if(items!=null){
				for (PrintItem item : items) {
					image = item.getImage();
					if(images==null){
						images = new ArrayList<PhotoInfo>();
					}
					Iterator<PhotoInfo> itor = images.iterator();
					boolean isPhotoExist = false;
					while (itor.hasNext()) {
						PhotoInfo photoInfo = (PhotoInfo) itor.next();
						if(image.equalsNotConsiderDesId(photoInfo)){
							isPhotoExist = true;
							break;
						}
					}
					if(!isPhotoExist){
						images.add(image);
					}
				}
			}
			
			int tempCount = 0;
			for (RssEntry entry : entries) {
				int count = 0;
				for (PrintItem item : items) {
					if (item.getEntry().equals(entry)) {
						tempCount = item.getCount();
						if (count < tempCount) {
							count = tempCount;
						}
					}
				}
				itemSelectedCounts.put(entry, count);
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
			if (convertView == null) {
				holder = new Holder();
				convertView = layoutInflater.inflate(R.layout.item_change_all_print_size_dialog, null);
				holder.vTxtPrintSize = (TextView) convertView.findViewById(R.id.txt_changAllSizes_print_size);
				holder.vBtnPrintNumber = (Button) convertView.findViewById(R.id.btn_changAllSizes_print_number);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			final RssEntry entry = entries.get(position);
			
			// disable the products that the current selected retailers do not support
			if(enableProducts != null && enableProducts.size()>0){
				holder.vBtnPrintNumber.setEnabled(enableProducts.contains(entry));
			}
			// update the color of the products which are not supported by the selected retailers
			int textColorId = 0;
			if(holder.vBtnPrintNumber.isEnabled()){
				textColorId = R.color.near_black;
			} else {
				textColorId = R.color.near_black_50alpha;
			}
			holder.vTxtPrintSize.setTextColor(getResources().getColor(textColorId));
			
			holder.vTxtPrintSize.setText(entry.proDescription.shortName);
			holder.vBtnPrintNumber.setText(itemSelectedCounts.get(entry).toString());
			cView = convertView;
			btn = (Button) holder.vBtnPrintNumber;
			setItemEvents(cView, btn, entry);
			return convertView;
		}

		public Map<RssEntry, Integer> getItemSelectedCounts() {
			return itemSelectedCounts;
		}

		private class Holder {
			TextView vTxtPrintSize;
			TextView vBtnPrintNumber;
		}

		private void showPopWindow(final View convertView) {
			View popContentView = LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.number_picker, null);
			numberPicker = (CustomNumberPicker) popContentView.findViewById(R.id.numberPicker);
			numberPicker.setMaxValue(100);
			numberPicker.setMinValue(0);
			numberPicker.setValue(Integer.valueOf(selectButton.getText().toString()));		
			numberPicker.setOnValueChangedListener(new OnValueChangeListener() {
				
				@Override
				public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
					selectButton.setText(numberPicker.getValue() + "");
					itemSelectedCounts.put(selectEntry, numberPicker.getValue());
					
					if(KM2Application.getInstance().getFlowType().isPrintWorkFlow()){
						enableProducts = ((PrintManager) mManager).getEnableProducts(selectedProducts());
					}
					
				}
			});
			
			
			int pending = DimensionUtil.dip2px(mContext, 16);
			if (null != popupWindow && popupWindow.isShowing()) {
				popupWindow.dismiss();
			} else {
				popupWindow = new PopupWindow(popContentView, convertView.getWidth() + 2 * pending, convertView.getHeight() * 5, true);
				popupWindow.setFocusable(true);
				popupWindow.setOutsideTouchable(true);
				popupWindow.setBackgroundDrawable(new PaintDrawable(Color.WHITE));
				popupWindow.showAsDropDown(convertView, -pending, 0);
			}
		}

		private void setItemEvents(final View convertView, final Button btn, final RssEntry entry) {
			btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if(enableProducts != null){
						if(!enableProducts.contains(entry)){
							return;
						}
					}
					
					selectButton = btn;
					selectEntry = entry;
					showPopWindow(convertView);
				}
			});
		}

		private Button selectButton = null;
		private RssEntry selectEntry = null;

		
		private List<RssEntry> selectedProducts(){
			List<RssEntry> entries = new ArrayList<RssEntry>();
			if(itemSelectedCounts != null){
				Iterator<RssEntry> iter = itemSelectedCounts.keySet().iterator();
				while(iter.hasNext()){
					final RssEntry entry = iter.next();
					if(itemSelectedCounts.get(entry) > 0){
						entries.add(entry);
					}
				}
			}
			return entries;
		}

	}

	public interface AllPrintItemsChangedListener {
		public void onAllPrintItemsChanged();
	}

	private String getTitle() {
		String title = "";
		Integer photoCount = images.size();
		if (photoCount == 1) {
			title = mContext.getString(R.string.changeAllSize_title_one);
		} else {
			title = mContext.getString(R.string.changeAllSize_title_two).replace("%%", photoCount.toString());
		}
		return title;
	}

}
