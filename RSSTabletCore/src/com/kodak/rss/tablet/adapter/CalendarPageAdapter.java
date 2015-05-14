package com.kodak.rss.tablet.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.view.CalendarPageView;

public class CalendarPageAdapter extends CalendarProductAdapter {
		
	public List<CalendarPage> pageItems;	
	
	public CalendarPageAdapter(Context context,float ratio,LruCache<String, Bitmap> mMemoryCache,Map<String, Request> pendingRequests){
		super(context, ratio, mMemoryCache, pendingRequests);		
		Calendar currentCalendar = CalendarUtil.getCurrentCalendar();				
		pageItems = currentCalendar.pages;
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
		pageItems = currentCalendar.pages;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		CalendarPageView pageItemView = null;		
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.calendar_page_item, null);
			pageItemView = (CalendarPageView) convertView.findViewById(R.id.upImage);
			convertView.setTag(pageItemView);					
		}else {
			pageItemView = (CalendarPageView) convertView.getTag();			
		}	
		CalendarPage calendarPage = pageItems.get(position);
		pageItemView.setLayoutParams(mLayoutParams);
		setValue(calendarPage,pageItemView, position,true,true);		
		return convertView;
	}

}
