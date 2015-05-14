package com.kodak.rss.tablet.adapter;

import java.net.URI;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.CalendarMainView;
import com.kodak.rss.tablet.view.CalendarPageView;

public abstract class CalendarProductAdapter extends BaseAdapter implements onProcessImageResponseListener{
	
	public Context mContext;	
	public DisplayMetrics dm;
	public int itemSize;
	public int selectedPostions = -1;	
	public LinearLayout.LayoutParams mLayoutParams;
	public int pageWidth,pageHeight;
	public Bitmap waitBitmap;
	public ImageUseURIDownloader imageDownloader;
	public final Map<String, Request> pendingRequests;
	public LruCache<String, Bitmap> mMemoryCache;
	
	private CalendarMainView calendarMainView;
	
	public CalendarProductAdapter(Context context,float ratio,LruCache<String, Bitmap> mMemoryCache,Map<String, Request> pendingRequests){
		this.mContext = context;	
		this.pendingRequests = pendingRequests;
		this.dm = context.getResources().getDisplayMetrics();	
		pageWidth = (int) ((dm.widthPixels - dm.density*70)/7f);					
		pageHeight = (int) (pageWidth*ratio);		
		this.imageDownloader = new ImageUseURIDownloader(context,pendingRequests,this);	
		this.imageDownloader.setSaveType(FilePathConstant.calendarType);
		this.imageDownloader.setIsThumbnail(false);				
		this.imageDownloader.setViewParameters((int)pageWidth,(int)pageHeight);
		this.mMemoryCache = mMemoryCache;				
		this.mLayoutParams = new LinearLayout.LayoutParams(pageWidth,pageHeight);		
	}
	
	public void setViewSynLoadBitmap(CalendarMainView calendarMainView){
		this.calendarMainView = calendarMainView;
	}
	
	public void refresh(){
		refreshItem();
		notifyDataSetChanged();	
	}
	
	public abstract void refreshItem();

	@Override
	public void onProcess(Response response, String profileId, View view,int position, String flowType,String productId) {					
		Calendar calendar = CalendarUtil.getCurrentCalendar();
		if (calendar == null || calendar.pages == null) return;	
		if (productId != null && !productId.equals(calendar.id)) return;	
		if (response == null || imageDownloader == null) return;
		if (profileId == null) return;
		int refreshCount = response.getRequest().getRefreshCount();
		MemoryCacheUtil.removeBitmap(mMemoryCache, profileId);	
		if (response.getError() != null) {

		} else {
			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);	
			if (bitmap != null) {	
				CalendarUtil.refreshSucPageInCalendar(profileId, refreshCount);				
				if (view != null && view instanceof CalendarPageView) {					
					if (view.getTag() != null && profileId.equals(view.getTag().toString())) {
						if (view.getVisibility() == View.VISIBLE) {
							((CalendarPageView) view).setImageBitmap(bitmap);
						}	
					}else {
						notifyDataSetChanged();
					}																					
				}
				if (calendarMainView != null) {
					CalendarPage[] currentPages = calendarMainView.getCurrentPages();
					if (currentPages != null) {
						for (int i = 0; i < currentPages.length; i++) {
							CalendarPage page = currentPages[i];
							if (page != null && page.id != null && profileId.equals(page.id)) {
								calendarMainView.synNotifyDataSet(i);
								break;
							}
						}
					}
				}
				
			}
		}	
	}

	public void setValue(CalendarPage page,CalendarPageView imageView,int position,boolean isSimplex,boolean isUp){		
		Calendar calendar = CalendarUtil.getCurrentCalendar();
		imageView.setBasicInfo(CalendarProductAdapter.this, position, isSimplex,isUp);
		if (page != null) {							
			URI pictureURI = CalendarUtil.getURI(page,pageWidth*3, pageHeight*3);			
			if (pictureURI != null) {
				String pageId = page.id;
				Bitmap bitmap = getBitmapFromCache(page,imageView,position);
				imageView.setTag(pageId);					
				imageView.setImageBitmap(bitmap == null ? getWaitBitmap() : bitmap); 
				if (page.isWantMainRefresh() || bitmap == null ) {														
					imageDownloader.downloadProfilePicture(pageId, pictureURI, imageView,position,false,calendar.id,page.getMainRefreshCount());											
				}						
			}else {
				imageView.setImageBitmap(null);   
			}		
		}else {
			imageView.setImageBitmap(null);   
		}
	}

	private Bitmap getBitmapFromCache(CalendarPage page,CalendarPageView imageView, int position){
		Bitmap bitmap = MemoryCacheUtil.getBitmap(mMemoryCache, page.id);		
		if(bitmap == null){
			bitmap = directUseUrlNative(page,imageView, position);
		}		
		return bitmap;
	}
	
	private Bitmap directUseUrlNative(CalendarPage page, CalendarPageView imageView, int position){
		if (page == null) return null;
		String pageId = page.id;
		String dispalyPath =  FilePathConstant.getLoadFilePath(FilePathConstant.calendarType, pageId, false,page.getMainRefreshCount(),page.getMainRefreshSucCount());	
		if (dispalyPath == null) return null;
		if (!page.isWantMainRefresh()) {
			imageDownloader.downloadProfilePicture(pageId, dispalyPath, imageView, position);
			return getWaitBitmap();
		}		
		return null;
	}
	
	private Bitmap getWaitBitmap(){
		if (waitBitmap == null || waitBitmap.isRecycled()) {
            waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);
    	}
		return waitBitmap;
	}

	public void cancelRequest(){
		if (pendingRequests == null) return;
		if (imageDownloader == null) return;		
		Calendar calendar = CalendarUtil.getCurrentCalendar();
		if (calendar == null) return;
		if (calendar.pages == null) return;
		for (int i = 0; i < calendar.pages.size(); i++) {
			CalendarPage page = calendar.pages.get(i);
			if (page == null) continue;			
			int refreshCount = page.getMainRefreshCount();	
			imageDownloader.cancelRequest(page.id, calendar.id, refreshCount);			
		}				
	}
	
	
}
