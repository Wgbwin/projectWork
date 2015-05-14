package com.kodak.rss.tablet.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;

import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.view.PhotoBookPageView;
import com.kodak.rss.tablet.view.PhotoBookPagesItemView;

public class PhotoBooksProductPagesAdapter extends PhotoBooksProductAdapter{				
	public List<PhotobookPage> itemPages;
	public List<PhotobookPage> pages;
	private boolean hideItem = false;
	private PhotobookPage mDragPage;
	public boolean isDuplex = false;	
	
	public PhotoBooksProductPagesAdapter(Context context,Photobook photobook,float ratio,LruCache<String, Bitmap> mMemoryCache) {
		super(context,ratio,mMemoryCache);				
		this.mPhotobook = photobook;
		size = mPhotobook.pages.size();	
		isDuplex = mPhotobook.isDuplex;
		leftBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.photobook_6x4_page);				
		this.imageDownloader.setIsThumbnail(true);
		if (vlayoutParams != null) {
			this.imageDownloader.setViewParameters(vlayoutParams.width, vlayoutParams.height);
		}		
		pages = new ArrayList<PhotobookPage>(size);		
		for (int i = 0; i < size; i++) {
			PhotobookPage page = mPhotobook.pages.get(i);		
			pages.add(i, page);
		}		
		itemSize = getPages(pages); 
	}
	
	@Override
	public int getCount() {		
		return itemSize;
	}

	@Override
	public Object getItem(int position) {		
		return position;
	}

	@Override
	public long getItemId(int position) {		
		return position;
	}
	
	public void refresh(){
		refreshItem();
		notifyDataSetChanged();	
	}
	
	public void refreshItem(){
		mPhotobook = PhotoBookProductUtil.getCurrentPhotoBook();
		size = mPhotobook.pages.size();		
		pages = new ArrayList<PhotobookPage>(size);		
		for (int i = 0; i < size; i++) {
			PhotobookPage page = mPhotobook.pages.get(i);		
			pages.add(i, page);
		}	
		itemSize = getPages(pages); 		
	}
	
	public void hideDropItem(boolean hideItem, PhotobookPage page){
		this.hideItem = hideItem;
		this.mDragPage = page;
		notifyDataSetChanged();	
	}

	public void onChange(PhotobookPage toPage,PhotobookPage dragPage){		
		if (toPage == null || dragPage == null) return;
		this.mDragPage = dragPage;
		int fromPos = -1;
		int toPos = -1;		
		List<PhotobookPage> tempPages = new ArrayList<PhotobookPage>(size);		
		for (int i = 0; i < size; i++) {
			PhotobookPage page = pages.get(i);		
			tempPages.add(i, page);
			if (dragPage.id.equals(page.id)) {
				fromPos = i;				
			}
			if (toPage.id.equals(page.id)) {
				toPos = i;				
			}			
		}					
		if (fromPos == -1 || toPos == -1) return;
		
		if (fromPos > toPos) {	
			for (int j = toPos ; j <= fromPos; j++) {
				if (j == toPos) {
					tempPages.set(j, dragPage);
				}else {
					PhotobookPage page = pages.get(j-1);	
					tempPages.set(j, page);
				}
			}				
		}else if (fromPos< toPos){					
			for (int j = fromPos+1; j <= toPos; j++) {
				PhotobookPage page = pages.get(j);	
				tempPages.set(j-1, page);
				
				if (j == toPos) {
					tempPages.set(j, dragPage);
				}
			}			
		}		
		pages = tempPages;	
		itemSize = getPages(pages); 
		notifyDataSetChanged();		
	}	
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PhotoBookPagesItemView itemView = null;		
		if (convertView == null) {
			itemView =  new PhotoBookPagesItemView(mContext,PhotoBooksProductPagesAdapter.this);			
			convertView = itemView;
			convertView.setTag(itemView);
		} else {
			itemView = (PhotoBookPagesItemView) convertView.getTag();
		}
				
		PhotobookPage pageItem = itemPages.get(position);
		itemView.setBasicValue(pageItem,position,mPhotobook.isDuplex); 	

		if (hideItem && mDragPage != null) {			
			if (pageItem != null && pageItem.id.equals(mDragPage.id)) {			
				itemView.setBasicHideVisible(true,true);
			}else {
				itemView.setBasicHideVisible(true,false);
			}				
		}else {
			itemView.setBasicHideVisible(false,false);
		}		
		return convertView;
	}
	
	@Override
	public void onProcess(Response response, String profileId, View view,int position,String flowTpye,String productId) {			
		Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		if (currentPhotoBook == null || currentPhotoBook.pages == null) return;	
		if (productId != null && !productId.equals(currentPhotoBook.id)) return;	
		if (response == null || imageDownloader == null) return;		
		int refreshCount = response.getRequest().getRefreshCount();	
		MemoryCacheUtil.removeBitmap(mMemoryCache, profileId);	
		if (response.getError() != null) {

		} else {
			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);	
			if (bitmap != null) {	
				PhotoBookProductUtil.refreshSucPageInPhotobook(profileId, true, refreshCount);				
				if (view != null) {
					if (view instanceof PhotoBookPageView) {
						if (profileId.equals(view.getTag().toString())) {
							if (view.getVisibility() == View.VISIBLE) {
								((PhotoBookPageView) view).setImageBitmap(bitmap);
							}	
						}else {
							notifyDataSetChanged();
						}																
					}
				}else {
					notifyDataSetChanged();
				}
			}
		}		
	}
	
	@Override
	public void prioritizeViewRange() {
		if ((end_index < start_index)) return;  
		for (int i = start_index; i <= end_index; i++) {
			if (i < itemPages.size() -1) {
				PhotobookPage page = itemPages.get(i);       
				dealRequest(page, false,true);
			}	
		}	
	}
	
	private int getPages(List<PhotobookPage> pageList){
		int itemNum = 0;
		if (mPhotobook.isDuplex) {
			itemNum = pageList.size()+1;
			itemPages = new ArrayList<PhotobookPage>(itemNum);			
			for (int i = 0; i < itemNum; i++) {
				PhotobookPage page = null;
				if (i == 0) {
					page = null;
				}else {
					page = pageList.get(i-1);
				}					
				itemPages.add(i, page);
			}				
		}else {
			itemNum = (pageList.size() - 3)*2 +3;
			itemPages = new ArrayList<PhotobookPage>(itemNum);			
			for (int i = 0; i < itemNum; i++) {
				PhotobookPage page = null;
				if (i == 0) {
					page = null;
				}else if(i==1||i==2||i==3){
					page = pageList.get(i-1);
				}else if (i == itemNum-1 || i== itemNum-2) {
					page = pageList.get(pageList.size()+(i-itemNum));
				}else if (i % 2 == 0) {
					page = null;
				}else {
					int j = (i+1)/2;
					page = pageList.get(j);
				}
				itemPages.add(i, page);
			}						
		}
		return itemNum;
	}	
	
}
