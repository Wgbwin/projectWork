package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.AppConstants.FlowType;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.bean.items.HomeRibbonItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfigEntry;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.Catalog;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.manager.KMConfigManager;
import com.kodakalaris.kodakmomentslib.util.ImageUtil;
import com.kodakalaris.kodakmomentslib.util.StringUtils;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;

public class HomeProductsListAdapter extends BaseAdapter {
	private Context mContext;
	private int itemViewResource;
	private LayoutInflater listContainer;
	private ListItemView listItemView = null;
	private Handler handler = new Handler();
	private List<HomeRibbonItem> mList;
	private WeakMemoryCache mCache = new WeakMemoryCache();

	public HomeProductsListAdapter(Context context, List<FlowType> data, int resource) {
		this.mContext = context;
		this.itemViewResource = resource;
		listContainer = LayoutInflater.from(mContext);
		mList = convertToList(data);
	}
	
	public HomeProductsListAdapter(Context context, KMConfig config, int resource) {
		this.mContext = context;
		this.itemViewResource = resource;
		listContainer = LayoutInflater.from(mContext);
		mList = convertToList(config);
	}
	
	private List<HomeRibbonItem> convertToList(List<FlowType> flowTypes) {
		List<HomeRibbonItem> list = new ArrayList<HomeRibbonItem>();
		for (FlowType flowType : flowTypes) {
			HomeRibbonItem item = new HomeRibbonItem();
			switch (flowType) {
			case PRINT:
				item.title = mContext.getString(R.string.TitlePage_Prints_Enlargements);
				item.imgResId = R.drawable.image_prints;
				item.action = KMConfigEntry.ACTION_PRINTS_WORKFLOW;
				break;
				
			case KIOSK:
				item.title = mContext.getString(R.string.TitlePage_WifiConnect);
				item.imgResId = R.drawable.image_kioskconnect;
				item.action = KMConfigEntry.ACTION_KIOSK_CONNECT_WORKFLOW;
				break;
			}
			
			if (item.action != null) {
				list.add(item);
			}
		}
		return list;
	}
	
	private List<HomeRibbonItem> convertToList(KMConfig config) {
		List<HomeRibbonItem> list = new ArrayList<HomeRibbonItem>();
		if (config.configData.entries != null) {
			for (KMConfigEntry entry: config.configData.entries) {
				HomeRibbonItem item = new HomeRibbonItem();
				item.action = entry.action;
				item.title = entry.title;
				item.subTitle = entry.subtitle;
				item.imgUrl = entry.imageUrl;
				list.add(item);
			}
		}
		return list;
	}
	
	class ListItemView {
		public ImageView vImageProduct;
		public TextView vTxtProductName, vTxtProductPrice, vTxtProductDetail, vTxtProuctFrom;
		public View vViewDivide;
		public RelativeLayout vRelaLyProductItem;
		
		void setContent(final HomeRibbonItem item) {
			vTxtProductName.setText(item.title);
			if (!StringUtils.isEmpty(item.subTitle)) {
				vTxtProductDetail.setText(item.subTitle);
				vTxtProductDetail.setVisibility(View.VISIBLE);
			} else {
				vTxtProductDetail.setVisibility(View.INVISIBLE);
			}
			
			vViewDivide.setVisibility(View.INVISIBLE);
			vTxtProuctFrom.setVisibility(View.INVISIBLE);
			vTxtProductPrice.setVisibility(View.INVISIBLE);
			
			if (item.imgResId > 0) {
				vImageProduct.setBackgroundResource(item.imgResId);
			} else {
				String path = KMConfigManager.getInstance().getConfigImageFilePath(item.imgUrl);
				if (mCache.get(path) != null) {
					vImageProduct.setImageBitmap(decodeBitmap(item.imgUrl));
				} else {
					vImageProduct.setImageBitmap(null);
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							Bitmap bm = decodeBitmap(item.imgUrl);
							if (bm != null) {
								handler.post(new Runnable() {
									
									@Override
									public void run() {
										notifyDataSetChanged();
									}
								});
							}
						}
					}).start();
				}
			}
		}
		
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public HomeRibbonItem getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// be careful, don't remove the blocked code below. on 2015-1-22 Kane
		if (convertView == null) {
			convertView = listContainer.inflate(itemViewResource, null);
			listItemView = new ListItemView();
			listItemView.vImageProduct = (ImageView) convertView.findViewById(R.id.img_home_product);
			listItemView.vTxtProductName = (TextView) convertView.findViewById(R.id.txt_home_productName);
			listItemView.vTxtProductPrice = (TextView) convertView.findViewById(R.id.txt_home_productPrice);
			listItemView.vTxtProductDetail = (TextView) convertView.findViewById(R.id.txt_home_productDetail);
			listItemView.vTxtProuctFrom = (TextView) convertView.findViewById(R.id.txt_home_from);
			listItemView.vRelaLyProductItem = (RelativeLayout) convertView.findViewById(R.id.relaLy_home_productItem);
			listItemView.vViewDivide = convertView.findViewById(R.id.v_home_divide);

			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}
		
		listItemView.setContent(mList.get(position));
		
		return convertView;
	}
	
	private Bitmap decodeBitmap(String url) {
		String path = KMConfigManager.getInstance().getConfigImageFilePath(url);
		if (mCache.get(path) == null) {
			if (!new File(path).exists()) {
				return null;
			}
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, opts);
			
			//TODO : final value * 2 to avoid oom, not a good idea, try optimize it
			opts.inSampleSize = calculateInSampleSize(opts.outWidth, opts.outHeight, KM2Application.getInstance().getScreenW(), KM2Application.getInstance().getScreenH() / 4) * 2;
			opts.inJustDecodeBounds = false;
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			Bitmap bm = ImageUtil.decodeImageIgnorOom(path, opts);
			if (bm != null) {
				mCache.put(path, bm);
			}
		}
		
		return mCache.get(path);
	}
	
	private int calculateInSampleSize(int imgWidth, int imgHeight, int reqWidth, int reqHeight) {
		if (imgWidth < reqWidth || imgHeight < reqHeight) {
			return 1;
		}
		
		int inSampleSize = 1;
		int stretchW = (int) Math.floor((float) imgWidth / (float) reqWidth);
		int stretchH = (int) Math.floor((float) imgHeight / (float) reqHeight);
		
		inSampleSize = stretchW < stretchH ? stretchW : stretchH;
		
		return inSampleSize;
	}
	
}
