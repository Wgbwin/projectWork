package com.kodak.rss.tablet.view;

import java.net.URI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.PhotoBooksProductAdapter;
import com.kodak.rss.tablet.adapter.PhotoBooksProductRearrangeSimplexAdapter;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;

/**
 * Purpose: 
 * Author: Bing Wang 
 */
public class PhotoBookPagesItemView extends LinearLayout{   
    private Context mContext;    
    private PhotoBookPageView imageView;    
    private TextView positionText;	
    private View leftGapView; //leftGap
    private View rightGapView; //rightGap
    private PhotoBooksProductAdapter adapter;
    
	public PhotoBookPagesItemView(Context context,PhotoBooksProductAdapter mAdapter) {
		super(context);		
		this.adapter = mAdapter;		
		initView(context);	
	}	

	public PhotoBookPagesItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public PhotoBookPagesItemView(Context context) {
		super(context);		
	}

	private  void initView(Context context){
		inflate(context,R.layout.photobooks_page_item, this);	
		this.mContext = context;		
					
		imageView = (PhotoBookPageView) findViewById(R.id.image);	
		positionText =  (TextView) findViewById(R.id.position);
		leftGapView = findViewById(R.id.leftGap);
		rightGapView = findViewById(R.id.rightGap);

		if (adapter != null) {					
			imageView.setLayoutParams(adapter.vlayoutParams);												
			positionText.setLayoutParams(adapter.txtlayoutParams);
			positionText.setGravity(Gravity.CENTER);			
		}		
	}
	
	public void setAdpter(PhotoBooksProductAdapter mAdapter){
		adapter = mAdapter;		
		imageView.setLayoutParams(adapter.vlayoutParams);												
		positionText.setLayoutParams(adapter.txtlayoutParams);
		positionText.setGravity(Gravity.CENTER);
		
		if (adapter instanceof PhotoBooksProductRearrangeSimplexAdapter) {			
			leftGapView.setLayoutParams(adapter.gaplayoutParams);	
			rightGapView.setLayoutParams(adapter.gaplayoutParams);
			leftGapView.setVisibility(View.INVISIBLE);
			rightGapView.setVisibility(View.INVISIBLE);
		}
			
	}	
	
	public void setBasicHideVisible(boolean isHide, boolean isInVisible){
		imageView.setHideHight(isHide,false);
		if (isInVisible) {
			imageView.setVisibility(View.INVISIBLE);			
			positionText.setVisibility(View.INVISIBLE);			
		}else {
			imageView.setVisibility(View.VISIBLE);			
			positionText.setVisibility(View.VISIBLE);						
		}	
	}
			
	public void setBasicValue(PhotobookPage page,int position,boolean isDuplex){
		Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		imageView.setVisibility(View.VISIBLE);
		positionText.setVisibility(View.VISIBLE);	
		positionText.setText(PhotoBookProductUtil.getPageIndexText(mContext, currentPhotoBook, page));
		
		if (adapter instanceof PhotoBooksProductRearrangeSimplexAdapter) {
			imageView.setBasicInfo(adapter, position, true);
		}else {
			if (position % 2 == 0) {
				imageView.setBasicInfo(adapter, position, true);
				leftGapView.setVisibility(View.VISIBLE);
				rightGapView.setVisibility(View.GONE);
			}else {			
				imageView.setBasicInfo(adapter, position, false);			
				leftGapView.setVisibility(View.GONE);
				rightGapView.setVisibility(View.VISIBLE);
			}		
		}

		if (page != null) {	
			URI pictureURI = PhotoBookProductUtil.getURI(page, adapter.vlayoutParams.width,adapter.vlayoutParams.height);			
			if (pictureURI != null) {
				String pageId = page.id;
				Bitmap bitmap = getBitmapFromCache(page);
				imageView.setTag(pageId);					
				imageView.setImageBitmap(bitmap == null ? getWaitBitmap() : bitmap); 	
				if (page.isWantThumbRefresh() || bitmap == null ) {														
					adapter.imageDownloader.downloadProfilePicture(pageId, pictureURI, imageView,position,false,currentPhotoBook.id,page.getThumbRefreshCount());											
				}
			}else {
				imageView.setImageBitmap(null);   
			}		
		}else {			
			if (!isDuplex && position != 0) {
				if (adapter.leftBitmap == null || adapter.leftBitmap.isRecycled()) {
            		adapter.leftBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.photobook_6x4_page);
    			 }	
				imageView.setImageBitmap(adapter.leftBitmap);    	
			}else {
				imageView.setImageBitmap(null); 
			}
		}
	}
	
	private Bitmap getBitmapFromCache(PhotobookPage page){		
		Bitmap bitmap = MemoryCacheUtil.getBitmap(adapter.mMemoryCache, page.id);
		if(bitmap == null){
			bitmap = directUseUrlNative(page);
		}		
		return bitmap;
	}
	
	private Bitmap directUseUrlNative(PhotobookPage page){
		if (page == null) return null;
		String pageId = page.id;
		String dispalyPath =  FilePathConstant.getLoadFilePath(FilePathConstant.bookType, pageId, true,page.getThumbRefreshCount(),page.getThumbRefreshSucCount());	
		if (dispalyPath == null) return null;
		Bitmap bitmap = BitmapFactory.decodeFile(dispalyPath);	
		MemoryCacheUtil.putBitmap(adapter.mMemoryCache, pageId, bitmap);		
		return bitmap;
	}
	
	private Bitmap getWaitBitmap(){
		if (adapter.waitBitmap == null || adapter.waitBitmap.isRecycled()) {
            adapter.waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);
    	}
		return adapter.waitBitmap;
	}

}
