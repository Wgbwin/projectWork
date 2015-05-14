package com.kodak.rss.tablet.view;

import java.net.URI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.adapter.PhotoBooksProductMainAdapter;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.view.MainPageView.OnLayerClickListener;
import com.kodak.rss.tablet.view.PhotoBookMainPageView.OnPageSelectedListener;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class PhotoBookMainItemView extends LinearLayout{
	private static final String TAG = "PhotoBookMainItemView";
	
	private Context context;
    public PhotoBookMainPageView ivLeft;
    public PhotoBookMainPageView ivRight;
    private TextView indexLeft;
    private TextView indexRight;
    private ImageView ivLowResWarnLeft;
    private ImageView ivLowResWarnRight;
    private PhotoBooksProductActivity activity;
    private PhotoBooksProductMainAdapter adapter;
    
    private View viewClickLowResWarnLeft;
    private View viewClickLowResWarnRight;
    
	public PhotoBookMainItemView(Context context,PhotoBooksProductMainAdapter adapter) {
		super(context);
		this.context = context;
		this.adapter = adapter;		
		initView();	
	}	

	private  void initView(){			
		inflate(context,R.layout.photobook_main_page, this);	
		this.activity = (PhotoBooksProductActivity) context;
		
		ivLeft = (PhotoBookMainPageView) findViewById(R.id.image_left);
		ivRight = (PhotoBookMainPageView) findViewById(R.id.image_right);
		indexLeft =  (TextView) findViewById(R.id.index_left);
		indexRight =  (TextView) findViewById(R.id.index_right);
		ivLowResWarnLeft = (ImageView) findViewById(R.id.iv_low_res_warning_left);
		ivLowResWarnRight = (ImageView) findViewById(R.id.iv_low_res_warning_right);
		viewClickLowResWarnLeft = findViewById(R.id.view_click_low_res_left);
		viewClickLowResWarnRight = findViewById(R.id.view_click_low_res_right);
		
		viewClickLowResWarnLeft.setOnClickListener(lowResClickListner);
		viewClickLowResWarnRight.setOnClickListener(lowResClickListner);
	}
	
	private OnClickListener lowResClickListner = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			new InfoDialog.Builder(v.getContext()).setMessage(R.string.ComposePhotobook_LowResWarning)
										.setPositiveButton(R.string.d_ok, null)
										.create()
										.show();
		}
	};
	
	public void setBasicValue(PhotobookPage[] pageItem,int position,boolean isDuplex){
		setValue(indexLeft, ivLeft, pageItem[0], position, isDuplex, true);
		setValue(indexRight, ivRight, pageItem[1], position, isDuplex, false);
	}
			
	public void setOnPageSelectedListener(OnPageSelectedListener onPageClickListener) {
		ivLeft.setOnPageSelectedListener(onPageClickListener);
		ivRight.setOnPageSelectedListener(onPageClickListener);
	}

	public void setOnPageLayerClickListener(OnLayerClickListener<PhotoBookMainPageView, PhotobookPage, Layer> onLayerClickListener) {
		ivLeft.setOnLayerClickListener(onLayerClickListener);
		ivRight.setOnLayerClickListener(onLayerClickListener);
	}
	
	private void setValue(TextView tvIndex, PhotoBookMainPageView pv,PhotobookPage page,int position,boolean isDuplex,boolean isLeft){
		Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		tvIndex.setText(PhotoBookProductUtil.getPageIndexText(context,currentPhotoBook, page));
		//check low res warning
		boolean lowRes = false;
		if(page != null && page.layers!=null){
			for(int i=0;i<page.layers.length;i++){
				if(page.layers[i] != null && PhotoBookProductUtil.isLayerLowRes(page.layers[i])){
					lowRes = true;
					break;
				}
			}
		}
		if(isLeft){
			ivLowResWarnLeft.setVisibility(lowRes? View.VISIBLE : View.INVISIBLE);
			viewClickLowResWarnLeft.setVisibility(lowRes? View.VISIBLE : View.INVISIBLE);
		}
		else{
			ivLowResWarnRight.setVisibility(lowRes ? View.VISIBLE : View.INVISIBLE);
			viewClickLowResWarnRight.setVisibility(lowRes? View.VISIBLE : View.INVISIBLE);
		}
		
		pv.setBasicInfo(adapter, page, position, isLeft);
		
		if (page != null) {	
			double width = adapter.dm.widthPixels*0.55;
			double height = width*adapter.wHRatio;	
			if (adapter.imageDownloader.viewParameters == null) {
				adapter.imageDownloader.setViewParameters((int)width,(int)height);
			}
			URI pictureURI = PhotoBookProductUtil.getURI(page,(int)width,(int)height);			
			if (pictureURI != null) {
				Bitmap bitmap = getBitmapFromCache(page);
				pv.setTag(page.id);
				pv.setImageBitmap(bitmap == null ? getWaitBitmap() : bitmap);
				
				if(PhotoBookProductUtil.isTitlePage(page) && !adapter.isCanDownloadTitlepage()){
					Log.i(TAG, "Can't download title page now");
				}else if (page.isWantMainRefresh() || bitmap == null ) {					
					Log.d(TAG,"try download page image:" + page.id);					
					adapter.imageDownloader.downloadProfilePicture(page.id, pictureURI, pv,position,false,currentPhotoBook.id,page.getMainRefreshCount());										
				}
			}else {
				pv.setImageBitmap(null);  
			}		
		}else if(isLeft){			
			if (!isDuplex && position != 0) {
				if (adapter.leftBitmap == null || adapter.leftBitmap.isRecycled()) {
            		adapter.leftBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.photobook_6x4_page);
    			 }	
            	pv.setImageBitmap(adapter.leftBitmap);    	
			}
		}else{
			pv.setImageBitmap(null);
		}
	}
	
	private Bitmap getBitmapFromCache(PhotobookPage page){
		Bitmap bitmap = MemoryCacheUtil.getBitmap(adapter.mMemoryCache, page.id);
		if(bitmap == null){
			bitmap = getBitmapFromDiskCache(page);
		}		
		return bitmap;
	}
	
	private Bitmap getBitmapFromDiskCache(PhotobookPage page){
		String localPath =  FilePathConstant.getLoadFilePath(FilePathConstant.bookType, page.id, false,page.getMainRefreshCount(),page.getMainRefreshSucCount());
		if(localPath == null){
			return null;
		}
		
		int[] size = activity.pbLayout.getFlipViewController().getPageMaxSizeWhenNotZoomIn();							
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bitmap = null;
		try {
			bitmap = ImageUtil.getImageLocal(localPath, size[0],size[1],opts);			
			MemoryCacheUtil.putBitmap(adapter.mMemoryCache, page.id, bitmap);		
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			try{
				bitmap = ImageUtil.getImageLocal(localPath, size[0]/3,size[1]/3,opts);
			}catch (OutOfMemoryError e2){
				e2.printStackTrace();
			}
		}
		
		
		return bitmap;
	}
	
	private Bitmap getWaitBitmap(){
		if (adapter.waitBitmap == null || adapter.waitBitmap.isRecycled()) {
            adapter.waitBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.image_wait232x174);
    	}
		return adapter.waitBitmap;
	}


}
