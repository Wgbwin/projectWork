package com.kodak.rss.tablet.adapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.OnProcessResponseEndListener;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.view.PhotoBookPageView;
import com.kodak.rss.tablet.view.PhotoBookPagesItemView;
import com.kodak.rss.tablet.view.SelectImageView;

public class PhotoBooksProductRearrangeSimplexAdapter extends PhotoBooksProductAdapter {			
	
	public int totelWidth,imageWidth,rearrangeHeadHeight;			
	private int maxNum;
	private LinearLayout.LayoutParams sParams;
	
	public PhotoBooksProductRearrangeSimplexAdapter(Context context,float ratio,LruCache<String, Bitmap> mMemoryCache) {
		super(context,ratio,mMemoryCache);				
		this.mPhotobook = PhotoBookProductUtil.getCurrentPhotoBook();		
		size = mPhotobook.pages.size();
		this.imageDownloader.setIsThumbnail(true);		
		rearrangeHeadHeight = pageHeight+ txtHeight;
		totelWidth = dm.widthPixels - 2*pageWidth ;			
		sParams = new LayoutParams(LayoutParams.MATCH_PARENT, rearrangeHeadHeight);				
	}
	
	@Override
	public int getCount() {	
		mPhotobook = PhotoBookProductUtil.getCurrentPhotoBook();
		if (size != mPhotobook.pages.size()) {
			activity.simplexPages  = PhotoBookProductUtil.getSimplexPage(mPhotobook.pages);
		}	
		itemSize =  activity.simplexPages.size();
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {				
		PhotoBooksProductRearrangeHolder photosViewHolder = null;		
		if (convertView == null) {	
			convertView = mInflater.inflate(R.layout.photobook_rearrange_simplex_item, null);
			photosViewHolder = new PhotoBooksProductRearrangeHolder();									
			photosViewHolder.pageImage = (PhotoBookPagesItemView) convertView.findViewById(R.id.l_page_image);
			photosViewHolder.lSpaceView = convertView.findViewById(R.id.l_space);
			photosViewHolder.contentLayout = (LinearLayout) convertView.findViewById(R.id.l_page_content);				
			convertView.setTag(photosViewHolder);		
		}else {
			photosViewHolder = (PhotoBooksProductRearrangeHolder) convertView.getTag();
		} 	
		if (imagelayoutParams == null || maxNum != activity.maxNumberOfImages) {
			maxNum = activity.maxNumberOfImages;
			imageWidth  = PhotoBookProductUtil.getViewWidth(defaultImageWidth,maxNum, totelWidth, (int)dm.density*10);		
			imagelayoutParams = new LinearLayout.LayoutParams(imageWidth,(int) (rearrangeHeadHeight- dm.density*10));	
			imagelayoutParams.setMargins((int)dm.density*10, 0, 0, 0);		
		}

		if (position == 0) {
			photosViewHolder.lSpaceView.setVisibility(View.VISIBLE);			
			photosViewHolder.lSpaceView.setLayoutParams(sParams);
		}else {
			photosViewHolder.lSpaceView.setVisibility(View.GONE);			
		}
		photosViewHolder.contentLayout.removeAllViews();
		
		if (end_index > itemSize - 1) {
			end_index = itemSize - 1;
		}	

		photosViewHolder.pageImage.setAdpter(PhotoBooksProductRearrangeSimplexAdapter.this);
		PhotobookPage page = activity.simplexPages.get(position);
		photosViewHolder.pageImage.setBasicValue(page, position, false);		

		ArrayList<Layer> layerList = PhotoBookProductUtil.getImageTypeLayers(page.layers);
		int lenght = layerList.size();
		for (int i = 0; i < lenght; i++) {
			SelectImageView imageView = new SelectImageView(mContext);						
			photosViewHolder.contentLayout.addView(imageView, i, imagelayoutParams);
		}		
			
		for(int j = 0; j < lenght; j++ ) {
			Layer layer = layerList.get(j);		
			SelectImageView iView = ((SelectImageView) photosViewHolder.contentLayout.getChildAt(j));
				
			ImageInfo pBImageInfo = PhotoBookProductUtil.getLayerImageInfo(layer,mPhotobook.chosenpics);
			Layer pBLayer =  PhotoBookProductUtil.getLayerInfo(layer, mPhotobook.chosenLayers);				
			if (pBLayer != null) {
				URI pictureURI = PhotoBookProductUtil.getURI(pBLayer,imagelayoutParams.width,imagelayoutParams.height);
				if (pictureURI == null) {
					iView.setTag(j);					
					iView.setImageBitmap(null); 
				}else {
					String layerId = pBLayer.contentId;
					Bitmap mBitmap = MemoryCacheUtil.getBitmap(mMemoryCache, layerId);							     					           	            	
					iView.setTag(layerId);
					if (selectedPo != null && selectedPo.layer.contentId.equals(layer.contentId)){
						iView.setImageBitmap(null);
					}else {
						iView.setImageBitmap(mBitmap == null ? getWaitBitmap() : mBitmap); 
					}					
					if (isNeedDown(position) && mBitmap == null ) {					
						imageDownloader.downloadProfilePicture(layerId, pictureURI, iView, position, true, mPhotobook.id);
					}								
				}
			}else if (pBImageInfo != null) {
				String infoId = pBImageInfo.id;
				Bitmap mBitmap = MemoryCacheUtil.getBitmap(mMemoryCache, infoId);	
				iView.setTag(infoId);
				if (selectedPo != null && selectedPo.layer.contentId.equals(layer.contentId)) {
					iView.setImageBitmap(null);
				}else {
					iView.setImageBitmap(mBitmap == null ? getWaitBitmap() : mBitmap);	
				}
				if (isNeedDown(position) && mBitmap == null ) {					
					if (pBImageInfo.isfromNative) {									
						imageDownloader.downloadProfilePicture(infoId, null, iView, j);															
					}else {							
						String thumbnailPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, infoId, true);
						if (thumbnailPath != null) {
							imageDownloader.downloadProfilePicture(infoId, thumbnailPath, iView, j);			
						}else {
							URI pictureURI = null ;			
							try {
								pictureURI = new URI(pBImageInfo.downloadThumbnailUrl);
						    } catch (URISyntaxException e) {
						    	pictureURI = null;
						    }  
							if (pictureURI != null) {
								RssTabletApp.getInstance().imageDownloader.downloadProfilePicture(pBImageInfo.id, pictureURI, iView, position, true,true,FilePathConstant.bookType,mPhotobook.id);
							    RssTabletApp.getInstance().setOnProcessResponseEndListener(new OnProcessResponseEndListener() {						
									@Override
									public void onProcessEnd(ImageInfo imageInfo,boolean isEdit) {
										if (isEdit) return;
											notifyDataSetChanged();						
									}
								});			
							}				
						}			
					}
				}								
			}							
		}				  					
		return convertView;
	}
	
	private boolean isNeedDown(int position){
		boolean isNeed = false;
		if (lock) return isNeed;
		if (((start_index > 0 && end_index > 0) && start_index <= position && end_index >= position) ||(start_index == 0 && end_index >= 0)) {								
			isNeed = true;			
		}
		return isNeed;
	}
	
	
	private Bitmap getWaitBitmap(){
		if (waitBitmap == null || waitBitmap.isRecycled()) {
            waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);
    	}
		return waitBitmap;
	}
	
	@Override
	public void onProcess(Response response, String profileId, View view,int position,String flowTpye, String productId) {			
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
					if (view instanceof SelectImageView ) {
						if (profileId.equals(view.getTag().toString())) {
							if (view.getVisibility() == View.VISIBLE) {
								((SelectImageView) view).setImageBitmap(bitmap);
							}	
						}							
					} else if (view instanceof PhotoBookPageView) {
						notifyDataSetChanged();
					}
				}
			}
		}
	}
	
	public void cancelRequest(){
		if (imageDownloader == null) return;
		super.cancelRequest(false);
		if (mPhotobook.chosenLayers == null) return;		
		for (int j = 0; j < mPhotobook.chosenLayers.size(); j++) {
			Layer layer = mPhotobook.chosenLayers.get(j);       
			cancelRequest(layer);
		}	
		for (int j = 0; j < mPhotobook.chosenpics.size(); j++) {
			ImageInfo info = mPhotobook.chosenpics.get(j);			
			cancelRequest(info);			
		}			
	}		
	
	private void cancelRequest(ImageInfo info){
		if (info == null) return;
		String infoId = info.id;		
		imageDownloader.cancelRequest(infoId, null, 0);		
	}
	
}

class PhotoBooksProductRearrangeHolder {		
	PhotoBookPagesItemView pageImage;
	LinearLayout contentLayout;	
	View lSpaceView;
}

