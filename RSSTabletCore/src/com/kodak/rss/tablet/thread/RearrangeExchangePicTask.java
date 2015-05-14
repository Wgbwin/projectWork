package com.kodak.rss.tablet.thread;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class RearrangeExchangePicTask extends AsyncTask<String, Void, Object>{

	private String TAG = "RearrangeExchangePicTask :";		
	private Photobook currentPhotoBook;		
	private Layer layer;
	private Layer delLayer;
	private String mLayerId;
	private String delLayerId;
	private int layerPosition;
	private int addPos;
	private PhotobookPage fromPage;
	private PhotobookPage toPage;
	private Context mContext;		
	private InfoDialog waitingDialog;
	private int action;
	
	public final static int AddPic = 1;
	public final static int RemovePic = 2;
	public final static int ExchangePic = 3;
	private Layer giveUpLayer;
	
	public RearrangeExchangePicTask(Context mContext,PhotobookPage fromPage, PhotobookPage toPage, Layer layer,int layerPosition,int addPos, Layer delLayer,int actionFlag) {
		this.mContext = mContext;			
		this.fromPage = fromPage;
		this.toPage = toPage;
		this.layer = layer;
		if (layer != null) {
			this.mLayerId = layer.contentId;
		}
		this.action = actionFlag;
		this.layerPosition = layerPosition;
		this.addPos = addPos;
		this.delLayer = delLayer;		
		if (delLayer != null) {
			this.delLayerId = delLayer.contentId;
		}	
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();	
		
		giveUpLayer = null;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();		
		if (toPage != null && (PhotoBookProductUtil.isTitlePage(toPage) || PhotoBookProductUtil.isBackCoverPageBlank(toPage))) {
			if (delLayerId != null && delLayerId.equals(mLayerId)) return ;
		}
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait)
		.setProgressBar(true)				
		.create();
		waitingDialog.show();				
	}
	
	private boolean isDealGiveUp = false;
	
	@Override
	protected Object doInBackground(String... params) {		
		Log.d(TAG, "removePhotoFromPageTask LayerId:"+mLayerId);
		isDealGiveUp = false;
		PhotobookWebService pbService = new PhotobookWebService(mContext);				
		switch (action) {		
		case AddPic:
			if (toPage == null ) return false;	
			if (mLayerId == null) return false;	
			
			if (PhotoBookProductUtil.isTitlePage(toPage) || PhotoBookProductUtil.isBackCoverPageBlank(toPage)) {	
				if (delLayerId != null && delLayerId.equals(mLayerId)) return false;					
				if (delLayerId != null && !"".equals(delLayerId)) {
					try{
						PhotobookPage newPage = pbService.removePhotoFromPageTask(currentPhotoBook.id, delLayerId);
						if (newPage != null) {						
							PhotoBookProductUtil.updatePageInPhotobook(newPage, true);											
						}
					} catch (RssWebServiceException e) {
						e.printStackTrace();	
						if (toPage != null && toPage.layers != null) {
							toPage.layers[0] = delLayer;	
						}
						giveUpLayer = layer;
						isDealGiveUp = true;						
						return e;
					} 					
				}
				try{
					PhotobookPage newToPage = pbService.addImageToPageTask(currentPhotoBook.id, toPage, mLayerId);
					if (newToPage != null) {
						PhotoBookProductUtil.updatePageInPhotobook(newToPage, true);	
					}
				} catch (RssWebServiceException e) {
					e.printStackTrace();
					if (toPage != null && toPage.layers != null) {
						toPage.layers[0] = null;	
					}
					giveUpLayer = layer;
					isDealGiveUp = true;		
					return e;
				} 			
			}else {
				try {
					PhotobookPage newPage = pbService.addImageToPageTask(currentPhotoBook.id, toPage, mLayerId);
					if (newPage != null ) {
						PhotoBookProductUtil.updatePageInPhotobook(newPage, true);							
					}					
				} catch (RssWebServiceException e1) {				
					e1.printStackTrace();
					giveUpLayer = layer;
					isDealGiveUp = true;
					toPage.layers[addPos] = null;
					return e1;
				}
			}
			break;
		case RemovePic:	
			if (fromPage == null) return false;
			if (mLayerId == null) return false;				
			try {
				String fromPageId = fromPage.id;
				PhotobookPage newPage = pbService.removeContentFromPageTask(currentPhotoBook.id, fromPageId, mLayerId);
				if (newPage != null ) {
					PhotoBookProductUtil.updatePageInPhotobook(newPage, true);							
				}					
			} catch (RssWebServiceException e1) {				
				e1.printStackTrace();				
				PhotoBookProductUtil.insertLayers(fromPage, layerPosition, layer);
				giveUpLayer = layer;
				isDealGiveUp = true;			
				return e1;
			}
			break;

		case ExchangePic:
			if (toPage == null ) return false;		
			String fromPageId = fromPage.id;
			String toPageId = toPage.id;
			
			if (PhotoBookProductUtil.isTitlePage(toPage) || PhotoBookProductUtil.isBackCoverPageBlank(toPage)) {		
				if (delLayerId != null && delLayerId.equals(mLayerId)) return false;	
				List<PhotobookPage> pages = new ArrayList<PhotobookPage>(2);
				if (delLayerId != null && !"".equals(delLayerId)) {
					try{
						PhotobookPage page = pbService.removePhotoFromPageTask(currentPhotoBook.id, delLayerId);
						if (page != null) {
							pages.add(page);
						}
					} catch (RssWebServiceException e) {
						e.printStackTrace();	
						if (toPage != null && toPage.layers != null) {
							toPage.layers[0] = delLayer;	
						}
						giveUpLayer = delLayer;
						isDealGiveUp = true;		
						PhotoBookProductUtil.insertLayers(fromPage, layerPosition, layer);							
						return e;
					} 	
				}
				if (PhotoBookProductUtil.isInlayer(mLayerId)) {
					try{
						PhotobookPage newFromPage = pbService.removePhotoFromPageTask(currentPhotoBook.id, mLayerId);
						if (newFromPage != null) {
							int item = -1;
							for (int i = 0; i < pages.size(); i++) {
								PhotobookPage  dealPage = pages.get(i);
								if (dealPage != null && dealPage.id.equals(newFromPage.id)) {
									item = i;					
									break;
								}
							}
							if (item >= 0) {
								pages.set(item, newFromPage);		
							}else {
								pages.add(newFromPage);
							}	
						}
					} catch (RssWebServiceException e) {
						e.printStackTrace();	
						PhotoBookProductUtil.insertLayers(fromPage, layerPosition, layer);															
						return e;
					} 	
				}				       	
				PhotobookPage newToPage = null;
				try{
					newToPage = pbService.addImageToPageTask(currentPhotoBook.id, toPage, mLayerId);
					if (newToPage != null) {
						int item = -1;
						for (int i = 0; i < pages.size(); i++) {
							PhotobookPage  dealPage = pages.get(i);
							if (dealPage != null && dealPage.id.equals(newToPage.id)) {
								item = i;					
								break;
							}
						}
						if (item >= 0) {
							pages.set(item, newToPage);		
						}else {
							pages.add(newToPage);
						}	
					}
				} catch (RssWebServiceException e) {
					e.printStackTrace();																		
					return e;
				} 		
					
				for (int i = 0; i < pages.size(); i++) {
					PhotobookPage newPage = pages.get(i);
					if (newPage != null) {	
						PhotoBookProductUtil.updatePageInPhotobook(newPage, true);					
					}
				}
									
			}else if (PhotoBookProductUtil.getPhotobookPageEditable(toPage)) {			
				try{
					List<PhotobookPage> pages = null;
					pages = pbService.moveContentTask(currentPhotoBook.id, fromPageId, toPageId, mLayerId);
					if (pages != null) {			
						for (int i = 0; i < pages.size(); i++) {
							PhotobookPage newPage = pages.get(i);						
							if (newPage != null ) {
								PhotoBookProductUtil.updatePageInPhotobook(newPage, true);							
							}	
						}				
					}else {
						PhotoBookProductUtil.insertLayers(fromPage, layerPosition, layer);
						toPage.layers[addPos] = null;					
					}
				} catch (RssWebServiceException e) {
					e.printStackTrace();					
					PhotoBookProductUtil.insertLayers(fromPage, layerPosition, layer);
					toPage.layers[addPos] = null;							
					return e;
				} 	
			}						
			break;
		}
		Log.d(TAG, "RearrangeExchangePicTask succeed");	
		return true;		
	}

	@Override
	protected void onPostExecute(Object  result) {
		super.onPostExecute(result);
		 if (mContext != null && !((Activity)mContext).isFinishing()) {
			 if(waitingDialog != null && waitingDialog.isShowing()){					
				 waitingDialog.dismiss();            	
			 }		 		
		
			if(result instanceof RssWebServiceException){
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			} 
			if(mContext != null && !((Activity)mContext).isFinishing()){
				if (mContext instanceof PhotoBooksProductActivity) {			
					((PhotoBooksProductActivity)mContext).notifyPhotoBookPagesChanged();
				}	
				if (isDealGiveUp) {
					if (action == AddPic) {
						((PhotoBooksProductActivity)mContext).giveUpView.addLayer(giveUpLayer);
					}else {
						((PhotoBooksProductActivity)mContext).giveUpView.removeLayer(giveUpLayer);
					}				
				}			
			}
		 }
	}	

}
