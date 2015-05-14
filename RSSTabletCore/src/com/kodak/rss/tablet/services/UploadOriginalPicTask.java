package com.kodak.rss.tablet.services;

import java.util.Date;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.upload.ImageResource;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.SplashPageActivity;
import com.kodak.rss.tablet.util.UploadProgressUtil;

public class UploadOriginalPicTask implements Runnable{
	
	public  String FIRST_UPLOAD_ORIGINAL = "FIRST_UPLOAD_ORIGINAL";
	public  String uploadPicAction ="com.kodak.rss.tablet.action.upload";	
	private ImageInfo mImageInfo;
	public  String uploadingURI;
	public  String contentId;
	private Context mContext;
	private NotificationManager mManager;
	private Notification mNotification;
	private WebService service = null;
	private int mSuccessNum ;
	private int mUploadTotal;
	private String flowType;
	private String productId;
	private String proDescriptionId;	
	
	RssTabletApp app;
	public UploadOriginalPicTask(Context context, NotificationManager manager, Notification notification, ImageInfo imageInfo, String flowType, String productId,String proDescriptionId){
		this.mContext = context;
		this.mManager = manager;
		this.mNotification = notification;
		this.mImageInfo = imageInfo;
		this.flowType = flowType;
		this.productId = productId;
		this.proDescriptionId = proDescriptionId;	
		app = RssTabletApp.getInstance();
		service = new WebService(context);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {			
		if(mImageInfo.uploadOriginalUrl.startsWith(FIRST_UPLOAD_ORIGINAL)) return;
		uploadingURI = mImageInfo.uploadOriginalUrl;
		contentId = "";	
		if (mImageInfo.imageThumbnailResource != null) {
			contentId = mImageInfo.imageThumbnailResource.id;
		}		
		mImageInfo.uploadOriginalUrl = FIRST_UPLOAD_ORIGINAL + uploadingURI;
		
		ImageResource result = null;
		if (service == null) {
			service = new WebService(mContext);
		}	
		try {
			if (!mImageInfo.isfromNative && mImageInfo.origHeight > 0 && mImageInfo.origWidth > 0){			
				result = service.addImageFromWebTask(uploadingURI,mImageInfo.origHeight,mImageInfo.origWidth);				
			}else {
				result = service.uploadImageTask(uploadingURI,contentId, false, mImageInfo.isfromNative, flowType, proDescriptionId);	
			}
		} catch (RssWebServiceException e) {
			e.printStackTrace();
		}			
			
		mImageInfo.isHavedOriginalUpload = true;	      	
		mImageInfo.imageOriginalResource = result;	
		if (result != null) {							
			mSuccessNum ++;	
			mImageInfo.uploadOriginalSucTime = new Date().getTime();
            if (app.products != null) {    
            	for(ProductInfo pInfo : app.products) {
            		if (pInfo != null && AppConstants.printType.equals(pInfo.productType) && pInfo.correspondId == null && pInfo.chosenImageList!= null ) {
            			ImageInfo choseIamgeInfo = pInfo.chosenImageList.get(0);
            			if (choseIamgeInfo.id.equals(mImageInfo.id)) {           
            				pInfo.correspondId = mImageInfo.imageOriginalResource.id;            
            			}
            		}
               } 
            }
		} 

		try {
			List<ImageInfo>  imageInfoList =  UploadProgressUtil.allImages();
			mUploadTotal = imageInfoList.size();	
			mSuccessNum = UploadProgressUtil.getUploadPicSuccessNum(imageInfoList,false);
			
			//let click notify will return our activity
			Intent notificationIntent = new Intent(mContext,SplashPageActivity.class);
			notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            
			mNotification.setLatestEventInfo(mContext,mContext.getString(R.string.app_name),
					mContext.getString(R.string.upload) + " "+ mSuccessNum + "/" + mUploadTotal,PendingIntent.getActivity(mContext, 0,notificationIntent, 0));
			(mManager == null ? (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE): mManager).notify(0, mNotification);
		} catch (Exception ex) {

		}		
		
		Intent intent=new Intent(uploadPicAction);		
		if (mImageInfo.imageOriginalResource == null) {
			intent.putExtra(AppConstants.PicUploadFailFlag, true);			
		}else {
			intent.putExtra(AppConstants.PicUploadFailFlag, false);
		}
		intent.putExtra(AppConstants.PicUploadSuceessId, mImageInfo.id);	
		intent.putExtra(AppConstants.isThumbnail, false);
		intent.putExtra(AppConstants.productId, productId);	
		intent.putExtra(AppConstants.flowType, flowType);		
		mContext.sendBroadcast(intent);						
	}

}
