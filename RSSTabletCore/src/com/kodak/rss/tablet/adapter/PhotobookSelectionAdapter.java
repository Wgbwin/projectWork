package com.kodak.rss.tablet.adapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;

public class PhotobookSelectionAdapter extends BaseAdapter implements onProcessImageResponseListener{
	
	private Context mContext;
	private List<RssEntry> photobooks = new ArrayList<RssEntry>();	
	private RelativeLayout.LayoutParams mLayoutParams ;
	
	public ImageUseURIDownloader imageDownloader;
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();
	private LruCache<String, Bitmap> mMemoryCache;  	
	
	public PhotobookSelectionAdapter(Context context, int width,int height,List<RssEntry> photobooks,LruCache<String, Bitmap> mMemoryCache){
		mContext = context;
		this.photobooks = photobooks;		
		mLayoutParams = new RelativeLayout.LayoutParams(width, height);
		mLayoutParams.setMargins(0, 10, 10, 10);		
		
		this.imageDownloader = new ImageUseURIDownloader(context,pendingRequests,this);	
		this.imageDownloader.setSaveType(FilePathConstant.bookType);
		this.imageDownloader.setIsThumbnail(true);
		this.imageDownloader.setViewParameters(width, height/2);
		this.mMemoryCache = mMemoryCache;	
	}

	@Override
	public int getCount() {
		if(photobooks == null)return 0;
		return photobooks.size();
	}

	@Override
	public Object getItem(int position) {
		return photobooks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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
		holder.wvDetail.loadDataWithBaseURL(null, photobooks.get(position).getMarketing(), "text/html", "utf-8", null);
		holder.wvDetail.setBackgroundColor(0);
		holder.wvDetail.getBackground().setAlpha(2);
		
		RssEntry rssEntry = photobooks.get(position);
		URI pictureURI = PhotoBookProductUtil.getURI(rssEntry);	
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
		return convertView;
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
}
