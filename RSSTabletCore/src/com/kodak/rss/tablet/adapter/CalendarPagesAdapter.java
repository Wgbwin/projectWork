package com.kodak.rss.tablet.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;

import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.view.CalendarPagesItemView;

public class CalendarPagesAdapter extends CalendarProductAdapter {
		
	public ArrayList<CalendarPage[]> pageItems;	
	
	public CalendarPagesAdapter(Context context,float ratio,LruCache<String, Bitmap> mMemoryCache,Map<String, Request> pendingRequests){
		super(context, ratio, mMemoryCache, pendingRequests);		
		Calendar currentCalendar = CalendarUtil.getCurrentCalendar();				
		pageItems = setPageItems(currentCalendar.pages);
	}

	@Override
	public int getCount() {
		if(pageItems == null)return 0;
		itemSize = pageItems.size();
		return itemSize;
	}

	@Override
	public Object getItem(int position) {
		if(pageItems == null)return null;
		return pageItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}	
	
	public void refreshItem(){
		Calendar currentCalendar = CalendarUtil.getCurrentCalendar();				
		pageItems = setPageItems(currentCalendar.pages);
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		CalendarPagesItemView pagesItemView = null;		
		if (convertView == null) {
			pagesItemView = new CalendarPagesItemView(mContext,CalendarPagesAdapter.this);
			convertView = pagesItemView;
			convertView.setTag(pagesItemView);
		}else {
			pagesItemView = (CalendarPagesItemView) convertView.getTag();
		}	
		CalendarPage[] calendarPageItem = pageItems.get(position);
		
		pagesItemView.setBasicValue(calendarPageItem, position);
		return convertView;
	}


	private ArrayList<CalendarPage[]> setPageItems(List<CalendarPage> pages){
		ArrayList<CalendarPage[]> calendarPageItems = new ArrayList<CalendarPage[]>();	
		if (pages == null) return calendarPageItems;					
		int totelSize = pages.size()/2 + 1;		
		for (int i = 0; i < totelSize; i++) {
			CalendarPage[] item = new CalendarPage[2];
			if (i == 0) {
				item[0] = null;
				item[1] = pages.get(i);
			}else if(i == totelSize -1){
				item[0] = pages.get(2*i - 1);
				if (2*i == pages.size()-1) {
					item[1] = pages.get(2*i);
				}
			}else {
				item[0] = pages.get(2*i - 1);
				item[1] = pages.get(2*i);
			}
			calendarPageItems.add(item);
		}
		return calendarPageItems;
	}
	
}
