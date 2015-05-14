package com.kodak.rss.tablet.thread;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.text.Font;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.activities.PhotoBooksThemeSelectActivity;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class SetPhotoBookParamsTask extends AsyncTask<String, Void, Object>{
	private static final String TAG = "SetPhotoBookParamsTask:";	
	private Context mContext;	
	private InfoDialog waitingDialog;
	
	private Photobook  photobook;
	private ArrayList<String> imageResources;
	private Theme mSelectedTheme;
	private boolean isFromProduct;	
	private String backCoverResourceId;
	
	public SetPhotoBookParamsTask(Context context,ArrayList<String> imageResources,Theme selectedTheme ,boolean fromProduct,String backCoverResourceId){
		this.mContext = context;		
		this.imageResources = imageResources;
		this.mSelectedTheme = selectedTheme;
		this.isFromProduct = fromProduct;
		this.backCoverResourceId = backCoverResourceId;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.i(TAG, "Start load Themes ...");				
		String displayStr = "";
		if (isFromProduct) {
			displayStr = mContext.getString(R.string.Common_Wait);
		}else {
			displayStr = mContext.getString(R.string.Task_CreatingPhotobook);
		}		
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(displayStr)
		.setProgressBar(true)				
		.create();
		waitingDialog.show();						
	}
	
	@Override
	protected Object doInBackground(String... params) {	
		photobook = PhotoBookProductUtil.getCurrentPhotoBook();
		if (photobook == null) return null;
		PhotobookWebService pbService = new PhotobookWebService(mContext);
		boolean isSetBackCover = false;		
		if(PhotoBookProductUtil.isBackCoverPageBlank(photobook) && backCoverResourceId != null){
			isSetBackCover = true;
		}
		
		Photobook  mPhotobook = null;	
		try {
			if (isFromProduct) {
				if (mSelectedTheme != null) {					
					mPhotobook = pbService.setPhotobookThemeTask(photobook.id, mSelectedTheme.id);
				}else {				
					//select native theme 
					mPhotobook = pbService.setPhotobookThemeTask(photobook.id, "MyPictures");
				}		
				return mPhotobook;
			}
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}	
		
		try {	
			//1 add resources
			String titleId = imageResources.get(0);		
			imageResources.remove(0);		
			if (isSetBackCover) {
				imageResources.remove(backCoverResourceId);
			}
		
			if (mSelectedTheme != null) {
				mPhotobook = pbService.buildPhotobookTask(photobook.id, mSelectedTheme.id, imageResources);
				if (mPhotobook == null) return null;
			}else {
				mPhotobook = pbService.buildPhotobookTask(photobook.id, "MyPictures",imageResources);
				if (mPhotobook == null) return null;			
			}
			
			//2. set dealPage
			if(mPhotobook != null){
				//set title page
				PhotobookPage titlePage = PhotoBookProductUtil.getTitlePage(mPhotobook);
				if (titlePage != null){
					pbService.addImageToPageTask(mPhotobook.id, titlePage, titleId);
				}
				//set backcover page
				if(isSetBackCover){
					PhotobookPage backCover = PhotoBookProductUtil.getBackCoverPage(mPhotobook);
					if (backCover != null) {
						pbService.addImageToPageTask(mPhotobook.id, backCover, backCoverResourceId);
					}				
				}
			}
			
			//3. layout photobook
			mPhotobook = pbService.layoutPhotobookTask(mPhotobook.id);
	    	if(mPhotobook == null)return null;
	    	
	    	//4. get fonts
	    	if(RssTabletApp.getInstance().fonts == null){
	    		WebService webService = new WebService(mContext);
	    		List<Font> fonts;
	    		fonts = webService.getAvailableFontsTask(RssTabletApp.getInstance().getCurrentLanguage());
	    		RssTabletApp.getInstance().fonts = fonts;
	    	}
			return mPhotobook;
		} catch (RssWebServiceException e) {		
			if (e!= null && e.getCode()!= null && !"".equals(e.getCode())) {
				try {
					Photobook book = pbService.createPhotobookTask(photobook.proDescId);
					if (book != null) {
						RssTabletApp app = RssTabletApp.getInstance();
						if (app.chosenBookList == null) {
							app.chosenBookList = new ArrayList<Photobook>(); 
						}
						synchronized (app.chosenBookList){														
							PhotoBookProductUtil.addCurrentPhotoBook(book);
							book.chosenpics = photobook.chosenpics;
							app.chosenBookList.remove(photobook);	
						}
						
						if (app.products == null) {
							app.products = new ArrayList<ProductInfo>();
						}
						synchronized (app.products){
							PhotoBookProductUtil.dealWithItem(mContext,0);
							ProductInfo delInfo = null;
							for(ProductInfo pInfo : app.products) {
								if (pInfo != null && AppConstants.bookType.equals(pInfo.productType)) {				
									if (pInfo.correspondId.equals(photobook.id) ) {												
										delInfo = pInfo;
										break;												
									}
								}
							}									
							app.products.remove(delInfo);
						}						
					}						
				} catch (RssWebServiceException e1) {				
					e1.printStackTrace();
					return e1;
				}
			}else {
				e.printStackTrace();
			}
			return e;
		}
		
	}

	@Override
	protected void onPostExecute(Object  result) {
		super.onPostExecute(result);
		if(mContext != null && !((Activity)mContext).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){				
				waitingDialog.dismiss();	             		
			}
		
			if(result instanceof RssWebServiceException){
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			}else if(result == null){
				Log.e(TAG, "SetPhotoBookParamsTask failed.");				
			} else {
				Log.i(TAG, "SetPhotoBookParamsTask succeed.");
								
				PhotoBookProductUtil.setCurrentPhotoBook((Photobook) result);				
				if (isFromProduct) {								
					Intent mIntent = new Intent(mContext, PhotoBooksProductActivity.class);	
					mIntent.putExtra("isNotChangeTheme", false);
					((PhotoBooksThemeSelectActivity)mContext).setResult(((PhotoBooksThemeSelectActivity)mContext).RESULT_OK,mIntent);	
				}else {
					Intent mIntent = new Intent(mContext, PhotoBooksProductActivity.class);
					mIntent.putExtra("justCreated", true);
					mContext.startActivity(mIntent);
				}

				if (mContext instanceof PhotoBooksThemeSelectActivity) {					
					((PhotoBooksThemeSelectActivity)mContext).mHListView.setAdapter(null);
				}
				System.gc();				
				((Activity)mContext).finish();	
			}
		}
	}

}	