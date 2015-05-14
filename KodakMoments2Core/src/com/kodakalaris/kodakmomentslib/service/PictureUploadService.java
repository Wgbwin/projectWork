package com.kodakalaris.kodakmomentslib.service;

import java.util.Iterator;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.AppConstants.FlowType;
import com.kodakalaris.kodakmomentslib.AppConstants.PhotoUploadingState;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.upload.ImageResource;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.util.UploadProgressUtil;

public class PictureUploadService extends Service{
	private static final String TAG = "PictureUploadService :";
	public static boolean mTerminated = false;	
	public static boolean isRunning = true;
	private GeneralAPI mGeneralAPI;
	Object RSS = new Object();
	private NotificationManager mManager;
	private Notification mNotification;
	
	
	
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "PictureUploadService onCreate");
		mTerminated = false;	
		isRunning = true;	
		mGeneralAPI = new GeneralAPI(PictureUploadService.this);
		
		
		
		mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = new Notification(R.drawable.icon,"Notify", System.currentTimeMillis());	
	}
	
	
	@Override
	public void onDestroy() {
		isRunning = false;
		mTerminated = true;			
		UploadImagesThread.mUploadImagesThread = null;		
		super.onDestroy();
		Log.w(TAG, "onDestroy() complete");
	}
	
	
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (UploadImagesThread.mUploadImagesThread != null && UploadImagesThread.mUploadImagesThread.isAlive()) {
			
		}else {
			UploadImagesThread.getInstance(new Runnable() {
				@Override
				public void run() {
					while (isRunning) {
						if (mTerminated) {
							Log.d(TAG,
									"stop  UploadImagesThread sleep 3 seconds");
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {
							Log.d(TAG, "UploadImagesThread start run");
							if (KM2Application.getInstance().getFlowType()
									.isPrintWorkFlow()) {

								uploadOriginalImageInPrint();
								uploadThumbnailImageInPhotoBook();
								uploadOriginalImageInPhotoBook();
								uploadOriginalImageInCard();
								uploadOriginalImageInCollage();

							} else if (KM2Application.getInstance()
									.getFlowType().isPhotoBookWorkFlow()) {

								uploadThumbnailImageInPhotoBook();
								uploadOriginalImageInPhotoBook();
								uploadOriginalImageInPrint();
								uploadOriginalImageInCard();
								uploadOriginalImageInCollage();

							} else if (KM2Application.getInstance()
									.getFlowType().isGreetingCardWorkFlow()) {

								uploadOriginalImageInCard();
								uploadOriginalImageInPrint();
								uploadThumbnailImageInPhotoBook();
								uploadOriginalImageInPhotoBook();
								uploadOriginalImageInCollage();

							} else if (KM2Application.getInstance()
									.getFlowType().isCollageWorkFlow()) {

								uploadOriginalImageInCollage();
								uploadOriginalImageInPrint();
								uploadThumbnailImageInPhotoBook();
								uploadOriginalImageInPhotoBook();
								uploadOriginalImageInCard();
							}
							
							
							try {
								Thread.sleep(3000);
								synchronized (RSS) {
									List<PhotoInfo> allPhotos =  UploadProgressUtil.getAllUploadImages(getApplicationContext()); 
									if( allPhotos!=null && allPhotos.size()>0 ){
										long lastUploadTime = UploadProgressUtil.getLastUploadTime(allPhotos);	
										if (System.currentTimeMillis()- lastUploadTime > 2*1000*60) {// 2*1000*60
											PictureUploadService.mTerminated = true;
											stopSelf();
										}	
										
									}else{
										PictureUploadService.mTerminated = true;
										stopSelf();
									}
									
								}
								
								
							} catch (InterruptedException e) {
								
								e.printStackTrace();
							}

						}

					}
				}
					
			}).start();
		
		}
	
	
		return START_STICKY;
		
	}
	

	private void uploadOriginalImageInPrint(){
		Log.d(TAG, "uploadOriginalImageInPrint start");
		List<PhotoInfo> photos = PrintManager.getInstance(getApplicationContext()).getmPrintPhotos();
		if(photos!=null && photos.size()>0){
			uploadOriginalImage(photos,"");	
		}
		
		Log.d(TAG, "uploadOriginalImageInPrint end");
    }
	
	//TODO @SUNNY
	private void uploadThumbnailImageInPhotoBook() {
		
	}
	//TODO @SUNNY
	private void uploadOriginalImageInPhotoBook() {
		
	}
	//TODO @SUNNY
	private void uploadOriginalImageInCard(){
		
	}
	//TODO @SUNNY
	private void uploadOriginalImageInCollage(){
		
	}
	
	//TODO @SUNNY
    private void uploadThumbnailImage(){
		
	}
    

	private void uploadOriginalImage(List<PhotoInfo> photos,String descriptionId){
		boolean isPhotoWaitToUpload = checkWantUpload(photos,false);
		if (isPhotoWaitToUpload) {
			Iterator<PhotoInfo> itor = photos.iterator();
			while (itor.hasNext() && isPhotoWaitToUpload) {
				if (mTerminated || !isRunning){
					return;		
				}
				
				PhotoInfo photo = null;
				try {
					photo = (PhotoInfo) itor.next();
				} catch (Exception e) {
					Log.w(TAG, e.toString());
					break;
				}

				
				if(photo==null){
					continue;
				}
				
				if(photo.getImageResource()!=null && !photo.getPhotoUploadingState().isInital()){
					continue;
				}
				
				if(photo.getFlowType().isPhotoBookWorkFlow() && !photo.isThumbnailUploaded()){
					continue;
				}
				
				//do upload original photo
				if(mGeneralAPI==null){
					mGeneralAPI = new GeneralAPI(getApplicationContext());
				}
				ImageResource imageResource = null; 
				try {
					photo.setPhotoUploadingState(PhotoUploadingState.UPLOADING);
					
					Intent intent=new Intent(AppConstants.UPLOAD_PHOTO_ACTION);	
					intent.putExtra(AppConstants.UPLOAD_PHOTO_FLAG, photo);
					PictureUploadService.this.sendBroadcast(intent);
					if(photo.getPhotoSource().isFromPhone()){
						imageResource = mGeneralAPI.uploadImageTask(photo, false, descriptionId);
					}else {
						imageResource = mGeneralAPI.addImageFromWebTask(photo, photo.getWidth(), photo.getHeight());
					}
				} catch (WebAPIException e) {
					e.printStackTrace();
				}

				//....
				if(imageResource!=null){
					photo.setImageResource(imageResource);
					photo.setPhotoUploadingState(PhotoUploadingState.UPLOADED_SUCCESS);
					photo.setUploadOriginalTime(System.currentTimeMillis());
					
					
				}else {
					photo.setPhotoUploadingState(PhotoUploadingState.UPLOADED_FAILED);
					photo.setUploadOriginalTime(System.currentTimeMillis());
					
				}
				Intent intent=new Intent(AppConstants.UPLOAD_PHOTO_ACTION);	
				intent.putExtra(AppConstants.UPLOAD_PHOTO_FLAG, photo);
				PictureUploadService.this.sendBroadcast(intent);
				
				
				
				//TODO
//				List<PhotoInfo> allPhotos = UploadProgressUtil.getAllUploadImages(getApplicationContext());
//				int mSuccessNum = UploadProgressUtil.getUploadPicSuccessNum(allPhotos);
//				Intent notificationIntent = new Intent(PictureUploadService.this,SplashActivity.class);
//				notificationIntent.setAction(Intent.ACTION_MAIN);
//	            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//				mNotification.setLatestEventInfo(mContext,mContext.getString(R.string.app_name),
//						mContext.getString(R.string.upload) + " "+ mSuccessNum + "/" + mUploadTotal,PendingIntent.getActivity(mContext, 0,notificationIntent, 0));
//				(mManager == null ? (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE): mManager).notify(0, mNotification);

				
				FlowType  flowType =  KM2Application.getInstance().getFlowType();
				if(flowType != photo.getFlowType()){
					break ;
				}
				
				
				isPhotoWaitToUpload = checkWantUpload(photos,false);
			}
		}
	}
	
	/**
	 * 
	 * @param list
	 * @param isThumbnail
	 * @return
	 */
	private boolean checkWantUpload(List<PhotoInfo> list,boolean isThumbnail){
		boolean isWant = false;
		if (list==null) return false;
		for (int i = 0; i < list.size(); i++) {
			PhotoInfo photoInfo = list.get(i);
			if (isThumbnail) {
				if(photoInfo!=null && photoInfo.getImageResource()==null && !photoInfo.isThumbnailUploaded()){
					isWant = true ;
					break;
				}
				
				
			}else {
				if(photoInfo!=null && (photoInfo.getImageResource()==null && photoInfo.getPhotoUploadingState().isInital())){
					isWant = true ;
					break;
				}
				
				
			}
		}
		return isWant;
	}
	
	
	public static class UploadImagesThread extends Thread {
		public volatile static UploadImagesThread mUploadImagesThread;		
		private UploadImagesThread(Runnable runnable) {
			super(runnable);
		}

		public static UploadImagesThread getInstance(Runnable runnable) {
			if (mUploadImagesThread == null) {
				synchronized (UploadImagesThread.class) {
					if (mUploadImagesThread == null) {
						mUploadImagesThread = new UploadImagesThread(runnable);
					}
				}
			}
			return mUploadImagesThread;
		}

	}
}
