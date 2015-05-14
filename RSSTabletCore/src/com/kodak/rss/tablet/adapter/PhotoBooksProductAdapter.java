package com.kodak.rss.tablet.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.bean.PhotoLocationPo;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageDownloader;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;

public abstract class PhotoBooksProductAdapter extends BaseAdapter implements onProcessImageResponseListener{

	public Context mContext;	
	public PhotoLocationPo selectedPo;
	public PhotoBooksProductActivity activity;
	public LayoutInflater mInflater;	
	public Bitmap waitBitmap;
	public List<PhotobookPage> mPages;	
	public DisplayMetrics dm;
	public ImageUseURIDownloader imageDownloader;	
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();
	public Photobook mPhotobook;
	public int itemSize;
	public int size;
	public float wHRatio;
	public Bitmap leftBitmap;	
	public int pageWidth,pageHeight,txtHeight;
	public LinearLayout.LayoutParams vlayoutParams;
	public RelativeLayout.LayoutParams mLayoutParams;
	public LinearLayout.LayoutParams txtlayoutParams;	
	public Bitmap selectedBitmap;
	public LinearLayout.LayoutParams imagelayoutParams;	
	public LinearLayout.LayoutParams gaplayoutParams;	
	public int defaultImageWidth;	
	public int[] selectedPostions = new int[2] ;	
	public Bitmap leftShadowBitmap;
	public Bitmap rightShadowBitmap;
	
	public LruCache<String, Bitmap> mMemoryCache;
	
	public int start_index = 0;	
	public int end_index = 0;
	public boolean lock = false;	
	
	public PhotoBooksProductAdapter(Context context,float ratio,LruCache<String, Bitmap> mMemoryCache){
		this.mContext = context;
		this.mMemoryCache = mMemoryCache;
		this.wHRatio  = ratio;		
		this.activity = (PhotoBooksProductActivity) context;
		this.dm = context.getResources().getDisplayMetrics();	
		this.mInflater = LayoutInflater.from(context);
		this.imageDownloader = new ImageUseURIDownloader(context, pendingRequests, this);
		this.imageDownloader.setSaveType(FilePathConstant.bookType);	
		this.waitBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.imagewait60x60);				
		pageWidth = (int) ((dm.widthPixels - dm.density*50)/8f);		
		defaultImageWidth = pageWidth;		
		pageHeight = (int) (pageWidth*ratio);				
		txtHeight = (int) (dm.density*20);	
		this.vlayoutParams = new LinearLayout.LayoutParams(pageWidth,pageHeight);	
		this.mLayoutParams = new RelativeLayout.LayoutParams(pageWidth,pageHeight);	
		this.txtlayoutParams = new LinearLayout.LayoutParams(pageWidth,txtHeight);	
		this.gaplayoutParams = new LinearLayout.LayoutParams(pageWidth/2,pageHeight+txtHeight);		
		
		selectedPostions[0] = -1;
		selectedPostions[1] = -1;

		selectedBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.edit_preview_sel_xxhdpi);
		
		leftShadowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.book_insideshadow_left_xxhdpi);
		rightShadowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.book_insideshadow_right_xxhdpi);
	}

	@Override
	public void onProcess(Response response, String profileId, View view,int position,String flowTpye, String productId) {}	

	public void prioritizeViewRange(){			
		lock = false;	
		notifyDataSetChanged();
	}	
	
	public void cancelRequest(boolean isThumbnail){
		Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		if (currentPhotoBook.pages == null) return;		
		for (int j = 0; j < currentPhotoBook.pages.size(); j++) {
			PhotobookPage page = currentPhotoBook.pages.get(j);       
			dealRequest(page, true,isThumbnail);
		}		
	}		
	
	public void prioritizeSingleRequest(PhotobookPage page,boolean isThumbnail){		      
		dealRequest(page, false,isThumbnail);		
	}
	
	public void dealRequest(PhotobookPage page,boolean isCancel,boolean isThumbnail){
		if (page == null) return;
		String pageId = page.id;
		if (pageId == null) return;		
		if("".equals(pageId)) return;
		int refreshCount = 0;		
		String pendKey = pageId;
		if (isThumbnail) {
			refreshCount = page.getThumbRefreshCount();			
			pendKey = FilePathConstant.thumbnail+ pageId;
		} else {
			refreshCount = page.getMainRefreshCount();			
		}
		Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		if (currentPhotoBook != null && currentPhotoBook.id != null) {
			pendKey = currentPhotoBook.id + pendKey;
		}		
		if (refreshCount > 0) {
			for (int i = 0; i < refreshCount; i++) {
				String tmpPendKey = null;
				if (i == 0) {
					tmpPendKey = pendKey;
				}else {
					tmpPendKey = i + pendKey;
				}
				Request request = pendingRequests.get(tmpPendKey);
				if (request != null) {
					ImageDownloader.cancelRequest(request);
					pendingRequests.remove(tmpPendKey);
				}	
			}			
			pendKey = refreshCount + pendKey;
		}
        Request request = pendingRequests.get(pendKey);
        if (request != null) {
        	if (isCancel) {
        		ImageDownloader.cancelRequest(request);        		
        		pendingRequests.remove(pendKey);						      				           	
			}else {
				ImageDownloader.prioritizeRequest(request);
			}          	
        }        
	}

	public void cancelRequest(Layer layer){
		if (layer == null) return;
		if (mPhotobook == null) return;
		imageDownloader.cancelRequest(layer.contentId, mPhotobook.id, 0);	
	}
	
}
