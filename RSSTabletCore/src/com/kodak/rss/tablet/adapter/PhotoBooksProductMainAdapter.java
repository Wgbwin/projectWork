package com.kodak.rss.tablet.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.v4.util.LruCache;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.view.MainPageView.OnLayerClickListener;
import com.kodak.rss.tablet.view.PhotoBookEditLayer;
import com.kodak.rss.tablet.view.PhotoBookMainItemView;
import com.kodak.rss.tablet.view.PhotoBookMainPageView;
import com.kodak.rss.tablet.view.PhotoBookMainPageView.OnPageSelectedListener;

/**
 * @author Robin
 *
 */
public class PhotoBooksProductMainAdapter extends PhotoBooksProductAdapter{
	private final static String TAG = "PhotoBookAdapter";
	
	private PhotoBookEditLayer layerEdit;
	private ArrayList<PhotobookPage[]> pageItems;
	private Point screenSize;
	
	//if true, flipView will call notifydatasetchaned after flipped
	public boolean isNeedNotifyAfterFlipped = false;
	
	private SparseArray<WeakReference<PhotoBookMainItemView>> cachedViews;	
	public Bitmap leftShadowBitmap;
	public Bitmap rightShadowBitmap;
	public Bitmap leftPageEdgeBitmap;
	public Bitmap rightPageEdgeBitmap;
	
	public PhotoBooksProductMainAdapter(Context context, Photobook photobook, float wHRation, final PhotoBookEditLayer layerEdit,LruCache<String, Bitmap> mMemoryCache) {
		super(context,wHRation,mMemoryCache);				
		this.mPhotobook = photobook;
		this.pageItems = PhotoBookProductUtil.getPageItems(photobook);
		this.layerEdit = layerEdit;						
		this.imageDownloader.setIsThumbnail(false);				
		leftBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.photobook_6x4_page);
		leftShadowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.book_insideshadow_left_xxhdpi);
		rightShadowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.book_insideshadow_right_xxhdpi);
		leftPageEdgeBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftpage_edge_xxhdpi);
		rightPageEdgeBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightpage_edge_xxhdpi);
		waitBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.image_wait232x174);
		screenSize = new Point();
		((Activity)mContext).getWindowManager().getDefaultDisplay().getSize(screenSize);
		
		cachedViews = new SparseArray<WeakReference<PhotoBookMainItemView>>();
	}
	

	@Override
	public int getCount() {
		return pageItems.size();
	}

	@Override
	public PhotobookPage[] getItem(int position) {
		return pageItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View cv, ViewGroup parent) {
		PhotoBookMainItemView itemView = null;
		if(true){//stop reuse will avoid many bugs
			itemView = new PhotoBookMainItemView(mContext, this);
			cv = itemView;
			cv.setTag(itemView);
		}else{
			itemView = (PhotoBookMainItemView) cv.getTag();
		}
		
		itemView.setBasicValue(pageItems.get(position), position, mPhotobook.isDuplex);
		itemView.setOnPageSelectedListener(new OnPageSelectedListener() {
			
			@Override
			public void onPageSelected(PhotoBookMainPageView pageView, PhotobookPage page) {
				if(PhotoBookProductUtil.getPhotobookPageEditable(page)){
					layerEdit.showPageEditPop(pageView, page);
				}
			}
		});
		itemView.setOnPageLayerClickListener(new OnLayerClickListener<PhotoBookMainPageView, PhotobookPage, Layer>() {
			
			@Override
			public void onLayerClick(PhotoBookMainPageView pageView, PhotobookPage page, Layer layer,
					RectF layerRect) {
				if(PhotoBookProductUtil.isLayerEditable(layer)){
					layerEdit.showEditImageAndPop(pageView, layer, layerRect);
				}
			}
		});
		cachedViews.put(position, new WeakReference<PhotoBookMainItemView>(itemView));
		return cv;
	}
	
	@Override
	public void notifyDataSetChanged() {
		isNeedNotifyAfterFlipped = false;
		mPhotobook = PhotoBookProductUtil.getCurrentPhotoBook();
		pageItems = PhotoBookProductUtil.getPageItems(mPhotobook);
		super.notifyDataSetChanged();
		((PhotoBooksProductActivity)mContext).pbLayout.refreshAllPages();
	}
	
	/**
	 * if we call notifyDataSetChanged() when flipping , the flipview will flash
	 */
	public void postNotifyDataSetChanged() {
		if(activity.pbLayout.getFlipViewController().isInFlipAnimation()){
			//if set ture, flipview will call notifyDataSetChanged() after flipped 
			isNeedNotifyAfterFlipped = true;
		}else{
			notifyDataSetChanged();
		}
	}
	
	@Override
	public void onProcess(Response response, String profileId, View view,int position,String flowTpye,String productId) {			
		Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		if (currentPhotoBook == null || currentPhotoBook.pages == null) return;	
		if (productId != null && !productId.equals(currentPhotoBook.id)) return;
		if (response == null || imageDownloader == null) return;			
		int refreshCount = response.getRequest().getRefreshCount();
		MemoryCacheUtil.removeBitmap(mMemoryCache,profileId);					
		if (response.getError() != null) {

		} else {
			Bitmap bitmap = response.getBitmap();
			if (bitmap != null) {
				PhotoBookProductUtil.refreshSucPageInPhotobook(profileId, false, refreshCount);
				MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);					
				if (view != null) {
					//TODO: page always disordered, so comment and use notifydatasetchanged now.
//					if (view instanceof PhotoBookMainPageView) {
//							if(profileId.equals(view.getTag())){
//								PhotoBookMainPageView pv = (PhotoBookMainPageView) view;
//								pv.setImageBitmap(bitmap);
//								pv.invalidate();
//								((PhotoBooksProductActivity)mContext).pbLayout.refreshAllPages();
//							}else{
//								postNotifyDataSetChanged();
//							}
//					}else {
//						postNotifyDataSetChanged();
//					}
					postNotifyDataSetChanged();
				}else{
					postNotifyDataSetChanged();
				}
				
			}
		}
	}
	
	/**
	 * Find view from cache. It works well when you want to get the current view.
	 * If you want to get the other view, it may return null.
	 * @param position
	 * @return
	 */
	public PhotoBookMainItemView getViewByPosition(int position){
		WeakReference<PhotoBookMainItemView> viewRef = cachedViews.get(position);
		if(viewRef != null){
			return viewRef.get();
		}
		return null;
	}
	
	int count = 0;
	public boolean isCanDownloadTitlepage(){
		//add count to avoid activity.canDownloadTitlePage always be false(if there is some bug)
		count ++;
		try{
			PhotoBooksProductActivity activity = (PhotoBooksProductActivity) mContext;
			if(activity.canDownloadTitlePage || count>3){
				return true;
			}
			
			return false;
		}catch(Exception e){
			Log.e(TAG,e);
		}
		
		return true;
	}

}
