package com.kodak.rss.tablet.services;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.upload.ImageResource;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.SplashPageActivity;
import com.kodak.rss.tablet.util.UploadProgressUtil;

public class UploadThumbnailPicTask implements Runnable{
	
	public  String FIRST_UPLOAD_THUMBNAIL = "FIRST_UPLOAD_THUMBNAIL";
	public  String uploadPicAction ="com.kodak.rss.tablet.action.upload";	
	private ImageInfo mPbcp;
	public  String uploadingURI;
	public  String contentId;
	private Context mContext;
	private NotificationManager mManager;
	private Notification mNotification;
	private PhotobookWebService service = null;
	private int mSuccessNum ;
	private int mUploadTotal;
	private String flowType;
	private String productId;
	private String proDescriptionId;	
	
	RssTabletApp app;
	public UploadThumbnailPicTask(Context context, NotificationManager manager, Notification notification, ImageInfo pbcp, String flowType, String productId,String proDescriptionId){
		this.mContext = context;
		this.mManager = manager;
		this.mNotification = notification;
		this.mPbcp = pbcp;	
		this.flowType = flowType;
		this.productId = productId;
		this.proDescriptionId = proDescriptionId;	
		app = RssTabletApp.getInstance();
		service = new PhotobookWebService(context);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {				
		if(mPbcp.uploadThumbnailUrl.startsWith(FIRST_UPLOAD_THUMBNAIL)) return;		
		uploadingURI = mPbcp.uploadThumbnailUrl;					
		contentId = "";
		mPbcp.uploadThumbnailUrl = FIRST_UPLOAD_THUMBNAIL + uploadingURI;
		
		ImageResource result = null;
		if (service == null) {
			service = new PhotobookWebService(mContext);			
		}	
		
		try {
			if (!mPbcp.isfromNative && mPbcp.origHeight > 0 && mPbcp.origWidth > 0){			
				result = service.addImageFromWebTask(uploadingURI,mPbcp.origHeight,mPbcp.origWidth);				
			}else {
				result = service.uploadImageTask(uploadingURI,contentId, true, mPbcp.isfromNative, flowType, proDescriptionId);	
			}
		} catch (RssWebServiceException e) {
			e.printStackTrace();
		}	
		
		mPbcp.isHavedThumbnailUpload = true;	       
		mPbcp.imageThumbnailResource = result;		
		if (!mPbcp.isfromNative && mPbcp.origHeight > 0 && mPbcp.origWidth > 0) {			
			mPbcp.isHavedOriginalUpload = true;	       	 
			mPbcp.imageOriginalResource = result;	
		}
		try {
			List<ImageInfo>  imageInfoList =  UploadProgressUtil.allImages();
			mUploadTotal = imageInfoList.size();	
			mSuccessNum = UploadProgressUtil.getUploadPicSuccessNum(imageInfoList,true);	
			
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
		if (mPbcp.imageThumbnailResource == null) {
			intent.putExtra(AppConstants.PicUploadFailFlag, true);			
		}else {
			intent.putExtra(AppConstants.PicUploadFailFlag, false);
		}
		intent.putExtra(AppConstants.PicUploadSuceessId, mPbcp.id);		
		intent.putExtra(AppConstants.isThumbnail, true);
		intent.putExtra(AppConstants.productId, productId);
		intent.putExtra(AppConstants.flowType, flowType);
		mContext.sendBroadcast(intent);				
		
	}

}
