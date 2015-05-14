package com.kodak.rss.tablet.adapter;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.kodak.rss.core.n2r.bean.calendar.CalendarTheme;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.CalendarThemeSelectionActivity;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.CalendarThemeItemView;

public class CalendarThemeSelectionAdapter extends BaseAdapter implements onProcessImageResponseListener{
	
	private Context mContext;
	private List<CalendarTheme> calendarThemes;	
	
	public ImageUseURIDownloader imageDownloader;
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();
	public LruCache<String, Bitmap> mMemoryCache; 
	
	public CalendarTheme curSelectedTheme;
	public Bitmap waitBitmap;
	private int height;
	
	public CalendarThemeSelectionAdapter(Context context,int height, List<CalendarTheme> calendarThemes,LruCache<String, Bitmap> mMemoryCache){
		mContext = context;
		this.calendarThemes = calendarThemes;		
		this.height = height;
		
		this.imageDownloader = new ImageUseURIDownloader(context,pendingRequests,this);	
		this.imageDownloader.setSaveType(FilePathConstant.calendarType);
		this.imageDownloader.setIsThumbnail(true);
		this.mMemoryCache = mMemoryCache;	
	}

	@Override
	public int getCount() {
		if(calendarThemes == null)return 0;
		return calendarThemes.size();
	}

	@Override
	public Object getItem(int position) {
		return calendarThemes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CalendarThemeItemView calendarThemeItemView = null;
		if(convertView == null){
			calendarThemeItemView = new CalendarThemeItemView(mContext,height);
			convertView = calendarThemeItemView;
			convertView.setTag(calendarThemeItemView);
		} else {
			calendarThemeItemView = (CalendarThemeItemView) convertView.getTag();
		}
		CalendarTheme calendarTheme = calendarThemes.get(position);	
		
		calendarThemeItemView.setValue((CalendarThemeSelectionActivity)mContext, calendarTheme, this, position);	
		return convertView;
	}
	
	public Bitmap getBitmap(String themeId,URI pictureURI,View view,int position){
    	Bitmap mBitmap = MemoryCacheUtil.getBitmap(mMemoryCache, themeId);	        	        
        if (mBitmap == null) {
        	view.setTag(themeId);
        	if (waitBitmap ==null || waitBitmap.isRecycled()) {
				waitBitmap=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.image_wait234x156);
 			 }	    	    	        	 
			mBitmap = waitBitmap;        	
        	if (pictureURI != null) {
				imageDownloader.downloadProfilePicture(themeId, pictureURI, view,position,true);   	  	 						
        	}        	   	    	        	
		}
        return mBitmap;
    }	

	@Override
	public void onProcess(Response response, String profileId, View view,int position, String flowType,String productId) {			
		if (response == null || imageDownloader == null) return;				
		if (response.getError() != null) {
			
		}else{			
			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);										
			if (bitmap != null && view != null && view instanceof ImageView) {
				if (view.getTag().toString().equals(profileId)) {
					if (view.getVisibility() == View.VISIBLE) {
						((ImageView) view).setImageBitmap(bitmap);
					}
				}
			}	
		}									
	}
	
	public void cancelRequest(){
		if (pendingRequests == null) return;
		if (imageDownloader == null) return;				
		if (calendarThemes == null) return;		
		for (int i = 0; i < calendarThemes.size(); i++) {
			CalendarTheme theme = calendarThemes.get(i);
			if (theme == null) continue;						
			imageDownloader.cancelRequest(theme.id, null, 0);			
		}				
	}	
	
}
