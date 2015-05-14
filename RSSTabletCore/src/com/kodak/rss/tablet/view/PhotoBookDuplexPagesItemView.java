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
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;

public class PhotoBookDuplexPagesItemView extends LinearLayout{
	private Context mContext;  
    private PhotoBookPageView leftImageView;
    private PhotoBookPageView rightImageView;
    private TextView leftPositionText;
    private TextView rightPositionText;	
    private PhotoBooksProductAdapter adapter;
    
	public PhotoBookDuplexPagesItemView(Context context,PhotoBooksProductAdapter mAdapter) {
		super(context);		
		this.adapter = mAdapter;		
		initView(context);	
	}	

	public PhotoBookDuplexPagesItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public PhotoBookDuplexPagesItemView(Context context) {
		super(context);		
	}

	private  void initView(Context context){
		inflate(context,R.layout.photobooks_duplex_page_item, this);	
		this.mContext = context;		
					
		leftImageView = (PhotoBookPageView) findViewById(R.id.leftImage);
		rightImageView = (PhotoBookPageView) findViewById(R.id.rightImage);
		leftPositionText =  (TextView) findViewById(R.id.left_position);
		rightPositionText =  (TextView) findViewById(R.id.right_position);
		
		if (adapter != null) {
			leftImageView.setLayoutParams(adapter.vlayoutParams);		
			rightImageView.setLayoutParams(adapter.vlayoutParams);									
			leftPositionText.setLayoutParams(adapter.txtlayoutParams);
			leftPositionText.setGravity(Gravity.CENTER);
			rightPositionText.setLayoutParams(adapter.txtlayoutParams);
			rightPositionText.setGravity(Gravity.CENTER);		
		}		
	}
	
	public void setAdpter(PhotoBooksProductAdapter mAdapter){
		adapter = mAdapter;		
		leftImageView.setLayoutParams(adapter.vlayoutParams);		
		rightImageView.setLayoutParams(adapter.vlayoutParams);									
		leftPositionText.setLayoutParams(adapter.txtlayoutParams);
		leftPositionText.setGravity(Gravity.CENTER);
		rightPositionText.setLayoutParams(adapter.txtlayoutParams);
		rightPositionText.setGravity(Gravity.CENTER);		
	}		
			
	public void setBasicValue(PhotobookPage[] pageItem,int position,boolean isDuplex){			
		leftImageView.setVisibility(View.VISIBLE);
		leftPositionText.setVisibility(View.VISIBLE);
		rightImageView.setVisibility(View.VISIBLE);
		rightPositionText.setVisibility(View.VISIBLE);		
		setLeftValue(pageItem[0], position, isDuplex);
		setRightValue(pageItem[1], position, isDuplex);
	}
	
	public void setLeftValue(PhotobookPage page,int position,boolean isDuplex){		
		Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		leftPositionText.setText(PhotoBookProductUtil.getPageIndexText(mContext, currentPhotoBook, page));							
		leftImageView.setBasicInfo(adapter, position,true);
		if (page != null) {	
			URI pictureURI = PhotoBookProductUtil.getURI(page,adapter.vlayoutParams.width, adapter.vlayoutParams.height);			
			if (pictureURI != null) {
				String pageId = page.id;
				Bitmap bitmap = getBitmapFromCache(page);
				leftImageView.setTag(pageId);					
				leftImageView.setImageBitmap(bitmap == null ? getWaitBitmap() : bitmap); 
				if (page.isWantThumbRefresh() || bitmap == null ) {														
					adapter.imageDownloader.downloadProfilePicture(pageId, pictureURI, leftImageView,position,false,currentPhotoBook.id,page.getThumbRefreshCount());											
				}						
			}else {
				leftImageView.setImageBitmap(null);   
			}		
		}else {
			leftImageView.setImageBitmap(null);   
		}
	}
	
	public void setRightValue(PhotobookPage page,int position,boolean isDuplex){
		Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		rightPositionText.setText(PhotoBookProductUtil.getPageIndexText(mContext, currentPhotoBook, page));								
		rightImageView.setBasicInfo(adapter, position,false);
		if (page != null) {				
			URI pictureURI = PhotoBookProductUtil.getURI(page,adapter.vlayoutParams.width, adapter.vlayoutParams.height);			
			if (pictureURI != null) {
				String pageId = page.id;
				Bitmap bitmap = getBitmapFromCache(page);
				rightImageView.setTag(pageId);					
				rightImageView.setImageBitmap(bitmap == null ? getWaitBitmap() : bitmap); 
				if (page.isWantThumbRefresh() || bitmap == null ) {														
					adapter.imageDownloader.downloadProfilePicture(pageId, pictureURI, rightImageView,position,false,currentPhotoBook.id,page.getThumbRefreshCount());											
				}								
			}else {
				rightImageView.setImageBitmap(null); 
			}										
		}else {
			rightImageView.setImageBitmap(null); 	     
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
