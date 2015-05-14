package com.kodak.rss.tablet.adapter;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.bean.content.Theme.BackGround;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.PhotoBooksThemeSelectActivity;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.ThemeItemContentView;
import com.kodak.rss.tablet.view.ThemeItemView;

public class PhotobookThemeSelectionAdapter extends BaseAdapter implements onProcessImageResponseListener{	
	private Context mContext;
	private RssTabletApp app;
	private int size;	
	private int width;	
	private int height;
	private boolean isLoadZore;
	private boolean isHideMyPictures;
	
	public Bitmap waitBitmap;		
	public ImageUseURIDownloader imageDownloader;			
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();
	public LruCache<String, Bitmap> mMemoryCache;  	
	public DisplayMetrics dm;
		
	public PhotobookThemeSelectionAdapter(Context context,int width,int height,LruCache<String, Bitmap> mMemoryCache,boolean isHideMyPictures){
		this.mContext = context;		
		this.width = width;
		this.height = height;
		this.isHideMyPictures = isHideMyPictures;
		app = RssTabletApp.getInstance();	
		if (isHideMyPictures) {
			size = app.getThemes().size()%2 == 0 ? app.getThemes().size()/2 : app.getThemes().size()/2 +1;
		}else {		
			size = (app.getThemes().size()+1)%2 == 0 ? (app.getThemes().size()+1)/2 :(app.getThemes().size()+1)/2 +1;	
		}		
		dm = context.getResources().getDisplayMetrics();
		imageDownloader = new ImageUseURIDownloader(context, pendingRequests, this);	
		this.imageDownloader.setSaveType(FilePathConstant.bookType);
		this.imageDownloader.setIsThumbnail(true);
		this.imageDownloader.setViewParameters(width, height);
		waitBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.image_wait234x156);       
        this.mMemoryCache = mMemoryCache;
	}

	@Override
	public int getCount() {		
		return size;		
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
		ThemeItemsHolder themeItemsHolder = null;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.photobook_theme_select_item, null,false);	
			themeItemsHolder = new ThemeItemsHolder();			
			themeItemsHolder.themeItemView_one = (ThemeItemView) convertView.findViewById(R.id.themeItem_one);	
			themeItemsHolder.themeItemView_two = (ThemeItemView) convertView.findViewById(R.id.themeItem_two);			
			convertView.setTag(themeItemsHolder);
		} else {
			themeItemsHolder = (ThemeItemsHolder) convertView.getTag();
		}
		
		if (position == 0 ) {			
			if (!isLoadZore) {	
				if (isHideMyPictures) {
					int pos = position;
					if (pos < app.getThemes().size()) {
						Theme theme = app.getThemes().get(pos);	
						themeItemsHolder.themeItemView_one.setValue((PhotoBooksThemeSelectActivity)mContext,theme,PhotobookThemeSelectionAdapter.this,pos);
					}
					pos = position+1;
					if (pos < app.getThemes().size()) {
						Theme theme = app.getThemes().get(pos);	
						themeItemsHolder.themeItemView_two.setValue((PhotoBooksThemeSelectActivity)mContext,theme,PhotobookThemeSelectionAdapter.this,pos);
					}						
				}else {
					themeItemsHolder.themeItemView_one.setValue((PhotoBooksThemeSelectActivity)mContext,null,PhotobookThemeSelectionAdapter.this,position);	
					int pos = position;
					if (app.getThemes() != null && pos < app.getThemes().size()) {
						themeItemsHolder.themeItemView_two.setVisibility(View.VISIBLE);
						Theme theme = app.getThemes().get(pos);	
						themeItemsHolder.themeItemView_two.setValue((PhotoBooksThemeSelectActivity)mContext,theme,PhotobookThemeSelectionAdapter.this,position+1);
					}else {
						themeItemsHolder.themeItemView_two.setVisibility(View.GONE);
					}								
				}
			}
			isLoadZore = true;
		}else {				
			isLoadZore = false;
			if (isHideMyPictures) {
				int pos = 2* position;
				if (pos < app.getThemes().size()) {
					Theme theme = app.getThemes().get(pos);	
					themeItemsHolder.themeItemView_one.setValue((PhotoBooksThemeSelectActivity)mContext,theme,PhotobookThemeSelectionAdapter.this,pos);		
				}								
				pos = 2*position+1;	
				if (pos < app.getThemes().size()) {
					themeItemsHolder.themeItemView_two.setVisibility(View.VISIBLE);
					Theme theme = app.getThemes().get(pos);	
					themeItemsHolder.themeItemView_two.setValue((PhotoBooksThemeSelectActivity)mContext,theme,PhotobookThemeSelectionAdapter.this,pos);	
				}else {
					themeItemsHolder.themeItemView_two.setVisibility(View.GONE);
				}		
			}else {
				int pos = 2* position - 1 ;
				if (pos < app.getThemes().size()) {
					Theme theme = app.getThemes().get(pos);	
					themeItemsHolder.themeItemView_one.setValue((PhotoBooksThemeSelectActivity)mContext,theme,PhotobookThemeSelectionAdapter.this,pos+1);	
				}
				pos = 2*position ;
				if (pos < app.getThemes().size()) {
					themeItemsHolder.themeItemView_two.setVisibility(View.VISIBLE);
					pos = 2*position ;
					Theme theme = app.getThemes().get(pos);	
					themeItemsHolder.themeItemView_two.setValue((PhotoBooksThemeSelectActivity)mContext,theme,PhotobookThemeSelectionAdapter.this,pos+1);
				}else {
					themeItemsHolder.themeItemView_two.setVisibility(View.GONE);
				}	
			}
		}
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width,height);
		layoutParams.setMargins(0, (int)dm.density*10, (int)dm.density*20, (int)dm.density*10);
		themeItemsHolder.themeItemView_one.setLayoutParams(layoutParams);		
		themeItemsHolder.themeItemView_two.setLayoutParams(layoutParams);			
		return convertView;
	}

	@Override
	public void onProcess(Response response, String profileId,View view, int position,String flowTpye,String productId) {			
		if (response == null || imageDownloader == null) return;									
		if (response.getError() != null) {
			
		}else{			
			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId,bitmap); 
			if (bitmap != null && view != null) {
				if (view instanceof ThemeItemContentView) {	
					if (position >= 1 && position <= 2 ) {
						notifyDataSetChanged();
					}else if (view.getVisibility() == View.VISIBLE) {
						view.postInvalidate();		
					}																														
				}					
			}
		}				
	}
	
	public void cancelRequest(){
		if (app.getThemes() == null) return;		
		for (int i = 0; i < app.getThemes().size(); i++) {
			Theme theme = app.getThemes().get(i); 
			if (theme == null) continue;
			if (theme.backGrounds == null) continue;
			for (int j = 0; j < theme.backGrounds.length; j++) {
				BackGround backGroud = theme.backGrounds[j];	
				dealRequest(backGroud, true);
			}			
		}		
	}
	
	private void dealRequest(BackGround backGroud,boolean isCancel){
		if (backGroud == null) return;
		if (imageDownloader == null) return;
		if (isCancel) {
			imageDownloader.cancelRequest(backGroud.id, null, 0);
		}else {
			imageDownloader.prioritizeRequest(backGroud.id, null, 0);
		}	
	}
	
	public Bitmap getBitmap(String backGroudId,URI pictureURI,View view,int position){
    	Bitmap mBitmap = MemoryCacheUtil.getBitmap(mMemoryCache, backGroudId);	        	        
        if (mBitmap == null) {      	
			if (waitBitmap ==null || waitBitmap.isRecycled()) {
				waitBitmap=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.image_wait234x156);
	 			}	    	    	        	 
			mBitmap = waitBitmap;
			view.setTag(backGroudId);
			imageDownloader.downloadProfilePicture(backGroudId, pictureURI, view,position,true);   	  	 						        	 	        	   	    	        	
		}
        return mBitmap;
    }
	
	class ThemeItemsHolder {		
		ThemeItemView themeItemView_one;
		ThemeItemView themeItemView_two;		
	}

}
