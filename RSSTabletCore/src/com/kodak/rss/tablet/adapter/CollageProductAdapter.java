package com.kodak.rss.tablet.adapter;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;

public abstract class CollageProductAdapter extends BaseAdapter implements onProcessImageResponseListener{

	public Context mContext;	
	public LayoutInflater mInflater;	
	public Bitmap waitBitmap;
	
	public DisplayMetrics dm;
	public ImageUseURIDownloader imageDownloader;	
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();
	public int itemSize;
	public float wHRatio;
	public int pageWidth,pageHeight;
	
	public RelativeLayout.LayoutParams mLayoutParams;
	public LruCache<String, Bitmap> mMemoryCache;
	
	public int start_index = 0;	
	public int end_index = 0;
	public boolean lock = false;	
	public Collage currentCollage;
	
	public CollageProductAdapter(Context context,float ratio,LruCache<String, Bitmap> mMemoryCache){		
		this.mContext = context;
		this.mMemoryCache = mMemoryCache;
		this.wHRatio  = ratio;		
		this.mInflater = LayoutInflater.from(context);
		this.imageDownloader = new ImageUseURIDownloader(context, pendingRequests, this);
		this.imageDownloader.setSaveType(FilePathConstant.collageType);	
		this.waitBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.imagewait60x60);
		dm =  context.getResources().getDisplayMetrics();	
		pageWidth = (int) ((dm.widthPixels - dm.density*50)/8f);				
		pageHeight = (int) (pageWidth*ratio);				
		this.mLayoutParams = new RelativeLayout.LayoutParams(pageWidth,pageHeight);	
		currentCollage = CollageUtil.getCurrentCollage();
	}

	@Override
	public void onProcess(Response response, String profileId, View view,int position,String flowTpye, String productId) {}	

	public void prioritizeViewRange(){			
		lock = false;	
		notifyDataSetChanged();
	}	
	
}
