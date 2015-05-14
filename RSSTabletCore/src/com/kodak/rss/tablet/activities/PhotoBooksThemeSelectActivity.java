package com.kodak.rss.tablet.activities;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.util.DeviceInfoUtil;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.AppManager;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.PhotobookThemeSelectionAdapter;
import com.kodak.rss.tablet.thread.SetPhotoBookParamsTask;
import com.kodak.rss.tablet.thread.UploadImagesTask;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.ShoppingCartUtil;
import com.kodak.rss.tablet.util.UploadProgressUtil;
import com.kodak.rss.tablet.view.HorizontalListView;
import com.kodak.rss.tablet.view.ThemeItemView;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class PhotoBooksThemeSelectActivity extends BaseNetActivity implements OnClickListener{	
	
	public HorizontalListView mHListView;		
	private boolean isFromProduct = false;
	private float wHRatio;	
	private boolean isFromMyProject = false;	
	public boolean isExistShowPicDialog;
	public PhotobookThemeSelectionAdapter adapter;	
	public Theme curSelectedTheme = null;
	public int curSelectedPosition = -1;
	public ThemeItemView preItemView;
	RssTabletApp app;	
	private TextView selectPropmtText;	
	private Button previousButton;
	private Button cartButton;
	private Photobook currentPhotoBook;
	public LruCache<String, Bitmap> mMemoryCache; 	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);								
		setContentView(R.layout.activity_theme_select_photo_books);	
		if (getIntent() != null) {				
			isFromProduct = getIntent().getBooleanExtra(AppConstants.isFromPhotoBookProduct, false);
			wHRatio = getIntent().getFloatExtra("wHRatio", 0);
			isFromMyProject = getIntent().getBooleanExtra(AppConstants.isFromMyProject, false);			
		}
		int ratio = 16; 
		if (isFromProduct) {
			ratio = ratio/2;
		} 
		mMemoryCache = MemoryCacheUtil.generMemoryCache(ratio);

		initData();			
		initView();
	}
	
	private void initData(){
		app = RssTabletApp.getInstance();
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
	}
		
	private void initView(){						
	
		int width = (int) (dm.widthPixels / 3.4f);				
		int statusBarHeight = DeviceInfoUtil.getStatusHeight(PhotoBooksThemeSelectActivity.this);
		int height = (int) ((dm.heightPixels - dm.density * 130  - statusBarHeight)/2f);
		
		mHListView = (HorizontalListView) findViewById(R.id.hlv_photobooks); 
		
		boolean isHideMyPictures = false;
		if (isFromProduct && isFromMyProject) {
			isHideMyPictures = true;
		}		
		adapter = new PhotobookThemeSelectionAdapter(this,width,height,mMemoryCache,isHideMyPictures);
		mHListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		
		selectPropmtText = (TextView) findViewById(R.id.select_propmt);
		selectPropmtText.setText(R.string.select_theme_propmt);
		previousButton = (Button) findViewById(R.id.previous_button);
		cartButton = (Button) findViewById(R.id.cart_button);
		cartButton.setOnClickListener(this);
		if (isFromProduct) {
			previousButton.setVisibility(View.INVISIBLE);
			cartButton.setText(R.string.Common_Done);
		}else {
			previousButton.setOnClickListener(this);
		}										
	}		
			
	@Override
	public void startOver() {
		adapter.cancelRequest();
		mHListView.setAdapter(null);
		super.startOver();
	}
	
	@Override
	public void judgeHaveItems(){
		if (!isFromProduct) return;
		PhotoBookProductUtil.dealWithItem(PhotoBooksThemeSelectActivity.this,wHRatio);
		adapter.cancelRequest();
		mHListView.setAdapter(null);	
		AppManager.getInstance().goToShoppingActivity();		
	}
	
	@Override
	protected void onPause() {			
		MemoryCacheUtil.evictAll(mMemoryCache);
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();				
		mMemoryCache = null;
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v.getId()==R.id.previous_button){			
			mHListView.setAdapter(null);
			Intent mIntent = new Intent(PhotoBooksThemeSelectActivity.this, PhotoBooksPicSelectActivity.class);
			startActivity(mIntent);			
			PhotoBooksThemeSelectActivity.this.finish();								
		}else if(v.getId()==R.id.cart_button ){			
			boolean isNotChangeTheme = false;
			if (isFromProduct) {
				if (curSelectedPosition == -1) {
					isNotChangeTheme = true;
				}else {
					if (curSelectedTheme != null) {
						if (currentPhotoBook.theme != null&& curSelectedTheme.id.equals(currentPhotoBook.theme)) {
							isNotChangeTheme = true;
						}
					}else if ("".equals(currentPhotoBook.theme)){
						isNotChangeTheme = true;
					}
				}			
			}
						
			if (isNotChangeTheme) {
				Intent mIntent = new Intent(PhotoBooksThemeSelectActivity.this, PhotoBooksProductActivity.class);	
				mIntent.putExtra("isNotChangeTheme", true);
				PhotoBooksThemeSelectActivity.this.setResult(RESULT_OK,mIntent);
				adapter.cancelRequest();
				mHListView.setAdapter(null);
				PhotoBooksThemeSelectActivity.this.finish();	
			}else {								
				if (((curSelectedPosition == 0 &&  curSelectedTheme == null)|| curSelectedTheme != null)) {	
					adapter.cancelRequest();					
					ShoppingCartUtil.judgeImageDownload(PhotoBooksThemeSelectActivity.this,true,false);
					if (isFromProduct) {						
						SetPhotoBookParamsTask setParamsTask = new SetPhotoBookParamsTask(PhotoBooksThemeSelectActivity.this,null,curSelectedTheme,true,null);
						setParamsTask.execute();		
					}else {	
						int size = currentPhotoBook.chosenpics.size();
						if (UploadProgressUtil.getUploadPicSuccessNum(currentPhotoBook.chosenpics,true) < size ){
							//1.upload all images							
							ShoppingCartUtil.judgeImageUpload(PhotoBooksThemeSelectActivity.this,true,false);		
							UploadImagesTask uploadImagesTask = new UploadImagesTask(PhotoBooksThemeSelectActivity.this,curSelectedTheme,false);
							uploadImagesTask.execute();	
						}else {
							String backCoverResourceId = null;
							String facebookId = SharedPreferrenceUtil.getFacebookUserId(PhotoBooksThemeSelectActivity.this);					
							ArrayList<String> imageResources = new ArrayList<String>(size);
							for (ImageInfo info : currentPhotoBook.chosenpics) {
								if (facebookId != null && !"".equals(facebookId) && facebookId.equals(info.id)) {
									if (info.imageThumbnailResource != null) {
										backCoverResourceId = info.imageThumbnailResource.id;
									}					
								}				
								if (info.imageThumbnailResource != null) {
									imageResources.add(info.imageThumbnailResource.id);
								}		
							}
							SetPhotoBookParamsTask setParamsTask = new SetPhotoBookParamsTask(PhotoBooksThemeSelectActivity.this,imageResources,curSelectedTheme,isFromProduct,backCoverResourceId);
							setParamsTask.execute();	
						}						
					}					
				}else {
					String prompt = getResources().getString(R.string.want_theme_propmt);
					android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();			
						}		
					};		
					new InfoDialog.Builder(PhotoBooksThemeSelectActivity.this).setMessage(prompt)			
					.setNegativeButton(R.string.d_yes, yesOnClickListener)
					.create()
					.show();		
				}				
			}
		}
	}	
			
}
