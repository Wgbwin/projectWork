package com.kodak.rss.tablet.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.tablet.bean.MainMenuItem;
import com.kodak.rss.tablet.view.MainMenuItemView;

public class MainMenuAdapter extends BaseAdapter{
	private Context mContext;
	private List<MainMenuItem> mList = new ArrayList<MainMenuItem>();

	public MainMenuAdapter(Context context, List<Catalog> catalogs) {
		mContext = context;
		
		initList(catalogs);
	}
	
	private void initList(List<Catalog> catalogs) {
		if (catalogs == null || catalogs.isEmpty()) {
			//in this case, only show kiosk
			addToList(new MainMenuItem(mContext, MainMenuItem.TYPE_KIOSK));
		} else {
			addToList(new MainMenuItem(mContext, MainMenuItem.TYPE_MY_PROJECTS));
			for (Catalog catalog : catalogs) {
				for (RssEntry entry : catalog.rssEntries) {
					MainMenuItem item = new MainMenuItem(mContext, entry.proDescription);
					addToList(item);
				}
			}
			addToList(new MainMenuItem(mContext, MainMenuItem.TYPE_KIOSK));
		}
	}
	
	
	/**
	 * Avoid duplicate item
	 * And when add in list, order by type desc
	 * @param product
	 */
	private void addToList(MainMenuItem product) {
		if (product.getProductType() == 0) {
			return;
		}
		
		int index = mList.size();
		for (int i = 0; i < mList.size(); i++) {
			MainMenuItem item = mList.get(i);
			
			//avoid duplicate item
			if (item.getProductType() == product.getProductType()) {
				return;
			}
			
			if (product.getProductType() < item.getProductType()) {
				index = i;
				break;
			}
		}
	
		mList.add(index,product);
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public MainMenuItem getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View cv, ViewGroup parent) {
		MainMenuItem item = getItem(position);
		
		MainMenuItemView view;
		if (cv == null) {
			view = new MainMenuItemView(mContext);
			cv = view;
		} else {
			view = (MainMenuItemView) cv;
		}
		
		view.setInfo(item);
		return view;
	}
	
}
