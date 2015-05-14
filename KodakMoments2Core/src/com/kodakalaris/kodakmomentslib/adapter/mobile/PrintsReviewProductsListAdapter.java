package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.util.ArrayList;
import java.util.List;

import net.simonvt.numberpicker.NumberPicker;
import net.simonvt.numberpicker.NumberPicker.OnValueChangeListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.photoedit.MPhotoEditActivity;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROIWithRotateDegree;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.interfaces.IKM2Manager;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.util.DimensionUtil;
import com.kodakalaris.kodakmomentslib.util.FontUtils;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.mobile.CustomNumberPicker;
import com.kodakalaris.kodakmomentslib.widget.mobile.PrintSizesDialog;
import com.kodakalaris.kodakmomentslib.widget.mobile.PrintSizesDialog.PrintItemsChangedListener;
import com.kodakalaris.kodakmomentslib.widget.mobile.ReviewPhotoView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class PrintsReviewProductsListAdapter extends BaseAdapter {
	private Context mContext;
	private int itemViewResource;
	private List<PrintItem> mAllPrintItems;
	private List<List<PrintItem>> mPrintItemsClassified;
	private LayoutInflater listContainer;
	private CustomNumberPicker numberPicker = null;
	private PopupWindow popupWindow = null;
	private IKM2Manager printManager = null;
	private PrintItemsChangedListener mPrintItemsChangedListener;
	private TextView selectedText = null;

	public PrintsReviewProductsListAdapter() {

	}

	public PrintsReviewProductsListAdapter(Context context, List<PrintItem> data, int resource) {
		this.mContext = context;
		this.itemViewResource = resource;
		this.mAllPrintItems = data;
		listContainer = LayoutInflater.from(mContext);
		initPhotoReviewProductsData();
	}
	
	public void setDataSource(List<PrintItem> dataList) {
		mAllPrintItems = dataList;
		if (mAllPrintItems != null && getCount() > 0) {
			initPhotoReviewProductsData();
			notifyDataSetChanged();
		} else {
			notifyDataSetInvalidated();
		}
	}
	
	public void initPhotoReviewProductsData(){
		List<RssEntry> entries = null;
		if (KM2Application.getInstance().getFlowType().isPrintWorkFlow()) {
			printManager = PrintManager.getInstance(mContext);
			entries = ((PrintManager) printManager).getPrintProducts();
		} else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()) {
			printManager = PrintHubManager.getInstance();
			entries = ((PrintHubManager) printManager).getPrintProducts();
		}
		//sort items
		sortPrintItems(entries);
		List<PhotoInfo> selectedPhotoIgnoreDesIdSet = new ArrayList<PhotoInfo>();
		for (PrintItem item : mAllPrintItems) {
			PhotoInfo photo = item.getImage();
			boolean isExist = false;
			for (PhotoInfo photoInfo : selectedPhotoIgnoreDesIdSet) {
				if(photo.equalsNotConsiderDesId(photoInfo)){
					isExist = true;
					break;
				}
			}
			if(!isExist){
				selectedPhotoIgnoreDesIdSet.add(photo);
			}
			
		}
		
		mPrintItemsClassified = new ArrayList<List<PrintItem>>();
		for (PhotoInfo photoInfo : selectedPhotoIgnoreDesIdSet) {
			List<PrintItem> itemsWithSamePhoto = new ArrayList<PrintItem>();
			for (PrintItem item : mAllPrintItems) {
				if(item.getImage().equalsNotConsiderDesId(photoInfo)){
					itemsWithSamePhoto.add(item);
				}
			}
			mPrintItemsClassified.add(itemsWithSamePhoto);
			
		}
		
	}
	
	private void sortPrintItems(List<RssEntry> entries){
		List<PrintItem> sortedList = new ArrayList<PrintItem>();
		for (RssEntry entry : entries) {
			for (PrintItem item : mAllPrintItems) {
				if(item.getEntry().equals(entry)){
					sortedList.add(item);
				}
			}
		}
		if(sortedList!=null && sortedList.size()>0){
			mAllPrintItems = sortedList;
		}
	}

	@Override
	public int getCount() {
		if(mPrintItemsClassified!=null){
			return mPrintItemsClassified.size();
		}else{
			return 0;
		}
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
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = listContainer.inflate(itemViewResource, parent ,false);
			viewHolder = new ViewHolder();
			viewHolder.vRelaLyImageSelect = (RelativeLayout) convertView.findViewById(R.id.relaLy_PrintsReview_imageSelect);
			viewHolder.vRealLyLeft = (RelativeLayout) convertView.findViewById(R.id.realLy_PrintsReview_left);
			viewHolder.vRealLyCenter = (RelativeLayout) convertView.findViewById(R.id.realLy_PrintsReview_center);
			viewHolder.vAddSizeBtn = (Button) convertView.findViewById(R.id.btn_select_print_size);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		initItem(viewHolder, position);
		return convertView;
	}
	
	
	class ViewHolder{
		RelativeLayout vRelaLyImageSelect;
		RelativeLayout vRealLyLeft;
		RelativeLayout vRealLyCenter; 
		Button vAddSizeBtn;
		
	}
	
	public void getLatestData() {
		if (KM2Application.getInstance().getFlowType().isPrintWorkFlow()) {
			this.mAllPrintItems = ((PrintManager) printManager).getPrintItems();
		} else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()) {
			this.mAllPrintItems = ((PrintHubManager) printManager).getPrintItems();
		}
	}

	
	private void initItem(ViewHolder viewHolder, int position){
		final List<PrintItem> mPrintItemsWithSamePhoto = mPrintItemsClassified.get(position);
		String loadFilePath = "";
		int columnNumber = mPrintItemsWithSamePhoto.size();
		int targetH = (int) (KM2Application.getInstance().getScreenH() / 4);
		int w = DimensionUtil.dip2px(mContext, 40);
		int parentW = KM2Application.getInstance().getScreenW() - 2 * w;
		viewHolder.vRelaLyImageSelect.removeAllViews();
		viewHolder.vRealLyLeft.removeAllViews();
		viewHolder.vRealLyCenter.removeAllViews();
		int maxHigh = getmaxHigh(mPrintItemsWithSamePhoto);
		
		for (int i= 0;i<columnNumber;i++ ) {
			final PrintItem item = mPrintItemsWithSamePhoto.get(i);
			final RssEntry entry = item.getEntry();
			ROI roi = item.getRoi();
			int rotateDegree = item.rotateDegree;
			String photoEditPath = item.getImage().getPhotoEditPath();
			ROIWithRotateDegree roiWithRotateDegree = new ROIWithRotateDegree();
			RelativeLayout.LayoutParams rpLeftParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, w);
			RelativeLayout.LayoutParams rpCenterParams = new RelativeLayout.LayoutParams(w, w);
			ReviewPhotoView imageView = new ReviewPhotoView(mContext,item);
			int pageW =  entry.proDescription.pageWidth;
			int pageH =  entry.proDescription.pageHeight;
			double scaleVale = 1.0;
			int imageViewWide = 0;
			int imageViewHeigh = 0;
			int tempContainter = 0;
			if (null!=roi) {
				int containerW = (int) (roi.w * roi.ContainerW);
				int containerH = (int) (roi.h * roi.ContainerH);
				roiWithRotateDegree.setRoi(roi);
				roiWithRotateDegree.setRotateDegree(rotateDegree);
				if (item.getImage().isNeedSwapWidthAndHeightForCalculate() && !item.isServerImage) {
					tempContainter = containerW;
					containerW = containerH;
					containerH = tempContainter;
				}
				if (containerW > containerH) {
					int tempValue = 0;
					if (pageW < pageH) {
						tempValue = pageH;
						pageH = pageW;
						pageW = tempValue;
					}

				} else if (containerW < containerH) {
					int tempValue = 0;
					if (pageW > pageH) {
						tempValue = pageH;
						pageH = pageW;
						pageW = tempValue;
					}
				}
			}
			if (targetH > maxHigh) {
				scaleVale = targetH / maxHigh;
				imageViewHeigh = (int) (pageH * scaleVale);
				imageViewWide = (int) (pageW * scaleVale);
			} else {
				scaleVale = maxHigh / targetH;
				imageViewHeigh = (int) (pageH / scaleVale);
				imageViewWide = (int) (pageW / scaleVale);
			}
			
			DisplayImageOptions displayImageOptions = null;
			displayImageOptions = getImageLoadOptions(roiWithRotateDegree);
			if (!"".equals(photoEditPath)) {
				loadFilePath = photoEditPath;
			} else {
				loadFilePath = item.getImage().getPhotoPath();
			}
			
			if(item.getImage().getPhotoSource().isFromPhone()){
				ImageLoader.getInstance().displayImage("file://" + loadFilePath, imageView, displayImageOptions);
			}
			RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(imageViewWide, imageViewHeigh);
			imageView.setId(10000 + i);
			imageView.setProductName(entry.proDescription.shortName);
			imageView.setScaleType(ScaleType.FIT_XY);
			imageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent(mContext, MPhotoEditActivity.class);
					if (item.getImage().getImageResource() != null) {
						Log.i("PrintsReviewProductsListAdapter", "getImageResource" + item.getImage().getImageResource().toString());
						if (item.getImage().getPhotoPath() != null) {
							Log.i("PrintsReviewProductsListAdapter", "getPhotoPath:" + item.getImage().getPhotoPath());
						}

					} else {
						Log.i("PrintsReviewProductsListAdapter", "getImageResource==null");
					}
					Bundle bundle = new Bundle();
					item.isCheckedInstance = true;
					bundle.putSerializable("printItem", item);
					intent.putExtras(bundle);
					mContext.startActivity(intent);
				
				}
				
			});
			
			TextView vTxtprintSize = new TextView(mContext);
			vTxtprintSize.setText(entry.proDescription.shortName);
			vTxtprintSize.setId(1000 + i);
			vTxtprintSize.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			vTxtprintSize.setTextAppearance(mContext, R.style.h2_text);
			vTxtprintSize.setTypeface(FontUtils.getFont(mContext, FontUtils.NAME_THIN));
			
			TextView vTxtprintNumber = new TextView(mContext);
			vTxtprintNumber.setBackgroundResource(R.drawable.quantity_box);
			vTxtprintNumber.setText((item.getCount()) + "");
			vTxtprintNumber.setGravity(Gravity.CENTER);
			vTxtprintNumber.setTextAppearance(mContext, R.style.h2_text);
			vTxtprintNumber.setTextColor(mContext.getResources().getColor(R.color.kodak_red));
			vTxtprintNumber.setTypeface(FontUtils.getFont(mContext, FontUtils.NAME_THIN));
			vTxtprintNumber.setId(100 + i);
			vTxtprintNumber.setTag(i);
			vTxtprintNumber.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					//TODO
					selectedText = (TextView) v;
					showPopWindow(v,item);
					
				}
				
			});
			
			imageParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			if (columnNumber == 1) {
				imageParams.setMargins((parentW - imageViewWide) / 2, 0, 0, 0);
			}
			if (i > 0) {
				imageParams.addRule(RelativeLayout.RIGHT_OF, 10000 + i - 1);
				imageParams.setMargins(10, 0, 0, 0);

				rpLeftParams.addRule(RelativeLayout.BELOW, 1000 + i - 1);
				rpLeftParams.addRule(RelativeLayout.ALIGN_LEFT, 1000 + i - 1);
				rpLeftParams.setMargins(0, 20, 0, 0);
				rpCenterParams.addRule(RelativeLayout.BELOW, 100 + i - 1);
				rpCenterParams.addRule(RelativeLayout.ALIGN_LEFT, 100 + i - 1);
				rpCenterParams.setMargins(0, 20, 0, 0);
			}
			viewHolder.vRealLyLeft.addView(vTxtprintSize, rpLeftParams);
			viewHolder.vRealLyCenter.addView(vTxtprintNumber, rpCenterParams);
			viewHolder.vRelaLyImageSelect.addView(imageView, imageParams);
			
		}
		viewHolder.vAddSizeBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				PrintSizesDialog dialog = new PrintSizesDialog(mContext, mPrintItemsWithSamePhoto, mPrintItemsChangedListener, false);
				dialog.initDialog(mContext);
				dialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "PrintSizesDialog"); // TODO modify
																									// this tag
			}
		});
		
	}
	
	private void showPopWindow(final View view ,final PrintItem printItem){
		View popContentView = LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.number_picker, null);
		numberPicker = (CustomNumberPicker) popContentView.findViewById(R.id.numberPicker);
		numberPicker.setMaxValue(100);
		numberPicker.setMinValue(0);
		numberPicker.setValue(printItem.getCount());
		numberPicker.setOnValueChangedListener(new OnValueChangeListener() {
			
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				selectedText.setText(newVal + "");
				
			}
		});
		

		
		if (null != popupWindow && popupWindow.isShowing()) {
			popupWindow.dismiss();
		} else {
			int w = KM2Application.getInstance().getScreenW();
			int h = KM2Application.getInstance().getScreenH() / 3;
			popupWindow = new PopupWindow(popContentView, w, h, true);
			popupWindow.setFocusable(true);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setBackgroundDrawable(new PaintDrawable(Color.WHITE));
			popupWindow.showAsDropDown(view);
		}
		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				int count = numberPicker.getValue();
				if (count == 0) {
					if (KM2Application.getInstance().getFlowType().isPrintWorkFlow()) {
						((PrintManager) printManager).deletePrintItem(printItem);
					} else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()) {
						((PrintHubManager) printManager).deletePrintItem(printItem);
					}
					getLatestData();
					initPhotoReviewProductsData();
					notifyDataSetChanged();
				} else {
					if (KM2Application.getInstance().getFlowType().isPrintWorkFlow()) {
						printItem.setCount(count);
						((PrintManager) printManager).updateNumber(count, printItem);
					} else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()) {
						((PrintHubManager) printManager).updateNumber(count, printItem);
					}
					getLatestData();
					initPhotoReviewProductsData();
				}
				mPrintItemsChangedListener.onPrintItemsChanged();
			
				
			}
			
		});
		
	}
	
	private DisplayImageOptions getImageLoadOptions(ROIWithRotateDegree roiWithRotateDegree) {
		DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.imageerror).showImageOnFail(R.drawable.imageerror)
				.cacheOnDisk(false).considerExifParams(true).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).roi(roiWithRotateDegree).build();
		return displayImageOptions;
	}

	private int getmaxHigh(List<PrintItem> printItems) {
		int maxHigh = 0;
		int tempH = 0;
		for (PrintItem item : printItems) {
			tempH = item.getEntry().proDescription.pageHeight;
			if (maxHigh < tempH) {
				maxHigh = tempH;
			}

		}
		return maxHigh;
	}

	public void setmPrintItemsChangedListener(PrintItemsChangedListener mPrintItemsChangedListener) {
		this.mPrintItemsChangedListener = mPrintItemsChangedListener;
	}

}
