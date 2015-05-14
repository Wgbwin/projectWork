package com.kodak.rss.tablet.adapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;

public class CalendarProductSelectionAdapter extends BaseAdapter implements onProcessImageResponseListener{
	
	private Context mContext;
	private List<RssEntry> calendarEntrys = new ArrayList<RssEntry>();	
	private RelativeLayout.LayoutParams mLayoutParams;
	private DisplayMetrics dm;
	private SelectCalendarListener selectCalendarListener;
	
	public ImageUseURIDownloader imageDownloader;
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();
	private LruCache<String, Bitmap> mMemoryCache;  	
	
	public CalendarProductSelectionAdapter(Context context,int height,List<RssEntry> calendarEntrys,LruCache<String, Bitmap> mMemoryCache){
		mContext = context;
		this.calendarEntrys = calendarEntrys;				
		mLayoutParams = new  RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);		
		this.imageDownloader = new ImageUseURIDownloader(context,pendingRequests,this);	
		this.imageDownloader.setSaveType(FilePathConstant.calendarType);
		this.imageDownloader.setIsThumbnail(true);
		this.mMemoryCache = mMemoryCache;	
		dm = context.getResources().getDisplayMetrics();
	}

	@Override
	public int getCount() {
		if(calendarEntrys == null)return 0;
		return calendarEntrys.size();
	}

	@Override
	public Object getItem(int position) {
		return calendarEntrys.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}	
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.photobook_select_item, null);
			holder = new Holder();
			holder.llayoutV =  convertView.findViewById(R.id.ll_photobook);			
			holder.ivPreview = (ImageView) convertView.findViewById(R.id.iv_photobook_preview);
			holder.wvDetail = (WebView) convertView.findViewById(R.id.wv_photobook_detail);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		holder.llayoutV.setLayoutParams(mLayoutParams);
		holder.wvDetail.loadDataWithBaseURL(null, calendarEntrys.get(position).getMarketing(), "text/html", "utf-8", null);
		holder.wvDetail.setBackgroundColor(0);
		holder.wvDetail.getBackground().setAlpha(2);
		
		RssEntry rssEntry = calendarEntrys.get(position);
		URI pictureURI = CalendarUtil.getURI(rssEntry);	
		if (pictureURI != null) {
			String profileId = rssEntry.proDescription.id;
			Bitmap bitmap = MemoryCacheUtil.getBitmap(mMemoryCache, profileId);			
			if (bitmap == null ||(bitmap != null && bitmap.isRecycled())) {
				holder.ivPreview.setTag(profileId);							
				imageDownloader.downloadProfilePicture(profileId, pictureURI, holder.ivPreview,position,true);				
			}
			holder.ivPreview.setImageBitmap(bitmap); 
		}else {
			holder.ivPreview.setImageBitmap(null);
		}	
				
		holder.wvDetail.setOnTouchListener(new OnCTouchListener(position));
		holder.wvDetail.setOnLongClickListener(new OnLongClickListener() {			
			@Override
			public boolean onLongClick(View v) {				
				return true;
			}
		});
		holder.ivPreview.setOnClickListener(new OnCCListener(position));
		holder.llayoutV.setOnClickListener(new OnCCListener(position));
		return convertView;
	}
	
	private class OnCCListener implements OnClickListener {
		
		private int position;
		public OnCCListener(int position){
			this.position = position;
		}		
		@Override
		public void onClick(View v) {		
			if (selectCalendarListener != null) {
				selectCalendarListener.onselectCalendar(position);	
			}
		}
	};
	
	public interface SelectCalendarListener {
		public void onselectCalendar(int position);
	}
	
	public void setSelectCalendarListener(SelectCalendarListener selectListener){
		selectCalendarListener = selectListener;
	}
	
	private float downRawX,downRawY;
	private boolean isScroll;
	private class OnCTouchListener implements OnTouchListener {
		
		private int position;
		public OnCTouchListener(int position){
			this.position = position;
		}		
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			float mRawX = event.getRawX();
			float mRawY = event.getRawY();
			
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				downRawX = mRawX;
				downRawY = mRawY;
				isScroll = false;
				break;
			case MotionEvent.ACTION_MOVE:
				float moveSpacing = spacing(mRawX, downRawX, mRawY, downRawY);
				if (moveSpacing > dm.density*3) {
					isScroll = true;
				}				
				break;
			case MotionEvent.ACTION_UP:	
				if (!isScroll) {
					float spacing = spacing(mRawX, downRawX, mRawY, downRawY);
					if (spacing < dm.density*3) {
						if (selectCalendarListener != null) {
							selectCalendarListener.onselectCalendar(position);	
						}
					}
				}				
				isScroll = false;
				break;
			}
			return false;			
		}
	};
	
	private float spacing(float x1,float x2, float y1,float y2) {
		float space = (x2 - x1)*(x2 - x1) + (y2-y1)*(y2-y1);
		return FloatMath.sqrt(space);
	}	

	class Holder {
		View llayoutV;		
		private ImageView ivPreview;
		private WebView wvDetail;
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
		if (calendarEntrys == null) return;		
		for (int i = 0; i < calendarEntrys.size(); i++) {
			RssEntry rssEntry = calendarEntrys.get(i);
			if (rssEntry == null) continue;		
			if (rssEntry.proDescription == null) continue;
			imageDownloader.cancelRequest(rssEntry.proDescription.id, null, 0);			
		}				
	}	
}
